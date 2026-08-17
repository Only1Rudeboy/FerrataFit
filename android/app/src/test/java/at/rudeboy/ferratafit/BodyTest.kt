package at.rudeboy.ferratafit

import at.rudeboy.ferratafit.data.Body
import at.rudeboy.ferratafit.data.BodyMeasurement
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Prüft die Auswertung der Waagendaten.
 *
 * Kritisch sind zwei Dinge: dass eine veraltete Messung nicht als aktueller Wert ins
 * Profil wandert, und dass der Trend gegen eine sinnvolle Referenz gerechnet wird —
 * sonst zeigt die App Gewichtsverläufe an, die es so nicht gab.
 */
class BodyTest {

    private val day = 24 * 60 * 60 * 1000L
    private val now = 1_700_000_000_000L

    private fun m(daysAgo: Int, kg: Double, fat: Double? = null) =
        BodyMeasurement(at = now - daysAgo * day, weightKg = kg, bodyFatPercent = fat)

    @Test
    fun `neueste Messung wird gefunden`() {
        val list = listOf(m(30, 80.0), m(1, 78.0), m(15, 79.0))
        assertEquals(78.0, Body.latest(list)!!.weightKg, 0.001)
    }

    @Test
    fun `ohne Messungen gibt es nichts zu melden`() {
        assertNull(Body.latest(emptyList()))
        assertNull(Body.weightTrend(emptyList(), now))
        assertNull(Body.reference(emptyList(), now))
    }

    @Test
    fun `Trend vergleicht mit der Messung vor einem Monat`() {
        val list = listOf(m(35, 80.0), m(20, 79.0), m(1, 77.5))
        val trend = Body.weightTrend(list, now, days = 30)
        // 77,5 heute gegen 80,0 vor 35 Tagen
        assertEquals(-2.5, trend!!, 0.001)
    }

    @Test
    fun `eine einzelne Messung ergibt keinen Trend`() {
        assertNull(Body.weightTrend(listOf(m(1, 78.0)), now))
    }

    @Test
    fun `ohne alte Messung dient die erste als Vergleich`() {
        // Beide innerhalb der letzten 30 Tage — dann zählt die älteste vorhandene
        val list = listOf(m(10, 79.0), m(1, 78.0))
        assertEquals(-1.0, Body.weightTrend(list, now)!!, 0.001)
    }

    @Test
    fun `frische Messungen werden von alten unterschieden`() {
        assertTrue(Body.isFresh(now - 5 * day, now))
        assertTrue(Body.isFresh(now - 29 * day, now))
        assertTrue(!Body.isFresh(now - 45 * day, now))
    }

    @Test
    fun `Alter wird verstaendlich benannt`() {
        assertEquals("heute gemessen", Body.freshnessLabel(now, now))
        assertEquals("gestern gemessen", Body.freshnessLabel(now - day, now))
        assertEquals("vor 3 Tagen gemessen", Body.freshnessLabel(now - 3 * day, now))
        assertTrue(Body.freshnessLabel(now - 90 * day, now).contains("Monat"))
    }

    @Test
    fun `Fettmasse folgt aus Gewicht und Anteil`() {
        val x = m(1, 80.0, fat = 20.0)
        assertEquals(16.0, x.fatMassKg!!, 0.001)
    }

    @Test
    fun `ohne Fettanteil keine Fettmasse`() {
        assertNull(m(1, 80.0).fatMassKg)
    }

    @Test
    fun `Last je Klimmzug sinkt mit dem Koerpergewicht`() {
        val schwer = Body.pullupLoadPerRep(80.0, 5)!!
        val leicht = Body.pullupLoadPerRep(76.0, 5)!!
        assertTrue("Leichter muss weniger Last je Wiederholung ergeben", leicht < schwer)
        assertEquals(16.0, schwer, 0.001)
    }

    @Test
    fun `ohne Klimmzuege keine Last je Wiederholung`() {
        assertNull(Body.pullupLoadPerRep(80.0, 0))
    }

    @Test
    fun `Gewichtsverlust wird in Wiederholungen umgerechnet`() {
        // Drei Kilo leichter entspricht grob einer zusätzlichen Wiederholung
        assertEquals(1.0, Body.pullupEquivalent(-3.0), 0.001)
        // Zunahme geht in die andere Richtung
        assertTrue(Body.pullupEquivalent(3.0) < 0)
    }

    @Test
    fun `BMI braucht eine plausible Groesse`() {
        assertEquals(24.2, Body.bmi(78.0, 179.5)!!, 0.05)
        assertNull("Ohne Größe kein BMI", Body.bmi(78.0, null))
        assertNull("Unplausible Größe wird verworfen", Body.bmi(78.0, 40.0))
    }

    @Test
    fun `Koerperfett wird ohne Zielvorgabe eingeordnet`() {
        assertEquals("athletisch", Body.bodyFatLabel(12.0))
        assertEquals("im mittleren Bereich", Body.bodyFatLabel(18.0))
        assertEquals("erhöht", Body.bodyFatLabel(30.0))
        // Die Einordnung darf nichts fordern
        listOf(5.0, 12.0, 18.0, 24.0, 30.0).forEach {
            val label = Body.bodyFatLabel(it)
            assertTrue("Zielvorgabe im Text: $label", !label.contains("sollte"))
            assertTrue("Zielvorgabe im Text: $label", !label.contains("Ziel"))
        }
    }
}
