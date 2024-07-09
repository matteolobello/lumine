package it.matteolobello.lumine.ui.theme

import android.content.Context

/**
 * Create new theme following these steps:
 *
 *  - accents.xml:
 *        and add the new color inside the array
 *
 *  - styles.xml:
 *        create a new style
 *
 *  - ThemeManager.kt:
 *        add a new constant
 *
 *  - SettingsActivity.kt:
 *        Scroll to SettingsFragment and check the OnPreferenceChangeListener,
 *        convert the int color to its corresponding Preferences key
 *
 *  - BaseActivity.kt:
 *        Change the fetchTheme method implementing the
 *        new Preference key and the corresponding theme
 *
 */
object ThemeManager {

    const val THEME_DEFAULT = "Default"
    const val THEME_RED = "Red"
    const val THEME_BLUE = "Blue"
    const val THEME_PEACH = "Peach"
    const val THEME_PURPLE = "Purple"
    const val THEME_BROWN = "Brown"
    const val THEME_YELLOW = "Yellow"

    private const val PREFS_FILE_NAME = "theme_manager"
    private const val KEY_ACCENT_COLOR = "accent_color"

    fun setTheme(context: Context, theme: String) =
            sharedPreferences(context).edit()
                    .putString(KEY_ACCENT_COLOR, theme)
                    .apply()

    fun getTheme(context: Context) = sharedPreferences(context).getString(KEY_ACCENT_COLOR, THEME_DEFAULT)!!

    private fun sharedPreferences(context: Context) = context.getSharedPreferences(PREFS_FILE_NAME, Context.MODE_PRIVATE)
}