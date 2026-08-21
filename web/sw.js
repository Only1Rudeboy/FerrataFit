/**
 * Service Worker: macht die App offline nutzbar — wichtig, weil im Keller oder in der
 * Halle oft kein Empfang ist.
 *
 * Strategie: Beim Installieren alles in den Cache legen, danach zuerst aus dem Cache
 * ausliefern und im Hintergrund auffrischen. So startet die App sofort und holt sich
 * Aktualisierungen still nach.
 */

// Beim Anheben der App-Version mit hochzählen — dann verwirft der Browser den alten
// Bestand. Ein Import aus app.js ginge nicht: Ein klassischer Service Worker kennt
// keine ES-Module.
const CACHE = 'ferratafit-v10';
const ASSETS = [
  './',
  './index.html',
  './app.css',
  './app.js',
  './data.js',
  './exercises.js',
  './bodyweight.js',
  './journey.js',
  './bodyimport.js',
  './ferrata.js',
  './ferratas.js',
  './ferrageo.js',
  './icon.svg',
  './icon-192.png',
  './manifest.webmanifest',
];

self.addEventListener('install', (event) => {
  event.waitUntil(
    caches.open(CACHE)
      .then((cache) => cache.addAll(ASSETS))
      .then(() => self.skipWaiting())
  );
});

self.addEventListener('activate', (event) => {
  event.waitUntil(
    caches.keys()
      .then((keys) => Promise.all(keys.filter((k) => k !== CACHE).map((k) => caches.delete(k))))
      .then(() => self.clients.claim())
  );
});

self.addEventListener('fetch', (event) => {
  if (event.request.method !== 'GET') return;

  event.respondWith(
    caches.match(event.request).then((cached) => {
      const network = fetch(event.request)
        .then((response) => {
          if (response && response.status === 200 && response.type === 'basic') {
            const copy = response.clone();
            caches.open(CACHE).then((cache) => cache.put(event.request, copy));
          }
          return response;
        })
        .catch(() => cached);

      return cached || network;
    })
  );
});
