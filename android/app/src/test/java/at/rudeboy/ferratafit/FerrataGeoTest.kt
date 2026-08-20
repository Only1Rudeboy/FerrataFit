package at.rudeboy.ferratafit

import at.rudeboy.ferratafit.data.FerrataGeo
import at.rudeboy.ferratafit.data.FerrataRoutes
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.hypot

/**
 * Prüft die Kartendaten.
 *
 * Ein falscher Punkt fällt auf der kleinen Karte nicht auf — er sieht aus wie ein
 * Steig, den es dort eben gibt. Deshalb prüfen hier Zahlen statt Augen: Jeder Steig
 * hat genau einen Punkt, jeder Punkt liegt im Land, und was am selben Fels liegt,
 * liegt auch auf der Karte beieinander.
 */
class FerrataGeoTest {

    /** Abstand zweier Punkte in Metern — grob, für Plausibilität reicht es. */
    private fun distM(a: String, b: String): Int {
        val pa = FerrataGeo.byId(a)!!
        val pb = FerrataGeo.byId(b)!!
        val dy = (pa.lat - pb.lat) * 111_320.0
        val dx = (pa.lon - pb.lon) * 111_320.0 * cos(Math.toRadians((pa.lat + pb.lat) / 2))
        return hypot(dx, dy).toInt()
    }

    @Test
    fun jederSteigHatGenauEinenPunkt() {
        val routeIds = FerrataRoutes.all.map { it.id }.toSet()
        val geoIds = FerrataGeo.points.map { it.id }
        assertTrue(
            "Ohne Punkt: ${routeIds - geoIds.toSet()}",
            geoIds.containsAll(routeIds)
        )
        assertTrue(
            "Punkt ohne Steig: ${geoIds.toSet() - routeIds}",
            routeIds.containsAll(geoIds)
        )
        assertTrue("Doppelte Punkte", geoIds.size == geoIds.toSet().size)
    }

    @Test
    fun allePunkteLiegenImLand() {
        FerrataGeo.points.forEach {
            assertTrue(
                "${it.id} liegt bei ${it.lat}/${it.lon} — außerhalb Vorarlbergs",
                it.lat in 46.80..47.65 && it.lon in 9.45..10.30
            )
        }
    }

    /** Steige am selben Fels müssen auch auf der Karte beieinander liegen. */
    @Test
    fun gleicheWandGleicherOrt() {
        listOf(
            Triple("alpin-live-linke-route", "alpin-live-rechte-route", 150),
            Triple("kalbersee-variante-b", "kalbersee-variante-d", 150),
            Triple("via-kapf", "via-kessi", 400),
            Triple("hohe-wand", "pfeilerwand", 300),
            Triple("kanzele", "kaenzele-rechte-variante", 200)
        ).forEach { (a, b, limit) ->
            assertTrue("$a und $b liegen ${distM(a, b)} m auseinander", distM(a, b) <= limit)
        }
    }

    /** Und getrennte Einstiege dürfen NICHT zusammenfallen — die Karte muss sie trennen. */
    @Test
    fun getrennteEinstiegeFallenNichtZusammen() {
        assertTrue(
            "Karhorn Ost- und Westgrat haben eigene Einstiege",
            distM("karhorn-ostgrat", "karhorn-westgrat-panorama") > 300
        )
    }

    @Test
    fun umrissUmschliesstAllePunkte() {
        // Grobprüfung über den umschließenden Kasten des Umrisses — ein echter
        // Punkt-im-Polygon-Test wäre hier Scheingenauigkeit, die Silhouette ist vereinfacht.
        val lats = FerrataGeo.outline.map { it.first }
        val lons = FerrataGeo.outline.map { it.second }
        FerrataGeo.points.forEach {
            assertTrue("${it.id} liegt außerhalb des Umriss-Kastens",
                it.lat in lats.min()..lats.max() && it.lon in lons.min()..lons.max())
        }
    }

    @Test
    fun kartengrenzenSindStimmig() {
        assertTrue(FerrataGeo.MIN_LAT < FerrataGeo.MAX_LAT)
        assertTrue(FerrataGeo.MIN_LON < FerrataGeo.MAX_LON)
        // Das Seitenverhältnis der Karte muss in vernünftigen Grenzen bleiben
        val aspect = (FerrataGeo.MAX_LON - FerrataGeo.MIN_LON) *
            cos(Math.toRadians((FerrataGeo.MIN_LAT + FerrataGeo.MAX_LAT) / 2)) /
            (FerrataGeo.MAX_LAT - FerrataGeo.MIN_LAT)
        assertTrue("Seitenverhältnis $aspect", abs(aspect) in 0.4..2.5)
    }
}
