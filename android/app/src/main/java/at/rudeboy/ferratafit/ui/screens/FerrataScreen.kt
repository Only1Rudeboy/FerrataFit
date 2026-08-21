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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.ui.platform.LocalContext
import at.rudeboy.ferratafit.data.FerrataMedia
import at.rudeboy.ferratafit.ui.RemoteImage
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import at.rudeboy.ferratafit.data.AppState
import at.rudeboy.ferratafit.data.Ascent
import at.rudeboy.ferratafit.data.FerrataGrade
import at.rudeboy.ferratafit.data.TourLoad
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.sp
import at.rudeboy.ferratafit.data.Ferrata
import at.rudeboy.ferratafit.data.FerrataRoute
import at.rudeboy.ferratafit.data.FerrataRoutes
import at.rudeboy.ferratafit.data.Fit
import at.rudeboy.ferratafit.data.Stats
import at.rudeboy.ferratafit.data.buildSteigPass
import at.rudeboy.ferratafit.ui.EmptyHint
import at.rudeboy.ferratafit.ui.FerrataMap
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
    onTogglePlanned: (String) -> Unit,
    onRemoveAscent: (String) -> Unit = {},
    onAddRoutePhoto: (String, android.net.Uri) -> Unit = { _, _ -> },
    onRemoveRoutePhoto: (String) -> Unit = {}
) {
    fun extrasFor(routeId: String) = RouteExtras(
        ownPhotos = ownPhotosFor(state, routeId),
        webPhotosEnabled = state.profile.webPhotosEnabled,
        onAddPhoto = onAddRoutePhoto,
        onRemovePhoto = onRemoveRoutePhoto
    )
    val now = System.currentTimeMillis()
    val readiness = Stats.ferrataReadiness(state.sessions, now)
    val pass = remember(state.ascents, readiness) {
        buildSteigPass(state.ascents, readiness, now)
    }

    var region by remember { mutableStateOf<String?>(null) }
    var openId by remember { mutableStateOf<String?>(null) }
    var showTooEarly by remember { mutableStateOf(false) }
    var showMap by rememberSaveable { mutableStateOf(false) }
    var deleteAsk by rememberSaveable { mutableStateOf<String?>(null) }

    // Passung aller Steige, ungefiltert — die Karte zeigt immer das ganze Land.
    val fitById = remember(state.ascents, readiness) {
        FerrataRoutes.all.associate { it.id to Ferrata.fitFor(it, state.ascents, readiness) }
    }

    val sorted = remember(state.ascents, readiness, region) {
        FerrataRoutes.all
            .filter { region == null || it.region == region }
            .map { it to Ferrata.fitFor(it, state.ascents, readiness) }
            .sortedWith(compareBy({ it.second.ordinal }, { it.first.gradeEnum.ordinal }, { it.first.name }))
    }
    val groups = remember(sorted) { sorted.groupBy { it.second } }

    deleteAsk?.let { id ->
        val name = state.ascents.firstOrNull { it.id == id }?.name ?: "diese Begehung"
        AlertDialog(
            onDismissRequest = { deleteAsk = null },
            title = { Text("Begehung löschen?") },
            text = {
                Text(
                    "„$name“ verschwindet aus Verlauf, Rang und Höhenmetern. " +
                        "Ein Foto dazu wird mitgelöscht."
                )
            },
            confirmButton = {
                TextButton(onClick = { onRemoveAscent(id); deleteAsk = null }) {
                    Text("Löschen", color = Palette.Rose)
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteAsk = null }) { Text("Behalten") }
            },
            containerColor = Palette.SurfaceHigh
        )
    }

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

        item {
            Row(
                Modifier.horizontalScroll(rememberScrollState()).padding(vertical = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                FilterChip(if (showMap) "☰ Liste" else "🗺 Karte", showMap) { showMap = !showMap }
                FilterChip("Alle Gebiete", region == null && !showMap) { region = null; showMap = false }
                FerrataRoutes.regions.forEach { r ->
                    FilterChip(r.substringBefore(" ("), region == r && !showMap) {
                        region = r; showMap = false
                    }
                }
            }
        }

        if (showMap) {
            item {
                FfCard(padding = 10.dp) {
                    FerrataMap(
                        fits = fitById,
                        selectedId = openId,
                        onSelect = { openId = it }
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        MapLegend(Palette.Emerald, "passt")
                        MapLegend(Palette.Amber, "mit Puffer")
                        MapLegend(Palette.Violet, "nächster Schritt")
                        MapLegend(Palette.TextLow, "noch nicht")
                    }
                }
            }
            // Der angetippte Steig erscheint direkt unter der Karte — aufgeklappt,
            // mit allem, was die Liste auch zeigt.
            openId?.let { id ->
                FerrataRoutes.byId(id)?.let { route ->
                    item {
                        RouteCard(
                            route = route,
                            fit = fitById[id] ?: Fit.ZU_FRUEH,
                            planned = id in state.plannedRouteIds,
                            expanded = true,
                            onToggle = { openId = null },
                            onPlan = { onTogglePlanned(id) },
                            extras = extrasFor(id)
                        )
                    }
                }
            }
        }

        if (!showMap) {
        item { Spacer(Modifier.height(0.dp)) }

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
                        onPlan = { onTogglePlanned(route.id) },
                        extras = extrasFor(route.id)
                    )
                }
            }
        }

        // Liegt gerade nichts im Rahmen, bleibt die Liste sonst wortlos leer. Der Grund
        // steht zwar oben im Steigpass, aber nicht dort, wo man ihn sucht.
        if (listOf(Fit.PASST, Fit.KNAPP, Fit.ZIEL).all { groups[it].isNullOrEmpty() }) {
            item {
                EmptyHint(
                    if (region != null) {
                        "In diesem Gebiet liegt gerade nichts im Rahmen. Andere Gebiete zeigen mehr."
                    } else {
                        pass.reason + " Die Steige darunter stehen weiter offen — sie sind nur " +
                            "noch nichts, wozu die App raten würde."
                    }
                )
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
                        onPlan = { onTogglePlanned(route.id) },
                        extras = extrasFor(route.id)
                    )
                }
            }
        }

        if (state.ascents.isNotEmpty()) {
            item {
                Text(
                    "Deine Begehungen",
                    style = MaterialTheme.typography.titleMedium,
                    color = Palette.TextHigh, fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 12.dp, start = 4.dp)
                )
            }
            items(state.ascents.reversed(), key = { "a_" + it.id }) { a ->
                AscentRow(a, onRemove = { deleteAsk = a.id })
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

/**
 * Die Tagesskizze: Zustieg, Wand, Abstieg.
 *
 * Bewusst „Skizze" und nicht „Profil": Belegt sind nur Einstiegshöhe, Ausstiegshöhe
 * und die drei Zeiten. Zustieg und Abstieg sind deshalb gestrichelt — ihre Form ist
 * schematisch, nur die Wand dazwischen ist echte Angabe. Eine Kurve, die mehr
 * behauptet, würde Wissen vortäuschen, das keine Quelle hergibt.
 */
@Composable
private fun DayProfile(route: FerrataRoute) {
    val measurer = rememberTextMeasurer()
    val topAlt = maxOf(route.summitAlt, route.startAlt + route.climbMeters)
    val (za, fe, ab) = Ferrata.daySegments(route.approachMin, route.ferrataMin, route.descentMin)
    val gradeColor = when (route.gradeEnum) {
        FerrataGrade.A, FerrataGrade.B -> Palette.Emerald
        FerrataGrade.C -> Palette.Sky
        FerrataGrade.D -> Palette.Amber
        else -> Palette.Rose
    }

    Canvas(
        Modifier
            .fillMaxWidth()
            .height(110.dp)
    ) {
        val w = size.width
        val h = size.height
        val yBase = h * 0.78f
        val yIn = h * 0.62f
        val yTop = h * 0.10f
        val x1 = w * za
        val x2 = w * (za + fe)
        val dash = PathEffect.dashPathEffect(floatArrayOf(9f, 9f))

        // Zustieg — schematisch, deshalb gestrichelt
        drawLine(Palette.TextLow, Offset(0f, yBase), Offset(x1, yIn), strokeWidth = 3f, pathEffect = dash)
        // Die Wand — die einzige belegte Strecke, deshalb durchgezogen und farbig
        drawLine(gradeColor, Offset(x1, yIn), Offset(x2, yTop), strokeWidth = 6f)
        drawCircle(gradeColor, radius = 6f, center = Offset(x1, yIn))
        drawCircle(gradeColor, radius = 6f, center = Offset(x2, yTop))
        // Abstieg
        drawLine(Palette.TextLow, Offset(x2, yTop), Offset(w, yBase), strokeWidth = 3f, pathEffect = dash)

        val small = TextStyle(fontSize = 10.sp, color = Palette.TextLow)
        drawText(measurer, "${route.startAlt} m", Offset(x1 + 8f, yIn - 4f), small)
        // Links unterhalb des Gipfelpunkts — oberhalb ist bei flachen Karten kein Platz,
        // und auf dem Punkt selbst klebte das Label im ersten Wurf
        drawText(measurer, "$topAlt m", Offset((x2 - 78f).coerceAtLeast(0f), yTop + 8f), small)

        fun min(v: Int) = if (v > 0) "$v min" else "—"
        drawText(measurer, min(route.approachMin), Offset(x1 / 2 - 20f, h - 16f), small)
        drawText(
            measurer, "${route.climbMeters} Hm · ${min(route.ferrataMin)}",
            Offset((x1 + x2) / 2 - 45f, h - 16f),
            TextStyle(fontSize = 10.sp, color = gradeColor)
        )
        drawText(measurer, min(route.descentMin), Offset((x2 + w) / 2 - 20f, h - 16f), small)
    }
}

/** Eine eingetragene Begehung — mit Foto, falls eines dazugehört. */
@Composable
private fun AscentRow(a: Ascent, onRemove: () -> Unit) {
    FfCard(padding = 14.dp) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Palette.Sky.copy(alpha = 0.13f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    a.grade,
                    style = MaterialTheme.typography.labelLarge,
                    color = Palette.Sky, fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.width(11.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    a.name,
                    style = MaterialTheme.typography.titleSmall,
                    color = Palette.TextHigh, fontWeight = FontWeight.Bold
                )
                Text(
                    buildList {
                        add(
                            java.text.SimpleDateFormat("dd.MM.yyyy", java.util.Locale.GERMAN)
                                .format(java.util.Date(a.date))
                        )
                        if (a.climbMeters > 0) add("${a.climbMeters} Hm")
                        add("${TourLoad.label(TourLoad.score(a))}")
                        if (a.turnedBack) add("umgekehrt")
                    }.joinToString(" · "),
                    style = MaterialTheme.typography.bodySmall, color = Palette.TextLow
                )
            }
            TextButton(onClick = onRemove) {
                Text("löschen", color = Palette.TextLow, style = MaterialTheme.typography.labelMedium)
            }
        }

        // Das Foto liegt verkleinert im App-Ordner; fürs Listenbild reicht ein
        // weiter heruntergerechnetes Exemplar — vier Kacheln teilen sich sonst
        // den Speicher eines ganzen Bildschirms.
        if (a.photoPath.isNotBlank()) {
            val thumb = remember(a.photoPath) {
                runCatching {
                    android.graphics.BitmapFactory.decodeFile(
                        a.photoPath,
                        android.graphics.BitmapFactory.Options().apply { inSampleSize = 2 }
                    )?.asImageBitmap()
                }.getOrNull()
            }
            thumb?.let {
                Spacer(Modifier.height(10.dp))
                Image(
                    bitmap = it,
                    contentDescription = "Foto: ${a.name}",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .clip(RoundedCornerShape(13.dp))
                )
            }
        }
    }
}

@Composable
private fun MapLegend(color: androidx.compose.ui.graphics.Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(8.dp).clip(CircleShape).background(color))
        Spacer(Modifier.width(5.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = Palette.TextLow)
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

/** Was die Routenkarte über die Anzeige hinaus braucht — eigene Fotos und ihre Aktionen. */
data class RouteExtras(
    val ownPhotos: List<Pair<String, String?>> = emptyList(),
    val webPhotosEnabled: Boolean = true,
    val onAddPhoto: (String, android.net.Uri) -> Unit = { _, _ -> },
    val onRemovePhoto: (String) -> Unit = {}
)

private fun ownPhotosFor(state: AppState, routeId: String): List<Pair<String, String?>> =
    // Fotos aus Begehungen dieser Route (nicht löschbar von hier — sie gehören zur
    // Begehung) und direkt angehängte Fotos (löschbar, mit Kennung)
    state.ascents.filter { it.routeId == routeId && it.photoPath.isNotBlank() }
        .map { it.photoPath to null } +
        state.routePhotos.filter { it.routeId == routeId }.map { it.path to it.id }

@Composable
private fun RouteCard(
    route: FerrataRoute,
    fit: Fit,
    planned: Boolean,
    expanded: Boolean,
    onToggle: () -> Unit,
    onPlan: () -> Unit,
    extras: RouteExtras = RouteExtras()
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
            var tab by rememberSaveable(route.id) { mutableStateOf(0) }
            Column {
                Spacer(Modifier.height(13.dp))

                // Vier Reiter: die Beschreibung, Fotos aus dem Netz, eigene Fotos, Topo.
                // Die Zahl hinter „Fotos" verrät vorab, ob sich der Tipp lohnt.
                val webCount = FerrataMedia.photosFor(route.id).size
                Row(
                    Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(
                        "Info",
                        if (webCount > 0) "Fotos · $webCount" else "Fotos",
                        if (extras.ownPhotos.isNotEmpty()) "Eigene · ${extras.ownPhotos.size}" else "Eigene",
                        "Topo"
                    ).forEachIndexed { i, label ->
                        FilterChip(label, tab == i) { tab = i }
                    }
                }
                Spacer(Modifier.height(12.dp))

                when (tab) {
                    1 -> WebPhotosTab(route, extras.webPhotosEnabled)
                    2 -> OwnPhotosTab(route, extras)
                    3 -> TopoTab(route)
                    else -> Column {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (route.crux.isNotBlank()) {
                        Pill("Schlüsselstelle ${route.crux}", Palette.Rose)
                    }
                    if (route.hasExit) Pill("Notausstieg", Palette.Emerald)
                    if (route.familyFriendly) Pill("familientauglich", Palette.Sky)
                }

                if (route.climbMeters > 0 && route.startAlt > 0) {
                    Spacer(Modifier.height(12.dp))
                    DayProfile(route)
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
    }
}

/**
 * Fotos von Wikimedia Commons — geladen, wenn der Reiter aufgeht, nicht vorher.
 *
 * Jedes Bild nennt Urheber und Lizenz: Das ist die Bedingung der freien Lizenzen und
 * der Grund, warum diese Fotos in einer öffentlichen App überhaupt gezeigt werden
 * dürfen. Dazu der Link zur Galerie des Tourenportals — dort liegen die Fotos, die
 * die App aus Rechtsgründen nicht einbinden kann.
 */
@Composable
private fun WebPhotosTab(route: FerrataRoute, enabled: Boolean) {
    val context = LocalContext.current
    val photos = FerrataMedia.photosFor(route.id)
    val gallery = FerrataMedia.galleries[route.id]

    fun open(url: String) {
        runCatching {
            context.startActivity(
                android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(url))
            )
        }
    }

    Column {
        when {
            !enabled -> Text(
                "Das Nachladen von Fotos ist unter Mehr → Netz ausgeschaltet. " +
                    "Die App lädt dann nichts aus dem Internet.",
                style = MaterialTheme.typography.bodySmall, color = Palette.TextLow
            )
            photos.isEmpty() -> Text(
                "Für diesen Steig gibt es auf Wikimedia Commons kein frei lizenziertes Foto. " +
                    "Fremde Fotos aus dem Netz nimmt die App bewusst nicht auf — Urheberrecht.",
                style = MaterialTheme.typography.bodySmall, color = Palette.TextLow
            )
            else -> photos.forEach { p ->
                RemoteImage(
                    url = p.url,
                    contentDescription = p.shows,
                    modifier = Modifier.clip(RoundedCornerShape(13.dp)),
                    height = 200.dp
                )
                Spacer(Modifier.height(5.dp))
                Text(p.shows, style = MaterialTheme.typography.bodySmall, color = Palette.TextMid)
                Text(
                    "${p.author} · ${p.license} · Wikimedia Commons",
                    style = MaterialTheme.typography.labelSmall,
                    color = Palette.Sky,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .clickable { open(p.pageUrl) }
                        .padding(vertical = 3.dp)
                )
                Spacer(Modifier.height(12.dp))
            }
        }

        gallery?.let { url ->
            Spacer(Modifier.height(4.dp))
            Box(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(13.dp))
                    .background(Palette.SurfaceHigh)
                    .clickable { open(url) }
                    .padding(13.dp)
            ) {
                Text(
                    "Weitere Fotos auf ${url.removePrefix("https://").removePrefix("www.").substringBefore("/")} ↗",
                    style = MaterialTheme.typography.bodyMedium, color = Palette.Sky
                )
            }
        }
    }
}

/** Eigene Fotos: aus Begehungen dieser Route und direkt angehängte. */
@Composable
private fun OwnPhotosTab(route: FerrataRoute, extras: RouteExtras) {
    val picker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri -> uri?.let { extras.onAddPhoto(route.id, it) } }

    Column {
        if (extras.ownPhotos.isEmpty()) {
            Text(
                "Noch kein eigenes Bild. Fotos aus einer Begehung erscheinen hier von selbst — " +
                    "oder du hängst direkt eines an den Steig.",
                style = MaterialTheme.typography.bodySmall, color = Palette.TextLow
            )
            Spacer(Modifier.height(10.dp))
        }
        extras.ownPhotos.forEach { (path, id) ->
            val bmp = remember(path) {
                runCatching {
                    android.graphics.BitmapFactory.decodeFile(
                        path, android.graphics.BitmapFactory.Options().apply { inSampleSize = 2 }
                    )?.asImageBitmap()
                }.getOrNull()
            }
            bmp?.let {
                Image(
                    bitmap = it,
                    contentDescription = "Eigenes Foto: ${route.name}",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(13.dp))
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        if (id == null) "aus einer Begehung" else "direkt angehängt",
                        style = MaterialTheme.typography.labelSmall, color = Palette.TextLow,
                        modifier = Modifier.weight(1f)
                    )
                    if (id != null) {
                        TextButton(onClick = { extras.onRemovePhoto(id) }) {
                            Text("entfernen", color = Palette.TextLow, style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
            }
        }
        Box(
            Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clip(RoundedCornerShape(13.dp))
                .background(Palette.SurfaceHigh)
                .clickable {
                    picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                },
            contentAlignment = Alignment.Center
        ) {
            Text("📷  Foto hinzufügen", color = Palette.TextMid)
        }
    }
}

/**
 * Die schematische Topo: vom Einstieg unten zum Ausstieg oben.
 *
 * Gezeichnet aus den Abschnitten, die die Recherche aus den Tourenbeschreibungen
 * gezogen hat — Reihenfolge, Art, Grad. Das sind Fakten und damit frei; die
 * Zeichnung ist unsere. Eine gezeichnete Original-Topo von bergsteigen.com ist
 * Urheberwerk, deshalb gibt es sie nur als Link.
 */
@Composable
private fun TopoTab(route: FerrataRoute) {
    val context = LocalContext.current
    val segs = FerrataMedia.topoFor(route.id)
    val url = FerrataMedia.topoUrls[route.id]

    fun gradeColor(g: FerrataGrade) = when (g) {
        FerrataGrade.A, FerrataGrade.B -> Palette.Emerald
        FerrataGrade.C -> Palette.Sky
        FerrataGrade.D -> Palette.Amber
        else -> Palette.Rose
    }

    Column {
        if (segs.isEmpty()) {
            Text(
                "Für diesen Steig liegt noch keine Abschnittsfolge vor.",
                style = MaterialTheme.typography.bodySmall, color = Palette.TextLow
            )
        } else {
            // Ohne Einstiegshöhe im Katalog wäre „Ausstieg · 60 m" eine Seehöhe, die keine
            // ist — dann lieber die Klettermeter als das, was sie sind.
            val topLabel = when {
                route.startAlt > 0 -> " · ${maxOf(route.summitAlt, route.startAlt + route.climbMeters)} m"
                route.climbMeters > 0 -> " · +${route.climbMeters} Hm"
                else -> ""
            }
            Text(
                "▲ Ausstieg$topLabel",
                style = MaterialTheme.typography.labelMedium, color = Palette.TextLow,
                modifier = Modifier.padding(start = 2.dp, bottom = 6.dp)
            )
            // Von oben nach unten gelesen ist der letzte Abschnitt der oberste —
            // wie auf einer Topo, die man vor der Wand in der Hand hält.
            segs.reversed().forEachIndexed { i, seg ->
                val color = if (seg.kind == "exit") Palette.TextLow else gradeColor(seg.gradeEnum)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Die durchgehende Linie: jeder Abschnitt trägt sein Stück in seiner Farbe
                    Box(
                        Modifier
                            .width(8.dp)
                            .height(if (seg.kind == "exit") 30.dp else 46.dp)
                            .background(color.copy(alpha = if (seg.kind == "exit") 0.35f else 0.85f))
                    )
                    Spacer(Modifier.width(10.dp))
                    Box(
                        Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(11.dp))
                            .background(
                                if (seg.crux) Palette.Rose.copy(alpha = 0.18f)
                                else color.copy(alpha = 0.14f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            if (seg.kind == "exit") "🚪" else seg.grade,
                            style = MaterialTheme.typography.labelLarge,
                            color = if (seg.crux) Palette.Rose else color,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            "${seg.icon} ${seg.label}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (seg.kind == "exit") Palette.TextLow else Palette.TextHigh
                        )
                        val sub = buildList {
                            if (seg.crux) add("Schlüsselstelle")
                            if (seg.meters > 0) add("${seg.meters} Hm")
                            if (seg.kind == "exit") add("Notausstieg")
                        }
                        if (sub.isNotEmpty()) {
                            Text(
                                sub.joinToString(" · "),
                                style = MaterialTheme.typography.labelSmall,
                                color = if (seg.crux) Palette.Rose else Palette.TextLow
                            )
                        }
                    }
                }
            }
            Text(
                "▼ Einstieg" + if (route.startAlt > 0) " · ${route.startAlt} m" else "",
                style = MaterialTheme.typography.labelMedium, color = Palette.TextLow,
                modifier = Modifier.padding(start = 2.dp, top = 6.dp)
            )

            Spacer(Modifier.height(10.dp))
            Text(
                "Schematisch, aus den Tourenbeschreibungen abgeleitet — Reihenfolge und Grade " +
                    "der Abschnitte, nicht ihre Länge. Am Einstieg zählt die Tafel vor Ort.",
                style = MaterialTheme.typography.bodySmall, color = Palette.TextLow
            )
        }

        url?.let { u ->
            Spacer(Modifier.height(10.dp))
            Box(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(13.dp))
                    .background(Palette.SurfaceHigh)
                    .clickable {
                        runCatching {
                            context.startActivity(
                                android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(u))
                            )
                        }
                    }
                    .padding(13.dp)
            ) {
                Text(
                    "Gezeichnete Topo auf ${u.removePrefix("https://").removePrefix("www.").substringBefore("/")} ↗",
                    style = MaterialTheme.typography.bodyMedium, color = Palette.Sky
                )
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
