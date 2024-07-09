package it.matteolobello.lumine.ui.view

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet

import androidx.appcompat.widget.AppCompatEditText
import it.matteolobello.lumine.R

class EditTextWithBullets : AppCompatEditText {

    var withBullets = false
        set(newValue) {
            post { hint = if (newValue) "• " else context.getString(R.string.write_something) }
            field = newValue
        }

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    init {
        addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(editable: Editable?) {
            }

            override fun beforeTextChanged(charSequence: CharSequence?, start: Int, lengthBefore: Int, lengthAfter: Int) {
            }

            override fun onTextChanged(text: CharSequence, start: Int, lengthBefore: Int, lengthAfter: Int) {
                if (withBullets) {
                    var text = text
                    if (lengthAfter > lengthBefore) {
                        if (text.toString().length == 1) {
                            text = "• $text"
                            setText(text)
                            setSelection(getText()!!.length)
                        }
                        if (text.toString().endsWith("\n")) {
                            text = text.toString().replace("\n", "\n• ")
                            text = text.toString().replace("• •", "•")
                            setText(text)
                            setSelection(getText()!!.length)
                        }
                    }
                }
            }
        })
    }
}