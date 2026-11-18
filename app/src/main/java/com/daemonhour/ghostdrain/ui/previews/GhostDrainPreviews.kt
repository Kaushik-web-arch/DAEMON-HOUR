package com.daemonhour.ghostdrain.ui.previews

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.daemonhour.ghostdrain.ui.screens.DrainSession
import com.daemonhour.ghostdrain.ui.screens.GhostDrainScreen
import com.daemonhour.ghostdrain.ui.screens.GhostDrainUiState
import com.daemonhour.ghostdrain.ui.theme.WorkTheme

private val sampleNormalSession = DrainSession(
    id = "1",
    screenOffTimestamp = System.currentTimeMillis() - 7_200_000L, // 2 hours ago
    screenOnTimestamp = System.currentTimeMillis(),
    startBatteryPercent = 90,
    endBatteryPercent = 88 // 2% in 2 hours = 1.0%/hr (Normal)
)

private val sampleHighDrainSession = DrainSession(
    id = "2",
    screenOffTimestamp = System.currentTimeMillis() - 3_600_000L, // 1 hour ago
    screenOnTimestamp = System.currentTimeMillis(),
    startBatteryPercent = 80,
    endBatteryPercent = 71 // 9% in 1 hour = 9.0%/hr (HIGH POCKET DRAIN)
)

private val sampleHistory = listOf(
    sampleHighDrainSession,
    sampleNormalSession,
    DrainSession(
        id = "3",
        screenOffTimestamp = System.currentTimeMillis() - 18_000_000L,
        screenOnTimestamp = System.currentTimeMillis() - 14_400_000L,
        startBatteryPercent = 95,
        endBatteryPercent = 94
    )
)

@Preview(showBackground = true, name = "1. Normal Idle Drain (Below Threshold)")
@Composable
fun GhostDrainNormalPreview() {
    WorkTheme {
        GhostDrainScreen(
            uiState = GhostDrainUiState(
                isTrackingActive = true,
                currentBatteryPercent = 88,
                alertThresholdPerHour = 3.0f,
                latestSession = sampleNormalSession,
                history = sampleHistory
            ),
            onStartStop = {},
            onThresholdChanged = {}
        )
    }
}

@Preview(showBackground = true, name = "2. High Pocket Drain Alert (Above Threshold)")
@Composable
fun GhostDrainHighAlertPreview() {
    WorkTheme {
        GhostDrainScreen(
            uiState = GhostDrainUiState(
                isTrackingActive = true,
                currentBatteryPercent = 71,
                alertThresholdPerHour = 3.0f,
                latestSession = sampleHighDrainSession,
                history = sampleHistory
            ),
            onStartStop = {},
            onThresholdChanged = {}
        )
    }
}

@Preview(showBackground = true, name = "3. Fresh Install / Empty State")
@Composable
fun GhostDrainEmptyPreview() {
    WorkTheme {
        GhostDrainScreen(
            uiState = GhostDrainUiState(
                isTrackingActive = true,
                currentBatteryPercent = 100,
                alertThresholdPerHour = 3.0f,
                latestSession = null,
                history = emptyList()
            ),
            onStartStop = {},
            onThresholdChanged = {}
        )
    }
}

@Preview(showBackground = true, name = "4. Dark Theme High Drain Alert")
@Composable
fun GhostDrainDarkThemePreview() {
    WorkTheme(darkTheme = true, dynamicColor = false) {
        GhostDrainScreen(
            uiState = GhostDrainUiState(
                isTrackingActive = true,
                currentBatteryPercent = 71,
                alertThresholdPerHour = 3.0f,
                latestSession = sampleHighDrainSession,
                history = sampleHistory
            ),
            onStartStop = {},
            onThresholdChanged = {}
        )
    }
}

