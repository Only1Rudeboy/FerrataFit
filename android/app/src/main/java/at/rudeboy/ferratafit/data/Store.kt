package at.rudeboy.ferratafit.data

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import java.io.File

/**
 * Persistenz.
 *
 * Bewusst eine einzelne JSON-Datei statt einer Datenbank: Der Datenumfang bleibt auch nach
 * Jahren im niedrigen einstelligen Megabyte-Bereich, dafür ist der komplette Bestand
 * jederzeit als Text exportierbar und lesbar — praktisch fürs Backup.
 */
class Store(context: Context) {

    private val file = File(context.filesDir, "ferratafit.json")
    private val backupFile = File(context.filesDir, "ferratafit.backup.json")
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    private val _state = MutableStateFlow(load())
    val state: StateFlow<AppState> = _state.asStateFlow()

    private fun load(): AppState = try {
        if (file.exists()) json.decodeFromString<AppState>(file.readText())
        else if (backupFile.exists()) json.decodeFromString<AppState>(backupFile.readText())
        else AppState()
    } catch (_: Exception) {
        // Eine kaputte Datei darf den Start nicht verhindern — lieber leer beginnen
        // als die App gar nicht öffnen können.
        try {
            if (backupFile.exists()) json.decodeFromString<AppState>(backupFile.readText()) else AppState()
        } catch (_: Exception) {
            AppState()
        }
    }

    /** Zustand ändern und sofort schreiben. */
    fun update(block: (AppState) -> AppState) {
        val next = block(_state.value)
        _state.value = next
        scope.launch { persist(next) }
    }

    private fun persist(s: AppState) {
        try {
            if (file.exists()) file.copyTo(backupFile, overwrite = true)
            file.writeText(json.encodeToString(AppState.serializer(), s))
        } catch (_: Exception) {
            // Schreibfehler still schlucken: Der Zustand lebt im Speicher weiter,
            // der nächste Schreibversuch kommt beim nächsten Satz.
        }
    }

    fun exportJson(): String = json.encodeToString(AppState.serializer(), _state.value)

    fun importJson(text: String): Boolean = try {
        val parsed = json.decodeFromString<AppState>(text)
        _state.value = parsed
        scope.launch { persist(parsed) }
        true
    } catch (_: Exception) {
        false
    }
}
