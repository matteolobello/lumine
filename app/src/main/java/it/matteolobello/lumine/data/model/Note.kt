package it.matteolobello.lumine.data.model

import android.content.Context
import android.os.Parcelable
import androidx.recyclerview.widget.DiffUtil
import com.google.gson.Gson
import it.matteolobello.lumine.data.local.sharedpreferences.NotesPreferenceManager
import kotlinx.android.parcel.Parcelize
import java.util.*

@Parcelize
class Note(
        var id: String,
        var type: String,
        var title: String,
        var body: String,
        var images: ArrayList<String>,
        var isTrashed: Boolean,
        var isMarkedAsDone: Boolean,
        var category: String,
        var position: Int,
        var reminderTimestamp: Double,
        var lastEditTimeStamp: Double,
        var color: String,
        var isEveryDayReminder: Boolean
) : Parcelable {

    companion object {
        const val TYPE_PLAIN_TEXT = "PLAIN_TEXT"
        const val TYPE_LIST = "LIST"
        const val TYPE_REMINDER = "REMINDER"
        const val TYPE_TASK = "TASK"

        fun arrayContainsNote(note: Note, notes: ArrayList<Note>): Boolean {
            notes.forEach {
                if (it.id == note.id) {
                    return true
                }
            }

            return false
        }

        fun generateNewId(context: Context): String {
            val newId = UUID.randomUUID().toString()

            NotesPreferenceManager.get().all(context)?.keys?.forEach {
                if (it == newId) {
                    return generateNewId(context)
                }
            }

            return newId
        }
    }

    constructor() : this(
            id = "",
            type = "",
            title = "",
            body = "",
            images = arrayListOf<String>(),
            isTrashed = false,
            isMarkedAsDone = false,
            category = "",
            position = 0,
            reminderTimestamp = Double.MAX_VALUE,
            lastEditTimeStamp = Double.MIN_VALUE,
            color = "",
            isEveryDayReminder = false
    )

    override fun toString() = Gson().toJson(this).toString()
}

class DiffUtilNoteImplementation(
        private val oldNotes: ArrayList<Note>,
        private val newNotes: ArrayList<Note>) : DiffUtil.Callback() {

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int) =
            oldNotes[oldItemPosition].id == newNotes[newItemPosition].id

    override fun getOldListSize() = oldNotes.size

    override fun getNewListSize() = newNotes.size

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int) =
            oldNotes[oldItemPosition] == newNotes[newItemPosition]
}