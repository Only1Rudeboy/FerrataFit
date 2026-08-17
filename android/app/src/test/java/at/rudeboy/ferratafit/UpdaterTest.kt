package at.rudeboy.ferratafit

import at.rudeboy.ferratafit.update.Updater
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Prüft den Versionsvergleich und die Aufbereitung der Veröffentlichungsnotizen.
 *
 * Der Vergleich entscheidet, ob überhaupt ein Update angeboten wird — ein Fehler hier
 * führt entweder zu einer Endlosschleife („Update verfügbar", obwohl es schon installiert
 * ist) oder dazu, dass neue Fassungen nie auffallen.
 */
class UpdaterTest {

    @Test
    fun `neuere Version wird als neuer erkannt`() {
        assertTrue(Updater.compareVersions("1.3", "1.2") > 0)
        assertTrue(Updater.compareVersions("2.0", "1.9") > 0)
        assertTrue(Updater.compareVersions("1.2.1", "1.2") > 0)
        assertTrue(Updater.compareVersions("1.10", "1.9") > 0)
    }

    @Test
    fun `aeltere Version wird als aelter erkannt`() {
        assertTrue(Updater.compareVersions("1.2", "1.3") < 0)
        assertTrue(Updater.compareVersions("1.9", "2.0") < 0)
        assertTrue(Updater.compareVersions("1.2", "1.2.1") < 0)
    }

    @Test
    fun `gleiche Version ergibt null`() {
        assertEquals(0, Updater.compareVersions("1.2", "1.2"))
        assertEquals(0, Updater.compareVersions("1.2.0", "1.2"))
        assertEquals(0, Updater.compareVersions("v1.2", "1.2"))
    }

    @Test
    fun `fuehrendes v stoert nicht`() {
        assertTrue(Updater.compareVersions("v1.3", "1.2") > 0)
        assertTrue(Updater.compareVersions("v1.3", "v1.2") > 0)
    }

    @Test
    fun `unsaubere Angaben fuehren nicht zum Absturz`() {
        // Was auch immer als Kennung kommt — es darf keine Ausnahme geben
        assertEquals(0, Updater.compareVersions("", ""))
        assertTrue(Updater.compareVersions("1.3-beta", "1.2") > 0)
        assertTrue(Updater.compareVersions("1.2", "unsinn") >= 0)
    }

    @Test
    fun `Veroeffentlichungsnotizen werden lesbar aufbereitet`() {
        val raw = """
            ## Neu: Der Steig

            Ein **Sieben-Tage-Zyklus**, in dem kein Tag leer bleibt.

            | # | Etappe |
            |---|---|
            | 1 | Zug |

            ---

            Siehe [Anleitung](https://example.com/doku).
        """.trimIndent()

        val out = Updater.shortenNotes(raw)

        assertTrue("Überschriftenzeichen bleiben stehen", !out.contains("##"))
        assertTrue("Fettauszeichnung bleibt stehen", !out.contains("**"))
        assertTrue("Verweis nicht aufgelöst", out.contains("Anleitung") && !out.contains("http"))
        assertTrue("Tabelle nicht entfernt", !out.contains("|---|"))
        assertTrue("Inhalt verloren", out.contains("Neu: Der Steig"))
    }

    @Test
    fun `lange Notizen werden gekuerzt`() {
        val many = (1..40).joinToString("\n") { "Zeile $it" }
        val out = Updater.shortenNotes(many, maxLines = 5)

        assertEquals(6, out.lines().size)   // fünf Zeilen plus Auslassungszeichen
        assertTrue(out.endsWith("…"))
    }

    @Test
    fun `kurze Notizen bleiben unveraendert kurz`() {
        val out = Updater.shortenNotes("Kleine Korrektur.", maxLines = 5)
        assertEquals("Kleine Korrektur.", out)
    }
}
