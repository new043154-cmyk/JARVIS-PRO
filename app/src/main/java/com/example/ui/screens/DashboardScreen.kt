package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.CompassCalibration
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AppScreen
import com.example.model.PersonaType
import com.example.ui.MainViewModel
import com.example.ui.components.JarvisOrb
import com.example.ui.theme.OrbitronFontFamily
import com.example.ui.theme.ShareTechMonoFontFamily
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val orbState by viewModel.orbState.collectAsState()
    val micLevel by viewModel.voiceManager.micLevel.collectAsState()
    val persona by viewModel.selectedPersona.collectAsState()
    val weatherData by viewModel.powerToolsManager.weatherData.collectAsState()
    val isWeatherLoading by viewModel.powerToolsManager.isWeatherLoading.collectAsState()
    val isFlashlightOn by viewModel.deviceActionManager.isFlashlightOn.collectAsState()
    val isListening by viewModel.voiceManager.isListening.collectAsState()
    val statusBanner by viewModel.statusBanner.collectAsState()

    val scope = rememberCoroutineScope()
    val battery = remember { viewModel.deviceActionManager.getBatteryStatus() }
    val storage = remember { viewModel.deviceActionManager.getStorageInfo() }

    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val surfaceColor = MaterialTheme.colorScheme.surface
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface

    val currentTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
    val currentDate = SimpleDateFormat("EEE // dd MMM yyyy", Locale.getDefault()).format(Date()).uppercase()

    val greeting = when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
        in 4..11 -> "MORNING PROTOCOL"
        in 12..16 -> "MIDDAY SURVEILLANCE"
        else -> "EVENING PROTOCOL"
    }
    val title = if (persona == PersonaType.JARVIS) "SIR" else "COMPANION"

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("dashboard_screen"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Futuristic Header Telemetry & Persona Pill
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(primaryColor)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "$greeting // $title",
                            fontFamily = OrbitronFontFamily,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp,
                            color = primaryColor
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "$currentDate • $currentTime UTC",
                        fontSize = 11.sp,
                        color = onSurfaceColor.copy(alpha = 0.65f),
                        fontFamily = ShareTechMonoFontFamily,
                        letterSpacing = 1.sp
                    )
                }

                // Persona Switcher Pill
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = primaryColor.copy(alpha = 0.12f),
                    border = CardDefaults.outlinedCardBorder().copy(
                        width = 1.dp,
                        brush = Brush.horizontalGradient(listOf(primaryColor, secondaryColor))
                    ),
                    modifier = Modifier.clickable {
                        val next = if (persona == PersonaType.JARVIS) PersonaType.PARI else PersonaType.JARVIS
                        viewModel.setPersona(next)
                    }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(primaryColor)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = persona.displayName,
                            fontFamily = OrbitronFontFamily,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = primaryColor
                        )
                    }
                }
            }
        }

        // Status banner if present
        if (!statusBanner.isNullOrEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = primaryColor.copy(alpha = 0.14f),
                    border = CardDefaults.outlinedCardBorder().copy(
                        width = 1.dp,
                        brush = Brush.linearGradient(listOf(primaryColor, Color.Transparent))
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = statusBanner ?: "",
                        fontFamily = ShareTechMonoFontFamily,
                        fontSize = 12.sp,
                        color = primaryColor,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        }

        // 2. Animated Arc Reactor AI Orb Core
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                JarvisOrb(
                    orbState = orbState,
                    micLevel = micLevel,
                    size = 230.dp,
                    onClick = {
                        viewModel.toggleVoiceListening()
                    }
                )
            }
        }

        // Voice quick trigger action button
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = { viewModel.toggleVoiceListening() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isListening) Color(0xFFFF1744) else primaryColor,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .height(48.dp)
                        .testTag("voice_trigger_button")
                ) {
                    Icon(
                        imageVector = if (isListening) Icons.Default.MicOff else Icons.Default.Mic,
                        contentDescription = "Voice"
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isListening) "DEACTIVATE TRANSDUCER" else "ENGAGE VOICE SYNAPSE",
                        fontFamily = OrbitronFontFamily,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
            }
        }

        // 3. Quick Action Chips Row
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "// TACTICAL SUBSYSTEMS",
                        fontFamily = OrbitronFontFamily,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp,
                        color = primaryColor.copy(alpha = 0.9f)
                    )
                    Text(
                        text = "READY",
                        fontFamily = ShareTechMonoFontFamily,
                        fontSize = 11.sp,
                        color = Color(0xFF00E676)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(horizontal = 2.dp)
                ) {
                    items(listOf(
                        QuickActionItem("Chat", Icons.AutoMirrored.Filled.Chat, AppScreen.CHAT),
                        QuickActionItem("Voice HUD", Icons.Default.Mic, AppScreen.VOICE),
                        QuickActionItem("Coding Studio", Icons.Default.Code, AppScreen.CODING),
                        QuickActionItem("Terminal", Icons.Default.Terminal, AppScreen.TERMINAL),
                        QuickActionItem("Automations", Icons.Default.Schedule, AppScreen.AUTOMATIONS),
                        QuickActionItem("Power Tools", Icons.Default.Bolt, AppScreen.POWER_TOOLS),
                        QuickActionItem("Memory", Icons.Default.Psychology, AppScreen.MEMORY),
                        QuickActionItem("Settings", Icons.Default.Settings, AppScreen.SETTINGS)
                    )) { item ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = surfaceColor,
                            border = CardDefaults.outlinedCardBorder().copy(
                                width = 1.dp,
                                brush = Brush.linearGradient(listOf(primaryColor.copy(alpha = 0.6f), Color.Transparent))
                            ),
                            modifier = Modifier
                                .clickable { viewModel.navigateTo(item.screen) }
                                .testTag("quick_action_${item.title.lowercase().replace(" ", "_")}")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.title,
                                    tint = primaryColor,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = item.title,
                                    fontFamily = OrbitronFontFamily,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = onSurfaceColor
                                )
                            }
                        }
                    }
                }
            }
        }

        // 4. Live Atmospheric Weather Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = surfaceColor),
                border = CardDefaults.outlinedCardBorder().copy(
                    width = 1.dp,
                    brush = Brush.horizontalGradient(listOf(primaryColor.copy(alpha = 0.5f), secondaryColor.copy(alpha = 0.2f), Color.Transparent))
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("weather_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.WbSunny,
                                contentDescription = "Weather",
                                tint = primaryColor
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "ATMOSPHERIC METRICS",
                                fontFamily = OrbitronFontFamily,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.5.sp,
                                color = primaryColor
                            )
                        }

                        IconButton(
                            onClick = {
                                scope.launch { viewModel.powerToolsManager.refreshWeather() }
                            },
                            enabled = !isWeatherLoading,
                            modifier = Modifier.size(32.dp)
                        ) {
                            if (isWeatherLoading) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = primaryColor)
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Refresh Weather",
                                    tint = primaryColor,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Column {
                            Text(
                                text = "${weatherData.temperature.toInt()}°C",
                                fontFamily = OrbitronFontFamily,
                                fontSize = 36.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = onSurfaceColor
                            )
                            Text(
                                text = "${weatherData.condition.uppercase()} // ${weatherData.city.uppercase()}",
                                fontFamily = ShareTechMonoFontFamily,
                                fontSize = 12.sp,
                                color = primaryColor
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "HUMIDITY: ${weatherData.humidity}%",
                                fontSize = 11.sp,
                                color = onSurfaceColor.copy(alpha = 0.7f),
                                fontFamily = ShareTechMonoFontFamily
                            )
                            Text(
                                text = "VELOCITY: ${weatherData.windSpeed} KM/H",
                                fontSize = 11.sp,
                                color = onSurfaceColor.copy(alpha = 0.7f),
                                fontFamily = ShareTechMonoFontFamily
                            )
                        }
                    }
                }
            }
        }

        // 5. Hardware Telemetry HUD Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = surfaceColor),
                border = CardDefaults.outlinedCardBorder().copy(
                    width = 1.dp,
                    brush = Brush.horizontalGradient(listOf(Color.Transparent, primaryColor.copy(alpha = 0.5f)))
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
                            Icon(
                                imageVector = Icons.Default.BatteryChargingFull,
                                contentDescription = "Battery",
                                tint = primaryColor
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "STARK REACTOR RESERVES",
                                fontFamily = OrbitronFontFamily,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.5.sp,
                                color = primaryColor
                            )
                        }

                        // Daily briefing button
                        OutlinedButton(
                            onClick = { viewModel.runDailyBriefing() },
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.height(30.dp)
                        ) {
                            Text(
                                text = "BRIEFING",
                                fontFamily = OrbitronFontFamily,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = primaryColor
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TelemetryMetric("POWER RESERVE", "${battery.percentage}%", if (battery.isCharging) "CHARGING" else "BATTERY")
                        TelemetryMetric("CORE TEMP", "${battery.temperatureCelsius}°C", "NOMINAL")
                        TelemetryMetric("STORAGE", "${String.format("%.1f", storage.availableGb)} GB", "FREE SPACE")
                        TelemetryMetric("TORCH", if (isFlashlightOn) "ONLINE" else "STANDBY", "HARDWARE")
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun TelemetryMetric(label: String, value: String, sub: String) {
    Column {
        Text(
            text = label,
            fontSize = 9.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
            fontFamily = ShareTechMonoFontFamily,
            letterSpacing = 0.5.sp
        )
        Text(
            text = value,
            fontFamily = OrbitronFontFamily,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = sub,
            fontSize = 9.sp,
            fontFamily = ShareTechMonoFontFamily,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }
}

private data class QuickActionItem(
    val title: String,
    val icon: ImageVector,
    val screen: AppScreen
)
