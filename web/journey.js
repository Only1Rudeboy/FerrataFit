/**
 * Der Steig — Etappensystem, Dehnkatalog, Höhenmeter und Abzeichen.
 *
 * Grundidee: Jeder Tag hat eine Aufgabe, nicht nur die drei Krafttage. Die Tage
 * dazwischen sind keine Leerstellen, sondern eigene Etappen mit Mobility- oder
 * Ausdauerinhalt — laut Trainingslehre passiert die Anpassung ohnehin dort und
 * nicht während der Belastung.
 *
 * Eine Etappe schaltet die nächste frei. Wer eine auslässt, kommt trotzdem weiter
 * (Überspringen ist erlaubt), bekommt dafür aber keine Höhenmeter — die App bremst
 * also nicht, sie belohnt nur.
 */

import { DAYS } from './data.js';

// ---------------------------------------------------------------------------
// Dehn- und Mobility-Katalog
// ---------------------------------------------------------------------------

/**
 * Haltezeiten nach gängiger Empfehlung: 20–30 Sekunden im Alltag, an
 * Regenerationstagen 30–90 Sekunden, weil längeres Halten das Nervensystem
 * herunterfährt und die Erholung unterstützt.
 *
 * Die Auswahl folgt den Zonen, die bei Kletterern und am Steig am meisten
 * zumachen: Unterarme, Waden, Hüfte, Schultern und Brustwirbelsäule.
 */
export const MOBILITY = [
  {
    id: 'forearm_flexor', name: 'Unterarm-Beuger dehnen', seconds: 30, perSide: true,
    zone: 'Unterarme',
    cue: 'Arm strecken, Handfläche nach vorne, Finger mit der anderen Hand sanft zu dir ziehen.',
    why: 'Die Griffmuskulatur verkürzt durch jedes Hängen und Ziehen. Diese Dehnung ist die wichtigste Gegenmaßnahme gegen Ellbogenbeschwerden.',
  },
  {
    id: 'forearm_extensor', name: 'Unterarm-Strecker dehnen', seconds: 30, perSide: true,
    zone: 'Unterarme',
    cue: 'Arm strecken, Handrücken nach unten, Hand sanft nach innen ziehen.',
    why: 'Gegenspieler zur Griffmuskulatur. Beide zusammen halten das Ellbogengelenk im Gleichgewicht.',
  },
  {
    id: 'chest_doorway', name: 'Brustdehnung im Türrahmen', seconds: 30, perSide: true,
    zone: 'Brust & Schulter',
    cue: 'Unterarm an den Rahmen, Ellbogen auf Schulterhöhe, Oberkörper langsam wegdrehen.',
    why: 'Öffnet die Brust gegen den Rundrücken, den Rucksacktragen und Drückübungen begünstigen.',
  },
  {
    id: 'lat_stretch', name: 'Latissimus dehnen', seconds: 30, perSide: true,
    zone: 'Rücken',
    cue: 'Hand an einer Stange über Kopf, Gesäß nach hinten schieben, Flanke lang machen.',
    why: 'Der Latissimus arbeitet bei jedem Zug am Steig. Verkürzt er, leidet die Schulterfreiheit über Kopf.',
  },
  {
    id: 'hip_flexor', name: 'Hüftbeuger im Ausfallschritt', seconds: 30, perSide: true,
    zone: 'Hüfte',
    cue: 'Tiefer Ausfallschritt, hinteres Knie am Boden, Becken nach vorne schieben, Gesäß anspannen.',
    why: 'Die Hüfte ist die kritischste Zone für hohe Tritte. Vom vielen Sitzen verkürzt der Hüftbeuger und bremst genau diese Bewegung.',
  },
  {
    id: 'pigeon', name: 'Taubenstellung', seconds: 45, perSide: true,
    zone: 'Hüfte',
    cue: 'Vorderes Bein angewinkelt ablegen, hinteres Bein lang, Oberkörper aufrecht oder abgelegt.',
    why: 'Öffnet die äußere Hüfte — das schafft Bewegungsspielraum für weite und hohe Tritte.',
  },
  {
    id: 'hamstring', name: 'Oberschenkelrückseite dehnen', seconds: 30, perSide: true,
    zone: 'Beine',
    cue: 'Bein gestreckt vor dir aufstellen, Hüfte nach hinten schieben, Rücken gerade lassen.',
    why: 'Verkürzte Rückseiten ziehen das Becken nach hinten und stehlen dir Höhe beim Tritt.',
  },
  {
    id: 'calf_wall', name: 'Wadendehnung an der Wand', seconds: 30, perSide: true,
    zone: 'Waden',
    cue: 'Fußballen an die Wand, Ferse am Boden, Hüfte nach vorne schieben.',
    why: 'Die Waden tragen dich stundenlang auf schmalen Klammern. Sie gehören zu den Zonen, die am schnellsten zumachen.',
  },
  {
    id: 'thoracic', name: 'Brustwirbelsäule mobilisieren', seconds: 40, perSide: false,
    zone: 'Rücken',
    cue: 'Vierfüßlerstand, abwechselnd Rücken runden und sanft strecken (Katze–Kuh), ruhig atmen.',
    why: 'Eine bewegliche Brustwirbelsäule nimmt Druck von Schultern und Lendenwirbeln.',
  },
  {
    id: 'child_pose', name: 'Kindshaltung', seconds: 60, perSide: false,
    zone: 'Rücken & Schultern',
    cue: 'Fersensitz, Oberkörper ablegen, Arme lang nach vorne, tief in den Rücken atmen.',
    why: 'Streckt den ganzen Rücken und beruhigt nach der Belastung. Guter Abschluss jeder Einheit.',
  },
  {
    id: 'neck', name: 'Nacken lösen', seconds: 25, perSide: true,
    zone: 'Nacken',
    cue: 'Kopf zur Seite neigen, Schulter bewusst unten lassen, mit der Hand leicht nachhelfen.',
    why: 'Der Nacken verspannt beim Sichern und beim Blick nach oben an der Wand.',
  },
  {
    id: 'wrist_circles', name: 'Handgelenke kreisen', seconds: 30, perSide: false,
    zone: 'Handgelenke',
    cue: 'Finger verschränken, langsam in beide Richtungen kreisen, dann Hände öffnen und schließen.',
    why: 'Bringt Durchblutung in die Handgelenke — die tragen am Steig alles.',
  },
];

export const mobilityById = (id) => MOBILITY.find((m) => m.id === id);

// ---------------------------------------------------------------------------
// Etappen
// ---------------------------------------------------------------------------

export const STAGE_KIND = {
  STRENGTH: 'STRENGTH',     // Krafteinheit am Gerät
  MOBILITY: 'MOBILITY',     // Dehnen und Mobilisieren
  ENDURANCE: 'ENDURANCE',   // Wandern, Treppen, Rad — selbst eintragen
  RECOVERY: 'RECOVERY',     // Langes Dehnen, bewusst ruhig
};

/**
 * Ein Wochenzyklus aus sieben Etappen. Zwischen zwei Krafteinheiten liegt immer
 * mindestens ein Tag mit leichterem Inhalt — so bleibt der Reiz hoch und die
 * Erholung trotzdem gewahrt.
 */
export const STAGES = [
  {
    id: 'S1', kind: STAGE_KIND.STRENGTH, dayId: 'A',
    title: 'Zug & Griff', subtitle: 'Der Klettersteig-Tag',
    icon: '💪', meters: 120,
  },
  {
    id: 'S2', kind: STAGE_KIND.MOBILITY,
    title: 'Lockern', subtitle: 'Unterarme und Brust öffnen',
    icon: '🧘', meters: 40,
    mobilityIds: ['forearm_flexor', 'forearm_extensor', 'chest_doorway', 'lat_stretch', 'wrist_circles'],
  },
  {
    id: 'S3', kind: STAGE_KIND.STRENGTH, dayId: 'B',
    title: 'Beine & Steigkraft', subtitle: 'Höhenmeter-Motor',
    icon: '🦵', meters: 120,
  },
  {
    id: 'S4', kind: STAGE_KIND.MOBILITY,
    title: 'Hüfte & Beine', subtitle: 'Platz für hohe Tritte',
    icon: '🧘', meters: 40,
    mobilityIds: ['hip_flexor', 'pigeon', 'hamstring', 'calf_wall', 'thoracic'],
  },
  {
    id: 'S5', kind: STAGE_KIND.STRENGTH, dayId: 'C',
    title: 'Druck & Stabilität', subtitle: 'Balance und Haltung',
    icon: '💪', meters: 120,
  },
  {
    id: 'S6', kind: STAGE_KIND.ENDURANCE,
    title: 'Rausgehen', subtitle: 'Wandern, Treppen oder Rad',
    icon: '🥾', meters: 80,
    hint: 'Mindestens 30 Minuten am Stück. Höhenmeter zählen doppelt — Treppenhaus gilt.',
  },
  {
    id: 'S7', kind: STAGE_KIND.RECOVERY,
    title: 'Runterkommen', subtitle: 'Langes Dehnen, ruhig atmen',
    icon: '🌙', meters: 50,
    mobilityIds: ['child_pose', 'pigeon', 'thoracic', 'neck', 'calf_wall', 'forearm_flexor'],
    longHold: true,
  },
];

export const stageAt = (index) => STAGES[index % STAGES.length];
export const stageById = (id) => STAGES.find((s) => s.id === id);

/** Welcher Trainingstag gehört zur Etappe? Nur bei Krafteinheiten belegt. */
export function dayForStage(stage) {
  return stage.dayId ? DAYS.find((d) => d.id === stage.dayId) : null;
}

/**
 * Wie viele Etappen sind erledigt? Daraus ergibt sich, welche gerade offen ist.
 * Übersprungene zählen mit, damit man nicht steckenbleibt.
 */
export function currentStageIndex(progress) {
  return progress.length;
}

export function currentStage(progress) {
  return stageAt(currentStageIndex(progress));
}

/** Wievielte Runde durch den Wochenzyklus? Ab 1 gezählt. */
export function cycleNumber(progress) {
  return Math.floor(currentStageIndex(progress) / STAGES.length) + 1;
}

// ---------------------------------------------------------------------------
// Höhenmeter und Gipfel
// ---------------------------------------------------------------------------

/** Summe der Höhenmeter aus tatsächlich erledigten Etappen. */
export function totalMeters(progress) {
  return progress.reduce((sum, p) => sum + (p.skipped ? 0 : (p.meters || 0)), 0);
}

/**
 * Gipfel als Etappenziele. Die Zahlen sind echte Höhen — das macht den Fortschritt
 * greifbarer als eine abstrakte Punktzahl.
 */
export const SUMMITS = [
  { m: 300, name: 'Erste Aussicht', note: 'Der Anfang ist gemacht' },
  { m: 800, name: 'Waldgrenze', note: 'Ab hier wird die Sicht frei' },
  { m: 1600, name: 'Almhochtal', note: 'Du bist im Rhythmus' },
  { m: 2600, name: 'Hoher Freschen', note: '2.004 m — dein Hausberg wäre geschafft' },
  { m: 4000, name: 'Piz Buin', note: '3.312 m — Vorarlbergs höchster' },
  { m: 6000, name: 'Mont Blanc', note: '4.810 m — der Höchste der Alpen' },
  { m: 9000, name: 'Everest', note: '8.848 m — Grenze des Denkbaren' },
  { m: 14000, name: 'Zweimal Everest', note: 'Jetzt wird es albern. Weiter so.' },
];

export function summitProgress(meters) {
  const next = SUMMITS.find((s) => s.m > meters) || SUMMITS[SUMMITS.length - 1];
  const prevIdx = SUMMITS.indexOf(next) - 1;
  const prev = prevIdx >= 0 ? SUMMITS[prevIdx] : { m: 0 };
  const span = next.m - prev.m || 1;
  return {
    next,
    reached: SUMMITS.filter((s) => s.m <= meters),
    progress: Math.min(1, Math.max(0, (meters - prev.m) / span)),
    toGo: Math.max(0, next.m - meters),
  };
}

// ---------------------------------------------------------------------------
// Abzeichen
// ---------------------------------------------------------------------------

/**
 * Jedes Abzeichen prüft sich selbst gegen den Gesamtzustand. `check` bekommt
 * ein Objekt mit Sitzungen, Etappenfortschritt und Bestwerten.
 */
export const BADGES = [
  {
    id: 'first_stage', icon: '🥾', name: 'Losgegangen',
    desc: 'Erste Etappe abgeschlossen',
    check: (s) => s.progress.filter((p) => !p.skipped).length >= 1,
  },
  {
    id: 'first_week', icon: '📅', name: 'Erste Runde',
    desc: 'Alle sieben Etappen eines Zyklus geschafft',
    check: (s) => s.progress.filter((p) => !p.skipped).length >= 7,
  },
  {
    id: 'hang30', icon: '✊', name: 'Griffig',
    desc: '30 Sekunden am Stück gehangen',
    check: (s) => s.best.deadhang >= 30,
  },
  {
    id: 'hang60', icon: '🔒', name: 'Griffmeister',
    desc: 'Eine volle Minute Dead Hang',
    check: (s) => s.best.deadhang >= 60,
  },
  {
    id: 'pullup1', icon: '⬆️', name: 'Der erste Zug',
    desc: 'Ein sauberer Klimmzug',
    check: (s) => s.best.pullup >= 1,
  },
  {
    id: 'pullup5', icon: '🧗', name: 'Fünf am Stück',
    desc: 'Fünf Klimmzüge in einem Satz',
    check: (s) => s.best.pullup >= 5,
  },
  {
    id: 'first_increase', icon: '📈', name: 'Aufgelastet',
    desc: 'Zum ersten Mal mehr Gewicht aufgelegt',
    check: (s) => s.increases >= 1,
  },
  {
    id: 'mobility10', icon: '🧘', name: 'Geschmeidig',
    desc: 'Zehn Mobility-Etappen abgeschlossen',
    check: (s) => s.progress.filter((p) => !p.skipped && (p.kind === STAGE_KIND.MOBILITY || p.kind === STAGE_KIND.RECOVERY)).length >= 10,
  },
  {
    id: 'streak4', icon: '🔥', name: 'Dranbleiber',
    desc: 'Vier Wochen in Folge trainiert',
    check: (s) => s.weeklyStreak >= 4,
  },
  {
    id: 'summit1', icon: '⛰️', name: 'Waldgrenze',
    desc: '800 Höhenmeter gesammelt',
    check: (s) => s.meters >= 800,
  },
  {
    id: 'summit_freschen', icon: '🏔', name: 'Hoher Freschen',
    desc: '2.600 Höhenmeter — dein Hausberg',
    check: (s) => s.meters >= 2600,
  },
  {
    id: 'no_skip_cycle', icon: '💎', name: 'Lückenlos',
    desc: 'Einen kompletten Zyklus ohne Auslassen',
    check: (s) => {
      const last7 = s.progress.slice(-7);
      return last7.length === 7 && last7.every((p) => !p.skipped);
    },
  },
  {
    id: 'outdoor5', icon: '🌄', name: 'Draußen unterwegs',
    desc: 'Fünf Ausdauer-Etappen im Freien',
    check: (s) => s.progress.filter((p) => !p.skipped && p.kind === STAGE_KIND.ENDURANCE).length >= 5,
  },
];

export function earnedBadges(snapshot) {
  return BADGES.filter((b) => {
    try { return b.check(snapshot); } catch { return false; }
  });
}

// ---------------------------------------------------------------------------
// Motivation
// ---------------------------------------------------------------------------

/**
 * Sprüche mit Bergbezug. Die Auswahl richtet sich nach dem Kalendertag, damit sie
 * über den Tag stabil bleibt und sich nicht bei jedem Antippen ändert.
 */
export const QUOTES = [
  { text: 'Berge erklimmt man nicht, indem man auf den Gipfel starrt, sondern indem man den nächsten Schritt macht.', by: null },
  { text: 'Es sind nicht die Berge, die wir bezwingen, sondern wir selbst.', by: 'Edmund Hillary' },
  { text: 'Der beste Zeitpunkt zum Anfangen war letzte Woche. Der zweitbeste ist jetzt.', by: null },
  { text: 'Ein Satz mehr als beim letzten Mal ist Fortschritt. Mehr braucht es heute nicht.', by: null },
  { text: 'Die Unterarme geben zuerst auf, nicht der Wille. Genau deshalb trainierst du sie.', by: null },
  { text: 'Wer sich Zeit zum Erholen nimmt, trainiert nicht weniger — er trainiert klüger.', by: null },
  { text: 'Kraft kommt nicht vom Draufhalten, sondern vom Wiederkommen.', by: null },
  { text: 'Nur wer sein Ziel kennt, findet den Weg.', by: 'Laotse' },
  { text: 'Der Gipfel ist optional. Zurückzukommen ist Pflicht.', by: 'Ed Viesturs' },
  { text: 'Heute ist nur eine Etappe. Aber ohne sie gibt es keine nächste.', by: null },
  { text: 'Dehnen fühlt sich nach nichts an — bis du merkst, wie weit dein Fuß plötzlich hochkommt.', by: null },
  { text: 'Man wächst nicht an der Last, sondern an der Regelmäßigkeit.', by: null },
  { text: 'Fünf Kilo mehr sind kein Zufall. Die hast du dir über Wochen geholt.', by: null },
  { text: 'Am Steig zählt nicht, wie stark du bist, sondern wie lange du stark bleibst.', by: null },
  { text: 'Das Schwierige an schwierigen Dingen ist meistens der Anfang.', by: null },
  { text: 'Berge lehren Geduld. Der Fels wartet, bis du bereit bist.', by: null },
  { text: 'Wer heute lockert, greift morgen fester zu.', by: null },
  { text: 'Es geht nicht darum, nie müde zu werden — sondern darum, trotzdem loszugehen.', by: null },
  { text: 'Jede Wiederholung, die du nicht machst, fehlt dir irgendwann an der Wand.', by: null },
  { text: 'Der Weg ist das Ziel, aber ein Gipfel schadet auch nicht.', by: null },
  { text: 'Konstanz schlägt Intensität. Immer.', by: null },
];

/** Spruch des Tages — stabil über den Kalendertag. */
export function quoteOfDay(now = Date.now()) {
  const dayNumber = Math.floor(now / 86400000);
  return QUOTES[dayNumber % QUOTES.length];
}

/** Kurzer Zuspruch nach abgeschlossener Etappe, passend zur Art. */
export function completionLine(kind) {
  const lines = {
    [STAGE_KIND.STRENGTH]: [
      'Sauber durchgezogen. Das zahlt direkt auf den Steig ein.',
      'Etappe geschafft — die Kraft holst du dir jetzt in der Erholung.',
      'Stark. Genau so wächst der Griff.',
    ],
    [STAGE_KIND.MOBILITY]: [
      'Gut gelockert. Morgen greifst du fester zu.',
      'Das ist die Arbeit, die man nicht sieht — aber am Fels spürt.',
      'Beweglichkeit ist Reichweite. Reichweite ist Sicherheit.',
    ],
    [STAGE_KIND.ENDURANCE]: [
      'Draußen war es besser als drinnen. Immer.',
      'Höhenmeter in den Beinen sind Höhenmeter im Konto.',
      'Genau die Grundlage, die dich am Zustieg trägt.',
    ],
    [STAGE_KIND.RECOVERY]: [
      'Runtergefahren. Der Körper baut jetzt auf.',
      'Erholung ist kein Nichtstun — sie ist der Teil, in dem du stärker wirst.',
      'Gut abgeschlossen. Morgen geht es frisch weiter.',
    ],
  };
  const pool = lines[kind] || lines[STAGE_KIND.STRENGTH];
  return pool[Math.floor(Math.random() * pool.length)];
}
