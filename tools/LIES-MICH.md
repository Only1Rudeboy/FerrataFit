# Werkzeuge

Diese Skripte erzeugen die generierten Dateien der App. Sie liegen im Repo, damit
die Herkunft der Daten nachvollziehbar bleibt — die Korrekturen der Prüfrunden
stehen darin als Code mit Begründung, nicht als verlorene Chat-Prosa.

- `gen_routes.py` — erzeugt `data/FerrataRoutes.kt` und `web/ferratas.js` aus der
  Routen-Recherche. Enthält die Streichliste (Dubletten, versicherte Wanderwege)
  und alle Gradkorrekturen.
- `gen_geo.py` + `geo-result.json` — erzeugt `data/FerrataGeo.kt` und
  `web/ferrageo.js` aus der Koordinaten-Recherche. Enthält die Handkorrektur
  für den Karhorn-Westgrat.

Beide Skripte erwarten ihre Eingabedaten im selben Ordner.
