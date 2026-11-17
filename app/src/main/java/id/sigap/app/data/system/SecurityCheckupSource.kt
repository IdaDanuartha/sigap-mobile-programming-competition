package id.sigap.app.data.system

import android.app.KeyguardManager
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import android.view.accessibility.AccessibilityManager
import android.accessibilityservice.AccessibilityServiceInfo
import id.sigap.app.core.util.MonitoredApps
import id.sigap.app.core.util.PermissionChecks
import id.sigap.app.domain.checkup.CheckIds
import id.sigap.app.domain.checkup.CheckKind
import id.sigap.app.domain.checkup.CheckupItem
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/** Reads the auto-checked items of the Security checkup from the device (S09). */
class SecurityCheckupSource(private val context: Context) {

    fun autoChecks(): List<CheckupItem> = listOf(
        CheckupItem(CheckIds.LISTENER, CheckKind.AUTO, CheckIds.WEIGHT_LISTENER, PermissionChecks.isNotificationListenerEnabled(context)),
        CheckupItem(CheckIds.SCREEN_LOCK, CheckKind.AUTO, CheckIds.WEIGHT_SCREEN_LOCK, hasScreenLock()),
        CheckupItem(CheckIds.PATCH, CheckKind.AUTO, CheckIds.WEIGHT_PATCH, isPatchRecent()),
        accessibilityCheck(),
    )

    /** Monitored messaging apps that are installed, one self-confirmed row each. */
    fun installedMonitoredApps(): List<String> =
        MonitoredApps.DEFAULT_PACKAGES.filter { isInstalled(it) }.sorted()

    private fun hasScreenLock(): Boolean =
        context.getSystemService(KeyguardManager::class.java)?.isDeviceSecure == true

    private fun isPatchRecent(): Boolean = runCatching {
        val patch = LocalDate.parse(Build.VERSION.SECURITY_PATCH)
        ChronoUnit.MONTHS.between(patch, LocalDate.now()) < MAX_PATCH_AGE_MONTHS
    }.getOrDefault(true)

    private fun accessibilityCheck(): CheckupItem {
        val manager = context.getSystemService(AccessibilityManager::class.java)
        val risky = manager?.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)
            .orEmpty()
            .map { it.resolveInfo.serviceInfo }
            .filter { it.packageName !in TRUSTED_ACCESSIBILITY_PACKAGES && it.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM == 0 }
        return CheckupItem(CheckIds.ACCESSIBILITY, CheckKind.AUTO, CheckIds.WEIGHT_ACCESSIBILITY, risky.isEmpty(), flaggedCount = risky.size)
    }

    private fun isInstalled(packageName: String): Boolean =
        runCatching { context.packageManager.getPackageInfo(packageName, 0) }.isSuccess

    private companion object {
        const val MAX_PATCH_AGE_MONTHS = 12L
        val TRUSTED_ACCESSIBILITY_PACKAGES = setOf(
            "com.google.android.marvin.talkback",
            "com.google.android.accessibility.switchaccess",
        )
    }
}
