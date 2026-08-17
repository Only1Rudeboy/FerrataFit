/**
 * FerrataFit Web — Oberfläche und Zustandsverwaltung.
 *
 * Bewusst ohne Framework: Der Zustand ist ein einziges Objekt, jede Änderung schreibt
 * ihn in den localStorage und zeichnet den aktiven Bereich neu. Bei dieser Größe ist
 * das übersichtlicher als eine Bibliothek und läuft ohne Build-Schritt.
 */

import * as D from './data.js';
import * as J from './journey.js';

const STORAGE_KEY = 'ferratafit.v1';

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
  },
  sessions: [],
  hiddenExercises: [],
  /** Abgeschlossene Etappen in der Reihenfolge, in der sie gegangen wurden. */
  progress: [],
  /** Bereits vergebene Abzeichen — gemerkt, damit neue erkennbar bleiben. */
  seenBadges: [],
};

let state = load();
let tab = 'home';
let planOpen = null;
let active = null;      // laufende Krafteinheit
let activeStage = null; // laufende Mobility-/Ausdauer-Etappe
let restTimer = null;
let trendPick = null;

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

  // Kraftetappen zeigen direkt, was heute aufgelastet wird.
  let upsHtml = '';
  let statsHtml = '';
  if (stage.kind === J.STAGE_KIND.STRENGTH) {
    const day = J.dayForStage(stage);
    const exs = D.exercisesFor(day, p, state.hiddenExercises);
    const ups = exs.map((e) => D.suggest(e, state.sessions, p, now))
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
        ${stage.kind === J.STAGE_KIND.STRENGTH && deload
          ? `<div class="pill emerald" style="margin-bottom:10px">Entlastungswoche — bewusst leichter</div>` : ''}
        ${upsHtml}
        ${stage.hint ? `<p class="muted" style="margin:0 0 14px;font-size:13.5px">${esc(stage.hint)}</p>` : ''}
        <button class="btn primary" data-stage-start="${stage.id}">▶ Etappe starten</button>
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
          ${exs.map((ex) => exerciseRow(ex, D.suggest(ex, state.sessions, p, now), false)).join('')}
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
      <div class="muted">${esc(ex.cue)}</div>
      <div class="why">${esc(ex.why)}</div>
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

    <div class="section-title"><span>Sicherung</span></div>
    <div class="card">
      <p class="muted" style="font-size:13.5px;margin-top:0">Deine Daten liegen ausschließlich in diesem
        Browser. Vor einem Gerätewechsel exportierst du sie am besten einmal.</p>
      <div class="btn-row">
        <button class="btn ghost small" id="export">⬇ Export</button>
        <button class="btn ghost small" id="import">⬆ Import</button>
      </div>
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
      <p class="dim" style="font-size:10.5px;margin-bottom:0">FerrataFit Web · Version 1.1 · Alle Daten bleiben in diesem Browser</p>
    </div>
  </div>`;
}

// ---------------------------------------------------------------------------
// Training
// ---------------------------------------------------------------------------

function startWorkout(dayId) {
  const now = Date.now();
  const day = D.dayById(dayId);
  const entries = D.exercisesFor(day, state.profile, state.hiddenExercises).map((ex) => {
    const sug = D.suggest(ex, state.sessions, state.profile, now);
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
  active = { dayId, startedAt: now, entries, current: 0 };
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
        <summary>Ausführung &amp; Warum</summary>
        <div class="info-block"><div class="h">So geht's</div><div class="b">${esc(ex.cue)}</div></div>
        <div class="info-block"><div class="h">Warum am Steig</div><div class="b">${esc(ex.why)}</div></div>
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

function startStage(stageId) {
  const stage = J.stageById(stageId);
  if (!stage) return;

  if (stage.kind === J.STAGE_KIND.STRENGTH) {
    startWorkout(stage.dayId);
    return;
  }
  if (stage.kind === J.STAGE_KIND.ENDURANCE) {
    activeStage = { stage, minutes: 30, meters: 0 };
  } else {
    activeStage = {
      stage,
      items: stage.mobilityIds.map((id) => ({ id, done: false })),
    };
  }
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
          <summary>Ausführung &amp; Warum</summary>
          <div class="info-block"><div class="h">So geht's</div><div class="b">${esc(m.cue)}</div></div>
          <div class="info-block"><div class="h">Warum</div><div class="b">${esc(m.why)}</div></div>
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
    stopRest();
    completeStage({ ...stage, meters: total }, { detail });
    return;
  }

  const done = activeStage.items.filter((i) => i.done).length;
  if (done === 0) {
    activeStage = null;
    render();
    toast('Etappe verworfen — es war keine Übung abgehakt.');
    return;
  }
  detail = `${done} von ${activeStage.items.length} Übungen`;
  activeStage = null;
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

let restLeft = 0, restTotal = 0, restRunning = false;

function startRest(seconds) {
  restLeft = seconds;
  restTotal = seconds;
  restRunning = true;
  drawRest();
  clearInterval(restTimer);
  restTimer = setInterval(() => {
    if (!restRunning) return;
    restLeft--;
    if (restLeft <= 0) { stopRest(); return; }
    drawRest();
  }, 1000);
}

function stopRest() {
  clearInterval(restTimer);
  restTimer = null;
  restRunning = false;
  restLeft = 0;
  $('#rest')?.remove();
}

function drawRest() {
  let el = $('#rest');
  if (!el) {
    el = document.createElement('div');
    el.id = 'rest';
    document.body.appendChild(el);
  }
  const mm = Math.floor(restLeft / 60);
  const ss = String(restLeft % 60).padStart(2, '0');
  el.innerHTML = `
    <div class="row">
      <div class="grow"><div class="l">Pause</div><div class="t">${mm}:${ss}</div></div>
      <button data-rest="add">+30 s</button>
      <button data-rest="toggle">${restRunning ? '⏸' : '▶'}</button>
      <button class="go" data-rest="skip">Fertig</button>
    </div>
    <div class="bar" style="height:5px;margin-top:8px">
      <i style="width:${restTotal ? (restLeft / restTotal) * 100 : 0}%"></i></div>`;
}

// ---------------------------------------------------------------------------
// Zeichnen und Ereignisse
// ---------------------------------------------------------------------------

const TABS = [
  { id: 'home', label: 'Heute', icon: '🏠' },
  { id: 'plan', label: 'Plan', icon: '📋' },
  { id: 'progress', label: 'Fortschritt', icon: '📈' },
  { id: 'settings', label: 'Mehr', icon: '⚙️' },
];

function render() {
  const view = { home: viewHome, plan: viewPlan, progress: viewProgress, settings: viewSettings }[tab];
  document.body.innerHTML = `
    <div id="app">${view()}</div>
    <nav>${TABS.map((t) => `<button data-tab="${t.id}" class="${tab === t.id ? 'on' : ''}">
      <span class="ic">${t.icon}</span><span>${t.label}</span></button>`).join('')}</nav>
    ${active ? viewWorkout() : activeStage ? viewStage() : ''}`;
  if (restLeft > 0) drawRest();
  bindEvents();
}

function bindEvents() {
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
  document.querySelectorAll('[data-rest]').forEach((b) => {
    b.onclick = () => {
      const a = b.dataset.rest;
      if (a === 'add') { restLeft += 30; restTotal = Math.max(restTotal, restLeft); drawRest(); }
      else if (a === 'toggle') { restRunning = !restRunning; drawRest(); }
      else stopRest();
    };
  });
}

function bindSettings() {
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
      render();
    };
  });

  const cancel = document.getElementById('stage-cancel');
  if (cancel) cancel.onclick = () => {
    if (confirm('Etappe verlassen? Der Fortschritt darin geht verloren.')) {
      activeStage = null;
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
      stopRest();
      completeStage(stage, { skipped: true });
    }
  };
}

function bindWorkout() {
  if (!active) return;

  document.querySelectorAll('[data-pick]').forEach((b) => {
    b.onclick = () => { active.current = +b.dataset.pick; render(); };
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
      render();
    };
  });

  document.querySelectorAll('[data-done]').forEach((b) => {
    b.onclick = () => {
      const i = +b.dataset.done;
      const entry = active.entries[active.current];
      const wasDone = entry.sets[i].done;
      entry.sets[i].done = !wasDone;
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
      render();
      toast('Auf die restlichen Sätze übernommen.');
    };
  });

  const cancel = $('#wo-cancel');
  if (cancel) cancel.onclick = () => {
    if (confirm('Training verwerfen? Die bisher abgehakten Sätze gehen dabei verloren.')) {
      active = null;
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

function boot() {
  if (!state.profile.onboarded) renderOnboarding();
  else render();
}

boot();

if ('serviceWorker' in navigator) {
  window.addEventListener('load', () => {
    navigator.serviceWorker.register('./sw.js').catch(() => {
      // Offline-Betrieb ist ein Zusatz — ohne Service Worker läuft die App trotzdem.
    });
  });
}
