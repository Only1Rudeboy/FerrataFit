package at.rudeboy.ferratafit

import at.rudeboy.ferratafit.data.LocalMedia
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.nio.file.Files
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/**
 * Prüft das Einlesen eines Medienpakets.
 *
 * Das Paket kommt von außen — eine ZIP-Datei, die irgendjemand gebaut haben kann. Die
 * wichtigste Prüfung ist deshalb keine Funktions-, sondern eine Sicherheitsprüfung:
 * Ein Archiv mit `../`-Pfaden darf nicht aus dem App-Ordner ausbrechen und dort Dateien
 * überschreiben. Das ist ein bekannter Angriff („Zip Slip"), und er ist leicht zu übersehen.
 */
class LocalMediaTest {

    private fun zip(vararg entries: Pair<String, ByteArray>): ByteArray {
        val bytes = ByteArrayOutputStream()
        ZipOutputStream(bytes).use { z ->
            entries.forEach { (name, data) ->
                z.putNextEntry(ZipEntry(name))
                z.write(data)
                z.closeEntry()
            }
        }
        return bytes.toByteArray()
    }

    private val jpegStub = byteArrayOf(0xFF.toByte(), 0xD8.toByte(), 0xFF.toByte(), 0, 1, 2, 3)

    private fun index(routeId: String = "saulakopf") = """
        {"created":"2026-08-21","routes":{"$routeId":{"name":"Test",
         "photos":[{"file":"$routeId/foto01.jpg","caption":"Wand","source":"https://example.org/x"}],
         "topos":[{"file":"$routeId/topo01.jpg","source":"https://example.org/x"}]}}}
    """.trimIndent().toByteArray()

    private fun tempDir(): File = Files.createTempDirectory("medien").toFile()

    @Test
    fun gutesPaketWirdEingelesen() {
        val target = tempDir()
        val count = LocalMedia.importZipStream(
            ByteArrayInputStream(zip(
                "index.json" to index(),
                "saulakopf/foto01.jpg" to jpegStub,
                "saulakopf/topo01.jpg" to jpegStub
            )),
            target
        )
        assertEquals(2, count)
        val pack = LocalMedia.loadDir(target)
        assertNotNull(pack)
        assertEquals(1, pack!!.photoCount)
        assertEquals(1, pack.topoCount)
        assertEquals("Wand", pack.forRoute("saulakopf")!!.photos[0].caption)
        target.deleteRecursively()
    }

    /** Der eigentliche Grund für diese Datei. */
    @Test
    fun zipSlipWirdAbgewiesen() {
        val target = tempDir()
        val outside = File(target.parentFile, "boese-${System.nanoTime()}.txt")
        var thrown = false
        try {
            LocalMedia.importZipStream(
                ByteArrayInputStream(zip(
                    "index.json" to index(),
                    "../${outside.name}" to "x".toByteArray()
                )),
                target
            )
        } catch (e: SecurityException) {
            thrown = true
        }
        assertTrue("Ein Pfad mit ../ muss abgewiesen werden", thrown)
        assertFalse("Außerhalb des Zielordners darf nichts entstehen", outside.exists())
        assertFalse("Ein abgewiesenes Paket hinterlässt keinen halben Bestand", File(target, "index.json").exists())
        outside.delete()
        target.deleteRecursively()
    }

    @Test
    fun ohneIndexIstEsKeinPaket() {
        val target = tempDir()
        var thrown = false
        try {
            LocalMedia.importZipStream(ByteArrayInputStream(zip("a/foto01.jpg" to jpegStub)), target)
        } catch (e: IllegalArgumentException) {
            thrown = true
        }
        assertTrue(thrown)
        assertNull(LocalMedia.loadDir(target))
        target.deleteRecursively()
    }

    /** Fehlende Dateien fallen still heraus — keine leeren Kacheln. */
    @Test
    fun fehlendeDateienWerdenUebersprungen() {
        val target = tempDir()
        LocalMedia.importZipStream(
            ByteArrayInputStream(zip("index.json" to index(), "saulakopf/foto01.jpg" to jpegStub)),
            target
        )
        val pack = LocalMedia.loadDir(target)!!
        assertEquals(1, pack.photoCount)
        assertEquals("Die Topo-Datei fehlt im Archiv und darf nicht gelistet sein", 0, pack.topoCount)
        target.deleteRecursively()
    }

    @Test
    fun neuesPaketErsetztDasAlte() {
        val target = tempDir()
        LocalMedia.importZipStream(
            ByteArrayInputStream(zip("index.json" to index("saulakopf"), "saulakopf/foto01.jpg" to jpegStub)), target)
        LocalMedia.importZipStream(
            ByteArrayInputStream(zip("index.json" to index("kellenegg"), "kellenegg/foto01.jpg" to jpegStub)), target)
        val pack = LocalMedia.loadDir(target)!!
        assertNull("Der alte Steig darf nicht mehr da sein", pack.forRoute("saulakopf"))
        assertNotNull(pack.forRoute("kellenegg"))
        target.deleteRecursively()
    }
}
