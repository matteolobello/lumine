package it.matteolobello.lumine.data.local.sharedpreferences

import it.matteolobello.lumine.data.local.sharedpreferences.base.BasePreferenceHelper

class NotesPreferenceManager : BasePreferenceHelper() {

    companion object {

        private var INSTANCE: NotesPreferenceManager? = null

        fun get(): NotesPreferenceManager {
            if (INSTANCE == null) {
                INSTANCE = NotesPreferenceManager()
            }

            return INSTANCE!!
        }
    }

    override fun getFileName() = "notes"
}