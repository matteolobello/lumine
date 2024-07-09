package it.matteolobello.lumine.ui.adapter.recyclerview

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import it.matteolobello.circleimageview.CircleImageView
import it.matteolobello.lumine.R
import it.matteolobello.lumine.extension.dpToPx

class ColorsRecyclerViewAdapter(
        private val context: Context,
        private val onColorSelected: (color: Int) -> Unit) : RecyclerView.Adapter<ColorsRecyclerViewAdapter.ViewHolder>() {

    private val colors = context.resources.getIntArray(R.array.drawingColors)

    private var previousSelectedColorImageView: CircleImageView? = null
    private var previousSelectedColor: Int = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder = ViewHolder(
            LayoutInflater.from(context).inflate(R.layout.item_color, parent, false))

    override fun getItemCount() = colors.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val color = colors[position]

        with(holder.itemView as CircleImageView) {
            setImageDrawable(ColorDrawable(color))
            if (color == Color.WHITE) {
                setBorderWidth(dpToPx(1f))
                setBorderColor(Color.GRAY)
            }
            setOnClickListener {
                if (previousSelectedColorImageView != null && previousSelectedColor != -1) {
                    if (previousSelectedColor == Color.WHITE) {
                        previousSelectedColorImageView!!.setBorderWidth(dpToPx(1f))
                        previousSelectedColorImageView!!.setBorderColor(Color.GRAY)
                    } else {
                        previousSelectedColorImageView!!.setBorderWidth(0)
                        previousSelectedColorImageView!!.setBorderColor(Color.TRANSPARENT)
                    }
                }

                setBorderColor(Color.BLACK)
                setBorderWidth(dpToPx(1f))
                onColorSelected(color)

                previousSelectedColorImageView = this
                previousSelectedColor = color
            }
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}