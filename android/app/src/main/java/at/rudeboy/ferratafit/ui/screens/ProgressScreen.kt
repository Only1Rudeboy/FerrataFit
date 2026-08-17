package at.rudeboy.ferratafit.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import at.rudeboy.ferratafit.data.*
import at.rudeboy.ferratafit.ui.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ProgressScreen(
    state: AppState,
    earnedBadgeIds: Set<String>,
    bodySyncing: Boolean,
    onSyncBody: () -> Unit,
    onAddBodyManual: (Double, Double?) -> Unit = { _, _ -> },
    onImportBodyFile: (String) -> Unit = {}
) {
    val now = System.currentTimeMillis()
    val sessions = state.sessions
    val meters = Journey.totalMeters(state.progress)
    val trained = Catalog.exercises.filter { ex -> sessions.any { s -> s.sets.any { it.exerciseId == ex.id } } }
    var selected by remember(trained.size) {
        mutableStateOf(trained.firstOrNull { it.ferrataFocus >= 3 }?.id ?: trained.firstOrNull()?.id)
    }
    val records = Stats.records(sessions)

    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Column(Modifier.padding(top = 6.dp)) {
                Text("Fortschritt", style = MaterialTheme.typography.headlineLarge, color = Palette.TextHigh)
                Text(
                    if (sessions.isEmpty() && state.progress.isEmpty())
                        "Hier wird es spannend, sobald die erste Etappe steht."
                    else buildString {
                        append("${state.progress.count { !it.skipped }} Etappen")
                        if (sessions.isNotEmpty()) {
                            append(" · ${sessions.size} Einheiten")
                            append(" · ${Stats.totalVolume(sessions).toInt()} kg bewegt")
                        }
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = Palette.TextMid
                )
            }
        }

        // ---------- Körperdaten von der Waage ----------
        item { SectionTitle("Körper") }
        item {
            BodyCard(
                state = state,
                syncing = bodySyncing,
                onSync = onSyncBody,
                onAddManual = onAddBodyManual,
                onImportFile = onImportBodyFile
            )
        }

        // ---------- Höhenmeter und Gipfel ----------
        item {
            FfCard(accent = Palette.Amber) {
                Row(verticalAlignment = Alignment.Top) {
                    Column(Modifier.weight(1f)) {
                        Text("HÖHENMETER", style = MaterialTheme.typography.labelSmall, color = Palette.TextLow)
                        Text(
                            "$meters Hm",
                            style = MaterialTheme.typography.headlineLarge,
                            color = Palette.Amber
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Erreicht", style = MaterialTheme.typography.labelSmall, color = Palette.TextLow)
                        Text(
                            "${Journey.summitProgress(meters).reached.size} Gipfel",
                            style = MaterialTheme.typography.titleMedium,
                            color = Palette.TextHigh
                        )
                    }
                }
                Spacer(Modifier.height(14.dp))
                Journey.summits.forEach { s ->
                    val reached = meters >= s.meters
                    Row(
                        Modifier.fillMaxWidth().padding(vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            if (reached) "⛰️" else "·",
                            modifier = Modifier.width(24.dp),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            s.name,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (reached) Palette.TextHigh else Palette.TextLow,
                            fontWeight = if (reached) FontWeight.SemiBold else FontWeight.Normal,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            "${s.meters} Hm",
                            style = MaterialTheme.typography.bodySmall,
                            color = Palette.TextLow
                        )
                    }
                }
            }
        }

        // ---------- Abzeichen ----------
        item { SectionTitle("Abzeichen", "${earnedBadgeIds.size} von ${Journey.badges.size}") }
        item {
            FfCard {
                Journey.badges.chunked(3).forEach { row ->
                    Row(
                        Modifier.fillMaxWidth().padding(vertical = 5.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        row.forEach { b ->
                            val on = b.id in earnedBadgeIds
                            Column(
                                Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(
                                        if (on) Palette.Amber.copy(alpha = 0.09f) else Palette.SurfaceHigh
                                    )
                                    .border(
                                        1.dp,
                                        if (on) Palette.Amber.copy(alpha = 0.4f) else Color.Transparent,
                                        RoundedCornerShape(16.dp)
                                    )
                                    .padding(vertical = 12.dp, horizontal = 6.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    b.icon,
                                    style = MaterialTheme.typography.headlineSmall,
                                    modifier = Modifier.alpha(if (on) 1f else 0.25f)
                                )
                                Spacer(Modifier.height(5.dp))
                                Text(
                                    b.name,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (on) Palette.TextHigh else Palette.TextLow,
                                    textAlign = TextAlign.Center,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    b.desc,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Palette.TextLow,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                        // Restplätze auffüllen, damit die Kacheln gleich breit bleiben
                        repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
                    }
                }
            }
        }

        if (sessions.isEmpty()) {
            item {
                EmptyHint(
                    "Nach der ersten Krafteinheit erscheinen hier auch deine Kurven, " +
                        "nach der zweiten beginnt die App zu steigern."
                )
            }
            item { Spacer(Modifier.height(80.dp)) }
            return@LazyColumn
        }

        // ---------- Klettersteig-Kennwerte ----------
        item {
            FfCard {
                Text("Steig-Kennwerte", style = MaterialTheme.typography.titleLarge, color = Palette.TextHigh)
                Spacer(Modifier.height(4.dp))
                Text(
                    "Die drei Werte, die am Fels wirklich zählen.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Palette.TextLow
                )
                Spacer(Modifier.height(16.dp))

                val hang = bestOf(sessions, "deadhang") { it.seconds }
                val pull = bestOf(sessions, "pullup") { it.reps }
                val knee = bestOf(sessions, "hang_knee_raise") { it.reps }

                KeyMetric("Dead Hang", "$hang s", "Ziel: 60 s", hang / 60f, Gradients.amber)
                Spacer(Modifier.height(14.dp))
                KeyMetric("Klimmzüge", "$pull", "Ziel: 8", pull / 8f, Gradients.sky)
                Spacer(Modifier.height(14.dp))
                KeyMetric("Knieheben hängend", "$knee", "Ziel: 12", knee / 12f, Gradients.emerald)
            }
        }

        // ---------- Verlaufsdiagramm ----------
        if (trained.isNotEmpty()) {
            item { SectionTitle("Verlauf") }
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(trained) { ex ->
                        val isSel = ex.id == selected
                        Box(
                            Modifier
                                .clip(CircleShape)
                                .background(if (isSel) Palette.Sky else Palette.Surface)
                                .border(
                                    1.dp,
                                    if (isSel) Color.Transparent else Palette.Outline,
                                    CircleShape
                                )
                                .clickable { selected = ex.id }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text(
                                ex.name,
                                style = MaterialTheme.typography.labelLarge,
                                color = if (isSel) Palette.Ink else Palette.TextMid
                            )
                        }
                    }
                }
            }
            item {
                val ex = Catalog.byId(selected ?: "")
                val points = Stats.trend(selected ?: "", sessions)
                FfCard {
                    if (ex != null) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(
                                    ex.name,
                                    style = MaterialTheme.typography.titleLarge,
                                    color = Palette.TextHigh
                                )
                                Text(
                                    "${points.size} Einheiten aufgezeichnet",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Palette.TextLow
                                )
                            }
                            if (points.size >= 2) {
                                val delta = points.last().value - points.first().value
                                Pill(
                                    (if (delta >= 0) "+" else "") + when (ex.progression) {
                                        ProgressionKind.TIME -> "${delta.toInt()} s"
                                        ProgressionKind.WEIGHT -> fmtKg(delta)
                                        ProgressionKind.REPS -> "${delta.toInt()} Wdh."
                                    },
                                    if (delta >= 0) Palette.Emerald else Palette.Rose
                                )
                            }
                        }
                        Spacer(Modifier.height(16.dp))
                        TrendChart(points)
                        if (points.size >= 2) {
                            Spacer(Modifier.height(10.dp))
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    "${dateShort(points.first().at)} · ${points.first().label}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Palette.TextLow
                                )
                                Text(
                                    "${dateShort(points.last().at)} · ${points.last().label}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Palette.Sky
                                )
                            }
                        }
                    }
                }
            }
        }

        // ---------- Bestleistungen ----------
        if (records.isNotEmpty()) {
            item { SectionTitle("Bestleistungen", "${records.size}") }
            item {
                FfCard(padding = 8.dp) {
                    records.take(12).forEachIndexed { i, r ->
                        Row(
                            Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 11.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                when {
                                    i == 0 -> Icons.Filled.EmojiEvents
                                    Catalog.byId(r.exerciseId)?.progression == ProgressionKind.TIME ->
                                        Icons.Filled.Timer
                                    else -> Icons.Filled.FitnessCenter
                                },
                                null,
                                tint = if (i == 0) Palette.Amber else Palette.TextLow,
                                modifier = Modifier.size(17.dp)
                            )
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(
                                    r.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Palette.TextHigh
                                )
                                Text(
                                    dateShort(r.achievedAt),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Palette.TextLow
                                )
                            }
                            Text(
                                r.value,
                                style = MaterialTheme.typography.titleMedium,
                                color = Palette.Sky,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        if (i < records.take(12).size - 1) {
                            HorizontalDivider(
                                color = Palette.Outline.copy(alpha = 0.4f),
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                        }
                    }
                }
            }
        }

        // ---------- Wochenübersicht ----------
        item { SectionTitle("Einheiten je Woche") }
        item {
            FfCard {
                val byWeek = sessions.groupBy { weekLabel(it.startedAt) }
                    .toList().sortedBy { it.second.minOf { s -> s.startedAt } }
                    .takeLast(8)
                val max = byWeek.maxOfOrNull { it.second.size } ?: 1
                Row(
                    Modifier.fillMaxWidth().height(110.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    byWeek.forEach { (label, list) ->
                        Column(
                            Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                "${list.size}",
                                style = MaterialTheme.typography.labelSmall,
                                color = Palette.TextMid
                            )
                            Spacer(Modifier.height(4.dp))
                            Box(
                                Modifier
                                    .fillMaxWidth()
                                    .height((70 * (list.size.toFloat() / max)).dp.coerceAtLeast(6.dp))
                                    .clip(RoundedCornerShape(7.dp))
                                    .background(
                                        if (list.size >= 3) Gradients.emerald else Gradients.sky
                                    )
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(
                                label,
                                style = MaterialTheme.typography.labelSmall,
                                color = Palette.TextLow
                            )
                        }
                    }
                }
            }
        }

        item { Spacer(Modifier.height(80.dp)) }
    }
}

@Composable
private fun KeyMetric(
    name: String,
    value: String,
    goal: String,
    progress: Float,
    brush: androidx.compose.ui.graphics.Brush
) {
    Column {
        Row(verticalAlignment = Alignment.Bottom) {
            Text(name, style = MaterialTheme.typography.bodyMedium, color = Palette.TextMid)
            Spacer(Modifier.weight(1f))
            Text(value, style = MaterialTheme.typography.titleLarge, color = Palette.TextHigh)
            Spacer(Modifier.width(8.dp))
            Text(goal, style = MaterialTheme.typography.labelSmall, color = Palette.TextLow)
        }
        Spacer(Modifier.height(7.dp))
        BarMeter(progress = progress, brush = brush, height = 7.dp)
    }
}

private fun bestOf(sessions: List<Session>, id: String, sel: (SetLog) -> Int): Int =
    sessions.flatMap { it.sets }.filter { it.exerciseId == id }.maxOfOrNull(sel) ?: 0

private fun dateShort(millis: Long): String =
    SimpleDateFormat("d. MMM", Locale.GERMAN).format(Date(millis))

private fun weekLabel(millis: Long): String =
    "KW" + SimpleDateFormat("w", Locale.GERMAN).format(Date(millis))
