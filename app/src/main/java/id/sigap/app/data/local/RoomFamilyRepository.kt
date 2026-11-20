package id.sigap.app.data.local

import id.sigap.app.data.local.db.FamilyDao
import id.sigap.app.data.local.db.FamilyLinkEntity
import id.sigap.app.data.local.db.GuardianAlertEntity
import id.sigap.app.domain.family.FamilyAlertLevel
import id.sigap.app.domain.family.FamilyLink
import id.sigap.app.domain.family.FamilyRepository
import id.sigap.app.domain.family.FamilyRole
import id.sigap.app.domain.family.GuardianAlert
import id.sigap.app.domain.family.InviteCode
import id.sigap.app.domain.model.ReasonCode
import id.sigap.app.domain.model.RiskLevel
import id.sigap.app.domain.model.ScamCategory
import kotlin.random.Random
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

class RoomFamilyRepository(
    private val dao: FamilyDao,
    private val demoModeProvider: () -> Boolean = { false },
) : FamilyRepository {

    private var cleanedDummy = false

    private suspend fun ensureRealDataOnly() {
        if (cleanedDummy) return
        cleanedDummy = true
        if (!demoModeProvider()) {
            val dummyIds = dao.findSeededDummyLinkIds()
            dummyIds.forEach { id ->
                dao.deleteAlertsForLink(id)
                dao.deleteLink(id)
            }
        }
    }

    override fun observeLinks(): Flow<List<FamilyLink>> = flow {
        ensureRealDataOnly()
        emitAll(
            combine(dao.observeLinks(), dao.observeAllAlerts()) { links, alerts ->
                links.map { linkEntity ->
                    val linkAlerts = alerts.filter { it.linkId == linkEntity.id }
                    val unhandledAlerts = linkAlerts.filter { !it.handled }
                    val latestAlert = linkAlerts.maxByOrNull { it.timestamp }
                    FamilyLink(
                        id = linkEntity.id,
                        displayName = linkEntity.displayName,
                        role = FamilyRole.valueOf(linkEntity.role),
                        alertLevel = FamilyAlertLevel.valueOf(linkEntity.alertLevel),
                        shareSnippet = linkEntity.shareSnippet,
                        paused = linkEntity.paused,
                        lastAlertTime = latestAlert?.timestamp ?: linkEntity.lastAlertTime,
                        alertsThisWeek = unhandledAlerts.size,
                        phoneNumber = linkEntity.phoneNumber,
                    )
                }
            }
        )
    }

    override fun observeAlerts(linkId: Long): Flow<List<GuardianAlert>> = flow {
        ensureRealDataOnly()
        emitAll(dao.observeAlerts(linkId).map { rows -> rows.map { it.toDomain() } })
    }

    override fun observeAlert(alertId: Long): Flow<GuardianAlert?> = flow {
        ensureRealDataOnly()
        emitAll(dao.observeAlert(alertId).map { it?.toDomain() })
    }

    override suspend fun generateInvite(shareSnippet: Boolean): InviteCode {
        val code = (100_000 + Random.nextInt(900_000)).toString()
        val expiresAt = System.currentTimeMillis() + INVITE_TTL_MS
        return InviteCode(code = code, expiresAt = expiresAt, shareSnippet = shareSnippet)
    }

    override suspend fun redeemInvite(
        code: String,
        guardianName: String,
        alertLevel: FamilyAlertLevel,
    ): FamilyLink {
        val protectedName = guardianName.trim().ifBlank { "Anggota Keluarga (#${code.takeLast(3)})" }
        val id = dao.insertLink(
            FamilyLinkEntity(
                displayName = protectedName,
                role = FamilyRole.PROTECTED_BY_ME.name,
                alertLevel = alertLevel.name,
                shareSnippet = false,
                paused = false,
                lastAlertTime = null,
                alertsThisWeek = 0,
            ),
        )
        return FamilyLink(
            id = id,
            displayName = protectedName,
            role = FamilyRole.PROTECTED_BY_ME,
            alertLevel = alertLevel,
            shareSnippet = false,
            paused = false,
            lastAlertTime = null,
            alertsThisWeek = 0,
        )
    }

    override suspend fun togglePause(linkId: Long, paused: Boolean) = dao.setPaused(linkId, paused)

    override suspend fun unlink(linkId: Long) {
        dao.deleteAlertsForLink(linkId)
        dao.deleteLink(linkId)
    }

    override suspend fun markAlertHandled(alertId: Long) = dao.markAlertHandled(alertId)

    private fun FamilyLinkEntity.toDomain() = FamilyLink(
        id = id,
        displayName = displayName,
        role = FamilyRole.valueOf(role),
        alertLevel = FamilyAlertLevel.valueOf(alertLevel),
        shareSnippet = shareSnippet,
        paused = paused,
        lastAlertTime = lastAlertTime,
        alertsThisWeek = alertsThisWeek,
        phoneNumber = phoneNumber,
    )

    private fun GuardianAlertEntity.toDomain() = GuardianAlert(
        id = id,
        linkId = linkId,
        protectedName = protectedName,
        riskLevel = RiskLevel.valueOf(riskLevel),
        category = ScamCategory.valueOf(category),
        topReasonKey = topReasonKey,
        sourcePackage = sourcePackage,
        timestamp = timestamp,
        maskedSnippet = maskedSnippet,
        handled = handled,
    )

    companion object {
        const val INVITE_TTL_MS = 10 * 60 * 1_000L
    }
}
