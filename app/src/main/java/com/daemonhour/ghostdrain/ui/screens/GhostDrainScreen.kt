
package com.daemonhour.ghostdrain.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.daemonhour.ghostdrain.ui.components.DrainHistoryCard
import com.daemonhour.ghostdrain.ui.components.DrainSummaryCard
import com.daemonhour.ghostdrain.ui.components.HighDrainAlertBanner
import com.daemonhour.ghostdrain.ui.components.ThresholdSliderCard

@Composable
fun GhostDrainScreen(
    uiState: GhostDrainUiState,
    onStartStop: () -> Unit,
    onThresholdChanged: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(modifier = modifier.fillMaxSize()) { padding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Column {
                    Text(
                        "Ghost Drain Hunter",
                        style = MaterialTheme.typography.headlineLarge
                    )
                    Text(
                        "Screen-Off Battery Loss Tracker",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            item {
                Text(
                    if (uiState.isTrackingActive)
                        "Monitoring active"
                    else
                        "Monitoring stopped",
                    color = MaterialTheme.colorScheme.primary
                )

                Text("Live battery percentage: unavailable")
            }

            item {
                Button(
                    onClick = onStartStop,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        if (uiState.isTrackingActive)
                            "Stop Monitoring"
                        else
                            "Start Monitoring"
                    )
                }
            }

            item {
                DrainSummaryCard(
                    session = uiState.latestSession,
                    thresholdPerHour = uiState.alertThresholdPerHour
                )
            }

            if (uiState.isHighDrainDetected &&
                uiState.latestSession != null
            ) {
                item {
                    HighDrainAlertBanner(
                        drainRatePerHour =
                            uiState.latestSession.drainRatePerHour,
                        thresholdPerHour =
                            uiState.alertThresholdPerHour
                    )
                }
            }

            item {
                ThresholdSliderCard(
                    thresholdPerHour = uiState.alertThresholdPerHour,
                    onThresholdChanged = onThresholdChanged
                )
            }

            item {
                Text(
                    "Screen-Off History",
                    style = MaterialTheme.typography.titleLarge
                )
            }

            if (uiState.history.isEmpty()) {
                item {
                    Text("No completed monitoring sessions yet.")
                }
            } else {
                items(
                    items = uiState.history,
                    key = { it.id }
                ) { session ->
                    DrainHistoryCard(
                        session = session,
                        thresholdPerHour =
                            uiState.alertThresholdPerHour
                    )
                }
            }
        }
    }
}
