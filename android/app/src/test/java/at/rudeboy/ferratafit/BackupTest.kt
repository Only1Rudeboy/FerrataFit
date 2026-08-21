package at.rudeboy.ferratafit

import at.rudeboy.ferratafit.data.Backup
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Prüft die Fälligkeitsregel der automatischen Sicherung.
 *
 * Das Schreiben selbst braucht Android und läuft am Gerät; hier geht es um die
 * Frage, WANN geschrieben wird — die darf sich nicht stillschweigend ändern,
 * sonst sichert die App entweder nie oder bei jedem Start.
 */
class BackupTest {

    private val TAG = 24 * 60 * 60 * 1000L

    @Test
    fun ohneJedeSicherungIstSofortFaellig() {
        assertTrue(Backup.due(lastBackupAt = 0L, now = 1L))
    }

    @Test
    fun eineWocheDanachIstWiederFaellig() {
        val letzte = 1_000_000_000_000L
        assertFalse("Nach drei Tagen noch nicht", Backup.due(letzte, letzte + 3 * TAG))
        assertFalse("Am sechsten Tag noch nicht", Backup.due(letzte, letzte + 6 * TAG))
        assertTrue("Am siebten Tag ja", Backup.due(letzte, letzte + 7 * TAG))
    }
}
