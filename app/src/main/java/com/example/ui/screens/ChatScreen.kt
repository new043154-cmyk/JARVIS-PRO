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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ChatMessage
import com.example.model.MessageRole
import com.example.ui.MainViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ChatScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val messages by viewModel.messages.collectAsState()
    val chatInput by viewModel.chatInput.collectAsState()
    val isListening by viewModel.voiceManager.isListening.collectAsState()
    val isSpeaking by viewModel.voiceManager.isSpeaking.collectAsState()
    val persona by viewModel.selectedPersona.collectAsState()

    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceColor = MaterialTheme.colorScheme.surface
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface

    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .imePadding()
            .testTag("chat_screen")
    ) {
        // Chat Header Bar
        Surface(
            color = surfaceColor,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "NEURAL CONVERSATION",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = primaryColor
                    )
                    Text(
                        text = "Active Persona: ${persona.displayName}",
                        fontSize = 11.sp,
                        color = onSurfaceColor.copy(alpha = 0.6f)
                    )
                }

                if (isSpeaking) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFFF1744).copy(alpha = 0.2f),
                        modifier = Modifier.clip(RoundedCornerShape(12.dp))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            IconButton(
                                onClick = { viewModel.stopSpeaking() },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Stop,
                                    contentDescription = "Stop Speech",
                                    tint = Color(0xFFFF1744),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Text("Speaking", fontSize = 11.sp, color = Color(0xFFFF1744))
                        }
                    }
                }
            }
        }

        // Messages list
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(messages) { message ->
                MessageBubble(
                    message = message,
                    onSpeak = { viewModel.voiceManager.speak(message.text) },
                    onCopy = { viewModel.deviceActionManager.copyToClipboard("JARVIS Message", message.text) }
                )
            }
        }

        // Input control row
        Surface(
            color = surfaceColor,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Speech to text toggle
                IconButton(
                    onClick = { viewModel.toggleVoiceListening() },
                    modifier = Modifier
                        .size(44.dp)
                        .background(
                            if (isListening) Color(0xFFFF1744).copy(alpha = 0.2f) else primaryColor.copy(alpha = 0.12f),
                            CircleShape
                        )
                ) {
                    Icon(
                        imageVector = if (isListening) Icons.Default.MicOff else Icons.Default.Mic,
                        contentDescription = "Microphone",
                        tint = if (isListening) Color(0xFFFF1744) else primaryColor
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                OutlinedTextField(
                    value = chatInput,
                    onValueChange = { viewModel.setChatInput(it) },
                    placeholder = {
                        Text(
                            "Ask JARVIS or command device...",
                            fontSize = 13.sp,
                            color = onSurfaceColor.copy(alpha = 0.5f)
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = primaryColor,
                        unfocusedBorderColor = primaryColor.copy(alpha = 0.3f),
                        cursorColor = primaryColor
                    ),
                    shape = RoundedCornerShape(24.dp),
                    maxLines = 4,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("chat_input_field")
                )

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = { viewModel.sendMessage() },
                    enabled = chatInput.isNotBlank(),
                    modifier = Modifier
                        .size(44.dp)
                        .background(
                            if (chatInput.isNotBlank()) primaryColor else primaryColor.copy(alpha = 0.2f),
                            CircleShape
                        )
                        .testTag("chat_send_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send",
                        tint = if (chatInput.isNotBlank()) Color.Black else onSurfaceColor.copy(alpha = 0.4f)
                    )
                }
            }
        }
    }
}

@Composable
private fun MessageBubble(
    message: ChatMessage,
    onSpeak: () -> Unit,
    onCopy: () -> Unit
) {
    val isUser = message.role == MessageRole.USER
    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceColor = MaterialTheme.colorScheme.surface
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface

    val timeFormatted = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(message.timestamp))

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        // Role & Time header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
        ) {
            Text(
                text = if (isUser) "USER" else message.modelUsed,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = if (isUser) onSurfaceColor.copy(alpha = 0.6f) else primaryColor,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = timeFormatted,
                fontSize = 10.sp,
                color = onSurfaceColor.copy(alpha = 0.4f),
                fontFamily = FontFamily.Monospace
            )
        }

        // Bubble body
        Card(
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isUser) 16.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 16.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = if (isUser) primaryColor.copy(alpha = 0.18f) else surfaceColor
            ),
            border = CardDefaults.outlinedCardBorder().copy(
                width = 1.dp,
                brush = Brush.horizontalGradient(
                    if (isUser) listOf(primaryColor.copy(alpha = 0.4f), primaryColor.copy(alpha = 0.8f))
                    else listOf(primaryColor.copy(alpha = 0.5f), Color.Transparent)
                )
            ),
            modifier = Modifier
                .fillMaxWidth(if (message.text.length > 50) 0.95f else 0.85f)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                // Parse potential code blocks in message
                val parts = splitCodeBlocks(message.text)
                parts.forEach { part ->
                    if (part.isCode) {
                        CodeBlockCard(language = part.language, code = part.text)
                    } else {
                        Text(
                            text = part.text,
                            fontSize = 14.sp,
                            color = onSurfaceColor,
                            lineHeight = 20.sp
                        )
                    }
                }

                // Assistant action bar (Speak aloud, Copy)
                if (!isUser) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onSpeak,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Listen to response",
                                tint = primaryColor,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        IconButton(
                            onClick = onCopy,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy message",
                                tint = primaryColor,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CodeBlockCard(language: String, code: String) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val context = LocalContext.current

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFF070B14),
        border = CardDefaults.outlinedCardBorder().copy(width = 1.dp, brush = Brush.horizontalGradient(listOf(primaryColor.copy(alpha = 0.5f), Color.Transparent))),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = language.ifEmpty { "code" }.uppercase(),
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = primaryColor
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = code,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                color = Color(0xFF80DEEA),
                lineHeight = 16.sp
            )
        }
    }
}

private data class MessagePart(val text: String, val isCode: Boolean = false, val language: String = "")

private fun splitCodeBlocks(raw: String): List<MessagePart> {
    val result = mutableListOf<MessagePart>()
    val codeRegex = Regex("```([a-zA-Z0-9_]*)\\n?([\\s\\S]*?)```")
    var lastIndex = 0

    codeRegex.findAll(raw).forEach { match ->
        val start = match.range.first
        if (start > lastIndex) {
            val textBefore = raw.substring(lastIndex, start)
            if (textBefore.isNotBlank()) {
                result.add(MessagePart(text = textBefore, isCode = false))
            }
        }
        val lang = match.groupValues[1]
        val code = match.groupValues[2]
        result.add(MessagePart(text = code, isCode = true, language = lang))
        lastIndex = match.range.last + 1
    }

    if (lastIndex < raw.length) {
        val remaining = raw.substring(lastIndex)
        if (remaining.isNotBlank()) {
            result.add(MessagePart(text = remaining, isCode = false))
        }
    }

    if (result.isEmpty()) {
        result.add(MessagePart(text = raw, isCode = false))
    }

    return result
}
