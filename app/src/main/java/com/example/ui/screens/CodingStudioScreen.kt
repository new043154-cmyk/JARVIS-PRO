package com.example.ui.screens

import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.ui.MainViewModel

@Composable
fun CodingStudioScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val files by viewModel.codingProjectManager.files.collectAsState()
    val activeIndex by viewModel.codingProjectManager.activeFileIndex.collectAsState()

    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceColor = MaterialTheme.colorScheme.surface
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface

    var isPreviewMode by remember { mutableStateOf(false) }
    var showNewFileDialog by remember { mutableStateOf(false) }
    var newFileName by remember { mutableStateOf("") }
    var newFileLang by remember { mutableStateOf("java") }

    val activeFile = if (activeIndex in files.indices) files[activeIndex] else null

    Column(
        modifier = modifier
            .fillMaxSize()
            .imePadding()
            .testTag("coding_studio_screen")
    ) {
        // Studio Top Bar
        Surface(color = surfaceColor, modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Code,
                        contentDescription = "Code",
                        tint = primaryColor
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "CODING STUDIO",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = primaryColor
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    // AI Explain
                    OutlinedButton(
                        onClick = { viewModel.explainCurrentCode() },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Psychology, contentDescription = "Explain", modifier = Modifier.size(14.dp), tint = primaryColor)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Explain", fontSize = 11.sp, color = primaryColor)
                    }

                    // AI Fix
                    OutlinedButton(
                        onClick = { viewModel.suggestFixForCode() },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Icon(imageVector = Icons.Default.AutoFixHigh, contentDescription = "Fix", modifier = Modifier.size(14.dp), tint = primaryColor)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Fix", fontSize = 11.sp, color = primaryColor)
                    }

                    // Live Web Preview toggle for HTML projects
                    Button(
                        onClick = { isPreviewMode = !isPreviewMode },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isPreviewMode) Color(0xFFFF4081) else primaryColor,
                            contentColor = Color.Black
                        ),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Preview", modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (isPreviewMode) "Editor" else "Preview", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // File Tabs Bar
        Surface(color = Color(0xFF070B14), modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                LazyRow(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    itemsIndexed(files) { index, file ->
                        val isSelected = index == activeIndex
                        Surface(
                            shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp),
                            color = if (isSelected) surfaceColor else Color(0xFF0F1522),
                            border = CardDefaults.outlinedCardBorder().copy(
                                width = 1.dp,
                                brush = Brush.verticalGradient(
                                    if (isSelected) listOf(primaryColor, Color.Transparent)
                                    else listOf(Color.Transparent, Color.Transparent)
                                )
                            ),
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = file.name,
                                    fontSize = 12.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = if (isSelected) primaryColor else onSurfaceColor.copy(alpha = 0.6f),
                                    modifier = Modifier.padding(end = 4.dp)
                                )
                                if (files.size > 1) {
                                    IconButton(
                                        onClick = { viewModel.codingProjectManager.deleteFile(index) },
                                        modifier = Modifier.size(16.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Close Tab",
                                            tint = onSurfaceColor.copy(alpha = 0.5f),
                                            modifier = Modifier.size(12.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // New file button
                IconButton(
                    onClick = { showNewFileDialog = true },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "New File", tint = primaryColor)
                }
            }
        }

        // Main Editor or Live Preview Area
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(Color(0xFF080C14))
        ) {
            if (isPreviewMode) {
                // Live HTML WebView Runner
                val htmlContent = if (activeFile?.language == "html") {
                    activeFile.content
                } else {
                    files.firstOrNull { it.language == "html" }?.content
                        ?: "<html><body style='background:#0a0e1a;color:#00e5ff;font-family:monospace;padding:20px;'><h3>Select an HTML file to run live in WebView</h3></body></html>"
                }

                AndroidView(
                    factory = { ctx ->
                        WebView(ctx).apply {
                            settings.javaScriptEnabled = true
                            settings.domStorageEnabled = true
                            webViewClient = WebViewClient()
                            loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null)
                        }
                    },
                    update = { webView ->
                        webView.loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null)
                    },
                    modifier = Modifier.fillMaxSize()
                )
            } else if (activeFile != null) {
                // Code Editor with Line Numbers
                Row(modifier = Modifier.fillMaxSize()) {
                    // Line numbers column
                    val lineCount = activeFile.content.lines().size.coerceAtLeast(1)
                    val lineNumbersText = (1..lineCount).joinToString("\n")

                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .background(Color(0xFF05080E))
                            .padding(horizontal = 8.dp, vertical = 12.dp)
                    ) {
                        Text(
                            text = lineNumbersText,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            color = onSurfaceColor.copy(alpha = 0.35f),
                            lineHeight = 18.sp
                        )
                    }

                    // Editable Code Area
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp)
                    ) {
                        BasicTextField(
                            value = activeFile.content,
                            onValueChange = { newText ->
                                viewModel.codingProjectManager.updateActiveContent(newText)
                            },
                            textStyle = TextStyle(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 13.sp,
                                color = Color(0xFFB2EBF2),
                                lineHeight = 18.sp
                            ),
                            cursorBrush = SolidColor(primaryColor),
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .horizontalScroll(rememberScrollState())
                                .testTag("code_editor_field")
                        )
                    }
                }
            }
        }

        // Templates Drawer Row at Bottom
        Surface(color = surfaceColor, modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "TEMPLATES:",
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = primaryColor
                )
                Spacer(modifier = Modifier.width(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(listOf("Android Java Activity", "HTML5 Canvas Game", "REST API Client")) { tpl ->
                        TextButton(
                            onClick = { viewModel.codingProjectManager.loadTemplate(tpl) },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier.height(28.dp)
                        ) {
                            Text(tpl, fontSize = 11.sp, color = primaryColor)
                        }
                    }
                }
            }
        }
    }

    // New File Dialog
    if (showNewFileDialog) {
        AlertDialog(
            onDismissRequest = { showNewFileDialog = false },
            title = { Text("Create New Project File", color = primaryColor) },
            text = {
                Column {
                    OutlinedTextField(
                        value = newFileName,
                        onValueChange = { newFileName = it },
                        label = { Text("File Name (e.g. MyService.java)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("Language:", fontSize = 12.sp, color = onSurfaceColor)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("java", "html", "javascript", "python", "json", "xml").forEach { lang ->
                            FilterChip(
                                selected = newFileLang == lang,
                                onClick = { newFileLang = lang },
                                label = { Text(lang) }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newFileName.isNotBlank()) {
                            viewModel.codingProjectManager.createFile(newFileName, newFileLang)
                            showNewFileDialog = false
                            newFileName = ""
                        }
                    }
                ) {
                    Text("Create")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewFileDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
