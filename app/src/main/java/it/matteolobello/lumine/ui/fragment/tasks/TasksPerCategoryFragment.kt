package it.matteolobello.lumine.ui.fragment.tasks

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import it.matteolobello.lumine.R
import it.matteolobello.lumine.data.bundle.BundleKeys
import it.matteolobello.lumine.data.model.Note
import it.matteolobello.lumine.ui.adapter.recyclerview.TasksPerCategoryRecyclerViewAdapter
import it.matteolobello.lumine.ui.fragment.base.HomeFragment
import kotlinx.android.synthetic.main.fragment_tasks_per_category.*

class TasksPerCategoryFragment : HomeFragment() {

    companion object {
        fun newInstance(allTaskCategories: ArrayList<String>, categoryName: String, tasksOfThatCategory: ArrayList<Note>) = TasksPerCategoryFragment().apply {
            arguments = Bundle().apply {
                putStringArrayList(BundleKeys.EXTRA_CATEGORIES, allTaskCategories)
                putString(BundleKeys.EXTRA_CATEGORY_NAME, categoryName)
                putParcelableArrayList(BundleKeys.EXTRA_TASKS, tasksOfThatCategory)
            }
            retainInstance = true
        }
    }

    val allTaskCategories: ArrayList<String> by lazy { arguments!!.getStringArrayList(BundleKeys.EXTRA_CATEGORIES) }
    val categoryName: String by lazy { arguments!!.getString(BundleKeys.EXTRA_CATEGORY_NAME) }
    val tasks: ArrayList<Note> by lazy { arguments!!.getParcelableArrayList<Note>(BundleKeys.EXTRA_TASKS) }

    override fun getLayoutRes() = R.layout.fragment_tasks_per_category

    override fun setupUi(rootView: View) {
        val adapter = TasksPerCategoryRecyclerViewAdapter(allTaskCategories)
        adapter.setTasks(tasks)

        tasksPerCategoryRecyclerView.adapter = adapter
        tasksPerCategoryRecyclerView.layoutManager = LinearLayoutManager(context)
    }
}