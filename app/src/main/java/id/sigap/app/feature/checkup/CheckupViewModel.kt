package id.sigap.app.feature.checkup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.sigap.app.data.local.SettingsRepository
import id.sigap.app.data.system.SecurityCheckupSource
import id.sigap.app.domain.checkup.CheckIds
import id.sigap.app.domain.checkup.CheckKind
import id.sigap.app.domain.checkup.CheckupItem
import id.sigap.app.domain.checkup.CheckupLabel
import id.sigap.app.domain.checkup.CheckupScorer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class CheckupUiState(
    val score: Int = 100,
    val label: CheckupLabel = CheckupLabel.GOOD,
    val autoChecks: List<CheckupItem> = emptyList(),
    val selfChecks: List<CheckupItem> = emptyList(),
)

class CheckupViewModel(
    private val source: SecurityCheckupSource,
    private val settings: SettingsRepository,
) : ViewModel() {

    private val refreshTick = MutableStateFlow(0)

    val uiState: StateFlow<CheckupUiState> =
        combine(settings.settings, refreshTick) { userSettings, _ ->
            val auto = source.autoChecks()
            val unknownRows = source.installedMonitoredApps().map { pkg ->
                val id = CheckIds.UNKNOWN_SOURCES_PREFIX + pkg
                CheckupItem(
                    id = id,
                    kind = CheckKind.SELF,
                    weight = CheckIds.WEIGHT_UNKNOWN_SOURCES,
                    passed = id in userSettings.confirmedChecks,
                    packageName = pkg,
                )
            }
            val protectRow = CheckupItem(
                id = CheckIds.PLAY_PROTECT,
                kind = CheckKind.SELF,
                weight = CheckIds.WEIGHT_PLAY_PROTECT,
                passed = CheckIds.PLAY_PROTECT in userSettings.confirmedChecks,
            )
            val self = unknownRows + protectRow
            val all = auto + self
            val score = CheckupScorer.score(all)
            CheckupUiState(
                score = score,
                label = CheckupScorer.label(score),
                autoChecks = auto,
                selfChecks = self,
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000L), CheckupUiState())

    fun refresh() {
        refreshTick.value++
    }

    fun toggleConfirmed(id: String, confirmed: Boolean) {
        viewModelScope.launch { settings.setCheckConfirmed(id, confirmed) }
    }
}
