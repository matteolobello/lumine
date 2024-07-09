package it.matteolobello.lumine.data.local.sharedpreferences

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.google.gson.Gson
import it.matteolobello.lumine.data.bundle.BundleKeys
import it.matteolobello.lumine.data.local.sharedpreferences.base.BasePreferenceHelper
import it.matteolobello.lumine.data.model.Note
import it.matteolobello.lumine.data.receiver.ReminderReceiver
import it.matteolobello.lumine.extension.isRunningAtLeastM


class RemindersPreferenceManager : BasePreferenceHelper() {

    companion object {

        private var INSTANCE: RemindersPreferenceManager? = null

        fun get(): RemindersPreferenceManager {
            if (INSTANCE == null) {
                INSTANCE = RemindersPreferenceManager()
            }

            return INSTANCE!!
        }
    }

    override fun getFileName() = "active_reminders"

    fun registerReminder(context: Context, reminder: Note) {
        val intent = Intent(context, ReminderReceiver::class.java)
                .putExtra(BundleKeys.EXTRA_NOTE_AS_JSON, Gson().toJson(reminder))
        val pendingIntent = PendingIntent.getBroadcast(context, System.currentTimeMillis().toInt(), intent, 0)

        with(context.getSystemService(Context.ALARM_SERVICE) as AlarmManager) {
            if (context.isRunningAtLeastM()) {
                setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, reminder.reminderTimestamp.toLong(), pendingIntent)
            } else {
                setExact(AlarmManager.RTC_WAKEUP, reminder.reminderTimestamp.toLong(), pendingIntent)
            }
        }

        setValue(context, reminder.id, Gson().toJson(reminder))
    }

    fun cancelReminder(context: Context, reminder: Note) = remove(context, reminder.id)

    fun isActive(context: Context, reminder: Note) = getValue(context, reminder.id) != null

    fun allReminders(context: Context): ArrayList<Note> {
        val reminders = arrayListOf<Note>()
        all(context)?.forEach {
            reminders.add(Gson().fromJson(it.value.toString(), Note::class.java))
        }

        return reminders
    }
}