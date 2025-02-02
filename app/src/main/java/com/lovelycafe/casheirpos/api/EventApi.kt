package com.lovelycafe.casheirpos.api

import retrofit2.Call
import retrofit2.http.Headers
import retrofit2.http.POST

interface EventApi {

    @Headers("Content-Type: application/json")
    @POST("reset_auto_increment_orders.php")
    fun resetAutoIncrementOrders(): Call<ResetOrdersResponse>
}

data class ResetOrdersResponse(
    val success: Boolean,
    val message: String
)