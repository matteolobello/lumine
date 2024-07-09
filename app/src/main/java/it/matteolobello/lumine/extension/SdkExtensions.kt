package it.matteolobello.lumine.extension

import android.content.Context
import android.os.Build

fun Context.isRunningAtLeastM() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M

fun Context.isRunningAtLeastO() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O