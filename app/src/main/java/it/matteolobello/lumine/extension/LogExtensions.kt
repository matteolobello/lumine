package it.matteolobello.lumine.extension

import android.util.Log
import it.matteolobello.lumine.BuildConfig

const val TAG = "Lumine"

fun log(vararg whatToLog: Any?) {
    if (BuildConfig.DEBUG) {
        if (whatToLog.isEmpty()) {
            Log.d(TAG, " ")
        } else {
            whatToLog.forEach {
                Log.d(TAG, it.toString())
            }
        }
    }
}