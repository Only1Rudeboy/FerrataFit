package at.rudeboy.ferratafit.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import at.rudeboy.ferratafit.data.AppState
import at.rudeboy.ferratafit.data.Ferrata
import at.rudeboy.ferratafit.data.FerrataRoute
import at.rudeboy.ferratafit.data.FerrataRoutes
import at.rudeboy.ferratafit.data.Fit
import at.rudeboy.ferratafit.data.Stats
import at.rudeboy.ferratafit.data.buildSteigPass
import at.rudeboy.ferratafit.ui.FfCard
import at.rudeboy.ferratafit.ui.Palette
import at.rudeboy.ferratafit.ui.Pill
import at.rudeboy.ferratafit.ui.SteigPassCard

/**
 * Die Missionsübersicht: welcher Steig gerade zu dir passt.
 *
 * Die Reihenfolge ist Absicht. Ganz oben steht der Steigpass mit der einen Zahl, die
 * zählt — bis zu welcher Stufe du im Rahmen bist. Darunter die Routen, sortiert nach
 * Passung, nicht nach Schwierigkeit oder Alphabet: Was heute geht, steht oben; was noch
 * zu früh ist, steht unten und ist eingeklappt. Wer scrollen muss, um an die schweren
 * Sachen zu kommen, überlegt es sich unterwegs vielleicht anders.
 *
 * Der Hinweistext am Ende steht dort dauerhaft, nicht nur beim ersten Öffnen.
 */
@Composable
fun FerrataScreen(
    state: AppState,
    onLogAscent: () -> Unit,
    onTogglePlanned: (String) -> Unit
) {
    val now = System.currentTimeMillis()
    val readiness = Stats.ferrataReadiness(state.sessions, now)
    val pass = remember(state.ascents, readiness) {
        buildSteigPass(state.ascents, readiness, now)
    }

    var region by remember { mutableStateOf<String?>(null) }
    var openId by remember { mutableStateOf<String?>(null) }
    var showTooEarly by remember { mutableStateOf(false) }

    val sorted = remember(state.ascents, readiness, region) {
        FerrataRoutes.all
            .filter { region == null || it.region == region }
            .map { it to Ferrata.fitFor(it, state.ascents, readiness) }
            .sortedWith(compareBy({ it.second.ordinal }, { it.first.gradeEnum.ordinal }, { it.first.name }))
    }
    val groups = remember(sorted) { sorted.groupBy { it.second } }

    LazyColumn(
        Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item { SteigPassCard(pass) }

        item {
            Button(
                onClick = onLogAscent,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Palette.Amber, contentColor = Palette.Ink
                )
            ) {
                Icon(Icons.Filled.Add, null, Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("Begehung eintragen", fontWeight = FontWeight.Bold)
            }
        }

        item { RegionFilter(region) { region = it } }

        // Die drei offenen Kategorien in fester Reihenfolge — passend, dann knapp, dann Ziel.
        listOf(Fit.PASST, Fit.KNAPP, Fit.ZIEL).forEach { fit ->
            val routes = groups[fit].orEmpty()
            if (routes.isNotEmpty()) {
                item { FitHeader(fit, routes.size) }
                items(routes, key = { it.first.id }) { (route, f) ->
                    RouteCard(
                        route = route,
                        fit = f,
                        planned = route.id in state.plannedRouteIds,
                        expanded = openId == route.id,
                        onToggle = { openId = if (openId == route.id) null else route.id },
                        onPlan = { onTogglePlanned(route.id) }
                    )
                }
            }
        }

        val tooEarly = groups[Fit.ZU_FRUEH].orEmpty()
        if (tooEarly.isNotEmpty()) {
            item {
                FfCard(onClick = { showTooEarly = !showTooEarly }) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                "Noch nicht dran",
                                style = MaterialTheme.typography.titleSmall,
                                color = Palette.TextMid, fontWeight = FontWeight.Bold
                            )
                            Text(
                                "${tooEarly.size} Steige über deinem bisherigen Stand",
                                style = MaterialTheme.typography.bodySmall, color = Palette.TextLow
                            )
                        }
                        Icon(
                            if (showTooEarly) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                            null, tint = Palette.TextLow
                        )
                    }
                }
            }
            if (showTooEarly) {
                items(tooEarly, key = { "e_" + it.first.id }) { (route, f) ->
                    RouteCard(
                        route = route, fit = f,
                        planned = route.id in state.plannedRouteIds,
                        expanded = openId == route.id,
                        onToggle = { openId = if (openId == route.id) null else route.id },
                        onPlan = { onTogglePlanned(route.id) }
                    )
                }
            }
        }

        item {
            Spacer(Modifier.height(6.dp))
            Row(Modifier.padding(horizontal = 4.dp)) {
                Icon(Icons.Filled.Info, null, tint = Palette.TextLow, modifier = Modifier.size(15.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    Ferrata.DISCLAIMER,
                    style = MaterialTheme.typography.bodySmall,
                    color = Palette.TextLow
                )
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun RegionFilter(selected: String?, onSelect: (String?) -> Unit) {
    Row(
        Modifier.horizontalScroll(rememberScrollState()).padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        FilterChip("Alle Gebiete", selected == null) { onSelect(null) }
        FerrataRoutes.regions.forEach { r ->
            FilterChip(r.substringBefore(" ("), selected == r) { onSelect(r) }
        }
    }
}

@Composable
private fun FilterChip(label: String, active: Boolean, onClick: () -> Unit) {
    Box(
        Modifier
            .clip(CircleShape)
            .background(if (active) Palette.Sky else Palette.SurfaceHigh)
            .clickable(onClick = onClick)
            .padding(horizontal = 13.dp, vertical = 8.dp)
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            color = if (active) Palette.Ink else Palette.TextMid,
            fontWeight = if (active) FontWeight.Bold else FontWeight.Normal
        )
    }
}

private fun fitColor(fit: Fit): Color = when (fit) {
    Fit.PASST -> Palette.Emerald
    Fit.KNAPP -> Palette.Amber
    Fit.ZIEL -> Palette.Violet
    Fit.ZU_FRUEH -> Palette.TextLow
}

private fun fitTitle(fit: Fit): String = when (fit) {
    Fit.PASST -> "Passt zu dir"
    Fit.KNAPP -> "Machbar mit Puffer"
    Fit.ZIEL -> "Der nächste Schritt"
    Fit.ZU_FRUEH -> "Noch nicht dran"
}

@Composable
private fun FitHeader(fit: Fit, count: Int) {
    Column(Modifier.padding(top = 8.dp, start = 4.dp, bottom = 2.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(8.dp).clip(CircleShape).background(fitColor(fit)))
            Spacer(Modifier.width(9.dp))
            Text(
                fitTitle(fit),
                style = MaterialTheme.typography.titleMedium,
                color = Palette.TextHigh, fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.width(7.dp))
            Text("$count", style = MaterialTheme.typography.bodySmall, color = Palette.TextLow)
        }
        Text(
            when (fit) {
                Fit.ZIEL -> "Eine Stufe über dem Bestätigten — nur Steige mit Notausstieg oder kurzer Wand."
                else -> Ferrata.fitLabel(fit)
            },
            style = MaterialTheme.typography.bodySmall,
            color = Palette.TextLow,
            modifier = Modifier.padding(start = 17.dp, top = 1.dp)
        )
    }
}

@Composable
private fun RouteCard(
    route: FerrataRoute,
    fit: Fit,
    planned: Boolean,
    expanded: Boolean,
    onToggle: () -> Unit,
    onPlan: () -> Unit
) {
    FfCard(accent = if (expanded) fitColor(fit) else null, onClick = onToggle) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(13.dp))
                    .background(fitColor(fit).copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    route.grade,
                    style = MaterialTheme.typography.titleSmall,
                    color = fitColor(fit), fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    route.name,
                    style = MaterialTheme.typography.titleSmall,
                    color = Palette.TextHigh, fontWeight = FontWeight.Bold
                )
                Text(
                    buildList {
                        if (route.climbMeters > 0) add("${route.climbMeters} Klettermeter")
                        if (route.totalMin > 0) add("${route.totalMin / 60} h ${route.totalMin % 60} min")
                        add(route.region.substringBefore(" ("))
                    }.joinToString(" · "),
                    style = MaterialTheme.typography.bodySmall, color = Palette.TextLow
                )
            }
            Icon(
                if (planned) Icons.Filled.Star else Icons.Filled.StarBorder,
                if (planned) "Vorgemerkt" else "Vormerken",
                tint = if (planned) Palette.Amber else Palette.TextLow,
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onPlan)
                    .padding(6.dp)
            )
        }

        AnimatedVisibility(expanded) {
            Column {
                Spacer(Modifier.height(13.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (route.crux.isNotBlank()) {
                        Pill("Schlüsselstelle ${route.crux}", Palette.Rose)
                    }
                    if (route.hasExit) Pill("Notausstieg", Palette.Emerald)
                    if (route.familyFriendly) Pill("familientauglich", Palette.Sky)
                }

                if (route.summary.isNotBlank()) {
                    Spacer(Modifier.height(11.dp))
                    Text(
                        route.summary,
                        style = MaterialTheme.typography.bodySmall, color = Palette.TextMid
                    )
                }

                Detail("Zustieg", route.approach)
                Detail("Abstieg", route.descent)
                Detail("Ausrüstung", route.gear)
                if (route.season.isNotBlank()) Detail("Saison", route.season)

                if (route.warnings.isNotEmpty()) {
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "Zu beachten",
                        style = MaterialTheme.typography.labelMedium,
                        color = Palette.Rose, fontWeight = FontWeight.Bold
                    )
                    route.warnings.forEach {
                        Row(Modifier.padding(top = 4.dp)) {
                            Text("·", color = Palette.Rose)
                            Spacer(Modifier.width(7.dp))
                            Text(it, style = MaterialTheme.typography.bodySmall, color = Palette.TextMid)
                        }
                    }
                }

                if (!route.verified) {
                    Spacer(Modifier.height(12.dp))
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Palette.Amber.copy(alpha = 0.1f))
                            .padding(11.dp)
                    ) {
                        Text(
                            "Die Quellen widersprechen sich bei diesem Steig — meist beim " +
                                "Schwierigkeitsgrad. Vor Ort prüfen und im Zweifel die " +
                                "schwerere Angabe annehmen.",
                            style = MaterialTheme.typography.bodySmall, color = Palette.Amber
                        )
                    }
                }

                if (route.sources.isNotEmpty()) {
                    Spacer(Modifier.height(11.dp))
                    Text(
                        "Quellen: " + route.sources.joinToString("  ") {
                            it.removePrefix("https://").removePrefix("www.").substringBefore("/")
                        },
                        style = MaterialTheme.typography.bodySmall, color = Palette.TextLow
                    )
                }
            }
        }
    }
}

@Composable
private fun Detail(label: String, text: String) {
    if (text.isBlank()) return
    Spacer(Modifier.height(11.dp))
    Text(
        label,
        style = MaterialTheme.typography.labelMedium,
        color = Palette.Sky, fontWeight = FontWeight.Bold
    )
    Text(
        text,
        style = MaterialTheme.typography.bodySmall,
        color = Palette.TextMid,
        modifier = Modifier.padding(top = 2.dp)
    )
}
