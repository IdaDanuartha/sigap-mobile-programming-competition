package id.sigap.app.data.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import id.sigap.app.SigapApp
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.flow.first

/** Daily job that deletes scan results older than the retention setting. */
class PurgeWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val container = (applicationContext as SigapApp).container
        val retentionDays = container.settings.settings.first().retentionDays
        val cutoff = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(retentionDays.toLong())
        container.scanHistory.deleteOlderThan(cutoff)
        return Result.success()
    }

    companion object {
        private const val UNIQUE_NAME = "purge_expired_results"

        fun schedule(context: Context) {
            val request = PeriodicWorkRequestBuilder<PurgeWorker>(1, TimeUnit.DAYS).build()
            WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(UNIQUE_NAME, ExistingPeriodicWorkPolicy.KEEP, request)
        }
    }
}
