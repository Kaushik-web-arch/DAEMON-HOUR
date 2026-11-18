package com.example.work.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

/**
 * Stateful wrapper route for [GhostDrainScreen].
 *
 * Manages local UI state for standalone execution and demonstration.
 *
 * TODO: Kaushik/Prithvi will replace this local state holder with the production
 * ViewModel (e.g. GhostDrainViewModel) and real background service drain logic.
 * Note that OnSimulateScreenOffDrain is a presentation/demo helper only.
 */
@Composable
fun GhostDrainRoute(
    modifier: Modifier = Modifier
) {
    var uiState by remember {
        mutableStateOf(
            GhostDrainUiState(
                isTrackingActive = true,
                currentBatteryPercent = 85,
                latestSession = null,
                history = emptyList()
            )
        )
    }

    GhostDrainScreen(
        uiState = uiState,
        onEvent = { event ->
            when (event) {
                is GhostDrainUiEvent.OnThresholdChanged -> {
                    uiState = uiState.copy(alertThresholdPerHour = event.newThreshold)
                }

                is GhostDrainUiEvent.OnToggleTracking -> {
                    uiState = uiState.copy(isTrackingActive = !uiState.isTrackingActive)
                }

                is GhostDrainUiEvent.OnClearHistory -> {
                    uiState = uiState.copy(
                        history = emptyList(),
                        latestSession = null
                    )
                }

                is GhostDrainUiEvent.OnSimulateScreenOffDrain -> {
                    val now = System.currentTimeMillis()
                    val simSession = DrainSession(
                        id = now.toString(),
                        screenOffTimestamp = now - 3_600_000L,
                        screenOnTimestamp = now,
                        startBatteryPercent = uiState.currentBatteryPercent,
                        endBatteryPercent = (uiState.currentBatteryPercent - 10).coerceAtLeast(0)
                    )
                    uiState = uiState.copy(
                        latestSession = simSession,
                        history = listOf(simSession) + uiState.history,
                        currentBatteryPercent = simSession.endBatteryPercent
                    )
                }
            }
        },
        modifier = modifier
    )
}
