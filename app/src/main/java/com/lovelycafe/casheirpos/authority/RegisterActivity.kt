package com.lovelycafe.casheirpos.authority

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.lovelycafe.casheirpos.R
import com.lovelycafe.casheirpos.api.AuthorityApi
import com.lovelycafe.casheirpos.api.RegisterResponse
import com.lovelycafe.casheirpos.api.User
import com.lovelycafe.casheirpos.api.RetrofitAuthority
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import android.widget.ImageView

class RegisterActivity : AppCompatActivity() {

    private lateinit var userTypeSpinner: Spinner
    private lateinit var usernameEditText: EditText
    private lateinit var phoneEditText: EditText
    private lateinit var addressEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var confirmPasswordEditText: EditText
    private lateinit var passwordToggle: ImageView
    private lateinit var confirmPasswordToggle: ImageView
    private lateinit var registerButton: Button
    private lateinit var loginTextView: TextView

    private val apiService: AuthorityApi by lazy { RetrofitAuthority.instance }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        initializeViews()
        setupSpinner()
        setupClickListeners()
    }

    private fun initializeViews() {
        userTypeSpinner = findViewById(R.id.userTypeSpinner)
        usernameEditText = findViewById(R.id.usernameEditText)
        phoneEditText = findViewById(R.id.phoneEditText)
        addressEditText = findViewById(R.id.addressEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        confirmPasswordEditText = findViewById(R.id.confirmPasswordEditText)
        passwordToggle = findViewById(R.id.passwordToggle)
        confirmPasswordToggle = findViewById(R.id.confirmPasswordToggle)
        registerButton = findViewById(R.id.registerButton)
        loginTextView = findViewById(R.id.loginTextView)
    }

    private fun setupSpinner() {
        val userTypes = arrayOf("Client", "Admin")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, userTypes)
        userTypeSpinner.adapter = adapter

        userTypeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val visibility = if (position == 1) View.GONE else View.VISIBLE
                phoneEditText.visibility = visibility
                addressEditText.visibility = visibility
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun setupClickListeners() {
        registerButton.setOnClickListener {
            handleRegistration()
        }

        loginTextView.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        passwordToggle.setOnClickListener {
            togglePasswordVisibility(passwordEditText, passwordToggle)
        }

        confirmPasswordToggle.setOnClickListener {
            togglePasswordVisibility(confirmPasswordEditText, confirmPasswordToggle)
        }
    }

    private fun togglePasswordVisibility(editText: EditText, toggleIcon: ImageView) {
        val cursorPosition = editText.selectionStart
        if (editText.inputType == android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD) {
            editText.inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
            toggleIcon.setImageResource(R.drawable.ic_eye_closed)
        } else {
            editText.inputType = android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            toggleIcon.setImageResource(R.drawable.ic_eye_open)
        }
        editText.requestFocus()
        editText.setSelection(cursorPosition)
    }

    private fun handleRegistration() {
        val username = usernameEditText.text.toString().trim()
        val phone = phoneEditText.text.toString().trim()
        val address = addressEditText.text.toString().trim()
        val password = passwordEditText.text.toString().trim()
        val confirmPassword = confirmPasswordEditText.text.toString().trim()
        val userType = if (userTypeSpinner.selectedItem.toString() == "Client") "client" else "admin"

        if (!validateInputs(username, phone, address, password, confirmPassword, userType)) {
            return
        }

        val user = User(
            type = userType,
            username = username,
            phoneNumber = if (userType == "client") phone else "",
            address = if (userType == "client") address else "",
            password = password,
            confirmPassword = confirmPassword
        )

        apiService.registerUser(user).enqueue(object : Callback<RegisterResponse> {
            override fun onResponse(call: Call<RegisterResponse>, response: Response<RegisterResponse>) {
                if (response.isSuccessful && response.body()?.success == true) {
                    Toast.makeText(this@RegisterActivity, "Registration Successful", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this@RegisterActivity, LoginActivity::class.java))
                    finish()
                } else {
                    val errorMessage = response.body()?.message ?: "Server Error: ${response.code()}"
                    Toast.makeText(this@RegisterActivity, errorMessage, Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<RegisterResponse>, t: Throwable) {
                Log.e("RegisterActivity", "Network Error", t)
                Toast.makeText(this@RegisterActivity, "Network Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun validateInputs(
        username: String,
        phone: String,
        address: String,
        password: String,
        confirmPassword: String,
        userType: String
    ): Boolean {
        if (TextUtils.isEmpty(username)) {
            usernameEditText.error = "Username is required"
            return false
        }
        if (userType == "client") {
            if (TextUtils.isEmpty(phone)) {
                phoneEditText.error = "Phone number is required"
                return false
            }
            if (TextUtils.isEmpty(address)) {
                addressEditText.error = "Address is required"
                return false
            }
        }
        if (TextUtils.isEmpty(password)) {
            passwordEditText.error = "Password is required"
            return false
        }
        if (password.length < 6) {
            passwordEditText.error = "Password must be at least 6 characters"
            return false
        }
        if (TextUtils.isEmpty(confirmPassword)) {
            confirmPasswordEditText.error = "Please confirm your password"
            return false
        }
        if (password != confirmPassword) {
            confirmPasswordEditText.error = "Passwords do not match"
            return false
        }
        return true
    }
}
