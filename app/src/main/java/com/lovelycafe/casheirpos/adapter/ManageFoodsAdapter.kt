package com.lovelycafe.casheirpos.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.lovelycafe.casheirpos.api.*
import com.lovelycafe.casheirpos.api.Food
import com.lovelycafe.casheirpos.api.DeleteFoodRequest
import com.lovelycafe.casheirpos.api.RetrofitFood
import com.lovelycafe.casheirpos.R

class ManageFoodsAdapter (
    private var foodList: MutableList<Food>,
    private val context: Context,  // Pass context to use for Toast
    private val onDeleteClickListener: (DeleteFoodRequest) -> Unit
) : RecyclerView.Adapter<ManageFoodsAdapter.FoodViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FoodViewHolder {
        // Inflate the row layout
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.manage_food_item, parent, false)
        return FoodViewHolder(view)
    }

    override fun onBindViewHolder(holder: FoodViewHolder, position: Int) {
        val food = foodList[position]
        holder.bind(food, onDeleteClickListener)
    }

    override fun getItemCount(): Int = foodList.size

    fun updateList(newFoodList: List<Food>) {
        val diffCallback = FoodDiffCallback(foodList, newFoodList)
        val diffResult = DiffUtil.calculateDiff(diffCallback)

        foodList.clear()
        foodList.addAll(newFoodList)
        diffResult.dispatchUpdatesTo(this)
    }

    fun fetchFoods(onFetchComplete: () -> Unit) {
        val apiService = RetrofitFood.instance
        apiService.fetchFoods().enqueue(object : Callback<List<Food>> {
            override fun onResponse(
                call: Call<List<Food>>,
                response: Response<List<Food>>
            ) {
                if (response.isSuccessful) {
                    val foods = response.body() ?: emptyList()
                    updateList(foods)
                    Log.d("AdminFoodsAdapter", "Fetched foods: ${foods.size}")
                } else {
                    Log.e(
                        "AdminFoodsAdapter",
                        "Failed to fetch foods: ${response.message()}"
                    )
                }
                onFetchComplete()
            }

            override fun onFailure(call: Call<List<Food>>, t: Throwable) {
                Log.e("AdminFoodsAdapter", "Error fetching foods", t)
                Toast.makeText(
                    context,
                    "Failed to load foods. Please try again.",
                    Toast.LENGTH_SHORT
                ).show()
                onFetchComplete()
            }
        })
    }

    fun removeFood(requestBody: DeleteFoodRequest, onComplete: () -> Unit) {
        val apiService = RetrofitFood.instance
        // Make a network call to delete the food from the server
        apiService.deleteFood(requestBody).enqueue(object : Callback<DeleteFoodResponse> {
            override fun onResponse(
                call: Call<DeleteFoodResponse>,
                response: Response<DeleteFoodResponse>
            ) {
                if (response.isSuccessful && response.body()?.success == true) {
                    // Log the successful deletion using ID from the request body
                    Log.d(
                        "AdminFoodsAdapter",
                        "Food successfully deleted: ID = ${requestBody.id}"
                    )

                    // Remove the food from the list
                    val position = foodList.indexOfFirst { it.id == requestBody.id }
                    if (position != -1) {
                        foodList.removeAt(position)
                        notifyItemRemoved(position)  // Notify that the item was removed
                    }

                } else {
                    Log.e(
                        "AdminFoodsAdapter",
                        "Failed to delete food: ${response.message()}"
                    )
                }
                onComplete()
            }

            override fun onFailure(
                call: Call<DeleteFoodResponse>,
                t: Throwable
            ) {
                Log.e("AdminFoodsAdapter", "Error deleting food", t)
                onComplete()
            }
        })
    }

    class FoodViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val foodIdTextView: TextView = itemView.findViewById(R.id.foodIdTextView)
        private val foodNameTextView: TextView = itemView.findViewById(R.id.foodNameTextView)
        private val foodCategoryTextView: TextView = itemView.findViewById(R.id.foodCategoryTextView)
        private val foodPriceTextView: TextView = itemView.findViewById(R.id.foodPriceTextView)
        private val deleteButton: Button = itemView.findViewById(R.id.deleteFoodButton)

        fun bind(
            food: Food,
            onDeleteClickListener: (DeleteFoodRequest) -> Unit
        ) {
            // Set the food ID, Name, Category and Price
            foodIdTextView.text = itemView.context.getString(R.string.food_id_format, food.id)
            foodNameTextView.text = itemView.context.getString(R.string.food_name_format, food.foodName)
            foodCategoryTextView.text = itemView.context.getString(R.string.food_category_format, food.categoryName)
            foodPriceTextView.text = itemView.context.getString(R.string.food_price_format, food.price)
            // Click listeners
            // Set up delete button click listener
            deleteButton.setOnClickListener {
                if (food.id > 0) {
                    val deleteRequest = DeleteFoodRequest(food.id)
                    onDeleteClickListener(deleteRequest)
                } else {
                    Log.e("FoodViewHolder", "Invalid food ID: ${food.id}")
                }
            }
        }
    }

    // DiffUtil class for efficient updates
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