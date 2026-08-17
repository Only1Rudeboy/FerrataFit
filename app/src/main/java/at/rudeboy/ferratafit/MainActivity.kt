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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
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
    val toast by vm.toast.collectAsState()
    val steps by vm.steps.collectAsState()

    var tab by remember { mutableIntStateOf(0) }
    var planDayId by remember { mutableStateOf<String?>(null) }
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(toast) {
        toast?.let {
            snackbar.showSnackbar(it.message)
            vm.clearToast()
        }
    }

    LaunchedEffect(state.profile.healthConnectEnabled) {
        if (state.profile.healthConnectEnabled) vm.refreshSteps()
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
                        onStartWorkout = { vm.startWorkout(it) },
                        onOpenDay = { planDayId = it; tab = 1 }
                    )
                    Tab.PLAN -> PlanScreen(
                        state = state,
                        initialDayId = planDayId,
                        onStartWorkout = { vm.startWorkout(it) },
                        onToggleExercise = { vm.toggleHiddenExercise(it) }
                    )
                    Tab.PROGRESS -> ProgressScreen(state = state)
                    Tab.SETTINGS -> SettingsScreen(
                        state = state,
                        health = vm.health,
                        onUpdateProfile = { vm.updateProfile(it) },
                        onToggleStation = { vm.toggleStation(it) },
                        onSetHealthEnabled = { vm.setHealthEnabled(it) },
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
    }

    // Zurück-Geste: erst zurück auf „Heute“, statt die App zu schließen.
    BackHandler(enabled = active == null && tab != 0) { tab = 0 }
}
