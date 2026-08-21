package at.rudeboy.ferratafit.data

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.provider.MediaStore

/**
 * Die automatische Sicherung nach Documents/FerrataFit.
 *
 * Der Hauptbestand liegt im privaten App-Ordner — den nimmt Android beim
 * Deinstallieren mit, und an ihn kommt auch kein Dateimanager heran. Wer die App
 * versehentlich löscht oder das Gerät wechselt, hätte ohne diese Kopie Jahre an
 * Trainingsdaten verloren, nur weil er nie auf „Exportieren" getippt hat.
 *
 * Deshalb legt die App einmal pro Woche still eine Kopie in den Documents-Ordner.
 * Eine einzige Datei, die überschrieben wird — kein wachsender Stapel von
 * Schnappschüssen, den nie jemand aufräumt. Wer Verläufe will, hat den Export.
 *
 * Technik: MediaStore statt direktem Dateizugriff, weil Apps seit Android 10 nicht
 * mehr frei in Documents schreiben dürfen — über MediaStore dürfen sie es für
 * selbst angelegte Dateien ohne jede Berechtigung. Darunter (Android 8/9) bräuchte
 * es die alte Speicherberechtigung; die ist den stillen Komfort nicht wert, dort
 * gibt es die Sicherung schlicht nicht.
 */
object Backup {

    private const val WEEK_MS = 7 * 24 * 60 * 60 * 1000L
    private const val FILE_NAME = "FerrataFit-Sicherung.json"
    private const val FOLDER = "Documents/FerrataFit"

    /** Fällig, wenn die letzte Sicherung eine Woche her ist — oder nie eine lief. */
    fun due(lastBackupAt: Long, now: Long): Boolean =
        lastBackupAt <= 0L || now - lastBackupAt >= WEEK_MS

    val available: Boolean get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q

    /**
     * Schreibt die Sicherung. Gibt zurück, ob es geklappt hat — mehr Rückmeldung
     * braucht ein stiller Vorgang nicht: Beim nächsten App-Start wird es wieder
     * versucht, und der Export von Hand steht immer offen.
     */
    fun write(context: Context, json: String): Boolean {
        if (!available) return false
        return try {
            val resolver = context.contentResolver
            val collection = MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)

            // Gibt es die Datei schon? Dann überschreiben statt einen Stapel anzulegen.
            val existing = resolver.query(
                collection,
                arrayOf(MediaStore.MediaColumns._ID),
                "${MediaStore.MediaColumns.RELATIVE_PATH} = ? AND ${MediaStore.MediaColumns.DISPLAY_NAME} = ?",
                arrayOf("$FOLDER/", FILE_NAME),
                null
            )?.use { c -> if (c.moveToFirst()) c.getLong(0) else null }

            val uri = if (existing != null) {
                android.content.ContentUris.withAppendedId(collection, existing)
            } else {
                resolver.insert(collection, ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, FILE_NAME)
                    put(MediaStore.MediaColumns.MIME_TYPE, "application/json")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, FOLDER)
                }) ?: return false
            }

            // "wt" kürzt die Datei — sonst blieben Reste der alten, längeren Fassung stehen
            resolver.openOutputStream(uri, "wt")?.use { it.write(json.toByteArray()) } ?: return false
            true
        } catch (_: Exception) {
            false
        }
    }
}
