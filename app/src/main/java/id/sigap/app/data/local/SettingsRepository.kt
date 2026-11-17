package id.sigap.app.data.local

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import id.sigap.app.domain.model.Sensitivity
import id.sigap.app.domain.model.ThemeMode
import id.sigap.app.domain.model.UserSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.settingsStore by preferencesDataStore(name = "settings")

/** Persists user settings in DataStore Preferences. */
class SettingsRepository(private val context: Context) {

    /** Emits whether the user has finished onboarding. */
    val onboardingDone: Flow<Boolean> =
        context.settingsStore.data.map { it[KEY_ONBOARDING_DONE] ?: false }

    val settings: Flow<UserSettings> = context.settingsStore.data.map(::toSettings)

    suspend fun setOnboardingDone() = edit { it[KEY_ONBOARDING_DONE] = true }
    suspend fun setUserName(name: String) = edit { it[KEY_USER_NAME] = name }
    suspend fun setSensitivity(value: Sensitivity) = edit { it[KEY_SENSITIVITY] = value.name }
    suspend fun setCloudConsent(value: Boolean) = edit { it[KEY_CLOUD] = value }
    suspend fun setRetentionDays(value: Int) = edit { it[KEY_RETENTION] = value }
    suspend fun setThemeMode(value: ThemeMode) = edit { it[KEY_THEME] = value.name }
    suspend fun setHideDangerous(value: Boolean) = edit { it[KEY_HIDE_DANGEROUS] = value }

    suspend fun setPackageEnabled(packageName: String, enabled: Boolean) = edit {
        val current = it[KEY_DISABLED_PACKAGES].orEmpty()
        it[KEY_DISABLED_PACKAGES] = if (enabled) current - packageName else current + packageName
    }

    suspend fun setCheckConfirmed(id: String, confirmed: Boolean) = edit {
        val current = it[KEY_CONFIRMED_CHECKS].orEmpty()
        it[KEY_CONFIRMED_CHECKS] = if (confirmed) current + id else current - id
    }

    private suspend fun edit(block: (androidx.datastore.preferences.core.MutablePreferences) -> Unit) {
        context.settingsStore.edit { block(it) }
    }

    private fun toSettings(prefs: Preferences) = UserSettings(
        userName = prefs[KEY_USER_NAME] ?: "Danuartha",
        sensitivity = prefs[KEY_SENSITIVITY]?.let { runCatching { Sensitivity.valueOf(it) }.getOrNull() } ?: Sensitivity.BALANCED,
        cloudConsent = prefs[KEY_CLOUD] ?: true,
        retentionDays = prefs[KEY_RETENTION] ?: UserSettings.DEFAULT_RETENTION_DAYS,
        themeMode = prefs[KEY_THEME]?.let { runCatching { ThemeMode.valueOf(it) }.getOrNull() } ?: ThemeMode.SYSTEM,
        hideDangerous = prefs[KEY_HIDE_DANGEROUS] ?: false,
        disabledPackages = prefs[KEY_DISABLED_PACKAGES].orEmpty(),
        confirmedChecks = prefs[KEY_CONFIRMED_CHECKS].orEmpty(),
    )

    private companion object {
        val KEY_USER_NAME = stringPreferencesKey("user_name")
        val KEY_ONBOARDING_DONE = booleanPreferencesKey("onboarding_done")
        val KEY_SENSITIVITY = stringPreferencesKey("sensitivity")
        val KEY_CLOUD = booleanPreferencesKey("cloud_consent")
        val KEY_RETENTION = intPreferencesKey("retention_days")
        val KEY_THEME = stringPreferencesKey("theme_mode")
        val KEY_HIDE_DANGEROUS = booleanPreferencesKey("hide_dangerous")
        val KEY_DISABLED_PACKAGES = stringSetPreferencesKey("disabled_packages")
        val KEY_CONFIRMED_CHECKS = stringSetPreferencesKey("confirmed_checks")
    }
}
