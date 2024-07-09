package it.matteolobello.lumine.data.local

import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.gson.Gson
import it.matteolobello.lumine.data.local.sharedpreferences.NotesPreferenceManager
import it.matteolobello.lumine.data.model.Note
import it.matteolobello.lumine.data.receiver.ReceiverIntentFilterActions
import java.lang.ref.WeakReference

/**
 * Structure:
 *  - Key   -> ID
 *  - Value -> Note object saved as JSON
 */
class LocalNotesManager {

    companion object {

        @Volatile
        private var INSTANCE: LocalNotesManager? = null

        fun get(): LocalNotesManager =
                INSTANCE ?: synchronized(this) {
                    INSTANCE ?: LocalNotesManager().also { INSTANCE = it }
                }
    }

    fun loadNotes(context: Context): ArrayList<Note> {
        val notes = arrayListOf<Note>()

        with(NotesPreferenceManager.get()) {
            all(context)?.forEach {
                notes.add(Gson().fromJson(it.value.toString(), Note::class.java))
            }
        }

        return notes
    }

    fun insertNewNote(context: Context, note: Note, broadcastUpdate: Boolean = true) {
        NotesPreferenceManager.get().setValue(context, note.id, Gson().toJson(note))

        if (broadcastUpdate) {
            LocalBroadcastManager.getInstance(context)
                    .sendBroadcast(Intent(ReceiverIntentFilterActions.ACTION_LOCAL_DATA_CHANGE))
        }
    }

    fun removeNote(context: Context, note: Note, broadcastUpdate: Boolean = true) {
        with(NotesPreferenceManager.get()) {
            all(context)?.forEach {
                val cachedNoteKey = it.key

                if (cachedNoteKey == note.id) {
                    remove(context, it.key)
                }
            }
        }

        if (broadcastUpdate) {
            LocalBroadcastManager.getInstance(context)
                    .sendBroadcast(Intent(ReceiverIntentFilterActions.ACTION_LOCAL_DATA_CHANGE))
        }
    }

    fun editNote(context: Context, oldNote: Note, newNote: Note, broadcastUpdate: Boolean = true) {
        with(NotesPreferenceManager.get()) {
            all(context)?.forEach {
                val cachedNoteKey = it.key

                if (cachedNoteKey == oldNote.id) {
                    setValue(context, cachedNoteKey, Gson().toJson(newNote))
                }
            }
        }

        if (broadcastUpdate) {
            LocalBroadcastManager.getInstance(context)
                    .sendBroadcast(Intent(ReceiverIntentFilterActions.ACTION_LOCAL_DATA_CHANGE))
        }
    }

    fun editMultipleNotes(context: Context, newNotes: ArrayList<Note>, broadcastUpdate: Boolean = true) {
        with(NotesPreferenceManager.get()) {
            all(context)?.forEach {
                val cachedNoteKey = it.key

                newNotes.forEach { newNote ->
                    if (cachedNoteKey == newNote.id) {
                        setValue(context, cachedNoteKey, Gson().toJson(newNote))
                    }
                }
            }
        }

        if (broadcastUpdate) {
            LocalBroadcastManager.getInstance(context)
                    .sendBroadcast(Intent(ReceiverIntentFilterActions.ACTION_LOCAL_DATA_CHANGE))
        }
    }

    fun loadNotesAsync(context: Context, callback: (notes: ArrayList<Note>) -> Unit) {
        LoadLocalNotesAsyncTask(WeakReference(context)) {
            callback(it)
        }.execute()
    }

    fun renameCategory(context: Context, oldCategoryName: String, newCategoryName: String, broadcastUpdate: Boolean = true) {
        with(NotesPreferenceManager.get()) {
            all(context)?.forEach {
                val oldNote = Gson().fromJson(it.value.toString(), Note::class.java)
                if (oldNote.category.toUpperCase() == oldCategoryName.toUpperCase()) {
                    val newNote = oldNote
                    newNote.category = newCategoryName

                    editNote(context, oldNote, newNote, false)
                }
            }
        }

        if (broadcastUpdate) {
            LocalBroadcastManager.getInstance(context)
                    .sendBroadcast(Intent(ReceiverIntentFilterActions.ACTION_LOCAL_DATA_CHANGE))
        }
    }

    fun emptyTrash(context: Context) {
        val notes = loadNotes(context)
        notes.forEach {
            if (it.isTrashed) {
                removeNote(context, it)
            }
        }
    }

    fun clearAll(context: Context) {
        val notes = loadNotes(context)
        notes.forEach {
            removeNote(context, it)
        }
    }

    private class LoadLocalNotesAsyncTask(
            private val weakRefContext: WeakReference<Context>,
            private val callback: (notes: ArrayList<Note>) -> Unit) : AsyncTask<Unit, Unit, ArrayList<Note>>() {

        override fun doInBackground(vararg unit: Unit) =
                LocalNotesManager.get().loadNotes(weakRefContext.get()!!)

        override fun onPostExecute(result: ArrayList<Note>) {
            super.onPostExecute(result)

            callback(result)
        }
    }
}