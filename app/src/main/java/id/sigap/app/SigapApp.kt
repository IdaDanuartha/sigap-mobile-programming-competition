package id.sigap.app

import android.app.Application
import id.sigap.app.core.AppContainer
import id.sigap.app.data.worker.PurgeWorker

/** Application entry point. Owns the manual dependency container. */
class SigapApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        container.alertNotifier.createChannels()
        PurgeWorker.schedule(this)
    }
}

