package id.sigap.app.data.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ScanResultDao {
    @Insert
    suspend fun insert(entity: ScanResultEntity): Long

    @Query("SELECT * FROM scan_results WHERE riskLevel != 'SAFE' ORDER BY timestamp DESC LIMIT :limit")
    fun observeLatestAlerts(limit: Int): Flow<List<ScanResultEntity>>

    @Query("SELECT * FROM scan_results ORDER BY timestamp DESC")
    fun observeAll(): Flow<List<ScanResultEntity>>

    @Query("SELECT * FROM scan_results WHERE id = :id")
    fun observeById(id: Long): Flow<ScanResultEntity?>

    @Query("DELETE FROM scan_results WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT riskLevel, COUNT(*) AS total FROM scan_results WHERE timestamp >= :since GROUP BY riskLevel")
    fun observeCountsSince(since: Long): Flow<List<LevelCount>>

    @Query("SELECT COUNT(*) FROM scan_results")
    suspend fun count(): Int

    @Query("DELETE FROM scan_results WHERE timestamp < :cutoff")
    suspend fun deleteOlderThan(cutoff: Long): Int

    @Query(
        """
        DELETE FROM scan_results
        WHERE redactedSnippet LIKE 'Tautan pendek mencurigakan mengaku dari kurir paket J&T%'
           OR redactedSnippet LIKE 'Kepada Yth. Bapak/Ibu sekalian, kami mengundang kehadiran Anda di acara pernikahan kami%'
           OR redactedSnippet LIKE 'Paket J&T Express tertahan di sortir Cakung%'
           OR redactedSnippet LIKE 'Polda Metro Jaya: Kendaraan B 4820 KLP%'
           OR redactedSnippet LIKE 'Tawaran kerja paruh waktu like & subscribe komisi Rp 500rb/hari%'
        """
    )
    suspend fun deleteSeededDummyScans(): Int
}

@Dao
interface TrustedSenderDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(entity: TrustedSenderEntity)

    @Query("SELECT EXISTS(SELECT 1 FROM trusted_senders WHERE senderKey = :key)")
    suspend fun isTrusted(key: String): Boolean

    @Query("SELECT * FROM trusted_senders ORDER BY addedAt DESC")
    fun observeAll(): Flow<List<TrustedSenderEntity>>

    @Query("DELETE FROM trusted_senders WHERE senderKey = :key")
    suspend fun delete(key: String)

    @Query("SELECT COUNT(*) FROM trusted_senders")
    suspend fun count(): Int
}

@Dao
interface ReportDraftDao {
    @Insert
    suspend fun insert(entity: ReportDraftEntity): Long
}



@Dao
interface CommunityEntityDao {
    @Query("SELECT * FROM community_entities WHERE entityHash = :hash")
    suspend fun findByHash(hash: String): CommunityEntityRow?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(row: CommunityEntityRow)

    @Query("SELECT COUNT(*) FROM community_entities")
    suspend fun count(): Int
}

@Dao
interface FamilyDao {
    @Query("SELECT COUNT(*) FROM family_links")
    suspend fun count(): Int

    @Query("SELECT * FROM family_links ORDER BY id DESC")
    fun observeLinks(): Flow<List<FamilyLinkEntity>>

    @Query("SELECT * FROM guardian_alerts ORDER BY timestamp DESC")
    fun observeAllAlerts(): Flow<List<GuardianAlertEntity>>

    @Query("SELECT * FROM guardian_alerts WHERE linkId = :linkId ORDER BY timestamp DESC")
    fun observeAlerts(linkId: Long): Flow<List<GuardianAlertEntity>>

    @Insert
    suspend fun insertLink(entity: FamilyLinkEntity): Long

    @Insert
    suspend fun insertAlert(entity: GuardianAlertEntity): Long

    @Query("UPDATE family_links SET paused = :paused WHERE id = :linkId")
    suspend fun setPaused(linkId: Long, paused: Boolean)

    @Query("DELETE FROM family_links WHERE id = :linkId")
    suspend fun deleteLink(linkId: Long)

    @Query("DELETE FROM guardian_alerts WHERE linkId = :linkId")
    suspend fun deleteAlertsForLink(linkId: Long)

    @Query("SELECT * FROM guardian_alerts WHERE id = :alertId LIMIT 1")
    fun observeAlert(alertId: Long): Flow<GuardianAlertEntity?>

    @Query("SELECT * FROM guardian_alerts WHERE id = :alertId LIMIT 1")
    suspend fun getAlert(alertId: Long): GuardianAlertEntity?

    @Query("UPDATE guardian_alerts SET handled = 1 WHERE id = :alertId")
    suspend fun markAlertHandled(alertId: Long)

    @Query(
        """
        SELECT id FROM family_links
        WHERE (displayName = 'Dewi (Kakak)' AND phoneNumber = '+6281234568812')
           OR (displayName = 'Pak Budi (Ayah)' AND phoneNumber = '+6281388214490')
           OR (displayName = 'Ibu' AND phoneNumber = '+6281192319102')
        """
    )
    suspend fun findSeededDummyLinkIds(): List<Long>
}
