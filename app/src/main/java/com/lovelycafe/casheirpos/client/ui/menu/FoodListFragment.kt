package com.lovelycafe.casheirpos.client.ui.menu

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.RecyclerView
import com.lovelycafe.casheirpos.api.Food
import com.lovelycafe.casheirpos.adapter.FoodAdapter
import com.lovelycafe.casheirpos.R
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.GridLayoutManager
import okhttp3.*
import java.io.IOException

class FoodListFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var foodAdapter: FoodAdapter
    private val foodList: MutableList<Food> = mutableListOf()
    private val args: FoodListFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_food_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.foodListRecyclerView)
        val numberOfColumns = if (resources.configuration.screenWidthDp >= 600) 5 else 2

        recyclerView.layoutManager = GridLayoutManager(requireContext(), numberOfColumns)

        // Create the adapter with a click listener
        foodAdapter = FoodAdapter(foodList, context = requireContext()) { food ->
            showPopupDialog(food) // Call the method to show the dialog
        }
        recyclerView.adapter = foodAdapter

        // Get the category ID from arguments
        val categoryId = args.categoryId

        // Fetch food items for the selected category
        fetchListFoods(categoryId)
    }

    private fun fetchListFoods(categoryId: Int) {
        foodAdapter.fetchListFoods(categoryId) {
            // Toast.makeText(requireContext(), "Foods updated", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showPopupDialog(food: Food) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Select an Action")
            .setMessage(" ${food.foodName} ")
            .setPositiveButton("ወደ ማዘዣ ጨምር") { _, _ ->
                insertToCart(food)
            }
            /**
            .setNegativeButton("Insert to Bill") { _, _ ->
            insertToBill(food)
            }
             **/
            .setNeutralButton("አጥፋ") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun insertToCart(food: Food) {
        val client = OkHttpClient()

        val itemPrice = food.price * food.quantity

        // Create the request body
        val requestBody = FormBody.Builder()
            .add("foodName", food.foodName)
            .add("price", itemPrice.toString())
            .add("quantity", food.quantity.toString())
            .build()

        // Create the request
        val request = Request.Builder()
            .url("https://utopiacorehub.com/lovelycafe_api/cart/cart.php")
            .post(requestBody)
            .build()

        // Execute the request
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                // Handle the error
                e.printStackTrace()
                Toast.makeText(requireContext(), "Failed to add to cart", Toast.LENGTH_SHORT).show()
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    // Handle the success
                    requireActivity().runOnUiThread {
                        Toast.makeText(requireContext(), "${food.foodName} ወደ ማዘዣ ተጨምሯል", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    // Handle the error response
                    requireActivity().runOnUiThread {
                        Toast.makeText(requireContext(), "Error: ${response.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }

    /**
    private fun insertToBill(food: Food) {
    Toast.makeText(requireContext(), "${food.foodName} added to bill", Toast.LENGTH_SHORT).show()
    }
     **/
}
