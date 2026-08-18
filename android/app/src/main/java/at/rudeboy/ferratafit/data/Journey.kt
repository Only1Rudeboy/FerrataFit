package at.rudeboy.ferratafit.data

import kotlinx.serialization.Serializable
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * Der Steig — Etappensystem, Dehnkatalog, Höhenmeter und Abzeichen.
 *
 * Grundidee: Jeder Tag hat eine Aufgabe, nicht nur die drei Krafttage. Die Tage
 * dazwischen sind keine Leerstellen, sondern eigene Etappen mit Mobility- oder
 * Ausdauerinhalt — laut Trainingslehre passiert die Anpassung ohnehin dort und
 * nicht während der Belastung.
 *
 * Eine Etappe schaltet die nächste frei. Wer eine auslässt, kommt trotzdem weiter
 * (Überspringen ist erlaubt), bekommt dafür aber keine Höhenmeter — die App bremst
 * also nicht, sie belohnt nur.
 */

/** Art einer Etappe. */
enum class StageKind {
    /** Krafteinheit am Gerät. */
    STRENGTH,

    /** Dehnen und Mobilisieren. */
    MOBILITY,

    /** Wandern, Treppen, Rad — selbst eingetragen. */
    ENDURANCE,

    /** Langes Dehnen, bewusst ruhig. */
    RECOVERY,

    /**
     * Eine echte Begehung am Fels. Steht außerhalb des Wochenzyklus und wird nie als
     * offene Etappe angeboten — die App plant keine Bergtouren, sie hält sie fest.
     */
    FERRATA
}

/** Eine Dehn- oder Mobilisationsübung. */
@Serializable
data class MobilityDrill(
    val id: String,
    val name: String,
    val seconds: Int,
    val perSide: Boolean,
    val zone: String,
    /** Kurzfassung für die enge Anzeige. */
    val cue: String,
    val why: String,
    /** Der Ablauf, Schritt für Schritt. */
    val steps: List<String> = emptyList(),
    /** Was typischerweise schiefgeht. */
    val mistakes: List<String> = emptyList(),
    /** Suchbegriff für YouTube — eine Suche statt fester Kennung, damit nichts veraltet. */
    val video: String = ""
)

/** Eine Etappe im Wochenzyklus. */
data class Stage(
    val id: String,
    val kind: StageKind,
    val title: String,
    val subtitle: String,
    val icon: String,
    val meters: Int,
    /** Nur bei Krafteinheiten belegt. */
    val dayId: String? = null,
    val mobilityIds: List<String> = emptyList(),
    val hint: String? = null,
    /** Regenerationsetappen halten länger. */
    val longHold: Boolean = false
)

/** Eine abgeschlossene Etappe. */
@Serializable
data class StageLog(
    val stageId: String,
    val kind: String,
    val meters: Int,
    val at: Long,
    val skipped: Boolean = false,
    val detail: String = ""
)

/** Ein Gipfel als Etappenziel. */
data class Summit(val meters: Int, val name: String, val note: String)

/** Ein Abzeichen. */
data class Badge(
    val id: String,
    val icon: String,
    val name: String,
    val desc: String,
    val check: (BadgeSnapshot) -> Boolean
)

/** Alles, was ein Abzeichen abfragen könnte, an einer Stelle gebündelt. */
data class BadgeSnapshot(
    val progress: List<StageLog>,
    val meters: Int,
    val weeklyStreak: Int,
    val increases: Int,
    val bestDeadhang: Int,
    val bestPullup: Int
)

object Journey {

    // -----------------------------------------------------------------------
    // Dehn- und Mobility-Katalog
    // -----------------------------------------------------------------------

    /**
     * Haltezeiten nach gängiger Empfehlung: 20–30 Sekunden im Alltag, an
     * Regenerationstagen 30–90 Sekunden, weil längeres Halten das Nervensystem
     * herunterfährt und die Erholung unterstützt.
     *
     * Die Auswahl folgt den Zonen, die bei Kletterern und am Steig am meisten
     * zumachen: Unterarme, Waden, Hüfte, Schultern und Brustwirbelsäule.
     */
    val mobility: List<MobilityDrill> = listOf(
        MobilityDrill(
            id = "forearm_flexor",
            name = "Unterarm-Beuger dehnen",
            seconds = 30, perSide = true,
            zone = "Unterarme",
            cue =
                "Arm strecken, Handfläche nach vorne, Finger mit der anderen Hand sanft zu dir " +
                "ziehen.",
            why =
                "Die Griffmuskulatur verkürzt durch jedes Hängen und Ziehen. Diese Dehnung ist " +
                "die wichtigste Gegenmaßnahme gegen Ellbogenbeschwerden.",
            steps = listOf(

                    "Arm auf Schulterhöhe nach vorne strecken, Handfläche zeigt nach vorne wie beim " +
                    "Stopp-Zeichen.",
                "Mit der anderen Hand die Finger fassen und sanft zu dir ziehen.",
                "Der Arm bleibt gestreckt. Du spürst die Dehnung an der Innenseite des Unterarms.",
                "Halten, ruhig atmen, dann die Seite wechseln."
            ),
            mistakes = listOf(
                "Ruckartig ziehen. Die Dehnung wird langsam aufgebaut.",
                "Den Ellbogen beugen — dann geht die Dehnung verloren."
            ),
            video = "Unterarm dehnen Beuger Übung"
        ),
        MobilityDrill(
            id = "forearm_extensor",
            name = "Unterarm-Strecker dehnen",
            seconds = 30, perSide = true,
            zone = "Unterarme",
            cue = "Arm strecken, Handrücken nach unten, Hand sanft nach innen ziehen.",
            why =
                "Gegenspieler zur Griffmuskulatur. Beide zusammen halten das Ellbogengelenk im " +
                "Gleichgewicht.",
            steps = listOf(
                "Arm nach vorne strecken, Handrücken zeigt nach unten, Finger hängen locker.",

                    "Mit der anderen Hand über den Handrücken fassen und die Hand sanft zu dir und " +
                    "nach innen ziehen.",
                "Die Dehnung liegt jetzt auf der Oberseite des Unterarms.",
                "Halten, dann Seite wechseln."
            ),
            mistakes = listOf(
                "Zu fest ziehen. Es soll ziehen, nicht schmerzen."
            ),
            video = "Unterarm Strecker dehnen Übung"
        ),
        MobilityDrill(
            id = "chest_doorway",
            name = "Brustdehnung im Türrahmen",
            seconds = 30, perSide = true,
            zone = "Brust & Schulter",
            cue = "Unterarm an den Rahmen, Ellbogen auf Schulterhöhe, Oberkörper langsam wegdrehen.",
            why =
                "Öffnet die Brust gegen den Rundrücken, den Rucksacktragen und Drückübungen " +
                "begünstigen.",
            steps = listOf(
                "In einen Türrahmen stellen, Unterarm seitlich an den Rahmen legen.",
                "Der Ellbogen ist etwa auf Schulterhöhe, der Winkel im Arm rund 90 Grad.",

                    "Einen kleinen Schritt nach vorne machen und den Oberkörper langsam von der Hand " +
                    "wegdrehen.",
                "Die Dehnung liegt vorne in der Brust und Schulter. Halten, dann Seite wechseln."
            ),
            mistakes = listOf(
                "Zu weit vorschieben, bis es in der Schulter sticht. Ein deutliches Ziehen reicht.",
                "Den Rücken ins Hohlkreuz drücken."
            ),
            video = "Brustdehnung Türrahmen Übung"
        ),
        MobilityDrill(
            id = "lat_stretch",
            name = "Latissimus dehnen",
            seconds = 30, perSide = true,
            zone = "Rücken",
            cue = "Hand an einer Stange über Kopf, Gesäß nach hinten schieben, Flanke lang machen.",
            why =
                "Der Latissimus arbeitet bei jedem Zug am Steig. Verkürzt er, leidet die " +
                "Schulterfreiheit über Kopf.",
            steps = listOf(
                "Beide Hände an einer Stange, Tischkante oder Türklinke etwa auf Hüft- bis Brusthöhe.",

                    "Einen Schritt zurücktreten und das Gesäß nach hinten schieben, bis die Arme lang " +
                    "sind.",
                "Den Kopf zwischen die Arme sinken lassen, Rücken lang machen.",

                    "Für mehr Wirkung leicht zu einer Seite verlagern — dann spürst du die Flanke " +
                    "deutlicher."
            ),
            mistakes = listOf(
                "Den Rücken rund machen. Die Wirbelsäule bleibt lang.",
                "Die Schultern hochziehen."
            ),
            video = "Latissimus dehnen Übung Rücken"
        ),
        MobilityDrill(
            id = "hip_flexor",
            name = "Hüftbeuger im Ausfallschritt",
            seconds = 30, perSide = true,
            zone = "Hüfte",
            cue =
                "Tiefer Ausfallschritt, hinteres Knie am Boden, Becken nach vorne schieben, Gesäß " +
                "anspannen.",
            why =
                "Die Hüfte ist die kritischste Zone für hohe Tritte. Vom vielen Sitzen verkürzt " +
                "der Hüftbeuger und bremst genau diese Bewegung.",
            steps = listOf(

                    "In einen tiefen Ausfallschritt gehen und das hintere Knie auf dem Boden ablegen " +
                    "(Matte oder Kissen drunter).",
                "Oberkörper aufrecht aufrichten, Hände auf dem vorderen Knie oder in der Hüfte.",

                    "Jetzt das Gesäß der hinteren Seite fest anspannen und das Becken leicht nach " +
                    "vorne schieben.",

                    "Die Dehnung liegt vorne in der Hüfte des hinteren Beins. Halten, dann Seite " +
                    "wechseln."
            ),
            mistakes = listOf(

                    "Ins Hohlkreuz gehen statt das Becken aufzurichten. Genau deshalb das Gesäß " +
                    "anspannen — das kippt das Becken richtig.",
                "Den Oberkörper nach vorne lehnen.",
                "Das vordere Knie weit über die Fußspitze schieben."
            ),
            video = "Hüftbeuger dehnen Ausfallschritt Übung"
        ),
        MobilityDrill(
            id = "pigeon",
            name = "Taubenstellung",
            seconds = 45, perSide = true,
            zone = "Hüfte",
            cue =
                "Vorderes Bein angewinkelt ablegen, hinteres Bein lang, Oberkörper aufrecht oder " +
                "abgelegt.",
            why = "Öffnet die äußere Hüfte — das schafft Bewegungsspielraum für weite und hohe Tritte.",
            steps = listOf(

                    "Aus dem Vierfüßlerstand ein Bein nach vorne bringen und angewinkelt ablegen — " +
                    "Knie hinter dem Handgelenk, Unterschenkel quer.",
                "Das hintere Bein lang nach hinten strecken, Fußrücken liegt auf.",

                    "Becken gerade halten und langsam absinken lassen. Bei Bedarf ein Kissen unter " +
                    "die vordere Gesäßhälfte.",

                    "Aufrecht bleiben oder den Oberkörper nach vorne ablegen. Halten, dann Seite " +
                    "wechseln."
            ),
            mistakes = listOf(
                "Das Becken zur Seite kippen lassen. Lieber ein Kissen unterlegen und gerade bleiben.",

                    "Ins vordere Knie drücken, wenn es zwickt. Bei Knieproblemen lieber die Variante " +
                    "im Liegen wählen."
            ),
            video = "Taubenstellung Pigeon Pose Hüfte dehnen"
        ),
        MobilityDrill(
            id = "hamstring",
            name = "Oberschenkelrückseite dehnen",
            seconds = 30, perSide = true,
            zone = "Beine",
            cue = "Bein gestreckt vor dir aufstellen, Hüfte nach hinten schieben, Rücken gerade lassen.",
            why = "Verkürzte Rückseiten ziehen das Becken nach hinten und stehlen dir Höhe beim Tritt.",
            steps = listOf(
                "Ein Bein gestreckt nach vorne stellen, Ferse am Boden, Fußspitze zeigt nach oben.",

                    "Das hintere Bein leicht beugen und das Gesäß nach hinten schieben — wie bei " +
                    "einer Verbeugung aus der Hüfte.",
                "Der Rücken bleibt gerade. Du spürst die Dehnung hinten im vorderen Oberschenkel.",
                "Halten, dann Seite wechseln."
            ),
            mistakes = listOf(

                    "Den Rücken runden und mit den Händen zum Fuß greifen. Die Bewegung kommt aus der " +
                    "Hüfte, nicht aus dem Rücken.",
                "Das vordere Knie überstrecken."
            ),
            video = "Oberschenkelrückseite dehnen Hamstring Übung"
        ),
        MobilityDrill(
            id = "calf_wall",
            name = "Wadendehnung an der Wand",
            seconds = 30, perSide = true,
            zone = "Waden",
            cue = "Fußballen an die Wand, Ferse am Boden, Hüfte nach vorne schieben.",
            why =
                "Die Waden tragen dich stundenlang auf schmalen Klammern. Sie gehören zu den " +
                "Zonen, die am schnellsten zumachen.",
            steps = listOf(
                "Vor eine Wand stellen, beide Hände auf Schulterhöhe anlegen.",
                "Einen Fuß nach hinten setzen, Bein gestreckt, Ferse fest am Boden.",
                "Die Hüfte langsam nach vorne zur Wand schieben, bis es in der Wade zieht.",

                    "Halten. Für die tiefere Wadenmuskulatur das hintere Knie leicht beugen und " +
                    "nochmal halten.",
                "Seite wechseln."
            ),
            mistakes = listOf(
                "Die Ferse abheben. Sie bleibt am Boden, sonst dehnt nichts.",
                "Den hinteren Fuß nach außen drehen. Die Fußspitze zeigt geradeaus."
            ),
            video = "Wadendehnung Wand Übung"
        ),
        MobilityDrill(
            id = "thoracic",
            name = "Brustwirbelsäule mobilisieren",
            seconds = 40, perSide = false,
            zone = "Rücken",
            cue =
                "Vierfüßlerstand, abwechselnd Rücken runden und sanft strecken (Katze–Kuh), ruhig " +
                "atmen.",
            why = "Eine bewegliche Brustwirbelsäule nimmt Druck von Schultern und Lendenwirbeln.",
            steps = listOf(
                "In den Vierfüßlerstand gehen: Hände unter den Schultern, Knie unter der Hüfte.",

                    "Einatmen und dabei den Rücken sanft ins leichte Hohlkreuz sinken lassen, Brust " +
                    "öffnen, Blick leicht nach vorne (Kuh).",
                "Ausatmen und den Rücken Wirbel für Wirbel nach oben runden, Kinn zur Brust (Katze).",
                "Im Atemrhythmus wechseln, langsam und ohne Endanschlag."
            ),
            mistakes = listOf(
                "Zu schnell wechseln. Jede Position folgt einem vollen Atemzug.",
                "Nur im unteren Rücken bewegen. Die Bewegung soll durch die ganze Wirbelsäule laufen."
            ),
            video = "Katze Kuh Übung Brustwirbelsäule mobilisieren"
        ),
        MobilityDrill(
            id = "child_pose",
            name = "Kindshaltung",
            seconds = 60, perSide = false,
            zone = "Rücken & Schultern",
            cue = "Fersensitz, Oberkörper ablegen, Arme lang nach vorne, tief in den Rücken atmen.",
            why =
                "Streckt den ganzen Rücken und beruhigt nach der Belastung. Guter Abschluss jeder " +
                "Einheit.",
            steps = listOf(
                "In den Fersensitz gehen, Knie etwa hüftbreit oder weiter auseinander.",
                "Den Oberkörper nach vorne ablegen, Arme lang nach vorne strecken.",
                "Die Stirn auf dem Boden oder einem Kissen ablegen.",
                "Tief in den Rücken atmen und mit jeder Ausatmung etwas weiter sinken."
            ),
            mistakes = listOf(
                "Die Schultern hochziehen. Sie sinken Richtung Boden.",

                    "Erzwingen, wenn die Fersen nicht erreicht werden — dann ein Kissen zwischen " +
                    "Waden und Oberschenkel legen."
            ),
            video = "Kindshaltung Balasana Dehnübung"
        ),
        MobilityDrill(
            id = "neck",
            name = "Nacken lösen",
            seconds = 25, perSide = true,
            zone = "Nacken",
            cue =
                "Kopf zur Seite neigen, Schulter bewusst unten lassen, mit der Hand leicht " +
                "nachhelfen.",
            why = "Der Nacken verspannt beim Sichern und beim Blick nach oben an der Wand.",
            steps = listOf(
                "Aufrecht sitzen oder stehen, Schultern bewusst nach unten sinken lassen.",
                "Den Kopf langsam zu einer Seite neigen, Ohr Richtung Schulter.",
                "Die Hand derselben Seite kann leicht am Kopf nachhelfen — ohne zu ziehen.",
                "Die gegenüberliegende Schulter bleibt unten. Halten, dann Seite wechseln."
            ),
            mistakes = listOf(
                "Kräftig am Kopf ziehen. Das Eigengewicht der Hand reicht völlig.",
                "Die Gegenschulter mit hochziehen — dann passiert nichts."
            ),
            video = "Nacken dehnen Übung Verspannung"
        ),
        MobilityDrill(
            id = "wrist_circles",
            name = "Handgelenke kreisen",
            seconds = 30, perSide = false,
            zone = "Handgelenke",
            cue =
                "Finger verschränken, langsam in beide Richtungen kreisen, dann Hände öffnen und " +
                "schließen.",
            why = "Bringt Durchblutung in die Handgelenke — die tragen am Steig alles.",
            steps = listOf(
                "Die Finger beider Hände ineinander verschränken.",
                "Langsam große Kreise mit den Handgelenken ziehen, erst in die eine Richtung.",
                "Nach der Hälfte die Richtung wechseln.",
                "Zum Abschluss die Hände mehrmals fest zur Faust schließen und wieder weit öffnen."
            ),
            mistakes = listOf(
                "Zu schnell kreisen. Es geht um Durchblutung, nicht um Tempo."
            ),
            video = "Handgelenke mobilisieren Aufwärmen Übung"
        ),
    )

    fun drill(id: String): MobilityDrill? = mobility.firstOrNull { it.id == id }

    // -----------------------------------------------------------------------
    // Etappen
    // -----------------------------------------------------------------------

    /**
     * Ein Wochenzyklus aus sieben Etappen. Zwischen zwei Krafteinheiten liegt immer
     * mindestens ein Tag mit leichterem Inhalt — so bleibt der Reiz hoch und die
     * Erholung trotzdem gewahrt.
     */
    val stages: List<Stage> = listOf(
        Stage(
            id = "S1", kind = StageKind.STRENGTH, dayId = "A",
            title = "Zug & Griff", subtitle = "Der Klettersteig-Tag",
            icon = "💪", meters = 120
        ),
        Stage(
            id = "S2", kind = StageKind.MOBILITY,
            title = "Lockern", subtitle = "Unterarme und Brust öffnen",
            icon = "🧘", meters = 40,
            mobilityIds = listOf("forearm_flexor", "forearm_extensor", "chest_doorway", "lat_stretch", "wrist_circles")
        ),
        Stage(
            id = "S3", kind = StageKind.STRENGTH, dayId = "B",
            title = "Beine & Steigkraft", subtitle = "Höhenmeter-Motor",
            icon = "🦵", meters = 120
        ),
        Stage(
            id = "S4", kind = StageKind.MOBILITY,
            title = "Hüfte & Beine", subtitle = "Platz für hohe Tritte",
            icon = "🧘", meters = 40,
            mobilityIds = listOf("hip_flexor", "pigeon", "hamstring", "calf_wall", "thoracic")
        ),
        Stage(
            id = "S5", kind = StageKind.STRENGTH, dayId = "C",
            title = "Druck & Stabilität", subtitle = "Balance und Haltung",
            icon = "💪", meters = 120
        ),
        Stage(
            id = "S6", kind = StageKind.ENDURANCE,
            title = "Rausgehen", subtitle = "Wandern, Treppen oder Rad",
            icon = "🥾", meters = 80,
            hint = "Mindestens 30 Minuten am Stück. Höhenmeter zählen zusätzlich — Treppenhaus gilt."
        ),
        Stage(
            id = "S7", kind = StageKind.RECOVERY,
            title = "Runterkommen", subtitle = "Langes Dehnen, ruhig atmen",
            icon = "🌙", meters = 50,
            mobilityIds = listOf("child_pose", "pigeon", "thoracic", "neck", "calf_wall", "forearm_flexor"),
            longHold = true
        )
    )

    fun stageAt(index: Int): Stage = stages[((index % stages.size) + stages.size) % stages.size]
    fun stage(id: String): Stage? = stages.firstOrNull { it.id == id }

    /** Welcher Trainingstag gehört zur Etappe? Nur bei Krafteinheiten belegt. */
    fun dayFor(stage: Stage): TrainingDay? = stage.dayId?.let { id -> Catalog.days.firstOrNull { it.id == id } }

    /**
     * Die gerade offene Etappe ergibt sich aus der Zahl der gegangenen.
     *
     * Begehungen am Fels zählen hier bewusst nicht mit: Sie stehen außerhalb des
     * Wochenzyklus, weil man einen Klettersteig geht, wenn Wetter und Zeit passen —
     * nicht wenn eine App es vorsieht. Würden sie mitzählen, verschöbe jede Tour den
     * ganzen Rhythmus.
     */
    fun currentIndex(progress: List<StageLog>): Int =
        progress.count { it.stageId != Ferrata.EXTRA_STAGE_ID }
    fun current(progress: List<StageLog>): Stage = stageAt(currentIndex(progress))

    /** Wievielte Runde durch den Wochenzyklus? Ab 1 gezählt. */
    fun cycleNumber(progress: List<StageLog>): Int = currentIndex(progress) / stages.size + 1

    // -----------------------------------------------------------------------
    // Höhenmeter und Gipfel
    // -----------------------------------------------------------------------

    fun totalMeters(progress: List<StageLog>): Int =
        progress.sumOf { if (it.skipped) 0 else it.meters }

    /**
     * Gipfel als Etappenziele. Die Zahlen sind echte Höhen — das macht den Fortschritt
     * greifbarer als eine abstrakte Punktzahl.
     */
    val summits: List<Summit> = listOf(
        Summit(300, "Erste Aussicht", "Der Anfang ist gemacht"),
        Summit(800, "Waldgrenze", "Ab hier wird die Sicht frei"),
        Summit(1600, "Almhochtal", "Du bist im Rhythmus"),
        Summit(2600, "Hoher Freschen", "2.004 m — dein Hausberg wäre geschafft"),
        Summit(4000, "Piz Buin", "3.312 m — Vorarlbergs höchster"),
        Summit(6000, "Mont Blanc", "4.810 m — der Höchste der Alpen"),
        Summit(9000, "Everest", "8.848 m — Grenze des Denkbaren"),
        Summit(14000, "Zweimal Everest", "Jetzt wird es albern. Weiter so.")
    )

    data class SummitProgress(
        val next: Summit,
        val reached: List<Summit>,
        val fraction: Float,
        val toGo: Int
    )

    fun summitProgress(meters: Int): SummitProgress {
        val next = summits.firstOrNull { it.meters > meters } ?: summits.last()
        val prevIdx = summits.indexOf(next) - 1
        val prev = if (prevIdx >= 0) summits[prevIdx].meters else 0
        val span = max(1, next.meters - prev)
        return SummitProgress(
            next = next,
            reached = summits.filter { it.meters <= meters },
            fraction = min(1f, max(0f, (meters - prev).toFloat() / span)),
            toGo = max(0, next.meters - meters)
        )
    }

    // -----------------------------------------------------------------------
    // Abzeichen
    // -----------------------------------------------------------------------

    val badges: List<Badge> = listOf(
        Badge("first_stage", "🥾", "Losgegangen", "Erste Etappe abgeschlossen") {
            it.progress.count { p -> !p.skipped } >= 1
        },
        Badge("first_week", "📅", "Erste Runde", "Alle sieben Etappen eines Zyklus geschafft") {
            it.progress.count { p -> !p.skipped } >= 7
        },
        Badge("hang30", "✊", "Griffig", "30 Sekunden am Stück gehangen") {
            it.bestDeadhang >= 30
        },
        Badge("hang60", "🔒", "Griffmeister", "Eine volle Minute Dead Hang") {
            it.bestDeadhang >= 60
        },
        Badge("pullup1", "⬆️", "Der erste Zug", "Ein sauberer Klimmzug") {
            it.bestPullup >= 1
        },
        Badge("pullup5", "🧗", "Fünf am Stück", "Fünf Klimmzüge in einem Satz") {
            it.bestPullup >= 5
        },
        Badge("first_increase", "📈", "Aufgelastet", "Zum ersten Mal mehr Gewicht aufgelegt") {
            it.increases >= 1
        },
        Badge("mobility10", "🧘", "Geschmeidig", "Zehn Mobility-Etappen abgeschlossen") { s ->
            s.progress.count {
                !it.skipped && (it.kind == StageKind.MOBILITY.name || it.kind == StageKind.RECOVERY.name)
            } >= 10
        },
        Badge("streak4", "🔥", "Dranbleiber", "Vier Wochen in Folge trainiert") {
            it.weeklyStreak >= 4
        },
        Badge("summit1", "⛰️", "Waldgrenze", "800 Höhenmeter gesammelt") {
            it.meters >= 800
        },
        Badge("summit_freschen", "🏔", "Hoher Freschen", "2.600 Höhenmeter — dein Hausberg") {
            it.meters >= 2600
        },
        Badge("no_skip_cycle", "💎", "Lückenlos", "Einen kompletten Zyklus ohne Auslassen") { s ->
            val last7 = s.progress.takeLast(7)
            last7.size == 7 && last7.none { it.skipped }
        },
        Badge("outdoor5", "🌄", "Draußen unterwegs", "Fünf Ausdauer-Etappen im Freien") { s ->
            s.progress.count { !it.skipped && it.kind == StageKind.ENDURANCE.name } >= 5
        }
    )

    fun earnedBadges(snapshot: BadgeSnapshot): List<Badge> =
        badges.filter { runCatching { it.check(snapshot) }.getOrDefault(false) }

    // -----------------------------------------------------------------------
    // Motivation
    // -----------------------------------------------------------------------

    /**
     * Sprüche mit Bergbezug. Die Auswahl richtet sich nach dem Kalendertag, damit sie
     * über den Tag stabil bleibt und sich nicht bei jedem Öffnen ändert.
     */
    data class Quote(val text: String, val by: String? = null)

    val quotes: List<Quote> = listOf(
        Quote("Berge erklimmt man nicht, indem man auf den Gipfel starrt, sondern indem man den nächsten Schritt macht."),
        Quote("Es sind nicht die Berge, die wir bezwingen, sondern wir selbst.", "Edmund Hillary"),
        Quote("Der beste Zeitpunkt zum Anfangen war letzte Woche. Der zweitbeste ist jetzt."),
        Quote("Ein Satz mehr als beim letzten Mal ist Fortschritt. Mehr braucht es heute nicht."),
        Quote("Die Unterarme geben zuerst auf, nicht der Wille. Genau deshalb trainierst du sie."),
        Quote("Wer sich Zeit zum Erholen nimmt, trainiert nicht weniger — er trainiert klüger."),
        Quote("Kraft kommt nicht vom Draufhalten, sondern vom Wiederkommen."),
        Quote("Nur wer sein Ziel kennt, findet den Weg.", "Laotse"),
        Quote("Der Gipfel ist optional. Zurückzukommen ist Pflicht.", "Ed Viesturs"),
        Quote("Heute ist nur eine Etappe. Aber ohne sie gibt es keine nächste."),
        Quote("Dehnen fühlt sich nach nichts an — bis du merkst, wie weit dein Fuß plötzlich hochkommt."),
        Quote("Man wächst nicht an der Last, sondern an der Regelmäßigkeit."),
        Quote("Fünf Kilo mehr sind kein Zufall. Die hast du dir über Wochen geholt."),
        Quote("Am Steig zählt nicht, wie stark du bist, sondern wie lange du stark bleibst."),
        Quote("Das Schwierige an schwierigen Dingen ist meistens der Anfang."),
        Quote("Berge lehren Geduld. Der Fels wartet, bis du bereit bist."),
        Quote("Wer heute lockert, greift morgen fester zu."),
        Quote("Es geht nicht darum, nie müde zu werden — sondern darum, trotzdem loszugehen."),
        Quote("Jede Wiederholung, die du nicht machst, fehlt dir irgendwann an der Wand."),
        Quote("Der Weg ist das Ziel, aber ein Gipfel schadet auch nicht."),
        Quote("Konstanz schlägt Intensität. Immer.")
    )

    /** Spruch des Tages — stabil über den Kalendertag. */
    fun quoteOfDay(now: Long): Quote {
        val dayNumber = (now / 86_400_000L).toInt()
        return quotes[((dayNumber % quotes.size) + quotes.size) % quotes.size]
    }

    /** Kurzer Zuspruch nach abgeschlossener Etappe, passend zur Art. */
    fun completionLine(kind: StageKind): String = when (kind) {
        StageKind.STRENGTH -> listOf(
            "Sauber durchgezogen. Das zahlt direkt auf den Steig ein.",
            "Etappe geschafft — die Kraft holst du dir jetzt in der Erholung.",
            "Stark. Genau so wächst der Griff."
        )
        StageKind.MOBILITY -> listOf(
            "Gut gelockert. Morgen greifst du fester zu.",
            "Das ist die Arbeit, die man nicht sieht — aber am Fels spürt.",
            "Beweglichkeit ist Reichweite. Reichweite ist Sicherheit."
        )
        StageKind.ENDURANCE -> listOf(
            "Draußen war es besser als drinnen. Immer.",
            "Höhenmeter in den Beinen sind Höhenmeter im Konto.",
            "Genau die Grundlage, die dich am Zustieg trägt."
        )
        StageKind.RECOVERY -> listOf(
            "Runtergefahren. Der Körper baut jetzt auf.",
            "Erholung ist kein Nichtstun — sie ist der Teil, in dem du stärker wirst.",
            "Gut abgeschlossen. Morgen geht es frisch weiter."
        )
        // Begehungen bekommen ihren eigenen Abschlusstext in Ferrata.completionLine,
        // weil dort auch das Umkehren gewürdigt werden muss.
        StageKind.FERRATA -> listOf(
            "Am Fels gewesen. Genau dafür war das Training da."
        )
    }.random()

    /** Wie lange dauert eine Dehn-Etappe ungefähr? */
    fun estimateMinutes(stage: Stage): Int {
        if (stage.kind == StageKind.ENDURANCE) return 30
        val seconds = stage.mobilityIds.sumOf { id ->
            val d = drill(id) ?: return@sumOf 0
            val hold = if (stage.longHold) (d.seconds * 1.6).roundToInt() else d.seconds
            hold * (if (d.perSide) 2 else 1) + 12
        }
        return max(1, (seconds / 60.0).roundToInt())
    }

    /** Haltezeit einer Übung innerhalb einer Etappe. */
    fun holdSeconds(stage: Stage, drill: MobilityDrill): Int =
        if (stage.longHold) (drill.seconds * 1.6).roundToInt() else drill.seconds
}
