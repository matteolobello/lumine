package it.matteolobello.lumine.ui.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import it.matteolobello.lumine.R
import it.matteolobello.lumine.data.bundle.BundleKeys
import it.matteolobello.lumine.data.local.LocalNotesManager
import it.matteolobello.lumine.extension.setNavigationBarColor
import it.matteolobello.lumine.extension.setStatusBarColor
import it.matteolobello.lumine.ui.activity.base.BaseActivity
import it.matteolobello.lumine.ui.adapter.viewpager.OnboardingViewPagerAdapter
import kotlinx.android.synthetic.main.activity_onboarding.*

class OnboardingActivity : BaseActivity() {

    private companion object {
        private const val RC_PERMISSIONS = 102
    }

    override fun layoutRes() = R.layout.activity_onboarding

    override fun setupUi() {
        setNavigationBarColor(ContextCompat.getColor(this, android.R.color.white))
        setStatusBarColor(ContextCompat.getColor(this, android.R.color.white))

        onboardingViewPager.adapter = OnboardingViewPagerAdapter(this)
        onboardingViewPager.offscreenPageLimit = 3

        viewPagerDotsIndicator.setViewPager(onboardingViewPager)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        onGetStartedButtonClick()
    }

    fun onGetStartedButtonClick() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            startActivity(Intent(this, HomeActivity::class.java)
                    .putExtra(BundleKeys.EXTRA_NOTES, LocalNotesManager.get().loadNotes(this)))
            finish()
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), RC_PERMISSIONS)
        }
    }

    fun onSkipButtonClick() {
        onboardingViewPager.currentItem = 2
    }
}