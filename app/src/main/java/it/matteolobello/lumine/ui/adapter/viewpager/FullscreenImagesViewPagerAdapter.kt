package it.matteolobello.lumine.ui.adapter.viewpager

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentPagerAdapter
import it.matteolobello.lumine.ui.fragment.fullscreenimage.FullscreenImageFragment

class FullscreenImagesViewPagerAdapter(activity: AppCompatActivity, imagePaths: ArrayList<String>)
    : FragmentPagerAdapter(activity.supportFragmentManager) {

    private val fragments = arrayListOf<Fragment>().also { fragments ->
        imagePaths.forEach { imagePath ->
            fragments.add(FullscreenImageFragment.newInstance(imagePath))
        }
    }

    override fun getItem(position: Int) = fragments[position]

    override fun getCount() = fragments.size
}