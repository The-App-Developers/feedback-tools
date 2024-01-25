package uk.co.tappable.feedbuck

import android.app.Application
import timber.log.Timber
import uk.co.tappable.feedback.logs.InAppLoggingTree

class FeedbuckApp : Application() {

    override fun onCreate() {
        super.onCreate()
        Timber.plant(
            Timber.DebugTree(),
            InAppLoggingTree(true, cacheFile = cacheDir.resolve("logs.txt"))
        )
    }
}