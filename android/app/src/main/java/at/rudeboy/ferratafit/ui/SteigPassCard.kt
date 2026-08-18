package at.rudeboy.ferratafit.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import at.rudeboy.ferratafit.data.SteigPass

/**
 * Der Steigpass.
 *
 * Rang und Form messen verschiedene Dinge und würden nebeneinander gestellt miteinander
 * konkurrieren: Der Rang ist ein Werdegang und fällt nie, die Form ist ein Zustand und
 * schwankt. Deshalb stehen sie hier ineinander — der Rang als Überschrift, die Form als
 * Balken, und darunter die einzige Zahl, die für die Routenwahl zählt.
 */
@Composable
fun SteigPassCard(pass: SteigPass, compact: Boolean = false) {
    FfCard(accent = Palette.Amber) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(15.dp))
                    .background(Palette.Amber.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(pass.rank.icon, style = MaterialTheme.typography.headlineSmall)
            }
            Spacer(Modifier.width(13.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    pass.rank.title.uppercase(),
                    style = MaterialTheme.typography.titleLarge,
                    color = Palette.TextHigh,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    buildString {
                        append("${pass.cleanAscents} ")
                        append(if (pass.cleanAscents == 1) "Begehung" else "Begehungen")
                        pass.mastered?.let { append(" · bestätigt bis ${it.label}") }
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = Palette.TextLow
                )
            }
        }

        if (!compact) {
            Spacer(Modifier.height(4.dp))
            Text(
                pass.rank.subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = Palette.TextMid
            )
        }

        // Form als Balken — nicht als Rivalin zur Rangzahl
        Spacer(Modifier.height(16.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "Form heute",
                style = MaterialTheme.typography.bodySmall,
                color = Palette.TextMid,
                modifier = Modifier.width(78.dp)
            )
            Box(Modifier.weight(1f)) {
                BarMeter(
                    progress = pass.readiness / 100f,
                    height = 7.dp,
                    brush = when {
                        pass.readiness >= 65 -> Gradients.emerald
                        pass.readiness >= 35 -> Gradients.sky
                        else -> Gradients.amber
                    }
                )
            }
            Spacer(Modifier.width(10.dp))
            Text(
                "${pass.readiness}",
                style = MaterialTheme.typography.titleMedium,
                color = Palette.TextHigh
            )
        }

        // Die eine Zahl, die zählt
        Spacer(Modifier.height(16.dp))
        Column(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(Palette.Sky.copy(alpha = 0.08f))
                .padding(13.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "IM RAHMEN",
                    style = MaterialTheme.typography.labelSmall,
                    color = Palette.Sky
                )
                Spacer(Modifier.weight(1f))
                Text(
                    "bis Stufe ${pass.recommended.label}",
                    style = MaterialTheme.typography.titleLarge,
                    color = Palette.Sky,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.height(7.dp))
            Text(
                pass.reason,
                style = MaterialTheme.typography.bodyMedium,
                color = Palette.TextMid
            )
        }

        if (!compact && pass.nextRank != null) {
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(pass.nextRank.icon, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.width(9.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        "Nächster Rang: ${pass.nextRank.title}",
                        style = MaterialTheme.typography.titleMedium,
                        color = Palette.TextHigh
                    )
                    Text(
                        pass.nextHint,
                        style = MaterialTheme.typography.bodySmall,
                        color = Palette.TextLow
                    )
                }
            }
        }

        if (!compact && pass.meters > 0) {
            Spacer(Modifier.height(12.dp))
            Text(
                "${pass.meters} Höhenmeter am Fels gesammelt",
                style = MaterialTheme.typography.bodySmall,
                color = Palette.TextLow
            )
        }
    }
}
