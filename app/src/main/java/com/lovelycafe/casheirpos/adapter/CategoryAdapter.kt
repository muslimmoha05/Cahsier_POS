package com.lovelycafe.casheirpos.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.lovelycafe.casheirpos.R
import com.lovelycafe.casheirpos.api.Image

class CategoryAdapter(
    private val images: List<Image>,
    private val onImagesClick: (Image) -> Unit
) : RecyclerView.Adapter<CategoryAdapter.ImageViewHolder>() {

    inner class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textViewName: TextView = itemView.findViewById(R.id.textViewName)
        val textViewTranslatedName: TextView = itemView.findViewById(R.id.textViewTranslatedName)
        val imageView: ImageView = itemView.findViewById(R.id.imageView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_image, parent, false)
        return ImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val image = images[position]
        holder.textViewName.text = holder.itemView.context.getString(R.string.image_name_label, image.name)
        holder.textViewTranslatedName.text = image.translated

        // Load the image using Glide
        Glide.with(holder.itemView.context)
            .load("https://utopiacorehub.com/lovelycafe_api/categories/${image.path}")
            .into(holder.imageView)

        // Set Click Listener
        holder.itemView.setOnClickListener {
            onImagesClick(image)
        }
    }

    override fun getItemCount(): Int = images.size
}