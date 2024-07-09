package it.matteolobello.lumine.ui.activity.base

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import it.matteolobello.lumine.R
import it.matteolobello.lumine.ui.theme.ThemeManager

abstract class BaseActivity : AppCompatActivity() {

    abstract fun layoutRes(): Int
    abstract fun setupUi()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setTheme(fetchTheme())

        val layoutRes = layoutRes()
        if (layoutRes != -1) {
            setContentView(layoutRes)
        }
        setupUi()
    }

    private fun fetchTheme() = when (ThemeManager.getTheme(this)) {
        ThemeManager.THEME_RED -> R.style.AppTheme_RedAccent
        ThemeManager.THEME_BLUE -> R.style.AppTheme_BlueAccent
        ThemeManager.THEME_PEACH -> R.style.AppTheme_PeachAccent
        ThemeManager.THEME_PURPLE -> R.style.AppTheme_PurpleAccent
        ThemeManager.THEME_BROWN -> R.style.AppTheme_BrownAccent
        ThemeManager.THEME_YELLOW -> R.style.AppTheme_YellowAccent
        else -> R.style.AppTheme
    }
}