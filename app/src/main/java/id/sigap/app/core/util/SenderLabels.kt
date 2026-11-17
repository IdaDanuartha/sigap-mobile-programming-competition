package id.sigap.app.core.util

import java.security.MessageDigest

/** Helpers for classifying and hashing notification sender labels. */
object SenderLabels {
    private const val MIN_PHONE_DIGITS = 8
    private const val MAX_SHORTCODE_LENGTH = 11
    private const val PHONE_CHARS = "+ -()"

    fun looksLikePhoneNumber(label: String): Boolean =
        label.count { it.isDigit() } >= MIN_PHONE_DIGITS && label.all { it.isDigit() || it in PHONE_CHARS }

    /** Alphanumeric sender ID without spaces, such as "BCA" or "JNE-INFO". */
    fun looksLikeShortCode(label: String): Boolean =
        label.length <= MAX_SHORTCODE_LENGTH && label.any { it.isLetter() } &&
            label.all { it.isLetterOrDigit() || it == '-' }

    /** SHA-256 of the trimmed, lowercased label. Used as the trusted-sender key. */
    fun hash(label: String): String =
        MessageDigest.getInstance("SHA-256")
            .digest(label.trim().lowercase().toByteArray())
            .joinToString("") { "%02x".format(it) }
}
