package at.rudeboy.ferratafit.data

/**
 * Übungskatalog mit vollständigen Ausführungsanleitungen.
 *
 * Inhaltlich identisch zur Web-Fassung (web/exercises.js) — wer hier etwas ändert,
 * sollte es dort ebenfalls anpassen, sonst laufen die beiden Varianten auseinander.
 */
object Exercises {

    val all: List<Exercise> = listOf(
        Exercise(
            id = "pullup",
            name = "Klimmzug",
            station = Station.PULLUP_BAR,
            muscles = listOf(Muscle.BACK, Muscle.BICEPS, Muscle.GRIP),
            sets = 4, repMin = 3, repMax = 10, restSec = 150,
            progression = ProgressionKind.REPS, increment = 2.5, ferrataFocus = 3,
            cue = "Schulterblätter zuerst nach unten ziehen, dann erst die Arme beugen.",
            why =
                "Die direkteste Übertragung auf den Steig — genau diese Bewegung ziehst du an " +
                "senkrechten Passagen an den Eisenklammern.",
            setup =
                "Stange im Obergriff fassen, Hände etwa schulterbreit oder eine Handbreit weiter. " +
                "Frei hängen lassen, Beine gestreckt oder angewinkelt überkreuzt.",
            variant =
                "Schaffst du noch keinen? Stell dich auf einen Stuhl, spring in die obere " +
                "Position und lass dich fünf Sekunden lang so langsam wie möglich ab. Diese " +
                "negativen Wiederholungen zählen genauso — trag sie einfach ein.",
            video = "Klimmzug richtige Technik Anfänger",
            steps = listOf(
                "Aus dem freien Hang starten, Arme lang, Schultern locker.",

                    "Erst die Schulterblätter nach unten ziehen, als wolltest du sie in die " +
                    "Gesäßtaschen stecken. Der Körper hebt sich dabei schon ein paar Zentimeter.",

                    "Jetzt die Arme beugen und die Brust Richtung Stange ziehen. Ellbogen wandern " +
                    "nach unten-hinten, nicht nach außen.",
                "Oben ist das Kinn über der Stange, kurz halten.",
                "Kontrolliert ablassen — etwa zwei Sekunden — bis die Arme wieder lang sind."
            ),
            mistakes = listOf(

                    "Mit den Beinen schwingen, um Schwung zu holen. Lieber weniger Wiederholungen, " +
                    "dafür sauber.",
                "Nur halb ablassen. Der volle Hang unten ist der Teil, der die Griffkraft baut.",
                "Kopf in den Nacken werfen, um höher zu wirken."
            )
        ),
        Exercise(
            id = "latpull_wide",
            name = "Latzug breit",
            station = Station.LAT_PULLDOWN,
            muscles = listOf(Muscle.BACK, Muscle.BICEPS),
            sets = 3, repMin = 8, repMax = 12, restSec = 120,
            progression = ProgressionKind.WEIGHT, increment = 5.0, ferrataFocus = 3,
            cue = "Oberkörper leicht zurück, Stange zum Schlüsselbein. Nicht mit Schwung reißen.",
            why =
                "Baut die Zugkraft auf, die dich über Überhänge bringt, und entlastet die Arme, " +
                "weil der Rücken die Arbeit übernimmt.",
            setup =
                "Beinpolster so einstellen, dass die Oberschenkel fest darunter klemmen — sonst " +
                "hebt es dich beim Ziehen vom Sitz. Stange im Obergriff, Hände deutlich weiter " +
                "als schulterbreit.",
            video = "Latzug richtig ausführen Technik",
            steps = listOf(
                "Aufrecht hinsetzen, Oberschenkel unter das Polster, Arme oben lang.",

                    "Brust rausstrecken und den Oberkörper etwa 10 bis 20 Grad zurücklehnen. Diese " +
                    "Position hältst du während des ganzen Satzes.",

                    "Schulterblätter zusammen und nach unten ziehen, dann die Stange zum oberen " +
                    "Brustbein bringen.",
                "Unten kurz halten und die Spannung im Rücken spüren.",

                    "Langsam nach oben zurückführen, bis die Arme fast gestreckt sind — dabei nicht " +
                    "die Spannung verlieren."
            ),
            mistakes = listOf(

                    "Den Oberkörper bei jeder Wiederholung nach hinten reißen. Der Lehnwinkel bleibt " +
                    "konstant.",

                    "Die Stange in den Nacken ziehen. Das belastet die Schulter unnötig und bringt " +
                    "nichts extra.",
                "Nur mit den Armen ziehen. Erst kommen die Schulterblätter, dann die Arme."
            )
        ),
        Exercise(
            id = "row_cable",
            name = "Rudern am Kabel",
            station = Station.CABLE_LOW,
            muscles = listOf(Muscle.BACK, Muscle.BICEPS, Muscle.CORE),
            sets = 3, repMin = 10, repMax = 14, restSec = 105,
            progression = ProgressionKind.WEIGHT, increment = 5.0, ferrataFocus = 2,
            cue = "Rücken gerade, Ellbogen eng am Körper nach hinten, kurz halten.",
            why =
                "Stärkt die obere Rückenpartie — die hält dich am Steig aufrecht, wenn der " +
                "Rucksack nach hinten zieht.",
            setup =
                "Am unteren Zugpunkt sitzen, Füße gegen die Stützen, Knie leicht gebeugt. Griff " +
                "mit beiden Händen fassen, Oberkörper aufrecht.",
            video = "Rudern am Kabelzug sitzend Technik",
            steps = listOf(
                "Aufrecht sitzen, Arme nach vorne gestreckt, Rücken gerade — kein Rundrücken.",
                "Brust rausstrecken, Schulterblätter zusammenziehen.",

                    "Griff zum Bauchnabel ziehen, Ellbogen bleiben dicht am Körper und wandern nach " +
                    "hinten.",
                "Hinten eine Sekunde halten, Schulterblätter maximal zusammen.",
                "Kontrolliert zurückführen, bis die Arme lang sind. Der Oberkörper bleibt aufrecht."
            ),
            mistakes = listOf(
                "Mit dem Oberkörper vor- und zurückschaukeln, um mehr Gewicht zu bewegen.",
                "Runder Rücken beim Zurückführen — das ist die Position, in der man sich verhebt.",
                "Ellbogen nach außen abspreizen. Eng am Körper trifft den Rücken besser."
            )
        ),
        Exercise(
            id = "row_lat_narrow",
            name = "Enger Zug zum Körper",
            station = Station.LAT_PULLDOWN,
            muscles = listOf(Muscle.BACK, Muscle.BICEPS),
            sets = 3, repMin = 10, repMax = 14, restSec = 105,
            progression = ProgressionKind.WEIGHT, increment = 5.0, ferrataFocus = 2,
            cue = "Enger Griff, Ellbogen dicht am Rumpf nach unten ziehen.",
            why =
                "Ersatz fürs Kabelrudern, wenn kein unterer Zugpunkt da ist — trifft dieselbe " +
                "Zugmuskulatur.",
            setup =
                "Wie beim Latzug, aber mit engem Griff — Hände etwa schulterbreit oder enger, " +
                "falls vorhanden mit dem V-Griff.",
            video = "Latzug enger Griff Technik",
            steps = listOf(
                "Hinsetzen, Oberschenkel unter das Polster, aufrecht sitzen.",
                "Griff fassen, Arme oben lang, Brust raus.",

                    "Ellbogen dicht am Rumpf nach unten und hinten ziehen, bis die Hände auf " +
                    "Brusthöhe sind.",
                "Kurz halten, dann langsam nach oben zurückführen."
            ),
            mistakes = listOf(
                "Zu weit zurücklehnen — bei der engen Variante bleibst du fast senkrecht.",
                "Die Bewegung aus dem Bizeps machen statt aus dem Rücken."
            )
        ),
        Exercise(
            id = "hang_knee_raise",
            name = "Hängendes Knieheben",
            station = Station.PULLUP_BAR,
            muscles = listOf(Muscle.CORE, Muscle.GRIP),
            sets = 3, repMin = 8, repMax = 15, restSec = 90,
            progression = ProgressionKind.REPS, increment = 2.5, ferrataFocus = 3,
            cue = "Kein Schwingen — Knie kontrolliert zur Brust, langsam ablassen.",
            why =
                "Rumpfspannung plus Griffkraft in einer Übung. Beides brauchst du gleichzeitig, " +
                "wenn du am Überhang die Beine nachziehst.",
            setup =
                "An der Klimmzugstange hängen, Hände schulterbreit im Obergriff. Körper ruhig " +
                "werden lassen, bevor du startest.",
            variant =
                "Zu schwer? Mach dasselbe im Liegen auf dem Rücken — Beine anheben und wieder " +
                "absenken, ohne die Fersen abzulegen.",
            video = "Hängendes Knieheben Technik Bauch",
            steps = listOf(

                    "Frei hängen, Schultern aktiv nach unten ziehen — nicht passiv in den Gelenken " +
                    "hängen.",
                "Bauch anspannen, dann beide Knie gleichzeitig Richtung Brust ziehen.",

                    "Oben ist der Oberschenkel mindestens waagrecht. Wer weiter kommt: Knie bis zur " +
                    "Brusthöhe.",
                "Kurz halten, dann die Beine langsam wieder strecken — etwa zwei Sekunden.",
                "Unten nicht schwingen lassen. Kurz stabilisieren, dann die nächste Wiederholung."
            ),
            mistakes = listOf(
                "Mit Schwung arbeiten. Wenn der Körper pendelt, macht der Bauch kaum noch mit.",
                "Nur die Knie leicht anheben. Der Oberschenkel sollte mindestens waagrecht werden.",
                "Die Luft anhalten. Beim Anheben ausatmen."
            )
        ),
        Exercise(
            id = "curl",
            name = "Bizeps-Curl",
            station = Station.CABLE_LOW,
            muscles = listOf(Muscle.BICEPS, Muscle.FOREARMS),
            sets = 3, repMin = 10, repMax = 14, restSec = 90,
            progression = ProgressionKind.WEIGHT, increment = 2.5, ferrataFocus = 1,
            cue = "Ellbogen bleiben am Körper fixiert, oben kurz anspannen.",
            why =
                "Unterstützt das Halten und Ziehen an den Klammern und beugt Überlastung der " +
                "Ellbogensehnen vor.",
            setup =
                "Vor dem unteren Zugpunkt stehen, Griff im Untergriff fassen, Arme hängen lang. " +
                "Füße hüftbreit, Knie minimal gebeugt.",
            video = "Bizeps Curl Kabelzug richtige Ausführung",
            steps = listOf(

                    "Aufrecht stehen, Ellbogen dicht an den Rippen fixieren — sie bleiben dort den " +
                    "ganzen Satz.",
                "Nur die Unterarme bewegen sich: Griff nach oben zu den Schultern führen.",
                "Oben eine Sekunde anspannen.",
                "Langsam ablassen, bis die Arme fast gestreckt sind."
            ),
            mistakes = listOf(
                "Die Ellbogen nach vorne wandern lassen. Dann übernimmt die Schulter.",
                "Mit dem Oberkörper nachhelfen und den Rücken ins Hohlkreuz drücken.",
                "Zu schnell ablassen — die Absenkphase ist die Hälfte der Arbeit."
            )
        ),
        Exercise(
            id = "curl_bw",
            name = "Curl am Latzug",
            station = Station.LAT_PULLDOWN,
            muscles = listOf(Muscle.BICEPS, Muscle.FOREARMS),
            sets = 3, repMin = 10, repMax = 14, restSec = 90,
            progression = ProgressionKind.WEIGHT, increment = 2.5, ferrataFocus = 1,
            cue = "Untergriff an der Stange, nur die Unterarme bewegen sich.",
            why = "Alternative, wenn kein unterer Kabelzug vorhanden ist.",
            setup =
                "Am Latzug sitzen, Stange im Untergriff (Handflächen zeigen zu dir), Hände " +
                "schulterbreit. Oberschenkel unter dem Polster.",
            video = "Bizeps Curl am Latzug Ausführung",
            steps = listOf(
                "Aufrecht sitzen, Arme nach oben gestreckt.",

                    "Ellbogen auf Höhe halten und nur die Unterarme beugen, sodass die Stange zum " +
                    "Kopf kommt.",
                "Kurz anspannen, dann kontrolliert strecken."
            ),
            mistakes = listOf(
                "Den ganzen Arm ziehen statt nur zu beugen — dann ist es ein Latzug, kein Curl.",
                "Zu viel Gewicht. Der Bizeps allein trägt deutlich weniger als der Rücken."
            )
        ),
        Exercise(
            id = "deadhang",
            name = "Dead Hang",
            station = Station.PULLUP_BAR,
            muscles = listOf(Muscle.GRIP, Muscle.FOREARMS, Muscle.CORE),
            sets = 4, repMin = 1, repMax = 1, restSec = 90,
            progression = ProgressionKind.TIME, increment = 5.0, ferrataFocus = 3,
            cue =
                "Locker hängen, Schultern aktiv nach unten ziehen. Abbrechen, bevor der Griff " +
                "aufgeht.",
            why =
                "Der Klassiker gegen den „Pump\" in den Unterarmen. Genau das ist der Grund, " +
                "warum Einsteiger am Steig alle 10–15 Minuten pausieren müssen.",
            setup =
                "Klimmzugstange, Hände schulterbreit im Obergriff, Daumen umschließt die Stange. " +
                "Etwas zum Draufsteigen ist praktisch, damit du nicht springen musst.",
            variant =
                "Zu schwer? Lass die Füße locker am Boden und nimm nur einen Teil des Gewichts " +
                "auf die Arme. Die Zeit zählt trotzdem.",
            video = "Dead Hang richtig hängen Griffkraft",
            steps = listOf(
                "Stange fassen und die Füße vom Boden lösen. Beine gestreckt oder leicht angewinkelt.",

                    "Schultern aktiv nach unten ziehen, sodass Abstand zwischen Ohren und Schultern " +
                    "entsteht. Du hängst nicht schlaff in den Gelenken.",
                "Ruhig weiteratmen und die Zeit halten. Der Blick geht geradeaus.",
                "Abbrechen, sobald der Griff spürbar aufgeht — nicht erst, wenn du abrutschst."
            ),
            mistakes = listOf(
                "Passiv in den Schultern hängen. Das belastet die Gelenke und trainiert weniger.",
                "Die Luft anhalten und verkrampfen.",
                "Bis zum Abrutschen halten. Kontrolliert loslassen ist sicherer."
            )
        ),
        Exercise(
            id = "stepup",
            name = "Step-up (hohe Stufe)",
            station = Station.BODYWEIGHT,
            muscles = listOf(Muscle.QUADS, Muscle.GLUTES, Muscle.CALVES),
            sets = 4, repMin = 8, repMax = 15, restSec = 105,
            progression = ProgressionKind.REPS, increment = 5.0, ferrataFocus = 3,
            cue = "Ganzes Gewicht auf den oberen Fuß, nicht mit dem hinteren Bein abdrücken.",
            why =
                "Die spezifischste Beinübung überhaupt für den Steig — genau so steigst du von " +
                "Klammer zu Klammer. Trainiert zusätzlich das Gleichgewicht auf einem Bein.",
            setup =
                "Du brauchst eine stabile Erhöhung: Treppenstufe, Hantelbank, Kiste oder ein " +
                "Trittschemel. Die richtige Höhe ist erreicht, wenn dein Oberschenkel etwa " +
                "waagrecht steht, sobald der Fuß oben ist — also ungefähr Kniehöhe. Stell dich " +
                "aufrecht davor.",
            counting =
                "Zähl die Wiederholungen pro Seite. Trägst du 10 ein, hast du 10 links und 10 " +
                "rechts gemacht.",
            variant =
                "Zu leicht? Nimm eine höhere Stufe oder halte in jeder Hand etwas Schweres " +
                "(Wasserkanister, Rucksack mit Büchern).",
            video = "Step Up Übung richtig ausführen Bein",
            steps = listOf(

                    "Einen Fuß komplett auf die Erhöhung setzen. Die ganze Sohle steht auf, nicht nur " +
                    "der Ballen — die Ferse darf nicht überstehen.",
                "Das Gewicht bewusst auf diesen Fuß verlagern, besonders auf die Ferse.",

                    "Ausschließlich über das obere Bein hochdrücken. Das untere Bein darf nicht mit " +
                    "abstoßen — es hängt nur mit.",

                    "Oben ganz aufrichten, Standbein durchgestreckt, Gesäß kurz anspannen. Der andere " +
                    "Fuß bleibt in der Luft oder tippt nur leicht auf.",
                "Kontrolliert absenken, zwei bis drei Sekunden, bis der untere Fuß den Boden berührt.",
                "Alle Wiederholungen auf einer Seite, dann Bein wechseln."
            ),
            mistakes = listOf(

                    "Mit dem unteren Bein abspringen. Das ist der häufigste Fehler und nimmt der " +
                    "Übung fast die ganze Wirkung. Wenn es nicht anders geht: niedrigere Stufe " +
                    "wählen.",
                "Die Stufe zu niedrig wählen. Unter Kniehöhe wird es schnell zu leicht.",
                "Sich nach vorne fallen lassen statt sauber hochzudrücken.",
                "Das Knie nach innen kippen lassen. Es zeigt in Richtung der Fußspitze."
            )
        ),
        Exercise(
            id = "leg_ext",
            name = "Beinstrecker",
            station = Station.LEG_EXTENSION,
            muscles = listOf(Muscle.QUADS),
            sets = 3, repMin = 10, repMax = 15, restSec = 90,
            progression = ProgressionKind.WEIGHT, increment = 5.0, ferrataFocus = 2,
            cue = "Oben 1 Sekunde halten, langsam zurück. Knie nicht durchschlagen lassen.",
            why =
                "Kräftigt den Oberschenkel gezielt und stabilisiert das Knie — wichtig für lange " +
                "Abstiege, die oft härter sind als der Aufstieg.",
            setup =
                "Auf die Bank setzen, Rücken an die Lehne. Die Polsterrolle liegt vorne am " +
                "Schienbein, knapp oberhalb der Fußgelenke — nicht auf dem Spann. Dein Knie " +
                "sollte auf Höhe der Drehachse des Geräts liegen; bei den meisten Stationen ist " +
                "das der Punkt, an dem sich der Hebel dreht.",
            video = "Beinstrecker richtig einstellen Ausführung",
            steps = listOf(
                "Aufrecht sitzen, Rücken an der Lehne, Hände seitlich am Sitz oder an den Griffen.",

                    "Die Beine gegen die Rolle nach vorne strecken, bis sie fast ganz durchgestreckt " +
                    "sind.",
                "Oben eine Sekunde halten und den Oberschenkel bewusst anspannen.",

                    "Langsam ablassen — etwa drei Sekunden — bis kurz vor die Ausgangsposition. Die " +
                    "Gewichte sollen sich nicht ganz ablegen, die Spannung bleibt."
            ),
            mistakes = listOf(

                    "Das Knie oben mit Schwung durchschlagen. Kontrolliert strecken, nicht knallen " +
                    "lassen.",

                    "Die Rolle zu weit oben am Schienbein — das drückt aufs Schienbein statt zu " +
                    "trainieren.",
                "Mit dem Becken vom Sitz abheben, um mehr Gewicht zu schaffen."
            )
        ),
        Exercise(
            id = "leg_curl",
            name = "Beinbeuger",
            station = Station.LEG_CURL,
            muscles = listOf(Muscle.HAMSTRINGS),
            sets = 3, repMin = 10, repMax = 15, restSec = 90,
            progression = ProgressionKind.WEIGHT, increment = 5.0, ferrataFocus = 2,
            cue = "Ferse kontrolliert zum Gesäß, ohne das Becken abzuheben.",
            why =
                "Gegenspieler zum Oberschenkel vorne. Das Gleichgewicht schützt Knie und Rücken " +
                "bei steilen Passagen.",
            setup =
                "Je nach Gerät liegend auf dem Bauch oder sitzend. Die Polsterrolle liegt hinten " +
                "an den Waden, knapp oberhalb der Fersen — nicht in der Kniekehle. Auch hier " +
                "gilt: Knie auf Höhe der Drehachse des Hebels.",
            video = "Beinbeuger Beincurl richtige Ausführung",
            steps = listOf(
                "In Position gehen und die Griffe fassen, damit der Oberkörper ruhig bleibt.",
                "Die Fersen gegen die Rolle Richtung Gesäß ziehen, so weit es geht.",
                "Am Umkehrpunkt kurz halten und die Oberschenkelrückseite bewusst anspannen.",
                "Langsam zurücklassen, etwa drei Sekunden, bis die Beine fast gestreckt sind."
            ),
            mistakes = listOf(

                    "Das Becken abheben oder ins Hohlkreuz gehen, um die Rolle weiter zu bewegen. Der " +
                    "Rumpf bleibt ruhig — notfalls weniger Gewicht.",
                "Mit Schwung arbeiten und die Rolle zurückschnappen lassen.",
                "Die Rolle in der Kniekehle statt an den Waden. Das drückt und begrenzt den Weg."
            )
        ),
        Exercise(
            id = "split_squat",
            name = "Ausfallschritt statisch",
            station = Station.BODYWEIGHT,
            muscles = listOf(Muscle.QUADS, Muscle.GLUTES, Muscle.CORE),
            sets = 3, repMin = 8, repMax = 12, restSec = 105,
            progression = ProgressionKind.REPS, increment = 5.0, ferrataFocus = 2,
            cue = "Hinteres Knie Richtung Boden, Oberkörper aufrecht. Beide Füße bleiben stehen.",
            why = "Einbeinige Kraft und Stabilität — am Fels stehst du selten auf beiden Beinen.",
            setup =
                "Ein großer Schritt nach vorne, etwa 60 bis 80 Zentimeter — so weit, dass beide " +
                "Knie unten etwa 90 Grad erreichen. Der Unterschied zum normalen Ausfallschritt: " +
                "Du gehst nicht, sondern bleibst die ganze Zeit in dieser Stellung stehen. " +
                "Hinterer Fuß auf dem Ballen, Fersen beide in einer Linie mit der Hüfte, nicht " +
                "auf einem Seil balancieren.",
            counting = "Zähl pro Seite. 10 Wiederholungen heißt 10 links und 10 rechts.",
            variant =
                "Zu leicht? Stell den hinteren Fuß auf eine niedrige Erhöhung (etwa " +
                "Schienbeinhöhe, nicht höher) — das ist die bulgarische Variante und deutlich " +
                "anspruchsvoller.",
            video = "Split Squat statischer Ausfallschritt Technik",
            steps = listOf(
                "In der Schrittstellung stehen, Oberkörper aufrecht, Blick geradeaus.",

                    "Senkrecht nach unten absenken — nicht nach vorne. Das hintere Knie geht Richtung " +
                    "Boden.",

                    "Runter, bis das hintere Knie knapp über dem Boden ist und das vordere Knie etwa " +
                    "90 Grad hat. Das vordere Schienbein bleibt dabei möglichst senkrecht.",

                    "Über die Ferse des vorderen Fußes wieder hochdrücken, ohne die Position zu " +
                    "verlassen.",
                "Alle Wiederholungen auf einer Seite, dann wechseln."
            ),
            mistakes = listOf(

                    "Der Schritt ist zu kurz. Dann schiebt sich das vordere Knie weit über die " +
                    "Fußspitze und drückt auf die Kniescheibensehne. Lieber einen größeren Schritt " +
                    "machen.",
                "Nach vorne kippen. Der Oberkörper bleibt aufrecht, höchstens eine leichte Neigung.",

                    "Nicht tief genug gehen. Erst die letzten Zentimeter bringen die Wirkung — " +
                    "hinteres Knie fast bis zum Boden.",
                "Das vordere Knie nach innen kippen lassen, besonders wenn es anstrengend wird."
            )
        ),
        Exercise(
            id = "calf_raise",
            name = "Wadenheben",
            station = Station.BODYWEIGHT,
            muscles = listOf(Muscle.CALVES),
            sets = 3, repMin = 12, repMax = 20, restSec = 60,
            progression = ProgressionKind.REPS, increment = 5.0, ferrataFocus = 2,
            cue = "Auf einer Stufe, Ferse tief ablassen, oben ganz durchdrücken.",
            why =
                "Am Steig stehst du oft nur mit dem Fußballen auf einer schmalen Klammer. Starke " +
                "Waden verhindern, dass die Füße nach einer Stunde zittern.",
            setup =
                "Stell dich mit den Fußballen auf eine Treppenstufe oder Türschwelle, sodass die " +
                "Fersen frei in der Luft hängen. Mit einer Hand an der Wand oder am Geländer " +
                "abstützen — nur zum Balancieren, nicht zum Hochziehen.",
            variant = "Zu leicht? Auf einem Bein statt auf beiden. Dann zählst du pro Seite.",
            video = "Wadenheben Stufe richtige Ausführung",
            steps = listOf(
                "Fußballen auf der Kante, Fersen hängen frei.",
                "Fersen so weit wie möglich absenken, bis du die Dehnung in der Wade spürst.",
                "Langsam bis ganz nach oben auf die Zehenspitzen drücken.",
                "Oben eine Sekunde halten und die Wade fest anspannen.",
                "Kontrolliert wieder tief absenken. Nicht federn."
            ),
            mistakes = listOf(

                    "Nur ein kleines Stück wippen. Die Wirkung kommt aus dem vollen Weg — tief " +
                    "runter, ganz hoch.",
                "Sich am Geländer hochziehen.",
                "Zu schnell arbeiten. Die Wade ist ausdauernd, sie braucht Kontrolle statt Tempo."
            )
        ),
        Exercise(
            id = "plank",
            name = "Unterarmstütz",
            station = Station.BODYWEIGHT,
            muscles = listOf(Muscle.CORE),
            sets = 3, repMin = 1, repMax = 1, restSec = 60,
            progression = ProgressionKind.TIME, increment = 10.0, ferrataFocus = 2,
            cue = "Gesäß anspannen, Körper bildet eine Linie. Kein Hohlkreuz.",
            why =
                "Rumpfspannung hält dich nah an der Wand. Wer durchhängt, hängt in den Armen und " +
                "ermüdet doppelt so schnell.",
            setup =
                "Auf den Boden, am besten auf eine Matte. Unterarme parallel aufsetzen, Ellbogen " +
                "genau unter den Schultern, Hände flach oder Fäuste.",
            variant =
                "Zu schwer? Auf den Knien statt auf den Füßen. Die Linie von Knie bis Kopf bleibt " +
                "gerade.",
            video = "Plank Unterarmstütz richtige Ausführung",
            steps = listOf(
                "Unterarme aufsetzen, Beine nach hinten strecken, auf den Fußballen abstützen.",

                    "Gesäß und Bauch fest anspannen. Das Becken kippt leicht nach hinten, sodass die " +
                    "Lende flach wird.",

                    "Der Körper bildet eine gerade Linie von den Fersen bis zum Kopf. Der Blick geht " +
                    "auf den Boden zwischen die Hände.",
                "Ruhig weiteratmen und die Zeit halten.",
                "Abbrechen, sobald die Hüfte durchhängt — dann bringt es nichts mehr."
            ),
            mistakes = listOf(

                    "Das Gesäß nach oben strecken. Sieht leichter aus, ist es auch — und trainiert " +
                    "weniger.",
                "Ins Hohlkreuz durchhängen. Das belastet die Lendenwirbel.",
                "Die Luft anhalten."
            )
        ),
        Exercise(
            id = "side_plank",
            name = "Seitstütz",
            station = Station.BODYWEIGHT,
            muscles = listOf(Muscle.CORE),
            sets = 2, repMin = 1, repMax = 1, restSec = 45,
            progression = ProgressionKind.TIME, increment = 10.0, ferrataFocus = 1,
            cue = "Hüfte hoch, Körper in einer Linie. Pro Seite.",
            why = "Seitliche Rumpfkette — die stabilisiert dich bei Quergängen.",
            setup =
                "Auf die Seite legen, unterer Unterarm aufgestellt, Ellbogen direkt unter der " +
                "Schulter. Beine gestreckt übereinander.",
            counting = "Die eingetragene Zeit gilt pro Seite.",
            variant = "Zu schwer? Die Knie ablegen und von dort stützen.",
            video = "Seitstütz Side Plank Ausführung",
            steps = listOf(
                "Auf der Seite liegen, Unterarm senkrecht unter der Schulter.",
                "Hüfte anheben, bis Kopf, Becken und Füße eine gerade Linie bilden.",
                "Obere Hand in die Hüfte oder zur Decke strecken.",
                "Zeit halten, dann Seite wechseln."
            ),
            mistakes = listOf(
                "Die Hüfte absacken lassen.",
                "Nach vorne oder hinten kippen. Der Körper bleibt in einer Ebene."
            )
        ),
        Exercise(
            id = "chest_press",
            name = "Brustpresse",
            station = Station.CHEST_PRESS,
            muscles = listOf(Muscle.CHEST, Muscle.SHOULDERS, Muscle.TRICEPS),
            sets = 3, repMin = 8, repMax = 12, restSec = 120,
            progression = ProgressionKind.WEIGHT, increment = 5.0, ferrataFocus = 1,
            cue = "Schulterblätter zusammen und unten lassen, kontrolliert zurückkommen.",
            why =
                "Sorgt für Muskelbalance zum vielen Ziehen. Ein reines Zug-Programm zieht die " +
                "Schultern langfristig nach vorne.",
            setup =
                "Sitzhöhe so einstellen, dass die Griffe etwa auf Höhe der Brustmitte liegen — " +
                "nicht auf Höhe der Schultern. Rücken vollständig an die Lehne, Füße flach am " +
                "Boden.",
            video = "Brustpresse Maschine richtige Ausführung",
            steps = listOf(

                    "Hinsetzen, Rücken an die Lehne, Schulterblätter zusammenziehen und nach unten " +
                    "schieben. Diese Position hältst du.",
                "Griffe fassen, Handgelenke gerade.",

                    "Gleichmäßig nach vorne drücken, bis die Arme fast gestreckt sind. Die Ellbogen " +
                    "nicht ganz durchdrücken.",
                "Vorne kurz halten.",

                    "Langsam zurückkommen, bis die Hände etwa auf Brusthöhe sind. Nicht ganz ablegen " +
                    "— die Spannung bleibt."
            ),
            mistakes = listOf(
                "Die Schultern nach vorne rollen lassen. Sie bleiben hinten und unten.",
                "Zu weit zurückkommen, bis es in der Schulter zieht.",
                "Die Ellbogen ganz durchschlagen."
            )
        ),
        Exercise(
            id = "shoulder_press",
            name = "Schulterdrücken",
            station = Station.CHEST_PRESS,
            muscles = listOf(Muscle.SHOULDERS, Muscle.TRICEPS),
            sets = 3, repMin = 8, repMax = 12, restSec = 105,
            progression = ProgressionKind.WEIGHT, increment = 2.5, ferrataFocus = 2,
            cue = "Rippen unten lassen, nicht ins Hohlkreuz ausweichen.",
            why =
                "Stabile Schultern tragen den Rucksack und halten dich über Kopf an hohen " +
                "Klammern sicher.",
            setup =
                "Wenn deine Station eine eigene Schulterdrück-Position hat, Sitz so einstellen, " +
                "dass die Griffe auf Schulterhöhe liegen. Sonst geht es auch mit dem oberen " +
                "Kabelzug im Stehen oder mit Kurzhanteln, falls vorhanden.",
            video = "Schulterdrücken richtige Ausführung Technik",
            steps = listOf(
                "Aufrecht sitzen oder stehen, Rumpf fest anspannen.",
                "Griffe auf Schulterhöhe fassen, Ellbogen leicht vor dem Körper, nicht ganz seitlich.",
                "Nach oben drücken, bis die Arme fast gestreckt sind.",
                "Kontrolliert zurück auf Schulterhöhe."
            ),
            mistakes = listOf(

                    "Ins Hohlkreuz ausweichen. Die Rippen bleiben unten, der Bauch angespannt — sonst " +
                    "landet die Last in der Lende.",
                "Die Ellbogen ganz nach außen stellen. Das belastet die Schulter stärker als nötig.",
                "Den Kopf vorschieben."
            )
        ),
        Exercise(
            id = "butterfly",
            name = "Butterfly",
            station = Station.BUTTERFLY,
            muscles = listOf(Muscle.CHEST),
            sets = 3, repMin = 10, repMax = 14, restSec = 90,
            progression = ProgressionKind.WEIGHT, increment = 5.0, ferrataFocus = 1,
            cue = "Leichte Ellbogenbeugung beibehalten, vorne kurz halten.",
            why = "Formt die Brust — der Definitionsanteil deines Ziels.",
            setup =
                "Sitzhöhe so, dass die Griffe oder Polster etwa auf Brusthöhe sind. Rücken an die " +
                "Lehne, Füße am Boden.",
            video = "Butterfly Maschine Brust Ausführung",
            steps = listOf(
                "Hinsetzen, Rücken anlehnen, Schulterblätter zusammen und unten.",

                    "Arme zu den Seiten öffnen, Ellbogen dabei leicht gebeugt — dieser Winkel bleibt " +
                    "die ganze Zeit gleich.",
                "Die Arme vor der Brust zusammenführen, als würdest du etwas Großes umarmen.",
                "Vorne eine Sekunde halten und die Brust anspannen.",
                "Langsam wieder öffnen, bis du die Dehnung spürst — aber nicht weiter."
            ),
            mistakes = listOf(

                    "Die Ellbogen beim Zusammenführen strecken und beim Öffnen beugen. Der Winkel " +
                    "bleibt konstant.",
                "Zu weit aufmachen, bis es vorne in der Schulter zieht.",
                "Die Schultern nach vorne ziehen."
            )
        ),
        Exercise(
            id = "pushup",
            name = "Liegestütz",
            station = Station.BODYWEIGHT,
            muscles = listOf(Muscle.CHEST, Muscle.TRICEPS, Muscle.CORE),
            sets = 3, repMin = 8, repMax = 20, restSec = 90,
            progression = ProgressionKind.REPS, increment = 2.5, ferrataFocus = 1,
            cue = "Körper bleibt eine Linie, Ellbogen etwa 45° zum Rumpf.",
            why = "Drückbewegung ohne Gerät — und trainiert die Rumpfspannung gleich mit.",
            setup =
                "Hände etwas weiter als schulterbreit auf dem Boden, direkt unter oder knapp vor " +
                "den Schultern. Beine nach hinten gestreckt, auf den Fußballen.",
            variant =
                "Zu schwer? Hände auf eine Erhöhung — Tischkante, Fensterbank, Treppenstufe. Je " +
                "höher, desto leichter. Das ist besser als auf den Knien, weil die Körperlinie " +
                "erhalten bleibt.",
            video = "Liegestütz richtige Ausführung Technik",
            steps = listOf(
                "Rumpf und Gesäß anspannen, der Körper bildet eine Linie von den Fersen bis zum Kopf.",

                    "Absenken, bis die Brust knapp über dem Boden ist. Die Ellbogen zeigen etwa 45 " +
                    "Grad nach hinten-außen, nicht rechtwinklig zur Seite.",
                "Unten kurz halten, ohne abzulegen.",
                "Gleichmäßig hochdrücken, bis die Arme fast gestreckt sind."
            ),
            mistakes = listOf(
                "Die Hüfte durchhängen lassen oder das Gesäß hochstrecken.",
                "Nur halb absenken.",
                "Den Kopf vorstrecken statt in Verlängerung der Wirbelsäule zu lassen."
            )
        ),
        Exercise(
            id = "reverse_fly",
            name = "Reverse Butterfly",
            station = Station.BUTTERFLY,
            muscles = listOf(Muscle.SHOULDERS, Muscle.BACK),
            sets = 3, repMin = 12, repMax = 16, restSec = 75,
            progression = ProgressionKind.WEIGHT, increment = 2.5, ferrataFocus = 2,
            cue = "Arme nach hinten öffnen, Schulterblätter zusammenführen.",
            why =
                "Hintere Schulter und oberer Rücken — die Haltungsversicherung gegen den " +
                "Rundrücken vom Rucksacktragen.",
            setup =
                "Am Butterfly-Gerät umgekehrt sitzen — mit der Brust zur Lehne, falls das Gerät " +
                "das erlaubt. Die Arme greifen die Griffe von innen. Sitzhöhe so, dass die Arme " +
                "waagrecht sind.",
            video = "Reverse Butterfly hintere Schulter Ausführung",
            steps = listOf(
                "Brust an die Lehne, Arme nach vorne gestreckt mit leicht gebeugten Ellbogen.",
                "Die Arme nach hinten-außen öffnen, als würdest du eine Tür aufstoßen.",
                "Am Endpunkt die Schulterblätter zusammenziehen und eine Sekunde halten.",
                "Langsam zurückführen, ohne die Spannung zu verlieren."
            ),
            mistakes = listOf(
                "Mit Schwung reißen. Das Gewicht ist hier bewusst niedrig.",
                "Die Schultern zu den Ohren ziehen. Sie bleiben unten.",
                "Die Bewegung aus dem Rücken statt aus der hinteren Schulter machen."
            )
        ),
        Exercise(
            id = "triceps",
            name = "Trizepsdrücken",
            station = Station.LAT_PULLDOWN,
            muscles = listOf(Muscle.TRICEPS),
            sets = 3, repMin = 10, repMax = 14, restSec = 75,
            progression = ProgressionKind.WEIGHT, increment = 2.5, ferrataFocus = 1,
            cue = "Ellbogen fix am Körper, nur die Unterarme arbeiten.",
            why = "Ergänzt die Drückkette und stabilisiert den Ellbogen beim Abstützen.",
            setup =
                "Vor dem Latzug stehen, Stange im Obergriff, Hände schulterbreit oder enger. " +
                "Leicht nach vorne geneigt, Füße hüftbreit.",
            video = "Trizepsdrücken Kabelzug richtige Ausführung",
            steps = listOf(
                "Stange auf Brusthöhe fassen, Ellbogen dicht an die Rippen legen.",
                "Die Ellbogen bleiben genau dort — nur die Unterarme bewegen sich.",
                "Stange nach unten drücken, bis die Arme fast gestreckt sind.",
                "Unten eine Sekunde anspannen.",
                "Kontrolliert zurück auf Brusthöhe."
            ),
            mistakes = listOf(
                "Die Ellbogen nach vorne oder außen wandern lassen. Dann drückt der Rücken mit.",
                "Mit dem Oberkörper nachwippen.",
                "Zu viel Gewicht — der Trizeps allein schafft deutlich weniger als der Latzug."
            )
        ),
        Exercise(
            id = "farmer_hold",
            name = "Farmer's Hold",
            station = Station.DUMBBELL,
            muscles = listOf(Muscle.GRIP, Muscle.FOREARMS, Muscle.CORE),
            sets = 3, repMin = 1, repMax = 1, restSec = 90,
            progression = ProgressionKind.TIME, increment = 10.0, ferrataFocus = 3,
            cue = "Schwere Hanteln, aufrecht stehen, Schultern unten.",
            why = "Griffkraft unter Last — direkt übertragbar aufs Festhalten am Drahtseil.",
            setup =
                "In jede Hand etwas Schweres: Kurzhanteln, Wasserkanister, gefüllte Taschen. " +
                "Beide Seiten etwa gleich schwer.",
            video = "Farmers Walk Carry richtige Ausführung",
            steps = listOf(
                "Aufrecht hinstellen, Gewichte seitlich am Körper hängen lassen.",
                "Brust raus, Schultern nach hinten und unten — nicht hochziehen lassen.",
                "Fest zugreifen und die Zeit halten. Wer will, geht dabei langsam auf und ab.",
                "Ruhig atmen. Abbrechen, bevor der Griff aufgeht."
            ),
            mistakes = listOf(
                "Die Schultern von den Gewichten nach oben ziehen lassen.",
                "Ins Hohlkreuz gehen.",
                "Die Gewichte fallen lassen statt kontrolliert abzustellen."
            )
        ),
    )
}
