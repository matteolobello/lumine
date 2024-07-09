package it.matteolobello.lumine.ui.view

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import it.matteolobello.lumine.R

class CheckBox : AppCompatImageView {

    var onCheckChange: (isChecked: Boolean) -> Unit = {}

    var isChecked = false
        set(value) {
            setImageResource(
                    if (value) R.drawable.twotone_checked_24px
                    else R.drawable.twotone_not_checked_24px
            )

            field = value
        }

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    init {
        setImageResource(R.drawable.twotone_not_checked_24px)

        setOnClickListener {
            isChecked = !isChecked

            onCheckChange(isChecked)
        }
    }
}