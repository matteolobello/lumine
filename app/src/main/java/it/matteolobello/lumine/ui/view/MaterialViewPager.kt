package it.matteolobello.lumine.ui.view

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.viewpager.widget.ViewPager

class MaterialViewPager @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null
) : ViewPager(context, attrs) {

    private companion object {
        const val SWIPE_ENABLE = false
    }

    override fun onTouchEvent(motionEvent: MotionEvent?) = SWIPE_ENABLE

    override fun onInterceptTouchEvent(motionEvent: MotionEvent?) = SWIPE_ENABLE
}