package at.rudeboy.ferratafit.data

/**
 * Übungen für unterwegs — alles mit dem eigenen Körpergewicht.
 *
 * Gedacht für Reisen, Urlaub oder jeden Tag, an dem das Gerät nicht erreichbar ist.
 * Die Etappe zählt dabei ganz normal: Höhenmeter, Serie und Abzeichen laufen weiter,
 * denn eine Einheit ohne Gerät ist eine Einheit.
 *
 * Wichtig für den Fortschritt: Jede Übung führt ihre eigene Geschichte. Machst du
 * unterwegs Liegestütze statt Brustpresse, wird die Liegestütz-Reihe fortgeschrieben —
 * deine Brustpresse-Lasten bleiben unberührt und stehen zu Hause unverändert bereit.
 * Es geht also auf keiner Seite Fortschritt verloren.
 *
 * Inhaltlich identisch zur Web-Fassung (web/bodyweight.js).
 */
object BodyweightExercises {

    val all: List<Exercise> = listOf(
        Exercise(
            id = "bw_inverted_row",
            name = "Umgekehrtes Rudern",
            station = Station.BODYWEIGHT,
            muscles = listOf(Muscle.BACK, Muscle.BICEPS, Muscle.GRIP),
            sets = 4, repMin = 8, repMax = 15, restSec = 105,
            progression = ProgressionKind.REPS, increment = 2.5, ferrataFocus = 3,
            cue = "Unter einen stabilen Tisch legen, an der Kante hochziehen. Körper bleibt eine Linie.",
            why =
                "Der beste Zug-Ersatz ohne Gerät. Trifft dieselbe Muskulatur wie Latzug und " +
                "Rudern — und die brauchst du am Steig an jeder senkrechten Passage.",
            setup =
                "Ein stabiler Tisch, ein Geländer oder eine tief eingehängte Stange. Du legst " +
                "dich darunter und greifst die Kante etwas weiter als schulterbreit. Je " +
                "waagrechter dein Körper liegt, desto schwerer wird es.",
            variant =
                "Zu schwer? Knie anwinkeln und die Füße näher heranstellen. Zu leicht? Füße auf " +
                "einen zweiten Stuhl legen, sodass der Körper waagrecht liegt.",
            video = "Umgekehrtes Rudern Inverted Row Tisch Technik",
            steps = listOf(
                "Unter die Kante legen, Griff etwas weiter als schulterbreit.",
                "Körper anspannen: Fersen, Gesäß und Schultern bilden eine gerade Linie.",
                "Schulterblätter zusammenziehen, dann die Brust zur Kante ziehen.",
                "Oben kurz halten.",
                "Langsam ablassen, bis die Arme fast gestreckt sind."
            ),
            mistakes = listOf(
                "Die Hüfte durchhängen lassen. Der Körper bleibt ein Brett.",
                "Nur mit den Armen ziehen — erst kommen die Schulterblätter.",
                "Zu hoch greifen und dadurch fast senkrecht stehen. Dann macht die Übung kaum etwas."
            )
        ),
        Exercise(
            id = "bw_towel_row",
            name = "Türrudern mit Handtuch",
            station = Station.BODYWEIGHT,
            muscles = listOf(Muscle.BACK, Muscle.BICEPS, Muscle.GRIP),
            sets = 3, repMin = 10, repMax = 16, restSec = 90,
            progression = ProgressionKind.REPS, increment = 2.5, ferrataFocus = 2,
            cue = "Handtuch um die Türklinke, zurücklehnen und hochziehen.",
            why =
                "Funktioniert in jedem Hotelzimmer. Der Handtuchgriff fordert die Griffkraft " +
                "sogar stärker als eine Stange — genau das, was am Drahtseil zählt.",
            setup =
                "Ein kräftiges Handtuch um beide Türklinken einer geschlossenen Tür schlingen, " +
                "oder um einen stabilen Pfosten. Die Tür muss zu sein und darf nicht aufgehen " +
                "können — stell dich auf der Scharnierseite hin.",
            variant =
                "Schwerer wird es, je weiter du die Füße nach vorne stellst und je tiefer du dich " +
                "lehnst.",
            video = "Türrudern Handtuch Rücken Übung ohne Geräte",
            steps = listOf(
                "Handtuchenden fest greifen, Füße nah an die Tür.",
                "Mit gestreckten Armen zurücklehnen, bis die Arme lang sind.",
                "Körper anspannen und sich zur Tür ziehen, Ellbogen eng am Körper.",
                "Kurz halten, dann langsam zurücklehnen."
            ),
            mistakes = listOf(
                "Eine Tür nehmen, die nachgeben kann. Immer die Scharnierseite wählen.",
                "Mit dem Rücken einrollen statt aufrecht zu bleiben."
            )
        ),
        Exercise(
            id = "bw_superman",
            name = "Superman",
            station = Station.BODYWEIGHT,
            muscles = listOf(Muscle.BACK, Muscle.GLUTES),
            sets = 3, repMin = 10, repMax = 16, restSec = 60,
            progression = ProgressionKind.REPS, increment = 2.5, ferrataFocus = 1,
            cue = "Bauchlage, Arme und Beine gleichzeitig anheben, kurz halten.",
            why = "Kräftigt die Rückenstrecker, die dich mit Rucksack aufrecht halten.",
            setup = "Bauchlage auf einer Matte oder dem Teppich, Arme nach vorne gestreckt.",
            video = "Superman Übung Rückenstrecker Ausführung",
            steps = listOf(
                "Flach auf den Bauch legen, Arme lang nach vorne, Beine gestreckt.",
                "Arme, Brust und Beine gleichzeitig vom Boden abheben.",
                "Oben ein bis zwei Sekunden halten, Blick bleibt zum Boden.",
                "Langsam ablegen."
            ),
            mistakes = listOf(
                "Den Kopf in den Nacken werfen. Der Nacken bleibt in Verlängerung der Wirbelsäule.",
                "Ruckartig hochschnellen."
            )
        ),
        Exercise(
            id = "bw_squat",
            name = "Kniebeuge",
            station = Station.BODYWEIGHT,
            muscles = listOf(Muscle.QUADS, Muscle.GLUTES),
            sets = 4, repMin = 12, repMax = 25, restSec = 90,
            progression = ProgressionKind.REPS, increment = 5.0, ferrataFocus = 2,
            cue = "Füße hüftbreit, Gesäß nach hinten unten, Knie folgen den Fußspitzen.",
            why = "Der Grundstock für alles, was mit Höhenmetern zu tun hat.",
            setup = "Freier Stand, Füße etwa hüft- bis schulterbreit, Fußspitzen leicht nach außen.",
            variant =
                "Zu leicht? Langsamer absenken (drei Sekunden) oder auf einem Bein an einer Wand " +
                "abstützen.",
            video = "Kniebeuge Körpergewicht richtige Technik",
            steps = listOf(
                "Aufrecht stehen, Arme zum Ausbalancieren nach vorne nehmen.",

                    "Gesäß nach hinten schieben und gleichzeitig in die Knie gehen — als wolltest du " +
                    "dich setzen.",
                "So tief, bis die Oberschenkel mindestens waagrecht sind. Fersen bleiben am Boden.",
                "Über die Fersen wieder hochdrücken, oben Gesäß anspannen."
            ),
            mistakes = listOf(
                "Die Fersen abheben. Dann sitzt du zu weit vorne — Gesäß mehr nach hinten schieben.",
                "Die Knie nach innen kippen lassen.",
                "Nur halb tief gehen."
            )
        ),
        Exercise(
            id = "bw_hip_thrust",
            name = "Beckenheben einbeinig",
            station = Station.BODYWEIGHT,
            muscles = listOf(Muscle.HAMSTRINGS, Muscle.GLUTES),
            sets = 3, repMin = 10, repMax = 16, restSec = 90,
            progression = ProgressionKind.REPS, increment = 5.0, ferrataFocus = 2,
            cue = "Rückenlage, ein Fuß aufgestellt, Becken hochdrücken. Pro Seite.",
            why =
                "Ersatz für den Beinbeuger. Hält die Rückseite im Gleichgewicht zur Vorderseite — " +
                "das schützt Knie und Rücken bei steilen Passagen.",
            setup =
                "Rückenlage, ein Fuß etwa 30 cm vom Gesäß aufgestellt, das andere Bein gestreckt " +
                "in die Luft oder angewinkelt abgelegt.",
            counting = "Zähl pro Seite. 12 heißt 12 links und 12 rechts.",
            video = "Einbeiniges Beckenheben Hip Thrust Ausführung",
            steps = listOf(
                "Auf den Rücken legen, einen Fuß aufstellen, Arme seitlich am Boden.",

                    "Ferse in den Boden drücken und das Becken anheben, bis Oberschenkel und Rumpf " +
                    "eine Linie bilden.",
                "Oben das Gesäß fest anspannen und eine Sekunde halten.",
                "Langsam ablassen, ohne das Becken ganz abzulegen.",
                "Alle Wiederholungen auf einer Seite, dann wechseln."
            ),
            mistakes = listOf(
                "Ins Hohlkreuz drücken statt das Gesäß anzuspannen.",
                "Über die Fußspitze drücken. Der Druck kommt von der Ferse."
            )
        ),
        Exercise(
            id = "bw_wall_sit",
            name = "Wandsitz",
            station = Station.BODYWEIGHT,
            muscles = listOf(Muscle.QUADS),
            sets = 3, repMin = 1, repMax = 1, restSec = 75,
            progression = ProgressionKind.TIME, increment = 10.0, ferrataFocus = 2,
            cue = "Mit dem Rücken an die Wand, als säßest du auf einem Stuhl. Zeit halten.",
            why =
                "Genau die Belastung, die beim langen Abstieg auf den Oberschenkel wirkt — dort " +
                "brennt es am Steig zuerst.",
            setup = "Rücken flach an eine Wand, Füße etwa 50 cm davor, hüftbreit.",
            video = "Wandsitz Wall Sit Übung Ausführung",
            steps = listOf(
                "An der Wand nach unten rutschen, bis die Oberschenkel waagrecht sind.",
                "Knie stehen senkrecht über den Fersen, nicht davor.",
                "Rücken bleibt flach an der Wand, Hände locker oder vor der Brust.",
                "Ruhig weiteratmen und die Zeit halten."
            ),
            mistakes = listOf(
                "Sich mit den Händen auf den Oberschenkeln abstützen.",
                "Nicht tief genug — die Oberschenkel sollen waagrecht sein."
            )
        ),
        Exercise(
            id = "bw_pike_pushup",
            name = "Pike-Liegestütz",
            station = Station.BODYWEIGHT,
            muscles = listOf(Muscle.SHOULDERS, Muscle.TRICEPS),
            sets = 3, repMin = 6, repMax = 14, restSec = 105,
            progression = ProgressionKind.REPS, increment = 2.5, ferrataFocus = 2,
            cue = "Umgekehrtes V, Kopf Richtung Boden absenken.",
            why =
                "Ersatz fürs Schulterdrücken. Stabile Schultern tragen den Rucksack und halten " +
                "dich über Kopf an hohen Klammern sicher.",
            setup =
                "Liegestützposition, dann das Gesäß hoch schieben, bis der Körper ein umgekehrtes " +
                "V bildet. Hände etwas weiter als schulterbreit.",
            variant = "Zu schwer? Hände auf eine Erhöhung. Zu leicht? Füße auf einen Stuhl.",
            video = "Pike Push Up Schulter Liegestütz Technik",
            steps = listOf(
                "Ins umgekehrte V gehen, Beine so gestreckt wie es die Beweglichkeit zulässt.",
                "Ellbogen beugen und den Scheitel Richtung Boden zwischen die Hände senken.",
                "Kurz vor dem Boden umkehren und wieder hochdrücken.",
                "Die Hüfte bleibt die ganze Zeit oben."
            ),
            mistakes = listOf(
                "Die Hüfte absinken lassen — dann wird daraus ein normaler Liegestütz.",
                "Den Kopf nach vorne statt nach unten führen."
            )
        ),
        Exercise(
            id = "bw_dips_chair",
            name = "Dips am Stuhl",
            station = Station.BODYWEIGHT,
            muscles = listOf(Muscle.TRICEPS, Muscle.CHEST, Muscle.SHOULDERS),
            sets = 3, repMin = 8, repMax = 16, restSec = 90,
            progression = ProgressionKind.REPS, increment = 2.5, ferrataFocus = 1,
            cue = "Hände auf der Stuhlkante hinter dir, absenken und hochdrücken.",
            why = "Ersatz fürs Trizepsdrücken. Stabilisiert den Ellbogen beim Abstützen am Fels.",
            setup =
                "Ein stabiler Stuhl oder eine Bettkante im Rücken. Hände schulterbreit auf die " +
                "Kante, Finger zeigen nach vorne. Füße nach vorne ausgestreckt.",
            variant = "Zu schwer? Knie anwinkeln und die Füße näher heranstellen.",
            video = "Dips am Stuhl Trizeps Übung Technik",
            steps = listOf(
                "Gesäß von der Kante lösen, Gewicht auf den Händen.",

                    "Ellbogen nach hinten beugen und den Körper absenken, bis der Oberarm etwa " +
                    "waagrecht ist.",
                "Über die Handflächen wieder hochdrücken.",
                "Der Rücken bleibt dicht an der Kante."
            ),
            mistakes = listOf(
                "Die Ellbogen nach außen abspreizen. Sie zeigen nach hinten.",
                "Zu tief gehen, bis es vorne in der Schulter zieht.",
                "Sich vom Stuhl wegbewegen — dann kippt die Belastung ins Gelenk."
            )
        ),
        Exercise(
            id = "bw_pushup_wide",
            name = "Breiter Liegestütz",
            station = Station.BODYWEIGHT,
            muscles = listOf(Muscle.CHEST, Muscle.SHOULDERS),
            sets = 3, repMin = 8, repMax = 20, restSec = 90,
            progression = ProgressionKind.REPS, increment = 2.5, ferrataFocus = 1,
            cue = "Hände deutlich weiter als schulterbreit, Brust tief zum Boden.",
            why =
                "Ersatz für Butterfly und Brustpresse — hält die Schultern im Gleichgewicht zum " +
                "vielen Ziehen.",
            setup = "Liegestützposition, Hände etwa anderthalb Schulterbreiten auseinander.",
            variant = "Zu schwer? Hände auf eine Erhöhung — Tischkante oder Fensterbank.",
            video = "Breiter Liegestütz Brust Ausführung",
            steps = listOf(
                "Körper anspannen, Linie von den Fersen bis zum Kopf.",
                "Absenken, bis die Brust knapp über dem Boden ist.",
                "Kurz halten, dann gleichmäßig hochdrücken."
            ),
            mistakes = listOf(
                "Die Hüfte durchhängen lassen.",
                "Nur halb absenken."
            )
        ),
        Exercise(
            id = "bw_towel_curl",
            name = "Handtuch-Curl",
            station = Station.BODYWEIGHT,
            muscles = listOf(Muscle.BICEPS, Muscle.FOREARMS),
            sets = 3, repMin = 1, repMax = 1, restSec = 75,
            progression = ProgressionKind.TIME, increment = 5.0, ferrataFocus = 2,
            cue = "Handtuch unter den Fuß, dagegen ziehen und die Spannung halten.",
            why =
                "Bizeps und Griffkraft ohne jedes Gerät. Der feste Griff ins Handtuch trainiert " +
                "genau das, was am Drahtseil ermüdet.",
            setup =
                "Ein Handtuch mittig unter einen Fuß klemmen, beide Enden greifen. Aufrecht " +
                "stehen, Ellbogen am Körper.",
            video = "Handtuch Curl isometrisch Bizeps ohne Geräte",
            steps = listOf(
                "Handtuchenden fest greifen, Arme etwa im rechten Winkel.",
                "Mit den Armen nach oben ziehen und gleichzeitig mit dem Fuß dagegenhalten.",
                "Die Spannung aufbauen, bis es deutlich zieht, und halten.",
                "Ruhig weiteratmen, nicht die Luft anhalten."
            ),
            mistakes = listOf(
                "Nur locker ziehen. Der Reiz kommt aus der vollen Anspannung.",
                "Ins Hohlkreuz gehen."
            )
        ),
        Exercise(
            id = "bw_leg_raise",
            name = "Beinheben im Liegen",
            station = Station.BODYWEIGHT,
            muscles = listOf(Muscle.CORE),
            sets = 3, repMin = 10, repMax = 18, restSec = 75,
            progression = ProgressionKind.REPS, increment = 2.5, ferrataFocus = 2,
            cue = "Rückenlage, Beine gestreckt anheben und langsam absenken, ohne abzulegen.",
            why =
                "Ersatz fürs hängende Knieheben. Rumpfspannung hält dich nah an der Wand — wer " +
                "durchhängt, hängt in den Armen.",
            setup =
                "Rückenlage auf einer Matte, Hände flach unter dem Gesäß oder seitlich am Boden. " +
                "Die Hände unter dem Gesäß nehmen Druck von der Lende.",
            variant = "Zu schwer? Knie anwinkeln. Zu leicht? Unten kurz halten, bevor du wieder hochgehst.",
            video = "Beinheben liegend Bauch Übung Technik",
            steps = listOf(
                "Flach auf den Rücken legen, Beine gestreckt, Lende flach an den Boden drücken.",
                "Beide Beine gestreckt anheben, bis sie etwa senkrecht stehen.",

                    "Langsam absenken — zwei bis drei Sekunden — bis die Fersen knapp über dem Boden " +
                    "sind.",
                "Nicht ablegen, sondern direkt die nächste Wiederholung anschließen."
            ),
            mistakes = listOf(

                    "Die Lende vom Boden abheben lassen. Sobald ein Hohlkreuz entsteht, ist es zu " +
                    "schwer — dann die Knie leicht beugen.",
                "Die Beine fallen lassen statt kontrolliert abzusenken."
            )
        ),
    )

    /**
     * Was ersetzt was, wenn kein Gerät da ist.
     *
     * Der Ersatz trifft dieselbe Muskulatur; er ist keine exakte Kopie, sondern die beste
     * Annäherung mit dem eigenen Körpergewicht. Auch die Klimmzugstange gilt als Gerät —
     * unterwegs ist sie selten vorhanden.
     */
    val substitutes: Map<String, String> = mapOf(
        "pullup" to "bw_inverted_row",
        "latpull_wide" to "bw_inverted_row",
        "row_cable" to "bw_inverted_row",
        "row_lat_narrow" to "bw_towel_row",
        "curl" to "bw_towel_curl",
        "curl_bw" to "bw_towel_curl",
        "deadhang" to "bw_towel_curl",
        "hang_knee_raise" to "bw_leg_raise",
        "leg_ext" to "bw_squat",
        "leg_curl" to "bw_hip_thrust",
        "chest_press" to "bw_pushup_wide",
        "butterfly" to "bw_pushup_wide",
        "shoulder_press" to "bw_pike_pushup",
        "triceps" to "bw_dips_chair",
        "reverse_fly" to "bw_superman",
        "farmer_hold" to "bw_towel_curl",
    )

    fun byId(id: String): Exercise? = all.firstOrNull { it.id == id }

    /** Der Ersatz für eine Geräteübung, falls es einen gibt. */
    fun substituteFor(exerciseId: String): Exercise? = substitutes[exerciseId]?.let { byId(it) }
}
