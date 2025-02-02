package com.lovelycafe.casheirpos.client.ui.cart

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.lovelycafe.casheirpos.adapter.CartAdapter
import com.lovelycafe.casheirpos.api.*
import com.lovelycafe.casheirpos.databinding.FragmentCartBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.lovelycafe.casheirpos.R


class CartFragment : Fragment() {

    private var _binding: FragmentCartBinding? = null
    private val binding get() = _binding!!
    private lateinit var cartAdapter: CartAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCartBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupCartRecyclerView()
        setupEventListeners()
        fetchCartItems()
        fetchWaiters()
    }

    private fun setupCartRecyclerView() {
        cartAdapter = CartAdapter(
            onUpdate = { foodName, quantity -> updateCartItem(foodName, quantity) },
            onDelete = { id -> deleteCartItem(id) }
        )

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = cartAdapter
        }
    }

    private fun setupEventListeners() {
        binding.buttonDeleteAll.setOnClickListener {
            clearCartItems()
        }

        binding.btnSendOrder.setOnClickListener {
            handleOrderSubmission()
        }
    }

    private fun fetchCartItems() {
        RetrofitCart.instance.fetchCart().enqueue(object : Callback<List<CartItem>> {
            override fun onResponse(call: Call<List<CartItem>>, response: Response<List<CartItem>>) {
                if (response.isSuccessful) {
                    val cartItems = response.body() ?: emptyList()
                    cartAdapter.submitList(cartItems)

                    if (cartItems.isEmpty()) {
                        binding.textNoOrders.visibility = View.VISIBLE
                        binding.recyclerView.visibility = View.GONE
                    } else {
                        binding.textNoOrders.visibility = View.GONE
                        binding.recyclerView.visibility = View.VISIBLE
                        updateTotalPrice(cartItems)
                    }
                } else {
                    showToast("Failed to fetch cart")
                }
            }

            override fun onFailure(call: Call<List<CartItem>>, t: Throwable) {
                showToast("Error: ${t.message}")
            }
        })
    }

    private fun updateCartItem(foodName: String, quantity: Int) {
        val updateRequest = UpdateCartRequest(foodName, quantity)

        RetrofitCart.instance.updateCart(updateRequest).enqueue(object : Callback<UpdateCartResponse> {
            override fun onResponse(call: Call<UpdateCartResponse>, response: Response<UpdateCartResponse>) {
                if (response.isSuccessful) {
                    // showToast("Item updated")
                    fetchCartItems()
                } else {
                    showToast("Failed to update cart")
                }
            }

            override fun onFailure(call: Call<UpdateCartResponse>, t: Throwable) {
                showToast("Error: ${t.message}")
            }
        })
    }

    private fun deleteCartItem(id: Int) {
        RetrofitCart.instance.deleteCart(DeleteCartRequest(id)).enqueue(object : Callback<DeleteCartResponse> {
            override fun onResponse(call: Call<DeleteCartResponse>, response: Response<DeleteCartResponse>) {
                if (response.isSuccessful) {
                    // showToast("Item removed")
                    fetchCartItems()
                } else {
                    showToast("Failed to delete item")
                }
            }

            override fun onFailure(call: Call<DeleteCartResponse>, t: Throwable) {
                showToast("Error: ${t.message}")
            }
        })
    }

    private fun clearCartItems() {
        RetrofitCart.instance.clear().enqueue(object : Callback<ClearCartResponse> {
            override fun onResponse(call: Call<ClearCartResponse>, response: Response<ClearCartResponse>) {
                if (response.isSuccessful) {
                    // showToast("Cart cleared successfully")
                    fetchCartItems()
                } else {
                    showToast("Failed to clear cart")
                }
            }

            override fun onFailure(call: Call<ClearCartResponse>, t: Throwable) {
                showToast("Error: ${t.message}")
            }
        })
    }

    private fun fetchWaiters() {
        RetrofitWaiter.instance.fetchWaiters().enqueue(object : Callback<List<Waiter>> {
            override fun onResponse(call: Call<List<Waiter>>, response: Response<List<Waiter>>) {
                if (response.isSuccessful) {
                    val waiters = response.body()?.map { it.waiterName } ?: emptyList()
                    val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, waiters)
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    binding.spinnerWaiters.adapter = adapter
                } else {
                    showToast("Error fetching waiters")
                }
            }

            override fun onFailure(call: Call<List<Waiter>>, t: Throwable) {
                showToast("Failed to fetch waiters: ${t.message}")
            }
        })
    }

    private fun updateTotalPrice(cartItems: List<CartItem>) {
        val totalPrice = if (cartItems.isEmpty()) {
            0.0
        } else {
            cartItems.sumOf { (it.price * it.quantity) / it.quantity }
        }
        binding.totalPrice.text = String.format(getString(R.string.total_price_format), totalPrice)
    }

    private fun handleOrderSubmission() {
        val selectedWaiter = binding.spinnerWaiters.selectedItem?.toString() ?: ""
        // val tableNumberInput = binding.tableNumber.text.toString()

        /*
        if (tableNumberInput.isEmpty()) {
            showToast("Please enter a table number")
            return
        }
        val tableNumber = tableNumberInput.toIntOrNull()
        if (tableNumber == null || tableNumber < 1 || tableNumber > 99) {
            showToast("Table number must be between 1 and 99")
            return
        }
         */

        val cartItems = cartAdapter.currentList
        if (cartItems.isEmpty()) {
            showToast("Cart is empty")
            return
        }

        val individualTotals = cartItems.associate { it.foodName to ((it.price * it.quantity) / it.quantity) }
        val orderTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

        val orderRequests = cartItems.map { item ->
            // Get the total price for the current item
            val itemTotal = individualTotals[item.foodName] ?: 0.0

            // Create and return the OrderRequest object
            OrderRequest(
                waiterName = selectedWaiter,
                // tableNumber = tableNumber,
                foodName = item.foodName,
                quantity = item.quantity,
                totalPrice = itemTotal,
                orderTime = orderTime
            )
        }

        orderRequests.forEach { request ->
            RetrofitOrder.instance.sendOrder(request).enqueue(object : Callback<OrderResponse> {
                override fun onResponse(call: Call<OrderResponse>, response: Response<OrderResponse>) {
                    if (response.isSuccessful) {
                        showToast("Order sent successfully!")
                        clearCartItems()
                    } else {
                        showToast("Failed to send order")
                    }
                }

                override fun onFailure(call: Call<OrderResponse>, t: Throwable) {
                    showToast("Error: ${t.message}")
                }
            })
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
