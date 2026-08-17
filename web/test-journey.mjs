/**
 * Prüft das Etappensystem: Freischaltung, Höhenmeter, Gipfel und Abzeichen.
 *
 * Aufruf:  node web/test-journey.mjs
 */

import * as J from './journey.js';
import { EXERCISES } from './exercises.js';

let failed = 0;

function check(name, actual, expected) {
  const ok = JSON.stringify(actual) === JSON.stringify(expected);
  if (!ok) failed++;
  console.log(`${ok ? '  ✓' : '  ✗'} ${name}${ok ? '' : `\n      erwartet: ${JSON.stringify(expected)}\n      war:      ${JSON.stringify(actual)}`}`);
}

/** Baut einen Fortschritt aus n abgeschlossenen Etappen. */
const walk = (n, opts = {}) =>
  Array.from({ length: n }, (_, i) => {
    const s = J.stageAt(i);
    const skipped = (opts.skip || []).includes(i);
    return { stageId: s.id, kind: s.kind, meters: skipped ? 0 : s.meters, at: 0, skipped, detail: '' };
  });

console.log('\nEtappensystem\n');

// --- Reihenfolge und Freischaltung ---
check('ohne Fortschritt steht Etappe 1 an', J.currentStage([]).id, 'S1');
check('nach einer Etappe steht die zweite an', J.currentStage(walk(1)).id, 'S2');
check('nach sieben Etappen beginnt der Zyklus von vorne', J.currentStage(walk(7)).id, 'S1');
check('Zyklusnummer zaehlt ab eins', [
  J.cycleNumber([]), J.cycleNumber(walk(6)), J.cycleNumber(walk(7)), J.cycleNumber(walk(14)),
], [1, 1, 2, 3]);

// --- Aufbau des Zyklus ---
const kinds = J.STAGES.map((s) => s.kind);
check('sieben Etappen im Zyklus', J.STAGES.length, 7);
check('drei davon sind Krafteinheiten', kinds.filter((k) => k === J.STAGE_KIND.STRENGTH).length, 3);
check('zwischen zwei Krafteinheiten liegt immer eine leichtere', (() => {
  for (let i = 0; i < kinds.length; i++) {
    const a = kinds[i], b = kinds[(i + 1) % kinds.length];
    if (a === J.STAGE_KIND.STRENGTH && b === J.STAGE_KIND.STRENGTH) return false;
  }
  return true;
})(), true);
check('jede Krafteinheit zeigt auf einen Trainingstag', (() => {
  return J.STAGES.filter((s) => s.kind === J.STAGE_KIND.STRENGTH)
    .every((s) => J.dayForStage(s) !== null && J.dayForStage(s) !== undefined);
})(), true);
check('jede Dehn-Etappe hat Uebungen hinterlegt', (() => {
  return J.STAGES
    .filter((s) => s.kind === J.STAGE_KIND.MOBILITY || s.kind === J.STAGE_KIND.RECOVERY)
    .every((s) => Array.isArray(s.mobilityIds) && s.mobilityIds.length >= 4
      && s.mobilityIds.every((id) => J.mobilityById(id)));
})(), true);

// --- Höhenmeter ---
const fullCycle = J.STAGES.reduce((n, s) => n + s.meters, 0);
check('ein vollstaendiger Zyklus ergibt die Summe aller Etappen', J.totalMeters(walk(7)), fullCycle);
check('uebersprungene Etappen zaehlen nicht', J.totalMeters(walk(7, { skip: [0, 2] })),
  fullCycle - J.STAGES[0].meters - J.STAGES[2].meters);
check('ohne Fortschritt null Hoehenmeter', J.totalMeters([]), 0);

// --- Gipfel ---
const sp0 = J.summitProgress(0);
check('am Anfang ist der erste Gipfel das Ziel', [sp0.next.m, sp0.reached.length], [J.SUMMITS[0].m, 0]);
const sp = J.summitProgress(1000);
check('bei 1000 Hm sind die ersten beiden Gipfel erreicht', sp.reached.length, 2);
check('Restweg wird korrekt gerechnet', sp.toGo, sp.next.m - 1000);
check('Gipfel sind aufsteigend sortiert',
  J.SUMMITS.every((s, i) => i === 0 || s.m > J.SUMMITS[i - 1].m), true);

// --- Abzeichen ---
const emptySnap = {
  progress: [], sessions: [], meters: 0, weeklyStreak: 0, increases: 0,
  best: { deadhang: 0, pullup: 0, plank: 0 },
};
check('ohne Fortschritt kein Abzeichen', J.earnedBadges(emptySnap).length, 0);

const oneStage = { ...emptySnap, progress: walk(1), meters: 120 };
check('erste Etappe vergibt "Losgegangen"',
  J.earnedBadges(oneStage).map((b) => b.id), ['first_stage']);

const strong = {
  ...emptySnap,
  progress: walk(14),
  meters: 3000,
  weeklyStreak: 5,
  increases: 3,
  best: { deadhang: 65, pullup: 6, plank: 90 },
};
const ids = J.earnedBadges(strong).map((b) => b.id);
check('fortgeschrittener Stand vergibt die passenden Abzeichen', [
  ids.includes('hang30'), ids.includes('hang60'), ids.includes('pullup1'),
  ids.includes('pullup5'), ids.includes('first_week'), ids.includes('streak4'),
  ids.includes('summit1'), ids.includes('first_increase'), ids.includes('no_skip_cycle'),
], [true, true, true, true, true, true, true, true, true]);

const skipped = { ...strong, progress: walk(14, { skip: [13] }) };
check('"Lueckenlos" faellt weg, sobald im letzten Zyklus etwas fehlt',
  J.earnedBadges(skipped).map((b) => b.id).includes('no_skip_cycle'), false);

check('jedes Abzeichen hat Symbol, Name und Beschreibung',
  J.BADGES.every((b) => b.icon && b.name && b.desc && typeof b.check === 'function'), true);
check('Abzeichen-Kennungen sind eindeutig',
  new Set(J.BADGES.map((b) => b.id)).size, J.BADGES.length);

// --- Sprüche ---
check('Spruch bleibt ueber den Tag stabil', (() => {
  const t = 1_700_000_000_000;
  return J.quoteOfDay(t).text === J.quoteOfDay(t + 3600_000).text;
})(), true);
check('Spruch wechselt am naechsten Tag', (() => {
  const t = 1_700_000_000_000;
  return J.quoteOfDay(t).text !== J.quoteOfDay(t + 86400_000).text;
})(), true);
check('jede Etappenart hat einen Abschlusssatz',
  Object.values(J.STAGE_KIND).every((k) => typeof J.completionLine(k) === 'string'
    && J.completionLine(k).length > 10), true);

// --- Dehnkatalog ---
check('Dehnuebungen sind vollstaendig beschrieben',
  J.MOBILITY.every((m) => m.id && m.name && m.zone && m.cue && m.why
    && m.seconds >= 20 && m.seconds <= 90), true);
check('Dehn-Kennungen sind eindeutig',
  new Set(J.MOBILITY.map((m) => m.id)).size, J.MOBILITY.length);
check('Unterarme sind abgedeckt — die wichtigste Zone am Steig',
  J.MOBILITY.some((m) => m.zone === 'Unterarme'), true);

// --- Ausführungsanleitungen ---

check('jede Übung hat Aufbau, Ablauf und Videosuche', (() => {
  const bad = EXERCISES.filter((e) => !e.setup || !Array.isArray(e.steps) || e.steps.length < 3 || !e.video);
  return bad.length ? bad.map((e) => e.id) : [];
})(), []);

check('jede Übung nennt typische Fehler', (() => {
  const bad = EXERCISES.filter((e) => !Array.isArray(e.mistakes) || e.mistakes.length < 1);
  return bad.length ? bad.map((e) => e.id) : [];
})(), []);

check('jede Übung erklärt den Steig-Bezug', (() => {
  const bad = EXERCISES.filter((e) => !e.why || e.why.length < 30);
  return bad.length ? bad.map((e) => e.id) : [];
})(), []);

check('einseitige Übungen erklären die Zählweise', (() => {
  // Bei Step-up, Ausfallschritt und Seitstütz ist unklar, ob pro Seite oder gesamt gezählt wird
  const needsCounting = ['stepup', 'split_squat', 'side_plank'];
  const bad = needsCounting.filter((id) => {
    const e = EXERCISES.find((x) => x.id === id);
    return !e || !e.counting;
  });
  return bad;
})(), []);

check('jede Dehnübung hat Ablauf und Videosuche', (() => {
  const bad = J.MOBILITY.filter((m) => !Array.isArray(m.steps) || m.steps.length < 3 || !m.video);
  return bad.length ? bad.map((m) => m.id) : [];
})(), []);

check('Videosuchen sind nicht leer und nennen die Übung', (() => {
  const all = [...EXERCISES, ...J.MOBILITY];
  return all.filter((e) => !e.video || e.video.trim().length < 8).map((e) => e.id);
})(), []);

// --- Körperdaten ---
const DAY = 86400000;
const nowB = 1_700_000_000_000;
const bm = (daysAgo, kg, fat) => ({ at: nowB - daysAgo * DAY, weightKg: kg, bodyFatPercent: fat });

check('neueste Messung wird gefunden',
  J.Body.latest([bm(30, 80), bm(1, 78), bm(15, 79)]).weightKg, 78);

check('ohne Messungen gibt es nichts zu melden',
  [J.Body.latest([]), J.Body.weightTrend([], nowB)], [null, null]);

check('Trend vergleicht mit der Messung vor einem Monat',
  J.Body.weightTrend([bm(35, 80), bm(20, 79), bm(1, 77.5)], nowB, 30), -2.5);

check('eine einzelne Messung ergibt keinen Trend',
  J.Body.weightTrend([bm(1, 78)], nowB), null);

check('Last je Klimmzug sinkt mit dem Koerpergewicht',
  [J.Body.pullupLoadPerRep(80, 5), J.Body.pullupLoadPerRep(80, 0)], [16, null]);

check('Gewichtsverlust wird in Wiederholungen umgerechnet',
  J.Body.pullupEquivalent(-3), 1);

check('BMI braucht eine plausible Groesse',
  [J.Body.bmi(78, 179.5), J.Body.bmi(78, null), J.Body.bmi(78, 40)], [24.2, null, null]);

check('frische Messungen werden von alten unterschieden',
  [J.Body.isFresh(nowB - 5 * DAY, nowB), J.Body.isFresh(nowB - 45 * DAY, nowB)], [true, false]);

check('Koerperfett wird ohne Zielvorgabe eingeordnet', (() => {
  const labels = [5, 12, 18, 24, 30].map((x) => J.Body.bodyFatLabel(x));
  return labels.filter((l) => /sollte|Ziel|abnehmen/i.test(l));
})(), []);

check('Android und Web rechnen gleich', (() => {
  // Dieselben Faelle wie in BodyTest.kt — Abweichungen faenden hier auf
  const list = [bm(35, 80), bm(1, 77.5)];
  return [
    J.Body.weightTrend(list, nowB, 30),
    J.Body.pullupLoadPerRep(80, 5),
    J.Body.bmi(78, 179.5),
    J.Body.pullupEquivalent(-3),
  ];
})(), [-2.5, 16, 24.2, 1]);

console.log(`\n${failed === 0 ? '✓ Alle Prüfungen bestanden' : `✗ ${failed} Prüfung(en) fehlgeschlagen`}\n`);
process.exit(failed === 0 ? 0 : 1);
