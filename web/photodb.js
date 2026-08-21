/**
 * Eigene Fotos im Browser — in IndexedDB, nicht in localStorage.
 *
 * localStorage fasst rund fünf Megabyte und hält den ganzen Trainingsbestand; zehn
 * Fotos hätten ihn gesprengt und damit das Speichern der Einheiten gleich mit. IndexedDB
 * fasst Hunderte Megabyte und speichert Binärdaten ohne Umweg über Text.
 *
 * Die Bilder werden vor dem Ablegen verkleinert (längste Kante 1200 Pixel) — fürs
 * Wiedererkennen reicht das, und die Datenbank wächst nicht ins Unermessliche.
 */
const DB_NAME = 'ferratafit-photos';
const STORE = 'photos';

function openDb() {
  return new Promise((resolve, reject) => {
    if (typeof indexedDB === 'undefined') { reject(new Error('kein IndexedDB')); return; }
    const req = indexedDB.open(DB_NAME, 1);
    req.onupgradeneeded = () => {
      const db = req.result;
      if (!db.objectStoreNames.contains(STORE)) {
        db.createObjectStore(STORE, { keyPath: 'id' }).createIndex('routeId', 'routeId');
      }
    };
    req.onsuccess = () => resolve(req.result);
    req.onerror = () => reject(req.error);
  });
}

function tx(db, mode, fn) {
  return new Promise((resolve, reject) => {
    const t = db.transaction(STORE, mode);
    const store = t.objectStore(STORE);
    const out = fn(store);
    t.oncomplete = () => resolve(out && out.result !== undefined ? out.result : out);
    t.onerror = () => reject(t.error);
  });
}

/** Verkleinert eine Bilddatei zu einem JPEG-Blob. */
function shrink(file, maxEdge = 1200) {
  return new Promise((resolve, reject) => {
    const url = URL.createObjectURL(file);
    const img = new Image();
    img.onload = () => {
      const f = Math.min(1, maxEdge / Math.max(img.width, img.height));
      const c = document.createElement('canvas');
      c.width = Math.max(1, Math.round(img.width * f));
      c.height = Math.max(1, Math.round(img.height * f));
      c.getContext('2d').drawImage(img, 0, 0, c.width, c.height);
      URL.revokeObjectURL(url);
      c.toBlob((b) => (b ? resolve(b) : reject(new Error('kein Blob'))), 'image/jpeg', 0.85);
    };
    img.onerror = () => { URL.revokeObjectURL(url); reject(new Error('Bild unlesbar')); };
    img.src = url;
  });
}

export async function addPhoto(routeId, file) {
  const blob = await shrink(file);
  const db = await openDb();
  const id = 'P' + Date.now();
  await tx(db, 'readwrite', (s) => s.put({ id, routeId, blob, addedAt: Date.now() }));
  return id;
}

export async function listPhotos(routeId) {
  const db = await openDb();
  return new Promise((resolve, reject) => {
    const req = db.transaction(STORE, 'readonly').objectStore(STORE).index('routeId').getAll(routeId);
    req.onsuccess = () => resolve(req.result || []);
    req.onerror = () => reject(req.error);
  });
}

export async function deletePhoto(id) {
  const db = await openDb();
  await tx(db, 'readwrite', (s) => s.delete(id));
}
