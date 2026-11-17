package id.sigap.app.service

import android.app.Notification
import android.os.Bundle
import android.service.notification.StatusBarNotification

/** Sender label and combined text read from a message notification. */
data class ParsedNotification(val sender: String, val text: String)

/** Extracts the fields the scanner needs from a notification. Returns null when it should be ignored. */
object NotificationParser {

    fun parse(sbn: StatusBarNotification, ownPackage: String): ParsedNotification? {
        val notification = sbn.notification
        if (sbn.packageName == ownPackage || shouldIgnore(notification)) return null
        val extras = notification.extras
        val sender = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString().orEmpty().trim()
        val text = collectText(extras)
        return if (sender.isBlank() || text.isBlank()) null else ParsedNotification(sender, text)
    }

    private fun shouldIgnore(notification: Notification): Boolean =
        notification.flags and Notification.FLAG_GROUP_SUMMARY != 0 ||
            notification.flags and Notification.FLAG_ONGOING_EVENT != 0 ||
            notification.category == Notification.CATEGORY_CALL

    @Suppress("DEPRECATION")
    private fun collectText(extras: Bundle): String {
        val lines = linkedSetOf<String>()
        extras.getCharSequence(Notification.EXTRA_TEXT)?.let { lines += it.toString() }
        extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.let { lines += it.toString() }
        extras.getParcelableArray(Notification.EXTRA_MESSAGES)?.forEach { item ->
            (item as? Bundle)?.getCharSequence("text")?.let { lines += it.toString() }
        }
        return lines.map { it.trim() }.filter { it.isNotEmpty() }.joinToString("\n")
    }
}
