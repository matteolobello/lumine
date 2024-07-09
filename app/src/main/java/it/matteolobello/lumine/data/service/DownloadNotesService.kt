package it.matteolobello.lumine.data.service

import android.app.IntentService
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import it.matteolobello.lumine.R
import it.matteolobello.lumine.data.local.LocalNotesManager
import it.matteolobello.lumine.data.local.sharedpreferences.NotesPreferenceManager
import it.matteolobello.lumine.data.remote.DatabaseOperations

class DownloadNotesService : IntentService(UploadNotesService::class.java.simpleName) {

    private companion object {

        private const val CHANNEL_ID = "channel_misc"
        private const val NOTIFICATION_ID = 702
    }

    override fun onHandleIntent(intent: Intent) {
        showNotification()

        DatabaseOperations.downloadNotesAsync(applicationContext) { notes ->
            NotesPreferenceManager.get().clearAll(this)

            notes.forEach {
                LocalNotesManager.get().insertNewNote(this, it)
            }

            (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).cancel(NOTIFICATION_ID)
        }
    }

    private fun showNotification() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        createMiscNotificationChannelIfNeeded()

        val intent = Intent()
        val pendingIntent = PendingIntent.getActivity(
                applicationContext, 0, intent, 0)
        val notificationBuilder = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(getString(R.string.downloading_notes_from_cloud))
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .setProgress(0, 0, true)

        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())
    }

    private fun createMiscNotificationChannelIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.misc_channel_name)
            val description = getString(R.string.misc_channel_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance)
            channel.description = description
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager!!.createNotificationChannel(channel)
        }
    }
}