package it.matteolobello.lumine.ui.fragment.home

import android.content.Context
import android.content.Intent
import android.os.Vibrator
import android.view.View
import android.widget.PopupMenu
import androidx.lifecycle.Observer
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import it.matteolobello.lumine.R
import it.matteolobello.lumine.data.local.LocalNotesManager
import it.matteolobello.lumine.data.model.Note
import it.matteolobello.lumine.data.receiver.ReceiverIntentFilterActions
import it.matteolobello.lumine.extension.fixTabTextViewsLetterSpacingAndAddMoreMargin
import it.matteolobello.lumine.extension.setOnTabItemLongClickListener
import it.matteolobello.lumine.ui.adapter.viewpager.TasksViewPagerAdapter
import it.matteolobello.lumine.ui.fragment.base.HomeFragment
import it.matteolobello.lumine.ui.view.dialog.RenameCategoryDialogView
import kotlinx.android.synthetic.main.fragment_tasks.*

class TasksFragment : HomeFragment() {

    override fun getLayoutRes() = R.layout.fragment_tasks

    override fun setupUi(rootView: View) {
        homeActivity.viewModel.tasks.observe(homeActivity, Observer {
            if (it.size == 0) {
                tasksContentWrapper.visibility = View.INVISIBLE
                emptyTasksWrapper.visibility = View.VISIBLE
            } else {
                tasksContentWrapper.visibility = View.VISIBLE
                emptyTasksWrapper.visibility = View.INVISIBLE

                setupViewPager(it)
            }
        })
    }

    private fun setupViewPager(tasks: ArrayList<Note>) {
        tasksViewPager.offscreenPageLimit = 0
        tasksViewPager.adapter = TasksViewPagerAdapter(this, tasks)
        tasksCategoriesTabLayout.setupWithViewPager(tasksViewPager)
        tasksCategoriesTabLayout.tabRippleColor = null
        tasksCategoriesTabLayout.fixTabTextViewsLetterSpacingAndAddMoreMargin()
        tasksCategoriesTabLayout.setOnTabItemLongClickListener { view, categoryName ->
            val popupMenu = PopupMenu(view.context, view)
            popupMenu.inflate(R.menu.task_categories_popup_menu)
            popupMenu.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.action_delete_category -> {
                        val notes = LocalNotesManager.get().loadNotes(homeActivity)
                        notes.forEach {
                            if (it.category.toUpperCase() == categoryName.toUpperCase()) {
                                LocalNotesManager.get().removeNote(homeActivity, it, false)
                            }
                        }

                        LocalBroadcastManager.getInstance(homeActivity)
                                .sendBroadcast(Intent(ReceiverIntentFilterActions.ACTION_LOCAL_DATA_CHANGE))
                    }
                    R.id.action_rename_category -> {
                        RenameCategoryDialogView.create(
                                activity = homeActivity,
                                onPositiveButtonClickListener = { dialog, newCategoryName ->
                                    LocalNotesManager.get().renameCategory(homeActivity, categoryName, newCategoryName)

                                    dialog.dismiss()
                                },
                                onNegativeButtonClickListener = {
                                    it.dismiss()
                                },
                                previousCategoryName = categoryName
                        )
                    }
                }

                return@setOnMenuItemClickListener false
            }
            popupMenu.show()

            (homeActivity.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator).vibrate(7)
        }
    }
}