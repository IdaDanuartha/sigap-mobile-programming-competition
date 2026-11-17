package id.sigap.app.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import id.sigap.app.MainActivity
import id.sigap.app.R
import id.sigap.app.core.util.PermissionChecks
import id.sigap.app.core.util.stringRes
import id.sigap.app.domain.model.AlertNotifier
import id.sigap.app.domain.model.RiskLevel
import id.sigap.app.domain.model.ScanResult

/** Creates alert channels and posts Danger and Caution warnings (PRD section 5.6). */
class AlertNotificationManager(private val context: Context) : AlertNotifier {

    fun createChannels() {
        val manager = context.getSystemService(NotificationManager::class.java)
        listOf(
            Triple(CHANNEL_DANGER, R.string.channel_danger, NotificationManager.IMPORTANCE_HIGH),
            Triple(CHANNEL_CAUTION, R.string.channel_caution, NotificationManager.IMPORTANCE_DEFAULT),
            Triple(CHANNEL_FAMILY, R.string.channel_family, NotificationManager.IMPORTANCE_HIGH),
        ).forEach { (id, name, importance) ->
            manager.createNotificationChannel(NotificationChannel(id, context.getString(name), importance))
        }
    }

    override fun notify(result: ScanResult, senderKey: String) {
        if (!PermissionChecks.canPostNotifications(context)) return
        val danger = result.level == RiskLevel.DANGER
        val title = context.getString(if (danger) R.string.alert_title_danger else R.string.alert_title_caution)
        val body = result.reasons.firstOrNull()?.let { context.getString(it.stringRes()) }
            ?: context.getString(R.string.alert_body_fallback)
        val notificationId = result.id.toInt()

        val builder = NotificationCompat.Builder(context, if (danger) CHANNEL_DANGER else CHANNEL_CAUTION)
            .setSmallIcon(R.drawable.ic_stat_sigap)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(if (danger) NotificationCompat.PRIORITY_HIGH else NotificationCompat.PRIORITY_DEFAULT)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .setPublicVersion(publicVersion(danger))
            .setAutoCancel(true)
            .setContentIntent(openAppIntent(notificationId))
            .addAction(0, context.getString(R.string.action_details), openAppIntent(notificationId))
            .addAction(0, context.getString(R.string.action_trust_sender), actionIntent(NotificationActionReceiver.ACTION_TRUST, notificationId, senderKey))
            .addAction(0, context.getString(R.string.action_dismiss), actionIntent(NotificationActionReceiver.ACTION_DISMISS, notificationId, senderKey))

        runCatching { NotificationManagerCompat.from(context).notify(notificationId, builder.build()) }
    }

    fun notifyFamily(memberName: String, threatSummary: String, alertId: Long = 1L) {
        if (!PermissionChecks.canPostNotifications(context)) return
        val title = "Peringatan Keluarga: $memberName"
        val body = "Terdeteksi ancaman pada ponsel $memberName: $threatSummary. Segera hubungi untuk verifikasi."
        val notificationId = (FAMILY_NOTIFICATION_OFFSET + alertId).toInt()

        val builder = NotificationCompat.Builder(context, CHANNEL_FAMILY)
            .setSmallIcon(R.drawable.ic_stat_sigap)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(openAppIntent(notificationId))
            .addAction(0, context.getString(R.string.action_details), openAppIntent(notificationId))

        runCatching { NotificationManagerCompat.from(context).notify(notificationId, builder.build()) }
    }

    private fun publicVersion(danger: Boolean) =
        NotificationCompat.Builder(context, if (danger) CHANNEL_DANGER else CHANNEL_CAUTION)
            .setSmallIcon(R.drawable.ic_stat_sigap)
            .setContentTitle(context.getString(R.string.alert_public_text))
            .build()

    private fun openAppIntent(requestCode: Int): PendingIntent = PendingIntent.getActivity(
        context,
        requestCode,
        Intent(context, MainActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            .putExtra(MainActivity.EXTRA_RESULT_ID, requestCode.toLong()),
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
    )

    private fun actionIntent(action: String, notificationId: Int, senderKey: String): PendingIntent {
        val intent = Intent(context, NotificationActionReceiver::class.java)
            .setAction(action)
            .putExtra(NotificationActionReceiver.EXTRA_NOTIFICATION_ID, notificationId)
            .putExtra(NotificationActionReceiver.EXTRA_SENDER_KEY, senderKey)
        val requestCode = notificationId * ACTION_REQUEST_MULTIPLIER + action.hashCode() % ACTION_REQUEST_MULTIPLIER
        return PendingIntent.getBroadcast(context, requestCode, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
    }

    companion object {
        const val CHANNEL_DANGER = "danger_alerts"
        const val CHANNEL_CAUTION = "caution_alerts"
        const val CHANNEL_FAMILY = "family_alerts"
        private const val ACTION_REQUEST_MULTIPLIER = 10
        private const val FAMILY_NOTIFICATION_OFFSET = 90_000
    }
}

