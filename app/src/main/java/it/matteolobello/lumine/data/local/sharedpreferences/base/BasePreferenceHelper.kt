package it.matteolobello.lumine.data.local.sharedpreferences.base

import android.content.Context
import android.content.SharedPreferences

abstract class BasePreferenceHelper {

    abstract fun getFileName(): String

    private var sharedPreferences: SharedPreferences? = null

    fun setValue(context: Context, key: String, value: String) =
            getSharedPreferences(context)
                    .edit()
                    .putString(key, value)
                    .apply()

    fun getValue(context: Context, key: String): String? = getSharedPreferences(context).getString(key, null)

    fun remove(context: Context, key: String) = getSharedPreferences(context).edit().remove(key).apply()

    fun all(context: Context): MutableMap<String, *>? = getSharedPreferences(context).all

    fun clearAll(context: Context) = getSharedPreferences(context).edit().clear().apply()

    private fun getSharedPreferences(context: Context): SharedPreferences {
        if (sharedPreferences == null) {
            sharedPreferences = context.getSharedPreferences(getFileName(), Context.MODE_PRIVATE)
        }

        return sharedPreferences!!
    }
}