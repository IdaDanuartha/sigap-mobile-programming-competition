package id.sigap.app.core.util

/** Message apps Sigap watches by default (PRD section 5.1). */
object MonitoredApps {
    val SMS_PACKAGES = setOf(
        "com.google.android.apps.messaging",
        "com.samsung.android.messaging",
        "com.android.mms",
    )
    val DEFAULT_PACKAGES = setOf("com.whatsapp", "com.whatsapp.w4b", "org.telegram.messenger") + SMS_PACKAGES
}
