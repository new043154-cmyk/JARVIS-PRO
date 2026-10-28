package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.AIProviderManager
import com.example.ai.AIResponseResult
import com.example.automation.AutomationEngine
import com.example.coding.CodingProjectManager
import com.example.device.BatteryStatus
import com.example.device.DeviceActionManager
import com.example.device.StorageInfo
import com.example.memory.MemoryManager
import com.example.model.AIProviderConfig
import com.example.model.AppScreen
import com.example.model.ChatMessage
import com.example.model.MessageRole
import com.example.model.OrbState
import com.example.model.PersonaType
import com.example.model.ThemeType
import com.example.terminal.TerminalEngine
import com.example.tools.PowerToolsManager
import com.example.tools.SpeedTestResult
import com.example.voice.VoiceManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class MainViewModel(application: Application) : AndroidViewModel(application) {

    val deviceActionManager = DeviceActionManager(application)
    val aiProviderManager = AIProviderManager(deviceActionManager)
    val codingProjectManager = CodingProjectManager()
    val memoryManager = MemoryManager(application)
    val powerToolsManager = PowerToolsManager(application)

    // Screen navigation stack
    private val screenStack = mutableListOf(AppScreen.DASHBOARD)
    private val _currentScreen = MutableStateFlow(AppScreen.DASHBOARD)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    // Personas and Theming
    private val _selectedPersona = MutableStateFlow(PersonaType.JARVIS)
    val selectedPersona: StateFlow<PersonaType> = _selectedPersona.asStateFlow()

    private val _selectedTheme = MutableStateFlow(ThemeType.RED_REACTOR)
    val selectedTheme: StateFlow<ThemeType> = _selectedTheme.asStateFlow()

    // Chat
    private val _messages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                role = MessageRole.ASSISTANT,
                text = "Good day, Sir. J.A.R.V.I.S. PRO v1.0.0 is online. All telemetry and subroutines are nominal. How may I be of assistance?",
                modelUsed = "JARVIS Core"
            )
        )
    )
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _chatInput = MutableStateFlow("")
    val chatInput: StateFlow<String> = _chatInput.asStateFlow()

    // Voice & Orb State
    private val _orbState = MutableStateFlow(OrbState.IDLE)
    val orbState: StateFlow<OrbState> = _orbState.asStateFlow()

    private val _statusBanner = MutableStateFlow<String?>(null)
    val statusBanner: StateFlow<String?> = _statusBanner.asStateFlow()

    val voiceManager: VoiceManager = VoiceManager(
        context = application,
        onSpeechRecognized = { text ->
            onVoiceSpeechRecognized(text)
        },
        onError = { err ->
            _orbState.value = OrbState.ERROR
            _statusBanner.value = err
            viewModelScope.launch {
                kotlinx.coroutines.delay(4000)
                if (_orbState.value == OrbState.ERROR) {
                    _orbState.value = OrbState.IDLE
                }
                _statusBanner.value = null
            }
        }
    )

    val terminalEngine = TerminalEngine(deviceActionManager, aiProviderManager, codingProjectManager)

    val automationEngine = AutomationEngine(
        context = application,
        deviceActionManager = deviceActionManager,
        voiceManager = voiceManager,
        onRunDailyBriefing = { runDailyBriefing() }
    )

    init {
        // Refresh live weather
        viewModelScope.launch {
            powerToolsManager.refreshWeather()
        }

        // Sync orb with voice manager states
        viewModelScope.launch {
            combine(voiceManager.isListening, voiceManager.isSpeaking) { listening, speaking ->
                when {
                    listening -> OrbState.LISTENING
                    speaking -> OrbState.SPEAKING
                    _orbState.value == OrbState.THINKING -> OrbState.THINKING
                    _orbState.value == OrbState.ERROR -> OrbState.ERROR
                    else -> OrbState.IDLE
                }
            }.collect { state ->
                if (_orbState.value != OrbState.THINKING && _orbState.value != OrbState.ERROR) {
                    _orbState.value = state
                }
            }
        }
    }

    // Navigation
    fun navigateTo(screen: AppScreen) {
        if (_currentScreen.value != screen) {
            screenStack.add(screen)
            _currentScreen.value = screen
        }
    }

    fun handleBack(): Boolean {
        if (screenStack.size > 1) {
            screenStack.removeAt(screenStack.size - 1)
            _currentScreen.value = screenStack.last()
            return true
        }
        return false
    }

    // Theme & Persona
    fun setTheme(theme: ThemeType) {
        _selectedTheme.value = theme
    }

    fun setPersona(persona: PersonaType) {
        _selectedPersona.value = persona
        voiceManager.applyPersona(persona)
        _statusBanner.value = "Persona changed to ${persona.displayName}"
    }

    // Chat operations
    fun setChatInput(text: String) {
        _chatInput.value = text
    }

    fun sendMessage(textOverride: String? = null, isVoice: Boolean = false) {
        val query = (textOverride ?: _chatInput.value).trim()
        if (query.isEmpty()) return

        if (textOverride == null) {
            _chatInput.value = ""
        }

        val userMsg = ChatMessage(role = MessageRole.USER, text = query, isVoiceInput = isVoice)
        _messages.value = _messages.value + userMsg

        _orbState.value = OrbState.THINKING

        viewModelScope.launch {
            val memoryTexts = memoryManager.memories.value.map { "${it.title}: ${it.content}" }
            val response = aiProviderManager.generateResponse(
                prompt = query,
                history = _messages.value,
                persona = _selectedPersona.value,
                memories = memoryTexts
            )

            when (response) {
                is AIResponseResult.Success -> {
                    val assistantMsg = ChatMessage(
                        role = MessageRole.ASSISTANT,
                        text = response.text,
                        modelUsed = response.modelUsed
                    )
                    _messages.value = _messages.value + assistantMsg
                    _orbState.value = OrbState.SPEAKING
                    voiceManager.speak(response.text)
                }
                is AIResponseResult.Error -> {
                    val errorMsg = ChatMessage(
                        role = MessageRole.ASSISTANT,
                        text = "System alert: ${response.message}",
                        modelUsed = "Diagnostic Core"
                    )
                    _messages.value = _messages.value + errorMsg
                    _orbState.value = OrbState.ERROR
                    voiceManager.speak("System error encountered.")
                }
            }
        }
    }

    private fun onVoiceSpeechRecognized(text: String) {
        sendMessage(textOverride = text, isVoice = true)
    }

    fun toggleVoiceListening() {
        if (voiceManager.isListening.value) {
            voiceManager.stopListening()
            _orbState.value = OrbState.IDLE
        } else {
            _orbState.value = OrbState.LISTENING
            voiceManager.startListening()
        }
    }

    fun stopSpeaking() {
        voiceManager.stopSpeaking()
        _orbState.value = OrbState.IDLE
    }

    fun runDailyBriefing() {
        viewModelScope.launch {
            val battery = deviceActionManager.getBatteryStatus()
            val weather = powerToolsManager.weatherData.value
            val time = SimpleDateFormat("h:mm a, EEEE, MMMM d", Locale.getDefault()).format(Date())
            val greeting = when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
                in 4..11 -> "Good morning"
                in 12..16 -> "Good afternoon"
                else -> "Good evening"
            }
            val title = if (_selectedPersona.value == PersonaType.JARVIS) "Sir" else "my friend"

            val briefing = "$greeting, $title. The current time is $time. Live weather in ${weather.city} reports ${weather.temperature}°C with ${weather.condition}. Power reserves are at ${battery.percentage}%. All Stark defense networks and automated routines are operational."

            val assistantMsg = ChatMessage(
                role = MessageRole.ASSISTANT,
                text = briefing,
                modelUsed = "Daily Briefing Engine"
            )
            _messages.value = _messages.value + assistantMsg
            voiceManager.speak(briefing)
        }
    }

    // AI Coding Studio actions
    fun explainCurrentCode() {
        val file = codingProjectManager.getActiveFile() ?: return
        val prompt = "Explain the architecture and functionality of this ${file.language} file (${file.name}):\n\n```${file.language}\n${file.content}\n```"
        navigateTo(AppScreen.CHAT)
        sendMessage(textOverride = prompt)
    }

    fun suggestFixForCode() {
        val file = codingProjectManager.getActiveFile() ?: return
        val prompt = "Analyze this ${file.language} file for bugs, edge cases, and performance improvements:\n\n```${file.language}\n${file.content}\n```"
        navigateTo(AppScreen.CHAT)
        sendMessage(textOverride = prompt)
    }

    fun generateCodePrompt(spec: String) {
        val prompt = "Generate high-quality, production-ready code for: $spec. Provide clean code blocks."
        navigateTo(AppScreen.CHAT)
        sendMessage(textOverride = prompt)
    }

    override fun onCleared() {
        super.onCleared()
        voiceManager.destroy()
        deviceActionManager.stopSensors()
        powerToolsManager.disarmAntiTheft()
    }
}
