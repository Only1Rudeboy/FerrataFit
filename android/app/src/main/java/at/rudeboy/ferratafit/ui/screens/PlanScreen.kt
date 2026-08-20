package at.rudeboy.ferratafit.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import at.rudeboy.ferratafit.data.*
import at.rudeboy.ferratafit.ui.*

@Composable
fun PlanScreen(
    state: AppState,
    initialDayId: String?,
    onStartWorkout: (String) -> Unit,
    onToggleExercise: (String) -> Unit
) {
    val now = System.currentTimeMillis()
    var expanded by remember { mutableStateOf(initialDayId ?: Stats.nextDayId(state.sessions)) }
    val nextDay = Stats.nextDayId(state.sessions)

    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Column(Modifier.padding(top = 6.dp)) {
                Text("Dein Plan", style = MaterialTheme.typography.headlineLarge, color = Palette.TextHigh)
                Text(
                    "Drei Einheiten pro Woche, im Wechsel durchrotiert. Zwischen zwei Einheiten " +
                        "sollte mindestens ein Tag Pause liegen.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Palette.TextMid
                )
            }
        }

        Catalog.days.forEach { day ->
            item {
                val exercises = PlanBuilder.exercisesFor(day, state.profile, state.hiddenExercises)
                val isNext = day.id == nextDay
                val open = expanded == day.id

                FfCard(accent = if (isNext) Palette.Sky else null, padding = 0.dp) {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clickable { expanded = if (open) "" else day.id }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (isNext) Palette.Sky else Palette.SurfaceHigh),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                day.id,
                                style = MaterialTheme.typography.titleLarge,
                                color = if (isNext) Palette.Ink else Palette.TextMid,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(Modifier.width(13.dp))
                        Column(Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    day.title,
                                    style = MaterialTheme.typography.titleLarge,
                                    color = Palette.TextHigh
                                )
                                if (isNext) {
                                    Spacer(Modifier.width(8.dp))
                                    Pill("als Nächstes", Palette.Sky)
                                }
                            }
                            Text(
                                "${exercises.size} Übungen · ${exercises.sumOf { it.sets }} Sätze",
                                style = MaterialTheme.typography.bodySmall,
                                color = Palette.TextLow
                            )
                        }
                        Icon(
                            if (open) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                            null, tint = Palette.TextLow
                        )
                    }

                    AnimatedVisibility(
                        visible = open,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Column(Modifier.padding(horizontal = 16.dp).padding(bottom = 16.dp)) {
                            Text(
                                day.subtitle,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Palette.Sky
                            )
                            Spacer(Modifier.height(12.dp))

                            exercises.forEach { ex ->
                                val sug = Progression.suggest(
                                    ex, state.sessions, state.profile, now,
                                    Recovery.state(state.ascents, now)
                                )
                                ExerciseRow(
                                    exercise = ex,
                                    suggestion = sug,
                                    hidden = false,
                                    onToggle = { onToggleExercise(ex.id) }
                                )
                                Spacer(Modifier.height(8.dp))
                            }

                            // Ausgeblendete Übungen dieses Tages
                            val hidden = day.exerciseIds
                                .mapNotNull { Catalog.byId(it) }
                                .filter { it.id in state.hiddenExercises && it.station in state.profile.stations }
                            if (hidden.isNotEmpty()) {
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    "AUSGEBLENDET",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Palette.TextLow
                                )
                                Spacer(Modifier.height(6.dp))
                                hidden.forEach { ex ->
                                    ExerciseRow(
                                        exercise = ex,
                                        suggestion = null,
                                        hidden = true,
                                        onToggle = { onToggleExercise(ex.id) }
                                    )
                                    Spacer(Modifier.height(8.dp))
                                }
                            }

                            Spacer(Modifier.height(8.dp))
                            Button(
                                onClick = { onStartWorkout(day.id) },
                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                shape = RoundedCornerShape(15.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isNext) Palette.Sky else Palette.SurfaceHigh,
                                    contentColor = if (isNext) Palette.Ink else Palette.TextHigh
                                )
                            ) {
                                Icon(Icons.Filled.PlayArrow, null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Diese Einheit starten", fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }
        }

        item {
            FfCard {
                Text(
                    "Warum dieser Aufbau?",
                    style = MaterialTheme.typography.titleLarge,
                    color = Palette.TextHigh
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "Der Zug-Tag steht vorne, weil Griffkraft und Zugkraft am Steig zuerst " +
                        "limitieren — die willst du ausgeruht trainieren. Der Bein-Tag folgt, " +
                        "damit die Unterarme sich erholen, während die Beine arbeiten. " +
                        "Der Druck-Tag hält die Schultern im Gleichgewicht: Wer nur zieht, " +
                        "zieht sich die Haltung nach vorne.\n\n" +
                        "Innerhalb einer Einheit stehen die anspruchsvollen Übungen oben. " +
                        "Am Ende der Liste sind Rumpf und Griffkraft — dort schadet etwas " +
                        "Vorermüdung nicht, im Gegenteil.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Palette.TextMid
                )
            }
        }

        item { Spacer(Modifier.height(80.dp)) }
    }
}

@Composable
private fun ExerciseRow(
    exercise: Exercise,
    suggestion: Suggestion?,
    hidden: Boolean,
    onToggle: () -> Unit
) {
    var showDetail by remember { mutableStateOf(false) }
    val shape = RoundedCornerShape(15.dp)

    Column(
        Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(Palette.SurfaceHigh.copy(alpha = if (hidden) 0.4f else 1f))
            .clickable { showDetail = !showDetail }
            .padding(13.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        exercise.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = if (hidden) Palette.TextLow else Palette.TextHigh
                    )
                    if (exercise.ferrataFocus >= 3) {
                        Spacer(Modifier.width(7.dp))
                        Box(
                            Modifier
                                .clip(CircleShape)
                                .background(Palette.Amber.copy(alpha = 0.18f))
                                .padding(horizontal = 7.dp, vertical = 2.dp)
                        ) {
                            Text(
                                "Steig-Kern",
                                style = MaterialTheme.typography.labelSmall,
                                color = Palette.Amber
                            )
                        }
                    }
                }
                Text(
                    "${exercise.sets} × ${
                        if (exercise.progression == ProgressionKind.TIME) "halten"
                        else "${exercise.repMin}–${exercise.repMax}"
                    } · ${exercise.station.label}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Palette.TextLow
                )
            }
            if (suggestion != null) {
                Text(
                    suggestion.headline,
                    style = MaterialTheme.typography.titleMedium,
                    color = when (suggestion.advice) {
                        Advice.INCREASE -> Palette.Amber
                        Advice.DELOAD -> Palette.Emerald
                        Advice.BACKOFF -> Palette.Rose
                        else -> Palette.TextMid
                    }
                )
                Spacer(Modifier.width(8.dp))
            }
            IconButton(onClick = onToggle, modifier = Modifier.size(32.dp)) {
                Icon(
                    if (hidden) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                    if (hidden) "Wieder einblenden" else "Ausblenden",
                    tint = Palette.TextLow,
                    modifier = Modifier.size(17.dp)
                )
            }
        }

        AnimatedVisibility(visible = showDetail, enter = expandVertically(), exit = shrinkVertically()) {
            Column(Modifier.padding(top = 11.dp)) {
                Text(exercise.cue, style = MaterialTheme.typography.bodySmall, color = Palette.TextMid)
                Spacer(Modifier.height(7.dp))
                Text(exercise.why, style = MaterialTheme.typography.bodySmall, color = Palette.Sky)
                if (suggestion != null) {
                    Spacer(Modifier.height(7.dp))
                    Text(
                        suggestion.reason,
                        style = MaterialTheme.typography.bodySmall,
                        color = Palette.TextLow
                    )
                }
            }
        }
    }
}
