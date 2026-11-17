package id.sigap.app.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.sigap.app.data.local.SettingsRepository
import id.sigap.app.domain.model.Sensitivity
import id.sigap.app.domain.model.ThemeMode
import id.sigap.app.domain.model.TrustedSender
import id.sigap.app.domain.model.TrustedSenderRepository
import id.sigap.app.domain.model.UserSettings
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val settings: SettingsRepository,
    private val trusted: TrustedSenderRepository,
    private val clearAll: () -> Unit,
) : ViewModel() {

    val userSettings: StateFlow<UserSettings> =
        settings.settings.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000L), UserSettings())

    val trustedSenders: StateFlow<List<TrustedSender>> =
        trusted.observeAll().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000L), emptyList())

    fun setUserName(name: String) {
        viewModelScope.launch { settings.setUserName(name) }
    }

    fun setSensitivity(value: Sensitivity) {
        viewModelScope.launch { settings.setSensitivity(value) }
    }

    fun setCloudConsent(value: Boolean) {
        viewModelScope.launch { settings.setCloudConsent(value) }
    }

    fun setRetentionDays(value: Int) {
        viewModelScope.launch { settings.setRetentionDays(value) }
    }

    fun setThemeMode(value: ThemeMode) {
        viewModelScope.launch { settings.setThemeMode(value) }
    }

    fun setHideDangerous(value: Boolean) {
        viewModelScope.launch { settings.setHideDangerous(value) }
    }

    fun setPackageEnabled(packageName: String, enabled: Boolean) {
        viewModelScope.launch { settings.setPackageEnabled(packageName, enabled) }
    }

    fun removeTrustedSender(key: String) {
        viewModelScope.launch { trusted.remove(key) }
    }

    fun addTrustedSender(key: String) {
        viewModelScope.launch { trusted.trust(key) }
    }

    fun clearAllData() {
        clearAll()
    }
}
