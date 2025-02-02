package com.lovelycafe.casheirpos.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.lovelycafe.casheirpos.api.Food
import com.lovelycafe.casheirpos.api.RetrofitFood
import com.lovelycafe.casheirpos.R
import java.text.NumberFormat
import java.util.Locale

class FoodAdapter (
    private var foodList: MutableList<Food>,
    private val context: Context,
    private val onFoodClick: (Food) -> Unit
): RecyclerView.Adapter<FoodAdapter.FoodViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FoodViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_food, parent, false)
        return FoodViewHolder(view)
    }

    override fun onBindViewHolder(holder: FoodViewHolder, position: Int) {
        val food = foodList[position]
        holder.bind(food, onFoodClick)
    }

    override fun getItemCount(): Int = foodList.size

    fun updateList(newFoodList: List<Food>) {
        val diffCallback = FoodDiffCallback(foodList, newFoodList)
        val diffResult = DiffUtil.calculateDiff(diffCallback)

        foodList.clear()
        foodList.addAll(newFoodList)
        diffResult.dispatchUpdatesTo(this)
    }

    fun fetchListFoods(categoryId: Int, onFetchComplete: () -> Unit) {
        val apiService = RetrofitFood.instance
        apiService.fetchListFoods(categoryId).enqueue(object : Callback<List<Food>> {
            override fun onResponse(call: Call<List<Food>>, response: Response<List<Food>>) {
                if (response.isSuccessful) {
                    val foods = response.body() ?: emptyList()
                    val updatedFoods = foods.map { food -> food.copy(quantity = 1) }
                    updateList(updatedFoods)
                    Log.d("FoodAdapter", "Fetched foods: ${foods.size}")
                } else {
                    Log.e("FoodAdapter", "Failed to fetch foods: ${response.message()}")
                }
                onFetchComplete()
            }

            override fun onFailure(call: Call<List<Food>>, t: Throwable) {
                Log.e("FoodAdapter", "Error fetching foods", t)
                Toast.makeText(context, "Failed to load foods. Please try again.",
                    Toast.LENGTH_SHORT).show()
                onFetchComplete()
            }
        })
    }

    class FoodViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val foodNameTextView: TextView = itemView.findViewById(R.id.foodNameTextView)
        private val foodPriceTextView: TextView = itemView.findViewById(R.id.foodPriceTextView)
        private val quantityTextView: TextView = itemView.findViewById(R.id.quantityTextView)
        private val plusButton: ImageButton = itemView.findViewById(R.id.foodPlusButton)
        private val minusButton: ImageButton = itemView.findViewById(R.id.foodMinusButton)

        private val numberFormat: NumberFormat = NumberFormat.getNumberInstance(Locale.getDefault())

        fun bind(food: Food, onFoodClick: (Food) -> Unit) {
            foodNameTextView.text = food.foodName
            foodPriceTextView.text = String.format(Locale.US, "%.2f ETB", food.price)
            quantityTextView.text = numberFormat.format(food.quantity)

            plusButton.setOnClickListener {
                food.quantity++
                quantityTextView.text = numberFormat.format(food.quantity)
            }

            minusButton.setOnClickListener {
                if (food.quantity > 0) {
                    food.quantity--
                    quantityTextView.text = numberFormat.format(food.quantity)
                }
            }

            // Set an OnClickListener for the item view to show the dialog
            itemView.setOnClickListener {
                onFoodClick(food)
            }
        }
    }

    class FoodDiffCallback(
        private val oldList: List<Food>,
        private val newList: List<Food>
    ) : DiffUtil.Callback() {

        override fun getOldListSize(): Int = oldList.size
        override fun getNewListSize(): Int = newList.size
        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition].id == newList[newItemPosition].id
        }

        override fun areContentsTheSame(
            oldItemPosition: Int,
            newItemPosition: Int
        ): Boolean {
            return oldList[oldItemPosition] == newList[newItemPosition]
        }
    }
}