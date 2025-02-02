package com.lovelycafe.casheirpos.admin.ui.dashboard

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.lovelycafe.casheirpos.R
import com.lovelycafe.casheirpos.api.AddFoodRequest
import com.lovelycafe.casheirpos.api.AddFoodResponse
import com.lovelycafe.casheirpos.api.DeleteFoodRequest
import com.lovelycafe.casheirpos.api.Food
import com.lovelycafe.casheirpos.api.RetrofitFood
import com.lovelycafe.casheirpos.adapter.ManageFoodsAdapter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import androidx.recyclerview.widget.GridLayoutManager

class ManageFoodsFragment : Fragment() {

    private lateinit var adapter: ManageFoodsAdapter
    private lateinit var foodsRecyclerView: RecyclerView
    private lateinit var addFoodButton: MaterialButton
    private val foodList: MutableList<Food> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view =  inflater.inflate(R.layout.fragment_manage_foods, container, false)

        foodsRecyclerView = view.findViewById(R.id.foodsRecyclerView)
        val gridLayoutManager = GridLayoutManager(requireContext(), 2) // 2 columns
        foodsRecyclerView.layoutManager = gridLayoutManager

        addFoodButton = view.findViewById(R.id.addFoodButton)

        adapter = ManageFoodsAdapter(
            foodList,
            context = requireContext(),
            onDeleteClickListener = { deleteRequest -> confirmDeleteFood(deleteRequest) }
        )

        foodsRecyclerView.setLayoutManager(LinearLayoutManager(requireContext()))
        foodsRecyclerView.setAdapter(adapter)

        addFoodButton.setOnClickListener { addFoodDialog() }

        fetchFoods()

        return view
    }

    private fun fetchFoods() {
        adapter.fetchFoods {
            Toast.makeText(requireContext(), "Foods updated", Toast.LENGTH_SHORT).show() }
    }

    private fun addFoodDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_food, null)
        val foodNameInput = dialogView.findViewById<EditText>(R.id.foodNameInput)
        val categoryNameInput = dialogView.findViewById<EditText>(R.id.foodCategoryInput)
        val priceInput = dialogView.findViewById<EditText>(R.id.foodPriceInput)

        val dialogBuilder = MaterialAlertDialogBuilder(requireContext())
        dialogBuilder.setTitle("Add Food")
            .setView(dialogView)
            .setPositiveButton("Add") { _, _ ->
                val foodName = foodNameInput.text.toString()
                val categoryName = categoryNameInput.text.toString()
                val priceString = priceInput.text.toString()
                if (priceString.isNotEmpty()) {
                    val price = priceString.toDouble()
                    addFood(foodName, categoryName, price)
                } else {
                    Toast.makeText(requireContext(), "Please enter a valid price", Toast.LENGTH_SHORT)
                        .show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun addFood(foodName: String, categoryName: String, price: Double) {
        val request = AddFoodRequest(foodName, categoryName, price)

        RetrofitFood.instance.addFood(request)
            .enqueue(object : Callback<AddFoodResponse?> {
                override fun onResponse(
                    call: Call<AddFoodResponse?>,
                    response: Response<AddFoodResponse?>
                ) {
                    if (response.isSuccessful && response.body() != null) {
                        if ("success" == response.body()!!.status) {
                            fetchFoods()
                            Toast.makeText(
                                requireContext(),
                                response.body()!!.message,
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            Toast.makeText(
                                requireContext(),
                                "Failed: " + response.body()!!.message,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }

                override fun onFailure(call: Call<AddFoodResponse?>, t: Throwable) {
                    Toast.makeText(
                        requireContext(),
                        "Error: " + t.message,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    private fun confirmDeleteFood(deleteRequest: DeleteFoodRequest) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Delete Food")
            .setMessage("Are you sure you want to delete this food?")
            .setPositiveButton("Delete") { _, _ ->
                deleteFood(deleteRequest) }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteFood(deleteRequest: DeleteFoodRequest) {
        adapter.removeFood(deleteRequest) {
            Toast.makeText(requireContext(),
                "Food deleted",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}