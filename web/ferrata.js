// Der Fels-Bereich: Rang, Steigpass und die Einordnung einer Route.
//
// Diese Datei spiegelt data/Ferrata.kt der Android-App. Beide müssen dieselben
// Antworten geben — eine Empfehlung, die im Browser großzügiger ausfällt als am
// Handy, wäre schlimmer als gar keine.
//
// Die Leitsätze, an denen sich alles hier misst:
//   - Höhenmeter belohnen Umfang, nie Schwierigkeit.
//   - Der Rang belohnt Wiederholung, nie den einen kühnen Versuch.
//   - Kein Vorschlag liegt mehr als eine Stufe über dem, was zweimal mit Reserve
//     gegangen wurde. Ohne Ausnahme, unabhängig von jeder Trainingszahl.

const DAY_MS = 86400000;

export const GRADES = [
  { id: 'A', label: 'A', desc: 'leicht — gesichertes Gehgelände, wenig steil' },
  { id: 'B', label: 'B', desc: 'mäßig schwierig — erste steile Passagen, etwas Armkraft' },
  { id: 'C', label: 'C', desc: 'schwierig — senkrecht und ausgesetzt, Armkraft nötig' },
  { id: 'D', label: 'D', desc: 'sehr schwierig — überhängende Stellen, gute Kraft nötig' },
  { id: 'E', label: 'E', desc: 'extrem schwierig — durchgehend kraftraubend' },
  { id: 'F', label: 'F', desc: 'extrem — nur für sehr erfahrene und sehr starke Geher' },
];

/** Liest auch „C/D" — es zählt die schwerere Stufe, weil die über den Tag entscheidet. */
export function gradeIndex(text) {
  const letters = String(text || '').toUpperCase().replace(/[^A-F]/g, '');
  if (!letters) return 0;
  const i = GRADES.findIndex((g) => g.id === letters[letters.length - 1]);
  return i < 0 ? 0 : i;
}
export const gradeLabel = (i) => (GRADES[i] || GRADES[0]).label;

export const FEELS = [
  { id: 'LOCKER', label: 'Locker — ich hätte noch viel gehabt', reserve: 4 },
  { id: 'GUT', label: 'Gut gefordert — Reserve war da', reserve: 3 },
  { id: 'FORDERND', label: 'Fordernd — am Ende wurde es knapp', reserve: 2 },
  { id: 'GRENZWERTIG', label: 'Grenzwertig — ich musste mich zusammenreißen', reserve: 1 },
  { id: 'ZU_VIEL', label: 'Zu viel — das war über meiner Grenze', reserve: 0 },
];
export const feelById = (id) => FEELS.find((f) => f.id === id) || FEELS[1];

export const FLAGS = [
  { id: 'UNTERARME', label: 'Unterarme sind zugegangen' },
  { id: 'ZUGKRAFT', label: 'Das Ziehen ging aus' },
  { id: 'BEINE', label: 'Beine waren müde' },
  { id: 'KONDITION', label: 'Mir ist die Luft ausgegangen' },
  { id: 'KOPF', label: 'Die Ausgesetztheit war das Thema' },
  { id: 'ZEIT', label: 'Wir waren langsamer als geplant' },
  { id: 'ZWICKEN', label: 'Etwas hat gezwickt' },
  { id: 'RUND', label: 'Nichts davon — es lief rund' },
];

export const RANKS = [
  { id: 'TALGAENGER', icon: '🥾', title: 'Talgänger', minClean: 0, minGrade: null, minMeters: 0 },
  { id: 'STEIGFINDER', icon: '🧭', title: 'Steigfinder', minClean: 1, minGrade: null, minMeters: 0 },
  { id: 'DRAHTSEILGEHER', icon: '⛓', title: 'Drahtseilgeher', minClean: 3, minGrade: 0, minMeters: 600 },
  { id: 'KLAMMERKLETTERER', icon: '🪜', title: 'Klammerkletterer', minClean: 6, minGrade: 1, minMeters: 1800 },
  { id: 'WANDGEHER', icon: '🧗', title: 'Wandgeher', minClean: 10, minGrade: 2, minMeters: 4000 },
  { id: 'GRATGEHER', icon: '🏔', title: 'Gratgeher', minClean: 16, minGrade: 3, minMeters: 8000 },
  { id: 'FELSVERTRAUT', icon: '⛰️', title: 'Felsvertraut', minClean: 25, minGrade: 4, minMeters: 15000 },
];

/** Sauber heißt: durchgestiegen und mit Reserve am Ausstieg. */
export const isClean = (a) => !a.turnedBack && feelById(a.feel).reserve >= 2;

/** Höchste Stufe, die mindestens zweimal sauber gegangen wurde. -1, solange nichts steht. */
export function masteredIndex(ascents) {
  let last = -1;
  GRADES.forEach((g, i) => {
    const n = ascents.filter((a) => isClean(a) && gradeIndex(a.grade) >= i).length;
    if (n >= 2) last = i;
  });
  return last;
}

/** Höchstens eine Stufe über dem Bestätigten — der Deckel ohne Ausnahme. */
export const experienceIndex = (ascents) => Math.max(masteredIndex(ascents), 0) + 1;

export const cleanCount = (ascents) => ascents.filter(isClean).length;

/** Echte Höhenmeter aus Begehungen. Umkehren zählt voll mit. */
export const ascentMeters = (ascents) => ascents.reduce((s, a) => s + (a.climbMeters || 0), 0);

export function rank(ascents) {
  const clean = cleanCount(ascents);
  const mastered = masteredIndex(ascents);
  const meters = ascentMeters(ascents);
  let found = RANKS[0];
  RANKS.forEach((r) => {
    if (clean >= r.minClean && meters >= r.minMeters &&
        (r.minGrade === null || mastered >= r.minGrade)) found = r;
  });
  return found;
}

export function nextRankHint(ascents) {
  const cur = rank(ascents);
  const next = RANKS[RANKS.indexOf(cur) + 1];
  if (!next) return null;
  const fehlt = [];
  const clean = cleanCount(ascents);
  if (clean < next.minClean) fehlt.push(`${next.minClean - clean} saubere Begehungen`);
  const meters = ascentMeters(ascents);
  if (meters < next.minMeters) fehlt.push(`${next.minMeters - meters} Höhenmeter am Fels`);
  if (next.minGrade !== null && masteredIndex(ascents) < next.minGrade) {
    fehlt.push(`zweimal ${GRADES[next.minGrade].label} mit Reserve`);
  }
  return { rank: next, hint: fehlt.length ? 'Dafür fehlt: ' + fehlt.join(', ') : 'Alles beisammen.' };
}

/** Welche Stufe die Trainingsform hergibt. */
export function readinessIndex(readiness) {
  if (readiness >= 85) return 4;
  if (readiness >= 65) return 3;
  if (readiness >= 45) return 2;
  if (readiness >= 25) return 1;
  return 0;
}

/**
 * Die Stufe, die im Rahmen liegt — immer die kleinere aus Erfahrung und Form.
 *
 * Eine hohe Trainingszahl kann fehlende Routine nicht ersetzen: Wer stark ist, aber
 * noch nie an einer ausgesetzten Stelle stand, bekommt trotzdem nur B.
 */
export function recommendedIndex(ascents, readiness, now = Date.now(), weeksSinceTraining = 0) {
  let idx = Math.min(experienceIndex(ascents), readinessIndex(readiness));

  // Eine knappe oder abgebrochene Begehung deckelt auf ihre eigene Stufe, solange
  // sie nicht durch eine saubere derselben Stufe überholt wurde.
  ascents
    .filter((a) => feelById(a.feel).reserve <= 1 || a.turnedBack)
    .filter((a) => !ascents.some((b) => isClean(b) && gradeIndex(b.grade) === gradeIndex(a.grade) && b.date > a.date))
    .forEach((a) => { idx = Math.min(idx, gradeIndex(a.grade)); });

  // Nach langer Pause eine Stufe zurück.
  const last = ascents.reduce((m, a) => Math.max(m, a.date || 0), 0);
  const months = last ? Math.floor((now - last) / (30 * DAY_MS)) : 0;
  if (months >= 8 || weeksSinceTraining >= 3) idx -= 1;

  return Math.max(0, Math.min(GRADES.length - 1, idx));
}

export function recommendationReason(ascents, readiness) {
  const exp = experienceIndex(ascents);
  const form = readinessIndex(readiness);
  const mi = masteredIndex(ascents);
  const masteredLabel = mi >= 0 ? GRADES[mi].label : null;
  if (!ascents.length) {
    return 'Noch keine Begehung eingetragen. Bis dahin bleibt der Rahmen bei den leichten ' +
      'Stufen — unabhängig davon, wie gut das Training läuft.';
  }
  if (exp < form && masteredLabel) {
    return `Deine Kraft würde mehr hergeben. Am Fels hast du ${masteredLabel} bestätigt, ` +
      'deshalb bleibt es eine Stufe darüber.';
  }
  if (form < exp) return 'Du kannst mehr, als du gerade trainiert hast. Die Form begrenzt hier, nicht die Erfahrung.';
  return 'Erfahrung und Form passen gerade zusammen.';
}

export function buildSteigPass(ascents, readiness, now = Date.now(), weeksSinceTraining = 0) {
  const next = nextRankHint(ascents);
  const mi = masteredIndex(ascents);
  return {
    rank: rank(ascents),
    cleanAscents: cleanCount(ascents),
    mastered: mi >= 0 ? GRADES[mi] : null,
    meters: ascentMeters(ascents),
    readiness,
    recommended: GRADES[recommendedIndex(ascents, readiness, now, weeksSinceTraining)],
    reason: recommendationReason(ascents, readiness),
    nextRank: next ? next.rank : null,
    nextHint: next ? next.hint : 'Höchster Rang erreicht.',
  };
}

export const FIT = { PASST: 0, KNAPP: 1, ZIEL: 2, ZU_FRUEH: 3 };

/**
 * Wie eine Route zum Stand passt.
 *
 * Die schärfste Regel steckt in ZIEL: Eine Stufe nach oben wird nur auf Routen
 * vorgeschlagen, die einen Notausstieg haben oder kurz sind. Wer sich steigert, soll
 * das dort tun, wo Umkehren noch möglich ist — nicht in der Mitte einer langen Wand.
 */
export function fitFor(route, ascents, readiness) {
  const basis = Math.max(masteredIndex(ascents), 0);
  const rIdx = gradeIndex(route.grade);
  const gradeGap = rIdx - basis;
  const formIdx = readinessIndex(readiness);

  const clean = ascents.filter(isClean);
  const bestMeters = clean.reduce((m, a) => Math.max(m, a.climbMeters || 0), 0) || 250;
  const bestMin = clean.reduce((m, a) => Math.max(m, a.durationMin || 0), 0) || 180;
  const sizeGap = Math.max(
    route.climbMeters ? route.climbMeters / bestMeters : 0,
    route.totalMin ? route.totalMin / bestMin : 0,
  );

  const ofGrade = ascents.filter((a) => gradeIndex(a.grade) === rIdx);
  const lastOfGrade = ofGrade.length
    ? ofGrade.reduce((m, a) => (a.date > m.date ? a : m))
    : null;
  const wasHard = !!lastOfGrade && feelById(lastOfGrade.feel).reserve <= 2;

  if (gradeGap <= 0 && sizeGap <= 1.3 && rIdx <= formIdx && !wasHard) return FIT.PASST;
  if (gradeGap <= 0 && rIdx <= formIdx) return FIT.KNAPP;
  if (gradeGap === 1 && rIdx <= formIdx && (route.hasExit || sizeGap <= 1.0)) return FIT.ZIEL;
  return FIT.ZU_FRUEH;
}

export const FIT_LABEL = [
  'Im Rahmen dessen, was du schon gegangen bist',
  'Machbar, aber deutlich mehr als bisher — Zeit und Puffer einplanen',
  'Eine Stufe über dem Bestätigten — nur Steige mit Notausstieg oder kurzer Wand.',
  'Über deinen bisherigen Begehungen',
];
export const FIT_TITLE = ['Passt zu dir', 'Machbar mit Puffer', 'Der nächste Schritt', 'Noch nicht dran'];

/** Kennung für Einträge, die außerhalb des Wochenzyklus stehen. */
export const EXTRA_STAGE_ID = 'F0';

/** Der Fußtext unter jeder Routenliste. Er steht dort immer, nicht nur beim ersten Mal. */
export const DISCLAIMER =
  'Die App kennt weder Wetter noch Zustand der Sicherungen noch deine Tagesverfassung. ' +
  'Die Angaben stammen aus öffentlichen Quellen und können veraltet sein. ' +
  'Entschieden wird am Einstieg, nicht am Handy.';

/** Rückmeldung nach dem Eintragen — Umkehren ist ausdrücklich kein Misserfolg. */
export function completionLine(a) {
  if (a.turnedBack) {
    return 'Eingetragen. Umkehren ist eine Entscheidung, keine Niederlage — und die einzige, ' +
      'die man immer treffen kann.';
  }
  if (a.feel === 'LOCKER') return 'Eingetragen. Das saß. Beim nächsten Mal darf es etwas mehr sein.';
  if (a.feel === 'ZU_VIEL') return 'Eingetragen. Gut, dass du es weißt — die App rechnet ab jetzt damit.';
  return `Eingetragen. ${a.climbMeters || 0} Höhenmeter am Fels.`;
}
