/**
 * Prüft das Einlesen von Waagen-Dateien.
 *
 * Dieselben Fälle wie `BodyImportTest.kt` — laufen die beiden Fassungen auseinander,
 * verstünde dieselbe Datei auf Handy und im Browser etwas anderes.
 *
 * Aufruf:  node web/test-import.mjs
 */

import { parseBodyFile, mergeBody } from './bodyimport.js';

let failed = 0;

function check(name, actual, expected) {
  const ok = JSON.stringify(actual) === JSON.stringify(expected);
  if (!ok) failed++;
  console.log(`${ok ? '  ✓' : '  ✗'} ${name}${ok ? '' : `\n      erwartet: ${JSON.stringify(expected)}\n      war:      ${JSON.stringify(actual)}`}`);
}

const now = 1_700_000_000_000;
const DAY = 86400000;

console.log('\nEinlesen von Waagen-Dateien\n');

// --- Trennzeichen und Dezimaltrenner ---
check('einfache Tabelle mit Komma', (() => {
  const r = parseBodyFile('Datum,Gewicht,Körperfett\n2026-08-01 07:30,78.4,20.1\n2026-08-08 07:15,77.9,19.8', now);
  return [r.error, r.measurements.length, r.measurements[0].weightKg, r.measurements[1].bodyFatPercent];
})(), [null, 2, 78.4, 19.8]);

check('Semikolon und Komma als Dezimaltrenner', (() => {
  const r = parseBodyFile('Datum;Gewicht;Fett\n01.08.2026;78,4;20,1\n08.08.2026;77,9;19,8', now);
  return [r.error, r.measurements.length, r.measurements[0].weightKg];
})(), [null, 2, 78.4]);

check('Tabulator wird erkannt',
  parseBodyFile('Date\tWeight\n2026-08-01\t78.4\n2026-08-08\t77.9', now).measurements.length, 2);

// --- Spaltennamen ---
check('englische Spaltennamen', (() => {
  const m = parseBodyFile(
    'Date,Weight (kg),Body Fat,Muscle,Water,Bone,BMR\n2026-08-01,78.4,20.1,58.0,45.0,3.2,1750', now
  ).measurements[0];
  return [m.weightKg, m.bodyFatPercent, m.leanMassKg, m.waterMassKg, m.boneMassKg, m.basalKcal];
})(), [78.4, 20.1, 58, 45, 3.2, 1750]);

check('Einheiten in der Zelle stoeren nicht',
  parseBodyFile('Datum,Gewicht\n2026-08-01,"78.4 kg"', now).measurements[0].weightKg, 78.4);

// --- Plausibilität ---
check('unsinnige Gewichte werden verworfen', (() => {
  const r = parseBodyFile(
    'Datum,Gewicht\n2026-08-01,78.4\n2026-08-02,3.5\n2026-08-03,999\n2026-08-04,\n2026-08-05,abc', now
  );
  return [r.measurements.length, r.skipped];
})(), [1, 4]);

check('unplausible Nebenwerte werden einzeln verworfen', (() => {
  const m = parseBodyFile('Datum,Gewicht,Fett\n2026-08-01,78.4,95', now).measurements[0];
  return [m.weightKg, m.bodyFatPercent];
})(), [78.4, null]);

// --- Fehlermeldungen ---
check('ohne Gewichtsspalte kommt eine verstaendliche Meldung', (() => {
  const r = parseBodyFile('Datum,Schritte\n2026-08-01,8000', now);
  return [r.error !== null, r.error.includes('Gewichtsspalte'), r.error.includes('schritte')];
})(), [true, true, true]);

check('leere Datei fuehrt nicht zum Absturz', [
  parseBodyFile('', now).error !== null,
  parseBodyFile('nur eine Zeile', now).error !== null,
], [true, true]);

// --- Datum ---
check('ohne Datumsspalte gilt der aktuelle Zeitpunkt',
  parseBodyFile('Gewicht\n78.4', now).measurements[0].at, now);

check('Zeitstempel in Sekunden und Millisekunden', [
  parseBodyFile('Datum,Gewicht\n1700000000,78.4', now).measurements[0].at,
  parseBodyFile('Datum,Gewicht\n1700000000000,78.4', now).measurements[0].at,
], [1_700_000_000_000, 1_700_000_000_000]);

// --- Zusammenfassen ---
check('mehrere Waegungen am selben Tag werden zusammengefasst', (() => {
  const r = parseBodyFile(
    'Datum,Gewicht\n2026-08-01 07:00,78.8\n2026-08-01 19:30,78.2\n2026-08-02 07:00,78.0', now
  );
  return [r.measurements.length, r.measurements[0].weightKg];
})(), [2, 78.2]);

check('Zusammenfuehren erhaelt vorhandene Messungen', (() => {
  const vorhanden = [
    { at: now - 10 * DAY, weightKg: 79.0 },
    { at: now - 5 * DAY, weightKg: 78.5 },
  ];
  const neu = [
    { at: now - 5 * DAY + 3600000, weightKg: 78.3 },  // selber Tag, später
    { at: now - 1 * DAY, weightKg: 78.0 },            // neuer Tag
  ];
  const m = mergeBody(vorhanden, neu);
  return [m.length, m[0].weightKg, m[1].weightKg, m[2].weightKg];
})(), [3, 79.0, 78.3, 78.0]);

check('Zusammenfuehren sortiert nach Zeit', (() => {
  const m = mergeBody(
    [{ at: now, weightKg: 78.0 }],
    [{ at: now - 20 * DAY, weightKg: 80.0 }]
  );
  return m[0].at < m[1].at;
})(), true);

console.log(`\n${failed === 0 ? '✓ Alle Prüfungen bestanden' : `✗ ${failed} Prüfung(en) fehlgeschlagen`}\n`);
process.exit(failed === 0 ? 0 : 1);
