package id.sigap.app.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import id.sigap.app.SigapApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/** Handles the "Trust sender" and "Dismiss" notification actions. */
class NotificationActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, -1)
        NotificationManagerCompat.from(context).cancel(notificationId)
        if (intent.action != ACTION_TRUST) return
        val senderKey = intent.getStringExtra(EXTRA_SENDER_KEY) ?: return
        val container = (context.applicationContext as SigapApp).container
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                container.trustedSenders.trust(senderKey)
            } finally {
                pending.finish()
            }
        }
    }

    companion object {
        const val ACTION_TRUST = "id.sigap.app.action.TRUST_SENDER"
        const val ACTION_DISMISS = "id.sigap.app.action.DISMISS_ALERT"
        const val EXTRA_NOTIFICATION_ID = "notification_id"
        const val EXTRA_SENDER_KEY = "sender_key"
    }
}
