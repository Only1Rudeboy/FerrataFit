package at.rudeboy.ferratafit.data

import kotlinx.serialization.Serializable

/**
 * Der angefangene, noch nicht abgeschlossene Zustand.
 *
 * Warum es das gibt: Android beendet Apps im Hintergrund, wann immer es Speicher braucht.
 * Wer während einer Einheit kurz die Musik wechselt oder einen Anruf annimmt, kommt in eine
 * frisch gestartete App zurück — und ohne diese Datei wären alle eingetippten Sätze weg.
 * Genau das ist passiert, bevor es diese Datei gab.
 *
 * Bewusst getrennt vom übrigen Zustand und in einer eigenen Datei:
 *
 *  - Beim Tippen entstehen sehr viele kleine Änderungen. Der Hauptbestand wächst über die
 *    Jahre auf einige hundert Kilobyte und wird bei jedem Schreiben zusätzlich gesichert —
 *    das bei jedem Antippen eines Steppers durchzuziehen wäre Verschwendung.
 *  - Ein halbfertiges Training gehört nicht in die Datensicherung. Wer seinen Bestand
 *    exportiert, will sein Trainingsbuch, keine angefangene Eingabemaske.
 *
 * Gespeichert wird nur, was der Nutzer eingegeben hat, plus die Kennungen. Alles Abgeleitete
 * — Vorschlag, Begründung, Übungstexte — wird beim Wiederaufnehmen neu berechnet, und zwar
 * mit dem ursprünglichen Startzeitpunkt. So steht nach der Rückkehr dieselbe Empfehlung da
 * wie vorher, auch wenn inzwischen Mitternacht war.
 */
@Serializable
data class DraftSet(
    val weightKg: Double = 0.0,
    val reps: Int = 0,
    val seconds: Int = 0,
    val done: Boolean = false
)

@Serializable
data class DraftEntry(
    val exerciseId: String,
    val sets: List<DraftSet> = emptyList()
)

/** Eine angefangene Krafteinheit. */
@Serializable
data class WorkoutDraft(
    val dayId: String,
    val startedAt: Long,
    /**
     * Wann zuletzt etwas eingetippt wurde. Entscheidet darüber, ob wortlos weitergemacht
     * oder nachgefragt wird — nicht der Start. Wer um acht anfängt und um zwei zwischen
     * zwei Sätzen kurz weggeht, hat nichts vergessen, auch wenn die Einheit sechs Stunden
     * alt ist.
     */
    val lastTouchedAt: Long = 0L,
    /** Die Etappe, die diese Einheit abdeckt. Siehe [StageDraft.stageId]. */
    val stageId: String? = null,
    val currentIndex: Int = 0,
    val entries: List<DraftEntry> = emptyList(),
    /**
     * Endzeitpunkt der Pausenuhr als absolute Zeit. Dadurch stimmt sie auch dann noch,
     * wenn die App zwischendurch gar nicht lief — es zählt der Kalender, nicht ein Zähler.
     */
    val restEndsAt: Long = 0L,
    val restTotal: Int = 0,
    val restPausedWith: Int? = null
)

/** Eine angefangene Dehn-, Ausdauer- oder Regenerationsetappe. */
@Serializable
data class StageDraft(
    /**
     * Die Etappe, die beim Start offen war.
     *
     * Beim Abschließen wird gegen diese Kennung geprüft, nicht gegen die dann offene:
     * Zwischendurch kann eine Begehung den Zeiger weitergerückt haben, und dann würde die
     * Einheit stillschweigend nicht gutgeschrieben.
     */
    val stageId: String,
    val startedAt: Long = 0L,
    val lastTouchedAt: Long = 0L,
    val doneDrills: List<String> = emptyList(),
    val minutes: Int = 30,
    val extraMeters: Int = 0
)

/**
 * Was auf der Platte liegt, solange etwas angefangen ist.
 * Beide Felder sind selten gleichzeitig belegt, aber es kostet nichts, beides zu können.
 */
@Serializable
data class Draft(
    val workout: WorkoutDraft? = null,
    val stage: StageDraft? = null
) {
    val isEmpty: Boolean get() = workout == null && stage == null

    val startedAt: Long get() = workout?.startedAt ?: stage?.startedAt ?: 0L

    /** Letzte Eingabe — der Bezugspunkt für die Frage, ob etwas vergessen wurde. */
    val lastTouchedAt: Long
        get() = maxOf(
            workout?.let { maxOf(it.lastTouchedAt, it.startedAt) } ?: 0L,
            stage?.let { maxOf(it.lastTouchedAt, it.startedAt) } ?: 0L
        )
}

object Drafts {

    private const val HOUR_MS = 60 * 60 * 1000L

    /**
     * Bis hierher wird eine angefangene Einheit wortlos wieder aufgeschlagen.
     *
     * Sechs Stunden decken jede Unterbrechung ab, die zu einem Training gehört — Anruf,
     * Musikwechsel, Fahrt zum Gerät. Alles darüber ist keine Unterbrechung mehr, sondern
     * ein vergessenes Training, und das darf nicht ungefragt aufspringen.
     */
    const val RESUME_WINDOW_H = 6

    /** Ab hier wird gar nicht mehr gefragt — die Einheit ist erkennbar vergessen worden. */
    const val EXPIRY_H = 72

    fun ageHours(draft: Draft, now: Long): Int {
        val touched = draft.lastTouchedAt
        if (touched <= 0L) return 0
        return ((now - touched) / HOUR_MS).toInt().coerceAtLeast(0)
    }

    /** Direkt weitermachen, ohne zu fragen? */
    fun resumesSilently(draft: Draft, now: Long): Boolean =
        !draft.isEmpty && ageHours(draft, now) < RESUME_WINDOW_H

    /** Nachfragen, statt einfach aufzuspringen. */
    fun needsAsking(draft: Draft, now: Long): Boolean =
        !draft.isEmpty && ageHours(draft, now) in RESUME_WINDOW_H until EXPIRY_H

    fun isExpired(draft: Draft, now: Long): Boolean =
        !draft.isEmpty && ageHours(draft, now) >= EXPIRY_H

    /**
     * Wie lange die Einheit gedauert hat — gedeckelt.
     *
     * Ohne Deckel bekäme eine über Nacht vergessene Einheit vierzehn Stunden Dauer
     * eingetragen und würde jede Statistik verzerren.
     */
    const val MAX_SESSION_MIN = 240

    fun cappedDuration(startedAt: Long, finishedAt: Long): Long {
        val max = MAX_SESSION_MIN * 60 * 1000L
        return if (finishedAt - startedAt > max) startedAt + max else finishedAt
    }

    /** Beschreibt das Alter in Worten — für die Rückfrage. */
    fun ageLabel(draft: Draft, now: Long): String {
        val h = ageHours(draft, now)
        return when {
            h < 1 -> "vor wenigen Minuten"
            h < 2 -> "vor einer Stunde"
            h < 24 -> "vor $h Stunden"
            h < 48 -> "gestern"
            else -> "vor ${h / 24} Tagen"
        }
    }
}
