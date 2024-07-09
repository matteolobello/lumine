package it.matteolobello.lumine.ui.adapter.viewpager

import androidx.fragment.app.FragmentStatePagerAdapter
import it.matteolobello.lumine.data.model.Note
import it.matteolobello.lumine.ui.fragment.home.TasksFragment
import it.matteolobello.lumine.ui.fragment.tasks.TasksPerCategoryFragment
import java.util.LinkedHashSet
import kotlin.collections.ArrayList

class TasksViewPagerAdapter(rootFragment: TasksFragment, tasks: ArrayList<Note>?) : FragmentStatePagerAdapter(rootFragment.childFragmentManager) {

    private val fragments = arrayListOf<TasksPerCategoryFragment>()

    init {
        val allTaskCategoriesAsLinkedHashSet = LinkedHashSet<String>()

        tasks?.forEach {
            if (!it.isTrashed) {
                allTaskCategoriesAsLinkedHashSet.add(it.category)
            }
        }

        val allTaskCategories = ArrayList(allTaskCategoriesAsLinkedHashSet)

        if (tasks != null && tasks.size > 0) {
            tasks.groupBy {
                it.category.toUpperCase()
            }.toSortedMap(compareBy { it.toUpperCase() }).forEach {
                val categoryName = it.key!!
                val tasksOfThatCategory = ArrayList(it.value)

                fragments.add(TasksPerCategoryFragment.newInstance(allTaskCategories,
                        categoryName, tasksOfThatCategory))
            }
        }
    }

    override fun getItem(position: Int) = fragments[position]

    override fun getCount() = fragments.size

    override fun getPageTitle(position: Int) = fragments[position].categoryName
}