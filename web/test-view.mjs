/**
 * Rendert die Fels-Ansichten und prüft das erzeugte HTML.
 *
 * Die übrigen Prüfungen fassen nur die Regeln an. Diese hier baut die Oberfläche
 * tatsächlich auf — mit minimalen Attrappen für die Browser-Bausteine, die Node nicht
 * mitbringt. Sie fängt die Klasse Fehler, die keine Regelprüfung sieht: ein „undefined"
 * mitten im Text, eine leere Liste ohne Erklärung, ein Formular ohne Absenden-Sperre.
 *
 * Aufruf:  node web/test-view.mjs
 */
// Ein bereits eingerichtetes Profil mit zwei sauberen B-Begehungen — damit die
// Ansicht mit echten Daten läuft und nicht in die Ersteinrichtung springt.
const store = {
  'ferratafit.v1': JSON.stringify({
    profile: { onboarded: true, stations: ['LAT_PULLDOWN','PULLUP_BAR','BODYWEIGHT'],
               daysPerWeek: 3, bodyweightKg: 78, plateStepKg: 5, cycleStart: Date.now() },
    sessions: Array.from({ length: 9 }, (_, i) => ({
      id: 's' + i, dayId: ['A','B','C'][i % 3],
      startedAt: Date.now() - 86400000 * (i * 2 + 1),
      finishedAt: Date.now() - 86400000 * (i * 2 + 1) + 3300000,
      // Die Übungen, aus denen sich die Steig-Bereitschaft speist
      sets: [
        { exerciseId: 'deadhang', setIndex: 0, seconds: 45 },
        { exerciseId: 'pullup', setIndex: 0, reps: 6 },
        { exerciseId: 'hang_knee_raise', setIndex: 0, reps: 10 },
        { exerciseId: 'plank', setIndex: 0, seconds: 70 },
        { exerciseId: 'stepup', setIndex: 0, weightKg: 20, reps: 12 },
      ],
    })), hiddenExercises: [], progress: [], seenBadges: [], body: [],
    ascents: [
      { id:'a1', date: Date.now()-86400000*20, name:'Klettersteig Kellenegg', grade:'B',
        climbMeters:60, durationMin:60, feel:'GUT', flags:[], turnedBack:false },
      { id:'a2', date: Date.now()-86400000*8, name:'Via Örfla', grade:'B',
        climbMeters:110, durationMin:90, feel:'LOCKER', flags:['RUND'], turnedBack:false },
    ],
    plannedRouteIds: [],
  }),
};
globalThis.localStorage = {
  getItem: (k) => store[k] ?? null,
  setItem: (k, v) => { store[k] = v; },
  removeItem: (k) => { delete store[k]; },
};
const el = () => ({
  onclick: null, oninput: null, onchange: null, value: '', dataset: {},
  focus() {}, setSelectionRange() {}, remove() {}, closest: () => null, showModal() {}, close() {},
  querySelectorAll: () => [], querySelector: () => el(), appendChild() {},
  classList: { add() {}, remove() {} }, style: {},
});
globalThis.document = {
  body: { innerHTML: '', appendChild() {}, removeChild() {} },
  querySelectorAll: () => [], querySelector: () => el(),
  getElementById: () => null, createElement: el,
  addEventListener() {}, documentElement: el(),
};
globalThis.window = { addEventListener() {}, matchMedia: () => ({ matches: false }) };
// localStorage-Attrappe teilt sich das Ablageobjekt mit den Prüfungen unten
globalThis.store = store;
Object.defineProperty(globalThis, 'navigator', { value: { serviceWorker: { register: () => Promise.resolve() } }, configurable: true });
globalThis.confirm = () => true;

const app = await import('./app.js');
const html = document.body.innerHTML;

let bad = 0;
const ok = (name, cond, detail = '') => {
  if (!cond) bad++;
  console.log(`${cond ? '  ✓' : '  ✗'} ${name}${cond ? '' : '  ' + detail}`);
};

ok('App startet ohne Absturz', typeof html === 'string' && html.length > 500);
ok('Reiter „Am Fels" erscheint in der Navigation', html.includes('Am Fels'));

// Die Fels-Ansicht selbst rendern
const ferrata = app.__test.viewFerrata();
ok('Steigpass wird angezeigt', /Im Rahmen/.test(ferrata));
ok('Rang wird genannt', /Steigfinder|Drahtseilgeher|Talgänger/.test(ferrata));
ok('Routen erscheinen', (ferrata.match(/data-route=/g) || []).length > 5,
   `nur ${(ferrata.match(/data-route=/g) || []).length}`);
ok('nichts wird zwei Stufen über dem Bestätigten empfohlen',
   !/data-route[\s\S]{0,400}>[DEF]</.test(ferrata.split('Noch nicht dran')[0]));
ok('Hinweistext steht am Ende', /Entschieden wird am Einstieg/.test(ferrata));
ok('Gebietsfilter ist da', /data-region=/.test(ferrata));
ok('Begehungen werden aufgelistet', /Via Örfla/.test(ferrata));
ok('kein rohes undefined im HTML', !/undefined/.test(ferrata),
   (ferrata.match(/.{40}undefined.{40}/) || [''])[0]);

// Das Formular rendern
const form = app.__test.viewAscentForm({
  routeId: null, name: '', region: '', search: '', gradeIdx: 2,
  meters: '', minutes: '', feel: null, flags: [], turnedBack: false, partners: '', note: '',
});
ok('Formular fragt nach dem Gefühl', /Wie hat es sich angefühlt/.test(form));
ok('Umkehren steht gleichberechtigt da', /Umgekehrt/.test(form));
ok('Speichern ist ohne Angaben gesperrt', /disabled/.test(form));
ok('alle sechs Stufen zur Auswahl', (form.match(/data-grade=/g) || []).length === 6);
ok('kein rohes undefined im Formular', !/undefined/.test(form),
   (form.match(/.{40}undefined.{40}/) || [''])[0]);
// ------------------------------------------------------------------
// Angefangenes überlebt ein Neuladen
// ------------------------------------------------------------------
// Der Anlass: Wer während einer Einheit die Musik wechselt, findet den Tab beim
// Zurückkommen oft neu geladen vor. Ohne Zwischenspeicher wären alle Sätze weg.

const DRAFT_KEY = 'ferratafit.v1.draft';
const t = app.__test;

t.startWorkout('A');
ok('Einheit gestartet', t.active() !== null);
ok('Entwurf liegt sofort im Speicher', !!store[DRAFT_KEY]);

// Zwei Sätze eintragen und einen abhaken
const w = t.active();
w.entries[0].sets[0].reps = 9;
w.entries[0].sets[0].done = true;
w.entries[1].sets[0].weightKg = 47.5;
t.saveDraft();

const d = JSON.parse(store[DRAFT_KEY]);
ok('Wiederholungen sind gesichert', d.workout.entries[0].sets[0].reps === 9);
ok('Der Haken ist gesichert', d.workout.entries[0].sets[0].done === true);
ok('Das Gewicht ist gesichert', d.workout.entries[1].sets[0].weightKg === 47.5);
ok('Nur Kennungen, keine Übungstexte', !JSON.stringify(d).includes('Wiederholung'),
   'der Entwurf schleppt Katalogtext mit');
ok('Entwurf bleibt klein', store[DRAFT_KEY].length < 4000, `${store[DRAFT_KEY].length} Zeichen`);

// Neuladen simulieren: Zustand wegwerfen, aus dem Speicher wiederherstellen
t.reset();
ok('nach dem Neuladen erst einmal leer', t.active() === null);
t.restoreDraft(JSON.parse(store[DRAFT_KEY]));
const r = t.active();
ok('Einheit ist wieder da', r !== null);
ok('Wiederholungen sind wieder da', r && r.entries[0].sets[0].reps === 9);
ok('Der Haken ist wieder da', r && r.entries[0].sets[0].done === true);
ok('Das Gewicht ist wieder da', r && r.entries[1].sets[0].weightKg === 47.5);
ok('Der Startzeitpunkt bleibt', r && r.startedAt === w.startedAt);

// Abschließen räumt den Entwurf weg — sonst spränge er beim nächsten Start wieder auf
t.finishWorkout();
ok('nach dem Abschließen ist der Entwurf weg', !store[DRAFT_KEY]);

// Eine unbekannte Übung darf nicht die ganze Einheit kosten
t.reset();
const kaputt = { lastTouchedAt: Date.now(), workout: { dayId: 'A', startedAt: Date.now(),
  current: 0, entries: [{ exerciseId: 'gibtesnicht', sets: [{ reps: 5, done: true }] },
                        { exerciseId: 'pullup', sets: [{ reps: 7, done: true }] }] } };
t.restoreDraft(kaputt);
ok('unbekannte Übung wirft nicht die Einheit weg', t.active() !== null);
ok('die übrigen Sätze sind da', t.active() && t.active().entries[0].sets[0].reps === 7);

// Ein unbekannter Trainingstag darf nicht zum Absturz führen
t.reset();
t.restoreDraft({ lastTouchedAt: Date.now(), workout: { dayId: 'GIBTESNICHT', startedAt: 1,
  current: 0, entries: [{ exerciseId: 'pullup', sets: [{ reps: 5 }] }] } });
ok('unbekannter Trainingstag führt nicht in ein kaputtes Training', t.active() === null);

// ------------------------------------------------------------------
// Eine Begehung wirkt auf den Plan — durch den echten Code, nicht durch eine Kopie
// ------------------------------------------------------------------

const tourForm = (over = {}) => ({
  routeId: null, name: 'Testtour', region: '', search: '', gradeIdx: 3,
  meters: '380', minutes: '330', avgHr: '', feel: 'GUT', flags: [],
  turnedBack: false, partners: '', note: '', ...over,
});

// Die früheren Tests haben heute schon trainiert — für diesen Fall muss der Tag
// frei sein, sonst greift (korrekt) die Regel „nicht zweimal am selben Tag".
t.state().progress.forEach((p) => { p.at -= 86400000 * 3; });
t.state().sessions.forEach((sn) => { sn.startedAt -= 86400000 * 3; sn.finishedAt -= 86400000 * 3; });

const stagesBefore = t.state().progress.filter((p) => p.stageId !== 'F0').length;
t.saveAscent(tourForm());
const nachGross = t.state().progress;
ok('großer Bergtag hakt die offene Etappe ab',
  nachGross.filter((p) => p.stageId !== 'F0').length === stagesBefore + 1,
  JSON.stringify(nachGross.at(-1)));
ok('der Eintrag trägt die echte Etappenkennung, nicht F0',
  nachGross.at(-1).stageId !== 'F0');

// Ein Übungssteig am nächsten Tag: Höhenmeter ja, Etappe nein
t.state().ascents.at(-1).date -= 86400000 * 2;   // die große Tour altern lassen
t.state().progress.at(-1).at -= 86400000 * 2;
t.saveAscent(tourForm({ name: 'Übungssteig', gradeIdx: 1, meters: '60', minutes: '75' }));
const nachKlein = t.state().progress;
ok('Übungssteig bleibt außerhalb des Zyklus', nachKlein.at(-1).stageId === 'F0',
  JSON.stringify(nachKlein.at(-1)));
ok('seine Höhenmeter zählen trotzdem', nachKlein.at(-1).meters === 60);

// ------------------------------------------------------------------
// Die Reiter der Routenkarte
// ------------------------------------------------------------------
const karte = (tab) => t.routeCard('saulakopf', tab);
ok('Reiter Info zeigt die Beschreibung', /Ostwand/.test(karte(0)));
ok('Reiter Fotos zeigt Commons-Bilder mit Urheber und Lizenz',
  /upload\.wikimedia\.org/.test(karte(1)) && /CC BY-SA/.test(karte(1)) && /Wikimedia Commons/.test(karte(1)));
ok('Reiter Fotos verlinkt die Galerie des Portals', /bergsteigen\.com/.test(karte(1)));
ok('Reiter Eigene bietet das Hinzufügen an', /Foto hinzufügen/.test(karte(2)));
ok('Reiter Topo zeigt die Abschnitte von oben nach unten',
  /Ausstieg/.test(karte(3)) && /Einstieg/.test(karte(3)) && /Schlüsselstelle/.test(karte(3)));
ok('Reiter Topo markiert den Notausstieg', /Notausstieg/.test(karte(3)));
ok('Topo ist als schematisch gekennzeichnet', /Schematisch/.test(karte(3)));
ok('kein rohes undefined in den Reitern', ![0, 1, 2, 3].some((i) => /undefined/.test(karte(i))));

// Ohne Netzfreigabe: keine Bild-URL im HTML
t.state().profile.webPhotosEnabled = false;
ok('abgeschaltet lädt der Foto-Reiter nichts', !/upload\.wikimedia\.org/.test(karte(1)) && /ausgeschaltet/.test(karte(1)));
t.state().profile.webPhotosEnabled = true;

console.log(bad === 0 ? '\nRauchtest bestanden.\n' : `\n${bad} Problem(e).\n`);
process.exit(bad ? 1 : 0);
