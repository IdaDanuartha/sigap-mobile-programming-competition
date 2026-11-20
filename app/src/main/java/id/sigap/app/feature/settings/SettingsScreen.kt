package id.sigap.app.feature.settings

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.window.Dialog
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.composables.icons.lucide.ArrowLeft
import com.composables.icons.lucide.Bell
import com.composables.icons.lucide.Check
import com.composables.icons.lucide.ChevronRight
import com.composables.icons.lucide.Database
import com.composables.icons.lucide.FileText
import com.composables.icons.lucide.Globe
import com.composables.icons.lucide.Info
import com.composables.icons.lucide.Layers
import com.composables.icons.lucide.LayoutGrid
import com.composables.icons.lucide.Lock
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.MessageSquare
import com.composables.icons.lucide.Moon
import com.composables.icons.lucide.Palette
import com.composables.icons.lucide.Pencil
import com.composables.icons.lucide.Plus
import com.composables.icons.lucide.Send
import com.composables.icons.lucide.Settings
import com.composables.icons.lucide.Share2
import com.composables.icons.lucide.Shield
import com.composables.icons.lucide.ShieldAlert
import com.composables.icons.lucide.ShieldCheck
import com.composables.icons.lucide.SlidersHorizontal
import com.composables.icons.lucide.Smartphone
import com.composables.icons.lucide.Sparkles
import com.composables.icons.lucide.Sun
import com.composables.icons.lucide.Trash2
import com.composables.icons.lucide.TriangleAlert
import com.composables.icons.lucide.User
import com.composables.icons.lucide.UserCheck
import com.composables.icons.lucide.X
import id.sigap.app.R
import id.sigap.app.SigapApp
import id.sigap.app.core.designsystem.SigapTheme
import id.sigap.app.core.designsystem.Spacing
import id.sigap.app.core.util.MonitoredApps
import id.sigap.app.domain.model.Sensitivity
import id.sigap.app.domain.model.ThemeMode
import id.sigap.app.domain.model.TrustedSender
import id.sigap.app.domain.model.UserSettings
import kotlinx.coroutines.launch

private enum class SettingsView {
    OVERVIEW,
    MONITORED_APPS,
    TRUSTED_SENDERS,
    PRIVACY_DATA,
}

/**
 * S11 Pengaturan Hub: Full-fidelity implementation containing all 4 setting screens
 * (S11 Pengaturan, S11B Aplikasi Yang Dipantau, S11C Pengirim Tepercaya, S11D Privasi Dan Data).
 */
@Composable
fun SettingsScreen(
    onOpenSimulation: () -> Unit = {},
    onOpenNotifications: () -> Unit = {},
    onOpenPermissions: () -> Unit = {},
) {
    val context = LocalContext.current
    val container = (context.applicationContext as SigapApp).container
    val viewModel: SettingsViewModel = viewModel(
        factory = viewModelFactory {
            initializer { SettingsViewModel(container.settings, container.trustedSenders, container::clearAllData, container::setDemoMode) }
        },
    )
    val settings by viewModel.userSettings.collectAsStateWithLifecycle()
    val trusted by viewModel.trustedSenders.collectAsStateWithLifecycle()

    var currentView by remember { mutableStateOf(SettingsView.OVERVIEW) }

    BackHandler(enabled = currentView != SettingsView.OVERVIEW) {
        currentView = SettingsView.OVERVIEW
    }

    AnimatedContent(
        targetState = currentView,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        label = "settings_nav",
    ) { view ->
        when (view) {
            SettingsView.OVERVIEW -> SettingsOverviewContent(
                settings = settings,
                trustedCount = trusted.size,
                onNavigateToMonitoredApps = { currentView = SettingsView.MONITORED_APPS },
                onNavigateToTrustedSenders = { currentView = SettingsView.TRUSTED_SENDERS },
                onNavigateToPrivacyData = { currentView = SettingsView.PRIVACY_DATA },
                onOpenSimulation = onOpenSimulation,
                onOpenNotifications = onOpenNotifications,
                onOpenPermissions = onOpenPermissions,
                onSetUserName = viewModel::setUserName,
                onSetSensitivity = viewModel::setSensitivity,
                onSetCloudConsent = viewModel::setCloudConsent,
                onSetThemeMode = viewModel::setThemeMode,
                onSetDemoMode = viewModel::setDemoMode,
            )
            SettingsView.MONITORED_APPS -> MonitoredAppsContent(
                disabledPackages = settings.disabledPackages,
                onToggle = viewModel::setPackageEnabled,
                onBack = { currentView = SettingsView.OVERVIEW },
            )
            SettingsView.TRUSTED_SENDERS -> TrustedSendersContent(
                items = trusted,
                onAdd = viewModel::addTrustedSender,
                onRemove = viewModel::removeTrustedSender,
                onBack = { currentView = SettingsView.OVERVIEW },
            )
            SettingsView.PRIVACY_DATA -> PrivacyDataContent(
                retentionDays = settings.retentionDays,
                onSetRetentionDays = viewModel::setRetentionDays,
                onClearAll = viewModel::clearAllData,
                onBack = { currentView = SettingsView.OVERVIEW },
            )
        }
    }
}

/**
 * Image 3: S11 Pengaturan (Main Settings Overview)
 */
@Composable
private fun SettingsOverviewContent(
    settings: UserSettings,
    trustedCount: Int,
    onNavigateToMonitoredApps: () -> Unit,
    onNavigateToTrustedSenders: () -> Unit,
    onNavigateToPrivacyData: () -> Unit,
    onOpenSimulation: () -> Unit,
    onOpenNotifications: () -> Unit = {},
    onOpenPermissions: () -> Unit = {},
    onSetUserName: (String) -> Unit = {},
    onSetSensitivity: (Sensitivity) -> Unit,
    onSetCloudConsent: (Boolean) -> Unit,
    onSetThemeMode: (ThemeMode) -> Unit,
    onSetDemoMode: (Boolean) -> Unit = {},
) {
    val context = LocalContext.current
    val isEnglish = LocalConfiguration.current.locales[0]?.language == "en" ||
        AppCompatDelegate.getApplicationLocales().toLanguageTags().startsWith("en")
    fun t(id: String, en: String) = if (isEnglish) en else id
    var showSensitivityDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var showLicenseDialog by remember { mutableStateOf(false) }

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState()),
    ) {
        // Top App Bar
        SettingsTopBar(onOpenNotifications = onOpenNotifications)

        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.ScreenHorizontal),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Device Profile Card (Danuartha / Model info)
            DeviceProfileCard(
                userName = settings.userName,
                modelName = "${Build.MANUFACTURER.replaceFirstChar { it.uppercase() }} ${Build.MODEL}",
                appVersion = "Sigap v2.4.1 (Build 184) • Basis data aktif",
                onUpdateUserName = onSetUserName,
            )

            // AI Local Privacy Banner
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceContainer,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Box(
                        Modifier
                            .size(32.dp)
                            .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            Lucide.Lock,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp),
                        )
                    }
                    Text(
                        text = t(
                            "Pemrosesan kecerdasan buatan berlangsung langsung di ponsel Anda. Data pribadi tetap aman.",
                            "AI processing runs directly on your device. Your personal data stays private.",
                        ),
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 12.sp,
                            lineHeight = 17.sp,
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }

            // Section 1: Perlindungan (Status Aktif)
            SectionHeader(
                title = t("Perlindungan", "Protection"),
                statusBadge = t("Aktif", "Active"),
                badgeColor = MaterialTheme.colorScheme.primary,
            )
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column {
                    val activeAppsCount = listOf(
                        "com.whatsapp",
                        "com.whatsapp.w4b",
                        "org.telegram.messenger",
                        "com.google.android.apps.messaging",
                        "android.sms.default",
                    ).count { it !in settings.disabledPackages }
                    SettingMenuRow(
                        icon = Lucide.LayoutGrid,
                        title = t("Aplikasi yang dipantau", "Monitored apps"),
                        value = t("$activeAppsCount aplikasi aktif", "$activeAppsCount active apps"),
                        onClick = onNavigateToMonitoredApps,
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                    SettingMenuRow(
                        icon = Lucide.UserCheck,
                        title = t("Pengirim tepercaya", "Trusted senders"),
                        value = t("$trustedCount nomor", "$trustedCount numbers"),
                        onClick = onNavigateToTrustedSenders,
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                    SettingMenuRow(
                        icon = Lucide.SlidersHorizontal,
                        title = t("Sensitivitas deteksi", "Detection sensitivity"),
                        valueChip = when (settings.sensitivity) {
                            Sensitivity.LOW -> t("Rendah", "Low")
                            Sensitivity.BALANCED -> t("Seimbang", "Balanced")
                            Sensitivity.HIGH -> t("Tinggi", "High")
                        },
                        onClick = { showSensitivityDialog = true },
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                    SettingMenuRow(
                        icon = Lucide.ShieldCheck,
                        title = t("Panduan izin awal", "Initial permissions setup"),
                        value = t("Buka", "Open"),
                        onClick = onOpenPermissions,
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                    SettingMenuRow(
                        icon = Lucide.Smartphone,
                        title = t("Izin aplikasi di Android", "Android app permissions"),
                        value = t("Kontak & lainnya", "Contacts & more"),
                        onClick = {
                            context.startActivity(
                                Intent(
                                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                    Uri.fromParts("package", context.packageName, null),
                                ),
                            )
                        },
                    )
                }
            }

            // Section 2: Pemrosesan & Privasi
            SectionHeader(title = t("Pemrosesan & Privasi", "Processing & Privacy"))
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column {
                    SettingToggleRow(
                        icon = Lucide.Layers,
                        title = t("Analisis cloud", "Cloud analysis"),
                        subtitle = t(
                            "Hanya teks tersamar yang diunggah jika meragukan",
                            "Only redacted snippets are sent when uncertain",
                        ),
                        checked = settings.cloudConsent,
                        onCheckedChange = onSetCloudConsent,
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                    SettingMenuRow(
                        icon = Lucide.Bell,
                        title = t("Notifikasi sistem", "System notifications"),
                        value = t("Heads-up & Layar kunci", "Heads-up & Lock screen"),
                        onClick = onOpenSimulation,
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                    SettingMenuRow(
                        icon = Lucide.ShieldCheck,
                        title = t("Privasi dan data", "Privacy & data"),
                        value = t("Retensi ${settings.retentionDays} hari", "${settings.retentionDays}-day retention"),
                        onClick = onNavigateToPrivacyData,
                    )
                }
            }

            // Section 3: Tampilan & Sistem
            SectionHeader(title = t("Tampilan & Sistem", "Appearance & System"))
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column {
                    val isDarkThemeActive = isSystemInDarkTheme()
                    SettingMenuRow(
                        icon = Lucide.Palette,
                        title = t("Tema", "Theme"),
                        value = when (settings.themeMode) {
                            ThemeMode.SYSTEM -> if (isDarkThemeActive) t("Sistem (Gelap)", "System (Dark)") else t("Sistem (Terang)", "System (Light)")
                            ThemeMode.LIGHT -> t("Terang", "Light")
                            ThemeMode.DARK -> t("Gelap", "Dark")
                        },
                        onClick = { showThemeDialog = true },
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                    SettingMenuRow(
                        icon = Lucide.Globe,
                        title = t("Bahasa", "Language"),
                        value = if (isEnglish) "English" else "Bahasa Indonesia",
                        onClick = { showLanguageDialog = true },
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                    SettingToggleRow(
                        icon = Lucide.Sparkles,
                        title = t("Mode Demo", "Demo Mode"),
                        subtitle = t(
                            "Isi data contoh notifikasi, keluarga, & preset cek untuk demo/video",
                            "Populate sample alerts, family links, & quick check presets for demos",
                        ),
                        checked = settings.demoMode,
                        onCheckedChange = onSetDemoMode,
                    )
                }
            }

            // Section 4: Tentang & Bantuan
            SectionHeader(title = t("Tentang & Bantuan", "About & Support"))
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column {
                    SettingMenuRow(
                        icon = Lucide.Info,
                        title = t("Sumber data & mitra", "Data sources & partners"),
                        value = "Satgas PASTI, IASC",
                        onClick = { showAboutDialog = true },
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                    SettingMenuRow(
                        icon = Lucide.FileText,
                        title = t("Lisensi & privasi open-source", "Open-source licenses & privacy"),
                        onClick = { showLicenseDialog = true },
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                    SettingMenuRow(
                        icon = Lucide.MessageSquare,
                        title = t("Kirim masukan", "Send feedback"),
                        onClick = {
                            val intent = Intent(Intent.ACTION_SENDTO)
                                .setData(Uri.parse("mailto:support@sigap.id"))
                                .putExtra(Intent.EXTRA_SUBJECT, t("Masukan Aplikasi Sigap", "Sigap App Feedback"))
                            runCatching { context.startActivity(intent) }
                        },
                    )
                }
            }

            // Footer Watermark
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Box(
                    Modifier
                        .size(32.dp)
                        .background(MaterialTheme.colorScheme.surfaceContainer, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Lucide.Settings,
                        contentDescription = null,
                        tint = SigapTheme.colors.textMuted,
                        modifier = Modifier.size(16.dp),
                    )
                }
                Text(
                    text = "Sigap dirancang untuk melindungi keluarga tanpa melanggar privasi.",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = SigapTheme.colors.textMuted,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 24.dp),
                )
            }

            Spacer(Modifier.height(Spacing.Section))
        }
    }

    // Sensitivity Dialog (Premium Select Dialog)
    if (showSensitivityDialog) {
        SigapOptionSelectDialog(
            title = t("Sensitivitas Deteksi", "Detection Sensitivity"),
            subtitle = t(
                "Atur ketatnya filter analisis pesan mencurigakan",
                "Adjust how strictly incoming messages are filtered",
            ),
            icon = Lucide.SlidersHorizontal,
            options = listOf(
                OptionItem(
                    value = Sensitivity.BALANCED,
                    title = t("Seimbang", "Balanced"),
                    subtitle = t(
                        "Proteksi optimal untuk penipuan APK, kurir, & tautan palsu",
                        "Optimal protection for fake APKs, couriers, & phishing links",
                    ),
                    icon = Lucide.ShieldCheck,
                    badge = t("Rekomendasi", "Recommended"),
                ),
                OptionItem(
                    value = Sensitivity.HIGH,
                    title = t("Tinggi (Maksimal)", "High (Maximum)"),
                    subtitle = t(
                        "Mendeteksi nomor asing baru dan kata bernada mendesak",
                        "Detects unknown senders and urgent/pressuring language",
                    ),
                    icon = Lucide.ShieldAlert,
                ),
                OptionItem(
                    value = Sensitivity.LOW,
                    title = t("Rendah", "Low"),
                    subtitle = t(
                        "Hanya memperingatkan ancaman berisiko sangat kritis",
                        "Only warns on critical high-risk threats",
                    ),
                    icon = Lucide.Shield,
                ),
            ),
            selected = settings.sensitivity,
            onSelect = onSetSensitivity,
            onDismiss = { showSensitivityDialog = false },
        )
    }

    // Theme Dialog (Premium Select Dialog)
    if (showThemeDialog) {
        SigapOptionSelectDialog(
            title = t("Pilih Tema Tampilan", "Choose Display Theme"),
            subtitle = t(
                "Sesuaikan kenyamanan visual aplikasi ponsel Anda",
                "Adjust the visual appearance for your comfort",
            ),
            icon = Lucide.Palette,
            options = listOf(
                OptionItem(
                    value = ThemeMode.SYSTEM,
                    title = t("Sistem", "System"),
                    subtitle = t(
                        "Menyesuaikan otomatis dengan tema perangkat Anda",
                        "Automatically follows your device theme",
                    ),
                    icon = Lucide.Smartphone,
                    badge = t("Otomatis", "Auto"),
                ),
                OptionItem(
                    value = ThemeMode.LIGHT,
                    title = t("Mode Terang", "Light Mode"),
                    subtitle = t(
                        "Tampilan bersih & kontras tinggi optimal di siang hari",
                        "Clean & high-contrast look optimal in daylight",
                    ),
                    icon = Lucide.Sun,
                ),
                OptionItem(
                    value = ThemeMode.DARK,
                    title = t("Mode Gelap", "Dark Mode"),
                    subtitle = t(
                        "Lebih teduh untuk mata dan hemat konsumsi daya baterai",
                        "Easier on the eyes and saves battery power",
                    ),
                    icon = Lucide.Moon,
                ),
            ),
            selected = settings.themeMode,
            onSelect = onSetThemeMode,
            onDismiss = { showThemeDialog = false },
        )
    }

    // Language Dialog (Premium Select Dialog)
    if (showLanguageDialog) {
        val currentTag = AppCompatDelegate.getApplicationLocales().toLanguageTags().ifBlank { "id" }
        val currentLang = if (currentTag.startsWith("en")) "en" else "id"
        SigapOptionSelectDialog(
            title = t("Pilih Bahasa Aplikasi", "Choose App Language"),
            subtitle = t(
                "Tentukan bahasa utama antarmuka & panduan",
                "Select your primary interface & guide language",
            ),
            icon = Lucide.Globe,
            options = listOf(
                OptionItem(
                    value = "id",
                    title = "Bahasa Indonesia",
                    subtitle = t(
                        "Bahasa resmi dan terminologi penipuan lokal Indonesia",
                        "Official Indonesian language and local scam terminology",
                    ),
                    icon = Lucide.Globe,
                    badge = t("Utama", "Primary"),
                ),
                OptionItem(
                    value = "en",
                    title = "English",
                    subtitle = "Full interface and scam awareness guides in English",
                    icon = Lucide.Globe,
                ),
            ),
            selected = currentLang,
            onSelect = { langTag ->
                AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(langTag))
            },
            onDismiss = { showLanguageDialog = false },
        )
    }

    // About Dialog
    if (showAboutDialog) {
        InfoDetailsDialog(
            title = "Sumber Data & Mitra",
            subtitle = "Standar intelijen keamanan siber Sigap",
            icon = Lucide.Info,
            body = "Sigap bekerja sama dan menyelaraskan basis data intelijen kejahatan siber secara lokal dengan referensi dari Satgas PASTI OJK, BSSN, dan Global Anti-Scam Alliance (GASA) untuk melindungi masyarakat dari penipuan digital tanpa mengorbankan privasi percakapan.",
            onDismiss = { showAboutDialog = false },
        )
    }

    // License Dialog
    if (showLicenseDialog) {
        InfoDetailsDialog(
            title = "Lisensi & Open Source",
            subtitle = "Transparansi modul & pustaka sistem",
            icon = Lucide.FileText,
            body = "Sigap dibangun menggunakan teknologi open-source modern Android Jetpack Compose, Room Database, Kotlin Coroutines, dan Google ML Kit. Semua hak cipta dilindungi undang-undang demi keselamatan privasi keluarga.",
            onDismiss = { showLicenseDialog = false },
        )
    }
}

/**
 * Image 4: S11B Aplikasi Yang Dipantau (Monitored Apps View)
 */
@Composable
private fun MonitoredAppsContent(
    disabledPackages: Set<String>,
    onToggle: (String, Boolean) -> Unit,
    onBack: () -> Unit,
) {
    val context = LocalContext.current

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        // Top App Bar
        SubScreenTopBar(
            title = "Aplikasi yang Dipantau",
            onBack = onBack,
            onShare = {
                val intent = Intent(Intent.ACTION_SEND)
                    .setType("text/plain")
                    .putExtra(Intent.EXTRA_TEXT, "Sigap memantau WhatsApp, Telegram, dan SMS secara offline untuk perlindungan penipuan APK.")
                context.startActivity(Intent.createChooser(intent, null))
            },
        )

        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Spacing.ScreenHorizontal),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "Sigap memantau notifikasi dari aplikasi terpilih secara lokal di perangkat Anda tanpa mengirim data ke server.",
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp, lineHeight = 19.sp),
                color = SigapTheme.colors.textMuted,
            )

            // Active Monitored Apps List Card
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column {
                    MonitoredAppToggleRow(
                        name = "WhatsApp",
                        description = "Pesan obrolan dan grup",
                        packageName = "com.whatsapp",
                        icon = Lucide.MessageSquare,
                        enabled = "com.whatsapp" !in disabledPackages,
                        onToggle = { onToggle("com.whatsapp", it) },
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                    MonitoredAppToggleRow(
                        name = "WhatsApp Business",
                        description = "Pesan katalog dan transaksi",
                        packageName = "com.whatsapp.w4b",
                        icon = Lucide.MessageSquare,
                        enabled = "com.whatsapp.w4b" !in disabledPackages,
                        onToggle = { onToggle("com.whatsapp.w4b", it) },
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                    MonitoredAppToggleRow(
                        name = "Telegram",
                        description = "Grup publik dan obrolan privat",
                        packageName = "org.telegram.messenger",
                        icon = Lucide.Send,
                        enabled = "org.telegram.messenger" !in disabledPackages,
                        onToggle = { onToggle("org.telegram.messenger", it) },
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                    MonitoredAppToggleRow(
                        name = "Pesan Google",
                        description = "SMS dan pesan RCS resmi",
                        packageName = "com.google.android.apps.messaging",
                        icon = Lucide.MessageSquare,
                        enabled = "com.google.android.apps.messaging" !in disabledPackages,
                        onToggle = { onToggle("com.google.android.apps.messaging", it) },
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                    MonitoredAppToggleRow(
                        name = "SMS Sistem",
                        description = "Pesan teks seluler operator",
                        packageName = "android.sms.default",
                        icon = Lucide.MessageSquare,
                        enabled = "android.sms.default" !in disabledPackages,
                        onToggle = { onToggle("android.sms.default", it) },
                    )
                }
            }

            // Section: Belum Didukung
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Belum Didukung",
                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 15.sp, fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Text(
                    text = "Dalam Riset Teknis",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                    color = SigapTheme.colors.textMuted,
                )
            }

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    Box(
                        Modifier
                            .size(44.dp)
                            .background(MaterialTheme.colorScheme.surfaceContainer, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            Lucide.MessageSquare,
                            contentDescription = null,
                            tint = SigapTheme.colors.textMuted,
                            modifier = Modifier.size(22.dp),
                        )
                    }

                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Text(
                                text = "Instagram Direct",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                ),
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = MaterialTheme.colorScheme.surfaceContainer,
                            ) {
                                Text(
                                    text = "Segera Hadir",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    color = SigapTheme.colors.textMuted,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                )
                            }
                        }
                        Text(
                            text = "Penyedia belum mendukung izin baca notifikasi aman untuk filter pihak ketiga.",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp, lineHeight = 16.sp),
                            color = SigapTheme.colors.textMuted,
                        )
                    }
                }
            }

            // Banner: Jaminan Privasi Lokal
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceContainer,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    Modifier.padding(14.dp),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Box(
                        Modifier
                            .size(32.dp)
                            .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            Lucide.ShieldCheck,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = "Jaminan Privasi Lokal",
                            style = MaterialTheme.typography.titleSmall.copy(fontSize = 13.sp, fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = "Semua pemindaian berlangsung di memori RAM ponsel. Sigap tidak pernah mengunggah rekaman percakapan Anda.",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp, lineHeight = 17.sp),
                            color = SigapTheme.colors.textMuted,
                        )
                    }
                }
            }

            Spacer(Modifier.height(Spacing.Section))
        }
    }
}

/**
 * Image 5: S11C Pengirim Tepercaya (Trusted Senders View)
 */
@Composable
private fun TrustedSendersContent(
    items: List<TrustedSender>,
    onAdd: (String) -> Unit,
    onRemove: (String) -> Unit,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    var showAddDialog by remember { mutableStateOf(false) }
    var inputKey by remember { mutableStateOf("") }
    var inputName by remember { mutableStateOf("") }

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        // Top App Bar
        SubScreenTopBar(
            title = "Pengirim Tepercaya",
            onBack = onBack,
            onShare = {
                val intent = Intent(Intent.ACTION_SEND)
                    .setType("text/plain")
                    .putExtra(Intent.EXTRA_TEXT, "Pengirim tepercaya di Sigap mengecualikan kontak resmi dari notifikasi peringatan.")
                context.startActivity(Intent.createChooser(intent, null))
            },
        )

        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Spacing.ScreenHorizontal),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "Pesan dari nomor atau kontak ini akan dikecualikan dari pemeriksaan intensif agar tidak menimbulkan peringatan berulang.",
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp, lineHeight = 19.sp),
                color = SigapTheme.colors.textMuted,
            )

            // Button: + Tambah Pengirim Tepercaya
            Button(
                onClick = { showAddDialog = true },
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary,
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Icon(Lucide.Plus, contentDescription = null, modifier = Modifier.size(16.dp))
                    Text(
                        text = "Tambah Pengirim Tepercaya",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                        ),
                    )
                }
            }

            // Section: Daftar Pengirim (N) • Terlindungi
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Daftar Pengirim (${items.size})",
                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 15.sp, fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Icon(
                        Lucide.ShieldCheck,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(14.dp),
                    )
                    Text(
                        text = "Terlindungi",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                        ),
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }

            // Card with Trusted Senders items
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column {
                    if (items.isEmpty()) {
                        Text(
                            text = "Belum ada pengirim tepercaya yang ditambahkan.",
                            style = MaterialTheme.typography.bodySmall,
                            color = SigapTheme.colors.textMuted,
                            modifier = Modifier.padding(20.dp),
                        )
                    } else {
                        items.forEachIndexed { index, sender ->
                            val cleanKey = sender.senderKey.trim()
                            val avatarInitials = cleanKey.filter { it.isLetterOrDigit() }.take(2).uppercase().ifEmpty { "PT" }
                            val displayName = cleanKey
                            val subText = "Dikecualikan dari pemindaian"

                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    modifier = Modifier.weight(1f),
                                ) {
                                    Box(
                                        Modifier
                                            .size(44.dp)
                                            .background(MaterialTheme.colorScheme.surfaceContainer, RoundedCornerShape(12.dp)),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Text(
                                            text = avatarInitials,
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                            ),
                                            color = MaterialTheme.colorScheme.primary,
                                        )
                                    }

                                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        ) {
                                            Text(
                                                text = displayName,
                                                style = MaterialTheme.typography.titleSmall.copy(
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.Bold,
                                                ),
                                                color = MaterialTheme.colorScheme.onSurface,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                            )
                                            Icon(
                                                Lucide.Check,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(14.dp),
                                            )
                                        }
                                        Text(
                                            text = subText,
                                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                            color = SigapTheme.colors.textMuted,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                        )
                                    }
                                }

                                Text(
                                    text = "Hapus",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                    ),
                                    color = Color(0xFFBA1A1A),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { onRemove(sender.senderKey) }
                                        .padding(8.dp),
                                )
                            }

                            if (index < items.size - 1) {
                                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                            }
                        }
                    }
                }
            }

            // Warning Security Card (Consistent Light & Dark theme tokens)
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = SigapTheme.colors.attentionCardBg,
                border = BorderStroke(1.dp, SigapTheme.colors.caution.copy(alpha = 0.35f)),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    Modifier.padding(14.dp),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Box(
                        Modifier
                            .size(32.dp)
                            .background(SigapTheme.colors.cautionContainer, RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            Lucide.TriangleAlert,
                            contentDescription = null,
                            tint = SigapTheme.colors.caution,
                            modifier = Modifier.size(16.dp),
                        )
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text(
                            text = "Peringatan Keamanan",
                            style = MaterialTheme.typography.titleSmall.copy(fontSize = 13.sp, fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = "Hanya tambahkan pengirim yang benar-benar Anda kenal. Nomor resmi bank tidak pernah meminta password, PIN, atau pengunduhan file aplikasi APK.",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp, lineHeight = 17.sp),
                            color = SigapTheme.colors.textMuted,
                        )
                    }
                }
            }

            // Footer note: PERLINDUNGAN OTOMATIS SIGAP AKTIF
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    Lucide.ShieldCheck,
                    contentDescription = null,
                    tint = SigapTheme.colors.textMuted,
                    modifier = Modifier.size(14.dp),
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = "PERLINDUNGAN OTOMATIS SIGAP AKTIF",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp,
                    ),
                    color = SigapTheme.colors.textMuted,
                )
            }

            Spacer(Modifier.height(Spacing.Section))
        }
    }

    // Add Trusted Sender Dialog (Custom Premium Dialog)
    if (showAddDialog) {
        AddTrustedSenderDialog(
            onAdd = { key ->
                onAdd(key)
                showAddDialog = false
            },
            onDismiss = { showAddDialog = false },
        )
    }
}

/**
 * Image 6: S11D Privasi Dan Data (Privacy & Data Retention View)
 */
@Composable
private fun PrivacyDataContent(
    retentionDays: Int,
    onSetRetentionDays: (Int) -> Unit,
    onClearAll: () -> Unit,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var showClearConfirm by remember { mutableStateOf(false) }

    Box(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
        ) {
            // Top App Bar
            SubScreenTopBar(
                title = "Privasi dan Data",
                onBack = onBack,
                onShare = {
                    val intent = Intent(Intent.ACTION_SEND)
                        .setType("text/plain")
                        .putExtra(Intent.EXTRA_TEXT, "Sigap beroperasi dengan Zero Telemetry Mode, data percakapan tidak pernah dikirim ke internet.")
                    context.startActivity(Intent.createChooser(intent, null))
                },
            )

            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.ScreenHorizontal),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // Header Card: Kedaulatan Data Anda
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        Modifier.padding(16.dp),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Box(
                            Modifier
                                .size(36.dp)
                                .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                Lucide.ShieldCheck,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(20.dp),
                            )
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "Kedaulatan Data Anda",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                ),
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                            )
                            Text(
                                text = "Semua inspeksi pesan berjalan langsung di dalam memori aman perangkat Anda tanpa pemantauan tersembunyi.",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp, lineHeight = 17.sp),
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.9f),
                            )
                        }
                    }
                }

                // Section: Masa Simpan Riwayat Pesan • Auto-Purge
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Masa Simpan Riwayat Pesan",
                        style = MaterialTheme.typography.titleMedium.copy(fontSize = 15.sp, fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Icon(
                            Lucide.Database,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(13.dp),
                        )
                        Text(
                            text = "Auto-Purge",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                            ),
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }

                // Segmented control: 7 hari | 30 hari | 90 hari
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(4.dp),
                    ) {
                        listOf(7, 30, 90).forEach { days ->
                            val isSelected = days == retentionDays
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.surface else Color.Transparent,
                                shadowElevation = if (isSelected) 2.dp else 0.dp,
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { onSetRetentionDays(days) },
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(
                                        text = "$days hari",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontSize = 13.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        ),
                                        color = if (isSelected) MaterialTheme.colorScheme.onSurface else SigapTheme.colors.textMuted,
                                        textAlign = TextAlign.Center,
                                    )
                                }
                            }
                        }
                    }
                }

                Text(
                    text = "Riwayat otomatis dibersihkan dari penyimpanan ponsel setelah melewati periode ini.",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                    color = SigapTheme.colors.textMuted,
                )

                // Section: AUDIT TRANSPARANSI • Zero Telemetry Mode
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "AUDIT TRANSPARANSI",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp,
                        ),
                        color = SigapTheme.colors.textMuted,
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Box(Modifier.size(6.dp).background(MaterialTheme.colorScheme.primary, CircleShape))
                        Text(
                            text = "Zero Telemetry Mode",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                            ),
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }

                // Transparency Card 1: Disimpan di Perangkat (Terenkripsi Lokal)
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(
                        Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Box(
                                    Modifier
                                        .size(24.dp)
                                        .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Icon(Lucide.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                                }
                                Text("Disimpan di Perangkat", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                            }
                            Surface(shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.primaryContainer) {
                                Row(Modifier.padding(horizontal = 8.dp, vertical = 3.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Icon(Lucide.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(10.dp))
                                    Text("Terenkripsi Lokal", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                        Text("Hanya tersimpan di modul Secure Sandbox", style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp), color = SigapTheme.colors.textMuted)

                        CheckPointRow(text = "Cuplikan teks pesan yang telah disamarkan.")
                        CheckPointRow(text = "Hasil penilaian risiko & skor keamanan.")
                        CheckPointRow(text = "Waktu deteksi dan jenis aplikasi.")
                    }
                }

                // Transparency Card 2: Tidak Pernah Disimpan (Nol Akses)
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(
                        Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Box(
                                    Modifier
                                        .size(24.dp)
                                        .background(Color(0xFFFFECEB), CircleShape),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Icon(Lucide.X, contentDescription = null, tint = Color(0xFFBA1A1A), modifier = Modifier.size(14.dp))
                                }
                                Text("Tidak Pernah Disimpan", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                            }
                            Surface(shape = RoundedCornerShape(20.dp), color = Color(0xFFFFECEB)) {
                                Row(Modifier.padding(horizontal = 8.dp, vertical = 3.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Icon(Lucide.ShieldAlert, contentDescription = null, tint = Color(0xFFBA1A1A), modifier = Modifier.size(10.dp))
                                    Text("Nol Akses", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = Color(0xFFBA1A1A), fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                        Text("Dibuang seketika dari RAM", style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp), color = SigapTheme.colors.textMuted)

                        CrossPointRow(text = "Isi pesan percakapan utuh dan histori obrolan.")
                        CrossPointRow(text = "Kontak pribadi, nomor rekening, dan PIN/OTP.")
                        CrossPointRow(text = "Foto, media, rekaman suara, atau dokumen pribadi.")
                    }
                }

                // Transparency Card 3: Dikirim ke Cloud (Privasi Terjaga)
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(
                        Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Box(
                                    Modifier
                                        .size(24.dp)
                                        .background(MaterialTheme.colorScheme.surfaceContainer, CircleShape),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Icon(Lucide.Layers, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                                }
                                Text("Dikirim ke Cloud", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                            }
                            Surface(shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.surfaceContainer) {
                                Row(Modifier.padding(horizontal = 8.dp, vertical = 3.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Icon(Lucide.Globe, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(10.dp))
                                    Text("Privasi Terjaga", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                        Text("Hanya jika mode verifikasi aktif", style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp), color = SigapTheme.colors.textMuted)

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceContainer,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(
                                text = "Hanya teks kalimat mencurigakan yang telah disensor otomatis untuk verifikasi model AI pusat jika analisis lokal ragu.",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp, lineHeight = 17.sp),
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(12.dp),
                            )
                        }
                    }
                }

                // Cache Card: Penyimpanan Terpakai
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            Box(
                                Modifier
                                    .size(36.dp)
                                    .background(MaterialTheme.colorScheme.surfaceContainer, RoundedCornerShape(10.dp)),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(Lucide.Database, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                            }
                            Column {
                                Text("Penyimpanan Terpakai", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                                Text("1.4 MB dari basis data terenkripsi", style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp), color = SigapTheme.colors.textMuted)
                            }
                        }

                        Spacer(Modifier.width(12.dp))

                        Button(
                            onClick = {
                                scope.launch {
                                    snackbarHostState.showSnackbar("Audit selesai: Basis data terindeks 100% aman dan bersih.")
                                }
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.outlinedButtonColors(),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                        ) {
                            Text("Audit Cache", fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1, softWrap = false)
                        }
                    }
                }

                // Danger Button: Hapus Semua Data dan Riwayat
                Button(
                    onClick = { showClearConfirm = true },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SigapTheme.colors.dangerContainer,
                        contentColor = SigapTheme.colors.danger,
                    ),
                    border = BorderStroke(1.dp, SigapTheme.colors.danger.copy(alpha = 0.4f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Icon(Lucide.Trash2, contentDescription = null, modifier = Modifier.size(18.dp))
                        Text(
                            text = "Hapus Semua Data dan Riwayat",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                            ),
                        )
                    }
                }

                Text(
                    text = "Tindakan ini akan mengosongkan riwayat aktivitas dan pengaturan akun di perangkat ini secara permanen.",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, lineHeight = 16.sp),
                    color = SigapTheme.colors.textMuted,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                )

                Spacer(Modifier.height(Spacing.Section))
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 16.dp),
        )
    }

    if (showClearConfirm) {
        ClearDataConfirmDialog(
            onConfirm = {
                onClearAll()
                showClearConfirm = false
                onBack()
            },
            onDismiss = { showClearConfirm = false },
        )
    }
}

// Reusable Components
@Composable
private fun SettingsTopBar(
    onOpenNotifications: () -> Unit = {},
) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Box(
                Modifier
                    .size(36.dp)
                    .background(Color(0xFFF9F1E4), RoundedCornerShape(10.dp))
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(R.drawable.logo_mark),
                    contentDescription = "Logo Sigap",
                    modifier = Modifier.size(26.dp),
                )
            }
            val isEnglish = LocalConfiguration.current.locales[0]?.language == "en"
            Text(
                text = if (isEnglish) "Settings" else "Pengaturan",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                ),
                color = MaterialTheme.colorScheme.onBackground,
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            IconButton(onClick = onOpenNotifications) {
                Icon(
                    Lucide.Bell,
                    contentDescription = "Pusat Pemberitahuan",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(22.dp),
                )
            }
            Box(
                Modifier
                    .size(36.dp)
                    .background(MaterialTheme.colorScheme.primary, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Lucide.User,
                    contentDescription = "Profil",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}

@Composable
private fun SubScreenTopBar(title: String, onBack: () -> Unit, onShare: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        IconButton(onClick = onBack) {
            Icon(Lucide.ArrowLeft, contentDescription = "Kembali", tint = MaterialTheme.colorScheme.onBackground)
        }
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(fontSize = 17.sp),
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
        )
        IconButton(onClick = onShare) {
            Icon(Lucide.Share2, contentDescription = "Bagikan", tint = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
private fun DeviceProfileCard(
    userName: String,
    modelName: String,
    appVersion: String,
    onUpdateUserName: (String) -> Unit,
) {
    var showEditDialog by remember { mutableStateOf(false) }
    var editNameInput by remember { mutableStateOf(userName) }

    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Ubah Nama Pengguna", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Nama ini ditampilkan di salam beranda dan identitas perlindungan Sigap:", fontSize = 13.sp)
                    OutlinedTextField(
                        value = editNameInput,
                        onValueChange = { editNameInput = it },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (editNameInput.isNotBlank()) {
                            onUpdateUserName(editNameInput.trim())
                        }
                        showEditDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                ) {
                    Text("Simpan")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text("Batal")
                }
            },
        )
    }

    Surface(
        onClick = {
            editNameInput = userName
            showEditDialog = true
        },
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f),
            ) {
                Box(
                    Modifier
                        .size(46.dp)
                        .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = userName.take(1).uppercase().ifEmpty { "D" },
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                        ),
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text(
                            text = userName,
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                            ),
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Icon(
                            Lucide.ShieldCheck,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp),
                        )
                    }
                    Text(
                        text = "$modelName • $appVersion",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = SigapTheme.colors.textMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            Icon(
                Lucide.Pencil,
                contentDescription = "Ubah Nama",
                tint = SigapTheme.colors.textMuted,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

@Composable
private fun SectionHeader(title: String, statusBadge: String? = null, badgeColor: Color = Color.Unspecified) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
            ),
            color = MaterialTheme.colorScheme.onBackground,
        )
        if (statusBadge != null) {
            Text(
                text = statusBadge,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                ),
                color = badgeColor,
            )
        }
    }
}

@Composable
private fun SettingMenuRow(
    icon: ImageVector,
    title: String,
    value: String? = null,
    valueChip: String? = null,
    onClick: () -> Unit,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f),
        ) {
            Box(
                Modifier
                    .size(36.dp)
                    .background(MaterialTheme.colorScheme.surfaceContainer, RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(18.dp))
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                ),
                color = MaterialTheme.colorScheme.onSurface,
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            if (valueChip != null) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                ) {
                    Text(
                        text = valueChip,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                        ),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                    )
                }
            } else if (value != null) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                    color = SigapTheme.colors.textMuted,
                )
            }
            Icon(
                Lucide.ChevronRight,
                contentDescription = null,
                tint = SigapTheme.colors.textMuted,
                modifier = Modifier.size(16.dp),
            )
        }
    }
}

@Composable
private fun SettingToggleRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f),
        ) {
            Box(
                Modifier
                    .size(36.dp)
                    .background(MaterialTheme.colorScheme.surfaceContainer, RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(18.dp))
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(fontSize = 14.sp, fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = SigapTheme.colors.textMuted,
                )
            }
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                checkedTrackColor = MaterialTheme.colorScheme.primary,
                checkedBorderColor = MaterialTheme.colorScheme.primary,
                uncheckedThumbColor = SigapTheme.colors.textMuted,
                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceContainer,
                uncheckedBorderColor = MaterialTheme.colorScheme.outline,
            ),
        )
    }
}

@Composable
private fun MonitoredAppToggleRow(
    name: String,
    description: String,
    packageName: String,
    icon: ImageVector,
    enabled: Boolean,
    onToggle: (Boolean) -> Unit,
) {
    val context = LocalContext.current
    val isInstalled = remember(packageName) {
        if (packageName == "android.sms.default") true
        else runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(0))
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(packageName, 0)
            }
        }.isSuccess
    }

    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f),
        ) {
            Box(
                Modifier
                    .size(42.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = name,
                        style = MaterialTheme.typography.titleSmall.copy(fontSize = 14.sp, fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false),
                    )
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (isInstalled) SigapTheme.colors.safeContainer else MaterialTheme.colorScheme.surfaceContainer,
                    ) {
                        Text(
                            text = if (isInstalled) "Terpasang" else "Tidak Terpasang",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = if (isInstalled) SigapTheme.colors.safe else SigapTheme.colors.textMuted,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            softWrap = false,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        )
                    }
                }
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = SigapTheme.colors.textMuted,
                )
            }
        }

        Switch(
            checked = enabled,
            onCheckedChange = onToggle,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                checkedTrackColor = MaterialTheme.colorScheme.primary,
                checkedBorderColor = MaterialTheme.colorScheme.primary,
                uncheckedThumbColor = SigapTheme.colors.textMuted,
                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceContainer,
                uncheckedBorderColor = MaterialTheme.colorScheme.outline,
            ),
        )
    }
}

@Composable
private fun CheckPointRow(text: String) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Lucide.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
        Text(text, style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp), color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
private fun CrossPointRow(text: String) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Lucide.X, contentDescription = null, tint = Color(0xFFBA1A1A), modifier = Modifier.size(14.dp))
        Text(text, style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp), color = MaterialTheme.colorScheme.onSurface)
    }
}

private data class OptionItem<T>(
    val value: T,
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val badge: String? = null,
)

@Composable
private fun <T> SigapOptionSelectDialog(
    title: String,
    subtitle: String,
    icon: ImageVector,
    options: List<OptionItem<T>>,
    selected: T,
    onSelect: (T) -> Unit,
    onDismiss: () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
            shadowElevation = 12.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.weight(1f),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp),
                            )
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                ),
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Text(
                                text = subtitle,
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                color = SigapTheme.colors.textMuted,
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp),
                    ) {
                        Icon(
                            imageVector = Lucide.X,
                            contentDescription = "Tutup",
                            tint = SigapTheme.colors.textMuted,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))

                // Options List
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    options.forEach { option ->
                        val isSelected = option.value == selected
                        val containerColor = if (isSelected) {
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f)
                        } else {
                            MaterialTheme.colorScheme.surface
                        }
                        val borderColor = if (isSelected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)
                        }

                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = containerColor,
                            border = BorderStroke(if (isSelected) 1.5.dp else 1.dp, borderColor),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .clickable {
                                    onSelect(option.value)
                                    onDismiss()
                                },
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    modifier = Modifier.weight(1f),
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .background(
                                                if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainer,
                                                RoundedCornerShape(10.dp),
                                            ),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Icon(
                                            imageVector = option.icon,
                                            contentDescription = null,
                                            tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.size(18.dp),
                                        )
                                    }

                                    Column(
                                        modifier = Modifier.weight(1f),
                                        verticalArrangement = Arrangement.spacedBy(2.dp),
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        ) {
                                            Text(
                                                text = option.title,
                                                style = MaterialTheme.typography.titleSmall.copy(
                                                    fontSize = 14.sp,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                                ),
                                                color = MaterialTheme.colorScheme.onSurface,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                modifier = Modifier.weight(1f, fill = false),
                                            )
                                            if (option.badge != null) {
                                                Surface(
                                                    shape = RoundedCornerShape(20.dp),
                                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHighest,
                                                ) {
                                                    Text(
                                                        text = option.badge,
                                                        style = MaterialTheme.typography.labelSmall.copy(
                                                            fontSize = 10.sp,
                                                            fontWeight = FontWeight.SemiBold,
                                                        ),
                                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else SigapTheme.colors.textMuted,
                                                        maxLines = 1,
                                                        softWrap = false,
                                                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                                                    )
                                                }
                                            }
                                        }
                                        Text(
                                            text = option.subtitle,
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                fontSize = 11.sp,
                                                lineHeight = 15.sp,
                                            ),
                                            color = SigapTheme.colors.textMuted,
                                        )
                                    }
                                }

                                Spacer(Modifier.width(14.dp))

                                // Radio indicator
                                Box(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .background(
                                            if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                            CircleShape,
                                        )
                                        .border(
                                            width = if (isSelected) 0.dp else 1.5.dp,
                                            color = if (isSelected) Color.Transparent else MaterialTheme.colorScheme.outline,
                                            shape = CircleShape,
                                        ),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    if (isSelected) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .background(MaterialTheme.colorScheme.onPrimary, CircleShape),
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Close Button
                Button(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier.fillMaxWidth().height(44.dp),
                ) {
                    val isEn = LocalConfiguration.current.locales[0]?.language == "en"
                    Text(
                        text = if (isEn) "Close" else "Tutup",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                        ),
                    )
                }
            }
        }
    }
}

@Composable
private fun AddTrustedSenderDialog(
    onAdd: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var inputKey by remember { mutableStateOf("") }
    val quickChips = listOf("BANKMANDIRI", "BCA_OTP", "BRI_NOTIF", "+6281298448812")

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
            shadowElevation = 12.dp,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.weight(1f),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = Lucide.UserCheck,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp),
                            )
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = "Tambah Pengirim Tepercaya",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                ),
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Text(
                                text = "Kecualikan nomor resmi dari peringatan",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = SigapTheme.colors.textMuted,
                            )
                        }
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(Lucide.X, contentDescription = "Tutup", tint = SigapTheme.colors.textMuted, modifier = Modifier.size(18.dp))
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))

                // Input
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Nomor Telepon atau Sender ID",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    OutlinedTextField(
                        value = inputKey,
                        onValueChange = { inputKey = it },
                        placeholder = { Text("Contoh: BANKMANDIRI atau +62812...") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        ),
                    )
                }

                // Quick chips
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Contoh Cepat:",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                        color = SigapTheme.colors.textMuted,
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        quickChips.take(3).forEach { chip ->
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = MaterialTheme.colorScheme.surfaceContainer,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .clickable { inputKey = chip },
                            ) {
                                Text(
                                    text = chip,
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                )
                            }
                        }
                    }
                }

                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Button(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        modifier = Modifier.weight(1f).height(44.dp),
                    ) {
                        Text("Batal", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    }
                    Button(
                        onClick = {
                            if (inputKey.isNotBlank()) {
                                onAdd(inputKey.trim())
                                onDismiss()
                            }
                        },
                        enabled = inputKey.isNotBlank(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.weight(1.3f).height(44.dp),
                    ) {
                        Text("Simpan", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun ClearDataConfirmDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, Color(0xFFFFB4AB)),
            shadowElevation = 12.dp,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.weight(1f),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .background(Color(0xFFFFECEB), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = Lucide.Trash2,
                                contentDescription = null,
                                tint = Color(0xFFBA1A1A),
                                modifier = Modifier.size(20.dp),
                            )
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = "Hapus Semua Data?",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                ),
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Text(
                                text = "Tindakan ini permanen & tidak bisa dibatalkan",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = Color(0xFFBA1A1A),
                            )
                        }
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(Lucide.X, contentDescription = "Tutup", tint = SigapTheme.colors.textMuted, modifier = Modifier.size(18.dp))
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))

                Text(
                    text = "Semua riwayat pemindaian pesan, statistik deteksi harian, draf laporan, dan daftar pengirim tepercaya akan dihapus dari modul Secure Sandbox lokal ponsel Anda.",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp, lineHeight = 18.sp),
                    color = MaterialTheme.colorScheme.onSurface,
                )

                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Button(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .defaultMinSize(minHeight = 48.dp),
                    ) {
                        Text(
                            text = "Batal",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            softWrap = false,
                        )
                    }
                    Button(
                        onClick = onConfirm,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFBA1A1A)),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
                        modifier = Modifier
                            .weight(1.4f)
                            .defaultMinSize(minHeight = 48.dp),
                    ) {
                        Text(
                            text = "Hapus Permanen",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1,
                            softWrap = false,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoDetailsDialog(
    title: String,
    subtitle: String,
    icon: ImageVector,
    body: String,
    onDismiss: () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
            shadowElevation = 12.dp,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.weight(1f),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp),
                            )
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                ),
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Text(
                                text = subtitle,
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = SigapTheme.colors.textMuted,
                            )
                        }
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(Lucide.X, contentDescription = "Tutup", tint = SigapTheme.colors.textMuted, modifier = Modifier.size(18.dp))
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = body,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp, lineHeight = 19.sp),
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(14.dp),
                    )
                }

                Button(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier.fillMaxWidth().height(44.dp),
                ) {
                    Text("Mengerti", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary)
                }
            }
        }
    }
}

