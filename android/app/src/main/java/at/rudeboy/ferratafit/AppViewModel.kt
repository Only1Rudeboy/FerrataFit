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
import at.rudeboy.ferratafit.data.SessionEdit
import at.rudeboy.ferratafit.data.SetLog
import at.rudeboy.ferratafit.data.Ascent
import at.rudeboy.ferratafit.data.Badge
import at.rudeboy.ferratafit.data.Ferrata
import at.rudeboy.ferratafit.data.Body
import at.rudeboy.ferratafit.data.BodyImport
import at.rudeboy.ferratafit.data.BodyMeasurement
import at.rudeboy.ferratafit.data.BadgeSnapshot
import at.rudeboy.ferratafit.data.Journey
import at.rudeboy.ferratafit.data.Stage
import at.rudeboy.ferratafit.data.StageKind
import at.rudeboy.ferratafit.data.StageLog
import at.rudeboy.ferratafit.data.Station
import at.rudeboy.ferratafit.data.Stats
import at.rudeboy.ferratafit.data.Store
import at.rudeboy.ferratafit.data.Suggestion
import at.rudeboy.ferratafit.data.fmtKg
import at.rudeboy.ferratafit.health.HealthBridge
import at.rudeboy.ferratafit.reminder.Reminders
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

/** Eine Dehnübung, während die Etappe läuft. */
data class ActiveDrill(val id: String, val done: Boolean = false)

/** Eine laufende Etappe ohne Gerät: Dehnen, Regeneration oder Ausdauer. */
data class ActiveStage(
    val stage: Stage,
    val drills: List<ActiveDrill> = emptyList(),
    /** Nur bei Ausdauer-Etappen belegt. */
    val minutes: Int = 30,
    val extraMeters: Int = 0
) {
    val doneCount: Int get() = drills.count { it.done }
    val progress: Float get() = if (drills.isEmpty()) 0f else doneCount.toFloat() / drills.size
}

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

    private val _activeStage = MutableStateFlow<ActiveStage?>(null)
    val activeStage: StateFlow<ActiveStage?> = _activeStage.asStateFlow()

    /** Frisch verdiente Abzeichen — die Oberfläche zeigt sie und meldet zurück. */
    private val _freshBadges = MutableStateFlow<List<Badge>>(emptyList())
    val freshBadges: StateFlow<List<Badge>> = _freshBadges.asStateFlow()

    fun clearToast() { _toast.value = null }
    fun notify(msg: String, error: Boolean = false) { _toast.value = Toast(msg, error) }
    fun clearFreshBadges() { _freshBadges.value = emptyList() }

    // ---------------- Etappen ----------------

    /** Momentaufnahme für die Abzeichenprüfung. */
    private fun badgeSnapshot(s: AppState = state.value): BadgeSnapshot {
        // Wie oft wurde eine Übung tatsächlich aufgelastet?
        val increases = Catalog.exercises.sumOf { ex ->
            val loads = s.sessions
                .filter { session -> session.sets.any { it.exerciseId == ex.id } }
                .sortedBy { it.startedAt }
                .map { session -> session.sets.filter { it.exerciseId == ex.id }.maxOf { it.weightKg } }
            loads.filterIndexed { i, v -> i > 0 && v > loads[i - 1] }.size
        }
        fun best(id: String, sel: (SetLog) -> Int): Int =
            s.sessions.flatMap { it.sets }.filter { it.exerciseId == id }.maxOfOrNull(sel) ?: 0

        return BadgeSnapshot(
            progress = s.progress,
            meters = Journey.totalMeters(s.progress),
            weeklyStreak = Stats.weeklyStreak(s.sessions, System.currentTimeMillis()),
            increases = increases,
            bestDeadhang = best("deadhang") { it.seconds },
            bestPullup = best("pullup") { it.reps }
        )
    }

    /**
     * Etappe abschließen: Höhenmeter gutschreiben und neue Abzeichen melden.
     * [skipped] verbucht sie als gegangen, aber ohne Gutschrift.
     */
    private fun completeStage(
        stage: Stage,
        skipped: Boolean = false,
        detail: String = "",
        metersOverride: Int? = null
    ) {
        val before = Journey.earnedBadges(badgeSnapshot()).map { it.id }.toSet()

        store.update { s ->
            s.copy(
                progress = s.progress + StageLog(
                    stageId = stage.id,
                    kind = stage.kind.name,
                    meters = if (skipped) 0 else (metersOverride ?: stage.meters),
                    at = System.currentTimeMillis(),
                    skipped = skipped,
                    detail = detail
                )
            )
        }

        val after = Journey.earnedBadges(badgeSnapshot())
        val fresh = after.filterNot { it.id in before }
        if (fresh.isNotEmpty()) {
            store.update { s -> s.copy(seenBadges = after.map { it.id }.toSet()) }
            _freshBadges.value = fresh
        } else if (skipped) {
            notify("Etappe übersprungen — die nächste ist frei.")
        } else {
            val m = metersOverride ?: stage.meters
            notify("+$m Hm · ${Journey.completionLine(stage.kind)}")
        }
    }

    /** Startet die offene Etappe — je nach Art als Krafteinheit oder als Dehn-/Ausdauerblock. */
    fun startStage(stageId: String) {
        val stage = Journey.stage(stageId) ?: return
        when (stage.kind) {
            StageKind.STRENGTH -> stage.dayId?.let { startWorkout(it) }
            StageKind.ENDURANCE -> _activeStage.value = ActiveStage(stage)
            else -> _activeStage.value = ActiveStage(
                stage = stage,
                drills = stage.mobilityIds.map { ActiveDrill(it) }
            )
        }
    }

    fun skipStage(stageId: String) {
        val stage = Journey.stage(stageId) ?: return
        _activeStage.value = null
        completeStage(stage, skipped = true)
    }

    fun toggleDrill(index: Int) {
        val a = _activeStage.value ?: return
        val drills = a.drills.toMutableList()
        val d = drills.getOrNull(index) ?: return
        drills[index] = d.copy(done = !d.done)
        _activeStage.value = a.copy(drills = drills)
    }

    fun adjustEndurance(minutes: Int? = null, meters: Int? = null) {
        val a = _activeStage.value ?: return
        _activeStage.value = a.copy(
            minutes = minutes?.coerceAtLeast(0) ?: a.minutes,
            extraMeters = meters?.coerceAtLeast(0) ?: a.extraMeters
        )
    }

    fun cancelStage() { _activeStage.value = null }

    /** Wie viele Abzeichen sind verdient? Für die Kachel auf der Startseite. */
    fun badgeCount(): Int = Journey.earnedBadges(badgeSnapshot()).size

    /** Welche Abzeichen sind verdient? Für die Übersicht im Fortschritt. */
    fun earnedBadgeIds(): Set<String> = Journey.earnedBadges(badgeSnapshot()).map { it.id }.toSet()

    fun finishStage() {
        val a = _activeStage.value ?: return
        val stage = a.stage

        if (stage.kind == StageKind.ENDURANCE) {
            val detail = buildString {
                append("${a.minutes} min")
                if (a.extraMeters > 0) append(" · ${a.extraMeters} Hm")
            }
            _activeStage.value = null
            completeStage(stage, detail = detail, metersOverride = stage.meters + a.extraMeters)
            return
        }

        if (a.doneCount == 0) {
            _activeStage.value = null
            notify("Etappe verworfen — es war keine Übung abgehakt.")
            return
        }
        _activeStage.value = null
        completeStage(stage, detail = "${a.doneCount} von ${a.drills.size} Übungen")
    }

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

    // ---------------- Am Fels ----------------

    /**
     * Trägt eine Begehung ein.
     *
     * Sie landet an zwei Stellen: in [AppState.ascents] als Grundlage für Rang und
     * Empfehlung, und als Etappenprotokoll für die Höhenmeter und Abzeichen. Die
     * Kennung [Ferrata.EXTRA_STAGE_ID] sorgt dafür, dass sie den Wochenzyklus nicht
     * weiterschiebt — ein Steig wird gegangen, wenn Wetter und Zeit passen, nicht
     * wenn der Plan es vorsieht.
     */
    fun addAscent(ascent: Ascent) {
        val before = Journey.earnedBadges(badgeSnapshot()).map { it.id }.toSet()

        store.update { s ->
            s.copy(
                ascents = s.ascents + ascent,
                progress = s.progress + StageLog(
                    stageId = Ferrata.EXTRA_STAGE_ID,
                    kind = StageKind.FERRATA.name,
                    meters = ascent.climbMeters,
                    at = ascent.date,
                    detail = ascent.name
                ),
                // Eine vorgemerkte Route ist mit der Begehung erledigt
                plannedRouteIds = s.plannedRouteIds - (ascent.routeId ?: "")
            )
        }

        val after = Journey.earnedBadges(badgeSnapshot())
        val fresh = after.filterNot { it.id in before }
        if (fresh.isNotEmpty()) {
            store.update { s -> s.copy(seenBadges = after.map { it.id }.toSet()) }
            _freshBadges.value = fresh
        }
        notify(Ferrata.completionLine(ascent))
    }

    fun removeAscent(id: String) = store.update { s ->
        val gone = s.ascents.firstOrNull { it.id == id }
        s.copy(
            ascents = s.ascents.filterNot { it.id == id },
            // Nur das zugehörige Protokoll entfernen, nicht jede Begehung desselben Tages
            progress = if (gone == null) s.progress else s.progress.filterNot {
                it.stageId == Ferrata.EXTRA_STAGE_ID && it.at == gone.date
            }
        )
    }

    fun togglePlannedRoute(id: String) = store.update { s ->
        s.copy(
            plannedRouteIds = if (id in s.plannedRouteIds) s.plannedRouteIds - id
            else s.plannedRouteIds + id
        )
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

        // Die Krafteinheit ist zugleich die offene Etappe — abhaken und gutschreiben.
        val duration = session.durationMin
        val stage = Journey.current(state.value.progress)
        if (stage.kind == StageKind.STRENGTH && stage.dayId == w.dayId) {
            completeStage(stage, detail = "${logs.size} Sätze · $duration min")
        } else {
            notify("Gespeichert: ${logs.size} Sätze in $duration Minuten. Stark!")
        }

        if (state.value.profile.healthConnectEnabled) {
            viewModelScope.launch {
                val title = "FerrataFit · ${Catalog.day(w.dayId).title}"
                health.writeSession(session, title)
                    .onSuccess { notify("An Samsung Health übergeben.") }
                    .onFailure { notify("Health Connect: ${it.message ?: "Übertragung fehlgeschlagen"}", true) }
            }
        }
    }

    // ---------------- Einheiten nachbearbeiten ----------------

    /** Die Einheit, die gerade bearbeitet wird. */
    private val _editing = MutableStateFlow<Session?>(null)
    val editing: StateFlow<Session?> = _editing.asStateFlow()

    fun startEditSession(id: String) {
        _editing.value = state.value.sessions.firstOrNull { it.id == id }
    }

    fun cancelEdit() { _editing.value = null }

    /** Ändert einen Satz in der Bearbeitung — noch ohne zu speichern. */
    fun editSet(index: Int, block: (SetLog) -> SetLog) {
        val session = _editing.value ?: return
        val sets = session.sets.toMutableList()
        val old = sets.getOrNull(index) ?: return
        sets[index] = block(old)
        _editing.value = session.copy(sets = sets)
    }

    /** Entfernt einen Satz aus der Bearbeitung. */
    fun removeSetFromEdit(index: Int) {
        val session = _editing.value ?: return
        val sets = session.sets.toMutableList()
        if (index !in sets.indices) return
        sets.removeAt(index)
        _editing.value = session.copy(sets = SessionEdit.renumber(sets))
    }

    /**
     * Übernimmt die Bearbeitung.
     *
     * Der Zeitpunkt der Einheit bleibt unangetastet: Die Progressionslogik sortiert die
     * Historie danach, und eine verschobene Einheit würde die 2-für-2-Regel durcheinander
     * bringen. Der Etappen-Fortschritt bleibt ebenfalls stehen — die Etappe wurde ja
     * gegangen, unabhängig davon, ob ein Wert später korrigiert wird.
     */
    fun saveEdit() {
        val edited = _editing.value ?: return
        if (!SessionEdit.isSaveable(edited)) {
            notify("Ohne Sätze bleibt nichts übrig — lösch die Einheit lieber ganz.", true)
            return
        }
        store.update { s ->
            val sessions = SessionEdit.replace(s.sessions, edited)
            s.copy(
                sessions = sessions,
                lastLoads = SessionEdit.recomputeLastLoads(sessions),
                seenBadges = prunedBadges(s.copy(sessions = sessions))
            )
        }
        _editing.value = null
        notify("Einheit angepasst. Die Vorschläge rechnen ab sofort damit.")
        pushToHealth(edited)
    }

    /**
     * Löscht eine Einheit.
     *
     * Der Etappen-Eintrag bleibt bestehen — sonst rutschte der gesamte Wochenzyklus eine
     * Etappe zurück, weil sich die offene Etappe aus der Zahl der gegangenen ergibt.
     * Gegangen ist gegangen, auch wenn die Zahlen dazu falsch waren.
     */
    fun deleteSession(id: String) {
        store.update { s ->
            val sessions = SessionEdit.remove(s.sessions, id)
            s.copy(
                sessions = sessions,
                lastLoads = SessionEdit.recomputeLastLoads(sessions),
                seenBadges = prunedBadges(s.copy(sessions = sessions))
            )
        }
        _editing.value = null
        notify("Einheit gelöscht. Höhenmeter und Etappe bleiben dir erhalten.")
    }

    /**
     * Abzeichen, die nach der Änderung noch verdient sind.
     *
     * Vergeben werden sie ohnehin bei jeder Anzeige neu berechnet; hier wird nur die
     * Merkliste gekürzt, damit ein später wieder erreichtes Abzeichen erneut gefeiert wird.
     */
    private fun prunedBadges(s: AppState): Set<String> {
        val stillEarned = Journey.earnedBadges(badgeSnapshot(s)).map { it.id }.toSet()
        return s.seenBadges intersect stillEarned
    }

    /** Schiebt eine geänderte Einheit erneut zu Health Connect. */
    private fun pushToHealth(session: Session) {
        if (!state.value.profile.healthConnectEnabled) return
        viewModelScope.launch {
            health.writeSession(session, "FerrataFit · ${Catalog.day(session.dayId).title}")
        }
    }

    // ---------------- Health Connect ----------------

    fun setHealthEnabled(enabled: Boolean) {
        updateProfile { it.copy(healthConnectEnabled = enabled) }
        if (enabled) refreshSteps()
    }

    fun refreshSteps() {
        viewModelScope.launch { _steps.value = health.stepsToday() }
    }

    // ---------------- Waage ----------------

    private val _bodySyncing = MutableStateFlow(false)
    val bodySyncing: StateFlow<Boolean> = _bodySyncing.asStateFlow()

    /**
     * Holt die Körperdaten aus Health Connect.
     *
     * Bei einer FitDays-Waage läuft die Kette FitDays → Samsung Health → Health Connect;
     * hier kommt nur das Ende davon an. Ist die neueste Messung frisch genug, wandert das
     * Gewicht ins Profil — davon hängen die Startschätzungen und alle Auswertungen ab,
     * die das Körpergewicht einbeziehen.
     */
    fun syncBody(announce: Boolean = true) {
        if (_bodySyncing.value) return
        viewModelScope.launch {
            _bodySyncing.value = true
            try {
                if (!health.hasBodyPermissions()) {
                    if (announce) notify("Für die Waage fehlt noch die Freigabe in Health Connect.", true)
                    return@launch
                }

                val measurements = health.readBody()
                val height = health.readHeightCm()
                val now = System.currentTimeMillis()

                if (measurements.isEmpty()) {
                    store.update { it.copy(bodySyncedAt = now) }
                    if (announce) {
                        notify("Keine Waagendaten gefunden. Prüfe, ob FitDays mit Samsung Health abgleicht.")
                    }
                    return@launch
                }

                val latest = Body.latest(measurements)
                var adopted: Double? = null

                store.update { s ->
                    val takeWeight = s.profile.autoWeightFromScale &&
                        latest != null &&
                        Body.isFresh(latest.at, now) &&
                        kotlin.math.abs(latest.weightKg - s.profile.bodyweightKg) >= 0.3
                    if (takeWeight && latest != null) adopted = latest.weightKg

                    s.copy(
                        body = measurements,
                        heightCm = height ?: s.heightCm,
                        bodySyncedAt = now,
                        profile = if (takeWeight && latest != null)
                            s.profile.copy(bodyweightKg = latest.weightKg) else s.profile
                    )
                }

                if (announce) {
                    val n = measurements.size
                    val w = adopted
                    notify(
                        if (w != null) "$n Messungen übernommen · Körpergewicht auf ${fmtKg(w)} gesetzt"
                        else "$n Messungen übernommen."
                    )
                }
            } catch (e: Exception) {
                if (announce) notify("Waage: ${e.message ?: "Abgleich fehlgeschlagen"}", true)
            } finally {
                _bodySyncing.value = false
            }
        }
    }

    // ---------------- Pausenuhr ----------------

    /**
     * Wann die laufende Pause endet — als Zeitpunkt, nicht als Restsekunden.
     *
     * Ein herunterzählender Zähler lief nur, solange die App im Vordergrund war: Wer
     * zwischen zwei Sätzen kurz auf die Nachrichten schaut, kam mit stehengebliebener
     * Uhr zurück. Ein gemerkter Endzeitpunkt überlebt das, weil die Restzeit jederzeit
     * aus der aktuellen Uhrzeit folgt — ganz ohne Hintergrunddienst.
     */
    private val _restEndsAt = MutableStateFlow(0L)
    val restEndsAt: StateFlow<Long> = _restEndsAt.asStateFlow()

    /** Wie lange die Pause insgesamt dauern sollte — für den Fortschrittsbalken. */
    private val _restTotal = MutableStateFlow(0)
    val restTotal: StateFlow<Int> = _restTotal.asStateFlow()

    /** Bei angehaltener Uhr steht hier die verbleibende Zeit. */
    private val _restPausedWith = MutableStateFlow<Int?>(null)
    val restPausedWith: StateFlow<Int?> = _restPausedWith.asStateFlow()

    fun startRest(seconds: Int) {
        _restTotal.value = seconds
        _restPausedWith.value = null
        _restEndsAt.value = System.currentTimeMillis() + seconds * 1000L
    }

    fun addRest(seconds: Int) {
        if (_restPausedWith.value != null) {
            _restPausedWith.value = (_restPausedWith.value ?: 0) + seconds
        } else if (_restEndsAt.value > 0) {
            _restEndsAt.value += seconds * 1000L
        }
        _restTotal.value = maxOf(_restTotal.value, remainingRest())
    }

    fun toggleRest() {
        val paused = _restPausedWith.value
        if (paused != null) {
            _restPausedWith.value = null
            _restEndsAt.value = System.currentTimeMillis() + paused * 1000L
        } else {
            _restPausedWith.value = remainingRest()
            _restEndsAt.value = 0L
        }
    }

    fun stopRest() {
        _restEndsAt.value = 0L
        _restTotal.value = 0
        _restPausedWith.value = null
    }

    /** Restsekunden — aus dem Endzeitpunkt gerechnet, also unabhängig davon, ob die App lief. */
    fun remainingRest(): Int {
        _restPausedWith.value?.let { return it }
        val end = _restEndsAt.value
        if (end <= 0) return 0
        return ((end - System.currentTimeMillis()) / 1000L).toInt().coerceAtLeast(0)
    }

    // ---------------- Erinnerung ----------------

    /**
     * Schaltet die tägliche Erinnerung.
     *
     * Der Alarm wird sofort gesetzt oder gelöscht, damit der Schalter nicht nur eine
     * Absichtserklärung ist. Die Freigabe für Benachrichtigungen holt die Oberfläche ein.
     */
    fun setReminder(enabled: Boolean, hour: Int? = null, minute: Int? = null) {
        updateProfile {
            it.copy(
                reminderEnabled = enabled,
                reminderHour = hour ?: it.reminderHour,
                reminderMinute = minute ?: it.reminderMinute
            )
        }
        val p = state.value.profile
        if (enabled) {
            Reminders.schedule(getApplication(), p.reminderHour, p.reminderMinute)
            notify("Erinnerung gestellt auf ${"%02d:%02d".format(p.reminderHour, p.reminderMinute)}.")
        } else {
            Reminders.cancel(getApplication())
            notify("Erinnerung ausgeschaltet.")
        }
    }

    fun setReminderSkipIfDone(skip: Boolean) {
        updateProfile { it.copy(reminderSkipIfDone = skip) }
    }

    /** Setzt die Planung beim Start neu auf — Alarme überleben weder Neustart noch Update. */
    fun ensureReminderScheduled() {
        Reminders.rescheduleFrom(getApplication(), state.value)
    }

    /**
     * Unterwegs-Modus schalten. Der Plan weicht dann auf Körpergewichtsübungen aus;
     * die Geräte-Historie bleibt unangetastet und steht zu Hause unverändert bereit.
     */
    fun setTravelMode(enabled: Boolean) {
        updateProfile { it.copy(travelMode = enabled) }
        notify(
            if (enabled) "Unterwegs-Modus an — der Plan kommt ohne Gerät aus."
            else "Zurück am Gerät. Deine Lasten stehen unverändert bereit."
        )
    }

    fun setAutoWeight(enabled: Boolean) {
        updateProfile { it.copy(autoWeightFromScale = enabled) }
        if (enabled) syncBody(announce = false)
    }

    /**
     * Trägt eine Messung von Hand ein — der Weg, wenn die Kette über Samsung Health
     * nicht steht oder die Waage gar nicht vernetzt ist.
     */
    fun addBodyManual(weightKg: Double, bodyFatPercent: Double? = null) {
        if (weightKg < 25 || weightKg > 300) {
            notify("Bitte ein Gewicht zwischen 25 und 300 kg eintragen.", true)
            return
        }
        val now = System.currentTimeMillis()
        val entry = BodyMeasurement(
            at = now,
            weightKg = weightKg,
            bodyFatPercent = bodyFatPercent?.takeIf { it in 1.0..70.0 }
        )
        store.update { s ->
            s.copy(
                body = BodyImport.merge(s.body, listOf(entry)),
                profile = if (s.profile.autoWeightFromScale)
                    s.profile.copy(bodyweightKg = weightKg) else s.profile
            )
        }
        notify("${fmtKg(weightKg)} eingetragen.")
    }

    /**
     * Liest eine aus FitDays (oder einer anderen Waagen-App) geteilte Tabelle ein.
     * Vorhandene Messungen bleiben erhalten; je Tag gewinnt die jüngere.
     */
    fun importBodyFile(text: String) {
        val result = BodyImport.parse(text)
        if (result.error != null) {
            notify(result.error, true)
            return
        }
        val latest = Body.latest(result.measurements)
        store.update { s ->
            val merged = BodyImport.merge(s.body, result.measurements)
            val newest = Body.latest(merged)
            s.copy(
                body = merged,
                profile = if (s.profile.autoWeightFromScale && newest != null &&
                    Body.isFresh(newest.at, System.currentTimeMillis())
                ) s.profile.copy(bodyweightKg = newest.weightKg) else s.profile
            )
        }
        notify(
            buildString {
                append("${result.measurements.size} Messungen eingelesen")
                if (result.skipped > 0) append(", ${result.skipped} Zeilen übersprungen")
                latest?.let { append(" · zuletzt ${fmtKg(it.weightKg)}") }
            }
        )
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
