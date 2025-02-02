package com.lovelycafe.casheirpos.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.lovelycafe.casheirpos.api.CartItem
import com.lovelycafe.casheirpos.databinding.ItemCartBinding
import com.lovelycafe.casheirpos.R
import java.util.Locale

class CartAdapter(
    private val onUpdate: (foodName: String, quantity: Int) -> Unit,
    private val onDelete: (id: Int) -> Unit
) : ListAdapter<CartItem, CartAdapter.CartViewHolder>(CartDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val binding = ItemCartBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CartViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class CartViewHolder(private val binding: ItemCartBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(cartItem: CartItem) {
            binding.apply {
                foodName.text = cartItem.foodName
                price.text = String.format(
                    binding.root.context.getString(R.string.price_format),
                    cartItem.price)
                quantity.text = String.format(Locale.getDefault(), "%d", cartItem.quantity)

                plusButton.setOnClickListener {
                    val newQuantity = cartItem.quantity + 1
                    onUpdate(cartItem.foodName, newQuantity)
                }

                minusButton.setOnClickListener {
                    val newQuantity = if (cartItem.quantity > 1) cartItem.quantity - 1 else 1
                    onUpdate(cartItem.foodName, newQuantity)
                }

                deleteButton.setOnClickListener {
                    onDelete(cartItem.id)
                }
            }
        }
    }
}

class CartDiffCallback : DiffUtil.ItemCallback<CartItem>() {
    override fun areItemsTheSame(oldItem: CartItem, newItem: CartItem): Boolean = oldItem.id == newItem.id

    override fun areContentsTheSame(oldItem: CartItem, newItem: CartItem): Boolean = oldItem == newItem
}