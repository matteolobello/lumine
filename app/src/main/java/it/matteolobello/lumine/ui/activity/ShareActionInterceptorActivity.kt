package it.matteolobello.lumine.ui.activity

import android.content.Intent
import android.net.Uri
import android.os.Parcelable
import android.widget.Toast
import androidx.core.content.ContextCompat
import it.matteolobello.lumine.R
import it.matteolobello.lumine.data.local.LocalNotesManager
import it.matteolobello.lumine.data.model.Note
import it.matteolobello.lumine.extension.showToast
import it.matteolobello.lumine.extension.toHex
import it.matteolobello.lumine.extension.toPath
import it.matteolobello.lumine.ui.activity.base.BaseActivity

class ShareActionInterceptorActivity : BaseActivity() {
    override fun layoutRes() = -1

    override fun setupUi() {
        overridePendingTransition(0, 0)

        var error = true
        when {
            intent?.action == Intent.ACTION_SEND -> {
                if ("text/plain" == intent.type) {
                    error = handleSendText(intent) // Handle text being sent
                } else if (intent.type?.startsWith("image/") == true) {
                    error = handleSendImage(intent) // Handle single image being sent
                }
            }
            intent?.action == Intent.ACTION_SEND_MULTIPLE
                    && intent.type?.startsWith("image/") == true -> {
                error = handleSendMultipleImages(intent) // Handle multiple images being sent
            }
            else -> {
                error = true
            }
        }

        showToast(message = getString(if (error) R.string.generic_error else R.string.done), length = Toast.LENGTH_LONG)
        finish()
        overridePendingTransition(0, 0)
    }

    private fun handleSendText(intent: Intent): Boolean {
        intent.getStringExtra(Intent.EXTRA_TEXT)?.let { body ->
            val newNote = Note(
                    id = Note.generateNewId(this),
                    type = Note.TYPE_PLAIN_TEXT,
                    title = getString(R.string.note),
                    body = body,
                    images = arrayListOf(),
                    isTrashed = false,
                    isMarkedAsDone = false,
                    category = "",
                    position = 0,
                    reminderTimestamp = 0.toDouble(),
                    lastEditTimeStamp = System.currentTimeMillis().toDouble(),
                    color = ContextCompat.getColor(this, R.color.defaultNewNoteBackgroundColor).toHex(),
                    isEveryDayReminder = false
            )

            LocalNotesManager.get().insertNewNote(this, newNote)

            return false
        }

        return true
    }

    private fun handleSendImage(intent: Intent): Boolean {
        (intent.getParcelableExtra<Parcelable>(Intent.EXTRA_STREAM) as? Uri)?.let { uri ->
            val path = uri.toPath(this) ?: return true

            val newNote = Note(
                    id = Note.generateNewId(this),
                    type = Note.TYPE_PLAIN_TEXT,
                    title = getString(R.string.note),
                    body = getString(R.string.images),
                    images = arrayListOf<String>().also { it.add(path) },
                    isTrashed = false,
                    isMarkedAsDone = false,
                    category = "",
                    position = 0,
                    reminderTimestamp = 0.toDouble(),
                    lastEditTimeStamp = System.currentTimeMillis().toDouble(),
                    color = ContextCompat.getColor(this, R.color.defaultNewNoteBackgroundColor).toHex(),
                    isEveryDayReminder = false
            )

            LocalNotesManager.get().insertNewNote(this, newNote)

            return false
        }

        return true
    }

    private fun handleSendMultipleImages(intent: Intent): Boolean {
        intent.getParcelableArrayListExtra<Parcelable>(Intent.EXTRA_STREAM)?.let {
            val newNote = Note()
            newNote.id = Note.generateNewId(this)
            newNote.type = Note.TYPE_PLAIN_TEXT
            newNote.title = getString(R.string.note)
            newNote.body = getString(R.string.images)
            newNote.isMarkedAsDone = false
            newNote.category = ""
            newNote.lastEditTimeStamp = System.currentTimeMillis().toDouble()
            newNote.color = ContextCompat.getColor(this, R.color.defaultNewNoteBackgroundColor).toHex()

            it.forEach {
                if (it is Uri) {
                    val path = it.toPath(this)
                    if (path != null) {
                        newNote.images.add(path)
                    }
                }
            }

            LocalNotesManager.get().insertNewNote(this, newNote)

            return false
        }

        return true
    }
}
