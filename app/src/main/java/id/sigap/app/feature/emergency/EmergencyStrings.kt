package id.sigap.app.feature.emergency

import androidx.annotation.StringRes
import id.sigap.app.R
import id.sigap.app.domain.emergency.EmergencyStep
import id.sigap.app.domain.emergency.Evidence
import id.sigap.app.domain.emergency.Situation
import id.sigap.app.domain.emergency.StepAction

@StringRes
fun Situation.labelRes(): Int = when (this) {
    Situation.OPENED_LINK -> R.string.situation_link
    Situation.INSTALLED_APP -> R.string.situation_app
    Situation.SHARED_CREDENTIAL -> R.string.situation_credential
    Situation.TRANSFERRED_MONEY -> R.string.situation_money
    Situation.SHARED_PERSONAL_DATA -> R.string.situation_data
}

@StringRes
fun EmergencyStep.textRes(): Int = when (this) {
    EmergencyStep.AIRPLANE_MODE -> R.string.step_airplane
    EmergencyStep.DONT_ENTER_DATA -> R.string.step_dont_enter
    EmergencyStep.CLOSE_PAGE -> R.string.step_close_page
    EmergencyStep.UNINSTALL_APP -> R.string.step_uninstall
    EmergencyStep.REVIEW_ACCESSIBILITY -> R.string.step_accessibility
    EmergencyStep.CHANGE_CREDENTIAL_NOW -> R.string.step_change_cred_now
    EmergencyStep.CONTACT_BANK -> R.string.step_contact_bank
    EmergencyStep.CHECK_TRANSACTIONS -> R.string.step_check_tx
    EmergencyStep.CHANGE_PASSWORDS_CLEAN_DEVICE -> R.string.step_change_clean
    EmergencyStep.CLEAR_BROWSER_DATA -> R.string.step_clear_browser
    EmergencyStep.GATHER_EVIDENCE -> R.string.step_gather_evidence
    EmergencyStep.WATCH_FOLLOWUP -> R.string.step_watch_followup
    EmergencyStep.NOTIFY_PROVIDERS -> R.string.step_notify_providers
    EmergencyStep.REPORT_IASC -> R.string.step_report_iasc
}

@StringRes
fun StepAction.labelRes(): Int = when (this) {
    StepAction.OPEN_APP_LIST -> R.string.step_action_app_list
    StepAction.OPEN_ACCESSIBILITY -> R.string.step_action_accessibility
    StepAction.OPEN_IASC -> R.string.step_action_draft
}

@StringRes
fun Evidence.labelRes(): Int = when (this) {
    Evidence.SCREENSHOTS -> R.string.evidence_screenshots
    Evidence.TRANSFER_RECEIPT -> R.string.evidence_receipt
    Evidence.CHAT_EXPORT -> R.string.evidence_chat
}
