package id.sigap.app.domain.detection

import id.sigap.app.domain.model.MessageInput
import id.sigap.app.domain.model.ReasonCode
import id.sigap.app.domain.model.RiskLevel
import id.sigap.app.domain.model.ScamCategory
import id.sigap.app.domain.model.Sensitivity
import id.sigap.app.domain.model.Signal
import id.sigap.app.domain.model.Verdict

/** On-device weighted rule engine (PRD section 5.3 and 5.4). Pure Kotlin, no Android dependencies. */
class RuleEngine(private val urlAnalyzer: UrlAnalyzer = UrlAnalyzer()) {

    /** Scores [input] and returns the level, category and ordered reasons. */
    fun analyze(input: MessageInput, sensitivity: Sensitivity = Sensitivity.BALANCED): Verdict {
        val text = input.text
        val urls = urlAnalyzer.extract(text)
        val hasApk = DetectionConfig.APK_FILE.containsMatchIn(text)

        if (isLegitimateOtp(text, hasApk, urls.isEmpty())) {
            return Verdict(DetectionConfig.SAFE_OTP_SCORE, RiskLevel.SAFE, ScamCategory.OTHER, emptyList(), false)
        }

        val reasons = collectReasons(text, urls, hasApk)
        val signals = reasons.map { Signal(it, DetectionConfig.WEIGHTS.getValue(it)) }.sortedByDescending { it.weight }
        val hardDanger = isHardDanger(reasons, hasApk)

        var score = signals.sumOf { it.weight }.coerceAtMost(DetectionConfig.MAX_SCORE)
        if (hardDanger) score = maxOf(score, sensitivity.dangerAt)

        return Verdict(
            score = score,
            level = levelFor(score, sensitivity),
            category = categorize(text, hasApk, reasons),
            reasons = signals.map { it.reason }.take(DetectionConfig.MAX_REASONS),
            hardDanger = hardDanger,
        )
    }

    private fun collectReasons(text: String, urls: List<ParsedUrl>, hasApk: Boolean): Set<ReasonCode> =
        buildSet {
            if (hasApk) add(ReasonCode.APK_FILE)
            if (DetectionConfig.FILE_LURE.matches(text)) add(ReasonCode.FILE_LURE)
            if (urls.isNotEmpty()) add(ReasonCode.LINK_FROM_UNKNOWN)
            addAll(urlAnalyzer.reasonsFor(urls))
            if (DetectionConfig.URGENCY.matches(text)) add(ReasonCode.URGENCY)
            if (DetectionConfig.OTP_SHARE_REQUEST.containsMatchIn(text)) add(ReasonCode.OTP_SHARE_REQUEST)
            if (DetectionConfig.CREDENTIAL.matches(text)) add(ReasonCode.CREDENTIAL_REQUEST)
            if (DetectionConfig.IMPERSONATION.matches(text)) add(ReasonCode.IMPERSONATION)
            if (DetectionConfig.MONEY_REWARD.matches(text)) add(ReasonCode.MONEY_REWARD)
            if (DetectionConfig.SOCIAL_ENGINEERING.matches(text)) add(ReasonCode.SOCIAL_ENGINEERING)
        }

    /** A real OTP message warns not to share the code, has no link or file, and never asks for the code. */
    private fun isLegitimateOtp(text: String, hasApk: Boolean, noUrls: Boolean): Boolean =
        DetectionConfig.OTP_TERMS.matches(text) &&
            DetectionConfig.NO_SHARE_WARNING.matches(text) &&
            noUrls && !hasApk &&
            !DetectionConfig.OTP_SHARE_REQUEST.containsMatchIn(text)

    private fun isHardDanger(reasons: Set<ReasonCode>, hasApk: Boolean): Boolean =
        hasApk || (ReasonCode.CREDENTIAL_REQUEST in reasons && ReasonCode.URGENCY in reasons)

    private fun levelFor(score: Int, sensitivity: Sensitivity): RiskLevel = when {
        score >= sensitivity.dangerAt -> RiskLevel.DANGER
        score >= sensitivity.cautionAt -> RiskLevel.CAUTION
        else -> RiskLevel.SAFE
    }

    private fun categorize(text: String, hasApk: Boolean, reasons: Set<ReasonCode>): ScamCategory {
        val hinted = DetectionConfig.CATEGORY_HINTS.firstOrNull { (group, _) -> group.matches(text) }?.second
        return when {
            hinted != null -> hinted
            hasApk -> ScamCategory.APK_MALWARE
            reasons.isEmpty() -> ScamCategory.OTHER
            ReasonCode.LOOKALIKE_DOMAIN in reasons -> ScamCategory.BANK_PHISHING
            else -> ScamCategory.OTHER
        }
    }
}
