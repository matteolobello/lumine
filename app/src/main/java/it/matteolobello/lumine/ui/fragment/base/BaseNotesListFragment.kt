package it.matteolobello.lumine.ui.fragment.base

import android.view.View
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import it.matteolobello.lumine.R
import it.matteolobello.lumine.data.local.LocalNotesManager
import it.matteolobello.lumine.extension.log
import it.matteolobello.lumine.ui.adapter.recyclerview.NotesRecyclerViewAdapter
import kotlinx.android.synthetic.main.fragment_notes.*

abstract class BaseNotesListFragment : HomeFragment() {

    abstract fun isTrashedNotes(): Boolean

    val recyclerView: RecyclerView by lazy { notesRecyclerView }

    override fun getLayoutRes() = R.layout.fragment_notes

    override fun setupUi(rootView: View) {
        notesRecyclerView.adapter = NotesRecyclerViewAdapter(rootView.context, isTrashedNotes())
        notesRecyclerView.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        setupScrollingView(notesRecyclerView)

        homeActivity.viewModel.notes.observe(this, Observer {
            val notesAdapter = notesRecyclerView.adapter as NotesRecyclerViewAdapter
            notesAdapter.setNotes(it)

            log("Number of displayed notes ${notesAdapter.itemCount}")
            if (notesAdapter.itemCount == 0) {
                notesContentWrapper.visibility = View.GONE
                emptyNotesWrapper.visibility = View.VISIBLE
            } else {
                notesContentWrapper.visibility = View.VISIBLE
                emptyNotesWrapper.visibility = View.GONE
            }
        })

        if (isTrashedNotes()) {
            noTrashedNotesTextView.visibility = View.VISIBLE

            nothingToSeeTextView.visibility = View.GONE
            emptyNotesTapThePencilTextView.visibility = View.GONE
            emptyNotesImageView.visibility = View.GONE
        }

        handleNotesDrag()
    }

    private fun handleNotesDrag() {
        val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.Callback() {

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            }

            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, targetViewHolder: RecyclerView.ViewHolder): Boolean {
                val notesAdapter = notesRecyclerView.adapter as NotesRecyclerViewAdapter

                val copyOfMovedItem = notesAdapter.notes[viewHolder.adapterPosition]
                notesAdapter.notes.removeAt(viewHolder.adapterPosition)
                notesAdapter.notes.add(targetViewHolder.adapterPosition, copyOfMovedItem)

                notesAdapter.notifyItemMoved(viewHolder.adapterPosition, targetViewHolder.adapterPosition)

                return true
            }

            override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
                super.clearView(recyclerView, viewHolder)

                val notesAdapter = notesRecyclerView.adapter as NotesRecyclerViewAdapter

                for (i in 0 until notesAdapter.notes.size) {
                    notesAdapter.notes[i].position = i
                }

                if (context != null) {
                    LocalNotesManager.get().editMultipleNotes(context!!, notesAdapter.notes)
                }
            }

            override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) =
                    makeFlag(ItemTouchHelper.ACTION_STATE_DRAG,
                            ItemTouchHelper.DOWN or ItemTouchHelper.UP or ItemTouchHelper.START or ItemTouchHelper.END)
        })
        itemTouchHelper.attachToRecyclerView(notesRecyclerView)
    }
}