/**
 * FerrataFit Web — Oberfläche und Zustandsverwaltung.
 *
 * Bewusst ohne Framework: Der Zustand ist ein einziges Objekt, jede Änderung schreibt
 * ihn in den localStorage und zeichnet den aktiven Bereich neu. Bei dieser Größe ist
 * das übersichtlicher als eine Bibliothek und läuft ohne Build-Schritt.
 */

import * as D from './data.js';
import * as J from './journey.js';
import { parseBodyFile, mergeBody } from './bodyimport.js';
import * as FE from './ferrata.js';
import { FERRATAS, ferrataById, ferrataRegions } from './ferratas.js';
import { GEO_BOUNDS, GEO_POINTS, GEO_OUTLINE, GEO_LANDMARKS } from './ferrageo.js';
import { photosFor, topoFor, GALLERIES, TOPO_URLS } from './ferramedia.js';
import * as PhotoDb from './photodb.js';

const STORAGE_KEY = 'ferratafit.v1';
const APP_VERSION = '1.12';

const DEFAULT_STATE = {
  profile: {
    stations: ['LAT_PULLDOWN', 'CHEST_PRESS', 'BUTTERFLY', 'LEG_EXTENSION', 'LEG_CURL', 'PULLUP_BAR', 'BODYWEIGHT'],
    daysPerWeek: 3,
    bodyweightKg: 78,
    plateStepKg: 5,
    cycleStart: 0,
    targetFerrataDate: null,
    targetFerrataName: '',
    onboarded: false,
    /** Frei lizenzierte Fotos von Wikimedia Commons nachladen — abschaltbar. */
    webPhotosEnabled: true,
  },
  sessions: [],
  hiddenExercises: [],
  /** Abgeschlossene Etappen in der Reihenfolge, in der sie gegangen wurden. */
  progress: [],
  /** Bereits vergebene Abzeichen — gemerkt, damit neue erkennbar bleiben. */
  seenBadges: [],
  /** Messungen von der Waage. Im Browser von Hand eingetragen. */
  body: [],
  heightCm: null,
  /** Echte Begehungen am Fels. */
  ascents: [],
  /** Routen, die als Ziel vorgemerkt wurden. */
  plannedRouteIds: [],
};

let state = load();
let tab = 'home';
let planOpen = null;
let active = null;      // laufende Krafteinheit
let activeStage = null; // laufende Mobility-/Ausdauer-Etappe
let restTimer = null;
let trendPick = null;
let ferrataRegion = null;   // Gebietsfilter in der Missionsübersicht
let ferrataMap = false;     // Karte statt Liste
let routeTab = 0;           // Reiter in der aufgeklappten Routenkarte: 0 Info, 1 Fotos, 2 Eigene, 3 Topo
const ownPhotoCache = {};   // routeId -> [{id, url}] aus IndexedDB, für die synchrone Anzeige
let ferrataOpen = null;     // aufgeklappte Route
let showTooEarly = false;
let ascentForm = null;      // offenes Eintragsformular

// ---------------------------------------------------------------------------
// Zustand
// ---------------------------------------------------------------------------

function load() {
  try {
    const raw = localStorage.getItem(STORAGE_KEY);
    if (!raw) return structuredClone(DEFAULT_STATE);
    const parsed = JSON.parse(raw);
    // Fehlende Felder ergänzen, damit ältere Sicherungen weiter funktionieren.
    return {
      ...structuredClone(DEFAULT_STATE),
      ...parsed,
      profile: { ...DEFAULT_STATE.profile, ...(parsed.profile || {}) },
    };
  } catch {
    return structuredClone(DEFAULT_STATE);
  }
}

function save() {
  try {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(state));
  } catch {
    toast('Speichern fehlgeschlagen — ist der Speicher des Browsers voll?', true);
  }
}

function update(fn) {
  fn(state);
  save();
  render();
}

// ---------------------------------------------------------------------------
// Angefangenes
// ---------------------------------------------------------------------------

/**
 * Der laufende Zustand liegt in eigenen Variablen und wäre nach einem Neuladen weg.
 *
 * Auf dem Handy passiert das schnell: Wer während einer Einheit die Musik wechselt oder
 * einen Anruf annimmt, findet den Tab beim Zurückkommen oft neu geladen vor — und ohne
 * diesen Zwischenspeicher wären alle eingetippten Sätze verloren. Dieselbe Lösung wie in
 * der Android-App, nur mit localStorage statt einer Datei.
 *
 * Gespeichert wird ausschließlich Eingetipptes plus Kennungen; der Vorschlag wird beim
 * Wiederaufnehmen aus dem ursprünglichen Startzeitpunkt neu gerechnet.
 */
const DRAFT_KEY = STORAGE_KEY + '.draft';
const RESUME_WINDOW_H = 6;
const EXPIRY_H = 72;
const MAX_SESSION_MIN = 240;

function saveDraft() {
  try {
    if (!active && !activeStage) { localStorage.removeItem(DRAFT_KEY); return; }
    const now = Date.now();
    localStorage.setItem(DRAFT_KEY, JSON.stringify({
      lastTouchedAt: now,
      workout: active ? {
        dayId: active.dayId,
        startedAt: active.startedAt,
        stageId: active.stageId || null,
        current: active.current,
        entries: active.entries.map((e) => ({ exerciseId: e.sug.exercise.id, sets: e.sets })),
        restEndsAt, restTotal, restPausedWith,
      } : null,
      stage: activeStage ? {
        stageId: activeStage.stage.id,
        startedAt: activeStage.startedAt || now,
        minutes: activeStage.minutes,
        meters: activeStage.meters,
        doneDrills: (activeStage.items || []).filter((i) => i.done).map((i) => i.id),
      } : null,
    }));
  } catch {
    // Voller Speicher darf das Training nicht anhalten
  }
}

function loadDraft() {
  try {
    const raw = localStorage.getItem(DRAFT_KEY);
    return raw ? JSON.parse(raw) : null;
  } catch {
    return null;
  }
}

const draftAgeH = (d) => (Date.now() - (d.lastTouchedAt || 0)) / 3600000;

/** Baut die angefangene Einheit wieder auf. Gibt zurück, ob etwas wiederhergestellt wurde. */
function restoreDraft(d) {
  let ok = false;
  if (d.workout && D.dayById(d.workout.dayId)) {
    const w = d.workout;
    let verloren = 0;
    const entries = w.entries.map((de) => {
      const ex = D.byId(de.exerciseId);
      if (!ex) { verloren++; return null; }
      // Mit dem ursprünglichen Startzeitpunkt rechnen, nicht mit jetzt — sonst stünde
      // nach einer Unterbrechung über Mitternacht eine andere Empfehlung da.
      return { sug: D.suggest(ex, state.sessions, state.profile, w.startedAt,
        FE.recoveryState(state.ascents, w.startedAt)), sets: de.sets };
    }).filter(Boolean);

    if (entries.length) {
      active = {
        dayId: w.dayId, startedAt: w.startedAt, entries,
        current: Math.min(w.current || 0, entries.length - 1),
        stageId: w.stageId || null,
      };
      restEndsAt = w.restEndsAt || 0;
      restTotal = w.restTotal || 0;
      restPausedWith = w.restPausedWith ?? null;
      if (remainingRest() > 0) startRest(remainingRest());
      ok = true;
      if (verloren) {
        toast(`${verloren} ${verloren === 1 ? 'Übung gibt' : 'Übungen gibt'} es nicht mehr — der Rest ist wieder da.`);
      }
    }
  }
  if (d.stage) {
    // Die eingeschobene Erholung steht nicht im Etappenkatalog — sie wird aus dem
    // Erholungszustand neu gebaut.
    const stage = d.stage.stageId === FE.EXTRA_STAGE_ID
      ? {
          id: FE.EXTRA_STAGE_ID, kind: J.STAGE_KIND.RECOVERY,
          title: 'Erholung',
          subtitle: `Nach: ${FE.recoveryState(state.ascents, d.stage.startedAt)?.sourceName || 'der Tour'}`,
          icon: '🛌', meters: 30, longHold: true,
          mobilityIds: ['forearm_flexor', 'forearm_extensor', 'child_pose', 'pigeon', 'calf_wall', 'neck'],
        }
      : J.stageById(d.stage.stageId);
    if (stage) {
      activeStage = {
        stage,
        startedAt: d.stage.startedAt,
        minutes: d.stage.minutes ?? 30,
        meters: d.stage.meters ?? 0,
        items: stage.mobilityIds.map((id) => ({ id, done: (d.stage.doneDrills || []).includes(id) })),
      };
      ok = true;
    }
  }
  if (!ok) localStorage.removeItem(DRAFT_KEY);
  return ok;
}

// ---------------------------------------------------------------------------
// Hilfsfunktionen
// ---------------------------------------------------------------------------

const $ = (sel) => document.querySelector(sel);
const esc = (s) => String(s).replace(/[&<>"']/g, (c) =>
  ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c]));

function toast(msg, isError = false) {
  const old = $('#toast');
  if (old) old.remove();
  const el = document.createElement('div');
  el.id = 'toast';
  if (isError) el.className = 'err';
  el.textContent = msg;
  document.body.appendChild(el);
  setTimeout(() => el.remove(), 3800);
}

const DAY_MS = 86400000;

/** Berg als Inline-SVG statt Emoji — nicht jede Schriftart bringt 🏔 mit. */
const MOUNTAIN = `<svg viewBox="0 0 24 24" width="17" height="17" style="vertical-align:-3px">
  <path d="M12 4 L20 18 H4 Z" fill="currentColor" opacity=".55"/>
  <path d="M9 8 L16 20 H2 Z" fill="currentColor"/>
</svg>`;

function relativeDay(then, now) {
  const days = Math.floor((now - then) / DAY_MS);
  if (days <= 0) return 'heute';
  if (days === 1) return 'gestern';
  if (days < 7) return `vor ${days} Tagen`;
  if (days < 14) return 'letzte Woche';
  return `vor ${Math.floor(days / 7)} Wochen`;
}

const dateShort = (ms) =>
  new Date(ms).toLocaleDateString('de-AT', { day: 'numeric', month: 'short' });

function greeting() {
  const h = new Date().getHours();
  if (h < 5) return 'Noch wach?';
  if (h < 11) return 'Guten Morgen';
  if (h < 14) return 'Mahlzeit';
  if (h < 18) return 'Guten Nachmittag';
  return 'Guten Abend';
}

const estimateMinutes = (exs) =>
  (Math.floor(exs.reduce((s, e) => s + e.sets * (e.restSec + 40), 0) / 60 / 5) + 1) * 5;

/**
 * Momentaufnahme für die Abzeichenprüfung — bündelt alles, was ein Abzeichen
 * abfragen könnte, an einer Stelle.
 */
function badgeSnapshot() {
  const now = Date.now();
  const increases = D.EXERCISES.reduce((n, ex) => {
    const loads = state.sessions
      .filter((s) => s.sets.some((x) => x.exerciseId === ex.id))
      .sort((a, b) => a.startedAt - b.startedAt)
      .map((s) => Math.max(...s.sets.filter((x) => x.exerciseId === ex.id).map((x) => x.weightKg || 0)));
    return n + loads.filter((v, i) => i > 0 && v > loads[i - 1]).length;
  }, 0);

  return {
    progress: state.progress,
    sessions: state.sessions,
    meters: J.totalMeters(state.progress),
    weeklyStreak: D.weeklyStreak(state.sessions, now),
    increases,
    best: {
      deadhang: D.bestOf(state.sessions, 'deadhang', 'seconds'),
      pullup: D.bestOf(state.sessions, 'pullup', 'reps'),
      plank: D.bestOf(state.sessions, 'plank', 'seconds'),
    },
  };
}

/**
 * Etappe abschließen: Höhenmeter gutschreiben, neue Abzeichen melden.
 * `skipped` verbucht sie als gegangen, aber ohne Gutschrift.
 */
function completeStage(stage, { skipped = false, detail = '' } = {}) {
  const before = J.earnedBadges(badgeSnapshot()).map((b) => b.id);

  update((s) => {
    s.progress.push({
      stageId: stage.id,
      kind: stage.kind,
      meters: skipped ? 0 : stage.meters,
      at: Date.now(),
      skipped,
      detail,
    });
  });

  const after = J.earnedBadges(badgeSnapshot());
  const fresh = after.filter((b) => !before.includes(b.id));
  if (fresh.length) {
    update((s) => { s.seenBadges = after.map((b) => b.id); });
    showBadgeDialog(fresh);
  } else if (!skipped) {
    toast(`+${stage.meters} Hm · ${J.completionLine(stage.kind)}`);
  } else {
    toast('Etappe übersprungen — die nächste ist frei.');
  }
}

/** Feierliche Meldung, wenn ein Abzeichen dazukommt. */
function showBadgeDialog(badges) {
  const dlg = document.createElement('dialog');
  dlg.innerHTML = `
    <div style="text-align:center">
      <div style="font-size:44px;line-height:1">${badges.map((b) => b.icon).join(' ')}</div>
      <h2 style="margin:12px 0 4px">${badges.length > 1 ? 'Neue Abzeichen!' : 'Neues Abzeichen!'}</h2>
      ${badges.map((b) => `
        <div style="margin:14px 0">
          <div style="font-weight:700;font-size:17px;color:var(--amber)">${esc(b.name)}</div>
          <div class="muted" style="font-size:13px">${esc(b.desc)}</div>
        </div>`).join('')}
      <button class="btn primary" style="margin-top:10px" id="badge-ok">Weiter</button>
    </div>`;
  document.body.appendChild(dlg);
  dlg.showModal();
  dlg.querySelector('#badge-ok').onclick = () => { dlg.close(); dlg.remove(); };
}

const adviceClass = (a) => ({
  INCREASE: 'amber', DELOAD: 'emerald', BACKOFF: 'rose', START: 'violet', HOLD: 'sky',
}[a] || 'sky');

const adviceLabel = (a) => ({
  INCREASE: 'Steigern', DELOAD: 'Entlastung', BACKOFF: 'Zurück', START: 'Einstieg', HOLD: 'Halten',
}[a] || '');

const adviceColor = (a) => ({
  INCREASE: 'var(--amber)', DELOAD: 'var(--emerald)', BACKOFF: 'var(--rose)',
  START: 'var(--violet)', HOLD: 'var(--sky)',
}[a] || 'var(--sky)');

/**
 * Die vollständige Anleitung zu einer Übung — Aufbau, Ablauf, typische Fehler
 * und ein Verweis auf ein Video. Wird im Training wie in den Dehn-Etappen genutzt.
 *
 * `item` braucht mindestens `why`; alles andere ist optional, damit auch knapp
 * beschriebene Einträge sauber gerendert werden.
 */
function guideHtml(item, opts = {}) {
  const parts = [];

  if (item.setup) {
    parts.push(`<div class="info-block"><div class="h">Aufbau</div>
      <div class="b">${esc(item.setup)}</div></div>`);
  }

  if (item.steps?.length) {
    parts.push(`<div class="info-block"><div class="h">Ablauf</div>
      <ol class="guide-steps">${item.steps.map((s) => `<li>${esc(s)}</li>`).join('')}</ol></div>`);
  } else if (item.cue) {
    parts.push(`<div class="info-block"><div class="h">So geht's</div>
      <div class="b">${esc(item.cue)}</div></div>`);
  }

  if (item.mistakes?.length) {
    parts.push(`<div class="info-block warn"><div class="h">Achte darauf</div>
      <ul class="guide-list">${item.mistakes.map((s) => `<li>${esc(s)}</li>`).join('')}</ul></div>`);
  }

  if (item.counting) {
    parts.push(`<div class="info-block"><div class="h">Zählweise</div>
      <div class="b">${esc(item.counting)}</div></div>`);
  }

  if (item.variant) {
    parts.push(`<div class="info-block"><div class="h">Leichter oder schwerer</div>
      <div class="b">${esc(item.variant)}</div></div>`);
  }

  if (item.why && opts.showWhy !== false) {
    parts.push(`<div class="info-block"><div class="h">${opts.whyLabel || 'Warum am Steig'}</div>
      <div class="b">${esc(item.why)}</div></div>`);
  }

  if (item.video) {
    const url = `https://www.youtube.com/results?search_query=${encodeURIComponent(item.video)}`;
    parts.push(`<a class="video-link" href="${url}" target="_blank" rel="noopener noreferrer">
      <span class="play">▶</span>
      <span class="txt"><b>Video ansehen</b><br><span class="dim">Öffnet YouTube · „${esc(item.video)}"</span></span>
    </a>`);
  }

  return parts.join('');
}

/** Fortschrittsring als SVG — 270° Bogen, wie in der Android-Variante. */
function ringSvg(progress, color = 'var(--sky)') {
  const r = 50, c = 2 * Math.PI * r, sweep = c * 0.75;
  const filled = sweep * Math.max(0, Math.min(1, progress));
  return `<svg viewBox="0 0 116 116" width="116" height="116">
    <circle cx="58" cy="58" r="${r}" fill="none" stroke="var(--outline)" stroke-width="11"
      stroke-linecap="round" stroke-dasharray="${sweep} ${c}"/>
    <circle cx="58" cy="58" r="${r}" fill="none" stroke="${color}" stroke-width="11"
      stroke-linecap="round" stroke-dasharray="${filled} ${c}"
      style="transition:stroke-dasharray .9s ease"/>
  </svg>`;
}

/** Verlaufskurve mit angedeuteter Fläche darunter. */
function chartSvg(points, color = 'var(--sky)') {
  if (points.length < 2) {
    return `<div class="empty">Noch zu wenig Daten — ab der zweiten Einheit erscheint hier die Kurve.</div>`;
  }
  const W = 300, H = 120, pad = 14;
  const vals = points.map((p) => p.value);
  const min = Math.min(...vals), max = Math.max(...vals);
  const span = max - min > 0.01 ? max - min : 1;
  const stepX = W / (points.length - 1);
  const y = (v) => H - pad - ((v - min) / span) * (H - 2 * pad);

  const line = vals.map((v, i) => `${i === 0 ? 'M' : 'L'}${(i * stepX).toFixed(1)},${y(v).toFixed(1)}`).join(' ');
  const area = `M0,${H} ` + vals.map((v, i) => `L${(i * stepX).toFixed(1)},${y(v).toFixed(1)}`).join(' ') + ` L${W},${H} Z`;
  const dots = vals.map((v, i) =>
    `<circle cx="${(i * stepX).toFixed(1)}" cy="${y(v).toFixed(1)}" r="4" fill="${color}"/>
     <circle cx="${(i * stepX).toFixed(1)}" cy="${y(v).toFixed(1)}" r="1.8" fill="var(--ink)"/>`).join('');

  return `<svg class="chart" viewBox="0 0 ${W} ${H}" preserveAspectRatio="none">
    <defs><linearGradient id="fill" x1="0" y1="0" x2="0" y2="1">
      <stop offset="0" stop-color="${color}" stop-opacity=".28"/>
      <stop offset="1" stop-color="${color}" stop-opacity="0"/>
    </linearGradient></defs>
    <path d="${area}" fill="url(#fill)"/>
    <path d="${line}" fill="none" stroke="${color}" stroke-width="3" stroke-linecap="round" stroke-linejoin="round"/>
    ${dots}
  </svg>`;
}

// ---------------------------------------------------------------------------
// Onboarding
// ---------------------------------------------------------------------------

let obStep = 0;
let obDraft = {
  stations: ['LAT_PULLDOWN', 'CHEST_PRESS', 'BUTTERFLY', 'LEG_EXTENSION', 'LEG_CURL', 'PULLUP_BAR'],
  bodyweight: '78', plateStep: '5', targetName: '', weeks: 12, hasTarget: true,
};

function renderOnboarding() {
  const stationRows = Object.entries(D.STATIONS)
    .filter(([k]) => k !== 'BODYWEIGHT')
    .map(([k, v]) => `
      <div class="check ${obDraft.stations.includes(k) ? 'on' : ''}" data-station="${k}">
        <div class="box">${obDraft.stations.includes(k) ? '✓' : ''}</div>
        <div class="grow"><div class="t">${esc(v.label)}</div><div class="h">${esc(v.hint)}</div></div>
      </div>`).join('');

  const body = [
    `<h1>Was steht bei dir?</h1>
     <p class="sub">Hak ab, was dein Multifunktionsgerät hat. Die App plant nur Übungen ein,
       die du damit auch wirklich ausführen kannst.</p>
     ${stationRows}`,

    `<h1>Zwei Zahlen</h1>
     <p class="sub">Aus dem Körpergewicht schätzt die App deine Startlasten. Die Gewichtsstufe
       ist der Sprung von einer Steckplatte zur nächsten — an den meisten Kraftstationen sind das 5 kg.</p>
     <div class="field"><label>Körpergewicht in kg</label>
       <input type="number" inputmode="decimal" id="ob-bw" value="${obDraft.bodyweight}"></div>
     <div class="field"><label>Gewichtsstufe je Platte in kg</label>
       <input type="number" inputmode="decimal" id="ob-step" value="${obDraft.plateStep}"></div>
     <p class="dim" style="font-size:12.5px">Steht auf den Platten meist aufgedruckt. Wenn nicht: 5 kg ist ein guter Richtwert.</p>`,

    `<h1>Dein Ziel</h1>
     <p class="sub">Ein konkretes Datum macht den Unterschied. Für eine anspruchsvollere Tour
       gelten 10 bis 12 Wochen Vorlauf als sinnvoll.</p>
     <div class="check ${obDraft.hasTarget ? 'on' : ''}" data-target-toggle>
       <div class="box">${obDraft.hasTarget ? '✓' : ''}</div>
       <div class="grow"><div class="t">Ich habe eine Tour im Blick</div></div>
     </div>
     ${obDraft.hasTarget ? `
       <div class="field" style="margin-top:14px"><label>Welcher Klettersteig?</label>
         <input id="ob-name" placeholder="z. B. Känzele oder Rüfikopf" value="${esc(obDraft.targetName)}"></div>
       <div style="font-size:19px;font-weight:700;color:var(--amber);margin:18px 0 6px">
         In ${obDraft.weeks} Wochen</div>
       <input type="range" min="4" max="40" value="${obDraft.weeks}" id="ob-weeks">
       <p class="dim" style="font-size:12.5px">${
         obDraft.weeks < 8 ? 'Knapp, aber machbar — bleib konsequent bei drei Einheiten.'
         : obDraft.weeks <= 14 ? 'Guter Rahmen. Genau dafür ist der Plan gebaut.'
         : 'Viel Zeit — du wirst mehrere Steigerungsblöcke durchlaufen.'}</p>` : ''}`,
  ][obStep];

  document.body.innerHTML = `
    <div id="onboarding"><div class="wrap">
      <div class="row">
        <div class="logo" style="color:#0b1220">${MOUNTAIN.replace('width="17" height="17"', 'width="24" height="24"')}</div>
        <div><div style="font-size:23px;font-weight:700">FerrataFit</div>
          <div style="font-size:12.5px;color:var(--sky)">Kraft für den Klettersteig</div></div>
      </div>
      <div class="steps">${[0, 1, 2].map((i) => `<i class="${i <= obStep ? 'on' : ''}"></i>`).join('')}</div>
      ${body}
      <div class="btn-row" style="margin-top:28px">
        ${obStep > 0 ? '<button class="btn ghost" id="ob-back">Zurück</button>' : ''}
        <button class="btn primary" id="ob-next" ${obStep === 0 && !obDraft.stations.length ? 'disabled' : ''}>
          ${obStep < 2 ? 'Weiter' : "Los geht's"}</button>
      </div>
    </div></div>`;

  document.querySelectorAll('[data-station]').forEach((el) => {
    el.onclick = () => {
      const k = el.dataset.station;
      obDraft.stations = obDraft.stations.includes(k)
        ? obDraft.stations.filter((x) => x !== k) : [...obDraft.stations, k];
      renderOnboarding();
    };
  });

  const toggle = $('[data-target-toggle]');
  if (toggle) toggle.onclick = () => { obDraft.hasTarget = !obDraft.hasTarget; renderOnboarding(); };

  const weeks = $('#ob-weeks');
  if (weeks) weeks.oninput = (e) => {
    obDraft.weeks = +e.target.value;
    obDraft.targetName = $('#ob-name')?.value ?? obDraft.targetName;
    renderOnboarding();
  };

  const back = $('#ob-back');
  if (back) back.onclick = () => { captureOnboarding(); obStep--; renderOnboarding(); };

  $('#ob-next').onclick = () => {
    captureOnboarding();
    if (obStep < 2) { obStep++; renderOnboarding(); return; }

    const target = obDraft.hasTarget ? Date.now() + obDraft.weeks * 7 * DAY_MS : null;
    state.profile = {
      ...state.profile,
      stations: [...new Set([...obDraft.stations, 'BODYWEIGHT'])],
      bodyweightKg: parseFloat(obDraft.bodyweight) || 78,
      plateStepKg: parseFloat(obDraft.plateStep) || 5,
      targetFerrataDate: target,
      targetFerrataName: obDraft.targetName.trim(),
      cycleStart: Date.now(),
      onboarded: true,
    };
    save();
    boot();
  };
}

function captureOnboarding() {
  const bw = $('#ob-bw'); if (bw) obDraft.bodyweight = bw.value;
  const st = $('#ob-step'); if (st) obDraft.plateStep = st.value;
  const nm = $('#ob-name'); if (nm) obDraft.targetName = nm.value;
}

// ---------------------------------------------------------------------------
// Bereich: Heute
// ---------------------------------------------------------------------------

function viewHome() {
  const now = Date.now();
  const p = state.profile;
  const stage = J.currentStage(state.progress);
  const idx = J.currentStageIndex(state.progress);
  const meters = J.totalMeters(state.progress);
  const summit = J.summitProgress(meters);
  const quote = J.quoteOfDay(now);
  const week = D.weekInCycle(p, now);
  const deload = D.isDeloadWeek(week);
  const readiness = D.ferrataReadiness(state.sessions, now);
  const streak = D.weeklyStreak(state.sessions, now);
  const ringColor = readiness >= 65 ? 'var(--emerald)' : readiness >= 35 ? 'var(--sky)' : 'var(--amber)';
  const recovery = FE.recoveryState(state.ascents, now);
  // Nach einer großen Tour wird die Krafteinheit aufgeschoben — der Plan passt sich der
  // Tour an, nicht umgekehrt. Leichte Etappen bleiben: Genau die sind jetzt richtig.
  const deferStrength = recovery && recovery.level.id === 'ERHOLUNG'
    && stage.kind === J.STAGE_KIND.STRENGTH;

  // Kraftetappen zeigen direkt, was heute aufgelastet wird.
  let upsHtml = '';
  let statsHtml = '';
  if (stage.kind === J.STAGE_KIND.STRENGTH) {
    const day = J.dayForStage(stage);
    const exs = D.exercisesFor(day, p, state.hiddenExercises);
    const ups = exs.map((e) => D.suggest(e, state.sessions, p, now, FE.recoveryState(state.ascents, now)))
      .filter((s) => s.advice === D.ADVICE.INCREASE);
    statsHtml = `<div class="row" style="gap:22px;margin-top:14px">
      <div><div style="font-size:17px;font-weight:600">${exs.length}</div><div class="dim" style="font-size:10.5px">Übungen</div></div>
      <div><div style="font-size:17px;font-weight:600">${exs.reduce((s, e) => s + e.sets, 0)}</div><div class="dim" style="font-size:10.5px">Sätze</div></div>
      <div><div style="font-size:17px;font-weight:600">~${estimateMinutes(exs)}</div><div class="dim" style="font-size:10.5px">Minuten</div></div>
    </div>`;
    upsHtml = ups.length ? `
      <div style="color:var(--amber);font-weight:600;font-size:15px;margin-bottom:8px">
        ↑ ${ups.length === 1 ? 'Heute wird aufgelastet' : `Heute wird ${ups.length}× aufgelastet`}</div>
      ${ups.slice(0, 3).map((s) => `
        <div class="row" style="padding:3px 0">
          <span class="grow muted" style="font-size:13.5px">${esc(s.exercise.name)}</span>
          ${s.previousHeadline ? `<span class="dim" style="font-size:12px">${esc(s.previousHeadline)} →</span>` : ''}
          <b style="color:var(--amber)">${esc(s.headline)}</b>
        </div>`).join('')}
      <div style="height:14px"></div>` : '';
  } else {
    const mins = stage.kind === J.STAGE_KIND.ENDURANCE ? 30
      : Math.round(J.STAGES.find((s) => s.id === stage.id).mobilityIds
          .reduce((sum, id) => sum + (J.mobilityById(id).seconds * (J.mobilityById(id).perSide ? 2 : 1) + 12), 0) / 60);
    const count = stage.mobilityIds ? stage.mobilityIds.length : 1;
    statsHtml = `<div class="row" style="gap:22px;margin-top:14px">
      <div><div style="font-size:17px;font-weight:600">${count}</div><div class="dim" style="font-size:10.5px">${stage.kind === J.STAGE_KIND.ENDURANCE ? 'Aufgabe' : 'Übungen'}</div></div>
      <div><div style="font-size:17px;font-weight:600">~${mins}</div><div class="dim" style="font-size:10.5px">Minuten</div></div>
      <div><div style="font-size:17px;font-weight:600">+${stage.meters}</div><div class="dim" style="font-size:10.5px">Höhenmeter</div></div>
    </div>`;
  }

  return `
  <div class="screen">
    <p class="sub" style="margin:0">${greeting()} · Etappe ${idx + 1}</p>
    <h1>${esc(stage.title)}</h1>

    <div class="quote">
      <div class="q">„${esc(quote.text)}"</div>
      ${quote.by ? `<div class="by">— ${esc(quote.by)}</div>` : ''}
    </div>

    <!-- Höhenmeter-Konto -->
    <div class="card accent-amber">
      <div class="row between">
        <div>
          <div class="dim" style="font-size:10.5px;text-transform:uppercase;letter-spacing:.7px">Gesammelte Höhenmeter</div>
          <div style="font-size:32px;font-weight:700;letter-spacing:-1px;color:var(--amber)">${meters.toLocaleString('de-AT')} <span style="font-size:17px">Hm</span></div>
        </div>
        <div style="text-align:right">
          <div class="dim" style="font-size:10.5px">Nächstes Ziel</div>
          <div style="font-weight:600">${esc(summit.next.name)}</div>
          <div class="dim" style="font-size:11.5px">noch ${summit.toGo.toLocaleString('de-AT')} Hm</div>
        </div>
      </div>
      <div class="bar amber" style="margin-top:12px"><i style="width:${summit.progress * 100}%"></i></div>
      <div class="dim" style="font-size:11.5px;margin-top:7px">${esc(summit.next.note)}</div>
    </div>

    <!-- Aktuelle Etappe -->
    <div class="card flush accent-sky">
      <div class="hero">
        <div class="row">
          <div class="badge" style="font-size:20px">${stage.icon}</div>
          <div class="grow">
            <div style="font-size:19px;font-weight:600">${esc(stage.title)}</div>
            <div style="font-size:12.5px;color:var(--sky)">${esc(stage.subtitle)}</div>
          </div>
          <span class="pill sky">Zyklus ${J.cycleNumber(state.progress)}</span>
        </div>
        ${statsHtml}
      </div>
      <div style="padding:18px">
        ${deferStrength ? `
          <div class="pill violet" style="margin-bottom:10px">${esc(FE.loadLabel(recovery.score))} — ${esc(recovery.sourceName)}</div>
          <p class="muted" style="margin:0 0 14px;font-size:13.5px">Die Tour steckt dir noch in den
            Armen. Die App schiebt „${esc(stage.title)}" auf — noch etwa
            ${FE.recoveryHoursLeft(recovery, now)} Stunden. Heute ist Lockern das bessere Training.</p>
          <button class="btn primary" style="background:var(--violet)" data-recovery-break>Erholung starten</button>
          <button class="btn ghost small" style="margin-top:8px" data-stage-start="${stage.id}">
            Trotzdem trainieren — die Vorschläge bleiben gesenkt</button>
        ` : `
        ${recovery && recovery.level.id === 'ANGESCHLAGEN' && stage.kind === J.STAGE_KIND.STRENGTH
          ? `<div class="pill violet" style="margin-bottom:10px">Noch leicht angeschlagen — Vorschläge um 10 % gesenkt</div>` : ''}
        ${stage.kind === J.STAGE_KIND.STRENGTH && deload
          ? `<div class="pill emerald" style="margin-bottom:10px">Entlastungswoche — bewusst leichter</div>` : ''}
        ${upsHtml}
        ${stage.hint ? `<p class="muted" style="margin:0 0 14px;font-size:13.5px">${esc(stage.hint)}</p>` : ''}
        <button class="btn primary" data-stage-start="${stage.id}">▶ Etappe starten</button>`}
        <div class="row" style="gap:8px;margin-top:8px">
          ${stage.kind === J.STAGE_KIND.STRENGTH
            ? `<button class="link" data-goto-plan="${stage.dayId}">Plan ansehen</button>` : ''}
          <button class="link" data-stage-skip="${stage.id}">Überspringen</button>
        </div>
      </div>
    </div>

    <!-- Der Steig: kommende Etappen -->
    <div class="section-title"><span>Der Steig</span><span>Etappe ${idx + 1}</span></div>
    <div class="card" style="padding:14px">
      ${[0, 1, 2, 3].map((offset) => {
        const s = J.stageAt(idx + offset);
        const locked = offset > 0;
        return `<div class="path-row ${locked ? 'locked' : 'open'} ${offset === 3 ? 'last' : ''}">
          <div class="dot">${locked ? '🔒' : s.icon}</div>
          <div class="grow">
            <div class="t">${esc(s.title)}</div>
            <div class="m">${locked ? 'Wird frei, wenn die vorige Etappe steht' : esc(s.subtitle)}</div>
          </div>
          <span class="hm">+${s.meters}</span>
        </div>`;
      }).join('')}
    </div>

    <!-- Bereitschaft -->
    <div class="card">
      <div class="row" style="gap:16px">
        <div class="ring">
          ${ringSvg(readiness / 100, ringColor)}
          <div class="center"><div><div class="v">${readiness}</div><div class="l">von 100</div></div></div>
        </div>
        <div class="grow">
          <h2 style="font-size:16px">${MOUNTAIN} Steig-Bereitschaft</h2>
          <p class="muted" style="font-size:13px;margin:0">${esc(D.readinessLabel(readiness))}</p>
          ${p.targetFerrataDate && p.targetFerrataDate > now ? `
            <div style="margin-top:10px"><span class="pill amber">${
              p.targetFerrataName ? esc(p.targetFerrataName) + ' · ' : 'noch '
            }${Math.ceil((p.targetFerrataDate - now) / DAY_MS)} Tage</span></div>` : ''}
        </div>
      </div>
    </div>

    <div class="tiles">
      <div class="tile"><div class="val">${state.progress.filter((x) => !x.skipped).length}</div><div class="lbl">Etappen gegangen</div></div>
      <div class="tile"><div class="val">${streak}</div><div class="lbl">${streak === 1 ? 'Woche' : 'Wochen'} in Folge</div></div>
      <div class="tile"><div class="val">${J.earnedBadges(badgeSnapshot()).length}</div><div class="lbl">Abzeichen</div></div>
    </div>

    ${state.progress.length ? `
      <div class="section-title"><span>Zuletzt gegangen</span></div>
      ${state.progress.slice().reverse().slice(0, 4).map((pr) => {
        const s = J.stageById(pr.stageId);
        return `<div class="card" style="padding:14px">
          <div class="row">
            <div class="badge" style="width:36px;height:36px;font-size:16px;${pr.skipped ? 'opacity:.4' : ''}">${s ? s.icon : '•'}</div>
            <div class="grow">
              <div style="font-weight:600;${pr.skipped ? 'color:var(--text-low)' : ''}">${esc(s ? s.title : 'Etappe')}</div>
              <div class="dim" style="font-size:12px">${relativeDay(pr.at, now)}${pr.detail ? ' · ' + esc(pr.detail) : ''}${pr.skipped ? ' · übersprungen' : ''}</div>
            </div>
            ${pr.skipped ? '' : `<div style="color:var(--amber);font-weight:600">+${pr.meters}</div>`}
          </div>
        </div>`;
      }).join('')}`
    : `<div class="empty" style="margin-top:14px">Noch keine Etappe gegangen. Die erste Krafteinheit
         dient dazu, deine Lasten zu finden — danach übernimmt die App das Steigern.</div>`}
  </div>`;
}

// ---------------------------------------------------------------------------
// Bereich: Plan
// ---------------------------------------------------------------------------

function viewPlan() {
  const now = Date.now();
  const p = state.profile;
  const next = D.nextDayId(state.sessions);
  const open = planOpen ?? next;

  return `
  <div class="screen">
    <h1>Dein Plan</h1>
    <p class="sub">Drei Einheiten pro Woche, im Wechsel durchrotiert. Zwischen zwei Einheiten
      sollte mindestens ein Tag Pause liegen.</p>

    ${D.DAYS.map((day) => {
      const exs = D.exercisesFor(day, p, state.hiddenExercises);
      const isNext = day.id === next;
      const isOpen = day.id === open;
      const hidden = day.exerciseIds.map(D.byId).filter(
        (e) => e && state.hiddenExercises.includes(e.id) && p.stations.includes(e.station));

      return `<div class="card flush ${isNext ? 'accent-sky' : ''}">
        <div class="row" style="padding:16px;cursor:pointer" data-day-toggle="${day.id}">
          <div class="badge ${isNext ? 'on' : ''}">${day.id}</div>
          <div class="grow">
            <div class="row" style="gap:8px">
              <span style="font-size:17px;font-weight:600">${esc(day.title)}</span>
              ${isNext ? '<span class="pill sky">als Nächstes</span>' : ''}
            </div>
            <div class="dim" style="font-size:12px">${exs.length} Übungen · ${exs.reduce((s, e) => s + e.sets, 0)} Sätze</div>
          </div>
          <span class="dim">${isOpen ? '▲' : '▼'}</span>
        </div>
        ${isOpen ? `<div style="padding:0 16px 16px">
          <p style="color:var(--sky);font-size:13.5px;margin:0 0 12px">${esc(day.subtitle)}</p>
          ${exs.map((ex) => exerciseRow(ex, D.suggest(ex, state.sessions, p, now, FE.recoveryState(state.ascents, now)), false)).join('')}
          ${hidden.length ? `<div class="section-title" style="margin-top:12px"><span>Ausgeblendet</span></div>
            ${hidden.map((ex) => exerciseRow(ex, null, true)).join('')}` : ''}
          <button class="btn ${isNext ? 'primary' : 'ghost'} small" style="margin-top:10px"
            data-start="${day.id}">▶ Diese Einheit starten</button>
        </div>` : ''}
      </div>`;
    }).join('')}

    <div class="card">
      <h2>Warum dieser Aufbau?</h2>
      <p class="muted" style="font-size:13.5px">Der Zug-Tag steht vorne, weil Griffkraft und Zugkraft
        am Steig zuerst limitieren — die willst du ausgeruht trainieren. Der Bein-Tag folgt, damit
        die Unterarme sich erholen, während die Beine arbeiten. Der Druck-Tag hält die Schultern im
        Gleichgewicht: Wer nur zieht, zieht sich die Haltung nach vorne.</p>
      <p class="muted" style="font-size:13.5px">Innerhalb einer Einheit stehen die anspruchsvollen
        Übungen oben. Am Ende der Liste sind Rumpf und Griffkraft — dort schadet etwas Vorermüdung
        nicht, im Gegenteil.</p>
    </div>
  </div>`;
}

function exerciseRow(ex, sug, hidden) {
  return `<div class="ex-row ${hidden ? 'hidden-ex' : ''}" data-ex-row="${ex.id}">
    <div class="row">
      <div class="grow">
        <div class="row" style="gap:7px">
          <span class="t">${esc(ex.name)}</span>
          ${ex.ferrataFocus >= 3 ? '<span class="pill amber">Steig-Kern</span>' : ''}
        </div>
        <div class="m">${ex.sets} × ${ex.progression === D.KIND.TIME ? 'halten' : `${ex.repMin}–${ex.repMax}`}
          · ${esc(D.STATIONS[ex.station].label)}</div>
      </div>
      ${sug ? `<b style="color:${adviceColor(sug.advice)}">${esc(sug.headline)}</b>` : ''}
      <button data-hide="${ex.id}" style="padding:6px;font-size:15px" title="${hidden ? 'Einblenden' : 'Ausblenden'}">
        ${hidden ? '🚫' : '👁'}</button>
    </div>
    <div class="detail" style="display:none">
      ${guideHtml(ex)}
      ${sug ? `<div class="rs">${esc(sug.reason)}</div>` : ''}
    </div>
  </div>`;
}

// ---------------------------------------------------------------------------
// Bereich: Fortschritt
// ---------------------------------------------------------------------------

function viewProgress() {
  const now = Date.now();
  const sessions = state.sessions;

  const snap = badgeSnapshot();
  const earned = J.earnedBadges(snap).map((b) => b.id);
  const badgesHtml = `
    <div class="section-title"><span>Abzeichen</span><span>${earned.length} von ${J.BADGES.length}</span></div>
    <div class="card">
      <div class="badges">
        ${J.BADGES.map((b) => {
          const on = earned.includes(b.id);
          return `<div class="badge-tile ${on ? 'on' : 'off'}" title="${esc(b.desc)}">
            <div class="ic">${b.icon}</div>
            <div class="n">${esc(b.name)}</div>
            <div class="d">${esc(b.desc)}</div>
          </div>`;
        }).join('')}
      </div>
    </div>`;

  const metersHtml = `
    <div class="card accent-amber">
      <div class="row between">
        <div>
          <div class="dim" style="font-size:10.5px;text-transform:uppercase;letter-spacing:.7px">Höhenmeter</div>
          <div style="font-size:28px;font-weight:700;color:var(--amber)">${snap.meters.toLocaleString('de-AT')} Hm</div>
        </div>
        <div style="text-align:right">
          <div class="dim" style="font-size:10.5px">Erreicht</div>
          <div style="font-weight:600">${J.summitProgress(snap.meters).reached.length} Gipfel</div>
        </div>
      </div>
      <div style="margin-top:14px">
        ${J.SUMMITS.map((s) => {
          const on = snap.meters >= s.m;
          return `<div class="row" style="padding:6px 0;${on ? '' : 'opacity:.45'}">
            <span style="width:22px">${on ? '⛰️' : '·'}</span>
            <span class="grow" style="font-size:13.5px;${on ? 'font-weight:600' : ''}">${esc(s.name)}</span>
            <span class="dim" style="font-size:12px">${s.m.toLocaleString('de-AT')} Hm</span>
          </div>`;
        }).join('')}
      </div>
    </div>`;

  if (!sessions.length) {
    return `<div class="screen"><h1>Fortschritt</h1>
      <p class="sub">${state.progress.length ? `${state.progress.filter((x) => !x.skipped).length} Etappen gegangen` : 'Hier wird es spannend, sobald die erste Etappe steht.'}</p>
      ${bodySection()}
      ${metersHtml}
      ${badgesHtml}
      <div class="empty">Nach der ersten Krafteinheit erscheinen hier auch deine Kurven,
        nach der zweiten beginnt die App zu steigern.</div>
      <div style="height:80px"></div></div>`;
  }

  const trained = D.EXERCISES.filter((ex) => sessions.some((s) => s.sets.some((x) => x.exerciseId === ex.id)));
  const pick = trendPick && trained.some((e) => e.id === trendPick)
    ? trendPick
    : (trained.find((e) => e.ferrataFocus >= 3) || trained[0]).id;
  const ex = D.byId(pick);
  const points = D.trend(pick, sessions);
  const recs = D.records(sessions);
  const totalVol = Math.round(sessions.reduce((s, x) => s + D.volumeOf(x), 0));

  const hang = D.bestOf(sessions, 'deadhang', 'seconds');
  const pull = D.bestOf(sessions, 'pullup', 'reps');
  const knee = D.bestOf(sessions, 'hang_knee_raise', 'reps');

  // Einheiten je Kalenderwoche, letzte acht
  const byWeek = new Map();
  for (const s of sessions) {
    const d = new Date(s.startedAt);
    const onejan = new Date(d.getFullYear(), 0, 1);
    const kw = Math.ceil((((d - onejan) / DAY_MS) + onejan.getDay() + 1) / 7);
    const key = `KW${kw}`;
    byWeek.set(key, (byWeek.get(key) || 0) + 1);
  }
  const weeks = [...byWeek.entries()].slice(-8);
  const maxW = Math.max(1, ...weeks.map((w) => w[1]));

  const delta = points.length >= 2 ? points.at(-1).value - points[0].value : null;

  return `
  <div class="screen">
    <h1>Fortschritt</h1>
    <p class="sub">${state.progress.filter((x) => !x.skipped).length} Etappen · ${sessions.length} Einheiten · ${totalVol} kg bewegt</p>

    ${bodySection()}
    ${metersHtml}
    ${badgesHtml}

    <div class="card">
      <h2>Steig-Kennwerte</h2>
      <p class="dim" style="font-size:12.5px;margin:0 0 16px">Die drei Werte, die am Fels wirklich zählen.</p>
      ${keyMetric('Dead Hang', `${hang} s`, 'Ziel: 60 s', hang / 60, 'amber')}
      ${keyMetric('Klimmzüge', `${pull}`, 'Ziel: 8', pull / 8, '')}
      ${keyMetric('Knieheben hängend', `${knee}`, 'Ziel: 12', knee / 12, 'emerald')}
    </div>

    <div class="section-title"><span>Verlauf</span></div>
    <div class="chips">
      ${trained.map((e) => `<button class="chip ${e.id === pick ? 'on' : ''}" data-trend="${e.id}">${esc(e.name)}</button>`).join('')}
    </div>
    <div class="card">
      <div class="row between">
        <div>
          <div style="font-size:17px;font-weight:600">${esc(ex.name)}</div>
          <div class="dim" style="font-size:12px">${points.length} Einheiten aufgezeichnet</div>
        </div>
        ${delta !== null ? `<span class="pill ${delta >= 0 ? 'emerald' : 'rose'}">${delta >= 0 ? '+' : ''}${
          ex.progression === D.KIND.TIME ? `${Math.round(delta)} s`
          : ex.progression === D.KIND.WEIGHT ? D.fmtKg(delta) : `${Math.round(delta)} Wdh.`}</span>` : ''}
      </div>
      <div style="margin-top:16px">${chartSvg(points)}</div>
      ${points.length >= 2 ? `<div class="row between" style="margin-top:10px">
        <span class="dim" style="font-size:10.5px">${dateShort(points[0].at)} · ${esc(points[0].label)}</span>
        <span style="font-size:10.5px;color:var(--sky)">${dateShort(points.at(-1).at)} · ${esc(points.at(-1).label)}</span>
      </div>` : ''}
    </div>

    ${recs.length ? `<div class="section-title"><span>Bestleistungen</span><span>${recs.length}</span></div>
      <div class="card" style="padding:8px">
        ${recs.slice(0, 12).map((r, i) => `<div class="line">
          <span>${i === 0 ? '🏆' : D.byId(r.exerciseId)?.progression === D.KIND.TIME ? '⏱' : '🏋'}</span>
          <div class="grow"><div style="font-weight:600">${esc(r.name)}</div>
            <div class="dim" style="font-size:10.5px">${dateShort(r.achievedAt)}</div></div>
          <b style="color:var(--sky)">${esc(r.value)}</b>
        </div>`).join('')}
      </div>` : ''}

    <div class="section-title"><span>Einheiten je Woche</span></div>
    <div class="card">
      <div class="weekbars">
        ${weeks.map(([k, n]) => `<div class="wb">
          <span class="c">${n}</span>
          <div class="b ${n >= 3 ? 'full' : ''}" style="height:${Math.max(6, (n / maxW) * 70)}px"></div>
          <span class="k">${esc(k)}</span>
        </div>`).join('')}
      </div>
    </div>
  </div>`;
}


/**
 * Körperdaten. Die Android-App holt sie über Health Connect von der Waage
 * (Kette FitDays → Samsung Health → Health Connect); im Browser gibt es diesen
 * Zugang nicht, dort trägt man das Gewicht von Hand ein.
 */
function bodySection() {
  const now = Date.now();
  const latest = J.Body.latest(state.body);
  const trend = J.Body.weightTrend(state.body, now);
  const bestPullup = D.bestOf(state.sessions, 'pullup', 'reps');

  const compo = [];
  if (latest) {
    if (latest.bodyFatPercent != null) {
      compo.push(['Körperfett', `${Math.round(latest.bodyFatPercent * 10) / 10} %`,
        J.Body.bodyFatLabel(latest.bodyFatPercent)]);
    }
    const bmi = J.Body.bmi(latest.weightKg, state.heightCm);
    if (bmi) compo.push(['BMI', `${bmi}`, '']);
  }

  return `
    <div class="section-title"><span>Körper</span></div>
    <div class="card ${latest ? 'accent-violet' : ''}">
      ${latest ? `
        <div class="row" style="align-items:flex-end;gap:12px">
          <span style="font-size:30px;font-weight:700;letter-spacing:-1px">${D.fmtKg(latest.weightKg)}</span>
          ${trend != null && Math.abs(trend) >= 0.1 ? `
            <span class="pill ${trend < 0 ? 'emerald' : ''}" style="margin-bottom:6px">
              ${trend < 0 ? '' : '+'}${Math.round(trend * 10) / 10} kg / Monat</span>` : ''}
        </div>
        <div class="dim" style="font-size:12px;margin-top:4px">${esc(J.Body.freshnessLabel(latest.at, now))}</div>
        ${compo.length ? `<div class="tiles" style="grid-template-columns:repeat(${compo.length},1fr);margin-top:14px">
          ${compo.map(([l, v, h]) => `<div class="tile"><div class="val" style="font-size:19px">${esc(v)}</div>
            <div class="lbl">${esc(l)}</div>${h ? `<div class="lbl" style="color:var(--violet)">${esc(h)}</div>` : ''}</div>`).join('')}
        </div>` : ''}
        ${bestPullup > 0 ? `<div class="info-block" style="margin-top:14px">
          <div class="h">Am Steig bedeutet das</div>
          <div class="b">Bei ${bestPullup} Klimmzügen bewegst du gerade
            ${D.fmtKg(J.Body.pullupLoadPerRep(latest.weightKg, bestPullup))} je Wiederholung.${
              trend != null && trend < -0.4
                ? ` Du bist ${D.fmtKg(Math.abs(trend))} leichter als vor einem Monat — das entspricht
                    bei gleicher Kraft grob ${Math.round(J.Body.pullupEquivalent(trend) * 10) / 10}
                    zusätzlichen Wiederholungen.`
                : ' Jedes Kilo weniger macht dieselbe Leistung leichter.'}</div>
        </div>` : ''}
        ${state.body.length >= 2 ? `<div style="margin-top:14px">${
          chartSvg(state.body.slice(-30).map((b) => ({ at: b.at, value: b.weightKg, label: D.fmtKg(b.weightKg) })),
            'var(--violet)')}</div>` : ''}
      ` : `<p class="muted" style="font-size:13.5px;margin-top:0">Noch kein Gewicht eingetragen.
             Die Android-App holt es automatisch von deiner Waage — hier im Browser trägst du
             es von Hand ein.</p>`}
      <div class="row" style="gap:8px;margin-top:14px">
        <input type="number" inputmode="decimal" id="body-weight" placeholder="Gewicht in kg"
          style="flex:1;background:var(--surface-high);border:1px solid var(--outline);
                 border-radius:13px;padding:12px 14px;color:var(--text-high);outline:none">
        <button class="btn ghost small" id="body-add" style="width:auto;padding:0 18px">Eintragen</button>
      </div>
      <button class="btn ghost small" id="body-file" style="margin-top:8px">
        ⬆ Datei aus deiner Waagen-App einlesen</button>
      <p class="dim" style="font-size:11.5px;margin:8px 0 0">Eine aus FitDays geteilte Tabelle.
        Komma, Semikolon oder Tabulator, deutsche wie englische Spalten — der Leser kommt
        mit allem zurecht.</p>
    </div>`;
}

function keyMetric(name, value, goal, progress, tone) {
  return `<div style="margin-bottom:14px">
    <div class="row between" style="align-items:flex-end">
      <span class="muted" style="font-size:13.5px">${esc(name)}</span>
      <span><b style="font-size:17px">${esc(value)}</b>
        <span class="dim" style="font-size:10.5px;margin-left:6px">${esc(goal)}</span></span>
    </div>
    <div class="bar ${tone}" style="margin-top:7px;height:7px">
      <i style="width:${Math.min(100, Math.max(0, progress * 100))}%"></i></div>
  </div>`;
}

// ---------------------------------------------------------------------------
// Bereich: Mehr
// ---------------------------------------------------------------------------

// ---------------------------------------------------------------------------
// Bereich: Am Fels
// ---------------------------------------------------------------------------

/**
 * Die Missionsübersicht.
 *
 * Die Reihenfolge ist Absicht. Ganz oben der Steigpass mit der einen Zahl, die zählt —
 * bis zu welcher Stufe du im Rahmen bist. Darunter die Routen, sortiert nach Passung,
 * nicht nach Schwierigkeit: Was heute geht, steht oben; was noch zu früh ist, steht
 * unten und ist eingeklappt. Wer scrollen muss, um an die schweren Sachen zu kommen,
 * überlegt es sich unterwegs vielleicht anders.
 */
function viewFerrata() {
  const now = Date.now();
  const readiness = D.ferrataReadiness(state.sessions, now);
  const pass = FE.buildSteigPass(state.ascents, readiness, now);

  const sorted = FERRATAS
    .filter((r) => !ferrataRegion || r.region === ferrataRegion)
    .map((r) => ({ r, fit: FE.fitFor(r, state.ascents, readiness) }))
    .sort((a, b) => a.fit - b.fit
      || FE.gradeIndex(a.r.grade) - FE.gradeIndex(b.r.grade)
      || a.r.name.localeCompare(b.r.name));

  const group = (f) => sorted.filter((x) => x.fit === f);
  const tooEarly = group(FE.FIT.ZU_FRUEH);

  const section = (f) => {
    const rows = group(f);
    if (!rows.length) return '';
    const tone = ['emerald', 'amber', 'violet', 'dim'][f];
    return `
      <div class="section-title" style="margin-top:16px">
        <span><span class="fit-dot ${tone}"></span>${FE.FIT_TITLE[f]}
          <span class="dim" style="font-weight:400"> ${rows.length}</span></span>
      </div>
      <p class="muted" style="font-size:12.5px;margin:-4px 0 8px 17px">${esc(FE.FIT_LABEL[f])}</p>
      ${rows.map((x) => routeCard(x.r, x.fit)).join('')}`;
  };

  return `
  <div class="wrap">
    <h1>🧗 Am Fels</h1>
    ${steigPassCard(pass)}

    <button class="btn primary block" data-log-ascent style="margin:12px 0">
      Begehung eintragen
    </button>

    <div class="chips">
      <button class="chip ${ferrataMap ? 'on' : ''}" data-ferrata-map>${ferrataMap ? '☰ Liste' : '🗺 Karte'}</button>
      <button class="chip ${!ferrataRegion && !ferrataMap ? 'on' : ''}" data-region="">Alle Gebiete</button>
      ${ferrataRegions.map((r) => `<button class="chip ${ferrataRegion === r && !ferrataMap ? 'on' : ''}"
        data-region="${esc(r)}">${esc(r.split(' (')[0])}</button>`).join('')}
    </div>

    ${ferrataMap ? `
      <div class="card" style="padding:10px">
        ${mapSvg(Object.fromEntries(FERRATAS.map((r) => [r.id, FE.fitFor(r, state.ascents, readiness)])), ferrataOpen)}
        <div class="row" style="gap:12px;margin-top:8px;flex-wrap:wrap">
          <span class="dim" style="font-size:11px"><span class="fit-dot emerald"></span>passt</span>
          <span class="dim" style="font-size:11px"><span class="fit-dot amber"></span>mit Puffer</span>
          <span class="dim" style="font-size:11px"><span class="fit-dot violet"></span>nächster Schritt</span>
          <span class="dim" style="font-size:11px"><span class="fit-dot dim"></span>noch nicht</span>
        </div>
      </div>
      ${ferrataOpen && ferrataById(ferrataOpen)
        ? routeCard(ferrataById(ferrataOpen), FE.fitFor(ferrataById(ferrataOpen), state.ascents, readiness))
        : `<p class="dim" style="font-size:12.5px;text-align:center;margin-top:10px">
             Einen Punkt antippen — Punkte am selben Fels wechseln reihum.</p>`}
    ` : `
    ${section(FE.FIT.PASST)}${section(FE.FIT.KNAPP)}${section(FE.FIT.ZIEL)}`}

    ${!group(FE.FIT.PASST).length && !group(FE.FIT.KNAPP).length && !group(FE.FIT.ZIEL).length ? `
      <div class="empty" style="margin-top:16px">${
        ferrataRegion
          ? 'In diesem Gebiet liegt gerade nichts im Rahmen. Andere Gebiete zeigen mehr.'
          : esc(pass.reason) + ' Die Steige darunter stehen weiter offen — sie sind nur ' +
            'noch nichts, wozu die App raten würde.'}</div>` : ''}

    ${!ferrataMap && tooEarly.length ? `
      <div class="card" style="margin-top:16px;padding:14px;cursor:pointer" data-toggle-early>
        <div class="row">
          <div class="grow">
            <div style="font-weight:600;color:var(--text-mid)">Noch nicht dran</div>
            <div class="dim" style="font-size:12px">${tooEarly.length} Steige über deinem bisherigen Stand</div>
          </div>
          <div class="dim">${showTooEarly ? '▲' : '▼'}</div>
        </div>
      </div>
      ${showTooEarly ? tooEarly.map((x) => routeCard(x.r, x.fit)).join('') : ''}` : ''}

    ${state.ascents.length ? `
      <div class="section-title" style="margin-top:20px"><span>Deine Begehungen</span></div>
      ${state.ascents.slice().reverse().map((a) => `
        <div class="card" style="padding:14px">
          <div class="row">
            <div class="badge" style="width:36px;height:36px;font-size:14px">${esc(a.grade)}</div>
            <div class="grow">
              <div style="font-weight:600">${esc(a.name)}</div>
              <div class="dim" style="font-size:12px">${relativeDay(a.date, now)}${
                a.climbMeters ? ' · ' + a.climbMeters + ' Hm' : ''}${
                a.turnedBack ? ' · umgekehrt' : ''}</div>
            </div>
            <button class="btn ghost small" data-del-ascent="${esc(a.id)}">löschen</button>
          </div>
        </div>`).join('')}` : ''}

    <p class="muted" style="font-size:12px;margin-top:20px">ℹ️ ${esc(FE.DISCLAIMER)}</p>
  </div>`;
}

/**
 * Die Karte der Klettersteige — selbst gezeichnetes SVG statt einer Kartenbibliothek.
 *
 * Die App lädt nichts nach: keine Kacheln, keine Fremdanbieter, funktioniert am Berg
 * ohne Netz. Für „wo liegt was, und was passt zu mir" reicht die Silhouette des
 * Landes; für den Zustieg braucht man ohnehin eine echte Wanderkarte.
 */
function mapSvg(fits, selectedId) {
  const B = GEO_BOUNDS;
  // Längengrade werden zum Pol hin schmaler — ohne den Faktor wäre das Land
  // um ein Drittel in die Breite gezogen.
  const lonScale = Math.cos(((B.minLat + B.maxLat) / 2) * Math.PI / 180);
  const W = 1000;
  const H = W * (B.maxLat - B.minLat) / ((B.maxLon - B.minLon) * lonScale);
  const px = (lon) => ((lon - B.minLon) / (B.maxLon - B.minLon)) * W;
  const py = (lat) => (1 - (lat - B.minLat) / (B.maxLat - B.minLat)) * H;

  const outline = GEO_OUTLINE.map(([lat, lon], i) =>
    `${i ? 'L' : 'M'}${px(lon).toFixed(1)},${py(lat).toFixed(1)}`).join(' ') + ' Z';

  const fitColor = (f) => f === FE.FIT.PASST ? 'var(--emerald)'
    : f === FE.FIT.KNAPP ? 'var(--amber)'
    : f === FE.FIT.ZIEL ? 'var(--violet)' : 'var(--text-low)';

  // Graue zuerst, farbige darüber, der gewählte zuoberst
  const pts = [...GEO_POINTS].sort((a, b) => {
    const rank = (p) => p.id === selectedId ? 2 : (fits[p.id] !== FE.FIT.ZU_FRUEH ? 1 : 0);
    return rank(a) - rank(b);
  });

  return `
  <svg viewBox="0 0 ${W} ${H.toFixed(0)}" style="width:100%;display:block" role="img"
       aria-label="Karte der Vorarlberger Klettersteige">
    <path d="${outline}" fill="var(--surface-high)" stroke="var(--outline)" stroke-width="2.5"/>
    ${GEO_LANDMARKS.map((l) => `
      <circle cx="${px(l.lon).toFixed(1)}" cy="${py(l.lat).toFixed(1)}" r="4" fill="var(--text-low)"/>
      <text x="${(px(l.lon) + 9).toFixed(1)}" y="${(py(l.lat) - 8).toFixed(1)}"
            font-size="22" fill="var(--text-low)">${esc(l.name)}</text>`).join('')}
    ${pts.map((p) => {
      const sel = p.id === selectedId;
      return `${sel ? `<circle cx="${px(p.lon).toFixed(1)}" cy="${py(p.lat).toFixed(1)}" r="22"
          fill="${fitColor(fits[p.id])}" opacity=".25"/>
        <circle cx="${px(p.lon).toFixed(1)}" cy="${py(p.lat).toFixed(1)}" r="12" fill="var(--text-high)"/>` : ''}
      <circle cx="${px(p.lon).toFixed(1)}" cy="${py(p.lat).toFixed(1)}" r="${sel ? 9 : 8}"
        fill="${fitColor(fits[p.id])}" data-map-route="${esc(p.id)}" style="cursor:pointer"/>`;
    }).join('')}
  </svg>`;
}

/** Rang als Überschrift, Form als Balken, darunter die eine Zahl, die zählt. */
function steigPassCard(pass) {
  return `
  <div class="card accent-amber">
    <div class="row" style="gap:13px">
      <div class="badge" style="width:46px;height:46px;font-size:22px;background:rgba(251,191,36,.15)">${pass.rank.icon}</div>
      <div class="grow">
        <h2 style="font-size:17px;margin:0">${esc(pass.rank.title)}</h2>
        <p class="dim" style="font-size:12.5px;margin:2px 0 0">${
          pass.cleanAscents} saubere ${pass.cleanAscents === 1 ? 'Begehung' : 'Begehungen'} ·
          ${pass.meters} Hm am Fels${pass.mastered ? ' · ' + pass.mastered.label + ' bestätigt' : ''}</p>
      </div>
    </div>

    <div style="margin-top:14px">
      <div class="row" style="font-size:12.5px;margin-bottom:5px">
        <span class="dim grow">Trainingsform</span>
        <span style="color:var(--text-mid)">${pass.readiness}/100</span>
      </div>
      <div class="bar"><i style="width:${pass.readiness}%"></i></div>
    </div>

    <div style="margin-top:15px;padding-top:14px;border-top:1px solid var(--outline)">
      <div class="row">
        <div class="grow">
          <div class="dim" style="font-size:12px">Im Rahmen</div>
          <div style="font-size:19px;font-weight:700;color:var(--amber)">bis Stufe ${pass.recommended.label}</div>
        </div>
      </div>
      <p class="muted" style="font-size:12.5px;margin:7px 0 0">${esc(pass.reason)}</p>
    </div>

    ${pass.nextRank ? `
      <p class="dim" style="font-size:12px;margin:12px 0 0">
        Nächster Rang ${pass.nextRank.icon} ${esc(pass.nextRank.title)} — ${esc(pass.nextHint)}</p>` : ''}
  </div>`;
}

function routeCard(r, fit) {
  const tone = ['emerald', 'amber', 'violet', 'dim'][fit];
  const open = ferrataOpen === r.id;
  const planned = state.plannedRouteIds.includes(r.id);
  const meta = [
    r.climbMeters ? r.climbMeters + ' Klettermeter' : null,
    r.totalMin ? `${Math.floor(r.totalMin / 60)} h ${r.totalMin % 60} min` : null,
    (r.region || '').split(' (')[0],
  ].filter(Boolean).join(' · ');

  return `
  <div class="card ${open ? 'accent-' + tone : ''}" style="padding:15px">
    <div class="row" data-route="${esc(r.id)}" style="cursor:pointer">
      <div class="badge grade ${tone}">${esc(r.grade)}</div>
      <div class="grow">
        <div style="font-weight:600">${esc(r.name)}</div>
        <div class="dim" style="font-size:12px">${esc(meta)}</div>
      </div>
      <button class="star ${planned ? 'on' : ''}" data-plan="${esc(r.id)}"
        title="${planned ? 'Vorgemerkt' : 'Vormerken'}">${planned ? '★' : '☆'}</button>
    </div>

    ${open ? `
      <div style="margin-top:13px">
        <div class="chips" style="margin-bottom:12px;padding:0">
          ${['Info',
             photosFor(r.id).length ? `Fotos · ${photosFor(r.id).length}` : 'Fotos',
             (ownPhotoCache[r.id] || []).length ? `Eigene · ${ownPhotoCache[r.id].length}` : 'Eigene',
             'Topo'].map((l, i) => `<button class="chip ${routeTab === i ? 'on' : ''}"
               data-route-tab="${i}">${l}</button>`).join('')}
        </div>
        ${routeTab === 1 ? webPhotosTab(r) : routeTab === 2 ? ownPhotosTab(r) : routeTab === 3 ? topoTab(r) : `
        <div class="chips" style="margin-bottom:10px">
          ${r.crux ? `<span class="pill rose">Schlüsselstelle ${esc(r.crux)}</span>` : ''}
          ${r.hasExit ? '<span class="pill emerald">Notausstieg</span>' : ''}
          ${r.familyFriendly ? '<span class="pill sky">familientauglich</span>' : ''}
        </div>
        ${dayProfileSvg(r)}
        ${r.summary ? `<p class="muted" style="font-size:13px">${esc(r.summary)}</p>` : ''}
        ${detailBlock('Zustieg', r.approach)}
        ${detailBlock('Abstieg', r.descent)}
        ${detailBlock('Ausrüstung', r.gear)}
        ${r.season ? detailBlock('Saison', r.season) : ''}
        ${r.warnings && r.warnings.length ? `
          <h4 style="color:var(--rose);font-size:12.5px;margin:14px 0 4px">Zu beachten</h4>
          <ul class="warn">${r.warnings.map((w) => `<li>${esc(w)}</li>`).join('')}</ul>` : ''}
        ${r.verified === false ? `
          <div class="note-amber">Die Quellen widersprechen sich bei diesem Steig — meist beim
            Schwierigkeitsgrad. Vor Ort prüfen und im Zweifel die schwerere Angabe annehmen.</div>` : ''}
        ${r.sources && r.sources.length ? `
          <p class="dim" style="font-size:11.5px;margin-top:11px">Quellen: ${
            r.sources.map((u) => `<a href="${esc(u)}" target="_blank" rel="noopener">${
              esc(u.replace(/^https?:\/\/(www\.)?/, '').split('/')[0])}</a>`).join(' ')}</p>` : ''}`}
      </div>` : ''}
  </div>`;
}

const host = (u) => u.replace(/^https?:\/\/(www\.)?/, '').split('/')[0];

/**
 * Fotos von Wikimedia Commons — geladen, wenn der Reiter aufgeht, nicht vorher.
 * Jedes Bild nennt Urheber und Lizenz: die Bedingung der freien Lizenzen und der Grund,
 * warum diese Fotos in einer öffentlichen App überhaupt gezeigt werden dürfen.
 */
function webPhotosTab(r) {
  const photos = photosFor(r.id);
  const gallery = GALLERIES[r.id];
  let body;
  if (!state.profile.webPhotosEnabled) {
    body = `<p class="dim" style="font-size:13px">Das Nachladen von Fotos ist unter Mehr → Netz
      ausgeschaltet. Die App lädt dann nichts aus dem Internet.</p>`;
  } else if (!photos.length) {
    body = `<p class="dim" style="font-size:13px">Für diesen Steig gibt es auf Wikimedia Commons kein
      frei lizenziertes Foto. Fremde Fotos aus dem Netz nimmt die App bewusst nicht auf — Urheberrecht.</p>`;
  } else {
    body = photos.map((p) => `
      <figure class="webphoto">
        <img src="${esc(p.url)}" alt="${esc(p.shows)}" loading="lazy">
        <figcaption>
          <div class="muted" style="font-size:13px">${esc(p.shows)}</div>
          <a href="${esc(p.pageUrl)}" target="_blank" rel="noopener" style="font-size:11.5px">
            ${esc(p.author)} · ${esc(p.license)} · Wikimedia Commons</a>
        </figcaption>
      </figure>`).join('');
  }
  return body + (gallery ? `
    <a class="linkcard" href="${esc(gallery)}" target="_blank" rel="noopener">
      Weitere Fotos auf ${esc(host(gallery))} ↗</a>` : '');
}

/** Eigene Fotos aus IndexedDB — beim Öffnen des Reiters nachgeladen. */
function ownPhotosTab(r) {
  const own = ownPhotoCache[r.id];
  if (!own) loadOwnPhotos(r.id);
  return `
    ${!own ? '<p class="dim" style="font-size:13px">Lade …</p>'
      : !own.length ? `<p class="dim" style="font-size:13px">Noch kein eigenes Bild. Häng eines direkt
          an den Steig — es bleibt in diesem Browser gespeichert.</p>`
      : own.map((p) => `
        <figure class="webphoto">
          <img src="${p.url}" alt="Eigenes Foto: ${esc(r.name)}">
          <figcaption class="row">
            <span class="dim grow" style="font-size:11.5px">direkt angehängt</span>
            <button class="btn ghost small" data-del-photo="${esc(p.id)}" data-photo-route="${esc(r.id)}"
              style="width:auto;height:34px;padding:0 12px">entfernen</button>
          </figcaption>
        </figure>`).join('')}
    <label class="linkcard" style="cursor:pointer">📷&nbsp; Foto hinzufügen
      <input type="file" accept="image/*" data-add-photo="${esc(r.id)}" style="display:none"></label>`;
}

async function loadOwnPhotos(routeId) {
  try {
    const rows = await PhotoDb.listPhotos(routeId);
    (ownPhotoCache[routeId] || []).forEach((p) => URL.revokeObjectURL(p.url));
    ownPhotoCache[routeId] = rows.map((row) => ({ id: row.id, url: URL.createObjectURL(row.blob) }));
  } catch {
    ownPhotoCache[routeId] = [];
  }
  render();
}

/**
 * Die schematische Topo: vom Einstieg unten zum Ausstieg oben.
 * Gezeichnet aus Abschnitten, die aus den Tourenbeschreibungen gezogen sind —
 * Reihenfolge, Art, Grad. Fakten sind frei; die gezeichnete Original-Topo ist
 * Urheberwerk und gibt es deshalb nur als Link.
 */
function topoTab(r) {
  const segs = topoFor(r.id);
  const url = TOPO_URLS[r.id];
  const ICON = { ladder: '🪜', bridge: '🌉', ridge: '⛰', gully: '🏔', cave: '🕳', overhang: '🧗',
    walk: '🥾', exit: '🚪', traverse: '↔', wall: '▲' };
  const tone = (g) => { const i = FE.gradeIndex(g); return i <= 1 ? 'emerald' : i === 2 ? 'sky' : i === 3 ? 'amber' : 'rose'; };
  // Ohne Einstiegshöhe im Katalog wäre „Ausstieg · 60 m" eine Seehöhe, die keine ist
  const topLabel = r.startAlt ? ` · ${Math.max(r.summitAlt || 0, r.startAlt + (r.climbMeters || 0))} m`
    : r.climbMeters ? ` · +${r.climbMeters} Hm` : '';
  if (!segs.length) return '<p class="dim" style="font-size:13px">Für diesen Steig liegt noch keine Abschnittsfolge vor.</p>';
  return `
    <div class="dim" style="font-size:12px;margin-bottom:6px">▲ Ausstieg${topLabel}</div>
    <div class="topo">
      ${[...segs].reverse().map((s) => `
        <div class="seg ${s.kind === 'exit' ? 'exit' : tone(s.grade)} ${s.crux ? 'crux' : ''}">
          <i></i>
          <b>${s.kind === 'exit' ? '🚪' : esc(s.grade)}</b>
          <div class="grow">
            <div>${ICON[s.kind] || '▲'} ${esc(s.label)}</div>
            ${s.crux || s.meters || s.kind === 'exit' ? `<small>${[
              s.crux ? 'Schlüsselstelle' : '', s.meters ? `${s.meters} Hm` : '',
              s.kind === 'exit' ? 'Notausstieg' : ''].filter(Boolean).join(' · ')}</small>` : ''}
          </div>
        </div>`).join('')}
    </div>
    <div class="dim" style="font-size:12px;margin-top:6px">▼ Einstieg${r.startAlt ? ` · ${r.startAlt} m` : ''}</div>
    <p class="dim" style="font-size:12px;margin-top:10px">Schematisch, aus den Tourenbeschreibungen
      abgeleitet — Reihenfolge und Grade der Abschnitte, nicht ihre Länge. Am Einstieg zählt die Tafel vor Ort.</p>
    ${url ? `<a class="linkcard" href="${esc(url)}" target="_blank" rel="noopener">Gezeichnete Topo auf ${esc(host(url))} ↗</a>` : ''}`;
}

/**
 * Die Tagesskizze: Zustieg, Wand, Abstieg.
 *
 * Bewusst „Skizze" und nicht „Profil": Belegt sind nur Einstiegshöhe, Ausstiegshöhe
 * und die drei Zeiten. Zustieg und Abstieg sind gestrichelt — ihre Form ist
 * schematisch, nur die Wand dazwischen ist echte Angabe.
 */
function dayProfileSvg(r) {
  if (!r.climbMeters || !r.startAlt) return '';
  const topAlt = Math.max(r.summitAlt || 0, r.startAlt + r.climbMeters);
  const [za, fe] = FE.daySegments(r.approachMin, r.ferrataMin, r.descentMin);
  const gi = FE.gradeIndex(r.grade);
  const color = gi <= 1 ? 'var(--emerald)' : gi === 2 ? 'var(--sky)' : gi === 3 ? 'var(--amber)' : 'var(--rose)';

  const W = 640, H = 120;
  const yBase = H * 0.78, yIn = H * 0.62, yTop = H * 0.10;
  const x1 = W * za, x2 = W * (za + fe);
  const min = (v) => (v > 0 ? `${v} min` : '—');

  return `
  <svg viewBox="0 0 ${W} ${H}" style="width:100%;display:block;margin-top:12px" role="img"
       aria-label="Tagesskizze: Zustieg, Klettersteig, Abstieg">
    <line x1="0" y1="${yBase}" x2="${x1}" y2="${yIn}" stroke="var(--text-low)"
      stroke-width="2" stroke-dasharray="6 6"/>
    <line x1="${x1}" y1="${yIn}" x2="${x2}" y2="${yTop}" stroke="${color}" stroke-width="4"/>
    <circle cx="${x1}" cy="${yIn}" r="4.5" fill="${color}"/>
    <circle cx="${x2}" cy="${yTop}" r="4.5" fill="${color}"/>
    <line x1="${x2}" y1="${yTop}" x2="${W}" y2="${yBase}" stroke="var(--text-low)"
      stroke-width="2" stroke-dasharray="6 6"/>
    <text x="${x1 + 8}" y="${yIn - 6}" font-size="13" fill="var(--text-low)">${r.startAlt} m</text>
    <text x="${Math.max(x2 - 55, 4)}" y="${Math.max(yTop - 8, 12)}" font-size="13"
      fill="var(--text-low)">${topAlt} m</text>
    <text x="${x1 / 2}" y="${H - 6}" font-size="13" fill="var(--text-low)"
      text-anchor="middle">${min(r.approachMin)}</text>
    <text x="${(x1 + x2) / 2}" y="${H - 6}" font-size="13" fill="${color}"
      text-anchor="middle">${r.climbMeters} Hm · ${min(r.ferrataMin)}</text>
    <text x="${(x2 + W) / 2}" y="${H - 6}" font-size="13" fill="var(--text-low)"
      text-anchor="middle">${min(r.descentMin)}</text>
  </svg>`;
}

function detailBlock(label, text) {
  if (!text) return '';
  return `<h4 style="color:var(--sky);font-size:12.5px;margin:13px 0 3px">${label}</h4>
    <p class="muted" style="font-size:13px;margin:0">${esc(text)}</p>`;
}

/**
 * Das Eintragsformular.
 *
 * Die wichtigste Frage steht in der Mitte und ist keine Zahl: Wie hat es sich angefühlt?
 * Umkehren steht bewusst gleichberechtigt neben dem Durchstieg, nicht als Kleingedrucktes —
 * wer meint, ein Abbruch sei ein Makel im Verlauf, kehrt beim nächsten Mal vielleicht
 * später um als gut wäre.
 */
function viewAscentForm() {
  const f = ascentForm;
  const hits = f.search.length >= 2 && !f.routeId
    ? FERRATAS.filter((r) => r.name.toLowerCase().includes(f.search.toLowerCase())
        || (r.region || '').toLowerCase().includes(f.search.toLowerCase())).slice(0, 6)
    : [];
  const canSave = (f.name || '').trim() && f.feel;

  return `
  <div id="workout"><div class="wrap">
      <div class="row" style="margin-bottom:8px">
        <button class="btn ghost" data-close-ascent>← Zurück</button>
      </div>
      <h1 style="font-size:22px">Begehung eintragen</h1>

      <div class="card">
        <h3>Welcher Steig?</h3>
        ${f.routeId ? `
          <div class="row" style="margin-top:9px">
            <div class="grow"><div style="font-weight:600">${esc(f.name)}</div></div>
            <button class="btn ghost small" data-clear-route>ändern</button>
          </div>`
        : `
          <input id="asc-search" placeholder="Name suchen oder frei eintragen"
                 value="${esc(f.search)}" autocomplete="off">
          ${hits.map((r) => `
            <div class="hit" data-pick="${esc(r.id)}">
              <span class="g">${esc(r.grade)}</span><span>${esc(r.name)}</span>
            </div>`).join('')}
          ${f.search.length >= 2 && !hits.length ? `
            <p class="dim" style="font-size:12px;margin-top:8px">Nicht im Katalog — wird als
              eigener Eintrag gespeichert. Schwierigkeit bitte unten selbst wählen.</p>` : ''}`}
      </div>

      <div class="card">
        <h3>Schwierigkeit</h3>
        <p class="dim" style="font-size:12px;margin:3px 0 9px">Bei Zwischenstufen wie C/D zählt die schwerere.</p>
        <div class="grades">
          ${FE.GRADES.map((g, i) => `<button class="grade-btn ${f.gradeIdx === i ? 'on' : ''}"
            data-grade="${i}">${g.label}</button>`).join('')}
        </div>
        <p class="muted" style="font-size:12.5px;margin:9px 0 0">${esc(FE.GRADES[f.gradeIdx].desc)}</p>
      </div>

      <div class="card">
        <h3>Umfang</h3>
        <div class="row" style="gap:10px;margin-top:9px">
          <input id="asc-meters" type="number" inputmode="numeric" placeholder="Klettermeter" value="${f.meters}">
          <input id="asc-minutes" type="number" inputmode="numeric" placeholder="Dauer in min" value="${f.minutes}">
        </div>
        <div class="row" style="gap:10px;margin-top:10px">
          <input id="asc-hr" type="number" inputmode="numeric" placeholder="Ø Puls (optional)" value="${f.avgHr || ''}">
        </div>
        <p class="dim" style="font-size:12px;margin:9px 0 0">Klettermeter meint den gesicherten
          Steig, nicht den ganzen Tag. Der Zustieg zählt für den Rang nicht mit.</p>
      </div>

      <div class="card ${f.turnedBack ? 'accent-sky' : ''}">
        <h3>Wie ist es ausgegangen?</h3>
        <div class="row" style="gap:9px;margin-top:10px">
          <button class="choice ${!f.turnedBack ? 'on' : ''}" data-turned="0">Durchgestiegen</button>
          <button class="choice ${f.turnedBack ? 'on' : ''}" data-turned="1">Umgekehrt</button>
        </div>
        ${f.turnedBack ? `<p style="color:var(--sky);font-size:12.5px;margin:10px 0 0">Die Höhenmeter
          zählen voll. Der Rang bleibt, wo er ist — Umkehren kostet hier nichts.</p>` : ''}
      </div>

      <div class="card ${!f.feel ? 'accent-amber' : ''}">
        <h3>Wie hat es sich angefühlt?</h3>
        <p class="dim" style="font-size:12px;margin:3px 0 9px">Das ist die Angabe, aus der die App am meisten zieht.</p>
        ${FE.FEELS.map((x) => `
          <button class="opt ${f.feel === x.id ? 'on' : ''}" data-feel="${x.id}">
            <span class="dot"></span>${esc(x.label)}</button>`).join('')}
      </div>

      <div class="card">
        <h3>Was war das Thema?</h3>
        <p class="dim" style="font-size:12px;margin:3px 0 9px">Mehrfachauswahl. Fließt in die Trainingsempfehlung ein.</p>
        ${FE.FLAGS.map((x) => `
          <button class="opt check ${f.flags.includes(x.id) ? 'on' : ''}" data-flag="${x.id}">
            <span class="box"></span>${esc(x.label)}</button>`).join('')}
      </div>

      <div class="card">
        <h3>Notiz</h3>
        <input id="asc-partners" placeholder="Mit wem?" value="${esc(f.partners)}" style="margin-top:9px">
        <input id="asc-note" placeholder="Wie war der Tag?" value="${esc(f.note)}" style="margin-top:10px">
      </div>

      <button class="btn primary block" data-save-ascent ${canSave ? '' : 'disabled'}
        style="margin:6px 0 30px">${canSave ? 'Eintragen' : 'Name und Gefühl fehlen noch'}</button>
  </div></div>`;
}

/**
 * Trägt eine Begehung ein.
 *
 * Sie landet an zwei Stellen: bei den Begehungen als Grundlage für Rang und Empfehlung,
 * und als Etappenprotokoll für die Höhenmeter und Abzeichen. Die Kennung F0 sorgt dafür,
 * dass sie den Wochenzyklus nicht weiterschiebt.
 */
function saveAscent() {
  const f = ascentForm;
  const a = {
    id: 'A' + Date.now(),
    date: Date.now(),
    name: (f.name || '').trim(),
    routeId: f.routeId || null,
    region: f.region || '',
    grade: FE.GRADES[f.gradeIdx].id,
    climbMeters: parseInt(f.meters, 10) || 0,
    durationMin: parseInt(f.minutes, 10) || 0,
    feel: f.feel,
    flags: f.flags,
    turnedBack: !!f.turnedBack,
    partners: (f.partners || '').trim(),
    note: (f.note || '').trim(),
    avgHr: parseInt(f.avgHr, 10) || 0,
  };

  const before = J.earnedBadges(badgeSnapshot()).map((b) => b.id);

  // Der Tag am Fels zählt als Training — aber nur einmal, und nur wenn er auch einer
  // war: Ein kurzer Übungssteig hakt keine Krafteinheit ab. Deckt die Begehung den Tag
  // ab, trägt sie die Kennung der offenen Etappe und rückt den Zyklus um genau eine
  // weiter; sonst steht sie unter der Sonderkennung außerhalb des Rhythmus.
  const score = FE.tourLoad(a);
  const covers = FE.coversStage(state.progress, a.date) && FE.countsAsTraining(score);
  const openStage = J.currentStage(state.progress);

  update((st) => {
    st.ascents = [...st.ascents, a];
    st.progress = [...st.progress, {
      stageId: covers ? openStage.id : J.EXTRA_STAGE_ID,
      kind: J.STAGE_KIND.FERRATA,
      meters: a.climbMeters,
      at: a.date,
      skipped: false,
      detail: a.name,
    }];
    if (a.routeId) st.plannedRouteIds = st.plannedRouteIds.filter((x) => x !== a.routeId);
  });

  ascentForm = null;
  const after = J.earnedBadges(badgeSnapshot());
  const fresh = after.filter((b) => !before.includes(b.id));
  if (fresh.length) {
    update((st) => { st.seenBadges = after.map((b) => b.id); });
    showBadgeDialog(fresh);
  }
  // Was hat der Tag gekostet, und was folgt daraus für den Plan?
  const etappe = covers ? ` Die Etappe „${openStage.title}" ist damit abgehakt.` : '';
  toast(`${FE.completionLine(a)}${etappe} ${FE.loadLabel(score)}. ${FE.planLine(score)}`);
  render();
}

function viewSettings() {
  const p = state.profile;
  return `
  <div class="screen">
    <h1>Einstellungen</h1>

    <div class="section-title"><span>Profil</span></div>
    <div class="card">
      <div class="field"><label>Körpergewicht in kg</label>
        <input type="number" inputmode="decimal" id="set-bw" value="${p.bodyweightKg}"></div>
      <div class="field"><label>Gewichtsstufe je Platte in kg</label>
        <input type="number" inputmode="decimal" id="set-step" value="${p.plateStepKg}"></div>
      <div class="field" style="margin-bottom:0"><label>Ziel-Klettersteig</label>
        <input id="set-target" value="${esc(p.targetFerrataName)}"></div>
    </div>

    <div class="section-title"><span>Ausstattung</span></div>
    <div class="card" style="padding:10px">
      ${Object.entries(D.STATIONS).filter(([k]) => k !== 'BODYWEIGHT').map(([k, v]) => `
        <div class="line" style="cursor:pointer" data-toggle-station="${k}">
          <div class="grow">
            <div style="font-weight:600;${p.stations.includes(k) ? '' : 'color:var(--text-low)'}">${esc(v.label)}</div>
            <div class="dim" style="font-size:12px">${esc(v.hint)}</div>
          </div>
          <span style="font-size:19px">${p.stations.includes(k) ? '🟦' : '⬜'}</span>
        </div>`).join('')}
    </div>

    <div class="section-title"><span>Trainingsblock</span></div>
    <div class="card">
      <p class="muted" style="font-size:13.5px;margin-top:0">Die App arbeitet in Blöcken zu ${D.CYCLE_WEEKS}
        Wochen: vier Wochen steigern, eine Woche entlasten. Nach einer längeren Pause ist es sinnvoll,
        den Block neu zu starten — dann beginnst du wieder mit reichlich Puffer statt an der Belastungsspitze.</p>
      <button class="btn ghost small" id="restart-cycle">↻ Block neu starten</button>
    </div>

    <div class="section-title"><span>Netz</span></div>
    <div class="card">
      <label class="row" style="cursor:pointer">
        <div class="grow">
          <div style="font-weight:600">Fotos aus dem Internet laden</div>
          <div class="dim" style="font-size:12.5px">Frei lizenzierte Bilder zu den Steigen von Wikimedia
            Commons — geladen erst, wenn du den Foto-Reiter eines Steigs öffnest.</div>
        </div>
        <input type="checkbox" id="set-webphotos" ${state.profile.webPhotosEnabled ? 'checked' : ''}>
      </label>
    </div>

    <div class="section-title"><span>Sicherung</span></div>
    <div class="card">
      <p class="muted" style="font-size:13.5px;margin-top:0">Deine Daten liegen ausschließlich in diesem
        Browser. Vor einem Gerätewechsel exportierst du sie am besten einmal.</p>
      <div class="btn-row">
        <button class="btn ghost small" id="export">⬇ Export</button>
        <button class="btn ghost small" id="import">⬆ Import</button>
      </div>
    </div>

    <div class="section-title"><span>App-Aktualisierung</span></div>
    <div class="card accent-sky">
      <p class="muted" style="font-size:13.5px;margin-top:0">Die Web-Variante frischt sich beim
        Öffnen selbst auf. Warst du gerade offline oder hakt etwas, kannst du hier von Hand
        nachladen.</p>
      <div class="row between" style="margin:12px 0">
        <span class="dim" style="font-size:12.5px">Installiert</span>
        <span style="font-weight:600">Version ${APP_VERSION}</span>
      </div>
      <button class="btn ghost small" id="check-update">↻ Jetzt auffrischen</button>
      <p class="dim" style="font-size:12px;margin:12px 0 0">Die Android-App kann sich selbst
        aktualisieren — dort findest du unter „Mehr" eine Schaltfläche, die neue Fassungen
        herunterlädt und installiert.</p>
    </div>

    <div class="section-title"><span>Samsung Health</span></div>
    <div class="card">
      <p class="muted" style="font-size:13.5px;margin-top:0">Die Web-Variante kann nicht direkt mit
        Samsung Health sprechen — dafür fehlt dem Browser der Zugang zu Health Connect. Wenn du die
        Verbindung möchtest, nutze die Android-App aus demselben Projekt; sie schreibt jede Einheit
        über Health Connect nach Samsung Health.</p>
    </div>

    <div class="card">
      <h2>Wie die App steigert</h2>
      <p class="muted" style="font-size:13.5px">Gesteigert wird nach der Doppelten Progression mit der
        2-für-2-Regel: Zuerst arbeitest du dich im Wiederholungsfenster nach oben. Erst wenn du das
        obere Ende in zwei Einheiten hintereinander erreichst, kommt Gewicht drauf — und die
        Wiederholungen fallen wieder auf den unteren Wert.</p>
      <p class="muted" style="font-size:13.5px">Bleibt der Fortschritt über drei Einheiten aus, nimmt
        die App bewusst etwa zehn Prozent zurück. Bei Halteübungen wie dem Dead Hang kommen fünf
        Sekunden dazu; ab einer Minute lohnt Zusatzgewicht mehr als noch längeres Hängen.</p>
      <p class="dim" style="font-size:10.5px;margin-bottom:0">FerrataFit Web · Version ${APP_VERSION} · Alle Daten bleiben in diesem Browser</p>
    </div>
  </div>`;
}

// ---------------------------------------------------------------------------
// Training
// ---------------------------------------------------------------------------

function startWorkout(dayId) {
  // Über eine offene Rückfrage muss erst entschieden werden — sonst löschte ein neuer
  // Start die gestrigen Sätze wortlos weg.
  if (resumeAsk) { toast('Es ist noch eine Einheit offen. Entscheide zuerst, ob du sie fortsetzt.'); return; }
  const now = Date.now();
  const day = D.dayById(dayId);
  const entries = D.exercisesFor(day, state.profile, state.hiddenExercises).map((ex) => {
    const sug = D.suggest(ex, state.sessions, state.profile, now, FE.recoveryState(state.ascents, now));
    return {
      sug,
      sets: Array.from({ length: sug.sets }, () => ({
        weightKg: sug.weightKg,
        reps: ex.progression === D.KIND.TIME ? 0 : sug.targetReps,
        seconds: sug.targetSeconds,
        done: false,
      })),
    };
  });
  active = {
    dayId, startedAt: now, entries, current: 0,
    // Die offene Etappe festhalten: Beim Abschließen wird gegen sie geprüft, nicht
    // gegen die dann offene — dazwischen kann eine Begehung den Zeiger bewegt haben.
    stageId: J.currentStage(state.progress).dayId === dayId ? J.currentStage(state.progress).id : null,
  };
  saveDraft();
  render();
}

function viewWorkout() {
  const w = active;
  const day = D.dayById(w.dayId);
  const entry = w.entries[w.current];
  if (!entry) return '';
  const ex = entry.sug.exercise;
  const totalSets = w.entries.reduce((s, e) => s + e.sets.length, 0);
  const doneSets = w.entries.reduce((s, e) => s + e.sets.filter((x) => x.done).length, 0);
  const usesWeight = ex.progression !== D.KIND.TIME;
  const color = adviceColor(entry.sug.advice);

  return `
  <div id="workout"><div class="wrap">
    <div class="wo-head">
      <div class="row">
        <button id="wo-cancel" style="font-size:20px;padding:4px 8px;color:var(--text-mid)">✕</button>
        <div class="grow">
          <div style="font-size:17px;font-weight:600">${esc(day.title)}</div>
          <div style="font-size:12.5px;color:var(--sky)">${doneSets} von ${totalSets} Sätzen</div>
        </div>
        <button id="wo-finish" style="color:var(--amber);font-weight:700;padding:6px 10px">Fertig</button>
      </div>
      <div class="bar" style="height:6px;margin-top:10px">
        <i style="width:${totalSets ? (doneSets / totalSets) * 100 : 0}%"></i></div>
    </div>

    <div class="chips">
      ${w.entries.map((e, i) => {
        const all = e.sets.every((s) => s.done);
        return `<button class="chip ${i === w.current ? 'on' : ''}" data-pick="${i}">
          ${all ? '✓ ' : ''}${esc(e.sug.exercise.name)}
          <span class="n">${e.sets.filter((s) => s.done).length}/${e.sets.length}</span></button>`;
      }).join('')}
    </div>

    <div class="card suggest accent-${adviceClass(entry.sug.advice)}">
      <div class="row between" style="align-items:flex-start">
        <div class="grow">
          <div style="font-size:19px;font-weight:600">${esc(ex.name)}</div>
          <div class="dim" style="font-size:12px">${ex.muscles.map(esc).join(' · ')}</div>
        </div>
        <span class="pill ${adviceClass(entry.sug.advice)}">${adviceLabel(entry.sug.advice)}</span>
      </div>
      <div class="row" style="align-items:flex-end;gap:8px;margin-top:16px">
        ${entry.sug.previousHeadline && entry.sug.advice === D.ADVICE.INCREASE
          ? `<span class="prev">${esc(entry.sug.previousHeadline)} →</span>` : ''}
        <span class="big" style="color:${color}">${esc(entry.sug.headline)}</span>
        <span class="muted" style="font-size:13.5px;padding-bottom:5px">× ${entry.sets.length} Sätze</span>
      </div>
      <p class="reason">${esc(entry.sug.reason)}</p>
      <details class="info">
        <summary>Wie geht die Übung?</summary>
        ${guideHtml(ex)}
      </details>
    </div>

    <div class="section-title"><span>Sätze</span><span>Pause ${ex.restSec} s</span></div>
    ${entry.sets.map((s, i) => `
      <div class="set ${s.done ? 'done' : ''}">
        <div class="row">
          <div class="num">${i + 1}</div>
          <span class="grow" style="font-weight:600">${usesWeight ? `Satz ${i + 1}` : 'Halten'}</span>
          ${s.done ? `<button data-copy="${i}" title="Auf restliche Sätze übernehmen"
            style="font-size:15px;padding:6px;color:var(--text-low)">⧉</button>` : ''}
          <button class="tick" data-done="${i}">✓</button>
        </div>
        <div class="steppers">
          ${usesWeight ? `
            ${stepper(ex.progression === D.KIND.REPS ? 'Zusatz' : 'Gewicht', D.fmtKg(s.weightKg), i, 'w')}
            ${stepper('Wdh.', String(s.reps), i, 'r')}`
          : stepper('Sekunden', `${s.seconds} s`, i, 's')}
        </div>
      </div>`).join('')}

    ${w.current < w.entries.length - 1
      ? `<button class="btn ghost" data-pick="${w.current + 1}" style="margin-top:6px">
           Nächste Übung: ${esc(w.entries[w.current + 1].sug.exercise.name)}</button>`
      : `<button class="btn go" id="wo-finish2" style="margin-top:6px">Einheit abschließen</button>`}
  </div></div>`;
}

// ---------------------------------------------------------------------------
// Etappen ohne Gerät: Dehnen, Regeneration, Ausdauer
// ---------------------------------------------------------------------------

/**
 * Die eingeschobene Erholungseinheit nach einer großen Tour.
 * Sie trägt die Sonderkennung F0 und rückt den Wochenzyklus deshalb nicht weiter —
 * die aufgeschobene Krafteinheit bleibt stehen, bis das Fenster um ist.
 */
function startRecoveryBreak() {
  const rec = FE.recoveryState(state.ascents, Date.now());
  if (!rec) return;
  const stage = {
    id: FE.EXTRA_STAGE_ID,
    kind: J.STAGE_KIND.RECOVERY,
    title: 'Erholung',
    subtitle: `Nach: ${rec.sourceName}`,
    icon: '🛌',
    meters: 30,
    mobilityIds: ['forearm_flexor', 'forearm_extensor', 'child_pose', 'pigeon', 'calf_wall', 'neck'],
    longHold: true,
  };
  activeStage = {
    stage,
    startedAt: Date.now(),
    items: stage.mobilityIds.map((id) => ({ id, done: false })),
  };
  saveDraft();
  render();
}

function startStage(stageId) {
  const stage = J.stageById(stageId);
  if (!stage) return;

  if (stage.kind === J.STAGE_KIND.STRENGTH) {
    startWorkout(stage.dayId);
    return;
  }
  const now = Date.now();
  if (stage.kind === J.STAGE_KIND.ENDURANCE) {
    activeStage = { stage, startedAt: now, minutes: 30, meters: 0 };
  } else {
    activeStage = {
      stage,
      startedAt: now,
      items: stage.mobilityIds.map((id) => ({ id, done: false })),
    };
  }
  saveDraft();
  render();
}

function viewStage() {
  const { stage } = activeStage;

  if (stage.kind === J.STAGE_KIND.ENDURANCE) {
    return `
    <div id="workout"><div class="wrap">
      <div class="wo-head">
        <div class="row">
          <button id="stage-cancel" style="font-size:20px;padding:4px 8px;color:var(--text-mid)">✕</button>
          <div class="grow">
            <div style="font-size:17px;font-weight:600">${esc(stage.title)}</div>
            <div style="font-size:12.5px;color:var(--sky)">${esc(stage.subtitle)}</div>
          </div>
        </div>
      </div>

      <div class="card accent-sky" style="margin-top:14px">
        <div style="font-size:40px;text-align:center;margin-bottom:6px">${stage.icon}</div>
        <p class="muted" style="font-size:13.5px;text-align:center;margin:0">${esc(stage.hint)}</p>
      </div>

      <div class="card">
        <h2>Was hast du gemacht?</h2>
        <p class="dim" style="font-size:12.5px;margin:0 0 16px">Trag ein, was zusammengekommen ist — grob reicht.</p>
        <div class="steppers">
          ${stepper('Minuten', `${activeStage.minutes}`, 0, 'min')}
          ${stepper('Höhenmeter', `${activeStage.meters}`, 0, 'hm')}
        </div>
        <p class="dim" style="font-size:12px;margin-top:14px">
          Höhenmeter zählen zusätzlich zu den ${stage.meters} Hm der Etappe — flach unterwegs?
          Dann lass sie einfach auf null.</p>
      </div>

      <button class="btn go" id="stage-finish" style="margin-top:6px">Etappe abschließen</button>
      <button class="link" id="stage-skip-inline">Doch überspringen</button>
      <div style="height:100px"></div>
    </div></div>`;
  }

  // Dehn- und Regenerationsetappen
  const done = activeStage.items.filter((i) => i.done).length;
  const total = activeStage.items.length;
  const holdNote = stage.longHold
    ? 'Heute länger halten — 30 bis 90 Sekunden. Das beruhigt das Nervensystem und unterstützt die Erholung.'
    : 'Jede Position ruhig 20 bis 30 Sekunden halten und dabei weiteratmen.';

  return `
  <div id="workout"><div class="wrap">
    <div class="wo-head">
      <div class="row">
        <button id="stage-cancel" style="font-size:20px;padding:4px 8px;color:var(--text-mid)">✕</button>
        <div class="grow">
          <div style="font-size:17px;font-weight:600">${esc(stage.title)}</div>
          <div style="font-size:12.5px;color:var(--sky)">${done} von ${total} erledigt</div>
        </div>
        <button id="stage-finish" style="color:var(--amber);font-weight:700;padding:6px 10px">Fertig</button>
      </div>
      <div class="bar" style="height:6px;margin-top:10px">
        <i style="width:${total ? (done / total) * 100 : 0}%"></i></div>
    </div>

    <div class="card accent-emerald" style="margin-top:14px">
      <p class="muted" style="font-size:13.5px;margin:0">${holdNote}</p>
    </div>

    ${activeStage.items.map((item, i) => {
      const m = J.mobilityById(item.id);
      const secs = stage.longHold ? Math.round(m.seconds * 1.6) : m.seconds;
      return `<div class="set ${item.done ? 'done' : ''}">
        <div class="row">
          <div class="num">${i + 1}</div>
          <div class="grow">
            <div style="font-weight:600">${esc(m.name)}</div>
            <div class="dim" style="font-size:12px">${secs} s${m.perSide ? ' pro Seite' : ''} · ${esc(m.zone)}</div>
          </div>
          <button class="tick" data-mob-done="${i}">✓</button>
        </div>
        <details class="info" style="margin-top:10px">
          <summary>Wie geht die Übung?</summary>
          ${guideHtml(m, { whyLabel: 'Warum' })}
        </details>
        ${item.done ? '' : `<button class="btn ghost small" style="margin-top:10px" data-mob-timer="${i}:${secs}">
          ⏱ ${secs} s Timer starten</button>`}
      </div>`;
    }).join('')}

    <button class="btn go" id="stage-finish2" style="margin-top:6px">Etappe abschließen</button>
    <button class="link" id="stage-skip-inline">Doch überspringen</button>
    <div style="height:100px"></div>
  </div></div>`;
}

function finishStage() {
  const { stage } = activeStage;
  let detail = '';

  if (stage.kind === J.STAGE_KIND.ENDURANCE) {
    const extra = activeStage.meters;
    detail = `${activeStage.minutes} min${extra ? ` · ${extra} Hm` : ''}`;
    const total = stage.meters + extra;
    activeStage = null;
    saveDraft();
    stopRest();
    completeStage({ ...stage, meters: total }, { detail });
    return;
  }

  const done = activeStage.items.filter((i) => i.done).length;
  if (done === 0) {
    activeStage = null;
    saveDraft();
    render();
    toast('Etappe verworfen — es war keine Übung abgehakt.');
    return;
  }
  detail = `${done} von ${activeStage.items.length} Übungen`;
  activeStage = null;
  saveDraft();
  stopRest();
  completeStage(stage, { detail });
}

function stepper(label, value, setIndex, field) {
  return `<div class="stepper">
    <div class="lbl">${label}</div>
    <div class="ctl">
      <button data-step="${setIndex}:${field}:-1">−</button>
      <span class="v">${esc(value)}</span>
      <button class="plus" data-step="${setIndex}:${field}:1">+</button>
    </div>
  </div>`;
}

function finishWorkout(note = '') {
  const w = active;
  const now = Date.now();
  const sets = w.entries.flatMap((e) =>
    e.sets.map((s, i) => (s.done ? {
      exerciseId: e.sug.exercise.id,
      setIndex: i,
      weightKg: s.weightKg,
      reps: s.reps,
      seconds: s.seconds,
    } : null)).filter(Boolean));

  if (!sets.length) {
    active = null;
    saveDraft();
    render();
    toast('Einheit verworfen — es war kein Satz abgehakt.');
    return;
  }

  const session = {
    id: (crypto.randomUUID ? crypto.randomUUID() : String(now)),
    dayId: w.dayId,
    startedAt: w.startedAt,
    finishedAt: now,
    sets,
    note,
  };

  active = null;

  saveDraft();
  stopRest();
  update((s) => {
    s.sessions.push(session);
    if (!s.profile.cycleStart) s.profile.cycleStart = now;
  });

  // Die Krafteinheit ist zugleich die offene Etappe — abhaken und gutschreiben.
  const stage = J.currentStage(state.progress);
  const minutes = Math.max(1, Math.round((now - w.startedAt) / 60000));
  if (stage.kind === J.STAGE_KIND.STRENGTH && stage.dayId === w.dayId) {
    completeStage(stage, { detail: `${sets.length} Sätze · ${minutes} min` });
  } else {
    toast(`Gespeichert: ${sets.length} Sätze in ${minutes} Minuten. Stark!`);
  }
}

// ---------------------------------------------------------------------------
// Pausenuhr
// ---------------------------------------------------------------------------

// Die Pause wird als Endzeitpunkt gehalten, nicht als herunterlaufender Zähler.
// Browser drosseln setInterval in Hintergrund-Tabs auf bis zu einmal pro Minute — ein
// Zähler bliebe also stehen, während die echte Pause weiterläuft. Aus dem Endzeitpunkt
// ergibt sich die Restzeit dagegen immer richtig, auch nach einem Neuladen der Seite.
let restEndsAt = 0, restTotal = 0, restPausedWith = null;

function remainingRest() {
  if (restPausedWith !== null) return restPausedWith;
  if (!restEndsAt) return 0;
  return Math.max(0, Math.round((restEndsAt - Date.now()) / 1000));
}

function startRest(seconds) {
  restTotal = seconds;
  restPausedWith = null;
  restEndsAt = Date.now() + seconds * 1000;
  saveDraft();
  drawRest();
  clearInterval(restTimer);
  restTimer = setInterval(() => {
    if (remainingRest() <= 0 && restPausedWith === null) { stopRest(); return; }
    drawRest();
  }, 500);
}

function addRest(seconds) {
  if (restPausedWith !== null) restPausedWith += seconds;
  else if (restEndsAt) restEndsAt += seconds * 1000;
  restTotal = Math.max(restTotal, remainingRest());
  saveDraft();
  drawRest();
}

function toggleRest() {
  if (restPausedWith !== null) {
    restEndsAt = Date.now() + restPausedWith * 1000;
    restPausedWith = null;
  } else {
    restPausedWith = remainingRest();
    restEndsAt = 0;
  }
  saveDraft();
  drawRest();
}

function stopRest() {
  clearInterval(restTimer);
  restTimer = null;
  restEndsAt = 0;
  restTotal = 0;
  restPausedWith = null;
  saveDraft();
  $('#rest')?.remove();
}

function drawRest() {
  let el = $('#rest');
  if (!el) {
    el = document.createElement('div');
    el.id = 'rest';
    document.body.appendChild(el);
  }
  const left = remainingRest();
  const mm = Math.floor(left / 60);
  const ss = String(left % 60).padStart(2, '0');
  // Die Knöpfe werden hier jede halbe Sekunde neu erzeugt. Ihre Handler hängen deshalb
  // nicht an ihnen selbst, sondern einmalig am Dokument — siehe bindGlobalOnce().
  el.innerHTML = `
    <div class="row">
      <div class="grow"><div class="l">Pause</div><div class="t">${mm}:${ss}</div></div>
      <button data-rest="add">+30 s</button>
      <button data-rest="toggle">${restPausedWith === null ? '⏸' : '▶'}</button>
      <button class="go" data-rest="skip">Fertig</button>
    </div>
    <div class="bar" style="height:5px;margin-top:8px">
      <i style="width:${restTotal ? (left / restTotal) * 100 : 0}%"></i></div>`;
}

// ---------------------------------------------------------------------------
// Zeichnen und Ereignisse
// ---------------------------------------------------------------------------

const TABS = [
  { id: 'home', label: 'Heute', icon: '🏠' },
  { id: 'plan', label: 'Plan', icon: '📋' },
  { id: 'progress', label: 'Fortschritt', icon: '📈' },
  { id: 'ferrata', label: 'Am Fels', icon: '🧗' },
  { id: 'settings', label: 'Mehr', icon: '⚙️' },
];

function render() {
  const view = { home: viewHome, plan: viewPlan, progress: viewProgress, ferrata: viewFerrata, settings: viewSettings }[tab];
  document.body.innerHTML = `
    <div id="app">${view()}</div>
    <nav>${TABS.map((t) => `<button data-tab="${t.id}" class="${tab === t.id ? 'on' : ''}">
      <span class="ic">${t.icon}</span><span>${t.label}</span></button>`).join('')}</nav>
    ${active ? viewWorkout() : activeStage ? viewStage() : ascentForm ? viewAscentForm() : ''}
    ${resumeAsk ? viewResumeAsk() : ''}`;
  if (remainingRest() > 0) drawRest();
  bindEvents();
}

/**
 * Handler, die nur einmal gesetzt werden dürfen.
 *
 * Die Knöpfe der Pausenuhr entstehen in drawRest() jede halbe Sekunde neu. Wären ihre
 * Handler wie alle anderen in bindEvents() gesetzt, wären sie nach dem ersten Tick tot —
 * „+30 s“, Pause und „Fertig“ hätten dann sichtbar nichts mehr getan.
 */
let globalsBound = false;
function bindGlobalOnce() {
  if (globalsBound) return;
  globalsBound = true;

  document.addEventListener('click', (e) => {
    const b = e.target.closest('[data-rest]');
    if (!b) return;
    const a = b.dataset.rest;
    if (a === 'add') addRest(30);
    else if (a === 'toggle') toggleRest();
    else if (a === 'skip') stopRest();
  });

  // Beim Verlassen der Seite sichern: Mobile Browser verwerfen Tabs im Hintergrund,
  // und pagehide ist das letzte Ereignis, das dabei zuverlässig noch ankommt.
  window.addEventListener('pagehide', saveDraft);
  document.addEventListener('visibilitychange', () => {
    if (document.visibilityState === 'hidden') saveDraft();
  });
}

/**
 * Rückfrage zu einer älteren angefangenen Einheit.
 *
 * Sie springt nicht ungefragt auf: Wer nach zwei Tagen die App öffnet und unvermittelt in
 * einem halben Training landet, weiß nicht, was er da vor sich hat — und hakt im Zweifel
 * Sätze ab, die er nie gemacht hat.
 */
function viewResumeAsk() {
  const h = Math.floor(draftAgeH(resumeAsk));
  const alter = h < 24 ? `vor ${h} Stunden` : h < 48 ? 'gestern' : `vor ${Math.floor(h / 24)} Tagen`;
  return `
  <div class="modal">
    <div class="sheet">
      <h3>Angefangene Einheit</h3>
      <p class="muted">Du hast ${alter} etwas begonnen und nicht abgeschlossen.
        Weitermachen oder verwerfen?</p>
      <div class="btn-row" style="margin-top:16px">
        <button class="btn ghost" data-resume="no">Verwerfen</button>
        <button class="btn primary" data-resume="yes">Weitermachen</button>
      </div>
    </div>
  </div>`;
}

function bindEvents() {
  bindGlobalOnce();

  document.querySelectorAll('[data-resume]').forEach((b) => {
    b.onclick = () => {
      const d = resumeAsk;
      resumeAsk = null;
      if (b.dataset.resume === 'yes') restoreDraft(d);
      else localStorage.removeItem(DRAFT_KEY);
      render();
    };
  });
  document.querySelectorAll('[data-tab]').forEach((b) => {
    b.onclick = () => { tab = b.dataset.tab; planOpen = null; render(); };
  });

  document.querySelectorAll('[data-start]').forEach((b) => {
    b.onclick = () => startWorkout(b.dataset.start);
  });

  document.querySelectorAll('[data-goto-plan]').forEach((b) => {
    b.onclick = () => { tab = 'plan'; planOpen = b.dataset.gotoPlan; render(); };
  });

  document.querySelectorAll('[data-day-toggle]').forEach((b) => {
    b.onclick = () => {
      planOpen = planOpen === b.dataset.dayToggle ? '' : b.dataset.dayToggle;
      render();
    };
  });

  document.querySelectorAll('[data-ex-row]').forEach((row) => {
    row.onclick = (e) => {
      if (e.target.closest('[data-hide]')) return;
      const d = row.querySelector('.detail');
      d.style.display = d.style.display === 'none' ? 'block' : 'none';
    };
  });

  document.querySelectorAll('[data-hide]').forEach((b) => {
    b.onclick = (e) => {
      e.stopPropagation();
      const id = b.dataset.hide;
      update((s) => {
        s.hiddenExercises = s.hiddenExercises.includes(id)
          ? s.hiddenExercises.filter((x) => x !== id) : [...s.hiddenExercises, id];
      });
    };
  });

  document.querySelectorAll('[data-trend]').forEach((b) => {
    b.onclick = () => { trendPick = b.dataset.trend; render(); };
  });

  document.querySelectorAll('[data-toggle-station]').forEach((b) => {
    b.onclick = () => {
      const k = b.dataset.toggleStation;
      update((s) => {
        const has = s.profile.stations.includes(k);
        const next = has ? s.profile.stations.filter((x) => x !== k) : [...s.profile.stations, k];
        s.profile.stations = [...new Set([...next, 'BODYWEIGHT'])];
      });
    };
  });

  // Etappen starten und überspringen
  document.querySelectorAll('[data-stage-start]').forEach((b) => {
    b.onclick = () => startStage(b.dataset.stageStart);
  });

  document.querySelectorAll('[data-recovery-break]').forEach((b) => {
    b.onclick = () => startRecoveryBreak();
  });
  document.querySelectorAll('[data-stage-skip]').forEach((b) => {
    b.onclick = () => {
      const stage = J.stageById(b.dataset.stageSkip);
      if (!stage) return;
      if (confirm(`„${stage.title}" überspringen?\n\nDie nächste Etappe wird frei, du bekommst aber keine Höhenmeter dafür.`)) {
        completeStage(stage, { skipped: true });
      }
    };
  });

  bindSettings();
  bindWorkout();
  bindStage();
  bindFerrata();
}

/** Ereignisse im Fels-Bereich und im Eintragsformular. */
function bindFerrata() {
  document.querySelectorAll('[data-region]').forEach((b) => {
    b.onclick = () => { ferrataRegion = b.dataset.region || null; ferrataMap = false; render(); };
  });

  document.querySelectorAll('[data-route-tab]').forEach((b) => {
    b.onclick = (ev) => { ev.stopPropagation(); routeTab = +b.dataset.routeTab; render(); };
  });
  document.querySelectorAll('[data-add-photo]').forEach((inp) => {
    inp.onchange = async () => {
      const file = inp.files && inp.files[0];
      if (!file) return;
      const routeId = inp.dataset.addPhoto;
      try {
        await PhotoDb.addPhoto(routeId, file);
        delete ownPhotoCache[routeId];
        loadOwnPhotos(routeId);
      } catch (e) {
        toast('Das Bild ließ sich nicht übernehmen.', true);
      }
    };
  });
  document.querySelectorAll('[data-del-photo]').forEach((b) => {
    b.onclick = async (ev) => {
      ev.stopPropagation();
      const routeId = b.dataset.photoRoute;
      await PhotoDb.deletePhoto(b.dataset.delPhoto).catch(() => {});
      delete ownPhotoCache[routeId];
      loadOwnPhotos(routeId);
    };
  });

  const mapToggle = document.querySelector('[data-ferrata-map]');
  if (mapToggle) mapToggle.onclick = () => { ferrataMap = !ferrataMap; render(); };

  document.querySelectorAll('[data-map-route]').forEach((el) => {
    el.onclick = () => {
      const id = el.dataset.mapRoute;
      // Punkte am selben Fels liegen übereinander — wiederholtes Tippen wechselt durch
      ferrataOpen = ferrataOpen === id ? null : id;
      render();
    };
  });

  document.querySelectorAll('[data-route]').forEach((el) => {
    el.onclick = (ev) => {
      // Der Stern liegt in derselben Zeile — sein Klick darf die Karte nicht aufklappen
      if (ev.target.closest('[data-plan]')) return;
      routeTab = 0;
      const id = el.dataset.route;
      ferrataOpen = ferrataOpen === id ? null : id;
      render();
    };
  });

  document.querySelectorAll('[data-plan]').forEach((b) => {
    b.onclick = (ev) => {
      ev.stopPropagation();
      const id = b.dataset.plan;
      update((st) => {
        st.plannedRouteIds = st.plannedRouteIds.includes(id)
          ? st.plannedRouteIds.filter((x) => x !== id)
          : [...st.plannedRouteIds, id];
      });
    };
  });

  const early = document.querySelector('[data-toggle-early]');
  if (early) early.onclick = () => { showTooEarly = !showTooEarly; render(); };

  document.querySelectorAll('[data-del-ascent]').forEach((b) => {
    b.onclick = () => {
      const id = b.dataset.delAscent;
      if (!confirm('Diese Begehung löschen?')) return;
      update((st) => {
        const gone = st.ascents.find((a) => a.id === id);
        st.ascents = st.ascents.filter((a) => a.id !== id);
        // Nur das zugehörige Protokoll entfernen, nicht jede Begehung desselben Tages
        if (gone) {
          st.progress = st.progress.filter(
            (pr) => !(pr.stageId === J.EXTRA_STAGE_ID && pr.at === gone.date));
        }
      });
    };
  });

  const openBtn = document.querySelector('[data-log-ascent]');
  if (openBtn) {
    openBtn.onclick = () => {
      ascentForm = {
        routeId: null, name: '', region: '', search: '', gradeIdx: 0,
        meters: '', minutes: '', avgHr: '', feel: null, flags: [], turnedBack: false,
        partners: '', note: '',
      };
      render();
    };
  }

  if (!ascentForm) return;

  // Freitext wird laufend übernommen, damit ein Steig außerhalb des Katalogs
  // nicht beim ersten Neuzeichnen verlorengeht.
  const search = document.getElementById('asc-search');
  if (search) {
    search.oninput = () => { ascentForm.search = search.value; ascentForm.name = search.value; };
    search.onchange = () => render();
    // Nach dem Neuzeichnen soll der Cursor dort stehen, wo er war
    if (ascentForm.search) {
      search.focus();
      search.setSelectionRange(search.value.length, search.value.length);
    }
  }

  document.querySelectorAll('[data-pick]').forEach((el) => {
    el.onclick = () => {
      const r = FERRATAS.find((x) => x.id === el.dataset.pick);
      if (!r) return;
      Object.assign(ascentForm, {
        routeId: r.id, name: r.name, region: r.region || '',
        gradeIdx: FE.gradeIndex(r.grade),
        meters: r.climbMeters ? String(r.climbMeters) : ascentForm.meters,
        minutes: r.totalMin ? String(r.totalMin) : ascentForm.minutes,
      });
      render();
    };
  });

  const clear = document.querySelector('[data-clear-route]');
  if (clear) {
    clear.onclick = () => {
      Object.assign(ascentForm, { routeId: null, name: '', search: '', region: '' });
      render();
    };
  }

  document.querySelectorAll('[data-grade]').forEach((b) => {
    b.onclick = () => { captureAscentInputs(); ascentForm.gradeIdx = +b.dataset.grade; render(); };
  });
  document.querySelectorAll('[data-turned]').forEach((b) => {
    b.onclick = () => { captureAscentInputs(); ascentForm.turnedBack = b.dataset.turned === '1'; render(); };
  });
  document.querySelectorAll('[data-feel]').forEach((b) => {
    b.onclick = () => { captureAscentInputs(); ascentForm.feel = b.dataset.feel; render(); };
  });
  document.querySelectorAll('[data-flag]').forEach((b) => {
    b.onclick = () => {
      captureAscentInputs();
      const id = b.dataset.flag;
      const on = ascentForm.flags.includes(id);
      // „Nichts davon" schließt die anderen aus, und umgekehrt
      if (id === 'RUND') ascentForm.flags = on ? [] : ['RUND'];
      else if (on) ascentForm.flags = ascentForm.flags.filter((x) => x !== id);
      else ascentForm.flags = [...ascentForm.flags.filter((x) => x !== 'RUND'), id];
      render();
    };
  });

  const close = document.querySelector('[data-close-ascent]');
  if (close) close.onclick = () => { ascentForm = null; render(); };

  const save = document.querySelector('[data-save-ascent]');
  if (save) save.onclick = () => { captureAscentInputs(); saveAscent(); };
}

/**
 * Rettet die Eingabefelder vor dem Neuzeichnen.
 *
 * Die Oberfläche baut sich bei jeder Änderung neu auf. Ohne diesen Schritt wäre alles
 * Getippte weg, sobald jemand eine Schwierigkeit antippt.
 */
function captureAscentInputs() {
  if (!ascentForm) return;
  const get = (id) => (document.getElementById(id) || {}).value;
  const search = get('asc-search');
  if (search !== undefined && !ascentForm.routeId) {
    ascentForm.search = search;
    ascentForm.name = search;
  }
  const m = get('asc-meters');       if (m !== undefined) ascentForm.meters = m;
  const t = get('asc-minutes');      if (t !== undefined) ascentForm.minutes = t;
  const hr = get('asc-hr');          if (hr !== undefined) ascentForm.avgHr = hr;
  const pa = get('asc-partners');    if (pa !== undefined) ascentForm.partners = pa;
  const no = get('asc-note');        if (no !== undefined) ascentForm.note = no;
}

function bindSettings() {
  const wp = document.getElementById('set-webphotos');
  if (wp) wp.onchange = () => update((st) => { st.profile.webPhotosEnabled = wp.checked; });
  const bw = $('#set-bw');
  if (bw) bw.onchange = () => {
    const v = parseFloat(bw.value);
    if (v > 0) update((s) => { s.profile.bodyweightKg = v; });
  };
  const st = $('#set-step');
  if (st) st.onchange = () => {
    const v = parseFloat(st.value);
    if (v > 0) update((s) => { s.profile.plateStepKg = v; });
  };
  const tg = $('#set-target');
  if (tg) tg.onchange = () => update((s) => { s.profile.targetFerrataName = tg.value; });

  const ba = $('#body-add');
  if (ba) ba.onclick = () => {
    const input = $('#body-weight');
    const kg = parseFloat((input?.value || '').replace(',', '.'));
    if (!kg || kg < 25 || kg > 300) {
      toast('Bitte ein Gewicht zwischen 25 und 300 kg eintragen.', true);
      return;
    }
    update((s) => {
      s.body = [...s.body.filter((b) => new Date(b.at).toDateString() !== new Date().toDateString()),
                { at: Date.now(), weightKg: kg }].sort((a, b) => a.at - b.at);
      s.profile.bodyweightKg = kg;
    });
    toast(`${kg} kg eingetragen — die Lastschätzungen rechnen ab jetzt damit.`);
  };

  const bf = $('#body-file');
  if (bf) bf.onclick = () => {
    const input = document.createElement('input');
    input.type = 'file';
    input.accept = '.csv,.txt,text/csv,text/plain';
    input.onchange = async () => {
      const file = input.files?.[0];
      if (!file) return;
      const result = parseBodyFile(await file.text());
      if (result.error) { toast(result.error, true); return; }
      update((s) => {
        s.body = mergeBody(s.body, result.measurements);
        const newest = J.Body.latest(s.body);
        if (newest && J.Body.isFresh(newest.at, Date.now())) s.profile.bodyweightKg = newest.weightKg;
      });
      toast(`${result.measurements.length} Messungen eingelesen`
        + (result.skipped ? `, ${result.skipped} Zeilen übersprungen` : '') + '.');
    };
    input.click();
  };

  const cu = $('#check-update');
  if (cu) cu.onclick = async () => {
    toast('Wird aufgefrischt…');
    try {
      // Zwischenspeicher leeren und den Service Worker neu holen, dann neu laden
      if ('serviceWorker' in navigator) {
        const regs = await navigator.serviceWorker.getRegistrations();
        await Promise.all(regs.map((r) => r.update()));
      }
      if (window.caches) {
        const keys = await caches.keys();
        await Promise.all(keys.map((k) => caches.delete(k)));
      }
      setTimeout(() => location.reload(), 600);
    } catch {
      toast('Auffrischen fehlgeschlagen — lade die Seite von Hand neu.', true);
    }
  };

  const rc = $('#restart-cycle');
  if (rc) rc.onclick = () => {
    update((s) => { s.profile.cycleStart = Date.now(); });
    toast('Neuer Block gestartet — Woche 1 mit reichlich Puffer.');
  };

  const ex = $('#export');
  if (ex) ex.onclick = () => {
    const blob = new Blob([JSON.stringify(state, null, 2)], { type: 'application/json' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `ferratafit-sicherung-${new Date().toISOString().slice(0, 10)}.json`;
    a.click();
    URL.revokeObjectURL(url);
    toast('Sicherung heruntergeladen.');
  };

  const im = $('#import');
  if (im) im.onclick = () => {
    const input = document.createElement('input');
    input.type = 'file';
    input.accept = 'application/json,.json';
    input.onchange = async () => {
      const file = input.files?.[0];
      if (!file) return;
      try {
        const parsed = JSON.parse(await file.text());
        if (!parsed.profile || !Array.isArray(parsed.sessions)) throw new Error('Format');
        state = { ...structuredClone(DEFAULT_STATE), ...parsed,
          profile: { ...DEFAULT_STATE.profile, ...parsed.profile } };
        save();
        render();
        toast('Sicherung eingelesen.');
      } catch {
        toast('Die Datei konnte nicht gelesen werden.', true);
      }
    };
    input.click();
  };
}

function bindStage() {
  if (!activeStage) return;
  const { stage } = activeStage;

  document.querySelectorAll('[data-mob-done]').forEach((b) => {
    b.onclick = () => {
      const i = +b.dataset.mobDone;
      activeStage.items[i].done = !activeStage.items[i].done;
      saveDraft();
      render();
    };
  });

  document.querySelectorAll('[data-mob-timer]').forEach((b) => {
    b.onclick = () => {
      const [i, secs] = b.dataset.mobTimer.split(':').map(Number);
      const m = J.mobilityById(activeStage.items[i].id);
      // Beidseitige Dehnungen laufen zweimal — der Timer deckt beide Seiten ab.
      startRest(m.perSide ? secs * 2 : secs);
    };
  });

  // Ausdauer-Etappe: Minuten und Höhenmeter einstellen
  document.querySelectorAll('[data-step]').forEach((b) => {
    const [, field, dir] = b.dataset.step.split(':');
    if (field !== 'min' && field !== 'hm') return;
    b.onclick = () => {
      const d = +dir;
      if (field === 'min') activeStage.minutes = Math.max(0, activeStage.minutes + d * 5);
      else activeStage.meters = Math.max(0, activeStage.meters + d * 50);
      saveDraft();
      render();
    };
  });

  const cancel = document.getElementById('stage-cancel');
  if (cancel) cancel.onclick = () => {
    if (confirm('Etappe verlassen? Der Fortschritt darin geht verloren.')) {
      activeStage = null;
      saveDraft();
      stopRest();
      render();
    }
  };

  ['stage-finish', 'stage-finish2'].forEach((id) => {
    const el = document.getElementById(id);
    if (el) el.onclick = finishStage;
  });

  const skip = document.getElementById('stage-skip-inline');
  if (skip) skip.onclick = () => {
    if (confirm(`„${stage.title}" überspringen?\n\nDie nächste Etappe wird frei, du bekommst aber keine Höhenmeter dafür.`)) {
      activeStage = null;
      saveDraft();
      stopRest();
      completeStage(stage, { skipped: true });
    }
  };
}

function bindWorkout() {
  if (!active) return;

  document.querySelectorAll('[data-pick]').forEach((b) => {
    b.onclick = () => { active.current = +b.dataset.pick; saveDraft(); render(); };
  });

  document.querySelectorAll('[data-step]').forEach((b) => {
    b.onclick = () => {
      const [idx, field, dir] = b.dataset.step.split(':');
      const entry = active.entries[active.current];
      const set = entry.sets[+idx];
      const ex = entry.sug.exercise;
      const d = +dir;
      if (field === 'w') set.weightKg = Math.max(0, set.weightKg + d * ex.increment);
      else if (field === 'r') set.reps = Math.max(0, set.reps + d);
      else set.seconds = Math.max(0, set.seconds + d * 5);
      saveDraft();
      render();
    };
  });

  document.querySelectorAll('[data-done]').forEach((b) => {
    b.onclick = () => {
      const i = +b.dataset.done;
      const entry = active.entries[active.current];
      const wasDone = entry.sets[i].done;
      entry.sets[i].done = !wasDone;
      saveDraft();
      render();
      if (!wasDone) startRest(entry.sug.exercise.restSec);
    };
  });

  document.querySelectorAll('[data-copy]').forEach((b) => {
    b.onclick = () => {
      const i = +b.dataset.copy;
      const entry = active.entries[active.current];
      const src = entry.sets[i];
      entry.sets.forEach((s, j) => {
        if (j > i && !s.done) { s.weightKg = src.weightKg; s.reps = src.reps; s.seconds = src.seconds; }
      });
      saveDraft();
      render();
      toast('Auf die restlichen Sätze übernommen.');
    };
  });

  const cancel = $('#wo-cancel');
  if (cancel) cancel.onclick = () => {
    if (confirm('Training verwerfen? Die bisher abgehakten Sätze gehen dabei verloren.')) {
      active = null;
      saveDraft();
      stopRest();
      render();
    }
  };

  const finish = () => {
    const done = active.entries.reduce((s, e) => s + e.sets.filter((x) => x.done).length, 0);
    const total = active.entries.reduce((s, e) => s + e.sets.length, 0);
    const note = prompt(
      `${done} von ${total} Sätzen sind abgehakt. Nicht abgehakte Sätze werden nicht gespeichert.\n\nNotiz (optional):`, '');
    if (note === null) return;   // Abbruch im Dialog
    finishWorkout(note);
  };
  const f1 = $('#wo-finish'); if (f1) f1.onclick = finish;
  const f2 = $('#wo-finish2'); if (f2) f2.onclick = finish;
}

// ---------------------------------------------------------------------------
// Start
// ---------------------------------------------------------------------------

let resumeAsk = null;   // angefangene Einheit, über die noch entschieden werden muss

function boot() {
  if (!state.profile.onboarded) { renderOnboarding(); return; }

  const d = loadDraft();
  if (d && !d.workout && !d.stage) localStorage.removeItem(DRAFT_KEY);
  else if (d) {
    const alter = draftAgeH(d);
    // Erkennbar vergessen — wortlos wegräumen, nicht danach fragen
    if (alter >= EXPIRY_H) localStorage.removeItem(DRAFT_KEY);
    else if (alter < RESUME_WINDOW_H) restoreDraft(d);
    else resumeAsk = d;
  }
  render();
}

boot();

if ('serviceWorker' in navigator) {
  window.addEventListener('load', () => {
    navigator.serviceWorker.register('./sw.js').catch(() => {
      // Offline-Betrieb ist ein Zusatz — ohne Service Worker läuft die App trotzdem.
    });
  });
}

// Nur für den Rauchtest: Die Ansichten sollen sich ohne Browser aufrufen lassen.
export const __test = {
  viewFerrata,
  viewAscentForm: (f) => { const o = ascentForm; ascentForm = f; const h = viewAscentForm(); ascentForm = o; return h; },
  startWorkout, finishWorkout, saveDraft, restoreDraft, loadDraft,
  active: () => active,
  state: () => state,
  saveAscent: (f) => { ascentForm = f; saveAscent(); },
  routeCard: (id, tab) => {
    const o = ferrataOpen, ot = routeTab;
    ferrataOpen = id; routeTab = tab;
    ownPhotoCache[id] = ownPhotoCache[id] || [];
    const h = routeCard(ferrataById(id), FE.FIT.ZU_FRUEH);
    ferrataOpen = o; routeTab = ot;
    return h;
  },
  reset: () => { active = null; activeStage = null; resumeAsk = null; },
};
