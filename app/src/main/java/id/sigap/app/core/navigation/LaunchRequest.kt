package id.sigap.app.core.navigation

import android.net.Uri

/** Something the activity was asked to do by an incoming intent. */
sealed interface LaunchRequest {
    data class OpenAlert(val id: Long) : LaunchRequest
    data class SharedText(val text: String) : LaunchRequest
    data class SharedImage(val uri: Uri) : LaunchRequest
}
