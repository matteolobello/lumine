package it.matteolobello.lumine

import android.app.Application
import android.content.Intent
import it.matteolobello.lumine.data.bundle.BundleKeys
import it.matteolobello.lumine.ui.activity.CrashActivity

class LumineApp : Application() {

    override fun onCreate() {
        super.onCreate()

        Thread.setDefaultUncaughtExceptionHandler { _, throwable ->
            startActivity(Intent(this, CrashActivity::class.java)
                    .putExtra(BundleKeys.EXTRA_THROWABLE, throwable))
        }
    }
}