package id.sigap.app.data.local

import android.content.Context
import android.net.Uri
import android.provider.ContactsContract
import id.sigap.app.core.util.PermissionChecks
import id.sigap.app.core.util.SenderLabels
import id.sigap.app.domain.model.ContactLookup

/** Looks up sender labels in the device contacts. Requires READ_CONTACTS. */
class AndroidContactLookup(private val context: Context) : ContactLookup {

    override fun hasPermission(): Boolean = PermissionChecks.hasContactsAccess(context)

    override fun isSavedContact(label: String): Boolean = runCatching {
        if (SenderLabels.looksLikePhoneNumber(label)) lookupByNumber(label) else lookupByName(label)
    }.getOrDefault(false)

    private fun lookupByNumber(number: String): Boolean {
        val uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number))
        return context.contentResolver.query(uri, arrayOf(ContactsContract.PhoneLookup._ID), null, null, null)
            ?.use { it.count > 0 } ?: false
    }

    private fun lookupByName(name: String): Boolean =
        context.contentResolver.query(
            ContactsContract.Contacts.CONTENT_URI,
            arrayOf(ContactsContract.Contacts._ID),
            "${ContactsContract.Contacts.DISPLAY_NAME} = ?",
            arrayOf(name),
            null,
        )?.use { it.count > 0 } ?: false
}
