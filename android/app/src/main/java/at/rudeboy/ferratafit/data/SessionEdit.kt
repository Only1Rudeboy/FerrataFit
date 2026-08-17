package at.rudeboy.ferratafit.data

/**
 * Rechenschritte für das Nachbearbeiten von Einheiten.
 *
 * Bewusst als reine Funktionen ohne Zustand — so lässt sich prüfen, dass eine Korrektur
 * die Progressionsdaten sauber hinterlässt, ohne die halbe App hochzufahren.
 */
object SessionEdit {

    /**
     * Nummeriert die Sätze je Übung neu durch.
     *
     * [SetLog] hat keine eigene Kennung — ein Satz ist nur über Übung und Nummer bestimmt.
     * Bliebe nach dem Löschen eine Lücke, stünde in der Anzeige „Satz 1, Satz 3", und die
     * Zuordnung liefe bei der nächsten Bearbeitung auseinander.
     */
    fun renumber(sets: List<SetLog>): List<SetLog> {
        val counter = mutableMapOf<String, Int>()
        return sets.map { set ->
            val n = counter.getOrDefault(set.exerciseId, 0)
            counter[set.exerciseId] = n + 1
            set.copy(setIndex = n)
        }
    }

    /**
     * Die zuletzt verwendete Last je Übung, frisch aus allen Einheiten gerechnet.
     *
     * Muss nach jeder Korrektur neu bestimmt werden, sonst steht in der gespeicherten
     * Datei ein Wert, den es in keiner Einheit mehr gibt.
     */
    fun recomputeLastLoads(sessions: List<Session>): Map<String, Double> =
        sessions.sortedBy { it.startedAt }
            .flatMap { it.sets }
            .groupBy { it.exerciseId }
            .mapValues { (_, sets) -> sets.maxOf { it.weightKg } }

    /**
     * Ersetzt eine Einheit in der Liste. Die Reihenfolge bleibt erhalten, weil die
     * Progression nach Startzeit sortiert und ein Umsortieren die 2-für-2-Regel
     * durcheinanderbringen würde.
     */
    fun replace(sessions: List<Session>, edited: Session): List<Session> =
        sessions.map { if (it.id == edited.id) edited else it }

    /** Entfernt eine Einheit. Der Etappen-Fortschritt bleibt davon unberührt. */
    fun remove(sessions: List<Session>, id: String): List<Session> =
        sessions.filterNot { it.id == id }

    /**
     * Ist die Bearbeitung noch sinnvoll speicherbar?
     * Eine Einheit ohne Sätze wäre ein leerer Eintrag in der Statistik.
     */
    fun isSaveable(session: Session): Boolean = session.sets.isNotEmpty()
}
