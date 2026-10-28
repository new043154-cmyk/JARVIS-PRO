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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ai.AIResponseResult
import com.example.model.AIProviderConfig
import com.example.model.AIProviderType
import com.example.model.PersonaType
import com.example.model.ThemeType
import com.example.ui.MainViewModel
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val selectedTheme by viewModel.selectedTheme.collectAsState()
    val persona by viewModel.selectedPersona.collectAsState()
    val currentLanguage by viewModel.voiceManager.currentLanguage.collectAsState()

    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceColor = MaterialTheme.colorScheme.surface
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val scope = rememberCoroutineScope()

    val activeProvider = remember { viewModel.aiProviderManager.getActiveProvider() }
    var apiKeyInput by remember { mutableStateOf(activeProvider.apiKey) }
    var modelIdInput by remember { mutableStateOf(activeProvider.modelId) }
    var baseUrlInput by remember { mutableStateOf(activeProvider.baseUrl) }
    var isApiKeyVisible by remember { mutableStateOf(false) }
    var testResultText by remember { mutableStateOf<String?>(null) }
    var isTestingConnection by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("settings_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Column {
                Text(
                    text = "SYSTEM CONFIGURATION",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                    color = primaryColor
                )
                Text(
                    text = "Manage neural models, futuristic theme styling & voice parameters",
                    fontSize = 12.sp,
                    color = onSurfaceColor.copy(alpha = 0.6f)
                )
            }
        }

        // 1. AI Provider & Model Manager
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = surfaceColor),
                border = CardDefaults.outlinedCardBorder().copy(width = 1.dp, brush = Brush.horizontalGradient(listOf(primaryColor.copy(alpha = 0.5f), Color.Transparent))),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Hub, contentDescription = "AI", tint = primaryColor)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "AI PROVIDER & NEURAL PIPELINE",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            color = primaryColor
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = baseUrlInput,
                        onValueChange = { baseUrlInput = it },
                        label = { Text("API Base URL") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("api_base_url_field")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = modelIdInput,
                        onValueChange = { modelIdInput = it },
                        label = { Text("Model Identifier") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("model_id_field")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = apiKeyInput,
                        onValueChange = { apiKeyInput = it },
                        label = { Text("Secret API Key") },
                        singleLine = true,
                        visualTransformation = if (isApiKeyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { isApiKeyVisible = !isApiKeyVisible }) {
                                Icon(
                                    imageVector = if (isApiKeyVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = "Toggle Visibility"
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth().testTag("api_key_field")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = {
                                val updated = activeProvider.copy(
                                    apiKey = apiKeyInput,
                                    modelId = modelIdInput,
                                    baseUrl = baseUrlInput
                                )
                                viewModel.aiProviderManager.addOrUpdateProvider(updated)
                                testResultText = "Configuration saved successfully."
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = primaryColor, contentColor = Color.Black),
                            modifier = Modifier.testTag("save_provider_button")
                        ) {
                            Text("Save Config", fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = {
                                isTestingConnection = true
                                testResultText = "Testing connection to endpoint..."
                                scope.launch {
                                    val res = viewModel.aiProviderManager.generateResponse(
                                        "Say 'Connection Verified'",
                                        emptyList(),
                                        persona
                                    )
                                    isTestingConnection = false
                                    testResultText = when (res) {
                                        is AIResponseResult.Success -> "Success: ${res.text.take(60)}"
                                        is AIResponseResult.Error -> "Failed: ${res.message}"
                                    }
                                }
                            },
                            enabled = !isTestingConnection
                        ) {
                            Text("Test Pipeline")
                        }
                    }

                    if (!testResultText.isNullOrEmpty()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = testResultText ?: "",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            color = primaryColor
                        )
                    }
                }
            }
        }

        // 2. 10 Futuristic Selectable Themes
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = surfaceColor),
                border = CardDefaults.outlinedCardBorder().copy(width = 1.dp, brush = Brush.horizontalGradient(listOf(Color.Transparent, primaryColor.copy(alpha = 0.5f)))),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Palette, contentDescription = "Themes", tint = primaryColor)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "FUTURISTIC THEMES (10 AVAILABLE)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            color = primaryColor
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    ThemeType.values().forEach { theme ->
                        val isSelected = selectedTheme == theme
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) primaryColor.copy(alpha = 0.15f) else Color.Transparent,
                            border = CardDefaults.outlinedCardBorder().copy(
                                width = 1.dp,
                                brush = Brush.horizontalGradient(
                                    if (isSelected) listOf(primaryColor, Color.Transparent)
                                    else listOf(Color.Transparent, Color.Transparent)
                                )
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable { viewModel.setTheme(theme) }
                                .testTag("theme_option_${theme.name.lowercase()}")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(16.dp)
                                            .clip(CircleShape)
                                            .background(getThemePreviewColor(theme))
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = theme.displayName,
                                        fontSize = 13.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) primaryColor else onSurfaceColor
                                    )
                                }

                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Active",
                                        tint = primaryColor,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 3. Dual Personas & Speech Diagnostics
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = surfaceColor),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Person, contentDescription = "Persona", tint = primaryColor)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "ASSISTANT PERSONAS & VOICE",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            color = primaryColor
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    PersonaType.values().forEach { p ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (persona == p) primaryColor.copy(alpha = 0.15f) else Color.Transparent,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable { viewModel.setPersona(p) }
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = p.displayName,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (persona == p) primaryColor else onSurfaceColor
                                )
                                Text(
                                    text = p.description,
                                    fontSize = 11.sp,
                                    color = onSurfaceColor.copy(alpha = 0.6f)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = {
                            viewModel.voiceManager.speak("Testing speech synthesis diagnostics. All Stark voice modules operational.")
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.VolumeUp, contentDescription = "Test Speech", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Run Voice Diagnostic Test")
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

private fun getThemePreviewColor(theme: ThemeType): Color {
    return when (theme) {
        ThemeType.MIDNIGHT_DARK -> Color(0xFF00E5FF)
        ThemeType.IRON_HUD -> Color(0xFFFFD700)
        ThemeType.CYBERPUNK_NEON -> Color(0xFFFF007F)
        ThemeType.EMERALD_OBSIDIAN -> Color(0xFF00E676)
        ThemeType.SUNSET_PLASMA -> Color(0xFFFF6D00)
        ThemeType.MIDNIGHT_ONYX -> Color(0xFFE0E0E0)
        ThemeType.ROSE_GOLD -> Color(0xFFF48FB1)
        ThemeType.PURE_WHITE -> Color(0xFF0077CC)
        ThemeType.RED_REACTOR -> Color(0xFFFF2A4B)
        ThemeType.QUANTUM_PURPLE -> Color(0xFFB388FF)
    }
}
