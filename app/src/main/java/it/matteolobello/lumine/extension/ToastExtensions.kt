package it.matteolobello.lumine.extension

import android.content.Context
import android.view.Gravity
import android.view.View
import android.widget.Toast

fun Context.showToast(view: View? = null, gravity: Int = Gravity.END, message: String, length: Int = Toast.LENGTH_SHORT) {
    if (view != null) {
        val x = view.left
        val y = view.top + 2 * view.height
        val toast = Toast.makeText(this, message, length)
        toast.setGravity(Gravity.TOP or gravity, x, y)
        toast.show()
    } else {
        Toast.makeText(this, message, length).show()
    }
}