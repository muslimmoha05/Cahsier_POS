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
import com.lovelycafe.casheirpos.api.Waiter
import com.lovelycafe.casheirpos.api.DeleteWaiterRequest
import com.lovelycafe.casheirpos.api.RetrofitWaiter
import com.lovelycafe.casheirpos.R
import java.text.SimpleDateFormat
import java.util.Locale

class ManageWaitersAdapter (
    private val waiterList: MutableList<Waiter>,
    private val context: Context,
    private val onDeleteClickListener: (DeleteWaiterRequest) -> Unit
) : RecyclerView.Adapter<ManageWaitersAdapter.WaiterViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): WaiterViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.manage_waiter_item, parent, false)
        return WaiterViewHolder(view)
    }

    override fun onBindViewHolder(holder: WaiterViewHolder, position: Int) {
        val waiter = waiterList[position]
        holder.bind(waiter, onDeleteClickListener)
    }

    override fun getItemCount(): Int = waiterList.size

    fun updateList(newWaiterList: List<Waiter>) {
        val diffCallback = WaiterDiffCallback(waiterList, newWaiterList)
        val diffResult = DiffUtil.calculateDiff(diffCallback)

        waiterList.clear()
        waiterList.addAll(newWaiterList)
        diffResult.dispatchUpdatesTo(this)
    }

    fun fetchWaiters(onFetchComplete: () -> Unit) {
        val apiService = RetrofitWaiter.instance
        apiService.fetchWaiters().enqueue(object : Callback<List<Waiter>> {
            override fun onResponse(call: Call<List<Waiter>>, response: Response<List<Waiter>>) {
                if (response.isSuccessful) {
                    val waiters = response.body() ?: emptyList()
                    updateList(waiters)
                    Log.d("ManageWaitersAdapter", "Fetched waiters: ${waiters.size}")
                } else {
                    Log.e("ManageWaitersAdapter", "Failed to fetch waiters: ${response.message()}")
                }
                onFetchComplete()
            }

            override fun onFailure(call: Call<List<Waiter>>, t: Throwable) {
                Log.e("MangeWaitersAdapter", "Error fetching waiters", t)
                Toast.makeText(
                    context, "Failed to lead waiters. Please try again",
                    Toast.LENGTH_SHORT
                ).show()
                onFetchComplete()
            }
        })
    }

    fun removeWaiter(requestBody: DeleteWaiterRequest, onComplete: () -> Unit) {
        val apiService = RetrofitWaiter.instance
        apiService.deleteWaiter(requestBody).enqueue(object : Callback<DeleteWaiterResponse> {
            override fun onResponse(
                call: Call<DeleteWaiterResponse>,
                response: Response<DeleteWaiterResponse>
            ) {
                if (response.isSuccessful && response.body()?.success == true) {
                    Log.d(
                        "ManageWaitersAdapter",
                        "Waiter successfully removed: ID = ${requestBody.id}"
                    )

                    val position = waiterList.indexOfFirst { it.id == requestBody.id }
                    if (position != -1) {
                        waiterList.removeAt(position)
                        notifyItemRemoved(position)
                    }
                } else {
                    Log.e("ManageWaitersAdapter", "Failed to remove waiter: ${response.message()}")
                }
                onComplete()
            }

            override fun onFailure(call: Call<DeleteWaiterResponse>, t: Throwable) {
                Log.e("ManageWaitersAdapter", "Error deleting waiter", t)
                onComplete()
            }
        })
    }

    class WaiterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val waiterIdTextView: TextView = itemView.findViewById(R.id.waiterIdTextView)
        private val waiterNameTextView: TextView = itemView.findViewById(R.id.waiterNameTextView)
        private val hireDateTextView: TextView = itemView.findViewById(R.id.hireDateTextView)
        private val deleteButton: Button = itemView.findViewById(R.id.deleteWaiterButton)

        fun bind(
            waiter: Waiter, onDeleteClickListener: (DeleteWaiterRequest) -> Unit
        ) {
            // Set the waiter ID, name and date
            waiterIdTextView.text = itemView.context.getString(R.string.waiter_id_format, waiter.id)
            waiterNameTextView.text =
                itemView.context.getString(R.string.waiter_name_format, waiter.waiterName)
            val inputDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val outputDateFormat = SimpleDateFormat("MMMM d, yyyy", Locale.getDefault())

            try {
                val date = inputDateFormat.parse(waiter.hireDate)
                val formattedDate = date?. let { outputDateFormat.format(it) } ?: "Invalid date"
                hireDateTextView.text =
                    itemView.context.getString(R.string.hire_date_format, formattedDate)
            } catch (e: Exception) {
                hireDateTextView.text =
                    itemView.context.getString(R.string.hire_date_format, "Invalid date")
                Log.e("WaiterViewHolder", "Error parsing hire date: ${waiter.hireDate}", e)
            }

            deleteButton.setOnClickListener {
                if (waiter.id > 0) {
                    val deleteRequest = DeleteWaiterRequest(waiter.id)
                    onDeleteClickListener(deleteRequest)
                } else {
                    Log.e("WaiterViewHolder", "Invalid waiter ID: ${waiter.id}")
                }
            }
        }
    }

    class WaiterDiffCallback(
        private val oldList: List<Waiter>,
        private val newList: List<Waiter>
    ) : DiffUtil.Callback() {
        override fun getOldListSize(): Int = oldList.size
        override fun getNewListSize(): Int = newList.size
        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition].id == newList[newItemPosition].id
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition] == newList[newItemPosition]
        }
    }
}