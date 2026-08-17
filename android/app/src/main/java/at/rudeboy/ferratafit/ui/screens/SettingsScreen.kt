package at.rudeboy.ferratafit.ui.screens

import android.content.Context
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.health.connect.client.PermissionController
import at.rudeboy.ferratafit.data.AppState
import at.rudeboy.ferratafit.data.Profile
import at.rudeboy.ferratafit.data.Station
import at.rudeboy.ferratafit.health.HealthBridge
import at.rudeboy.ferratafit.ui.*
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    state: AppState,
    health: HealthBridge,
    onUpdateProfile: ((Profile) -> Profile) -> Unit,
    onToggleStation: (Station) -> Unit,
    onSetHealthEnabled: (Boolean) -> Unit,
    onSyncAll: () -> Unit,
    onRestartCycle: () -> Unit,
    onExport: () -> String,
    onImport: (String) -> Unit,
    onNotify: (String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var availability by remember { mutableStateOf(health.availability()) }
    var granted by remember { mutableStateOf(false) }
    var checking by remember { mutableStateOf(true) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = PermissionController.createRequestPermissionResultContract()
    ) { result ->
        granted = result.containsAll(HealthBridge.PERMISSIONS)
        onSetHealthEnabled(granted)
        onNotify(
            if (granted) "Samsung Health ist verbunden. Neue Einheiten werden automatisch übergeben."
            else "Ohne Freigabe kann nichts übertragen werden."
        )
    }

    LaunchedEffect(Unit) {
        availability = health.availability()
        granted = health.hasPermissions()
        checking = false
        if (granted && !state.profile.healthConnectEnabled) onSetHealthEnabled(true)
    }

    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Column(Modifier.padding(top = 6.dp)) {
                Text("Einstellungen", style = MaterialTheme.typography.headlineLarge, color = Palette.TextHigh)
            }
        }

        // ---------------- Samsung Health ----------------
        item { SectionTitle("Samsung Health") }
        item {
            FfCard(accent = if (granted) Palette.Emerald else Palette.Sky) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(13.dp))
                            .background(
                                if (granted) Palette.Emerald.copy(alpha = 0.16f)
                                else Palette.Sky.copy(alpha = 0.14f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            if (granted) Icons.Filled.Check else Icons.Filled.FavoriteBorder,
                            null,
                            tint = if (granted) Palette.Emerald else Palette.Sky,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(Modifier.width(13.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            when {
                                checking -> "Wird geprüft…"
                                granted -> "Verbunden"
                                availability == HealthBridge.Availability.READY -> "Nicht freigegeben"
                                availability == HealthBridge.Availability.NEEDS_UPDATE -> "Health Connect veraltet"
                                else -> "Health Connect fehlt"
                            },
                            style = MaterialTheme.typography.titleLarge,
                            color = Palette.TextHigh
                        )
                        Text(
                            "über Health Connect",
                            style = MaterialTheme.typography.bodySmall,
                            color = Palette.TextLow
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))
                Text(
                    "Samsung Health tauscht Training, Schritte und Puls über Health Connect aus. " +
                        "Gibst du das hier frei, landet jede abgeschlossene Einheit als Krafttraining " +
                        "in Samsung Health. Der Abgleich läuft nicht sofort, sondern in der Regel " +
                        "innerhalb einer Stunde.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Palette.TextMid
                )

                Spacer(Modifier.height(14.dp))

                when (availability) {
                    HealthBridge.Availability.READY -> {
                        if (granted) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(
                                    onClick = onSyncAll,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Filled.Sync, null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(7.dp))
                                    Text("Alles übertragen")
                                }
                                OutlinedButton(
                                    onClick = { safeStart(context, health.settingsIntent(), onNotify) },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Filled.OpenInNew, null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(7.dp))
                                    Text("Freigaben")
                                }
                            }
                        } else {
                            Button(
                                onClick = { permissionLauncher.launch(HealthBridge.PERMISSIONS) },
                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                shape = RoundedCornerShape(15.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Palette.Sky,
                                    contentColor = Palette.Ink
                                )
                            ) {
                                Text("Mit Samsung Health verbinden", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    else -> {
                        Button(
                            onClick = { safeStart(context, health.installIntent(), onNotify) },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(15.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Palette.Amber,
                                contentColor = Palette.Ink
                            )
                        ) {
                            Text(
                                if (availability == HealthBridge.Availability.NEEDS_UPDATE)
                                    "Health Connect aktualisieren"
                                else "Health Connect installieren",
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Ab Android 14 ist Health Connect fest im System eingebaut. Auf älteren " +
                                "Geräten wird es als App aus dem Play Store nachinstalliert.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Palette.TextLow
                        )
                    }
                }
            }
        }

        // ---------------- Profil ----------------
        item { SectionTitle("Profil") }
        item {
            FfCard {
                var bw by remember(state.profile.bodyweightKg) {
                    mutableStateOf(state.profile.bodyweightKg.toInt().toString())
                }
                var step by remember(state.profile.plateStepKg) {
                    mutableStateOf(
                        state.profile.plateStepKg.let {
                            if (it % 1.0 == 0.0) it.toInt().toString() else it.toString()
                        }
                    )
                }
                OutlinedTextField(
                    value = bw,
                    onValueChange = {
                        bw = it.filter { c -> c.isDigit() || c == '.' }
                        bw.toDoubleOrNull()?.let { v -> onUpdateProfile { p -> p.copy(bodyweightKg = v) } }
                    },
                    label = { Text("Körpergewicht in kg") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = step,
                    onValueChange = {
                        step = it.filter { c -> c.isDigit() || c == '.' }
                        step.toDoubleOrNull()?.let { v ->
                            if (v > 0) onUpdateProfile { p -> p.copy(plateStepKg = v) }
                        }
                    },
                    label = { Text("Gewichtsstufe je Platte in kg") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
                var target by remember(state.profile.targetFerrataName) {
                    mutableStateOf(state.profile.targetFerrataName)
                }
                OutlinedTextField(
                    value = target,
                    onValueChange = {
                        target = it
                        onUpdateProfile { p -> p.copy(targetFerrataName = it) }
                    },
                    label = { Text("Ziel-Klettersteig") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // ---------------- Ausstattung ----------------
        item { SectionTitle("Ausstattung") }
        item {
            FfCard(padding = 8.dp) {
                Station.entries.filter { it != Station.BODYWEIGHT }.forEach { st ->
                    val on = st in state.profile.stations
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onToggleStation(st) }
                            .padding(horizontal = 10.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                st.label,
                                style = MaterialTheme.typography.titleMedium,
                                color = if (on) Palette.TextHigh else Palette.TextLow
                            )
                            Text(
                                st.hint,
                                style = MaterialTheme.typography.bodySmall,
                                color = Palette.TextLow
                            )
                        }
                        Switch(
                            checked = on,
                            onCheckedChange = { onToggleStation(st) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Palette.Ink,
                                checkedTrackColor = Palette.Sky
                            )
                        )
                    }
                }
            }
        }

        // ---------------- Trainingsblock ----------------
        item { SectionTitle("Trainingsblock") }
        item {
            FfCard {
                Text(
                    "Die App arbeitet in Blöcken zu ${at.rudeboy.ferratafit.data.Progression.CYCLE_WEEKS} " +
                        "Wochen: vier Wochen steigern, eine Woche entlasten. Nach einer längeren " +
                        "Pause ist es sinnvoll, den Block neu zu starten — dann beginnst du wieder " +
                        "mit reichlich Puffer statt an der Belastungsspitze.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Palette.TextMid
                )
                Spacer(Modifier.height(14.dp))
                OutlinedButton(onClick = onRestartCycle, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Filled.Refresh, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Block neu starten")
                }
            }
        }

        // ---------------- Sicherung ----------------
        item { SectionTitle("Sicherung") }
        item {
            var importText by remember { mutableStateOf("") }
            var showImport by remember { mutableStateOf(false) }
            FfCard {
                Text(
                    "Deine Daten liegen ausschließlich auf dem Gerät. Vor einem Handywechsel " +
                        "exportierst du sie am besten einmal.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Palette.TextMid
                )
                Spacer(Modifier.height(14.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = {
                            val json = onExport()
                            val send = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, "FerrataFit Sicherung")
                                putExtra(Intent.EXTRA_TEXT, json)
                            }
                            safeStart(context, Intent.createChooser(send, "Sicherung teilen"), onNotify)
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Filled.Backup, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(7.dp))
                        Text("Export")
                    }
                    OutlinedButton(onClick = { showImport = true }, modifier = Modifier.weight(1f)) {
                        Text("Import")
                    }
                }
            }
            if (showImport) {
                AlertDialog(
                    onDismissRequest = { showImport = false },
                    title = { Text("Sicherung einlesen") },
                    text = {
                        Column {
                            Text(
                                "Der aktuelle Bestand wird dabei vollständig ersetzt.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Palette.Rose
                            )
                            Spacer(Modifier.height(10.dp))
                            OutlinedTextField(
                                value = importText,
                                onValueChange = { importText = it },
                                label = { Text("JSON einfügen") },
                                modifier = Modifier.fillMaxWidth().height(160.dp)
                            )
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = {
                            onImport(importText)
                            showImport = false
                        }) { Text("Einlesen") }
                    },
                    dismissButton = {
                        TextButton(onClick = { showImport = false }) { Text("Abbrechen") }
                    }
                )
            }
        }

        // ---------------- Info ----------------
        item {
            FfCard {
                Text("Wie die App steigert", style = MaterialTheme.typography.titleLarge, color = Palette.TextHigh)
                Spacer(Modifier.height(8.dp))
                Text(
                    "Gesteigert wird nach der Doppelten Progression mit der 2-für-2-Regel: " +
                        "Zuerst arbeitest du dich im Wiederholungsfenster nach oben. Erst wenn " +
                        "du das obere Ende in zwei Einheiten hintereinander erreichst, kommt " +
                        "Gewicht drauf — und die Wiederholungen fallen wieder auf den unteren Wert.\n\n" +
                        "Bleibt der Fortschritt über drei Einheiten aus, nimmt die App bewusst " +
                        "etwa zehn Prozent zurück. Das löst eine Blockade zuverlässiger, als " +
                        "immer weiter dagegen zu drücken.\n\n" +
                        "Bei Halteübungen wie dem Dead Hang kommen fünf Sekunden dazu. Ab einer " +
                        "Minute lohnt Zusatzgewicht mehr als noch längeres Hängen.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Palette.TextMid
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    "FerrataFit · Version 1.2 · Alle Daten bleiben auf dem Gerät",
                    style = MaterialTheme.typography.labelSmall,
                    color = Palette.TextLow
                )
            }
        }

        item { Spacer(Modifier.height(80.dp)) }
    }
}

/** Startet eine Absicht und meldet sich, statt abzustürzen, wenn nichts sie annehmen kann. */
private fun safeStart(context: Context, intent: Intent, onNotify: (String) -> Unit) {
    try {
        context.startActivity(intent)
    } catch (_: Exception) {
        onNotify("Dafür ist auf diesem Gerät keine passende App vorhanden.")
    }
}
