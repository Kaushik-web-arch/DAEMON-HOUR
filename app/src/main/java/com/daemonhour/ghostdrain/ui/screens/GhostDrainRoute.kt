
package com.daemonhour.ghostdrain.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.daemonhour.ghostdrain.contract.DrainEvent
import com.daemonhour.ghostdrain.contract.DrainUiState
import com.daemonhour.ghostdrain.contract.MonitorStatus

@Composable
fun GhostDrainRoute(
    state: DrainUiState,
    onEvent: (DrainEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    val sessions = state.sessions.mapNotNull { result ->
        val session = result.session

        if (result.assessment.ratePerHour == null) {
            return@mapNotNull null
        }

        DrainSession(
            id = session.start.wallTimeMs.toString(),
            screenOffTimestamp = session.start.wallTimeMs,
            screenOnTimestamp = session.end.wallTimeMs,
            startBatteryPercent = session.start.percent,
            endBatteryPercent = session.end.percent
        )
    }

    val latest = sessions.firstOrNull()

    val uiState = GhostDrainUiState(
        isTrackingActive = state.monitoring != MonitorStatus.STOPPED,
        currentBatteryPercent = -1,
        isScreenCurrentlyOn = state.monitoring != MonitorStatus.SCREEN_OFF,
        alertThresholdPerHour = state.threshold.toFloat(),
        latestSession = latest,
        history = sessions
    )

    GhostDrainScreen(
        uiState = uiState,
        onStartStop = {
            onEvent(
                if (state.monitoring == MonitorStatus.STOPPED) {
                    DrainEvent.Start
                } else {
                    DrainEvent.Stop
                }
            )
        },
        onThresholdChanged = {
            onEvent(DrainEvent.SetThreshold(it.toDouble()))
        },
        modifier = modifier
    )
}
