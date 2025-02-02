package com.lovelycafe.casheirpos.admin.ui.dashboard

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.lovelycafe.casheirpos.R
import com.lovelycafe.casheirpos.api.RetrofitOrder
import com.lovelycafe.casheirpos.api.SummaryResponse
import com.lovelycafe.casheirpos.databinding.FragmentSummaryBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SummaryFragment : Fragment() {
    private lateinit var binding: FragmentSummaryBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSummaryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fetchSummary()

        binding.refreshButton.setOnClickListener {
            fetchSummary()
        }
    }

    private fun fetchSummary() {
        RetrofitOrder.instance.fetchSummary().enqueue(object : Callback<SummaryResponse> {
            override fun onResponse(
                call: Call<SummaryResponse>,
                response: Response<SummaryResponse>
            ) {
                if (response.isSuccessful) {
                    response.body()?.let { summaryResponse ->
                        if (summaryResponse.success) {
                            val summary = summaryResponse.data

                            // Update the UI elements
                            binding.totalOrdersTextView.text =
                                getString(R.string.total_completed_orders, summary.totalOrders)
                            binding.totalSumTextView.text =
                                getString(R.string.total_price_sum, summary.totalSum.replace(",", "").toDouble())
                            binding.selectedItemsTextView.text =
                                getString(R.string.selected_items, summary.selectedItems)
                        } else {
                            Log.e("SummaryFragment", "API returned unsuccessful response")
                        }
                    } ?: run {
                        Log.e("SummaryFragment", "Response body is null")
                    }
                } else {
                    Log.e("SummaryFragment", "Response not successful: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<SummaryResponse>, t: Throwable) {
                Log.e("SummaryFragment", "Network call failed", t)
            }
        })
    }
}
