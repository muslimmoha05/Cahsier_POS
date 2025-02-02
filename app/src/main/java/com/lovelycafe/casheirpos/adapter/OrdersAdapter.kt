package com.lovelycafe.casheirpos.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.lovelycafe.casheirpos.R
import com.lovelycafe.casheirpos.api.Order

class OrdersAdapter(
    private var orders: List<Order>,
    private val onCancelOrder: (Int) -> Unit,
    private val onPrintOrder: (Order) -> Unit
) : RecyclerView.Adapter<OrdersAdapter.OrderViewHolder>() {

    class OrderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val orderId: TextView = view.findViewById(R.id.orderId)
        val waiterName: TextView = view.findViewById(R.id.waiterName)
        // val tableNumber: TextView = view.findViewById(R.id.tableNumber)
        val foodName: TextView = view.findViewById(R.id.foodName)
        val quantity: TextView = view.findViewById(R.id.quantity)
        val totalPrice: TextView = view.findViewById(R.id.totalPrice)
        val orderTime: TextView = view.findViewById(R.id.orderTime)
        val printButton: Button = view.findViewById(R.id.printButton)
        val cancelButton: Button = view.findViewById(R.id.cancelButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_orders, parent, false)
        return OrderViewHolder(view)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val order = orders[position]
        val itemViewContext = holder.itemView.context

        holder.orderId.text = itemViewContext.getString(R.string.order_id, order.id)
        holder.waiterName.text = itemViewContext.getString(R.string.order_waiter_name, order.waiterName)
        // holder.tableNumber.text = itemViewContext.getString(R.string.order_table_number, order.tableNumber)
        holder.foodName.text = itemViewContext.getString(R.string.item_format, order.foodName)
        holder.quantity.text = itemViewContext.getString(R.string.quantity_format, order.quantity)
        holder.totalPrice.text = itemViewContext.getString(R.string.order_total_price, order.totalPrice)
        holder.orderTime.text = itemViewContext.getString(R.string.order_time, order.orderTime)

        holder.printButton.setOnClickListener {
            onPrintOrder(order)
        }

        holder.cancelButton.setOnClickListener {
            val builder = androidx.appcompat.app.AlertDialog.Builder(itemViewContext)
            builder.setTitle("ትዕዛዙን አጥፋ")
            builder.setMessage("ይህን ትዕዛዝ ማጥፋት ይፈልጋሉ?")
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
