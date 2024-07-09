package it.matteolobello.lumine.ui.adapter.recyclerview

import android.animation.ValueAnimator
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Vibrator
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import it.matteolobello.lumine.R
import it.matteolobello.lumine.data.bundle.BundleKeys
import it.matteolobello.lumine.data.local.LocalNotesManager
import it.matteolobello.lumine.data.model.DiffUtilNoteImplementation
import it.matteolobello.lumine.data.model.Note
import it.matteolobello.lumine.extension.changeAlpha
import it.matteolobello.lumine.extension.evaluateColors
import it.matteolobello.lumine.extension.fetchAccentColor
import it.matteolobello.lumine.ui.activity.TaskActivity
import it.matteolobello.lumine.ui.view.CheckBox
import java.util.*

class TasksPerCategoryRecyclerViewAdapter(private val allTaskCategories: ArrayList<String>)
    : RecyclerView.Adapter<TasksPerCategoryRecyclerViewAdapter.ViewHolder>() {

    val tasks = arrayListOf<Note>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_task, parent, false))

    override fun getItemCount() = tasks.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val task = tasks[position]

        val colorAccent = holder.checkBox.context.fetchAccentColor()

        holder.titleTextView.text = task.title
        holder.checkBox.isChecked = task.isMarkedAsDone

        holder.checkBox.imageTintList = ColorStateList.valueOf(colorAccent)
        holder.checkBox.onCheckChange = { isChecked ->
            val newTask = task
            newTask.isMarkedAsDone = isChecked

            LocalNotesManager.get().editNote(holder.checkBox.context, task, newTask, false)

            val startColorBg = if (newTask.isMarkedAsDone) {
                ContextCompat.getColor(holder.checkBox.context, R.color.taskNotMarkedAsDoneBgColor)
            } else {
                colorAccent.changeAlpha(0.15f)
            }

            val endColorBg = if (newTask.isMarkedAsDone) {
                colorAccent.changeAlpha(0.15f)
            } else {
                ContextCompat.getColor(holder.checkBox.context, R.color.taskNotMarkedAsDoneBgColor)
            }

            ValueAnimator().evaluateColors({
                (holder.itemView as CardView).setCardBackgroundColor(it)
            }, startColorBg, endColorBg)
        }

        val cardBackgroundColor = if (task.isMarkedAsDone) {
            colorAccent.changeAlpha(0.15f)
        } else {
            ContextCompat.getColor(holder.checkBox.context, R.color.taskNotMarkedAsDoneBgColor)
        }

        (holder.itemView as CardView).setCardBackgroundColor(cardBackgroundColor)

        holder.itemView.setOnClickListener { view ->
            val categories = LinkedHashSet<String>()

            tasks.forEach {
                if (!it.isTrashed) {
                    categories.add(it.category)
                }
            }

            view.context.startActivity(Intent(view.context, TaskActivity::class.java)
                    .putExtra(BundleKeys.EXTRA_NOTE, task)
                    .putExtra(BundleKeys.EXTRA_CATEGORIES, allTaskCategories))
        }

        holder.itemView.setOnLongClickListener { view ->
            val popupMenu = PopupMenu(view.context, view)
            popupMenu.inflate(R.menu.task_item_popup_menu)
            popupMenu.setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.action_delete_task -> {
                        LocalNotesManager.get().removeNote(view.context, task)
                    }
                }

                return@setOnMenuItemClickListener false
            }
            popupMenu.show()

            (view.context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator).vibrate(7)

            false
        }
    }

    fun setTasks(newTasks: ArrayList<Note>) {
        if (newTasks.size == 0) {
            tasks.clear()
            notifyDataSetChanged()

            return
        }

        val notesWithoutDuplicates = arrayListOf<Note>()
        newTasks.forEach {
            if (!Note.arrayContainsNote(it, notesWithoutDuplicates)) {
                notesWithoutDuplicates.add(it)
            }
        }

        notesWithoutDuplicates.sortWith(compareBy { it.title })

        val diffResult = DiffUtil.calculateDiff(DiffUtilNoteImplementation(tasks, notesWithoutDuplicates))
        tasks.clear()
        tasks.addAll(notesWithoutDuplicates)
        diffResult.dispatchUpdatesTo(this)
    }

    class ViewHolder(rootView: View) : RecyclerView.ViewHolder(rootView) {
        val checkBox = rootView.findViewById<CheckBox>(R.id.taskItemCheckBox)!!
        val titleTextView = rootView.findViewById<TextView>(R.id.taskItemTitleTextView)!!
    }
}