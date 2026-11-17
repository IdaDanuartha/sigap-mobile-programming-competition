package id.sigap.app.domain.detection

import id.sigap.app.core.util.levenshtein
import id.sigap.app.domain.model.ReasonCode

/** A URL split into the parts the rules care about. */
data class ParsedUrl(val raw: String, val host: String, val hasUserInfo: Boolean)

/** Finds URLs in text and reports link-risk reasons for them. */
class UrlAnalyzer {

    private val urlRegex = Regex(
        "(?:https?://\\S+)|(?:\\bwww\\.\\S+)|(?:\\b[a-z0-9-]+(?:\\.[a-z0-9-]+)*\\.[a-z]{2,}(?:/\\S*)?)",
        RegexOption.IGNORE_CASE,
    )
    private val ipRegex = Regex("^\\d{1,3}(?:\\.\\d{1,3}){3}$")
    private val leetMap = mapOf('0' to 'o', '1' to 'l', '3' to 'e', '5' to 's', '4' to 'a')

    /** Extracts URLs, skipping file names such as "invite.apk" that only look like domains. */
    fun extract(text: String): List<ParsedUrl> =
        urlRegex.findAll(text)
            .map { it.value.trimEnd('.', ',', ';', ')', '!', '?') }
            .map(::parse)
            .filter { it.host.contains('.') && it.host.substringAfterLast('.') !in DetectionConfig.FILE_EXTENSIONS }
            .distinctBy { it.raw }
            .toList()

    /** Returns the set of link-risk reasons for the given URLs. */
    fun reasonsFor(urls: List<ParsedUrl>): Set<ReasonCode> = buildSet {
        urls.forEach { url ->
            val domain = registrableDomain(url.host)
            if (domain in DetectionConfig.URL_SHORTENERS) add(ReasonCode.SHORTENED_LINK)
            if (ipRegex.matches(url.host)) add(ReasonCode.IP_LINK)
            if (url.host.substringAfterLast('.') in DetectionConfig.RISKY_TLDS) add(ReasonCode.RISKY_TLD)
            if (url.hasUserInfo) add(ReasonCode.USER_INFO_URL)
            if (url.host.split('.').any { it.startsWith("xn--") }) add(ReasonCode.PUNYCODE_URL)
            if (isLookalike(domain)) add(ReasonCode.LOOKALIKE_DOMAIN)
        }
    }

    /** Last two labels, or three when the second-level label is generic (for example co.id). */
    fun registrableDomain(host: String): String {
        val labels = host.split('.')
        if (labels.size <= 2) return host
        val useThree = labels.last().length == 2 && labels[labels.size - 2] in DetectionConfig.SECOND_LEVEL_LABELS
        return labels.takeLast(if (useThree) 3 else 2).joinToString(".")
    }

    private fun parse(raw: String): ParsedUrl {
        val noScheme = raw.replace(Regex("^[a-z][a-z0-9+.-]*://", RegexOption.IGNORE_CASE), "")
        val authority = noScheme.takeWhile { it != '/' && it != '?' && it != '#' }
        val host = authority.substringAfterLast('@').replace(Regex(":\\d+$"), "").lowercase()
        return ParsedUrl(raw, host, hasUserInfo = authority.contains('@'))
    }

    private fun isLookalike(registrable: String): Boolean {
        val name = registrable.substringBefore('.').map { leetMap[it] ?: it }.joinToString("")
        val tokens = name.split('-').filter { it.isNotEmpty() }
        return DetectionConfig.BRANDS.any { (brand, officials) ->
            registrable !in officials && matchesBrand(name, tokens, brand)
        }
    }

    private fun matchesBrand(name: String, tokens: List<String>, brand: String): Boolean {
        if (tokens.any { it == brand }) return true
        val closeEnough = brand.length >= DetectionConfig.LOOKALIKE_MIN_BRAND_LENGTH &&
            tokens.any { levenshtein(it, brand) <= DetectionConfig.LOOKALIKE_MAX_DISTANCE }
        val contained = brand.length >= DetectionConfig.LOOKALIKE_CONTAINS_MIN_BRAND_LENGTH && name.contains(brand)
        return closeEnough || contained
    }
}
