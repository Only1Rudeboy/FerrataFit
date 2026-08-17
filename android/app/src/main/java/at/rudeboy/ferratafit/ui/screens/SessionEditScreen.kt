package at.rudeboy.ferratafit.ui.screens

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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import at.rudeboy.ferratafit.data.Catalog
import at.rudeboy.ferratafit.data.ProgressionKind
import at.rudeboy.ferratafit.data.Session
import at.rudeboy.ferratafit.data.SetLog
import at.rudeboy.ferratafit.data.fmtKg
import at.rudeboy.ferratafit.ui.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Eine gespeicherte Einheit nachbearbeiten.
 *
 * Ohne diesen Weg bliebe ein Tippfehler dauerhaft: Trägt man beim Curl 50 statt 15 kg ein,
 * rechnet die Progression für immer damit und schlägt beim nächsten Mal 55 kg vor.
 *
 * Der Zeitpunkt der Einheit lässt sich bewusst nicht ändern — die Progressionslogik
 * sortiert die Historie danach, und eine verschobene Einheit würde die Reihenfolge und
 * damit die 2-für-2-Regel durcheinanderbringen.
 */
@Composable
fun SessionEditScreen(
    session: Session,
    onEditSet: (Int, (SetLog) -> SetLog) -> Unit,
    onRemoveSet: (Int) -> Unit,
    onSave: () -> Unit,
    onDelete: () -> Unit,
    onCancel: () -> Unit
) {
    var askDelete by remember { mutableStateOf(false) }
    val day = Catalog.days.firstOrNull { it.id == session.dayId }
    val dateText = remember(session.startedAt) {
        SimpleDateFormat("EEEE, d. MMMM yyyy · HH:mm", Locale.GERMAN).format(Date(session.startedAt))
    }

    Column(Modifier.fillMaxSize()) {

        Column(
            Modifier
                .fillMaxWidth()
                .background(Gradients.hero)
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onCancel) {
                    Icon(Icons.Filled.Close, "Verwerfen", tint = Palette.TextMid)
                }
                Column(Modifier.weight(1f)) {
                    Text(
                        day?.title ?: "Einheit",
                        style = MaterialTheme.typography.titleLarge,
                        color = Palette.TextHigh
                    )
                    Text(dateText, style = MaterialTheme.typography.bodySmall, color = Palette.Sky)
                }
                TextButton(onClick = onSave) {
                    Text("Sichern", color = Palette.Amber, fontWeight = FontWeight.Bold)
                }
            }
        }

        LazyColumn(
            Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                FfCard(accent = Palette.Sky) {
                    Text(
                        "Korrigiere hier, was falsch eingetragen wurde. Die Vorschläge für die " +
                            "nächste Einheit rechnen sofort mit den neuen Werten.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Palette.TextMid
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Datum und Uhrzeit bleiben fest — die Reihenfolge der Einheiten " +
                            "entscheidet über die Steigerung.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Palette.TextLow
                    )
                }
            }

            item { SectionTitle("Sätze", "${session.sets.size}") }

            itemsIndexed(session.sets) { i, set ->
                val ex = Catalog.byId(set.exerciseId)
                val isTime = ex?.progression == ProgressionKind.TIME
                val step = ex?.increment ?: 2.5

                FfCard(padding = 14.dp) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(Palette.SurfaceHigh),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "${set.setIndex + 1}",
                                style = MaterialTheme.typography.labelLarge,
                                color = Palette.TextMid
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                ex?.name ?: set.exerciseId,
                                style = MaterialTheme.typography.titleMedium,
                                color = Palette.TextHigh
                            )
                            Text(
                                if (isTime) "${set.seconds} s"
                                else "${fmtKg(set.weightKg)} × ${set.reps}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Palette.TextLow
                            )
                        }
                        IconButton(onClick = { onRemoveSet(i) }, modifier = Modifier.size(36.dp)) {
                            Icon(
                                Icons.Filled.Delete, "Satz entfernen",
                                tint = Palette.Rose.copy(alpha = 0.8f),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        if (isTime) {
                            EditStepper(
                                label = "Sekunden",
                                value = "${set.seconds} s",
                                onMinus = { onEditSet(i) { it.copy(seconds = (it.seconds - 5).coerceAtLeast(0)) } },
                                onPlus = { onEditSet(i) { it.copy(seconds = it.seconds + 5) } },
                                modifier = Modifier.weight(1f)
                            )
                        } else {
                            EditStepper(
                                label = if (ex?.progression == ProgressionKind.REPS) "Zusatz" else "Gewicht",
                                value = fmtKg(set.weightKg),
                                onMinus = { onEditSet(i) { it.copy(weightKg = (it.weightKg - step).coerceAtLeast(0.0)) } },
                                onPlus = { onEditSet(i) { it.copy(weightKg = it.weightKg + step) } },
                                modifier = Modifier.weight(1f)
                            )
                            EditStepper(
                                label = "Wdh.",
                                value = "${set.reps}",
                                onMinus = { onEditSet(i) { it.copy(reps = (it.reps - 1).coerceAtLeast(0)) } },
                                onPlus = { onEditSet(i) { it.copy(reps = it.reps + 1) } },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            item {
                Spacer(Modifier.height(6.dp))
                OutlinedButton(
                    onClick = { askDelete = true },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(15.dp)
                ) {
                    Icon(Icons.Filled.Delete, null, tint = Palette.Rose, modifier = Modifier.size(17.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Ganze Einheit löschen", color = Palette.Rose)
                }
                Spacer(Modifier.height(90.dp))
            }
        }
    }

    if (askDelete) {
        AlertDialog(
            onDismissRequest = { askDelete = false },
            title = { Text("Einheit löschen?") },
            text = {
                Text(
                    "Die Sätze verschwinden aus Verlauf und Bestleistungen, und die Vorschläge " +
                        "rechnen ohne sie weiter.\n\nDeine Höhenmeter und die gegangene Etappe " +
                        "bleiben erhalten — trainiert hast du ja trotzdem.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(onClick = { askDelete = false; onDelete() }) {
                    Text("Löschen", color = Palette.Rose)
                }
            },
            dismissButton = {
                TextButton(onClick = { askDelete = false }) { Text("Behalten") }
            }
        )
    }
}

@Composable
private fun EditStepper(
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
