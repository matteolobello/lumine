package it.matteolobello.lumine.ui.activity

import android.app.Activity
import android.content.Intent
import android.text.TextUtils
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import it.matteolobello.lumine.R
import it.matteolobello.lumine.data.bundle.BundleKeys
import it.matteolobello.lumine.data.local.LocalNotesManager
import it.matteolobello.lumine.extension.setNavigationBarColor
import it.matteolobello.lumine.extension.setStatusBarColor
import it.matteolobello.lumine.ui.activity.base.BaseActivity
import it.matteolobello.lumine.ui.adapter.recyclerview.TaskCategorySelectionRecyclerViewAdapter
import it.matteolobello.lumine.ui.view.dialog.NewCategoryDialogView
import kotlinx.android.synthetic.main.activity_task_category_selection.*

class TaskCategorySelectionActivity : BaseActivity() {

    companion object {
        const val RC_CATEGORY = 731
    }

    override fun layoutRes() = R.layout.activity_task_category_selection

    override fun setupUi() {
        setNavigationBarColor(ContextCompat.getColor(this, android.R.color.white))
        setStatusBarColor(ContextCompat.getColor(this, android.R.color.white))

        val notes = LocalNotesManager.get().loadNotes(this)

        val categoriesWitNumTasksHashMap = linkedMapOf<String, Int>()

        notes.forEach {
            if (categoriesWitNumTasksHashMap[it.category] == null) {
                categoriesWitNumTasksHashMap[it.category] = 0
            }

            categoriesWitNumTasksHashMap[it.category] = categoriesWitNumTasksHashMap[it.category]!! + 1
        }

        val categoriesWitNumTasksArrayList = arrayListOf<Pair<String, Int>>()
        for ((key, value) in categoriesWitNumTasksHashMap) {
            if (!TextUtils.isEmpty(key)) {
                categoriesWitNumTasksArrayList.add(Pair(key, value))
            }
        }

        if (categoriesWitNumTasksArrayList.isEmpty()) {
            taskCategorySelectionRecyclerView.visibility = View.GONE
            taskCategorySelectionNoCategoriesTextView.visibility = View.VISIBLE
        } else {
            taskCategorySelectionRecyclerView.apply {
                layoutManager = LinearLayoutManager(this@TaskCategorySelectionActivity)
                adapter = TaskCategorySelectionRecyclerViewAdapter(categoriesWitNumTasksArrayList) {
                    setResult(Activity.RESULT_OK, Intent().putExtra(BundleKeys.EXTRA_CATEGORY_NAME, it))
                    finish()
                }
            }
        }

        taskCategorySelectionFab.setOnClickListener {
            NewCategoryDialogView.create(
                    activity = this,
                    onPositiveButtonClickListener = { _, categoryName ->
                        setResult(Activity.RESULT_OK, Intent().putExtra(BundleKeys.EXTRA_CATEGORY_NAME, categoryName))
                        finish()
                    },
                    onNegativeButtonClickListener = {
                        it.dismiss()
                    }
            )
        }

        taskCategorySelectionBackArrowImageView.setOnClickListener {
            finish()
        }
    }
}