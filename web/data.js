/**
 * Übungskatalog, Split und Progressionslogik der Web-Variante.
 *
 * Inhaltlich identisch zur Android-App (siehe docs/TRAININGSWISSEN.md) — wer hier etwas
 * ändert, sollte es dort ebenfalls anpassen, sonst laufen die beiden Varianten
 * inhaltlich auseinander.
 */

export const STATIONS = {
  LAT_PULLDOWN: { label: 'Latzug', hint: 'Zugstange oben am Turm' },
  CHEST_PRESS: { label: 'Brustpresse', hint: 'Drückhebel sitzend' },
  BUTTERFLY: { label: 'Butterfly', hint: 'Schwingarme für die Brust' },
  CABLE_LOW: { label: 'Unterer Kabelzug', hint: 'Zugpunkt unten, für Rudern und Curls' },
  LEG_EXTENSION: { label: 'Beinstrecker', hint: 'Beinhebel sitzend strecken' },
  LEG_CURL: { label: 'Beinbeuger', hint: 'Beinhebel beugen' },
  PULLUP_BAR: { label: 'Klimmzugstange', hint: 'Freie Stange zum Hängen' },
  BODYWEIGHT: { label: 'Körpergewicht', hint: 'Immer verfügbar' },
  DUMBBELL: { label: 'Freie Hanteln', hint: 'Kurzhanteln oder Langhantel' },
};

// Übungskatalog und Ausführungsanleitungen liegen in einer eigenen Datei,
// damit diese hier auf Split und Progressionslogik beschränkt bleibt.
// Importiert *und* re-exportiert, damit die Namen auch hier drin nutzbar sind.
import { KIND, EXERCISES, videoUrl } from './exercises.js';
import { BODYWEIGHT_EXERCISES, BODYWEIGHT_SUBSTITUTES } from './bodyweight.js';
export { KIND, EXERCISES, videoUrl, BODYWEIGHT_EXERCISES, BODYWEIGHT_SUBSTITUTES };

/**
 * Der vollständige Katalog: Geräteübungen und die Körpergewichts-Entsprechungen für
 * unterwegs. Beide gehören zusammen, weil Verlauf, Bestleistungen und Progression über
 * die Übungskennung laufen — fehlte hier eine unterwegs trainierte Übung, verschwände
 * sie aus der Statistik.
 */
export const ALL_EXERCISES = [...EXERCISES, ...BODYWEIGHT_EXERCISES];
export const byId = (id) => ALL_EXERCISES.find((e) => e.id === id);

export const DAYS = [
  {
    id: 'A', title: 'Zug & Griff', subtitle: 'Der Klettersteig-Tag',
    exerciseIds: ['pullup', 'latpull_wide', 'row_cable', 'row_lat_narrow', 'hang_knee_raise', 'curl', 'curl_bw', 'deadhang'],
  },
  {
    id: 'B', title: 'Beine & Steigkraft', subtitle: 'Höhenmeter-Motor',
    exerciseIds: ['stepup', 'leg_ext', 'leg_curl', 'split_squat', 'calf_raise', 'plank', 'side_plank'],
  },
  {
    id: 'C', title: 'Druck & Stabilität', subtitle: 'Balance und Haltung',
    exerciseIds: ['chest_press', 'shoulder_press', 'butterfly', 'pushup', 'reverse_fly', 'triceps', 'farmer_hold', 'deadhang'],
  },
];

export const dayById = (id) => DAYS.find((d) => d.id === id);

/** Paare aus Übung und Ersatz — ist die erste verfügbar, fliegt die zweite raus. */
const ALTERNATIVES = [
  ['row_cable', 'row_lat_narrow'],
  ['curl', 'curl_bw'],
  ['chest_press', 'pushup'],
  ['butterfly', 'pushup'],
];

export function exercisesFor(day, profile, hidden = []) {
  const available = day.exerciseIds
    .map(byId)
    .filter(Boolean)
    .filter((e) => profile.stations.includes(e.station))
    .filter((e) => !hidden.includes(e.id));

  const drop = new Set();
  for (const [primary, fallback] of ALTERNATIVES) {
    if (available.some((e) => e.id === primary)) drop.add(fallback);
  }
  return available.filter((e) => !drop.has(e.id));
}

// ---------------------------------------------------------------------------
// Progression
// ---------------------------------------------------------------------------

export const ADVICE = {
  START: 'START', HOLD: 'HOLD', INCREASE: 'INCREASE', BACKOFF: 'BACKOFF', DELOAD: 'DELOAD',
};

export const CYCLE_WEEKS = 5;
const DAY_MS = 24 * 60 * 60 * 1000;

export function weekInCycle(profile, now) {
  if (!profile.cycleStart) return 1;
  const weeks = Math.floor((now - profile.cycleStart) / (7 * DAY_MS));
  return (weeks % CYCLE_WEEKS) + 1;
}

export const isDeloadWeek = (week) => week === CYCLE_WEEKS;

export function weekLabel(week) {
  return [
    'Aufbau — locker starten, 3 Wdh. Puffer',
    'Steigerung — 2 Wdh. Puffer',
    'Intensiv — 1 Wdh. Puffer',
    'Spitze — bis kurz vors Limit',
    'Entlastung — bewusst leicht, der Körper baut jetzt auf',
  ][week - 1];
}

export function roundToPlate(weight, step) {
  if (!step || step <= 0) return weight;
  return Math.max(0, Math.round(weight / step) * step);
}

export function fmtKg(kg) {
  const r = Math.round(kg * 10) / 10;
  return Number.isInteger(r) ? `${r} kg` : `${r} kg`;
}

const START_FACTORS = {
  latpull_wide: 0.45, row_cable: 0.4, row_lat_narrow: 0.4, chest_press: 0.4,
  butterfly: 0.25, shoulder_press: 0.25, leg_ext: 0.35, leg_curl: 0.3,
  curl: 0.15, curl_bw: 0.15, triceps: 0.15, reverse_fly: 0.12,
};

const START_SECONDS = { deadhang: 15, plank: 30, side_plank: 20, farmer_hold: 20 };

function startWeight(ex, profile) {
  const factor = START_FACTORS[ex.id] ?? 0.3;
  const step = Math.min(ex.increment, profile.plateStepKg);
  return roundToPlate(profile.bodyweightKg * factor, step);
}

/** Sätze einer Übung je Einheit, neueste zuerst. */
function history(exerciseId, sessions) {
  return sessions
    .slice()
    .sort((a, b) => b.startedAt - a.startedAt)
    .map((s) => s.sets.filter((x) => x.exerciseId === exerciseId))
    .filter((arr) => arr.length > 0);
}

/** Die in einer Einheit hauptsächlich verwendete Last. */
function workingLoad(sets) {
  const counts = new Map();
  for (const s of sets) counts.set(s.weightKg, (counts.get(s.weightKg) || 0) + 1);
  let best = 0, bestCount = -1;
  for (const [w, c] of counts) if (c > bestCount) { best = w; bestCount = c; }
  return best;
}

/**
 * Oberes Ende erreicht? Der letzte Satz darf abfallen — verlangt wird das obere
 * Ende in allen Sätzen bis auf einen.
 */
function hitTopOfRange(ex, sets) {
  if (!sets.length) return false;
  const required = Math.max(1, sets.length - 1);
  if (ex.progression === KIND.TIME) {
    const best = Math.max(...sets.map((s) => s.seconds || 0));
    return sets.filter((s) => (s.seconds || 0) >= best).length >= required;
  }
  return sets.filter((s) => s.reps >= ex.repMax).length >= required;
}

/**
 * Was soll heute auf die Maschine?
 *
 * Doppelte Progression mit 2-für-2-Regel: Erst im Wiederholungsfenster nach oben,
 * aufgelastet wird erst nach zwei Einheiten am oberen Ende.
 */
export function suggest(ex, sessions, profile, now, recovery = null) {
  const week = weekInCycle(profile, now);
  // Genau eine Reduktion, nie mehrere übereinander: Das Erholungsfenster nach einer
  // Tour ist akuter als die planmäßige Entlastungswoche.
  const deload = !recovery && isDeloadWeek(week);
  const hist = history(ex.id, sessions);

  const mk = (advice, weightKg, targetReps, targetSeconds, sets, headline, reason, previousHeadline = null) =>
    ({ exercise: ex, advice, weightKg, targetReps, targetSeconds, sets, headline, reason, previousHeadline });

  // --- Fall 1: noch nie trainiert ---
  if (!hist.length) {
    if (ex.progression === KIND.TIME) {
      const s = START_SECONDS[ex.id] ?? 20;
      return mk(ADVICE.START, 0, 0, s, ex.sets, `${s} s`,
        'Erste Einheit. Halte so lange sauber, wie es geht — der Wert ist nur ein Startpunkt.');
    }
    if (ex.progression === KIND.REPS) {
      return mk(ADVICE.START, 0, ex.repMin, 0, ex.sets, `${ex.repMin}–${ex.repMax} Wdh.`,
        'Erste Einheit mit dem eigenen Körpergewicht. Notiere ehrlich, was du schaffst.');
    }
    const w = startWeight(ex, profile);
    return mk(ADVICE.START, w, ex.repMin, 0, ex.sets, fmtKg(w),
      'Startschätzung aus deinem Körpergewicht. Wenn sich der erste Satz zu leicht anfühlt, leg direkt eine Platte drauf.');
  }

  const last = hist[0];
  const lastLoad = workingLoad(last);
  const lastBestReps = Math.max(...last.map((s) => s.reps || 0));
  const lastBestSecs = Math.max(...last.map((s) => s.seconds || 0));

  // --- Fall 1c: Eine Tour wirkt noch nach ---
  if (recovery) {
    const f = recovery.level.factor;
    const sets = Math.max(2, ex.sets - 1);
    const warum = `${recovery.sourceName} steckt dir noch in den Armen — ` +
      (recovery.level.id === 'ERHOLUNG'
        ? 'heute wäre Erholung besser. Wenn du trainierst, dann deutlich leichter.'
        : 'heute bewusst leichter, morgen geht es normal weiter.');
    if (ex.progression === KIND.TIME) {
      const t = Math.round(lastBestSecs * f);
      return mk(ADVICE.DELOAD, 0, 0, t, sets, `${t} s`, warum, `${lastBestSecs} s`);
    }
    const w = roundToPlate(lastLoad * f, profile.plateStepKg);
    const reps = Math.max(ex.repMin, Math.round(lastBestReps * f));
    return mk(ADVICE.DELOAD, w, reps, 0, sets,
      ex.progression === KIND.WEIGHT ? fmtKg(w) : `${reps} Wdh.`,
      warum,
      ex.progression === KIND.WEIGHT ? fmtKg(lastLoad) : null);
  }

  // --- Fall 2: Entlastungswoche ---
  if (deload) {
    const sets = Math.max(2, ex.sets - 1);
    if (ex.progression === KIND.TIME) {
      const t = Math.round(lastBestSecs * 0.7);
      return mk(ADVICE.DELOAD, 0, 0, t, sets, `${t} s`,
        'Entlastungswoche: rund 30 % kürzer halten, ein Satz weniger. Der Fortschritt entsteht jetzt in der Erholung.',
        `${lastBestSecs} s`);
    }
    const w = roundToPlate(lastLoad * 0.85, profile.plateStepKg);
    return mk(ADVICE.DELOAD, w, ex.repMin, 0, sets,
      ex.progression === KIND.WEIGHT ? fmtKg(w) : `${ex.repMin} Wdh.`,
      'Entlastungswoche: etwa 15 % weniger Last, ein Satz weniger, und immer 3–4 Wiederholungen Puffer lassen.',
      ex.progression === KIND.WEIGHT ? fmtKg(lastLoad) : null);
  }

  const topLast = hitTopOfRange(ex, last);
  let topBefore = false;
  if (hist[1]) {
    const prev = hist[1];
    // Die zweite Einheit zählt nur, wenn seither kein Rückschritt passiert ist.
    let noRegression;
    if (ex.progression === KIND.WEIGHT) {
      noRegression = workingLoad(prev) >= lastLoad - 0.01;
    } else if (ex.progression === KIND.TIME) {
      noRegression = lastBestSecs >= Math.max(0, ...prev.map((s) => s.seconds || 0));
    } else {
      noRegression = lastBestReps >= Math.max(0, ...prev.map((s) => s.reps || 0))
        && workingLoad(prev) >= lastLoad - 0.01;
    }
    topBefore = noRegression && hitTopOfRange(ex, prev);
  }

  // --- Fall 3: 2-für-2 erfüllt → auflasten ---
  if (topLast && topBefore) {
    if (ex.progression === KIND.TIME) {
      const next = lastBestSecs + ex.increment;
      const extra = lastBestSecs >= 60
        ? ' Du bist über einer Minute — ab hier lohnt sich Zusatzgewicht statt noch mehr Zeit.' : '';
      return mk(ADVICE.INCREASE, 0, 0, next, ex.sets, `${next} s`,
        `Zweimal in Folge das Ziel gehalten — plus ${ex.increment} Sekunden.${extra}`, `${lastBestSecs} s`);
    }
    if (ex.progression === KIND.REPS) {
      if (lastBestReps >= ex.repMax) {
        const w = roundToPlate(lastLoad + ex.increment, ex.increment);
        return mk(ADVICE.INCREASE, w, ex.repMin, 0, ex.sets,
          w > 0 ? `+${fmtKg(w)} Zusatz` : `${ex.repMax} Wdh.`,
          `Du bist am oberen Ende des Fensters. Jetzt Zusatzgewicht anhängen und wieder bei ${ex.repMin} Wiederholungen anfangen.`,
          `${lastBestReps} Wdh.`);
      }
      const next = Math.min(lastBestReps + 1, ex.repMax);
      return mk(ADVICE.INCREASE, lastLoad, next, 0, ex.sets, `${next} Wdh.`,
        'Zweimal sauber durchgezogen — eine Wiederholung mehr pro Satz.', `${lastBestReps} Wdh.`);
    }
    const w = roundToPlate(lastLoad + ex.increment, ex.increment);
    return mk(ADVICE.INCREASE, w, ex.repMin, 0, ex.sets, fmtKg(w),
      `Du hast ${fmtKg(lastLoad)} zweimal in Folge mit ${ex.repMax} Wiederholungen geschafft. Zeit für ${fmtKg(w)} — die Wiederholungen fallen dabei wieder auf ${ex.repMin}, das ist so gewollt.`,
      fmtKg(lastLoad));
  }

  // --- Fall 4: einmal oben, einmal fehlt noch ---
  if (topLast) {
    const hint = 'Noch eine Einheit auf diesem Niveau, dann wird aufgelastet.';
    if (ex.progression === KIND.TIME) {
      return mk(ADVICE.HOLD, 0, 0, lastBestSecs, ex.sets, `${lastBestSecs} s`, `Ziel erreicht. ${hint}`);
    }
    return mk(ADVICE.HOLD, lastLoad, ex.repMax, 0, ex.sets,
      ex.progression === KIND.WEIGHT ? fmtKg(lastLoad) : `${ex.repMax} Wdh.`,
      `Obere Grenze erreicht. ${hint}`);
  }

  // --- Fall 5: Stagnation über drei Einheiten ---
  if (hist.length >= 3) {
    const loads = hist.slice(0, 3).map(workingLoad);
    const bests = hist.slice(0, 3).map((s) =>
      ex.progression === KIND.TIME
        ? Math.max(0, ...s.map((x) => x.seconds || 0))
        : Math.max(0, ...s.map((x) => x.reps || 0)));
    const sameLoad = new Set(loads).size === 1;
    if (sameLoad && bests[0] <= bests[2]) {
      if (ex.progression === KIND.TIME) {
        const next = Math.max(10, Math.round(lastBestSecs * 0.8));
        return mk(ADVICE.BACKOFF, 0, 0, next, ex.sets, `${next} s`,
          'Drei Einheiten ohne Fortschritt. Geh bewusst zurück und bau die Zeit neu auf — das löst die Blockade meist schneller als Draufhalten.',
          `${lastBestSecs} s`);
      }
      const w = roundToPlate(lastLoad * 0.9, profile.plateStepKg);
      return mk(ADVICE.BACKOFF, w, ex.repMin + 2, 0, ex.sets,
        ex.progression === KIND.WEIGHT ? fmtKg(w) : `${ex.repMin + 2} Wdh.`,
        'Drei Einheiten auf der Stelle. Nimm rund 10 % runter, sammle zwei saubere Einheiten und greif dann wieder an.',
        ex.progression === KIND.WEIGHT ? fmtKg(lastLoad) : null);
    }
  }

  // --- Fall 6: normaler Weg ---
  if (ex.progression === KIND.TIME) {
    const next = lastBestSecs + 2;
    return mk(ADVICE.HOLD, 0, 0, next, ex.sets, `${next} s`,
      'Dranbleiben: zwei Sekunden mehr als beim letzten Mal.', `${lastBestSecs} s`);
  }
  const next = Math.min(Math.max(lastBestReps + 1, ex.repMin), ex.repMax);
  return mk(ADVICE.HOLD, lastLoad, next, 0, ex.sets,
    ex.progression === KIND.WEIGHT ? fmtKg(lastLoad) : `${next} Wdh.`,
    `Gleiche Last, Ziel sind ${next} Wiederholungen. Bei ${ex.repMax} in zwei Einheiten kommt Gewicht drauf.`,
    ex.progression === KIND.WEIGHT ? fmtKg(lastLoad) : `${lastBestReps} Wdh.`);
}

// ---------------------------------------------------------------------------
// Auswertungen
// ---------------------------------------------------------------------------

function weekKey(millis) {
  const d = new Date(millis);
  const target = new Date(d.valueOf());
  const dayNr = (d.getDay() + 6) % 7;
  target.setDate(target.getDate() - dayNr + 3);
  const firstThursday = new Date(target.getFullYear(), 0, 4);
  const diff = target - firstThursday;
  return `${target.getFullYear()}-${1 + Math.round(diff / (7 * DAY_MS))}`;
}

export function weeklyStreak(sessions, now) {
  if (!sessions.length) return 0;
  const weeks = new Set(sessions.map((s) => weekKey(s.startedAt)));
  let streak = 0;
  let cursor = now;
  if (!weeks.has(weekKey(now))) cursor -= 7 * DAY_MS;
  while (weeks.has(weekKey(cursor))) {
    streak++;
    cursor -= 7 * DAY_MS;
  }
  return streak;
}

export function sessionsThisWeek(sessions, now) {
  const key = weekKey(now);
  return sessions.filter((s) => weekKey(s.startedAt) === key).length;
}

export function nextDayId(sessions) {
  if (!sessions.length) return DAYS[0].id;
  const lastDay = sessions.reduce((a, b) => (a.startedAt > b.startedAt ? a : b)).dayId;
  const idx = DAYS.findIndex((d) => d.id === lastDay);
  if (idx < 0) return DAYS[0].id;
  return DAYS[(idx + 1) % DAYS.length].id;
}

export const volumeOf = (session) =>
  session.sets.reduce((sum, s) => sum + (s.weightKg || 0) * (s.reps || 0), 0);

export function bestOf(sessions, id, field) {
  const vals = sessions.flatMap((s) => s.sets).filter((s) => s.exerciseId === id).map((s) => s[field] || 0);
  return vals.length ? Math.max(...vals) : 0;
}

/**
 * Klettersteig-Bereitschaft 0–100: Griffkraft 30 %, Zugkraft 25 %, Rumpf 20 %,
 * Beine 15 %, Regelmäßigkeit 10 %.
 */
export function ferrataReadiness(sessions, now) {
  const hang = bestOf(sessions, 'deadhang', 'seconds');
  const pull = bestOf(sessions, 'pullup', 'reps');
  const knee = bestOf(sessions, 'hang_knee_raise', 'reps');
  const plank = bestOf(sessions, 'plank', 'seconds');
  const step = bestOf(sessions, 'stepup', 'reps');

  const grip = Math.min(hang / 60, 1) * 30;
  const pullS = Math.min(pull / 8, 1) * 25;
  const core = ((Math.min(knee / 12, 1) + Math.min(plank / 90, 1)) / 2) * 20;
  const legs = Math.min(step / 15, 1) * 15;
  const consistency = Math.min(weeklyStreak(sessions, now) / 8, 1) * 10;

  return Math.min(100, Math.max(0, Math.round(grip + pullS + core + legs + consistency)));
}

export function readinessLabel(score) {
  if (score >= 85) return 'Steigfest — auch längere D-Routen sind drin';
  if (score >= 65) return 'Gut dabei — C/D-Routen solltest du sauber durchziehen';
  if (score >= 45) return 'Solide Basis — B/C-Routen passen, mach bei der Griffkraft weiter';
  if (score >= 25) return 'Im Aufbau — halte dich an leichtere Routen mit kurzem Zustieg';
  return 'Am Anfang — die ersten Wochen zahlen am meisten ein';
}

export function trend(exerciseId, sessions) {
  const ex = byId(exerciseId);
  if (!ex) return [];
  return sessions
    .slice()
    .sort((a, b) => a.startedAt - b.startedAt)
    .map((s) => {
      const sets = s.sets.filter((x) => x.exerciseId === exerciseId);
      if (!sets.length) return null;
      if (ex.progression === KIND.TIME) {
        const v = Math.max(...sets.map((x) => x.seconds || 0));
        return { at: s.startedAt, value: v, label: `${v} s` };
      }
      if (ex.progression === KIND.REPS) {
        const v = Math.max(...sets.map((x) => x.reps || 0));
        return { at: s.startedAt, value: v, label: `${v} Wdh.` };
      }
      const v = Math.max(...sets.map((x) => x.weightKg || 0));
      return { at: s.startedAt, value: v, label: fmtKg(v) };
    })
    .filter(Boolean);
}

export function records(sessions) {
  return EXERCISES.map((ex) => {
    const pairs = sessions.flatMap((s) => s.sets.filter((x) => x.exerciseId === ex.id).map((x) => [s, x]));
    if (!pairs.length) return null;
    if (ex.progression === KIND.TIME) {
      const [s, set] = pairs.reduce((a, b) => ((b[1].seconds || 0) > (a[1].seconds || 0) ? b : a));
      return { exerciseId: ex.id, name: ex.name, value: `${set.seconds} s`, achievedAt: s.startedAt };
    }
    if (ex.progression === KIND.REPS) {
      const [s, set] = pairs.reduce((a, b) => ((b[1].reps || 0) > (a[1].reps || 0) ? b : a));
      const extra = set.weightKg > 0 ? ` + ${fmtKg(set.weightKg)}` : '';
      return { exerciseId: ex.id, name: ex.name, value: `${set.reps} Wdh.${extra}`, achievedAt: s.startedAt };
    }
    const [s, set] = pairs.reduce((a, b) => ((b[1].weightKg || 0) > (a[1].weightKg || 0) ? b : a));
    return { exerciseId: ex.id, name: ex.name, value: `${fmtKg(set.weightKg)} × ${set.reps}`, achievedAt: s.startedAt };
  }).filter(Boolean).sort((a, b) => b.achievedAt - a.achievedAt);
}
