package com.lovelycafe.casheirpos.api

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

data class Image(
    val id: Int,
    val name: String?,
    val translated: String?,
    val path: String?
)

data class DeleteCategoryRequest(
    val id: Int
)

interface CategoryApi {
    @GET("get_categories.php")
    fun getImages(): Call<List<Image>>

    @Multipart
    @POST("add_categories.php")
    fun addCategory(
        @Part image: MultipartBody.Part,
        @Part("name") name: RequestBody,
        @Part("translated") translated: RequestBody
    ): Call<AddCategoryResponse>

    @Multipart
    @POST("update_categories.php")
    fun updateCategory(
        @Part image: MultipartBody.Part?,
        @Part("name") name: RequestBody?,
        @Part("translated") translated: RequestBody,
        @Part("id") id: RequestBody
    ): Call<UpdateCategoryResponse>

    @POST("delete_categories.php")
    fun deleteCategory(
        @Body requestBody: DeleteCategoryRequest
    ): Call<DeleteCategoryResponse>
}

open class ApiResponse(
    val success: Boolean
)

data class AddCategoryResponse(
    val path: String,
    val id: Int? = null
) : ApiResponse(success = true)
data class UpdateCategoryResponse(
    val path: String? = null
) : ApiResponse(success = true)
data class DeleteCategoryResponse(
    val deletedId: Int? = null
) : ApiResponse(success = true)