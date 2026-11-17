package id.sigap.app.data.local.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/** Stored scan result. Holds a redacted snippet only, never the full message text. */
@Entity(tableName = "scan_results")
data class ScanResultEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val sourcePackage: String,
    val senderKind: String,
    val riskLevel: String,
    val score: Int,
    val category: String,
    /** Comma-separated [id.sigap.app.domain.model.ReasonCode] names. */
    val reasons: String,
    val redactedSnippet: String,
    val checkedOnDevice: Boolean,
    val senderKey: String? = null,
)

/** A sender the user chose to trust. The key is a SHA-256 hash of the sender label. */
@Entity(tableName = "trusted_senders")
data class TrustedSenderEntity(
    @PrimaryKey val senderKey: String,
    val addedAt: Long,
)

/** Row for grouped risk-level counts. */
data class LevelCount(
    @ColumnInfo(name = "riskLevel") val riskLevel: String,
    @ColumnInfo(name = "total") val total: Int,
)


/** A report draft the user explicitly saved. Stored in app-private storage only. */
@Entity(tableName = "report_drafts")
data class ReportDraftEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val createdAt: Long,
    val body: String,
)

/** Aggregated community entity reports stored locally (seeded + user submissions). */
@Entity(tableName = "community_entities")
data class CommunityEntityRow(
    @PrimaryKey val entityHash: String,
    val type: String,
    val maskedDisplay: String,
    val reporterCount: Int,
    val topCategory: String,
    val firstSeen: Long,
    val lastSeen: Long,
)

/** Family guardian link stored locally. */
@Entity(tableName = "family_links")
data class FamilyLinkEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val displayName: String,
    val role: String,
    val alertLevel: String,
    val shareSnippet: Boolean,
    val paused: Boolean = false,
    val lastAlertTime: Long? = null,
    val alertsThisWeek: Int = 0,
    val phoneNumber: String = "+628123456789",
)

/** Guardian alert item for a linked family member. */
@Entity(tableName = "guardian_alerts")
data class GuardianAlertEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val linkId: Long,
    val protectedName: String,
    val riskLevel: String,
    val category: String,
    val topReasonKey: String,
    val sourcePackage: String,
    val timestamp: Long,
    val maskedSnippet: String? = null,
    val handled: Boolean = false,
)
