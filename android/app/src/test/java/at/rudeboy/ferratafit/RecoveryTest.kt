package at.rudeboy.ferratafit

import at.rudeboy.ferratafit.data.Ascent
import at.rudeboy.ferratafit.data.Exercises
import at.rudeboy.ferratafit.data.Ferrata
import at.rudeboy.ferratafit.data.Profile
import at.rudeboy.ferratafit.data.Progression
import at.rudeboy.ferratafit.data.Recovery
import at.rudeboy.ferratafit.data.RecoveryLevel
import at.rudeboy.ferratafit.data.Session
import at.rudeboy.ferratafit.data.SetLog
import at.rudeboy.ferratafit.data.TourLoad
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Prüft das Belastungsmodell: Was eine Tour gekostet hat und was daraus folgt.
 *
 * Die Richtung der Fehler ist hier anders als beim Steigpass. Dort wäre Großzügigkeit
 * gefährlich — hier wäre es die Verharmlosung: Ein großer Bergtag, nach dem die App
 * am nächsten Morgen volles Krafttraining vorschlägt, ist der direkte Weg in die
 * Überlastung. Die Prüfungen achten deshalb vor allem darauf, dass große Touren
 * niemals als klein durchgehen.
 */
class RecoveryTest {

    private val HOUR = 3_600_000L

    private fun tour(
        grade: String = "C",
        meters: Int = 200,
        minutes: Int = 240,
        feel: String = "GUT",
        avgHr: Int = 0,
        date: Long = 0L,
        name: String = "Teststeig"
    ) = Ascent(
        id = "t$date", date = date, name = name, grade = grade,
        climbMeters = meters, durationMin = minutes, feel = feel, avgHr = avgHr
    )

    // ------------------------------------------------------------------
    // Die Belastungszahl
    // ------------------------------------------------------------------

    /** Ein Übungssteig ist kein Bergtag — und ein Bergtag kein Übungssteig. */
    @Test
    fun uebungssteigUndBergtagLiegenWeitAuseinander() {
        val kellenegg = TourLoad.score(tour(grade = "C", meters = 60, minutes = 75))
        val saulakopf = TourLoad.score(tour(grade = "D", meters = 380, minutes = 330))
        assertTrue("Kellenegg ($kellenegg) muss unter 20 liegen", kellenegg < 20)
        assertTrue("Saulakopf ($saulakopf) muss über 60 liegen", saulakopf >= 60)
    }

    /** Dieselben Meter im D kosten mehr als im B. */
    @Test
    fun schwierigkeitVerteuert() {
        val b = TourLoad.score(tour(grade = "B", meters = 300, minutes = 300))
        val d = TourLoad.score(tour(grade = "D", meters = 300, minutes = 300))
        assertTrue("D ($d) muss deutlich über B ($b) liegen", d > b * 1.4)
    }

    /** Die Rückmeldung schlägt aufs Konto — grenzwertig war teurer als locker. */
    @Test
    fun gefuehlVerteuert() {
        val locker = TourLoad.score(tour(feel = "LOCKER"))
        val grenzwertig = TourLoad.score(tour(feel = "GRENZWERTIG"))
        assertTrue(grenzwertig > locker * 1.4)
    }

    /** Ein hoher Puls von der Uhr hebt die Zahl, ein niedriger senkt sie. */
    @Test
    fun pulsVonDerUhrFliesstEin() {
        val ohne = TourLoad.score(tour(avgHr = 0))
        val ruhig = TourLoad.score(tour(avgHr = 95))
        val vollgas = TourLoad.score(tour(avgHr = 165))
        assertTrue("Ruhiger Puls ($ruhig) senkt gegenüber ohne ($ohne)", ruhig < ohne)
        assertTrue("Hoher Puls ($vollgas) hebt gegenüber ohne ($ohne)", vollgas > ohne)
    }

    /** Ein Messfehler der Uhr darf die Zahl nicht explodieren lassen. */
    @Test
    fun pulsIstGedeckelt() {
        val extrem = TourLoad.score(tour(avgHr = 250))
        val hoch = TourLoad.score(tour(avgHr = 170))
        assertEquals("Über 170 darf nichts mehr dazukommen", hoch, extrem)
    }

    /**
     * Eichpunkte — wortgleich in web/test-ferrata.mjs.
     * Wer hier Zahlen ändert, muss sie dort mitändern, sonst rechnen Browser und
     * Handy verschieden — und eine Empfehlung, die im Browser anders ausfällt als
     * am Gerät, ist schlimmer als gar keine.
     */
    @Test
    fun eichpunkteStimmenMitDemBrowserUeberein() {
        assertEquals(9, TourLoad.score(tour(grade = "C", meters = 60, minutes = 75)))
        assertEquals(63, TourLoad.score(tour(grade = "D", meters = 380, minutes = 330)))
        assertEquals(100, TourLoad.score(tour(grade = "D", meters = 450, minutes = 390, feel = "GRENZWERTIG")))
    }

    /** Ein Übungssteig hakt keine Krafteinheit ab — ein Bergtag schon. */
    @Test
    fun uebungssteigZaehltNichtAlsTrainingstag() {
        assertTrue(!Recovery.countsAsTraining(TourLoad.score(tour(grade = "C", meters = 60, minutes = 75))))
        assertTrue(Recovery.countsAsTraining(TourLoad.score(tour(grade = "D", meters = 380, minutes = 330))))
        assertTrue("Örfla-Klasse zählt als Training",
            Recovery.countsAsTraining(TourLoad.score(tour(grade = "D", meters = 110, minutes = 220))))
    }

    // ------------------------------------------------------------------
    // Das Erholungsfenster
    // ------------------------------------------------------------------

    @Test
    fun kleineTourOeffnetKeinFenster() {
        val a = tour(grade = "B", meters = 60, minutes = 75, date = 1_000_000_000_000L)
        assertNull(Recovery.state(listOf(a), a.date + 2 * HOUR))
    }

    /** Nach dem großen Bergtag: erst volle Erholung, dann leichter, dann frei. */
    @Test
    fun grosserBergtagStaffeltDieErholung() {
        val a = tour(grade = "D", meters = 380, minutes = 330, date = 1_000_000_000_000L)

        assertEquals(RecoveryLevel.ERHOLUNG, Recovery.state(listOf(a), a.date + 12 * HOUR)?.level)
        assertEquals(RecoveryLevel.ANGESCHLAGEN, Recovery.state(listOf(a), a.date + 30 * HOUR)?.level)
        assertNull("Nach 48 h muss das Fenster zu sein", Recovery.state(listOf(a), a.date + 50 * HOUR))
    }

    /** Ein Tag an der Grenze sperrt volle 48 Stunden. */
    @Test
    fun grenzwertigerTagSperrtLaenger() {
        val a = tour(grade = "D", meters = 450, minutes = 390, feel = "GRENZWERTIG", date = 1_000_000_000_000L)
        assertTrue("Score muss über 90 liegen", TourLoad.score(a) >= 90)
        assertEquals(RecoveryLevel.ERHOLUNG, Recovery.state(listOf(a), a.date + 40 * HOUR)?.level)
        assertEquals(RecoveryLevel.ANGESCHLAGEN, Recovery.state(listOf(a), a.date + 60 * HOUR)?.level)
        assertNull(Recovery.state(listOf(a), a.date + 80 * HOUR))
    }

    /** Zwei Touren: Es gilt die strengste noch aktive, nicht die Summe. */
    @Test
    fun fensterAddierenSichNicht() {
        val now = 1_000_000_000_000L
        val gross = tour(grade = "D", meters = 380, minutes = 330, date = now - 30 * HOUR)
        val klein = tour(grade = "C", meters = 200, minutes = 240, date = now - 4 * HOUR, name = "Zweite")

        val state = Recovery.state(listOf(gross, klein), now)
        // Die grosse ist bei Stunde 30 nur noch ANGESCHLAGEN; die kleine (Score 30-59)
        // traegt ebenfalls ANGESCHLAGEN — es bleibt bei leichter, nicht bei voller Sperre.
        assertEquals(RecoveryLevel.ANGESCHLAGEN, state?.level)
    }

    /** Eine Woche später wirkt nichts mehr nach. */
    @Test
    fun alteTourenSindVergessen() {
        val a = tour(grade = "E", meters = 500, minutes = 400, feel = "GRENZWERTIG",
            date = 1_000_000_000_000L)
        assertNull(Recovery.state(listOf(a), a.date + 7 * 24 * HOUR))
    }

    // ------------------------------------------------------------------
    // Die Vorschläge sinken
    // ------------------------------------------------------------------

    private val profile = Profile(onboarded = true, cycleStart = 1L)

    private fun session(exerciseId: String, weightKg: Double, reps: Int, at: Long) = Session(
        id = "s$at", dayId = "A", startedAt = at, finishedAt = at + 3_000_000L,
        sets = List(3) { SetLog(exerciseId, it, weightKg = weightKg, reps = reps) }
    )

    @Test
    fun erholungSenktDenVorschlag() {
        val ex = Exercises.all.first { it.id == "latpull_wide" }
        val now = 1_000_000_000_000L
        val sessions = listOf(session("latpull_wide", 50.0, 8, now - 3 * 24 * HOUR))
        val tour = tour(grade = "D", meters = 380, minutes = 330, date = now - 10 * HOUR, name = "Saulakopf")

        val normal = Progression.suggest(ex, sessions, profile, now)
        val reduziert = Progression.suggest(ex, sessions, profile, now, Recovery.state(listOf(tour), now))

        assertTrue(
            "Im Fenster (${reduziert.weightKg} kg) muss weniger vorgeschlagen werden als sonst (${normal.weightKg} kg)",
            reduziert.weightKg < normal.weightKg
        )
        assertTrue("Der Grund muss die Tour nennen", reduziert.reason.contains("Saulakopf"))
    }

    /** Ohne Fensterangabe ändert sich nichts — Aufrufer ohne Begehungen bleiben korrekt. */
    @Test
    fun ohneFensterBleibtAllesBeimAlten() {
        val ex = Exercises.all.first { it.id == "latpull_wide" }
        val now = 1_000_000_000_000L
        val sessions = listOf(session("latpull_wide", 50.0, 8, now - 3 * 24 * HOUR))
        assertEquals(
            Progression.suggest(ex, sessions, profile, now).weightKg,
            Progression.suggest(ex, sessions, profile, now, null).weightKg,
            0.001
        )
    }

    // ------------------------------------------------------------------
    // Die Grenzen des Modells
    // ------------------------------------------------------------------

    /**
     * Die Belastungszahl darf NIEMALS in die Steigpass-Empfehlung fließen.
     *
     * Der Steigpass beantwortet „was kann ich?", die Belastung beantwortet „was hat es
     * gekostet?". Wer die beiden vermischt, macht aus einem anstrengenden Tag einen
     * Kompetenznachweis — genau die Verwechslung, gegen die das ganze Regelwerk gebaut ist.
     */
    @Test
    fun belastungIstKeinKompetenznachweis() {
        val now = 1_000_000_000_000L
        // Eine einzige brutale D-Tour, grenzwertig — hohe Belastung, keine Bestätigung
        val eine = listOf(tour(grade = "D", meters = 450, minutes = 390, feel = "GRENZWERTIG", date = now - HOUR))
        assertTrue("Score hoch", TourLoad.score(eine[0]) >= 90)
        // Der Steigpass bleibt davon unbeeindruckt: nichts zweimal sauber -> Basis A/B
        assertTrue(
            "Eine grenzwertige Tour darf die Empfehlung nicht heben",
            Ferrata.recommendedIndex(eine, readiness = 100, now = now) <= 1
        )
    }

    /** Die eingeschobene Erholung trägt die Sonderkennung — sie rückt den Zyklus nicht weiter. */
    @Test
    fun eingeschobeneErholungStehtAusserhalbDesZyklus() {
        assertEquals(Ferrata.EXTRA_STAGE_ID, Recovery.breakStage("Saulakopf").id)
    }
}
