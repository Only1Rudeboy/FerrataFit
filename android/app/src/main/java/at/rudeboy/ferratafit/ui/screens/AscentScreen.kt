package at.rudeboy.ferratafit.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import at.rudeboy.ferratafit.data.Ascent
import at.rudeboy.ferratafit.data.AscentFlag
import at.rudeboy.ferratafit.data.Feel
import at.rudeboy.ferratafit.data.FerrataGrade
import at.rudeboy.ferratafit.data.FerrataRoute
import at.rudeboy.ferratafit.data.FerrataRoutes
import at.rudeboy.ferratafit.ui.FfCard
import at.rudeboy.ferratafit.ui.Palette

/**
 * Eine Begehung eintragen.
 *
 * Die wichtigste Frage steht in der Mitte und ist keine Zahl: Wie hat es sich angefühlt?
 * Aus dieser Antwort zieht die App mehr als aus Höhenmetern — eine grenzwertige Begehung
 * deckelt die Empfehlung sofort auf ihre Stufe, egal was das Training sagt.
 *
 * Umkehren steht bewusst gleichberechtigt neben dem Durchstieg, nicht als Kleingedrucktes.
 * Wer das Gefühl hat, ein Abbruch sei ein Makel im Verlauf, kehrt beim nächsten Mal
 * vielleicht später um als gut wäre.
 */
@Composable
fun AscentScreen(
    onSave: (Ascent) -> Unit,
    onCancel: () -> Unit
) {
    var route by remember { mutableStateOf<FerrataRoute?>(null) }
    var freeName by remember { mutableStateOf("") }
    var grade by remember { mutableStateOf(FerrataGrade.A) }
    var meters by remember { mutableStateOf("") }
    var minutes by remember { mutableStateOf("") }
    var feel by remember { mutableStateOf<Feel?>(null) }
    var flags by remember { mutableStateOf(setOf<AscentFlag>()) }
    var turnedBack by remember { mutableStateOf(false) }
    var partners by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var search by remember { mutableStateOf("") }

    val name = route?.name ?: freeName.trim()
    val canSave = name.isNotBlank() && feel != null

    LazyColumn(
        Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onCancel) {
                    Icon(Icons.Filled.ArrowBack, "Zurück", tint = Palette.TextMid)
                }
                Text(
                    "Begehung eintragen",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Palette.TextHigh, fontWeight = FontWeight.Bold
                )
            }
        }

        // --- Welcher Steig ---------------------------------------------------
        item {
            FfCard {
                Label("Welcher Steig?")
                Spacer(Modifier.height(9.dp))

                if (route != null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                route!!.name,
                                style = MaterialTheme.typography.titleSmall,
                                color = Palette.TextHigh, fontWeight = FontWeight.Bold
                            )
                            Text(
                                "${route!!.grade} · ${route!!.region}",
                                style = MaterialTheme.typography.bodySmall, color = Palette.TextLow
                            )
                        }
                        Text(
                            "ändern",
                            style = MaterialTheme.typography.labelMedium,
                            color = Palette.Sky,
                            modifier = Modifier
                                .clip(CircleShape)
                                .clickable { route = null; search = "" }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                } else {
                    OutlinedTextField(
                        value = search,
                        onValueChange = { search = it; freeName = it },
                        label = { Text("Name suchen oder frei eintragen") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    val hits = remember(search) {
                        if (search.length < 2) emptyList()
                        else FerrataRoutes.all.filter {
                            it.name.contains(search, true) || it.region.contains(search, true)
                        }.take(6)
                    }
                    hits.forEach { r ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    route = r
                                    grade = r.gradeEnum
                                    if (r.climbMeters > 0) meters = r.climbMeters.toString()
                                    if (r.totalMin > 0) minutes = r.totalMin.toString()
                                }
                                .padding(vertical = 9.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                r.grade,
                                style = MaterialTheme.typography.labelMedium,
                                color = Palette.Sky, fontWeight = FontWeight.Bold,
                                modifier = Modifier.width(38.dp)
                            )
                            Text(
                                r.name,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Palette.TextMid
                            )
                        }
                    }
                    if (search.length >= 2 && hits.isEmpty()) {
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "Nicht im Katalog — wird als eigener Eintrag gespeichert. " +
                                "Schwierigkeit bitte unten selbst wählen.",
                            style = MaterialTheme.typography.bodySmall, color = Palette.TextLow
                        )
                    }
                }
            }
        }

        // --- Schwierigkeit ---------------------------------------------------
        item {
            FfCard {
                Label("Schwierigkeit")
                Spacer(Modifier.height(4.dp))
                Text(
                    "Bei Zwischenstufen wie C/D zählt die schwerere.",
                    style = MaterialTheme.typography.bodySmall, color = Palette.TextLow
                )
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    FerrataGrade.entries.forEach { g ->
                        Box(
                            Modifier
                                .weight(1f)
                                .height(44.dp)
                                .clip(RoundedCornerShape(13.dp))
                                .background(if (grade == g) Palette.Sky else Palette.SurfaceHigh)
                                .clickable { grade = g },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                g.label,
                                style = MaterialTheme.typography.titleSmall,
                                color = if (grade == g) Palette.Ink else Palette.TextMid,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    grade.desc,
                    style = MaterialTheme.typography.bodySmall, color = Palette.TextMid
                )
            }
        }

        // --- Umfang ----------------------------------------------------------
        item {
            FfCard {
                Label("Umfang")
                Spacer(Modifier.height(9.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = meters,
                        onValueChange = { meters = it.filter(Char::isDigit).take(4) },
                        label = { Text("Klettermeter") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = minutes,
                        onValueChange = { minutes = it.filter(Char::isDigit).take(4) },
                        label = { Text("Dauer in min") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    "Klettermeter meint den gesicherten Steig, nicht den ganzen Tag. " +
                        "Der Zustieg zählt für den Rang nicht mit.",
                    style = MaterialTheme.typography.bodySmall, color = Palette.TextLow
                )
            }
        }

        // --- Umkehren --------------------------------------------------------
        item {
            FfCard(accent = if (turnedBack) Palette.Sky else null) {
                Label("Wie ist es ausgegangen?")
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                    Choice("Durchgestiegen", !turnedBack, Modifier.weight(1f)) { turnedBack = false }
                    Choice("Umgekehrt", turnedBack, Modifier.weight(1f)) { turnedBack = true }
                }
                if (turnedBack) {
                    Spacer(Modifier.height(10.dp))
                    Text(
                        "Die Höhenmeter zählen voll. Der Rang bleibt, wo er ist — Umkehren " +
                            "kostet hier nichts.",
                        style = MaterialTheme.typography.bodySmall, color = Palette.Sky
                    )
                }
            }
        }

        // --- Gefühl ----------------------------------------------------------
        item {
            FfCard(accent = if (feel == null) Palette.Amber else null) {
                Label("Wie hat es sich angefühlt?")
                Spacer(Modifier.height(4.dp))
                Text(
                    "Das ist die Angabe, aus der die App am meisten zieht.",
                    style = MaterialTheme.typography.bodySmall, color = Palette.TextLow
                )
                Spacer(Modifier.height(10.dp))
                Feel.entries.forEach { f ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp)
                            .clip(RoundedCornerShape(13.dp))
                            .background(if (feel == f) Palette.Sky.copy(alpha = 0.16f) else Palette.SurfaceHigh)
                            .clickable { feel = f }
                            .padding(horizontal = 13.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            Modifier
                                .size(17.dp)
                                .clip(CircleShape)
                                .background(if (feel == f) Palette.Sky else Palette.Outline),
                            contentAlignment = Alignment.Center
                        ) {
                            if (feel == f) {
                                Icon(
                                    Icons.Filled.Check, null,
                                    tint = Palette.Ink, modifier = Modifier.size(12.dp)
                                )
                            }
                        }
                        Spacer(Modifier.width(11.dp))
                        Text(
                            f.label,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (feel == f) Palette.TextHigh else Palette.TextMid
                        )
                    }
                }
            }
        }

        // --- Was war das Thema -----------------------------------------------
        item {
            FfCard {
                Label("Was war das Thema?")
                Spacer(Modifier.height(4.dp))
                Text(
                    "Mehrfachauswahl. Fließt in die Trainingsempfehlung ein.",
                    style = MaterialTheme.typography.bodySmall, color = Palette.TextLow
                )
                Spacer(Modifier.height(10.dp))
                AscentFlag.entries.forEach { f ->
                    val on = f in flags
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp)
                            .clip(RoundedCornerShape(11.dp))
                            .clickable {
                                // „Nichts davon“ schließt die anderen aus, und umgekehrt
                                flags = when {
                                    f == AscentFlag.RUND -> if (on) emptySet() else setOf(f)
                                    on -> flags - f
                                    else -> flags - AscentFlag.RUND + f
                                }
                            }
                            .padding(horizontal = 10.dp, vertical = 9.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            Modifier
                                .size(15.dp)
                                .clip(RoundedCornerShape(5.dp))
                                .background(if (on) Palette.Emerald else Palette.Outline)
                        )
                        Spacer(Modifier.width(11.dp))
                        Text(
                            f.label,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (on) Palette.TextHigh else Palette.TextMid
                        )
                    }
                }
            }
        }

        item {
            FfCard {
                Label("Notiz")
                Spacer(Modifier.height(9.dp))
                OutlinedTextField(
                    value = partners,
                    onValueChange = { partners = it },
                    label = { Text("Mit wem?") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Wie war der Tag?") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        item {
            Button(
                onClick = {
                    onSave(
                        Ascent(
                            id = "A" + System.currentTimeMillis(),
                            date = System.currentTimeMillis(),
                            name = name,
                            routeId = route?.id,
                            region = route?.region.orEmpty(),
                            grade = grade.name,
                            climbMeters = meters.toIntOrNull() ?: 0,
                            durationMin = minutes.toIntOrNull() ?: 0,
                            feel = (feel ?: Feel.GUT).name,
                            flags = flags.map { it.name },
                            turnedBack = turnedBack,
                            partners = partners.trim(),
                            note = note.trim()
                        )
                    )
                },
                enabled = canSave,
                modifier = Modifier.fillMaxWidth().height(54.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Palette.Emerald,
                    contentColor = Palette.Ink,
                    disabledContainerColor = Palette.SurfaceHigh,
                    disabledContentColor = Palette.TextLow
                )
            ) {
                Text(
                    if (canSave) "Eintragen" else "Name und Gefühl fehlen noch",
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.height(20.dp))
        }
    }
}

@Composable
private fun Label(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.titleSmall,
        color = Palette.TextHigh, fontWeight = FontWeight.Bold
    )
}

@Composable
private fun Choice(label: String, active: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier
            .height(46.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(if (active) Palette.Sky else Palette.SurfaceHigh)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            color = if (active) Palette.Ink else Palette.TextMid,
            fontWeight = if (active) FontWeight.Bold else FontWeight.Normal
        )
    }
}
