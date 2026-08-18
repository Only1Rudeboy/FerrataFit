package at.rudeboy.ferratafit

import at.rudeboy.ferratafit.data.FerrataGrade
import at.rudeboy.ferratafit.data.FerrataRoutes
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Prüft den Routenkatalog auf Datenfehler.
 *
 * Der Katalog wird aus Recherchedaten erzeugt, und genau dort schleichen sich Fehler ein,
 * die keine Übersetzung meldet: eine Höhenmeterangabe, die in Wahrheit den ganzen
 * Tagesaufstieg meint; ein Schwierigkeitsgrad, den keine Bewertungsskala kennt; ein Steig,
 * der zweimal unter verschiedenen Namen dasteht. Solche Fehler wirken sich direkt auf die
 * Empfehlung aus — deshalb prüft sie diese Datei, nicht das Auge.
 */
class FerrataRoutesTest {

    private val routes = FerrataRoutes.all

    @Test
    fun katalogIstNichtLeer() {
        assertTrue("Der Katalog darf nicht leer sein", routes.size >= 40)
    }

    @Test
    fun jedeKennungIstEindeutig() {
        val doppelt = routes.groupBy { it.id }.filter { it.value.size > 1 }.keys
        assertTrue("Doppelte Kennungen: $doppelt", doppelt.isEmpty())
    }

    @Test
    fun keinNameStehtZweimalDa() {
        val doppelt = routes.groupBy { it.name.lowercase() }.filter { it.value.size > 1 }.keys
        assertTrue("Doppelte Namen: $doppelt", doppelt.isEmpty())
    }

    @Test
    fun jederGradIstLesbar() {
        routes.forEach {
            assertNotNull(
                "Grad '${it.grade}' bei ${it.name} ist keine gültige Bewertung",
                FerrataGrade.parse(it.grade)
            )
        }
    }

    /**
     * Zwischenstufen wie „C/D" müssen als die schwerere Stufe zählen. Läse die App die
     * leichtere, empföhle sie einen D-Steig als C — genau der Fehler, der wehtut.
     */
    @Test
    fun zwischenstufenZaehlenAlsSchwerere() {
        assertEquals(FerrataGrade.D, FerrataGrade.parse("C/D"))
        routes.filter { it.grade.contains("/") }.forEach {
            val schwerer = it.grade.last().uppercaseChar().toString()
            assertEquals(
                "${it.name}: '${it.grade}' muss als $schwerer zählen",
                schwerer, it.gradeEnum.name
            )
        }
    }

    /**
     * Die Klettermeter meinen den gesicherten Steig, nie den ganzen Tagesaufstieg. Wo
     * jemand versehentlich die Tageshöhenmeter einträgt, entstehen Werte weit jenseits
     * dessen, was eine Wand hergibt — und die Empfehlung hält jede echte Route für zu groß.
     */
    @Test
    fun klettermeterSindWandhoehenKeineTageshoehenmeter() {
        routes.forEach {
            assertTrue(
                "${it.name}: ${it.climbMeters} Klettermeter — das ist ein Tagesaufstieg, keine Wand",
                it.climbMeters <= 700
            )
        }
    }

    @Test
    fun zeitenSindInSichStimmig() {
        routes.filter { it.totalMin > 0 && it.ferrataMin > 0 }.forEach {
            assertTrue(
                "${it.name}: Gesamtzeit ${it.totalMin} min ist kürzer als die Kletterzeit ${it.ferrataMin} min",
                it.totalMin >= it.ferrataMin
            )
        }
    }

    @Test
    fun gipfelLiegtUeberDemEinstieg() {
        routes.filter { it.startAlt > 0 && it.summitAlt > 0 }.forEach {
            assertTrue(
                "${it.name}: Gipfel ${it.summitAlt} m liegt nicht über dem Einstieg ${it.startAlt} m",
                it.summitAlt > it.startAlt
            )
        }
    }

    /** Ohne Quelle lässt sich eine Angabe nicht nachprüfen — dann gehört sie nicht in den Katalog. */
    @Test
    fun jederSteigNenntSeineQuelle() {
        routes.forEach {
            assertTrue("${it.name} nennt keine Quelle", it.sources.isNotEmpty())
        }
    }

    @Test
    fun jederSteigHatEineBeschreibung() {
        routes.forEach {
            assertTrue("${it.name} hat keine Beschreibung", it.summary.length > 80)
        }
    }

    /**
     * Ein Notausstieg ist der einzige Grund, aus dem die App eine Stufe nach oben
     * vorschlägt. Er darf deshalb nur dort stehen, wo er belegt ist — im Zweifel nicht.
     */
    @Test
    fun notausstiegIstDieAusnahmeNichtDieRegel() {
        val mitAusstieg = routes.count { it.hasExit }
        assertTrue(
            "$mitAusstieg von ${routes.size} Steigen behaupten einen Notausstieg — im Zweifel gehört er weg",
            mitAusstieg <= routes.size / 4
        )
    }

    /** Wo die Schlüsselstelle angegeben ist, muss sie über dem Gesamtgrad liegen. Sonst ist sie keine. */
    @Test
    fun schluesselstelleLiegtUeberDemGesamtgrad() {
        routes.filter { it.crux.isNotBlank() }.forEach {
            val crux = FerrataGrade.parse(it.crux)
            assertNotNull("${it.name}: Schlüsselstelle '${it.crux}' ist keine gültige Bewertung", crux)
            assertTrue(
                "${it.name}: Schlüsselstelle ${it.crux} liegt nicht über dem Gesamtgrad ${it.grade}",
                crux!!.ordinal >= it.gradeEnum.ordinal
            )
        }
    }

    /**
     * Der Nutzer wollte ausdrücklich keine versicherten Wanderwege im Katalog. Die beiden
     * Fälle, die die Recherche zutage gefördert hat, dürfen nicht zurückkehren.
     */
    @Test
    fun keineVersichertenWanderwege() {
        val raus = listOf("zitterklapfen", "bocksberg", "leiterli")
        routes.forEach { r ->
            raus.forEach {
                assertFalse(
                    "${r.name} ist ein versicherter Bergweg, kein Klettersteig",
                    r.id.contains(it) || r.name.lowercase().contains(it)
                )
            }
        }
    }

    @Test
    fun byIdFindetJedenSteig() {
        routes.forEach { assertEquals(it, FerrataRoutes.byId(it.id)) }
        assertTrue(FerrataRoutes.byId("gibtesnicht") == null)
    }

    /** Unsichere Angaben müssen als solche markiert sein — sonst wirkt Geraten wie Wissen. */
    @Test
    fun strittigeAngabenSindMarkiert() {
        assertTrue(
            "Kein einziger Steig ist als unsicher markiert — das ist bei dieser Quellenlage unglaubwürdig",
            routes.any { !it.verified }
        )
    }
}
