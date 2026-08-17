package at.rudeboy.ferratafit.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import at.rudeboy.ferratafit.ActiveEntry
import at.rudeboy.ferratafit.ActiveSet
import at.rudeboy.ferratafit.ActiveWorkout
import at.rudeboy.ferratafit.data.Advice
import at.rudeboy.ferratafit.data.ProgressionKind
import at.rudeboy.ferratafit.data.fmtKg
import at.rudeboy.ferratafit.ui.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutScreen(
    workout: ActiveWorkout,
    onSelectExercise: (Int) -> Unit,
    onUpdateSet: (Int, Int, (ActiveSet) -> ActiveSet) -> Unit,
    onToggleDone: (Int, Int) -> Unit,
    onApplyToRemaining: (Int, Int) -> Unit,
    onFinish: (String) -> Unit,
    onCancel: () -> Unit
) {
    val entry = workout.entries.getOrNull(workout.currentIndex) ?: return
    var restSeconds by remember { mutableIntStateOf(0) }
    var restRunning by remember { mutableStateOf(false) }
    var showFinishDialog by remember { mutableStateOf(false) }
    var showCancelDialog by remember { mutableStateOf(false) }
    var showWhy by remember { mutableStateOf(false) }
    val chipState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    // Pausenuhr
    LaunchedEffect(restRunning) {
        while (restRunning && restSeconds > 0) {
            delay(1000)
            restSeconds--
        }
        if (restSeconds <= 0) restRunning = false
    }

    LaunchedEffect(workout.currentIndex) {
        showWhy = false
        chipState.animateScrollToItem(workout.currentIndex.coerceAtMost(workout.entries.size - 1))
    }

    Column(Modifier.fillMaxSize()) {

        // ---------- Kopfzeile ----------
        Column(
            Modifier
                .fillMaxWidth()
                .background(Gradients.hero)
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { showCancelDialog = true }) {
                    Icon(Icons.Filled.Close, "Abbrechen", tint = Palette.TextMid)
                }
                Column(Modifier.weight(1f)) {
                    Text(
                        workout.day.title,
                        style = MaterialTheme.typography.titleLarge,
                        color = Palette.TextHigh
                    )
                    Text(
                        "${workout.doneSets} von ${workout.totalSets} Sätzen",
                        style = MaterialTheme.typography.bodySmall,
                        color = Palette.Sky
                    )
                }
                TextButton(onClick = { showFinishDialog = true }) {
                    Text("Fertig", color = Palette.Amber, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(Modifier.height(10.dp))
            BarMeter(progress = workout.progress, height = 6.dp)
        }

        // ---------- Übungsauswahl ----------
        LazyRow(
            state = chipState,
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            itemsIndexed(workout.entries) { i, e ->
                val selected = i == workout.currentIndex
                val bg by animateColorAsState(
                    if (selected) Palette.Sky else Palette.Surface,
                    label = "chipbg"
                )
                Row(
                    Modifier
                        .clip(CircleShape)
                        .background(bg)
                        .border(
                            1.dp,
                            if (selected) Color.Transparent else Palette.Outline,
                            CircleShape
                        )
                        .clickable { onSelectExercise(i) }
                        .padding(horizontal = 14.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (e.allDone) {
                        Icon(
                            Icons.Filled.Check, null,
                            tint = if (selected) Palette.Ink else Palette.Emerald,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(Modifier.width(5.dp))
                    }
                    Text(
                        e.exercise.name,
                        style = MaterialTheme.typography.labelLarge,
                        color = if (selected) Palette.Ink else Palette.TextMid
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        "${e.doneCount}/${e.sets.size}",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (selected) Palette.Ink.copy(alpha = 0.65f) else Palette.TextLow
                    )
                }
            }
        }

        // ---------- Aktuelle Übung ----------
        LazyColumn(
            Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                val accent = when (entry.suggestion.advice) {
                    Advice.INCREASE -> Palette.Amber
                    Advice.DELOAD -> Palette.Emerald
                    Advice.BACKOFF -> Palette.Rose
                    else -> Palette.Sky
                }
                FfCard(accent = accent) {
                    Row(verticalAlignment = Alignment.Top) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                entry.exercise.name,
                                style = MaterialTheme.typography.headlineSmall,
                                color = Palette.TextHigh
                            )
                            Text(
                                entry.exercise.muscles.joinToString(" · ") { it.label },
                                style = MaterialTheme.typography.bodySmall,
                                color = Palette.TextLow
                            )
                        }
                        when (entry.suggestion.advice) {
                            Advice.INCREASE -> Pill("Steigern", Palette.Amber, icon = Icons.Filled.ArrowUpward)
                            Advice.DELOAD -> Pill("Entlastung", Palette.Emerald)
                            Advice.BACKOFF -> Pill("Zurück", Palette.Rose)
                            Advice.START -> Pill("Einstieg", Palette.Violet)
                            Advice.HOLD -> Pill("Halten", Palette.Sky)
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    Row(verticalAlignment = Alignment.Bottom) {
                        if (entry.suggestion.previousHeadline != null &&
                            entry.suggestion.advice == Advice.INCREASE
                        ) {
                            Text(
                                entry.suggestion.previousHeadline!!,
                                style = MaterialTheme.typography.titleLarge,
                                color = Palette.TextLow
                            )
                            Text(
                                "  →  ",
                                style = MaterialTheme.typography.titleLarge,
                                color = Palette.TextLow
                            )
                        }
                        Text(
                            entry.suggestion.headline,
                            style = MaterialTheme.typography.displayMedium,
                            color = accent
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(
                            "× ${entry.sets.size} Sätze",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Palette.TextMid,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                    }

                    Spacer(Modifier.height(10.dp))
                    Text(
                        entry.suggestion.reason,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Palette.TextMid
                    )

                    Spacer(Modifier.height(12.dp))
                    Row(
                        Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { showWhy = !showWhy }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.Info, null, tint = Palette.Sky, modifier = Modifier.size(15.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(
                            "Ausführung & Warum",
                            style = MaterialTheme.typography.labelLarge,
                            color = Palette.Sky
                        )
                        Spacer(Modifier.weight(1f))
                        Icon(
                            Icons.Filled.ExpandMore, null,
                            tint = Palette.Sky,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    AnimatedVisibility(
                        visible = showWhy,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Column(Modifier.padding(top = 8.dp)) {
                            InfoBlock("So geht's", entry.exercise.cue)
                            Spacer(Modifier.height(10.dp))
                            InfoBlock("Warum am Steig", entry.exercise.why)
                        }
                    }
                }
            }

            item { SectionTitle("Sätze", "Pause ${entry.exercise.restSec} s") }

            itemsIndexed(entry.sets) { setIndex, set ->
                SetRow(
                    index = setIndex,
                    set = set,
                    entry = entry,
                    onChange = { block -> onUpdateSet(workout.currentIndex, setIndex, block) },
                    onToggleDone = {
                        onToggleDone(workout.currentIndex, setIndex)
                        if (!set.done) {
                            restSeconds = entry.exercise.restSec
                            restRunning = true
                        }
                    },
                    onCopyDown = { onApplyToRemaining(workout.currentIndex, setIndex) }
                )
            }

            item {
                if (workout.currentIndex < workout.entries.size - 1) {
                    Spacer(Modifier.height(6.dp))
                    OutlinedButton(
                        onClick = { onSelectExercise(workout.currentIndex + 1) },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(15.dp)
                    ) {
                        Text("Nächste Übung: ${workout.entries[workout.currentIndex + 1].exercise.name}")
                    }
                } else {
                    Button(
                        onClick = { showFinishDialog = true },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Palette.Emerald,
                            contentColor = Palette.Ink
                        )
                    ) {
                        Text("Einheit abschließen", fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(Modifier.height(100.dp))
            }
        }
    }

    // ---------- Pausenuhr ----------
    if (restSeconds > 0) {
        RestTimerBar(
            seconds = restSeconds,
            total = entry.exercise.restSec,
            running = restRunning,
            onToggle = { restRunning = !restRunning },
            onSkip = { restSeconds = 0; restRunning = false },
            onAdd = { restSeconds += 30 }
        )
    }

    // ---------- Dialoge ----------
    if (showFinishDialog) {
        var note by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showFinishDialog = false },
            title = { Text("Einheit abschließen?") },
            text = {
                Column {
                    Text(
                        "${workout.doneSets} von ${workout.totalSets} Sätzen sind abgehakt. " +
                            "Nicht abgehakte Sätze werden nicht gespeichert.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(Modifier.height(14.dp))
                    OutlinedTextField(
                        value = note,
                        onValueChange = { note = it },
                        label = { Text("Notiz (optional)") },
                        placeholder = { Text("z. B. Schulter hat gezwickt") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showFinishDialog = false; onFinish(note) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Palette.Emerald,
                        contentColor = Palette.Ink
                    )
                ) { Text("Speichern") }
            },
            dismissButton = {
                TextButton(onClick = { showFinishDialog = false }) { Text("Weiter trainieren") }
            }
        )
    }

    if (showCancelDialog) {
        AlertDialog(
            onDismissRequest = { showCancelDialog = false },
            title = { Text("Training verwerfen?") },
            text = { Text("Die bisher abgehakten Sätze gehen dabei verloren.") },
            confirmButton = {
                TextButton(onClick = { showCancelDialog = false; onCancel() }) {
                    Text("Verwerfen", color = Palette.Rose)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCancelDialog = false }) { Text("Zurück") }
            }
        )
    }
}

@Composable
private fun InfoBlock(title: String, body: String) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Palette.SurfaceHigh)
            .padding(13.dp)
    ) {
        Text(title.uppercase(), style = MaterialTheme.typography.labelSmall, color = Palette.Sky)
        Spacer(Modifier.height(5.dp))
        Text(body, style = MaterialTheme.typography.bodyMedium, color = Palette.TextMid)
    }
}

/** Eine Satzzeile mit Steppern — auf dem Handy schneller als Tastatureingabe. */
@Composable
private fun SetRow(
    index: Int,
    set: ActiveSet,
    entry: ActiveEntry,
    onChange: ((ActiveSet) -> ActiveSet) -> Unit,
    onToggleDone: () -> Unit,
    onCopyDown: () -> Unit
) {
    val isTime = entry.exercise.progression == ProgressionKind.TIME
    val usesWeight = entry.exercise.progression != ProgressionKind.TIME
    val shape = RoundedCornerShape(18.dp)
    val bg by animateColorAsState(
        if (set.done) Palette.Emerald.copy(alpha = 0.10f) else Palette.Surface,
        label = "setbg"
    )

    Column(
        Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(bg)
            .border(
                1.dp,
                if (set.done) Palette.Emerald.copy(alpha = 0.4f) else Palette.Outline.copy(alpha = 0.5f),
                shape
            )
            .padding(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(if (set.done) Palette.Emerald else Palette.SurfaceHigh),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "${index + 1}",
                    style = MaterialTheme.typography.labelLarge,
                    color = if (set.done) Palette.Ink else Palette.TextMid
                )
            }
            Spacer(Modifier.width(12.dp))
            Text(
                if (isTime) "Halten" else "Satz ${index + 1}",
                style = MaterialTheme.typography.titleMedium,
                color = Palette.TextHigh,
                modifier = Modifier.weight(1f)
            )
            if (set.done) {
                IconButton(onClick = onCopyDown, modifier = Modifier.size(34.dp)) {
                    Icon(
                        Icons.Filled.ContentCopy, "Auf restliche Sätze übernehmen",
                        tint = Palette.TextLow, modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(Modifier.width(4.dp))
            }
            Box(
                Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(if (set.done) Palette.Emerald else Palette.SurfaceHigh)
                    .clickable { onToggleDone() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.Check, "Erledigt",
                    tint = if (set.done) Palette.Ink else Palette.TextLow,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            if (usesWeight) {
                Stepper(
                    // Bei Körpergewichtsübungen ist die Last ein Zusatz, kein Gesamtgewicht.
                    label = if (entry.exercise.progression == ProgressionKind.REPS) "Zusatz" else "Gewicht",
                    value = fmtKg(set.weightKg),
                    onMinus = {
                        onChange { it.copy(weightKg = (it.weightKg - entry.exercise.increment).coerceAtLeast(0.0)) }
                    },
                    onPlus = { onChange { it.copy(weightKg = it.weightKg + entry.exercise.increment) } },
                    modifier = Modifier.weight(1f)
                )
                Stepper(
                    label = "Wdh.",
                    value = "${set.reps}",
                    onMinus = { onChange { it.copy(reps = (it.reps - 1).coerceAtLeast(0)) } },
                    onPlus = { onChange { it.copy(reps = it.reps + 1) } },
                    modifier = Modifier.weight(1f)
                )
            } else {
                Stepper(
                    label = "Sekunden",
                    value = "${set.seconds} s",
                    onMinus = { onChange { it.copy(seconds = (it.seconds - 5).coerceAtLeast(0)) } },
                    onPlus = { onChange { it.copy(seconds = it.seconds + 5) } },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun Stepper(
    label: String,
    value: String,
    onMinus: () -> Unit,
    onPlus: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier) {
        Text(label.uppercase(), style = MaterialTheme.typography.labelSmall, color = Palette.TextLow)
        Spacer(Modifier.height(5.dp))
        Row(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(13.dp))
                .background(Palette.SurfaceHigh),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onMinus, modifier = Modifier.size(40.dp)) {
                Icon(Icons.Filled.Remove, "Weniger", tint = Palette.TextMid, modifier = Modifier.size(17.dp))
            }
            Text(
                value,
                style = MaterialTheme.typography.titleMedium,
                color = Palette.TextHigh,
                modifier = Modifier.weight(1f),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            IconButton(onClick = onPlus, modifier = Modifier.size(40.dp)) {
                Icon(Icons.Filled.Add, "Mehr", tint = Palette.Sky, modifier = Modifier.size(17.dp))
            }
        }
    }
}

/** Pausenuhr, die von unten einschwebt und den Rest der Oberfläche freilässt. */
@Composable
private fun RestTimerBar(
    seconds: Int,
    total: Int,
    running: Boolean,
    onToggle: () -> Unit,
    onSkip: () -> Unit,
    onAdd: () -> Unit
) {
    val progress by animateFloatAsState(
        if (total > 0) seconds.toFloat() / total else 0f,
        tween(400), label = "rest"
    )
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
        Column(
            Modifier
                .padding(16.dp)
                .navigationBarsPadding()
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(Palette.SurfaceHigh)
                .border(1.dp, Palette.Sky.copy(alpha = 0.35f), RoundedCornerShape(20.dp))
                .padding(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Pause", style = MaterialTheme.typography.labelSmall, color = Palette.TextLow)
                    Text(
                        "${seconds / 60}:${(seconds % 60).toString().padStart(2, '0')}",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Palette.Sky
                    )
                }
                TextButton(onClick = onAdd) { Text("+30 s", color = Palette.TextMid) }
                IconButton(onClick = onToggle) {
                    Icon(
                        if (running) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        if (running) "Anhalten" else "Weiter",
                        tint = Palette.TextHigh
                    )
                }
                TextButton(onClick = onSkip) { Text("Fertig", color = Palette.Amber) }
            }
            Spacer(Modifier.height(8.dp))
            BarMeter(progress = progress, height = 5.dp)
        }
    }
}
