package it.matteolobello.lumine.viewmodel

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import it.matteolobello.lumine.data.local.LocalNotesManager
import it.matteolobello.lumine.data.model.Note
import it.matteolobello.lumine.extension.log

class HomeViewModel : ViewModel() {

    private val allNotes: MutableLiveData<ArrayList<Note>> = MutableLiveData()

    val notes: MutableLiveData<ArrayList<Note>> = MutableLiveData()
    val tasks: MutableLiveData<ArrayList<Note>> = MutableLiveData()
    val reminders: MutableLiveData<ArrayList<Note>> = MutableLiveData()

    fun loadUserNotes(context: Context) {
        allNotes.value = LocalNotesManager.get().loadNotes(context)

        updateNotesList()
        updateTasksList()
        updateRemindersList()
    }

    fun updateLocalNotesWithoutReloading(notes: ArrayList<Note>) {
        allNotes.value = notes

        updateNotesList()
        updateTasksList()
        updateRemindersList()
    }

    private fun updateNotesList() {
        val updatedNotesList = arrayListOf<Note>()

        allNotes.value?.forEach {
            if (it.type == Note.TYPE_PLAIN_TEXT || it.type == Note.TYPE_LIST) {
                updatedNotesList.add(it)
            }
        }

        notes.value = updatedNotesList
    }

    private fun updateTasksList() {
        val updatedTasksList = arrayListOf<Note>()

        allNotes.value?.forEach {
            if (it.type == Note.TYPE_TASK) {
                updatedTasksList.add(it)
            }
        }

        tasks.value = updatedTasksList
    }

    private fun updateRemindersList() {
        val updatedRemindersList = arrayListOf<Note>()

        allNotes.value?.forEach {
            if (it.type == Note.TYPE_REMINDER) {
                log("Found reminder -> $it")
                updatedRemindersList.add(it)
            }
        }

        reminders.value = updatedRemindersList
    }
}