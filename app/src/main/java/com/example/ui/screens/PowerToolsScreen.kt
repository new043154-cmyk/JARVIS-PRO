package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.CompassCalibration
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.Mosque
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainViewModel
import kotlinx.coroutines.launch

@Composable
fun PowerToolsScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val isFlashlightOn by viewModel.deviceActionManager.isFlashlightOn.collectAsState()
    val compassAzimuth by viewModel.deviceActionManager.compassAzimuth.collectAsState()
    val speedTestState by viewModel.powerToolsManager.speedTestState.collectAsState()
    val isAntiTheftArmed by viewModel.powerToolsManager.isAntiTheftArmed.collectAsState()
    val isAntiTheftTriggered by viewModel.powerToolsManager.isAntiTheftTriggered.collectAsState()
    val expenses by viewModel.memoryManager.expenses.collectAsState()

    val scope = rememberCoroutineScope()
    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceColor = MaterialTheme.colorScheme.surface
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface

    val prayerTimes = remember { viewModel.powerToolsManager.calculatePrayerTimes() }
    val qiblaAngle = remember { viewModel.powerToolsManager.calculateQiblaDirection() }

    var expenseTitle by remember { mutableStateOf("") }
    var expenseAmount by remember { mutableStateOf("") }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("power_tools_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Column {
                Text(
                    text = "STARK POWER UTILITIES",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                    color = primaryColor
                )
                Text(
                    text = "Tactical sensors, hardware controls & telemetry utilities",
                    fontSize = 12.sp,
                    color = onSurfaceColor.copy(alpha = 0.6f)
                )
            }
        }

        // 1. Hardware Flashlight & SOS Emergency
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = surfaceColor),
                border = CardDefaults.outlinedCardBorder().copy(width = 1.dp, brush = Brush.horizontalGradient(listOf(primaryColor.copy(alpha = 0.5f), Color.Transparent))),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "HARDWARE ILLUMINATION & SOS",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = primaryColor
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { viewModel.deviceActionManager.toggleFlashlight() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isFlashlightOn) Color(0xFFFFD54F) else primaryColor.copy(alpha = 0.2f),
                                contentColor = if (isFlashlightOn) Color.Black else primaryColor
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("power_torch_button")
                        ) {
                            Icon(imageVector = Icons.Default.FlashlightOn, contentDescription = "Torch")
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (isFlashlightOn) "TORCH ON" else "TORCH OFF", fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { viewModel.deviceActionManager.openDialer("911") },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFFF1744).copy(alpha = 0.25f),
                                contentColor = Color(0xFFFF5252)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("sos_dialer_button")
                        ) {
                            Icon(imageVector = Icons.Default.Phone, contentDescription = "SOS")
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("EMERGENCY SOS", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        // 2. Real Internet Speed Test
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = surfaceColor),
                border = CardDefaults.outlinedCardBorder().copy(width = 1.dp, brush = Brush.horizontalGradient(listOf(Color.Transparent, primaryColor.copy(alpha = 0.5f)))),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Speed, contentDescription = "Speed Test", tint = primaryColor)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "NETWORK SPEED & LATENCY",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                                color = primaryColor
                            )
                        }

                        Button(
                            onClick = { scope.launch { viewModel.powerToolsManager.runSpeedTest() } },
                            enabled = !speedTestState.isRunning,
                            colors = ButtonDefaults.buttonColors(containerColor = primaryColor, contentColor = Color.Black),
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.height(32.dp).testTag("start_speed_test_button")
                        ) {
                            Text(if (speedTestState.isRunning) "Testing..." else "Run Test", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (speedTestState.isRunning) {
                        LinearProgressIndicator(
                            progress = { speedTestState.progress },
                            color = primaryColor,
                            modifier = Modifier.fillMaxWidth().height(6.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("DOWNLOAD SPEED", fontSize = 10.sp, fontFamily = FontFamily.Monospace, color = onSurfaceColor.copy(alpha = 0.6f))
                            Text(
                                text = "${String.format("%.1f", speedTestState.downloadSpeedMbps)} Mbps",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = primaryColor
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("PING LATENCY", fontSize = 10.sp, fontFamily = FontFamily.Monospace, color = onSurfaceColor.copy(alpha = 0.6f))
                            Text(
                                text = "${speedTestState.latencyMs} ms",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = onSurfaceColor
                            )
                        }
                    }
                }
            }
        }

        // 3. Sensor Compass & Qibla Direction
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = surfaceColor),
                border = CardDefaults.outlinedCardBorder().copy(width = 1.dp, brush = Brush.horizontalGradient(listOf(primaryColor.copy(alpha = 0.4f), Color.Transparent))),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "TACTICAL COMPASS & QIBLA DIRECTION",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = primaryColor
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Rotating needle dial
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(90.dp)
                                .background(Color(0xFF090D17), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CompassCalibration,
                                contentDescription = "Compass",
                                tint = primaryColor,
                                modifier = Modifier
                                    .size(60.dp)
                                    .rotate(compassAzimuth)
                            )
                        }

                        Column {
                            Text("HEADING AZIMUTH", fontSize = 10.sp, fontFamily = FontFamily.Monospace, color = onSurfaceColor.copy(alpha = 0.6f))
                            Text("${compassAzimuth.toInt()}° N", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = primaryColor)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("QIBLA BEARING", fontSize = 10.sp, fontFamily = FontFamily.Monospace, color = onSurfaceColor.copy(alpha = 0.6f))
                            Text("${qiblaAngle.toInt()}° (Kaaba)", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF00E676))
                        }
                    }
                }
            }
        }

        // 4. Astronomical Prayer Times
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = surfaceColor),
                border = CardDefaults.outlinedCardBorder().copy(width = 1.dp, brush = Brush.horizontalGradient(listOf(Color.Transparent, primaryColor.copy(alpha = 0.4f)))),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Mosque, contentDescription = "Prayer Times", tint = primaryColor)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "ASTRONOMICAL PRAYER SCHEDULE",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            color = primaryColor
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        PrayerTimeCol("Fajr", prayerTimes.fajr)
                        PrayerTimeCol("Sunrise", prayerTimes.sunrise)
                        PrayerTimeCol("Dhuhr", prayerTimes.dhuhr)
                        PrayerTimeCol("Asr", prayerTimes.asr)
                        PrayerTimeCol("Maghrib", prayerTimes.maghrib)
                        PrayerTimeCol("Isha", prayerTimes.isha)
                    }
                }
            }
        }

        // 5. Anti-Theft Motion Sensor Guard
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = surfaceColor),
                border = CardDefaults.outlinedCardBorder().copy(
                    width = 1.dp,
                    brush = Brush.horizontalGradient(
                        if (isAntiTheftTriggered) listOf(Color(0xFFFF1744), Color(0xFFFF5252))
                        else listOf(primaryColor.copy(alpha = 0.5f), Color.Transparent)
                    )
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Security, contentDescription = "Security", tint = if (isAntiTheftTriggered) Color(0xFFFF1744) else primaryColor)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isAntiTheftTriggered) "INTRUDER ALERT TRIGGERED!" else "ANTI-THEFT MOTION GUARD",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isAntiTheftTriggered) Color(0xFFFF1744) else primaryColor
                            )
                        }

                        Button(
                            onClick = {
                                if (isAntiTheftArmed) viewModel.powerToolsManager.disarmAntiTheft()
                                else viewModel.powerToolsManager.armAntiTheft()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isAntiTheftArmed) Color(0xFFFF1744) else primaryColor,
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.height(32.dp).testTag("anti_theft_toggle")
                        ) {
                            Text(if (isAntiTheftArmed) "DISARM" else "ARM SENSOR", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (isAntiTheftArmed) "Armed: Device motion will sound alarm tone and vibrate." else "Disarmed: Arm device before leaving on table to detect movement.",
                        fontSize = 12.sp,
                        color = onSurfaceColor.copy(alpha = 0.7f)
                    )
                }
            }
        }

        // 6. Device Settings Direct Launchers
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = surfaceColor),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "SYSTEM SETTINGS SHORTCUTS",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = primaryColor
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { viewModel.deviceActionManager.openSettings("wifi") },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Wifi, contentDescription = "Wi-Fi", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Wi-Fi", fontSize = 11.sp)
                        }
                        OutlinedButton(
                            onClick = { viewModel.deviceActionManager.openSettings("bluetooth") },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Bluetooth, contentDescription = "BT", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Bluetooth", fontSize = 11.sp)
                        }
                        OutlinedButton(
                            onClick = { viewModel.deviceActionManager.openSettings("sound") },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = "Sound", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Sound", fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        // 7. Expense Tracker
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = surfaceColor),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.AttachMoney, contentDescription = "Expenses", tint = primaryColor)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "STARK EXPENSE TRACKER",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            color = primaryColor
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = expenseTitle,
                            onValueChange = { expenseTitle = it },
                            placeholder = { Text("Item", fontSize = 12.sp) },
                            singleLine = true,
                            modifier = Modifier.weight(1.5f)
                        )
                        OutlinedTextField(
                            value = expenseAmount,
                            onValueChange = { expenseAmount = it },
                            placeholder = { Text("$$$", fontSize = 12.sp) },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        Button(
                            onClick = {
                                val amt = expenseAmount.toDoubleOrNull()
                                if (expenseTitle.isNotBlank() && amt != null) {
                                    viewModel.memoryManager.addExpense(expenseTitle, amt, "General")
                                    expenseTitle = ""
                                    expenseAmount = ""
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = primaryColor, contentColor = Color.Black),
                            modifier = Modifier.height(54.dp)
                        ) {
                            Text("Add")
                        }
                    }

                    if (expenses.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        val total = expenses.sumOf { it.amount }
                        Text(
                            text = "Total Logged: $${String.format("%.2f", total)} (${expenses.size} items)",
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = primaryColor
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
private fun PrayerTimeCol(name: String, time: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = name,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        Text(
            text = time,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}
