package com.lovelycafe.casheirpos.admin.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.lovelycafe.casheirpos.adapter.CompletedOrdersAdapter
import com.lovelycafe.casheirpos.api.ClearCompletedOrdersResponse
import com.lovelycafe.casheirpos.api.CompletedOrder
import com.lovelycafe.casheirpos.api.CompletedOrdersResponse
import com.lovelycafe.casheirpos.api.ResetOrdersResponse
import com.lovelycafe.casheirpos.api.RetrofitEvent
import com.lovelycafe.casheirpos.api.RetrofitOrder
import com.lovelycafe.casheirpos.databinding.FragmentCompletedOrdersBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CompletedOrdersFragment : Fragment() {

    private var _binding: FragmentCompletedOrdersBinding? = null
    private val binding get() = _binding!!

    private lateinit var completedOrdersAdapter: CompletedOrdersAdapter
    private val completedOrdersList = mutableListOf<CompletedOrder>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCompletedOrdersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize the adapter with an empty list
        completedOrdersAdapter = CompletedOrdersAdapter(completedOrdersList)
        binding.completedOrdersRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.completedOrdersRecyclerView.adapter = completedOrdersAdapter

        binding.buttonDeleteAllCompleted.setOnClickListener {
            clearCompletedOrders()
        }

        // Fetch completed orders
        fetchCompletedOrders()
    }

    private fun fetchCompletedOrders() {
        binding.progressBar.visibility = View.VISIBLE
        RetrofitOrder.instance.fetchCompletedOrder().enqueue(object : Callback<CompletedOrdersResponse> {
            override fun onResponse(call: Call<CompletedOrdersResponse>, response: Response<CompletedOrdersResponse>) {
                binding.progressBar.visibility = View.GONE
                if (response.isSuccessful) {
                    response.body()?.let { responseBody ->
                        if (responseBody.success && responseBody.completedOrders.isNotEmpty()) {
                            // Update the adapter with the new list of completed orders
                            completedOrdersAdapter.updateCompletedOrders(responseBody.completedOrders)
                            binding.textNoCompletedOrders.visibility = View.GONE // Hide placeholder
                        } else {
                            // Show placeholder if no completed orders are found
                            binding.textNoCompletedOrders.visibility = View.VISIBLE
                            completedOrdersAdapter.updateCompletedOrders(emptyList()) // Clear adapter
                        }
                    } ?: run {
                        // Show placeholder if the response body is null
                        binding.textNoCompletedOrders.visibility = View.VISIBLE
                    }
                } else {
                    Toast.makeText(requireContext(), "Failed to fetch completed orders", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<CompletedOrdersResponse>, t: Throwable) {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun clearCompletedOrders() {
        // Create an AlertDialog to confirm the deletion
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("ሁሉንም አጥፋ")
        builder.setMessage("ሁሉንም ያለቁ ትዕዛዞች ማጥፋት እና የትዕዛዝ ቁጥሮችን ዳግም ማስጀመር ይፈልጋሉ?")

        // Set up the positive button
        builder.setPositiveButton("አዎ") { dialog, _ ->
            // User confirmed, proceed with deletion
            RetrofitOrder.instance.clearCompleted().enqueue(object : Callback<ClearCompletedOrdersResponse> {
                override fun onResponse(call: Call<ClearCompletedOrdersResponse>, response: Response<ClearCompletedOrdersResponse>) {
                    if (response.isSuccessful) {
                        Toast.makeText(requireContext(), "Orders Cleared Successfully", Toast.LENGTH_SHORT).show()

                        // Now call resetOrdersAutoIncrement() after clearing orders
                        resetOrdersAutoIncrement()

                        // Fetch completed orders to update the UI
                        completedOrdersAdapter.updateCompletedOrders(completedOrdersList)
                        fetchCompletedOrders()
                    } else {
                        Toast.makeText(requireContext(), "Failed to Clear Orders", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<ClearCompletedOrdersResponse>, t: Throwable) {
                    Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
            dialog.dismiss()
        }

        builder.setNegativeButton("አልፈልግም") { dialog, _ ->
            dialog.dismiss()
        }

        val alertDialog = builder.create()
        alertDialog.show()
    }

    private fun resetOrdersAutoIncrement() {
        binding.progressBar.visibility = View.VISIBLE
        RetrofitEvent.instance.resetAutoIncrementOrders().enqueue(object : Callback<ResetOrdersResponse> {
            override fun onResponse(call: Call<ResetOrdersResponse>, response: Response<ResetOrdersResponse>) {
                binding.progressBar.visibility = View.GONE
                if (response.isSuccessful) {
                    response.body()?.let { responseBody ->
                        if (responseBody.success) {
                            Toast.makeText(requireContext(), "Orders reset successfully", Toast.LENGTH_SHORT).show()
                            fetchCompletedOrders() // Refresh the list
                        } else {
                            Toast.makeText(requireContext(), responseBody.message, Toast.LENGTH_SHORT).show()
                        }
                    } ?: run {
                        Toast.makeText(requireContext(), "Response body is null", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(requireContext(), "Failed to reset orders", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ResetOrdersResponse>, t: Throwable) {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}