package it.matteolobello.lumine.ui.adapter.recyclerview

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import it.matteolobello.lumine.R
import it.matteolobello.lumine.data.bundle.BundleKeys
import it.matteolobello.lumine.data.model.DiffUtilNoteImplementation
import it.matteolobello.lumine.data.model.Note
import it.matteolobello.lumine.extension.debug
import it.matteolobello.lumine.extension.getBrightness
import it.matteolobello.lumine.extension.log
import it.matteolobello.lumine.ui.activity.NoteActivity
import java.util.*
import kotlin.math.max

class NotesRecyclerViewAdapter(private val context: Context,
                               private val showOnlyTrashedNotes: Boolean) : RecyclerView.Adapter<NotesRecyclerViewAdapter.ViewHolder>() {

    val notes = arrayListOf<Note>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
            LayoutInflater.from(context).inflate(R.layout.item_note, parent, false))

    override fun getItemCount() = notes.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val note = notes[position]

        val cardBackgroundColor = Color.parseColor(note.color)
        holder.cardView.setCardBackgroundColor(cardBackgroundColor)

        holder.itemView.setOnClickListener {
            it.context.startActivity(Intent(it.context, NoteActivity::class.java)
                    .putExtra(BundleKeys.EXTRA_NOTE, note))
        }

        if (note.images.size == 0) {
            holder.noteImagesListWrapper.visibility = View.GONE

            holder.imagesRecyclerView.layoutManager = null
            holder.imagesRecyclerView.adapter = null
            holder.imagesRecyclerView.onFlingListener = null
        } else {
            holder.noteImagesListWrapper.visibility = View.VISIBLE

            holder.imagesRecyclerView.layoutManager = LinearLayoutManager(holder.itemView.context, LinearLayoutManager.HORIZONTAL, false)
            holder.imagesRecyclerView.adapter = ImagesRecyclerViewAdapter(note.images) {
                holder.itemView.callOnClick()
            }.apply { roundedCorners = false }
            holder.imagesRecyclerView.onFlingListener = null
            PagerSnapHelper().attachToRecyclerView(holder.imagesRecyclerView)

            holder.multipleImagesIndicatorCardView.visibility =
                    if (max(note.images.size, note.images.size) > 1) View.VISIBLE
                    else View.GONE
        }

        if (TextUtils.isEmpty(note.title)) {
            holder.titleTextView.visibility = View.GONE
        } else {
            holder.titleTextView.visibility = View.VISIBLE
            holder.titleTextView.isSelected = true
            holder.titleTextView.text = note.title
            holder.titleTextView.setTextColor(ContextCompat.getColor(holder.itemView.context,
                    if (cardBackgroundColor.getBrightness() > 130) R.color.newNoteTitleColorLightBackground
                    else R.color.newNoteTitleColor))
        }

        if (TextUtils.isEmpty(note.body)) {
            holder.bodyTextView.visibility = View.GONE
        } else {
            holder.bodyTextView.visibility = View.VISIBLE
            holder.bodyTextView.text = note.body
            holder.bodyTextView.setTextColor(ContextCompat.getColor(holder.itemView.context,
                    if (cardBackgroundColor.getBrightness() > 130) R.color.newNoteBodyColorLightBackground
                    else R.color.newNoteBodyColor))
        }

        if (TextUtils.isEmpty(note.category)) {
            holder.categoryChip.visibility = View.GONE
        } else {
            holder.categoryChip.visibility = View.VISIBLE
        }
    }

    fun setNotes(newNotes: ArrayList<Note>, filterTrashedNotes: Boolean = true) {
        log("Number of notes: ${newNotes.size}")

        if (newNotes.size == 0) {
            notes.clear()
            notifyDataSetChanged()

            return
        }

        newNotes.sortWith(Comparator { noteOne, noteTwo ->
            noteOne.position.compareTo(noteTwo.position)
        })

        val tmpNotes = arrayListOf<Note>()
        if (filterTrashedNotes) {
            newNotes.forEach {
                if (showOnlyTrashedNotes) {
                    if (it.isTrashed) {
                        tmpNotes.add(it)
                    }
                } else if (!it.isTrashed) {
                    tmpNotes.add(it)
                }
            }
        } else {
            tmpNotes.addAll(newNotes)
        }

        val notesWithoutDuplicates = arrayListOf<Note>()
        tmpNotes.forEach {
            if (!Note.arrayContainsNote(it, notesWithoutDuplicates)) {
                notesWithoutDuplicates.add(it)
            }
        }

        notesWithoutDuplicates.debug()

        val diffResult = DiffUtil.calculateDiff(DiffUtilNoteImplementation(notes, notesWithoutDuplicates))
        notes.clear()
        notes.addAll(notesWithoutDuplicates)
        diffResult.dispatchUpdatesTo(this)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardView = itemView.findViewById<CardView>(R.id.noteCardView)!!
        val contentWrapper = itemView.findViewById<View>(R.id.noteItemContentWrapper)!!
        val imagesRecyclerView = itemView.findViewById<RecyclerView>(R.id.noteImagesRecyclerView)!!
        val noteImagesListWrapper = itemView.findViewById<View>(R.id.noteImagesListWrapper)!!
        val multipleImagesIndicatorImageView = itemView.findViewById<ImageView>(R.id.multipleImagesIndicatorImageView)!!
        val multipleImagesIndicatorCardView = itemView.findViewById<View>(R.id.multipleImagesIndicatorCardView)!!
        val titleTextView = itemView.findViewById<TextView>(R.id.noteTitleTextView)!!
        val bodyTextView = itemView.findViewById<TextView>(R.id.noteBodyTextView)!!
        val categoryChip = itemView.findViewById<Chip>(R.id.categoryChip)!!
    }
}