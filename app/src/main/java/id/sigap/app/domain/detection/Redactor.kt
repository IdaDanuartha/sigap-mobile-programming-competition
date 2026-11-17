package id.sigap.app.domain.detection

/** Removes personal data from message text before it is stored or sent to the cloud. */
class Redactor {

    private val email = Regex("[\\w.+-]+@[\\w-]+(?:\\.[\\w-]+)+")
    private val phone = Regex("(?<!\\d)(?:\\+?62[\\s-]?|0)8[\\d\\s-]{7,12}\\d")
    private val longDigits = Regex("\\d(?:[\\s-]?\\d){8,}")
    private val name = Regex("\\b(?:Pak|Bu|Bapak|Ibu|Kak|Mas|Mbak|Sdr|Sdri|Mr|Mrs|Ms)\\.?\\s+[A-Z][a-z]+")

    /** Replaces emails, phone numbers, long digit runs and honorific names with tokens. */
    fun redact(text: String): String =
        text.replace(email, TOKEN_EMAIL)
            .replace(phone, TOKEN_PHONE)
            .replace(longDigits, TOKEN_NUMBER)
            .replace(name, TOKEN_NAME)

    /** Redacts, collapses whitespace and truncates to [maxChars] characters. */
    fun snippet(text: String, maxChars: Int = DetectionConfig.SNIPPET_MAX_CHARS): String {
        val clean = redact(text).replace(Regex("\\s+"), " ").trim()
        return if (clean.length <= maxChars) clean else clean.take(maxChars - 1).trimEnd() + ELLIPSIS
    }

    companion object {
        const val TOKEN_EMAIL = "[EMAIL]"
        const val TOKEN_PHONE = "[PHONE]"
        const val TOKEN_NUMBER = "[NUMBER]"
        const val TOKEN_NAME = "[NAME]"
        private const val ELLIPSIS = "\u2026"
    }
}

