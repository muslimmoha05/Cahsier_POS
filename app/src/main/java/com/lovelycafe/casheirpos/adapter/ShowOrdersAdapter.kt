package com.lovelycafe.casheirpos.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.lovelycafe.casheirpos.api.Order
import com.lovelycafe.casheirpos.R

class ShowOrdersAdapter(
    private var orders: List<Order>,
    private val onCancelOrder: (Int) -> Unit
) : RecyclerView.Adapter<ShowOrdersAdapter.OrderViewHolder>() {

    class OrderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val orderId: TextView = itemView.findViewById(R.id.orderId)
        val waiterName: TextView = itemView.findViewById(R.id.waiterName)
        // val tableNumber: TextView = itemView.findViewById(R.id.tableNumber)
        val foodName: TextView = itemView.findViewById(R.id.foodName)
        val quantity: TextView = itemView.findViewById(R.id.quantity)
        val totalPrice: TextView = itemView.findViewById(R.id.totalPrice)
        val orderTime: TextView = itemView.findViewById(R.id.orderTime)
        val cancelButton: Button = view.findViewById(R.id.cancelButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.show_order_item, parent, false)
        return OrderViewHolder(view)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        if (position < 0 || position >= orders.size) {
            return
        }
        val order = orders[position]
        val itemViewContext = holder.itemView.context

        holder.orderId.text = itemViewContext.getString(R.string.order_id, order.id)
        holder.waiterName.text = itemViewContext.getString(R.string.order_waiter_name, order.waiterName)
        // holder.tableNumber.text = itemViewContext.getString(R.string.order_table_number, order.tableNumber)
        holder.foodName.text = itemViewContext.getString(R.string.item_format, order.foodName)
        holder.quantity.text = itemViewContext.getString(R.string.quantity_format, order.quantity)
        holder.totalPrice.text = itemViewContext.getString(R.string.order_total_price, order.totalPrice)
        holder.orderTime.text = itemViewContext.getString(R.string.order_time, order.orderTime)

        holder.cancelButton.setOnClickListener {
            val builder = androidx.appcompat.app.AlertDialog.Builder(itemViewContext)
            builder.setTitle("ትዕዛዝ አጥፋ")
            builder.setMessage("ይህ ትዕዛዝ እንዲጠፋ ይፈልጋሉ?")
            builder.setPositiveButton("አዎ") { dialog, _ ->
                onCancelOrder(order.id)
                dialog.dismiss()
            }
            builder.setNegativeButton("አልፈልግም") { dialog, _ ->
                dialog.dismiss()
            }
            builder.show()
        }

    }

    override fun getItemCount(): Int = orders.size

    fun updateOrders(newOrders: List<Order>) {
        val oldOrders = orders.toList()
        orders = newOrders

        val oldSize = oldOrders.size
        val newSize = newOrders.size

        if (newSize > oldSize) {
            notifyItemRangeInserted(oldSize, newSize - oldSize)
        } else if (oldSize > newSize) {
            notifyItemRangeRemoved(newSize, oldSize - newSize)
        }

        for (i in 0 until minOf(oldSize, newSize)) {
            if (oldOrders[i] != newOrders[i]) {
                notifyItemChanged(i)
            }
        }
    }
}