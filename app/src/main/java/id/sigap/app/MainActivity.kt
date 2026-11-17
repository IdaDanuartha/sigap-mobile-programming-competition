package id.sigap.app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.core.content.IntentCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import id.sigap.app.core.designsystem.SigapTheme
import id.sigap.app.core.navigation.LaunchRequest
import id.sigap.app.core.navigation.SigapNavHost
import id.sigap.app.domain.model.ThemeMode
import id.sigap.app.feature.splash.AnimatedLogoSplash
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

/** Single activity host. Extends AppCompatActivity so per-app language switching works seamlessly. */
class MainActivity : AppCompatActivity() {

    private var onboardingDone by mutableStateOf<Boolean?>(null)
    private var launchRequest by mutableStateOf<LaunchRequest?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        val systemSplash = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        if (savedInstanceState == null) launchRequest = parseIntent(intent)

        val container = (application as SigapApp).container
        val settings = container.settings
        systemSplash.setKeepOnScreenCondition { onboardingDone == null }
        lifecycleScope.launch {
            onboardingDone = withTimeoutOrNull(SETTINGS_TIMEOUT_MS) { settings.onboardingDone.first() } ?: false
        }

        setContent {
            val userSettings by container.settingsState.collectAsStateWithLifecycle()
            val darkTheme = when (userSettings.themeMode) {
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
                ThemeMode.SYSTEM -> androidx.compose.foundation.isSystemInDarkTheme()
            }
            SigapTheme(darkTheme = darkTheme) {
                var splashDone by rememberSaveable { mutableStateOf(false) }
                val done = onboardingDone
                when {
                    done == null -> Unit
                    !splashDone -> AnimatedLogoSplash(onFinished = { splashDone = true })
                    else -> SigapNavHost(
                        startOnboarding = !done,
                        onOnboardingFinished = { lifecycleScope.launch { settings.setOnboardingDone() } },
                        launchRequest = launchRequest,
                        onLaunchRequestHandled = { launchRequest = null },
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        parseIntent(intent)?.let { launchRequest = it }
    }

    private fun parseIntent(intent: Intent): LaunchRequest? {
        val alertId = intent.getLongExtra(EXTRA_RESULT_ID, NO_ID)
        return when {
            alertId != NO_ID -> LaunchRequest.OpenAlert(alertId)
            intent.action == Intent.ACTION_SEND && intent.type == MIME_TEXT ->
                intent.getStringExtra(Intent.EXTRA_TEXT)?.let { LaunchRequest.SharedText(it) }
            intent.action == Intent.ACTION_SEND && intent.type?.startsWith(MIME_IMAGE_PREFIX) == true ->
                IntentCompat.getParcelableExtra(intent, Intent.EXTRA_STREAM, Uri::class.java)?.let { LaunchRequest.SharedImage(it) }
            else -> null
        }
    }

    companion object {
        const val EXTRA_RESULT_ID = "id.sigap.app.extra.RESULT_ID"
        private const val NO_ID = -1L
        private const val SETTINGS_TIMEOUT_MS = 500L
        private const val MIME_TEXT = "text/plain"
        private const val MIME_IMAGE_PREFIX = "image/"
    }
}

