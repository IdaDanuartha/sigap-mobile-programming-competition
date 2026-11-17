package id.sigap.app.core.util

import androidx.annotation.StringRes
import id.sigap.app.R
import id.sigap.app.domain.model.ReasonCode

/** Maps reason codes to localized sentences. */
@StringRes
fun ReasonCode.stringRes(): Int = when (this) {
    ReasonCode.APK_FILE -> R.string.reason_apk_file
    ReasonCode.FILE_LURE -> R.string.reason_file_lure
    ReasonCode.SHORTENED_LINK -> R.string.reason_shortened_link
    ReasonCode.IP_LINK -> R.string.reason_ip_link
    ReasonCode.RISKY_TLD -> R.string.reason_risky_tld
    ReasonCode.LOOKALIKE_DOMAIN -> R.string.reason_lookalike_domain
    ReasonCode.USER_INFO_URL -> R.string.reason_user_info_url
    ReasonCode.PUNYCODE_URL -> R.string.reason_punycode_url
    ReasonCode.LINK_FROM_UNKNOWN -> R.string.reason_link_from_unknown
    ReasonCode.URGENCY -> R.string.reason_urgency
    ReasonCode.CREDENTIAL_REQUEST -> R.string.reason_credential_request
    ReasonCode.OTP_SHARE_REQUEST -> R.string.reason_otp_share_request
    ReasonCode.IMPERSONATION -> R.string.reason_impersonation
    ReasonCode.MONEY_REWARD -> R.string.reason_money_reward
    ReasonCode.SOCIAL_ENGINEERING -> R.string.reason_social_engineering
}
