package at.rudeboy.ferratafit

import at.rudeboy.ferratafit.data.Ascent
import at.rudeboy.ferratafit.data.Feel
import at.rudeboy.ferratafit.data.Ferrata
import at.rudeboy.ferratafit.data.FerrataGrade
import at.rudeboy.ferratafit.data.FerrataRoute
import at.rudeboy.ferratafit.data.Fit
import at.rudeboy.ferratafit.data.Rank
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Prüft die Regeln des Fels-Bereichs.
 *
 * Hier hängt mehr dran als an sonst einer Stelle der App: Eine zu großzügige Empfehlung
 * schickt jemanden in eine Wand, aus der er nicht mehr herauskommt. Die Prüfungen sind
 * deshalb bewusst darauf ausgelegt, Großzügigkeit aufzudecken.
 */
class FerrataTest {

    private val day = 24 * 60 * 60 * 1000L
    private val now = 1_700_000_000_000L

    private fun ascent(
        grade: FerrataGrade,
        feel: Feel = Feel.GUT,
        daysAgo: Int = 30,
        meters: Int = 300,
        minutes: Int = 180,
        turnedBack: Boolean = false
    ) = Ascent(
        id = "a${grade.name}$daysAgo$feel", date = now - daysAgo * day,
        name = "Test ${grade.label}", grade = grade.name, climbMeters = meters,
        durationMin = minutes, feel = feel.name, turnedBack = turnedBack
    )

    private fun route(grade: FerrataGrade, meters: Int = 300, min: Int = 180, exit: Boolean = true) =
        FerrataRoute(
            id = "r", name = "Testroute", grade = grade.name,
            climbMeters = meters, totalMin = min, hasExit = exit
        )

    // ---------------- Schwierigkeitsangaben lesen ----------------

    @Test
    fun `Zwischenstufen zaehlen als die schwerere`() {
        assertEquals(FerrataGrade.D, FerrataGrade.parse("C/D"))
        assertEquals(FerrataGrade.C, FerrataGrade.parse("C"))
        assertEquals(FerrataGrade.E, FerrataGrade.parse("D/E"))
        assertEquals(FerrataGrade.B, FerrataGrade.parse("A/B"))
    }

    @Test
    fun `unbrauchbare Angaben fuehren nicht zum Absturz`() {
        assertNull(FerrataGrade.parse(""))
        assertNull(FerrataGrade.parse("???"))
    }

    // ---------------- Bestätigte Stufe ----------------

    @Test
    fun `eine einzelne Begehung bestaetigt noch keine Stufe`() {
        assertEquals(-1, Ferrata.masteredIndex(listOf(ascent(FerrataGrade.C))))
    }

    @Test
    fun `zwei saubere Begehungen bestaetigen die Stufe`() {
        val a = listOf(ascent(FerrataGrade.C, daysAgo = 60), ascent(FerrataGrade.C, daysAgo = 30))
        assertEquals(FerrataGrade.C, Ferrata.mastered(a))
    }

    @Test
    fun `knapp geschaffte Begehungen bestaetigen nichts`() {
        val a = listOf(
            ascent(FerrataGrade.C, Feel.GRENZWERTIG, 60),
            ascent(FerrataGrade.C, Feel.ZU_VIEL, 30)
        )
        assertEquals(-1, Ferrata.masteredIndex(a))
    }

    @Test
    fun `Umkehren bestaetigt keine Stufe, zaehlt aber die Hoehenmeter`() {
        val a = listOf(
            ascent(FerrataGrade.C, daysAgo = 60, turnedBack = true, meters = 150),
            ascent(FerrataGrade.C, daysAgo = 30, turnedBack = true, meters = 150)
        )
        assertEquals(-1, Ferrata.masteredIndex(a))
        assertEquals("Umkehren zählt trotzdem voll", 300, Ferrata.ascentMeters(a))
    }

    // ---------------- Die zentrale Sicherheitsregel ----------------

    @Test
    fun `Kraft allein hebt den Rahmen nicht`() {
        // Sehr trainiert, aber noch nie am Fels: Die Form erlaubt E, die Erfahrung nur B.
        val empfohlen = Ferrata.recommended(emptyList(), readiness = 95, now = now)
        assertEquals(
            "Eine hohe Trainingszahl darf fehlende Routine nicht ersetzen",
            FerrataGrade.B, empfohlen
        )
    }

    @Test
    fun `nie mehr als eine Stufe ueber dem Bestaetigten`() {
        // Zweimal B sauber, Form auf Anschlag — es darf trotzdem nur C herauskommen
        val a = listOf(ascent(FerrataGrade.B, daysAgo = 60), ascent(FerrataGrade.B, daysAgo = 30))
        assertEquals(FerrataGrade.C, Ferrata.recommended(a, readiness = 100, now = now))
    }

    @Test
    fun `fehlende Form begrenzt trotz Erfahrung`() {
        val a = listOf(ascent(FerrataGrade.D, daysAgo = 60), ascent(FerrataGrade.D, daysAgo = 30))
        val empfohlen = Ferrata.recommended(a, readiness = 30, now = now)
        assertEquals("Außer Form heißt kleinere Stufe", FerrataGrade.B, empfohlen)
    }

    @Test
    fun `eine grenzwertige Begehung deckelt auf ihre Stufe`() {
        val a = listOf(
            ascent(FerrataGrade.B, daysAgo = 90), ascent(FerrataGrade.B, daysAgo = 80),
            ascent(FerrataGrade.C, Feel.GRENZWERTIG, daysAgo = 10)
        )
        assertEquals(
            "Nach einer knappen C-Route darf nicht C/D empfohlen werden",
            FerrataGrade.C, Ferrata.recommended(a, readiness = 100, now = now)
        )
    }

    @Test
    fun `eine spaetere saubere Begehung hebt den Deckel wieder`() {
        val a = listOf(
            ascent(FerrataGrade.B, daysAgo = 90), ascent(FerrataGrade.B, daysAgo = 80),
            ascent(FerrataGrade.C, Feel.GRENZWERTIG, daysAgo = 40),
            ascent(FerrataGrade.C, Feel.LOCKER, daysAgo = 10)
        )
        assertTrue(
            "Nach einer sauberen C darf der Deckel steigen",
            Ferrata.recommendedIndex(a, 100, now) >= FerrataGrade.C.ordinal
        )
    }

    @Test
    fun `nach langer Pause eine Stufe zurueck`() {
        val a = listOf(
            ascent(FerrataGrade.C, daysAgo = 400), ascent(FerrataGrade.C, daysAgo = 380)
        )
        val frisch = Ferrata.recommendedIndex(
            listOf(ascent(FerrataGrade.C, daysAgo = 30), ascent(FerrataGrade.C, daysAgo = 20)), 100, now
        )
        val verstaubt = Ferrata.recommendedIndex(a, 100, now)
        assertTrue("Nach über einem Jahr Pause muss es zurückgehen", verstaubt < frisch)
    }

    @Test
    fun `Trainingspause senkt den Rahmen ebenfalls`() {
        val a = listOf(ascent(FerrataGrade.C, daysAgo = 30), ascent(FerrataGrade.C, daysAgo = 20))
        val inForm = Ferrata.recommendedIndex(a, 100, now, weeksSinceTraining = 0)
        val pause = Ferrata.recommendedIndex(a, 100, now, weeksSinceTraining = 4)
        assertTrue(pause < inForm)
    }

    // ---------------- Routen einsortieren ----------------

    @Test
    fun `ohne Begehungen ist eine schwere Route zu frueh`() {
        assertEquals(Fit.ZU_FRUEH, Ferrata.fitFor(route(FerrataGrade.D), emptyList(), 90))
    }

    @Test
    fun `eine Stufe hoeher nur mit Notausstieg`() {
        val a = listOf(ascent(FerrataGrade.B, daysAgo = 60), ascent(FerrataGrade.B, daysAgo = 30))
        val mitAusstieg = Ferrata.fitFor(route(FerrataGrade.C, exit = true), a, 90)
        val ohneAusstieg = Ferrata.fitFor(route(FerrataGrade.C, meters = 600, min = 400, exit = false), a, 90)
        assertEquals(Fit.ZIEL, mitAusstieg)
        assertEquals(
            "Ohne Notausstieg und lang darf keine Steigerung vorgeschlagen werden",
            Fit.ZU_FRUEH, ohneAusstieg
        )
    }

    @Test
    fun `eine deutlich laengere Route derselben Stufe gilt als knapp`() {
        val a = listOf(
            ascent(FerrataGrade.C, daysAgo = 60, meters = 200, minutes = 120),
            ascent(FerrataGrade.C, daysAgo = 30, meters = 200, minutes = 120)
        )
        assertEquals(Fit.KNAPP, Ferrata.fitFor(route(FerrataGrade.C, meters = 600, min = 400), a, 90))
    }

    @Test
    fun `zwei Stufen darueber bleiben immer zu frueh`() {
        val a = listOf(ascent(FerrataGrade.B, daysAgo = 60), ascent(FerrataGrade.B, daysAgo = 30))
        assertEquals(Fit.ZU_FRUEH, Ferrata.fitFor(route(FerrataGrade.D), a, 100))
    }

    @Test
    fun `eine Route ueber der Form bleibt zu frueh`() {
        val a = listOf(ascent(FerrataGrade.D, daysAgo = 60), ascent(FerrataGrade.D, daysAgo = 30))
        // Erfahrung erlaubt E, aber die Form ist schlecht
        assertEquals(Fit.ZU_FRUEH, Ferrata.fitFor(route(FerrataGrade.D), a, 20))
    }

    // ---------------- Rang ----------------

    @Test
    fun `ohne Begehungen ist man Talgaenger`() {
        assertEquals(Rank.TALGAENGER, Ferrata.rank(emptyList()))
    }

    @Test
    fun `die erste Begehung macht zum Steigfinder`() {
        assertEquals(Rank.STEIGFINDER, Ferrata.rank(listOf(ascent(FerrataGrade.A))))
    }

    @Test
    fun `der Rang braucht alle drei Bedingungen`() {
        // Drei saubere Begehungen, aber zu wenige Höhenmeter
        val wenigMeter = List(3) { ascent(FerrataGrade.A, daysAgo = 30 + it, meters = 50) }
        assertEquals(Rank.STEIGFINDER, Ferrata.rank(wenigMeter))

        // Genug Höhenmeter und Begehungen
        val genug = List(3) { ascent(FerrataGrade.A, daysAgo = 30 + it, meters = 250) }
        assertEquals(Rank.DRAHTSEILGEHER, Ferrata.rank(genug))
    }

    @Test
    fun `eine einzelne harte Route reisst den Rang nicht hoch`() {
        val einmalE = listOf(ascent(FerrataGrade.E, meters = 800))
        assertEquals(
            "Ein kühner Einzelversuch darf nichts freischalten",
            Rank.STEIGFINDER, Ferrata.rank(einmalE)
        )
    }

    @Test
    fun `der naechste Rang nennt, was fehlt`() {
        val (next, hint) = Ferrata.nextRankHint(listOf(ascent(FerrataGrade.A)))!!
        assertEquals(Rank.DRAHTSEILGEHER, next)
        assertTrue("Der Hinweis muss konkret sein: $hint", hint.contains("fehlt"))
    }

    // ---------------- Haltung der Texte ----------------

    @Test
    fun `Umkehren wird nicht als Misserfolg benannt`() {
        val text = Ferrata.completionLine(ascent(FerrataGrade.C, turnedBack = true))
        assertTrue(text.contains("keine Niederlage"))
    }

    @Test
    fun `kein Text fordert zu einer Tour auf`() {
        val texte = Fit.entries.map { Ferrata.fitLabel(it) } + Ferrata.DISCLAIMER
        texte.forEach { t ->
            listOf("geh ", "musst", "solltest du unbedingt", "trau dich").forEach { wort ->
                assertTrue("Aufforderung in: $t", !t.lowercase().contains(wort))
            }
        }
    }

    @Test
    fun `der Sicherheitshinweis nennt die Grenzen der App`() {
        listOf("Wetter", "veraltet", "Einstieg").forEach {
            assertTrue("Im Hinweis fehlt: $it", Ferrata.DISCLAIMER.contains(it))
        }
    }

    // ------------------------------------------------------------------
    // Tagesskizze
    // ------------------------------------------------------------------
    // Dieselben Werte prüft test-ferrata.mjs im Browser — die Skizze muss auf
    // beiden Plattformen dieselben Anteile zeichnen.

    @Test
    fun tagesskizzeAnteileSummierenAufEins() {
        val (a, b, c) = Ferrata.daySegments(90, 120, 90)
        assertEquals(1f, a + b + c, 1e-5f)
        assertTrue("Der Steig ist der größte Abschnitt", b > a && b > c)
    }

    @Test
    fun tagesskizzeOhneZeitenDrittel() {
        val (a, b, c) = Ferrata.daySegments(0, 0, 0)
        assertEquals(a, b, 1e-6f)
        assertEquals(b, c, 1e-6f)
    }

    @Test
    fun miniZustiegBleibtSichtbar() {
        assertTrue(Ferrata.daySegments(5, 300, 60).first >= 0.1f)
    }
}
