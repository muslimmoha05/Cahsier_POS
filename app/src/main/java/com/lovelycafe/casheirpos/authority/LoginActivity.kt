package com.lovelycafe.casheirpos.authority

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.lovelycafe.casheirpos.R
import com.lovelycafe.casheirpos.admin.AdminMainActivity
import com.lovelycafe.casheirpos.api.Constants
import com.lovelycafe.casheirpos.api.Login
import com.lovelycafe.casheirpos.api.LoginResponse
import com.lovelycafe.casheirpos.api.RetrofitAuthority
import com.lovelycafe.casheirpos.client.MainActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {

    private lateinit var usernameEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginButton: Button
    private lateinit var registerLink: TextView
    private lateinit var userTypeGroup: RadioGroup
    private lateinit var passwordToggle: ImageView

    private val apiService = RetrofitAuthority.instance

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Initialize views
        usernameEditText = findViewById(R.id.username)
        passwordEditText = findViewById(R.id.password)
        loginButton = findViewById(R.id.loginButton)
        registerLink = findViewById(R.id.registerLink)
        userTypeGroup = findViewById(R.id.userTypeGroup)
        passwordToggle = findViewById(R.id.passwordToggle)

        // Set up the Register link
        registerLink.setOnClickListener {
            startActivity(Intent(this@LoginActivity, RegisterActivity::class.java))
        }

        // Set up the Login button
        loginButton.setOnClickListener {
            loginUser()
        }

        passwordToggle.setOnClickListener {
            togglePasswordVisibility()
        }
    }

    private fun togglePasswordVisibility() {
        if (passwordEditText.inputType == android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD) {
            passwordEditText.inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
            passwordToggle.setImageResource(R.drawable.ic_eye_closed) // Change to closed eye icon
        } else {
            passwordEditText.inputType = android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            passwordToggle.setImageResource(R.drawable.ic_eye_open) // Change to open eye icon
        }
        // Move the cursor to the end of the text
        passwordEditText.setSelection(passwordEditText.text.length)
    }

    private fun loginUser() {
        val username = usernameEditText.text.toString().trim()
        val password = passwordEditText.text.toString().trim()

        // Get selected user type
        val selectedUserTypeId = userTypeGroup.checkedRadioButtonId
        val userType = when (selectedUserTypeId) {
            R.id.radioAdmin -> "admin"
            R.id.radioClient -> "client"
            else -> {
                Toast.makeText(this, "Please select user type", Toast.LENGTH_SHORT).show()
                return
            }
        }

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill out all fields", Toast.LENGTH_SHORT).show()
            return
        }

        val login = Login(
            username = username,
            password = password,
            type = userType
        )

        apiService.loginUser(login).enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                try {
                    // Log the raw response
                    val rawResponse = response.raw().toString()
                    Log.d("LoginActivity", "Raw Response: $rawResponse")

                    // Log the response body
                    val responseBody = response.body()
                    Log.d("LoginActivity", "Response Body: $responseBody")

                    if (response.isSuccessful) {
                        val loginResponse = response.body()
                        if (loginResponse?.success == true) {
                            // Save the user type in SharedPreferences
                            val sharedPreferences = this@LoginActivity.getSharedPreferences("LovelyCafePrefs", MODE_PRIVATE)
                            val editor = sharedPreferences.edit()

                            val userTypeFromResponse = loginResponse.user?.type ?: "client"
                            editor.putString(Constants.USER_TYPE_KEY, userTypeFromResponse)
                            editor.apply()

                            Toast.makeText(this@LoginActivity, loginResponse.message, Toast.LENGTH_SHORT).show()

                            // Redirect based on user type
                            if (userTypeFromResponse == "admin") {
                                startActivity(Intent(this@LoginActivity, AdminMainActivity::class.java))
                            } else {
                                startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                            }

                            finish() // Close the login activity
                        } else {
                            Toast.makeText(this@LoginActivity, loginResponse?.message ?: "Login failed", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        val errorBody = response.errorBody()?.string()
                        Log.e("LoginActivity", "Error Response: $errorBody")
                        Toast.makeText(this@LoginActivity, "Login failed: $errorBody", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Log.e("LoginActivity", "Error processing response", e)
                    e.printStackTrace()
                    Toast.makeText(this@LoginActivity, "Error processing response: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                Log.e("LoginActivity", "Request failed", t)
                Toast.makeText(this@LoginActivity, "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
