package at.rudeboy.ferratafit.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Terrain
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import at.rudeboy.ferratafit.data.*
import at.rudeboy.ferratafit.ui.*
import java.util.Calendar
import java.util.concurrent.TimeUnit

@Composable
fun HomeScreen(
    state: AppState,
    steps: Long?,
    badgeCount: Int,
    onStartStage: (String) -> Unit,
    onSkipStage: (String) -> Unit,
    onOpenDay: (String) -> Unit,
    onOpenBody: () -> Unit = {}
) {
    val now = System.currentTimeMillis()
    val p = state.profile
    val stage = Journey.current(state.progress)
    val stageIndex = Journey.currentIndex(state.progress)
    val meters = Journey.totalMeters(state.progress)
    val summit = Journey.summitProgress(meters)
    val quote = Journey.quoteOfDay(now)
    val week = Progression.weekInCycle(p, now)
    val taper = Progression.taperStage(p, now)
    // Solange die Tour bevorsteht, gilt der Taper — sonst stünden zwei Ansagen nebeneinander.
    val deload = taper == null && Progression.isDeloadWeek(week)
    val readiness = Stats.ferrataReadiness(state.sessions, now)
    val streak = Stats.weeklyStreak(state.sessions, now)

    // Nur Krafteinheiten haben Übungen und Steigerungen zu zeigen.
    val exercises = Journey.dayFor(stage)
        ?.let { PlanBuilder.exercisesFor(it, p, state.hiddenExercises) } ?: emptyList()
    val increases = exercises
        .map { Progression.suggest(it, state.sessions, p, now) }
        .filter { it.advice == Advice.INCREASE }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Column(Modifier.padding(top = 6.dp, bottom = 2.dp)) {
                Text(
                    "${greeting()} · Etappe ${stageIndex + 1}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Palette.TextMid
                )
                Text(stage.title, style = MaterialTheme.typography.headlineLarge, color = Palette.TextHigh)
            }
        }

        // ---------- Spruch des Tages ----------
        item {
            Row(
                Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min)
                    .clip(RoundedCornerShape(topEnd = 14.dp, bottomEnd = 14.dp))
                    .background(
                        Brush.horizontalGradient(listOf(Palette.Amber.copy(alpha = 0.10f), Color.Transparent))
                    )
            ) {
                Box(
                    Modifier
                        .width(3.dp)
                        .fillMaxHeight()
                        .background(Palette.Amber)
                )
                Column(Modifier.padding(14.dp)) {
                    Text(
                        "„${quote.text}\"",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Palette.TextHigh,
                        fontStyle = FontStyle.Italic
                    )
                    quote.by?.let {
                        Spacer(Modifier.height(6.dp))
                        Text("— $it", style = MaterialTheme.typography.bodySmall, color = Palette.TextLow)
                    }
                }
            }
        }

        // ---------- Höhenmeter-Konto ----------
        item {
            FfCard(accent = Palette.Amber) {
                Row(verticalAlignment = Alignment.Top) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            "GESAMMELTE HÖHENMETER",
                            style = MaterialTheme.typography.labelSmall,
                            color = Palette.TextLow
                        )
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                formatMeters(meters),
                                style = MaterialTheme.typography.displayMedium,
                                color = Palette.Amber
                            )
                            Text(
                                " Hm",
                                style = MaterialTheme.typography.titleLarge,
                                color = Palette.Amber,
                                modifier = Modifier.padding(bottom = 5.dp)
                            )
                        }
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Nächstes Ziel", style = MaterialTheme.typography.labelSmall, color = Palette.TextLow)
                        Text(
                            summit.next.name,
                            style = MaterialTheme.typography.titleMedium,
                            color = Palette.TextHigh
                        )
                        Text(
                            "noch ${formatMeters(summit.toGo)} Hm",
                            style = MaterialTheme.typography.bodySmall,
                            color = Palette.TextLow
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
                BarMeter(progress = summit.fraction, brush = Gradients.amber)
                Spacer(Modifier.height(7.dp))
                Text(summit.next.note, style = MaterialTheme.typography.bodySmall, color = Palette.TextLow)
            }
        }

        // ---------- Aktuelle Etappe ----------
        item {
            FfCard(accent = Palette.Sky, padding = 0.dp) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp))
                        .background(Gradients.hero)
                        .padding(18.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(13.dp))
                                .background(Palette.Sky.copy(alpha = 0.18f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(stage.icon, style = MaterialTheme.typography.titleLarge)
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(stage.title, style = MaterialTheme.typography.headlineSmall, color = Palette.TextHigh)
                            Text(stage.subtitle, style = MaterialTheme.typography.bodySmall, color = Palette.Sky)
                        }
                        if (p.travelMode) Pill("Unterwegs", Palette.Amber)
                        else Pill("Zyklus ${Journey.cycleNumber(state.progress)}", Palette.Sky)
                    }
                    Spacer(Modifier.height(14.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        if (stage.kind == StageKind.STRENGTH) {
                            MiniStat("${exercises.size}", "Übungen")
                            MiniStat("${exercises.sumOf { it.sets }}", "Sätze")
                            MiniStat("~${estimateMinutes(exercises)}", "Minuten")
                        } else {
                            MiniStat(
                                if (stage.kind == StageKind.ENDURANCE) "1" else "${stage.mobilityIds.size}",
                                if (stage.kind == StageKind.ENDURANCE) "Aufgabe" else "Übungen"
                            )
                            MiniStat("~${Journey.estimateMinutes(stage)}", "Minuten")
                            MiniStat("+${stage.meters}", "Höhenmeter")
                        }
                    }
                }

                Column(Modifier.padding(18.dp)) {
                    if (stage.kind == StageKind.STRENGTH && deload) {
                        Pill("Entlastungswoche — bewusst leichter", Palette.Emerald)
                        Spacer(Modifier.height(12.dp))
                    }
                    if (taper != null) {
                        val days = Progression.daysToTarget(p, now) ?: 0
                        Pill(
                            when (taper) {
                                Progression.Taper.NUR_LOCKERN -> "Nur noch lockern"
                                Progression.Taper.DEUTLICH_LEICHTER -> "Tour in $days Tagen — deutlich zurückfahren"
                                Progression.Taper.LEICHTER -> "Tour in $days Tagen — ein Satz weniger"
                            },
                            Palette.Amber
                        )
                        Spacer(Modifier.height(10.dp))
                        Text(
                            Progression.taperLabel(taper, days),
                            style = MaterialTheme.typography.bodySmall,
                            color = Palette.TextMid
                        )
                        Spacer(Modifier.height(12.dp))
                    }
                    if (stage.kind == StageKind.STRENGTH && p.travelMode) {
                        Text(
                            "Ohne Gerät: Die Übungen sind durch Körpergewichts-Entsprechungen " +
                                "ersetzt. Die Etappe zählt genauso.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Palette.Amber
                        )
                        Spacer(Modifier.height(12.dp))
                    }

                    if (increases.isNotEmpty()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.ArrowUpward, null, tint = Palette.Amber, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(
                                if (increases.size == 1) "Heute wird aufgelastet"
                                else "Heute wird ${increases.size}× aufgelastet",
                                style = MaterialTheme.typography.titleMedium,
                                color = Palette.Amber
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                        increases.take(3).forEach { s ->
                            Row(
                                Modifier.fillMaxWidth().padding(vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    s.exercise.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Palette.TextMid,
                                    modifier = Modifier.weight(1f)
                                )
                                s.previousHeadline?.let {
                                    Text(it, style = MaterialTheme.typography.bodySmall, color = Palette.TextLow)
                                    Text("  →  ", style = MaterialTheme.typography.bodySmall, color = Palette.TextLow)
                                }
                                Text(
                                    s.headline,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Palette.Amber,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Spacer(Modifier.height(14.dp))
                    } else if (stage.hint != null) {
                        Text(stage.hint, style = MaterialTheme.typography.bodyMedium, color = Palette.TextMid)
                        Spacer(Modifier.height(14.dp))
                    }

                    Button(
                        onClick = { onStartStage(stage.id) },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Palette.Sky,
                            contentColor = Palette.Ink
                        )
                    ) {
                        Icon(Icons.Filled.PlayArrow, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Etappe starten", fontWeight = FontWeight.Bold)
                    }
                    Row(Modifier.fillMaxWidth()) {
                        stage.dayId?.let { dayId ->
                            TextButton(onClick = { onOpenDay(dayId) }, modifier = Modifier.weight(1f)) {
                                Text("Plan ansehen", color = Palette.TextMid)
                            }
                        }
                        TextButton(onClick = { onSkipStage(stage.id) }, modifier = Modifier.weight(1f)) {
                            Text("Überspringen", color = Palette.TextLow)
                        }
                    }
                }
            }
        }

        // ---------- Der Steig ----------
        item { SectionTitle("Der Steig", "Etappe ${stageIndex + 1}") }
        item {
            FfCard(padding = 14.dp) {
                (0..3).forEach { offset ->
                    val s = Journey.stageAt(stageIndex + offset)
                    PathRow(
                        icon = s.icon,
                        title = s.title,
                        subtitle = if (offset == 0) s.subtitle else "Wird frei, wenn die vorige Etappe steht",
                        meters = s.meters,
                        locked = offset > 0,
                        showConnector = offset < 3
                    )
                }
            }
        }

        // ---------- Bereitschaft ----------
        item {
            FfCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ProgressRing(
                        progress = readiness / 100f,
                        size = 108.dp,
                        stroke = 11.dp,
                        brush = when {
                            readiness >= 65 -> Gradients.emerald
                            readiness >= 35 -> Gradients.sky
                            else -> Gradients.amber
                        }
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("$readiness", style = MaterialTheme.typography.headlineMedium, color = Palette.TextHigh)
                            Text("von 100", style = MaterialTheme.typography.labelSmall, color = Palette.TextLow)
                        }
                    }
                    Spacer(Modifier.width(16.dp))
                    Column(Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Terrain, null, tint = Palette.Sky, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Steig-Bereitschaft", style = MaterialTheme.typography.titleMedium, color = Palette.TextHigh)
                        }
                        Spacer(Modifier.height(6.dp))
                        Text(
                            Stats.readinessLabel(readiness),
                            style = MaterialTheme.typography.bodySmall,
                            color = Palette.TextMid
                        )
                        val target = p.targetFerrataDate
                        if (target != null && target > now) {
                            Spacer(Modifier.height(10.dp))
                            val days = TimeUnit.MILLISECONDS.toDays(target - now).toInt()
                            Pill(
                                if (p.targetFerrataName.isBlank()) "noch $days Tage"
                                else "${p.targetFerrataName} · $days Tage",
                                Palette.Amber
                            )
                        }
                    }
                }
            }
        }

        // ---------- Körperdaten, sofern die Waage etwas geliefert hat ----------
        if (state.body.isNotEmpty()) {
            item { BodyStrip(state = state, onOpen = onOpenBody) }
        }

        // ---------- Kennzahlen ----------
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatTile(
                    value = "${state.progress.count { !it.skipped }}",
                    label = "Etappen gegangen",
                    icon = Icons.Filled.Terrain,
                    tint = Palette.Sky,
                    modifier = Modifier.weight(1f)
                )
                StatTile(
                    value = "$streak",
                    label = if (streak == 1) "Woche in Folge" else "Wochen in Folge",
                    icon = Icons.Filled.LocalFireDepartment,
                    tint = Palette.Amber,
                    modifier = Modifier.weight(1f)
                )
                StatTile(
                    value = "$badgeCount",
                    label = "Abzeichen",
                    icon = Icons.Filled.EmojiEvents,
                    tint = Palette.Violet,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // ---------- Zuletzt gegangen ----------
        if (state.progress.isNotEmpty()) {
            item { SectionTitle("Zuletzt gegangen") }
            items(state.progress.reversed().take(4)) { log ->
                val s = Journey.stage(log.stageId)
                FfCard(padding = 14.dp) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Palette.SurfaceHigh),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(s?.icon ?: "•", style = MaterialTheme.typography.titleMedium)
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                s?.title ?: "Etappe",
                                style = MaterialTheme.typography.titleMedium,
                                color = if (log.skipped) Palette.TextLow else Palette.TextHigh
                            )
                            Text(
                                buildString {
                                    append(relativeDay(log.at, now))
                                    if (log.detail.isNotBlank()) append(" · ${log.detail}")
                                    if (log.skipped) append(" · übersprungen")
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = Palette.TextLow
                            )
                        }
                        if (!log.skipped) {
                            Text(
                                "+${log.meters}",
                                style = MaterialTheme.typography.titleMedium,
                                color = Palette.Amber,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        } else {
            item {
                EmptyHint(
                    "Noch keine Etappe gegangen. Die erste Krafteinheit dient dazu, deine Lasten " +
                        "zu finden — danach übernimmt die App das Steigern."
                )
            }
        }

        item { Spacer(Modifier.height(80.dp)) }
    }
}

/** Eine Zeile im Etappenpfad, mit Verbindungslinie zur nächsten. */
@Composable
private fun PathRow(
    icon: String,
    title: String,
    subtitle: String,
    meters: Int,
    locked: Boolean,
    showConnector: Boolean
) {
    Row(Modifier.height(IntrinsicSize.Min)) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(if (locked) Palette.SurfaceHigh else Palette.Sky),
                contentAlignment = Alignment.Center
            ) {
                if (locked) {
                    Icon(Icons.Filled.Lock, null, tint = Palette.TextLow, modifier = Modifier.size(14.dp))
                } else {
                    Text(icon, style = MaterialTheme.typography.titleMedium)
                }
            }
            if (showConnector) {
                Box(
                    Modifier
                        .width(2.dp)
                        .weight(1f)
                        .background(Palette.Outline)
                )
            }
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f).padding(vertical = 6.dp)) {
            Text(
                title,
                style = MaterialTheme.typography.titleMedium,
                color = if (locked) Palette.TextLow else Palette.TextHigh,
                fontWeight = if (locked) FontWeight.Normal else FontWeight.SemiBold
            )
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = Palette.TextLow)
            if (showConnector) Spacer(Modifier.height(10.dp))
        }
        Text(
            "+$meters",
            style = MaterialTheme.typography.labelLarge,
            color = if (locked) Palette.TextLow.copy(alpha = 0.6f) else Palette.Amber,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

@Composable
private fun MiniStat(value: String, label: String) {
    Column {
        Text(value, style = MaterialTheme.typography.titleLarge, color = Palette.TextHigh)
        Text(label, style = MaterialTheme.typography.labelSmall, color = Palette.TextLow)
    }
}

private fun greeting(): String {
    val h = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return when {
        h < 5 -> "Noch wach?"
        h < 11 -> "Guten Morgen"
        h < 14 -> "Mahlzeit"
        h < 18 -> "Guten Nachmittag"
        else -> "Guten Abend"
    }
}

/** Grobe Dauer: Sätze mal Pause plus Ausführungszeit, aufgerundet auf 5 Minuten. */
private fun estimateMinutes(exercises: List<Exercise>): Int {
    if (exercises.isEmpty()) return 0
    val seconds = exercises.sumOf { it.sets * (it.restSec + 40) }
    return ((seconds / 60.0 / 5).toInt() + 1) * 5
}

/** Tausenderpunkte, wie im Deutschen üblich. */
private fun formatMeters(m: Int): String =
    m.toString().reversed().chunked(3).joinToString(".").reversed()

private fun relativeDay(then: Long, now: Long): String {
    val days = TimeUnit.MILLISECONDS.toDays(now - then).toInt()
    return when (days) {
        0 -> "heute"
        1 -> "gestern"
        in 2..6 -> "vor $days Tagen"
        in 7..13 -> "letzte Woche"
        else -> "vor ${days / 7} Wochen"
    }
}
