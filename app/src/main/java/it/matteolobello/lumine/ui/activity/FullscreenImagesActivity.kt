package it.matteolobello.lumine.ui.activity

import androidx.core.content.ContextCompat
import it.matteolobello.lumine.R
import it.matteolobello.lumine.data.bundle.BundleKeys
import it.matteolobello.lumine.extension.setNavigationBarColor
import it.matteolobello.lumine.extension.setStatusBarColor
import it.matteolobello.lumine.ui.activity.base.BaseActivity
import it.matteolobello.lumine.ui.adapter.viewpager.FullscreenImagesViewPagerAdapter
import kotlinx.android.synthetic.main.activity_fullscreen_image.*

class FullscreenImagesActivity : BaseActivity() {

    override fun layoutRes() = R.layout.activity_fullscreen_image

    override fun setupUi() {
        setStatusBarColor(ContextCompat.getColor(this, android.R.color.white))
        setNavigationBarColor(ContextCompat.getColor(this, android.R.color.white))

        val imagePaths = intent.getStringArrayListExtra(BundleKeys.EXTRA_IMAGE_PATHS)
                ?: throw NullPointerException("Image Paths not set")
        val index = intent.getIntExtra(BundleKeys.EXTRA_IMAGE_INDEX, 0)

        fullscreenImagesViewPager.adapter = FullscreenImagesViewPagerAdapter(this, imagePaths)
        fullscreenImagesViewPager.offscreenPageLimit = imagePaths.size
        fullscreenImagesViewPager.setCurrentItem(index, false)
    }
}