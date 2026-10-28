package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.model.AppScreen
import com.example.ui.MainViewModel
import com.example.ui.screens.AutomationsScreen
import com.example.ui.screens.ChatScreen
import com.example.ui.screens.CodingStudioScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.MemoryScreen
import com.example.ui.screens.PowerToolsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.TerminalScreen
import com.example.ui.screens.VoiceScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val selectedTheme by viewModel.selectedTheme.collectAsState()
            val currentScreen by viewModel.currentScreen.collectAsState()

            // Request runtime permissions for Audio, Camera (Torch), and Location gracefully
            val permissionsLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestMultiplePermissions()
            ) { /* Permissions result processed */ }

            LaunchedEffect(Unit) {
                val neededPermissions = listOf(
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.CAMERA,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ).filter {
                    ContextCompat.checkSelfPermission(this@MainActivity, it) != PackageManager.PERMISSION_GRANTED
                }
                if (neededPermissions.isNotEmpty()) {
                    permissionsLauncher.launch(neededPermissions.toTypedArray())
                }
            }

            // Predictive / custom back navigation handling
            BackHandler(enabled = currentScreen != AppScreen.DASHBOARD) {
                viewModel.handleBack()
            }

            MyApplicationTheme(selectedTheme = selectedTheme) {
                val primaryColor = MaterialTheme.colorScheme.primary
                val surfaceColor = MaterialTheme.colorScheme.surface
                val onSurfaceColor = MaterialTheme.colorScheme.onSurface

                var showMoreMenu by remember { mutableStateOf(false) }

                Scaffold(
                    contentWindowInsets = WindowInsets.safeDrawing,
                    topBar = {
                        TopAppBar(
                            title = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(primaryColor.copy(alpha = 0.2f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Image(
                                            painter = painterResource(id = R.drawable.ic_jarvis_core),
                                            contentDescription = "Core Logo",
                                            modifier = Modifier.size(22.dp).clip(CircleShape)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "JARVIS PRO",
                                            fontFamily = com.example.ui.theme.OrbitronFontFamily,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            letterSpacing = 1.5.sp,
                                            color = primaryColor
                                        )
                                        Text(
                                            text = currentScreen.title.uppercase(),
                                            fontSize = 10.sp,
                                            fontFamily = com.example.ui.theme.ShareTechMonoFontFamily,
                                            letterSpacing = 1.sp,
                                            color = onSurfaceColor.copy(alpha = 0.6f)
                                        )
                                    }
                                }
                            },
                            navigationIcon = {
                                if (currentScreen != AppScreen.DASHBOARD) {
                                    IconButton(onClick = { viewModel.handleBack() }) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                            contentDescription = "Navigate Back",
                                            tint = primaryColor
                                        )
                                    }
                                }
                            },
                            actions = {
                                // More menu for secondary screens
                                Box {
                                    IconButton(onClick = { showMoreMenu = true }) {
                                        Icon(
                                            imageVector = Icons.Default.MoreVert,
                                            contentDescription = "More Subsystems",
                                            tint = primaryColor
                                        )
                                    }

                                    DropdownMenu(
                                        expanded = showMoreMenu,
                                        onDismissRequest = { showMoreMenu = false }
                                    ) {
                                        DropdownMenuItem(
                                            text = { Text("Automations") },
                                            leadingIcon = { Icon(Icons.Default.Schedule, contentDescription = null, tint = primaryColor) },
                                            onClick = {
                                                showMoreMenu = false
                                                viewModel.navigateTo(AppScreen.AUTOMATIONS)
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("Power Tools") },
                                            leadingIcon = { Icon(Icons.Default.Bolt, contentDescription = null, tint = primaryColor) },
                                            onClick = {
                                                showMoreMenu = false
                                                viewModel.navigateTo(AppScreen.POWER_TOOLS)
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("Memory Vault") },
                                            leadingIcon = { Icon(Icons.Default.Psychology, contentDescription = null, tint = primaryColor) },
                                            onClick = {
                                                showMoreMenu = false
                                                viewModel.navigateTo(AppScreen.MEMORY)
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("Settings") },
                                            leadingIcon = { Icon(Icons.Default.Settings, contentDescription = null, tint = primaryColor) },
                                            onClick = {
                                                showMoreMenu = false
                                                viewModel.navigateTo(AppScreen.SETTINGS)
                                            }
                                        )
                                    }
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = surfaceColor
                            )
                        )
                    },
                    bottomBar = {
                        NavigationBar(
                            containerColor = surfaceColor,
                            modifier = Modifier
                                .windowInsetsPadding(WindowInsets.navigationBars)
                                .testTag("main_bottom_nav")
                        ) {
                            NavigationBarItem(
                                selected = currentScreen == AppScreen.DASHBOARD,
                                onClick = { viewModel.navigateTo(AppScreen.DASHBOARD) },
                                icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") },
                                label = { Text("HUD", fontSize = 10.sp, fontFamily = com.example.ui.theme.OrbitronFontFamily, fontWeight = FontWeight.Bold) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Color.Black,
                                    selectedTextColor = primaryColor,
                                    indicatorColor = primaryColor,
                                    unselectedIconColor = onSurfaceColor.copy(alpha = 0.6f),
                                    unselectedTextColor = onSurfaceColor.copy(alpha = 0.6f)
                                )
                            )
                            NavigationBarItem(
                                selected = currentScreen == AppScreen.CHAT,
                                onClick = { viewModel.navigateTo(AppScreen.CHAT) },
                                icon = { Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = "Chat") },
                                label = { Text("Chat", fontSize = 10.sp, fontFamily = com.example.ui.theme.OrbitronFontFamily, fontWeight = FontWeight.Bold) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Color.Black,
                                    selectedTextColor = primaryColor,
                                    indicatorColor = primaryColor,
                                    unselectedIconColor = onSurfaceColor.copy(alpha = 0.6f),
                                    unselectedTextColor = onSurfaceColor.copy(alpha = 0.6f)
                                )
                            )
                            NavigationBarItem(
                                selected = currentScreen == AppScreen.VOICE,
                                onClick = { viewModel.navigateTo(AppScreen.VOICE) },
                                icon = { Icon(Icons.Default.Mic, contentDescription = "Voice") },
                                label = { Text("Voice", fontSize = 10.sp, fontFamily = com.example.ui.theme.OrbitronFontFamily, fontWeight = FontWeight.Bold) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Color.Black,
                                    selectedTextColor = primaryColor,
                                    indicatorColor = primaryColor,
                                    unselectedIconColor = onSurfaceColor.copy(alpha = 0.6f),
                                    unselectedTextColor = onSurfaceColor.copy(alpha = 0.6f)
                                )
                            )
                            NavigationBarItem(
                                selected = currentScreen == AppScreen.CODING,
                                onClick = { viewModel.navigateTo(AppScreen.CODING) },
                                icon = { Icon(Icons.Default.Code, contentDescription = "Coding") },
                                label = { Text("Coding", fontSize = 10.sp, fontFamily = com.example.ui.theme.OrbitronFontFamily, fontWeight = FontWeight.Bold) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Color.Black,
                                    selectedTextColor = primaryColor,
                                    indicatorColor = primaryColor,
                                    unselectedIconColor = onSurfaceColor.copy(alpha = 0.6f),
                                    unselectedTextColor = onSurfaceColor.copy(alpha = 0.6f)
                                )
                            )
                            NavigationBarItem(
                                selected = currentScreen == AppScreen.TERMINAL,
                                onClick = { viewModel.navigateTo(AppScreen.TERMINAL) },
                                icon = { Icon(Icons.Default.Terminal, contentDescription = "Terminal") },
                                label = { Text("Terminal", fontSize = 10.sp, fontFamily = com.example.ui.theme.OrbitronFontFamily, fontWeight = FontWeight.Bold) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Color.Black,
                                    selectedTextColor = primaryColor,
                                    indicatorColor = primaryColor,
                                    unselectedIconColor = onSurfaceColor.copy(alpha = 0.6f),
                                    unselectedTextColor = onSurfaceColor.copy(alpha = 0.6f)
                                )
                            )
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    AnimatedContent(
                        targetState = currentScreen,
                        transitionSpec = { fadeIn() togetherWith fadeOut() },
                        label = "screen_transition",
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) { targetScreen ->
                        when (targetScreen) {
                            AppScreen.DASHBOARD -> DashboardScreen(viewModel = viewModel)
                            AppScreen.CHAT -> ChatScreen(viewModel = viewModel)
                            AppScreen.VOICE -> VoiceScreen(viewModel = viewModel)
                            AppScreen.CODING -> CodingStudioScreen(viewModel = viewModel)
                            AppScreen.TERMINAL -> TerminalScreen(viewModel = viewModel)
                            AppScreen.AUTOMATIONS -> AutomationsScreen(viewModel = viewModel)
                            AppScreen.POWER_TOOLS -> PowerToolsScreen(viewModel = viewModel)
                            AppScreen.MEMORY -> MemoryScreen(viewModel = viewModel)
                            AppScreen.SETTINGS -> SettingsScreen(viewModel = viewModel)
                        }
                    }
                }
            }
        }
    }
}
