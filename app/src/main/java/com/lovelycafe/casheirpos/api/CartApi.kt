package com.lovelycafe.casheirpos.api

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Body
import retrofit2.http.Headers

data class CartItem(
    val id: Int,
    val foodName: String,
    val price: Double,
    val quantity: Int
)
data class UpdateCartRequest(
    val foodName: String,
    val quantity: Int
)
data class DeleteCartRequest(
    val id: Int
)


interface CartApi {
    @GET("get_cart.php")
    fun fetchCart(): Call<List<CartItem>>

    @Headers("Content-Type: application/json")
    @POST("update_cart.php")
    fun updateCart(@Body requestBody: UpdateCartRequest): Call<UpdateCartResponse>

    @Headers("Content-Type: application/json")
    @POST("delete_cart.php")
    fun deleteCart(@Body requestBody: DeleteCartRequest): Call<DeleteCartResponse>

    @Headers("Content-Type: application/json")
    @POST("delete_all_cart.php")
    fun clear(): Call<ClearCartResponse>
}

data class DeleteCartResponse(
    val status: String,
    val message: String
)
data class UpdateCartResponse(
    val success: Boolean,
    val message: String
)
data class ClearCartResponse(
    val success: Boolean,
    val message: String
)
