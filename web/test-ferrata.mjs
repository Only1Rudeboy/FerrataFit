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

console.log(failed === 0 ? '\nAlles grün.\n' : `\n${failed} Prüfung(en) fehlgeschlagen.\n`);
process.exit(failed === 0 ? 0 : 1);
