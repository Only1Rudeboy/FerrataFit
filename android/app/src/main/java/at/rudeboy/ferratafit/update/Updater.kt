package at.rudeboy.ferratafit.update

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

/**
 * Selbstaktualisierung über GitHub.
 *
 * Die App liegt nicht im Play Store, also übernimmt sie das Update selbst: Sie fragt
 * die neueste Veröffentlichung ab, vergleicht die Versionsnummer, lädt bei Bedarf die
 * neue Fassung herunter und übergibt sie dem Paketinstallierer von Android.
 *
 * Das Netzwerk wird ausschließlich dafür genutzt. Trainingsdaten verlassen das Gerät nicht.
 */
object Updater {

    private const val REPO = "Only1Rudeboy/FerrataFit"
    private const val API = "https://api.github.com/repos/$REPO/releases/latest"
    const val RELEASES_PAGE = "https://github.com/$REPO/releases/latest"

    private val json = Json { ignoreUnknownKeys = true }

    /** Was bei der Prüfung herauskam. */
    sealed interface Result {
        /** Die installierte Fassung ist die neueste. */
        data class UpToDate(val current: String) : Result

        /** Es gibt etwas Neues. */
        data class Available(
            val version: String,
            val current: String,
            val downloadUrl: String,
            val notes: String,
            val sizeBytes: Long
        ) : Result

        /** Prüfung nicht möglich — kein Netz, GitHub gerade nicht erreichbar, o. ä. */
        data class Failed(val reason: String) : Result
    }

    /** Die Version der laufenden App, z. B. „1.2". */
    fun currentVersion(context: Context): String = runCatching {
        context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "?"
    }.getOrDefault("?")

    /**
     * Vergleicht zwei Versionsangaben der Form „1.2" oder „1.2.3".
     * Gibt einen Wert > 0 zurück, wenn [a] neuer ist als [b].
     */
    fun compareVersions(a: String, b: String): Int {
        fun parts(v: String) = v.trim().removePrefix("v")
            .split('.')
            .map { it.takeWhile(Char::isDigit).toIntOrNull() ?: 0 }

        val pa = parts(a)
        val pb = parts(b)
        for (i in 0 until maxOf(pa.size, pb.size)) {
            val d = (pa.getOrNull(i) ?: 0) - (pb.getOrNull(i) ?: 0)
            if (d != 0) return d
        }
        return 0
    }

    /** Fragt die neueste Veröffentlichung ab. Läuft im Hintergrund. */
    suspend fun check(context: Context): Result = withContext(Dispatchers.IO) {
        val current = currentVersion(context)
        try {
            val body = fetch(API) ?: return@withContext Result.Failed(
                "GitHub ist gerade nicht erreichbar."
            )
            val obj = json.parseToJsonElement(body).jsonObject
            val tag = obj["tag_name"]?.jsonPrimitive?.content
                ?: return@withContext Result.Failed("Die Antwort von GitHub war unvollständig.")
            val latest = tag.removePrefix("v")

            if (compareVersions(latest, current) <= 0) {
                return@withContext Result.UpToDate(current)
            }

            val asset = obj["assets"]?.jsonArray
                ?.map { it.jsonObject }
                ?.firstOrNull { it["name"]?.jsonPrimitive?.content?.endsWith(".apk") == true }
                ?: return@withContext Result.Failed(
                    "Zu Version $latest liegt keine installierbare Datei bereit."
                )

            Result.Available(
                version = latest,
                current = current,
                downloadUrl = asset["browser_download_url"]?.jsonPrimitive?.content.orEmpty(),
                notes = obj["body"]?.jsonPrimitive?.content.orEmpty().trim(),
                sizeBytes = asset["size"]?.jsonPrimitive?.content?.toLongOrNull() ?: 0L
            )
        } catch (e: Exception) {
            Result.Failed(e.message ?: "Die Prüfung ist fehlgeschlagen.")
        }
    }

    private fun fetch(url: String): String? {
        var conn: HttpURLConnection? = null
        return try {
            conn = (URL(url).openConnection() as HttpURLConnection).apply {
                connectTimeout = 12_000
                readTimeout = 12_000
                setRequestProperty("Accept", "application/vnd.github+json")
                setRequestProperty("User-Agent", "FerrataFit")
            }
            if (conn.responseCode !in 200..299) null
            else conn.inputStream.bufferedReader().use { it.readText() }
        } catch (_: Exception) {
            null
        } finally {
            conn?.disconnect()
        }
    }

    /**
     * Lädt die neue Fassung herunter und meldet den Fortschritt als Wert zwischen 0 und 1.
     * Gibt die fertige Datei zurück, oder null bei einem Fehler.
     */
    suspend fun download(
        context: Context,
        url: String,
        expectedBytes: Long,
        onProgress: (Float) -> Unit
    ): File? = withContext(Dispatchers.IO) {
        val dir = File(context.cacheDir, "updates").apply { mkdirs() }
        // Ältere Downloads wegräumen, damit der Zwischenspeicher nicht zuwächst
        dir.listFiles()?.forEach { it.delete() }
        val target = File(dir, "FerrataFit-update.apk")

        var conn: HttpURLConnection? = null
        try {
            conn = (URL(url).openConnection() as HttpURLConnection).apply {
                connectTimeout = 15_000
                readTimeout = 30_000
                instanceFollowRedirects = true
                setRequestProperty("User-Agent", "FerrataFit")
            }
            if (conn.responseCode !in 200..299) return@withContext null

            val total = if (expectedBytes > 0) expectedBytes else conn.contentLength.toLong()
            conn.inputStream.use { input ->
                target.outputStream().use { output ->
                    val buffer = ByteArray(64 * 1024)
                    var written = 0L
                    var lastReported = 0f
                    while (true) {
                        val read = input.read(buffer)
                        if (read < 0) break
                        output.write(buffer, 0, read)
                        written += read
                        if (total > 0) {
                            val p = (written.toFloat() / total).coerceIn(0f, 1f)
                            // Nur bei spürbarer Änderung melden — sonst flackert die Anzeige
                            if (p - lastReported >= 0.01f) {
                                lastReported = p
                                onProgress(p)
                            }
                        }
                    }
                }
            }
            onProgress(1f)
            if (target.length() > 0) target else null
        } catch (_: Exception) {
            target.delete()
            null
        } finally {
            conn?.disconnect()
        }
    }

    /** Darf die App eine Installation anstoßen? Ab Android 8 muss das eigens erlaubt sein. */
    fun canInstall(context: Context): Boolean =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.packageManager.canRequestPackageInstalls()
        } else true

    /** Führt zu der Systemeinstellung, in der man die Installation freigibt. */
    fun installPermissionIntent(context: Context): Intent =
        Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES)
            .setData(Uri.parse("package:${context.packageName}"))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

    /** Übergibt die heruntergeladene Datei an den Paketinstallierer. */
    fun install(context: Context, apk: File): Boolean = try {
        val uri = FileProvider.getUriForFile(
            context, "${context.packageName}.fileprovider", apk
        )
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
        true
    } catch (_: Exception) {
        false
    }

    /**
     * Kürzt die Veröffentlichungsnotizen auf etwas, das in einen Dialog passt.
     * Markdown-Auszeichnungen fallen dabei weg, Aufzählungen bleiben erhalten.
     */
    fun shortenNotes(notes: String, maxLines: Int = 12): String {
        val cleaned = notes.lineSequence()
            .map { line ->
                line.trim()
                    .replace(Regex("^#+\\s*"), "")
                    .replace(Regex("\\*\\*(.+?)\\*\\*"), "$1")
                    .replace(Regex("(?<!\\*)\\*(?!\\*)(.+?)(?<!\\*)\\*(?!\\*)"), "$1")
                    .replace(Regex("\\[(.+?)]\\(.+?\\)"), "$1")
                    .replace(Regex("^\\|.*\\|$"), "")
                    .replace(Regex("^-{3,}$"), "")
            }
            .filter { it.isNotBlank() }
            .toList()

        val kept = cleaned.take(maxLines)
        return kept.joinToString("\n") + if (cleaned.size > maxLines) "\n…" else ""
    }
}
