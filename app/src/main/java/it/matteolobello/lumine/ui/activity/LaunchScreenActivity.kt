package it.matteolobello.lumine.ui.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.github.javiersantos.piracychecker.*
import com.github.javiersantos.piracychecker.enums.InstallerID
import com.google.firebase.auth.FirebaseAuth
import it.matteolobello.lumine.R
import it.matteolobello.lumine.data.bundle.BundleKeys
import it.matteolobello.lumine.data.local.LocalNotesManager
import it.matteolobello.lumine.data.local.sharedpreferences.DefaultPreferenceManager
import it.matteolobello.lumine.extension.isNetworkAvailable
import it.matteolobello.lumine.ui.activity.base.BaseActivity

class LaunchScreenActivity : BaseActivity() {

    private var piracyChecker: PiracyChecker? = null

    override fun layoutRes() = -1

    override fun setupUi() {
        if (true||!isNetworkAvailable()) {
            onPurchaseVerified()
        } else {
            piracyChecker = piracyChecker {
                enableGooglePlayLicensing(getString(R.string.license_key_base_64))
                enableDebugCheck(true)
                enableInstallerId(InstallerID.GOOGLE_PLAY)
                enableUnauthorizedAppsCheck(true)
                callback {
                    allow {
                        onPurchaseVerified()
                    }
                    doNotAllow { piracyCheckerError, _ ->
                        Toast.makeText(applicationContext, piracyCheckerError.name, Toast.LENGTH_LONG).show()
                        finish()
                    }
                    onError { error ->
                        Toast.makeText(applicationContext, error.name, Toast.LENGTH_LONG).show()
                        finish()
                    }
                }
            }
            piracyChecker?.start()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        piracyChecker?.destroy()
    }

    private fun onPurchaseVerified() {
        if (FirebaseAuth.getInstance().currentUser == null
                && DefaultPreferenceManager.getInstance()
                        .getValue(applicationContext, DefaultPreferenceManager.KEY_LOGIN_SKIP) != "true") {
            startLoginActivity()
        } else {
            startHomeActivity()
        }
    }

    private fun startLoginActivity() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    private fun startHomeActivity() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            startActivity(Intent(this, HomeActivity::class.java)
                    .putExtra(BundleKeys.EXTRA_NOTES, LocalNotesManager.get().loadNotes(this)))
            finish()
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        } else {
            startActivity(Intent(this, OnboardingActivity::class.java))
            finish()
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }
    }
}