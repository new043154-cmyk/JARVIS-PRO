package com.example.ai

import com.example.BuildConfig
import com.example.device.DeviceActionManager
import com.example.model.AIProviderConfig
import com.example.model.AIProviderType
import com.example.model.ChatMessage
import com.example.model.MessageRole
import com.example.model.PersonaType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

sealed class AIResponseResult {
    data class Success(val text: String, val modelUsed: String, val executedAction: String? = null) : AIResponseResult()
    data class Error(val message: String) : AIResponseResult()
}

fun getInjectedApiKey(): String {
    return try {
        val field = BuildConfig::class.java.getField("GEMINI_API_KEY")
        field.get(null) as? String ?: ""
    } catch (_: Exception) {
        ""
    }
}

class AIProviderManager(
    private val deviceActionManager: DeviceActionManager
) {
    private val client = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(20, TimeUnit.SECONDS)
        .build()

    // Default built-in configurations
    private val defaultGemini = AIProviderConfig(
        id = "gemini_default",
        name = "Google Gemini 1.5 Flash",
        type = AIProviderType.GEMINI,
        baseUrl = "https://generativelanguage.googleapis.com",
        apiKey = try { BuildConfig.GEMINI_API_KEY } catch (_: Exception) { "" },
        modelId = "gemini-1.5-flash",
        isDefault = true,
        isEnabled = true
    )

    private val defaultOpenAI = AIProviderConfig(
        id = "openai_compatible",
        name = "OpenAI / Custom Endpoint",
        type = AIProviderType.OPENAI_COMPATIBLE,
        baseUrl = "https://api.openai.com/v1",
        apiKey = "",
        modelId = "gpt-4o-mini",
        isDefault = false,
        isEnabled = true
    )

    private val _providers = mutableListOf(defaultGemini, defaultOpenAI)
    val providers: List<AIProviderConfig> get() = _providers

    fun addOrUpdateProvider(config: AIProviderConfig) {
        val index = _providers.indexOfFirst { it.id == config.id }
        if (index >= 0) {
            _providers[index] = config
        } else {
            _providers.add(config)
        }
        if (config.isDefault) {
            _providers.forEachIndexed { i, p ->
                if (p.id != config.id && p.isDefault) {
                    _providers[i] = p.copy(isDefault = false)
                }
            }
        }
    }

    fun getActiveProvider(): AIProviderConfig {
        return _providers.firstOrNull { it.isDefault && it.isEnabled }
            ?: _providers.firstOrNull { it.isEnabled }
            ?: defaultGemini
    }

    suspend fun generateResponse(
        prompt: String,
        history: List<ChatMessage>,
        persona: PersonaType,
        memories: List<String> = emptyList()
    ): AIResponseResult = withContext(Dispatchers.IO) {
        // 1. First check if this is an explicit direct local device action
        val localAction = tryLocalDeviceAction(prompt)
        if (localAction != null) {
            return@withContext localAction
        }

        // 2. Select provider
        val provider = getActiveProvider()

        // If no API key configured and provider needs one, attempt smart on-device intelligence
        val key = provider.apiKey.ifEmpty {
            getInjectedApiKey()
        }

        if (key.isBlank() || provider.type == AIProviderType.OFFLINE_ON_DEVICE) {
            return@withContext onDeviceSmartResponse(prompt, persona, memories)
        }

        try {
            when (provider.type) {
                AIProviderType.GEMINI -> {
                    callGeminiApi(provider, key, prompt, history, persona, memories)
                }
                AIProviderType.OPENAI_COMPATIBLE, AIProviderType.CUSTOM_ENDPOINT -> {
                    callOpenAIApi(provider, key, prompt, history, persona, memories)
                }
                AIProviderType.OFFLINE_ON_DEVICE -> {
                    onDeviceSmartResponse(prompt, persona, memories)
                }
            }
        } catch (e: Exception) {
            // Graceful fallback to on-device brain if network or quota fails
            val fallback = onDeviceSmartResponse(prompt, persona, memories)
            if (fallback is AIResponseResult.Success) {
                AIResponseResult.Success(
                    text = "${fallback.text}\n\n[Network notice: Online model encountered an issue (${e.localizedMessage ?: "connection"}). JARVIS Core answered locally.]",
                    modelUsed = "JARVIS On-Device Core"
                )
            } else {
                AIResponseResult.Error("API request failed: ${e.message}")
            }
        }
    }

    private fun tryLocalDeviceAction(prompt: String): AIResponseResult? {
        val lower = prompt.lowercase(Locale.ROOT).trim()

        // Flashlight commands
        if (lower.contains("turn on flashlight") || lower.contains("torch on") || lower.contains("flashlight on")) {
            val res = deviceActionManager.setFlashlight(true)
            return if (res.isSuccess) {
                AIResponseResult.Success("Flashlight activated, Sir.", "Device Action", "Flashlight ON")
            } else {
                AIResponseResult.Success("Unable to turn on flashlight: ${res.exceptionOrNull()?.message}", "Device Action")
            }
        }
        if (lower.contains("turn off flashlight") || lower.contains("torch off") || lower.contains("flashlight off")) {
            val res = deviceActionManager.setFlashlight(false)
            return if (res.isSuccess) {
                AIResponseResult.Success("Flashlight turned off.", "Device Action", "Flashlight OFF")
            } else {
                AIResponseResult.Success("Unable to turn off flashlight: ${res.exceptionOrNull()?.message}", "Device Action")
            }
        }

        // Battery check
        if (lower.contains("battery") || lower.contains("charge level") || lower.contains("power status")) {
            val status = deviceActionManager.getBatteryStatus()
            val chargingText = if (status.isCharging) "currently charging via ${status.powerSource}" else "discharging on battery power"
            return AIResponseResult.Success(
                "Power reserves are at ${status.percentage}%. The device is $chargingText. Core temperature is ${status.temperatureCelsius}°C.",
                "Device Action"
            )
        }

        // Volume control
        if (lower.contains("volume up") || lower.contains("increase volume") || lower.contains("louder")) {
            val vol = deviceActionManager.adjustVolume(true)
            return AIResponseResult.Success("Media volume increased to $vol%.", "Device Action")
        }
        if (lower.contains("volume down") || lower.contains("decrease volume") || lower.contains("lower volume")) {
            val vol = deviceActionManager.adjustVolume(false)
            return AIResponseResult.Success("Media volume lowered to $vol%.", "Device Action")
        }

        // Open app commands
        if (lower.startsWith("open ") || lower.startsWith("launch ")) {
            val target = lower.removePrefix("open ").removePrefix("launch ").trim()
            val launchRes = deviceActionManager.launchAppByName(target)
            return if (launchRes.isSuccess) {
                AIResponseResult.Success(launchRes.getOrNull() ?: "Launching application.", "Device Action", "App Launch")
            } else {
                AIResponseResult.Success("Could not find or launch '$target': ${launchRes.exceptionOrNull()?.message}", "Device Action")
            }
        }

        // Alarms & Timers
        val timerRegex = Regex("set timer for (\\d+) (second|seconds|minute|minutes|min|sec)")
        val timerMatch = timerRegex.find(lower)
        if (timerMatch != null) {
            val num = timerMatch.groupValues[1].toIntOrNull() ?: 1
            val unit = timerMatch.groupValues[2]
            val totalSeconds = if (unit.startsWith("min")) num * 60 else num
            val res = deviceActionManager.setTimer(totalSeconds, "JARVIS Timer")
            return AIResponseResult.Success(res.getOrElse { "Timer could not be set: ${it.message}" }, "Device Action")
        }

        // Time check
        if (lower == "time" || lower == "what time is it" || lower.contains("current time") || lower.contains("what is the time")) {
            val time = SimpleDateFormat("h:mm a, EEEE, MMMM d, yyyy", Locale.getDefault()).format(Date())
            return AIResponseResult.Success("The current time is $time.", "Local Clock")
        }

        // Bengali greetings / commands
        if (lower.contains("কেমন আছো") || lower.contains("কেমন আছেন") || lower.contains("কি খবর")) {
            return AIResponseResult.Success("আমি ভালো আছি, স্যার! আপনার জন্য আমি কী করতে পারি?", "P.A.R.I. Core")
        }
        if (lower.contains("আলো জ্বালাও") || lower.contains("ফ্ল্যাশলাইট জ্বালাও")) {
            deviceActionManager.setFlashlight(true)
            return AIResponseResult.Success("ফ্ল্যাশলাইট জ্বালিয়ে দিয়েছি, স্যার।", "Device Action")
        }
        if (lower.contains("আলো বন্ধ করো") || lower.contains("ফ্ল্যাশলাইট বন্ধ করো")) {
            deviceActionManager.setFlashlight(false)
            return AIResponseResult.Success("ফ্ল্যাশলাইট নিভিয়ে দেওয়া হয়েছে।", "Device Action")
        }

        return null
    }

    private fun onDeviceSmartResponse(
        prompt: String,
        persona: PersonaType,
        memories: List<String>
    ): AIResponseResult {
        val lower = prompt.lowercase(Locale.ROOT).trim()

        // Math evaluator
        val mathResult = tryEvaluateMath(prompt)
        if (mathResult != null) {
            return AIResponseResult.Success(
                if (persona == PersonaType.JARVIS) "The calculated result is $mathResult, Sir." else "The answer is $mathResult!",
                "JARVIS Math Engine"
            )
        }

        // Identity questions
        if (lower.contains("who are you") || lower.contains("what is your name")) {
            val reply = if (persona == PersonaType.JARVIS) {
                "I am J.A.R.V.I.S. (Just A Rather Very Intelligent System) PRO, your personal AI assistant. I am equipped to manage device controls, automated workflows, voice recognition, code generation, terminal commands, and system diagnostics."
            } else {
                "Hi there! I am P.A.R.I., your friendly, multilingual personal companion! I'm here to assist with daily routines, conversations, device actions, and anything you need."
            }
            return AIResponseResult.Success(reply, "Identity Module")
        }

        if (lower.contains("who created you") || lower.contains("who made you")) {
            return AIResponseResult.Success("I was developed as the JARVIS PRO AI Assistant platform, engineered for high-performance productivity, privacy, and Android integration.", "System Info")
        }

        // Memory mention
        val memoryContext = if (memories.isNotEmpty()) {
            " (I recall from memory: ${memories.take(2).joinToString("; ")})"
        } else ""

        val response = if (persona == PersonaType.JARVIS) {
            "All systems are operational, Sir.$memoryContext I have logged your request: \"$prompt\". Configure an active Gemini or OpenAI API key in Settings to unlock deep neural reasoning, or ask me for device controls (flashlight, battery, apps, alarms, weather, terminal commands)."
        } else {
            "I heard you loud and clear!$memoryContext You said: \"$prompt\". You can also ask me to check your battery, turn on the flashlight, launch apps, or configure your API key in Settings for expanded AI capabilities!"
        }

        return AIResponseResult.Success(response, "JARVIS Core")
    }

    private fun tryEvaluateMath(prompt: String): String? {
        val cleaned = prompt
            .replace("what is", "", ignoreCase = true)
            .replace("calculate", "", ignoreCase = true)
            .replace("how much is", "", ignoreCase = true)
            .replace("x", "*", ignoreCase = true)
            .replace("plus", "+", ignoreCase = true)
            .replace("minus", "-", ignoreCase = true)
            .replace("times", "*", ignoreCase = true)
            .replace("divided by", "/", ignoreCase = true)
            .replace("?", "")
            .trim()

        val mathRegex = Regex("^([0-9.]+)\\s*([+\\-*/%^])\\s*([0-9.]+)$")
        val match = mathRegex.find(cleaned) ?: return null

        val a = match.groupValues[1].toDoubleOrNull() ?: return null
        val op = match.groupValues[2]
        val b = match.groupValues[3].toDoubleOrNull() ?: return null

        val result = when (op) {
            "+" -> a + b
            "-" -> a - b
            "*" -> a * b
            "/" -> if (b != 0.0) a / b else return "undefined (division by zero)"
            "%" -> a % b
            "^" -> Math.pow(a, b)
            else -> return null
        }
        return if (result % 1.0 == 0.0) result.toLong().toString() else String.format(Locale.US, "%.3f", result)
    }

    private fun callGeminiApi(
        provider: AIProviderConfig,
        apiKey: String,
        prompt: String,
        history: List<ChatMessage>,
        persona: PersonaType,
        memories: List<String>
    ): AIResponseResult {
        val url = "${provider.baseUrl.trimEnd('/')}/v1beta/models/${provider.modelId}:generateContent?key=$apiKey"

        val systemInstruction = buildSystemPrompt(persona, memories)

        val contentsArray = JSONArray()

        // Include recent history (up to last 6 messages)
        history.takeLast(6).forEach { msg ->
            if (msg.role == MessageRole.USER || msg.role == MessageRole.ASSISTANT) {
                val roleStr = if (msg.role == MessageRole.USER) "user" else "model"
                val partObj = JSONObject().put("text", msg.text)
                val msgObj = JSONObject()
                    .put("role", roleStr)
                    .put("parts", JSONArray().put(partObj))
                contentsArray.put(msgObj)
            }
        }

        // Current prompt
        val currentPart = JSONObject().put("text", prompt)
        contentsArray.put(JSONObject().put("role", "user").put("parts", JSONArray().put(currentPart)))

        val requestBodyJson = JSONObject()
            .put("contents", contentsArray)
            .put("systemInstruction", JSONObject().put("parts", JSONArray().put(JSONObject().put("text", systemInstruction))))

        val mediaType = "application/json; charset=utf-8".toMediaType()
        val request = Request.Builder()
            .url(url)
            .post(requestBodyJson.toString().toRequestBody(mediaType))
            .build()

        client.newCall(request).execute().use { response ->
            val responseBody = response.body?.string() ?: ""
            if (!response.isSuccessful) {
                return AIResponseResult.Error("Gemini Error HTTP ${response.code}: $responseBody")
            }

            val json = JSONObject(responseBody)
            val candidates = json.optJSONArray("candidates")
            if (candidates != null && candidates.length() > 0) {
                val firstCandidate = candidates.getJSONObject(0)
                val content = firstCandidate.optJSONObject("content")
                val parts = content?.optJSONArray("parts")
                if (parts != null && parts.length() > 0) {
                    val text = parts.getJSONObject(0).optString("text", "")
                    return AIResponseResult.Success(text.trim(), provider.name)
                }
            }
            return AIResponseResult.Error("No valid response candidate returned by Gemini.")
        }
    }

    private fun callOpenAIApi(
        provider: AIProviderConfig,
        apiKey: String,
        prompt: String,
        history: List<ChatMessage>,
        persona: PersonaType,
        memories: List<String>
    ): AIResponseResult {
        val base = provider.baseUrl.trimEnd('/')
        val url = if (base.endsWith("/chat/completions")) base else "$base/chat/completions"

        val systemInstruction = buildSystemPrompt(persona, memories)

        val messagesArray = JSONArray()
        messagesArray.put(JSONObject().put("role", "system").put("content", systemInstruction))

        history.takeLast(6).forEach { msg ->
            if (msg.role == MessageRole.USER || msg.role == MessageRole.ASSISTANT) {
                val roleStr = if (msg.role == MessageRole.USER) "user" else "assistant"
                messagesArray.put(JSONObject().put("role", roleStr).put("content", msg.text))
            }
        }
        messagesArray.put(JSONObject().put("role", "user").put("content", prompt))

        val requestBodyJson = JSONObject()
            .put("model", provider.modelId)
            .put("messages", messagesArray)
            .put("temperature", 0.7)

        val mediaType = "application/json; charset=utf-8".toMediaType()
        val requestBuilder = Request.Builder()
            .url(url)
            .post(requestBodyJson.toString().toRequestBody(mediaType))

        if (apiKey.isNotBlank()) {
            requestBuilder.addHeader("Authorization", "Bearer $apiKey")
        }

        client.newCall(requestBuilder.build()).execute().use { response ->
            val responseBody = response.body?.string() ?: ""
            if (!response.isSuccessful) {
                return AIResponseResult.Error("API Error HTTP ${response.code}: $responseBody")
            }

            val json = JSONObject(responseBody)
            val choices = json.optJSONArray("choices")
            if (choices != null && choices.length() > 0) {
                val firstChoice = choices.getJSONObject(0)
                val message = firstChoice.optJSONObject("message")
                val content = message?.optString("content", "") ?: ""
                return AIResponseResult.Success(content.trim(), provider.name)
            }
            return AIResponseResult.Error("No completion returned by provider.")
        }
    }

    private fun buildSystemPrompt(persona: PersonaType, memories: List<String>): String {
        val personaDesc = when (persona) {
            PersonaType.JARVIS -> "You are J.A.R.V.I.S., Tony Stark's iconic, highly analytical, sophisticated, witty, and calm AI assistant. Address the user with respect ('Sir' or user's preferred title), provide succinct, highly actionable, intelligent responses with technical depth."
            PersonaType.PARI -> "You are P.A.R.I., a warm, friendly, conversational, empathetic, multilingual AI companion. You speak fluently in English, Bengali, and Hindi with high enthusiasm and helpfulness."
        }
        val memoryBlock = if (memories.isNotEmpty()) {
            "\nUSER MEMORIES & PREFERENCES:\n" + memories.joinToString("\n") { "- $it" }
        } else ""

        return "$personaDesc\n$memoryBlock\nYou have access to device actions (flashlight, battery, apps, alarms, timer, weather, terminal commands, coding tools). When relevant, suggest or acknowledge real phone capabilities."
    }
}
