package com.lovelycafe.casheirpos.client.ui.profile

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.lovelycafe.casheirpos.R
import com.lovelycafe.casheirpos.api.RetrofitAuthority
import com.lovelycafe.casheirpos.authority.LoginActivity
import com.lovelycafe.casheirpos.databinding.FragmentProfileBinding
import com.lovelycafe.casheirpos.databinding.DialogUpdateProfileBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private var selectedProfileImageUri: Uri? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)

        fetchClientProfile()
        setupListeners()

        return binding.root
    }

    private fun fetchClientProfile() {
        lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    RetrofitAuthority.instance.getClient().execute()
                }

                if (response.isSuccessful && response.body() != null) {
                    val profileList = response.body()!!
                    if (profileList.isNotEmpty()) {
                        val profile = profileList[0]
                        displayProfile(profile.username, profile.phoneNumber, profile.address, profile.profileImage)
                    }
                } else {
                    Toast.makeText(requireContext(), "Failed to fetch profile!", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun displayProfile(username: String?, phoneNumber: String?, address: String?, profileImage: String?) {
        binding.textViewUsername.text = username ?: "N/A"
        binding.textViewPhoneNumber.text = phoneNumber ?: "N/A"
        binding.textViewAddress.text = address ?: "N/A"

        // Load the profile picture using Glide
        Glide.with(this)
            .load("https://utopiacorehub.com/lovelycafe_api/auth/${profileImage}")
            .placeholder(R.drawable.ic_profile_placeholder)
            .error(R.drawable.ic_error_profile_placeholder)
            .into(binding.imageViewProfile)
    }

    private fun setupListeners() {
        binding.buttonUpdateProfile.setOnClickListener {
            showUpdateProfileDialog()
        }

        // Add logout functionality to the logout button
        binding.buttonLogout.setOnClickListener {
            handleLogout()
        }
    }

    private fun handleLogout() {
        // Clear user session in SharedPreferences
        val sharedPreferences = requireActivity().getSharedPreferences("LovelyCafePrefs", Context.MODE_PRIVATE)
        sharedPreferences.edit().clear().apply()

        // Show logout success message
        Toast.makeText(requireContext(), "Logged out successfully!", Toast.LENGTH_SHORT).show()

        // Navigate to the login screen
        navigateToLogin()
    }

    private fun navigateToLogin() {
        // Clear back stack and navigate to the login screen
        val intent = Intent(requireActivity(), LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        requireActivity().finish()
    }

    private fun showUpdateProfileDialog() {
        val dialogBinding = DialogUpdateProfileBinding.inflate(layoutInflater)

        // Set a click listener for the profile image
        dialogBinding.imageProfile.setOnClickListener {
            openImageChooser()
        }

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Update Profile")
            .setView(dialogBinding.root)
            .setPositiveButton("Save") { _, _ ->
                val username = dialogBinding.editUsername.text.toString()
                val phoneNumber = dialogBinding.editPhoneNumber.text.toString()
                val address = dialogBinding.editAddress.text.toString()
                val password = dialogBinding.editPassword.text.toString()

                // Handle saving the updated details
                if (selectedProfileImageUri != null) {
                    Toast.makeText(requireContext(), "Profile picture and details updated!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Details updated without changing profile picture!", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .create()

        dialog.show()
    }

    private fun openImageChooser() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        imagePickerLauncher.launch(intent)
    }

    private val imagePickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            selectedProfileImageUri = result.data?.data
            Toast.makeText(requireContext(), "Profile picture selected!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
