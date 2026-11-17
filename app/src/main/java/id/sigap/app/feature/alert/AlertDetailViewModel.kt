package id.sigap.app.feature.alert

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.sigap.app.data.remote.HybridAiClassifier
import id.sigap.app.domain.model.AiAnalysisResult
import id.sigap.app.domain.model.ScanHistoryRepository
import id.sigap.app.domain.model.ScanResult
import id.sigap.app.domain.model.TrustedSenderRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface AiAnalysisUiState {
    data object Idle : AiAnalysisUiState
    data object Analyzing : AiAnalysisUiState
    data class Success(val result: AiAnalysisResult) : AiAnalysisUiState
    data class Error(val message: String) : AiAnalysisUiState
}

/** State of the alert detail screen. */
sealed interface AlertDetailUiState {
    data object Loading : AlertDetailUiState
    data object NotFound : AlertDetailUiState
    data class Loaded(val result: ScanResult) : AlertDetailUiState
}

class AlertDetailViewModel(
    private val id: Long,
    private val history: ScanHistoryRepository,
    private val trusted: TrustedSenderRepository,
    private val ai: HybridAiClassifier,
) : ViewModel() {

    val uiState: StateFlow<AlertDetailUiState> = history.observe(id)
        .map { if (it == null) AlertDetailUiState.NotFound else AlertDetailUiState.Loaded(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), AlertDetailUiState.Loading)

    private val _aiState = MutableStateFlow<AiAnalysisUiState>(AiAnalysisUiState.Idle)
    val aiState: StateFlow<AiAnalysisUiState> = _aiState.asStateFlow()

    fun requestAiAnalysis(result: ScanResult) {
        viewModelScope.launch {
            _aiState.value = AiAnalysisUiState.Analyzing
            val analysis = ai.analyze(result.redactedSnippet, result.reasons)
            if (analysis != null) {
                _aiState.value = AiAnalysisUiState.Success(analysis)
            } else {
                _aiState.value = AiAnalysisUiState.Error(
                    "Tidak ada respon AI. Pastikan OPENAI_API_KEY atau GEMINI_API_KEY terpasang di local.properties dan perangkat terhubung ke internet."
                )
            }
        }
    }

    fun trustSender(result: ScanResult) {
        val key = result.senderKey ?: return
        viewModelScope.launch { trusted.trust(key) }
    }

    fun delete() {
        viewModelScope.launch { history.delete(id) }
    }

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
