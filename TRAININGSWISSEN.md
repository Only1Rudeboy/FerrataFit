# Trainingswissen hinter FerrataFit

Diese Datei hält fest, worauf die Logik der App beruht — und wo es herkommt. Wenn du
später etwas anpassen willst, findest du hier die Begründung für jede Regel.

---

## 1. Was der Klettersteig tatsächlich fordert

Klettersteiggehen ist keine Kraftsportart im klassischen Sinn. Was limitiert, ist fast
immer dasselbe, und zwar in dieser Reihenfolge:

1. **Griffkraft und Unterarm-Ausdauer.** Der brennende, aufgepumpte Unterarm ist der
   Grund, warum Einsteiger auf mittelschweren Routen alle 10–15 Minuten pausieren
   müssen. Kein Ausdauertraining der Welt bereitet darauf vor — das muss gezielt
   trainiert werden.
2. **Zugkraft im Oberkörper.** Für senkrechte Passagen und Überhänge, wo du dich an
   Klammern und Seil hochziehst.
3. **Rumpfspannung.** Wer durchhängt, hängt in den Armen und ermüdet doppelt so schnell.
   Körperspannung hält dich nah an der Wand.
4. **Beinkraft für hohe Tritte.** Am Steig steigst du von Klammer zu Klammer — meist
   einbeinig, oft kniehoch. Der Abstieg fordert die Oberschenkel oft härter als der Aufstieg.
5. **Grundausdauer** für Zustieg und Höhenmeter.

Empfohlener Vorlauf für eine ambitionierte Tour: **10–12 Wochen** bei **2–4 Einheiten
pro Woche**. Als Richtwert für „bereit für eine B-Route mit kurzem Zustieg“ gilt:
30 Sekunden freihängen können und eine Zwei-Stunden-Wanderung mit 500 Höhenmetern schaffen.

**Daraus folgt in der App:**
- Dead Hang, Klimmzug und hängendes Knieheben sind mit `ferrataFocus = 3` markiert und
  stehen weit vorne in den Einheiten — da bist du noch frisch.
- Der Zug-Tag ist Tag A, nicht Tag C.
- Die Kennzahl „Steig-Bereitschaft“ gewichtet Griffkraft mit 30 %, Zugkraft mit 25 %,
  Rumpf mit 20 %, Beine mit 15 % und Regelmäßigkeit mit 10 %.

---

## 2. Wie gesteigert wird: Doppelte Progression + 2-für-2-Regel

Das ist der Kern deines Wunsches „nach einer gewissen Zeit soll die App sagen, nimm 25 kg“.

**Doppelte Progression** heißt: Es werden zwei Größen nacheinander gesteigert, nicht nur
das Gewicht. Zuerst arbeitest du dich innerhalb eines Wiederholungsfensters nach oben
(z. B. 8 → 12), erst danach kommt Gewicht drauf — und die Wiederholungen fallen wieder
auf den unteren Wert.

Die **2-für-2-Regel** verschärft das sinnvoll: Aufgelastet wird erst, wenn du das obere
Ende des Fensters in **zwei aufeinanderfolgenden Einheiten** erreichst. Ein einzelner
guter Tag reicht nicht. Das verhindert Steigerungen, die du danach zwei Wochen lang
nicht sauber bewältigst.

**Konkret in der App** (`Progression.kt`):

| Situation | Was die App vorschlägt |
|---|---|
| Übung noch nie trainiert | Startschätzung aus dem Körpergewicht, konservativ |
| Oberes Ende in 2 Einheiten erreicht | **Gewicht + 1 Stufe**, Wiederholungen zurück auf Minimum |
| Oberes Ende einmal erreicht | Gewicht halten — „noch eine Einheit, dann wird aufgelastet“ |
| Normal unterwegs | Gleiche Last, eine Wiederholung mehr als zuletzt |
| 3 Einheiten ohne Fortschritt | **−10 %** und neu anlaufen |
| Entlastungswoche | **−15 % Last, ein Satz weniger**, 3–4 Wiederholungen Puffer |

Die Steigerung ist bewusst kleinteilig: 2,5 kg bei Arm- und Schulterübungen, 5 kg
(eine Steckplatte) bei den großen Zug- und Beinübungen.

Realistisches Tempo: Am Anfang geht eine Steigerung pro Woche und Übung, später alle
zwei bis vier Wochen. Das ist normal und kein Zeichen von Stillstand.

---

## 3. Wochenperiodisierung und Entlastung

Vier Wochen durchgehend an der Belastungsgrenze funktioniert nicht — der Körper baut
in der Erholung auf, nicht im Training.

Die App arbeitet in **5-Wochen-Blöcken** mit absteigendem Puffer (Wiederholungen in
Reserve, also wie viele am Satzende noch gegangen wären):

| Woche | Puffer | Charakter |
|---|---|---|
| 1 | 3 Wdh. | Aufbau, locker starten |
| 2 | 2 Wdh. | Steigerung |
| 3 | 1 Wdh. | Intensiv |
| 4 | 1 Wdh. | Spitze |
| 5 | 3–4 Wdh. | **Entlastung**: −15 % Last, −1 Satz |

Entlastungswochen werden üblicherweise alle 4–8 Wochen eingeplant; bei viel Volumen
oder stressigem Alltag eher am unteren Ende. Fünf Wochen sind für dein Ziel
(Allgemeinfitness, kein Wettkampf) ein guter Mittelweg.

Nach einer längeren Pause solltest du den Block über *Einstellungen → Block neu starten*
zurücksetzen, damit du wieder mit Puffer einsteigst statt an der Spitze.

---

## 4. Dead Hang: die wichtigste Einzelübung

Für die Griffkraft gilt ein eigenes Schema, weil hier Zeit statt Gewicht gesteigert wird:

- Einsteiger halten 10–20 Sekunden, 3–4 Sätze, 60–90 Sekunden Pause.
- **Pro Woche etwa 5 Sekunden dazu.** Die 30-Sekunden-Marke fällt meist nach 3–4 Wochen.
- Ab **60 Sekunden** in allen Sätzen bringt Zusatzgewicht mehr als noch längeres Hängen.
- 2–3 Einheiten pro Woche; der Fortschritt läuft typischerweise 8–12 Wochen, dann flacht er ab.
- Wichtig: abbrechen, *bevor* der Griff komplett aufgeht, und die Schultern aktiv nach
  unten ziehen statt passiv in den Gelenken zu hängen.

Die App setzt genau das um: `increment = 5.0` Sekunden, Steigerung nach der 2-für-2-Regel,
und ab 60 Sekunden ein Hinweis auf Zusatzgewicht.

---

## 5. Wiederholungsbereich und Zielsetzung

Dein Ziel ist Allgemeinfitness mit leichter Definition, kein Muskelaufbau um jeden Preis.
Deshalb liegen die Fenster bei **8–15 Wiederholungen** statt im schweren 3–5er-Bereich:

- 8–12 bei den großen Übungen (Latzug, Brustpresse, Beinstrecker)
- 10–15 bei kleineren Muskelgruppen und Bein-Isolation
- 12–20 bei Waden und Liegestützen

Das trainiert Kraftausdauer — genau das, was eine zwei- bis dreistündige Route verlangt —
und belastet Sehnen und Gelenke weniger als schwere Einzelwiederholungen.

---

## 6. Der Split

Drei Einheiten pro Woche, mindestens ein Ruhetag dazwischen:

**Tag A — Zug & Griff** (der Klettersteig-Tag)
Klimmzug → Latzug → Rudern → Knieheben hängend → Bizeps → Dead Hang

**Tag B — Beine & Steigkraft**
Step-up → Beinstrecker → Beinbeuger → Ausfallschritt → Wadenheben → Plank

**Tag C — Druck & Stabilität**
Brustpresse → Schulterdrücken → Butterfly → Reverse Butterfly → Trizeps → Dead Hang

**Warum diese Reihenfolge:** Der Zug-Tag steht vorne, weil Griff- und Zugkraft zuerst
limitieren — die willst du ausgeruht trainieren. Der Bein-Tag folgt, damit sich die
Unterarme erholen, während die Beine arbeiten. Der Druck-Tag hält die Schultern im
Gleichgewicht: Wer nur zieht, zieht sich die Haltung nach vorne.

Ergänzend, außerhalb der App: Wandern, Treppensteigen oder Bouldern. Bouldern trainiert
Grifftechnik und Höhengewöhnung zugleich und ist die beste Ergänzung überhaupt.

---

## Quellen

- [Krafttraining für Klettersteig — Der Klettersteiger](https://derklettersteiger.de/krafttraining-fuer-klettersteig/)
- [Vorbereitung & Training — DAV Summit Club](https://www.dav-summit-club.de/service/sicherheit-am-berg/vorbereitung-training)
- [Training — Deutscher Alpenverein](https://www.alpenverein.de/thema/training)
- [Vorbereitung auf einen Klettersteig — OUTSIDEstories](https://outside-stories.de/kooperationspartner/vorbereitung-auf-einen-klettersteig-trainings-und-ausruestungstipps)
- [How to prepare physically for a via ferrata route — SMExperiences](https://smexperiences.com/en/blog/how-to-prepare-physically-for-a-via-ferrata-route/)
- [Via ferrata climbing: 8 tips — Climbers Paradise Tirol](https://www.climbers-paradise.com/en/blog/article/via-ferrata-climbing-8-tips-for-a-successful-ascent/)
- [How to climb Via Ferratas — British Mountaineering Council](https://thebmc.co.uk/en/via-ferrata)
- [The 2 for 2 Rule — Workouts by Winter](https://workoutsbywinter.substack.com/p/the-2-for-2-rule-a-fool-proof-formula)
- [The Double Progression Method — Legion Athletics](https://legionathletics.com/double-progression/)
- [Double Progression Explained — Mesostrength](https://mesostrength.com/blog/double-progression)
- [A Practical Approach to Deloading (PDF, Sheffield Hallam University)](https://shura.shu.ac.uk/35313/3/Bell-APracticalApproach(AM).pdf)
- [Deloading Practices in Strength and Physique Sports — Sports Medicine Open](https://link.springer.com/article/10.1186/s40798-024-00691-y)
- [Effects of a one-week deload period — PMC](https://www.ncbi.nlm.nih.gov/pmc/articles/PMC10809978/)
- [Progressing for Hypertrophy — RP Strength](https://rpstrength.com/blogs/articles/progressing-for-hypertrophy)
- [Deadhang Progressions: Beginner to Elite](https://deadhangs.com/deadhang-progressions/)
- [Two Progression Methods Improve Max Dead Hang Time — Mountain Tactical Institute](https://mtntactical.com/research/mini-study-two-progression-methods-improve-max-dead-hang-time-in-untrained-athletes/)
- [Kraftstation Übungen — Hop-Sport](https://hop-sport.at/blog/kraftstation-ubungen-ganzkorper-trainingsplan-fur-anfanger)
- [Trainingsplan Ganzkörper-Workout an der Kraftstation — HAMMER](https://www.hammer.de/fitnesswissen/kraftstation-trainingsplan)
- [Develop Workout Experiences with Health Connect — Android Developers](https://developer.android.com/health-and-fitness/health-connect/experiences/workouts)
- [Accessing Samsung Health Data through Health Connect — Samsung Developer](https://developer.samsung.com/health/blog/en/accessing-samsung-health-data-through-health-connect)
