package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.MessageRole
import com.example.model.PersonaType
import com.example.ui.MainViewModel
import com.example.ui.components.JarvisOrb
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun VoiceScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val orbState by viewModel.orbState.collectAsState()
    val micLevel by viewModel.voiceManager.micLevel.collectAsState()
    val isListening by viewModel.voiceManager.isListening.collectAsState()
    val isSpeaking by viewModel.voiceManager.isSpeaking.collectAsState()
    val currentLanguage by viewModel.voiceManager.currentLanguage.collectAsState()
    val persona by viewModel.selectedPersona.collectAsState()
    val messages by viewModel.messages.collectAsState()

    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceColor = MaterialTheme.colorScheme.surface
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface

    var pitchSlider by remember { mutableFloatStateOf(persona.defaultVoicePitch) }
    var rateSlider by remember { mutableFloatStateOf(persona.defaultSpeechRate) }

    val lastAssistantMessage = messages.lastOrNull { it.role == MessageRole.ASSISTANT }?.text
    val lastUserMessage = messages.lastOrNull { it.role == MessageRole.USER }?.text

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("voice_screen"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Title HUD
        Text(
            text = "ACOUSTIC NEURAL HUD",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp,
            color = primaryColor
        )
        Text(
            text = "Live speech processing & synthesis engine",
            fontSize = 12.sp,
            color = onSurfaceColor.copy(alpha = 0.6f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Large Responsive Arc Reactor Orb
        JarvisOrb(
            orbState = orbState,
            micLevel = micLevel,
            size = 220.dp,
            onClick = { viewModel.toggleVoiceListening() }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Pink & Cyan Audio Level Meter Bars
        PinkAudioWaveformVisualizer(
            micLevel = micLevel,
            isListening = isListening,
            isSpeaking = isSpeaking,
            primaryColor = primaryColor,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .padding(horizontal = 24.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Large Push-to-Talk / Toggle Voice Button
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
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
                    .height(52.dp)
                    .testTag("voice_screen_listen_button")
            ) {
                Icon(
                    imageVector = if (isListening) Icons.Default.MicOff else Icons.Default.Mic,
                    contentDescription = "Mic"
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isListening) "STOP LISTENING" else "START LISTENING",
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            if (isSpeaking) {
                IconButton(
                    onClick = { viewModel.stopSpeaking() },
                    modifier = Modifier
                        .size(52.dp)
                        .background(Color(0xFFFF1744).copy(alpha = 0.2f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Stop,
                        contentDescription = "Stop Speech",
                        tint = Color(0xFFFF1744)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Persona & Language Selection
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = surfaceColor),
            border = CardDefaults.outlinedCardBorder().copy(width = 1.dp, brush = Brush.horizontalGradient(listOf(primaryColor.copy(alpha = 0.4f), Color.Transparent))),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "ASSISTANT PERSONA",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = primaryColor
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    PersonaType.values().forEach { p ->
                        FilterChip(
                            selected = persona == p,
                            onClick = {
                                viewModel.setPersona(p)
                                pitchSlider = p.defaultVoicePitch
                                rateSlider = p.defaultSpeechRate
                            },
                            label = { Text(p.displayName, fontWeight = FontWeight.Bold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = primaryColor,
                                selectedLabelColor = Color.Black
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "SPEECH LANGUAGE",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = primaryColor
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(
                        "en-US" to "English (US)",
                        "bn-BD" to "বাংলা (Bengali)",
                        "hi-IN" to "हिन्दी (Hindi)"
                    ).forEach { (code, label) ->
                        FilterChip(
                            selected = currentLanguage == code,
                            onClick = { viewModel.voiceManager.setLanguage(code) },
                            label = { Text(label, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = primaryColor,
                                selectedLabelColor = Color.Black
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Voice pitch slider
                Text(
                    text = "Voice Pitch: ${String.format("%.2f", pitchSlider)}x",
                    fontSize = 12.sp,
                    color = onSurfaceColor.copy(alpha = 0.8f)
                )
                Slider(
                    value = pitchSlider,
                    onValueChange = {
                        pitchSlider = it
                        viewModel.voiceManager.setVoiceParameters(pitchSlider, rateSlider)
                    },
                    valueRange = 0.6f..1.6f,
                    colors = SliderDefaults.colors(thumbColor = primaryColor, activeTrackColor = primaryColor)
                )

                // Voice rate slider
                Text(
                    text = "Speech Rate: ${String.format("%.2f", rateSlider)}x",
                    fontSize = 12.sp,
                    color = onSurfaceColor.copy(alpha = 0.8f)
                )
                Slider(
                    value = rateSlider,
                    onValueChange = {
                        rateSlider = it
                        viewModel.voiceManager.setVoiceParameters(pitchSlider, rateSlider)
                    },
                    valueRange = 0.7f..1.5f,
                    colors = SliderDefaults.colors(thumbColor = primaryColor, activeTrackColor = primaryColor)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Latest transcript display
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = surfaceColor),
            border = CardDefaults.outlinedCardBorder().copy(width = 1.dp, brush = Brush.horizontalGradient(listOf(Color.Transparent, primaryColor.copy(alpha = 0.4f)))),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "LATEST TRANSCRIPT",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = primaryColor
                )
                if (!lastUserMessage.isNullOrEmpty()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "You: \"$lastUserMessage\"",
                        fontSize = 13.sp,
                        color = onSurfaceColor
                    )
                }
                if (!lastAssistantMessage.isNullOrEmpty()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "${persona.displayName}: \"$lastAssistantMessage\"",
                        fontSize = 13.sp,
                        color = primaryColor.copy(alpha = 0.9f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun PinkAudioWaveformVisualizer(
    micLevel: Float,
    isListening: Boolean,
    isSpeaking: Boolean,
    primaryColor: Color,
    modifier: Modifier = Modifier
) {
    val pinkColor = Color(0xFFFF4081) // Neon pink
    val cyanColor = Color(0xFF00E5FF)

    Canvas(modifier = modifier) {
        val barCount = 28
        val spacing = 4.dp.toPx()
        val totalSpacing = spacing * (barCount - 1)
        val barWidth = (size.width - totalSpacing) / barCount
        val maxHeight = size.height

        for (i in 0 until barCount) {
            val progress = (i.toFloat() / barCount)
            val centerDist = 1f - kotlin.math.abs(progress - 0.5f) * 2f

            val dynamicHeight = when {
                isListening -> (maxHeight * (0.15f + (micLevel * centerDist * 0.85f))).coerceIn(4f, maxHeight)
                isSpeaking -> {
                    val pseudoRandom = (sin((i * 1.5 + System.currentTimeMillis() * 0.008).toDouble()).toFloat() + 1f) / 2f
                    (maxHeight * (0.2f + pseudoRandom * 0.7f)).coerceIn(4f, maxHeight)
                }
                else -> maxHeight * 0.12f
            }

            val barColor = if (i % 2 == 0) pinkColor else cyanColor

            val left = i * (barWidth + spacing)
            val top = (maxHeight - dynamicHeight) / 2f

            drawRoundRect(
                color = barColor,
                topLeft = Offset(left, top),
                size = Size(barWidth, dynamicHeight),
                cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
            )
        }
    }
}
