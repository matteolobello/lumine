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
import it.matteolobello.lumine.data.bundle.BundleKeys
import it.matteolobello.lumine.data.local.LocalNotesManager
import it.matteolobello.lumine.data.remote.DatabaseOperations
import it.matteolobello.lumine.extension.log
import it.matteolobello.lumine.ui.activity.HomeActivity

class UploadNotesService : IntentService(UploadNotesService::class.java.simpleName) {

    private companion object {

        private const val CHANNEL_ID = "channel_misc"
        private const val NOTIFICATION_ID = 701

        private const val MAX_PROGRESS = 100
    }

    override fun onHandleIntent(intent: Intent) {
        val notes = LocalNotesManager.get().loadNotes(this)
        if (notes.isEmpty()) {
            showSuccessNotification()
            return
        }

        updateProgressInNotification(0)

        DatabaseOperations.removeAll(this) {
            log("Removed User data from Database")

            DatabaseOperations.uploadNotes(this, notes) { progress ->
                log("Progress -> $progress%")

                updateProgressInNotification(progress)
            }
        }
    }

    private fun updateProgressInNotification(progress: Int) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (progress == MAX_PROGRESS) {
            notificationManager.cancel(NOTIFICATION_ID)
            showSuccessNotification()
            return
        }

        createMiscNotificationChannelIfNeeded()

        val intent = Intent()
        val pendingIntent = PendingIntent.getActivity(
                applicationContext, 0, intent, 0)
        val notificationBuilder = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(getString(R.string.syncing_notes_notification_title))
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .setProgress(MAX_PROGRESS, progress, false)

        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())
    }

    private fun showSuccessNotification() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        createMiscNotificationChannelIfNeeded()

        val intent = Intent(applicationContext, HomeActivity::class.java)
                .putExtra(BundleKeys.EXTRA_NOTES, LocalNotesManager.get().loadNotes(this))
        val pendingIntent = PendingIntent.getActivity(
                applicationContext, 0, intent, 0)
        val notificationBuilder = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(getString(R.string.notes_synced_notification_title))
                .setContentText(getString(R.string.notes_synced_notification_text))
                .setContentIntent(pendingIntent)
                .setOngoing(false)

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