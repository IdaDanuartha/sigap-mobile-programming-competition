package id.sigap.app.core.util

import androidx.annotation.StringRes
import id.sigap.app.R
import id.sigap.app.domain.model.RiskLevel
import id.sigap.app.domain.model.ScamCategory

/** Localized name for a scam category. */
@StringRes
fun ScamCategory.stringRes(): Int = when (this) {
    ScamCategory.APK_MALWARE -> R.string.category_APK_MALWARE
    ScamCategory.FAKE_COURIER -> R.string.category_FAKE_COURIER
    ScamCategory.FAKE_INVITATION -> R.string.category_FAKE_INVITATION
    ScamCategory.AUTHORITY_IMPERSONATION -> R.string.category_AUTHORITY_IMPERSONATION
    ScamCategory.BANK_PHISHING -> R.string.category_BANK_PHISHING
    ScamCategory.FAKE_PRIZE -> R.string.category_FAKE_PRIZE
    ScamCategory.INVESTMENT -> R.string.category_INVESTMENT
    ScamCategory.ONLINE_LOAN -> R.string.category_ONLINE_LOAN
    ScamCategory.ROMANCE -> R.string.category_ROMANCE
    ScamCategory.JOB_SCAM -> R.string.category_JOB_SCAM
    ScamCategory.MARKETPLACE_FRAUD -> R.string.category_MARKETPLACE_FRAUD
    ScamCategory.OTP_THEFT -> R.string.category_OTP_THEFT
    ScamCategory.OTHER -> R.string.category_OTHER
}

/** Localized label for a risk level. */
@StringRes
fun RiskLevel.labelRes(): Int = when (this) {
    RiskLevel.DANGER -> R.string.risk_danger
    RiskLevel.CAUTION -> R.string.risk_caution
    RiskLevel.SAFE -> R.string.risk_safe
}

/** Localized label for the app a message came from. */
@StringRes
fun sourceLabelRes(sourcePackage: String): Int = when {
    sourcePackage.startsWith("com.whatsapp") -> R.string.source_whatsapp
    sourcePackage == "org.telegram.messenger" -> R.string.source_telegram
    sourcePackage in MonitoredApps.SMS_PACKAGES -> R.string.source_sms
    else -> R.string.source_manual
}

/** Advice steps for a risk level, in display order. */
@StringRes
fun RiskLevel.adviceRes(): List<Int> = when (this) {
    RiskLevel.DANGER -> listOf(R.string.advice_danger_1, R.string.advice_danger_2, R.string.advice_danger_3)
    RiskLevel.CAUTION -> listOf(R.string.advice_caution_1, R.string.advice_caution_2)
    RiskLevel.SAFE -> listOf(R.string.advice_safe_1)
}
