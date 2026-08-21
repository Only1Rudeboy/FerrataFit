package at.rudeboy.ferratafit.data

/**
 * Ein frei lizenziertes Foto von Wikimedia Commons.
 *
 * Die App bündelt keine fremden Bilder — sie lädt sie bei Bedarf von Commons, wie ein
 * Browser es täte, und nennt Urheber und Lizenz dazu. Das ist die Bedingung dieser
 * Lizenzen, und es ist der einzige Weg, fremde Fotos in einer öffentlichen App zu
 * zeigen, ohne jemandes Rechte zu verletzen.
 *
 * Welche Bilder in Frage kommen, hat eine Recherche ermittelt; ob Lizenz und Datei
 * tatsächlich stimmen, prüft der Generator über die Commons-API nach, bevor ein Eintrag
 * hier landet. Erlaubt sind nur CC0, CC BY, CC BY-SA, Public Domain und FAL.
 */
data class WebPhoto(
    val file: String,
    /** Verkleinerte Fassung (Breite 1200), direkt von upload.wikimedia.org. */
    val url: String,
    /** Die Dateiseite auf Commons — für die Namensnennung. */
    val pageUrl: String,
    val shows: String,
    val author: String,
    val license: String
)

/** Ein Abschnitt der schematischen Topo, vom Einstieg zum Ausstieg. */
data class TopoSegment(
    val label: String,
    val grade: String,
    /** wall, traverse, ladder, bridge, ridge, gully, cave, overhang, walk, exit */
    val kind: String,
    val meters: Int = 0,
    val crux: Boolean = false
) {
    val gradeEnum: FerrataGrade get() = FerrataGrade.parse(grade) ?: FerrataGrade.B

    val icon: String get() = when (kind) {
        "ladder" -> "🪜"
        "bridge" -> "🌉"
        "ridge" -> "⛰"
        "gully" -> "🏔"
        "cave" -> "🕳"
        "overhang" -> "🧗"
        "walk" -> "🥾"
        "exit" -> "🚪"
        "traverse" -> "↔"
        else -> "▲"
    }
}
