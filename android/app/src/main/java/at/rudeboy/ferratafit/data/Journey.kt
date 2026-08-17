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
    RECOVERY
}

/** Eine Dehn- oder Mobilisationsübung. */
@Serializable
data class MobilityDrill(
    val id: String,
    val name: String,
    val seconds: Int,
    val perSide: Boolean,
    val zone: String,
    val cue: String,
    val why: String
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
            "forearm_flexor", "Unterarm-Beuger dehnen", 30, true, "Unterarme",
            "Arm strecken, Handfläche nach vorne, Finger mit der anderen Hand sanft zu dir ziehen.",
            "Die Griffmuskulatur verkürzt durch jedes Hängen und Ziehen. Diese Dehnung ist die " +
                "wichtigste Gegenmaßnahme gegen Ellbogenbeschwerden."
        ),
        MobilityDrill(
            "forearm_extensor", "Unterarm-Strecker dehnen", 30, true, "Unterarme",
            "Arm strecken, Handrücken nach unten, Hand sanft nach innen ziehen.",
            "Gegenspieler zur Griffmuskulatur. Beide zusammen halten das Ellbogengelenk im Gleichgewicht."
        ),
        MobilityDrill(
            "chest_doorway", "Brustdehnung im Türrahmen", 30, true, "Brust & Schulter",
            "Unterarm an den Rahmen, Ellbogen auf Schulterhöhe, Oberkörper langsam wegdrehen.",
            "Öffnet die Brust gegen den Rundrücken, den Rucksacktragen und Drückübungen begünstigen."
        ),
        MobilityDrill(
            "lat_stretch", "Latissimus dehnen", 30, true, "Rücken",
            "Hand an einer Stange über Kopf, Gesäß nach hinten schieben, Flanke lang machen.",
            "Der Latissimus arbeitet bei jedem Zug am Steig. Verkürzt er, leidet die Schulterfreiheit über Kopf."
        ),
        MobilityDrill(
            "hip_flexor", "Hüftbeuger im Ausfallschritt", 30, true, "Hüfte",
            "Tiefer Ausfallschritt, hinteres Knie am Boden, Becken nach vorne schieben, Gesäß anspannen.",
            "Die Hüfte ist die kritischste Zone für hohe Tritte. Vom vielen Sitzen verkürzt der " +
                "Hüftbeuger und bremst genau diese Bewegung."
        ),
        MobilityDrill(
            "pigeon", "Taubenstellung", 45, true, "Hüfte",
            "Vorderes Bein angewinkelt ablegen, hinteres Bein lang, Oberkörper aufrecht oder abgelegt.",
            "Öffnet die äußere Hüfte — das schafft Bewegungsspielraum für weite und hohe Tritte."
        ),
        MobilityDrill(
            "hamstring", "Oberschenkelrückseite dehnen", 30, true, "Beine",
            "Bein gestreckt vor dir aufstellen, Hüfte nach hinten schieben, Rücken gerade lassen.",
            "Verkürzte Rückseiten ziehen das Becken nach hinten und stehlen dir Höhe beim Tritt."
        ),
        MobilityDrill(
            "calf_wall", "Wadendehnung an der Wand", 30, true, "Waden",
            "Fußballen an die Wand, Ferse am Boden, Hüfte nach vorne schieben.",
            "Die Waden tragen dich stundenlang auf schmalen Klammern. Sie gehören zu den Zonen, " +
                "die am schnellsten zumachen."
        ),
        MobilityDrill(
            "thoracic", "Brustwirbelsäule mobilisieren", 40, false, "Rücken",
            "Vierfüßlerstand, abwechselnd Rücken runden und sanft strecken (Katze–Kuh), ruhig atmen.",
            "Eine bewegliche Brustwirbelsäule nimmt Druck von Schultern und Lendenwirbeln."
        ),
        MobilityDrill(
            "child_pose", "Kindshaltung", 60, false, "Rücken & Schultern",
            "Fersensitz, Oberkörper ablegen, Arme lang nach vorne, tief in den Rücken atmen.",
            "Streckt den ganzen Rücken und beruhigt nach der Belastung. Guter Abschluss jeder Einheit."
        ),
        MobilityDrill(
            "neck", "Nacken lösen", 25, true, "Nacken",
            "Kopf zur Seite neigen, Schulter bewusst unten lassen, mit der Hand leicht nachhelfen.",
            "Der Nacken verspannt beim Sichern und beim Blick nach oben an der Wand."
        ),
        MobilityDrill(
            "wrist_circles", "Handgelenke kreisen", 30, false, "Handgelenke",
            "Finger verschränken, langsam in beide Richtungen kreisen, dann Hände öffnen und schließen.",
            "Bringt Durchblutung in die Handgelenke — die tragen am Steig alles."
        )
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

    /** Die gerade offene Etappe ergibt sich aus der Zahl der gegangenen. */
    fun currentIndex(progress: List<StageLog>): Int = progress.size
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
