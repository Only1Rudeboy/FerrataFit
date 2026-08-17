package at.rudeboy.ferratafit

import at.rudeboy.ferratafit.data.Advice
import at.rudeboy.ferratafit.data.Catalog
import at.rudeboy.ferratafit.data.Profile
import at.rudeboy.ferratafit.data.Progression
import at.rudeboy.ferratafit.data.Session
import at.rudeboy.ferratafit.data.SessionEdit
import at.rudeboy.ferratafit.data.SetLog
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Prüft das Nachbearbeiten von Einheiten.
 *
 * Der Zweck des Ganzen ist, dass ein Tippfehler die Progression nicht dauerhaft
 * verfälscht — genau das wird hier nachgestellt: falscher Wert eintragen, korrigieren,
 * und prüfen, dass der Vorschlag wieder stimmt.
 */
class SessionEditTest {

    private val day = 24 * 60 * 60 * 1000L
    private val now = 1_700_000_000_000L
    private val profile = Profile(cycleStart = now, plateStepKg = 5.0, bodyweightKg = 78.0)

    private fun session(id: String, daysAgo: Int, weight: Double, reps: Int, sets: Int = 3) =
        Session(
            id = id, dayId = "A",
            startedAt = now - daysAgo * day,
            finishedAt = now - daysAgo * day + 3_600_000L,
            sets = (0 until sets).map { SetLog("latpull_wide", it, weight, reps) }
        )

    // ---------------- Neunummerierung ----------------

    @Test
    fun `nach dem Loeschen bleibt keine Luecke`() {
        val sets = listOf(
            SetLog("latpull_wide", 0, 40.0, 10),
            SetLog("latpull_wide", 1, 40.0, 10),
            SetLog("latpull_wide", 2, 40.0, 10)
        )
        val ohneMittleren = SessionEdit.renumber(listOf(sets[0], sets[2]))
        assertEquals(listOf(0, 1), ohneMittleren.map { it.setIndex })
    }

    @Test
    fun `jede Uebung wird eigenstaendig durchnummeriert`() {
        val gemischt = listOf(
            SetLog("latpull_wide", 0, 40.0, 10),
            SetLog("curl", 0, 15.0, 12),
            SetLog("latpull_wide", 1, 40.0, 10),
            SetLog("curl", 1, 15.0, 12)
        )
        val out = SessionEdit.renumber(gemischt)
        assertEquals(listOf(0, 0, 1, 1), out.map { it.setIndex })
        // Die Reihenfolge der Sätze bleibt, nur die Nummern werden korrigiert
        assertEquals(listOf("latpull_wide", "curl", "latpull_wide", "curl"), out.map { it.exerciseId })
    }

    // ---------------- Zuletzt verwendete Last ----------------

    @Test
    fun `zuletzt verwendete Last kommt aus den Einheiten`() {
        val sessions = listOf(session("a", 14, 40.0, 10), session("b", 7, 45.0, 10))
        assertEquals(45.0, SessionEdit.recomputeLastLoads(sessions)["latpull_wide"]!!, 0.01)
    }

    @Test
    fun `nach dem Loeschen faellt die Last auf den verbliebenen Wert`() {
        val sessions = listOf(session("a", 14, 40.0, 10), session("b", 7, 45.0, 10))
        val rest = SessionEdit.remove(sessions, "b")
        assertEquals(40.0, SessionEdit.recomputeLastLoads(rest)["latpull_wide"]!!, 0.01)
    }

    @Test
    fun `ohne Einheiten bleibt nichts uebrig`() {
        assertTrue(SessionEdit.recomputeLastLoads(emptyList()).isEmpty())
    }

    // ---------------- Ersetzen und Entfernen ----------------

    @Test
    fun `Ersetzen behaelt die Reihenfolge bei`() {
        val sessions = listOf(session("a", 14, 40.0, 10), session("b", 7, 45.0, 10))
        val geaendert = sessions[0].copy(sets = sessions[0].sets.map { it.copy(weightKg = 42.5) })
        val out = SessionEdit.replace(sessions, geaendert)
        assertEquals(listOf("a", "b"), out.map { it.id })
        assertEquals(42.5, out[0].sets[0].weightKg, 0.01)
    }

    @Test
    fun `eine Einheit ohne Saetze laesst sich nicht sichern`() {
        assertFalse(SessionEdit.isSaveable(session("a", 1, 40.0, 10).copy(sets = emptyList())))
        assertTrue(SessionEdit.isSaveable(session("a", 1, 40.0, 10)))
    }

    // ---------------- Der eigentliche Zweck ----------------

    @Test
    fun `ein Tippfehler verfaelscht die Steigerung`() {
        // 400 statt 40 kg eingetragen — die App würde ab jetzt Unsinn vorschlagen
        val vertippt = listOf(session("a", 7, 40.0, 12), session("b", 3, 400.0, 12))
        val vorschlag = Progression.suggest(
            Catalog.byId("latpull_wide")!!, vertippt, profile, now
        )
        assertTrue(
            "Der falsche Wert müsste durchschlagen, war: ${vorschlag.weightKg}",
            vorschlag.weightKg > 100
        )
    }

    @Test
    fun `nach der Korrektur stimmt der Vorschlag wieder`() {
        val vertippt = listOf(session("a", 7, 40.0, 12), session("b", 3, 400.0, 12))
        val korrigiert = SessionEdit.replace(
            vertippt,
            vertippt[1].copy(sets = vertippt[1].sets.map { it.copy(weightKg = 40.0) })
        )
        val vorschlag = Progression.suggest(
            Catalog.byId("latpull_wide")!!, korrigiert, profile, now
        )
        // Zweimal 40 kg mit 12 Wiederholungen: die 2-für-2-Regel greift, plus eine Stufe
        assertEquals(Advice.INCREASE, vorschlag.advice)
        assertEquals(45.0, vorschlag.weightKg, 0.01)
    }

    @Test
    fun `nach dem Loeschen rechnet die Progression ohne die Einheit weiter`() {
        val vertippt = listOf(session("a", 7, 40.0, 12), session("b", 3, 400.0, 12))
        val bereinigt = SessionEdit.remove(vertippt, "b")
        val vorschlag = Progression.suggest(
            Catalog.byId("latpull_wide")!!, bereinigt, profile, now
        )
        // Nur noch eine Einheit am oberen Ende — die 2-für-2-Regel verlangt eine zweite
        assertEquals(Advice.HOLD, vorschlag.advice)
        assertEquals(40.0, vorschlag.weightKg, 0.01)
    }

    @Test
    fun `wird alles geloescht, faengt die Uebung wieder bei der Schaetzung an`() {
        val vorschlag = Progression.suggest(
            Catalog.byId("latpull_wide")!!, emptyList(), profile, now
        )
        assertEquals(Advice.START, vorschlag.advice)
        assertEquals(35.0, vorschlag.weightKg, 0.01)
    }
}
