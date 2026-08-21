package at.rudeboy.ferratafit.data

import kotlinx.serialization.Serializable
import java.util.Calendar
import kotlin.math.max
import kotlin.math.min

/**
 * Der Fels-Teil der App: echte Begehungen, Rang und Routenvorschläge.
 *
 * Drei Sätze bestimmen alles Weitere:
 *
 *  - Höhenmeter belohnen Umfang, nie Schwierigkeit. Eine lange A-Route zahlt mehr als
 *    eine kurze D-Route, damit niemand einen Grund hat, härter zu gehen als er will.
 *  - Der Rang belohnt Wiederholung, nie den einen kühnen Versuch. Erst die zweite saubere
 *    Begehung einer Stufe zählt — Können zeigt sich im Wiederholen.
 *  - Kein Vorschlag liegt mehr als eine Stufe über dem, was zweimal mit Reserve gegangen
 *    wurde. Ohne Ausnahme, unabhängig von jeder Trainingszahl.
 */

/** Schwierigkeit nach der in Österreich üblichen Skala. */
enum class FerrataGrade(val label: String, val desc: String) {
    A("A", "leicht — gesichertes Gehgelände, wenig steil"),
    B("B", "mäßig schwierig — erste steile Passagen, etwas Armkraft"),
    C("C", "schwierig — senkrecht und ausgesetzt, Armkraft nötig"),
    D("D", "sehr schwierig — überhängende Stellen, gute Kraft nötig"),
    E("E", "extrem schwierig — durchgehend kraftraubend"),
    F("F", "extrem — nur für sehr erfahrene und sehr starke Geher");

    companion object {
        /**
         * Liest auch Zwischenstufen wie „C/D" — es zählt die schwerere, weil die
         * über den Tag entscheidet.
         */
        fun parse(text: String): FerrataGrade? {
            val letters = text.uppercase().filter { it in 'A'..'F' }
            if (letters.isEmpty()) return null
            return entries.firstOrNull { it.name == letters.last().toString() }
        }
    }
}

/**
 * Wie voll war der Tank am Ausstieg?
 *
 * Das Gegenstück zum Wiederholungspuffer beim Krafttraining — und die ehrlichste
 * Kalibrierung, die es gibt. Was du selbst als „locker" empfunden hast, wiegt schwerer
 * als jede Berechnung aus Dead-Hang-Sekunden.
 */
enum class Feel(val label: String, val reserve: Int) {
    LOCKER("Locker — ich hätte noch viel gehabt", 4),
    GUT("Gut gefordert — Reserve war da", 3),
    FORDERND("Fordernd — am Ende wurde es knapp", 2),
    GRENZWERTIG("Grenzwertig — ich musste mich zusammenreißen", 1),
    ZU_VIEL("Zu viel — das war über meiner Grenze", 0)
}

/** Was tatsächlich limitiert hat. Mehrfachauswahl, bewusst wertfrei formuliert. */
enum class AscentFlag(val label: String) {
    UNTERARME("Unterarme sind zugegangen"),
    ZUGKRAFT("Das Ziehen ging aus"),
    BEINE("Beine waren müde"),
    KONDITION("Mir ist die Luft ausgegangen"),
    KOPF("Die Ausgesetztheit war das Thema"),
    ZEIT("Wir waren langsamer als geplant"),
    ZWICKEN("Etwas hat gezwickt"),
    RUND("Nichts davon — es lief rund")
}

/** Eine echte Begehung. */
@Serializable
data class Ascent(
    val id: String,
    val date: Long,
    val name: String,
    /** Verweis in den Routenkatalog, falls von dort gewählt. */
    val routeId: String? = null,
    val region: String = "",
    /** Als Name gespeichert, damit ältere Dateien beim Erweitern der Skala nicht brechen. */
    val grade: String = FerrataGrade.A.name,
    val climbMeters: Int = 0,
    val durationMin: Int = 0,
    val feel: String = Feel.GUT.name,
    val flags: List<String> = emptyList(),
    /** Umgekehrt statt durchgestiegen — das ist ausdrücklich kein Misserfolg. */
    val turnedBack: Boolean = false,
    val partners: String = "",
    val note: String = "",
    /** Pfad zu einem selbst aufgenommenen Foto. */
    val photoPath: String = "",
    /**
     * Von der Uhr, über Health Connect — falls die Tour aufgezeichnet wurde.
     * Der mittlere Puls fließt in die Belastungszahl ein: Er misst, was der Körper
     * tatsächlich geleistet hat, nicht was die Route auf dem Papier ist.
     */
    val avgHr: Int = 0,
    val maxHr: Int = 0,
    val kcal: Int = 0,
    /** Startzeit der übernommenen Aufzeichnung — verhindert doppeltes Übernehmen. */
    val watchStart: Long = 0L
) {
    val gradeEnum: FerrataGrade get() = runCatching { FerrataGrade.valueOf(grade) }.getOrDefault(FerrataGrade.A)
    val feelEnum: Feel get() = runCatching { Feel.valueOf(feel) }.getOrDefault(Feel.GUT)

    /** Sauber heißt: durchgestiegen und mit Reserve am Ausstieg. */
    val isClean: Boolean get() = !turnedBack && feelEnum.reserve >= 2
}

/** Ein Steig im Katalog. */
@Serializable
data class FerrataRoute(
    val id: String,
    val name: String,
    val area: String = "",
    val region: String = "",
    val grade: String = FerrataGrade.B.name,
    /** Schwierigste Einzelstelle, falls sie über der Gesamtbewertung liegt. */
    val crux: String = "",
    /** Höhenmeter innerhalb des gesicherten Steigs — nicht der ganze Tagesaufstieg. */
    val climbMeters: Int = 0,
    val lengthMeters: Int = 0,
    val approachMin: Int = 0,
    val ferrataMin: Int = 0,
    val descentMin: Int = 0,
    val totalMin: Int = 0,
    val startAlt: Int = 0,
    val summitAlt: Int = 0,
    val season: String = "",
    /** Gibt es Notausstiege oder Umgehungen? Entscheidet über den Schritt nach oben. */
    val hasExit: Boolean = false,
    val familyFriendly: Boolean = false,
    val summary: String = "",
    val approach: String = "",
    val descent: String = "",
    val highlights: List<String> = emptyList(),
    val warnings: List<String> = emptyList(),
    val gear: String = "",
    val sources: List<String> = emptyList(),
    /** Wo Quellen sich widersprachen, steht hier false — die Anzeige weist darauf hin. */
    val verified: Boolean = true
) {
    val gradeEnum: FerrataGrade get() = FerrataGrade.parse(grade) ?: FerrataGrade.B
}

/** Ränge — sie steigen und fallen nie. */
enum class Rank(
    val icon: String,
    val title: String,
    val minClean: Int,
    val minGrade: FerrataGrade?,
    val minMeters: Int
) {
    TALGAENGER("🥾", "Talgänger", 0, null, 0),
    STEIGFINDER("🧭", "Steigfinder", 1, null, 0),
    DRAHTSEILGEHER("⛓", "Drahtseilgeher", 3, FerrataGrade.A, 600),
    KLAMMERKLETTERER("🪜", "Klammerkletterer", 6, FerrataGrade.B, 1_800),
    WANDGEHER("🧗", "Wandgeher", 10, FerrataGrade.C, 4_000),
    GRATGEHER("🏔", "Gratgeher", 16, FerrataGrade.D, 8_000),
    FELSVERTRAUT("⛰️", "Felsvertraut", 25, FerrataGrade.E, 15_000);

    val subtitle: String
        get() = when (this) {
            TALGAENGER -> "Das Training läuft. Der Fels wartet."
            STEIGFINDER -> "Der erste Steig ist gegangen."
            DRAHTSEILGEHER -> "Du kennst das Gefühl am Seil."
            KLAMMERKLETTERER -> "Steile Passagen sind kein Fremdwort mehr."
            WANDGEHER -> "Senkrecht und ausgesetzt gehören dazu."
            GRATGEHER -> "Auch lange, harte Touren liegen im Rahmen."
            FELSVERTRAUT -> "Der Fels ist vertrautes Gelände."
        }
}

/** Wie eine Route zum aktuellen Stand passt. */
enum class Fit { PASST, KNAPP, ZIEL, ZU_FRUEH }

object Ferrata {

    /** Kennung für Einträge, die außerhalb des Wochenzyklus stehen. */
    const val EXTRA_STAGE_ID = "F0"

    private const val DAY_MS = 24 * 60 * 60 * 1000L

    // -----------------------------------------------------------------------
    // Erfahrung
    // -----------------------------------------------------------------------

    /**
     * Höchste Stufe, die mindestens zweimal sauber gegangen wurde.
     * Gibt -1 zurück, solange nichts bestätigt ist.
     */
    fun masteredIndex(ascents: List<Ascent>): Int =
        FerrataGrade.entries.indexOfLast { g ->
            ascents.count { it.isClean && it.gradeEnum.ordinal >= g.ordinal } >= 2
        }

    fun mastered(ascents: List<Ascent>): FerrataGrade? =
        masteredIndex(ascents).takeIf { it >= 0 }?.let { FerrataGrade.entries[it] }

    /** Höchstens eine Stufe über dem Bestätigten — der Deckel ohne Ausnahme. */
    fun experienceIndex(ascents: List<Ascent>): Int = max(masteredIndex(ascents), 0) + 1

    fun cleanCount(ascents: List<Ascent>): Int = ascents.count { it.isClean }

    /** Echte Höhenmeter aus Begehungen. Umkehren zählt voll mit. */
    fun ascentMeters(ascents: List<Ascent>): Int = ascents.sumOf { it.climbMeters }

    fun rank(ascents: List<Ascent>): Rank {
        val clean = cleanCount(ascents)
        val mastered = masteredIndex(ascents)
        val meters = ascentMeters(ascents)
        return Rank.entries.last { r ->
            clean >= r.minClean &&
                meters >= r.minMeters &&
                (r.minGrade == null || mastered >= r.minGrade.ordinal)
        }
    }

    /** Der nächste Rang und was dafür noch fehlt. */
    fun nextRankHint(ascents: List<Ascent>): Pair<Rank, String>? {
        val current = rank(ascents)
        val next = Rank.entries.getOrNull(current.ordinal + 1) ?: return null
        val fehlt = buildList {
            val clean = cleanCount(ascents)
            if (clean < next.minClean) add("${next.minClean - clean} saubere Begehungen")
            val meters = ascentMeters(ascents)
            if (meters < next.minMeters) add("${next.minMeters - meters} Höhenmeter am Fels")
            next.minGrade?.let { g ->
                if (masteredIndex(ascents) < g.ordinal) {
                    add("zweimal ${g.label} mit Reserve")
                }
            }
        }
        return next to (if (fehlt.isEmpty()) "Alles beisammen." else "Dafür fehlt: " + fehlt.joinToString(", "))
    }

    // -----------------------------------------------------------------------
    // Form
    // -----------------------------------------------------------------------

    /**
     * Welche Stufe die Trainingsform hergibt. Nutzt dieselben Schwellen wie die
     * Beschriftung der Steig-Bereitschaft, damit beides zusammenpasst.
     */
    fun readinessIndex(readiness: Int): Int = when {
        readiness >= 85 -> FerrataGrade.E.ordinal
        readiness >= 65 -> FerrataGrade.D.ordinal
        readiness >= 45 -> FerrataGrade.C.ordinal
        readiness >= 25 -> FerrataGrade.B.ordinal
        else -> FerrataGrade.A.ordinal
    }

    /**
     * Die Stufe, die im Rahmen liegt — immer die kleinere aus Erfahrung und Form.
     *
     * Eine hohe Trainingszahl kann fehlende Routine nicht ersetzen: Wer stark ist, aber
     * noch nie an einer ausgesetzten Stelle stand, bekommt trotzdem nur B. Das ist die
     * Regel, die den Unterschied zwischen Kraft und Können abbildet.
     */
    fun recommendedIndex(ascents: List<Ascent>, readiness: Int, now: Long, weeksSinceTraining: Int = 0): Int {
        var idx = min(experienceIndex(ascents), readinessIndex(readiness))

        // Eine knappe oder abgebrochene Begehung deckelt auf ihre eigene Stufe,
        // solange sie nicht durch eine saubere derselben Stufe überholt wurde.
        ascents.filter { it.feelEnum.reserve <= 1 || it.turnedBack }
            .filter { a -> ascents.none { it.isClean && it.gradeEnum == a.gradeEnum && it.date > a.date } }
            .minOfOrNull { it.gradeEnum.ordinal }
            ?.let { idx = min(idx, it) }

        // Nach langer Pause eine Stufe zurück — die erste Tour der Saison eine Nummer kleiner.
        val lastAscent = ascents.maxOfOrNull { it.date } ?: 0L
        val monthsSince = if (lastAscent == 0L) 0 else ((now - lastAscent) / (30 * DAY_MS)).toInt()
        if (monthsSince >= 8 || weeksSinceTraining >= 3) idx -= 1

        return idx.coerceIn(0, FerrataGrade.entries.lastIndex)
    }

    fun recommended(ascents: List<Ascent>, readiness: Int, now: Long, weeksSinceTraining: Int = 0): FerrataGrade =
        FerrataGrade.entries[recommendedIndex(ascents, readiness, now, weeksSinceTraining)]

    /**
     * Warum diese Stufe? Der Satz nennt die Achse, die gerade begrenzt — damit klar ist,
     * woran es liegt, statt nur an einer Zahl zu drehen.
     */
    fun recommendationReason(ascents: List<Ascent>, readiness: Int, now: Long): String {
        val exp = experienceIndex(ascents)
        val form = readinessIndex(readiness)
        val masteredLabel = mastered(ascents)?.label
        return when {
            ascents.isEmpty() ->
                "Noch keine Begehung eingetragen. Bis dahin bleibt der Rahmen bei den leichten Stufen — " +
                    "unabhängig davon, wie gut das Training läuft."
            exp < form && masteredLabel != null ->
                "Deine Kraft würde mehr hergeben. Am Fels hast du $masteredLabel bestätigt, " +
                    "deshalb bleibt es eine Stufe darüber."
            form < exp ->
                "Du kannst mehr, als du gerade trainiert hast. Die Form begrenzt hier, nicht die Erfahrung."
            else ->
                "Erfahrung und Form passen gerade zusammen."
        }
    }

    // -----------------------------------------------------------------------
    // Routen einsortieren
    // -----------------------------------------------------------------------

    /**
     * Wie eine Route zum Stand passt.
     *
     * Die schärfste Regel steckt in ZIEL: Eine Stufe nach oben wird nur auf Routen
     * vorgeschlagen, die einen Notausstieg haben oder kurz sind. Wer sich steigert, soll
     * das dort tun, wo Umkehren noch möglich ist — nicht in der Mitte einer langen Wand.
     */
    fun fitFor(route: FerrataRoute, ascents: List<Ascent>, readiness: Int): Fit {
        val basis = max(masteredIndex(ascents), 0)
        val gradeGap = route.gradeEnum.ordinal - basis
        val formIdx = readinessIndex(readiness)

        val bestMeters = ascents.filter { it.isClean }.maxOfOrNull { it.climbMeters }?.takeIf { it > 0 } ?: 250
        val bestMin = ascents.filter { it.isClean }.maxOfOrNull { it.durationMin }?.takeIf { it > 0 } ?: 180
        val sizeGap = max(
            if (route.climbMeters > 0) route.climbMeters.toFloat() / bestMeters else 0f,
            if (route.totalMin > 0) route.totalMin.toFloat() / bestMin else 0f
        )

        val lastOfGrade = ascents.filter { it.gradeEnum == route.gradeEnum }.maxByOrNull { it.date }
        val wasHard = lastOfGrade != null && lastOfGrade.feelEnum.reserve <= 2

        return when {
            gradeGap <= 0 && sizeGap <= 1.3f && route.gradeEnum.ordinal <= formIdx && !wasHard -> Fit.PASST
            gradeGap <= 0 && route.gradeEnum.ordinal <= formIdx -> Fit.KNAPP
            gradeGap == 1 && route.gradeEnum.ordinal <= formIdx && (route.hasExit || sizeGap <= 1.0f) -> Fit.ZIEL
            else -> Fit.ZU_FRUEH
        }
    }

    fun fitLabel(fit: Fit): String = when (fit) {
        Fit.PASST -> "Im Rahmen dessen, was du schon gegangen bist"
        Fit.KNAPP -> "Machbar, aber deutlich mehr als bisher — Zeit und Puffer einplanen"
        Fit.ZIEL -> "Als Ziel vorgemerkt"
        Fit.ZU_FRUEH -> "Über deinen bisherigen Begehungen"
    }

    /**
     * Deckt eine Begehung die offene Etappe ab?
     *
     * Der Nutzer will, dass ein Klettersteig aufs Training zählt — er hat an dem Tag
     * mehr geleistet als jede Einheit am Gerät. Gleichzeitig darf das den Wochenrhythmus
     * nicht durcheinanderbringen: Fünf Begehungen dürfen nicht fünf Etappen wegschieben.
     *
     * Die Regel, die beides zusammenbringt: Eine Begehung deckt genau dann die offene
     * Etappe ab, wenn an ihrem Tag noch keine Etappe abgehakt wurde. Damit zählt sie
     * einmal pro Tag — nicht öfter, und nicht zusätzlich zu einer Einheit am Gerät.
     */
    fun coversStage(progress: List<StageLog>, date: Long): Boolean =
        progress.none { it.stageId != EXTRA_STAGE_ID && sameDay(it.at, date) }

    /** Gleicher Kalendertag in der Zeitzone des Geräts. */
    fun sameDay(a: Long, b: Long): Boolean {
        if (a <= 0L || b <= 0L) return false
        val ca = Calendar.getInstance().apply { timeInMillis = a }
        val cb = Calendar.getInstance().apply { timeInMillis = b }
        return ca.get(Calendar.YEAR) == cb.get(Calendar.YEAR) &&
            ca.get(Calendar.DAY_OF_YEAR) == cb.get(Calendar.DAY_OF_YEAR)
    }

    /**
     * Zeitanteile Zustieg / Steig / Abstieg für die Tagesskizze — normiert auf 1.
     *
     * Fehlen die Zeiten, fällt die Skizze auf ein Drittel je Abschnitt zurück, statt
     * gar nicht zu erscheinen: Die Form der Skizze trägt auch ohne exakte Anteile.
     */
    fun daySegments(approachMin: Int, ferrataMin: Int, descentMin: Int): Triple<Float, Float, Float> {
        val total = approachMin + ferrataMin + descentMin
        if (total <= 0) return Triple(1f / 3f, 1f / 3f, 1f / 3f)
        // Kein Abschnitt unter 12 Prozent: Ein Fünf-Minuten-Zustieg wäre sonst ein
        // unsichtbarer Strich, und die Skizze soll lesbar sein, nicht maßstabsgetreu.
        val raw = floatArrayOf(
            approachMin.toFloat() / total,
            ferrataMin.toFloat() / total,
            descentMin.toFloat() / total
        )
        val min = 0.12f
        for (i in raw.indices) if (raw[i] < min) raw[i] = min
        val sum = raw[0] + raw[1] + raw[2]
        return Triple(raw[0] / sum, raw[1] / sum, raw[2] / sum)
    }

    /**
     * Der Fußtext unter jeder Routenliste. Er steht dort immer, nicht nur beim ersten Mal.
     */
    const val DISCLAIMER =
        "Die App kennt weder Wetter noch Zustand der Sicherungen noch deine Tagesverfassung. " +
            "Die Angaben stammen aus öffentlichen Quellen und können veraltet sein. " +
            "Entschieden wird am Einstieg, nicht am Handy."

    /** Rückmeldung nach dem Eintragen — Umkehren ist ausdrücklich kein Misserfolg. */
    fun completionLine(ascent: Ascent): String = when {
        ascent.turnedBack ->
            "Eingetragen. Umkehren ist eine Entscheidung, keine Niederlage — und die einzige, " +
                "die man immer treffen kann."
        ascent.feelEnum == Feel.LOCKER ->
            "Eingetragen. Das saß. Beim nächsten Mal darf es etwas mehr sein."
        ascent.feelEnum == Feel.ZU_VIEL ->
            "Eingetragen. Gut, dass du es weißt — die App rechnet ab jetzt damit."
        else ->
            "Eingetragen. ${ascent.climbMeters} Höhenmeter am Fels."
    }
}

/**
 * Der Steigpass — was auf einer Karte steht.
 *
 * Bewusst als ein Objekt statt zweier Zahlen nebeneinander: Rang ist ein Werdegang,
 * Form ein Zustand. Nebeneinander gestellt würden sie miteinander konkurrieren; ineinander
 * ergeben sie die eine Zahl, die zählt — welche Stufe gerade im Rahmen liegt.
 */
data class SteigPass(
    val rank: Rank,
    val cleanAscents: Int,
    val mastered: FerrataGrade?,
    val meters: Int,
    val readiness: Int,
    val recommended: FerrataGrade,
    val reason: String,
    val nextRank: Rank?,
    val nextHint: String
)

fun buildSteigPass(
    ascents: List<Ascent>,
    readiness: Int,
    now: Long,
    weeksSinceTraining: Int = 0
): SteigPass {
    val next = Ferrata.nextRankHint(ascents)
    return SteigPass(
        rank = Ferrata.rank(ascents),
        cleanAscents = Ferrata.cleanCount(ascents),
        mastered = Ferrata.mastered(ascents),
        meters = Ferrata.ascentMeters(ascents),
        readiness = readiness,
        recommended = Ferrata.recommended(ascents, readiness, now, weeksSinceTraining),
        reason = Ferrata.recommendationReason(ascents, readiness, now),
        nextRank = next?.first,
        nextHint = next?.second ?: "Höchster Rang erreicht."
    )
}
