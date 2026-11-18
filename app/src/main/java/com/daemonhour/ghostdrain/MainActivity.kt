package com.daemonhour.ghostdrain

import android.Manifest
import com.daemonhour.ghostdrain.ui.screens.GhostDrainRoute
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModelProvider
import com.daemonhour.ghostdrain.contract.DrainEvent

class MainActivity : ComponentActivity() {
    private val notificationPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { /* Monitoring remains available even without notification permission. */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val viewModel = ViewModelProvider(this)[DrainViewModel::class.java]
        enableEdgeToEdge()
        setContent {
            MaterialTheme(colorScheme = darkColorScheme(
                primary = Color(0xFFAEF2B0),
                onPrimary = Color(0xFF0F371F),
                background = Color(0xFF111516),
                surface = Color(0xFF151B1B),
                surfaceVariant = Color(0xFF242D2C),
                onSurfaceVariant = Color(0xFFC0CDCA),
                errorContainer = Color(0xFF592D27),
                onErrorContainer = Color(0xFFFFDAD2),
            )) {
                GhostDrainRoute(
                    state = viewModel.state,
                    onEvent = { event ->
                        viewModel.onEvent(event)

                        if (event == DrainEvent.Start) {
                            requestNotificationOnce()
                        }
                    }
                )
            }
        }
    }

    private fun requestNotificationOnce() {
        if (Build.VERSION.SDK_INT < 33 ||
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        ) return
        val preferences = getPreferences(MODE_PRIVATE)
        if (preferences.getBoolean("asked_notifications", false)) return
        preferences.edit().putBoolean("asked_notifications", true).apply()
        notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
    }
}
