package at.rudeboy.ferratafit.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import at.rudeboy.ferratafit.update.Updater
import kotlinx.coroutines.launch
import java.io.File

/** Wo im Ablauf die Aktualisierung gerade steht. */
private sealed interface UiState {
    data object Idle : UiState
    data object Checking : UiState
    data class UpToDate(val version: String) : UiState
    data class Found(val info: Updater.Result.Available) : UiState
    data class Downloading(val info: Updater.Result.Available, val progress: Float) : UiState
    data class Ready(val info: Updater.Result.Available, val file: File) : UiState
    data class Failed(val reason: String) : UiState
}

/**
 * Aktualisierung direkt in der App.
 *
 * Die App kommt nicht aus dem Play Store, also gibt es keinen automatischen Weg für
 * Updates. Diese Karte fragt die Veröffentlichungen auf GitHub ab, lädt bei Bedarf die
 * neue Fassung und übergibt sie dem Paketinstallierer.
 */
@Composable
fun UpdateCard(onNotify: (String) -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var state by remember { mutableStateOf<UiState>(UiState.Idle) }
    var showNotes by remember { mutableStateOf(false) }
    val current = remember { Updater.currentVersion(context) }

    fun check() {
        state = UiState.Checking
        scope.launch {
            state = when (val r = Updater.check(context)) {
                is Updater.Result.UpToDate -> UiState.UpToDate(r.current)
                is Updater.Result.Available -> UiState.Found(r)
                is Updater.Result.Failed -> UiState.Failed(r.reason)
            }
        }
    }

    fun startDownload(info: Updater.Result.Available) {
        state = UiState.Downloading(info, 0f)
        scope.launch {
            val file = Updater.download(context, info.downloadUrl, info.sizeBytes) { p ->
                state = UiState.Downloading(info, p)
            }
            state = if (file != null) UiState.Ready(info, file)
            else UiState.Failed("Der Download ist fehlgeschlagen.")
        }
    }

    fun install(file: File) {
        if (!Updater.canInstall(context)) {
            onNotify("Android fragt jetzt nach der Erlaubnis, Updates zu installieren.")
            runCatching { context.startActivity(Updater.installPermissionIntent(context)) }
            return
        }
        if (!Updater.install(context, file)) {
            onNotify("Die Installation ließ sich nicht starten.", )
        }
    }

    val accent = when (state) {
        is UiState.Found, is UiState.Ready -> Palette.Amber
        is UiState.UpToDate -> Palette.Emerald
        is UiState.Failed -> Palette.Rose
        else -> Palette.Sky
    }

    FfCard(accent = accent) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(13.dp))
                    .background(accent.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    when (state) {
                        is UiState.UpToDate -> Icons.Filled.CheckCircle
                        is UiState.Failed -> Icons.Filled.ErrorOutline
                        is UiState.Found, is UiState.Ready -> Icons.Filled.CloudDownload
                        else -> Icons.Filled.SystemUpdate
                    },
                    null, tint = accent, modifier = Modifier.size(20.dp)
                )
            }
            Spacer(Modifier.width(13.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    when (val s = state) {
                        is UiState.Checking -> "Wird geprüft…"
                        is UiState.UpToDate -> "Alles aktuell"
                        is UiState.Found -> "Version ${s.info.version} ist da"
                        is UiState.Downloading -> "Wird geladen…"
                        is UiState.Ready -> "Bereit zum Installieren"
                        is UiState.Failed -> "Prüfung fehlgeschlagen"
                        else -> "App-Aktualisierung"
                    },
                    style = MaterialTheme.typography.titleLarge,
                    color = Palette.TextHigh
                )
                Text(
                    "Installiert: Version $current",
                    style = MaterialTheme.typography.bodySmall,
                    color = Palette.TextLow
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        when (val s = state) {
            is UiState.Idle -> {
                Text(
                    "Die App kommt nicht aus dem Play Store, deshalb prüft sie selbst nach " +
                        "neuen Fassungen. Dafür wird nur die Veröffentlichungsseite auf GitHub " +
                        "abgefragt — deine Trainingsdaten bleiben auf dem Gerät.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Palette.TextMid
                )
                Spacer(Modifier.height(14.dp))
                Button(
                    onClick = { check() },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(15.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Palette.Sky, contentColor = Palette.Ink
                    )
                ) {
                    Icon(Icons.Filled.Refresh, null, modifier = Modifier.size(17.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Nach Updates suchen", fontWeight = FontWeight.Bold)
                }
            }

            is UiState.Checking -> {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(999.dp)),
                    color = Palette.Sky,
                    trackColor = Palette.Outline
                )
            }

            is UiState.UpToDate -> {
                Text(
                    "Du hast die neueste Fassung installiert.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Palette.TextMid
                )
                Spacer(Modifier.height(14.dp))
                OutlinedButton(onClick = { check() }, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Filled.Refresh, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Noch einmal prüfen")
                }
            }

            is UiState.Found -> {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Pill("$current → ${s.info.version}", Palette.Amber)
                    Spacer(Modifier.weight(1f))
                    if (s.info.sizeBytes > 0) {
                        Text(
                            "${s.info.sizeBytes / 1_000_000} MB",
                            style = MaterialTheme.typography.bodySmall,
                            color = Palette.TextLow
                        )
                    }
                }
                if (s.info.notes.isNotBlank()) {
                    Spacer(Modifier.height(12.dp))
                    Text(
                        Updater.shortenNotes(s.info.notes, maxLines = 5),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Palette.TextMid
                    )
                    TextButton(onClick = { showNotes = true }) {
                        Text("Alle Neuerungen ansehen", color = Palette.Sky)
                    }
                }
                Spacer(Modifier.height(10.dp))
                Button(
                    onClick = { startDownload(s.info) },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(15.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Palette.Amber, contentColor = Palette.Ink
                    )
                ) {
                    Icon(Icons.Filled.CloudDownload, null, modifier = Modifier.size(17.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Herunterladen", fontWeight = FontWeight.Bold)
                }
            }

            is UiState.Downloading -> {
                Text(
                    "${(s.progress * 100).toInt()} %",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Palette.Sky
                )
                Spacer(Modifier.height(8.dp))
                BarMeter(progress = s.progress)
            }

            is UiState.Ready -> {
                Text(
                    "Version ${s.info.version} ist heruntergeladen. Android fragt beim " +
                        "Installieren noch einmal nach — das ist normal, weil die App nicht " +
                        "aus dem Play Store kommt. Deine Daten bleiben erhalten.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Palette.TextMid
                )
                Spacer(Modifier.height(14.dp))
                Button(
                    onClick = { install(s.file) },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(15.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Palette.Emerald, contentColor = Palette.Ink
                    )
                ) {
                    Text("Jetzt installieren", fontWeight = FontWeight.Bold)
                }
            }

            is UiState.Failed -> {
                Text(
                    s.reason + "\n\nDu kannst die neue Fassung auch von Hand herunterladen.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Palette.TextMid
                )
                Spacer(Modifier.height(14.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { check() }, modifier = Modifier.weight(1f)) {
                        Text("Erneut prüfen")
                    }
                    OutlinedButton(
                        onClick = {
                            runCatching {
                                context.startActivity(
                                    Intent(Intent.ACTION_VIEW, Uri.parse(Updater.RELEASES_PAGE))
                                )
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Filled.OpenInNew, null, modifier = Modifier.size(15.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Auf GitHub")
                    }
                }
            }
        }
    }

    // Vollständige Veröffentlichungsnotizen
    val found = state as? UiState.Found
    if (showNotes && found != null) {
        AlertDialog(
            onDismissRequest = { showNotes = false },
            title = { Text("Neu in Version ${found.info.version}") },
            text = {
                Column(Modifier.heightIn(max = 420.dp).verticalScroll(rememberScrollState())) {
                    Text(
                        Updater.shortenNotes(found.info.notes, maxLines = 200),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Palette.TextMid
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showNotes = false }) { Text("Schließen") }
            }
        )
    }
}
