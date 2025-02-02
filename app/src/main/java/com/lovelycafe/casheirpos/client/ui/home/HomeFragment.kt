package com.lovelycafe.casheirpos.client.ui.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.lovelycafe.casheirpos.adapter.ShowOrdersAdapter
import com.lovelycafe.casheirpos.api.CancelOrderRequest
import com.lovelycafe.casheirpos.api.CancelOrderResponse
import com.lovelycafe.casheirpos.api.Order
import com.lovelycafe.casheirpos.api.RetrofitOrder
import com.lovelycafe.casheirpos.api.OrdersResponse
import com.lovelycafe.casheirpos.databinding.FragmentHomeBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var orderAdapter: ShowOrdersAdapter
    private lateinit var noOrdersTextView: TextView
    private val ordersList = mutableListOf<Order>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        // Initialize ProgressBar and TextView using the binding object
        val progressBar: ProgressBar = binding.progressBar
        noOrdersTextView = binding.textNoOrders

        // Set up RecyclerView
        orderAdapter = ShowOrdersAdapter(ordersList, ::cancelOrder)

        val numberOfColumns = if (resources.configuration.screenWidthDp >= 600) 3 else 1
        binding.recyclerViewOrders.layoutManager = GridLayoutManager(context, numberOfColumns)

        binding.recyclerViewOrders.adapter = orderAdapter

        // Fetch orders
        fetchOrders(progressBar)

        return binding.root
    }

    private fun fetchOrders(progressBar: ProgressBar) {
        progressBar.visibility = View.VISIBLE
        RetrofitOrder.instance.fetchOrder().enqueue(object : Callback<OrdersResponse> {
            override fun onResponse(call: Call<OrdersResponse>, response: Response<OrdersResponse>) {
                progressBar.visibility = View.GONE
                if (response.isSuccessful) {
                    response.body()?.let { ordersResponse ->
                        if (ordersResponse.success) {
                            orderAdapter.updateOrders(ordersResponse.orders)
                            updateOrderVisibility(ordersResponse.orders.isEmpty())
                        } else {
                            showNoOrdersMessage("No orders found.")
                        }
                    } ?: showNoOrdersMessage("Unexpected response format.")
                } else {
                    showNoOrdersMessage("Failed to fetch orders")
                }
            }

            override fun onFailure(call: Call<OrdersResponse>, t: Throwable) {
                progressBar.visibility = View.GONE
                Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun updateOrderVisibility(isEmpty: Boolean) {
        if (isEmpty) {
            noOrdersTextView.visibility = View.VISIBLE
            binding.recyclerViewOrders.visibility = View.GONE
        } else {
            noOrdersTextView.visibility = View.GONE
            binding.recyclerViewOrders.visibility = View.VISIBLE
        }
    }

    private fun showNoOrdersMessage(message: String) {
        noOrdersTextView.visibility = View.VISIBLE
        binding.recyclerViewOrders.visibility = View.GONE
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    private fun cancelOrder(orderId: Int) {
        val cancelRequest = CancelOrderRequest(orderId)
        RetrofitOrder.instance.cancelOrder(cancelRequest).enqueue(object : Callback<CancelOrderResponse> {
            override fun onResponse(call: Call<CancelOrderResponse>, response: Response<CancelOrderResponse>) {
                if (response.isSuccessful) {
                    response.body()?.let { cancelResponse ->
                        if (cancelResponse.success) {
                            Toast.makeText(requireContext(), "Order canceled successfully", Toast.LENGTH_SHORT).show()
                            ordersList.removeAll { it.id == orderId }
                            orderAdapter.updateOrders(ordersList)
                            fetchOrders(binding.progressBar)
                        } else {
                            Toast.makeText(requireContext(), "Failed to cancel order", Toast.LENGTH_SHORT).show()
                        }
                    } ?: run {
                        Toast.makeText(requireContext(), "Unexpected response format.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Log.e("API_ERROR", "Response error: ${response.errorBody()?.string()}")
                    Toast.makeText(requireContext(), "Failed to cancel order", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<CancelOrderResponse>, t: Throwable) {
                Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}