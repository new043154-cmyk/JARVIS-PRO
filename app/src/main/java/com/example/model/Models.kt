package com.example.model

enum class OrbState {
    IDLE,
    LISTENING,
    THINKING,
    SPEAKING,
    ERROR
}

enum class PersonaType(val displayName: String, val description: String, val defaultVoicePitch: Float, val defaultSpeechRate: Float) {
    JARVIS(
        displayName = "J.A.R.V.I.S.",
        description = "Professional, concise, analytical, calm British assistant",
        defaultVoicePitch = 0.92f,
        defaultSpeechRate = 1.0f
    ),
    PARI(
        displayName = "P.A.R.I.",
        description = "Warm, friendly, conversational, multilingual companion",
        defaultVoicePitch = 1.15f,
        defaultSpeechRate = 1.05f
    )
}

enum class AIProviderType {
    GEMINI,
    OPENAI_COMPATIBLE,
    CUSTOM_ENDPOINT,
    OFFLINE_ON_DEVICE
}

data class AIProviderConfig(
    val id: String,
    val name: String,
    val type: AIProviderType,
    val baseUrl: String = "https://generativelanguage.googleapis.com",
    val apiKey: String = "",
    val modelId: String = "gemini-1.5-flash",
    val isDefault: Boolean = false,
    val isEnabled: Boolean = true
)

enum class MessageRole {
    USER,
    ASSISTANT,
    SYSTEM
}

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val role: MessageRole,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isStreaming: Boolean = false,
    val modelUsed: String = "JARVIS Core",
    val isVoiceInput: Boolean = false
)

enum class AppScreen(val title: String) {
    DASHBOARD("Dashboard"),
    CHAT("AI Chat"),
    VOICE("Voice HUD"),
    CODING("Coding Studio"),
    TERMINAL("Terminal"),
    AUTOMATIONS("Automations"),
    POWER_TOOLS("Power Tools"),
    MEMORY("Memory Vault"),
    SETTINGS("Settings")
}

enum class ThemeType(val displayName: String) {
    MIDNIGHT_DARK("Midnight Dark"),
    IRON_HUD("Iron HUD"),
    CYBERPUNK_NEON("Cyberpunk Neon"),
    EMERALD_OBSIDIAN("Emerald Obsidian"),
    SUNSET_PLASMA("Sunset Plasma"),
    MIDNIGHT_ONYX("Midnight Onyx"),
    ROSE_GOLD("Rose Gold"),
    PURE_WHITE("Pure White"),
    RED_REACTOR("Red Reactor"),
    QUANTUM_PURPLE("Quantum Purple")
}

enum class TriggerType(val label: String) {
    SCHEDULED_TIME("Time of Day"),
    BATTERY_LOW("Battery Below 20%"),
    BATTERY_FULL("Battery 100% Charged"),
    CHARGER_CONNECTED("Charger Plugged In"),
    CHARGER_DISCONNECTED("Charger Unplugged"),
    VOICE_SHORTCUT("Voice Command Trigger")
}

enum class ActionType(val label: String) {
    SPEAK_ANNOUNCEMENT("Speak Custom Announcement"),
    TOGGLE_FLASHLIGHT("Toggle Flashlight"),
    DAILY_BRIEFING("Speak Daily Briefing"),
    OPEN_APP("Launch App"),
    VIBRATE_ALERT("Haptic Alert")
}

data class AutomationRule(
    val id: String = java.util.UUID.randomUUID().toString(),
    val title: String,
    val triggerType: TriggerType,
    val triggerParam: String = "",
    val actionType: ActionType,
    val actionParam: String = "",
    val isEnabled: Boolean = true,
    val lastExecuted: Long = 0L
)

data class MemoryEntry(
    val id: String = java.util.UUID.randomUUID().toString(),
    val title: String,
    val content: String,
    val category: String = "General",
    val timestamp: Long = System.currentTimeMillis()
)

data class CodingFile(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val language: String,
    val content: String,
    val isModified: Boolean = false
)

data class WeatherData(
    val city: String = "Local Area",
    val temperature: Double = 24.5,
    val condition: String = "Clear Sky",
    val humidity: Int = 55,
    val windSpeed: Double = 12.0,
    val lastUpdated: Long = System.currentTimeMillis(),
    val isCelsius: Boolean = true
)

data class PrayerTimes(
    val fajr: String = "05:08",
    val sunrise: String = "06:22",
    val dhuhr: String = "12:05",
    val asr: String = "15:20",
    val maghrib: String = "17:45",
    val isha: String = "19:00"
)

data class ExpenseItem(
    val id: String = java.util.UUID.randomUUID().toString(),
    val title: String,
    val amount: Double,
    val category: String,
    val timestamp: Long = System.currentTimeMillis()
)
