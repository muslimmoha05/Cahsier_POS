package com.lovelycafe.casheirpos.admin.ui.orders

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.lovelycafe.casheirpos.R
import com.lovelycafe.casheirpos.adapter.OrdersAdapter
import com.lovelycafe.casheirpos.api.*
import com.lovelycafe.casheirpos.printer.BluetoothPrinterManager
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import androidx.core.app.NotificationCompat
import android.app.NotificationManager
import android.content.Context
import android.media.RingtoneManager
import android.os.Handler
import android.os.Looper
import android.widget.TextView


class OrdersFragment : Fragment() {

    private lateinit var ordersRecyclerView: RecyclerView
    private lateinit var ordersAdapter: OrdersAdapter
    private val ordersList = mutableListOf<Order>()
    private lateinit var progressBar: ProgressBar
    private lateinit var noOrdersTextView: TextView
    private val bluetoothPrinterManager by lazy { BluetoothPrinterManager(requireContext()) }

    private val requestPermissionsLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            if (permissions.values.any { !it }) {
                Toast.makeText(requireContext(), "Bluetooth permissions are required.", Toast.LENGTH_SHORT).show()
            }
        }

    private val requestNotificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                fetchOrders() // Call fetchOrders again to send notification if needed
            } else {
                Toast.makeText(requireContext(), "Notification permission is required to send notifications.", Toast.LENGTH_SHORT).show()
            }
        }

    private lateinit var handler: Handler
    private lateinit var fetchOrdersRunnable: Runnable

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_orders, container, false).apply {
            ordersRecyclerView = findViewById(R.id.ordersRecyclerView)
            progressBar = findViewById(R.id.progressBar)
            noOrdersTextView = findViewById(R.id.text_no_orders)
        }

        ordersAdapter = OrdersAdapter(ordersList, ::cancelOrder, ::printOrder)
        ordersRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        ordersRecyclerView.adapter = ordersAdapter

        checkNotificationPermission()

        handler = Handler(Looper.getMainLooper())

        fetchOrdersRunnable = Runnable {
            fetchOrders()
            handler.postDelayed(fetchOrdersRunnable, 10000)
        }

        return view
    }

    override fun onStart() {
        super.onStart()
        // Start fetching orders when the fragment is visible
        handler.post(fetchOrdersRunnable)
    }

    override fun onStop() {
        super.onStop()
        // Stop fetching orders when the fragment is not visible
        handler.removeCallbacks(fetchOrdersRunnable)
    }

    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    // Permission is granted, proceed with fetching orders
                    fetchOrders()
                }
                else -> {
                    // Request the permission
                    requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        } else {
            // For devices below Android 13, directly fetch orders
            fetchOrders()
        }
    }

    private fun ensureBluetoothPermissions(): Boolean {
        val requiredPermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(Manifest.permission.BLUETOOTH_CONNECT)
        } else {
            arrayOf(Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN)
        }

        val missingPermissions = requiredPermissions.filter {
            ActivityCompat.checkSelfPermission(requireContext(), it) != PackageManager.PERMISSION_GRANTED
        }

        return if (missingPermissions.isNotEmpty()) {
            requestPermissionsLauncher.launch(missingPermissions.toTypedArray())
            false
        } else true
    }

    private fun fetchOrders() {
        progressBar.visibility = View.VISIBLE
        RetrofitOrder.instance.fetchOrder().enqueue(object : Callback<OrdersResponse> {
            override fun onResponse(call: Call<OrdersResponse>, response: Response<OrdersResponse>) {
                progressBar.visibility = View.GONE
                if (response.isSuccessful) {
                    response.body()?.let {
                        if (it.success) {
                            // Check if there are new orders
                            if (ordersList.isEmpty() && it.orders.isNotEmpty()) {
                                sendOrderNotification()
                            }
                            ordersAdapter.updateOrders(it.orders)
                            noOrdersTextView.visibility = View.GONE // Hide placeholder
                        } else {
                            // Show placeholder if no completed orders are found
                            noOrdersTextView.visibility = View.VISIBLE
                            ordersAdapter.updateOrders(emptyList())
                        }
                    }
                } else {
                    Toast.makeText(requireContext(), "Failed to fetch orders", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<OrdersResponse>, t: Throwable) {
                progressBar.visibility = View.GONE
                Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun sendOrderNotification() {
        val notificationManager = requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationId = 1

        val builder = NotificationCompat.Builder(requireContext(), "order_notifications")
            .setSmallIcon(R.drawable.ic_notifications)
            .setContentTitle("New Orders Arrived")
            .setContentText("You have new orders.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))

        notificationManager.notify(notificationId, builder.build())
    }

    private fun printOrder(order: Order) {
        if (!ensureBluetoothPermissions()) return

        lifecycleScope.launch {
            try {
                val printerDevice: BluetoothDevice? = bluetoothPrinterManager.findPrinter("Inner printer")
                if (printerDevice == null) {
                    Toast.makeText(requireContext(), "Printer not found.", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                val connected = bluetoothPrinterManager.connectToPrinter(printerDevice)
                if (!connected) {
                    Toast.makeText(requireContext(), "Failed to connect to printer.", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                printOrderDetails(order)

                val completedOrderRequest = CompletedOrderRequest(
                    id = order.id,
                    waiterName = order.waiterName,
                    // tableNumber = order.tableNumber,
                    foodName = order.foodName,
                    quantity = order.quantity,
                    totalPrice = order.totalPrice,
                    orderTime = order.orderTime
                )

                if (order.id <= 0) {
                    Toast.makeText(requireContext(), "Invalid order ID.", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                val orderId = order.id

                sendCompletedOrder(completedOrderRequest, orderId)

            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                bluetoothPrinterManager.disconnect()
            }
        }
    }

    private suspend fun printOrderDetails(order: Order) {
        // Create a larger text for order ID and waiter name
        val orderDetailsText = "            ${order.id}   :     ${order.waiterName}"
        val orderDetailsBitmap = bluetoothPrinterManager.textToImage(orderDetailsText, textSize = 32f) // Larger size

        // Print the larger text
        bluetoothPrinterManager.printBitmap(orderDetailsBitmap)

        // Print the separator line
        bluetoothPrinterManager.printAmharicText("_____________________________________________________")

        // Print food details with normal size
        val foodDetailsText = "${order.foodName}      x      ${order.quantity}   =   ${order.totalPrice} ብር"
        bluetoothPrinterManager.printAmharicText(foodDetailsText)

        // Print another separator line
        bluetoothPrinterManager.printAmharicText("_____________________________________________________")

        // Print order time with normal size
        bluetoothPrinterManager.printAmharicText(" ${order.orderTime} ")

        // Print the final separator line
        bluetoothPrinterManager.printAmharicText("_____________________________________________________")

        // Feed and cut
        bluetoothPrinterManager.printData("\n\n")

        // Notify the user
        Toast.makeText(requireContext(), "Printed successfully.", Toast.LENGTH_SHORT).show()
    }

    private fun sendCompletedOrder(completedOrderRequest: CompletedOrderRequest, orderId: Int) {
        RetrofitOrder.instance.sendCompletedOrder(completedOrderRequest).enqueue(object : Callback<CompletedOrderResponse> {
            override fun onResponse(call: Call<CompletedOrderResponse>, response: Response<CompletedOrderResponse>) {
                if (response.isSuccessful) {
                    response.body()?.let { completedOrderResponse ->
                        if (completedOrderResponse.success) {
                            Toast.makeText(requireContext(), "Order sent successfully.", Toast.LENGTH_SHORT).show()

                            ordersAdapter.updateOrders(ordersList)

                            cancelOrder(orderId)
                        } else {
                            Toast.makeText(requireContext(), "Failed to send order: ${completedOrderResponse.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(requireContext(), "Failed to send order.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<CompletedOrderResponse>, t: Throwable) {
                Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
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
                            ordersAdapter.updateOrders(ordersList)
                            fetchOrders()
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
}