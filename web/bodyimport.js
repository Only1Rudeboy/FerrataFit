/**
 * Einlesen von Waagendaten aus einer geteilten Datei.
 *
 * Wortgleich zur Android-Fassung (android/…/data/BodyImport.kt) — beide müssen dieselbe
 * Datei gleich verstehen, sonst steht der Bestand auf einem der Geräte anders drin.
 *
 * Hintergrund: Ein direkter Zugriff auf die FitDays-App ist nicht möglich, Android kapselt
 * Apps gegeneinander ab. Klappt der Weg über Health Connect nicht, bleibt der Export —
 * und weil jede Waagen-App ein eigenes Format schreibt, ist dieser Leser bewusst großzügig.
 */

/** Spaltennamen, die je Wert in Frage kommen — in mehreren Sprachen. */
const WEIGHT = ['gewicht', 'weight', 'masse', 'kg'];
const DATE = ['datum', 'date', 'zeit', 'time', 'gemessen', 'measured'];
const FAT = ['körperfett', 'koerperfett', 'fett', 'body fat', 'bodyfat', 'fat'];
const LEAN = ['magermasse', 'muskel', 'muscle', 'lean'];
const WATER = ['wasser', 'water'];
const BONE = ['knochen', 'bone'];
const BASAL = ['grundumsatz', 'bmr', 'basal', 'kcal'];

const DAY_MS = 86400000;

/** Erkennt das Trennzeichen an der Kopfzeile. */
function detectSeparator(header) {
  return [';', '\t', ','].reduce((best, sep) => {
    const count = header.split(sep).length - 1;
    const bestCount = header.split(best).length - 1;
    return count > bestCount ? sep : best;
  }, ';');
}

const splitLine = (line, sep) => line.split(sep).map((c) => c.trim().replace(/^"|"$/g, ''));

const indexOfAny = (header, needles) =>
  header.findIndex((cell) => needles.some((n) => cell.includes(n)));

/** Zahl aus einer Zelle — akzeptiert Punkt wie Komma und ignoriert Einheiten. */
function toNumber(cell) {
  if (cell == null || cell === '') return null;
  const cleaned = String(cell).replace(',', '.').replace(/[^\d.-]/g, '');
  const n = parseFloat(cleaned);
  return Number.isFinite(n) ? n : null;
}

/** Zeitpunkt aus einer Zelle — mehrere Schreibweisen und reine Zeitstempel. */
function toDate(cell) {
  if (!cell) return null;
  const raw = String(cell).trim();

  // Reiner Zeitstempel in Sekunden oder Millisekunden
  if (/^\d+$/.test(raw)) {
    const n = parseInt(raw, 10);
    return n > 100000000000 ? n : n * 1000;
  }

  // 01.08.2026 oder 01.08.2026 07:30
  let m = raw.match(/^(\d{1,2})\.(\d{1,2})\.(\d{4})(?:[ T](\d{1,2}):(\d{2})(?::(\d{2}))?)?$/);
  if (m) return new Date(+m[3], +m[2] - 1, +m[1], +(m[4] || 0), +(m[5] || 0), +(m[6] || 0)).getTime();

  // 2026-08-01 oder 2026-08-01T07:30:00
  m = raw.match(/^(\d{4})-(\d{1,2})-(\d{1,2})(?:[ T](\d{1,2}):(\d{2})(?::(\d{2}))?)?/);
  if (m) return new Date(+m[1], +m[2] - 1, +m[3], +(m[4] || 0), +(m[5] || 0), +(m[6] || 0)).getTime();

  // 08/01/2026 — im Zweifel Monat zuerst, wie in den meisten Exporten
  m = raw.match(/^(\d{1,2})\/(\d{1,2})\/(\d{4})(?:[ T](\d{1,2}):(\d{2}))?$/);
  if (m) return new Date(+m[3], +m[1] - 1, +m[2], +(m[4] || 0), +(m[5] || 0)).getTime();

  const parsed = Date.parse(raw);
  return Number.isFinite(parsed) ? parsed : null;
}

/**
 * Liest den Inhalt einer geteilten Datei.
 *
 * Erwartet eine Kopfzeile mit Spaltennamen und danach eine Zeile je Messung.
 * Liefert `{ measurements, skipped, error }`.
 */
export function parseBodyFile(text, now = Date.now()) {
  const lines = String(text)
    .split(/\r?\n/)
    .map((l) => l.trim())
    .filter((l) => l.length > 0);

  if (lines.length < 2) {
    return { measurements: [], skipped: 0, error: 'Die Datei enthält keine Messungen.' };
  }

  const sep = detectSeparator(lines[0]);
  const header = splitLine(lines[0], sep).map((c) => c.toLowerCase());

  const iWeight = indexOfAny(header, WEIGHT);
  if (iWeight < 0) {
    return {
      measurements: [], skipped: 0,
      error: 'In der Kopfzeile ist keine Gewichtsspalte zu finden. '
        + `Gefunden wurde: ${header.join(', ').slice(0, 120)}`,
    };
  }

  const iDate = indexOfAny(header, DATE);
  const iFat = indexOfAny(header, FAT);
  const iLean = indexOfAny(header, LEAN);
  const iWater = indexOfAny(header, WATER);
  const iBone = indexOfAny(header, BONE);
  const iBasal = indexOfAny(header, BASAL);

  /** Nimmt den Wert nur, wenn er im plausiblen Bereich liegt. */
  const inRange = (v, lo, hi) => (v != null && v >= lo && v <= hi ? v : null);

  const out = [];
  let skipped = 0;

  lines.slice(1).forEach((line) => {
    const cells = splitLine(line, sep);
    const weight = toNumber(cells[iWeight]);
    // Werte außerhalb dieses Bereichs sind keine Körpergewichte
    if (weight == null || weight < 25 || weight > 300) {
      skipped++;
      return;
    }
    const at = (iDate >= 0 ? toDate(cells[iDate]) : null) ?? now;
    const basal = iBasal >= 0 ? inRange(toNumber(cells[iBasal]), 500, 5000) : null;

    out.push({
      at,
      weightKg: weight,
      bodyFatPercent: iFat >= 0 ? inRange(toNumber(cells[iFat]), 1, 70) : null,
      leanMassKg: iLean >= 0 ? inRange(toNumber(cells[iLean]), 10, 200) : null,
      waterMassKg: iWater >= 0 ? inRange(toNumber(cells[iWater]), 5, 150) : null,
      boneMassKg: iBone >= 0 ? inRange(toNumber(cells[iBone]), 0.5, 10) : null,
      basalKcal: basal == null ? null : Math.round(basal),
    });
  });

  if (out.length === 0) {
    return { measurements: [], skipped, error: 'Es ließ sich keine gültige Messung herauslesen.' };
  }

  // Je Tag nur die letzte Messung behalten, wie beim Weg über Health Connect
  const byDay = new Map();
  out.forEach((m) => {
    const day = Math.floor(m.at / DAY_MS);
    const old = byDay.get(day);
    if (!old || m.at >= old.at) byDay.set(day, m);
  });

  return {
    measurements: [...byDay.values()].sort((a, b) => a.at - b.at),
    skipped,
    error: null,
  };
}

/** Führt eingelesene Messungen mit den vorhandenen zusammen, ohne Dubletten. */
export function mergeBody(existing, incoming) {
  const byDay = new Map();
  (existing || []).forEach((m) => byDay.set(Math.floor(m.at / DAY_MS), m));
  (incoming || []).forEach((m) => {
    const day = Math.floor(m.at / DAY_MS);
    const old = byDay.get(day);
    // Die jüngere Messung des Tages gewinnt
    if (!old || m.at >= old.at) byDay.set(day, m);
  });
  return [...byDay.values()].sort((a, b) => a.at - b.at);
}
