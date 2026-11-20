package id.sigap.app.core

import android.content.Context
import id.sigap.app.core.util.MonitoredApps
import id.sigap.app.data.local.AndroidContactLookup
import id.sigap.app.data.local.RoomReportDraftRepository
import id.sigap.app.data.local.RoomScanHistoryRepository
import id.sigap.app.data.local.RoomCommunityRepository
import id.sigap.app.data.local.RoomFamilyRepository
import id.sigap.app.data.local.RoomTrustedSenderRepository
import id.sigap.app.domain.community.CommunityRepository
import id.sigap.app.domain.family.FamilyRepository
import id.sigap.app.domain.normalization.EntityNormalizer
import id.sigap.app.data.local.SettingsRepository
import id.sigap.app.data.local.db.FamilyLinkEntity
import id.sigap.app.data.local.db.GuardianAlertEntity
import id.sigap.app.data.local.db.ScanResultEntity
import id.sigap.app.data.local.db.SigapDatabase
import id.sigap.app.data.local.db.TrustedSenderEntity
import id.sigap.app.data.mlkit.ImageTextExtractor
import id.sigap.app.data.system.SecurityCheckupSource
import id.sigap.app.domain.detection.Redactor
import id.sigap.app.domain.detection.RuleEngine
import id.sigap.app.domain.family.FamilyAlertLevel
import id.sigap.app.domain.family.FamilyRole
import id.sigap.app.domain.model.ReasonCode
import id.sigap.app.domain.model.ReportDraftRepository
import id.sigap.app.domain.model.RiskLevel
import id.sigap.app.domain.model.ScamCategory
import id.sigap.app.domain.model.ScanHistoryRepository
import id.sigap.app.domain.model.SenderKind
import id.sigap.app.domain.model.TrustedSenderRepository
import id.sigap.app.domain.model.UserSettings
import id.sigap.app.domain.usecase.AnalyzeMessageUseCase
import id.sigap.app.domain.usecase.ClassifySenderUseCase
import id.sigap.app.service.AlertNotificationManager
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Manual dependency container (Hilt is deferred, see README notes). */
class AppContainer(context: Context) {
    private val appContext = context.applicationContext

    val defaultDispatcher: CoroutineDispatcher = Dispatchers.Default
    private val appScope = CoroutineScope(SupervisorJob() + defaultDispatcher)

    val settings = SettingsRepository(appContext)

    /** Latest user settings, readable synchronously (for example from the listener service). */
    val settingsState: StateFlow<UserSettings> =
        settings.settings.stateIn(appScope, SharingStarted.Eagerly, UserSettings())

    private val database = SigapDatabase.create(appContext)
    val scanHistory: ScanHistoryRepository = RoomScanHistoryRepository(database.scanResultDao()) { settingsState.value.demoMode }
    val trustedSenders: TrustedSenderRepository = RoomTrustedSenderRepository(database.trustedSenderDao()) { settingsState.value.demoMode }
    val reportDrafts: ReportDraftRepository = RoomReportDraftRepository(database.reportDraftDao())
    val entityNormalizer = EntityNormalizer()
    val community: CommunityRepository = RoomCommunityRepository(database.communityEntityDao(), entityNormalizer)
    val family: FamilyRepository = RoomFamilyRepository(database.familyDao()) { settingsState.value.demoMode }
    val alertNotifier = AlertNotificationManager(appContext)
    val checkupSource = SecurityCheckupSource(appContext)
    val imageTextExtractor = ImageTextExtractor(appContext)
    val hybridAi = id.sigap.app.data.remote.HybridAiClassifier()

    val monitoredPackages: Set<String> = MonitoredApps.DEFAULT_PACKAGES

    val classifySender = ClassifySenderUseCase(
        trusted = trustedSenders,
        contacts = AndroidContactLookup(appContext),
        smsPackages = MonitoredApps.SMS_PACKAGES,
    )
    val analyzeMessage = AnalyzeMessageUseCase(
        engine = RuleEngine(),
        redactor = Redactor(),
        history = scanHistory,
        notifier = alertNotifier,
        sensitivityProvider = { settingsState.value.sensitivity },
    )

    init {
        appScope.launch(Dispatchers.IO) {
            trustedSenders.ensureSeeded()
        }
    }

    /** Deletes scan history, saved drafts and trusted senders. Used by "Clear all data". */
    fun clearAllData() {
        appScope.launch(Dispatchers.IO) { database.clearAllTables() }
    }

    /** Enables or disables Demo Mode, populating or clearing sample data for video/presentation demos. */
    fun setDemoMode(enabled: Boolean) {
        appScope.launch(Dispatchers.IO) {
            settings.setDemoMode(enabled)
            val scanDao = database.scanResultDao()
            val familyDao = database.familyDao()
            val trustedDao = database.trustedSenderDao()

            // Always remove previous demo entries first to avoid duplicates
            scanDao.deleteSeededDummyScans()
            familyDao.findSeededDummyLinkIds().forEach { id ->
                familyDao.deleteAlertsForLink(id)
                familyDao.deleteLink(id)
            }
            trustedDao.delete("BANKMANDIRI")
            trustedDao.delete("+62 812-9844-8812")

            if (!enabled) return@launch

            val now = System.currentTimeMillis()
            // 1. Demo scan alerts & safe history (for Home, Activity, & Notification Center)
            scanDao.insert(
                ScanResultEntity(
                    timestamp = now - 12 * 60_000L,
                    sourcePackage = "com.whatsapp",
                    senderKind = SenderKind.UNKNOWN.name,
                    riskLevel = RiskLevel.DANGER.name,
                    score = 94,
                    category = ScamCategory.FAKE_INVITATION.name,
                    reasons = "${ReasonCode.APK_FILE.name},${ReasonCode.SOCIAL_ENGINEERING.name}",
                    redactedSnippet = "Kepada Yth. Bapak/Ibu sekalian, kami mengundang kehadiran Anda di acara pernikahan kami. Silakan buka surat undangan: Undangan_Pernikahan.apk",
                    checkedOnDevice = true,
                    senderKey = "+62 821-4820-9921",
                ),
            )
            scanDao.insert(
                ScanResultEntity(
                    timestamp = now - 55 * 60_000L,
                    sourcePackage = "com.google.android.apps.messaging",
                    senderKind = SenderKind.SHORTCODE.name,
                    riskLevel = RiskLevel.DANGER.name,
                    score = 88,
                    category = ScamCategory.FAKE_COURIER.name,
                    reasons = "${ReasonCode.SHORTENED_LINK.name},${ReasonCode.URGENCY.name}",
                    redactedSnippet = "Tautan pendek mencurigakan mengaku dari kurir paket J&T untuk verifikasi alamat https://bit.ly/paket-jnt-resi",
                    checkedOnDevice = true,
                    senderKey = "INFO-KURIR",
                ),
            )
            scanDao.insert(
                ScanResultEntity(
                    timestamp = now - 3 * 3600_000L,
                    sourcePackage = "com.whatsapp",
                    senderKind = SenderKind.UNKNOWN.name,
                    riskLevel = RiskLevel.DANGER.name,
                    score = 92,
                    category = ScamCategory.AUTHORITY_IMPERSONATION.name,
                    reasons = "${ReasonCode.APK_FILE.name},${ReasonCode.IMPERSONATION.name}",
                    redactedSnippet = "Polda Metro Jaya: Kendaraan B 4820 KLP tercatat melanggar batas kecepatan. Surat tilang: Surat_Konfirmasi_ETLE.apk",
                    checkedOnDevice = true,
                    senderKey = "+62 813-7741-0029",
                ),
            )
            scanDao.insert(
                ScanResultEntity(
                    timestamp = now - 26 * 3600_000L,
                    sourcePackage = "org.telegram.messenger",
                    senderKind = SenderKind.UNKNOWN.name,
                    riskLevel = RiskLevel.CAUTION.name,
                    score = 58,
                    category = ScamCategory.JOB_SCAM.name,
                    reasons = "${ReasonCode.MONEY_REWARD.name},${ReasonCode.URGENCY.name}",
                    redactedSnippet = "Tawaran kerja paruh waktu like & subscribe komisi Rp 500rb/hari. Segera daftar sebelum kuota habis!",
                    checkedOnDevice = true,
                    senderKey = "@AdminKerjaCepat",
                ),
            )

            // 2. Demo family links & guardian alert (Penjagaku & Orang yang Kujaga)
            familyDao.insertLink(
                FamilyLinkEntity(
                    displayName = "Dewi (Kakak)",
                    role = FamilyRole.GUARDIAN_FOR_ME.name,
                    alertLevel = FamilyAlertLevel.DANGER_ONLY.name,
                    shareSnippet = false,
                    paused = false,
                    lastAlertTime = null,
                    alertsThisWeek = 0,
                    phoneNumber = "+6281234568812",
                ),
            )
            val pakBudiId = familyDao.insertLink(
                FamilyLinkEntity(
                    displayName = "Pak Budi (Ayah)",
                    role = FamilyRole.PROTECTED_BY_ME.name,
                    alertLevel = FamilyAlertLevel.DANGER_ONLY.name,
                    shareSnippet = true,
                    paused = false,
                    lastAlertTime = now - 12 * 60_000L,
                    alertsThisWeek = 2,
                    phoneNumber = "+6281388214490",
                ),
            )
            familyDao.insertLink(
                FamilyLinkEntity(
                    displayName = "Ibu",
                    role = FamilyRole.PROTECTED_BY_ME.name,
                    alertLevel = FamilyAlertLevel.DANGER_ONLY.name,
                    shareSnippet = false,
                    paused = false,
                    lastAlertTime = null,
                    alertsThisWeek = 0,
                    phoneNumber = "+6281192319102",
                ),
            )
            familyDao.insertAlert(
                GuardianAlertEntity(
                    linkId = pakBudiId,
                    protectedName = "Pak Budi (Ayah)",
                    riskLevel = RiskLevel.DANGER.name,
                    category = ScamCategory.FAKE_INVITATION.name,
                    topReasonKey = ReasonCode.APK_FILE.name,
                    sourcePackage = "com.whatsapp",
                    timestamp = now - 12 * 60_000L,
                    maskedSnippet = "Kepada Yth. Bapak/Ibu sekalian. Kami bermaksud mengundang kehadiran Anda. Silakan buka surat undangan: Undangan_Pernikahan.apk",
                    handled = false,
                ),
            )
            familyDao.insertAlert(
                GuardianAlertEntity(
                    linkId = pakBudiId,
                    protectedName = "Pak Budi (Ayah)",
                    riskLevel = RiskLevel.DANGER.name,
                    category = ScamCategory.FAKE_COURIER.name,
                    topReasonKey = ReasonCode.APK_FILE.name,
                    sourcePackage = "com.google.android.apps.messaging",
                    timestamp = now - 2 * 3600_000L,
                    maskedSnippet = "Paket J&T Express tertahan di sortir Cakung. Unduh aplikasi pelacak: Lacak_Paket_JNT.apk",
                    handled = false,
                ),
            )

            // 3. Demo trusted senders
            trustedDao.insert(TrustedSenderEntity("BANKMANDIRI", now - 86400000L * 3))
            trustedDao.insert(TrustedSenderEntity("+62 812-9844-8812", now - 86400000L * 7))
        }
    }
}

