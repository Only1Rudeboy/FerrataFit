package at.rudeboy.ferratafit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import at.rudeboy.ferratafit.data.Journey
import at.rudeboy.ferratafit.ui.screens.StageScreen
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import at.rudeboy.ferratafit.ui.FerrataTheme
import at.rudeboy.ferratafit.ui.Palette
import at.rudeboy.ferratafit.ui.screens.HomeScreen
import at.rudeboy.ferratafit.ui.screens.OnboardingScreen
import at.rudeboy.ferratafit.ui.screens.PlanScreen
import at.rudeboy.ferratafit.ui.screens.ProgressScreen
import at.rudeboy.ferratafit.ui.screens.SessionEditScreen
import at.rudeboy.ferratafit.ui.screens.SettingsScreen
import at.rudeboy.ferratafit.ui.screens.WorkoutScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            FerrataTheme(darkTheme = true) {
                FerrataApp()
            }
        }
    }
}

private enum class Tab(val label: String, val icon: ImageVector) {
    HOME("Heute", Icons.Filled.Home),
    PLAN("Plan", Icons.Filled.CalendarMonth),
    PROGRESS("Fortschritt", Icons.Filled.ShowChart),
    SETTINGS("Mehr", Icons.Filled.Settings)
}

@Composable
private fun FerrataApp(vm: AppViewModel = viewModel()) {
    val state by vm.state.collectAsState()
    val active by vm.active.collectAsState()
    val activeStage by vm.activeStage.collectAsState()
    val toast by vm.toast.collectAsState()
    val steps by vm.steps.collectAsState()
    val freshBadges by vm.freshBadges.collectAsState()
    val bodySyncing by vm.bodySyncing.collectAsState()
    val editing by vm.editing.collectAsState()

    var tab by remember { mutableIntStateOf(0) }
    var planDayId by remember { mutableStateOf<String?>(null) }
    var skipAsk by remember { mutableStateOf<String?>(null) }
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(toast) {
        toast?.let {
            snackbar.showSnackbar(it.message)
            vm.clearToast()
        }
    }

    LaunchedEffect(state.profile.healthConnectEnabled) {
        if (state.profile.healthConnectEnabled) {
            vm.refreshSteps()
            // Beim Öffnen still nachsehen, ob die Waage etwas Neues geliefert hat
            vm.syncBody(announce = false)
        }
    }

    // Ersteinrichtung blockiert alles andere, bis sie durch ist.
    if (!state.profile.onboarded) {
        OnboardingScreen { stations, bw, step, target, name ->
            vm.completeOnboarding(stations, bw, step, target, name)
        }
        return
    }

    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            snackbarHost = { SnackbarHost(snackbar) },
            bottomBar = {
                NavigationBar(containerColor = Palette.Surface) {
                    Tab.entries.forEachIndexed { i, t ->
                        NavigationBarItem(
                            selected = tab == i,
                            onClick = { tab = i },
                            icon = { Icon(t.icon, t.label) },
                            label = { Text(t.label) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Palette.Ink,
                                selectedTextColor = Palette.Sky,
                                indicatorColor = Palette.Sky,
                                unselectedIconColor = Palette.TextLow,
                                unselectedTextColor = Palette.TextLow
                            )
                        )
                    }
                }
            }
        ) { inner ->
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(bottom = inner.calculateBottomPadding())
                    .padding(top = inner.calculateTopPadding())
            ) {
                when (Tab.entries[tab]) {
                    Tab.HOME -> HomeScreen(
                        state = state,
                        steps = steps,
                        badgeCount = vm.badgeCount(),
                        onStartStage = { vm.startStage(it) },
                        onSkipStage = { skipAsk = it },
                        onOpenDay = { planDayId = it; tab = 1 },
                        onOpenBody = { tab = 2 }
                    )
                    Tab.PLAN -> PlanScreen(
                        state = state,
                        initialDayId = planDayId,
                        onStartWorkout = { vm.startWorkout(it) },
                        onToggleExercise = { vm.toggleHiddenExercise(it) }
                    )
                    Tab.PROGRESS -> ProgressScreen(
                        state = state,
                        earnedBadgeIds = vm.earnedBadgeIds(),
                        bodySyncing = bodySyncing,
                        onSyncBody = { vm.syncBody() },
                        onAddBodyManual = { kg, fat -> vm.addBodyManual(kg, fat) },
                        onImportBodyFile = { vm.importBodyFile(it) },
                        onEditSession = { vm.startEditSession(it) }
                    )
                    Tab.SETTINGS -> SettingsScreen(
                        state = state,
                        health = vm.health,
                        onUpdateProfile = { vm.updateProfile(it) },
                        onToggleStation = { vm.toggleStation(it) },
                        onSetHealthEnabled = { vm.setHealthEnabled(it) },
                        onSetAutoWeight = { vm.setAutoWeight(it) },
                        onSetTravelMode = { vm.setTravelMode(it) },
                        onSyncBody = { vm.syncBody() },
                        onSyncAll = { vm.syncAllToHealth() },
                        onRestartCycle = {
                            vm.restartCycle()
                            vm.notify("Neuer Block gestartet — Woche 1 mit reichlich Puffer.")
                        },
                        onExport = { vm.exportJson() },
                        onImport = { vm.importJson(it) },
                        onNotify = { vm.notify(it) }
                    )
                }
            }
        }

        // Das laufende Training legt sich als eigene Ebene über die Navigation,
        // damit man beim Loggen nicht versehentlich den Tab wechselt.
        AnimatedVisibility(
            visible = active != null,
            enter = fadeIn() + slideInVertically { it / 4 },
            exit = fadeOut() + slideOutVertically { it / 4 }
        ) {
            active?.let { w ->
                Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
                    WorkoutScreen(
                        workout = w,
                        onSelectExercise = { vm.selectExercise(it) },
                        onUpdateSet = { e, s, block -> vm.updateSet(e, s, block) },
                        onToggleDone = { e, s -> vm.toggleSetDone(e, s) },
                        onApplyToRemaining = { e, s -> vm.applyToRemaining(e, s) },
                        onFinish = { note -> vm.finishWorkout(note) },
                        onCancel = { vm.cancelWorkout() }
                    )
                }
            }
        }

        // Das Nachbearbeiten legt sich ebenfalls über die Navigation.
        AnimatedVisibility(
            visible = editing != null,
            enter = fadeIn() + slideInVertically { it / 4 },
            exit = fadeOut() + slideOutVertically { it / 4 }
        ) {
            editing?.let { session ->
                Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
                    SessionEditScreen(
                        session = session,
                        onEditSet = { i, block -> vm.editSet(i, block) },
                        onRemoveSet = { vm.removeSetFromEdit(it) },
                        onSave = { vm.saveEdit() },
                        onDelete = { vm.deleteSession(session.id) },
                        onCancel = { vm.cancelEdit() }
                    )
                }
            }
        }

        // Dehn-, Regenerations- und Ausdauer-Etappen liegen auf derselben Ebene.
        AnimatedVisibility(
            visible = activeStage != null,
            enter = fadeIn() + slideInVertically { it / 4 },
            exit = fadeOut() + slideOutVertically { it / 4 }
        ) {
            activeStage?.let { st ->
                Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
                    StageScreen(
                        active = st,
                        onToggleDrill = { vm.toggleDrill(it) },
                        onAdjust = { min, m -> vm.adjustEndurance(min, m) },
                        onFinish = { vm.finishStage() },
                        onSkip = { vm.skipStage(st.stage.id) },
                        onCancel = { vm.cancelStage() }
                    )
                }
            }
        }
    }

    // Rückfrage vor dem Überspringen — sonst ist die Etappe versehentlich weg.
    skipAsk?.let { stageId ->
        val stage = Journey.stage(stageId)
        AlertDialog(
            onDismissRequest = { skipAsk = null },
            title = { Text("„${stage?.title ?: "Etappe"}\" überspringen?") },
            text = {
                Text(
                    "Die nächste Etappe wird frei, du bekommst aber keine Höhenmeter dafür.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(onClick = { vm.skipStage(stageId); skipAsk = null }) {
                    Text("Überspringen", color = Palette.Rose)
                }
            },
            dismissButton = {
                TextButton(onClick = { skipAsk = null }) { Text("Zurück") }
            }
        )
    }

    // Neue Abzeichen bekommen einen eigenen Moment.
    if (freshBadges.isNotEmpty()) {
        AlertDialog(
            onDismissRequest = { vm.clearFreshBadges() },
            title = {
                Column(
                    Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        freshBadges.joinToString(" ") { it.icon },
                        style = MaterialTheme.typography.displayMedium
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        if (freshBadges.size > 1) "Neue Abzeichen!" else "Neues Abzeichen!",
                        style = MaterialTheme.typography.headlineSmall
                    )
                }
            },
            text = {
                Column(
                    Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    freshBadges.forEach { b ->
                        Spacer(Modifier.height(8.dp))
                        Text(
                            b.name,
                            style = MaterialTheme.typography.titleLarge,
                            color = Palette.Amber
                        )
                        Text(
                            b.desc,
                            style = MaterialTheme.typography.bodySmall,
                            color = Palette.TextMid,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { vm.clearFreshBadges() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Palette.Sky,
                        contentColor = Palette.Ink
                    )
                ) { Text("Weiter") }
            }
        )
    }

    // Zurück-Geste: erst zurück auf „Heute“, statt die App zu schließen.
    BackHandler(enabled = active == null && activeStage == null && editing == null && tab != 0) { tab = 0 }
}
