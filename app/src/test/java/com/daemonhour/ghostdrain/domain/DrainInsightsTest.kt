package com.daemonhour.ghostdrain.domain

import com.daemonhour.ghostdrain.contract.BatterySample
import com.daemonhour.ghostdrain.contract.DEFAULT_THRESHOLD
import com.daemonhour.ghostdrain.contract.DrainSession
import com.daemonhour.ghostdrain.contract.DrainStatus
import com.daemonhour.ghostdrain.contract.SessionResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DrainInsightsTest {

    private val minute = 60_000L
    private val hour = 60 * minute
    private val delta = 1e-9

    /** Builds a session. [elapsedStart] is offset from the wall clock so tests catch use of the wrong clock. */
    private fun session(
        startPercent: Int,
        endPercent: Int,
        durationMs: Long,
        startPlugged: Boolean = false,
        endPlugged: Boolean = false,
        pluggedDuring: Boolean = false,
        elapsedStart: Long = 5_000_000L,
    ) = DrainSession(
        start = BatterySample(startPercent, wallTimeMs = 1_700_000_000_000L, elapsedTimeMs = elapsedStart, pluggedIn = startPlugged),
        end = BatterySample(endPercent, wallTimeMs = 1_700_000_000_000L, elapsedTimeMs = elapsedStart + durationMs, pluggedIn = endPlugged),
        pluggedInDuringSession = pluggedDuring,
    )

    private fun analyze(s: DrainSession, threshold: Double = DEFAULT_THRESHOLD) = DrainInsights.analyze(s, threshold)

    // ---- The 7 cases required by GHOST_DRAIN.md --------------------------------------------

    @Test
    fun `brief 1 - two points in thirty minutes is 4 per hour, normal, not short`() {
        val a = analyze(session(80, 78, 30 * minute))
        assertEquals(4.0, a.ratePerHour!!, delta)
        assertEquals(DrainStatus.NORMAL, a.status)
        assertFalse(a.shortSample)
    }

    @Test
    fun `brief 2 - above threshold is high drain`() {
        val a = analyze(session(90, 84, hour)) // 6 /h > 5
        assertEquals(6.0, a.ratePerHour!!, delta)
        assertEquals(DrainStatus.HIGH_DRAIN, a.status)
    }

    @Test
    fun `brief 3 - exactly at threshold is not flagged`() {
        val a = analyze(session(90, 85, hour)) // 5 /h == 5
        assertEquals(5.0, a.ratePerHour!!, delta)
        assertEquals(DrainStatus.NORMAL, a.status)
    }

    @Test
    fun `brief 4 - zero loss is zero rate and normal`() {
        val a = analyze(session(70, 70, 2 * hour))
        assertEquals(0.0, a.ratePerHour!!, delta)
        assertEquals(DrainStatus.NORMAL, a.status)
    }

    @Test
    fun `brief 5 - charging is excluded from drain`() {
        val a = analyze(session(50, 49, hour, pluggedDuring = true))
        assertNull(a.ratePerHour)
        assertEquals(DrainStatus.CHARGING, a.status)
    }

    @Test
    fun `brief 6 - battery increase without charger is invalid and excluded`() {
        val a = analyze(session(50, 53, hour))
        assertNull(a.ratePerHour)
        assertEquals(DrainStatus.INVALID, a.status)
    }

    @Test
    fun `brief 7 - zero or negative duration is invalid`() {
        for (d in listOf(0L, -minute)) {
            val a = analyze(session(80, 70, d))
            assertNull("duration=$d", a.ratePerHour)
            assertEquals("duration=$d", DrainStatus.INVALID, a.status)
            assertFalse("duration=$d", a.shortSample)
        }
    }

    // ---- Additional edge cases -------------------------------------------------------------

    @Test
    fun `plugged in at start or end also counts as charging`() {
        assertEquals(DrainStatus.CHARGING, analyze(session(50, 45, hour, startPlugged = true)).status)
        assertEquals(DrainStatus.CHARGING, analyze(session(50, 45, hour, endPlugged = true)).status)
    }

    @Test
    fun `charging battery increase is reported as charging not invalid`() {
        assertEquals(DrainStatus.CHARGING, analyze(session(40, 60, hour, pluggedDuring = true)).status)
    }

    @Test
    fun `out of range percentages are invalid`() {
        assertEquals(DrainStatus.INVALID, analyze(session(101, 90, hour)).status)
        assertEquals(DrainStatus.INVALID, analyze(session(50, -1, hour)).status)
    }

    @Test
    fun `short sample boundary is fifteen minutes`() {
        assertTrue(analyze(session(80, 80, 1L)).shortSample)
        assertTrue(analyze(session(80, 79, 15 * minute - 1)).shortSample)
        assertFalse(analyze(session(80, 79, 15 * minute)).shortSample)
    }

    @Test
    fun `short sample still reports its rate`() {
        val a = analyze(session(80, 79, 10 * minute)) // 6 /h from a single tick
        assertTrue(a.shortSample)
        assertEquals(6.0, a.ratePerHour!!, delta)
        assertEquals(DrainStatus.HIGH_DRAIN, a.status)
    }

    @Test
    fun `rate uses elapsed clock not wall clock`() {
        // Wall times are identical in the helper; only elapsed time differs.
        val a = analyze(session(60, 57, 30 * minute))
        assertEquals(6.0, a.ratePerHour!!, delta)
    }

    @Test
    fun `custom threshold is respected`() {
        val s = session(90, 87, hour) // 3 /h
        assertEquals(DrainStatus.HIGH_DRAIN, analyze(s, threshold = 2.0).status)
        assertEquals(DrainStatus.NORMAL, analyze(s, threshold = 3.0).status)
    }

    @Test
    fun `non finite or non positive threshold falls back to default`() {
        val s = session(90, 84, hour) // 6 /h
        for (t in listOf(Double.NaN, Double.POSITIVE_INFINITY, 0.0, -1.0)) {
            assertEquals("threshold=$t", DrainStatus.HIGH_DRAIN, analyze(s, t).status)
        }
    }

    // ---- Optional extension: suspected-cause tags -----------------------------------------

    private fun cause(s: DrainSession, threshold: Double = DEFAULT_THRESHOLD) =
        DrainInsights.suspectedCause(SessionResult(s, analyze(s, threshold)), threshold)

    @Test
    fun `steep short drop is a rogue wake lock spike`() {
        val c = cause(session(80, 76, 20 * minute)) // 12 /h >= 2 x 5, 4 points
        assertEquals(SuspectedCause.ROGUE_WAKE_LOCK_SPIKE, c)
        assertEquals("Rogue Wake-Lock Spike", c!!.label)
    }

    @Test
    fun `long high drain is sustained background drain`() {
        val c = cause(session(90, 72, 3 * hour)) // 6 /h over 3 h
        assertEquals(SuspectedCause.SUSTAINED_BACKGROUND_DRAIN, c)
        assertEquals("Sustained Background Drain", c!!.label)
    }

    @Test
    fun `short window just above threshold is sustained not spike`() {
        assertEquals(SuspectedCause.SUSTAINED_BACKGROUND_DRAIN, cause(session(80, 77, 30 * minute))) // 6 /h
    }

    @Test
    fun `single tick in short window gets no cause`() {
        assertNull(cause(session(80, 79, 5 * minute))) // 12 /h, but only one point
    }

    @Test
    fun `non high drain statuses get no cause`() {
        assertNull(cause(session(80, 78, hour)))                       // NORMAL
        assertNull(cause(session(50, 40, hour, pluggedDuring = true))) // CHARGING
        assertNull(cause(session(50, 60, hour)))                       // INVALID
    }
}
