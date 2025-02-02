package com.lovelycafe.casheirpos.api

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.Query

// Define the data class to represent a Food
data class Food(
    val id: Int,
    val foodName: String,
    val categoryName: String,
    val price: Double,
    var quantity: Int = 1
)
// Define the data class to add a Food
data class AddFoodRequest(
    val foodName: String,
    val categoryName: String,
    val price: Double
)
// Define the data class to delete a Food
data class DeleteFoodRequest(
    val id: Int
)

// Retrofit interface for API calls
interface FoodApi {

    // Fetch all foods
    @GET("get_all_foods.php")
    fun fetchFoods(): Call<List<Food>>
    // Fetch food list
    @GET("get_foods.php")
    fun fetchListFoods(@Query("category_id") categoryId: Int): Call<List<Food>>
    // Add a new category
    @Headers("Content-Type: application/json")
    @POST("add_foods.php")
    fun addFood(@Body requestBody: AddFoodRequest): Call<AddFoodResponse>
    // Delete a category
    @Headers("Content-Type: application/json")
    @POST("delete_foods.php")
    fun deleteFood(@Body requestBody: DeleteFoodRequest): Call<DeleteFoodResponse>
}

data class AddFoodResponse(
    val status: String,
    val message: String
)
data class DeleteFoodResponse(
    val success: Boolean,
    val message: String
)