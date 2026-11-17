package id.sigap.app.domain.usecase

import id.sigap.app.core.util.SenderLabels
import id.sigap.app.domain.model.ContactLookup
import id.sigap.app.domain.model.SenderKind
import id.sigap.app.domain.model.TrustedSenderRepository

/** Decides whether a notification sender should be scanned. Returns null to skip. */
class ClassifySenderUseCase(
    private val trusted: TrustedSenderRepository,
    private val contacts: ContactLookup,
    private val smsPackages: Set<String>,
) {
    suspend operator fun invoke(sourcePackage: String, label: String): SenderKind? {
        if (label.isBlank() || trusted.isTrusted(SenderLabels.hash(label))) return null
        return if (contacts.hasPermission()) classifyWithContacts(label) else classifyWithoutContacts(sourcePackage, label)
    }

    private fun classifyWithContacts(label: String): SenderKind? = when {
        contacts.isSavedContact(label) -> null
        SenderLabels.looksLikeShortCode(label) -> SenderKind.SHORTCODE
        else -> SenderKind.UNKNOWN
    }

    private fun classifyWithoutContacts(sourcePackage: String, label: String): SenderKind? = when {
        SenderLabels.looksLikePhoneNumber(label) -> SenderKind.UNKNOWN
        sourcePackage in smsPackages && SenderLabels.looksLikeShortCode(label) -> SenderKind.SHORTCODE
        else -> null
    }
}
