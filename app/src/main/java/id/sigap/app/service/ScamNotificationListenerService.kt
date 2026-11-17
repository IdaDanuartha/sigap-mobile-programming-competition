package id.sigap.app.service

import android.content.ComponentName
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import id.sigap.app.SigapApp
import id.sigap.app.domain.model.MessageInput
import id.sigap.app.domain.model.RiskLevel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * Receives notification events and hands them to the use cases. Contains no business logic
 * and never does heavy work on the main thread (PRD section 5.1).
 */
class ScamNotificationListenerService : NotificationListenerService() {

    private val container get() = (application as SigapApp).container
    private val scope by lazy { CoroutineScope(SupervisorJob() + container.defaultDispatcher) }
    private val recent = LinkedHashSet<String>()

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val settings = container.settingsState.value
        if (sbn.packageName !in container.monitoredPackages || sbn.packageName in settings.disabledPackages) return
        val parsed = NotificationParser.parse(sbn, packageName) ?: return
        if (isDuplicate(sbn.key, parsed.text)) return

        scope.launch {
            val kind = container.classifySender(sbn.packageName, parsed.sender) ?: return@launch
            val result = container.analyzeMessage(
                MessageInput(sbn.packageName, parsed.sender, parsed.text, sbn.postTime, kind),
            )
            if (settings.hideDangerous && result.level == RiskLevel.DANGER) cancelNotification(sbn.key)
        }
    }

    override fun onListenerDisconnected() {
        requestRebind(ComponentName(this, ScamNotificationListenerService::class.java))
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }

    @Synchronized
    private fun isDuplicate(key: String, text: String): Boolean {
        val id = "$key:${text.hashCode()}"
        if (!recent.add(id)) return true
        if (recent.size > MAX_RECENT) recent.remove(recent.first())
        return false
    }

    private companion object {
        const val MAX_RECENT = 100
    }
}

