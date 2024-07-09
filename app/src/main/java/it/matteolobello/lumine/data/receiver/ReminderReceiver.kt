package it.matteolobello.lumine.data.receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.gson.Gson
import it.matteolobello.lumine.R
import it.matteolobello.lumine.data.bundle.BundleKeys
import it.matteolobello.lumine.data.local.LocalNotesManager
import it.matteolobello.lumine.data.local.sharedpreferences.RemindersPreferenceManager
import it.matteolobello.lumine.data.model.Note
import it.matteolobello.lumine.extension.log
import it.matteolobello.lumine.ui.activity.HomeActivity
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class ReminderReceiver : BroadcastReceiver() {

    private companion object {

        private const val CHANNEL_ID = "channel_reminders"
    }

    override fun onReceive(context: Context, intent: Intent) {
        log("ReminderReceiver -> onReceive")

        val reminder = Gson().fromJson<Note>(intent.getStringExtra(BundleKeys.EXTRA_NOTE_AS_JSON), Note::class.java)

        if (RemindersPreferenceManager.get().isActive(context, reminder)) {
            showNotification(context, reminder)

            RemindersPreferenceManager.get().cancelReminder(context, reminder)
            LocalNotesManager.get().removeNote(context, reminder)

            if (reminder.isEveryDayReminder) {
                reminder.reminderTimestamp += TimeUnit.DAYS.toMillis(1)

                RemindersPreferenceManager.get().registerReminder(context, reminder)
                LocalNotesManager.get().insertNewNote(context, reminder)
            }
        }
    }

    private fun showNotification(context: Context, reminder: Note) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        createRemindersNotificationChannelIfNeeded(context)

        val intent = Intent(context, HomeActivity::class.java)
                .putExtra(BundleKeys.EXTRA_NOTES, LocalNotesManager.get().loadNotes(context))
        val pendingIntent = PendingIntent.getActivity(
                context, 0, intent, 0)
        val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(reminder.title)
                .setContentIntent(pendingIntent)
                .setOngoing(false)

        notificationManager.notify(createNotificationId(), notificationBuilder.build())
    }

    private fun createRemindersNotificationChannelIfNeeded(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = context.getString(R.string.reminders_channel_name)
            val description = context.getString(R.string.reminders_channel_description)
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance)
            channel.description = description
            val notificationManager = context.getSystemService(NotificationManager::class.java)
            notificationManager!!.createNotificationChannel(channel)
        }
    }

    private fun createNotificationId() = Integer.parseInt(SimpleDateFormat("ddHHmmss", Locale.US).format(Date()))
}