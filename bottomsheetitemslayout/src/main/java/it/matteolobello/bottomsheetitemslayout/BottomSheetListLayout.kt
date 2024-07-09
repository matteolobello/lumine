package it.matteolobello.bottomsheetitemslayout

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton
import it.matteolobello.circleimageview.CircleImageView

class BottomSheetListLayout : LinearLayout {

    private lateinit var rootView: RelativeLayout
    lateinit var profilePicImageView: CircleImageView
    lateinit var bottomSheetButton: MaterialButton

    private var bottomSheetItems: HashMap<BottomSheetMenuItem, CardView> = hashMapOf()

    var highlightSelection = false

    var onBottomItemSelected: (bottomSheetMenuItem: BottomSheetMenuItem) -> Unit = {}

    var nameSurname: String? = null
        set(newNameSurname) {
            field = newNameSurname

            rootView.findViewById<TextView>(R.id.bottomSheetNameSurnameTextView).text = field
        }

    var email: String? = null
        set(newEmail) {
            field = newEmail

            rootView.findViewById<TextView>(R.id.bottomSheetEmailTextView).text = field
        }

    constructor(context: Context) : super(context) {
        initialize()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        initialize()
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        initialize()
    }

    private fun initialize() {
        orientation = LinearLayout.VERTICAL

        rootView = LayoutInflater.from(context).inflate(R.layout.bottom_sheet_list,
                this, false) as RelativeLayout
        profilePicImageView = rootView.findViewById(R.id.bottomSheetProfilePicImageView)
        bottomSheetButton = rootView.findViewById(R.id.bottomSheetButton)

        addView(rootView)
    }

    fun addItem(bottomSheetMenuItem: BottomSheetMenuItem) {
        val itemsRootView = rootView.findViewById<LinearLayout>(R.id.bottomSheetItemsRootView)

        val itemRootView = LayoutInflater.from(context).inflate(R.layout.bottom_sheet_list_item,
                itemsRootView, false) as CardView

        itemsRootView.addView(itemRootView)

        itemRootView.findViewById<ImageView>(R.id.bottomSheetItemImageView).setImageResource(bottomSheetMenuItem.iconRes)
        itemRootView.findViewById<TextView>(R.id.bottomSheetItemTitleTextView).text = bottomSheetMenuItem.title
        itemRootView.setOnClickListener { _ ->
            handleItemSelection(bottomSheetMenuItem)

            onBottomItemSelected(bottomSheetMenuItem)
        }

        bottomSheetItems[bottomSheetMenuItem] = itemRootView
    }

    private fun unselectItems() {
        bottomSheetItems.forEach {
            it.value.findViewById<AppCompatImageView>(R.id.bottomSheetItemImageView).imageTintList =
                    ColorStateList.valueOf(ContextCompat.getColor(context, R.color.unselectedBottomNavigationMenuItem))
            it.value.findViewById<TextView>(R.id.bottomSheetItemTitleTextView).setTextColor(
                    ColorStateList.valueOf(ContextCompat.getColor(context, R.color.unselectedBottomNavigationMenuItem)))
            it.value.setCardBackgroundColor(Color.TRANSPARENT)
        }
    }

    private fun selectItem(bottomSheetMenuItem: BottomSheetMenuItem) {
        bottomSheetItems.forEach {
            if (it.key == bottomSheetMenuItem) {
                it.value.findViewById<AppCompatImageView>(R.id.bottomSheetItemImageView).imageTintList =
                        ColorStateList.valueOf(fetchAccentColor())
                it.value.findViewById<TextView>(R.id.bottomSheetItemTitleTextView).setTextColor(
                        ColorStateList.valueOf(fetchAccentColor()))

                val selectionColorWithTransparency = Color.parseColor("#33" +
                        fetchAccentColor().toHex().replace("#", ""))
                it.value.setCardBackgroundColor(selectionColorWithTransparency)
            }
        }
    }

    fun handleItemSelection(bottomSheetMenuItem: BottomSheetMenuItem) {
        unselectItems()

        if (highlightSelection) {
            selectItem(bottomSheetMenuItem)
        }
    }

    data class BottomSheetMenuItem(val iconRes: Int, val title: String,
                                   var isSelected: Boolean = false)

    private fun Int.toHex() = String.format("#%06X", 0xFFFFFF and this)

    private fun fetchAccentColor(): Int {
        val typedValue = TypedValue()

        val styledAttribute = context.obtainStyledAttributes(typedValue.data, intArrayOf(R.attr.colorAccent))
        val color = styledAttribute.getColor(0, 0)

        styledAttribute.recycle()

        return color
    }
}