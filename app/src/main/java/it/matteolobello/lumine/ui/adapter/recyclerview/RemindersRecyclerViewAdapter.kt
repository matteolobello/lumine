package it.matteolobello.lumine.ui.adapter.recyclerview

import android.content.Context
import android.content.Intent
import android.os.Vibrator
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import it.matteolobello.lumine.R
import it.matteolobello.lumine.data.bundle.BundleKeys
import it.matteolobello.lumine.data.local.LocalNotesManager
import it.matteolobello.lumine.data.local.sharedpreferences.RemindersPreferenceManager
import it.matteolobello.lumine.data.model.DiffUtilNoteImplementation
import it.matteolobello.lumine.data.model.Note
import it.matteolobello.lumine.extension.getRelativeTimeDisplayString
import it.matteolobello.lumine.ui.activity.ReminderActivity
import java.util.*

class RemindersRecyclerViewAdapter : RecyclerView.Adapter<RemindersRecyclerViewAdapter.ViewHolder>() {

    val reminders = arrayListOf<Note>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_reminder, parent, false))

    override fun getItemCount() = reminders.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val reminder = reminders[position]

        val timeMessage = reminder.reminderTimestamp.getRelativeTimeDisplayString(holder.itemView.context)
        holder.titleTextView.text = reminder.title
        holder.timeTextView.text = timeMessage

        holder.itemView.setOnClickListener {
            it.context.startActivity(Intent(it.context, ReminderActivity::class.java)
                    .putExtra(BundleKeys.EXTRA_NOTE, reminder))
        }

        holder.itemView.setOnLongClickListener { view ->
            val popupMenu = PopupMenu(view.context, view)
            popupMenu.inflate(R.menu.reminder_item_popup_menu)
            popupMenu.setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.action_delete_reminder -> {
                        RemindersPreferenceManager.get().cancelReminder(view.context, reminder)
                        LocalNotesManager.get().removeNote(view.context, reminder)
                    }
                }

                return@setOnMenuItemClickListener false
            }
            popupMenu.show()

            (view.context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator).vibrate(7)

            false
        }
    }

    fun setReminders(newReminders: ArrayList<Note>) {
        if (newReminders.size == 0) {
            reminders.clear()
            notifyDataSetChanged()

            return
        }

        val notesWithoutDuplicates = arrayListOf<Note>()
        newReminders.forEach {
            if (!Note.arrayContainsNote(it, notesWithoutDuplicates)) {
                if (System.currentTimeMillis() < it.reminderTimestamp) {
                    notesWithoutDuplicates.add(it)
                }
            }
        }

        notesWithoutDuplicates.sortWith(compareBy { it.title })

        val diffResult = DiffUtil.calculateDiff(DiffUtilNoteImplementation(reminders, notesWithoutDuplicates))
        reminders.clear()
        reminders.addAll(notesWithoutDuplicates)
        diffResult.dispatchUpdatesTo(this)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleTextView = itemView.findViewById<TextView>(R.id.reminderItemTitleTextView)!!
        val timeTextView = itemView.findViewById<TextView>(R.id.reminderItemTimeTextView)!!
    }
}