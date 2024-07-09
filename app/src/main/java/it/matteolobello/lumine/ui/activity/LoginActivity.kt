package it.matteolobello.lumine.ui.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import it.matteolobello.lumine.R
import it.matteolobello.lumine.data.bundle.BundleKeys
import it.matteolobello.lumine.data.local.LocalNotesManager
import it.matteolobello.lumine.data.local.sharedpreferences.DefaultPreferenceManager
import it.matteolobello.lumine.extension.setNavigationBarColor
import it.matteolobello.lumine.extension.setStatusBarColor
import it.matteolobello.lumine.extension.showToast
import it.matteolobello.lumine.ui.activity.base.BaseActivity
import it.matteolobello.lumine.ui.view.dialog.GenericDialogView
import kotlinx.android.synthetic.main.activity_login.*
import java.util.*

class LoginActivity : BaseActivity() {

    private companion object {
        private const val RC_SIGN_IN = 505
    }

    override fun layoutRes() = R.layout.activity_login

    override fun setupUi() {
        setStatusBarColor(ContextCompat.getColor(this, R.color.loginBackgroundColor))
        setNavigationBarColor(ContextCompat.getColor(this, R.color.loginBackgroundColor))

        loginButton.setOnClickListener {
            startActivityForResult(
                    AuthUI.getInstance()
                            .createSignInIntentBuilder()
                            .setIsSmartLockEnabled(false)
                            .setTheme(AuthUI.getDefaultTheme())
                            .setAvailableProviders(
                                    Arrays.asList(AuthUI.IdpConfig.GoogleBuilder().build()))
                            .build(),
                    RC_SIGN_IN)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }

        skipLoginButton.setOnClickListener { _ ->
            GenericDialogView.create(
                    activity = this,
                    title = getString(R.string.warning),
                    message = getString(R.string.skip_login_dialog_message),
                    positiveButtonText = getString(R.string.ok),
                    negativeButtonText = getString(R.string.dismiss),
                    onPositiveButtonClickListener = {
                        it.dismiss()

                        DefaultPreferenceManager.getInstance().setValue(this, DefaultPreferenceManager.KEY_LOGIN_SKIP, "true")
                        startHomeActivity()
                    },
                    onNegativeButtonClickListener = {
                        it.dismiss()
                    }
            )
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            when (resultCode) {
                RESULT_OK -> {
                    val firebaseAuth = FirebaseAuth.getInstance()
                    if (firebaseAuth.currentUser != null) {
                        showToast(message = getString(R.string.welcome_name, firebaseAuth.currentUser!!.displayName!!))

                        startHomeActivity()
                    }
                }
                RESULT_CANCELED -> {
                }
            }
        }
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