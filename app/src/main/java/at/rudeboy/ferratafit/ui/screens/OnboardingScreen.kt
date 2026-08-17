package at.rudeboy.ferratafit.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Terrain
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import at.rudeboy.ferratafit.data.Station
import at.rudeboy.ferratafit.ui.Gradients
import at.rudeboy.ferratafit.ui.Palette
import java.util.Calendar

/**
 * Ersteinrichtung in drei Schritten. Die Ausstattung ist der wichtigste Punkt —
 * daraus leitet sich ab, welche Übungen überhaupt im Plan landen.
 */
@Composable
fun OnboardingScreen(
    onDone: (Set<Station>, Double, Double, Long?, String) -> Unit
) {
    var step by remember { mutableIntStateOf(0) }
    var stations by remember {
        mutableStateOf(
            setOf(
                Station.LAT_PULLDOWN, Station.CHEST_PRESS, Station.BUTTERFLY,
                Station.LEG_EXTENSION, Station.LEG_CURL, Station.PULLUP_BAR
            )
        )
    }
    var bodyweight by remember { mutableStateOf("78") }
    var plateStep by remember { mutableStateOf("5") }
    var targetName by remember { mutableStateOf("") }
    var weeksAhead by remember { mutableIntStateOf(12) }
    var hasTarget by remember { mutableStateOf(true) }

    Box(
        Modifier
            .fillMaxSize()
            .background(Gradients.hero)
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(horizontal = 22.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(Modifier.height(28.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Gradients.sky),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Terrain, null, tint = Palette.Ink, modifier = Modifier.size(24.dp))
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text("FerrataFit", style = MaterialTheme.typography.headlineMedium, color = Palette.TextHigh)
                    Text(
                        "Kraft für den Klettersteig",
                        style = MaterialTheme.typography.bodySmall,
                        color = Palette.Sky
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // Schrittanzeige
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                repeat(3) { i ->
                    Box(
                        Modifier
                            .weight(1f)
                            .height(4.dp)
                            .clip(CircleShape)
                            .background(if (i <= step) Palette.Sky else Palette.Outline)
                    )
                }
            }

            Spacer(Modifier.height(26.dp))

            AnimatedVisibility(
                visible = step == 0,
                enter = fadeIn() + slideInHorizontally { it / 3 },
                exit = fadeOut() + slideOutHorizontally { -it / 3 }
            ) {
                Column {
                    StepHeader(
                        "Was steht bei dir?",
                        "Hak ab, was dein Multifunktionsgerät hat. Die App plant nur Übungen ein, " +
                            "die du damit auch wirklich ausführen kannst."
                    )
                    Spacer(Modifier.height(16.dp))
                    Station.entries.filter { it != Station.BODYWEIGHT }.forEach { st ->
                        StationRow(
                            station = st,
                            checked = st in stations,
                            onToggle = {
                                stations = if (st in stations) stations - st else stations + st
                            }
                        )
                        Spacer(Modifier.height(8.dp))
                    }
                }
            }

            AnimatedVisibility(
                visible = step == 1,
                enter = fadeIn() + slideInHorizontally { it / 3 },
                exit = fadeOut() + slideOutHorizontally { -it / 3 }
            ) {
                Column {
                    StepHeader(
                        "Zwei Zahlen",
                        "Aus dem Körpergewicht schätzt die App deine Startlasten. Die Gewichtsstufe " +
                            "ist der Sprung von einer Steckplatte zur nächsten — an den meisten " +
                            "Kraftstationen sind das 5 kg."
                    )
                    Spacer(Modifier.height(20.dp))
                    OutlinedTextField(
                        value = bodyweight,
                        onValueChange = { bodyweight = it.filter { c -> c.isDigit() || c == '.' } },
                        label = { Text("Körpergewicht in kg") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(14.dp))
                    OutlinedTextField(
                        value = plateStep,
                        onValueChange = { plateStep = it.filter { c -> c.isDigit() || c == '.' } },
                        label = { Text("Gewichtsstufe je Platte in kg") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(10.dp))
                    Text(
                        "Steht auf den Platten meist aufgedruckt. Wenn nicht: 5 kg ist ein guter Richtwert.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Palette.TextLow
                    )
                }
            }

            AnimatedVisibility(
                visible = step == 2,
                enter = fadeIn() + slideInHorizontally { it / 3 },
                exit = fadeOut() + slideOutHorizontally { -it / 3 }
            ) {
                Column {
                    StepHeader(
                        "Dein Ziel",
                        "Ein konkretes Datum macht den Unterschied. Für eine anspruchsvollere Tour " +
                            "gelten 10 bis 12 Wochen Vorlauf als sinnvoll."
                    )
                    Spacer(Modifier.height(18.dp))
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { hasTarget = !hasTarget }
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(checked = hasTarget, onCheckedChange = { hasTarget = it })
                        Spacer(Modifier.width(6.dp))
                        Text("Ich habe eine Tour im Blick", color = Palette.TextHigh)
                    }
                    if (hasTarget) {
                        Spacer(Modifier.height(10.dp))
                        OutlinedTextField(
                            value = targetName,
                            onValueChange = { targetName = it },
                            label = { Text("Welcher Klettersteig?") },
                            placeholder = { Text("z. B. Känzele oder Rüfikopf") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(20.dp))
                        Text(
                            "In $weeksAhead Wochen",
                            style = MaterialTheme.typography.headlineSmall,
                            color = Palette.Amber
                        )
                        Slider(
                            value = weeksAhead.toFloat(),
                            onValueChange = { weeksAhead = it.toInt() },
                            valueRange = 4f..40f,
                            colors = SliderDefaults.colors(
                                thumbColor = Palette.Amber,
                                activeTrackColor = Palette.Amber
                            )
                        )
                        Text(
                            when {
                                weeksAhead < 8 -> "Knapp, aber machbar — bleib konsequent bei drei Einheiten."
                                weeksAhead <= 14 -> "Guter Rahmen. Genau dafür ist der Plan gebaut."
                                else -> "Viel Zeit — du wirst mehrere Steigerungsblöcke durchlaufen."
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = Palette.TextLow
                        )
                    }
                }
            }

            Spacer(Modifier.height(30.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                if (step > 0) {
                    OutlinedButton(onClick = { step-- }, modifier = Modifier.weight(1f)) {
                        Text("Zurück")
                    }
                }
                Button(
                    onClick = {
                        if (step < 2) step++
                        else {
                            val target = if (hasTarget) {
                                Calendar.getInstance().apply {
                                    add(Calendar.WEEK_OF_YEAR, weeksAhead)
                                }.timeInMillis
                            } else null
                            onDone(
                                stations,
                                bodyweight.toDoubleOrNull() ?: 78.0,
                                plateStep.toDoubleOrNull() ?: 5.0,
                                target,
                                targetName.trim()
                            )
                        }
                    },
                    enabled = step != 0 || stations.isNotEmpty(),
                    modifier = Modifier.weight(if (step > 0) 1.4f else 1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Palette.Sky, contentColor = Palette.Ink)
                ) {
                    Text(if (step < 2) "Weiter" else "Los geht's", fontWeight = FontWeight.Bold)
                }
            }

            Spacer(Modifier.height(30.dp))
        }
    }
}

@Composable
private fun StepHeader(title: String, body: String) {
    Column {
        Text(title, style = MaterialTheme.typography.headlineLarge, color = Palette.TextHigh)
        Spacer(Modifier.height(8.dp))
        Text(body, style = MaterialTheme.typography.bodyMedium, color = Palette.TextMid)
    }
}

@Composable
private fun StationRow(station: Station, checked: Boolean, onToggle: () -> Unit) {
    val shape = RoundedCornerShape(16.dp)
    Row(
        Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(if (checked) Palette.Sky.copy(alpha = 0.12f) else Palette.Surface)
            .border(1.dp, if (checked) Palette.Sky.copy(alpha = 0.5f) else Palette.Outline, shape)
            .clickable { onToggle() }
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(22.dp)
                .clip(RoundedCornerShape(7.dp))
                .background(if (checked) Palette.Sky else Palette.Outline),
            contentAlignment = Alignment.Center
        ) {
            if (checked) Icon(Icons.Filled.Check, null, tint = Palette.Ink, modifier = Modifier.size(15.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(station.label, style = MaterialTheme.typography.titleMedium, color = Palette.TextHigh)
            Text(station.hint, style = MaterialTheme.typography.bodySmall, color = Palette.TextLow)
        }
    }
}
