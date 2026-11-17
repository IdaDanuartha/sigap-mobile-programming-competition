package id.sigap.app.data.local

import id.sigap.app.data.local.db.CommunityEntityDao
import id.sigap.app.data.local.db.CommunityEntityRow
import id.sigap.app.domain.community.CommunityHit
import id.sigap.app.domain.community.CommunityRepository
import id.sigap.app.domain.model.ScamCategory
import id.sigap.app.domain.normalization.EntityNormalizer
import id.sigap.app.domain.normalization.EntityType
import id.sigap.app.domain.normalization.NormalizedEntity

class RoomCommunityRepository(
    private val dao: CommunityEntityDao,
    private val normalizer: EntityNormalizer = EntityNormalizer(),
) : CommunityRepository {

    private var seeded = false

    private suspend fun ensureSeeded() {
        if (seeded) return
        seeded = true
        if (dao.count() > 0) return
        val now = System.currentTimeMillis()
        val weekAgo = now - 7 * 86_400_000L
        listOf(
            Triple(EntityType.PHONE, "081299998888", ScamCategory.FAKE_INVITATION to 7),
            Triple(EntityType.PHONE, "+62 821-4820-9921", ScamCategory.FAKE_INVITATION to 14),
            Triple(EntityType.BANK_ACCOUNT, "1234567890", ScamCategory.MARKETPLACE_FRAUD to 5),
            Triple(EntityType.LINK, "bca-login.xyz", ScamCategory.BANK_PHISHING to 12),
            Triple(EntityType.LINK, "bit.ly/paket-jnt-resi", ScamCategory.FAKE_COURIER to 7),
            Triple(EntityType.LINK, "https://bit.ly/paket-jnt-resi", ScamCategory.FAKE_COURIER to 7),
        ).forEach { (type, raw, meta) ->
            normalizer.normalize(type, raw)?.let { entity ->
                dao.upsert(
                    CommunityEntityRow(
                        entityHash = entity.sha256,
                        type = entity.type.name,
                        maskedDisplay = entity.maskedDisplay,
                        reporterCount = meta.second,
                        topCategory = meta.first.name,
                        firstSeen = weekAgo,
                        lastSeen = now,
                    ),
                )
            }
        }
    }

    override suspend fun lookup(entity: NormalizedEntity): CommunityHit? {
        ensureSeeded()
        return dao.findByHash(entity.sha256)?.toDomain()
    }

    override suspend fun submitReport(
        entity: NormalizedEntity,
        category: ScamCategory,
        note: String,
    ): CommunityHit {
        ensureSeeded()
        val now = System.currentTimeMillis()
        val existing = dao.findByHash(entity.sha256)
        val updated = if (existing == null) {
            CommunityEntityRow(
                entityHash = entity.sha256,
                type = entity.type.name,
                maskedDisplay = entity.maskedDisplay,
                reporterCount = 1,
                topCategory = category.name,
                firstSeen = now,
                lastSeen = now,
            )
        } else {
            existing.copy(
                reporterCount = existing.reporterCount + 1,
                topCategory = category.name,
                lastSeen = now,
            )
        }
        dao.upsert(updated)
        return updated.toDomain()
    }

    private fun CommunityEntityRow.toDomain() = CommunityHit(
        entityHash = entityHash,
        type = EntityType.valueOf(type),
        maskedDisplay = maskedDisplay,
        reporterCount = reporterCount,
        topCategory = ScamCategory.valueOf(topCategory),
        firstSeen = firstSeen,
        lastSeen = lastSeen,
    )
}
