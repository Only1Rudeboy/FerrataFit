/**
 * Prüft die Progressionslogik der Web-Variante gegen dieselben Fälle wie der
 * Kotlin-Test der Android-App. Beide Varianten müssen identisch rechnen, sonst
 * bekommt man auf Handy und Rechner unterschiedliche Gewichtsvorschläge.
 *
 * Aufruf:  node web/test-progression.mjs
 */

import * as D from './data.js';

let failed = 0;

function check(name, actual, expected) {
  const ok = JSON.stringify(actual) === JSON.stringify(expected);
  if (!ok) failed++;
  console.log(`${ok ? '  ✓' : '  ✗'} ${name}${ok ? '' : `\n      erwartet: ${JSON.stringify(expected)}\n      war:      ${JSON.stringify(actual)}`}`);
}

const DAY = 86400000;
const now = 1_700_000_000_000;
const profile = {
  stations: ['LAT_PULLDOWN', 'CHEST_PRESS', 'BUTTERFLY', 'LEG_EXTENSION', 'LEG_CURL', 'PULLUP_BAR', 'BODYWEIGHT', 'CABLE_LOW', 'DUMBBELL'],
  bodyweightKg: 78, plateStepKg: 5, cycleStart: now, daysPerWeek: 3,
};

const session = (daysAgo, exerciseId, weight, reps, sets = 3) => ({
  id: `s${daysAgo}`, dayId: 'A',
  startedAt: now - daysAgo * DAY, finishedAt: now - daysAgo * DAY + 3600000,
  sets: Array.from({ length: sets }, (_, i) => ({ exerciseId, setIndex: i, weightKg: weight, reps, seconds: 0 })),
});

const timeSession = (daysAgo, exerciseId, seconds, sets = 4) => ({
  id: `t${daysAgo}`, dayId: 'A',
  startedAt: now - daysAgo * DAY, finishedAt: now - daysAgo * DAY + 3600000,
  sets: Array.from({ length: sets }, (_, i) => ({ exerciseId, setIndex: i, weightKg: 0, reps: 0, seconds })),
});

const lat = D.byId('latpull_wide');
const hang = D.byId('deadhang');

console.log('\nProgressionslogik der Web-Variante\n');

// 1
let s = D.suggest(lat, [], profile, now);
check('erste Einheit liefert konservative Startschätzung', [s.advice, s.weightKg], ['START', 35]);

// 2
s = D.suggest(lat, [session(7, 'latpull_wide', 40, 12), session(3, 'latpull_wide', 40, 12)], profile, now);
check('zwei Einheiten am oberen Ende lösen die Steigerung aus', [s.advice, s.weightKg, s.targetReps], ['INCREASE', 45, 8]);

// 3
s = D.suggest(lat, [session(7, 'latpull_wide', 40, 10), session(3, 'latpull_wide', 40, 12)], profile, now);
check('eine einzelne gute Einheit reicht noch nicht', [s.advice, s.weightKg], ['HOLD', 40]);

// 4
s = D.suggest(lat, [session(7, 'latpull_wide', 40, 12), session(3, 'latpull_wide', 45, 12)], profile, now);
check('nach einer Steigerung wird nicht sofort wieder gesteigert', [s.advice, s.weightKg], ['HOLD', 45]);

// 5
s = D.suggest(lat, [
  session(14, 'latpull_wide', 40, 9),
  session(9, 'latpull_wide', 40, 9),
  session(3, 'latpull_wide', 40, 8),
], profile, now);
check('drei Einheiten ohne Fortschritt führen zum Rückschritt', [s.advice, s.weightKg], ['BACKOFF', 35]);

// 6
const deloadProfile = { ...profile, cycleStart: now - 4 * 7 * DAY };
check('Woche 5 wird als Entlastungswoche erkannt', D.weekInCycle(deloadProfile, now), 5);
s = D.suggest(lat, [session(3, 'latpull_wide', 50, 12)], deloadProfile, now);
check('Entlastungswoche reduziert Last und Sätze', [s.advice, s.weightKg, s.sets < lat.sets], ['DELOAD', 45, true]);

// 7
s = D.suggest(hang, [timeSession(7, 'deadhang', 25), timeSession(3, 'deadhang', 25)], profile, now);
check('Dead Hang steigt um fünf Sekunden', [s.advice, s.targetSeconds], ['INCREASE', 30]);

// 8
s = D.suggest(hang, [timeSession(7, 'deadhang', 60), timeSession(3, 'deadhang', 60)], profile, now);
check('Dead Hang weist ab einer Minute auf Zusatzgewicht hin',
  [s.advice, s.reason.includes('Zusatzgewicht')], ['INCREASE', true]);

// 9 — abfallender letzter Satz darf die Steigerung nicht blockieren
const mixed = (daysAgo) => ({
  id: `m${daysAgo}`, dayId: 'A',
  startedAt: now - daysAgo * DAY, finishedAt: now - daysAgo * DAY + 3600000,
  sets: [
    { exerciseId: 'latpull_wide', setIndex: 0, weightKg: 40, reps: 12, seconds: 0 },
    { exerciseId: 'latpull_wide', setIndex: 1, weightKg: 40, reps: 12, seconds: 0 },
    { exerciseId: 'latpull_wide', setIndex: 2, weightKg: 40, reps: 10, seconds: 0 },
  ],
});
s = D.suggest(lat, [mixed(7), mixed(3)], profile, now);
check('abfallender letzter Satz gilt noch als erreicht', [s.advice, s.weightKg], ['INCREASE', 45]);

// 10
check('Wochenzählung läuft im Fünferblock rund', [
  D.weekInCycle(profile, now),
  D.weekInCycle(profile, now + 7 * DAY),
  D.weekInCycle(profile, now + 28 * DAY),
  D.weekInCycle(profile, now + 35 * DAY),
], [1, 2, 5, 1]);

// 11
const ohneBeinhebel = { ...profile, stations: profile.stations.filter((x) => x !== 'LEG_EXTENSION') };
const beine = D.exercisesFor(D.dayById('B'), ohneBeinhebel, []);
check('Plan enthält nur Übungen für vorhandene Stationen', [
  beine.some((e) => e.id === 'leg_ext'),
  beine.some((e) => e.id === 'stepup'),
], [false, true]);

// 12 — der Ersatzmechanismus darf keine Dubletten stehen lassen
const zugTag = D.exercisesFor(D.dayById('A'), profile, []);
check('Ersatzübungen fallen raus, wenn das Original verfügbar ist', [
  zugTag.some((e) => e.id === 'row_cable'),
  zugTag.some((e) => e.id === 'row_lat_narrow'),
], [true, false]);

console.log(`\n${failed === 0 ? '✓ Alle Prüfungen bestanden' : `✗ ${failed} Prüfung(en) fehlgeschlagen`}\n`);
process.exit(failed === 0 ? 0 : 1);
