# Werkzeuge

Diese Skripte erzeugen die generierten Dateien der App. Sie liegen im Repo, damit
die Herkunft der Daten nachvollziehbar bleibt — die Korrekturen der Prüfrunden
stehen darin als Code mit Begründung, nicht als verlorene Chat-Prosa.

- `gen_geo.py` + `geo-result.json` — erzeugt `data/FerrataGeo.kt` und
  `web/ferrageo.js` aus der Koordinaten-Recherche. Enthält die Handkorrektur
  für den Karhorn-Westgrat (eigener Einstieg am Westfuß).

Das Skript für den Routenkatalog (`FerrataRoutes.kt` / `web/ferratas.js`) ist mit
dem Arbeitsordner der Sitzung verloren gegangen, in der es lief. Seine
Entscheidungen sind dokumentiert: die Streichliste und alle Gradkorrekturen stehen
in der Commit-Nachricht von d232873 und im Dateikopf von `FerrataRoutes.kt`.
Wer den Katalog neu erzeugen will, schreibt das Skript anhand dieser beiden
Quellen neu — die Daten selbst sind vollständig im Repo.
