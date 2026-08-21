# Werkzeuge

Diese Skripte erzeugen die generierten Dateien der App. Sie liegen im Repo, damit
die Herkunft der Daten nachvollziehbar bleibt — die Korrekturen der Prüfrunden
stehen darin als Code mit Begründung, nicht als verlorene Chat-Prosa.

- `gen_geo.py` + `geo-result.json` — erzeugt `data/FerrataGeo.kt` und
  `web/ferrageo.js` aus der Koordinaten-Recherche. Enthält die Handkorrektur
  für den Karhorn-Westgrat (eigener Einstieg am Westfuß).

- `gen_media.py` + `photos-chunk*.json` + `topo-chunk*.json` — erzeugt
  `data/FerrataMedia.kt` und `web/ferramedia.js`. Prüft jede Commons-Datei über die
  API auf Existenz und freie Lizenz; nur CC0/CC BY/CC BY-SA/PD/FAL kommen durch.
  Braucht Netz.

Das Skript für den Routenkatalog (`FerrataRoutes.kt` / `web/ferratas.js`) ist mit
dem Arbeitsordner der Sitzung verloren gegangen, in der es lief. Seine
Entscheidungen sind dokumentiert: die Streichliste und alle Gradkorrekturen stehen
in der Commit-Nachricht von d232873 und im Dateikopf von `FerrataRoutes.kt`.
Wer den Katalog neu erzeugen will, schreibt das Skript anhand dieser beiden
Quellen neu — die Daten selbst sind vollständig im Repo.
