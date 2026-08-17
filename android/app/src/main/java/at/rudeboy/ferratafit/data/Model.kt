package at.rudeboy.ferratafit.data

import kotlinx.serialization.Serializable

/** Muskelgruppen, nach denen der Split aufgeteilt wird. */
enum class Muscle(val label: String) {
    CHEST("Brust"),
    BACK("Rücken"),
    SHOULDERS("Schultern"),
    BICEPS("Bizeps"),
    TRICEPS("Trizeps"),
    QUADS("Oberschenkel vorne"),
    HAMSTRINGS("Oberschenkel hinten"),
    GLUTES("Gesäß"),
    CALVES("Waden"),
    CORE("Rumpf"),
    GRIP("Griffkraft"),
    FOREARMS("Unterarme")
}

/**
 * Stationen am Multifunktionsgerät. Beim Onboarding hakt man ab, was vorhanden ist —
 * der Plan filtert dann automatisch die passenden Übungen.
 */
enum class Station(val label: String, val hint: String) {
    LAT_PULLDOWN("Latzug", "Zugstange oben am Turm"),
    CHEST_PRESS("Brustpresse", "Drückhebel sitzend"),
    BUTTERFLY("Butterfly", "Schwingarme für die Brust"),
    CABLE_LOW("Unterer Kabelzug", "Zugpunkt unten, für Rudern und Curls"),
    LEG_EXTENSION("Beinstrecker", "Beinhebel sitzend strecken"),
    LEG_CURL("Beinbeuger", "Beinhebel beugen"),
    PULLUP_BAR("Klimmzugstange", "Freie Stange zum Hängen"),
    BODYWEIGHT("Körpergewicht", "Immer verfügbar"),
    DUMBBELL("Freie Hanteln", "Kurzhanteln oder Langhantel")
}

/** Wie eine Übung gesteigert wird. */
enum class ProgressionKind {
    /** Klassisch: Last in kg erhöhen (Steckgewicht, Hantel). */
    WEIGHT,

    /** Körpergewichtsübung: erst Wiederholungen, dann Zusatzlast. */
    REPS,

    /** Haltezeit in Sekunden (Dead Hang, Plank). */
    TIME
}

/**
 * Eine Übung im Katalog.
 *
 * [ferrataFocus] 0–3 gewichtet, wie stark die Übung auf den Klettersteig einzahlt.
 * Der Plan sortiert danach, damit die klettersteigrelevanten Übungen früh in der
 * Einheit liegen — dann sind sie noch frisch trainierbar.
 */
@Serializable
data class Exercise(
    val id: String,
    val name: String,
    val station: Station,
    val muscles: List<Muscle>,
    val sets: Int,
    val repMin: Int,
    val repMax: Int,
    val restSec: Int,
    val progression: ProgressionKind,
    /** kg-Schritt bei WEIGHT/REPS, Sekunden-Schritt bei TIME. */
    val increment: Double,
    val ferrataFocus: Int,
    /** Kurzfassung für die enge Anzeige. */
    val cue: String,
    val why: String,
    /** Was du brauchst und wie du dich hinstellst. */
    val setup: String = "",
    /** Der Bewegungsablauf, Schritt für Schritt. */
    val steps: List<String> = emptyList(),
    /** Was typischerweise schiefgeht. */
    val mistakes: List<String> = emptyList(),
    /** Wie Wiederholungen gezählt werden — nur wo es nicht offensichtlich ist. */
    val counting: String = "",
    /** Leichtere oder schwerere Ausführung. */
    val variant: String = "",
    /**
     * Suchbegriff für YouTube. Bewusst eine Suche statt einer festen Video-Kennung:
     * einzelne Videos verschwinden, eine Suche liefert immer aktuelle Treffer.
     */
    val video: String = ""
)

/** Ein geloggter Satz. */
@Serializable
data class SetLog(
    val exerciseId: String,
    val setIndex: Int,
    val weightKg: Double = 0.0,
    val reps: Int = 0,
    val seconds: Int = 0,
    /** Reps in Reserve: wie viele Wiederholungen wären noch gegangen. */
    val rir: Int? = null
)

/** Eine abgeschlossene Trainingseinheit. */
@Serializable
data class Session(
    val id: String,
    val dayId: String,
    val startedAt: Long,
    val finishedAt: Long,
    val sets: List<SetLog>,
    val note: String = ""
) {
    val durationMin: Int get() = ((finishedAt - startedAt) / 60_000L).toInt().coerceAtLeast(0)

    /** Tonnage = Summe aus Last × Wiederholungen. Das Maß für „wie viel Arbeit war das“. */
    val volumeKg: Double get() = sets.sumOf { it.weightKg * it.reps }
}

/** Nutzerprofil und Ziel. */
@Serializable
data class Profile(
    val stations: Set<Station> = setOf(
        Station.LAT_PULLDOWN, Station.CHEST_PRESS, Station.BUTTERFLY,
        Station.LEG_EXTENSION, Station.LEG_CURL, Station.PULLUP_BAR, Station.BODYWEIGHT
    ),
    val daysPerWeek: Int = 3,
    val bodyweightKg: Double = 78.0,
    /** Startdatum des laufenden Trainingszyklus (Millis). Bestimmt die Wochennummer. */
    val cycleStart: Long = 0L,
    /** Optional: Datum des geplanten Klettersteigs — die App rechnet rückwärts. */
    val targetFerrataDate: Long? = null,
    val targetFerrataName: String = "",
    /** Gewichtsstufe des Steckgewichts in kg (typisch 5,0 an Kraftstationen). */
    val plateStepKg: Double = 5.0,
    val onboarded: Boolean = false,
    val healthConnectEnabled: Boolean = false,
    /** Körpergewicht automatisch von der Waage übernehmen. */
    val autoWeightFromScale: Boolean = true,
    /**
     * Unterwegs-Modus: Der Plan weicht auf Körpergewichtsübungen aus, weil kein Gerät
     * erreichbar ist. Die Etappe zählt trotzdem voll.
     */
    val travelMode: Boolean = false
)

/** Ein Trainingstag im Split. */
@Serializable
data class TrainingDay(
    val id: String,
    val title: String,
    val subtitle: String,
    val focus: List<Muscle>,
    val exerciseIds: List<String>
)

/** Kompletter App-Zustand, so wie er auf die Platte geht. */
@Serializable
data class AppState(
    val profile: Profile = Profile(),
    val sessions: List<Session> = emptyList(),
    /** Zuletzt vorgeschlagene bzw. zuletzt verwendete Last je Übung. */
    val lastLoads: Map<String, Double> = emptyMap(),
    /** Übungen, die der Nutzer aus dem Plan geworfen hat. */
    val hiddenExercises: Set<String> = emptySet(),
    /** Abgeschlossene Etappen in der Reihenfolge, in der sie gegangen wurden. */
    val progress: List<StageLog> = emptyList(),
    /** Bereits vergebene Abzeichen — gemerkt, damit neue erkennbar bleiben. */
    val seenBadges: Set<String> = emptySet(),
    /** Messungen von der Waage, über Health Connect eingelesen. */
    val body: List<BodyMeasurement> = emptyList(),
    /** Körpergröße in Zentimetern, für den BMI. */
    val heightCm: Double? = null,
    /** Wann zuletzt mit der Waage abgeglichen wurde. */
    val bodySyncedAt: Long = 0L
)
