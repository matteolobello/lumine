package it.matteolobello.lumine.ui.adapter.recyclerview

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.recyclerview.widget.RecyclerView
import it.matteolobello.lumine.R
import it.matteolobello.lumine.extension.dpToPx

class StrokeWidthsRecyclerViewAdapter(
        private val context: Context,
        private val onStrokeWidthSelected: (strokeWidth: Int) -> Unit) : RecyclerView.Adapter<StrokeWidthsRecyclerViewAdapter.ViewHolder>() {

    private val strokeWidths = intArrayOf(
            dpToPx(6f),
            dpToPx(8f),
            dpToPx(10f),
            dpToPx(12f),
            dpToPx(14f),
            dpToPx(16f),
            dpToPx(18f),
            dpToPx(22f),
            dpToPx(26f)
    ).reversed()

    private var previousSelectedStrokeView: View? = null
    private var previousSelectedStroke: Int = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder = ViewHolder(
            LayoutInflater.from(context).inflate(R.layout.item_stroke_width, parent, false))

    override fun getItemCount() = strokeWidths.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val strokeWidth = strokeWidths[position]

        val imageView = (holder.itemView as ViewGroup).getChildAt(0)

        with(imageView.layoutParams as RelativeLayout.LayoutParams) {
            width = strokeWidth
            height = strokeWidth
            imageView.layoutParams = this
        }
        holder.itemView.setOnClickListener {
            if (strokeWidth == previousSelectedStroke) {
                return@setOnClickListener
            }

            holder.itemView.setBackgroundResource(R.drawable.selected_stroke_width_circle)
            previousSelectedStrokeView?.setBackgroundResource(0)
            onStrokeWidthSelected(strokeWidth)

            previousSelectedStrokeView = it
            previousSelectedStroke = strokeWidth
        }

        if (position == 2 && previousSelectedStrokeView == null) {
            // Set default stroke to 18dp
            holder.itemView.callOnClick()
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}