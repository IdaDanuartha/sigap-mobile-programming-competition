package id.sigap.app.data.local

import id.sigap.app.data.local.db.LevelCount
import id.sigap.app.data.local.db.ScanResultDao
import id.sigap.app.data.local.db.ScanResultEntity
import id.sigap.app.data.local.db.TrustedSenderDao
import id.sigap.app.data.local.db.ReportDraftDao
import id.sigap.app.data.local.db.ReportDraftEntity
import id.sigap.app.data.local.db.TrustedSenderEntity
import id.sigap.app.domain.model.ReasonCode
import id.sigap.app.domain.model.RiskLevel
import id.sigap.app.domain.model.ScamCategory
import id.sigap.app.domain.model.ReportDraftRepository
import id.sigap.app.domain.model.ScanHistoryRepository
import id.sigap.app.domain.model.ScanResult
import id.sigap.app.domain.model.SenderKind
import id.sigap.app.domain.model.TodayStats
import id.sigap.app.domain.model.TrustedSender
import id.sigap.app.domain.model.TrustedSenderRepository
import java.util.Calendar
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

class RoomScanHistoryRepository(private val dao: ScanResultDao) : ScanHistoryRepository {

    override suspend fun save(result: ScanResult): Long = dao.insert(result.toEntity())

    override fun observeLatestAlerts(limit: Int): Flow<List<ScanResult>> =
        dao.observeLatestAlerts(limit).map { rows -> rows.map { it.toDomain() } }

    override fun observeAll(): Flow<List<ScanResult>> =
        dao.observeAll().map { rows -> rows.map { it.toDomain() } }

    override fun observe(id: Long): Flow<ScanResult?> =
        dao.observeById(id).map { it?.toDomain() }

    override suspend fun delete(id: Long) {
        dao.deleteById(id)
    }

    override suspend fun deleteOlderThan(cutoff: Long): Int = dao.deleteOlderThan(cutoff)

    override fun observeTodayStats(): Flow<TodayStats> =
        dao.observeCountsSince(startOfToday()).map(::toStats)

    private fun toStats(rows: List<LevelCount>): TodayStats {
        fun count(level: RiskLevel) = rows.firstOrNull { it.riskLevel == level.name }?.total ?: 0
        val danger = count(RiskLevel.DANGER)
        return TodayStats(
            scanned = rows.sumOf { it.total },
            warnings = count(RiskLevel.CAUTION) + danger,
            dangerous = danger,
        )
    }

    private fun startOfToday(): Long = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis
}

private fun ScanResult.toEntity() = ScanResultEntity(
    id = id,
    timestamp = timestamp,
    sourcePackage = sourcePackage,
    senderKind = senderKind.name,
    riskLevel = level.name,
    score = score,
    category = category.name,
    reasons = reasons.joinToString(",") { it.name },
    redactedSnippet = redactedSnippet,
    checkedOnDevice = checkedOnDevice,
    senderKey = senderKey,
)

private fun ScanResultEntity.toDomain() = ScanResult(
    id = id,
    timestamp = timestamp,
    sourcePackage = sourcePackage,
    senderKind = SenderKind.valueOf(senderKind),
    level = RiskLevel.valueOf(riskLevel),
    score = score,
    category = ScamCategory.valueOf(category),
    reasons = reasons.split(',').filter { it.isNotEmpty() }.map { ReasonCode.valueOf(it) },
    redactedSnippet = redactedSnippet,
    checkedOnDevice = checkedOnDevice,
    senderKey = senderKey,
)

class RoomTrustedSenderRepository(private val dao: TrustedSenderDao) : TrustedSenderRepository {
    override suspend fun isTrusted(senderKey: String): Boolean = dao.isTrusted(senderKey)

    override suspend fun trust(senderKey: String) =
        dao.insert(TrustedSenderEntity(senderKey, System.currentTimeMillis()))

    override fun observeAll(): Flow<List<TrustedSender>> =
        dao.observeAll().map { rows -> rows.map { TrustedSender(it.senderKey, it.addedAt) } }

    override suspend fun remove(senderKey: String) = dao.delete(senderKey)

    private var cleanedDummy = false

    override suspend fun ensureSeeded() {
        if (cleanedDummy) return
        cleanedDummy = true
        dao.delete("BANKMANDIRI")
        dao.delete("+62 812-9844-8812")
    }
}

class RoomReportDraftRepository(private val dao: ReportDraftDao) : ReportDraftRepository {
    override suspend fun save(body: String): Long = dao.insert(ReportDraftEntity(createdAt = System.currentTimeMillis(), body = body))
}

