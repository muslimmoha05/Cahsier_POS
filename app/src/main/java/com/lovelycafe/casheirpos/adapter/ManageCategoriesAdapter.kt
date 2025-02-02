package com.lovelycafe.casheirpos.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.lovelycafe.casheirpos.R
import com.lovelycafe.casheirpos.api.*

class ManageCategoriesAdapter(
    private val context: Context,
    private val categories: List<Image>,
    private val onEditClick: (Image) -> Unit,
    private val onDeleteClick: (Image) -> Unit
) : RecyclerView.Adapter<ManageCategoriesAdapter.CategoryViewHolder>() {

    inner class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val categoryImage: ImageView = itemView.findViewById(R.id.categoryImageView)
        val categoryId: TextView = itemView.findViewById(R.id.categoryIdTextView)
        val categoryName: TextView = itemView.findViewById(R.id.categoryNameTextView)
        val translatedName: TextView = itemView.findViewById(R.id.translatedCategoryNameTextView)
        val editButton: Button = itemView.findViewById(R.id.editCategoryButton)
        val deleteButton: Button = itemView.findViewById(R.id.deleteCategoryButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.manage_category_item, parent, false)
        return CategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val category = categories[position]
        holder.categoryId.text = context.getString(R.string.category_id_label, category.id)
        holder.categoryName.text = category.name
        holder.translatedName.text = category.translated

        Glide.with(holder.itemView.context)
            .load("https://utopiacorehub.com/lovelycafe_api/categories/${category.path}")
            .into(holder.categoryImage)

        holder.editButton.setOnClickListener { onEditClick(category) }
        holder.deleteButton.setOnClickListener { onDeleteClick(category) }
    }

    override fun getItemCount(): Int = categories.size
}
