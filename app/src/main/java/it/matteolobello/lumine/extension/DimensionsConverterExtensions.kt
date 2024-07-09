package it.matteolobello.lumine.extension

import android.content.res.Resources

fun Any.dpToPx(dp: Float): Int {
    val metrics = Resources.getSystem().displayMetrics
    val px = dp * (metrics.densityDpi / 160f)
    return Math.round(px)
}