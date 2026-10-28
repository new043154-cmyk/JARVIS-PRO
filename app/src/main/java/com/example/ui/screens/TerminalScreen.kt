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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ThemeType
import com.example.ui.MainViewModel
import kotlinx.coroutines.launch

@Composable
fun TerminalScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val history by viewModel.terminalEngine.history.collectAsState()
    val persona by viewModel.selectedPersona.collectAsState()

    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceColor = MaterialTheme.colorScheme.surface

    var commandInput by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    LaunchedEffect(history.size) {
        if (history.isNotEmpty()) {
            listState.animateScrollToItem(history.size - 1)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF06090F))
            .imePadding()
            .testTag("terminal_screen")
    ) {
        // Terminal Window Header Bar (Ubuntu style)
        Surface(color = Color(0xFF101522), modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Traffic light buttons
                    Box(modifier = Modifier.size(10.dp).background(Color(0xFFFF5F56), CircleShape))
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(modifier = Modifier.size(10.dp).background(Color(0xFFFFBD2E), CircleShape))
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(modifier = Modifier.size(10.dp).background(Color(0xFF27C93F), CircleShape))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "jarvis@stark-core: ~",
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Color.White
                    )
                }

                Text(
                    text = "BASH 5.2",
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    color = primaryColor
                )
            }
        }

        // Quick Command Pills
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF090D17))
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(listOf(
                "help", "status", "battery", "torch on", "torch off", "time", "ls", "calc 42*15", "clear"
            )) { cmd ->
                FilterChip(
                    selected = false,
                    onClick = {
                        commandInput = cmd
                        scope.launch {
                            viewModel.terminalEngine.executeCommand(cmd, persona) { themeArg ->
                                applyThemeFromTerminal(viewModel, themeArg)
                            }
                        }
                    },
                    label = { Text(cmd, fontSize = 11.sp, fontFamily = FontFamily.Monospace) },
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = Color(0xFF141A29),
                        labelColor = primaryColor
                    )
                )
            }
        }

        // Output Log Stream
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(history) { line ->
                val textColor = when {
                    line.isCommand -> Color(0xFFFFD54F) // Yellow prompt
                    line.isError -> Color(0xFFFF5252) // Red error
                    line.isSuccess -> Color(0xFF00E676) // Green success
                    else -> Color(0xFF80DEEA) // Cyan terminal text
                }

                Text(
                    text = line.text,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    color = textColor,
                    lineHeight = 16.sp
                )
            }
        }

        // Bottom Input Row
        Surface(color = Color(0xFF0B101C), modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$ ",
                    fontSize = 14.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = primaryColor,
                    modifier = Modifier.padding(start = 6.dp)
                )

                OutlinedTextField(
                    value = commandInput,
                    onValueChange = { commandInput = it },
                    placeholder = {
                        Text(
                            "Type command or 'help'...",
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            color = Color.Gray
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = primaryColor,
                        unfocusedBorderColor = Color.Transparent,
                        cursorColor = primaryColor
                    ),
                    textStyle = androidx.compose.ui.text.TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 13.sp,
                        color = Color.White
                    ),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(onSend = {
                        val toRun = commandInput
                        commandInput = ""
                        scope.launch {
                            viewModel.terminalEngine.executeCommand(toRun, persona) { themeArg ->
                                applyThemeFromTerminal(viewModel, themeArg)
                            }
                        }
                    }),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("terminal_input_field")
                )

                IconButton(
                    onClick = {
                        val toRun = commandInput
                        commandInput = ""
                        scope.launch {
                            viewModel.terminalEngine.executeCommand(toRun, persona) { themeArg ->
                                applyThemeFromTerminal(viewModel, themeArg)
                            }
                        }
                    },
                    enabled = commandInput.isNotBlank(),
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Execute",
                        tint = if (commandInput.isNotBlank()) primaryColor else Color.Gray
                    )
                }
            }
        }
    }
}

private fun applyThemeFromTerminal(viewModel: MainViewModel, arg: String) {
    val theme = when {
        arg.contains("iron") -> ThemeType.IRON_HUD
        arg.contains("neon") || arg.contains("cyber") -> ThemeType.CYBERPUNK_NEON
        arg.contains("emerald") || arg.contains("matrix") -> ThemeType.EMERALD_OBSIDIAN
        arg.contains("sunset") || arg.contains("plasma") -> ThemeType.SUNSET_PLASMA
        arg.contains("onyx") -> ThemeType.MIDNIGHT_ONYX
        arg.contains("rose") -> ThemeType.ROSE_GOLD
        arg.contains("white") || arg.contains("light") -> ThemeType.PURE_WHITE
        arg.contains("red") || arg.contains("reactor") -> ThemeType.RED_REACTOR
        arg.contains("purple") || arg.contains("quantum") -> ThemeType.QUANTUM_PURPLE
        else -> ThemeType.MIDNIGHT_DARK
    }
    viewModel.setTheme(theme)
}
