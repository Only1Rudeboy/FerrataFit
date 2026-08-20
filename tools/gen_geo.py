# -*- coding: utf-8 -*-
"""Erzeugt FerrataGeo.kt und ferrageo.js aus der Koordinaten-Recherche.

Wie beim Routenkatalog gilt: Beide Fassungen kommen aus einer Quelle, und die
Korrekturen der Plausibilitaetspruefung stehen als Code hier drin, nicht als Prosa.
"""
import json, os, sys

SP = os.path.dirname(os.path.abspath(__file__))
d = json.load(open(os.path.join(SP, 'geo-result.json'), encoding='utf-8'))
routes = json.load(open(os.path.join(SP, 'routes-for-geo.json'), encoding='utf-8'))

# Dubletten aus dem doppelten Lauf: der ERSTE Eintrag je Kennung gewinnt.
# Der erste Lauf hat je Paket gruendlich recherchiert; spaetere Doppelnennungen
# stammen aus Paket-Ueberlaeufen und sind pauschaler. Konkret sichtbar am
# Karhorn-Westgrat: Der Nachzuegler setzte ihn auf den Ostgrat-Einstieg, der
# eigene Westgrat-Einstieg am Westfuss ging dabei verloren.
points = {}
for p in d['geo']:
    points.setdefault(p['id'], p)

# Handkorrektur: Der Westgrat (Panorama-Klettersteig) hat einen eigenen Einstieg am
# Westfuss (OSM-Weg 298900709, westliches Ende). Eine Recherche-Runde setzte ihn auf
# den Ostgrat-Einstieg, weil die alte Kombi-Route dort beginnt — als eigener Steig
# gehoert er aber auf seine eigene Seite, sonst zeigt die Karte zwei Routen als eine.
points['karhorn-westgrat-panorama'] = dict(points.get('karhorn-westgrat-panorama', {}),
    id='karhorn-westgrat-panorama', lat=47.24729, lon=10.14783, confidence='exakt')

# Korrekturen der Pruefung anwenden
for c in (d.get('verdict') or {}).get('corrections', []):
    if c['id'] in points:
        points[c['id']].update(lat=c['lat'], lon=c['lon'])
        points[c['id']]['confidence'] = 'ort'

missing = [r['id'] for r in routes if r['id'] not in points]
if missing:
    print('FEHLEND:', missing); sys.exit(1)

# Plausibilitaet hart pruefen: alles muss im Kasten liegen
bad = [(i, p) for i, p in points.items()
       if not (46.80 <= p['lat'] <= 47.65 and 9.45 <= p['lon'] <= 10.30)]
if bad:
    print('AUSSERHALB:', bad); sys.exit(1)

outline = d['outline']['outline']
landmarks = d['outline']['landmarks']

lats = [p['lat'] for p in points.values()] + [o[0] for o in outline]
lons = [p['lon'] for p in points.values()] + [o[1] for o in outline]
pad = 0.02
bounds = dict(minLat=min(lats)-pad, maxLat=max(lats)+pad, minLon=min(lons)-pad, maxLon=max(lons)+pad)

# --- Kotlin -----------------------------------------------------------------
kt = ['''package at.rudeboy.ferratafit.data

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
''']
kt.append(f"    const val MIN_LAT = {bounds['minLat']:.4f}")
kt.append(f"    const val MAX_LAT = {bounds['maxLat']:.4f}")
kt.append(f"    const val MIN_LON = {bounds['minLon']:.4f}")
kt.append(f"    const val MAX_LON = {bounds['maxLon']:.4f}")
kt.append('')
kt.append('    val points: List<GeoPoint> = listOf(')
for r in routes:
    p = points[r['id']]
    kt.append(f'        GeoPoint("{p["id"]}", {p["lat"]:.5f}, {p["lon"]:.5f}),')
kt.append('    )')
kt.append('')
kt.append('    fun byId(id: String): GeoPoint? = points.firstOrNull { it.id == id }')
kt.append('')
kt.append('    /** Vereinfachte Silhouette Vorarlbergs, im Uhrzeigersinn. */')
kt.append('    val outline: List<Pair<Double, Double>> = listOf(')
for lat, lon in outline:
    kt.append(f'        {lat:.4f} to {lon:.4f},')
kt.append('    )')
kt.append('')
kt.append('    val landmarks: List<Landmark> = listOf(')
for lm in landmarks:
    kt.append(f'        Landmark("{lm["name"]}", {lm["lat"]:.4f}, {lm["lon"]:.4f}),')
kt.append('    )')
kt.append('}')
open('/mnt/c/Users/Rudeboy/Documents/FerrataFit/android/app/src/main/java/at/rudeboy/ferratafit/data/FerrataGeo.kt',
     'w', encoding='utf-8').write('\n'.join(kt) + '\n')

# --- Web --------------------------------------------------------------------
js = ['''// Koordinaten für die Kartenansicht — erzeugt aus derselben Recherche wie
// android/.../data/FerrataGeo.kt, beide Fassungen müssen deckungsgleich sein.
// Der Umriss ist eine vereinfachte Silhouette, keine amtliche Grenze.
''']
js.append(f"export const GEO_BOUNDS = {json.dumps(bounds)};")
js.append('')
js.append('export const GEO_POINTS = ' + json.dumps(
    [{'id': points[r['id']]['id'],
      'lat': round(points[r['id']]['lat'], 5),
      'lon': round(points[r['id']]['lon'], 5)} for r in routes], indent=1) + ';')
js.append('')
js.append('export const GEO_OUTLINE = ' + json.dumps(
    [[round(a, 4), round(b, 4)] for a, b in outline]) + ';')
js.append('')
js.append('export const GEO_LANDMARKS = ' + json.dumps(
    [{'name': l['name'], 'lat': round(l['lat'], 4), 'lon': round(l['lon'], 4)} for l in landmarks],
    ensure_ascii=False) + ';')
open('/mnt/c/Users/Rudeboy/Documents/FerrataFit/web/ferrageo.js', 'w', encoding='utf-8').write('\n'.join(js) + '\n')

conf = {}
for p in points.values(): conf[p.get('confidence','?')] = conf.get(p.get('confidence','?'),0)+1
print(f"{len(points)} Punkte -> FerrataGeo.kt + ferrageo.js | Vertrauen: {conf}")
print(f"Umriss: {len(outline)} Punkte, {len(landmarks)} Orte")
