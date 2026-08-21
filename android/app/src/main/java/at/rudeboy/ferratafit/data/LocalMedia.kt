package at.rudeboy.ferratafit.data

import android.content.Context
import android.net.Uri
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File
import java.util.zip.ZipInputStream

/**
 * Ein Medienpaket: Fotos und Topos, die der Nutzer selbst mitbringt.
 *
 * Die App bündelt keine fremden Bilder — aber sie kann ein Paket einlesen, das jemand
 * für sich zusammengestellt hat: eigene Fotos, Scans aus dem eigenen Führer, eine
 * Privatkopie der Tourenseiten. Was darin liegt, bleibt auf dem Gerät und geht die
 * App nichts an. Sie zeigt es nur dort, wo es hingehört: im Foto- und Topo-Reiter
 * des jeweiligen Steigs.
 *
 * Format: eine ZIP-Datei mit `index.json` und Bildern in Unterordnern je Steig-Kennung.
 * Die Kennungen sind die des Routenkatalogs (`FerrataRoutes`).
 */
@Serializable
data class LocalMediaItem(
    val file: String,
    val caption: String = "",
    val source: String = "",
    val origin: String = ""
)

@Serializable
data class LocalRouteMedia(
    val name: String = "",
    val photos: List<LocalMediaItem> = emptyList(),
    val topos: List<LocalMediaItem> = emptyList()
)

@Serializable
data class LocalMediaIndex(
    val created: String = "",
    val hinweis: String = "",
    val routes: Map<String, LocalRouteMedia> = emptyMap()
)

/** Das eingelesene Paket, aufgelöst auf absolute Pfade. */
data class MediaPack(
    val created: String,
    val routes: Map<String, LocalRouteMedia>,
    val dir: File
) {
    val photoCount: Int get() = routes.values.sumOf { it.photos.size }
    val topoCount: Int get() = routes.values.sumOf { it.topos.size }
    fun path(item: LocalMediaItem): File = File(dir, item.file)
    fun forRoute(id: String): LocalRouteMedia? = routes[id]
}

object LocalMedia {

    private val json = Json { ignoreUnknownKeys = true }

    fun dir(context: Context): File = File(context.filesDir, "medien")

    // Nur Einträge behalten, deren Datei wirklich da ist — ein halb kopiertes
    // Paket soll keine leeren Kacheln hinterlassen.
    fun load(context: Context): MediaPack? = loadDir(dir(context))

    /**
     * Entpackt ein gewähltes ZIP in den App-Ordner. Gibt die Zahl der Bilder zurück.
     *
     * Vorher wird der alte Bestand geleert — ein Paket ersetzt das vorige, sie werden
     * nicht gemischt. Und jeder Eintrag wird gegen „Zip Slip" geprüft: Ein Archiv mit
     * `../`-Pfaden darf nicht aus dem Ordner ausbrechen.
     */
    fun importZip(context: Context, uri: Uri): Result<Int> = runCatching {
        val input = context.contentResolver.openInputStream(uri)
            ?: throw IllegalStateException("Datei nicht lesbar")
        input.use { importZipStream(it, dir(context)) }
    }

    /**
     * Entpackt ein ZIP in den Zielordner. Gibt die Zahl der Bilder zurück.
     *
     * Vorher wird der alte Bestand geleert — ein Paket ersetzt das vorige, sie werden
     * nicht gemischt. Und jeder Eintrag wird gegen „Zip Slip" geprüft: Ein Archiv mit
     * `../`-Pfaden darf nicht aus dem Ordner ausbrechen. Getrennt vom Android-Teil,
     * damit genau diese Prüfung ohne Gerät testbar ist.
     */
    fun importZipStream(input: java.io.InputStream, target: File): Int {
        target.deleteRecursively()
        target.mkdirs()
        val canonicalTarget = target.canonicalPath
        var count = 0
        var hasIndex = false

        ZipInputStream(input.buffered()).use { zip ->
            var entry = zip.nextEntry
            while (entry != null) {
                val out = File(target, entry.name)
                if (!out.canonicalPath.startsWith(canonicalTarget + File.separator) &&
                    out.canonicalPath != canonicalTarget
                ) {
                    target.deleteRecursively()
                    throw SecurityException("Ungültiger Pfad im Paket: ${entry.name}")
                }
                if (entry.isDirectory) {
                    out.mkdirs()
                } else {
                    out.parentFile?.mkdirs()
                    out.outputStream().use { zip.copyTo(it) }
                    val lower = entry.name.lowercase()
                    if (lower == "index.json") hasIndex = true
                    else if (lower.endsWith(".jpg") || lower.endsWith(".jpeg") ||
                        lower.endsWith(".png") || lower.endsWith(".webp")
                    ) count++
                }
                zip.closeEntry()
                entry = zip.nextEntry
            }
        }

        if (!hasIndex) {
            target.deleteRecursively()
            throw IllegalArgumentException("Kein Medienpaket: index.json fehlt")
        }
        return count
    }

    /** Liest einen Ordner ein, ohne Android — für Prüfungen und Werkzeuge. */
    fun loadDir(d: File): MediaPack? {
        val idx = File(d, "index.json")
        if (!idx.exists()) return null
        return try {
            val parsed = json.decodeFromString<LocalMediaIndex>(idx.readText())
            val routes = parsed.routes.mapValues { (_, r) ->
                r.copy(
                    photos = r.photos.filter { File(d, it.file).exists() },
                    topos = r.topos.filter { File(d, it.file).exists() }
                )
            }.filterValues { it.photos.isNotEmpty() || it.topos.isNotEmpty() }
            MediaPack(parsed.created, routes, d)
        } catch (_: Exception) {
            null
        }
    }

    fun clear(context: Context) {
        dir(context).deleteRecursively()
    }
}
