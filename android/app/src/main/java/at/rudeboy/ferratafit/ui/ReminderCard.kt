package at.rudeboy.ferratafit.ui

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationManagerCompat
import at.rudeboy.ferratafit.data.Profile

/**
 * Tägliche Erinnerung an die offene Etappe.
 *
 * Ohne Anstupser lebt ein Etappensystem nicht — wer nicht in die App schaut, weiß nicht,
 * dass heute etwas ansteht.
 */
@Composable
fun ReminderCard(
    profile: Profile,
    onSetReminder: (Boolean, Int?, Int?) -> Unit,
    onSetSkipIfDone: (Boolean) -> Unit,
    onNotify: (String) -> Unit
) {
    val context = LocalContext.current
    var blocked by remember { mutableStateOf(false) }

    // Ab Android 13 muss die Freigabe für Benachrichtigungen erfragt werden
    val askPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            onSetReminder(true, null, null)
        } else {
            // Nach zweimaliger Ablehnung kehrt der Dialog sofort zurück — das sieht
            // aus wie ein Fehler. Deshalb der Verweis auf die Einstellungen.
            blocked = true
            onNotify("Ohne Freigabe kann die App nicht erinnern.")
        }
    }

    fun enable() {
        val allowed = NotificationManagerCompat.from(context).areNotificationsEnabled()
        when {
            allowed -> onSetReminder(true, null, null)
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU ->
                askPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
            else -> blocked = true
        }
    }

    FfCard(accent = if (profile.reminderEnabled) Palette.Sky else null) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(13.dp))
                    .background(
                        if (profile.reminderEnabled) Palette.Sky.copy(alpha = 0.16f)
                        else Palette.SurfaceHigh
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.NotificationsActive, null,
                    tint = if (profile.reminderEnabled) Palette.Sky else Palette.TextLow,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(Modifier.width(13.dp))
            Column(Modifier.weight(1f)) {
                Text("Tägliche Erinnerung", style = MaterialTheme.typography.titleLarge, color = Palette.TextHigh)
                Text(
                    if (profile.reminderEnabled)
                        "täglich um %02d:%02d".format(profile.reminderHour, profile.reminderMinute)
                    else "aus",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (profile.reminderEnabled) Palette.Sky else Palette.TextLow
                )
            }
            Switch(
                checked = profile.reminderEnabled,
                onCheckedChange = { on -> if (on) enable() else onSetReminder(false, null, null) },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Palette.Ink,
                    checkedTrackColor = Palette.Sky
                )
            )
        }

        if (profile.reminderEnabled) {
            Spacer(Modifier.height(16.dp))
            Text("UHRZEIT", style = MaterialTheme.typography.labelSmall, color = Palette.TextLow)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                TimeStepper(
                    label = "Stunde",
                    value = "%02d".format(profile.reminderHour),
                    onMinus = { onSetReminder(true, (profile.reminderHour + 23) % 24, null) },
                    onPlus = { onSetReminder(true, (profile.reminderHour + 1) % 24, null) },
                    modifier = Modifier.weight(1f)
                )
                TimeStepper(
                    label = "Minute",
                    value = "%02d".format(profile.reminderMinute),
                    onMinus = { onSetReminder(true, null, (profile.reminderMinute + 45) % 60) },
                    onPlus = { onSetReminder(true, null, (profile.reminderMinute + 15) % 60) },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(14.dp))
            Row(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onSetSkipIfDone(!profile.reminderSkipIfDone) }
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        "Nicht erinnern, wenn schon erledigt",
                        style = MaterialTheme.typography.titleMedium,
                        color = Palette.TextHigh
                    )
                    Text(
                        "Wer die Etappe früher am Tag abhakt, bekommt abends Ruhe.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Palette.TextLow
                    )
                }
                Switch(
                    checked = profile.reminderSkipIfDone,
                    onCheckedChange = onSetSkipIfDone,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Palette.Ink,
                        checkedTrackColor = Palette.Emerald
                    )
                )
            }

            Spacer(Modifier.height(12.dp))
            Text(
                "Die Erinnerung nennt die Etappe, die gerade ansteht. Sie kann ein paar " +
                    "Minuten später kommen als eingestellt — dafür braucht die App keine " +
                    "Sonderrechte fürs Wecken.",
                style = MaterialTheme.typography.bodySmall,
                color = Palette.TextLow
            )
        }

        if (blocked) {
            Spacer(Modifier.height(14.dp))
            OutlinedButton(
                onClick = {
                    runCatching {
                        context.startActivity(
                            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                .setData(Uri.parse("package:${context.packageName}"))
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Benachrichtigungen in den Einstellungen freigeben")
            }
            Spacer(Modifier.height(8.dp))
            Text(
                "Falls die Erinnerung trotz Freigabe ausbleibt: Auf Samsung-Geräten schaltet " +
                    "die Akkuverwaltung selten genutzte Apps in den Tiefschlaf. FerrataFit dort " +
                    "auf „Nicht optimiert\" stellen.",
                style = MaterialTheme.typography.bodySmall,
                color = Palette.TextLow
            )
        }
    }
}

@Composable
private fun TimeStepper(
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
                style = MaterialTheme.typography.headlineSmall,
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
