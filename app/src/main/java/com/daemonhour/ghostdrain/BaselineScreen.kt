package com.daemonhour.ghostdrain

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.daemonhour.ghostdrain.contract.DrainEvent
import com.daemonhour.ghostdrain.contract.DrainStatus
import com.daemonhour.ghostdrain.contract.DrainUiState
import com.daemonhour.ghostdrain.contract.MonitorStatus
import com.daemonhour.ghostdrain.contract.SessionResult
import java.text.DateFormat
import java.util.Date
import java.util.Locale

@Composable
internal fun BaselineScreen(state: DrainUiState, onEvent: (DrainEvent) -> Unit) {
    Scaffold(contentWindowInsets = WindowInsets.safeDrawing) { insets ->
        LazyColumn(
            modifier = Modifier.padding(insets),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                Text("Ghost Drain Hunter", style = MaterialTheme.typography.headlineLarge)
                Text("Battery use while your screen is off", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            item {
                val status = when (state.monitoring) {
                    MonitorStatus.STOPPED -> "Monitoring paused"
                    MonitorStatus.STARTING -> "Starting monitoring…"
                    MonitorStatus.WAITING_FOR_SCREEN_OFF -> "Ready · Turn your screen off"
                    MonitorStatus.SCREEN_OFF -> "Measuring screen-off interval"
                }
                Text(status, color = MaterialTheme.colorScheme.primary)
                state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                val stopped = state.monitoring == MonitorStatus.STOPPED
                Button(
                    onClick = { onEvent(if (stopped) DrainEvent.Start else DrainEvent.Stop) },
                    enabled = state.monitoring != MonitorStatus.STARTING,
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                ) {
                    Text(if (stopped) "Start monitoring" else "Stop monitoring")
                }
            }
            item {
                Text("Alert above", style = MaterialTheme.typography.titleMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(3.0, 5.0, 10.0).forEach { threshold ->
                        FilterChip(
                            selected = state.threshold == threshold,
                            onClick = { onEvent(DrainEvent.SetThreshold(threshold)) },
                            label = { Text("${threshold.toInt()} pp/h") },
                        )
                    }
                }
            }
            val latest = state.sessions.firstOrNull()
            if (latest == null) {
                item { Text("Start monitoring, unplug, turn off your screen, then wake it to see a result.") }
            } else {
                item {
                    Text("Latest reading", style = MaterialTheme.typography.titleLarge)
                    SessionCard(latest)
                }
                if (state.sessions.size > 1) {
                    item { Text("Previous sessions", style = MaterialTheme.typography.titleLarge) }
                    items(state.sessions.drop(1)) { SessionCard(it) }
                }
            }
        }
    }
}

@Composable
private fun SessionCard(result: SessionResult) {
    val (session, assessment) = result
    val isHigh = assessment.status == DrainStatus.HIGH_DRAIN
    val colors = MaterialTheme.colorScheme
    val status = when (assessment.status) {
        DrainStatus.NORMAL -> "Within selected threshold"
        DrainStatus.HIGH_DRAIN -> "Something is draining your phone in your pocket"
        DrainStatus.CHARGING -> "Excluded · Phone was plugged in"
        DrainStatus.INVALID -> "Excluded · Battery rose or the interval was invalid"
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = if (isHigh) colors.errorContainer else colors.surfaceVariant),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            assessment.ratePerHour?.let { rate ->
                Text(
                    String.format(Locale.getDefault(), "%.1f pp/hour", rate),
                    style = MaterialTheme.typography.headlineMedium,
                )
            }
            Text(status, style = MaterialTheme.typography.titleMedium)
            val date = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM)
            Text("Screen off · ${session.start.percent}% · ${date.format(Date(session.start.wallTimeMs))}")
            Text("Screen on · ${session.end.percent}% · ${date.format(Date(session.end.wallTimeMs))}")
            val seconds = session.durationMs.coerceAtLeast(0L) / 1000
            Text("Duration · ${seconds / 60}m ${seconds % 60}s")
            if (assessment.shortSample && assessment.ratePerHour != null) {
                Text("Short interval. One battery percentage point can significantly change this estimate.")
            }
        }
    }
}
