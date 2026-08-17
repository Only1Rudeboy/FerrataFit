package at.rudeboy.ferratafit.data

import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

/**
 * Einlesen von Waagendaten aus einer geteilten Datei.
 *
 * Hintergrund: Ein direkter Zugriff auf die FitDays-App ist nicht möglich — Android
 * kapselt Apps gegeneinander ab, und FitDays bietet keine Schnittstelle an. Der Weg
 * über Health Connect ist der bequemere, setzt aber voraus, dass die Kette
 * FitDays → Samsung Health → Health Connect steht.
 *
 * Klappt das nicht, bleibt der Export: FitDays und die meisten Waagen-Apps können ihre
 * Daten als Tabelle teilen. Dieser Leser ist bewusst großzügig, weil jede App ein
 * eigenes Format verwendet — er sucht in der Kopfzeile nach passenden Spalten und
 * kommt mit Punkt wie Komma als Dezimaltrenner zurecht.
 */
object BodyImport {

    /** Was beim Einlesen herauskam. */
    data class Result(
        val measurements: List<BodyMeasurement>,
        val skipped: Int,
        val error: String? = null
    )

    /** Spaltennamen, die je Wert in Frage kommen — in mehreren Sprachen. */
    private val WEIGHT = listOf("gewicht", "weight", "masse", "kg")
    private val DATE = listOf("datum", "date", "zeit", "time", "gemessen", "measured")
    private val FAT = listOf("körperfett", "koerperfett", "fett", "body fat", "bodyfat", "fat")
    private val LEAN = listOf("magermasse", "muskel", "muscle", "lean")
    private val WATER = listOf("wasser", "water")
    private val BONE = listOf("knochen", "bone")
    private val BASAL = listOf("grundumsatz", "bmr", "basal", "kcal")

    private val DATE_FORMATS = listOf(
        "yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd'T'HH:mm:ss", "yyyy-MM-dd HH:mm", "yyyy-MM-dd",
        "dd.MM.yyyy HH:mm:ss", "dd.MM.yyyy HH:mm", "dd.MM.yyyy",
        "MM/dd/yyyy HH:mm:ss", "MM/dd/yyyy HH:mm", "MM/dd/yyyy",
        "dd/MM/yyyy HH:mm", "dd/MM/yyyy"
    )

    /**
     * Liest den Inhalt einer geteilten Datei.
     *
     * Erwartet eine Kopfzeile mit Spaltennamen und danach eine Zeile je Messung.
     * Trennzeichen darf Komma, Semikolon oder Tabulator sein.
     */
    fun parse(text: String, now: Long = System.currentTimeMillis()): Result {
        val lines = text.lineSequence()
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .toList()

        if (lines.size < 2) {
            return Result(emptyList(), 0, "Die Datei enthält keine Messungen.")
        }

        val separator = detectSeparator(lines[0])
        val header = split(lines[0], separator).map { it.lowercase().trim('"', ' ') }

        val iWeight = indexOfAny(header, WEIGHT)
        if (iWeight < 0) {
            return Result(
                emptyList(), 0,
                "In der Kopfzeile ist keine Gewichtsspalte zu finden. " +
                    "Gefunden wurde: ${header.joinToString(", ").take(120)}"
            )
        }
        val iDate = indexOfAny(header, DATE)
        val iFat = indexOfAny(header, FAT)
        val iLean = indexOfAny(header, LEAN)
        val iWater = indexOfAny(header, WATER)
        val iBone = indexOfAny(header, BONE)
        val iBasal = indexOfAny(header, BASAL)

        val out = mutableListOf<BodyMeasurement>()
        var skipped = 0

        lines.drop(1).forEach { line ->
            val cells = split(line, separator)
            val weight = number(cells.getOrNull(iWeight))
            // Werte außerhalb dieses Bereichs sind keine Körpergewichte
            if (weight == null || weight < 25.0 || weight > 300.0) {
                skipped++
                return@forEach
            }
            val at = iDate.takeIf { it >= 0 }?.let { date(cells.getOrNull(it)) } ?: now
            out += BodyMeasurement(
                at = at,
                weightKg = weight,
                bodyFatPercent = iFat.takeIf { it >= 0 }?.let { number(cells.getOrNull(it)) }
                    ?.takeIf { it in 1.0..70.0 },
                leanMassKg = iLean.takeIf { it >= 0 }?.let { number(cells.getOrNull(it)) }
                    ?.takeIf { it in 10.0..200.0 },
                waterMassKg = iWater.takeIf { it >= 0 }?.let { number(cells.getOrNull(it)) }
                    ?.takeIf { it in 5.0..150.0 },
                boneMassKg = iBone.takeIf { it >= 0 }?.let { number(cells.getOrNull(it)) }
                    ?.takeIf { it in 0.5..10.0 },
                basalKcal = iBasal.takeIf { it >= 0 }?.let { number(cells.getOrNull(it)) }
                    ?.takeIf { it in 500.0..5000.0 }?.toInt()
            )
        }

        if (out.isEmpty()) {
            return Result(emptyList(), skipped, "Es ließ sich keine gültige Messung herauslesen.")
        }

        // Je Tag nur die letzte Messung behalten, wie beim Weg über Health Connect
        val deduped = out
            .groupBy { it.at / (24 * 60 * 60 * 1000L) }
            .map { (_, sameDay) -> sameDay.maxBy { it.at } }
            .sortedBy { it.at }

        return Result(deduped, skipped)
    }

    /** Führt eingelesene Messungen mit den vorhandenen zusammen, ohne Dubletten. */
    fun merge(existing: List<BodyMeasurement>, incoming: List<BodyMeasurement>): List<BodyMeasurement> {
        val byDay = existing.associateBy { it.at / (24 * 60 * 60 * 1000L) }.toMutableMap()
        incoming.forEach { m ->
            val day = m.at / (24 * 60 * 60 * 1000L)
            val old = byDay[day]
            // Die jüngere Messung des Tages gewinnt
            if (old == null || m.at >= old.at) byDay[day] = m
        }
        return byDay.values.sortedBy { it.at }
    }

    private fun detectSeparator(header: String): Char = listOf(';', '\t', ',')
        .maxBy { sep -> header.count { it == sep } }

    private fun split(line: String, sep: Char): List<String> =
        line.split(sep).map { it.trim().trim('"') }

    private fun indexOfAny(header: List<String>, needles: List<String>): Int =
        header.indexOfFirst { cell -> needles.any { cell.contains(it) } }

    /** Zahl aus einer Zelle — akzeptiert Punkt wie Komma und ignoriert Einheiten. */
    private fun number(cell: String?): Double? {
        if (cell.isNullOrBlank()) return null
        val cleaned = cell.replace(',', '.').filter { it.isDigit() || it == '.' || it == '-' }
        return cleaned.toDoubleOrNull()
    }

    private fun date(cell: String?): Long? {
        if (cell.isNullOrBlank()) return null
        // Reiner Zeitstempel in Sekunden oder Millisekunden
        cell.trim().toLongOrNull()?.let {
            return if (it > 100_000_000_000L) it else it * 1000L
        }
        DATE_FORMATS.forEach { pattern ->
            runCatching {
                val fmt = SimpleDateFormat(pattern, Locale.GERMAN)
                fmt.timeZone = TimeZone.getDefault()
                fmt.isLenient = false
                return fmt.parse(cell.trim())?.time
            }
        }
        return null
    }
}
