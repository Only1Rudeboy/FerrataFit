package at.rudeboy.ferratafit.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Terrain
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
    onStartWorkout: (String) -> Unit,
    onOpenDay: (String) -> Unit
) {
    val now = System.currentTimeMillis()
    val week = Progression.weekInCycle(state.profile, now)
    val deload = Progression.isDeloadWeek(week)
    val nextDayId = Stats.nextDayId(state.sessions)
    val day = Catalog.day(nextDayId)
    val exercises = PlanBuilder.exercisesFor(day, state.profile, state.hiddenExercises)
    val suggestions = exercises.map { Progression.suggest(it, state.sessions, state.profile, now) }
    val increases = suggestions.filter { it.advice == Advice.INCREASE }

    val readiness = Stats.ferrataReadiness(state.sessions, now)
    val streak = Stats.weeklyStreak(state.sessions, now)
    val thisWeek = Stats.sessionsThisWeek(state.sessions, now)

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Column(Modifier.padding(top = 6.dp, bottom = 2.dp)) {
                Text(greeting(), style = MaterialTheme.typography.bodyMedium, color = Palette.TextMid)
                Text(
                    "Bereit für ${day.title}?",
                    style = MaterialTheme.typography.headlineLarge,
                    color = Palette.TextHigh
                )
            }
        }

        // ---------- Wochenphase ----------
        item {
            FfCard(accent = if (deload) Palette.Emerald else Palette.Sky) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Pill(
                        "Woche $week von ${Progression.CYCLE_WEEKS}",
                        if (deload) Palette.Emerald else Palette.Sky
                    )
                    Spacer(Modifier.weight(1f))
                    if (deload) Pill("Entlastung", Palette.Emerald)
                }
                Spacer(Modifier.height(10.dp))
                Text(
                    Progression.weekLabel(week).substringAfter("· "),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Palette.TextMid
                )
                Spacer(Modifier.height(12.dp))
                BarMeter(
                    progress = week / Progression.CYCLE_WEEKS.toFloat(),
                    brush = if (deload) Gradients.emerald else Gradients.sky
                )
            }
        }

        // ---------- Heutige Einheit ----------
        item {
            FfCard(padding = 0.dp) {
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
                            Text(
                                day.id,
                                style = MaterialTheme.typography.titleLarge,
                                color = Palette.Sky,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(day.title, style = MaterialTheme.typography.headlineSmall, color = Palette.TextHigh)
                            Text(day.subtitle, style = MaterialTheme.typography.bodySmall, color = Palette.Sky)
                        }
                    }
                    Spacer(Modifier.height(14.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        MiniStat("${exercises.size}", "Übungen")
                        MiniStat("${exercises.sumOf { it.sets }}", "Sätze")
                        MiniStat("~${estimateMinutes(exercises)}", "Minuten")
                    }
                }

                Column(Modifier.padding(18.dp)) {
                    if (increases.isNotEmpty()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Filled.ArrowUpward, null,
                                tint = Palette.Amber, modifier = Modifier.size(16.dp)
                            )
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
                                if (s.previousHeadline != null) {
                                    Text(
                                        s.previousHeadline,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Palette.TextLow
                                    )
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
                    } else {
                        Text(
                            exercises.take(4).joinToString(" · ") { it.name } +
                                if (exercises.size > 4) " · …" else "",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Palette.TextMid
                        )
                        Spacer(Modifier.height(14.dp))
                    }

                    Button(
                        onClick = { onStartWorkout(nextDayId) },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Palette.Sky,
                            contentColor = Palette.Ink
                        )
                    ) {
                        Icon(Icons.Filled.PlayArrow, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Training starten", fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.height(8.dp))
                    TextButton(
                        onClick = { onOpenDay(nextDayId) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Erst den Plan ansehen", color = Palette.TextMid)
                    }
                }
            }
        }

        // ---------- Klettersteig-Bereitschaft ----------
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
                            Text(
                                "$readiness",
                                style = MaterialTheme.typography.headlineMedium,
                                color = Palette.TextHigh
                            )
                            Text("von 100", style = MaterialTheme.typography.labelSmall, color = Palette.TextLow)
                        }
                    }
                    Spacer(Modifier.width(16.dp))
                    Column(Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Terrain, null, tint = Palette.Sky, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(
                                "Steig-Bereitschaft",
                                style = MaterialTheme.typography.titleMedium,
                                color = Palette.TextHigh
                            )
                        }
                        Spacer(Modifier.height(6.dp))
                        Text(
                            Stats.readinessLabel(readiness),
                            style = MaterialTheme.typography.bodySmall,
                            color = Palette.TextMid
                        )
                        val target = state.profile.targetFerrataDate
                        if (target != null && target > now) {
                            Spacer(Modifier.height(10.dp))
                            val days = TimeUnit.MILLISECONDS.toDays(target - now).toInt()
                            Pill(
                                if (state.profile.targetFerrataName.isBlank()) "noch $days Tage"
                                else "${state.profile.targetFerrataName} · $days Tage",
                                Palette.Amber
                            )
                        }
                    }
                }
            }
        }

        // ---------- Kennzahlen ----------
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatTile(
                    value = "$streak",
                    label = if (streak == 1) "Woche in Folge" else "Wochen in Folge",
                    icon = Icons.Filled.LocalFireDepartment,
                    tint = Palette.Amber,
                    modifier = Modifier.weight(1f)
                )
                StatTile(
                    value = "$thisWeek/${state.profile.daysPerWeek}",
                    label = "diese Woche",
                    icon = Icons.Filled.TrendingUp,
                    tint = Palette.Emerald,
                    modifier = Modifier.weight(1f)
                )
                if (steps != null) {
                    StatTile(
                        value = formatSteps(steps),
                        label = "Schritte heute",
                        icon = Icons.Filled.DirectionsWalk,
                        tint = Palette.Sky,
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    StatTile(
                        value = "${state.sessions.size}",
                        label = "Einheiten gesamt",
                        icon = Icons.Filled.TrendingUp,
                        tint = Palette.Violet,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // ---------- Letzte Einheiten ----------
        if (state.sessions.isNotEmpty()) {
            item { SectionTitle("Zuletzt trainiert") }
            items(state.sessions.sortedByDescending { it.startedAt }.take(4)) { s ->
                val d = Catalog.days.firstOrNull { it.id == s.dayId }
                FfCard(padding = 14.dp) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Palette.SurfaceHigh),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(s.dayId, color = Palette.Sky, fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                d?.title ?: "Einheit",
                                style = MaterialTheme.typography.titleMedium,
                                color = Palette.TextHigh
                            )
                            Text(
                                "${relativeDay(s.startedAt, now)} · ${s.sets.size} Sätze · ${s.durationMin} min",
                                style = MaterialTheme.typography.bodySmall,
                                color = Palette.TextLow
                            )
                        }
                        if (s.volumeKg > 0) {
                            Text(
                                "${s.volumeKg.toInt()} kg",
                                style = MaterialTheme.typography.titleMedium,
                                color = Palette.TextMid
                            )
                        }
                    }
                }
            }
        } else {
            item {
                EmptyHint(
                    "Noch keine Einheit gespeichert. Die erste dient dazu, deine Lasten zu finden — " +
                        "danach übernimmt die App das Steigern."
                )
            }
        }

        item { Spacer(Modifier.height(80.dp)) }
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
    val seconds = exercises.sumOf { it.sets * (it.restSec + 40) }
    return ((seconds / 60.0 / 5).toInt() + 1) * 5
}

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

private fun formatSteps(steps: Long): String =
    if (steps >= 1000) "${(steps / 100) / 10.0}k" else "$steps"
