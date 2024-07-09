package it.matteolobello.lumine.ui.activity

import android.content.Intent
import android.text.TextUtils
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import it.matteolobello.lumine.R
import it.matteolobello.lumine.data.bundle.BundleKeys
import it.matteolobello.lumine.data.local.LocalNotesManager
import it.matteolobello.lumine.data.model.Note
import it.matteolobello.lumine.extension.changeAlpha
import it.matteolobello.lumine.extension.fetchAccentColor
import it.matteolobello.lumine.extension.setNavigationBarColor
import it.matteolobello.lumine.extension.setStatusBarColor
import it.matteolobello.lumine.ui.activity.base.BaseActivity
import it.matteolobello.lumine.ui.view.dialog.GenericDialogView
import it.matteolobello.lumine.ui.view.dialog.NewCategoryDialogView
import kotlinx.android.synthetic.main.activity_task.*
import java.util.*

class TaskActivity : BaseActivity() {

    private val categories: ArrayList<String> by lazy {
        intent.getStringArrayListExtra(BundleKeys.EXTRA_CATEGORIES)
    }

    private val originalNote: Note? by lazy {
        intent.getParcelableExtra(BundleKeys.EXTRA_NOTE) as Note?
    }

    private val isEditMode: Boolean by lazy {
        originalNote != null
    }

    private lateinit var newNote: Note

    override fun layoutRes() = R.layout.activity_task

    override fun setupUi() {
        newNote = if (isEditMode) {
            originalNote!!
        } else {
            Note()
        }

        setNavigationBarColor(ContextCompat.getColor(this, R.color.defaultNewNoteBackgroundColor))
        setStatusBarColor(ContextCompat.getColor(this, R.color.defaultNewNoteBackgroundColor))

        backArrowImageView.setOnClickListener { onBackPressed() }

        taskCategoryWrapper.setCardBackgroundColor(fetchAccentColor().changeAlpha(0.15f))
        taskNewCategoryButton.setCardBackgroundColor(fetchAccentColor().changeAlpha(0.20f))

        categories.sort()

        if (isEditMode) {
            taskCategoryNameTextView.text = newNote.category
        }

        taskCategoryWrapper.setOnClickListener {
            if (categories.size == 0) {
                showNewCategoryDialog()
            } else {
                startActivityForResult(Intent(this, TaskCategorySelectionActivity::class.java),
                        TaskCategorySelectionActivity.RC_CATEGORY)
            }
        }

        taskNewCategoryButton.setOnClickListener { _ ->
            showNewCategoryDialog()
        }

        if (isEditMode) {
            taskTitleEditText.setText(originalNote!!.title)
        }

        newTaskFab.setOnClickListener {
            val categoryName = taskCategoryNameTextView.text
            if (categoryName == getString(R.string.no_categories) || categoryName == getString(R.string.choose_category)) {
                Snackbar.make(newTaskRootLayout, R.string.choose_valid_category, Snackbar.LENGTH_LONG).show()
                return@setOnClickListener
            }

            val category = categoryName.toString()

            val title = taskTitleEditText.text.toString()
            if (TextUtils.isEmpty(title)) {
                Snackbar.make(newTaskRootLayout, R.string.insert_text, Snackbar.LENGTH_LONG).show()
                return@setOnClickListener
            }

            newNote.id = if (isEditMode)
                newNote.id
            else
                Note.generateNewId(this)

            newNote.title = title

            newNote.type = Note.TYPE_TASK
            newNote.category = category
            newNote.lastEditTimeStamp = System.currentTimeMillis().toDouble()

            with(LocalNotesManager.get()) {
                if (isEditMode) {
                    editNote(applicationContext, originalNote!!, newNote)
                } else {
                    insertNewNote(applicationContext, newNote)
                }
            }

            finish()
        }
    }

    override fun onBackPressed() {
        val dialog = GenericDialogView.findDialogInActivity(this)
        if (dialog != null) {
            dialog.dismiss()
        } else {
            super.onBackPressed()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == TaskCategorySelectionActivity.RC_CATEGORY && data != null) {
            data.getStringExtra(BundleKeys.EXTRA_CATEGORY_NAME)?.let { categoryName ->
                categories.remove(getString(R.string.no_categories))
                categories.add(categoryName)
                categories.sort()

                taskCategoryNameTextView.text = categoryName
            }
        }
    }

    private fun showNewCategoryDialog() {
        NewCategoryDialogView.create(
                activity = this,
                onPositiveButtonClickListener = { dialogView, categoryName ->
                    val categoryName = categoryName.toUpperCase()

                    categories.remove(getString(R.string.no_categories))
                    categories.add(categoryName)
                    categories.sort()

                    taskCategoryNameTextView.text = categoryName

                    dialogView.dismiss()
                },
                onNegativeButtonClickListener = {
                    it.dismiss()
                }
        )
    }
}