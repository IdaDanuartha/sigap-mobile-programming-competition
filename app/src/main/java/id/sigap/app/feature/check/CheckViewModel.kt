package id.sigap.app.feature.check

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.sigap.app.core.util.Outcome
import id.sigap.app.data.mlkit.ImageTextExtractor
import id.sigap.app.domain.model.MessageInput
import id.sigap.app.domain.model.SenderKind
import id.sigap.app.domain.usecase.AnalyzeMessageUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

enum class AnalysisStage {
    EXTRACTING_TEXT,     // Membaca teks masukan / OCR
    CHECKING_PATTERNS,   // Memeriksa tautan, APK, nomor, urgensi
    CALCULATING_SCORE,   // Menilai skor risiko heuristik & AI
    COMPLETE,            // Selesai
}

/** UI state of the Check hub. */
sealed interface CheckUiState {
    data object Idle : CheckUiState
    data class Analyzing(
        val stage: AnalysisStage,
        val progressText: String,
        val isImage: Boolean = false,
    ) : CheckUiState
    data object NoText : CheckUiState
    data object Error : CheckUiState
}

class CheckViewModel(
    private val analyze: AnalyzeMessageUseCase,
    private val extractor: ImageTextExtractor,
) : ViewModel() {

    private val _state = MutableStateFlow<CheckUiState>(CheckUiState.Idle)
    val state: StateFlow<CheckUiState> = _state

    private val resultChannel = Channel<Long>(Channel.BUFFERED)

    /** Emits the id of each finished scan result so the screen can open it. */
    val results = resultChannel.receiveAsFlow()

    private var activeJob: Job? = null

    fun checkText(text: String) {
        if (text.isBlank()) {
            _state.value = CheckUiState.NoText
            return
        }
        activeJob?.cancel()
        activeJob = viewModelScope.launch {
            // Stage 1: Membaca teks
            _state.value = CheckUiState.Analyzing(
                stage = AnalysisStage.EXTRACTING_TEXT,
                progressText = "Membaca teks masukan...",
                isImage = false,
            )
            delay(500)

            // Stage 2: Memeriksa tautan & indikator penipuan
            _state.value = CheckUiState.Analyzing(
                stage = AnalysisStage.CHECKING_PATTERNS,
                progressText = "Memeriksa tautan & kata kunci...",
                isImage = false,
            )
            delay(650)

            // Stage 3: Menilai skor risiko
            _state.value = CheckUiState.Analyzing(
                stage = AnalysisStage.CALCULATING_SCORE,
                progressText = "Menilai skor risiko & keamanan...",
                isImage = false,
            )

            val input = MessageInput(
                sourcePackage = AnalyzeMessageUseCase.MANUAL_SOURCE,
                senderLabel = "",
                text = text,
                timestamp = System.currentTimeMillis(),
                senderKind = SenderKind.UNKNOWN,
            )
            val result = analyze(input, notify = false)
            delay(500)

            // Stage 4: Selesai
            _state.value = CheckUiState.Analyzing(
                stage = AnalysisStage.COMPLETE,
                progressText = "Analisis selesai! Menampilkan hasil...",
                isImage = false,
            )
            delay(400)
            _state.value = CheckUiState.Idle
            resultChannel.send(result.id)
        }
    }

    fun checkImage(uri: Uri) {
        activeJob?.cancel()
        activeJob = viewModelScope.launch {
            // Stage 1: OCR ML Kit on-device
            _state.value = CheckUiState.Analyzing(
                stage = AnalysisStage.EXTRACTING_TEXT,
                progressText = "Membaca gambar tangkapan layar (OCR)...",
                isImage = true,
            )

            val outcome = extractor.extract(uri)
            val extractedText = when (outcome) {
                is Outcome.Failure -> {
                    _state.value = CheckUiState.Error
                    return@launch
                }
                is Outcome.Success -> outcome.value
            }

            if (extractedText.isBlank()) {
                _state.value = CheckUiState.NoText
                return@launch
            }
            delay(600)

            // Stage 2: Memeriksa tautan
            _state.value = CheckUiState.Analyzing(
                stage = AnalysisStage.CHECKING_PATTERNS,
                progressText = "Memeriksa tautan & berkas dalam gambar...",
                isImage = true,
            )
            delay(700)

            // Stage 3: Menilai skor risiko
            _state.value = CheckUiState.Analyzing(
                stage = AnalysisStage.CALCULATING_SCORE,
                progressText = "Menilai skor risiko AI on-device...",
                isImage = true,
            )

            val input = MessageInput(
                sourcePackage = AnalyzeMessageUseCase.MANUAL_SOURCE,
                senderLabel = "",
                text = extractedText,
                timestamp = System.currentTimeMillis(),
                senderKind = SenderKind.UNKNOWN,
            )
            val result = analyze(input, notify = false)
            delay(550)

            // Stage 4: Selesai
            _state.value = CheckUiState.Analyzing(
                stage = AnalysisStage.COMPLETE,
                progressText = "Analisis selesai! Menampilkan rincian...",
                isImage = true,
            )
            delay(400)
            _state.value = CheckUiState.Idle
            resultChannel.send(result.id)
        }
    }

    fun reset() {
        activeJob?.cancel()
        activeJob = null
        _state.value = CheckUiState.Idle
    }
}
