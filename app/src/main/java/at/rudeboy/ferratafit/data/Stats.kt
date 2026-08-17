package at.rudeboy.ferratafit.data

import java.util.Calendar
import kotlin.math.roundToInt

/** Bestleistung einer Übung. */
data class PersonalRecord(
    val exerciseId: String,
    val name: String,
    val value: String,
    val achievedAt: Long
)

/** Ein Punkt im Verlaufsdiagramm. */
data class TrendPoint(val at: Long, val value: Double, val label: String)

/** Auswertungen über die geloggten Einheiten. */
object Stats {

    private const val DAY_MS = 24 * 60 * 60 * 1000L

    /**
     * Serie in Wochen: Wie viele Wochen am Stück wurde mindestens einmal trainiert?
     * Die laufende Woche zählt nur mit, wenn schon etwas drin steht — sonst würde
     * die Serie am Montagmorgen fälschlich reißen.
     */
    fun weeklyStreak(sessions: List<Session>, now: Long): Int {
        if (sessions.isEmpty()) return 0
        val weeks = sessions.map { weekKey(it.startedAt) }.toSet()
        var streak = 0
        var cursor = now
        if (weekKey(now) !in weeks) cursor -= 7 * DAY_MS
        while (weekKey(cursor) in weeks) {
            streak++
            cursor -= 7 * DAY_MS
        }
        return streak
    }

    private fun weekKey(millis: Long): String {
        val c = Calendar.getInstance().apply { timeInMillis = millis; firstDayOfWeek = Calendar.MONDAY }
        return "${c.get(Calendar.YEAR)}-${c.get(Calendar.WEEK_OF_YEAR)}"
    }

    fun sessionsThisWeek(sessions: List<Session>, now: Long): Int {
        val key = weekKey(now)
        return sessions.count { weekKey(it.startedAt) == key }
    }

    fun totalVolume(sessions: List<Session>): Double = sessions.sumOf { it.volumeKg }

    /** Welcher Trainingstag ist als nächstes dran? Rotiert stumpf durch den Split. */
    fun nextDayId(sessions: List<Session>): String {
        val lastDay = sessions.maxByOrNull { it.startedAt }?.dayId ?: return Catalog.days.first().id
        val idx = Catalog.days.indexOfFirst { it.id == lastDay }
        if (idx < 0) return Catalog.days.first().id
        return Catalog.days[(idx + 1) % Catalog.days.size].id
    }

    /** Bestleistungen über alle Übungen. */
    fun records(sessions: List<Session>): List<PersonalRecord> =
        Catalog.exercises.mapNotNull { ex ->
            val sets = sessions.flatMap { s -> s.sets.filter { it.exerciseId == ex.id }.map { s to it } }
            if (sets.isEmpty()) return@mapNotNull null
            when (ex.progression) {
                ProgressionKind.TIME -> sets.maxByOrNull { it.second.seconds }?.let { (s, set) ->
                    PersonalRecord(ex.id, ex.name, "${set.seconds} s", s.startedAt)
                }
                ProgressionKind.REPS -> sets.maxByOrNull { it.second.reps * 1000 + it.second.weightKg.toInt() }
                    ?.let { (s, set) ->
                        val extra = if (set.weightKg > 0) " + ${fmtKg(set.weightKg)}" else ""
                        PersonalRecord(ex.id, ex.name, "${set.reps} Wdh.$extra", s.startedAt)
                    }
                ProgressionKind.WEIGHT -> sets.maxByOrNull { it.second.weightKg }?.let { (s, set) ->
                    PersonalRecord(ex.id, ex.name, "${fmtKg(set.weightKg)} × ${set.reps}", s.startedAt)
                }
            }
        }.sortedByDescending { it.achievedAt }

    /** Verlauf einer Übung für das Diagramm. */
    fun trend(exerciseId: String, sessions: List<Session>): List<TrendPoint> {
        val ex = Catalog.byId(exerciseId) ?: return emptyList()
        return sessions.sortedBy { it.startedAt }.mapNotNull { s ->
            val sets = s.sets.filter { it.exerciseId == exerciseId }
            if (sets.isEmpty()) return@mapNotNull null
            when (ex.progression) {
                ProgressionKind.TIME -> sets.maxOf { it.seconds }.toDouble()
                    .let { TrendPoint(s.startedAt, it, "${it.roundToInt()} s") }
                ProgressionKind.REPS -> sets.maxOf { it.reps }.toDouble()
                    .let { TrendPoint(s.startedAt, it, "${it.roundToInt()} Wdh.") }
                ProgressionKind.WEIGHT -> sets.maxOf { it.weightKg }
                    .let { TrendPoint(s.startedAt, it, fmtKg(it)) }
            }
        }
    }

    /**
     * Klettersteig-Bereitschaft, 0–100.
     *
     * Gewichtet die drei Dinge, die am Steig wirklich limitieren: Griffkraft (Dead Hang),
     * Zugkraft (Klimmzug) und Rumpfspannung — plus einen Anteil für Regelmäßigkeit.
     * Die Zielwerte orientieren sich an der gängigen Empfehlung, 30–60 Sekunden frei
     * hängen zu können, bevor man sich an eine anspruchsvollere Route wagt.
     */
    fun ferrataReadiness(sessions: List<Session>, now: Long): Int {
        fun best(id: String, sel: (SetLog) -> Int): Int =
            sessions.flatMap { it.sets }.filter { it.exerciseId == id }.maxOfOrNull(sel) ?: 0

        val hang = best("deadhang") { it.seconds }
        val pull = best("pullup") { it.reps }
        val knee = best("hang_knee_raise") { it.reps }
        val plank = best("plank") { it.seconds }
        val step = best("stepup") { it.reps }

        val gripScore = (hang / 60.0).coerceAtMost(1.0) * 30
        val pullScore = (pull / 8.0).coerceAtMost(1.0) * 25
        val coreScore = (((knee / 12.0).coerceAtMost(1.0) + (plank / 90.0).coerceAtMost(1.0)) / 2) * 20
        val legScore = (step / 15.0).coerceAtMost(1.0) * 15
        val consistency = (weeklyStreak(sessions, now) / 8.0).coerceAtMost(1.0) * 10

        return (gripScore + pullScore + coreScore + legScore + consistency).roundToInt().coerceIn(0, 100)
    }

    fun readinessLabel(score: Int): String = when {
        score >= 85 -> "Steigfest — auch längere D-Routen sind drin"
        score >= 65 -> "Gut dabei — C/D-Routen solltest du sauber durchziehen"
        score >= 45 -> "Solide Basis — B/C-Routen passen, mach bei der Griffkraft weiter"
        score >= 25 -> "Im Aufbau — halte dich an leichtere Routen mit kurzem Zustieg"
        else -> "Am Anfang — die ersten Wochen zahlen am meisten ein"
    }
}
