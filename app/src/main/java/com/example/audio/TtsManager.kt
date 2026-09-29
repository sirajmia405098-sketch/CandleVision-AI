package com.example.audio

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class TtsManager(context: Context) : TextToSpeech.OnInitListener {
    private var tts: TextToSpeech? = TextToSpeech(context.applicationContext, this)

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _isBengaliSupported = MutableStateFlow(false)
    val isBengaliSupported: StateFlow<Boolean> = _isBengaliSupported.asStateFlow()

    var speechRate: Float = 1.0f
        set(value) {
            field = value.coerceIn(0.5f, 2.5f)
            tts?.setSpeechRate(field)
        }

    var speechPitch: Float = 1.0f
        set(value) {
            field = value.coerceIn(0.5f, 1.5f)
            tts?.setPitch(field)
        }

    var isAutoSpeakEnabled: Boolean = true

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val ttsEngine = tts ?: return
            ttsEngine.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    _isSpeaking.value = true
                }

                override fun onDone(utteranceId: String?) {
                    _isSpeaking.value = false
                }

                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String?) {
                    _isSpeaking.value = false
                }
            })

            // Attempt Bengali locale
            val bengaliBd = Locale("bn", "BD")
            val bengaliIn = Locale("bn", "IN")
            val resBd = ttsEngine.isLanguageAvailable(bengaliBd)
            val resIn = ttsEngine.isLanguageAvailable(bengaliIn)

            if (resBd >= TextToSpeech.LANG_AVAILABLE) {
                ttsEngine.language = bengaliBd
                _isBengaliSupported.value = true
            } else if (resIn >= TextToSpeech.LANG_AVAILABLE) {
                ttsEngine.language = bengaliIn
                _isBengaliSupported.value = true
            } else {
                // Bengali not installed by default in engine, set default
                ttsEngine.language = Locale.getDefault()
                _isBengaliSupported.value = false
            }

            ttsEngine.setSpeechRate(speechRate)
            ttsEngine.setPitch(speechPitch)
        }
    }

    fun speak(text: String, force: Boolean = false) {
        if (!force && !isAutoSpeakEnabled) return
        val ttsEngine = tts ?: return

        stop()
        val params = android.os.Bundle()
        params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "candlevision_signal_${System.currentTimeMillis()}")
        ttsEngine.speak(text, TextToSpeech.QUEUE_FLUSH, params, "candlevision_signal")
    }

    fun stop() {
        tts?.stop()
        _isSpeaking.value = false
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
    }
}
