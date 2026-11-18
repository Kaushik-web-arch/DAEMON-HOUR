package com.daemonhour.ghostdrain

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import android.os.IBinder
import android.os.SystemClock
import com.daemonhour.ghostdrain.contract.BatterySample
import com.daemonhour.ghostdrain.contract.DrainSession
import com.daemonhour.ghostdrain.contract.MonitorStatus
import kotlin.math.roundToInt

class BatteryMonitorService : Service() {
    private val repository by lazy { DrainRepository.get(this) }
    private var screenOff: BatterySample? = null
    private var pluggedInDuringSession = false
    private var registered = false

    private val displayEvents = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                Intent.ACTION_SCREEN_OFF -> captureStart()
                Intent.ACTION_SCREEN_ON -> captureEnd()
                Intent.ACTION_POWER_CONNECTED -> {
                    if (screenOff != null) pluggedInDuringSession = true
                }
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopSelf()
            return START_NOT_STICKY
        }
        if (!registered) {
            try {
                startForeground(NOTIFICATION_ID, notification())
                val filter = IntentFilter().apply {
                    addAction(Intent.ACTION_SCREEN_OFF)
                    addAction(Intent.ACTION_SCREEN_ON)
                    addAction(Intent.ACTION_POWER_CONNECTED)
                }
                if (Build.VERSION.SDK_INT >= 33) {
                    registerReceiver(displayEvents, filter, Context.RECEIVER_NOT_EXPORTED)
                } else {
                    @Suppress("UnspecifiedRegisterReceiverFlag", "DEPRECATION")
                    registerReceiver(displayEvents, filter)
                }
                registered = true
                repository.status(MonitorStatus.WAITING_FOR_SCREEN_OFF)
            } catch (_: SecurityException) {
                repository.status(MonitorStatus.STOPPED, "Android blocked battery monitoring on this device.")
                stopSelf()
            } catch (_: IllegalStateException) {
                repository.status(MonitorStatus.STOPPED, "Open the app and tap Start monitoring again.")
                stopSelf()
            }
        }
        return START_NOT_STICKY
    }

    private fun captureStart() {
        if (screenOff != null) return
        val sample = readBattery()
        if (sample == null) {
            repository.status(MonitorStatus.WAITING_FOR_SCREEN_OFF, "Battery level unavailable. Try another session.")
            return
        }
        screenOff = sample
        pluggedInDuringSession = sample.pluggedIn
        repository.status(MonitorStatus.SCREEN_OFF)
    }

    private fun captureEnd() {
        val start = screenOff ?: return
        screenOff = null
        val end = readBattery()
        if (end == null) {
            repository.status(MonitorStatus.WAITING_FOR_SCREEN_OFF, "Battery level unavailable. Try another session.")
            return
        }
        repository.record(DrainSession(start, end, pluggedInDuringSession || end.pluggedIn))
        repository.status(MonitorStatus.WAITING_FOR_SCREEN_OFF)
    }

    private fun readBattery(): BatterySample? {
        val wallTime = System.currentTimeMillis()
        val elapsed = SystemClock.elapsedRealtime()
        val intent = registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED)) ?: return null
        val capacity = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
        if (scale <= 0 || capacity !in 0..scale) return null
        return BatterySample(
            percent = (capacity * 100.0 / scale).roundToInt(),
            wallTimeMs = wallTime,
            elapsedTimeMs = elapsed,
            pluggedIn = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0) != 0,
        )
    }

    private fun notification(): Notification {
        val channel = NotificationChannel(CHANNEL, "Drain monitoring", NotificationManager.IMPORTANCE_LOW)
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
        val open = PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE,
        )
        val stop = PendingIntent.getService(
            this, 1, Intent(this, BatteryMonitorService::class.java).setAction(ACTION_STOP),
            PendingIntent.FLAG_IMMUTABLE,
        )
        return Notification.Builder(this, CHANNEL)
            .setSmallIcon(R.drawable.ic_monitor)
            .setContentTitle("Ghost Drain Hunter")
            .setContentText("Measuring battery use while your screen is off")
            .setOngoing(true)
            .setContentIntent(open)
            .addAction(Notification.Action.Builder(null, "Stop", stop).build())
            .build()
    }

    override fun onDestroy() {
        if (registered) unregisterReceiver(displayEvents)
        stopForeground(STOP_FOREGROUND_REMOVE)
        if (repository.state.monitoring != MonitorStatus.STOPPED) {
            repository.status(MonitorStatus.STOPPED)
        }
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        private const val CHANNEL = "drain_monitor"
        private const val NOTIFICATION_ID = 14
        private const val ACTION_STOP = "com.daemonhour.ghostdrain.STOP"
    }
}
