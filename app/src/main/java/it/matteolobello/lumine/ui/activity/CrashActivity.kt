package it.matteolobello.lumine.ui.activity

import androidx.core.app.ShareCompat
import it.matteolobello.lumine.R
import it.matteolobello.lumine.data.bundle.BundleKeys
import it.matteolobello.lumine.ui.activity.base.BaseActivity
import kotlinx.android.synthetic.main.activity_crash.*

class CrashActivity : BaseActivity() {

    private companion object {
        private const val EMAIL = "contact@matteolobello.it"
        private const val SUBJECT = "Lumine Feedback"
    }

    private val emailBody: String by lazy {
        var body = "Crash stacktrace:"
        body += "\n"
        body += "\n"
        body += "\n"
        body += (intent.getSerializableExtra(BundleKeys.EXTRA_THROWABLE) as Throwable).localizedMessage

        return@lazy body
    }

    override fun layoutRes() = R.layout.activity_crash

    override fun setupUi() {
        crashSendFeedbackButton.setOnClickListener {
            ShareCompat.IntentBuilder.from(this)
                    .setType("message/rfc822")
                    .addEmailTo(EMAIL)
                    .setSubject(SUBJECT)
                    .setText(emailBody)
                    .setChooserTitle(getString(R.string.feedback))
                    .startChooser()
        }
    }
}