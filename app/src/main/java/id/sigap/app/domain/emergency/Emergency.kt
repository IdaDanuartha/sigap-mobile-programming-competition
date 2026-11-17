package id.sigap.app.domain.emergency

import id.sigap.app.domain.model.ScamCategory

/** What the user did after receiving a scam message (S07a). */
enum class Situation { OPENED_LINK, INSTALLED_APP, SHARED_CREDENTIAL, TRANSFERRED_MONEY, SHARED_PERSONAL_DATA }

/** A system screen a step can open. */
enum class StepAction { OPEN_APP_LIST, OPEN_ACCESSIBILITY, OPEN_IASC }

/**
 * One checklist step. Enum order is the display priority: containment first, then credentials,
 * then money, then cleanup, then reporting.
 */
enum class EmergencyStep(val action: StepAction? = null) {
    AIRPLANE_MODE,
    DONT_ENTER_DATA,
    CLOSE_PAGE,
    UNINSTALL_APP(StepAction.OPEN_APP_LIST),
    REVIEW_ACCESSIBILITY(StepAction.OPEN_ACCESSIBILITY),
    CHANGE_CREDENTIAL_NOW,
    CONTACT_BANK,
    CHECK_TRANSACTIONS,
    CHANGE_PASSWORDS_CLEAN_DEVICE,
    CLEAR_BROWSER_DATA,
    GATHER_EVIDENCE,
    WATCH_FOLLOWUP,
    NOTIFY_PROVIDERS,
    REPORT_IASC(StepAction.OPEN_IASC),
}

private val STEPS_BY_SITUATION: Map<Situation, Set<EmergencyStep>> = mapOf(
    Situation.OPENED_LINK to setOf(
        EmergencyStep.DONT_ENTER_DATA, EmergencyStep.CLOSE_PAGE, EmergencyStep.CLEAR_BROWSER_DATA,
    ),
    Situation.INSTALLED_APP to setOf(
        EmergencyStep.AIRPLANE_MODE, EmergencyStep.UNINSTALL_APP, EmergencyStep.REVIEW_ACCESSIBILITY,
        EmergencyStep.CHANGE_PASSWORDS_CLEAN_DEVICE, EmergencyStep.CONTACT_BANK,
    ),
    Situation.SHARED_CREDENTIAL to setOf(
        EmergencyStep.CHANGE_CREDENTIAL_NOW, EmergencyStep.CONTACT_BANK, EmergencyStep.CHECK_TRANSACTIONS,
    ),
    Situation.TRANSFERRED_MONEY to setOf(
        EmergencyStep.CONTACT_BANK, EmergencyStep.GATHER_EVIDENCE,
    ),
    Situation.SHARED_PERSONAL_DATA to setOf(
        EmergencyStep.WATCH_FOLLOWUP, EmergencyStep.NOTIFY_PROVIDERS,
    ),
)

/** Builds the ordered, de-duplicated checklist for the chosen situations. Always ends with reporting. */
class BuildChecklistUseCase {
    operator fun invoke(selected: Set<Situation>): List<EmergencyStep> {
        val steps = selected.flatMap { STEPS_BY_SITUATION.getValue(it) }.toSet() + EmergencyStep.REPORT_IASC
        return EmergencyStep.entries.filter { it in steps }
    }
}

/** Picks the situation to pre-select when the flow starts from a scan result. */
fun situationFor(category: ScamCategory?): Situation = when (category) {
    ScamCategory.APK_MALWARE, ScamCategory.FAKE_INVITATION, ScamCategory.FAKE_COURIER -> Situation.INSTALLED_APP
    ScamCategory.OTP_THEFT -> Situation.SHARED_CREDENTIAL
    else -> Situation.OPENED_LINK
}
