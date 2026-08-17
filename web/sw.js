/**
 * Service Worker: macht die App offline nutzbar — wichtig, weil im Keller oder in der
 * Halle oft kein Empfang ist.
 *
 * Strategie: Beim Installieren alles in den Cache legen, danach zuerst aus dem Cache
 * ausliefern und im Hintergrund auffrischen. So startet die App sofort und holt sich
 * Aktualisierungen still nach.
 */

// Version hochzählen, wenn sich Dateien ändern — dadurch wird der alte Cache verworfen.
const CACHE = 'ferratafit-v3';
const ASSETS = [
  './',
  './index.html',
  './app.css',
  './app.js',
  './data.js',
  './exercises.js',
  './journey.js',
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
