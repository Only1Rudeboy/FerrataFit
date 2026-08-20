package at.rudeboy.ferratafit.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.sp
import at.rudeboy.ferratafit.data.Fit
import at.rudeboy.ferratafit.data.GeoPoint
import at.rudeboy.ferratafit.data.FerrataGeo
import kotlin.math.cos
import kotlin.math.hypot

/**
 * Die Karte der Vorarlberger Klettersteige.
 *
 * Bewusst selbst gezeichnet statt einer Kartenbibliothek: Die App lädt nichts nach —
 * keine Kacheln, keine Fremdanbieter, funktioniert am Berg ohne Netz. Für „wo liegt
 * was, und was passt zu mir" reicht die Silhouette des Landes mit Punkten völlig;
 * für den Zustieg braucht man ohnehin eine echte Wanderkarte.
 *
 * Die Punkte tragen die Passungsfarbe aus der Missionsübersicht — die Karte
 * beantwortet damit dieselbe Frage wie die Liste, nur räumlich: Was in meiner
 * Nähe passt zu meinem Stand?
 */
@Composable
fun FerrataMap(
    fits: Map<String, Fit>,
    selectedId: String?,
    onSelect: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()

    // Kartengrenzen mit etwas Rand
    val minLat = FerrataGeo.MIN_LAT
    val maxLat = FerrataGeo.MAX_LAT
    val minLon = FerrataGeo.MIN_LON
    val maxLon = FerrataGeo.MAX_LON

    // Breitengrade sind überall gleich hoch, Längengrade werden zum Pol hin schmaler.
    // Ohne diesen Faktor wäre das Land um ein Drittel in die Breite gezogen.
    val lonScale = cos(Math.toRadians((minLat + maxLat) / 2)).toFloat()
    val aspect = ((maxLon - minLon) * lonScale / (maxLat - minLat)).toFloat()

    fun project(lat: Double, lon: Double, w: Float, h: Float): Offset = Offset(
        x = ((lon - minLon) / (maxLon - minLon)).toFloat() * w,
        y = (1f - ((lat - minLat) / (maxLat - minLat)).toFloat()) * h
    )

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(aspect)
            .pointerInput(fits) {
                detectTapGestures { tap ->
                    // Der nächstgelegene Punkt im Antipp-Radius gewinnt; daneben
                    // getippt wählt ab. Punkte am selben Fels liegen übereinander —
                    // wiederholtes Tippen wechselt reihum durch sie durch.
                    val w = size.width.toFloat()
                    val h = size.height.toFloat()
                    val radius = 40f
                    val hits = FerrataGeo.points
                        .map { it to project(it.lat, it.lon, w, h) }
                        .filter { (_, pos) -> hypot(pos.x - tap.x, pos.y - tap.y) < radius }
                        .sortedBy { (_, pos) -> hypot(pos.x - tap.x, pos.y - tap.y) }
                        .map { it.first }
                    if (hits.isEmpty()) {
                        onSelect(null)
                    } else {
                        val i = hits.indexOfFirst { it.id == selectedId }
                        onSelect(hits[(i + 1) % hits.size].id)
                    }
                }
            }
    ) {
        val w = size.width
        val h = size.height

        // --- Landesumriss ---------------------------------------------------
        val border = Path().apply {
            FerrataGeo.outline.forEachIndexed { i, (lat, lon) ->
                val p = project(lat, lon, w, h)
                if (i == 0) moveTo(p.x, p.y) else lineTo(p.x, p.y)
            }
            close()
        }
        drawPath(border, Palette.SurfaceHigh)
        drawPath(border, Palette.Outline, style = Stroke(width = 2.5f))

        // --- Orientierungsorte ----------------------------------------------
        FerrataGeo.landmarks.forEach { (name, lat, lon) ->
            val p = project(lat, lon, w, h)
            drawCircle(Palette.TextLow, radius = 4f, center = p)
            drawText(
                textMeasurer, name,
                topLeft = Offset(p.x + 9f, p.y - 16f),
                style = TextStyle(color = Palette.TextLow, fontSize = 9.sp)
            )
        }

        // --- Die Steige ------------------------------------------------------
        fun fitColor(fit: Fit?): Color = when (fit) {
            Fit.PASST -> Palette.Emerald
            Fit.KNAPP -> Palette.Amber
            Fit.ZIEL -> Palette.Violet
            else -> Palette.TextLow
        }

        // Erst die grauen, dann die farbigen, zuletzt der gewählte — was zählt, liegt oben.
        val ordered = FerrataGeo.points.sortedBy {
            when {
                it.id == selectedId -> 2
                fits[it.id] != null && fits[it.id] != Fit.ZU_FRUEH -> 1
                else -> 0
            }
        }
        ordered.forEach { pt ->
            val pos = project(pt.lat, pt.lon, w, h)
            val color = fitColor(fits[pt.id])
            val selected = pt.id == selectedId
            if (selected) {
                drawCircle(color.copy(alpha = 0.25f), radius = 22f, center = pos)
                drawCircle(Palette.TextHigh, radius = 9f, center = pos)
            }
            drawCircle(color, radius = if (selected) 7f else 6f, center = pos)
        }
    }
}
