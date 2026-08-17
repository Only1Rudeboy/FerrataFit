package at.rudeboy.ferratafit.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingFlat
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import at.rudeboy.ferratafit.data.AppState
import at.rudeboy.ferratafit.data.Body
import at.rudeboy.ferratafit.data.fmtKg
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * Körperdaten von der Waage.
 *
 * Für den Klettersteig zählt nicht das Gewicht allein, sondern das Verhältnis von Kraft
 * zu Last: Ein Klimmzug bei 74 kg ist eine andere Übung als bei 78 kg. Die Karte ordnet
 * deshalb ein, was die Veränderung fürs Ziehen bedeutet — ohne ein Zielgewicht vorzugeben.
 */
@Composable
fun BodyCard(
    state: AppState,
    syncing: Boolean,
    onSync: () -> Unit
) {
    val now = System.currentTimeMillis()
    val latest = Body.latest(state.body)
    val trend = Body.weightTrend(state.body, now)
    val bestPullup = state.sessions.flatMap { it.sets }
        .filter { it.exerciseId == "pullup" }.maxOfOrNull { it.reps } ?: 0

    FfCard(accent = if (latest != null) Palette.Violet else null) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(13.dp))
                    .background(Palette.Violet.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.MonitorWeight, null,
                    tint = Palette.Violet, modifier = Modifier.size(20.dp)
                )
            }
            Spacer(Modifier.width(13.dp))
            Column(Modifier.weight(1f)) {
                Text("Körperdaten", style = MaterialTheme.typography.titleLarge, color = Palette.TextHigh)
                Text(
                    if (latest != null) Body.freshnessLabel(latest.at, now)
                    else "von deiner Waage",
                    style = MaterialTheme.typography.bodySmall,
                    color = Palette.TextLow
                )
            }
            IconButton(onClick = onSync, enabled = !syncing) {
                if (syncing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = Palette.Violet
                    )
                } else {
                    Icon(Icons.Filled.Sync, "Abgleichen", tint = Palette.TextMid)
                }
            }
        }

        if (latest == null) {
            Spacer(Modifier.height(12.dp))
            Text(
                "Noch keine Messung übernommen. Stell dich auf die Waage und tippe dann auf " +
                    "das Abgleich-Zeichen.\n\nDamit die Werte ankommen, muss FitDays mit Samsung " +
                    "Health abgleichen und Samsung Health mit Health Connect — beides einmalig " +
                    "in den jeweiligen Apps einschalten.",
                style = MaterialTheme.typography.bodyMedium,
                color = Palette.TextMid
            )
            return@FfCard
        }

        // ---------- Gewicht mit Trend ----------
        Spacer(Modifier.height(14.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                fmtKg(latest.weightKg),
                style = MaterialTheme.typography.displayMedium,
                color = Palette.TextHigh
            )
            Spacer(Modifier.width(12.dp))
            if (trend != null && abs(trend) >= 0.1) {
                val down = trend < 0
                Row(
                    Modifier.padding(bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        if (down) Icons.Filled.TrendingDown else Icons.Filled.TrendingUp,
                        null,
                        tint = Palette.TextMid,
                        modifier = Modifier.size(17.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        "${if (down) "" else "+"}${(trend * 10).roundToInt() / 10.0} kg",
                        style = MaterialTheme.typography.titleMedium,
                        color = Palette.TextMid
                    )
                    Text(
                        " / Monat",
                        style = MaterialTheme.typography.bodySmall,
                        color = Palette.TextLow
                    )
                }
            } else if (trend != null) {
                Row(Modifier.padding(bottom = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.TrendingFlat, null, tint = Palette.TextLow, modifier = Modifier.size(17.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("stabil", style = MaterialTheme.typography.bodySmall, color = Palette.TextLow)
                }
            }
        }

        // ---------- Zusammensetzung ----------
        val parts = buildList {
            latest.bodyFatPercent?.let {
                add(Triple("Körperfett", "${(it * 10).roundToInt() / 10.0} %", Body.bodyFatLabel(it)))
            }
            latest.leanMassKg?.let { add(Triple("Magermasse", fmtKg(it), "")) }
            latest.waterMassKg?.let { add(Triple("Wasser", fmtKg(it), "")) }
            latest.boneMassKg?.let { add(Triple("Knochen", fmtKg(it), "")) }
            Body.bmi(latest.weightKg, state.heightCm)?.let { add(Triple("BMI", "$it", "")) }
            latest.basalKcal?.let { add(Triple("Grundumsatz", "$it kcal", "")) }
        }

        if (parts.isNotEmpty()) {
            Spacer(Modifier.height(16.dp))
            parts.chunked(2).forEach { row ->
                Row(
                    Modifier.fillMaxWidth().padding(bottom = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    row.forEach { (label, value, hint) ->
                        Column(
                            Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .background(Palette.SurfaceHigh)
                                .padding(12.dp)
                        ) {
                            Text(
                                label.uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                color = Palette.TextLow
                            )
                            Spacer(Modifier.height(3.dp))
                            Text(
                                value,
                                style = MaterialTheme.typography.titleLarge,
                                color = Palette.TextHigh
                            )
                            if (hint.isNotBlank()) {
                                Text(
                                    hint,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Palette.Violet
                                )
                            }
                        }
                    }
                    if (row.size == 1) Spacer(Modifier.weight(1f))
                }
            }
        }

        // ---------- Was das fürs Ziehen bedeutet ----------
        if (bestPullup > 0) {
            Spacer(Modifier.height(4.dp))
            Column(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Palette.Sky.copy(alpha = 0.08f))
                    .padding(13.dp)
            ) {
                Text(
                    "AM STEIG BEDEUTET DAS",
                    style = MaterialTheme.typography.labelSmall,
                    color = Palette.Sky
                )
                Spacer(Modifier.height(6.dp))
                val perRep = Body.pullupLoadPerRep(latest.weightKg, bestPullup)
                Text(
                    buildString {
                        append("Bei $bestPullup Klimmzügen bewegst du gerade ")
                        append(fmtKg(perRep ?: latest.weightKg))
                        append(" je Wiederholung. ")
                        if (trend != null && trend < -0.4) {
                            val gain = Body.pullupEquivalent(trend)
                            append(
                                "Du bist ${fmtKg(abs(trend))} leichter als vor einem Monat — " +
                                    "das entspricht bei gleicher Kraft grob ${(gain * 10).roundToInt() / 10.0} " +
                                    "zusätzlichen Wiederholungen."
                            )
                        } else if (trend != null && trend > 0.4) {
                            append(
                                "Du bist ${fmtKg(trend)} schwerer als vor einem Monat. Wenn die " +
                                    "Klimmzüge zäher werden, liegt es womöglich daran und nicht an der Kraft."
                            )
                        } else {
                            append("Jedes Kilo weniger macht dieselbe Leistung leichter.")
                        }
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = Palette.TextMid
                )
            }
        }

        // ---------- Verlauf ----------
        if (state.body.size >= 2) {
            Spacer(Modifier.height(14.dp))
            Text("VERLAUF", style = MaterialTheme.typography.labelSmall, color = Palette.TextLow)
            Spacer(Modifier.height(8.dp))
            TrendChart(
                points = state.body.takeLast(30).map {
                    at.rudeboy.ferratafit.data.TrendPoint(it.at, it.weightKg, fmtKg(it.weightKg))
                },
                lineColor = Palette.Violet
            )
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    "${state.body.size} Messungen",
                    style = MaterialTheme.typography.labelSmall,
                    color = Palette.TextLow
                )
                if (state.profile.autoWeightFromScale) {
                    Text(
                        "wird automatisch übernommen",
                        style = MaterialTheme.typography.labelSmall,
                        color = Palette.Emerald
                    )
                }
            }
        }
    }
}

/** Kompakte Zeile für die Startseite: nur Gewicht und Trend. */
@Composable
fun BodyStrip(state: AppState, onOpen: () -> Unit) {
    val now = System.currentTimeMillis()
    val latest = Body.latest(state.body) ?: return
    val trend = Body.weightTrend(state.body, now)

    FfCard(padding = 14.dp, onClick = onOpen) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Filled.MonitorWeight, null,
                tint = Palette.Violet, modifier = Modifier.size(19.dp)
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    fmtKg(latest.weightKg),
                    style = MaterialTheme.typography.titleLarge,
                    color = Palette.TextHigh,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    Body.freshnessLabel(latest.at, now),
                    style = MaterialTheme.typography.bodySmall,
                    color = Palette.TextLow
                )
            }
            latest.bodyFatPercent?.let {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "${(it * 10).roundToInt() / 10.0} %",
                        style = MaterialTheme.typography.titleMedium,
                        color = Palette.TextMid
                    )
                    Text("Körperfett", style = MaterialTheme.typography.labelSmall, color = Palette.TextLow)
                }
            }
            if (trend != null && abs(trend) >= 0.1) {
                Spacer(Modifier.width(12.dp))
                Pill(
                    "${if (trend < 0) "" else "+"}${(trend * 10).roundToInt() / 10.0} kg",
                    if (trend < 0) Palette.Emerald else Palette.TextMid
                )
            }
        }
    }
}
