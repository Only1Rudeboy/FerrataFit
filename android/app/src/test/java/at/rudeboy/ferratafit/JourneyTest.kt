package at.rudeboy.ferratafit

import at.rudeboy.ferratafit.data.BadgeSnapshot
import at.rudeboy.ferratafit.data.Exercises
import at.rudeboy.ferratafit.data.Journey
import at.rudeboy.ferratafit.data.StageKind
import at.rudeboy.ferratafit.data.StageLog
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Prüft das Etappensystem gegen dieselben Fälle wie `web/test-journey.mjs`.
 * Laufen Android und Web hier auseinander, stimmt der Fortschritt auf einem der
 * beiden Geräte nicht mehr.
 */
class JourneyTest {

    /** Baut einen Fortschritt aus n abgeschlossenen Etappen. */
    private fun walk(n: Int, skip: Set<Int> = emptySet()): List<StageLog> =
        (0 until n).map { i ->
            val s = Journey.stageAt(i)
            val skipped = i in skip
            StageLog(s.id, s.kind.name, if (skipped) 0 else s.meters, at = 0L, skipped = skipped)
        }

    private fun snapshot(
        progress: List<StageLog> = emptyList(),
        meters: Int = 0,
        streak: Int = 0,
        increases: Int = 0,
        hang: Int = 0,
        pull: Int = 0
    ) = BadgeSnapshot(progress, meters, streak, increases, hang, pull)

    // ---------------- Reihenfolge und Freischaltung ----------------

    @Test
    fun `ohne Fortschritt steht Etappe 1 an`() {
        assertEquals("S1", Journey.current(emptyList()).id)
    }

    @Test
    fun `nach einer Etappe steht die zweite an`() {
        assertEquals("S2", Journey.current(walk(1)).id)
    }

    @Test
    fun `nach sieben Etappen beginnt der Zyklus von vorne`() {
        assertEquals("S1", Journey.current(walk(7)).id)
    }

    @Test
    fun `Zyklusnummer zaehlt ab eins`() {
        assertEquals(1, Journey.cycleNumber(emptyList()))
        assertEquals(1, Journey.cycleNumber(walk(6)))
        assertEquals(2, Journey.cycleNumber(walk(7)))
        assertEquals(3, Journey.cycleNumber(walk(14)))
    }

    // ---------------- Aufbau des Zyklus ----------------

    @Test
    fun `sieben Etappen mit drei Krafteinheiten`() {
        assertEquals(7, Journey.stages.size)
        assertEquals(3, Journey.stages.count { it.kind == StageKind.STRENGTH })
    }

    @Test
    fun `zwischen zwei Krafteinheiten liegt immer eine leichtere`() {
        val kinds = Journey.stages.map { it.kind }
        kinds.indices.forEach { i ->
            val a = kinds[i]
            val b = kinds[(i + 1) % kinds.size]
            assertFalse(
                "Zwei Krafteinheiten direkt hintereinander bei Index $i",
                a == StageKind.STRENGTH && b == StageKind.STRENGTH
            )
        }
    }

    @Test
    fun `jede Krafteinheit zeigt auf einen Trainingstag`() {
        Journey.stages.filter { it.kind == StageKind.STRENGTH }.forEach {
            assertTrue("Etappe ${it.id} ohne Trainingstag", Journey.dayFor(it) != null)
        }
    }

    @Test
    fun `jede Dehn-Etappe hat hinterlegte Uebungen`() {
        Journey.stages
            .filter { it.kind == StageKind.MOBILITY || it.kind == StageKind.RECOVERY }
            .forEach { stage ->
                assertTrue("Etappe ${stage.id} hat zu wenige Übungen", stage.mobilityIds.size >= 4)
                stage.mobilityIds.forEach { id ->
                    assertTrue("Unbekannte Übung $id in ${stage.id}", Journey.drill(id) != null)
                }
            }
    }

    // ---------------- Höhenmeter ----------------

    @Test
    fun `ein vollstaendiger Zyklus ergibt die Summe aller Etappen`() {
        val expected = Journey.stages.sumOf { it.meters }
        assertEquals(expected, Journey.totalMeters(walk(7)))
    }

    @Test
    fun `uebersprungene Etappen zaehlen nicht`() {
        val full = Journey.stages.sumOf { it.meters }
        val expected = full - Journey.stages[0].meters - Journey.stages[2].meters
        assertEquals(expected, Journey.totalMeters(walk(7, skip = setOf(0, 2))))
    }

    // ---------------- Gipfel ----------------

    @Test
    fun `Gipfelfortschritt rechnet Restweg und Erreichte korrekt`() {
        val p = Journey.summitProgress(1000)
        assertEquals(2, p.reached.size)
        assertEquals(p.next.meters - 1000, p.toGo)
    }

    @Test
    fun `Gipfel sind aufsteigend sortiert`() {
        Journey.summits.forEachIndexed { i, s ->
            if (i > 0) assertTrue("Gipfel nicht aufsteigend", s.meters > Journey.summits[i - 1].meters)
        }
    }

    // ---------------- Abzeichen ----------------

    @Test
    fun `ohne Fortschritt kein Abzeichen`() {
        assertEquals(0, Journey.earnedBadges(snapshot()).size)
    }

    @Test
    fun `erste Etappe vergibt Losgegangen`() {
        val badges = Journey.earnedBadges(snapshot(progress = walk(1), meters = 120))
        assertEquals(listOf("first_stage"), badges.map { it.id })
    }

    @Test
    fun `fortgeschrittener Stand vergibt die passenden Abzeichen`() {
        val ids = Journey.earnedBadges(
            snapshot(progress = walk(14), meters = 3000, streak = 5, increases = 3, hang = 65, pull = 6)
        ).map { it.id }

        listOf(
            "hang30", "hang60", "pullup1", "pullup5", "first_week",
            "streak4", "summit1", "first_increase", "no_skip_cycle"
        ).forEach { assertTrue("Abzeichen $it fehlt", it in ids) }
    }

    @Test
    fun `Lueckenlos faellt weg sobald im letzten Zyklus etwas fehlt`() {
        val ids = Journey.earnedBadges(
            snapshot(progress = walk(14, skip = setOf(13)), meters = 3000, streak = 5, hang = 65, pull = 6)
        ).map { it.id }
        assertFalse("no_skip_cycle" in ids)
    }

    @Test
    fun `Abzeichen sind vollstaendig und eindeutig`() {
        Journey.badges.forEach {
            assertTrue(it.icon.isNotBlank() && it.name.isNotBlank() && it.desc.isNotBlank())
        }
        assertEquals(Journey.badges.size, Journey.badges.map { it.id }.toSet().size)
    }

    // ---------------- Sprüche ----------------

    @Test
    fun `Spruch bleibt ueber den Tag stabil und wechselt am naechsten`() {
        val t = 1_700_000_000_000L
        assertEquals(Journey.quoteOfDay(t).text, Journey.quoteOfDay(t + 3_600_000L).text)
        assertTrue(Journey.quoteOfDay(t).text != Journey.quoteOfDay(t + 86_400_000L).text)
    }

    @Test
    fun `jede Etappenart hat einen Abschlusssatz`() {
        StageKind.entries.forEach {
            assertTrue("Kein Abschlusssatz für $it", Journey.completionLine(it).length > 10)
        }
    }

    // ---------------- Dehnkatalog ----------------

    @Test
    fun `Dehnuebungen sind vollstaendig beschrieben`() {
        Journey.mobility.forEach { m ->
            assertTrue("Unvollständig: ${m.id}",
                m.name.isNotBlank() && m.zone.isNotBlank() && m.cue.isNotBlank() && m.why.isNotBlank())
            assertTrue("Haltezeit unplausibel bei ${m.id}", m.seconds in 20..90)
        }
        assertEquals(Journey.mobility.size, Journey.mobility.map { it.id }.toSet().size)
    }

    @Test
    fun `Unterarme sind abgedeckt`() {
        assertTrue(Journey.mobility.any { it.zone == "Unterarme" })
    }

    @Test
    fun `Regenerationsetappen halten laenger`() {
        val recovery = Journey.stages.first { it.longHold }
        val normal = Journey.stages.first { it.kind == StageKind.MOBILITY }
        val drill = Journey.drill("pigeon")!!
        assertTrue(
            "Lange Haltezeit greift nicht",
            Journey.holdSeconds(recovery, drill) > Journey.holdSeconds(normal, drill)
        )
    }

    // ---------------- Ausführungsanleitungen ----------------

    @Test
    fun `jede Uebung hat Aufbau, Ablauf und Videosuche`() {
        Exercises.all.forEach { e ->
            assertTrue("${e.id}: kein Aufbau", e.setup.isNotBlank())
            assertTrue("${e.id}: zu wenige Schritte (${e.steps.size})", e.steps.size >= 3)
            assertTrue("${e.id}: keine Videosuche", e.video.isNotBlank())
            e.steps.forEach { assertTrue("${e.id}: leerer Schritt", it.length > 15) }
        }
    }

    @Test
    fun `jede Uebung nennt typische Fehler`() {
        Exercises.all.forEach { e ->
            assertTrue("${e.id}: keine Fehlerhinweise", e.mistakes.isNotEmpty())
        }
    }

    @Test
    fun `einseitige Uebungen erklaeren die Zaehlweise`() {
        listOf("stepup", "split_squat", "side_plank").forEach { id ->
            val e = Exercises.all.first { it.id == id }
            assertTrue("$id: Zählweise fehlt", e.counting.isNotBlank())
        }
    }

    @Test
    fun `jede Dehnuebung hat Ablauf und Videosuche`() {
        Journey.mobility.forEach { m ->
            assertTrue("${m.id}: zu wenige Schritte", m.steps.size >= 3)
            assertTrue("${m.id}: keine Videosuche", m.video.isNotBlank())
        }
    }

    @Test
    fun `Katalog stimmt mit der Web-Fassung ueberein`() {
        // Beide Varianten muessen dieselben Uebungen in derselben Reihenfolge fuehren
        assertEquals(22, Exercises.all.size)
        assertEquals(12, Journey.mobility.size)
        assertEquals(Exercises.all.size, Exercises.all.map { it.id }.toSet().size)
    }
}
