package it.matteolobello.lumine.extension

import android.app.Activity
import android.graphics.Color
import android.view.View
import android.view.WindowManager
import androidx.core.content.ContextCompat
import it.matteolobello.lumine.R
import it.matteolobello.lumine.ui.view.dialog.GenericDialogView

fun Activity.setFullyTransparentStatusBar() {
    setWindowFlag(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, false)
    setStatusBarColor(Color.TRANSPARENT)

    this.window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
}

fun Activity.setFullyTransparentNavigationBars() {
    setWindowFlag(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION, true)
    setNavigationBarColor(Color.TRANSPARENT)

    this.window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
}

private fun Activity.setFullyTransparentSystemBars() {
    setFullyTransparentStatusBar()
    setFullyTransparentNavigationBars()
}

fun Activity.setNavigationBarColor(color: Int, customBrightnessThreshold: Int = -1) {
    if (GenericDialogView.findDialogInActivity(this) != null) {
        setSystemBarColor(false, ContextCompat.getColor(this, R.color.backgroundDim), customBrightnessThreshold)
    } else {
        setSystemBarColor(false, color, customBrightnessThreshold)
    }
}

fun Activity.setStatusBarColor(color: Int, customBrightnessThreshold: Int = -1) {
    if (GenericDialogView.findDialogInActivity(this) != null) {
        setSystemBarColor(true, ContextCompat.getColor(this, R.color.backgroundDim), customBrightnessThreshold)
    } else {
        setSystemBarColor(true, color, customBrightnessThreshold)
    }
}

fun Activity.setSystemBarColor(statusBar: Boolean, color: Int, customBrightnessThreshold: Int = -1) {
    var newColor = color

    when {
        customBrightnessThreshold != -1 && newColor.getBrightness() > customBrightnessThreshold
                || newColor.isLight() ->
            if (isRunningAtLeastM()) {
                if (statusBar) {
                    setLightStatusBar()
                } else {
                    if (isRunningAtLeastO()) {
                        setLightNavigationBar()
                    } else {
                        newColor = ContextCompat.getColor(this, R.color.systemBarsWhitePreM)
                    }
                }
            } else {
                newColor = ContextCompat.getColor(this, R.color.systemBarsWhitePreM)
            }
        else -> if (statusBar) clearLightStatusBar() else clearLightNavigationBar()
    }

    if (statusBar) {
        window.statusBarColor = newColor
    } else {
        window.navigationBarColor = newColor
    }
}

fun Activity.hasNavigationBar(): Boolean {
    val id = resources.getIdentifier("config_showNavigationBar", "bool", "android")
    return id > 0 && resources.getBoolean(id)
}

fun Activity.getNavigationBarHeight(): Int {
    val resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android")
    return if (resourceId > 0) {
        resources.getDimensionPixelSize(resourceId)
    } else 0
}

fun Activity.getStatusBarHeight(): Int {
    val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
    return if (resourceId > 0) {
        resources.getDimensionPixelSize(resourceId)
    } else 0
}

fun Activity.setLightNavigationBar() {
    if (isRunningAtLeastO()) {
        var flags = window.decorView.systemUiVisibility
        flags = flags or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
        window.decorView.systemUiVisibility = flags
    }
}

fun Activity.setLightStatusBar() {
    if (isRunningAtLeastM()) {
        var flags = window.decorView.systemUiVisibility
        flags = flags or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        window.decorView.systemUiVisibility = flags
    }
}

fun Activity.clearLightNavigationBar() {
    if (isRunningAtLeastO()) {
        var flags = window.decorView.systemUiVisibility
        flags = flags and View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR.inv()
        window.decorView.systemUiVisibility = flags
    }
}

fun Activity.clearLightStatusBar() {
    if (isRunningAtLeastM()) {
        var flags = window.decorView.systemUiVisibility
        flags = flags and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
        window.decorView.systemUiVisibility = flags
    }
}

private fun Activity.setWindowFlag(bits: Int, on: Boolean) {
    val win = window
    val winParams = win.attributes
    if (on) {
        winParams.flags = winParams.flags or bits
    } else {
        winParams.flags = winParams.flags and bits.inv()
    }
    win.attributes = winParams
}