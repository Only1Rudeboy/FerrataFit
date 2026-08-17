package at.rudeboy.ferratafit.data

import kotlinx.serialization.Serializable
import kotlin.math.roundToInt

/**
 * Eine Messung von der Waage.
 *
 * Die Werte kommen über Health Connect herein — bei einer FitDays-Waage über die Kette
 * FitDays → Samsung Health → Health Connect. Nicht jede Waage liefert alle Werte, deshalb
 * ist außer Gewicht und Zeitpunkt alles optional.
 */
@Serializable
data class BodyMeasurement(
    val at: Long,
    val weightKg: Double,
    /** Körperfett in Prozent. */
    val bodyFatPercent: Double? = null,
    /** Magermasse in kg — alles außer Fett. */
    val leanMassKg: Double? = null,
    val boneMassKg: Double? = null,
    val waterMassKg: Double? = null,
    /** Grundumsatz in Kilokalorien pro Tag. */
    val basalKcal: Int? = null
) {
    /** Fettmasse in kg, sofern der Anteil bekannt ist. */
    val fatMassKg: Double? get() = bodyFatPercent?.let { weightKg * it / 100.0 }
}

/**
 * Auswertungen rund um die Körperdaten.
 *
 * Der für den Klettersteig entscheidende Wert ist nicht das Gewicht allein, sondern das
 * Verhältnis von Kraft zu Last: Ein Klimmzug bei 74 kg ist eine andere Übung als bei
 * 78 kg. Wer Gewicht verliert und die Kraft hält, wird am Fels leichter — ohne eine
 * einzige zusätzliche Wiederholung.
 *
 * Die Auswertung ordnet nur ein, was gemessen wurde. Sie gibt keine Zielgewichte vor.
 */
object Body {

    private const val DAY_MS = 24 * 60 * 60 * 1000L

    fun latest(list: List<BodyMeasurement>): BodyMeasurement? = list.maxByOrNull { it.at }

    /** Die letzte Messung, die mindestens [days] Tage zurückliegt — für den Vergleich. */
    fun reference(list: List<BodyMeasurement>, now: Long, days: Int = 30): BodyMeasurement? {
        val cutoff = now - days * DAY_MS
        return list.filter { it.at <= cutoff }.maxByOrNull { it.at }
            ?: list.minByOrNull { it.at }?.takeIf { latest(list)?.at != it.at }
    }

    /** Veränderung des Gewichts gegenüber der Referenzmessung, in kg. */
    fun weightTrend(list: List<BodyMeasurement>, now: Long, days: Int = 30): Double? {
        val latest = latest(list) ?: return null
        val ref = reference(list, now, days) ?: return null
        if (ref.at == latest.at) return null
        return latest.weightKg - ref.weightKg
    }

    /**
     * Kraft-Last-Verhältnis am Beispiel des Klimmzugs: Wie viel Körpergewicht bewegst du
     * je Wiederholung? Sinkt der Wert, wird dieselbe Leistung leichter.
     *
     * Zurückgegeben wird das bewegte Gewicht pro Wiederholung; je kleiner, desto besser.
     */
    fun pullupLoadPerRep(bodyweightKg: Double, bestPullups: Int): Double? =
        if (bestPullups <= 0) null else bodyweightKg / bestPullups

    /**
     * Wie viele Klimmzüge entspräche die Gewichtsveränderung ungefähr?
     *
     * Faustregel: Der Aufwand für einen Klimmzug wächst etwa mit dem Körpergewicht. Wer
     * bei gleicher Kraft [deltaKg] leichter ist, spart je Wiederholung genau dieses
     * Gewicht — bei einem typischen Zuwachs von rund 3 kg je zusätzlicher Wiederholung
     * ergibt das den geschätzten Gewinn.
     */
    fun pullupEquivalent(deltaKg: Double): Double = -deltaKg / 3.0

    /** Körperfett grob einordnen. Bewusst weit gefasst und ohne Zielvorgabe. */
    fun bodyFatLabel(percent: Double): String = when {
        percent < 8 -> "sehr niedrig"
        percent < 14 -> "athletisch"
        percent < 20 -> "im mittleren Bereich"
        percent < 26 -> "leicht darüber"
        else -> "erhöht"
    }

    /** Body-Mass-Index, wenn die Größe bekannt ist. */
    fun bmi(weightKg: Double, heightCm: Double?): Double? {
        if (heightCm == null || heightCm < 100) return null
        val m = heightCm / 100.0
        return (weightKg / (m * m) * 10).roundToInt() / 10.0
    }

    /**
     * Wie frisch ist die letzte Messung? Alte Werte sollen nicht so wirken,
     * als wären sie von heute.
     */
    fun freshnessLabel(at: Long, now: Long): String {
        val days = ((now - at) / DAY_MS).toInt()
        return when {
            days <= 0 -> "heute gemessen"
            days == 1 -> "gestern gemessen"
            days < 7 -> "vor $days Tagen gemessen"
            days < 14 -> "vor einer Woche gemessen"
            days < 60 -> "vor ${days / 7} Wochen gemessen"
            else -> "vor ${days / 30} Monaten gemessen"
        }
    }

    /** Gilt der Wert noch als aktuell genug, um ihn ins Profil zu übernehmen? */
    fun isFresh(at: Long, now: Long, maxDays: Int = 30): Boolean =
        (now - at) <= maxDays * DAY_MS
}
