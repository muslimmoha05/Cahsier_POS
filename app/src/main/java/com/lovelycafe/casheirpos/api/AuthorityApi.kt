package com.lovelycafe.casheirpos.api

import retrofit2.Call
import retrofit2.http.POST
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers


data class User(
    val type: String,
    val username: String,
    val phoneNumber: String,
    val address: String,
    val password: String,
    val confirmPassword: String
)

data class Login(
    val username: String,
    val password: String,
    val type: String
)
data class UserDetails(
    val username: String = "",
    val type: String = ""
)

data class Profile(
    val id: Int,
    val username: String?,
    val phoneNumber: String? = null,
    val address: String? = null,
    val profileImage: String?
)

interface AuthorityApi {
    @POST("register.php")
    @Headers("Content-Type: application/json")
    fun registerUser(@Body user: User): Call<RegisterResponse>

    @POST("login.php")
    @Headers("Content-Type: application/json")
    fun loginUser(@Body login: Login): Call<LoginResponse>

    @GET("get_client_profile.php")
    fun getClient(): Call<List<Profile>>

    @GET("get_admin_profile.php")
    fun getAdmin(): Call<List<Profile>>
}

data class LoginResponse(
    val success: Boolean = false,
    val message: String = "",
    val user: UserDetails? = null
)
data class RegisterResponse(
    val success: Boolean,
    val message: String
)