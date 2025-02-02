package com.lovelycafe.casheirpos.api

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

data class OrderRequest(
    val waiterName: String,
    // val tableNumber: Int,
    val foodName: String,
    val quantity: Int,
    val totalPrice: Double,
    val orderTime: String
)

data class CompletedOrderRequest(
    val id: Int,
    val waiterName: String,
    // val tableNumber: String,
    val foodName: String,
    val quantity: Int,
    val totalPrice: Double,
    val orderTime: String
)

data class Order(
    val id: Int,
    val waiterName: String,
    // val tableNumber: String,
    val foodName: String,
    val quantity: Int,
    val totalPrice: Double,
    val orderTime: String
)

data class CompletedOrder(
    val id: Int,
    val waiterName: String,
    // val tableNumber: String,
    val foodName: String,
    val quantity: Int,
    val totalPrice: Double,
    val orderTime: String,
)

data class SummaryReport(
    val totalOrders: Int,
    val totalSum: String,
    val selectedItems: Double
)

data class CancelOrderRequest(
    val id: Int
)

interface OrderApi {
    @Headers("Content-Type: application/json")
    @POST("send_order.php")
    fun sendOrder(@Body orderRequest: OrderRequest): Call<OrderResponse>

    @Headers("Content-Type: application/json")
    @POST("send_completed_orders.php")
    fun sendCompletedOrder(@Body orderRequest: CompletedOrderRequest): Call<CompletedOrderResponse>

    @Headers("Content-Type: application/json")
    @POST("get_orders.php")
    fun fetchOrder(): Call<OrdersResponse>

    @Headers("Content-Type: application/json")
    @POST("get_completed_orders.php")
    fun fetchCompletedOrder(): Call<CompletedOrdersResponse>

    @Headers("Content-Type: application/json")
    @POST("summary.php")
    fun fetchSummary(): Call<SummaryResponse>

    @Headers("Content-Type: application/json")
    @POST("delete_order.php")
    fun cancelOrder(@Body requestBody: CancelOrderRequest): Call<CancelOrderResponse>

    @Headers("Content-Type: application/json")
    @POST("delete_all_completed_orders.php")
    fun clearCompleted(): Call<ClearCompletedOrdersResponse>

}

data class OrderResponse(
    val success: Boolean,
    val message: String
)

data class CompletedOrderResponse(
    val success: Boolean,
    val message: String
)

data class OrdersResponse(
    val success: Boolean,
    val orders: List<Order>
)

data class CompletedOrdersResponse(
    val success: Boolean,
    val completedOrders: List<CompletedOrder>
)

data class SummaryResponse(
    val success: Boolean,
    val data: SummaryReport
)

data class CancelOrderResponse(
    val success: Boolean,
    val message: String
)

data class ClearCompletedOrdersResponse(
    val success: Boolean,
    val message: String
)