package it.matteolobello.lumine.ui.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceFragment
import android.widget.Toast
import androidx.core.content.ContextCompat
import it.matteolobello.lumine.BuildConfig
import it.matteolobello.lumine.R
import it.matteolobello.lumine.data.bundle.BundleKeys
import it.matteolobello.lumine.extension.setNavigationBarColor
import it.matteolobello.lumine.extension.setStatusBarColor
import it.matteolobello.lumine.ui.activity.base.BaseActivity
import it.matteolobello.lumine.ui.theme.ThemeManager
import kotlinx.android.synthetic.main.activity_settings.*

class SettingsActivity : BaseActivity() {

    override fun layoutRes() = R.layout.activity_settings

    override fun setupUi() {
        setNavigationBarColor(ContextCompat.getColor(this, android.R.color.white))
        setStatusBarColor(ContextCompat.getColor(this, android.R.color.white))

        fragmentManager.beginTransaction()
                .replace(R.id.settingsFragmentContainer, SettingsFragment())
                .commit()

        settingsBackArrowImageView.setOnClickListener {
            finish()
        }
    }

    companion object {

        const val REQUEST_CODE = 177

        private const val PREFS_FILE_NAME = "settings"
        private const val PREF_KEY_ACCENT_COLOR = "accent_color"
    }

    class SettingsFragment : PreferenceFragment() {

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)

            preferenceManager.apply {
                sharedPreferencesName = PREFS_FILE_NAME
                sharedPreferencesMode = Context.MODE_PRIVATE
            }

            addPreferencesFromResource(R.xml.preferences)

            findPreference(PREF_KEY_ACCENT_COLOR).setOnPreferenceChangeListener { _, newValue ->
                val value = when (newValue) {
                    ContextCompat.getColor(activity, R.color.colorAccentRed) -> ThemeManager.THEME_RED
                    ContextCompat.getColor(activity, R.color.colorAccentBlue) -> ThemeManager.THEME_BLUE
                    ContextCompat.getColor(activity, R.color.colorAccentPeach) -> ThemeManager.THEME_PEACH
                    ContextCompat.getColor(activity, R.color.colorAccentPurple) -> ThemeManager.THEME_PURPLE
                    ContextCompat.getColor(activity, R.color.colorAccentBrown) -> ThemeManager.THEME_BROWN
                    ContextCompat.getColor(activity, R.color.colorAccentYellow) -> ThemeManager.THEME_YELLOW
                    else -> ThemeManager.THEME_DEFAULT
                }

                ThemeManager.setTheme(activity, value)

                Toast.makeText(activity, R.string.applying_changes, Toast.LENGTH_LONG).show()

                activity.setResult(Activity.RESULT_OK, Intent().putExtra(BundleKeys.EXTRA_THEME_CHANGED, true))
                activity.finish()

                true
            }

            findPreference("version").apply {
                summary = BuildConfig.VERSION_NAME
                setOnPreferenceClickListener {
                    Toast.makeText(activity, "❤️", Toast.LENGTH_SHORT).show()

                    false
                }
            }
        }
    }
}