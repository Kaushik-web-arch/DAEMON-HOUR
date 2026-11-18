package com.daemonhour.ghostdrain.ui.screens

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Represents a single recorded screen-off to screen-on session.
 */
data class DrainSession(
    val id: String,
    val screenOffTimestamp: Long,
    val screenOnTimestamp: Long,
    val startBatteryPercent: Int,
    val endBatteryPercent: Int
) {
    val durationMillis: Long
        get() = (screenOnTimestamp - screenOffTimestamp).coerceAtLeast(0L)

    val batteryLostPercent: Int
        get() = (startBatteryPercent - endBatteryPercent).coerceAtLeast(0)

    val durationFormatted: String
        get() {
            val totalSeconds = durationMillis / 1000
            val hours = totalSeconds / 3600
            val minutes = (totalSeconds % 3600) / 60
            val seconds = totalSeconds % 60
            return when {
                hours > 0 -> "${hours}h ${minutes}m"
                minutes > 0 -> "${minutes}m ${seconds}s"
                else -> "${seconds}s"
            }
        }

    val screenOffTimeFormatted: String
        get() = formatTimestamp(screenOffTimestamp)

    val screenOnTimeFormatted: String
        get() = formatTimestamp(screenOnTimestamp)

    /**
     * Calculates the battery drain rate normalized to percentage lost per hour.
     * Rate = (batteryLostPercent / durationHours)
     */
    val drainRatePerHour: Double
        get() {
            if (durationMillis <= 0L) return 0.0
            val durationHours = durationMillis.toDouble() / 3_600_000.0
            return (batteryLostPercent.toDouble() / durationHours)
        }

    private fun formatTimestamp(timestamp: Long): String {
        val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
}

/**
 * UI state contract for Ghost Drain Hunter.
 */
data class GhostDrainUiState(
    val isTrackingActive: Boolean = true,
    val currentBatteryPercent: Int = 85,
    val isScreenCurrentlyOn: Boolean = true,
    val alertThresholdPerHour: Float = 3.0f,
    val latestSession: DrainSession? = null,
    val history: List<DrainSession> = emptyList(),
    val isSimulating: Boolean = false
) {
    /**
     * PS 14 Requirement: Flag "Something is draining your phone in your pocket"
     * when the drain rate per hour exceeds the configured threshold.
     */
    val isHighDrainDetected: Boolean
        get() = latestSession != null && latestSession.drainRatePerHour >= alertThresholdPerHour
}

/**
 * UI events dispatched from the screen composables.
 */
sealed interface GhostDrainUiEvent {
    data class OnThresholdChanged(val newThreshold: Float) : GhostDrainUiEvent
    data object OnToggleTracking : GhostDrainUiEvent
    data object OnClearHistory : GhostDrainUiEvent
    data object OnSimulateScreenOffDrain : GhostDrainUiEvent
}

