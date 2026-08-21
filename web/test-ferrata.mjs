/**
 * Prüft die Regeln des Fels-Bereichs und den Gleichlauf mit der Android-App.
 *
 * Zwei Dinge stehen hier auf dem Spiel. Erstens dürfen die Regeln nicht großzügiger
 * werden, als sie sind — eine zu hohe Empfehlung schickt jemanden in eine Wand, aus
 * der er nicht mehr herauskommt. Zweitens müssen Browser und Handy dieselbe Antwort
 * geben; zwei Fassungen, die auseinanderlaufen, sind schlimmer als eine.
 *
 * Aufruf:  node web/test-ferrata.mjs
 */

import { readFileSync } from 'node:fs';
import { fileURLToPath } from 'node:url';
import { dirname, join } from 'node:path';
import * as F from './ferrata.js';
import { FERRATAS, ferrataById, ferrataRegions } from './ferratas.js';

const HERE = dirname(fileURLToPath(import.meta.url));
let failed = 0;

function check(name, actual, expected) {
  const ok = JSON.stringify(actual) === JSON.stringify(expected);
  if (!ok) failed++;
  console.log(`${ok ? '  ✓' : '  ✗'} ${name}${ok ? '' : `\n      erwartet: ${JSON.stringify(expected)}\n      war:      ${JSON.stringify(actual)}`}`);
}
function ok(name, cond, detail = '') {
  if (!cond) failed++;
  console.log(`${cond ? '  ✓' : '  ✗'} ${name}${cond ? '' : `\n      ${detail}`}`);
}

const asc = (grade, feel = 'GUT', extra = {}) => ({
  id: 'a' + Math.random(), date: Date.now() - 86400000, name: 'Test',
  grade, feel, climbMeters: 200, durationMin: 120, turnedBack: false, ...extra,
});

console.log('\nErfahrung und Rang');
check('ohne Begehung ist nichts bestätigt', F.masteredIndex([]), -1);
check('eine saubere B-Begehung reicht nicht', F.masteredIndex([asc('B')]), -1);
check('zwei saubere B-Begehungen bestätigen B', F.masteredIndex([asc('B'), asc('B')]), 1);
check('C/D zählt als D', F.gradeIndex('C/D'), 3);
check('grenzwertig gilt nicht als sauber', F.isClean(asc('C', 'GRENZWERTIG')), false);
check('Umkehren gilt nicht als sauber', F.isClean(asc('C', 'GUT', { turnedBack: true })), false);
check('Umkehren zählt bei den Höhenmetern voll',
  F.ascentMeters([asc('C', 'GUT', { turnedBack: true, climbMeters: 400 })]), 400);

console.log('\nDer Deckel — nie mehr als eine Stufe über dem Bestätigten');
ok('sehr trainiert, nie am Fels: höchstens B',
  F.recommendedIndex([], 100) <= 1, `war ${F.gradeLabel(F.recommendedIndex([], 100))}`);
ok('zweimal B bestätigt, Form auf Anschlag: höchstens C',
  F.recommendedIndex([asc('B'), asc('B')], 100) <= 2,
  `war ${F.gradeLabel(F.recommendedIndex([asc('B'), asc('B')], 100))}`);
ok('erfahren bis D, aber außer Form: Form begrenzt',
  F.recommendedIndex([asc('D'), asc('D')], 30) <= 1,
  `war ${F.gradeLabel(F.recommendedIndex([asc('D'), asc('D')], 30))}`);
ok('letzte C-Begehung war grenzwertig: bleibt bei C',
  F.recommendedIndex([asc('C'), asc('C'), asc('C', 'GRENZWERTIG')], 100) <= 2,
  'eine knappe Begehung muss deckeln');
ok('ein Jahr Pause: eine Stufe zurück',
  F.recommendedIndex([asc('C', 'GUT', { date: Date.now() - 400 * 86400000 }),
                      asc('C', 'GUT', { date: Date.now() - 400 * 86400000 })], 100)
  < F.recommendedIndex([asc('C'), asc('C')], 100),
  'nach langer Pause muss der Vorschlag sinken');

console.log('\nEinordnung der Routen');
const lang = { grade: 'C', climbMeters: 500, totalMin: 400, hasExit: false };
const kurz = { grade: 'C', climbMeters: 80, totalMin: 90, hasExit: false };
const mitAusstieg = { grade: 'C', climbMeters: 400, totalMin: 330, hasExit: true };
const stand = [asc('B'), asc('B')];
ok('eine Stufe höher in langer Wand ohne Ausstieg: zu früh',
  F.fitFor(lang, stand, 100) === F.FIT.ZU_FRUEH, `war ${F.FIT_TITLE[F.fitFor(lang, stand, 100)]}`);
ok('eine Stufe höher, aber kurz: als Ziel erlaubt',
  F.fitFor(kurz, stand, 100) === F.FIT.ZIEL, `war ${F.FIT_TITLE[F.fitFor(kurz, stand, 100)]}`);
ok('eine Stufe höher mit Notausstieg: als Ziel erlaubt',
  F.fitFor(mitAusstieg, stand, 100) === F.FIT.ZIEL,
  `war ${F.FIT_TITLE[F.fitFor(mitAusstieg, stand, 100)]}`);
ok('zwei Stufen höher: nie ein Ziel',
  F.fitFor({ grade: 'D', climbMeters: 50, totalMin: 60, hasExit: true }, stand, 100) === F.FIT.ZU_FRUEH,
  'zwei Stufen darüber darf nie vorgeschlagen werden');

console.log('\nKein Text darf zu einer Tour drängen');
const draengt = /\b(du solltest|trau dich|schaffst du locker|kein problem|ganz einfach)\b/i;
[F.DISCLAIMER, ...F.FIT_LABEL, F.completionLine(asc('C', 'GUT', { turnedBack: true })),
 F.completionLine(asc('C', 'ZU_VIEL')), F.recommendationReason([], 100)]
  .forEach((t, i) => ok(`Text ${i + 1} drängt nicht`, !draengt.test(t), t));
ok('Umkehren wird ausdrücklich nicht als Misserfolg benannt',
  /keine Niederlage/.test(F.completionLine(asc('C', 'GUT', { turnedBack: true }))));

console.log('\nRoutenkatalog');
ok('Katalog ist gefüllt', FERRATAS.length >= 40, `${FERRATAS.length} Steige`);
ok('Kennungen sind eindeutig', new Set(FERRATAS.map((f) => f.id)).size === FERRATAS.length);
ok('Namen sind eindeutig', new Set(FERRATAS.map((f) => f.name)).size === FERRATAS.length);
FERRATAS.forEach((f) => {
  if (f.climbMeters > 700) { failed++; console.log(`  ✗ ${f.name}: ${f.climbMeters} Klettermeter — das ist ein Tagesaufstieg`); }
  if (f.totalMin && f.ferrataMin && f.totalMin < f.ferrataMin) { failed++; console.log(`  ✗ ${f.name}: Gesamtzeit unter Kletterzeit`); }
  if (f.startAlt && f.summitAlt && f.summitAlt <= f.startAlt) { failed++; console.log(`  ✗ ${f.name}: Gipfel nicht über Einstieg`); }
  if (!f.sources || !f.sources.length) { failed++; console.log(`  ✗ ${f.name}: keine Quelle`); }
});
console.log('  ✓ Klettermeter, Zeiten, Höhen und Quellen geprüft');
ok('keine versicherten Wanderwege im Katalog',
  !FERRATAS.some((f) => /zitterklapfen|bocksberg|leiterli/i.test(f.id + f.name)));
ok('byId findet einen Steig', ferrataById(FERRATAS[0].id).name === FERRATAS[0].name);
ok('Gebiete sind abgeleitet', ferrataRegions.length > 5);

console.log('\nTagesskizze');
// Dieselben Werte prüft FerrataTest in Kotlin — die Skizze muss auf beiden
// Plattformen dieselben Anteile zeichnen.
const seg = F.daySegments(90, 120, 90);
ok('Anteile summieren auf 1', Math.abs(seg[0] + seg[1] + seg[2] - 1) < 1e-6);
ok('Steig ist der größte Abschnitt', seg[1] > seg[0] && seg[1] > seg[2]);
ok('ohne Zeiten: Drittel', F.daySegments(0, 0, 0).every((f) => Math.abs(f - 1 / 3) < 1e-6));
ok('Mini-Zustieg bleibt sichtbar', F.daySegments(5, 300, 60)[0] >= 0.1,
  `war ${F.daySegments(5, 300, 60)[0]}`);

console.log('\nKartendaten');
const { GEO_POINTS, GEO_OUTLINE, GEO_BOUNDS, GEO_LANDMARKS } = await import('./ferrageo.js');
ok('jeder Steig hat genau einen Punkt',
  GEO_POINTS.length === FERRATAS.length &&
  FERRATAS.every((r) => GEO_POINTS.some((p) => p.id === r.id)),
  `${GEO_POINTS.length} Punkte, ${FERRATAS.length} Steige`);
ok('alle Punkte liegen im Land',
  GEO_POINTS.every((p) => p.lat > 46.8 && p.lat < 47.65 && p.lon > 9.45 && p.lon < 10.3));
ok('Umriss ist gefüllt', GEO_OUTLINE.length >= 40);
ok('Orientierungsorte sind da', GEO_LANDMARKS.length >= 5);
ok('Grenzen sind stimmig', GEO_BOUNDS.minLat < GEO_BOUNDS.maxLat && GEO_BOUNDS.minLon < GEO_BOUNDS.maxLon);

// Gleichlauf: dieselben Koordinaten wie in der Android-Fassung
const geoKt = readFileSync(join(HERE, '..', 'android', 'app', 'src', 'main', 'java', 'at',
  'rudeboy', 'ferratafit', 'data', 'FerrataGeo.kt'), 'utf8');
const ktPoints = [...geoKt.matchAll(/GeoPoint\("([^"]+)", ([\d.]+), ([\d.]+)\)/g)]
  .reduce((m, x) => ({ ...m, [x[1]]: [parseFloat(x[2]), parseFloat(x[3])] }), {});
const geoAbweichend = GEO_POINTS.filter((p) => {
  const kt = ktPoints[p.id];
  return !kt || Math.abs(kt[0] - p.lat) > 0.0001 || Math.abs(kt[1] - p.lon) > 0.0001;
}).map((p) => p.id);
ok('Web und Android zeigen dieselben Punkte', geoAbweichend.length === 0, geoAbweichend.join(', '));

console.log('\nGleichlauf mit der Android-App');
const kt = readFileSync(join(HERE, '..', 'android', 'app', 'src', 'main', 'java', 'at',
  'rudeboy', 'ferratafit', 'data', 'FerrataRoutes.kt'), 'utf8');
const ktIds = [...kt.matchAll(/^\s+id = "([^"]+)"/gm)].map((m) => m[1]);
check('gleiche Anzahl Steige', FERRATAS.length, ktIds.length);
const nurWeb = FERRATAS.map((f) => f.id).filter((i) => !ktIds.includes(i));
const nurKt = ktIds.filter((i) => !FERRATAS.some((f) => f.id === i));
ok('keine Steige nur im Browser', nurWeb.length === 0, nurWeb.join(', '));
ok('keine Steige nur am Handy', nurKt.length === 0, nurKt.join(', '));

const ktGrades = [...kt.matchAll(/^\s+id = "([^"]+)"[\s\S]*?^\s+grade = "([^"]+)"/gm)]
  .reduce((m, x) => ({ ...m, [x[1]]: x[2] }), {});
const abweichend = FERRATAS.filter((f) => ktGrades[f.id] && ktGrades[f.id] !== f.grade)
  .map((f) => `${f.name}: Web ${f.grade} vs. Android ${ktGrades[f.id]}`);
ok('gleiche Schwierigkeitsgrade', abweichend.length === 0, abweichend.join('\n      '));

console.log('\nBegehung zählt aufs heutige Training');
const HOUR = 3600000;
const heute = 1700000000000;
const stageLog = (id, at) => ({ stageId: id, kind: 'STRENGTH', meters: 100, at });

// Geprüft wird die ECHTE exportierte Funktion — eine lokale Kopie der Regel hätte
// den Fehler nicht gefunden, dass die App sie gar nicht benutzt.
const coversStage = F.coversStage;

ok('ein Tag am Fels deckt die offene Etappe ab', coversStage([], heute));
ok('nach Training am selben Tag zählt sie nicht nochmal',
  !coversStage([stageLog('S1', heute - 2 * HOUR)], heute));
ok('zweite Begehung am selben Tag schiebt nichts weiter',
  !coversStage([{ stageId: 'S1', kind: 'FERRATA', meters: 400, at: heute - 4 * HOUR }], heute));
ok('Einträge außerhalb des Zyklus blockieren nicht',
  coversStage([{ stageId: F.EXTRA_STAGE_ID, kind: 'FERRATA', meters: 300, at: heute - 5 * HOUR }], heute));
ok('gestern zählt nicht für heute', coversStage([stageLog('S1', heute - 26 * HOUR)], heute));

console.log('\nBelastung und Erholung');
// Dieselben Faelle stehen wortgleich in RecoveryTest.kt — wer hier Zahlen aendert,
// muss sie dort mitaendern, sonst rechnen Browser und Handy verschieden.
const tourB = (g, m, min, feel = 'GUT', hr = 0) =>
  ({ grade: g, climbMeters: m, durationMin: min, feel, avgHr: hr, date: 0, name: 'T' });

check('Kellenegg-Klasse bleibt unter 20', F.tourLoad(tourB('B/C', 60, 75)) < 20, true);
check('Saulakopf-Klasse liegt ueber 60', F.tourLoad(tourB('D', 380, 330)) >= 60, true);
ok('Schwierigkeit verteuert',
  F.tourLoad(tourB('D', 300, 300)) > F.tourLoad(tourB('B', 300, 300)) * 1.4);
ok('grenzwertig war teurer als locker',
  F.tourLoad(tourB('C', 200, 240, 'GRENZWERTIG')) > F.tourLoad(tourB('C', 200, 240, 'LOCKER')) * 1.4);
ok('hoher Puls hebt, ruhiger senkt',
  F.tourLoad(tourB('C', 200, 240, 'GUT', 165)) > F.tourLoad(tourB('C', 200, 240)) &&
  F.tourLoad(tourB('C', 200, 240, 'GUT', 95)) < F.tourLoad(tourB('C', 200, 240)));
check('Puls ist gedeckelt',
  F.tourLoad(tourB('C', 200, 240, 'GUT', 250)), F.tourLoad(tourB('C', 200, 240, 'GUT', 170)));

// Exakte Werte — der eigentliche Gleichlauf-Anker zwischen beiden Fassungen
check('Eichpunkt Uebungssteig', F.tourLoad(tourB('B/C', 60, 75)), 9);
check('Eichpunkt Bergtag', F.tourLoad(tourB('D', 380, 330)), 63);
check('Eichpunkt Grenze', F.tourLoad(tourB('D', 450, 390, 'GRENZWERTIG')), 100);

const at = 1000000000000;
const gross = { ...tourB('D', 380, 330), date: at, name: 'Saulakopf' };
check('12 h nach dem Bergtag: Erholung',
  F.recoveryState([gross], at + 12 * HOUR)?.level.id, 'ERHOLUNG');
check('30 h danach: angeschlagen',
  F.recoveryState([gross], at + 30 * HOUR)?.level.id, 'ANGESCHLAGEN');
check('50 h danach: frei', F.recoveryState([gross], at + 50 * HOUR), null);
check('kleine Tour oeffnet kein Fenster',
  F.recoveryState([{ ...tourB('B', 60, 75), date: at }], at + 2 * HOUR), null);

console.log('\nDie App schreibt nichts nach Health Connect');
const manifest = readFileSync(join(HERE, '..', 'android', 'app', 'src', 'main', 'AndroidManifest.xml'), 'utf8');
ok('keine Schreibrechte im Manifest', !/permission\.health\.WRITE_/.test(manifest),
  (manifest.match(/permission\.health\.WRITE_\w+/g) || []).join(', '));
// Kommentare ausblenden — sonst schlägt der eigene Hinweis „kein getWritePermission" an
const ohneKommentare = (t) => t.replace(/\/\*[\s\S]*?\*\//g, '').replace(/\/\/.*$/gm, '');
const bridge = ohneKommentare(readFileSync(join(HERE, '..', 'android', 'app', 'src', 'main', 'java', 'at',
  'rudeboy', 'ferratafit', 'health', 'HealthBridge.kt'), 'utf8'));
ok('kein Schreibrecht in der Berechtigungsmenge', !/getWritePermission/.test(bridge));
ok('kein Schreibaufruf in der Brücke', !/insertRecords/.test(bridge));
const vm = ohneKommentare(readFileSync(join(HERE, '..', 'android', 'app', 'src', 'main', 'java', 'at',
  'rudeboy', 'ferratafit', 'AppViewModel.kt'), 'utf8'));
ok('kein Übertragen von Einheiten', !/writeSession|syncAllToHealth|pushToHealth/.test(vm));

console.log(failed === 0 ? '\nAlles grün.\n' : `\n${failed} Prüfung(en) fehlgeschlagen.\n`);
process.exit(failed === 0 ? 0 : 1);
