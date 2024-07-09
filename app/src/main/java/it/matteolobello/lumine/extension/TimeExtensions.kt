package it.matteolobello.lumine.extension

import android.content.Context
import android.text.format.DateUtils
import it.matteolobello.lumine.R

fun Double.getRelativeTimeDisplayString(context: Context): CharSequence {
    val now = System.currentTimeMillis()
    val difference = now - this
    return if (difference >= 0 && difference <= DateUtils.MINUTE_IN_MILLIS)
        context.resources.getString(R.string.less_than_a_min_ago)
    else
        DateUtils.getRelativeTimeSpanString(this.toLong(), now, DateUtils.MINUTE_IN_MILLIS,
                DateUtils.FORMAT_ABBREV_RELATIVE).toString()
}

fun String.addZeroIfNeeded() = if (this.length == 1) "0$this" else this