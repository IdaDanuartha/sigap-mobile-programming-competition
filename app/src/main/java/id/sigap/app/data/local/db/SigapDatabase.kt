package id.sigap.app.data.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        ScanResultEntity::class,
        TrustedSenderEntity::class,
        ReportDraftEntity::class,
        CommunityEntityRow::class,
        FamilyLinkEntity::class,
        GuardianAlertEntity::class,
    ],
    version = 5,
    exportSchema = false,
)
abstract class SigapDatabase : RoomDatabase() {
    abstract fun scanResultDao(): ScanResultDao
    abstract fun trustedSenderDao(): TrustedSenderDao
    abstract fun reportDraftDao(): ReportDraftDao
    abstract fun communityEntityDao(): CommunityEntityDao
    abstract fun familyDao(): FamilyDao

    companion object {
        fun create(context: Context): SigapDatabase =
            Room.databaseBuilder(context, SigapDatabase::class.java, "sigap.db")
                .fallbackToDestructiveMigration()
                .build()
    }
}

