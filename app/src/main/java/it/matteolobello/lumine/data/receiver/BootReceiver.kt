package it.matteolobello.lumine.data.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import it.matteolobello.lumine.data.local.sharedpreferences.RemindersPreferenceManager

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val reminders = RemindersPreferenceManager.get().allReminders(context)
            reminders.forEach {
                RemindersPreferenceManager.get().registerReminder(context, it)
            }
        }
    }
}