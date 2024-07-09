package it.matteolobello.lumine.extension

import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.google.android.material.tabs.TabLayout

fun TabLayout.fixTabTextViewsLetterSpacingAndAddMoreMargin() {
    val viewGroupInTabLayout = getChildAt(0) as ViewGroup
    val tabsCount = viewGroupInTabLayout.childCount

    for (i in 0 until tabsCount) {
        val viewGroupInTab = viewGroupInTabLayout.getChildAt(i) as ViewGroup
        val tabChildCount = viewGroupInTab.childCount

        (viewGroupInTab.layoutParams as LinearLayout.LayoutParams).apply {
            marginStart = if (i == 0) marginStart else dpToPx(6f)
            marginEnd = dpToPx(6f)
        }

        for (j in 0 until tabChildCount) {
            val tabViewChild = viewGroupInTab.getChildAt(j)
            if (tabViewChild is TextView) {
                tabViewChild.letterSpacing = 0f
            }
        }
    }
}

fun TabLayout.setOnTabItemLongClickListener(callback: (view: View, categoryName: String) -> Unit) {
    val tabStrip = getChildAt(0) as LinearLayout
    for (i in 0 until tabStrip.childCount) {
        tabStrip.getChildAt(i).setOnLongClickListener {
            callback(it, getTabAt(i)!!.text.toString())
            false
        }
    }
}