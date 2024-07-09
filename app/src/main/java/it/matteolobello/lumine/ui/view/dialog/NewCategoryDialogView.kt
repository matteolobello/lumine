package it.matteolobello.lumine.ui.view.dialog

import android.app.Activity
import android.content.Context
import android.text.TextUtils
import android.util.AttributeSet
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import com.google.android.material.textfield.TextInputLayout
import it.matteolobello.lumine.R

class NewCategoryDialogView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : GenericDialogView(context, attrs, defStyleAttr) {

    companion object {

        fun create(
                activity: Activity,
                onPositiveButtonClickListener: (dialogView: NewCategoryDialogView, categoryName: String) -> Unit,
                onNegativeButtonClickListener: (dialogView: NewCategoryDialogView) -> Unit) {
            val dialogView = NewCategoryDialogView(activity)
            val rootLayout = dialogView.generateWithLayoutRes(activity, R.layout.dialog_new_category)

            val dialogRenameCategoryNameEditText = rootLayout.findViewById<EditText>(R.id.dialogNewCategoryNameEditText)
            val dialogRenameCategoryNameTextInputLayout = rootLayout.findViewById<TextInputLayout>(R.id.dialogNewCategoryNameTextInputLayout)
            val positiveButton = rootLayout.findViewById<Button>(R.id.dialogNewCategoryPositiveButton)
            val negativeButton = rootLayout.findViewById<Button>(R.id.dialogNewCategoryNegativeButton)

            positiveButton.setOnClickListener {
                val categoryName: String? = dialogRenameCategoryNameEditText.text?.toString()
                if (TextUtils.isEmpty(categoryName) || categoryName == activity.getString(R.string.no_categories)) {
                    dialogRenameCategoryNameTextInputLayout.error = activity.getString(R.string.category_name_not_valid)
                } else {
                    onPositiveButtonClickListener(dialogView, categoryName!!)
                }
            }

            negativeButton.setOnClickListener {
                onNegativeButtonClickListener(dialogView)
            }

            activity.findViewById<ViewGroup>(android.R.id.content).addView(dialogView)

            dialogView.setOnClickListener {
                dialogView.dismiss()
            }

            dialogView.addInActivityRootViewAndAnimateIn(activity)
        }
    }
}