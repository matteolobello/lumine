package it.matteolobello.lumine.ui.adapter.recyclerview

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import it.matteolobello.lumine.R
import it.matteolobello.lumine.data.bundle.BundleKeys
import it.matteolobello.lumine.ui.activity.FullscreenImagesActivity
import java.io.File

class ImagesRecyclerViewAdapter(
        private val imagePaths: ArrayList<String>,
        private val onClickListener: (() -> Unit)?) : RecyclerView.Adapter<ImagesRecyclerViewAdapter.ViewHolder>() {

    var roundedCorners = true

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
            LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_image, parent, false))

    override fun getItemCount() = imagePaths.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val imagePath = imagePaths[position]

        holder.itemView.setOnClickListener { view ->
            if (onClickListener != null) {
                onClickListener.invoke()
            } else {
                view.context.startActivity(Intent(view.context, FullscreenImagesActivity::class.java)
                        .putExtra(BundleKeys.EXTRA_IMAGE_PATHS, imagePaths)
                        .putExtra(BundleKeys.EXTRA_IMAGE_INDEX, holder.adapterPosition))
            }
        }

        Picasso.get()
                .load(File(imagePath))
                .into(holder.noteImageView)

        if (!roundedCorners) {
            holder.itemView.findViewById<CardView>(R.id.noteImageCardView).radius = 0f
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val noteImageView = itemView.findViewById<ImageView>(R.id.noteImageView)!!
    }
}