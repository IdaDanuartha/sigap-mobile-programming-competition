package id.sigap.app.domain.model

import kotlinx.coroutines.flow.Flow

/** Persistence for scan results. */
interface ScanHistoryRepository {
    /** Saves [result] and returns its generated id. */
    suspend fun save(result: ScanResult): Long
    fun observeLatestAlerts(limit: Int): Flow<List<ScanResult>>
    fun observeAll(): Flow<List<ScanResult>>
    fun observe(id: Long): Flow<ScanResult?>
    suspend fun delete(id: Long)
    suspend fun deleteOlderThan(cutoff: Long): Int
    fun observeTodayStats(): Flow<TodayStats>
}

/** Senders the user marked as trusted. Keys are hashed labels. */
interface TrustedSenderRepository {
    suspend fun isTrusted(senderKey: String): Boolean
    suspend fun trust(senderKey: String)
    fun observeAll(): Flow<List<TrustedSender>>
    suspend fun remove(senderKey: String)
    suspend fun ensureSeeded()
}

/** Report drafts saved on explicit user action. */
interface ReportDraftRepository {
    suspend fun save(body: String): Long
}

/** Checks whether a sender label belongs to a saved phone contact. */
interface ContactLookup {
    fun hasPermission(): Boolean
    fun isSavedContact(label: String): Boolean
}

/** Shows a warning to the user for a stored non-safe result. */
interface AlertNotifier {
    fun notify(result: ScanResult, senderKey: String)
}


