package at.rudeboy.ferratafit.data

/**
 * Übungskatalog und Split.
 *
 * Inhaltliche Grundlage (siehe TRAININGSWISSEN.md im Projektordner):
 *  - Klettersteig fordert vor allem Griffkraft/Unterarm-Ausdauer, Zugkraft im Oberkörper,
 *    Rumpfspannung und Beinkraft für hohe Tritte. Deshalb sitzen Dead Hang, Klimmzug und
 *    Step-up weit vorne in den Einheiten.
 *  - Wiederholungsfenster liegen bewusst im Bereich 8–15 (Kraftausdauer/leichte Definition)
 *    statt im schweren 3–5er-Bereich — das passt zum Ziel „Allgemeinfitness, nicht Masse“.
 */
object Catalog {

    val exercises: List<Exercise> = listOf(

        // ---------- Tag A: Zug & Griffkraft (der Klettersteig-Kern) ----------
        Exercise(
            id = "pullup",
            name = "Klimmzug",
            station = Station.PULLUP_BAR,
            muscles = listOf(Muscle.BACK, Muscle.BICEPS, Muscle.GRIP),
            sets = 4, repMin = 3, repMax = 10, restSec = 150,
            progression = ProgressionKind.REPS, increment = 2.5, ferrataFocus = 3,
            cue = "Schulterblätter zuerst nach unten ziehen, dann erst die Arme beugen. " +
                "Wenn du noch keinen sauberen schaffst: Sprung nach oben und 5 Sekunden langsam ablassen.",
            why = "Die direkteste Übertragung auf den Steig — genau diese Bewegung ziehst du an " +
                "senkrechten Passagen an den Eisenklammern."
        ),
        Exercise(
            id = "latpull_wide",
            name = "Latzug breit",
            station = Station.LAT_PULLDOWN,
            muscles = listOf(Muscle.BACK, Muscle.BICEPS),
            sets = 3, repMin = 8, repMax = 12, restSec = 120,
            progression = ProgressionKind.WEIGHT, increment = 5.0, ferrataFocus = 3,
            cue = "Oberkörper leicht zurück, Stange zum Schlüsselbein. Nicht mit Schwung reißen.",
            why = "Baut die Zugkraft auf, die dich über Überhänge bringt, und entlastet " +
                "die Arme, weil der Rücken die Arbeit übernimmt."
        ),
        Exercise(
            id = "row_cable",
            name = "Rudern am Kabel",
            station = Station.CABLE_LOW,
            muscles = listOf(Muscle.BACK, Muscle.BICEPS, Muscle.CORE),
            sets = 3, repMin = 10, repMax = 14, restSec = 105,
            progression = ProgressionKind.WEIGHT, increment = 5.0, ferrataFocus = 2,
            cue = "Rücken gerade, Ellbogen eng am Körper nach hinten, kurz halten.",
            why = "Stärkt die obere Rückenpartie — die hält dich am Steig aufrecht, " +
                "wenn der Rucksack nach hinten zieht."
        ),
        Exercise(
            id = "row_lat_narrow",
            name = "Enger Zug zum Körper",
            station = Station.LAT_PULLDOWN,
            muscles = listOf(Muscle.BACK, Muscle.BICEPS),
            sets = 3, repMin = 10, repMax = 14, restSec = 105,
            progression = ProgressionKind.WEIGHT, increment = 5.0, ferrataFocus = 2,
            cue = "Enger Griff, Ellbogen dicht am Rumpf nach unten ziehen.",
            why = "Ersatz fürs Kabelrudern, wenn kein unterer Zugpunkt da ist — " +
                "trifft dieselbe Zugmuskulatur."
        ),
        Exercise(
            id = "curl",
            name = "Bizeps-Curl",
            station = Station.CABLE_LOW,
            muscles = listOf(Muscle.BICEPS, Muscle.FOREARMS),
            sets = 3, repMin = 10, repMax = 14, restSec = 90,
            progression = ProgressionKind.WEIGHT, increment = 2.5, ferrataFocus = 1,
            cue = "Ellbogen bleiben am Körper fixiert, oben kurz anspannen.",
            why = "Unterstützt das Halten und Ziehen an den Klammern und beugt " +
                "Überlastung der Ellbogensehnen vor."
        ),
        Exercise(
            id = "curl_bw",
            name = "Curl am Latzug",
            station = Station.LAT_PULLDOWN,
            muscles = listOf(Muscle.BICEPS, Muscle.FOREARMS),
            sets = 3, repMin = 10, repMax = 14, restSec = 90,
            progression = ProgressionKind.WEIGHT, increment = 2.5, ferrataFocus = 1,
            cue = "Untergriff an der Stange, nur die Unterarme bewegen sich.",
            why = "Alternative, wenn kein unterer Kabelzug vorhanden ist."
        ),
        Exercise(
            id = "deadhang",
            name = "Dead Hang",
            station = Station.PULLUP_BAR,
            muscles = listOf(Muscle.GRIP, Muscle.FOREARMS, Muscle.CORE),
            sets = 4, repMin = 1, repMax = 1, restSec = 90,
            progression = ProgressionKind.TIME, increment = 5.0, ferrataFocus = 3,
            cue = "Locker hängen, Schultern aktiv nach unten ziehen (nicht in den Gelenken hängen). " +
                "Abbrechen, bevor der Griff komplett aufgeht.",
            why = "Der Klassiker gegen den „Pump“ in den Unterarmen. Genau das ist der Grund, " +
                "warum Einsteiger am Steig alle 10–15 Minuten pausieren müssen."
        ),
        Exercise(
            id = "hang_knee_raise",
            name = "Hängendes Knieheben",
            station = Station.PULLUP_BAR,
            muscles = listOf(Muscle.CORE, Muscle.GRIP),
            sets = 3, repMin = 8, repMax = 15, restSec = 90,
            progression = ProgressionKind.REPS, increment = 2.5, ferrataFocus = 3,
            cue = "Kein Schwingen — Knie kontrolliert zur Brust, langsam ablassen.",
            why = "Rumpfspannung plus Griffkraft in einer Übung. Beides brauchst du gleichzeitig, " +
                "wenn du am Überhang die Beine nachziehst."
        ),

        // ---------- Tag B: Beine & Steigkraft ----------
        Exercise(
            id = "stepup",
            name = "Step-up (hohe Stufe)",
            station = Station.BODYWEIGHT,
            muscles = listOf(Muscle.QUADS, Muscle.GLUTES, Muscle.CALVES),
            sets = 4, repMin = 8, repMax = 15, restSec = 105,
            progression = ProgressionKind.REPS, increment = 5.0, ferrataFocus = 3,
            cue = "Stufe möglichst kniehoch. Ganzes Gewicht auf den oberen Fuß, " +
                "nicht mit dem hinteren Bein abdrücken. Langsam ablassen.",
            why = "Die spezifischste Beinübung überhaupt für den Steig — genau so steigst du " +
                "von Klammer zu Klammer. Trainiert zusätzlich das Gleichgewicht auf einem Bein."
        ),
        Exercise(
            id = "split_squat",
            name = "Ausfallschritt statisch",
            station = Station.BODYWEIGHT,
            muscles = listOf(Muscle.QUADS, Muscle.GLUTES, Muscle.CORE),
            sets = 3, repMin = 8, repMax = 12, restSec = 105,
            progression = ProgressionKind.REPS, increment = 5.0, ferrataFocus = 2,
            cue = "Hinteres Knie Richtung Boden, Oberkörper aufrecht. Pro Seite zählen.",
            why = "Einbeinige Kraft und Stabilität — am Fels stehst du selten auf beiden Beinen."
        ),
        Exercise(
            id = "leg_ext",
            name = "Beinstrecker",
            station = Station.LEG_EXTENSION,
            muscles = listOf(Muscle.QUADS),
            sets = 3, repMin = 10, repMax = 15, restSec = 90,
            progression = ProgressionKind.WEIGHT, increment = 5.0, ferrataFocus = 2,
            cue = "Oben 1 Sekunde halten, langsam zurück. Knie nicht durchschlagen lassen.",
            why = "Kräftigt den Oberschenkel gezielt und stabilisiert das Knie — " +
                "wichtig für lange Abstiege, die oft härter sind als der Aufstieg."
        ),
        Exercise(
            id = "leg_curl",
            name = "Beinbeuger",
            station = Station.LEG_CURL,
            muscles = listOf(Muscle.HAMSTRINGS),
            sets = 3, repMin = 10, repMax = 15, restSec = 90,
            progression = ProgressionKind.WEIGHT, increment = 5.0, ferrataFocus = 2,
            cue = "Ferse kontrolliert zum Gesäß, ohne das Becken abzuheben.",
            why = "Gegenspieler zum Oberschenkel vorne. Das Gleichgewicht schützt Knie " +
                "und Rücken bei steilen Passagen."
        ),
        Exercise(
            id = "calf_raise",
            name = "Wadenheben",
            station = Station.BODYWEIGHT,
            muscles = listOf(Muscle.CALVES),
            sets = 3, repMin = 12, repMax = 20, restSec = 60,
            progression = ProgressionKind.REPS, increment = 5.0, ferrataFocus = 2,
            cue = "Auf einer Stufe, Ferse tief ablassen, oben ganz durchdrücken.",
            why = "Am Steig stehst du oft nur mit dem Fußballen auf einer schmalen Klammer. " +
                "Starke Waden verhindern, dass die Füße nach einer Stunde zittern."
        ),
        Exercise(
            id = "plank",
            name = "Unterarmstütz",
            station = Station.BODYWEIGHT,
            muscles = listOf(Muscle.CORE),
            sets = 3, repMin = 1, repMax = 1, restSec = 60,
            progression = ProgressionKind.TIME, increment = 10.0, ferrataFocus = 2,
            cue = "Gesäß anspannen, Körper bildet eine Linie. Kein Hohlkreuz.",
            why = "Rumpfspannung hält dich nah an der Wand. Wer durchhängt, hängt in den Armen " +
                "und ermüdet doppelt so schnell."
        ),
        Exercise(
            id = "side_plank",
            name = "Seitstütz",
            station = Station.BODYWEIGHT,
            muscles = listOf(Muscle.CORE),
            sets = 2, repMin = 1, repMax = 1, restSec = 45,
            progression = ProgressionKind.TIME, increment = 10.0, ferrataFocus = 1,
            cue = "Hüfte hoch, Körper in einer Linie. Pro Seite.",
            why = "Seitliche Rumpfkette — die stabilisiert dich bei Quergängen."
        ),

        // ---------- Tag C: Druck & Stabilität ----------
        Exercise(
            id = "chest_press",
            name = "Brustpresse",
            station = Station.CHEST_PRESS,
            muscles = listOf(Muscle.CHEST, Muscle.SHOULDERS, Muscle.TRICEPS),
            sets = 3, repMin = 8, repMax = 12, restSec = 120,
            progression = ProgressionKind.WEIGHT, increment = 5.0, ferrataFocus = 1,
            cue = "Schulterblätter zusammen und unten lassen, kontrolliert zurückkommen.",
            why = "Sorgt für Muskelbalance zum vielen Ziehen. Ein reines Zug-Programm " +
                "zieht die Schultern langfristig nach vorne."
        ),
        Exercise(
            id = "butterfly",
            name = "Butterfly",
            station = Station.BUTTERFLY,
            muscles = listOf(Muscle.CHEST),
            sets = 3, repMin = 10, repMax = 14, restSec = 90,
            progression = ProgressionKind.WEIGHT, increment = 5.0, ferrataFocus = 1,
            cue = "Leichte Ellbogenbeugung beibehalten, vorne kurz halten.",
            why = "Formt die Brust — der Definitionsanteil deines Ziels."
        ),
        Exercise(
            id = "pushup",
            name = "Liegestütz",
            station = Station.BODYWEIGHT,
            muscles = listOf(Muscle.CHEST, Muscle.TRICEPS, Muscle.CORE),
            sets = 3, repMin = 8, repMax = 20, restSec = 90,
            progression = ProgressionKind.REPS, increment = 2.5, ferrataFocus = 1,
            cue = "Körper bleibt eine Linie, Ellbogen etwa 45° zum Rumpf.",
            why = "Drückbewegung ohne Gerät — und trainiert die Rumpfspannung gleich mit."
        ),
        Exercise(
            id = "shoulder_press",
            name = "Schulterdrücken",
            station = Station.CHEST_PRESS,
            muscles = listOf(Muscle.SHOULDERS, Muscle.TRICEPS),
            sets = 3, repMin = 8, repMax = 12, restSec = 105,
            progression = ProgressionKind.WEIGHT, increment = 2.5, ferrataFocus = 2,
            cue = "Rippen unten lassen, nicht ins Hohlkreuz ausweichen.",
            why = "Stabile Schultern tragen den Rucksack und halten dich über Kopf " +
                "an hohen Klammern sicher."
        ),
        Exercise(
            id = "triceps",
            name = "Trizepsdrücken",
            station = Station.LAT_PULLDOWN,
            muscles = listOf(Muscle.TRICEPS),
            sets = 3, repMin = 10, repMax = 14, restSec = 75,
            progression = ProgressionKind.WEIGHT, increment = 2.5, ferrataFocus = 1,
            cue = "Ellbogen fix am Körper, nur die Unterarme arbeiten.",
            why = "Ergänzt die Drückkette und stabilisiert den Ellbogen beim Abstützen."
        ),
        Exercise(
            id = "farmer_hold",
            name = "Farmer's Hold",
            station = Station.DUMBBELL,
            muscles = listOf(Muscle.GRIP, Muscle.FOREARMS, Muscle.CORE),
            sets = 3, repMin = 1, repMax = 1, restSec = 90,
            progression = ProgressionKind.TIME, increment = 10.0, ferrataFocus = 3,
            cue = "Schwere Hanteln, aufrecht stehen oder gehen, Schultern unten.",
            why = "Griffkraft unter Last — direkt übertragbar aufs Festhalten am Drahtseil."
        ),
        Exercise(
            id = "reverse_fly",
            name = "Reverse Butterfly",
            station = Station.BUTTERFLY,
            muscles = listOf(Muscle.SHOULDERS, Muscle.BACK),
            sets = 3, repMin = 12, repMax = 16, restSec = 75,
            progression = ProgressionKind.WEIGHT, increment = 2.5, ferrataFocus = 2,
            cue = "Arme nach hinten öffnen, Schulterblätter zusammenführen.",
            why = "Hintere Schulter und oberer Rücken — die Haltungsversicherung " +
                "gegen den Rundrücken vom Rucksacktragen."
        )
    )

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
