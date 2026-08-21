package at.rudeboy.ferratafit

import at.rudeboy.ferratafit.data.FerrataGrade
import at.rudeboy.ferratafit.data.FerrataMedia
import at.rudeboy.ferratafit.data.FerrataRoutes
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Prüft Fotos und Topos.
 *
 * Bei den Fotos geht es um Recht: Ein einziges Bild mit unfreier Lizenz in einer
 * öffentlichen App ist eine Abmahnung. Deshalb prüft diese Datei jede Lizenz gegen
 * eine Positivliste — nicht gegen eine Sperrliste, die immer etwas vergisst.
 */
class FerrataMediaTest {

    private val freeLicense = Regex(
        "^(CC0|CC BY(-SA)?( [0-9.]+)?( [a-z]{2})?|Public domain|PD|FAL|Copyrighted free use).*",
        RegexOption.IGNORE_CASE
    )

    @Test
    fun jedesFotoIstFreiLizenziert() {
        FerrataMedia.photos.values.flatten().forEach { p ->
            assertTrue(
                "${p.file}: Lizenz '${p.license}' steht nicht auf der Positivliste",
                freeLicense.matches(p.license)
            )
        }
    }

    @Test
    fun jedesFotoNenntUrheberUndQuelle() {
        FerrataMedia.photos.values.flatten().forEach { p ->
            assertTrue("${p.file} ohne Urheber", p.author.isNotBlank())
            assertTrue("${p.file} ohne Commons-Seite", p.pageUrl.startsWith("https://commons.wikimedia.org/"))
            assertTrue("${p.file} lädt nicht von Wikimedia", p.url.startsWith("https://upload.wikimedia.org/"))
        }
    }

    @Test
    fun fotosGehoerenZuEchtenSteigen() {
        val ids = FerrataRoutes.all.map { it.id }.toSet()
        FerrataMedia.photos.keys.forEach { assertTrue("Foto für unbekannten Steig $it", it in ids) }
        FerrataMedia.topos.keys.forEach { assertTrue("Topo für unbekannten Steig $it", it in ids) }
    }

    @Test
    fun fastJederSteigHatFotos() {
        val ohne = FerrataRoutes.all.filter { FerrataMedia.photosFor(it.id).isEmpty() }
        assertTrue(
            "Zu viele Steige ohne Foto: ${ohne.map { it.name }}",
            ohne.size <= 2
        )
    }

    @Test
    fun jederSteigHatEineTopo() {
        FerrataRoutes.all.forEach { r ->
            val segs = FerrataMedia.topoFor(r.id)
            assertTrue("${r.name}: nur ${segs.size} Abschnitte", segs.size >= 3)
        }
    }

    private val kinds = setOf("wall", "traverse", "ladder", "bridge", "ridge", "gully", "cave", "overhang", "walk", "exit")

    @Test
    fun topoAbschnitteSindWohlgeformt() {
        FerrataMedia.topos.forEach { (id, segs) ->
            segs.forEach { s ->
                assertTrue("$id: Art '${s.kind}' unbekannt", s.kind in kinds)
                assertNotNull("$id: Grad '${s.grade}' unlesbar", FerrataGrade.parse(s.grade))
                assertTrue("$id: Beschriftung zu lang: ${s.label}", s.label.length <= 40)
            }
            assertTrue("$id: mehr als eine Schlüsselstelle", segs.count { it.crux } <= 1)
        }
    }

    /**
     * Die schwerste Stelle der Topo darf den Gesamtgrad der Route nicht um mehr als eine
     * Stufe übersteigen — sonst widersprechen sich Katalog und Topo, und eine von beiden
     * verharmlost.
     */
    @Test
    fun topoUndKatalogWidersprechenSichNicht() {
        FerrataRoutes.all.forEach { r ->
            val hardest = FerrataMedia.topoFor(r.id)
                .filter { it.kind != "exit" }
                .maxOfOrNull { it.gradeEnum.ordinal } ?: return@forEach
            val katalog = (FerrataGrade.parse(r.crux) ?: r.gradeEnum).ordinal
            assertTrue(
                "${r.name}: Topo hat ${FerrataGrade.entries[hardest]}, Katalog höchstens ${FerrataGrade.entries[katalog]}",
                hardest <= katalog + 1
            )
        }
    }
}
