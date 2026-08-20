package at.rudeboy.ferratafit

import at.rudeboy.ferratafit.data.Draft
import at.rudeboy.ferratafit.data.DraftEntry
import at.rudeboy.ferratafit.data.DraftSet
import at.rudeboy.ferratafit.data.Drafts
import at.rudeboy.ferratafit.data.Ferrata
import at.rudeboy.ferratafit.data.StageDraft
import at.rudeboy.ferratafit.data.StageKind
import at.rudeboy.ferratafit.data.StageLog
import at.rudeboy.ferratafit.data.WorkoutDraft
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Prüft, dass eine angefangene Einheit einen Prozesstod übersteht.
 *
 * Der Anlass war handfest: Wer während des Trainings kurz die Musik wechselte, kam in eine
 * leere App zurück und musste alle Sätze aller Übungen neu eintippen. Android beendet Apps
 * im Hintergrund, sobald es Speicher braucht — der Arbeitsspeicher allein trägt nichts.
 */
class DraftTest {

    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }
    private val HOUR = 60 * 60 * 1000L

    private fun workout(startedAt: Long) = Draft(
        workout = WorkoutDraft(
            dayId = "A",
            startedAt = startedAt,
            currentIndex = 1,
            entries = listOf(
                DraftEntry("pullup", listOf(DraftSet(reps = 8, done = true), DraftSet(reps = 5))),
                DraftEntry("lat_pulldown", listOf(DraftSet(weightKg = 45.0, reps = 10, done = true)))
            ),
            restEndsAt = startedAt + 150_000L,
            restTotal = 150
        )
    )

    // ------------------------------------------------------------------
    // Speichern und Wiederherstellen
    // ------------------------------------------------------------------

    /** Was eingetippt wurde, muss den Weg durch die Datei unverändert überstehen. */
    @Test
    fun eingetippteWerteUeberlebenDasSpeichern() {
        val vorher = workout(1_700_000_000_000L)
        val nachher = json.decodeFromString<Draft>(json.encodeToString(Draft.serializer(), vorher))

        assertEquals(vorher, nachher)
        assertEquals(8, nachher.workout!!.entries[0].sets[0].reps)
        assertTrue("Der abgehakte Satz muss abgehakt bleiben", nachher.workout!!.entries[0].sets[0].done)
        assertEquals(45.0, nachher.workout!!.entries[1].sets[0].weightKg, 0.001)
        assertEquals("Die offene Übung muss dieselbe bleiben", 1, nachher.workout!!.currentIndex)
    }

    /**
     * Die Pausenuhr wird als Endzeitpunkt gespeichert, nicht als Restsekunden.
     * Nur so stimmt sie noch, wenn die App zwischendurch gar nicht lief.
     */
    @Test
    fun pausenuhrUeberlebtAlsEndzeitpunkt() {
        val d = workout(1_700_000_000_000L)
        val zurueck = json.decodeFromString<Draft>(json.encodeToString(Draft.serializer(), d))
        assertEquals(1_700_000_150_000L, zurueck.workout!!.restEndsAt)
    }

    @Test
    fun dehnEtappeUeberlebtEbenfalls() {
        val d = Draft(stage = StageDraft("M1", startedAt = 1L, doneDrills = listOf("a", "c"), minutes = 40))
        val zurueck = json.decodeFromString<Draft>(json.encodeToString(Draft.serializer(), d))
        assertEquals(listOf("a", "c"), zurueck.stage!!.doneDrills)
        assertEquals(40, zurueck.stage!!.minutes)
    }

    /** Ältere Dateien ohne die neuen Felder dürfen nicht zum Absturz führen. */
    @Test
    fun unbekannteFelderStoerenNicht() {
        val d = json.decodeFromString<Draft>("""{"workout":{"dayId":"A","startedAt":5,"quatsch":1}}""")
        assertEquals("A", d.workout!!.dayId)
    }

    // ------------------------------------------------------------------
    // Wann wird wieder aufgenommen?
    // ------------------------------------------------------------------

    @Test
    fun kurzeUnterbrechungLaeuftWortlosWeiter() {
        val now = 1_000_000_000_000L
        // Anruf, Musikwechsel, Fahrt zum Gerät — alles unter der Schwelle
        listOf(0L, 5 * 60_000L, 2 * HOUR, 5 * HOUR).forEach { pause ->
            assertTrue(
                "Nach $pause ms muss wortlos weitergehen",
                Drafts.resumesSilently(workout(now - pause), now)
            )
        }
    }

    /**
     * Eine über Nacht vergessene Einheit darf nicht aufspringen. Wer die App öffnet und
     * unvermittelt in einem halben Training landet, hakt im Zweifel Sätze ab, die er nie
     * gemacht hat — und verdirbt sich damit die Steigerungslogik.
     */
    @Test
    fun vergesseneEinheitFragtNach() {
        val now = 1_000_000_000_000L
        val ueberNacht = workout(now - 14 * HOUR)
        assertFalse(Drafts.resumesSilently(ueberNacht, now))
        assertTrue(Drafts.needsAsking(ueberNacht, now))
        assertFalse(Drafts.isExpired(ueberNacht, now))
    }

    @Test
    fun sehrAlteEinheitVerschwindetWortlos() {
        val now = 1_000_000_000_000L
        val uralt = workout(now - 100 * HOUR)
        assertTrue(Drafts.isExpired(uralt, now))
        assertFalse("Nach vier Tagen fragt niemand mehr", Drafts.needsAsking(uralt, now))
    }

    @Test
    fun ohneAngefangenesPassiertNichts() {
        val leer = Draft()
        assertTrue(leer.isEmpty)
        assertFalse(Drafts.resumesSilently(leer, 1L))
        assertFalse(Drafts.needsAsking(leer, 1L))
        assertFalse(Drafts.isExpired(leer, 1L))
    }

    /**
     * Eine vergessene Einheit würde sonst vierzehn Stunden Trainingsdauer eintragen
     * und jede Auswertung verzerren.
     */
    @Test
    fun dauerWirdGedeckelt() {
        val start = 1_000_000_000_000L
        val nachVierzehnStunden = start + 14 * HOUR
        val gedeckelt = Drafts.cappedDuration(start, nachVierzehnStunden)
        assertEquals(Drafts.MAX_SESSION_MIN * 60_000L, gedeckelt - start)

        // Eine normale Einheit bleibt unangetastet
        val normal = start + 55 * 60_000L
        assertEquals(normal, Drafts.cappedDuration(start, normal))
    }

    @Test
    fun altersangabeIstLesbar() {
        val now = 1_000_000_000_000L
        assertEquals("vor wenigen Minuten", Drafts.ageLabel(workout(now - 10 * 60_000L), now))
        assertEquals("vor 3 Stunden", Drafts.ageLabel(workout(now - 3 * HOUR), now))
        assertEquals("gestern", Drafts.ageLabel(workout(now - 30 * HOUR), now))
        assertEquals("vor 3 Tagen", Drafts.ageLabel(workout(now - 80 * HOUR), now))
    }

    // ------------------------------------------------------------------
    // Begehung zählt aufs heutige Training
    // ------------------------------------------------------------------

    private fun log(stageId: String, at: Long) =
        StageLog(stageId = stageId, kind = StageKind.STRENGTH.name, meters = 100, at = at)

    private val heute = 1_700_000_000_000L

    /** Ein Tag am Fels ist Training genug — die offene Etappe gilt als gegangen. */
    @Test
    fun begehungDecktDieOffeneEtappeAb() {
        assertTrue(Ferrata.coversStage(emptyList(), heute))
        assertTrue(Ferrata.coversStage(listOf(log("S1", heute - 3 * 24 * HOUR)), heute))
    }

    /** Wer heute schon am Gerät war, bekommt nicht noch eine Etappe geschenkt. */
    @Test
    fun nachTrainingAmSelbenTagZaehltSieNichtNochmal() {
        assertFalse(Ferrata.coversStage(listOf(log("S1", heute - 2 * HOUR)), heute))
    }

    /**
     * Zwei Steige an einem Tag sind ein Tag. Sonst schöbe jede weitere Begehung den
     * Wochenzyklus um eine Etappe weiter — genau der Fehler, den EXTRA_STAGE_ID verhindert.
     */
    @Test
    fun zweiteBegehungAmSelbenTagSchiebtNichtsWeiter() {
        val ersteHatGezaehlt = listOf(
            StageLog("S1", StageKind.FERRATA.name, 400, heute - 4 * HOUR, detail = "Erster Steig")
        )
        assertFalse(Ferrata.coversStage(ersteHatGezaehlt, heute))
    }

    /** Einträge außerhalb des Zyklus blockieren nicht — sie haben nie eine Etappe belegt. */
    @Test
    fun sonderkennungBlockiertNicht() {
        val nurAusserhalb = listOf(
            StageLog(Ferrata.EXTRA_STAGE_ID, StageKind.FERRATA.name, 300, heute - 5 * HOUR)
        )
        assertTrue(Ferrata.coversStage(nurAusserhalb, heute))
    }

    @Test
    fun gesternZaehltNichtFuerHeute() {
        assertTrue(Ferrata.coversStage(listOf(log("S1", heute - 26 * HOUR)), heute))
    }

    @Test
    fun sameDayIstRobust() {
        assertTrue(Ferrata.sameDay(heute, heute + 60_000L))
        assertFalse(Ferrata.sameDay(heute, heute + 3 * 24 * HOUR))
        assertFalse("Nullwerte dürfen nicht als gleicher Tag gelten", Ferrata.sameDay(0L, 0L))
    }
}
