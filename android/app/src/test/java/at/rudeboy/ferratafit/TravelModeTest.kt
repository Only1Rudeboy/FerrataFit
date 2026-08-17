package at.rudeboy.ferratafit

import at.rudeboy.ferratafit.data.BodyweightExercises
import at.rudeboy.ferratafit.data.Catalog
import at.rudeboy.ferratafit.data.Exercises
import at.rudeboy.ferratafit.data.PlanBuilder
import at.rudeboy.ferratafit.data.Profile
import at.rudeboy.ferratafit.data.Station
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Prüft den Unterwegs-Modus.
 *
 * Zwei Dinge dürfen dabei nicht passieren: dass eine Geräteübung durchrutscht (dann steht
 * man im Hotelzimmer vor einer Übung, die man nicht machen kann), und dass die unterwegs
 * trainierten Übungen aus der Statistik fallen (dann wäre der Fortschritt verloren).
 */
class TravelModeTest {

    private val zuhause = Profile(
        stations = setOf(
            Station.LAT_PULLDOWN, Station.CHEST_PRESS, Station.BUTTERFLY,
            Station.LEG_EXTENSION, Station.LEG_CURL, Station.PULLUP_BAR, Station.BODYWEIGHT
        ),
        travelMode = false
    )
    private val unterwegs = zuhause.copy(travelMode = true)

    @Test
    fun `unterwegs braucht keine Uebung ein Geraet`() {
        Catalog.days.forEach { day ->
            PlanBuilder.exercisesFor(day, unterwegs).forEach { ex ->
                assertEquals(
                    "Etappe ${day.id}: ${ex.name} verlangt ${ex.station}",
                    Station.BODYWEIGHT, ex.station
                )
            }
        }
    }

    @Test
    fun `unterwegs bleibt jeder Tag sinnvoll gefuellt`() {
        Catalog.days.forEach { day ->
            val list = PlanBuilder.exercisesFor(day, unterwegs)
            assertTrue("Etappe ${day.id} hat nur ${list.size} Übungen", list.size >= 4)
        }
    }

    @Test
    fun `am Geraet aendert sich nichts`() {
        Catalog.days.forEach { day ->
            val list = PlanBuilder.exercisesFor(day, zuhause)
            assertTrue("Etappe ${day.id} ist leer", list.isNotEmpty())
            // Am Gerät darf keine reine Unterwegs-Übung auftauchen
            list.forEach { ex ->
                assertFalse(
                    "${ex.name} gehört in den Unterwegs-Katalog",
                    BodyweightExercises.byId(ex.id) != null && ex.id.startsWith("bw_")
                )
            }
        }
    }

    @Test
    fun `keine Uebung taucht doppelt auf`() {
        Catalog.days.forEach { day ->
            val ids = PlanBuilder.exercisesFor(day, unterwegs).map { it.id }
            assertEquals("Doppelte Übung in Etappe ${day.id}: $ids", ids.size, ids.toSet().size)
        }
    }

    @Test
    fun `der Zug-Tag bleibt ein Zug-Tag`() {
        // Auch ohne Gerät muss die klettersteigrelevante Zugarbeit vorkommen
        val zug = PlanBuilder.exercisesFor(Catalog.day("A"), unterwegs)
        assertTrue(
            "Kein Zug im Zug-Tag: ${zug.map { it.name }}",
            zug.any { it.muscles.contains(at.rudeboy.ferratafit.data.Muscle.BACK) }
        )
        assertTrue(
            "Keine Griffkraft im Zug-Tag",
            zug.any { it.muscles.contains(at.rudeboy.ferratafit.data.Muscle.GRIP) }
        )
    }

    @Test
    fun `der Bein-Tag trifft die Beine`() {
        val beine = PlanBuilder.exercisesFor(Catalog.day("B"), unterwegs)
        assertTrue(
            "Keine Beinarbeit: ${beine.map { it.name }}",
            beine.any { it.muscles.contains(at.rudeboy.ferratafit.data.Muscle.QUADS) }
        )
    }

    @Test
    fun `jede Geraeteuebung hat einen Ersatz`() {
        // Was in den Trainingstagen vorkommt und ein Gerät braucht, muss ersetzbar sein
        val gebraucht = Catalog.days.flatMap { it.exerciseIds }.toSet()
        gebraucht.forEach { id ->
            val ex = Catalog.byId(id) ?: return@forEach
            if (ex.station == Station.BODYWEIGHT) return@forEach
            assertNotNull(
                "Kein Unterwegs-Ersatz für ${ex.name} ($id)",
                BodyweightExercises.substituteFor(id)
            )
        }
    }

    @Test
    fun `alle Ersatzverweise zeigen auf vorhandene Uebungen`() {
        BodyweightExercises.substitutes.forEach { (from, to) ->
            assertNotNull("Ersatz $to für $from existiert nicht", BodyweightExercises.byId(to))
            assertNotNull("Ausgangsübung $from existiert nicht", Catalog.byId(from))
        }
    }

    @Test
    fun `Unterwegs-Uebungen sind im Gesamtkatalog auffindbar`() {
        // Sonst fielen sie aus Verlauf, Bestleistungen und Progression heraus
        BodyweightExercises.all.forEach { ex ->
            assertNotNull("${ex.id} nicht über Catalog.byId erreichbar", Catalog.byId(ex.id))
        }
        assertEquals(
            Exercises.all.size + BodyweightExercises.all.size,
            Catalog.exercises.size
        )
    }

    @Test
    fun `Kennungen bleiben ueberschneidungsfrei`() {
        val geraet = Exercises.all.map { it.id }.toSet()
        val ohne = BodyweightExercises.all.map { it.id }.toSet()
        assertTrue(
            "Überschneidung: ${geraet intersect ohne}",
            (geraet intersect ohne).isEmpty()
        )
    }

    @Test
    fun `Unterwegs-Uebungen sind vollstaendig beschrieben`() {
        BodyweightExercises.all.forEach { ex ->
            assertTrue("${ex.id}: kein Aufbau", ex.setup.isNotBlank())
            assertTrue("${ex.id}: zu wenige Schritte", ex.steps.size >= 3)
            assertTrue("${ex.id}: keine Fehlerhinweise", ex.mistakes.isNotEmpty())
            assertTrue("${ex.id}: keine Videosuche", ex.video.isNotBlank())
            assertTrue("${ex.id}: keine Begründung", ex.why.length > 30)
        }
    }

    @Test
    fun `Unterwegs-Uebungen steigern ueber Wiederholungen oder Zeit`() {
        // Ohne Zusatzgewicht ergibt eine Laststeigerung keinen Sinn
        BodyweightExercises.all.forEach { ex ->
            assertTrue(
                "${ex.id} steigert über Last, obwohl kein Gerät da ist",
                ex.progression != at.rudeboy.ferratafit.data.ProgressionKind.WEIGHT
            )
        }
    }
}
