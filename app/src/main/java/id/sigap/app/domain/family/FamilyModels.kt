package id.sigap.app.domain.family

import id.sigap.app.domain.model.RiskLevel
import id.sigap.app.domain.model.ScamCategory
import kotlinx.coroutines.flow.Flow

enum class FamilyRole { GUARDIAN_FOR_ME, PROTECTED_BY_ME }

enum class FamilyAlertLevel { DANGER_ONLY, DANGER_AND_CAUTION }

data class FamilyLink(
    val id: Long,
    val displayName: String,
    val role: FamilyRole,
    val alertLevel: FamilyAlertLevel,
    val shareSnippet: Boolean,
    val paused: Boolean = false,
    val lastAlertTime: Long? = null,
    val alertsThisWeek: Int = 0,
    val phoneNumber: String = "+628123456789",
)

data class GuardianAlert(
    val id: Long,
    val linkId: Long,
    val protectedName: String,
    val riskLevel: RiskLevel,
    val category: ScamCategory,
    val topReasonKey: String,
    val sourcePackage: String,
    val timestamp: Long,
    val maskedSnippet: String? = null,
    val handled: Boolean = false,
)

data class InviteCode(
    val code: String,
    val expiresAt: Long,
    val shareSnippet: Boolean,
)

interface FamilyRepository {
    fun observeLinks(): Flow<List<FamilyLink>>
    fun observeAlerts(linkId: Long): Flow<List<GuardianAlert>>
    fun observeAlert(alertId: Long): Flow<GuardianAlert?>
    suspend fun generateInvite(shareSnippet: Boolean): InviteCode
    suspend fun redeemInvite(code: String, guardianName: String, alertLevel: FamilyAlertLevel): FamilyLink
    suspend fun togglePause(linkId: Long, paused: Boolean)
    suspend fun unlink(linkId: Long)
    suspend fun markAlertHandled(alertId: Long)
}
