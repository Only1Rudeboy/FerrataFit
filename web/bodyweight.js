/**
 * Übungen für unterwegs — alles mit dem eigenen Körpergewicht.
 *
 * Gedacht für Reisen, Urlaub oder jeden Tag, an dem das Gerät nicht erreichbar ist.
 * Die Etappe zählt dabei ganz normal: Höhenmeter, Serie und Abzeichen laufen weiter,
 * denn eine Einheit ohne Gerät ist eine Einheit.
 *
 * Wichtig für den Fortschritt: Jede Übung führt ihre eigene Geschichte. Machst du
 * unterwegs Liegestütze statt Brustpresse, wird die Liegestütz-Reihe fortgeschrieben —
 * deine Brustpresse-Lasten bleiben unberührt und stehen zu Hause unverändert bereit.
 * Es geht also auf keiner Seite Fortschritt verloren.
 *
 * Zur Auswahl: Alles hier braucht höchstens einen Stuhl, eine Tischkante, eine Wand
 * oder eine Türzarge. Nichts davon muss man einpacken.
 */

import { KIND } from './exercises.js';

export const BODYWEIGHT_EXERCISES = [

  // ======================= Zug & Griff =======================
  {
    id: 'bw_inverted_row', name: 'Umgekehrtes Rudern', station: 'BODYWEIGHT',
    muscles: ['Rücken', 'Bizeps', 'Griffkraft'],
    sets: 4, repMin: 8, repMax: 15, restSec: 105,
    progression: KIND.REPS, increment: 2.5, ferrataFocus: 3,
    cue: 'Unter einen stabilen Tisch legen, an der Kante hochziehen. Körper bleibt eine Linie.',
    why: 'Der beste Zug-Ersatz ohne Gerät. Trifft dieselbe Muskulatur wie Latzug und Rudern — '
      + 'und die brauchst du am Steig an jeder senkrechten Passage.',
    setup: 'Ein stabiler Tisch, ein Geländer oder eine tief eingehängte Stange. Du legst dich '
      + 'darunter und greifst die Kante etwas weiter als schulterbreit. Je waagrechter dein '
      + 'Körper liegt, desto schwerer wird es.',
    steps: [
      'Unter die Kante legen, Griff etwas weiter als schulterbreit.',
      'Körper anspannen: Fersen, Gesäß und Schultern bilden eine gerade Linie.',
      'Schulterblätter zusammenziehen, dann die Brust zur Kante ziehen.',
      'Oben kurz halten.',
      'Langsam ablassen, bis die Arme fast gestreckt sind.',
    ],
    mistakes: [
      'Die Hüfte durchhängen lassen. Der Körper bleibt ein Brett.',
      'Nur mit den Armen ziehen — erst kommen die Schulterblätter.',
      'Zu hoch greifen und dadurch fast senkrecht stehen. Dann macht die Übung kaum etwas.',
    ],
    variant: 'Zu schwer? Knie anwinkeln und die Füße näher heranstellen. Zu leicht? Füße auf '
      + 'einen zweiten Stuhl legen, sodass der Körper waagrecht liegt.',
    video: 'Umgekehrtes Rudern Inverted Row Tisch Technik',
  },
  {
    id: 'bw_towel_row', name: 'Türrudern mit Handtuch', station: 'BODYWEIGHT',
    muscles: ['Rücken', 'Bizeps', 'Griffkraft'],
    sets: 3, repMin: 10, repMax: 16, restSec: 90,
    progression: KIND.REPS, increment: 2.5, ferrataFocus: 2,
    cue: 'Handtuch um die Türklinke, zurücklehnen und hochziehen.',
    why: 'Funktioniert in jedem Hotelzimmer. Der Handtuchgriff fordert die Griffkraft sogar '
      + 'stärker als eine Stange — genau das, was am Drahtseil zählt.',
    setup: 'Ein kräftiges Handtuch um beide Türklinken einer geschlossenen Tür schlingen, oder '
      + 'um einen stabilen Pfosten. Die Tür muss zu sein und darf nicht aufgehen können — '
      + 'stell dich auf der Scharnierseite hin.',
    steps: [
      'Handtuchenden fest greifen, Füße nah an die Tür.',
      'Mit gestreckten Armen zurücklehnen, bis die Arme lang sind.',
      'Körper anspannen und sich zur Tür ziehen, Ellbogen eng am Körper.',
      'Kurz halten, dann langsam zurücklehnen.',
    ],
    mistakes: [
      'Eine Tür nehmen, die nachgeben kann. Immer die Scharnierseite wählen.',
      'Mit dem Rücken einrollen statt aufrecht zu bleiben.',
    ],
    variant: 'Schwerer wird es, je weiter du die Füße nach vorne stellst und je tiefer du dich lehnst.',
    video: 'Türrudern Handtuch Rücken Übung ohne Geräte',
  },
  {
    id: 'bw_superman', name: 'Superman', station: 'BODYWEIGHT',
    muscles: ['Rücken', 'Gesäß'],
    sets: 3, repMin: 10, repMax: 16, restSec: 60,
    progression: KIND.REPS, increment: 2.5, ferrataFocus: 1,
    cue: 'Bauchlage, Arme und Beine gleichzeitig anheben, kurz halten.',
    why: 'Kräftigt die Rückenstrecker, die dich mit Rucksack aufrecht halten.',
    setup: 'Bauchlage auf einer Matte oder dem Teppich, Arme nach vorne gestreckt.',
    steps: [
      'Flach auf den Bauch legen, Arme lang nach vorne, Beine gestreckt.',
      'Arme, Brust und Beine gleichzeitig vom Boden abheben.',
      'Oben ein bis zwei Sekunden halten, Blick bleibt zum Boden.',
      'Langsam ablegen.',
    ],
    mistakes: [
      'Den Kopf in den Nacken werfen. Der Nacken bleibt in Verlängerung der Wirbelsäule.',
      'Ruckartig hochschnellen.',
    ],
    video: 'Superman Übung Rückenstrecker Ausführung',
  },

  // ======================= Beine =======================
  {
    id: 'bw_squat', name: 'Kniebeuge', station: 'BODYWEIGHT',
    muscles: ['Oberschenkel', 'Gesäß'],
    sets: 4, repMin: 12, repMax: 25, restSec: 90,
    progression: KIND.REPS, increment: 5, ferrataFocus: 2,
    cue: 'Füße hüftbreit, Gesäß nach hinten unten, Knie folgen den Fußspitzen.',
    why: 'Der Grundstock für alles, was mit Höhenmetern zu tun hat.',
    setup: 'Freier Stand, Füße etwa hüft- bis schulterbreit, Fußspitzen leicht nach außen.',
    steps: [
      'Aufrecht stehen, Arme zum Ausbalancieren nach vorne nehmen.',
      'Gesäß nach hinten schieben und gleichzeitig in die Knie gehen — als wolltest du dich setzen.',
      'So tief, bis die Oberschenkel mindestens waagrecht sind. Fersen bleiben am Boden.',
      'Über die Fersen wieder hochdrücken, oben Gesäß anspannen.',
    ],
    mistakes: [
      'Die Fersen abheben. Dann sitzt du zu weit vorne — Gesäß mehr nach hinten schieben.',
      'Die Knie nach innen kippen lassen.',
      'Nur halb tief gehen.',
    ],
    variant: 'Zu leicht? Langsamer absenken (drei Sekunden) oder auf einem Bein an einer Wand abstützen.',
    video: 'Kniebeuge Körpergewicht richtige Technik',
  },
  {
    id: 'bw_hip_thrust', name: 'Beckenheben einbeinig', station: 'BODYWEIGHT',
    muscles: ['Oberschenkel hinten', 'Gesäß'],
    sets: 3, repMin: 10, repMax: 16, restSec: 90,
    progression: KIND.REPS, increment: 5, ferrataFocus: 2,
    cue: 'Rückenlage, ein Fuß aufgestellt, Becken hochdrücken. Pro Seite.',
    why: 'Ersatz für den Beinbeuger. Hält die Rückseite im Gleichgewicht zur Vorderseite — '
      + 'das schützt Knie und Rücken bei steilen Passagen.',
    setup: 'Rückenlage, ein Fuß etwa 30 cm vom Gesäß aufgestellt, das andere Bein gestreckt '
      + 'in die Luft oder angewinkelt abgelegt.',
    steps: [
      'Auf den Rücken legen, einen Fuß aufstellen, Arme seitlich am Boden.',
      'Ferse in den Boden drücken und das Becken anheben, bis Oberschenkel und Rumpf eine Linie bilden.',
      'Oben das Gesäß fest anspannen und eine Sekunde halten.',
      'Langsam ablassen, ohne das Becken ganz abzulegen.',
      'Alle Wiederholungen auf einer Seite, dann wechseln.',
    ],
    mistakes: [
      'Ins Hohlkreuz drücken statt das Gesäß anzuspannen.',
      'Über die Fußspitze drücken. Der Druck kommt von der Ferse.',
    ],
    counting: 'Zähl pro Seite. 12 heißt 12 links und 12 rechts.',
    video: 'Einbeiniges Beckenheben Hip Thrust Ausführung',
  },
  {
    id: 'bw_wall_sit', name: 'Wandsitz', station: 'BODYWEIGHT',
    muscles: ['Oberschenkel vorne'],
    sets: 3, repMin: 1, repMax: 1, restSec: 75,
    progression: KIND.TIME, increment: 10, ferrataFocus: 2,
    cue: 'Mit dem Rücken an die Wand, als säßest du auf einem Stuhl. Zeit halten.',
    why: 'Genau die Belastung, die beim langen Abstieg auf den Oberschenkel wirkt — '
      + 'dort brennt es am Steig zuerst.',
    setup: 'Rücken flach an eine Wand, Füße etwa 50 cm davor, hüftbreit.',
    steps: [
      'An der Wand nach unten rutschen, bis die Oberschenkel waagrecht sind.',
      'Knie stehen senkrecht über den Fersen, nicht davor.',
      'Rücken bleibt flach an der Wand, Hände locker oder vor der Brust.',
      'Ruhig weiteratmen und die Zeit halten.',
    ],
    mistakes: [
      'Sich mit den Händen auf den Oberschenkeln abstützen.',
      'Nicht tief genug — die Oberschenkel sollen waagrecht sein.',
    ],
    video: 'Wandsitz Wall Sit Übung Ausführung',
  },

  // ======================= Druck & Schulter =======================
  {
    id: 'bw_pike_pushup', name: 'Pike-Liegestütz', station: 'BODYWEIGHT',
    muscles: ['Schultern', 'Trizeps'],
    sets: 3, repMin: 6, repMax: 14, restSec: 105,
    progression: KIND.REPS, increment: 2.5, ferrataFocus: 2,
    cue: 'Umgekehrtes V, Kopf Richtung Boden absenken.',
    why: 'Ersatz fürs Schulterdrücken. Stabile Schultern tragen den Rucksack und halten dich '
      + 'über Kopf an hohen Klammern sicher.',
    setup: 'Liegestützposition, dann das Gesäß hoch schieben, bis der Körper ein umgekehrtes V '
      + 'bildet. Hände etwas weiter als schulterbreit.',
    steps: [
      'Ins umgekehrte V gehen, Beine so gestreckt wie es die Beweglichkeit zulässt.',
      'Ellbogen beugen und den Scheitel Richtung Boden zwischen die Hände senken.',
      'Kurz vor dem Boden umkehren und wieder hochdrücken.',
      'Die Hüfte bleibt die ganze Zeit oben.',
    ],
    mistakes: [
      'Die Hüfte absinken lassen — dann wird daraus ein normaler Liegestütz.',
      'Den Kopf nach vorne statt nach unten führen.',
    ],
    variant: 'Zu schwer? Hände auf eine Erhöhung. Zu leicht? Füße auf einen Stuhl.',
    video: 'Pike Push Up Schulter Liegestütz Technik',
  },
  {
    id: 'bw_dips_chair', name: 'Dips am Stuhl', station: 'BODYWEIGHT',
    muscles: ['Trizeps', 'Brust', 'Schultern'],
    sets: 3, repMin: 8, repMax: 16, restSec: 90,
    progression: KIND.REPS, increment: 2.5, ferrataFocus: 1,
    cue: 'Hände auf der Stuhlkante hinter dir, absenken und hochdrücken.',
    why: 'Ersatz fürs Trizepsdrücken. Stabilisiert den Ellbogen beim Abstützen am Fels.',
    setup: 'Ein stabiler Stuhl oder eine Bettkante im Rücken. Hände schulterbreit auf die Kante, '
      + 'Finger zeigen nach vorne. Füße nach vorne ausgestreckt.',
    steps: [
      'Gesäß von der Kante lösen, Gewicht auf den Händen.',
      'Ellbogen nach hinten beugen und den Körper absenken, bis der Oberarm etwa waagrecht ist.',
      'Über die Handflächen wieder hochdrücken.',
      'Der Rücken bleibt dicht an der Kante.',
    ],
    mistakes: [
      'Die Ellbogen nach außen abspreizen. Sie zeigen nach hinten.',
      'Zu tief gehen, bis es vorne in der Schulter zieht.',
      'Sich vom Stuhl wegbewegen — dann kippt die Belastung ins Gelenk.',
    ],
    variant: 'Zu schwer? Knie anwinkeln und die Füße näher heranstellen.',
    video: 'Dips am Stuhl Trizeps Übung Technik',
  },
  {
    id: 'bw_pushup_wide', name: 'Breiter Liegestütz', station: 'BODYWEIGHT',
    muscles: ['Brust', 'Schultern'],
    sets: 3, repMin: 8, repMax: 20, restSec: 90,
    progression: KIND.REPS, increment: 2.5, ferrataFocus: 1,
    cue: 'Hände deutlich weiter als schulterbreit, Brust tief zum Boden.',
    why: 'Ersatz für Butterfly und Brustpresse — hält die Schultern im Gleichgewicht zum vielen Ziehen.',
    setup: 'Liegestützposition, Hände etwa anderthalb Schulterbreiten auseinander.',
    steps: [
      'Körper anspannen, Linie von den Fersen bis zum Kopf.',
      'Absenken, bis die Brust knapp über dem Boden ist.',
      'Kurz halten, dann gleichmäßig hochdrücken.',
    ],
    mistakes: [
      'Die Hüfte durchhängen lassen.',
      'Nur halb absenken.',
    ],
    variant: 'Zu schwer? Hände auf eine Erhöhung — Tischkante oder Fensterbank.',
    video: 'Breiter Liegestütz Brust Ausführung',
  },
  {
    id: 'bw_towel_curl', name: 'Handtuch-Curl', station: 'BODYWEIGHT',
    muscles: ['Bizeps', 'Unterarme'],
    sets: 3, repMin: 1, repMax: 1, restSec: 75,
    progression: KIND.TIME, increment: 5, ferrataFocus: 2,
    cue: 'Handtuch unter den Fuß, dagegen ziehen und die Spannung halten.',
    why: 'Bizeps und Griffkraft ohne jedes Gerät. Der feste Griff ins Handtuch trainiert '
      + 'genau das, was am Drahtseil ermüdet.',
    setup: 'Ein Handtuch mittig unter einen Fuß klemmen, beide Enden greifen. Aufrecht stehen, '
      + 'Ellbogen am Körper.',
    steps: [
      'Handtuchenden fest greifen, Arme etwa im rechten Winkel.',
      'Mit den Armen nach oben ziehen und gleichzeitig mit dem Fuß dagegenhalten.',
      'Die Spannung aufbauen, bis es deutlich zieht, und halten.',
      'Ruhig weiteratmen, nicht die Luft anhalten.',
    ],
    mistakes: [
      'Nur locker ziehen. Der Reiz kommt aus der vollen Anspannung.',
      'Ins Hohlkreuz gehen.',
    ],
    video: 'Handtuch Curl isometrisch Bizeps ohne Geräte',
  },
  {
    id: 'bw_leg_raise', name: 'Beinheben im Liegen', station: 'BODYWEIGHT',
    muscles: ['Rumpf'],
    sets: 3, repMin: 10, repMax: 18, restSec: 75,
    progression: KIND.REPS, increment: 2.5, ferrataFocus: 2,
    cue: 'Rückenlage, Beine gestreckt anheben und langsam absenken, ohne abzulegen.',
    why: 'Ersatz fürs hängende Knieheben. Rumpfspannung hält dich nah an der Wand — '
      + 'wer durchhängt, hängt in den Armen.',
    setup: 'Rückenlage auf einer Matte, Hände flach unter dem Gesäß oder seitlich am Boden. '
      + 'Die Hände unter dem Gesäß nehmen Druck von der Lende.',
    steps: [
      'Flach auf den Rücken legen, Beine gestreckt, Lende flach an den Boden drücken.',
      'Beide Beine gestreckt anheben, bis sie etwa senkrecht stehen.',
      'Langsam absenken — zwei bis drei Sekunden — bis die Fersen knapp über dem Boden sind.',
      'Nicht ablegen, sondern direkt die nächste Wiederholung anschließen.',
    ],
    mistakes: [
      'Die Lende vom Boden abheben lassen. Sobald ein Hohlkreuz entsteht, ist es zu schwer — '
        + 'dann die Knie leicht beugen.',
      'Die Beine fallen lassen statt kontrolliert abzusenken.',
    ],
    variant: 'Zu schwer? Knie anwinkeln. Zu leicht? Unten kurz halten, bevor du wieder hochgehst.',
    video: 'Beinheben liegend Bauch Übung Technik',
  },
];

/**
 * Was ersetzt was, wenn kein Gerät da ist.
 *
 * Der Ersatz trifft dieselbe Muskulatur; er ist keine exakte Kopie, sondern die beste
 * Annäherung mit dem eigenen Körpergewicht. Auch die Klimmzugstange gilt als Gerät —
 * unterwegs ist sie selten vorhanden.
 */
export const BODYWEIGHT_SUBSTITUTES = {
  // Zug
  pullup: 'bw_inverted_row',
  latpull_wide: 'bw_inverted_row',
  row_cable: 'bw_inverted_row',
  row_lat_narrow: 'bw_towel_row',
  curl: 'bw_towel_curl',
  curl_bw: 'bw_towel_curl',
  deadhang: 'bw_towel_curl',
  hang_knee_raise: 'bw_leg_raise',
  // Beine
  leg_ext: 'bw_squat',
  leg_curl: 'bw_hip_thrust',
  // Druck
  chest_press: 'bw_pushup_wide',
  butterfly: 'bw_pushup_wide',
  shoulder_press: 'bw_pike_pushup',
  triceps: 'bw_dips_chair',
  reverse_fly: 'bw_superman',
  farmer_hold: 'bw_towel_curl',
};

export const bodyweightById = (id) => BODYWEIGHT_EXERCISES.find((e) => e.id === id);
