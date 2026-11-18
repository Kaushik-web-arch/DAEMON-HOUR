package com.daemonhour.ghostdrain

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.daemonhour.ghostdrain.contract.BatterySample
import com.daemonhour.ghostdrain.contract.DEFAULT_THRESHOLD
import com.daemonhour.ghostdrain.contract.DrainAnalyzer
import com.daemonhour.ghostdrain.contract.DrainSession
import com.daemonhour.ghostdrain.contract.DrainUiState
import com.daemonhour.ghostdrain.contract.MonitorStatus
import com.daemonhour.ghostdrain.contract.SessionResult
import com.daemonhour.ghostdrain.domain.DrainInsights
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

// The activity, ViewModel and service access this repository on the main thread.
internal class DrainRepository private constructor(context: Context) {
    private val preferences = context.getSharedPreferences("ghost_drain", Context.MODE_PRIVATE)
    private val analyzer: DrainAnalyzer = DrainInsights
    private var history = loadHistory()
    var state by mutableStateOf(DrainUiState(
        threshold = preferences.getFloat("threshold", DEFAULT_THRESHOLD.toFloat()).toDouble(),
    ))
        private set

    init {
        updateResults()
    }

    fun status(status: MonitorStatus, error: String? = null) {
        state = state.copy(monitoring = status, error = error)
    }

    fun record(session: DrainSession) {
        history = (listOf(session) + history).take(HISTORY_LIMIT)
        val data = JSONArray()
        history.forEach { entry ->
            data.put(JSONObject()
                .put("start", entry.start.toJson())
                .put("end", entry.end.toJson())
                .put("plugged", entry.pluggedInDuringSession))
        }
        preferences.edit().putString("history", data.toString()).apply()
        updateResults()
    }

    fun setThreshold(threshold: Double) {
        if (!threshold.isFinite() || threshold !in 0.1..1000.0) return
        val stored = threshold.toFloat()
        preferences.edit().putFloat("threshold", stored).apply()
        updateResults(stored.toDouble())
    }

    private fun updateResults(threshold: Double = state.threshold) {
        state = state.copy(
            threshold = threshold,
            sessions = history.map { SessionResult(it, analyzer.analyze(it, threshold)) },
        )
    }

    private fun loadHistory(): List<DrainSession> {
        val data = try {
            JSONArray(preferences.getString("history", "[]") ?: "[]")
        } catch (_: JSONException) {
            return emptyList()
        }
        return (0 until minOf(data.length(), HISTORY_LIMIT)).mapNotNull { index ->
            try {
                val item = data.getJSONObject(index)
                DrainSession(
                    item.getJSONObject("start").toSample(),
                    item.getJSONObject("end").toSample(),
                    item.getBoolean("plugged"),
                )
            } catch (_: JSONException) {
                null
            }
        }
    }

    private fun BatterySample.toJson(): JSONObject = JSONObject()
        .put("percent", percent)
        .put("wall", wallTimeMs)
        .put("elapsed", elapsedTimeMs)
        .put("plugged", pluggedIn)

    private fun JSONObject.toSample() = BatterySample(
        getInt("percent"), getLong("wall"), getLong("elapsed"), getBoolean("plugged"),
    )

    companion object {
        private const val HISTORY_LIMIT = 30
        private var instance: DrainRepository? = null

        fun get(context: Context): DrainRepository =
            instance ?: DrainRepository(context.applicationContext).also { instance = it }
    }
}
