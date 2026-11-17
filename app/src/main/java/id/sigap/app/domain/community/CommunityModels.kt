package id.sigap.app.domain.community

import id.sigap.app.domain.model.ScamCategory
import id.sigap.app.domain.normalization.EntityType
import id.sigap.app.domain.normalization.NormalizedEntity

/** Aggregated community reports for a normalized phone number, bank account or domain (PRD section 7.2). */
data class CommunityHit(
    val entityHash: String,
    val type: EntityType,
    val maskedDisplay: String,
    val reporterCount: Int,
    val topCategory: ScamCategory,
    val firstSeen: Long,
    val lastSeen: Long,
) {
    /** Counts as a hard-danger signal only when at least 3 unique reporters flagged it (PRD section 5.4, 7.4). */
    val isHardDanger: Boolean get() = reporterCount >= HARD_DANGER_REPORTER_THRESHOLD

    companion object {
        const val HARD_DANGER_REPORTER_THRESHOLD = 3
    }
}

interface CommunityRepository {
    suspend fun lookup(entity: NormalizedEntity): CommunityHit?
    suspend fun submitReport(entity: NormalizedEntity, category: ScamCategory, note: String): CommunityHit
}
