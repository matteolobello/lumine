package it.matteolobello.lumine.extension

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Color
import android.util.TypedValue
import it.matteolobello.lumine.R

fun Int.getBrightness() = Math.sqrt(
        Color.red(this) * Color.red(this) * .241 +
                Color.green(this) * Color.green(this) * .691 +
                Color.blue(this) * Color.blue(this) * .068)

fun Int.isLight() = getBrightness() > 200

fun Int.toHex() = String.format("#%06X", 0xFFFFFF and this)

fun ValueAnimator.evaluateColors(evaluation: (color: Int) -> Unit, vararg colors: Int, duration: Long = 300) {
    this.setIntValues(*colors)
    this.setEvaluator(ArgbEvaluator())
    this.addUpdateListener { evaluation(it.animatedValue as Int) }
    this.duration = duration
    this.start()
}

fun Context.fetchAccentColor(): Int {
    val typedValue = TypedValue()

    val a = obtainStyledAttributes(typedValue.data, intArrayOf(R.attr.colorAccent))
    val color = a.getColor(0, 0)

    a.recycle()
    return color
}

fun Int.changeAlpha(ratio: Float): Int {
    val alpha = Math.round(Color.alpha(this) * ratio)
    val r = Color.red(this)
    val g = Color.green(this)
    val b = Color.blue(this)

    return Color.argb(alpha, r, g, b)
}