package com.daemonhour.ghostdrain.contract

const val DEFAULT_THRESHOLD = 5.0

data class BatterySample(
    val percent: Int,
    val wallTimeMs: Long,
    val elapsedTimeMs: Long,
    val pluggedIn: Boolean,
)

data class DrainSession(
    val start: BatterySample,
    val end: BatterySample,
    val pluggedInDuringSession: Boolean,
) {
    val durationMs: Long get() = end.elapsedTimeMs - start.elapsedTimeMs
    val pointsLost: Int get() = start.percent - end.percent
}

enum class DrainStatus {
    NORMAL, HIGH_DRAIN, CHARGING, INVALID
}

data class DrainAssessment(
    val ratePerHour: Double?,
    val status: DrainStatus,
    val shortSample: Boolean,
)

fun interface DrainAnalyzer {
    fun analyze(session: DrainSession, threshold: Double): DrainAssessment
}

data class SessionResult(val session: DrainSession, val assessment: DrainAssessment)

enum class MonitorStatus {
    STOPPED, STARTING, WAITING_FOR_SCREEN_OFF, SCREEN_OFF
}

data class DrainUiState(
    val monitoring: MonitorStatus = MonitorStatus.STOPPED,
    val threshold: Double = DEFAULT_THRESHOLD,
    val sessions: List<SessionResult> = emptyList(),
    val error: String? = null,
)

sealed interface DrainEvent {
    data object Start : DrainEvent
    data object Stop : DrainEvent
    data class SetThreshold(val pointsPerHour: Double) : DrainEvent
}
