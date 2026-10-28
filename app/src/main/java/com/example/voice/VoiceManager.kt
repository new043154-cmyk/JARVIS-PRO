package com.example.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import com.example.model.PersonaType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class VoiceManager(
    private val context: Context,
    private val onSpeechRecognized: (String) -> Unit,
    private val onError: (String) -> Unit
) : TextToSpeech.OnInitListener {

    private var speechRecognizer: SpeechRecognizer? = null
    private var textToSpeech: TextToSpeech? = null
    private var isTtsInitialized = false

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _micLevel = MutableStateFlow(0f)
    val micLevel: StateFlow<Float> = _micLevel.asStateFlow()

    private val _currentLanguage = MutableStateFlow("en-US")
    val currentLanguage: StateFlow<String> = _currentLanguage.asStateFlow()

    private var speechPitch = 0.95f
    private var speechRate = 1.0f

    init {
        initializeSpeechRecognizer()
        textToSpeech = TextToSpeech(context.applicationContext, this)
    }

    private fun initializeSpeechRecognizer() {
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {
                        _isListening.value = true
                    }

                    override fun onBeginningOfSpeech() {
                        _isListening.value = true
                    }

                    override fun onRmsChanged(rmsdB: Float) {
                        // Normalize -2dB to 10dB into 0f..1f range
                        val normalized = ((rmsdB + 2f) / 12f).coerceIn(0f, 1f)
                        _micLevel.value = normalized
                    }

                    override fun onBufferReceived(buffer: ByteArray?) {}

                    override fun onEndOfSpeech() {
                        _isListening.value = false
                        _micLevel.value = 0f
                    }

                    override fun onError(error: Int) {
                        _isListening.value = false
                        _micLevel.value = 0f
                        val message = when (error) {
                            SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
                            SpeechRecognizer.ERROR_CLIENT -> "Speech recognition client error"
                            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission required"
                            SpeechRecognizer.ERROR_NETWORK -> "Network required for speech recognition"
                            SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Speech recognition network timeout"
                            SpeechRecognizer.ERROR_NO_MATCH -> "No speech detected"
                            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Recognition service busy"
                            SpeechRecognizer.ERROR_SERVER -> "Server error"
                            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech input heard"
                            else -> "Recognition error ($error)"
                        }
                        if (error != SpeechRecognizer.ERROR_NO_MATCH && error != SpeechRecognizer.ERROR_SPEECH_TIMEOUT) {
                            onError(message)
                        }
                    }

                    override fun onResults(results: Bundle?) {
                        _isListening.value = false
                        _micLevel.value = 0f
                        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        if (!matches.isNullOrEmpty()) {
                            onSpeechRecognized(matches[0])
                        }
                    }

                    override fun onPartialResults(partialResults: Bundle?) {
                        val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        if (!matches.isNullOrEmpty()) {
                            // Can show live transcript if desired
                        }
                    }

                    override fun onEvent(eventType: Int, params: Bundle?) {}
                })
            }
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isTtsInitialized = true
            setLanguageLocale(_currentLanguage.value)
            textToSpeech?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    _isSpeaking.value = true
                }

                override fun onDone(utteranceId: String?) {
                    _isSpeaking.value = false
                }

                override fun onError(utteranceId: String?) {
                    _isSpeaking.value = false
                }
            })
        }
    }

    fun startListening() {
        if (speechRecognizer == null) {
            initializeSpeechRecognizer()
        }

        stopSpeaking()

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, _currentLanguage.value)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, _currentLanguage.value)
            putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, _currentLanguage.value)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
        }

        try {
            speechRecognizer?.startListening(intent)
            _isListening.value = true
        } catch (e: Exception) {
            _isListening.value = false
            onError("Failed to start voice recognition: ${e.message}")
        }
    }

    fun stopListening() {
        try {
            speechRecognizer?.stopListening()
        } catch (_: Exception) {}
        _isListening.value = false
        _micLevel.value = 0f
    }

    fun speak(text: String, utteranceId: String = "JARVIS_RESPONSE") {
        if (!isTtsInitialized || textToSpeech == null) return

        // Clean out markdown symbols for clear speech
        val cleaned = text
            .replace(Regex("```[\\s\\S]*?```"), "Code block omitted from spoken audio.")
            .replace(Regex("[#*_`>]"), "")
            .trim()

        if (cleaned.isEmpty()) return

        textToSpeech?.setPitch(speechPitch)
        textToSpeech?.setSpeechRate(speechRate)
        textToSpeech?.speak(cleaned, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
        _isSpeaking.value = true
    }

    fun stopSpeaking() {
        textToSpeech?.stop()
        _isSpeaking.value = false
    }

    fun setLanguage(languageCode: String) {
        _currentLanguage.value = languageCode
        setLanguageLocale(languageCode)
    }

    private fun setLanguageLocale(code: String) {
        val locale = when (code) {
            "bn-BD", "bn" -> Locale.forLanguageTag("bn-BD")
            "hi-IN", "hi" -> Locale.forLanguageTag("hi-IN")
            else -> Locale.US
        }
        textToSpeech?.language = locale
    }

    fun applyPersona(persona: PersonaType) {
        speechPitch = persona.defaultVoicePitch
        speechRate = persona.defaultSpeechRate
        textToSpeech?.setPitch(speechPitch)
        textToSpeech?.setSpeechRate(speechRate)
    }

    fun setVoiceParameters(pitch: Float, rate: Float) {
        speechPitch = pitch.coerceIn(0.5f, 2.0f)
        speechRate = rate.coerceIn(0.5f, 2.0f)
        textToSpeech?.setPitch(speechPitch)
        textToSpeech?.setSpeechRate(speechRate)
    }

    fun destroy() {
        stopListening()
        stopSpeaking()
        speechRecognizer?.destroy()
        speechRecognizer = null
        textToSpeech?.shutdown()
        textToSpeech = null
    }
}
