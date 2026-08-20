package at.rudeboy.ferratafit.data

/**
 * Koordinaten für die Kartenansicht: Einstiege der Steige, Landesumriss, Orte.
 *
 * Erzeugt aus derselben Recherche wie web/ferrageo.js — beide Fassungen müssen
 * deckungsgleich sein. Der Umriss ist eine vereinfachte Silhouette für die kleine
 * Karte, keine amtliche Grenze; die Einstiegspunkte stammen aus OpenStreetMap und
 * den Tourenportalen und sind auf die Wand genau, nicht auf den Meter.
 */
data class GeoPoint(val id: String, val lat: Double, val lon: Double)

data class Landmark(val name: String, val lat: Double, val lon: Double)

object FerrataGeo {

    const val MIN_LAT = 46.8180
    const val MAX_LAT = 47.6200
    const val MIN_LON = 9.5020
    const val MAX_LON = 10.2650

    val points: List<GeoPoint> = listOf(
        GeoPoint("alpin-live-linke-route", 47.06478, 9.75599),
        GeoPoint("alpin-live-rechte-route", 47.06478, 9.75599),
        GeoPoint("kellenegg", 47.11003, 9.75416),
        GeoPoint("abendrot-widerschrofen", 47.35090, 9.95680),
        GeoPoint("walder-widerschrofen", 47.35100, 9.95670),
        GeoPoint("wandfluh", 47.23287, 9.94337),
        GeoPoint("kanzelwand-erlebnis-walsersteig", 47.33469, 10.20721),
        GeoPoint("mindelheimer", 47.31147, 10.21681),
        GeoPoint("zweilander-sport-kanzelwand", 47.33482, 10.21138),
        GeoPoint("klostertaler-am-fallbach", 47.12565, 9.96126),
        GeoPoint("karhorn-ostgrat", 47.25050, 10.15575),
        GeoPoint("karhorn-westgrat-panorama", 47.24729, 10.14783),
        GeoPoint("wasserfall-st-anton-im-montafon", 47.11428, 9.87407),
        GeoPoint("ubungs-klettergarten-latschau", 47.07917, 9.87708),
        GeoPoint("ubungs-rifa", 46.97443, 10.04460),
        GeoPoint("burg", 46.97348, 9.99619),
        GeoPoint("kalbersee-variante-b", 47.06810, 9.97362),
        GeoPoint("kalbersee-variante-c-kantenferrata", 47.06810, 9.97362),
        GeoPoint("kalbersee-variante-d", 47.06810, 9.97362),
        GeoPoint("kaenzele-rechte-variante", 47.48710, 9.75790),
        GeoPoint("kanzele", 47.48715, 9.75722),
        GeoPoint("via-kapf", 47.34224, 9.68457),
        GeoPoint("via-kessi", 47.34164, 9.68400),
        GeoPoint("via-orfla", 47.33001, 9.66785),
        GeoPoint("saulakopf", 47.07864, 9.77144),
        GeoPoint("sulzfluh-sudwandsteig", 47.01015, 9.84355),
        GeoPoint("bruckenwand", 47.09941, 9.83731),
        GeoPoint("fenster", 47.09941, 9.83731),
        GeoPoint("gauablickhohle", 47.02282, 9.84769),
        GeoPoint("hohe-wand", 47.09941, 9.83731),
        GeoPoint("blodigrinne-drusenfluh", 47.03446, 9.80481),
        GeoPoint("rongg-wasserfall", 46.97540, 9.91621),
        GeoPoint("madrisella", 46.96275, 9.98254),
        GeoPoint("pfeilerwand", 47.09941, 9.83731),
        GeoPoint("robischlucht-gargellner", 46.97770, 9.91849),
        GeoPoint("schlosswand-schluchtweg-uberschrei", 47.09987, 9.83980),
        GeoPoint("vaude-gargellner-kopfe-schmugglers", 46.95919, 9.88604),
        GeoPoint("neyerschartensteig", 47.08516, 9.78565),
        GeoPoint("kleinlitzner-sudgrat-ernst-scheib", 46.89878, 10.03282),
        GeoPoint("staumauer-silvrettasee", 46.91601, 10.09194),
        GeoPoint("uebungs-wiesbadener-huette-linker", 46.87131, 10.11407),
        GeoPoint("ubungs-e-wiesbadener-hutte", 46.87104, 10.11452),
        GeoPoint("hochjoch-hochalpila-grat", 47.06003, 9.98271),
        GeoPoint("hochjoch-westwand", 47.06832, 9.98190),
    )

    fun byId(id: String): GeoPoint? = points.firstOrNull { it.id == id }

    /** Vereinfachte Silhouette Vorarlbergs, im Uhrzeigersinn. */
    val outline: List<Pair<Double, Double>> = listOf(
        47.4900 to 9.5550,
        47.4950 to 9.5950,
        47.4980 to 9.6400,
        47.5050 to 9.6900,
        47.5030 to 9.7350,
        47.5350 to 9.7450,
        47.5550 to 9.7300,
        47.5650 to 9.7700,
        47.5850 to 9.8050,
        47.6000 to 9.8450,
        47.5800 to 9.8800,
        47.5500 to 9.9100,
        47.5250 to 9.9500,
        47.5100 to 9.9900,
        47.4850 to 10.0300,
        47.4630 to 10.0700,
        47.4400 to 10.0950,
        47.4200 to 10.1300,
        47.4050 to 10.1700,
        47.3950 to 10.2150,
        47.3750 to 10.2450,
        47.3400 to 10.2450,
        47.3050 to 10.2250,
        47.2700 to 10.2120,
        47.2400 to 10.2180,
        47.2050 to 10.2220,
        47.1600 to 10.2180,
        47.1300 to 10.2120,
        47.1000 to 10.1700,
        47.0650 to 10.1300,
        47.0250 to 10.1080,
        46.9750 to 10.1150,
        46.9300 to 10.1050,
        46.8850 to 10.1350,
        46.8580 to 10.1300,
        46.8380 to 10.1100,
        46.8450 to 10.0600,
        46.8620 to 10.0100,
        46.8850 to 9.9600,
        46.9120 to 9.9200,
        46.9450 to 9.8950,
        46.9850 to 9.8700,
        47.0200 to 9.8350,
        47.0420 to 9.7950,
        47.0520 to 9.7500,
        47.0550 to 9.7050,
        47.0600 to 9.6550,
        47.0600 to 9.6100,
        47.0900 to 9.5900,
        47.1250 to 9.5750,
        47.1580 to 9.5550,
        47.1950 to 9.5550,
        47.2280 to 9.5600,
        47.2550 to 9.5350,
        47.2700 to 9.5220,
        47.3050 to 9.5450,
        47.3400 to 9.5780,
        47.3800 to 9.6150,
        47.4200 to 9.6380,
        47.4550 to 9.6300,
        47.4750 to 9.5900,
    )

    val landmarks: List<Landmark> = listOf(
        Landmark("Bregenz", 47.5030, 9.7470),
        Landmark("Dornbirn", 47.4120, 9.7440),
        Landmark("Feldkirch", 47.2390, 9.5980),
        Landmark("Bludenz", 47.1530, 9.8220),
        Landmark("Schruns", 47.0800, 9.9180),
        Landmark("Lech", 47.2080, 10.1420),
    )
}
