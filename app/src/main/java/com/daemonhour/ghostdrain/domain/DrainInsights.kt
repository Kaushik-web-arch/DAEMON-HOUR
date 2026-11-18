package com.daemonhour.ghostdrain.domain

import com.daemonhour.ghostdrain.contract.DEFAULT_THRESHOLD
import com.daemonhour.ghostdrain.contract.DrainAnalyzer
import com.daemonhour.ghostdrain.contract.DrainAssessment
import com.daemonhour.ghostdrain.contract.DrainSession
import com.daemonhour.ghostdrain.contract.DrainStatus
import com.daemonhour.ghostdrain.contract.SessionResult

/**
 * Shared drain analyzer for Ghost Drain Hunter (PS 14).
 *
 * Rules, per GHOST_DRAIN.md:
 *  - Duration comes from the monotonic elapsed clock ([DrainSession.durationMs]), so wall-clock
 *    changes cannot distort the rate.
 *  - INVALID: non-positive duration, a battery reading outside 0..100, or a battery increase
 *    without a recorded plug-in. Rate is null and the session is excluded from drain figures.
 *  - CHARGING: plugged in at the start, at the end, or at any point during the session. Rate is
 *    null and the session is excluded from drain figures.
 *  - Otherwise the rate is points lost per hour. HIGH_DRAIN applies only when the rate is
 *    strictly above the threshold, so a session exactly at the threshold is NORMAL.
 *  - Differentiator: a valid duration under 15 minutes is marked [DrainAssessment.shortSample].
 *    A single one-point change in the battery reading swings its hourly estimate a lot.
 *
 * Pure Kotlin with no Android dependencies, so it is unit-tested on the JVM.
 */
object DrainInsights : DrainAnalyzer {

    /** Sessions shorter than this (with a positive duration) are labelled short samples. */
    const val SHORT_SAMPLE_MS = 15 * 60_000L

    private const val MS_PER_HOUR = 3_600_000.0

    override fun analyze(session: DrainSession, threshold: Double): DrainAssessment {
        val duration = session.durationMs
        val shortSample = duration in 1 until SHORT_SAMPLE_MS
        val limit = if (threshold.isFinite() && threshold > 0.0) threshold else DEFAULT_THRESHOLD

        if (duration <= 0L || session.start.percent !in 0..100 || session.end.percent !in 0..100) {
            return DrainAssessment(ratePerHour = null, status = DrainStatus.INVALID, shortSample = shortSample)
        }
        if (session.pluggedInDuringSession || session.start.pluggedIn || session.end.pluggedIn) {
            return DrainAssessment(ratePerHour = null, status = DrainStatus.CHARGING, shortSample = shortSample)
        }
        if (session.pointsLost < 0) {
            // Battery rose with no plug-in recorded: a gauge recalibration or a missed charge event.
            return DrainAssessment(ratePerHour = null, status = DrainStatus.INVALID, shortSample = shortSample)
        }

        val rate = session.pointsLost * MS_PER_HOUR / duration
        val status = if (rate > limit) DrainStatus.HIGH_DRAIN else DrainStatus.NORMAL
        return DrainAssessment(ratePerHour = rate, status = status, shortSample = shortSample)
    }
}

// ---------------------------------------------------------------------------------------------
// OPTIONAL EXTENSION: suspected-cause tags (proposal for Kaushik, not part of the frozen contract)
//
// Nothing in the app calls this yet, and it does not change DrainAssessment or any contract type.
// To adopt it, the UI can call `DrainInsights.suspectedCause(result, state.threshold)` for each
// SessionResult and show `label` under a HIGH_DRAIN reading. To drop it, delete this section and
// its tests.
// ---------------------------------------------------------------------------------------------

/** Likely cause of a HIGH_DRAIN session, inferred from how steep and how long the drain was. */
enum class SuspectedCause(val label: String) {
    /** A short window with a steep drop: something is holding the CPU awake in bursts. */
    ROGUE_WAKE_LOCK_SPIKE("Rogue Wake-Lock Spike"),

    /** Above the threshold but not spike-shaped: a steady background service or radio. */
    SUSTAINED_BACKGROUND_DRAIN("Sustained Background Drain"),
}

/** A spike must be at most this long... */
const val SPIKE_MAX_DURATION_MS = 30 * 60_000L

/** ...drain at least this many times the threshold... */
const val SPIKE_THRESHOLD_MULTIPLIER = 2.0

/** ...and lose at least this many points, so one gauge tick cannot fake a spike. */
const val SPIKE_MIN_POINTS = 2

/**
 * Tags a HIGH_DRAIN result with a suspected cause. Returns null for any other status, and for
 * high-drain readings built on a single one-point tick in a short window, which is too little
 * evidence to name a cause.
 */
fun DrainInsights.suspectedCause(result: SessionResult, threshold: Double): SuspectedCause? {
    val assessment = result.assessment
    val rate = assessment.ratePerHour
    if (assessment.status != DrainStatus.HIGH_DRAIN || rate == null) return null

    val session = result.session
    val limit = if (threshold.isFinite() && threshold > 0.0) threshold else DEFAULT_THRESHOLD
    val shortWindow = session.durationMs <= SPIKE_MAX_DURATION_MS

    return when {
        shortWindow && session.pointsLost < SPIKE_MIN_POINTS -> null
        shortWindow && rate >= limit * SPIKE_THRESHOLD_MULTIPLIER -> SuspectedCause.ROGUE_WAKE_LOCK_SPIKE
        else -> SuspectedCause.SUSTAINED_BACKGROUND_DRAIN
    }
}
