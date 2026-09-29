package com.example.ui

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.TtsManager
import com.example.data.MarketSimulator
import com.example.data.local.CandleVisionDatabase
import com.example.data.local.TradeHistoryEntity
import com.example.data.local.TradeOutcome
import com.example.data.local.TradeRepository
import com.example.data.model.AnalysisResult
import com.example.data.model.Candlestick
import com.example.data.model.MarketPattern
import com.example.data.remote.GeminiVisionClient
import com.example.service.FloatingWidgetService
import com.example.ui.components.renderCandlesToBitmap
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

sealed class AnalysisUiState {
    object Idle : AnalysisUiState()
    object Analyzing : AnalysisUiState()
    data class Success(val result: AnalysisResult) : AnalysisUiState()
    data class Error(val message: String) : AnalysisUiState()
}

enum class ActiveTab {
    SIMULATOR,
    IMAGE_CROPPER,
    HISTORY,
    SETTINGS
}

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val database = CandleVisionDatabase.getDatabase(application)
    val repository = TradeRepository(database.tradeDao())
    val geminiClient = GeminiVisionClient()
    val ttsManager = TtsManager(application)

    // Current navigation tab
    private val _activeTab = MutableStateFlow(ActiveTab.SIMULATOR)
    val activeTab: StateFlow<ActiveTab> = _activeTab.asStateFlow()

    fun setActiveTab(tab: ActiveTab) {
        _activeTab.value = tab
    }

    // Simulator State
    private val _selectedPattern = MutableStateFlow(MarketPattern.BULLISH_HAMMER)
    val selectedPattern: StateFlow<MarketPattern> = _selectedPattern.asStateFlow()

    private val _candles = MutableStateFlow<List<Candlestick>>(emptyList())
    val candles: StateFlow<List<Candlestick>> = _candles.asStateFlow()

    private val _supportLevel = MutableStateFlow<Float?>(1.0800f)
    val supportLevel: StateFlow<Float?> = _supportLevel.asStateFlow()

    private val _resistanceLevel = MutableStateFlow<Float?>(1.0890f)
    val resistanceLevel: StateFlow<Float?> = _resistanceLevel.asStateFlow()

    private val _isLiveTicking = MutableStateFlow(false)
    val isLiveTicking: StateFlow<Boolean> = _isLiveTicking.asStateFlow()

    private var tickingJob: Job? = null

    // Image Upload / Cropper State
    private val _uploadedBitmap = MutableStateFlow<Bitmap?>(null)
    val uploadedBitmap: StateFlow<Bitmap?> = _uploadedBitmap.asStateFlow()

    private val _croppedBitmap = MutableStateFlow<Bitmap?>(null)
    val croppedBitmap: StateFlow<Bitmap?> = _croppedBitmap.asStateFlow()

    // Analysis State
    private val _analysisState = MutableStateFlow<AnalysisUiState>(AnalysisUiState.Idle)
    val analysisState: StateFlow<AnalysisUiState> = _analysisState.asStateFlow()

    // TTS & Audio State
    val isSpeaking = ttsManager.isSpeaking
    val isBengaliSupported = ttsManager.isBengaliSupported

    private val _autoSpeak = MutableStateFlow(true)
    val autoSpeak: StateFlow<Boolean> = _autoSpeak.asStateFlow()

    private val _speechRate = MutableStateFlow(1.0f)
    val speechRate: StateFlow<Float> = _speechRate.asStateFlow()

    // Floating overlay service state
    private val _isFloatingOverlayActive = MutableStateFlow(FloatingWidgetService.isRunning)
    val isFloatingOverlayActive: StateFlow<Boolean> = _isFloatingOverlayActive.asStateFlow()

    // Room Database Observables
    val allTrades = repository.allTrades.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val totalTradesCount = repository.totalCount.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0
    )

    val winTradesCount = repository.winCount.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0
    )

    val lossTradesCount = repository.lossCount.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0
    )

    init {
        loadPattern(MarketPattern.BULLISH_HAMMER)
    }

    fun loadPattern(pattern: MarketPattern) {
        _selectedPattern.value = pattern
        val scenario = MarketSimulator.generateScenario(pattern)
        _candles.value = scenario.first
        _supportLevel.value = scenario.second.first
        _resistanceLevel.value = scenario.second.second
        _analysisState.value = AnalysisUiState.Idle
    }

    fun toggleLiveTicking() {
        val willTick = !_isLiveTicking.value
        _isLiveTicking.value = willTick

        if (willTick) {
            tickingJob?.cancel()
            tickingJob = viewModelScope.launch {
                while (isActive && _isLiveTicking.value) {
                    delay(1200L)
                    val currentList = _candles.value.toMutableList()
                    if (currentList.isNotEmpty()) {
                        val lastIdx = currentList.size - 1
                        val updatedLast = MarketSimulator.tickCandle(currentList[lastIdx])
                        currentList[lastIdx] = updatedLast
                        _candles.value = currentList
                    }
                }
            }
        } else {
            tickingJob?.cancel()
            tickingJob = null
        }
    }

    fun analyzeCurrentSimulatorChart() {
        viewModelScope.launch {
            val currentCandles = _candles.value
            if (currentCandles.isEmpty()) return@launch

            _analysisState.value = AnalysisUiState.Analyzing
            // Render to high resolution bitmap
            val chartBmp = renderCandlesToBitmap(
                candles = currentCandles,
                supportLevel = _supportLevel.value,
                resistanceLevel = _resistanceLevel.value
            )

            val result = geminiClient.analyzeCandlestickChart(chartBmp)
            result.onSuccess { analysisResult ->
                _analysisState.value = AnalysisUiState.Success(analysisResult)

                // Save to Room DB
                val entity = TradeHistoryEntity(
                    signal = analysisResult.signal.name,
                    confidence = analysisResult.confidence.name,
                    bengaliAnalysis = analysisResult.rawBengaliText,
                    pattern = analysisResult.patternDetected,
                    trend = analysisResult.trend,
                    latencyMs = analysisResult.latencyMs,
                    imageBase64 = analysisResult.imageBase64
                )
                repository.insert(entity)

                // Auto speak in Bengali if enabled
                if (_autoSpeak.value) {
                    ttsManager.speak(analysisResult.rawBengaliText)
                }
            }.onFailure { error ->
                _analysisState.value = AnalysisUiState.Error(error.localizedMessage ?: "বিশ্লেষণ ত্রুটি (Analysis Error)")
            }
        }
    }

    fun setUploadedImageUri(uri: Uri) {
        try {
            val context = getApplication<Application>()
            val bitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
            _uploadedBitmap.value = bitmap
            _croppedBitmap.value = null
            _analysisState.value = AnalysisUiState.Idle
        } catch (_: Exception) {}
    }

    fun setCroppedBitmap(bitmap: Bitmap) {
        _croppedBitmap.value = bitmap
    }

    fun analyzeCroppedBitmap() {
        val bitmap = _croppedBitmap.value ?: _uploadedBitmap.value ?: return
        viewModelScope.launch {
            _analysisState.value = AnalysisUiState.Analyzing
            val result = geminiClient.analyzeCandlestickChart(bitmap)
            result.onSuccess { analysisResult ->
                _analysisState.value = AnalysisUiState.Success(analysisResult)

                val entity = TradeHistoryEntity(
                    signal = analysisResult.signal.name,
                    confidence = analysisResult.confidence.name,
                    bengaliAnalysis = analysisResult.rawBengaliText,
                    pattern = analysisResult.patternDetected,
                    trend = analysisResult.trend,
                    latencyMs = analysisResult.latencyMs,
                    imageBase64 = analysisResult.imageBase64
                )
                repository.insert(entity)

                if (_autoSpeak.value) {
                    ttsManager.speak(analysisResult.rawBengaliText)
                }
            }.onFailure { error ->
                _analysisState.value = AnalysisUiState.Error(error.localizedMessage ?: "বিশ্লেষণ ত্রুটি")
            }
        }
    }

    fun speakText(text: String) {
        ttsManager.speak(text, force = true)
    }

    fun setAutoSpeak(enabled: Boolean) {
        _autoSpeak.value = enabled
        ttsManager.isAutoSpeakEnabled = enabled
    }

    fun setSpeechRate(rate: Float) {
        _speechRate.value = rate
        ttsManager.speechRate = rate
    }

    fun logTradeOutcome(outcome: TradeOutcome) {
        viewModelScope.launch {
            val currentState = _analysisState.value
            if (currentState is AnalysisUiState.Success) {
                // Find latest inserted trade and update outcome
                val recent = repository.allTrades
                // update outcome
            }
        }
    }

    fun updateTradeOutcome(tradeId: Long, outcome: TradeOutcome) {
        viewModelScope.launch {
            repository.updateOutcome(tradeId, outcome)
        }
    }

    fun deleteTrade(tradeId: Long) {
        viewModelScope.launch {
            repository.delete(tradeId)
        }
    }

    fun clearTradeHistory() {
        viewModelScope.launch {
            repository.clearAll()
        }
    }

    fun setFloatingOverlayActive(active: Boolean) {
        _isFloatingOverlayActive.value = active
    }

    override fun onCleared() {
        super.onCleared()
        tickingJob?.cancel()
        ttsManager.shutdown()
    }
}
