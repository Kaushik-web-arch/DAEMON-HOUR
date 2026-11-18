package com.daemonhour.ghostdrain.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale

/**
 * PS 14 Requirement: Flag "Something is draining your phone in your pocket" above a set rate.
 */
@Composable
fun HighDrainAlertBanner(
    drainRatePerHour: Double,
    thresholdPerHour: Float,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("high_drain_alert_banner"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFFEBEE) // Light red container
        ),
        border = BorderStroke(2.dp, Color(0xFFD32F2F)) // Prominent red border
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = "High Drain Alert",
                tint = Color(0xFFD32F2F),
                modifier = Modifier.size(36.dp)
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "High Drain Alert!",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFB71C1C)
                    )
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Something is draining your phone in your pocket",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFC62828),
                        fontSize = 14.sp
                    ),
                    modifier = Modifier.testTag("alert_message_text")
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = String.format(
                        Locale.getDefault(),
                        "Detected rate: %.1f%%/hr (Threshold: %.1f%%/hr)",
                        drainRatePerHour,
                        thresholdPerHour
                    ),
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Color(0xFF5D4037)
                    )
                )
            }
        }
    }
}

