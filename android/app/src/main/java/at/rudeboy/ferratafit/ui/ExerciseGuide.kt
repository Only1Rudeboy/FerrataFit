package at.rudeboy.ferratafit.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Die vollständige Anleitung zu einer Übung — Aufbau, Ablauf, typische Fehler und
 * ein Verweis auf ein Erklärvideo. Wird im Training wie in den Dehn-Etappen genutzt.
 *
 * Alle Felder sind optional; was leer ist, fällt weg.
 */
@Composable
fun ExerciseGuide(
    setup: String = "",
    steps: List<String> = emptyList(),
    cue: String = "",
    mistakes: List<String> = emptyList(),
    counting: String = "",
    variant: String = "",
    why: String = "",
    whyLabel: String = "Warum am Steig",
    video: String = "",
    modifier: Modifier = Modifier
) {
    Column(modifier) {
        if (setup.isNotBlank()) {
            GuidePanel("Aufbau") {
                Text(setup, style = MaterialTheme.typography.bodyMedium, color = Palette.TextMid)
            }
            Spacer(Modifier.height(10.dp))
        }

        if (steps.isNotEmpty()) {
            GuidePanel("Ablauf") {
                steps.forEachIndexed { i, step ->
                    Row(Modifier.padding(bottom = if (i == steps.size - 1) 0.dp else 9.dp)) {
                        Box(
                            Modifier
                                .size(20.dp)
                                .clip(CircleShape)
                                .background(Palette.Sky),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "${i + 1}",
                                style = MaterialTheme.typography.labelSmall,
                                color = Palette.Ink,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(Modifier.width(10.dp))
                        Text(
                            step,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Palette.TextMid,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
            Spacer(Modifier.height(10.dp))
        } else if (cue.isNotBlank()) {
            GuidePanel("So geht's") {
                Text(cue, style = MaterialTheme.typography.bodyMedium, color = Palette.TextMid)
            }
            Spacer(Modifier.height(10.dp))
        }

        if (mistakes.isNotEmpty()) {
            GuidePanel("Achte darauf", accent = Palette.Amber, tint = Palette.Amber.copy(alpha = 0.07f)) {
                mistakes.forEachIndexed { i, m ->
                    Row(Modifier.padding(bottom = if (i == mistakes.size - 1) 0.dp else 7.dp)) {
                        Text("•", color = Palette.Amber, style = MaterialTheme.typography.bodyMedium)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            m,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Palette.TextMid,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
            Spacer(Modifier.height(10.dp))
        }

        if (counting.isNotBlank()) {
            GuidePanel("Zählweise") {
                Text(counting, style = MaterialTheme.typography.bodyMedium, color = Palette.TextMid)
            }
            Spacer(Modifier.height(10.dp))
        }

        if (variant.isNotBlank()) {
            GuidePanel("Leichter oder schwerer") {
                Text(variant, style = MaterialTheme.typography.bodyMedium, color = Palette.TextMid)
            }
            Spacer(Modifier.height(10.dp))
        }

        if (why.isNotBlank()) {
            GuidePanel(whyLabel) {
                Text(why, style = MaterialTheme.typography.bodyMedium, color = Palette.TextMid)
            }
            Spacer(Modifier.height(10.dp))
        }

        if (video.isNotBlank()) VideoLink(video)
    }
}

@Composable
private fun GuidePanel(
    title: String,
    accent: Color = Palette.Sky,
    tint: Color = Palette.SurfaceHigh,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(tint)
            .padding(13.dp)
    ) {
        Text(title.uppercase(), style = MaterialTheme.typography.labelSmall, color = accent)
        Spacer(Modifier.height(7.dp))
        content()
    }
}

/**
 * Verweis auf ein Erklärvideo. Öffnet eine YouTube-Suche statt eines festen Videos —
 * einzelne Videos verschwinden mit der Zeit, eine Suche liefert immer Treffer.
 */
@Composable
private fun VideoLink(query: String) {
    val context = LocalContext.current
    val red = Color(0xFFEF4444)
    val shape = RoundedCornerShape(14.dp)

    Row(
        Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(red.copy(alpha = 0.09f))
            .border(1.dp, red.copy(alpha = 0.3f), shape)
            .clickable {
                val url = "https://www.youtube.com/results?search_query=" + Uri.encode(query)
                runCatching {
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                }
            }
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(red),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Filled.PlayArrow, null, tint = Color.White, modifier = Modifier.size(19.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                "Video ansehen",
                style = MaterialTheme.typography.titleMedium,
                color = Palette.TextHigh
            )
            Text(
                "Öffnet YouTube · „$query\"",
                style = MaterialTheme.typography.bodySmall,
                color = Palette.TextLow
            )
        }
    }
}
