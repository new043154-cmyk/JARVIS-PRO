package com.example.terminal

import com.example.ai.AIProviderManager
import com.example.ai.AIResponseResult
import com.example.coding.CodingProjectManager
import com.example.device.DeviceActionManager
import com.example.model.PersonaType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class TerminalLine(
    val text: String,
    val isCommand: Boolean = false,
    val isError: Boolean = false,
    val isSuccess: Boolean = false
)

class TerminalEngine(
    private val deviceActionManager: DeviceActionManager,
    private val aiProviderManager: AIProviderManager,
    private val codingProjectManager: CodingProjectManager
) {
    private val _history = MutableStateFlow<List<TerminalLine>>(
        listOf(
            TerminalLine("===================================================", isSuccess = true),
            TerminalLine(" J.A.R.V.I.S. PRO CYBER TERMINAL [Version 1.0.0]", isSuccess = true),
            TerminalLine(" Stark Industries Linux Subsystem (ARM64)", isSuccess = true),
            TerminalLine(" Type 'help' to inspect available system commands.", isSuccess = true),
            TerminalLine("===================================================", isSuccess = true)
        )
    )
    val history: StateFlow<List<TerminalLine>> = _history.asStateFlow()

    private val commandList = mutableListOf<String>()

    suspend fun executeCommand(commandStr: String, persona: PersonaType, onThemeChange: ((String) -> Unit)? = null) {
        val trimmed = commandStr.trim()
        if (trimmed.isEmpty()) return

        commandList.add(trimmed)
        appendLine("jarvis@stark-core:~$ $trimmed", isCommand = true)

        val parts = trimmed.split("\\s+".toRegex())
        val cmd = parts[0].lowercase(Locale.ROOT)
        val args = parts.drop(1)

        when (cmd) {
            "help" -> {
                appendLine("AVAILABLE SYSTEM COMMANDS:")
                appendLine("  help              - Show this instruction manual")
                appendLine("  status            - Inspect battery, network, storage & core health")
                appendLine("  battery           - Display power level & charging state")
                appendLine("  torch <on|off>    - Turn hardware flashlight on or off")
                appendLine("  volume <up|down>  - Adjust device media volume level")
                appendLine("  open <app>        - Launch target application (e.g. 'open youtube')")
                appendLine("  calc <expr>       - Perform arithmetic calculation (e.g. 'calc 42*8')")
                appendLine("  ai <prompt>       - Submit direct query to JARVIS neural engine")
                appendLine("  ls                - List files in current Coding Studio project")
                appendLine("  cat <filename>    - Read contents of a project file")
                appendLine("  time              - Display current system date and UTC time")
                appendLine("  theme <name>      - Switch UI theme (e.g. 'theme neon', 'theme iron')")
                appendLine("  clear             - Reset terminal output stream")
                appendLine("  history           - Review terminal input history")
            }
            "status" -> {
                val battery = deviceActionManager.getBatteryStatus()
                val storage = deviceActionManager.getStorageInfo()
                val network = if (deviceActionManager.isNetworkAvailable()) "CONNECTED (ONLINE)" else "DISCONNECTED (OFFLINE)"

                appendLine("--- STARK SYSTEM TELEMETRY ---", isSuccess = true)
                appendLine("  AI Core:         J.A.R.V.I.S. PRO v1.0.0")
                appendLine("  Active Provider: ${aiProviderManager.getActiveProvider().name}")
                appendLine("  Battery Level:   ${battery.percentage}% (${if (battery.isCharging) "Charging" else "Discharging"})")
                appendLine("  Core Temp:       ${battery.temperatureCelsius}°C")
                appendLine("  Network State:   $network")
                appendLine("  Storage:         ${String.format("%.1f", storage.availableGb)} GB Free / ${String.format("%.1f", storage.totalGb)} GB Total (${storage.usedPercentage}% used)")
            }
            "battery" -> {
                val b = deviceActionManager.getBatteryStatus()
                appendLine("Power: ${b.percentage}% | Charging: ${b.isCharging} | Source: ${b.powerSource} | Temp: ${b.temperatureCelsius}°C", isSuccess = true)
            }
            "torch" -> {
                val sub = args.firstOrNull()?.lowercase()
                when (sub) {
                    "on" -> {
                        val res = deviceActionManager.setFlashlight(true)
                        if (res.isSuccess) appendLine("Flashlight engaged.", isSuccess = true)
                        else appendLine("Flashlight error: ${res.exceptionOrNull()?.message}", isError = true)
                    }
                    "off" -> {
                        val res = deviceActionManager.setFlashlight(false)
                        if (res.isSuccess) appendLine("Flashlight extinguished.", isSuccess = true)
                        else appendLine("Flashlight error: ${res.exceptionOrNull()?.message}", isError = true)
                    }
                    else -> {
                        val res = deviceActionManager.toggleFlashlight()
                        if (res.isSuccess) appendLine("Flashlight toggled to: ${if (res.getOrDefault(false)) "ON" else "OFF"}", isSuccess = true)
                        else appendLine("Flashlight error: ${res.exceptionOrNull()?.message}", isError = true)
                    }
                }
            }
            "volume" -> {
                val dir = args.firstOrNull()?.lowercase()
                val increase = dir != "down"
                val level = deviceActionManager.adjustVolume(increase)
                appendLine("Volume adjusted: $level%", isSuccess = true)
            }
            "open" -> {
                if (args.isEmpty()) {
                    appendLine("Usage: open <app_name> (e.g. open youtube, open chrome, open camera)", isError = true)
                } else {
                    val appName = args.joinToString(" ")
                    val res = deviceActionManager.launchAppByName(appName)
                    if (res.isSuccess) {
                        appendLine("Executed: ${res.getOrNull()}", isSuccess = true)
                    } else {
                        appendLine("Failure: ${res.exceptionOrNull()?.message}", isError = true)
                    }
                }
            }
            "calc" -> {
                if (args.isEmpty()) {
                    appendLine("Usage: calc <expression> (e.g. calc 25 * 14)", isError = true)
                } else {
                    val expr = args.joinToString(" ")
                    when (val res = aiProviderManager.generateResponse("calculate $expr", emptyList(), persona)) {
                        is AIResponseResult.Success -> appendLine(res.text, isSuccess = true)
                        is AIResponseResult.Error -> appendLine("Error: ${res.message}", isError = true)
                    }
                }
            }
            "ai" -> {
                if (args.isEmpty()) {
                    appendLine("Usage: ai <prompt> (e.g. ai explain quantum entanglement)", isError = true)
                } else {
                    val query = args.joinToString(" ")
                    appendLine("Querying neural pipeline: \"$query\"...")
                    when (val res = aiProviderManager.generateResponse(query, emptyList(), persona)) {
                        is AIResponseResult.Success -> {
                            appendLine("[${res.modelUsed}]:", isSuccess = true)
                            res.text.lines().forEach { appendLine(it) }
                        }
                        is AIResponseResult.Error -> appendLine("AI Engine Error: ${res.message}", isError = true)
                    }
                }
            }
            "ls" -> {
                val files = codingProjectManager.files.value
                appendLine("Files in workspace (${files.size}):", isSuccess = true)
                files.forEach { file ->
                    appendLine("  ${file.name.padEnd(24)} [${file.language}] (${file.content.length} bytes)")
                }
            }
            "cat" -> {
                val filename = args.firstOrNull()
                if (filename == null) {
                    appendLine("Usage: cat <filename>", isError = true)
                } else {
                    val file = codingProjectManager.files.value.firstOrNull { it.name.equals(filename, ignoreCase = true) }
                    if (file != null) {
                        appendLine("--- ${file.name} ---", isSuccess = true)
                        file.content.lines().forEach { appendLine(it) }
                    } else {
                        appendLine("File not found: $filename", isError = true)
                    }
                }
            }
            "time" -> {
                val now = SimpleDateFormat("yyyy-MM-dd HH:mm:ss zzz (EEE)", Locale.getDefault()).format(Date())
                appendLine("Current System Time: $now", isSuccess = true)
            }
            "theme" -> {
                val themeArg = args.firstOrNull()?.lowercase() ?: ""
                onThemeChange?.invoke(themeArg)
                appendLine("Theme command applied for: '$themeArg'", isSuccess = true)
            }
            "clear" -> {
                _history.value = listOf(
                    TerminalLine("J.A.R.V.I.S. PRO Terminal reset.", isSuccess = true)
                )
            }
            "history" -> {
                appendLine("Recent commands (${commandList.size}):")
                commandList.takeLast(15).forEachIndexed { i, c ->
                    appendLine("  ${i + 1}: $c")
                }
            }
            else -> {
                appendLine("Command not recognized: '$cmd'. Type 'help' for command listing.", isError = true)
            }
        }
    }

    private fun appendLine(text: String, isCommand: Boolean = false, isError: Boolean = false, isSuccess: Boolean = false) {
        val current = _history.value.toMutableList()
        current.add(TerminalLine(text, isCommand = isCommand, isError = isError, isSuccess = isSuccess))
        // Keep up to 200 lines in history
        if (current.size > 200) {
            _history.value = current.takeLast(200)
        } else {
            _history.value = current
        }
    }
}
