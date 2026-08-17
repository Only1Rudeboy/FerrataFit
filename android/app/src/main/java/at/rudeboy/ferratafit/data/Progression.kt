package at.rudeboy.ferratafit.data

import kotlin.math.max
import kotlin.math.roundToInt

/** Was die App für die nächste Einheit empfiehlt. */
enum class Advice {
    /** Erste Einheit für diese Übung — Startwert ist nur eine Schätzung. */
    START,

    /** Gewicht bleibt, Ziel sind mehr Wiederholungen. */
    HOLD,

    /** Obere Grenze zweimal erreicht — jetzt wird aufgelastet. */
    INCREASE,

    /** Es geht seit mehreren Einheiten nicht vorwärts — bewusst zurück und neu anlaufen. */
    BACKOFF,

    /** Geplante Entlastungswoche. */
    DELOAD
}

/**
 * Vorschlag für eine Übung in der nächsten Einheit.
 *
 * [headline] ist der große Wert im UI („25 kg“ / „45 s“), [reason] der Satz darunter.
 */
data class Suggestion(
    val exercise: Exercise,
    val advice: Advice,
    val weightKg: Double,
    val targetReps: Int,
    val targetSeconds: Int,
    val sets: Int,
    val headline: String,
    val reason: String,
    /** Vorheriger Wert, für die „vorher → jetzt“-Anzeige. */
    val previousHeadline: String? = null
)

/**
 * Die Progressionslogik.
 *
 * Grundregel ist die Doppelte Progression, verschärft um die 2-für-2-Regel:
 * Erst wächst man innerhalb des Wiederholungsfensters nach oben, und erst wenn das
 * obere Ende in **zwei aufeinanderfolgenden Einheiten** steht, kommt Gewicht drauf.
 * Das verhindert, dass ein einzelner guter Tag zu einer Steigerung führt, die
 * danach zwei Wochen lang nicht mehr sauber zu bewältigen ist.
 *
 * Darüber liegt eine Wochenperiodisierung: In einem 5-Wochen-Block sinkt der
 * Puffer (Reps in Reserve) von 3 auf 1, danach folgt eine Entlastungswoche mit
 * reduziertem Volumen und reduzierter Last.
 */
object Progression {

    const val CYCLE_WEEKS = 5
    const val DELOAD_WEEK = 5

    private const val DAY_MS = 24 * 60 * 60 * 1000L

    /** Woche 1–5 innerhalb des laufenden Blocks. */
    fun weekInCycle(profile: Profile, now: Long): Int {
        if (profile.cycleStart <= 0L) return 1
        val weeksElapsed = ((now - profile.cycleStart) / (7 * DAY_MS)).toInt()
        return (weeksElapsed % CYCLE_WEEKS) + 1
    }

    fun isDeloadWeek(week: Int): Boolean = week == DELOAD_WEEK

    /** Ziel-Puffer der Woche: wie viele Wiederholungen am Satzende noch übrig sein sollen. */
    fun targetRir(week: Int): Int = when (week) {
        1 -> 3
        2 -> 2
        3 -> 1
        4 -> 1
        else -> 4
    }

    fun weekLabel(week: Int): String = when (week) {
        1 -> "Woche 1 · Aufbau — locker starten, 3 Wdh. Puffer"
        2 -> "Woche 2 · Steigerung — 2 Wdh. Puffer"
        3 -> "Woche 3 · Intensiv — 1 Wdh. Puffer"
        4 -> "Woche 4 · Spitze — bis kurz vors Limit"
        else -> "Woche 5 · Entlastung — bewusst leicht, der Körper baut jetzt auf"
    }

    /** Rundet auf die nächste Steckgewicht-Stufe der Kraftstation. */
    fun roundToPlate(weight: Double, step: Double): Double {
        if (step <= 0.0) return weight
        return max(0.0, (weight / step).roundToInt() * step)
    }

    /**
     * Startschätzung für die allererste Einheit einer Übung, abgeleitet vom Körpergewicht.
     * Bewusst konservativ — lieber eine Einheit zu leicht als eine Zerrung.
     */
    private fun startWeight(exercise: Exercise, profile: Profile): Double {
        val bw = profile.bodyweightKg
        val factor = when (exercise.id) {
            "latpull_wide" -> 0.45
            "row_cable", "row_lat_narrow" -> 0.40
            "chest_press" -> 0.40
            "butterfly" -> 0.25
            "shoulder_press" -> 0.25
            "leg_ext" -> 0.35
            "leg_curl" -> 0.30
            "curl", "curl_bw" -> 0.15
            "triceps" -> 0.15
            "reverse_fly" -> 0.12
            else -> 0.30
        }
        val step = if (exercise.increment < profile.plateStepKg) exercise.increment else profile.plateStepKg
        return roundToPlate(bw * factor, step)
    }

    private fun startSeconds(exercise: Exercise): Int = when (exercise.id) {
        "deadhang" -> 15
        "plank" -> 30
        "side_plank" -> 20
        "farmer_hold" -> 20
        else -> 20
    }

    /** Alle geloggten Sätze einer Übung, gruppiert je Einheit, neueste zuerst. */
    private fun history(exerciseId: String, sessions: List<Session>): List<List<SetLog>> =
        sessions.sortedByDescending { it.startedAt }
            .mapNotNull { s ->
                s.sets.filter { it.exerciseId == exerciseId }.takeIf { it.isNotEmpty() }
            }

    /** Die in einer Einheit hauptsächlich verwendete Last (der häufigste Wert). */
    private fun workingLoad(sets: List<SetLog>): Double =
        sets.groupBy { it.weightKg }.maxByOrNull { it.value.size }?.key ?: 0.0

    /**
     * Hat der Nutzer das obere Ende des Fensters erreicht?
     *
     * Es müssen nicht alle Sätze perfekt sein — der letzte Satz fällt erfahrungsgemäß ab.
     * Verlangt wird das obere Ende in allen Sätzen bis auf einen.
     */
    private fun hitTopOfRange(exercise: Exercise, sets: List<SetLog>): Boolean {
        if (sets.isEmpty()) return false
        val target = if (exercise.progression == ProgressionKind.TIME) {
            sets.count { it.seconds >= currentTimeTarget(exercise, sets) }
        } else {
            sets.count { it.reps >= exercise.repMax }
        }
        val required = max(1, sets.size - 1)
        return target >= required
    }

    /** Bei Zeitübungen: das Ziel, das in dieser Einheit stand (bester Satz als Referenz). */
    private fun currentTimeTarget(exercise: Exercise, sets: List<SetLog>): Int {
        val best = sets.maxOfOrNull { it.seconds } ?: return startSeconds(exercise)
        return best
    }

    /**
     * Der Kern: Was soll heute auf die Maschine?
     */
    fun suggest(
        exercise: Exercise,
        sessions: List<Session>,
        profile: Profile,
        now: Long
    ): Suggestion {
        val week = weekInCycle(profile, now)
        val deload = isDeloadWeek(week)
        val hist = history(exercise.id, sessions)

        // --- Fall 1: noch nie trainiert ---
        if (hist.isEmpty()) {
            return when (exercise.progression) {
                ProgressionKind.TIME -> {
                    val s = startSeconds(exercise)
                    Suggestion(
                        exercise, Advice.START, 0.0, 0, s, exercise.sets,
                        headline = "${s} s",
                        reason = "Erste Einheit. Halte so lange sauber, wie es geht — der Wert ist nur ein Startpunkt."
                    )
                }
                ProgressionKind.REPS -> Suggestion(
                    exercise, Advice.START, 0.0, exercise.repMin, 0, exercise.sets,
                    headline = "${exercise.repMin}–${exercise.repMax} Wdh.",
                    reason = "Erste Einheit mit dem eigenen Körpergewicht. Notiere ehrlich, was du schaffst."
                )
                ProgressionKind.WEIGHT -> {
                    val w = startWeight(exercise, profile)
                    Suggestion(
                        exercise, Advice.START, w, exercise.repMin, 0, exercise.sets,
                        headline = fmtKg(w),
                        reason = "Startschätzung aus deinem Körpergewicht. Wenn sich der erste Satz zu leicht anfühlt, " +
                            "leg direkt eine Platte drauf."
                    )
                }
            }
        }

        val last = hist[0]
        val lastLoad = workingLoad(last)
        val lastBestReps = last.maxOfOrNull { it.reps } ?: 0
        val lastBestSecs = last.maxOfOrNull { it.seconds } ?: 0

        // --- Fall 2: Entlastungswoche schlägt alles andere ---
        if (deload) {
            val sets = max(2, exercise.sets - 1)
            return when (exercise.progression) {
                ProgressionKind.TIME -> Suggestion(
                    exercise, Advice.DELOAD, 0.0, 0, (lastBestSecs * 0.7).roundToInt(), sets,
                    headline = "${(lastBestSecs * 0.7).roundToInt()} s",
                    reason = "Entlastungswoche: rund 30 % kürzer halten, ein Satz weniger. " +
                        "Der Fortschritt entsteht jetzt in der Erholung.",
                    previousHeadline = "$lastBestSecs s"
                )
                else -> {
                    val w = roundToPlate(lastLoad * 0.85, profile.plateStepKg)
                    Suggestion(
                        exercise, Advice.DELOAD, w, exercise.repMin, 0, sets,
                        headline = if (exercise.progression == ProgressionKind.WEIGHT) fmtKg(w)
                                   else "${exercise.repMin} Wdh.",
                        reason = "Entlastungswoche: etwa 15 % weniger Last, ein Satz weniger, " +
                            "und immer 3–4 Wiederholungen Puffer lassen.",
                        previousHeadline = if (exercise.progression == ProgressionKind.WEIGHT) fmtKg(lastLoad) else null
                    )
                }
            }
        }

        val topLast = hitTopOfRange(exercise, last)
        val topBefore = hist.getOrNull(1)?.let { prev ->
            // Die zweite Einheit zählt nur, wenn seither kein Rückschritt passiert ist.
            // Sonst würde eine schwächere Einheit auf eine starke folgen und trotzdem
            // eine Steigerung auslösen.
            val noRegression = when (exercise.progression) {
                ProgressionKind.WEIGHT ->
                    workingLoad(prev) >= lastLoad - 0.01
                ProgressionKind.TIME ->
                    lastBestSecs >= (prev.maxOfOrNull { it.seconds } ?: 0)
                ProgressionKind.REPS ->
                    lastBestReps >= (prev.maxOfOrNull { it.reps } ?: 0) &&
                        workingLoad(prev) >= lastLoad - 0.01
            }
            noRegression && hitTopOfRange(exercise, prev)
        } ?: false

        // --- Fall 3: 2-für-2 erfüllt → auflasten ---
        if (topLast && topBefore) {
            return when (exercise.progression) {
                ProgressionKind.TIME -> {
                    val next = lastBestSecs + exercise.increment.toInt()
                    val extra = if (lastBestSecs >= 60)
                        " Du bist über einer Minute — ab hier lohnt sich Zusatzgewicht statt noch mehr Zeit."
                    else ""
                    Suggestion(
                        exercise, Advice.INCREASE, 0.0, 0, next, exercise.sets,
                        headline = "$next s",
                        reason = "Zweimal in Folge das Ziel gehalten — plus ${exercise.increment.toInt()} Sekunden.$extra",
                        previousHeadline = "$lastBestSecs s"
                    )
                }
                ProgressionKind.REPS -> {
                    if (lastBestReps >= exercise.repMax) {
                        val w = roundToPlate(lastLoad + exercise.increment, exercise.increment)
                        Suggestion(
                            exercise, Advice.INCREASE, w, exercise.repMin, 0, exercise.sets,
                            headline = if (w > 0) "+${fmtKg(w)} Zusatz" else "${exercise.repMax} Wdh.",
                            reason = "Du bist am oberen Ende des Fensters. Jetzt Zusatzgewicht anhängen " +
                                "und wieder bei ${exercise.repMin} Wiederholungen anfangen.",
                            previousHeadline = "$lastBestReps Wdh."
                        )
                    } else {
                        val next = (lastBestReps + 1).coerceAtMost(exercise.repMax)
                        Suggestion(
                            exercise, Advice.INCREASE, lastLoad, next, 0, exercise.sets,
                            headline = "$next Wdh.",
                            reason = "Zweimal sauber durchgezogen — eine Wiederholung mehr pro Satz.",
                            previousHeadline = "$lastBestReps Wdh."
                        )
                    }
                }
                ProgressionKind.WEIGHT -> {
                    val w = roundToPlate(lastLoad + exercise.increment, exercise.increment)
                    Suggestion(
                        exercise, Advice.INCREASE, w, exercise.repMin, 0, exercise.sets,
                        headline = fmtKg(w),
                        reason = "Du hast ${fmtKg(lastLoad)} zweimal in Folge mit ${exercise.repMax} Wiederholungen " +
                            "geschafft. Zeit für ${fmtKg(w)} — die Wiederholungen fallen dabei wieder auf " +
                            "${exercise.repMin}, das ist so gewollt.",
                        previousHeadline = fmtKg(lastLoad)
                    )
                }
            }
        }

        // --- Fall 4: einmal oben, einmal fehlt noch ---
        if (topLast) {
            val nextHint = "Noch eine Einheit auf diesem Niveau, dann wird aufgelastet."
            return when (exercise.progression) {
                ProgressionKind.TIME -> Suggestion(
                    exercise, Advice.HOLD, 0.0, 0, lastBestSecs, exercise.sets,
                    headline = "$lastBestSecs s",
                    reason = "Ziel erreicht. $nextHint"
                )
                else -> Suggestion(
                    exercise, Advice.HOLD, lastLoad, exercise.repMax, 0, exercise.sets,
                    headline = if (exercise.progression == ProgressionKind.WEIGHT) fmtKg(lastLoad)
                               else "${exercise.repMax} Wdh.",
                    reason = "Obere Grenze erreicht. $nextHint"
                )
            }
        }

        // --- Fall 5: Stagnation über mehrere Einheiten → bewusst zurück ---
        val stagnating = hist.size >= 3 && run {
            val loads = hist.take(3).map { workingLoad(it) }
            val bests = hist.take(3).map { s ->
                if (exercise.progression == ProgressionKind.TIME) s.maxOfOrNull { it.seconds } ?: 0
                else s.maxOfOrNull { it.reps } ?: 0
            }
            loads.distinct().size == 1 && bests[0] <= bests[2]
        }
        if (stagnating) {
            return when (exercise.progression) {
                ProgressionKind.TIME -> {
                    val next = max(10, (lastBestSecs * 0.8).roundToInt())
                    Suggestion(
                        exercise, Advice.BACKOFF, 0.0, 0, next, exercise.sets,
                        headline = "$next s",
                        reason = "Drei Einheiten ohne Fortschritt. Geh bewusst zurück und bau die Zeit " +
                            "neu auf — das löst die Blockade meist schneller als Draufhalten.",
                        previousHeadline = "$lastBestSecs s"
                    )
                }
                else -> {
                    val w = roundToPlate(lastLoad * 0.9, profile.plateStepKg)
                    Suggestion(
                        exercise, Advice.BACKOFF, w, exercise.repMin + 2, 0, exercise.sets,
                        headline = if (exercise.progression == ProgressionKind.WEIGHT) fmtKg(w)
                                   else "${exercise.repMin + 2} Wdh.",
                        reason = "Drei Einheiten auf der Stelle. Nimm rund 10 % runter, sammle zwei saubere " +
                            "Einheiten und greif dann wieder an.",
                        previousHeadline = if (exercise.progression == ProgressionKind.WEIGHT) fmtKg(lastLoad) else null
                    )
                }
            }
        }

        // --- Fall 6: normaler Weg — Last halten, eine Wiederholung mehr holen ---
        return when (exercise.progression) {
            ProgressionKind.TIME -> {
                val next = lastBestSecs + 2
                Suggestion(
                    exercise, Advice.HOLD, 0.0, 0, next, exercise.sets,
                    headline = "$next s",
                    reason = "Dranbleiben: zwei Sekunden mehr als beim letzten Mal.",
                    previousHeadline = "$lastBestSecs s"
                )
            }
            else -> {
                val next = (lastBestReps + 1).coerceIn(exercise.repMin, exercise.repMax)
                Suggestion(
                    exercise, Advice.HOLD, lastLoad, next, 0, exercise.sets,
                    headline = if (exercise.progression == ProgressionKind.WEIGHT) fmtKg(lastLoad)
                               else "$next Wdh.",
                    reason = "Gleiche Last, Ziel sind $next Wiederholungen. " +
                        "Bei ${exercise.repMax} in zwei Einheiten kommt Gewicht drauf.",
                    previousHeadline = if (exercise.progression == ProgressionKind.WEIGHT) fmtKg(lastLoad)
                                       else "$lastBestReps Wdh."
                )
            }
        }
    }

    fun suggestAll(
        day: TrainingDay,
        state: AppState,
        now: Long
    ): List<Suggestion> =
        PlanBuilder.exercisesFor(day, state.profile, state.hiddenExercises)
            .map { suggest(it, state.sessions, state.profile, now) }
}

/** Kilogramm hübsch: „25 kg“ statt „25.0 kg“. */
fun fmtKg(kg: Double): String {
    val rounded = (kg * 10).roundToInt() / 10.0
    return if (rounded % 1.0 == 0.0) "${rounded.toInt()} kg" else "$rounded kg"
}
