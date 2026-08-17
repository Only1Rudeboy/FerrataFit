package at.rudeboy.ferratafit

import at.rudeboy.ferratafit.data.Advice
import at.rudeboy.ferratafit.data.Catalog
import at.rudeboy.ferratafit.data.Profile
import at.rudeboy.ferratafit.data.Progression
import at.rudeboy.ferratafit.data.Session
import at.rudeboy.ferratafit.data.SetLog
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Prüft das Zurückfahren vor dem Ziel-Klettersteig.
 *
 * Der heikle Punkt ist die Überschneidung mit der Entlastungswoche: Griffen beide
 * gleichzeitig, würde die Last doppelt reduziert — und man stünde entkräftet statt
 * ausgeruht am Einstieg.
 */
class TaperTest {

    private val day = 24 * 60 * 60 * 1000L
    private val now = 1_700_000_000_000L

    private fun profil(tageBisTour: Int?, zyklusStart: Long = now) = Profile(
        cycleStart = zyklusStart,
        plateStepKg = 5.0,
        bodyweightKg = 78.0,
        targetFerrataDate = tageBisTour?.let { now + it * day }
    )

    private fun historie(weight: Double = 40.0, reps: Int = 10) = listOf(
        Session(
            id = "a", dayId = "A",
            startedAt = now - 7 * day, finishedAt = now - 7 * day + 3_600_000L,
            sets = (0..2).map { SetLog("latpull_wide", it, weight, reps) }
        )
    )

    private val lat = Catalog.byId("latpull_wide")!!

    // ---------------- Stufen ----------------

    @Test
    fun `ohne Ziel gibt es kein Zurueckfahren`() {
        assertNull(Progression.taperStage(profil(null), now))
    }

    @Test
    fun `weit vor der Tour aendert sich nichts`() {
        assertNull(Progression.taperStage(profil(30), now))
        assertNull(Progression.taperStage(profil(15), now))
    }

    @Test
    fun `die Stufen greifen in der richtigen Reihenfolge`() {
        assertEquals(Progression.Taper.LEICHTER, Progression.taperStage(profil(14), now))
        assertEquals(Progression.Taper.LEICHTER, Progression.taperStage(profil(8), now))
        assertEquals(Progression.Taper.DEUTLICH_LEICHTER, Progression.taperStage(profil(7), now))
        assertEquals(Progression.Taper.DEUTLICH_LEICHTER, Progression.taperStage(profil(4), now))
        assertEquals(Progression.Taper.NUR_LOCKERN, Progression.taperStage(profil(3), now))
        assertEquals(Progression.Taper.NUR_LOCKERN, Progression.taperStage(profil(0), now))
    }

    @Test
    fun `nach der Tour ist wieder Ruhe`() {
        // Datum liegt in der Vergangenheit
        val vorbei = Profile(targetFerrataDate = now - 5 * day)
        assertNull(Progression.taperStage(vorbei, now))
    }

    // ---------------- Wirkung auf den Vorschlag ----------------

    @Test
    fun `beim Zurueckfahren sinken die Saetze, nicht die Last`() {
        val s = Progression.suggest(lat, historie(weight = 45.0), profil(10), now)
        assertEquals(Advice.DELOAD, s.advice)
        assertEquals("Die Last muss bleiben, sonst geht Kraft verloren", 45.0, s.weightKg, 0.01)
        assertTrue("Weniger Sätze erwartet", s.sets < lat.sets)
    }

    @Test
    fun `kurz vor der Tour wird deutlicher zurueckgefahren`() {
        val zehnTage = Progression.suggest(lat, historie(), profil(10), now)
        val fuenfTage = Progression.suggest(lat, historie(), profil(5), now)
        assertTrue(
            "Näher an der Tour müssen es weniger Sätze sein",
            fuenfTage.sets < zehnTage.sets
        )
    }

    @Test
    fun `in den letzten Tagen bleibt nur noch ein Satz`() {
        val s = Progression.suggest(lat, historie(), profil(2), now)
        assertEquals(1, s.sets)
        assertTrue("Der Hinweis soll zur Erholung raten", s.reason.contains("Erholung"))
    }

    // ---------------- Überschneidung mit der Entlastungswoche ----------------

    @Test
    fun `Zuruecfahren und Entlastungswoche reduzieren nicht doppelt`() {
        // Zyklusstart vier Wochen zurück ⇒ Woche 5 = Entlastung, gleichzeitig Tour in 10 Tagen
        val beides = profil(tageBisTour = 10, zyklusStart = now - 4 * 7 * day)
        assertEquals(5, Progression.weekInCycle(beides, now))

        val s = Progression.suggest(lat, historie(weight = 50.0), beides, now)
        // Der Taper hat Vorrang: volle Last, nur weniger Sätze.
        // Die Entlastungswoche würde auf 42,5 kg heruntergehen — das darf hier nicht passieren.
        assertEquals(50.0, s.weightKg, 0.01)
    }

    @Test
    fun `ohne Tour greift die Entlastungswoche weiterhin`() {
        val nurDeload = profil(tageBisTour = null, zyklusStart = now - 4 * 7 * day)
        val s = Progression.suggest(lat, historie(weight = 50.0), nurDeload, now)
        assertEquals(Advice.DELOAD, s.advice)
        assertEquals("Ohne Tour soll die Entlastungswoche die Last senken", 45.0, s.weightKg, 0.01)
    }

    @Test
    fun `eine unbekannte Uebung startet auch im Taper mit der Schaetzung`() {
        // Fall 1 muss vor dem Taper greifen, sonst gäbe es einen Vorschlag ohne Grundlage
        val s = Progression.suggest(lat, emptyList(), profil(5), now)
        assertEquals(Advice.START, s.advice)
        assertEquals(35.0, s.weightKg, 0.01)
    }

    @Test
    fun `Resttage werden korrekt gezaehlt`() {
        assertEquals(10, Progression.daysToTarget(profil(10), now))
        assertNull(Progression.daysToTarget(profil(null), now))
    }
}
