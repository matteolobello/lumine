package it.matteolobello.lumine.ui.view.dialog

import android.app.Activity
import android.content.Context
import android.text.TextUtils
import android.util.AttributeSet
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import it.matteolobello.lumine.R

class RenameCategoryDialogView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : GenericDialogView(context, attrs, defStyleAttr) {

    companion object {

        fun create(
                activity: Activity,
                onPositiveButtonClickListener: (renameCategoryDialogView: RenameCategoryDialogView, categoryName: String) -> Unit,
                onNegativeButtonClickListener: (renameCategoryDialogView: RenameCategoryDialogView) -> Unit,
                previousCategoryName: String) {
            val dialogView = RenameCategoryDialogView(activity)
            val rootLayout = dialogView.generateWithLayoutRes(activity, R.layout.dialog_rename_category)

            val dialogRenameCategoryNameEditText = rootLayout.findViewById<EditText>(R.id.dialogRenameCategoryNameEditText)
            val positiveButton = rootLayout.findViewById<Button>(R.id.dialogRenameCategoryPositiveButton)
            val negativeButton = rootLayout.findViewById<Button>(R.id.dialogRenameCategoryNegativeButton)

            dialogRenameCategoryNameEditText.setText(previousCategoryName)

            positiveButton.setOnClickListener {
                val categoryName: String? = dialogRenameCategoryNameEditText.text?.toString()
                if (TextUtils.isEmpty(categoryName) || categoryName == activity.getString(R.string.no_categories)) {
                    dialogRenameCategoryNameEditText.error = activity.getString(R.string.category_name_not_valid)
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