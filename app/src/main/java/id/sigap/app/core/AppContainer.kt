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
import id.sigap.app.data.local.db.SigapDatabase
import id.sigap.app.data.mlkit.ImageTextExtractor
import id.sigap.app.data.system.SecurityCheckupSource
import id.sigap.app.domain.detection.Redactor
import id.sigap.app.domain.detection.RuleEngine
import id.sigap.app.domain.model.ReportDraftRepository
import id.sigap.app.domain.model.ScanHistoryRepository
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
    val scanHistory: ScanHistoryRepository = RoomScanHistoryRepository(database.scanResultDao())
    val trustedSenders: TrustedSenderRepository = RoomTrustedSenderRepository(database.trustedSenderDao())
    val reportDrafts: ReportDraftRepository = RoomReportDraftRepository(database.reportDraftDao())
    val entityNormalizer = EntityNormalizer()
    val community: CommunityRepository = RoomCommunityRepository(database.communityEntityDao(), entityNormalizer)
    val family: FamilyRepository = RoomFamilyRepository(database.familyDao())
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
}

