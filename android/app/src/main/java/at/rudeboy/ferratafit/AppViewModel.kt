package at.rudeboy.ferratafit

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import at.rudeboy.ferratafit.data.AppState
import at.rudeboy.ferratafit.data.Catalog
import at.rudeboy.ferratafit.data.Profile
import at.rudeboy.ferratafit.data.ProgressionKind
import at.rudeboy.ferratafit.data.Progression
import at.rudeboy.ferratafit.data.Session
import at.rudeboy.ferratafit.data.SetLog
import at.rudeboy.ferratafit.data.Station
import at.rudeboy.ferratafit.data.Store
import at.rudeboy.ferratafit.data.Suggestion
import at.rudeboy.ferratafit.health.HealthBridge
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

/** Ein Satz, während er noch getippt wird. */
data class ActiveSet(
    val weightKg: Double = 0.0,
    val reps: Int = 0,
    val seconds: Int = 0,
    val done: Boolean = false
)

/** Eine Übung innerhalb der laufenden Einheit. */
data class ActiveEntry(
    val suggestion: Suggestion,
    val sets: List<ActiveSet>
) {
    val exercise get() = suggestion.exercise
    val allDone: Boolean get() = sets.all { it.done }
    val doneCount: Int get() = sets.count { it.done }
}

/** Die laufende Trainingseinheit. */
data class ActiveWorkout(
    val dayId: String,
    val startedAt: Long,
    val entries: List<ActiveEntry>,
    val currentIndex: Int = 0
) {
    val day get() = Catalog.day(dayId)
    val totalSets: Int get() = entries.sumOf { it.sets.size }
    val doneSets: Int get() = entries.sumOf { it.doneCount }
    val progress: Float get() = if (totalSets == 0) 0f else doneSets.toFloat() / totalSets
}

/** Kurze Rückmeldung nach dem Speichern (Snackbar). */
data class Toast(val message: String, val isError: Boolean = false)

class AppViewModel(app: Application) : AndroidViewModel(app) {

    private val store = Store(app)
    val health = HealthBridge(app)

    val state: StateFlow<AppState> = store.state

    private val _active = MutableStateFlow<ActiveWorkout?>(null)
    val active: StateFlow<ActiveWorkout?> = _active.asStateFlow()

    private val _toast = MutableStateFlow<Toast?>(null)
    val toast: StateFlow<Toast?> = _toast.asStateFlow()

    private val _steps = MutableStateFlow<Long?>(null)
    val steps: StateFlow<Long?> = _steps.asStateFlow()

    fun clearToast() { _toast.value = null }
    fun notify(msg: String, error: Boolean = false) { _toast.value = Toast(msg, error) }

    // ---------------- Profil ----------------

    fun completeOnboarding(
        stations: Set<Station>,
        bodyweight: Double,
        plateStep: Double,
        targetDate: Long?,
        targetName: String
    ) = store.update { s ->
        s.copy(
            profile = s.profile.copy(
                stations = stations + Station.BODYWEIGHT,
                bodyweightKg = bodyweight,
                plateStepKg = plateStep,
                targetFerrataDate = targetDate,
                targetFerrataName = targetName,
                cycleStart = System.currentTimeMillis(),
                onboarded = true
            )
        )
    }

    fun updateProfile(block: (Profile) -> Profile) = store.update { it.copy(profile = block(it.profile)) }

    fun toggleStation(station: Station) = store.update { s ->
        val next = if (station in s.profile.stations) s.profile.stations - station
                   else s.profile.stations + station
        s.copy(profile = s.profile.copy(stations = next + Station.BODYWEIGHT))
    }

    fun toggleHiddenExercise(id: String) = store.update { s ->
        s.copy(hiddenExercises = if (id in s.hiddenExercises) s.hiddenExercises - id else s.hiddenExercises + id)
    }

    /** Startet den 5-Wochen-Block neu — sinnvoll nach einer längeren Pause. */
    fun restartCycle() = store.update { s ->
        s.copy(profile = s.profile.copy(cycleStart = System.currentTimeMillis()))
    }

    // ---------------- Training ----------------

    fun startWorkout(dayId: String) {
        val s = state.value
        val now = System.currentTimeMillis()
        val entries = Progression.suggestAll(Catalog.day(dayId), s, now).map { sug ->
            ActiveEntry(
                suggestion = sug,
                sets = List(sug.sets) {
                    ActiveSet(
                        weightKg = sug.weightKg,
                        reps = if (sug.exercise.progression == ProgressionKind.TIME) 0 else sug.targetReps,
                        seconds = sug.targetSeconds
                    )
                }
            )
        }
        _active.value = ActiveWorkout(dayId, now, entries)
    }

    fun selectExercise(index: Int) {
        _active.value = _active.value?.copy(currentIndex = index)
    }

    fun updateSet(entryIndex: Int, setIndex: Int, block: (ActiveSet) -> ActiveSet) {
        val w = _active.value ?: return
        val entries = w.entries.toMutableList()
        val entry = entries.getOrNull(entryIndex) ?: return
        val sets = entry.sets.toMutableList()
        val set = sets.getOrNull(setIndex) ?: return
        sets[setIndex] = block(set)
        entries[entryIndex] = entry.copy(sets = sets)
        _active.value = w.copy(entries = entries)
    }

    fun toggleSetDone(entryIndex: Int, setIndex: Int) =
        updateSet(entryIndex, setIndex) { it.copy(done = !it.done) }

    /** Übernimmt die Werte des abgehakten Satzes auf alle noch offenen Sätze der Übung. */
    fun applyToRemaining(entryIndex: Int, setIndex: Int) {
        val w = _active.value ?: return
        val entry = w.entries.getOrNull(entryIndex) ?: return
        val src = entry.sets.getOrNull(setIndex) ?: return
        val sets = entry.sets.mapIndexed { i, s ->
            if (i > setIndex && !s.done) s.copy(weightKg = src.weightKg, reps = src.reps, seconds = src.seconds)
            else s
        }
        val entries = w.entries.toMutableList().also { it[entryIndex] = entry.copy(sets = sets) }
        _active.value = w.copy(entries = entries)
    }

    fun cancelWorkout() { _active.value = null }

    /**
     * Beendet die Einheit: speichert lokal und schiebt sie — falls aktiviert —
     * über Health Connect zu Samsung Health.
     */
    fun finishWorkout(note: String = "") {
        val w = _active.value ?: return
        val now = System.currentTimeMillis()

        val logs = w.entries.flatMap { entry ->
            entry.sets.mapIndexedNotNull { i, s ->
                if (!s.done) null
                else SetLog(
                    exerciseId = entry.exercise.id,
                    setIndex = i,
                    weightKg = s.weightKg,
                    reps = s.reps,
                    seconds = s.seconds
                )
            }
        }

        if (logs.isEmpty()) {
            _active.value = null
            notify("Einheit verworfen — es war kein Satz abgehakt.")
            return
        }

        val session = Session(
            id = UUID.randomUUID().toString(),
            dayId = w.dayId,
            startedAt = w.startedAt,
            finishedAt = now,
            sets = logs,
            note = note
        )

        val lastLoads = logs.groupBy { it.exerciseId }
            .mapValues { (_, v) -> v.maxOf { it.weightKg } }

        store.update { s ->
            s.copy(
                sessions = s.sessions + session,
                lastLoads = s.lastLoads + lastLoads,
                // Der erste Abschluss setzt den Zyklusstart, damit die Wochenzählung stimmt.
                profile = if (s.profile.cycleStart <= 0L)
                    s.profile.copy(cycleStart = now) else s.profile
            )
        }
        _active.value = null

        val duration = session.durationMin
        notify("Gespeichert: ${logs.size} Sätze in $duration Minuten. Stark!")

        if (state.value.profile.healthConnectEnabled) {
            viewModelScope.launch {
                val title = "FerrataFit · ${Catalog.day(w.dayId).title}"
                health.writeSession(session, title)
                    .onSuccess { notify("An Samsung Health übergeben.") }
                    .onFailure { notify("Health Connect: ${it.message ?: "Übertragung fehlgeschlagen"}", true) }
            }
        }
    }

    fun deleteSession(id: String) = store.update { s ->
        s.copy(sessions = s.sessions.filterNot { it.id == id })
    }

    // ---------------- Health Connect ----------------

    fun setHealthEnabled(enabled: Boolean) {
        updateProfile { it.copy(healthConnectEnabled = enabled) }
        if (enabled) refreshSteps()
    }

    fun refreshSteps() {
        viewModelScope.launch { _steps.value = health.stepsToday() }
    }

    /** Schiebt alle bisherigen Einheiten nachträglich zu Health Connect. */
    fun syncAllToHealth() {
        viewModelScope.launch {
            val sessions = state.value.sessions
            if (sessions.isEmpty()) {
                notify("Es gibt noch keine Einheiten zum Übertragen.")
                return@launch
            }
            var ok = 0
            var failed = 0
            sessions.forEach { s ->
                health.writeSession(s, "FerrataFit · ${Catalog.day(s.dayId).title}")
                    .onSuccess { ok++ }
                    .onFailure { failed++ }
            }
            notify(
                if (failed == 0) "$ok Einheiten an Samsung Health übergeben."
                else "$ok übertragen, $failed fehlgeschlagen.",
                failed > 0
            )
        }
    }

    // ---------------- Sicherung ----------------

    fun exportJson(): String = store.exportJson()

    fun importJson(text: String) {
        if (store.importJson(text)) notify("Sicherung eingelesen.")
        else notify("Die Datei konnte nicht gelesen werden.", true)
    }
}
