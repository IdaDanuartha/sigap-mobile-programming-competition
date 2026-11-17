package id.sigap.app.domain.normalization

import id.sigap.app.domain.detection.UrlAnalyzer
import java.security.MessageDigest

/** Type of entity a user looks up or reports (S06b, S12). */
enum class EntityType { PHONE, BANK_ACCOUNT, LINK }

/** Normalized, hashed and masked representation of a user-entered entity (PRD section 7.4). */
data class NormalizedEntity(
    val type: EntityType,
    val canonical: String,
    val sha256: String,
    val maskedDisplay: String,
    val fullUrl: String? = null,
)

/** Normalizes phone numbers (to E.164 +62), bank accounts (digits only) and links (registrable domain). */
class EntityNormalizer(private val urlAnalyzer: UrlAnalyzer = UrlAnalyzer()) {

    fun normalize(type: EntityType, rawInput: String): NormalizedEntity? {
        val trimmed = rawInput.trim()
        if (trimmed.isBlank()) return null
        return when (type) {
            EntityType.PHONE -> normalizePhone(trimmed)
            EntityType.BANK_ACCOUNT -> normalizeAccount(trimmed)
            EntityType.LINK -> normalizeLink(trimmed)
        }
    }

    private fun normalizePhone(raw: String): NormalizedEntity? {
        val digits = raw.filter { it.isDigit() }
        if (digits.length < 8) return null
        val e164 = when {
            digits.startsWith("62") -> "+$digits"
            digits.startsWith("0") -> "+62${digits.drop(1)}"
            digits.startsWith("8") -> "+62$digits"
            else -> "+$digits"
        }
        val body = e164.removePrefix("+62")
        val masked = if (body.length >= 7) {
            "+62 ${body.take(3)} **** ${body.takeLast(4)}"
        } else {
            e164
        }
        return NormalizedEntity(EntityType.PHONE, e164, sha256(e164), masked)
    }

    private fun normalizeAccount(raw: String): NormalizedEntity? {
        val digits = raw.filter { it.isDigit() }
        if (digits.length < 6) return null
        val masked = if (digits.length >= 7) {
            "${digits.take(3)} **** ${digits.takeLast(4)}"
        } else {
            digits
        }
        return NormalizedEntity(EntityType.BANK_ACCOUNT, digits, sha256(digits), masked)
    }

    private fun normalizeLink(raw: String): NormalizedEntity? {
        val parsed = urlAnalyzer.extract(raw).firstOrNull()
        val host = parsed?.host ?: raw
            .replace(Regex("^[a-z][a-z0-9+.-]*://", RegexOption.IGNORE_CASE), "")
            .takeWhile { it != '/' && it != '?' && it != '#' }
            .substringAfterLast('@')
            .replace(Regex(":\\d+$"), "")
            .lowercase()
        if (!host.contains('.')) return null
        val domain = urlAnalyzer.registrableDomain(host)
        val fullUrl = if (raw.startsWith("http://", true) || raw.startsWith("https://", true)) raw else "https://$raw"
        return NormalizedEntity(EntityType.LINK, domain, sha256(domain), domain, fullUrl = fullUrl)
    }

    companion object {
        fun sha256(value: String): String =
            MessageDigest.getInstance("SHA-256")
                .digest(value.toByteArray())
                .joinToString("") { "%02x".format(it) }
    }
}
