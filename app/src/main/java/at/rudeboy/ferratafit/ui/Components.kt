package at.rudeboy.ferratafit.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import at.rudeboy.ferratafit.data.TrendPoint

/** Die Standardkarte der App: abgerundet, leicht abgesetzt, dezent umrandet. */
@Composable
fun FfCard(
    modifier: Modifier = Modifier,
    accent: Color? = null,
    padding: Dp = 18.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(22.dp)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, accent?.copy(alpha = 0.35f) ?: MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), shape)
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .padding(padding),
        content = content
    )
}

/** Überschrift über einem Abschnitt. */
@Composable
fun SectionTitle(text: String, trailing: String? = null, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = Palette.TextLow,
            fontWeight = FontWeight.Bold
        )
        if (trailing != null) {
            Text(trailing, style = MaterialTheme.typography.labelSmall, color = Palette.TextLow)
        }
    }
}

/** Kennzahl-Kachel für die Übersicht. */
@Composable
fun StatTile(
    value: String,
    label: String,
    icon: ImageVector? = null,
    tint: Color = Palette.Sky,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(18.dp))
            .padding(14.dp)
    ) {
        if (icon != null) {
            Icon(icon, null, tint = tint, modifier = Modifier.size(18.dp))
            Spacer(Modifier.height(8.dp))
        }
        Text(value, style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onSurface)
        Text(label, style = MaterialTheme.typography.bodySmall, color = Palette.TextMid)
    }
}

/**
 * Fortschrittsring für die Klettersteig-Bereitschaft.
 * Der Wert wird animiert, damit man beim Öffnen sieht, dass sich etwas bewegt hat.
 */
@Composable
fun ProgressRing(
    progress: Float,
    size: Dp = 132.dp,
    stroke: Dp = 12.dp,
    brush: Brush = Gradients.sky,
    trackColor: Color = Palette.Outline,
    center: @Composable () -> Unit
) {
    val animated by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(900),
        label = "ring"
    )
    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(size)) {
        Canvas(Modifier.fillMaxSize()) {
            val s = stroke.toPx()
            val inset = s / 2
            val arcSize = Size(this.size.width - s, this.size.height - s)
            drawArc(
                color = trackColor,
                startAngle = 135f,
                sweepAngle = 270f,
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = arcSize,
                style = Stroke(width = s, cap = StrokeCap.Round)
            )
            drawArc(
                brush = brush,
                startAngle = 135f,
                sweepAngle = 270f * animated,
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = arcSize,
                style = Stroke(width = s, cap = StrokeCap.Round)
            )
        }
        center()
    }
}

/** Farbig hinterlegtes Kürzel, z. B. „+5 kg“ oder „Deload“. */
@Composable
fun Pill(
    text: String,
    color: Color,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null
) {
    Row(
        modifier = modifier
            .clip(CircleShape)
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Icon(icon, null, tint = color, modifier = Modifier.size(13.dp))
            Spacer(Modifier.width(5.dp))
        }
        Text(text, style = MaterialTheme.typography.labelSmall, color = color, fontWeight = FontWeight.Bold)
    }
}

/** Waagrechter Balken, z. B. für die Wochenauslastung. */
@Composable
fun BarMeter(
    progress: Float,
    modifier: Modifier = Modifier,
    height: Dp = 8.dp,
    brush: Brush = Gradients.sky
) {
    val animated by animateFloatAsState(progress.coerceIn(0f, 1f), tween(600), label = "bar")
    Box(
        modifier
            .fillMaxWidth()
            .height(height)
            .clip(CircleShape)
            .background(Palette.Outline.copy(alpha = 0.6f))
    ) {
        Box(
            Modifier
                .fillMaxWidth(animated)
                .height(height)
                .clip(CircleShape)
                .background(brush)
        )
    }
}

/**
 * Verlaufsdiagramm einer Übung.
 * Bewusst reduziert: Linie plus Punkte, keine Achsen — auf dem Handy zählt der Trend,
 * die genauen Zahlen stehen ohnehin daneben.
 */
@Composable
fun TrendChart(
    points: List<TrendPoint>,
    modifier: Modifier = Modifier,
    lineColor: Color = Palette.Sky
) {
    if (points.size < 2) {
        Box(modifier.fillMaxWidth().height(120.dp), contentAlignment = Alignment.Center) {
            Text(
                "Noch zu wenig Daten — ab der zweiten Einheit erscheint hier die Kurve.",
                style = MaterialTheme.typography.bodySmall,
                color = Palette.TextLow
            )
        }
        return
    }
    val values = points.map { it.value.toFloat() }
    val minV = values.min()
    val maxV = values.max()
    val span = (maxV - minV).takeIf { it > 0.01f } ?: 1f

    Canvas(modifier.fillMaxWidth().height(120.dp)) {
        val w = size.width
        val h = size.height
        val padV = 14f
        val stepX = if (points.size > 1) w / (points.size - 1) else w

        fun yFor(v: Float): Float = h - padV - ((v - minV) / span) * (h - 2 * padV)

        // Fläche unter der Kurve andeuten
        val fill = androidx.compose.ui.graphics.Path().apply {
            moveTo(0f, h)
            values.forEachIndexed { i, v -> lineTo(i * stepX, yFor(v)) }
            lineTo(w, h)
            close()
        }
        drawPath(
            fill,
            Brush.verticalGradient(listOf(lineColor.copy(alpha = 0.28f), Color.Transparent))
        )

        val path = androidx.compose.ui.graphics.Path().apply {
            values.forEachIndexed { i, v ->
                if (i == 0) moveTo(0f, yFor(v)) else lineTo(i * stepX, yFor(v))
            }
        }
        drawPath(path, lineColor, style = Stroke(width = 3.5f, cap = StrokeCap.Round))

        values.forEachIndexed { i, v ->
            drawCircle(lineColor, radius = 5f, center = Offset(i * stepX, yFor(v)))
            drawCircle(Palette.Ink, radius = 2.2f, center = Offset(i * stepX, yFor(v)))
        }
    }
}

/** Leerer Zustand mit Hinweis statt einer leeren Fläche. */
@Composable
fun EmptyHint(text: String, modifier: Modifier = Modifier) {
    Box(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.6f))
            .padding(22.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text,
            style = MaterialTheme.typography.bodyMedium,
            color = Palette.TextMid,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}
