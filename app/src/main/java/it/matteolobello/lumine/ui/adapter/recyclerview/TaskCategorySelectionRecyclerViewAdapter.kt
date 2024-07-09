package it.matteolobello.lumine.ui.adapter.recyclerview

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import it.matteolobello.lumine.R

class TaskCategorySelectionRecyclerViewAdapter(
        private val categoriesWithNumOfTasks: ArrayList<Pair<String, Int>>,
        private val onCategorySelection: (categoryName: String) -> Unit) : RecyclerView.Adapter<TaskCategorySelectionRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_task_cateogry, parent, false)
    )

    override fun getItemCount() = categoriesWithNumOfTasks.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val categoriesWithNumOfTask = categoriesWithNumOfTasks[position]
        holder.categoryNameTextView.text = categoriesWithNumOfTask.first

        val numTasks = "${categoriesWithNumOfTask.second}" + " " +
                holder.itemView.context.getString(
                        if (categoriesWithNumOfTask.second == 1) R.string.task
                        else R.string.tasks).toLowerCase()
        holder.tasksNumberTextView.text = numTasks

        holder.itemView.setOnClickListener {
            onCategorySelection(categoriesWithNumOfTask.first)
        }
    }

    class ViewHolder(rootView: View) : RecyclerView.ViewHolder(rootView) {
        val categoryNameTextView = rootView.findViewById<TextView>(R.id.taskCategorySelectionItemNameTextView)!!
        val tasksNumberTextView = rootView.findViewById<TextView>(R.id.taskCategorySelectionItemNumTasksTextView)!!
    }
}