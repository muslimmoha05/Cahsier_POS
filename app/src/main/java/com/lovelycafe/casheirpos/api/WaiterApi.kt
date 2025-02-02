package com.lovelycafe.casheirpos.api

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Body
import retrofit2.http.Headers

data class Waiter(
    val id: Int,
    val waiterName: String,
    val hireDate: String
)
data class AddWaiterRequest(
    val waiterName: String,
    val hireDate: String
)
data class DeleteWaiterRequest(
    val id: Int
)

// Retrofit interface for API calls
interface WaiterApi {

    @GET("get_waiters.php")
    fun fetchWaiters(): Call<List<Waiter>>

    @Headers("Content-Type: application/json")
    @POST("add_waiter.php")
    fun addWaiter(@Body requestBody: AddWaiterRequest): Call<AddWaiterResponse>

    @Headers("Content-Type: application/json")
    @POST("delete_waiter.php")
    fun deleteWaiter(@Body requestBody: DeleteWaiterRequest): Call<DeleteWaiterResponse>
}

data class AddWaiterResponse(
    val status: String,
    val message: String
)
data class DeleteWaiterResponse(
    val success: Boolean,
    val message: String
)