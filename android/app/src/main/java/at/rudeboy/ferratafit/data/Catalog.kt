package at.rudeboy.ferratafit.data

/**
 * Übungskatalog und Split.
 *
 * Inhaltliche Grundlage (siehe docs/TRAININGSWISSEN.md):
 *  - Klettersteig fordert vor allem Griffkraft/Unterarm-Ausdauer, Zugkraft im Oberkörper,
 *    Rumpfspannung und Beinkraft für hohe Tritte. Deshalb sitzen Dead Hang, Klimmzug und
 *    Step-up weit vorne in den Einheiten.
 *  - Wiederholungsfenster liegen bewusst im Bereich 8–15 (Kraftausdauer/leichte Definition)
 *    statt im schweren 3–5er-Bereich — das passt zum Ziel „Allgemeinfitness, nicht Masse“.
 */
object Catalog {

    /** Der Katalog liegt in [Exercises] — hier bleiben Split und Zusammenstellung. */
    val exercises: List<Exercise> get() = Exercises.all

    fun byId(id: String): Exercise? = exercises.firstOrNull { it.id == id }

    /**
     * Der 3-Tage-Split. Reihenfolge der IDs = Reihenfolge in der Einheit;
     * klettersteigrelevante Übungen stehen bewusst vorne.
     *
     * Pro Tag sind mehr Übungen hinterlegt, als tatsächlich trainiert werden —
     * [PlanBuilder] filtert nach vorhandenen Stationen und wirft Dubletten raus.
     */
    val days: List<TrainingDay> = listOf(
        TrainingDay(
            id = "A",
            title = "Zug & Griff",
            subtitle = "Der Klettersteig-Tag",
            focus = listOf(Muscle.BACK, Muscle.BICEPS, Muscle.GRIP),
            exerciseIds = listOf(
                "pullup", "latpull_wide", "row_cable", "row_lat_narrow",
                "hang_knee_raise", "curl", "curl_bw", "deadhang"
            )
        ),
        TrainingDay(
            id = "B",
            title = "Beine & Steigkraft",
            subtitle = "Höhenmeter-Motor",
            focus = listOf(Muscle.QUADS, Muscle.HAMSTRINGS, Muscle.CALVES, Muscle.CORE),
            exerciseIds = listOf(
                "stepup", "leg_ext", "leg_curl", "split_squat",
                "calf_raise", "plank", "side_plank"
            )
        ),
        TrainingDay(
            id = "C",
            title = "Druck & Stabilität",
            subtitle = "Balance und Haltung",
            focus = listOf(Muscle.CHEST, Muscle.SHOULDERS, Muscle.TRICEPS, Muscle.CORE),
            exerciseIds = listOf(
                "chest_press", "shoulder_press", "butterfly", "pushup",
                "reverse_fly", "triceps", "farmer_hold", "deadhang"
            )
        )
    )

    fun day(id: String): TrainingDay = days.first { it.id == id }
}

/**
 * Baut aus dem Katalog den konkreten Plan für die vorhandenen Stationen.
 *
 * Zwei Regeln halten die Einheit kurz und sinnvoll:
 *  - Übungen für nicht vorhandene Stationen fallen weg.
 *  - Wenn zwei Übungen dieselbe Muskelgruppe auf dieselbe Art treffen (z. B. Kabelrudern
 *    und enger Latzug), bleibt nur die erste — die zweite ist als Ersatz gedacht, nicht als Zusatz.
 */
object PlanBuilder {

    /** Paare aus Übung und ihrem Ersatz. Ist die erste verfügbar, fliegt die zweite raus. */
    private val alternatives = listOf(
        "row_cable" to "row_lat_narrow",
        "curl" to "curl_bw",
        "chest_press" to "pushup",
        "butterfly" to "pushup"
    )

    fun exercisesFor(day: TrainingDay, profile: Profile, hidden: Set<String> = emptySet()): List<Exercise> {
        val available = day.exerciseIds
            .mapNotNull { Catalog.byId(it) }
            .filter { it.station in profile.stations }
            .filter { it.id !in hidden }

        val drop = mutableSetOf<String>()
        for ((primary, fallback) in alternatives) {
            if (available.any { it.id == primary }) drop += fallback
        }
        return available.filter { it.id !in drop }
    }

    /** Alle Übungen, die mit der aktuellen Ausstattung überhaupt trainierbar sind. */
    fun allAvailable(profile: Profile): List<Exercise> =
        Catalog.exercises.filter { it.station in profile.stations }
}
