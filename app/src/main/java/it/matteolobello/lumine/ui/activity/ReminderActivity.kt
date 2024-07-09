package it.matteolobello.lumine.ui.activity

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.text.TextUtils
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import it.matteolobello.lumine.R
import it.matteolobello.lumine.data.bundle.BundleKeys
import it.matteolobello.lumine.data.local.LocalNotesManager
import it.matteolobello.lumine.data.local.sharedpreferences.RemindersPreferenceManager
import it.matteolobello.lumine.data.model.Note
import it.matteolobello.lumine.extension.*
import it.matteolobello.lumine.ui.activity.base.BaseActivity
import kotlinx.android.synthetic.main.activity_reminder.*
import java.util.*

class ReminderActivity : BaseActivity() {

    private val originalNote: Note? by lazy {
        intent.getParcelableExtra(BundleKeys.EXTRA_NOTE) as Note?
    }

    private val isEditMode: Boolean by lazy {
        originalNote != null
    }

    private val calendar = Calendar.getInstance().apply {
        add(Calendar.MINUTE, 1)
    }

    private lateinit var newNote: Note

    override fun layoutRes() = R.layout.activity_reminder

    override fun setupUi() {
        setNavigationBarColor(ContextCompat.getColor(this, R.color.defaultNewNoteBackgroundColor))
        setStatusBarColor(ContextCompat.getColor(this, R.color.defaultNewNoteBackgroundColor))

        newNote = if (isEditMode) {
            originalNote!!
        } else {
            Note()
        }

        if (isEditMode) {
            reminderTitleEditText.setText(originalNote!!.title)

            calendar.timeInMillis = newNote.reminderTimestamp.toLong()
        }

        reminderEveryDayCheckBox.isChecked = newNote.isEveryDayReminder

        updateUi()

        reminderDateCardView.setCardBackgroundColor(fetchAccentColor().changeAlpha(0.15f))
        reminderHourCardView.setCardBackgroundColor(fetchAccentColor().changeAlpha(0.15f))

        backArrowImageView.setOnClickListener { onBackPressed() }

        reminderDateCardView.setOnClickListener { startDatePicker() }
        reminderHourCardView.setOnClickListener { startHourPicker() }

        reminderFab.setOnClickListener {
            saveReminder()
        }
    }

    private fun updateUi() {
        val dayName = calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, Locale.getDefault()).capitalize()
        val month = calendar.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.getDefault()).capitalize()
        val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH).toString().addZeroIfNeeded()
        val year = calendar.get(Calendar.YEAR)

        val completeDayString = "$dayName, $month $dayOfMonth, $year"

        val hour = calendar.get(Calendar.HOUR_OF_DAY).toString().addZeroIfNeeded()
        val minute = calendar.get(Calendar.MINUTE).toString().addZeroIfNeeded()

        val completeHourString = "$hour:$minute"

        reminderDateTextView.text = completeDayString
        reminderHourTextView.text = completeHourString
    }

    private fun startDatePicker() {
        DatePickerDialog(this, DatePickerDialog.OnDateSetListener { _, year, month, day ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, day)

            updateUi()
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun startHourPicker() {
        TimePickerDialog(this, TimePickerDialog.OnTimeSetListener { _, hour, minute ->
            calendar.set(Calendar.HOUR_OF_DAY, hour)
            calendar.set(Calendar.MINUTE, minute)

            updateUi()
        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show()
    }

    private fun saveReminder() {
        val title = reminderTitleEditText.text.toString()
        if (TextUtils.isEmpty(title)) {
            Snackbar.make(reminderRootLayout, R.string.insert_text, Snackbar.LENGTH_SHORT).show()
            return
        }

        if (System.currentTimeMillis() > calendar.timeInMillis) {
            Snackbar.make(reminderRootLayout, R.string.cannot_choose_past_time, Snackbar.LENGTH_SHORT).show()
            return
        }

        if (originalNote != null) {
            LocalNotesManager.get().removeNote(applicationContext, newNote, broadcastUpdate = false)
        }

        newNote.id = Note.generateNewId(this)

        newNote.title = title

        newNote.type = Note.TYPE_REMINDER
        newNote.category = ""
        newNote.reminderTimestamp = calendar.timeInMillis.toDouble()
        newNote.lastEditTimeStamp = System.currentTimeMillis().toDouble()

        newNote.isEveryDayReminder = reminderEveryDayCheckBox.isChecked

        LocalNotesManager.get().insertNewNote(applicationContext, newNote)

        RemindersPreferenceManager.get().registerReminder(applicationContext, newNote)

        finish()
    }
}