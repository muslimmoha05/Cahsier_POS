package com.lovelycafe.casheirpos.admin.ui.users

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.lovelycafe.casheirpos.authority.LoginActivity
import com.lovelycafe.casheirpos.databinding.FragmentUsersBinding

class UsersFragment : Fragment() {

    private var _binding: FragmentUsersBinding? = null
    private val binding get() = _binding!!

    // LiveData-like properties removed, directly handling logout
    private var shouldLogout = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUsersBinding.inflate(inflater, container, false)

        setupListeners()

        return binding.root
    }

    private fun setupListeners() {
        binding.buttonLogout.setOnClickListener {
            handleLogout()
        }
    }

    private fun handleLogout() {
        // Handle logout session and clear SharedPreferences
        logoutSession()

        // Create and show the customized toast
        val toast = Toast.makeText(requireContext(), "Logged out successfully!", Toast.LENGTH_SHORT)
        toast.setGravity(Gravity.END, 0, 0) // Center the toast on the screen
        toast.show()

        // Navigate to the login screen after successful logout
        navigateToLogin()
    }

    private fun logoutSession() {
        // Directly handling the logout event
        shouldLogout = true
    }

    private fun navigateToLogin() {
        // Clear user session in SharedPreferences
        val sharedPreferences = requireActivity().getSharedPreferences("LovelyCafePrefs", Context.MODE_PRIVATE)
        sharedPreferences.edit().clear().apply()

        // Navigate back to the login screen
        val intent = Intent(requireActivity(), LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK // Clears back stack
        startActivity(intent)
        requireActivity().finish()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}