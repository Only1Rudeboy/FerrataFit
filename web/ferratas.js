// Die Vorarlberger Klettersteige. Erzeugt aus derselben Quelle wie
// android/.../data/FerrataRoutes.kt -- beide Fassungen muessen deckungsgleich sein.
//
// Aufnahmekriterium: durchgehende Sicherung ueber eine relevante Strecke und echter
// Klettercharakter. Ein Wanderweg mit dreissig Metern Drahtseil an einer ausgesetzten
// Stelle steht hier nicht drin, auch wenn ihn Tourismusseiten als Klettersteig fuehren.
//
// climbMeters meint die Hoehenmeter im gesicherten Steig, nie den ganzen Tagesaufstieg.
// verified: false steht dort, wo die Quellen sich widersprechen -- die Anzeige weist
// darauf hin, statt Geratenes wie gepruefte Angaben aussehen zu lassen.

export const FERRATAS = [
 {
  "id": "alpin-live-linke-route",
  "name": "Alpin live – linke Route",
  "grade": "D/E",
  "area": "Klettersteigpark Alpin live, Felswand unterhalb der Lünersee-Staumauer bei Brand",
  "region": "Brandnertal (Rätikon)",
  "crux": "E/F",
  "climbMeters": 80,
  "lengthMeters": 80,
  "approachMin": 15,
  "ferrataMin": 50,
  "descentMin": 15,
  "totalMin": 80,
  "startAlt": 1573,
  "season": "Mai bis Oktober",
  "summary": "Die etwas leichtere der beiden Linien im Klettersteigpark Alpin live, aber immer noch ein reiner Kraftsteig. Sie gliedert sich in drei Abschnitte: unten rund 40 Meter im Bereich C/D, in der Mitte 20 Meter im Grad D/E mit einem Überhang, oben 20 Meter im Grad D. Die unteren Abschnitte beider Routen sind untereinander verbunden, sodass sich Runden legen lassen. Üblich ist, rechts im E einzusteigen und über die linke Linie wieder abzuklettern. Für Fortgeschrittene mit solider C/D-Erfahrung ist die linke Route der sinnvolle Einstieg in die Anlage.",
  "approach": "Vom Parkplatz an der Talstation der Lünerseebahn (rund 1566 m) kurz dem Bach folgen und linkshaltend über eine Schutthalde zum Einstieg bei der weißen Informationstafel, rund 10 bis 15 Minuten. Die Versicherungen der linken Route beginnen links der Tafel.",
  "descent": "Kein Gehabstieg vorhanden. Man klettert über eine der Linien ab oder lässt sich an einem Umlenkpunkt ablassen, danach rund 10 Minuten zurück zum Parkplatz.",
  "gear": "Klettersteigset mit Bandfalldämpfer, Helm, Gurt und Handschuhe. Ein 40-Meter-Halbseil zum Abseilen bzw. Ablassen sowie Rastschlingen sind empfehlenswert.",
  "highlights": [
   "Drei Abschnitte von C/D bis D/E, gut zum Steigern",
   "Verbindungen zur rechten Route erlauben Runden statt Einzelbegehungen",
   "Talnah, praktisch kein Zustieg",
   "Kletterpark Brand direkt nebenan"
  ],
  "warnings": [
   "Trotz der Bezeichnung Übungsklettersteig kein Anfängerziel",
   "Kein offizieller Abstiegsweg, Partner zum Ablassen einplanen",
   "Bei Nässe nicht begehbar",
   "Die Aufteilung der Linien wird in den Quellen unterschiedlich beschrieben (zwei Hauptrouten bei via-ferrata.de, drei Varianten bei klettersteig.de)"
  ],
  "sources": [
   "https://www.via-ferrata.de/klettersteige/topo/alpin-live-klettersteige",
   "https://klettersteig.de/klettersteig/sportklettersteig_alpin_live_brand_luenersee/38",
   "https://www.bergsteigen.com/touren/klettersteig/luenersee-uebungs-klettersteig-alpin-live/"
  ],
  "verified": false
 },
 {
  "id": "alpin-live-rechte-route",
  "name": "Alpin live – rechte Route",
  "grade": "C/D",
  "area": "Klettersteigpark Alpin live, Felswand unterhalb der Lünersee-Staumauer bei Brand",
  "region": "Brandnertal (Rätikon)",
  "crux": "E/F",
  "climbMeters": 80,
  "lengthMeters": 80,
  "approachMin": 15,
  "ferrataMin": 60,
  "descentMin": 15,
  "totalMin": 90,
  "startAlt": 1573,
  "season": "Mai bis Oktober",
  "summary": "Die spektakulärere der beiden Linien im Klettersteigpark Alpin live und der härteste Klettersteig Vorarlbergs. Sie gliedert sich in drei Abschnitte: unten rund 40 Meter im Bereich B/C, in der Mitte 20 Meter im Grad D, oben 20 Meter im Grad E mit einem nahezu waagrechten, überhängenden Dach, das quer unter dem Fels durchführt. Weil die Zwischenverankerungen sehr eng gesetzt sind, bleibt die Sturzhöhe gering, was das Arbeiten an der eigenen Grenze erlaubt. Die Anlage führt zu keinem Gipfel und keiner Hütte, sie ist reines Krafttraining am Fels. Ohne solide C/D-Erfahrung und gute Arm- und Rumpfkraft gar nicht erst einsteigen.",
  "approach": "Vom Parkplatz an der Talstation der Lünerseebahn (rund 1566 m) kurz dem Bach folgen und linkshaltend über eine Schutthalde zum Einstieg bei der schon vom Parkplatz sichtbaren weißen Informationstafel aufsteigen, rund 10 bis 15 Minuten. Die Versicherungen der rechten Route beginnen rechts der Tafel.",
  "descent": "Es gibt keinen Gehabstieg. Man klettert eine der Nachbarlinien ab oder lässt sich an einem der Umlenkpunkte ablassen. Ein kurzes Seil und ein Partner zum Ablassen sind deshalb sinnvoll. Vom Wandfuß sind es rund 10 Minuten zurück zum Parkplatz.",
  "gear": "Klettersteigset mit Bandfalldämpfer, Helm, Gurt und feste Handschuhe. Kletterschuhe bringen an den Platten Vorteile. Zusätzlich empfehlenswert: 40-Meter-Halbseil zum Abseilen bzw. Ablassen, Expressschlingen und Karabiner zum Rasten.",
  "highlights": [
   "Nahezu waagrechte Dachquerung im Grad E, die spektakulärste Einzelstelle Vorarlbergs",
   "Enge Seilfixierungen und dadurch geringe Sturzhöhe",
   "Sehr kurzer Zustieg, als Feierabend- oder Trainingsrunde nutzbar",
   "Gut kombinierbar mit dem Saulakopf-Klettersteig am selben Tag"
  ],
  "warnings": [
   "Extremer Kraftsteig, nur für sehr starke Klettersteiggeher",
   "In den Überhängen gibt es kein Aussteigen ins Gelände, wer schlappmacht, muss abgelassen werden",
   "Kein offizieller Abstiegsweg, ohne Seil und Abseilgerät sind die Abseilstellen nutzlos",
   "Bei Nässe sind die Platten nicht zu halten",
   "Zur Spitzenbewertung uneinheitlich: klettersteig.de nennt für die Dachpassage E/F, via-ferrata.de und bergsteigen.com bleiben bei E",
   "Nicht mit dem Böser-Tritt-Steig zu verwechseln, das ist ein versicherter Wanderweg im selben Gebiet"
  ],
  "sources": [
   "https://www.via-ferrata.de/klettersteige/topo/alpin-live-klettersteige",
   "https://klettersteig.de/klettersteig/sportklettersteig_alpin_live_brand_luenersee/38",
   "https://www.bergsteigen.com/touren/klettersteig/luenersee-uebungs-klettersteig-alpin-live/"
  ],
  "verified": false
 },
 {
  "id": "kellenegg",
  "name": "Klettersteig Kellenegg",
  "grade": "B/C",
  "area": "Kellenegg am Ortsrand von Brand",
  "region": "Brandnertal (Rätikon)",
  "crux": "C",
  "climbMeters": 60,
  "lengthMeters": 80,
  "approachMin": 15,
  "ferrataMin": 30,
  "descentMin": 20,
  "totalMin": 75,
  "season": "Mai bis Oktober",
  "familyFriendly": true,
  "summary": "Kurzer, talnaher Einsteigersteig direkt am Ortsrand von Brand, bewusst entschärft angelegt. Auf rund 60 Höhenmetern und 80 Metern gesicherter Strecke reihen sich Steilstufe, Kante, Rampe, eine steile Platte und mehrere leicht überhängende Klammernpassagen aneinander. Weil sehr viele Trittklammern verbaut sind, klettert man im Grunde wie an einer Leiter, was auch Neulingen den Zugang öffnet. Der Großteil liegt bei B bis B/C, eine Querung und ein kleiner Überhang erreichen C und verlangen kurz Armkraft. Die Sicherung endet an einem Baum, danach führt ein unversicherter Waldpfad zurück ins Dorf.",
  "approach": "Vom Ortskern Brand am Kletterpark Brandnertal vorbei und dem beschilderten Wanderweg Kellenegg folgen, ab Parkplatz Kletterpark rund 15 Minuten, ab Parkplatz Dorfbahn etwa 25 Minuten. Mit öffentlichem Verkehr: Landbus bis Haltestelle Gemeindezentrum Brand. Parkraum ist knapp, an Schönwettertagen früh anreisen. Am Kletterpark lässt sich die komplette Klettersteigausrüstung ausleihen.",
  "descent": "Vom Ausstieg am Baum kurz bergauf zum Wanderweg Kellenegg, auf diesem links hinunter am Einstieg vorbei zurück ins Ortszentrum, rund 20 Minuten. Der Rückweg ist unversichert und stellenweise erdig-rutschig, Zurückklettern durch den Steig ist nicht vorgesehen.",
  "gear": "Komplettes Klettersteigset, Helm und feste Bergschuhe. Für Kinder ein Set im passenden Gewichtsbereich, Leihmaterial gibt es im Kletterpark Brandnertal direkt am Zustieg.",
  "highlights": [
   "Sehr dicht mit Eisenbügeln ausgestattet, einer der besten Erstlingssteige in Vorarlberg",
   "Nur 15 Gehminuten vom Ortszentrum Brand",
   "Schattige Nordostwand, auch im Hochsommer gut begehbar",
   "Ausrüstungsverleih und Kletterpark direkt am Ausgangspunkt"
  ],
  "warnings": [
   "Kein Notausstieg, wer einsteigt, muss die Linie zu Ende klettern",
   "Bei Nässe wird die Felsplatte glatt, dann nicht einsteigen",
   "Steinschlaggefahr durch vorausgehende Seilschaften, Helm auflassen",
   "Die beiden C-Stellen kommen früh, unerfahrene Kinder dort zusätzlich sichern",
   "Zu Einstiegshöhe (rund 1020 bis 1090 m) und gesicherter Länge (60 bis 140 m) widersprechen sich die Quellen, deshalb sind Ein- und Ausstiegshöhe hier nicht angegeben"
  ],
  "sources": [
   "https://www.bergsteigen.com/touren/klettersteig/kellenegg-klettersteig/",
   "https://klettersteig.de/klettersteig/klettersteig_kellenegg/2491",
   "https://www.via-ferrata.de/klettersteige/topo/klettersteig-kellenegg",
   "https://www.vorarlberg.travel/route/klettersteig-kellenegg-brand/",
   "https://www.bergfex.at/sommer/vorarlberg/touren/klettersteig/900119,klettersteig-kellenegg/"
  ]
 },
 {
  "id": "abendrot-widerschrofen",
  "name": "Abendrot-Klettersteig Widerschrofen",
  "grade": "D/E",
  "area": "Widerschrofen (Wiedaschrofa) bei Schnepfau, rechte der beiden Routen",
  "region": "Bregenzerwald",
  "crux": "E",
  "climbMeters": 100,
  "lengthMeters": 120,
  "approachMin": 10,
  "ferrataMin": 60,
  "descentMin": 15,
  "totalMin": 85,
  "startAlt": 760,
  "season": "Durch die geringe Höhe lange begehbar, bergsteigen.com nennt Februar bis Oktober",
  "summary": "Der Abendrot-Klettersteig ist die schwerere der beiden Routen am Widerschrofen, ebenfalls 2013 von der Bergrettung gebaut. Auf rund 100 Klettermetern steht die Wand fast durchgehend senkrecht oder überhängt. Die harten Stellen sind eine Plattenpassage nach dem ersten Band, ein Überhang im Mittelteil und die Ausstiegswand, erst die letzten Meter werden mit C wieder milder. Etwa auf halber Höhe gibt es eine Rastbank mit Blick zur Kanisfluh. Der Steig ist ausdrücklich sehr guten Kletterern mit viel Armkraft vorbehalten, der Kraftbedarf liegt deutlich über dem, was die Buchstaben vermuten lassen. Zwischen den beiden Linien besteht eine Verbindung, sodass sich beide an einem Tag kombinieren lassen.",
  "approach": "Anfahrt über die L200 nach Mellau und weiter nach Schnepfau, Parkplatz in der 180-Grad-Kurve am östlichen Ortsende. Auf der Forststraße bis zur Verbauung, dort rechts abzweigen, das meist trockene Bachbett queren und in rund 10 Minuten zum Einstieg der rechten Route aufsteigen.",
  "descent": "Über den Ausstieg hinaus durch den Zaun, links auf den Wanderweg und über die Forststraße in etwa 15 Minuten zurück zum Parkplatz.",
  "gear": "Komplette Klettersteigausrüstung, Helm und Handschuhe; Kletterschuhe von Vorteil, für Schwächere ein Sicherungsseil.",
  "highlights": [
   "Einer der schwersten Klettersteige Vorarlbergs auf engstem Raum",
   "Durchgehend senkrecht bis überhängend, extrem ausgesetzt",
   "Rastbank auf halber Höhe mit Blick zur Kanisfluh",
   "Nur 10 Minuten Zustieg",
   "Verbindung zum benachbarten Wälder-Klettersteig vorhanden"
  ],
  "warnings": [
   "Bewertung uneinheitlich: bergsteigen.com und via-ferrata.de nennen D/E, outdooractive und vorarlberg.travel führen den Steig insgesamt als E",
   "Extrem kraftraubend und ausgesetzt, ausschließlich für sehr gute Kletterer mit hoher Armkraft",
   "Wer schon an der Einstiegswand scheitert, sollte umkehren",
   "Bachbett im Zustieg bei Hochwasser meiden",
   "Kein sinnvoller Notausstieg in der Steilwand"
  ],
  "sources": [
   "https://www.bergsteigen.com/touren/klettersteig/abendrot-klettersteig-widerschrofen/",
   "https://www.via-ferrata.de/klettersteige/topo/waelderklettersteig-und-abendrot-klettersteig",
   "https://www.vorarlberg.travel/route/schnepfau-abendrot-klettersteig/",
   "https://www.outdooractive.com/de/route/klettersteig/bregenzerwald/abendrot-klettersteig-am-widerschrofen/17762400/"
  ],
  "verified": false
 },
 {
  "id": "walder-widerschrofen",
  "name": "Wälder-Klettersteig Widerschrofen",
  "grade": "D",
  "area": "Widerschrofen (Wiedaschrofa) bei Schnepfau, linke der beiden Routen",
  "region": "Bregenzerwald",
  "climbMeters": 100,
  "lengthMeters": 120,
  "approachMin": 10,
  "ferrataMin": 60,
  "descentMin": 15,
  "totalMin": 85,
  "startAlt": 760,
  "season": "Durch die geringe Höhe lange begehbar, bergsteigen.com nennt Februar bis Oktober",
  "summary": "Der Wälder-Klettersteig steht an den Felswänden des Widerschrofen zwischen Mellau und Au und wurde im Frühsommer 2013 von Mitgliedern der Bergrettung eingerichtet. Trotz nur rund 100 Klettermetern ist er ein ernstzunehmender Sportklettersteig im Grad D: Bereits die Einstiegswand ist eine der Schlüsselstellen, weiter oben folgt nach einer großen Rastplattform eine leicht überhängende Passage im selben Grad. Die Absicherung gilt als sehr gut, das Gelände ist steil und griffarm, gefragt ist vor allem Armkraft. Er ist die leichtere der beiden Routen am Widerschrofen, die Nachbarlinie Abendrot legt noch einen Grad drauf. Weil der Einstieg auf nur 760 Metern liegt, ist der Steig deutlich länger im Jahr begehbar als die hochalpinen Touren.",
  "approach": "Anfahrt über die L200 von Bregenz oder Dornbirn Richtung Mellau und weiter nach Schnepfau. Der kleine Klettersteig-Parkplatz liegt in einer 180-Grad-Kurve am östlichen Ortsende. Vom Parkplatz auf der Forststraße bis zur Verbauung, dort rechts dem Schild Wälder-Klettersteig folgen, das meist trockene Bachbett queren und zum Einstieg hinaufsteigen, etwa 10 Minuten.",
  "descent": "Am Ausstieg durch den Zaun, dann links auf den Wanderweg und über die Forststraße zurück zum Parkplatz, rund 15 Minuten.",
  "gear": "Komplette Klettersteigausrüstung und Helm, für schwächere Kletterer zusätzlich ein Sicherungsseil.",
  "highlights": [
   "Kurze, aber durchgehend anspruchsvolle Sportkletterei im Grad D",
   "Sehr gute Absicherung, 2013 von der Bergrettung gebaut",
   "Nur 10 Minuten Zustieg, ideal für einen halben Tag",
   "Große Rastplattform im oberen Teil",
   "Tieflage macht ihn zur Übergangszeit-Option, wenn oben noch Schnee liegt"
  ],
  "warnings": [
   "Die Einstiegswand ist bereits Schlüsselstelle im Grad D, wer dort Probleme hat, sollte umkehren",
   "Der Zustieg quert ein normalerweise trockenes Bachbett, bei Hochwasser nicht begehen",
   "Kein Steig für Einsteiger oder Kinder",
   "Helm zwingend, für Schwächere ein zusätzliches Sicherungsseil einplanen"
  ],
  "sources": [
   "https://www.bergsteigen.com/touren/klettersteig/waelder-klettersteig-widerschrofen/",
   "https://www.via-ferrata.de/klettersteige/topo/waelderklettersteig-und-abendrot-klettersteig",
   "https://www.bregenzerwald.at/aktivitaet/wiedaschrofa-klettersteige-schnepfau/"
  ]
 },
 {
  "id": "wandfluh",
  "name": "Wandfluh-Klettersteig",
  "grade": "C",
  "area": "Wandfluh (1574 m) oberhalb Sonntag-Stein, Biosphärenpark Großes Walsertal",
  "region": "Großes Walsertal / Lechquellengebirge",
  "crux": "C/D",
  "climbMeters": 110,
  "lengthMeters": 200,
  "approachMin": 30,
  "ferrataMin": 45,
  "descentMin": 35,
  "totalMin": 115,
  "startAlt": 1450,
  "summitAlt": 1574,
  "season": "Juni bis Oktober, zusätzlich abhängig von den Betriebszeiten der Seilbahn Sonntag-Stein",
  "summary": "Der jüngste Steig der Region: im Herbst 2023 an der Wandfluh im nördlichen Lechquellengebirge gebaut und im Frühsommer 2024 offiziell eröffnet, mit 65 Klebehaken und 20 Bügeln. Auf rund 200 Metern gesicherter Strecke und etwa 110 Höhenmetern beginnt er in grasdurchsetztem Fels und wird nach oben zunehmend alpin, mit versteckten Tritten und ausgesetzten Passagen. Die Schwierigkeit hält sich in langen C-Stellen mit kurzen C/D-Abschnitten, die letzten Meter zum Ausstieg fallen auf A/B ab. Die Nordwand liegt im Schatten und trocknet langsam, Notausstiege gibt es keine. Der Ausstieg liegt kurz unterhalb des Aussichtspunkts Wandfluh mit weitem Blick über den Biosphärenpark.",
  "approach": "Mit der Seilbahn Sonntag-Stein von der Talstation in Sonntag (888 m) zur Bergstation auf 1306 m, Parkplatz an der Talstation. Von der Bergstation etwa 30 Minuten der Straße folgen, am Grillplatz vorbei, dann rechts abbiegen und den Schildern zum Klettergarten folgen; ein kurzer Waldweg führt zum Einstieg auf rund 1450 m. Ohne Seilbahn geht es von der Lutzbrücke in rund 1:15 Stunden zu Fuß oder mit dem Rad hinauf.",
  "descent": "Vom Ausstieg auf dem bewaldeten Gipfelplateau der Wandfluh (1574 m) über den Wanderweg zurück zur Bergstation, rund 30 Minuten, alternativ über Partnom etwa 40 Minuten. Rückfahrt mit der Seilbahn, Betriebszeiten prüfen. Für den Abstieg ist Trittsicherheit nötig, das Gelände ist teils steil und grasig.",
  "gear": "Komplettes Klettersteigset mit Helm, Handschuhe und profilierte Bergschuhe wegen der grasdurchsetzten Passagen und des erdigen Ausstiegs.",
  "highlights": [
   "Neuester Klettersteig der Region, eröffnet 2024",
   "Einziger richtiger Klettersteig im Bergsteigerdorf Großes Walsertal",
   "Rundblick über den Biosphärenpark Großes Walsertal",
   "Bequemer Zustieg per Seilbahn, kompakte Halbtagestour",
   "Direkt neben dem Klettergarten Wandfluh"
  ],
  "warnings": [
   "Bewertung uneinheitlich: bergsteigen.com nennt C/D, via-ferrata.de und vorarlberg.travel führen den Steig als C; die Beschreibungen decken sich (lange C-Passagen, kurz C/D)",
   "Kein Notausstieg vorhanden",
   "Grasdurchsetzter und teils brüchiger Fels, bei Nässe erhebliche Rutschgefahr",
   "Nicht für Anfänger geeignet, viele lange und teils ausgesetzte C-Passagen",
   "Nordseitige Wand trocknet langsam und kann früh im Jahr noch Altschnee führen",
   "Rückweg ist an die Betriebszeiten der Seilbahn Sonntag-Stein gebunden"
  ],
  "sources": [
   "https://www.bergsteigen.com/touren/klettersteig/wandfluh-klettersteig/",
   "https://www.via-ferrata.de/klettersteige/topo/wandfluh-klettersteig-grosses-walsertal",
   "https://www.vorarlberg.travel/route/wandfluh-klettersteig-sonntag-stein/",
   "https://www.bergsteigerdoerfer.org/2490-0-Klettersteig-Grosses-Walsertal.html"
  ]
 },
 {
  "id": "kanzelwand-erlebnis-walsersteig",
  "name": "Kanzelwand-Erlebnis-Klettersteig (Walsersteig)",
  "grade": "B/C",
  "area": "Nordseite der Kanzelwand (2058 m), Riezlern im Kleinwalsertal",
  "region": "Kleinwalsertal (Allgäuer Alpen)",
  "climbMeters": 60,
  "lengthMeters": 200,
  "approachMin": 15,
  "ferrataMin": 45,
  "descentMin": 30,
  "totalMin": 90,
  "summitAlt": 2058,
  "season": "Juni bis Oktober, abhängig von Schneelage und Betriebszeiten der Kanzelwandbahn",
  "familyFriendly": true,
  "summary": "Der Walsersteig ist die leichte Alternative zum Zweiländer-Steig am selben Berg. Er ist kurz und kompakt: rund 200 Meter Sicherungslänge, etwa 60 Höhenmeter reine Kletterei und ein Zustieg von einer knappen Viertelstunde ab der Bergstation. Das Gelände bewegt sich im Bereich B/C mit Quergängen und einer steileren Kante kurz vor dem Gipfelplateau, die Absicherung ist sehr gut. Der Höhepunkt ist eine rund 25 Meter lange Burmabrücke, auf der man über ein Fußseil balanciert. Damit eignet sich der Steig gut für den ersten Kontakt mit Drahtseil und Klettersteigset und wird von den Bergschulen häufig für Kurse genutzt.",
  "approach": "Mit der Kanzelwandbahn von Riezlern zur Bergstation auf 1949 m, von dort auf dem markierten Weg Richtung Kanzelwand-Nordseite in rund 15 Minuten zum Einstieg. An der Talstation gebührenpflichtiger Parkplatz, Anfahrt auch mit dem Walserbus Linie 1.",
  "descent": "Ausstieg am Gipfelplateau der Kanzelwand (2058 m), dann auf dem normalen Wanderweg in etwa 30 Minuten zurück zur Bergstation und mit der Bahn ins Tal. Der Abstieg zu Fuß nach Riezlern ist möglich, dauert aber erheblich länger.",
  "gear": "Klettergurt, Klettersteigset und Helm. Die Bergschulen im Tal verleihen komplette Ausrüstung für Kurse.",
  "highlights": [
   "Rund 25 Meter lange Burmabrücke als Hauptattraktion",
   "Sehr kurzer Zustieg ab der Bergstation, gute Halbtagestour",
   "Gipfel der Kanzelwand mit weitem Blick ins Allgäu und ins Kleinwalsertal",
   "Beliebter Einsteigersteig, häufig für Kurse genutzt",
   "Mit dem Zweiländer-Sportklettersteig kombinierbar"
  ],
  "warnings": [
   "Trotz kurzer Länge kein Notausstieg",
   "Sicherungsseile hängen streckenweise hoch, für Kinder unter etwa 140 cm Körpergröße schwierig",
   "Bei Nässe und Schnee meiden",
   "An schönen Sommertagen stark frequentiert, früh starten",
   "Schwindelfreiheit und Trittsicherheit sind auch hier Voraussetzung"
  ],
  "sources": [
   "https://www.bergsteigen.com/touren/klettersteig/kanzelwand-erlebnisklettersteig-walsersteig/",
   "https://klettersteig.de/klettersteig/walsersteig_kanzelwand/1597",
   "https://www.vorarlberg.travel/route/walser-klettersteig/"
  ]
 },
 {
  "id": "mindelheimer",
  "name": "Mindelheimer Klettersteig",
  "grade": "C",
  "area": "Gipfelgrat der drei Schafalpenköpfe, Mittelberg im Kleinwalsertal",
  "region": "Kleinwalsertal (Allgäuer Alpen)",
  "climbMeters": 400,
  "lengthMeters": 1600,
  "approachMin": 180,
  "ferrataMin": 225,
  "descentMin": 135,
  "totalMin": 540,
  "summitAlt": 2320,
  "season": "Juli bis Oktober, erst wenn der Grat vollständig schneefrei ist",
  "summary": "Der Mindelheimer Klettersteig zieht über den zerrissenen Gipfelgrat der drei Schafalpenköpfe und folgt dabei der Grenze zwischen Kleinwalsertal und Allgäu. Technisch ist er nur mäßig anspruchsvoll: Der größte Teil bewegt sich zwischen A/B und B, dazwischen liegen immer wieder ungesicherte Gehstücke, die Schlüsselstelle ist ein kurzer, leicht überhängender Aufschwung beim Anstieg zum dritten Schafalpenkopf im Bereich C. Schwer macht ihn die Dimension: rund 1,6 Kilometer Drahtseilsicherung, dazu ein dreistündiger Zustieg und ein langer Abstieg, in Summe ein voller Bergtag. Die Absicherung mit Seilen, Klammern und Leitern gilt als sehr gut, die Ausgesetztheit ist durchgehend hoch. Wer den Tag nicht am Stück gehen will, teilt die Tour mit einer Übernachtung auf der Fiderepass- oder der Mindelheimer Hütte.",
  "approach": "Ausgangspunkt ist der Gasthof Schwendle bei Mittelberg (rund 1180 m). Durch das Wildental an der Wiesalpe vorbei bis zur Fluchtalpe, dort links halten und über die Vordere Wildenalpe hinauf zur Fiderepasshütte (2067 m). Nach kurzem Abstieg in eine Mulde zur Fiderescharte aufsteigen, dann rechts haltend zum Einstieg queren, rund 3 Stunden.",
  "descent": "Der Steig endet in der Nähe der Mindelheimer Hütte (2013 m). Von dort führt der Abstieg durch das Wildental zurück nach Mittelberg, etwa 2:15 Stunden. Wer die Tour auf zwei Tage aufteilt, übernachtet auf der Hütte und steigt am Folgetag ab, Platz vorab reservieren.",
  "gear": "Komplette Klettersteigausrüstung mit Helm, feste Bergschuhe, Verpflegung und Wasser für einen langen Tag; bei Hüttenübernachtung Hüttenschlafsack und Stirnlampe.",
  "highlights": [
   "Langer Gratsteig über drei Gipfel (2320 m, 2302 m, 2272 m)",
   "Grenzverlauf Österreich/Deutschland mit Blick auf den Allgäuer Hauptkamm",
   "Kleine Leiterbrücke und ausgesetzte Gratpassagen",
   "Zwei Hütten als Stützpunkte: Fiderepasshütte und Mindelheimer Hütte",
   "Klassiker seit 1975, sehr gut abgesichert"
  ],
  "warnings": [
   "Der Steig verläuft auf der Grenze zu Bayern, Teile des Grats liegen auf deutschem Boden",
   "Kein Flucht- oder Notausstieg, wer im Steig ist, muss durch",
   "Sehr wetterabhängig: Gewitter und Nebel am freien Grat sind gefährlich, Altschnee und Vereisung erhöhen das Risiko deutlich",
   "Sehr lange Tour, hohe Grundkondition erforderlich",
   "Für Kinder ungeeignet, Hüttenplatz rechtzeitig reservieren"
  ],
  "sources": [
   "https://www.bergsteigen.com/touren/klettersteig/mindelheimer-klettersteig/",
   "https://klettersteig.de/klettersteig/mindelheimer_klettersteig/63",
   "https://www.vorarlberg.travel/route/von-der-fiderepasshuette-ueber-den-mindelheimer-klettersteig/"
  ]
 },
 {
  "id": "zweilander-sport-kanzelwand",
  "name": "Zweiländer-Sportklettersteig Kanzelwand",
  "grade": "C/D",
  "area": "Nordabbrüche der Kanzelwand (2058 m), Riezlern im Kleinwalsertal",
  "region": "Kleinwalsertal (Allgäuer Alpen)",
  "crux": "D",
  "climbMeters": 210,
  "lengthMeters": 500,
  "approachMin": 45,
  "ferrataMin": 60,
  "descentMin": 30,
  "totalMin": 135,
  "summitAlt": 2058,
  "season": "Juni bis September, in guten Jahren bis Oktober; nordseitig und daher spät ausapernd",
  "summary": "Der 2007 gebaute Zweiländer-Sportklettersteig führt durch die senkrechten Nordabbrüche der Kanzelwand und ist ein moderner Sportklettersteig, kein alpiner Weg. Auf rund 500 Metern gesicherter Strecke und etwa 210 Höhenmetern reihen sich steile Aufschwünge, lange luftige Quergänge und eine Seilbrücke aneinander, die Schwierigkeit hält sich hartnäckig bei C und C/D. Der Einstieg beginnt gutmütig mit einer Rampe, führt dann über eine Platte zu einer ausgesetzten Kante. Entscheidend ist weniger die Klettertechnik als die Kraftausdauer in den Armen, die Buchstabenbewertung unterschätzt den Kraftbedarf deutlich. Einen Notausstieg gibt es nicht.",
  "approach": "Mit der Kanzelwandbahn von Riezlern zur Bergstation auf 1949 m. Von dort in etwa 45 Minuten unterhalb der Nordostwand zum Einstieg queren, wobei der Weg zunächst bis auf rund 1708 m an Höhe verliert. Ein Zustieg zu Fuß aus dem Tal ist möglich, kostet aber deutlich mehr Zeit. Gebührenpflichtiges Parken an der Talstation, Anfahrt auch mit dem Walserbus.",
  "descent": "Der Steig endet im Gipfelbereich der Kanzelwand (2058 m). Von dort auf dem markierten Wanderweg in rund 30 Minuten zurück zur Bergstation und mit der Bahn ins Tal. Wer noch Kraft hat, hängt den benachbarten Walsersteig als leichte Zugabe an.",
  "gear": "Komplette Klettersteigausrüstung mit Klettergurt, Klettersteigset und Helm, Handschuhe sehr empfehlenswert.",
  "highlights": [
   "Seilbrücke im oberen Teil des Steigs",
   "Durchgehend hohe Ausgesetztheit in der Nordwand",
   "Sehr kurzer Zustieg direkt ab der Bergstation",
   "Gipfel direkt auf der Grenze Österreich/Deutschland",
   "Sehr gute, moderne Absicherung"
  ],
  "warnings": [
   "Kein Flucht- oder Notausstieg",
   "Der Kraftbedarf liegt deutlich über dem, was C/D vermuten lässt, Kraftausdauer ist der limitierende Faktor",
   "Nordseitig: Altschnee bis in den Frühsommer, nach Regen lange nass und rutschig",
   "Steinschlag- und Gewittergefahr",
   "An schönen Sommertagen stark frequentiert",
   "Der Gipfel liegt auf der Grenze zu Bayern"
  ],
  "sources": [
   "https://www.bergsteigen.com/touren/klettersteig/zweilaender-sport-klettersteig-kanzelwand/",
   "https://klettersteig.de/klettersteig/zweilaender_klettersteig_kanzelwand/1572",
   "https://www.vorarlberg.travel/route/2-laender-sportklettersteig/"
  ]
 },
 {
  "id": "klostertaler-am-fallbach",
  "name": "Klostertaler Klettersteig am Fallbach",
  "grade": "C/D",
  "area": "Fallbachwand neben dem Fallbach-Wasserfall bei Dalaas",
  "region": "Klostertal",
  "climbMeters": 540,
  "lengthMeters": 1000,
  "approachMin": 30,
  "ferrataMin": 180,
  "descentMin": 60,
  "totalMin": 270,
  "startAlt": 890,
  "summitAlt": 1430,
  "season": "Anfang Mai bis Ende Oktober, offizielle Freigabe durch die Gemeinde Dalaas; im Winter gesperrt",
  "summary": "Der 2018 eröffnete Steig zieht rechts neben dem Fallbach-Wasserfall durch eine dunkle, kompakte Wand und ist der anspruchsvollste Klettersteig im Klostertal. Eine technische Extremstelle gibt es nicht, dafür reihen sich C- und C/D-Passagen über rund 540 Höhenmeter und etwa 1000 Meter gesicherte Strecke aneinander, unterbrochen von gestuften Gras- und Schrofenbändern. Auffällig ist die sparsame Eisenausstattung: Man klettert überwiegend am Fels und braucht saubere Fußtechnik statt Klammernhangeln. Nach einem flacheren Mittelteil stellt sich die Schlusswand noch einmal steil auf, ausgerechnet dort ist am wenigsten Material verbaut. Entscheidend sind Unterarmausdauer und ein kühler Kopf, denn einen Notausstieg gibt es nicht.",
  "approach": "Vom Parkplatz an der Arlbergstraße L97 unterhalb von Dalaas (rund 820 m) etwa 100 Meter talauswärts gehen, die Straße queren und durch das Tor im Wildzaun, dann den Schildern zum Klettersteig bzw. dem Weg Richtung Fallbachfall folgen bis unter die Wand. Kurz westlich des Wasserfalls über Schutt zum gut sichtbaren Einstieg, rund 20 bis 30 Minuten und etwa 70 bis 110 Höhenmeter. Anfahrt über die S16, Ausfahrt Dalaas; mit Öffis Landbus bis Haltestelle Gasthaus Krone.",
  "descent": "Vom Ausstieg den großen blauen Punkten durch den Wald folgen, den Fallbach queren und auf den weiß-blau-weiß markierten Wanderweg treffen. Der Pfad ist schmal, teils steil und abschüssig, gegen Ende überbrücken eine Leiter und eine Balkenpassage einen Felsvorsprung. Rund eine Stunde, bis zum Schluss konzentriert bleiben.",
  "gear": "Komplettes Klettersteigset mit Helm und Gurt ist Pflicht. Dazu Handschuhe, knöchelhohe Bergschuhe mit griffiger Sohle und eine Rastschlinge, weil es unterwegs keine Ausstiegsmöglichkeit gibt. Wasser für drei Stunden Wandzeit einpacken.",
  "highlights": [
   "Direkt neben einem der höchsten Wasserfälle Österreichs",
   "Sehr fels- und reibungsbetont, wenig Eisen, alpiner Charakter",
   "Rund 1000 Meter gesicherte Strecke, einer der längsten Steige Vorarlbergs",
   "Kurzer Zustieg, Parkplatz praktisch am Wandfuß"
  ],
  "warnings": [
   "Kein Notausstieg, ab dem Einstieg muss der Steig zu Ende geklettert werden",
   "Bei Nässe nicht einsteigen, der dunkle Fels wird glatt und Reibung ist die Hauptsicherung",
   "Bei Gewitter besteht in der Wand Lebensgefahr, Ausweichen ist nicht möglich",
   "Steinschlaggefahr durch vorausgehende Seilschaften, Helm auflassen und Abstand halten",
   "Der Steig war nach einem Felssturz zeitweise gesperrt, Status vorab bei der Gemeinde Dalaas prüfen",
   "Zum Höhengewinn schwanken die Quellen zwischen 540 m (Klettersteigportale, Höhenprofile) und 650 m (Gemeinde und Tourismusstellen)"
  ],
  "sources": [
   "https://www.bergsteigen.com/touren/klettersteig/klostertaler-klettersteig-am-fallbach/",
   "https://klettersteig.de/klettersteig/klostertaler_klettersteig_am_fallbach/2388",
   "https://www.via-ferrata.de/klettersteige/topo/klostertaler-klettersteig-am-fallbach",
   "https://www.vorarlberg.travel/route/klostertaler-klettersteig-am-fallbach-dalaas/",
   "https://www.dalaas.at/Fallbach_Klettersteig_geoeffnet"
  ]
 },
 {
  "id": "karhorn-ostgrat",
  "name": "Karhorn-Klettersteig Ostgrat",
  "grade": "B/C",
  "area": "Ostgrat des Karhorns (2416 m) bei Warth am Arlberg",
  "region": "Lechquellengebirge / Arlberg",
  "lengthMeters": 600,
  "approachMin": 45,
  "ferrataMin": 75,
  "descentMin": 90,
  "totalMin": 210,
  "startAlt": 2200,
  "summitAlt": 2416,
  "season": "Ende Juni bis Anfang Oktober, praktisch gebunden an den Sommerbetrieb des Steffisalp-Express",
  "familyFriendly": true,
  "summary": "Der Ostgrat ist ein langer, sehr aussichtsreicher Gratklettersteig, der technisch bewusst einfach gehalten wurde und deshalb als Einstiegstour gilt. Er beginnt am Wartherhornsattel mit einem kurzen Wandl und führt danach überwiegend im Grad A/B über und neben dem Grat bis zum Gipfel, mit einem steileren Aufschwung links des Grats als schwerster Stelle. Zwischendurch ist das Seil unterbrochen, dort geht man auf blau markiertem Gehgelände, in Scharten sogar kurz abwärts. Anspruch macht nicht die Kletterei, sondern die Länge, die Ausgesetztheit und die ungesicherten Passagen. Der Höhengewinn im Steig beträgt vom Einstieg auf 2200 m bis zum Gipfel gut 210 Meter.",
  "approach": "Mit dem Steffisalp-Express von Warth (1495 m) zur Bergstation auf rund 1880 m. Dort links auf den Wanderweg, an der Bergstation der Wartherhorn-Bahn vorbei und durch eine Mulde hinauf zum Wartherhornsattel (rund 2185 m). Am Sattel rechts halten und in wenigen Minuten zum Einstieg rechts des Grats auf rund 2200 m, insgesamt 45 bis 60 Minuten. Von Lech fährt der kostenlose Wanderbus ab Rüfiplatz nach Warth.",
  "descent": "Wenige Meter unter dem Gipfel zweigt der markierte Normalweg nach Südosten ab, nicht halblinks ins Geröllfeld absteigen. Er ist steil, geröllig, ausgesetzt und an kurzen Stellen versichert. Am Wandfuß links auf den Höhenweg, der unter dem Karhorn zurück zum Wartherhornsattel und zur Bergstation führt, etwa 1 bis 1,5 Stunden.",
  "gear": "Klettersteigset, Helm, Gurt und feste Bergschuhe. Handschuhe angenehm, Rastschlinge nützlich, dazu 1 bis 1,5 Liter Wasser, Sonnenschutz und Windjacke.",
  "highlights": [
   "Klassischer Gratklettersteig mit Tiefblick auf Lech und Warth",
   "Guter Einstiegssteig für Anfänger und erfahrene Kinder",
   "Gipfelpanorama vom Karhorn bis Valluga, Widderstein, Säntis und Allgäuer Alpen",
   "Bequemer Zustieg per Sesselbahn, Warther Horn als Abstecher"
  ],
  "warnings": [
   "Bewertung uneinheitlich: Warth-Schröcken und vorarlberg.travel führen den Ostgrat als durchgehend A/B, klettersteig.de und bergsteigen.com insgesamt als B/C",
   "Der Fels am Karhorn ist brüchig, Steinschlag durch andere Begeher ist die Hauptgefahr",
   "Mehrere Abschnitte sind unversichert oder das Seil hängt locker, Trittsicherheit und Schwindelfreiheit sind Pflicht",
   "Der Abstieg über den Normalweg ist steil und geröllig",
   "Am freien Grat gibt es bei Gewitter keinen Schutz, früh starten",
   "Angaben zur gesicherten Länge schwanken zwischen rund 600 m Kletterstrecke und 1400 m Gratlänge inklusive Gehpassagen"
  ],
  "sources": [
   "https://www.vorarlberg.travel/route/karhorn-klettersteig-ostgrat/",
   "https://www.bergsteigen.com/touren/klettersteig/karhorn-klettersteig/",
   "https://klettersteig.de/klettersteig/karhorn_klettersteig/51",
   "https://www.via-ferrata.de/klettersteige/topo/klettersteig-karhorn",
   "https://www.bergwelten.com/t/ks/46964"
  ],
  "verified": false
 },
 {
  "id": "karhorn-westgrat-panorama",
  "name": "Karhorn-Klettersteig Westgrat (Panorama-Klettersteig)",
  "grade": "C/D",
  "area": "Überschreitung des Karhorns (2416 m) über beide Grate, Warth am Arlberg",
  "region": "Lechquellengebirge / Arlberg",
  "lengthMeters": 1200,
  "approachMin": 45,
  "ferrataMin": 150,
  "descentMin": 90,
  "totalMin": 300,
  "startAlt": 2200,
  "summitAlt": 2416,
  "season": "Ende Juni bis Anfang Oktober, gebunden an den Steffisalp-Express",
  "summary": "Diese Variante hängt an den einfachen Ostgrat die 2009 errichtete Fortsetzung über den Westgrat an, oft Panorama-Klettersteig genannt. Sie beginnt am Karhorn-Gipfel und zieht südwestlich dem Kamm entlang, im ständigen Auf und Ab und ohne nennenswerten Höhengewinn, dafür sehr luftig und ausgesetzt. Unterwegs warten zwei kurze Seilbrücken und einzelne deutlich schwerere Stellen, den Ausstieg bildet eine lange, seilversicherte Felsplatte. Die schweren Passagen sind kurz und nicht kräftezehrend, es gibt genügend Standplätze zum Ausruhen, aber die Gesamtstrecke summiert sich auf rund 1200 Meter gesicherte Strecke. Am Gipfel lässt sich die Fortsetzung je nach Kraft und Wetter noch abwählen.",
  "approach": "Identisch zum Ostgrat: Mit dem Steffisalp-Express von Warth zur Bergstation auf rund 1880 m, dann über den Wanderweg und die Mulde zum Wartherhornsattel (rund 2185 m) und weiter zum Einstieg des Ostgrats auf etwa 2200 m, 45 bis 60 Minuten. Der Ostgrat wird komplett bis zum Gipfel geklettert, erst dort beginnt der Westgrat.",
  "descent": "Am Ausstieg des Westgrats, nach der langen seilversicherten Felsplatte, führt ein Pfad über den Südhang des Karhorns abwärts und dann am Fuß des Massivs entlang zurück zum Wartherhornsattel und zur Bergstation, ab Ausstieg etwa 1,5 Stunden. Alternativ über Bürstegg zur Bodenalpe oder über den Auenfeldsattel nach Oberlech mit Anschluss an den Wanderbus.",
  "gear": "Klettersteigset, Helm, Gurt, Bergschuhe, Handschuhe und Rastschlinge. Wegen der Sonnenseite und der Gesamtlänge 1,5 bis 2 Liter Wasser pro Person sowie Sonnenschutz einplanen.",
  "highlights": [
   "Komplette Überschreitung des Karhorns über beide Grate",
   "Zwei Seilbrücken und exponierte Gratkletterei",
   "Rundumblick von den Allgäuer Alpen über den Widderstein bis zur Valluga",
   "Am Gipfel jederzeit abwählbar"
  ],
  "warnings": [
   "Der Höhengewinn innerhalb des Steigs lässt sich nicht sauber angeben: Der Ostgrat bringt gut 210 Meter, der Westgrat verläuft im ständigen Auf und Ab ohne dokumentierten Nettogewinn, deshalb ist das Feld leer",
   "Bewertung uneinheitlich: Warth-Schröcken gibt für den Westgrat B/C mit zwei C-Stellen an, Bergwelten und mehrere Berichte stufen die Kombination als C/D ein",
   "Deutlich anspruchsvoller als der Ostgrat allein, ausgesetzte Gratkletterei",
   "Brüchiger Fels und Steinschlag durch andere Begeher",
   "Freier Grat ohne Schutz bei Gewitter, früh starten und die letzte Talfahrt einkalkulieren",
   "Angaben zur Gesamtdauer schwanken zwischen 3,5 und knapp 6 Stunden"
  ],
  "sources": [
   "https://www.vorarlberg.travel/route/karhorn-klettersteig-ost-und-westgrat/",
   "https://www.bergwelten.com/t/ks/17650",
   "https://www.lechtal-info.com/touren/karhorn-klettersteig.html",
   "https://bergseensucht.com/2021/08/18/karhorn-2416m-klettersteige-warth-ostgrat-westgrat-vorarlberg-viaferrata/"
  ],
  "verified": false
 },
 {
  "id": "wasserfall-st-anton-im-montafon",
  "name": "Wasserfall-Klettersteig St. Anton im Montafon",
  "grade": "C/D",
  "area": "Wasserfälle unterhalb der Davenna, oberhalb von St. Anton im Montafon",
  "region": "Montafon (Talnah)",
  "climbMeters": 190,
  "approachMin": 20,
  "ferrataMin": 60,
  "descentMin": 40,
  "totalMin": 120,
  "startAlt": 750,
  "season": "Mai bis Oktober, 1. Dezember bis 15. März gesperrt",
  "summary": "Ein talnaher Steig direkt oberhalb des Dorfes, der zwei Wasserfälle einbindet. Am unteren Wasserfall klettert man über eine Stufenleiter unmittelbar daneben empor, der obere ist von zwei rund 30 Meter langen Seilbrücken überspannt. Die Schwierigkeiten nehmen nach oben zu: Der Großteil liegt bei B bis C, dazu kommen zwei kurze C/D-Stellen, darunter eine feuchte Wand nach der zweiten Seilbrücke und die leicht überhängende Schlüsselstelle. Die Seilbrücken lassen sich umgehen. Wegen der geringen Höhenlage geht der Steig früh und spät in der Saison, dafür gibt es keinen Notausstieg.",
  "approach": "Ausgangspunkt ist der Kletterparkplatz beim Gemeindezentrum bzw. Bahnhof St. Anton im Montafon. Dem Wegweiser Klettersteig durch das Dorf folgen, unmittelbar vor der Brücke rechts auf die Forststraße abbiegen und den Schildern bis zur Aussichtskanzel am Wasserfall folgen, wo die Versicherungen beginnen, rund 20 Minuten. Weiter oben im Ort gibt es keine legalen Parkmöglichkeiten.",
  "descent": "Vom Ende der Versicherungen dem mit Steinmännchen markierten Pfad knapp 200 Meter zuerst ansteigend, dann querend folgen, danach scharf rechts abbiegen und auf steilem Wanderweg zurück nach St. Anton, etwa 40 Minuten. Alternativ über den Wanderweg Marentes-Jetzmunt zum Bahnhof Vandans und entlang der Ill zurück.",
  "gear": "Komplette Klettersteigausrüstung und Helm. Durchgehendes Stahlseil und sehr viele Klammern, Handschuhe angenehm.",
  "highlights": [
   "Kletterei unmittelbar neben dem unteren Wasserfall",
   "Zwei rund 30 Meter lange Seilbrücken über den oberen Wasserfall",
   "Am Nachmittag erzeugt die Sonne in der Gischt oft einen Regenbogen",
   "Kurzer Zustieg, gut für einen halben Tag"
  ],
  "warnings": [
   "Kein Notausstieg auf der gesamten Route",
   "Der Fels ist durch die vielen Begehungen glatt poliert, bei Nässe nicht zu empfehlen",
   "Die Schwierigkeiten nehmen nach oben zu",
   "Wintersperre von 1. Dezember bis 15. März"
  ],
  "sources": [
   "https://www.bergsteigen.com/touren/klettersteig/wasserfall-klettersteig-st-anton-im-montafon/",
   "https://klettersteig.de/klettersteig/wasserfall_ks_st_anton_im_montafon/2120",
   "https://www.montafon.at/de/klettersteig-wasserfall-st-anton-im-montafon-2_vc4440"
  ]
 },
 {
  "id": "ubungs-klettergarten-latschau",
  "name": "Übungsklettersteig Klettergarten Latschau",
  "grade": "A/B",
  "area": "Klettergarten am Staubecken Latschau bei Tschagguns",
  "region": "Montafon (Talnah)",
  "crux": "B/C",
  "climbMeters": 30,
  "approachMin": 15,
  "ferrataMin": 30,
  "descentMin": 15,
  "totalMin": 60,
  "startAlt": 985,
  "season": "April bis Oktober",
  "familyFriendly": true,
  "summary": "Der klassische Übungssteig im mittleren Montafon, direkt am Staubecken Latschau. Die Grundroute bleibt durchgehend bei A/B und ist der richtige Ort, um den Umgang mit dem Klettersteigset zu lernen; bergsteigen.com stuft einzelne Stellen mit B/C etwas höher ein. Empfehlenswert ist die Begehung als Querung von links nach rechts, dabei sind zwei kleine Seilbrücken eingebaut, die vor allem Kindern Spaß machen. Ein Ausstieg nach oben ist jederzeit möglich, und weil die Wand von unten gut einsehbar ist, können Begleitpersonen alles vom Ufer aus mitverfolgen. Mit rund 30 Höhenmetern ist das keine Bergtour, sondern eine Übungseinheit oder ein Familiennachmittag.",
  "approach": "Vom Parkplatz Lünerseewerk bzw. der Golmerbahn in Latschau (rund 985 m) am Speicherteich entlang bis zum markanten Felsen am gegenüberliegenden Ostufer, dort liegt der Einstieg. Rund 10 bis 15 Minuten. Latschau ist auch mit Öffis ab Bahnhof Tschagguns erreichbar.",
  "descent": "Auf demselben Weg am See entlang zurück zum Ausgangspunkt, rund 15 Minuten. Alternativ mit dem Flying Fox Golm über das Staubecken zurück.",
  "gear": "Komplettes Klettersteigset und Helm, für Kinder ein Set im passenden Gewichtsbereich. Ideal zum ersten Üben mit eigenem Material.",
  "highlights": [
   "Idealer Ort zum Üben des Umgangs mit dem Klettersteigset",
   "Zwei kleine Seilbrücken, kindgerecht",
   "Jederzeit Ausstieg nach oben möglich",
   "Von unten gut einsehbar, gut für Familien mit Zuschauern",
   "Blick auf die Zimba"
  ],
  "warnings": [
   "Der Steig führt über einem Klettergarten und einem Weg, keinen Steinschlag auslösen",
   "Trotz Übungscharakter besteht Absturzgefahr, Kinder konsequent sichern",
   "Die Bewertung reicht je nach Quelle von A/B bis B/C, einzelne Varianten sind deutlich schwerer",
   "Zur Länge der gesicherten Strecke widersprechen sich die Quellen deutlich, deshalb ist das Feld leer",
   "Die vorhandene Seilrutsche darf nur mit ortskundiger Führung benutzt werden"
  ],
  "sources": [
   "https://www.bergsteigen.com/touren/klettersteig/latschau-klettersteig/",
   "https://klettersteig.de/klettersteig/uebungsklettersteig_klettergarten_latschau/2328",
   "https://www.via-ferrata.de/klettersteige/topo/klettergarten-latschau-im-montafon",
   "https://www.golm.at/de/klettergarten-latschau_vc9431"
  ]
 },
 {
  "id": "ubungs-rifa",
  "name": "Übungsklettersteig Rifa",
  "grade": "A/B",
  "area": "Klettergarten Rifa beim Rifabecken zwischen Gaschurn und Partenen",
  "region": "Montafon (Talnah)",
  "crux": "C/D",
  "climbMeters": 20,
  "approachMin": 5,
  "ferrataMin": 30,
  "descentMin": 5,
  "totalMin": 40,
  "season": "Mai bis Oktober",
  "familyFriendly": true,
  "summary": "Ein kompakter Übungsklettersteig im Klettergarten Rifa, angelegt als Rundkurs mit fünf verschiedenen Auf- und Abstiegsvarianten. Die Grundroute liegt bei A/B und ist gut abgesichert, ideal für erste Erfahrungen am Fels, auch mit Kindern; für Fortgeschrittene gibt es Varianten bis in den Bereich C/D. Eine Hängebrücke und teils ausgesetzte Quergänge sorgen für Abwechslung. Die Wandhöhe beträgt nur rund 20 Meter, entsprechend eignet sich die Anlage vor allem für Technik- und Krafttraining sowie für Kurse. Rund um den Klettergarten gibt es ein Areal mit Wasserspielplatz und Grillstelle für Begleitpersonen.",
  "approach": "Der Einstieg liegt nur wenige Schritte vom Parkplatz entfernt beim gut sichtbaren Klettergarten Rifa. Anfahrt über die L188 bis Gaschurn und weiter bis kurz vor den Tunnel Richtung Partenen, dort rechts abbiegen und auf dem direkt folgenden Parkplatz stellen.",
  "descent": "Da der Steig als Rundkurs angelegt ist, kann über jede Variante auf- und abgestiegen werden, ein separater Abstiegsweg ist nicht nötig.",
  "gear": "Komplette Klettersteigausrüstung und Helm. Für Kinder ein Set im passenden Gewichtsbereich.",
  "highlights": [
   "Rundkurs mit fünf Varianten von A/B bis in den oberen Schwierigkeitsbereich",
   "Hängebrücke und ausgesetzte Quergänge",
   "Familienareal mit Wasserspielplatz und Grillstelle",
   "Guter Ort für Klettersteigkurse und Techniktraining"
  ],
  "warnings": [
   "Unterhalb des Quergangs verlaufen Kletterrouten, auf Kletterer im Wandbereich achten",
   "Die Varianten unterscheiden sich stark im Anspruch, vorab das Topo studieren",
   "Zur Länge der gesicherten Strecke liegen keine belastbaren Angaben vor"
  ],
  "sources": [
   "https://klettersteig.de/klettersteig/rifa_klettersteig/1762",
   "https://www.montafon.at/de/klettersteig-rifa_vc4465"
  ]
 },
 {
  "id": "burg",
  "name": "Klettersteig Burg",
  "grade": "A/B",
  "area": "Nordgrat der Burg (2247 m) oberhalb der Versettla Bahn, Gaschurn/St. Gallenkirch",
  "region": "Montafon (Verwall/Versettla)",
  "crux": "B/C",
  "climbMeters": 30,
  "lengthMeters": 200,
  "approachMin": 30,
  "ferrataMin": 45,
  "descentMin": 30,
  "totalMin": 105,
  "summitAlt": 2247,
  "season": "Juni bis Oktober; Winterbegehung möglich, dann deutlich schwerer",
  "familyFriendly": true,
  "summary": "Ein kurzer Gratklettersteig mit sehr wenig Höhenmetern und geringer Steigung, angelegt zum Üben von Technik und Sicherungsabläufen. Man klettert am Grat entlang mit durchgehend luftigen Tief- und Ausblicken, aber ohne nennenswerte Kraftpassagen; die gesicherte Strecke wird je nach Quelle mit rund 200 bis 250 Metern angegeben, der Höhengewinn im Steig liegt nur bei etwa 30 Metern. Der Steig lässt sich in beide Richtungen begehen, was ihn für Gruppen praktisch macht. Er eignet sich gut als Auftakt oder Abschluss einer Runde und lässt sich direkt an den Abstieg vom Madrisella-Klettersteig anhängen. Im Winter wird dieselbe Linie als Winterklettersteig begangen, dann liegen Teile der Sicherung unter Schnee und die Schwierigkeit steigt auf etwa C.",
  "approach": "Auffahrt mit der Versettla Bahn ab Gaschurn, Betriebszeiten beachten. Von der Bergstation der Wanderroute 10 Gipfelweg Madrisella folgen, nach etwa 15 Minuten rechts Richtung Gipfel abzweigen; der Zugang führt in der Nähe der Sprengmasten bergauf zum Einstieg am Nordgrat, rund 15 bis 30 Minuten.",
  "descent": "Vom Ausstieg über einen Wanderweg in gut 30 Minuten zurück zur Bergstation der Versettla Bahn. Alternativ über den Klettersteig zurückklettern und den Zustiegsweg nehmen.",
  "gear": "Komplette Klettersteigausrüstung und Helm. Für eine Winterbegehung zusätzlich Wintererfahrung, Steigeisen und die Bereitschaft, verschüttete Sicherungen freizulegen.",
  "highlights": [
   "Kurzer Gratsteig mit sehr wenig Höhenmetern, gut zum Üben",
   "In beide Richtungen begehbar",
   "Ausblick auf Heimspitze, Madrisella, Versettlaspitze und die Silvretta-Dreitausender",
   "Gut mit dem Madrisella-Klettersteig kombinierbar",
   "Im Winter als eigenständiger Winterklettersteig begehbar"
  ],
  "warnings": [
   "Der Winterklettersteig ist mit rund C deutlich schwerer und nur geübten Winterklettersteig-Gehern zu empfehlen",
   "Betriebszeiten der Versettla Bahn beachten",
   "Die Angaben zur Seillänge schwanken zwischen rund 200 und 250 Metern"
  ],
  "sources": [
   "https://www.bergsteigen.com/touren/klettersteig/burg-klettersteig/",
   "https://klettersteig.de/klettersteig/burg/1934",
   "https://www.via-ferrata.de/klettersteige/topo/winterklettersteig-burg",
   "https://www.montafon.at/en/via-ferrata-castle_vc4478"
  ]
 },
 {
  "id": "kalbersee-variante-b",
  "name": "Klettersteig Kälbersee – Variante B",
  "grade": "B",
  "area": "Kälbersee / Seetal am Hochjoch, Schruns",
  "region": "Montafon – Verwallgruppe, Vorarlberg",
  "climbMeters": 60,
  "lengthMeters": 120,
  "approachMin": 15,
  "ferrataMin": 30,
  "descentMin": 30,
  "totalMin": 75,
  "season": "Etwa Juni bis Oktober, solange Sennigrat-/Hochjochbahn laufen und die nordseitige Wand schneefrei und trocken ist",
  "familyFriendly": true,
  "summary": "Die B-Linie am Kälbersee ist die klassische Einsteigervariante mit ein wenig mehr Anspruch als die A-Route. Sie verläuft im linken Wandteil, ist gut mit Bügeln entschärft und verlangt bereits etwas Armkraft, ohne je wirklich steil zu werden. Zwei nahe beieinander liegende Einstiegsmöglichkeiten treffen nach kurzer Strecke zusammen, bevor sich die Linien in Wandmitte erneut auffächern. Wer das erste Mal ein Klettersteigset benutzt, findet hier die richtige Dosis Ausgesetztheit. Am Ausstieg warten zwei etwas luftigere Tritte um einen kleinen Felskopf herum.",
  "approach": "Mit Hochjoch- und Sennigratbahn von Schruns zur Bergstation Sennigrat (2.260 m), von dort rund 15 Minuten bergab zum Kälbersee. Der Einstieg liegt links im Fels auf etwa 2.160 m und ist mit silbernen Schildern markiert.",
  "descent": "Über den Wanderweg vom Ausstieg zurück zum See und in etwa 30 Minuten hinauf zur Bergstation Sennigrat. Wer mehrere Linien machen will, steigt über die leichte A-Variante wieder ab.",
  "gear": "Klettersteigset, Klettergurt, Helm, Handschuhe, feste Bergschuhe; Leihmaterial in Schruns erhältlich.",
  "highlights": [
   "Klassiker zum Erlernen der Klettersteigtechnik in gut abgesichertem Gelände",
   "Zwei Einstiegsvarianten dicht nebeneinander, ideal für Gruppen",
   "Kann direkt mit den Linien C und D am selben Fels kombiniert werden",
   "Traumhafte Kulisse zwischen den Seen im Seetal",
   "Lässt sich gut mit dem Klettersteig Hochjoch zu einem Tag verbinden"
  ],
  "warnings": [
   "Nordseitige Wand, bleibt nach Regen lange nass und ist dann heikel",
   "Zu kurz für einen ganzen Klettersteigtag",
   "Steinschlaggefahr durch Seilschaften in den benachbarten Linien",
   "Höhenangaben von Ein- und Ausstieg variieren je nach Quelle um einige Meter"
  ],
  "sources": [
   "https://www.bergsteigen.com/touren/klettersteig/klettersteig-kaelbersee/",
   "https://klettersteig.de/klettersteig/klettersteig_kaelbersee/2347",
   "https://www.via-ferrata.de/klettersteige/topo/kaelbersee",
   "https://www.montafon.at/de/kaelbersee-klettersteig_vc4477",
   "https://www.vorarlberg.travel/route/klettersteig-kaelbersee/"
  ],
  "verified": false
 },
 {
  "id": "kalbersee-variante-c-kantenferrata",
  "name": "Klettersteig Kälbersee – Variante C (Kantenferrata)",
  "grade": "C",
  "area": "Kälbersee / Seetal am Hochjoch, Schruns",
  "region": "Montafon – Verwallgruppe, Vorarlberg",
  "climbMeters": 60,
  "lengthMeters": 120,
  "approachMin": 15,
  "ferrataMin": 40,
  "descentMin": 30,
  "totalMin": 85,
  "season": "Etwa Juni bis Oktober, nordseitig – erst begehen, wenn die Wand wirklich trocken ist",
  "summary": "Die rechte Linie am Kälbersee klettert über eine ausgeprägte Felskante und ist die athletischste der beiden mittleren Schwierigkeiten. Sie ist mit C bewertet, verlangt also schon deutlich mehr Armkraft und Trittsicherheit als die Einsteigerlinien nebenan. Die Kante ist luftiger als der Rest der Wand, was den kurzen 60 Höhenmetern spürbar Charakter gibt. Für Familien mit Kindern ist sie nur bedingt geeignet, für ambitionierte Einsteiger dagegen die logische Steigerung nach der B-Linie. Wer die Wand ausreizen will, hängt gleich noch die schwere D-Linie an.",
  "approach": "Von der Bergstation Sennigrat (2.260 m) in rund 15 Minuten hinunter zum Kälbersee. Der Einstieg zur Kantenferrata liegt im rechten Teil des Felsriegels auf etwa 2.160 m.",
  "descent": "Vom Ausstieg über den Steig zurück zum See, dann in etwa 30 Minuten wieder hinauf zur Bergstation Sennigrat. Alternativ Abklettern über die leichte A-Variante.",
  "gear": "Klettersteigset, Klettergurt, Helm, Handschuhe, feste Bergschuhe mit guter Profilsohle.",
  "highlights": [
   "Ausgesetzte Felskante mit echtem Klettersteig-Feeling auf kurzer Strecke",
   "Ideale Steigerung nach den Einsteigerlinien A und B",
   "Sehr gute Sicherung mit kräftigem Stahlseil",
   "Alle vier Linien liegen nebeneinander – Schwierigkeit lässt sich frei dosieren",
   "Panoramalage über dem Kälbersee"
  ],
  "warnings": [
   "Nordexponiert, bei Nässe stark rutschig und dann nicht zu empfehlen",
   "Armkraft nötig – nicht für Kinder oder Klettersteig-Erstbegeher",
   "Kurze Strecke, aber Steinschlag durch Kletterer in den Nachbarlinien möglich",
   "Die Zuordnung der Schwierigkeitsgrade zu den einzelnen Linien wird auf bergsteigen.com und den Tourismusseiten leicht unterschiedlich beschrieben"
  ],
  "sources": [
   "https://www.bergsteigen.com/touren/klettersteig/klettersteig-kaelbersee/",
   "https://www.via-ferrata.de/klettersteige/topo/kaelbersee",
   "https://klettersteig.de/klettersteig/klettersteig_kaelbersee/2347",
   "https://www.montafon.at/de/kaelbersee-klettersteig_vc4477",
   "https://www.vorarlberg.travel/route/klettersteig-kaelbersee/"
  ],
  "verified": false
 },
 {
  "id": "kalbersee-variante-d",
  "name": "Klettersteig Kälbersee – Variante D",
  "grade": "D",
  "area": "Kälbersee / Seetal am Hochjoch, Schruns",
  "region": "Montafon – Verwallgruppe, Vorarlberg",
  "climbMeters": 60,
  "lengthMeters": 120,
  "approachMin": 15,
  "ferrataMin": 45,
  "descentMin": 30,
  "totalMin": 90,
  "season": "Etwa Juni bis Oktober, nordseitig – nur bei völlig trockenem Fels sinnvoll",
  "summary": "Die mittlere Linie am Kälbersee ist bewusst als Trainingsstrecke für Fortgeschrittene gebaut: Auf Trittbügel wurde hier absichtlich verzichtet, geklettert wird an den natürlichen Strukturen des Fels. Dadurch verlangt sie sauberes Fußtechnikspiel und ordentlich Kraft, obwohl es nur 60 Höhenmeter sind. Sie ist die schwerste der vier Linien und wird durchgehend mit D bewertet. Wer hier flüssig durchkommt, ist auch für längere D-Steige im Montafon gerüstet. Als kurze, intensive Trainingseinheit mit Seeblick ist sie im Gebiet einzigartig.",
  "approach": "Mit Hochjoch- und Sennigratbahn zur Bergstation (2.260 m), dann rund 15 Minuten Abstieg zum Kälbersee. Der Einstieg liegt in der Mitte des Felsriegels auf etwa 2.160 m.",
  "descent": "Zurück über den Steig zum See und in etwa 30 Minuten hinauf zur Bergstation Sennigrat; alternativ Abklettern über die leichte A-Linie neben der Route.",
  "gear": "Klettersteigset, Klettergurt, Helm, Handschuhe, eng sitzende feste Bergschuhe oder Zustiegsschuhe mit guter Kantenreibung; ein kurzes Seil zum Nachsichern von Partnern ist bei D-Schwierigkeit sinnvoll.",
  "highlights": [
   "Bewusst ohne Trittbügel gebaut – reines Klettern am natürlichen Fels",
   "Schwerste Linie des Übungsgebiets, ideal zum Krafttraining",
   "Sehr gute, engmaschige Stahlseilsicherung trotz hoher Schwierigkeit",
   "Direkt neben den leichteren Linien – perfekt für gemischte Gruppen",
   "Kurz genug, um sie mehrfach hintereinander zu klettern"
  ],
  "warnings": [
   "Erfordert deutliche Armkraft und gute Klettertechnik, klarer D-Anspruch",
   "Keine Trittbügel – bei Nässe oder in schweren Schuhen deutlich heikler",
   "Nordseite, trocknet langsam ab",
   "Nicht familienfreundlich, für Kinder ungeeignet"
  ],
  "sources": [
   "https://www.bergsteigen.com/touren/klettersteig/klettersteig-kaelbersee/",
   "https://www.montafon.at/de/kaelbersee-klettersteig_vc4477",
   "https://www.via-ferrata.de/klettersteige/topo/kaelbersee",
   "https://klettersteig.de/klettersteig/klettersteig_kaelbersee/2347",
   "https://www.vorarlberg.travel/route/klettersteig-kaelbersee/"
  ],
  "verified": false
 },
 {
  "id": "kaenzele-rechte-variante",
  "name": "Kaenzele-Klettersteig – rechte Variante",
  "grade": "D/E",
  "area": "Kaenzele bei Bregenz, rechte Linie",
  "region": "Rheintal / Bodensee",
  "climbMeters": 70,
  "approachMin": 40,
  "ferrataMin": 45,
  "descentMin": 30,
  "totalMin": 115,
  "season": "Ganzjaehrig bei trockenem Fels",
  "summary": "Die rechte der drei Linien am Kaenzele ist die mit Abstand schwerste und verlangt deutlich mehr Kraft als die mittlere. Der Nagelfluh bietet gute Tritte, wird bei Naesse aber ausgesprochen rutschig.",
  "warnings": [
   "Nagelfluh und Sandstein werden bei Naesse extrem rutschig",
   "Deutlich schwerer als die bekannte mittlere Variante",
   "Kein Notausstieg auf der kurzen, steilen Linie"
  ],
  "sources": [
   "https://www.bergsteigen.com/touren/klettersteig/kaenzele-klettersteig/"
  ],
  "verified": false
 },
 {
  "id": "kanzele",
  "name": "Känzele-Klettersteig",
  "grade": "C",
  "area": "Känzelefelsen am Gebhardsberg über Bregenz",
  "region": "Rheintal / Bodensee",
  "crux": "D/E",
  "lengthMeters": 70,
  "approachMin": 30,
  "ferrataMin": 30,
  "descentMin": 15,
  "totalMin": 75,
  "startAlt": 630,
  "summitAlt": 670,
  "season": "April bis Oktober, bei trockenen Verhältnissen auch außerhalb",
  "summary": "Kurzer Stadtklettersteig unmittelbar über Bregenz, angelegt im Nagelfluh- und Sandsteingelände des Känzelefelsens und vom ÖAV Bregenz betreut. Nach dem Einstieg über eine Sandsteinplatte und eine Leiter zieht der Steig bis zu einer Verzweigung, an der drei Linien zur Wahl stehen: links eine Variante um B/C mit großzügig gesetzten Klammern, in der Mitte die Normalroute um C, rechts eine 2018 ergänzte Linie mit überhängender Schlüsselstelle im Bereich D/E. Alle drei treffen wieder zusammen und enden über eine Querung am Känzelefelsen. Mit rund 20 bis 30 Minuten reiner Kletterzeit taugt der Steig als Feierabendrunde oder kurze Kraftprobe und lässt sich mehrfach hintereinander begehen.",
  "approach": "Von Bregenz auf den Gebhardsberg, gebührenpflichtiger Parkplatz bei der Ruine (rund 575 m). Von dort ein Stück Richtung Kennelbach absteigen und auf dem markierten Unteren Känzeleweg nach Osten queren, nach 20 bis 30 Minuten steht man am Wandfuß auf rund 630 m, wo die drei Einstiege nebeneinander beschildert sind. Mit Öffis ab Bahnhof Bregenz per Landbus bis Haltestelle Gebhardsberg.",
  "descent": "Vom Ausstieg am Känzelefelsen (rund 670 m) westwärts über den Grat- bzw. Wurzelweg entlang der Felskante zurück zur Burgruine Gebhardsberg und zum Parkplatz, 15 bis 20 Minuten. Die Ruine mit Gasthaus liegt direkt am Rückweg.",
  "gear": "Komplettes Klettersteigset und Helm. Handschuhe sind sinnvoll, das Konglomerat ist scharfkantig.",
  "highlights": [
   "Drei Varianten von B/C bis D/E an derselben Wand, gut zum Steigern",
   "Weiter Blick über Bodensee, Rheintal und die Stadt Bregenz",
   "Direkt am Stadtrand, mit Bus erreichbar",
   "Südlage: einer der wenigen Steige der Region, die auch im Winter oft gehen",
   "Ungewöhnlicher Fels aus Nagelfluh und Sandstein"
  ],
  "warnings": [
   "Zum Höhengewinn im Steig widersprechen sich die Quellen (rund 40 m aus den Höhenangaben, 70 bis 80 m laut bergsteigen.com), deshalb ist das Feld leer; die gesicherte Strecke wird mit 70 bis 140 m angegeben",
   "Nagelfluh und Sandstein werden bei Nässe extrem rutschig, bei Regen nicht einsteigen",
   "In der Steilzone gibt es keine Notausstiege",
   "Die rechte Variante überhängt und verlangt echte Armkraft",
   "Für Kinder allenfalls die linke B/C-Variante und nur mit vorhandener Klettersteigerfahrung"
  ],
  "sources": [
   "https://www.bergsteigen.com/touren/klettersteig/kaenzele-klettersteig/",
   "https://klettersteig.de/klettersteig/kaenzele_klettersteig/2275",
   "https://www.via-ferrata.de/klettersteige/topo/klettersteig-kaenzele-bregenz-kanzelfelsen",
   "https://www.alpenvereinaktiv.com/de/tour/klettersteig-kaenzele/18960530/"
  ],
  "verified": false
 },
 {
  "id": "via-kapf",
  "name": "Via Kapf",
  "grade": "D/E",
  "area": "Nordwestseite des Kapf über Meschach bei Götzis",
  "region": "Rheintal / Bodensee",
  "climbMeters": 110,
  "lengthMeters": 110,
  "approachMin": 30,
  "ferrataMin": 60,
  "descentMin": 30,
  "totalMin": 135,
  "startAlt": 1030,
  "summitAlt": 1153,
  "season": "April bis Oktober, nur bei trockenem, stabilem Wetter",
  "summary": "Sportlicher, durchgehend steiler Klettersteig an der Nordwestseite des Kapf über Götzis, eröffnet im Jahr 2000. Der Einstieg gleich nach der Rampe ist die Schlüsselstelle: senkrecht bis leicht überhängend im Bereich D/E. Danach folgen eine Querung nach rechts und eine glatte, gut abgesicherte Wandstelle, bevor der Steig durch ein ausgesetztes Verschneidungssystem zur Gipfelwiese zieht. Auf rund 110 Metern gesicherter Strecke und ebenso vielen Höhenmetern bleibt es durchgehend kraftfordernd, natürliche Griffe sind rar. Notausstiege gibt es keine, und der Steig lässt sich gut mit der parallel verlaufenden Via Kessi und der Via Örfla im Tal zu einem Steig-Triple verbinden.",
  "approach": "Von Götzis (448 m) über die kurvige Straße nach Meschach; entweder beim Schranken vor dem Spallenhof (rund 1040 m) kostenfrei parken oder weiter zur Millrütte (rund 1100 m, gebührenpflichtig). Auf Wald- und Schotterweg beziehungsweise steil im Wald hinunter zum Wandfuß auf etwa 1030 m. Die Zeitangaben der Quellen reichen von 15 Minuten bis zu einer Stunde, je nach Parkplatz; großzügig planen. Der steile Waldabstieg zum Einstieg ist bei Nässe der heikelste Abschnitt der Tour.",
  "descent": "Vom Ausstieg über die Wiese kurz zum Kapf-Gipfel (1153 m), dann auf dem Steig am Kamm nach Osten bequem zurück zum Parkplatz, rund 30 Minuten. Im Abstiegsbereich liegt eine Bogenschießanlage, unbedingt auf dem Weg bleiben. Alternativ über die Via Kessi abklettern, die sich dafür besser eignet.",
  "gear": "Komplette Klettersteigausrüstung, Helm und Klettersteighandschuhe. Gute Armkraft ist Voraussetzung.",
  "highlights": [
   "Überhängender Einstiegsaufschwung als Schlüsselstelle gleich zu Beginn",
   "Panorama auf Rheintal, Bodensee und den Säntis",
   "Direkt kombinierbar mit Via Kessi und Via Örfla zum Götzner Steig-Triple",
   "Sehr gutes Verhältnis von Anmarsch zu Kletterei"
  ],
  "warnings": [
   "Bewertungen der Quellen reichen von C/D (Marktgemeinde Götzis) über D (klettersteig.de) bis D/E (bergsteigen.com, via-ferrata.de); der Steig hält anhaltend D und erreicht am Einstieg D/E",
   "Keine Notausstiege in der oft senkrechten Wand, Umkehren ist praktisch nicht möglich",
   "Nur bei trockenem, stabilem Wetter begehen, der Zustieg im steilen Waldgelände wird bei Nässe gefährlich",
   "Für Anfänger und Kinder ungeeignet",
   "Im Abstiegsbereich befindet sich eine Bogenschießanlage"
  ],
  "sources": [
   "https://www.bergsteigen.com/touren/klettersteig/via-kapf-klettersteig/",
   "https://klettersteig.de/klettersteig/via_kapf/21",
   "https://www.via-ferrata.de/klettersteige/topo/via-kapf-klettersteig",
   "https://goetzis.at/freizeit/ausflugsziele/kapf-erwandern-und-beklettern/",
   "https://www.bergzeit.de/magazin/klettersteig-goetzis-vorarlberg/"
  ]
 },
 {
  "id": "via-kessi",
  "name": "Via Kessi",
  "grade": "D",
  "area": "Nordwestseite des Kapf über Meschach bei Götzis, parallel zur Via Kapf",
  "region": "Rheintal / Bodensee",
  "crux": "D/E",
  "climbMeters": 100,
  "lengthMeters": 100,
  "approachMin": 30,
  "ferrataMin": 60,
  "descentMin": 25,
  "totalMin": 130,
  "startAlt": 1010,
  "summitAlt": 1110,
  "season": "April bis Oktober, nur bei trockenem, stabilem Wetter",
  "summary": "Die Via Kessi verläuft parallel zur Via Kapf und wird meist mit ihr kombiniert, üblicherweise klettert man die Kessi ab und die Kapf wieder hinauf. Der Steig steht fast durchgehend senkrecht und arbeitet viel mit Trittstiften; zwei waagrechte Passagen unterbrechen die Steilheit und geben den Blick auf Bodensee und Rheintal frei. Die Route beginnt mit einer steilen, glatten Einstiegswand im Bereich C/D, quert nach rechts, steigt dann steil bis D an und erreicht in einem Überhang die Schlüsselstelle im Bereich D/E. Blickfang ist das Kessi-Loch, ein rund zwanzig Meter langer, vom Wasser glattgeschliffener Felsdurchschlupf. Auf rund 100 Metern bleibt es kraftfordernd, Notausstiege fehlen.",
  "approach": "Von Götzis auf der Bergstraße nach Meschach und beim Schranken kurz vor dem Spallenhof (rund 1040 m) kostenfrei parken, dann auf Forst- und Waldpfad in etwa 20 bis 30 Minuten zum Wandfuß auf rund 1010 m. Alternativ vom höher gelegenen Parkplatz Millrütte, von dort ist der Zustieg länger. Die Einstiege von Kessi und Kapf liegen nur wenige Minuten auseinander und sind beschildert.",
  "descent": "Vom Ausstieg auf dem markierten Wanderweg in 15 bis 25 Minuten zurück zum Parkplatz. Beliebt ist die Kombination mit der Via Kapf: einen Steig aufsteigen, den anderen abklettern.",
  "gear": "Komplettes Klettersteigset, Helm und Kletterhandschuhe. Das Stahlseil ist dünner als üblich, ein Set mit gut laufenden Karabinern zahlt sich aus.",
  "highlights": [
   "Kessi-Loch, ein ausgewaschener Felstunnel von rund 20 Metern",
   "Zwei waagrechte Aussichtspassagen mit Blick über das Rheintal bis zum Bodensee",
   "Ideale Doppelrunde mit der Via Kapf",
   "Schattige Nordwestwand, auch an Hitzetagen machbar"
  ],
  "warnings": [
   "Der frühere Widerspruch C/D gegen D/E löst sich in der Routenbeschreibung auf: die Linie hält anhaltend C/D bis D, die Schlüsselstelle im Überhang erreicht D/E; bergsteigen.com, via-ferrata.de und ferrata.guide führen den Steig deshalb insgesamt als D/E",
   "Nahezu senkrechtes bis überhängendes Gelände ohne Notausstieg",
   "Bei Nässe ist schon der steile Fußweg zum Einstieg heikel, der Ausstiegsbereich wird sehr rutschig",
   "Kein Anfängersteig, verlangt Kraftausdauer und Klettersteigroutine",
   "Das Sicherungsseil ist dünner als bei modernen Anlagen ausgeführt"
  ],
  "sources": [
   "https://www.bergsteigen.com/touren/klettersteig/via-kessi-klettersteig/",
   "https://klettersteig.de/klettersteig/via_kessi/20",
   "https://www.via-ferrata.de/klettersteige/topo/via-kessi",
   "https://www.bergzeit.de/magazin/klettersteig-goetzis-vorarlberg/"
  ]
 },
 {
  "id": "via-orfla",
  "name": "Via Örfla",
  "grade": "C/D",
  "area": "Örflaschlucht am Emmebach bei Götzis",
  "region": "Rheintal / Bodensee",
  "crux": "D",
  "climbMeters": 110,
  "lengthMeters": 110,
  "approachMin": 75,
  "ferrataMin": 45,
  "descentMin": 60,
  "totalMin": 180,
  "startAlt": 560,
  "season": "April bis Oktober, nur bei niedrigem Wasserstand des Emmebachs",
  "summary": "Ein Schluchtklettersteig ganz anderer Machart als die beiden Steige oben am Kapf: Die Via Örfla beginnt unten im Tal und folgt dem Emmebach flussaufwärts durch die enge, bewaldete Örflaschlucht. Über weite Strecken geht es unmittelbar am oder im Wasser entlang, der Bach wird mehrfach über Steine gequert, nasse Füße sind praktisch garantiert. Der Steig beginnt mit erdigen, gesicherten Bändern um B bis B/C und steigert sich zur technisch schwersten Passage am Ende: einer rund 30 Meter hohen, steilen bis leicht überhängenden Wand im Bereich C/D bis D, die sich auslassen lässt. Der Zustieg ist nicht beschildert und nur mit vereinzelten Steinmännchen markiert, Orientierungssinn und Trittsicherheit sind Voraussetzung. Durch das Mikroklima bleibt es auch im Hochsommer angenehm kühl.",
  "approach": "Ausgangspunkt ist der Parkplatz beim Schwimmbad bzw. Ringerzentrum in Götzis (rund 470 m). Von dort nach Osten in die Örflaschlucht hinein, zwischen Schwimmbad links und Emmebach rechts, unterhalb des Höhenrückens Hohe Lug. Der Weg ist unmarkiert, es gibt nur vereinzelte Steinmännchen und erkennbare Wegspuren. Bis zum Beginn der Drahtseilsicherung auf etwa 560 m rund 1:15 Stunden mit mehreren Bachquerungen.",
  "descent": "Nach dem Ausstieg über eine Brücke und weiter hinauf zur Kirche von Meschach, von dort auf Forststraße bzw. Wanderweg zurück nach Götzis, rund eine Stunde. Wer den ganzen Tag nutzen will, fährt anschließend zur Millrütte hoch und hängt Via Kapf und Via Kessi an.",
  "gear": "Komplette Klettersteigausrüstung, Helm und Handschuhe. Knöchelhohe, gut profilierte Schuhe wegen der Bachquerungen, Wechselsocken sinnvoll.",
  "highlights": [
   "Wilde Schluchtlandschaft direkt am Wasser",
   "Auch im Hochsommer kühl, gute Hitzealternative",
   "Steile, leicht überhängende Schlusswand als Höhepunkt, umgehbar",
   "Kaum begangener Geheimtipp, weil nicht beschildert"
  ],
  "warnings": [
   "Bachquerungen werden bei hohem Wasserstand zum Problem oder unmöglich, Pegel vorher prüfen",
   "Bei Gewitter besteht in der Schlucht Gefahr durch rasch ansteigendes Wasser",
   "Zustieg ist nicht markiert, Orientierungsvermögen und Trittsicherheit nötig",
   "Nasse, glitschige Steine im Bachbett, erhöhte Rutschgefahr",
   "Im Frühsommer Zeckengefahr im dichten Waldgelände",
   "Die Bewertungen der Quellen reichen von B/D über C/D bis D, weil die Schwierigkeit stark abschnittsweise variiert"
  ],
  "sources": [
   "https://www.bergsteigen.com/touren/klettersteig/via-oerfla-klettersteig/",
   "https://klettersteig.de/klettersteig/via_oerfla/22",
   "https://www.via-ferrata.de/klettersteige/topo/klettersteig-via-oerfla",
   "https://www.bergzeit.de/magazin/klettersteig-goetzis-vorarlberg/"
  ]
 },
 {
  "id": "saulakopf",
  "name": "Saulakopf-Klettersteig",
  "grade": "D",
  "area": "Ostwand und Südostgrat des Saulakopfs (2517 m)",
  "region": "Rätikon (Brandnertal/Montafon)",
  "crux": "D+",
  "climbMeters": 380,
  "lengthMeters": 400,
  "approachMin": 90,
  "ferrataMin": 120,
  "descentMin": 90,
  "totalMin": 330,
  "startAlt": 2140,
  "summitAlt": 2517,
  "season": "Juni bis Oktober",
  "hasExit": true,
  "summary": "Der bekannteste und schwerste alpine Klettersteig der Region zieht durch die steile Ostwand des Saulakopfs und geht oben in einen ausgesetzten Grat über. Die Schlüsselstelle kommt früh: rund 15 Minuten nach dem Einstieg wartet eine etwa 12 bis 15 Meter hohe, leicht überhängende Klammernreihe, die den Ausschlag für die Spitzenbewertung gibt. Danach pendelt der Steig meist zwischen C und C/D, mit senkrechten Wandstücken, einer Rinne und gestuftem Schrofengelände. Etwa in Wandmitte liegt ein Notausstieg, im oberen Drittel folgt eine luftige Leiterbrücke, die umgangen werden kann. Die 380 Klettermeter am Stück verlangen solide Armkraft und Ausdauer.",
  "approach": "Schnellste Variante: mit der Lünerseebahn ab Brand zur Bergstation (1979 m), über die Staumauer und den Saulajochsteig zum Saulajoch (2065 m), dann Richtung Heinrich-Hueter-Hütte; kurz vor der Hütte zweigt links ein beschilderter Pfad unter die Ostwand ab, ab Bergstation rund 1,5 Stunden. Alternative aus dem Montafon: Wanderbus ab Vandans zum Alpengasthof Rellstal, dann über die Heinrich-Hueter-Hütte in etwa 1,5 Stunden zum Einstieg. Im Schuttkar unter der Wand ist Steinschlag möglich, Helm und Gurt schon dort anlegen.",
  "descent": "Vom Gipfel südlich über den markierten Saulakopf-Normalweg (kurze versicherte Stellen im Bereich A/B) zum Saulajoch. Von dort entweder zurück über den Saulajochsteig zur Bergstation der Lünerseebahn oder ins Rellstal zur Bushaltestelle beim Alpengasthof absteigen. Der Normalweg ist der heikelste Teil des Tages, sobald er nass, verschneit oder vereist ist.",
  "gear": "Komplette Klettersteigausrüstung mit Helm. Für unsichere Geher ein kurzes Sicherungsseil, weil die Schlüsselstelle bereits am Anfang liegt.",
  "highlights": [
   "Kühne Linienführung durch die steile Ostwand",
   "Luftige Leiterbrücke im oberen Drittel",
   "Tiefblick zur Heinrich-Hueter-Hütte, Blick zur Zimba und über das Montafon",
   "Gut kombinierbar mit dem Alpin-live-Steig an der Lünerseebahn"
  ],
  "warnings": [
   "Bewertung uneinheitlich: klettersteig.de und die Tourismusstellen führen den Steig als D, bergsteigen.com stuft ihn insgesamt mit C/D bei einer D+-Schlüsselstelle ein",
   "Die schwerste Stelle kommt kurz nach dem Einstieg, Kräfte einteilen ist dort nicht möglich",
   "Steinschlaggefahr im Schuttkar unter der Wand und oberhalb des Notausstiegs",
   "Der Normalweg als Abstieg ist bei Nässe, Neuschnee oder Vereisung ernsthaft heikel",
   "Betriebszeiten der Lünerseebahn bzw. des Rellstal-Wanderbusses beachten"
  ],
  "sources": [
   "https://www.bergsteigen.com/touren/klettersteig/saulakopf-klettersteig/",
   "https://klettersteig.de/klettersteig/saulakopf_klettersteig/1575",
   "https://www.via-ferrata.de/klettersteige/topo/saulakopf-klettersteig",
   "https://www.montafon.at/de/klettersteig-saulakopf_vc4673"
  ],
  "verified": false
 },
 {
  "id": "sulzfluh-sudwandsteig",
  "name": "Sulzfluh-Klettersteig (Südwandsteig)",
  "grade": "C/D",
  "area": "Südwand der Sulzfluh (2817 m), Gipfel auf der Staatsgrenze",
  "region": "Rätikon (Grenze Vorarlberg/Graubünden)",
  "crux": "D",
  "climbMeters": 450,
  "lengthMeters": 600,
  "approachMin": 90,
  "ferrataMin": 150,
  "descentMin": 150,
  "totalMin": 390,
  "startAlt": 2360,
  "summitAlt": 2817,
  "season": "Juni bis Oktober",
  "summary": "Der große Südwandsteig auf die Sulzfluh gilt als einer der schönsten Sportklettersteige im Rätikon. Über rund 450 Höhenmeter wechseln Plattenpassagen, Steilstufen und Bänder, auf denen man kurz durchatmen kann; die Absicherung ist mit vielen Klammern und einzelnen Leitern vorbildlich. Nach dem Bankli im Mittelteil wird der Steig ausgesetzter, die leicht überhängenden Abschnitte fordern Kraft und erreichen mehrfach C/D. Unterwegs warten eine Dreiseilbrücke und eine Leiterbrücke. Wichtig für die Planung: Es gibt keinen Notausstieg, wer einsteigt, muss die Wand zu Ende klettern.",
  "approach": "Der übliche Zustieg führt von der Schweizer Seite: Anfahrt nach St. Antönien im Prättigau, Parkplatz Nr. 6 bei Partnun (1631 m), dann auf markiertem Weg Richtung Carschinahütte, unter der Südwand rechts abzweigen und über ein Schuttfeld zum Einstieg auf rund 2360 m, gekennzeichnet mit einer weißen Platte und rotem E. Rund 1,5 Stunden. Aus dem Montafon führt der Zugang über Latschau zur Tilisunahütte (mehrere Stunden, sinnvoll mit Hüttenübernachtung) und weiter über das Tilisunafürggeli Richtung Partnun.",
  "descent": "Vom Gipfel kurz nordwärts über Schrofen, dann östlich durch das Gemstobel absteigen; der markierte Weg führt oberhalb des Partnunsees zurück zum Parkplatz. Alternativ zurück zur Tilisunahütte oder weiter zur Lindauer Hütte, womit die Tour als Übergang ins Montafon endet.",
  "gear": "Komplette Klettersteigausrüstung und Helm, für schwächere Geher ein kurzes Sicherungsseil. Im Winter werden die Stahlseile am Ein- und Ausstieg demontiert.",
  "highlights": [
   "Langer, durchgehend sehr gut abgesicherter Sportklettersteig in kompaktem Kalk",
   "Dreiseilbrücke und Leiterbrücke im oberen Teil",
   "Gipfelpanorama bis zu den Eisbergen von Silvretta und Bernina",
   "Kombinierbar mit dem Gauablickhöhle-Steig auf der Vorarlberger Nordseite"
  ],
  "warnings": [
   "Grenzlage: Der Gipfel liegt auf der Staatsgrenze, Einstieg und Wand selbst auf Bündner Boden. Der Zustieg erfolgt praktisch immer von Partnun/St. Antönien in der Schweiz.",
   "Kein Notausstieg, Kraft und Kondition müssen für die gesamte Wand reichen",
   "Für Einsteiger trotz guter Absicherung sehr anstrengend, weil der Steig lang ist",
   "Zustieg über die Schweizer Seite: Parkgebühr, obere Parkplätze teils nur mit Saisonkarte",
   "Der Zustieg von der Carschinahütte war nach einem Felssturz gesperrt, aktuellen Stand prüfen"
  ],
  "sources": [
   "https://www.bergsteigen.com/touren/klettersteig/sulzfluh-klettersteig/",
   "https://klettersteig.de/klettersteig/klettersteig_sulzfluh/1343",
   "https://www.via-ferrata.de/klettersteige/topo/klettersteig-sulzfluh",
   "https://www.vorarlberg.travel/route/klettersteig-sulzfluh/"
  ],
  "verified": false
 },
 {
  "id": "bruckenwand",
  "name": "Brückenwand-Klettersteig",
  "grade": "D",
  "area": "Klettersteigpark Valkastiel, Burgruine oberhalb von Vandans",
  "region": "Rätikon (Montafon)",
  "approachMin": 50,
  "ferrataMin": 45,
  "descentMin": 40,
  "totalMin": 135,
  "startAlt": 1000,
  "season": "Juni bis September, talnah auch früher und später möglich",
  "summary": "Die zweite D-Linie im Klettersteigpark Valkastiel, benannt nach der Brückenwand über dem Mustergielbach. Sie ist steil, kraftfordernd und wird meist zusammen mit der Pfeilerwand geklettert, wenn man den Park an der oberen Schwierigkeitsgrenze angehen will. Für alle fünf Routen zusammen inklusive Zu- und Abstieg sollte man mehr als einen halben Tag rechnen. Getrennte Höhenmeter je Route werden von den Quellen nicht ausgewiesen.",
  "approach": "Zustieg wie zu allen Parkrouten: vom Ortszentrum Vandans entlang des Mustergielbachs bergwärts oder auf dem Forstweg Richtung Ruine Valkastiel, rund 50 Minuten.",
  "descent": "Über den Schluchtweg abwärts und auf dem Zustiegsweg zurück nach Vandans, etwa 40 Minuten.",
  "gear": "Komplette Klettersteigausrüstung und Helm, dazu eine Bandschlinge mit Karabiner zum Rasten.",
  "highlights": [
   "Steile Wandkletterei im Grad D",
   "Direkt neben der Pfeilerwand, beide gut kombinierbar",
   "Talnah und schnell erreichbar"
  ],
  "warnings": [
   "Weite Hakenabstände, rutschiges Gelände, nicht für Anfänger",
   "Bei Nässe nicht begehbar",
   "Vereinzelt Meldungen über verrostete Bügel und dünne Seilstücke, Zustand vor Ort prüfen",
   "Zu den Höhenmetern der Einzelroute machen die Quellen keine Angaben, deshalb ist das Feld leer"
  ],
  "sources": [
   "https://klettersteig.de/klettersteig/klettersteig_ruine_valkastiel_/2551",
   "https://www.bergsteigen.com/touren/klettersteig/valkastiel-ruine-klettersteig/",
   "https://www.vorarlberg.travel/route/klettersteigpark-valkastiel/"
  ],
  "verified": false
 },
 {
  "id": "fenster",
  "name": "Fenster-Klettersteig",
  "grade": "C",
  "area": "Klettersteigpark Valkastiel, Burgruine oberhalb von Vandans",
  "region": "Rätikon (Montafon)",
  "approachMin": 50,
  "ferrataMin": 40,
  "descentMin": 40,
  "totalMin": 130,
  "startAlt": 1000,
  "season": "Juni bis September, talnah auch früher und später möglich",
  "summary": "Die zweite Linie im Grad C im Klettersteigpark Valkastiel, benannt nach dem Felsenfenster, das sie durchsteigt. Sie ist kompakt und lässt sich gut als Ergänzung zur Hohen Wand klettern, wenn man die beiden D-Routen auslassen will. Wie alle Linien des Parks liegt sie in feuchter, schattiger Lage im engen Tal des Mustergielbachs. Getrennte Höhenmeter je Route werden von den Quellen nicht angegeben, für alle fünf Linien zusammen sind rund 90 Meter gesicherte Kletterstrecke dokumentiert.",
  "approach": "Zustieg wie zu allen Parkrouten: vom Ortszentrum Vandans entlang des Mustergielbachs bergwärts oder auf dem Forstweg Richtung Ruine Valkastiel, rund 50 Minuten.",
  "descent": "Über den Schluchtweg und den Zustiegsweg zurück nach Vandans, etwa 40 Minuten.",
  "gear": "Komplette Klettersteigausrüstung und Helm.",
  "highlights": [
   "Kletterei durch ein markantes Felsenfenster",
   "Kompakte C-Route, gut mit der Hohen Wand kombinierbar",
   "Schattig und kühl auch im Hochsommer"
  ],
  "warnings": [
   "Bei Nässe rutschig, der Fels trocknet in der engen Schlucht langsam ab",
   "Vereinzelt Meldungen über verrostete Bügel, Zustand vor Ort prüfen",
   "Zu den Höhenmetern der Einzelroute machen die Quellen keine Angaben, deshalb ist das Feld leer"
  ],
  "sources": [
   "https://klettersteig.de/klettersteig/klettersteig_ruine_valkastiel_/2551",
   "https://www.bergsteigen.com/touren/klettersteig/valkastiel-ruine-klettersteig/",
   "https://www.vorarlberg.travel/route/klettersteigpark-valkastiel/"
  ],
  "verified": false
 },
 {
  "id": "gauablickhohle",
  "name": "Gauablickhöhle-Klettersteig",
  "grade": "B/C",
  "area": "Rachenwand an der Sulzfluh-Nordseite, Stützpunkt Lindauer Hütte",
  "region": "Rätikon (Montafon)",
  "crux": "C",
  "climbMeters": 260,
  "lengthMeters": 500,
  "approachMin": 180,
  "ferrataMin": 120,
  "descentMin": 180,
  "totalMin": 480,
  "startAlt": 2200,
  "summitAlt": 2462,
  "season": "Juni bis Oktober",
  "summary": "Der Steig hat ein Alleinstellungsmerkmal: mitten in der Route liegt eine rund 300 bis 350 Meter lange Naturhöhle, die durchgehend mit Drahtseil gesichert und nur mit Stirnlampe begehbar ist. Klettertechnisch bleibt die Tour moderat, der Großteil liegt zwischen A/B und B/C, die einzige C-Stelle ist ein kleiner Überhang kurz nach dem Höhlenausgang. Anspruchsvoll ist der Umfang: Zu- und Abstieg summieren sich auf einen vollen Bergtag mit deutlich über 1000 Höhenmetern. In der Höhle geht es zunächst gesichert aufwärts, danach führt ein Handlaufseil eben bis leicht abschüssig durch den Berg. Der Austritt mitten in der Wand mit Blick ins Gauertal ist das eigentliche Highlight.",
  "approach": "Ausgangspunkt Latschau bei Tschagguns (Parkplatz Lünerseewerk/Golmerbahn, gebührenpflichtig). Zu Fuß durchs Gauertal oder mit der Golmerbahn und dem Latschätzer Höhenweg zur Lindauer Hütte (1744 m). Von der Hütte kurz ostwärts abwärts, dann rechts hinauf Richtung Rachen Sulzfluh, unterhalb der Wände von Drusenfluh, Drei Türme und Sulzfluh bis zum Plateau Auf den Bänken, wo links ein gelbes Schild zum Einstieg weist. Ab Lindauer Hütte etwa 1,5 Stunden; der untere Teil lässt sich mit dem Rad abkürzen, an der Abzweigung gibt es ein Fahrraddepot.",
  "descent": "Vom Ausstieg den Steigspuren folgen und über den blau-weiß markierten alpinen Steig durch den Rachen absteigen, loses Geröll, Helm anbehalten. Zurück zur Lindauer Hütte und auf dem Zustiegsweg nach Latschau. Alternativ über das Karrenfeld Richtung Tilisunahütte. Wer Reserven hat, erreicht vom Ausstieg in gut einer Stunde den Sulzfluh-Gipfel.",
  "gear": "Stirnlampe ist zwingend, ohne Licht ist die Höhle nicht begehbar. Sonst komplette Klettersteigausrüstung und Helm, dazu eine warme Schicht für die Höhle.",
  "highlights": [
   "Rund 300 bis 350 Meter lange Naturhöhle mitten im Klettersteig",
   "Austritt aus der Höhle mit Blick ins Gauertal",
   "Kombinierbar mit dem südseitigen Sulzfluh-Klettersteig zu einer Zwei-Tages-Runde",
   "Sulzfluh-Gipfel in einer Stunde als Zugabe erreichbar"
  ],
  "warnings": [
   "Ohne Stirnlampe unbegehbar",
   "Steinschlag beim Zustieg unter der Wand, schon an der Infotafel anseilen",
   "Notausstieg vorhanden, aber erst rund 90 Höhenmeter vor dem Ausstieg",
   "Sehr langer Zu- und Abstieg, konditionell fordernder als der Grad C vermuten lässt",
   "Der Rachen als Abstieg ist bei Schneelage nicht zu empfehlen"
  ],
  "sources": [
   "https://www.bergsteigen.com/touren/klettersteig/klettersteig-gauablickhoehle/",
   "https://klettersteig.de/klettersteig/gauablickhoehle_klettersteig/1508",
   "https://www.via-ferrata.de/klettersteige/topo/klettersteig-gauablickhoehle",
   "https://www.montafon.at/de/klettersteig-gauablick_vc4672"
  ]
 },
 {
  "id": "hohe-wand",
  "name": "Hohe-Wand-Klettersteig",
  "grade": "C",
  "area": "Klettersteigpark Valkastiel, Burgruine oberhalb von Vandans",
  "region": "Rätikon (Montafon)",
  "approachMin": 50,
  "ferrataMin": 45,
  "descentMin": 40,
  "totalMin": 135,
  "startAlt": 1000,
  "season": "Juni bis September, talnah auch früher und später möglich",
  "summary": "Die aussichtsreichste der mittelschweren Linien im Klettersteigpark Valkastiel. Die Route zieht über die Hohe Wand und bietet dabei luftige Passagen sowie zwei Seilbrücken, die den Charakter der Linie prägen. Klettertechnisch bleibt sie bei C und ist damit für Fortgeschrittene gut machbar, ohne extreme Kraftanforderungen. Sie lässt sich problemlos mit den anderen Parkrouten zu einem halben oder ganzen Tag kombinieren. Getrennte Wandhöhen je Linie werden von den Quellen nicht ausgewiesen.",
  "approach": "Vom Ortszentrum Vandans entlang des Mustergielbachs und dem Damm folgend bergwärts, alternativ auf dem Forstweg der Beschilderung Ruine Valkastiel folgen. Rund 50 Minuten bis zu den Einstiegen. Kostenlose Parkplätze im Zentrum oder an der Talstation der Golmerbahn.",
  "descent": "Über den Schluchtweg abwärts und auf dem Zustiegsweg zurück nach Vandans, etwa 40 Minuten.",
  "gear": "Komplette Klettersteigausrüstung und Helm. Die Seilbrücken einzeln begehen.",
  "highlights": [
   "Zwei luftige Seilbrücken",
   "Aussichtsreiche Wandpassagen über der Schlucht",
   "Gut mit den übrigen vier Parkrouten kombinierbar"
  ],
  "warnings": [
   "Bei Nässe sind Rinnen und Ausstiege sehr rutschig",
   "Vereinzelt Meldungen über verrostete Bügel, Zustand vor Ort prüfen",
   "Zu den Höhenmetern der Einzelroute machen die Quellen keine Angaben, deshalb ist das Feld leer"
  ],
  "sources": [
   "https://klettersteig.de/klettersteig/klettersteig_ruine_valkastiel_/2551",
   "https://www.bergsteigen.com/touren/klettersteig/valkastiel-ruine-klettersteig/",
   "https://www.vorarlberg.travel/route/klettersteigpark-valkastiel/"
  ],
  "verified": false
 },
 {
  "id": "blodigrinne-drusenfluh",
  "name": "Klettersteig Blodigrinne (Drusenfluh)",
  "grade": "C/D",
  "area": "Blodigrinne in der Nordflanke der Drusenfluh (2827 m), Stützpunkt Lindauer Hütte",
  "region": "Rätikon (Montafon)",
  "crux": "D",
  "climbMeters": 640,
  "lengthMeters": 1500,
  "approachMin": 90,
  "ferrataMin": 180,
  "descentMin": 240,
  "totalMin": 510,
  "startAlt": 2188,
  "summitAlt": 2827,
  "season": "Hochsommer bis Frühherbst, stark von der Schneelage abhängig",
  "summary": "Ein alpiner Klettersteig alter Schule auf einen der höchsten Gipfel des Rätikon, deutlich ernster als die Bewertung C vermuten lässt. Die Route zieht durch die Blodigrinne in der Nordflanke über meist festen Kalk, wobei gesicherte Passagen mit ungesicherten alpinen Wegstücken wechseln und die Sicherungen stellenweise weit auseinanderliegen. Im unteren Teil gibt es zwei Varianten: die schwere Einstiegsvariante erreicht D, die leichtere führt auf Klammern wie an einer Leiter hinauf und wird bei Altschnee nicht empfohlen. Entscheidend für die Planung: Rund 40 Meter unterhalb des Gipfels muss ungesichert im zweiten Schwierigkeitsgrad geklettert werden. Wer damit nicht souverän umgeht, sollte die Tour lassen.",
  "approach": "Basis ist die Lindauer Hütte (1744 m), erreichbar ab Latschau durchs Gauertal in gut zwei Stunden oder über die Golmerbahn und den Latschätzer Weg. Von der Hütte dem Wanderweg Richtung Öfapass folgen; auf etwa 2080 bis 2090 Metern zweigt bei einem Wegweiser links der Zustieg ab. Auf Trittspuren und roten Punkten bis zu einem kleinen Wasserfall, dort liegt der Einstieg auf rund 2188 m. Eine Übernachtung auf der Lindauer Hütte entzerrt den Tag deutlich.",
  "descent": "Auf derselben Route zurück, also durch den Klettersteig abklettern und dem Zustiegsweg zur Lindauer Hütte folgen, von dort nach Latschau. Für den gesamten Rückweg ins Tal rund vier Stunden rechnen. Die Orientierung im oberen Bereich ist anspruchsvoll, es gibt nur Steinmännchen und ausgeblichene Farbpunkte.",
  "gear": "Komplette Klettersteigausrüstung und Helm. Wegen der ungesicherten Kletterstelle unter dem Gipfel ist ein Seil zum Sichern des Partners sinnvoll.",
  "highlights": [
   "Hochalpiner Gipfel mitten im Rätikon",
   "Abwechslung aus Plattenkletterei, Rinne und alpinem Steig",
   "Zwei Einstiegsvarianten mit deutlich unterschiedlichem Anspruch"
  ],
  "warnings": [
   "Höhenmeter-Angabe unsicher: Die Rinne überwindet vom Einstieg bis zum Gipfel rund 640 Meter, tatsächlich durchgehend mit Drahtseil gesichert sind davon nur etwa 200 Meter, der Rest sind alpine Geh- und Kletterpassagen",
   "Rund 40 Meter ungesicherte Kletterei im II. Grad kurz unter dem Gipfel",
   "Keine Fluchtmöglichkeit aus der Route",
   "Sicherungen teilweise weit auseinander, ein Sturz hätte lange Fallhöhe",
   "Solange Altschnee auf den Bändern der Nordflanke liegt, wird von der leichteren rechten Variante abgeraten"
  ],
  "sources": [
   "https://www.bergsteigen.com/touren/klettersteig/blodigrinne-klettersteig/",
   "https://klettersteig.de/klettersteig/klettersteig_blodigrinne/1285",
   "https://www.via-ferrata.de/klettersteige/topo/klettersteig-blodigrinnedrusenfluh-im-montafon",
   "https://www.montafon.at/de/klettersteig-drusenfluh_vc13898"
  ],
  "verified": false
 },
 {
  "id": "rongg-wasserfall",
  "name": "Klettersteig Rongg-Wasserfall",
  "grade": "C",
  "area": "Ronggbachschlucht oberhalb von Gargellen",
  "region": "Rätikon (Montafon)",
  "climbMeters": 120,
  "approachMin": 20,
  "ferrataMin": 30,
  "descentMin": 30,
  "totalMin": 80,
  "startAlt": 1470,
  "season": "Juni bis Oktober",
  "hasExit": true,
  "summary": "Kurz, aber deutlich sportlicher als der benachbarte Röbischlucht-Steig. Die Route folgt der Ronggbachschlucht direkt am Wasserfall entlang; gleich zu Beginn quert eine Seilbrücke den Bach, danach zieht der Steig ausgesetzt oberhalb des Wassers weiter. Die Schlüsselstelle ist eine anfangs überhängende Wand im Grad C, anschließend geht es über steile Passagen neben dem Wasserfall hoch, bevor gestuftes Gelände zum Ausstieg führt. Mit rund 120 Höhenmetern in der Wand ist die Runde ideal für einen halben Tag, wegen der Nähe zum Wasser aber nur bei stabilen Verhältnissen. Im unteren Drittel gibt es einen Notausstieg.",
  "approach": "Vom Ortszentrum bzw. dem Parkplatz der Gargellner Bergbahnen auf dem Wanderweg Richtung Ronggalpe. Gegenüber der Talstation führt ein Pfad zur Bergkirche, dann am Hotel Madrisa vorbei über einen Wiesenweg Richtung Rongg-Wasserfall. Nach einer kleinen Holzbrücke ist der Einstieg auf etwa 1470 m erreicht, rund 15 bis 20 Minuten.",
  "descent": "Vom Ausstieg auf Steigspuren kurz zum Wanderweg, dann entweder links direkt hinunter nach Gargellen oder rechts leicht ansteigend zur bewirtschafteten Ronggalpe (1596 m) und von dort zurück ins Ortszentrum. Nicht über den Klettersteig absteigen, dort herrscht Gegenverkehr.",
  "gear": "Komplette Klettersteigausrüstung und Helm, Schuhe mit gutem Profil wegen der Feuchtigkeit in der Schlucht.",
  "highlights": [
   "Seilbrücke direkt am Anfang über den Ronggbach",
   "Steilpassage unmittelbar neben dem tosenden Wasserfall",
   "Einkehr auf der Ronggalpe am Rückweg",
   "Angenehm kühl an heißen Sommertagen"
  ],
  "warnings": [
   "Bei Nässe heikel, nasser Fels in der Schlucht ist rutschig",
   "Hochwassergefahr bei Starkregen und Gewitter, im Frühjahr auch bei Schneeschmelze",
   "Deutlich anspruchsvoller als der benachbarte Röbischlucht-Klettersteig"
  ],
  "sources": [
   "https://www.bergsteigen.com/touren/klettersteig/rongg-wasserfall-klettersteig/",
   "https://klettersteig.de/klettersteig/klettersteig_ronggwasserfall/1725",
   "https://www.montafon.at/de/klettersteig-rongg_vc13885"
  ]
 },
 {
  "id": "madrisella",
  "name": "Madrisella-Klettersteig",
  "grade": "C/D",
  "area": "Nordwand der Madrisella (2466 m), Novatal oberhalb Gaschurn/St. Gallenkirch",
  "region": "Rätikon (Montafon)",
  "climbMeters": 430,
  "lengthMeters": 750,
  "approachMin": 75,
  "ferrataMin": 150,
  "descentMin": 90,
  "totalMin": 315,
  "summitAlt": 2466,
  "season": "Juli bis Oktober",
  "summary": "Einer der beliebtesten neueren Steige der Region, 2018 eröffnet und ein echter Genussklettersteig für Fortgeschrittene. Die Route zieht durch die Nordwand der Madrisella und hält die Schwierigkeit ziemlich konstant bei C/D, mit steilen Wandpassagen, Querungen und kurzen Gratstücken. Der gut strukturierte Fels erlaubt viel natürliches Steigen, Bügel entschärfen gezielt die kräftigsten Stellen. Gleich zu Beginn steht eine fast senkrechte Wand an, danach folgt eine Rastbank mitten im Fels; der Höhepunkt ist eine rund 120 bis 150 Meter hohe, nahezu senkrechte Wand mit viel Luft unter den Füßen. Der Steig endet knapp unterhalb des Gipfels, von wo ein markierter Weg zum Gipfelkreuz führt.",
  "approach": "Auffahrt mit der Versettla Bahn ab Gaschurn, Betriebszeiten beachten. Von der Bergstation an der Nova Stoba vorbei hinunter zur Alpe Nova und weiter ins Novatal. Nach rund einer Stunde erreicht man den Seressee, wo ein Wegweiser am Felsblock links zum Klettersteig zeigt; Gurt und Helm am besten schon dort anlegen. Über steiles Blockgelände in 15 bis 20 Minuten zum Einstieg.",
  "descent": "Direkt beim Gipfelkreuz beginnt der markierte Wanderweg über die Versettla und an der Burg vorbei zurück zur Bergstation der Versettla Bahn, rund 1 bis 1,5 Stunden. Wer noch Lust hat, hängt gegen Ende den kurzen Klettersteig Burg an. Alternativ zur Nova Stoba absteigen und einkehren.",
  "gear": "Komplette Klettersteigausrüstung und Helm. Zusatzausrüstung ist nicht nötig, gesichert wird über dickes Stahlseil und viele Bügel.",
  "highlights": [
   "Konstant fordernde C/D-Kletterei in gut strukturiertem Urgestein",
   "Rund 120 bis 150 Meter hohe, fast senkrechte Schlüsselwand",
   "Pausenbank mitten in der Wand mit Blick ins Novatal",
   "Direkt mit dem kurzen Klettersteig Burg kombinierbar"
  ],
  "warnings": [
   "Keine Fluchtmöglichkeit aus der Route",
   "Bei Nässe unangenehm, es gibt einige erdige Passagen",
   "Steinschlaggefahr im Blockgelände des Zustiegs, Helm frühzeitig aufsetzen",
   "Betriebszeiten der Versettla Bahn beachten"
  ],
  "sources": [
   "https://www.bergsteigen.com/touren/klettersteig/madrisella-klettersteig/",
   "https://klettersteig.de/klettersteig/madrisella_klettersteig/2327",
   "https://www.montafon.at/de/klettersteig-madrisella-1_vc20897"
  ]
 },
 {
  "id": "pfeilerwand",
  "name": "Pfeilerwand-Klettersteig",
  "grade": "D",
  "area": "Klettersteigpark Valkastiel, Burgruine oberhalb von Vandans",
  "region": "Rätikon (Montafon)",
  "approachMin": 50,
  "ferrataMin": 45,
  "descentMin": 40,
  "totalMin": 135,
  "startAlt": 1000,
  "season": "Juni bis September, talnah auch früher und später möglich",
  "summary": "Eine der beiden schweren Linien im Klettersteigpark Valkastiel. Die Route klettert über einen steilen Pfeiler mit Überhängen, ausgeprägten Kanten und Platten und verlangt durchgehend Kraft und saubere Technik. Sie ist deutlich anspruchsvoller als die C-Routen des Parks und für Anfänger nicht geeignet. Eine Bandschlinge mit Karabiner zum Rasten ist hilfreich, weil es kaum entspannte Standplätze gibt. Getrennte Höhenmeter je Route sind in den Quellen nicht dokumentiert.",
  "approach": "Zustieg wie zu allen Parkrouten: vom Ortszentrum Vandans entlang des Mustergielbachs bergwärts oder auf dem Forstweg Richtung Ruine Valkastiel, rund 50 Minuten.",
  "descent": "Über den Schluchtweg abwärts und auf dem Zustiegsweg zurück nach Vandans, etwa 40 Minuten.",
  "gear": "Komplette Klettersteigausrüstung und Helm, dazu eine Bandschlinge mit Karabiner zum Rasten.",
  "highlights": [
   "Steile Pfeilerkletterei mit Überhängen",
   "Anspruchsvollste Linien des Parks direkt nebeneinander",
   "Talnahes Krafttraining am Fels ohne langen Anmarsch"
  ],
  "warnings": [
   "Weite Hakenabstände und rutschiges Gelände, ausdrücklich nichts für Anfänger",
   "Bei Nässe nicht begehbar",
   "Vereinzelt Meldungen über verrostete Bügel und dünne Seilstücke, Zustand vor Ort prüfen",
   "Zu den Höhenmetern der Einzelroute machen die Quellen keine Angaben, deshalb ist das Feld leer"
  ],
  "sources": [
   "https://klettersteig.de/klettersteig/klettersteig_ruine_valkastiel_/2551",
   "https://www.bergsteigen.com/touren/klettersteig/valkastiel-ruine-klettersteig/",
   "https://www.vorarlberg.travel/route/klettersteigpark-valkastiel/"
  ],
  "verified": false
 },
 {
  "id": "robischlucht-gargellner",
  "name": "Röbischlucht-Klettersteig (Gargellner Klettersteig)",
  "grade": "B/C",
  "area": "Röbischlucht am nördlichen Ortsrand von Gargellen",
  "region": "Rätikon (Montafon)",
  "climbMeters": 120,
  "approachMin": 15,
  "ferrataMin": 30,
  "descentMin": 30,
  "totalMin": 75,
  "startAlt": 1470,
  "season": "Juni bis Oktober",
  "familyFriendly": true,
  "summary": "Der leichtere der beiden Schluchtsteige in Gargellen und eine gute Wahl für Einsteiger und sportliche Kinder. Die Route verläuft knapp drei bis vier Meter über dem Bachbett durch die enge Schlucht und quert den Röbibach mehrfach. Im Schnitt bewegt sich der Steig im Bereich A/B, einzelne kurze Passagen erreichen B/C: eine glatte Schluchtwand, ein enger, oft rutschiger Durchschlupf unter einem Baum und die steilere Wandstufe beim Ausstieg. Dazwischen liegt immer wieder reines Gehgelände. An heißen Tagen ist die kühle Schlucht ein Argument für sich.",
  "approach": "Vom Parkplatz der Gargellner Bergbahnen dem Suggadinbach talauswärts folgen bis etwa 50 Meter nach dem Hotel Mateera. Dort die Hauptstraße queren, der Forststraße bergwärts folgen, rechts abzweigen, über eine schmale Holzbrücke und am rechten Ufer dem Röbibach entlang bis zum Beginn der Stahlseile auf rund 1470 m, etwa 15 Minuten.",
  "descent": "Vom Ausstieg in Kürze auf eine Wiese und über diese hinauf zur Ronggalpe. Von dort entweder direkt oder nordöstlich über Röbimaisäß auf markierten Wanderwegen zurück ins Ortszentrum von Gargellen. Nicht durch den Klettersteig absteigen.",
  "gear": "Komplette Klettersteigausrüstung und Helm. Für Kinder ein Set im passenden Gewichtsbereich, Schuhe mit gutem Profil wegen der nassen Flusssteine.",
  "highlights": [
   "Kurzer Schluchtsteig mit mehrfachen Bachquerungen",
   "Gut für Einsteiger und bergerfahrene Kinder geeignet",
   "Erfrischend an heißen Sommertagen",
   "Direkt mit dem Rongg-Wasserfall-Klettersteig kombinierbar"
  ],
  "warnings": [
   "Rutschgefahr auf nassem Fels, Flusssteinen und Holzstamm-Passagen",
   "Hochwassergefahr bei Starkregen und Gewitter, im Frühjahr bei Schneeschmelze",
   "Nach der ersten Bachquerung nicht den alten, ausgemusterten Versicherungen folgen"
  ],
  "sources": [
   "https://www.bergsteigen.com/touren/klettersteig/roebischlucht-klettersteig/",
   "https://klettersteig.de/klettersteig/gargellner_klettersteig_roebischluchtklettersteig_/462",
   "https://www.montafon.at/de/klettersteig-roebischlucht-3_vc13883"
  ]
 },
 {
  "id": "schlosswand-schluchtweg-uberschrei",
  "name": "Schlosswand – Schluchtweg – Überschreitung",
  "grade": "B/C",
  "area": "Klettersteigpark Valkastiel, Burgruine oberhalb von Vandans",
  "region": "Rätikon (Montafon)",
  "approachMin": 50,
  "ferrataMin": 45,
  "descentMin": 40,
  "totalMin": 135,
  "startAlt": 1000,
  "season": "Juni bis September, talnah auch früher und später möglich",
  "summary": "Die leichteste der fünf Linien im Klettersteigpark Valkastiel und der übliche Einstieg in die Anlage. Die Route verbindet die Schlosswand mit dem Schluchtweg zu einer Überschreitung der Burgruine und bleibt dabei im Bereich B/C. Der Schluchtweg dient bei mehreren der schwereren Parkrouten zugleich als Abstiegsverbindung, weshalb diese Linie auch als Rückweg genutzt wird. Talnah auf rund 1000 Metern gelegen, ist sie ein guter Saisonstart oder eine Ausweichoption bei schlechtem Bergwetter. Für die einzelnen Linien geben die Quellen keine getrennten Wandhöhen an, für alle fünf zusammen werden rund 90 Meter gesicherte Kletterstrecke genannt.",
  "approach": "Vom Ortszentrum Vandans der Hauptstraße bis zum Heitersheimer Platz folgen, nach der Brücke entlang des Mustergielbachs bergwärts und dem Damm folgen bis zum Einstieg. Alternativ auf dem Forstweg der Beschilderung Ruine Valkastiel folgen und später die rechte Abzweigung nehmen, um an den Wehren vorbeizukommen. Rund 50 Minuten. Kostenlose Parkplätze im Zentrum bei der Gemeinde oder an der Talstation der Golmerbahn.",
  "descent": "Über den Schluchtweg und anschließend auf dem Zustiegsweg entlang des Mustergielbachs zurück nach Vandans, etwa 40 Minuten.",
  "gear": "Komplette Klettersteigausrüstung und Helm.",
  "highlights": [
   "Leichteste Linie des Parks, guter Einstieg in die Anlage",
   "Kletterei durch Schlucht und über die Schlosswand",
   "Historische Burgruine in exponierter Lage",
   "Talnah und wetterunabhängiger als die hochalpinen Steige"
  ],
  "warnings": [
   "Rinnen- und Schluchtpassagen sind bei Nässe sehr rutschig",
   "In Einzelfällen wurden verrostete Bügel und dünne Seilstücke gemeldet, Zustand vor Ort prüfen",
   "Das enge, feuchte Tal ist bei Gewitter ungeeignet",
   "Zu den Höhenmetern der Einzelroute machen die Quellen keine Angaben, deshalb ist das Feld leer"
  ],
  "sources": [
   "https://klettersteig.de/klettersteig/klettersteig_ruine_valkastiel_/2551",
   "https://www.bergsteigen.com/touren/klettersteig/valkastiel-ruine-klettersteig/",
   "https://www.montafon.at/de/klettersteigpark-valkastiel-3_vc4853"
  ],
  "verified": false
 },
 {
  "id": "vaude-gargellner-kopfe-schmugglers",
  "name": "VAUDE Klettersteig Gargellner Köpfe (Schmugglersteig)",
  "grade": "C",
  "area": "Gargellner Köpfe (2559 m) oberhalb von Gargellen",
  "region": "Rätikon (Montafon)",
  "crux": "C/D",
  "climbMeters": 300,
  "lengthMeters": 900,
  "approachMin": 45,
  "ferrataMin": 90,
  "descentMin": 45,
  "totalMin": 180,
  "startAlt": 2270,
  "summitAlt": 2559,
  "season": "Juni bis Oktober",
  "summary": "Ein hochalpiner Steig mit ungewöhnlich kurzem Zustieg, weil die Bergbahn fast bis vor die Wand fährt. Geklettert wird im dunklen Gneis des Silvrettakristallins, abwechselnd über Grat- und Wandpassagen. Nach dem Einstieg über einen schrägen Grat und eine Steilstufe teilt sich die Route an einer Hinweistafel: Die Normalvariante bleibt bei C und führt über Plattenwände auf den Grat, die schwerere nimmt zwei Seilbrücken von 19 und 23 Metern mit und steigt anschließend durch die leicht überhängende Schmugglerwand, wo man auf rund 20 Metern im Grad C/D zupacken muss. Beide Varianten treffen sich wieder und enden nach der etwa 12 Meter hohen Kristallwand am Gipfel. Gesichert ist das Ganze mit rund 900 Metern Stahlseil und etwa 100 Trittklammern.",
  "approach": "Mit der Gargellner Schafbergbahn zur Bergstation beim Schafberg Hüsli (2130 m). Von dort Richtung St. Antönier Joch bis zur Bergstation der Kristallbahn, dann links auf einem Pfad am Grat entlang bis zu einer Scharte (eine kurze Stelle versichert), anschließend leicht rechts durch den Hang und über einen Schotterpfad zum Einstieg bei der Infotafel auf rund 2270 m. Etwa 45 Minuten.",
  "descent": "Direkt vom Gipfelkreuz führt eine drahtseilgesicherte Wandstufe abwärts auf den flacher werdenden Grat. Nach dem Ende der Drahtseile über einen steilen, teils unguten Schotterpfad zu den Lawinenverbauungen oberhalb der Bergstation des Sessellifts Gargellner Köpfe, von dort dem Fahrweg folgen, rund 15 Minuten.",
  "gear": "Komplette Klettersteigausrüstung und Helm. Die beiden Seilbrücken immer nur einzeln begehen.",
  "highlights": [
   "Zwei Seilbrücken von 19 und 23 Metern Länge",
   "Leicht überhängende Schmugglerwand als Schlüsselstelle der schweren Variante",
   "Senkrechte Kristallwand als Gipfelfinale",
   "360-Grad-Blick in Silvretta, Rätikon und zum Arlberg",
   "Sehr kurzer Zustieg dank Bergbahn"
  ],
  "warnings": [
   "Notabstieg vorhanden, aber steil, mit Geröll und steilem Gras, bei Nässe heikel",
   "Die Variante über die Seilbrücken ist mit C/D deutlich schwerer als die Normalroute",
   "Betriebszeiten der Gargellner Bergbahnen beachten"
  ],
  "sources": [
   "https://www.bergsteigen.com/touren/klettersteig/vaude-schmugglersteig-klettersteig/",
   "https://klettersteig.de/klettersteig/vaude_schmugglersteig/1757",
   "https://www.montafon.at/de/vaude-klettersteig-gargellner-koepfe_vc13858"
  ]
 },
 {
  "id": "neyerschartensteig",
  "name": "Neyerschartensteig",
  "grade": "C/D",
  "area": "Neyerscharte (2390 m) am Zimba-Ostgrat, zwischen Rellstal und Sarotlatal",
  "region": "Rätikon (Montafon/Brandnertal)",
  "climbMeters": 190,
  "approachMin": 75,
  "ferrataMin": 45,
  "descentMin": 120,
  "totalMin": 240,
  "startAlt": 2200,
  "summitAlt": 2390,
  "season": "Juli bis Anfang Oktober",
  "summary": "Ein 2003 angelegter alpiner Steig, der die Neyerscharte am Ostgrat der Zimba erschließt und damit Rellstal und Sarotlatal verbindet. Der Steig beginnt in einer breiteren Scharte, steigt zunächst mäßig an und ist mehrfach unterbrochen; das Herzstück ist eine rund 20 Meter lange Seilbrücke über eine Scharte, die auch umgangen werden kann. Die schwerste Passage ist die Querung eines Felsvorsprungs, an der Seilstücke und Trittstifte als Tritte dienen, dort liegt der Grad bei C/D. Der Großteil der Kletterei bewegt sich dagegen nur um B. Wichtig: Der Steig endet in der Scharte, der Weiterweg auf die Zimba ist Kletterei und kein Klettersteig.",
  "approach": "Ausgangspunkt ist die Rellskapelle (1465 m), Endhaltestelle des Wanderbusses ab Vandans. Von dort dem Fahrweg zur Heinrich-Hueter-Hütte (1766 m) folgen, hinter der Hütte dem markierten Weg Richtung Zimbajoch und Neyerscharte. Auf etwa 1980 m rechts halten und leicht ansteigend über Alm und Schotter zum Einstieg auf rund 2200 m. Ab der Hütte etwa 1:15 Stunden.",
  "descent": "Entweder auf dem Aufstiegsweg zurück zur Heinrich-Hueter-Hütte und zur Rellskapelle, oder auf der Gegenseite hinunter zur Sarotlahütte und weiter nach Brand, was einen Hüttenübergang von rund 4,5 Stunden ergibt. Fahrplan des Rellstal-Shuttles beachten, die letzte Rückfahrt liegt am Nachmittag.",
  "gear": "Komplette Klettersteigausrüstung und Helm, knöchelhohe Bergschuhe. Wegen des brüchigen Geländes ist der Helm nicht verhandelbar, für unsichere Geher ein kurzes Sicherungsseil.",
  "highlights": [
   "Rund 20 Meter lange Seilbrücke über die Scharte, umgehbar",
   "Direkter Blick auf die Zimba und ihre Kletterrouten",
   "Ermöglicht den Hüttenübergang Heinrich-Hueter-Hütte zur Sarotlahütte",
   "Wenig begangen und entsprechend ruhig"
  ],
  "warnings": [
   "Das Stahlseil ist mehrfach unterbrochen, einzelne Querungen sind luftig und ungesichert",
   "Der Sicherungszustand entspricht nicht dem Stand moderner Anlagen, Seile sind teils steinschlagbeschädigt",
   "Deutliche Steinschlaggefahr durch brüchigen, schuttigen Untergrund",
   "Bei Nässe ungeeignet",
   "Frühere Angaben von 325 oder 925 Höhenmetern beziehen sich auf den gesamten Tagesaufstieg ab Hütte bzw. ab Tal, nicht auf den Steig"
  ],
  "sources": [
   "https://www.bergsteigen.com/touren/klettersteig/neyerscharte-klettersteig-zimba/",
   "https://klettersteig.de/klettersteig/neyerschartensteig/1284",
   "https://www.klettersteig-montafon.de/via-ferrata/Neyerscharte/"
  ]
 },
 {
  "id": "kleinlitzner-sudgrat-ernst-scheib",
  "name": "Kleinlitzner-Südgrat (Ernst-Scheib-Steig)",
  "grade": "B/C",
  "area": "Kleinlitzner (2783 m) hinter der Saarbrücker Hütte",
  "region": "Silvretta (Montafon)",
  "crux": "C",
  "climbMeters": 120,
  "approachMin": 170,
  "ferrataMin": 50,
  "descentMin": 40,
  "totalMin": 330,
  "summitAlt": 2783,
  "season": "Juli bis September, abhängig von Schneelage",
  "summary": "Gilt als erster Klettersteig im Montafon und ist ein kurzer, aber hochalpiner Steig direkt hinter der Saarbrücker Hütte. Über eine felsige Rinne, erdige Rampen, einen kurzen Kamin und mehrere Aufschwünge geht es zum Gipfelplateau mit Kreuz, überwiegend im Bereich A/B bis B/C mit Schlüsselstellen im Grad C. Durchgehende Stahlseile und Trittstifte stützen in den steileren Passagen. Die eigentliche Herausforderung ist nicht die Kletterei, sondern die Höhe von fast 2800 Metern und der lange Anmarsch. Praktisch nur mit Hüttenübernachtung entspannt machbar.",
  "approach": "Wichtig: Die Silvretta-Hochalpenstraße ist längerfristig gesperrt, die Bielerhöhe ist aus dem Montafon derzeit nur über die Vermuntbahn ab Partenen und den anschließenden Tunnelbus erreichbar. Vom Vermuntsee zu Fuß in rund 2,5 Stunden zur Saarbrücker Hütte (2538 m). Der Einstieg liegt etwa 350 Meter südlich der Hütte: vom Wanderweg Richtung Tübinger Hütte zweigt nach etwa fünf Minuten rechts ein Pfad Richtung Klein Litzner ab. Die versicherte Linksquerung beginnt schon unmittelbar links der Hüttenterrasse.",
  "descent": "Auf derselben Route zurück zur Saarbrücker Hütte, dabei mit Gegenverkehr rechnen. Weiter zum Vermuntsee und mit Tunnelbus und Vermuntbahn nach Partenen.",
  "gear": "Komplette Klettersteigausrüstung und Helm. Der Helm ist hier nicht optional, Steinschlag durch Vorausgehende ist im Rinnengelände wahrscheinlich.",
  "highlights": [
   "Ältester Klettersteig im Montafon",
   "Kurze, lohnende Kletterei in festem Urgestein",
   "Gipfelblick auf die Dreitausender der Silvretta",
   "Ideale Ergänzung zu einer Hüttentour auf der Saarbrücker Hütte"
  ],
  "warnings": [
   "Steinschlaggefahr durch Vorausgehende, Helm zwingend",
   "Keine Fluchtmöglichkeit aus der Route",
   "Hochalpine Lage, Wetter und Schneelage vorab sorgfältig prüfen",
   "Anreise nur über Vermuntbahn und Tunnelbus, Betriebszeiten prüfen",
   "Die 330 Minuten Gesamtzeit enthalten den langen Hüttenzustieg, der Steig selbst dauert nur rund 50 Minuten"
  ],
  "sources": [
   "https://klettersteig.de/klettersteig/kleinlitzner_suedgrat_ernst_scheib_steig_/40",
   "https://www.montafon.at/de/klettersteig-kleinlitzner-2_vc13818"
  ]
 },
 {
  "id": "staumauer-silvrettasee",
  "name": "Staumauer-Klettersteig Silvrettasee",
  "grade": "A/B",
  "area": "Betonstaumauer des Silvretta-Stausees, Bielerhöhe (Gemeinde Gaschurn)",
  "region": "Silvretta (Montafon)",
  "crux": "B",
  "climbMeters": 50,
  "lengthMeters": 320,
  "approachMin": 15,
  "ferrataMin": 45,
  "descentMin": 5,
  "totalMin": 65,
  "startAlt": 1970,
  "summitAlt": 2030,
  "season": "Juni bis Oktober, abhängig von der Erreichbarkeit der Bielerhöhe",
  "familyFriendly": true,
  "summary": "Ein ungewöhnlicher Klettersteig, weil er nicht über Fels, sondern über die Betonstaumauer des Silvretta-Stausees führt. Auf rund 320 Metern gesicherter Strecke und etwa 50 Höhenmetern geht es die geneigte Mauer hinauf, künstliche Tritte und Griffe aus Silvretta-Gestein erleichtern das Steigen. Unterwegs gibt es aussichtsreiche Sitzbänke, eine mehrteilige Seilbrücke zwischen den Mauerpfeilern und ein zwölf Meter langes Schwingpendel, das sich über einen Handlauf umgehen lässt. Der Ausstieg erfolgt über eine Plattform direkt auf die Mauerkrone mit Blick zum Piz Buin. Mit gut einer Stunde ist es ein ideales Zusatzprogramm für einen Tag an der Bielerhöhe.",
  "approach": "Vom Parkplatz Silvrettasee dem Weg Richtung Madlenerhaus folgen, ein schmaler Pfad führt weiter zum Fuß der Staumauer auf rund 1970 m, wo der Einstieg an der tiefsten Stelle in der Mauermitte liegt, etwa 10 bis 15 Minuten. Wichtig für die Anreise: Die Silvretta-Hochalpenstraße ist von der Montafoner Seite gesperrt, die Bielerhöhe ist über die Vermuntbahn ab Partenen und den Tunnelbus erreichbar; von Galtür im Paznaun besteht die Zufahrt über die mautpflichtige Hochalpenstraße.",
  "descent": "Vom Ausstieg auf der Mauerkrone in wenigen Minuten zurück zum Ausgangspunkt, ein eigentlicher Abstieg entfällt.",
  "gear": "Komplettes Klettersteigset und Helm. Schuhe mit steifer, gut haftender Sohle sind wichtig, ein Teil der Tritte ist reine Reibung auf Beton. Eine Rastschlinge ist praktisch, für Kinder ein Set im passenden Gewichtsbereich.",
  "highlights": [
   "Klettersteig über eine Staumauer statt über Fels",
   "Zwölf Meter langes Schwingpendel, umgehbar",
   "Mehrteilige Seilbrücke zwischen den Mauerpfeilern",
   "Ausstiegsplattform auf der Mauerkrone mit Blick auf den Piz Buin",
   "Sehr kurz und gut mit einer Wanderung kombinierbar"
  ],
  "warnings": [
   "Zur Familientauglichkeit widersprechen sich die Quellen: bergsteigen.com und der Betreiber halten den Steig für kinderfreundlich, via-ferrata.de rät Einsteigern und Kindern wegen der weit auseinanderliegenden Betontritte ab",
   "Beton bietet bei Nässe kaum Reibung, weiche Zustiegsschuhe sind ungeeignet",
   "Ausgesetzte Seilbrücke, Schwindelfreiheit nötig",
   "Keine Fluchtmöglichkeit aus der Route",
   "Auf über 2000 Metern kann das Wetter schnell umschlagen"
  ],
  "sources": [
   "https://www.bergsteigen.com/touren/klettersteig/silvrettasee-staumauer-klettersteig/",
   "https://klettersteig.de/klettersteig/staumauer_klettersteig_silvrettasee/2459",
   "https://www.via-ferrata.de/klettersteige/topo/technosteig-staumauer-silvrettasee",
   "https://www.silvretta-bielerhoehe.at/en/active/kletterpark",
   "https://www.vorarlberg.travel/route/staumauer-klettersteig-silvrettasee/"
  ],
  "verified": false
 },
 {
  "id": "uebungs-wiesbadener-huette-linker",
  "name": "Uebungsklettersteig Wiesbadener Huette – linker Sektor",
  "grade": "B/C",
  "area": "Uebungsfelsen bei der Wiesbadener Huette (2443 m)",
  "region": "Silvretta (Montafon)",
  "climbMeters": 25,
  "ferrataMin": 30,
  "descentMin": 5,
  "totalMin": 35,
  "season": "Juli bis September",
  "summary": "Der zweite, schwerere der beiden Uebungssteige direkt bei der Huette. Kurz, steil und gut zum Ausprobieren, bevor es an die grossen Touren geht. Die angegebenen Zeiten gelten ab der Huette, nicht ab dem Tal.",
  "warnings": [
   "Zeiten gelten ab der Huette; der Zustieg dorthin ist ein eigener halber Tag",
   "Auf 2443 m kann auch im Sommer Neuschnee liegen"
  ],
  "sources": [
   "https://www.montafon.at/de/klettersteige"
  ],
  "verified": false
 },
 {
  "id": "ubungs-e-wiesbadener-hutte",
  "name": "Übungsklettersteige Wiesbadener Hütte",
  "grade": "A/B",
  "area": "Klettergarten unmittelbar bei der Wiesbadener Hütte im Ochsental",
  "region": "Silvretta (Montafon)",
  "crux": "B/C",
  "approachMin": 150,
  "ferrataMin": 40,
  "descentMin": 150,
  "totalMin": 340,
  "startAlt": 2450,
  "season": "Etwa Juli bis September, abhängig von Schneelage und Erreichbarkeit der Bielerhöhe",
  "familyFriendly": true,
  "summary": "Zwei kurze, erneuerte Übungsklettersteige im Klettergarten unmittelbar bei der Wiesbadener Hütte, gedacht zum Üben und für Ausbildungsgruppen. Der rechte Steig hält A/B mit einer kleinen Steilstufe im oberen Drittel, der linke ist mit B/C etwas anspruchsvoller und überhängt oben leicht, dort sind ein paar kräftige Züge gefragt. Gesichert wird durchgehend mit Stahlseil, Trittklammern gibt es kaum, geklettert wird überwiegend am Fels. Für sich allein lohnt der lange Zustieg nicht, in Kombination mit einer Hüttenübernachtung, einer Silvretta-Hochtour oder einem Ausbildungskurs sind die Steige aber ideal. Die reine Kletterzeit beträgt nur rund 20 bis 40 Minuten.",
  "approach": "Von der Bielerhöhe (2032 m) auf dem markierten Weg am Silvrettasee entlang und durch das Ochsental zur Wiesbadener Hütte (2443 m), rund 2:30 Stunden. Von der Hütte bei der Kapelle vorbei und dem markierten Steiglein folgen, nach wenigen Minuten steht man im Klettergarten. Die Bielerhöhe ist aus dem Montafon derzeit nur über Vermuntbahn und Tunnelbus ab Partenen erreichbar.",
  "descent": "Von beiden Übungssteigen führt ein Pfad in wenigen Minuten zurück zum Einstieg, dabei nicht bei den Ausstiegen nach rechts absteigen. Zurück zur Bielerhöhe auf dem Zustiegsweg, rund 2:30 Stunden.",
  "gear": "Komplettes Klettersteigset und Helm. Weil der Zustieg lang und hochalpin ist, gehören Bergschuhe, Wetterschutz und Verpflegung dazu.",
  "highlights": [
   "Zwei Linien in A/B und B/C direkt bei der Hütte",
   "Ideal für Ausbildung, Einsteiger und Familien mit Hüttenübernachtung",
   "Hochalpine Kulisse im Ochsental unter dem Piz Buin",
   "In wenigen Minuten von der Wiesbadener Hütte erreichbar"
  ],
  "warnings": [
   "Zur Wandhöhe machen die Quellen unterschiedliche Angaben (etwa 35 bis 60 Meter), deshalb ist das Feld leer",
   "Die Gesamtzeit besteht fast vollständig aus Hüttenzu- und -abstieg, die Steige selbst dauern 20 bis 40 Minuten",
   "Hochalpine Lage auf rund 2450 m, Wetterumschwung, Kälte und Altschneefelder im Frühsommer einplanen",
   "Wenig Trittklammern, es wird überwiegend am Fels geklettert",
   "Nicht bei den Ausstiegen nach rechts absteigen"
  ],
  "sources": [
   "https://www.bergsteigen.com/touren/klettersteig/uebungsklettersteige-wiesbadener-huette/",
   "https://www.vorarlberg.travel/route/uebungsklettersteig-wiesbadener-huette/",
   "https://www.wiesbadener-huette.com/klettern-wiesbadener-huette/"
  ],
  "verified": false
 },
 {
  "id": "hochjoch-hochalpila-grat",
  "name": "Klettersteig Hochjoch – Hochalpila-Grat",
  "grade": "B",
  "area": "Hochalpila-Grat zum Hochjoch (2520 m), Skigebiet Silvretta Montafon bei Schruns",
  "region": "Verwall (Montafon)",
  "climbMeters": 100,
  "lengthMeters": 750,
  "approachMin": 90,
  "ferrataMin": 60,
  "descentMin": 60,
  "totalMin": 210,
  "summitAlt": 2520,
  "season": "Juli bis Oktober",
  "summary": "Die deutlich moderatere Zugangsvariante zum Hochjoch. Der Steig folgt dem Hochalpila-Grat am durchgehenden Stahlseil und bleibt im Grad B, ist aber ein alpiner Gratsteig, der Trittsicherheit und Schwindelfreiheit verlangt. Er wurde bewusst so angelegt, dass ihn auch schwächere Geher im Aufstieg bewältigen können. Am Ende der Gratstrecke hat man die Wahl: direkt zum Gipfelkreuz und über denselben Grat zurück, oder zur 60 Meter langen Seilhängebrücke absteigen und diese als Höhepunkt mitnehmen. Damit ist die große Brücke auch ohne die vierstündige Westwand erreichbar.",
  "approach": "Von der Bergstation Sennigrat über die Wormser Hütte zur Bergstation der Hochalpila Bahn, zu Fuß rund 1,5 Stunden. Alternativ mit Grasjochbahn und Hochalpila Bahn direkt in die Nähe des Einstiegs. Vor Ort der Beschilderung Klettersteig Hochjoch folgen, der Einstieg liegt bei der Bergstation der Hochalpila Bahn.",
  "descent": "Über denselben Grat zurück zur Bergstation der Hochalpila Bahn und weiter zur Wormser Hütte beziehungsweise zum Sennigrat. Keinesfalls über die Westwand-Variante absteigen, dort herrscht Gegenverkehr.",
  "gear": "Komplette Klettersteigausrüstung und Helm.",
  "highlights": [
   "Zugang zur 60-Meter-Seilhängebrücke ohne die schwere Westwand",
   "Weiter Blick auf Silvretta, Rätikon und Verwall sowie die drei Bergseen",
   "Gute Wahl, wenn die Kondition für die Westwand nicht reicht"
  ],
  "warnings": [
   "Trotz Grad B ein alpiner Gratsteig, Trittsicherheit und Schwindelfreiheit erforderlich",
   "Bei Nässe rutschig durch erdige Passagen",
   "Nicht über die Westwand-Variante absteigen",
   "Betriebszeiten der Bergbahnen beachten"
  ],
  "sources": [
   "https://klettersteig.de/klettersteig/hochjoch_versicherte_gratwanderung_b_/2346",
   "https://www.montafon.at/en/hochjoch-via-ferrata_vc20895"
  ]
 },
 {
  "id": "hochjoch-westwand",
  "name": "Klettersteig Hochjoch – Westwand",
  "grade": "C",
  "area": "Westwand des Hochjochs (2520 m), Skigebiet Silvretta Montafon bei Schruns",
  "region": "Verwall (Montafon)",
  "climbMeters": 450,
  "lengthMeters": 1750,
  "approachMin": 45,
  "ferrataMin": 240,
  "descentMin": 60,
  "totalMin": 345,
  "startAlt": 2190,
  "summitAlt": 2520,
  "season": "Juli bis Oktober",
  "summary": "Mit rund 1750 Metern Seillänge ist das der längste Klettersteig Vorarlbergs. Technisch bleibt er mit Grad C moderat, konditionell ist er die eigentliche Herausforderung: Man hängt etwa vier Stunden am durchgehenden Stahlseil und legt dabei rund 450 Höhenmeter im Steig zurück. Vom Einstieg oberhalb des Schwarzsees führt die Route durch die Westwand zum Gipfelbereich, zwei steilere Plattenquerungen im Grad C verlangen Kraft und saubere Tritttechnik, nach oben wird der Steig leichter. Das Finale ist eine rund 60 Meter lange Seilhängebrücke kurz vor dem Gipfelkreuz, die sich gesichert umgehen lässt. Der Steig hat viele erdige und grasige Passagen und ist bei Nässe nicht zu empfehlen.",
  "approach": "Auffahrt mit Hochjochbahn und Sessellift Sennigrat ab Schruns. Von der Bergstation Sennigrat kurz nordwärts, dann rechts entlang der Piste und auf steilem Pfad hinunter zum Seetalhüsli am Kälbersee. Kurz dem Schotterweg Richtung Wormser Hütte folgen, bei einem Wegweiser links hinab zum Schwarzsee, um den See herum und über den steilen Wiesenpfad zum Einstieg auf rund 2190 m. Etwa 30 bis 45 Minuten.",
  "descent": "Der Klettersteig endet bei der Bergstation der Hochalpila Bahn (nur Winterbetrieb). Von dort der Piste und dem Wanderweg zur Wormser Hütte folgen und weiter zurück zum Sennigrat, dann mit Sessellift und Hochjochbahn ins Tal. Alternativ über den Schotterweg zum Seetalhüsli und durch den Skitunnel zur Bergstation der Hochjochbahn, das kostet etwa eine halbe Stunde mehr.",
  "gear": "Komplette Klettersteigausrüstung und Helm. Ausreichend zu trinken, es gibt in vier Stunden Wandzeit keine Einkehrmöglichkeit.",
  "highlights": [
   "Längster Klettersteig Vorarlbergs mit rund 1750 Metern Seillänge",
   "Rund 60 Meter lange Seilhängebrücke kurz vor dem Gipfel",
   "Panorama über Rätikon, Silvretta und Verwall sowie ins Seetal mit Herz-, Kälber- und Schwarzsee",
   "Kälbersee-Übungssteige liegen direkt am Zustieg als Ausweichoption"
  ],
  "warnings": [
   "Bei Nässe wegen der vielen erdigen und grasigen Passagen nicht zu empfehlen",
   "Sehr lange Runde, Wetterentwicklung und Gewittergefahr genau beobachten",
   "Der Notabstieg bei den Lawinensprengmasten ist steil und bei Nässe heikel",
   "Betriebszeiten der Bergbahnen beachten, sonst wird der Rückweg deutlich länger"
  ],
  "sources": [
   "https://www.bergsteigen.com/touren/klettersteig/hochjoch-klettersteig-schruns/",
   "https://klettersteig.de/klettersteig/klettersteig_hochjoch_variante_c_/2345",
   "https://www.montafon.at/en/hochjoch-via-ferrata_vc20895"
  ]
 }
];

export const ferrataById = (id) => FERRATAS.find((f) => f.id === id);
export const ferrataRegions = [...new Set(FERRATAS.map((f) => f.region).filter(Boolean))];
