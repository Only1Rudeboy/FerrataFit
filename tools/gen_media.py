# -*- coding: utf-8 -*-
"""Erzeugt FerrataMedia.kt und ferramedia.js: Commons-Fotos und Topo-Abschnitte je Steig.

Die Fotos hat eine Recherche vorgeschlagen; ob sie existieren und wie sie lizenziert
sind, entscheidet hier die Commons-API -- nicht die Recherche. Nur frei lizenzierte
Dateien (CC0, CC BY, CC BY-SA, Public Domain, FAL) kommen durch. Jeder Eintrag traegt
Urheber und Lizenz, denn genau das verlangen diese Lizenzen.

Eingaben im selben Ordner: photos-chunk0..3.json, topo-chunk0..3.json
(Abschriften der Recherche-Ergebnisse).
"""
import json, os, re, sys, html, time, urllib.request, urllib.parse

SP = os.path.dirname(os.path.abspath(__file__))
UA = "FerrataFit-Generator/1.0 (https://github.com/Only1Rudeboy/FerrataFit; Datenpflege)"

# --- Eingaben einlesen -------------------------------------------------------
photos, topos = {}, {}
for i in range(4):
    p = os.path.join(SP, f'photos-chunk{i}.json')
    if os.path.exists(p):
        for r in json.load(open(p, encoding='utf-8'))['routes']:
            photos[r['id']] = r
    t = os.path.join(SP, f'topo-chunk{i}.json')
    if os.path.exists(t):
        for r in json.load(open(t, encoding='utf-8'))['routes']:
            topos[r['id']] = r

routes = [r['id'] for r in json.load(open(os.path.join(SP, 'routes-for-geo.json'), encoding='utf-8'))] \
    if os.path.exists(os.path.join(SP, 'routes-for-geo.json')) else sorted(set(photos) | set(topos))
print(f'{len(photos)} Foto-Listen, {len(topos)} Topos, {len(routes)} Routen')

# --- Commons-API: Existenz, Lizenz, Urheber, Vorschau-URL --------------------
OK_LICENSES = re.compile(r'^(cc0|cc[ -]by(-sa)?[ -]?[0-9.]*( [a-z]{2})?|public domain|pd|fal|copyrighted free use)', re.I)

def api(titles):
    q = urllib.parse.urlencode({
        'action': 'query', 'format': 'json', 'prop': 'imageinfo',
        'iiprop': 'url|extmetadata', 'iiurlwidth': '1200',
        'iiextmetadatafilter': 'LicenseShortName|Artist|ImageDescription',
        'titles': '|'.join(titles),
    })
    req = urllib.request.Request('https://commons.wikimedia.org/w/api.php?' + q, headers={'User-Agent': UA})
    for attempt in range(3):
        try:
            with urllib.request.urlopen(req, timeout=30) as r:
                return json.load(r)
        except Exception as e:
            print('  API-Fehler, neuer Versuch:', e); time.sleep(2)
    return {'query': {'pages': {}}}

def strip_html(s):
    return html.unescape(re.sub(r'<[^>]+>', '', s or '')).strip()

all_files = sorted({f['file'] for r in photos.values() for f in r.get('files', [])})
verified = {}
for i in range(0, len(all_files), 40):
    batch = all_files[i:i + 40]
    data = api(batch)
    norm = {n['from']: n['to'] for n in data.get('query', {}).get('normalized', [])}
    by_title = {}
    for page in data.get('query', {}).get('pages', {}).values():
        by_title[page.get('title')] = page
    for f in batch:
        page = by_title.get(norm.get(f, f)) or by_title.get(f)
        if not page or 'missing' in page or not page.get('imageinfo'):
            print('  FEHLT auf Commons:', f); continue
        info = page['imageinfo'][0]
        meta = info.get('extmetadata', {})
        lic = strip_html(meta.get('LicenseShortName', {}).get('value', ''))
        artist = strip_html(meta.get('Artist', {}).get('value', ''))
        if not OK_LICENSES.match(lic):
            print(f'  LIZENZ NICHT FREI ({lic!r}):', f); continue
        verified[f] = dict(
            url=info.get('thumburl') or info.get('url'),
            pageUrl=info.get('descriptionurl'),
            license=lic,
            author=artist[:60] or 'unbekannt',
        )
print(f'{len(verified)} von {len(all_files)} Dateien bestaetigt')

# --- Zusammenfuehren ---------------------------------------------------------
out_photos, out_galleries, out_topos, out_topo_urls = {}, {}, {}, {}
for rid in routes:
    r = photos.get(rid, {})
    lst = []
    seen = set()
    for f in r.get('files', []):
        v = verified.get(f['file'])
        if not v or f['file'] in seen:
            continue
        seen.add(f['file'])
        lst.append(dict(file=f['file'], url=v['url'], pageUrl=v['pageUrl'],
                        shows=f.get('shows', '')[:140], author=v['author'], license=v['license']))
    out_photos[rid] = lst
    if r.get('galleryUrl'):
        out_galleries[rid] = r['galleryUrl']
    t = topos.get(rid, {})
    segs = []
    for s in t.get('segments', []):
        segs.append(dict(label=s['label'][:40], grade=s.get('grade', 'B'), kind=s.get('kind', 'wall'),
                         meters=int(s.get('meters', 0) or 0), crux=bool(s.get('crux', False))))
    out_topos[rid] = segs
    if t.get('topoUrl'):
        out_topo_urls[rid] = t['topoUrl']

ohne_foto = [r for r in routes if not out_photos.get(r)]
ohne_topo = [r for r in routes if len(out_topos.get(r, [])) < 3]
print('Ohne freies Foto:', ohne_foto)
print('Ohne Topo (<3 Abschnitte):', ohne_topo)

def kts(s):
    s = str(s).replace('\\', '\\\\').replace('"', '\\"').replace('$', '\\$').replace('\n', ' ')
    return '"' + s + '"'

# --- Kotlin -----------------------------------------------------------------
kt = ['''package at.rudeboy.ferratafit.data

/**
 * Fotos und Topo-Abschnitte je Steig.
 *
 * Erzeugt von tools/gen_media.py — nicht von Hand bearbeiten. Jedes Foto hat die
 * Commons-API auf Existenz und freie Lizenz geprüft; Urheber und Lizenz stehen dabei,
 * weil die Lizenzen das verlangen. Die Topo-Abschnitte sind aus den Tourenbeschreibungen
 * abgeleitete Fakten (Reihenfolge, Art, Grad) — die Zeichnung macht die App selbst.
 */
object FerrataMedia {

    val photos: Map<String, List<WebPhoto>> = mapOf(''']
for rid in routes:
    lst = out_photos.get(rid, [])
    if not lst:
        continue
    kt.append(f'        {kts(rid)} to listOf(')
    for p in lst:
        kt.append(f'            WebPhoto({kts(p["file"])}, {kts(p["url"])}, {kts(p["pageUrl"])}, {kts(p["shows"])}, {kts(p["author"])}, {kts(p["license"])}),')
    kt.append('        ),')
kt.append('    )')
kt.append('')
kt.append('    val galleries: Map<String, String> = mapOf(')
for rid, u in out_galleries.items():
    kt.append(f'        {kts(rid)} to {kts(u)},')
kt.append('    )')
kt.append('')
kt.append('    val topos: Map<String, List<TopoSegment>> = mapOf(')
for rid in routes:
    segs = out_topos.get(rid, [])
    if not segs:
        continue
    kt.append(f'        {kts(rid)} to listOf(')
    for s in segs:
        extra = ''
        if s['meters']: extra += f', meters = {s["meters"]}'
        if s['crux']: extra += ', crux = true'
        kt.append(f'            TopoSegment({kts(s["label"])}, {kts(s["grade"])}, {kts(s["kind"])}{extra}),')
    kt.append('        ),')
kt.append('    )')
kt.append('')
kt.append('    val topoUrls: Map<String, String> = mapOf(')
for rid, u in out_topo_urls.items():
    kt.append(f'        {kts(rid)} to {kts(u)},')
kt.append('    )')
kt.append('')
kt.append('    fun photosFor(id: String): List<WebPhoto> = photos[id].orEmpty()')
kt.append('    fun topoFor(id: String): List<TopoSegment> = topos[id].orEmpty()')
kt.append('}')
open('/mnt/c/Users/Rudeboy/Documents/FerrataFit/android/app/src/main/java/at/rudeboy/ferratafit/data/FerrataMedia.kt',
     'w', encoding='utf-8').write('\n'.join(kt) + '\n')

# --- Web --------------------------------------------------------------------
js = '''// Fotos und Topo-Abschnitte je Steig — erzeugt von tools/gen_media.py, nicht von Hand
// bearbeiten. Jedes Foto hat die Commons-API auf Existenz und freie Lizenz geprüft;
// Urheber und Lizenz stehen dabei, weil die Lizenzen das verlangen. Gleichlauf mit
// android/.../data/FerrataMedia.kt.

export const WEB_PHOTOS = ''' + json.dumps({k: v for k, v in out_photos.items() if v}, ensure_ascii=False, indent=1) + ''';

export const GALLERIES = ''' + json.dumps(out_galleries, ensure_ascii=False, indent=1) + ''';

export const TOPOS = ''' + json.dumps({k: v for k, v in out_topos.items() if v}, ensure_ascii=False, indent=1) + ''';

export const TOPO_URLS = ''' + json.dumps(out_topo_urls, ensure_ascii=False, indent=1) + ''';

export const photosFor = (id) => WEB_PHOTOS[id] || [];
export const topoFor = (id) => TOPOS[id] || [];
'''
open('/mnt/c/Users/Rudeboy/Documents/FerrataFit/web/ferramedia.js', 'w', encoding='utf-8').write(js)
print(f'{sum(len(v) for v in out_photos.values())} Fotos, {sum(len(v) for v in out_topos.values())} Abschnitte -> FerrataMedia.kt + ferramedia.js')
