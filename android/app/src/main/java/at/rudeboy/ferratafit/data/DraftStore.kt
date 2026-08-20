package at.rudeboy.ferratafit.data

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import java.io.File

/**
 * Speicher für den angefangenen Zustand.
 *
 * Eigene, kleine Datei neben dem Hauptbestand — die Begründung steht bei [Draft].
 *
 * Geschrieben wird bei jeder Änderung, auch bei jedem Antippen eines Steppers. Das klingt
 * viel, ist es aber nicht: Die Datei bleibt unter zwei Kilobyte, und nur so ist der Zustand
 * auch dann noch da, wenn Android die App eine Sekunde nach dem letzten Tippen wegräumt.
 * Ein verzögertes Schreiben würde genau die letzte Eingabe verlieren — also die, an die man
 * sich am ehesten erinnert.
 *
 * Ohne Sicherungskopie, anders als beim Hauptbestand: Geht diese Datei verloren, ist eine
 * angefangene Einheit weg. Ärgerlich, aber kein Datenverlust im eigentlichen Sinn — und eine
 * Kopie bei jedem Tastendruck wäre teurer als der Schaden.
 */
class DraftStore(context: Context) {

    private val file = File(context.filesDir, "ferratafit.draft.json")
    private val tmp = File(context.filesDir, "ferratafit.draft.tmp")

    /**
     * Genau ein Schreibfaden.
     *
     * Ein gehaltener Stepper löst fünf bis zehn Aufrufe je Sekunde aus. Ohne diese
     * Beschränkung liefen sie auf bis zu 64 Fäden gleichzeitig — und da [save] die Datei
     * erst kürzt und dann füllt, könnte der Zustand von vorgestern den von jetzt
     * überschreiben oder eine halb geschriebene Datei zurückbleiben.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO.limitedParallelism(1))

    /**
     * Schreibaufträge werden zusammengefasst: Kommen während eines Schreibvorgangs zehn
     * weitere Änderungen herein, wird am Ende nur die letzte geschrieben. Sie enthält
     * ohnehin alle vorherigen — der Entwurf ist immer der vollständige Stand.
     */
    private val pending = MutableStateFlow<Draft?>(null)

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    init {
        scope.launch {
            pending.collectLatest { draft -> if (draft != null) write(draft) }
        }
    }

    fun load(): Draft = try {
        // Eine liegengebliebene Nebendatei ist ein abgebrochener Schreibvorgang — weg damit
        if (tmp.exists()) tmp.delete()
        if (file.exists()) json.decodeFromString<Draft>(file.readText()) else Draft()
    } catch (_: Exception) {
        // Eine kaputte Entwurfsdatei darf den Start nicht verhindern. Im Zweifel
        // beginnt man ohne angefangene Einheit — schlimmer wäre eine App, die nicht startet.
        Draft()
    }

    fun save(draft: Draft) {
        pending.value = draft
    }

    /**
     * Erst in eine Nebendatei, dann umbenennen.
     *
     * Umbenennen ist innerhalb eines Dateisystems unteilbar. Geht dem Gerät mitten im
     * Schreiben der Strom aus, bleibt die vorherige, vollständige Fassung liegen — statt
     * einer halben, die beim nächsten Start nicht mehr lesbar wäre. Genau dieser Fall,
     * leerer Akku während des Trainings, ist der Grund für die ganze Datei.
     */
    private fun write(draft: Draft) {
        try {
            if (draft.isEmpty) {
                file.delete()
                tmp.delete()
                return
            }
            tmp.writeText(json.encodeToString(Draft.serializer(), draft))
            if (!tmp.renameTo(file)) {
                // Umbenennen kann auf manchen Geräten scheitern, wenn das Ziel existiert
                file.delete()
                tmp.renameTo(file)
            }
        } catch (_: Exception) {
            // Still schlucken: Der Zustand lebt im Speicher weiter, der nächste
            // Satz löst den nächsten Schreibversuch aus.
        }
    }

    fun clear() = save(Draft())
}
