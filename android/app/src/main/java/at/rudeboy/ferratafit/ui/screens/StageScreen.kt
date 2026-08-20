package at.rudeboy.ferratafit.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import at.rudeboy.ferratafit.ActiveStage
import at.rudeboy.ferratafit.data.Journey
import at.rudeboy.ferratafit.data.StageKind
import at.rudeboy.ferratafit.ui.*
import kotlinx.coroutines.delay

/**
 * Etappen ohne Gerät: Dehnen, Regeneration und Ausdauer.
 *
 * Bei Dehnetappen hakt man Übung für Übung ab und kann pro Position einen Timer
 * mitlaufen lassen. Ausdauer wird schlicht nachgetragen — die App misst nichts,
 * sie nimmt entgegen.
 */
@Composable
fun StageScreen(
    active: ActiveStage,
    onToggleDrill: (Int) -> Unit,
    onAdjust: (Int?, Int?) -> Unit,
    onFinish: () -> Unit,
    onSkip: () -> Unit,
    onCancel: () -> Unit
) {
    val stage = active.stage
    var showCancel by rememberSaveable { mutableStateOf(false) }
    var timerSeconds by remember { mutableIntStateOf(0) }
    var timerTotal by remember { mutableIntStateOf(0) }
    var timerRunning by remember { mutableStateOf(false) }

    LaunchedEffect(timerRunning) {
        while (timerRunning && timerSeconds > 0) {
            delay(1000)
            timerSeconds--
        }
        if (timerSeconds <= 0) timerRunning = false
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
                IconButton(onClick = { showCancel = true }) {
                    Icon(Icons.Filled.Close, "Verlassen", tint = Palette.TextMid)
                }
                Column(Modifier.weight(1f)) {
                    Text(stage.title, style = MaterialTheme.typography.titleLarge, color = Palette.TextHigh)
                    Text(
                        if (stage.kind == StageKind.ENDURANCE) stage.subtitle
                        else "${active.doneCount} von ${active.drills.size} erledigt",
                        style = MaterialTheme.typography.bodySmall,
                        color = Palette.Sky
                    )
                }
                TextButton(onClick = onFinish) {
                    Text("Fertig", color = Palette.Amber, fontWeight = FontWeight.Bold)
                }
            }
            if (stage.kind != StageKind.ENDURANCE) {
                Spacer(Modifier.height(10.dp))
                BarMeter(progress = active.progress, height = 6.dp, brush = Gradients.emerald)
            }
        }

        LazyColumn(
            Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (stage.kind == StageKind.ENDURANCE) {
                item {
                    FfCard(accent = Palette.Sky) {
                        Text(
                            stage.icon,
                            style = MaterialTheme.typography.displayMedium,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            stage.hint ?: stage.subtitle,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Palette.TextMid,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                item {
                    FfCard {
                        Text("Was hast du gemacht?", style = MaterialTheme.typography.titleLarge, color = Palette.TextHigh)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Trag ein, was zusammengekommen ist — grob reicht.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Palette.TextLow
                        )
                        Spacer(Modifier.height(16.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            NumberStepper(
                                label = "Minuten",
                                value = "${active.minutes}",
                                onMinus = { onAdjust(active.minutes - 5, null) },
                                onPlus = { onAdjust(active.minutes + 5, null) },
                                modifier = Modifier.weight(1f)
                            )
                            NumberStepper(
                                label = "Höhenmeter",
                                value = "${active.extraMeters}",
                                onMinus = { onAdjust(null, active.extraMeters - 50) },
                                onPlus = { onAdjust(null, active.extraMeters + 50) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Spacer(Modifier.height(14.dp))
                        Text(
                            "Deine Höhenmeter kommen zu den ${stage.meters} Hm der Etappe dazu. " +
                                "Flach unterwegs? Dann lass sie einfach auf null.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Palette.TextLow
                        )
                    }
                }
            } else {
                item {
                    FfCard(accent = Palette.Emerald) {
                        Text(
                            if (stage.longHold)
                                "Heute länger halten — 30 bis 90 Sekunden. Das beruhigt das Nervensystem " +
                                    "und unterstützt die Erholung."
                            else "Jede Position ruhig 20 bis 30 Sekunden halten und dabei weiteratmen.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Palette.TextMid
                        )
                    }
                }

                itemsIndexed(active.drills) { i, item ->
                    val drill = Journey.drill(item.id)
                    if (drill != null) {
                        val secs = Journey.holdSeconds(stage, drill)
                        DrillRow(
                            index = i,
                            name = drill.name,
                            zone = drill.zone,
                            seconds = secs,
                            perSide = drill.perSide,
                            cue = drill.cue,
                            why = drill.why,
                            steps = drill.steps,
                            mistakes = drill.mistakes,
                            video = drill.video,
                            done = item.done,
                            onToggle = { onToggleDrill(i) },
                            onTimer = {
                                timerTotal = if (drill.perSide) secs * 2 else secs
                                timerSeconds = timerTotal
                                timerRunning = true
                            }
                        )
                    }
                }
            }

            item {
                Button(
                    onClick = onFinish,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Palette.Emerald,
                        contentColor = Palette.Ink
                    )
                ) {
                    Text("Etappe abschließen", fontWeight = FontWeight.Bold)
                }
                TextButton(onClick = onSkip, modifier = Modifier.fillMaxWidth()) {
                    Text("Doch überspringen", color = Palette.TextLow)
                }
                Spacer(Modifier.height(90.dp))
            }
        }
    }

    // ---------- Timer ----------
    if (timerSeconds > 0) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
            Column(
                Modifier
                    .padding(16.dp)
                    .navigationBarsPadding()
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Palette.SurfaceHigh)
                    .border(1.dp, Palette.Emerald.copy(alpha = 0.35f), RoundedCornerShape(20.dp))
                    .padding(14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Halten", style = MaterialTheme.typography.labelSmall, color = Palette.TextLow)
                        Text(
                            "${timerSeconds / 60}:${(timerSeconds % 60).toString().padStart(2, '0')}",
                            style = MaterialTheme.typography.headlineMedium,
                            color = Palette.Emerald
                        )
                    }
                    TextButton(onClick = { timerSeconds = 0; timerRunning = false }) {
                        Text("Fertig", color = Palette.Amber)
                    }
                }
                Spacer(Modifier.height(8.dp))
                BarMeter(
                    progress = if (timerTotal > 0) timerSeconds.toFloat() / timerTotal else 0f,
                    height = 5.dp,
                    brush = Gradients.emerald
                )
            }
        }
    }

    if (showCancel) {
        AlertDialog(
            onDismissRequest = { showCancel = false },
            title = { Text("Etappe verlassen?") },
            text = { Text("Der Fortschritt in dieser Etappe geht dabei verloren.") },
            confirmButton = {
                TextButton(onClick = { showCancel = false; onCancel() }) {
                    Text("Verlassen", color = Palette.Rose)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCancel = false }) { Text("Zurück") }
            }
        )
    }
}

@Composable
private fun DrillRow(
    index: Int,
    name: String,
    zone: String,
    seconds: Int,
    perSide: Boolean,
    cue: String,
    why: String,
    steps: List<String>,
    mistakes: List<String>,
    video: String,
    done: Boolean,
    onToggle: () -> Unit,
    onTimer: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val shape = RoundedCornerShape(18.dp)
    val bg by animateColorAsState(
        if (done) Palette.Emerald.copy(alpha = 0.10f) else Palette.Surface,
        label = "drillbg"
    )

    Column(
        Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(bg)
            .border(
                1.dp,
                if (done) Palette.Emerald.copy(alpha = 0.4f) else Palette.Outline.copy(alpha = 0.5f),
                shape
            )
            .padding(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(if (done) Palette.Emerald else Palette.SurfaceHigh),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "${index + 1}",
                    style = MaterialTheme.typography.labelLarge,
                    color = if (done) Palette.Ink else Palette.TextMid
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(name, style = MaterialTheme.typography.titleMedium, color = Palette.TextHigh)
                Text(
                    "$seconds s${if (perSide) " pro Seite" else ""} · $zone",
                    style = MaterialTheme.typography.bodySmall,
                    color = Palette.TextLow
                )
            }
            Box(
                Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(if (done) Palette.Emerald else Palette.SurfaceHigh)
                    .clickable { onToggle() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.Check, "Erledigt",
                    tint = if (done) Palette.Ink else Palette.TextLow,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(Modifier.height(10.dp))
        Row(
            Modifier
                .clip(RoundedCornerShape(12.dp))
                .clickable { expanded = !expanded }
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Filled.Info, null, tint = Palette.Sky, modifier = Modifier.size(15.dp))
            Spacer(Modifier.width(6.dp))
            Text("Wie geht die Übung?", style = MaterialTheme.typography.labelLarge, color = Palette.Sky)
            Spacer(Modifier.weight(1f))
            Icon(Icons.Filled.ExpandMore, null, tint = Palette.Sky, modifier = Modifier.size(18.dp))
        }
        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            ExerciseGuide(
                steps = steps,
                cue = cue,
                mistakes = mistakes,
                why = why,
                whyLabel = "Warum",
                video = video,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        if (!done) {
            Spacer(Modifier.height(10.dp))
            OutlinedButton(
                onClick = onTimer,
                modifier = Modifier.fillMaxWidth().height(44.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(Icons.Filled.Timer, null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(8.dp))
                Text("$seconds s Timer starten")
            }
        }
    }
}


/** Plus-Minus-Feld, wie im Trainingsbildschirm. */
@Composable
fun NumberStepper(
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
                textAlign = TextAlign.Center
            )
            IconButton(onClick = onPlus, modifier = Modifier.size(40.dp)) {
                Icon(Icons.Filled.Add, "Mehr", tint = Palette.Sky, modifier = Modifier.size(17.dp))
            }
        }
    }
}
