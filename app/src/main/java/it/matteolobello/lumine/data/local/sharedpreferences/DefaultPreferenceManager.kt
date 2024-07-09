package it.matteolobello.lumine.data.local.sharedpreferences

import it.matteolobello.lumine.data.local.sharedpreferences.base.BasePreferenceHelper

class DefaultPreferenceManager : BasePreferenceHelper() {

    companion object {

        const val KEY_LOGIN_SKIP = "login_skip"

        private var INSTANCE: DefaultPreferenceManager? = null

        fun getInstance(): DefaultPreferenceManager {
            if (INSTANCE == null) {
                INSTANCE = DefaultPreferenceManager()
            }

            return INSTANCE!!
        }
    }


    override fun getFileName() = "default"
}