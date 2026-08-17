package at.rudeboy.ferratafit

import at.rudeboy.ferratafit.data.Advice
import at.rudeboy.ferratafit.data.Catalog
import at.rudeboy.ferratafit.data.Profile
import at.rudeboy.ferratafit.data.Progression
import at.rudeboy.ferratafit.data.Session
import at.rudeboy.ferratafit.data.SetLog
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Prüft die Progressionsregeln — der Teil, auf den sich der Nutzer beim Auflasten verlässt.
 */
class ProgressionTest {

    private val day = 24 * 60 * 60 * 1000L
    private val now = 1_700_000_000_000L
    private val profile = Profile(cycleStart = now, plateStepKg = 5.0, bodyweightKg = 78.0)

    /** Baut eine Einheit, in der eine Übung mit fester Last und Wiederholungszahl geloggt wurde. */
    private fun session(daysAgo: Int, exerciseId: String, weight: Double, reps: Int, sets: Int = 3) =
        Session(
            id = "s$daysAgo",
            dayId = "A",
            startedAt = now - daysAgo * day,
            finishedAt = now - daysAgo * day + 3_600_000L,
            sets = (0 until sets).map { SetLog(exerciseId, it, weight, reps) }
        )

    private fun timeSession(daysAgo: Int, exerciseId: String, seconds: Int, sets: Int = 4) =
        Session(
            id = "t$daysAgo",
            dayId = "A",
            startedAt = now - daysAgo * day,
            finishedAt = now - daysAgo * day + 3_600_000L,
            sets = (0 until sets).map { SetLog(exerciseId, it, 0.0, 0, seconds) }
        )

    @Test
    fun `erste Einheit liefert konservative Startschaetzung`() {
        val lat = Catalog.byId("latpull_wide")!!
        val s = Progression.suggest(lat, emptyList(), profile, now)

        assertEquals(Advice.START, s.advice)
        // 78 kg × 0,45 = 35,1 → auf 5er-Stufe gerundet
        assertEquals(35.0, s.weightKg, 0.01)
    }

    @Test
    fun `zwei Einheiten am oberen Ende loesen die Steigerung aus`() {
        val lat = Catalog.byId("latpull_wide")!!   // Fenster 8-12, Schritt 5 kg
        val history = listOf(
            session(daysAgo = 7, exerciseId = "latpull_wide", weight = 40.0, reps = 12),
            session(daysAgo = 3, exerciseId = "latpull_wide", weight = 40.0, reps = 12)
        )
        val s = Progression.suggest(lat, history, profile, now)

        assertEquals(Advice.INCREASE, s.advice)
        assertEquals(45.0, s.weightKg, 0.01)
        // Wiederholungen fallen zurück auf den unteren Wert des Fensters
        assertEquals(8, s.targetReps)
    }

    @Test
    fun `eine einzelne gute Einheit reicht noch nicht`() {
        val lat = Catalog.byId("latpull_wide")!!
        val history = listOf(
            session(daysAgo = 7, exerciseId = "latpull_wide", weight = 40.0, reps = 10),
            session(daysAgo = 3, exerciseId = "latpull_wide", weight = 40.0, reps = 12)
        )
        val s = Progression.suggest(lat, history, profile, now)

        assertEquals(Advice.HOLD, s.advice)
        assertEquals(40.0, s.weightKg, 0.01)
    }

    @Test
    fun `nach einer Steigerung wird nicht sofort wieder gesteigert`() {
        val lat = Catalog.byId("latpull_wide")!!
        // Zuletzt bereits mit mehr Gewicht trainiert — die alte Einheit zaehlt nicht mit.
        val history = listOf(
            session(daysAgo = 7, exerciseId = "latpull_wide", weight = 40.0, reps = 12),
            session(daysAgo = 3, exerciseId = "latpull_wide", weight = 45.0, reps = 12)
        )
        val s = Progression.suggest(lat, history, profile, now)

        assertEquals(Advice.HOLD, s.advice)
        assertEquals(45.0, s.weightKg, 0.01)
    }

    @Test
    fun `drei Einheiten ohne Fortschritt fuehren zum Rueckschritt`() {
        val lat = Catalog.byId("latpull_wide")!!
        val history = listOf(
            session(daysAgo = 14, exerciseId = "latpull_wide", weight = 40.0, reps = 9),
            session(daysAgo = 9, exerciseId = "latpull_wide", weight = 40.0, reps = 9),
            session(daysAgo = 3, exerciseId = "latpull_wide", weight = 40.0, reps = 8)
        )
        val s = Progression.suggest(lat, history, profile, now)

        assertEquals(Advice.BACKOFF, s.advice)
        assertEquals(35.0, s.weightKg, 0.01)   // 40 × 0,9 = 36 → auf 5er-Stufe
    }

    @Test
    fun `Entlastungswoche reduziert Last und Saetze`() {
        val lat = Catalog.byId("latpull_wide")!!
        // Zyklusstart vier Wochen zurueck ⇒ aktuell Woche 5 = Entlastung
        val deloadProfile = profile.copy(cycleStart = now - 4 * 7 * day)
        val history = listOf(session(daysAgo = 3, exerciseId = "latpull_wide", weight = 50.0, reps = 12))

        assertEquals(5, Progression.weekInCycle(deloadProfile, now))
        val s = Progression.suggest(lat, history, deloadProfile, now)

        assertEquals(Advice.DELOAD, s.advice)
        assertEquals(45.0, s.weightKg, 0.01)      // 50 × 0,85 = 42,5 → auf 5er-Stufe
        assertTrue("Deload muss Saetze reduzieren", s.sets < lat.sets)
    }

    @Test
    fun `Dead Hang steigt um fuenf Sekunden`() {
        val hang = Catalog.byId("deadhang")!!
        val history = listOf(
            timeSession(daysAgo = 7, exerciseId = "deadhang", seconds = 25),
            timeSession(daysAgo = 3, exerciseId = "deadhang", seconds = 25)
        )
        val s = Progression.suggest(hang, history, profile, now)

        assertEquals(Advice.INCREASE, s.advice)
        assertEquals(30, s.targetSeconds)
    }

    @Test
    fun `Dead Hang weist ab einer Minute auf Zusatzgewicht hin`() {
        val hang = Catalog.byId("deadhang")!!
        val history = listOf(
            timeSession(daysAgo = 7, exerciseId = "deadhang", seconds = 60),
            timeSession(daysAgo = 3, exerciseId = "deadhang", seconds = 60)
        )
        val s = Progression.suggest(hang, history, profile, now)

        assertEquals(Advice.INCREASE, s.advice)
        assertTrue(
            "Ab 60 s soll auf Zusatzgewicht hingewiesen werden, war: ${s.reason}",
            s.reason.contains("Zusatzgewicht")
        )
    }

    @Test
    fun `abfallender letzter Satz gilt noch als erreicht`() {
        val lat = Catalog.byId("latpull_wide")!!
        // Zwei Saetze auf 12, der letzte faellt auf 10 ab — das soll die Steigerung nicht blockieren.
        fun mixed(daysAgo: Int) = Session(
            id = "m$daysAgo",
            dayId = "A",
            startedAt = now - daysAgo * day,
            finishedAt = now - daysAgo * day + 3_600_000L,
            sets = listOf(
                SetLog("latpull_wide", 0, 40.0, 12),
                SetLog("latpull_wide", 1, 40.0, 12),
                SetLog("latpull_wide", 2, 40.0, 10)
            )
        )
        val s = Progression.suggest(lat, listOf(mixed(7), mixed(3)), profile, now)

        assertEquals(Advice.INCREASE, s.advice)
        assertEquals(45.0, s.weightKg, 0.01)
    }

    @Test
    fun `Wochenzaehlung laeuft im Fuenferblock rund`() {
        assertEquals(1, Progression.weekInCycle(profile, now))
        assertEquals(2, Progression.weekInCycle(profile, now + 7 * day))
        assertEquals(5, Progression.weekInCycle(profile, now + 28 * day))
        assertEquals(1, Progression.weekInCycle(profile, now + 35 * day))
        assertTrue(Progression.isDeloadWeek(Progression.weekInCycle(profile, now + 28 * day)))
    }

    @Test
    fun `Plan enthaelt nur Uebungen fuer vorhandene Stationen`() {
        val ohneBeinhebel = profile.copy(
            stations = profile.stations - at.rudeboy.ferratafit.data.Station.LEG_EXTENSION
        )
        val beine = at.rudeboy.ferratafit.data.PlanBuilder.exercisesFor(Catalog.day("B"), ohneBeinhebel)

        assertTrue(
            "Beinstrecker darf ohne Station nicht im Plan stehen",
            beine.none { it.id == "leg_ext" }
        )
        assertTrue("Step-up braucht kein Geraet und muss bleiben", beine.any { it.id == "stepup" })
    }
}
