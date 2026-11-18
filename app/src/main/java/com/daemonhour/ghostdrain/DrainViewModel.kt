package com.daemonhour.ghostdrain

import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import com.daemonhour.ghostdrain.contract.DrainEvent
import com.daemonhour.ghostdrain.contract.DrainUiState
import com.daemonhour.ghostdrain.contract.MonitorStatus

internal class DrainViewModel(private val app: Application) : AndroidViewModel(app) {
    private val repository = DrainRepository.get(app)
    val state: DrainUiState get() = repository.state

    fun onEvent(event: DrainEvent) {
        when (event) {
            DrainEvent.Start -> startMonitoring()
            DrainEvent.Stop -> {
                if (!app.stopService(Intent(app, BatteryMonitorService::class.java))) {
                    repository.status(MonitorStatus.STOPPED)
                }
            }
            is DrainEvent.SetThreshold -> repository.setThreshold(event.pointsPerHour)
        }
    }

    private fun startMonitoring() {
        if (state.monitoring != MonitorStatus.STOPPED) return
        repository.status(MonitorStatus.STARTING)
        try {
            app.startForegroundService(Intent(app, BatteryMonitorService::class.java))
        } catch (_: IllegalStateException) {
            repository.status(MonitorStatus.STOPPED, "Open the app and tap Start monitoring again.")
        } catch (_: SecurityException) {
            repository.status(MonitorStatus.STOPPED, "Android blocked battery monitoring on this device.")
        }
    }
}
