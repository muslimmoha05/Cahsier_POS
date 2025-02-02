package com.lovelycafe.casheirpos.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.lovelycafe.casheirpos.api.CompletedOrder
import com.lovelycafe.casheirpos.R

class CompletedOrdersAdapter(
    private var completedOrders: List<CompletedOrder> = emptyList(),
) : RecyclerView.Adapter<CompletedOrdersAdapter.OrderViewHolder>() {

    class OrderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val orderId: TextView = itemView.findViewById(R.id.orderId)
        val waiterName: TextView = itemView.findViewById(R.id.waiterName)
        // val tableNumber: TextView = itemView.findViewById(R.id.tableNumber)
        val foodName: TextView = itemView.findViewById(R.id.foodName)
        val quantity: TextView = itemView.findViewById(R.id.quantity)
        val totalPrice: TextView = itemView.findViewById(R.id.totalPrice)
        val orderTime: TextView = itemView.findViewById(R.id.orderTime)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.completed_orders_item, parent, false)
        return OrderViewHolder(view)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val completedOrder = completedOrders[position]
        val itemViewContext = holder.itemView.context

        holder.orderId.text = itemViewContext.getString(R.string.order_id, completedOrder.id)
        holder.waiterName.text = itemViewContext.getString(R.string.order_waiter_name, completedOrder.waiterName)
        // holder.tableNumber.text = itemViewContext.getString(R.string.order_table_number, completedOrder.tableNumber)
        holder.foodName.text = itemViewContext.getString(R.string.item_format, completedOrder.foodName)
        holder.quantity.text = itemViewContext.getString(R.string.quantity_format, completedOrder.quantity)
        holder.totalPrice.text = itemViewContext.getString(R.string.order_total_price, completedOrder.totalPrice)
        holder.orderTime.text = itemViewContext.getString(R.string.order_time, completedOrder.orderTime)

    }

    override fun getItemCount(): Int = completedOrders.size

    fun updateCompletedOrders(newCompletedOrders: List<CompletedOrder>?) {
        if (newCompletedOrders == null) return // or handle it as needed

        val oldCompletedOrders = completedOrders.toList()
        completedOrders = newCompletedOrders

        val oldSize = oldCompletedOrders.size
        val newSize = newCompletedOrders.size

        if (newSize > oldSize) {
            notifyItemRangeInserted(oldSize, newSize - oldSize)
        } else if (oldSize > newSize) {
            notifyItemRangeRemoved(newSize, oldSize - newSize)
        }

        for (i in 0 until minOf(oldSize, newSize)) {
            if (oldCompletedOrders[i] != newCompletedOrders[i]) {
                notifyItemChanged(i)
            }
        }
    }
}