package at.rudeboy.ferratafit

import at.rudeboy.ferratafit.data.BodyImport
import at.rudeboy.ferratafit.data.BodyMeasurement
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Prüft das Einlesen geteilter Waagen-Dateien.
 *
 * Jede Waagen-App verwendet ein eigenes Format, deshalb ist der Leser bewusst
 * großzügig. Diese Prüfungen halten fest, was er verkraften muss — und wo er
 * eine Zeile lieber verwerfen soll, statt Unsinn zu übernehmen.
 */
class BodyImportTest {

    private val now = 1_700_000_000_000L

    @Test
    fun `einfache Tabelle mit Komma wird gelesen`() {
        val csv = """
            Datum,Gewicht,Körperfett
            2026-08-01 07:30,78.4,20.1
            2026-08-08 07:15,77.9,19.8
        """.trimIndent()

        val r = BodyImport.parse(csv, now)
        assertNull(r.error)
        assertEquals(2, r.measurements.size)
        assertEquals(78.4, r.measurements[0].weightKg, 0.01)
        assertEquals(19.8, r.measurements[1].bodyFatPercent!!, 0.01)
    }

    @Test
    fun `Semikolon und Komma als Dezimaltrenner funktionieren`() {
        val csv = """
            Datum;Gewicht;Fett
            01.08.2026;78,4;20,1
            08.08.2026;77,9;19,8
        """.trimIndent()

        val r = BodyImport.parse(csv, now)
        assertNull(r.error)
        assertEquals(2, r.measurements.size)
        assertEquals(78.4, r.measurements[0].weightKg, 0.01)
    }

    @Test
    fun `Tabulator wird erkannt`() {
        val csv = "Date\tWeight\n2026-08-01\t78.4\n2026-08-08\t77.9"
        val r = BodyImport.parse(csv, now)
        assertEquals(2, r.measurements.size)
    }

    @Test
    fun `englische Spaltennamen funktionieren genauso`() {
        val csv = """
            Date,Weight (kg),Body Fat,Muscle,Water,Bone,BMR
            2026-08-01,78.4,20.1,58.0,45.0,3.2,1750
        """.trimIndent()

        val r = BodyImport.parse(csv, now)
        val m = r.measurements.single()
        assertEquals(78.4, m.weightKg, 0.01)
        assertEquals(20.1, m.bodyFatPercent!!, 0.01)
        assertEquals(58.0, m.leanMassKg!!, 0.01)
        assertEquals(45.0, m.waterMassKg!!, 0.01)
        assertEquals(3.2, m.boneMassKg!!, 0.01)
        assertEquals(1750, m.basalKcal)
    }

    @Test
    fun `Einheiten in der Zelle stoeren nicht`() {
        val csv = "Datum,Gewicht\n2026-08-01,\"78.4 kg\""
        val r = BodyImport.parse(csv, now)
        assertEquals(78.4, r.measurements.single().weightKg, 0.01)
    }

    @Test
    fun `unsinnige Gewichte werden verworfen`() {
        val csv = """
            Datum,Gewicht
            2026-08-01,78.4
            2026-08-02,3.5
            2026-08-03,999
            2026-08-04,
            2026-08-05,abc
        """.trimIndent()

        val r = BodyImport.parse(csv, now)
        assertEquals("Nur die plausible Zeile darf bleiben", 1, r.measurements.size)
        assertEquals(4, r.skipped)
    }

    @Test
    fun `unplausible Nebenwerte werden einzeln verworfen`() {
        // Gewicht stimmt, der Fettanteil ist Unsinn — dann bleibt nur das Gewicht
        val csv = "Datum,Gewicht,Fett\n2026-08-01,78.4,95"
        val m = BodyImport.parse(csv, now).measurements.single()
        assertEquals(78.4, m.weightKg, 0.01)
        assertNull("95 % Körperfett ist nicht plausibel", m.bodyFatPercent)
    }

    @Test
    fun `ohne Gewichtsspalte gibt es eine verstaendliche Meldung`() {
        val csv = "Datum,Schritte\n2026-08-01,8000"
        val r = BodyImport.parse(csv, now)
        assertNotNull(r.error)
        assertTrue(r.error!!.contains("Gewichtsspalte"))
        assertTrue("Die Meldung soll zeigen, was gefunden wurde", r.error!!.contains("schritte"))
    }

    @Test
    fun `leere Datei fuehrt nicht zum Absturz`() {
        assertNotNull(BodyImport.parse("", now).error)
        assertNotNull(BodyImport.parse("nur eine Zeile", now).error)
    }

    @Test
    fun `ohne Datumsspalte gilt der aktuelle Zeitpunkt`() {
        val r = BodyImport.parse("Gewicht\n78.4", now)
        assertEquals(now, r.measurements.single().at)
    }

    @Test
    fun `Zeitstempel in Sekunden und Millisekunden werden erkannt`() {
        val sek = BodyImport.parse("Datum,Gewicht\n1700000000,78.4", now)
        val ms = BodyImport.parse("Datum,Gewicht\n1700000000000,78.4", now)
        assertEquals(1_700_000_000_000L, sek.measurements.single().at)
        assertEquals(1_700_000_000_000L, ms.measurements.single().at)
    }

    @Test
    fun `mehrere Waegungen am selben Tag werden zusammengefasst`() {
        val csv = """
            Datum,Gewicht
            2026-08-01 07:00,78.8
            2026-08-01 19:30,78.2
            2026-08-02 07:00,78.0
        """.trimIndent()

        val r = BodyImport.parse(csv, now)
        assertEquals(2, r.measurements.size)
        // Die spätere Wägung des Tages gewinnt
        assertEquals(78.2, r.measurements[0].weightKg, 0.01)
    }

    @Test
    fun `Zusammenfuehren erhaelt vorhandene Messungen`() {
        val day = 24 * 60 * 60 * 1000L
        val vorhanden = listOf(
            BodyMeasurement(at = now - 10 * day, weightKg = 79.0),
            BodyMeasurement(at = now - 5 * day, weightKg = 78.5)
        )
        val neu = listOf(
            BodyMeasurement(at = now - 5 * day + 3600_000, weightKg = 78.3),  // selber Tag, später
            BodyMeasurement(at = now - 1 * day, weightKg = 78.0)              // neuer Tag
        )

        val merged = BodyImport.merge(vorhanden, neu)
        assertEquals("Drei Tage insgesamt", 3, merged.size)
        assertEquals("Ältester bleibt", 79.0, merged[0].weightKg, 0.01)
        assertEquals("Am doppelten Tag gewinnt die spätere", 78.3, merged[1].weightKg, 0.01)
        assertEquals("Neuer Tag kommt dazu", 78.0, merged[2].weightKg, 0.01)
    }

    @Test
    fun `Zusammenfuehren sortiert nach Zeit`() {
        val day = 24 * 60 * 60 * 1000L
        val merged = BodyImport.merge(
            listOf(BodyMeasurement(at = now, weightKg = 78.0)),
            listOf(BodyMeasurement(at = now - 20 * day, weightKg = 80.0))
        )
        assertTrue(merged[0].at < merged[1].at)
    }
}
