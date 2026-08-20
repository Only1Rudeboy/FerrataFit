package at.rudeboy.ferratafit.data

import kotlin.math.roundToInt

/**
 * Was eine Begehung gekostet hat — und was daraus für die nächsten Tage folgt.
 *
 * Ein Klettersteig ist nicht gleich Klettersteig: Der Übungssteig am Kellenegg ist nach
 * einer guten Stunde vorbei, der Saulakopf ist ein voller Bergtag mit 380 Klettermetern
 * im Grad D. Beides pauschal gleich zu behandeln, wäre in beide Richtungen falsch —
 * nach dem einen kann man am nächsten Tag normal trainieren, nach dem anderen wäre
 * genau das der Weg in die Überlastung.
 *
 * Deshalb bekommt jede Begehung eine Belastungszahl, und aus ihr folgt ein
 * Erholungsfenster, das den Wochenplan verschiebt und die Gewichtsvorschläge senkt.
 *
 * Die Zahl speist sich aus vier Quellen:
 *  - **Umfang**: Klettermeter und Gesamtdauer — die Grundmenge an Arbeit
 *  - **Schwierigkeit**: der Grad als Faktor — dieselben Meter im D kosten mehr als im B
 *  - **Rückmeldung**: wie es sich angefühlt hat — die ehrlichste Angabe von allen
 *  - **Uhr**: der mittlere Puls aus der Aufzeichnung, falls vorhanden — misst, was
 *    der Körper tatsächlich geleistet hat, nicht was die Route auf dem Papier ist
 */
object TourLoad {

    /**
     * Der Grad als Kostenfaktor. Nicht linear: Zwischen A und B liegt weniger als
     * zwischen D und E, weil oben jeder Meter am Arm hängt statt am Bein.
     */
    fun gradeFactor(grade: FerrataGrade): Double = when (grade) {
        FerrataGrade.A -> 0.6
        FerrataGrade.B -> 0.8
        FerrataGrade.C -> 1.0
        FerrataGrade.D -> 1.25
        FerrataGrade.E -> 1.5
        FerrataGrade.F -> 1.7
    }

    /** Die Rückmeldung schlägt aufs Konto: Was sich grenzwertig anfühlte, war teurer. */
    fun feelFactor(feel: Feel): Double = when (feel) {
        Feel.LOCKER -> 0.85
        Feel.GUT -> 1.0
        Feel.FORDERND -> 1.15
        Feel.GRENZWERTIG -> 1.35
        Feel.ZU_VIEL -> 1.5
    }

    /**
     * Der Puls von der Uhr, falls aufgezeichnet.
     *
     * 90 Schläge im Mittel heißt gemütlich (×0,85), 170 heißt Vollgas (×1,3).
     * Bewusst gedeckelt: Ein einzelner Messfehler darf die Zahl nicht verdoppeln.
     */
    fun hrFactor(avgHr: Int): Double {
        if (avgHr <= 0) return 1.0
        val t = ((avgHr - 90) / 80.0).coerceIn(0.0, 1.0)
        return 0.85 + t * 0.45
    }

    /**
     * Die Belastungszahl. Grobe Eichung:
     *  - unter 20: Übungssteig, kaum der Rede wert
     *  - 30–60:    ein ordentlicher Trainingstag
     *  - 60–90:    ein großer Bergtag
     *  - über 90:  ein Tag an der eigenen Grenze
     */
    fun score(ascent: Ascent): Int {
        val volume = ascent.climbMeters * 0.08 + ascent.durationMin * 0.06
        return (volume *
            gradeFactor(ascent.gradeEnum) *
            feelFactor(ascent.feelEnum) *
            hrFactor(ascent.avgHr))
            .roundToInt()
            .coerceAtLeast(0)
    }

    fun label(score: Int): String = when {
        score < 20 -> "Lockerer Tag"
        score < 30 -> "Spürbarer Tag"
        score < 60 -> "Ordentlicher Trainingstag"
        score < 90 -> "Großer Bergtag"
        else -> "Tag an der Grenze"
    }
}

/** Wie stark die Tour noch nachwirkt. */
enum class RecoveryLevel(val label: String, val loadFactor: Double) {
    /** Volle Erholung: Krafteinheiten werden aufgeschoben, nur Lockern und Dehnen. */
    ERHOLUNG("Erholung", 0.8),

    /** Trainieren geht, aber bewusst leichter. */
    ANGESCHLAGEN("Angeschlagen", 0.9)
}

/** Das aktive Erholungsfenster mit allem, was die Anzeige braucht. */
data class RecoveryState(
    val level: RecoveryLevel,
    /** Wann das Fenster endet (Millis). */
    val until: Long,
    /** Welche Tour es ausgelöst hat. */
    val sourceName: String,
    val score: Int
) {
    fun hoursLeft(now: Long): Int = (((until - now) / 3_600_000L) + 1).toInt().coerceAtLeast(1)
}

/**
 * Das Erholungsfenster nach einer Begehung.
 *
 * Die Staffelung ist bewusst grob — feiner wäre Scheingenauigkeit:
 *
 *  | Belastung | Fenster                                    |
 *  |-----------|--------------------------------------------|
 *  | unter 30  | keines — normal weitertrainieren           |
 *  | 30–59     | 24 h leichter                              |
 *  | 60–89     | 24 h Erholung, danach 24 h leichter        |
 *  | ab 90     | 48 h Erholung, danach 24 h leichter        |
 *
 * „Erholung" heißt: Die App schiebt die Krafteinheit auf und bietet stattdessen Dehnen
 * an. „Leichter" heißt: Trainieren geht, aber die Vorschläge sinken um zehn Prozent.
 * Wer trotzdem voll trainieren will, kann — die App legt niemandem Ketten an, sie
 * empfiehlt. Nur die Vorschlagszahlen bleiben gesenkt, denn genau dafür sind sie da.
 */
object Recovery {

    private const val HOUR_MS = 3_600_000L

    /** Ab dieser Belastung beginnt überhaupt ein Fenster. */
    const val MIN_SCORE = 30

    /**
     * Ab dieser Belastung deckt eine Begehung die offene Etappe ab.
     *
     * Ein Übungssteig von einer Stunde ist kein Trainingstag — er hakt keine
     * Krafteinheit ab, so wenig wie ein Spaziergang das täte. Die Höhenmeter
     * zählen trotzdem; nur der Wochenplan läuft normal weiter.
     */
    const val COVERS_STAGE_SCORE = 20

    fun countsAsTraining(score: Int): Boolean = score >= COVERS_STAGE_SCORE

    fun fullRestHours(score: Int): Int = when {
        score < 60 -> 0
        score < 90 -> 24
        else -> 48
    }

    fun lightHours(score: Int): Int = if (score < MIN_SCORE) 0 else 24

    fun totalHours(score: Int): Int = fullRestHours(score) + lightHours(score)

    /**
     * Der Zustand jetzt — oder null, wenn nichts mehr nachwirkt.
     *
     * Bei mehreren Begehungen gilt die strengste noch aktive. Zwei mittlere Touren an
     * zwei Tagen ergeben kein doppelt langes Fenster — der Körper erholt sich parallel,
     * nicht nacheinander. Aber die jüngere Tour kann das Fenster verlängern.
     */
    fun state(ascents: List<Ascent>, now: Long): RecoveryState? =
        ascents
            .asSequence()
            .filter { it.date in (now - 5 * 24 * HOUR_MS)..now }
            .mapNotNull { a ->
                val score = TourLoad.score(a)
                val fullRest = fullRestHours(score)
                val total = totalHours(score)
                if (total == 0) return@mapNotNull null

                val fullRestEnd = a.date + fullRest * HOUR_MS
                val totalEnd = a.date + total * HOUR_MS
                when {
                    now < fullRestEnd -> RecoveryState(RecoveryLevel.ERHOLUNG, fullRestEnd, a.name, score)
                    now < totalEnd -> RecoveryState(RecoveryLevel.ANGESCHLAGEN, totalEnd, a.name, score)
                    else -> null
                }
            }
            // ERHOLUNG schlägt ANGESCHLAGEN; bei Gleichstand das später endende Fenster
            .sortedWith(compareBy({ it.level.ordinal }, { -it.until }))
            .firstOrNull()

    /** Der Satz zur Einordnung — direkt nach dem Eintragen gezeigt. */
    fun planLine(score: Int): String {
        val fullRest = fullRestHours(score)
        val total = totalHours(score)
        return when {
            total == 0 -> "Der Plan läuft normal weiter."
            fullRest == 0 -> "Die App plant den nächsten Tag bewusst leichter."
            else -> "Die App schiebt die nächste Krafteinheit auf — erst ${fullRest} Stunden Erholung, " +
                "danach noch einen Tag leichter."
        }
    }

    /**
     * Die eingeschobene Erholungseinheit.
     *
     * Sie trägt die Sonderkennung und rückt den Wochenzyklus deshalb nicht weiter —
     * die aufgeschobene Krafteinheit bleibt stehen und wartet, bis das Fenster um ist.
     */
    fun breakStage(sourceName: String): Stage = Stage(
        id = Ferrata.EXTRA_STAGE_ID,
        kind = StageKind.RECOVERY,
        title = "Erholung",
        subtitle = "Nach: $sourceName",
        icon = "🛌",
        meters = 30,
        mobilityIds = listOf("forearm_flexor", "forearm_extensor", "child_pose", "pigeon", "calf_wall", "neck"),
        longHold = true
    )
}
