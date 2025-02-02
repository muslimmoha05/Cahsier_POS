package com.lovelycafe.casheirpos.admin.ui.dashboard

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.lovelycafe.casheirpos.R
import com.lovelycafe.casheirpos.databinding.FragmentDashboardBinding

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    private val dashboardViewModel: DashboardViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)

        setupObservers()
        setupButtonClickListener()

       return binding.root
    }

    private fun setupObservers() {
        dashboardViewModel.text.observe(viewLifecycleOwner) { newText ->
            binding.textDashboard.text = newText
        }
    }

    private fun setupButtonClickListener() {
        val manageCategoriesButton: Button = binding.manageCategoriesButton
        val manageFoodsButton: Button = binding.manageFoodsButton
        val manageWaitersButton: Button = binding.manageWaitersButton
        val completedOrdersButton: Button = binding.completedOrdersButton
        val orderSummaryButton: Button = binding.orderSummaryButton


        // Set up button clicks for fragment navigation
        manageCategoriesButton.setOnClickListener {
            val intent = Intent(requireContext(), ManageCategoriesActivity::class.java)
            startActivity(intent)
        }

        // Set up click listener for managing foods
        manageFoodsButton.setOnClickListener {
            val manageFoodsFragment = ManageFoodsFragment()
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.nav_host_fragment_activity_admin_main, manageFoodsFragment)
                .addToBackStack(null) // Optional: Add to back stack to allow back navigation
                .commit()
        }

        // Set up click listener for managing waiters
        manageWaitersButton.setOnClickListener {
            val manageWaitersFragment = ManageWaitersFragment()
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.nav_host_fragment_activity_admin_main, manageWaitersFragment)
                .addToBackStack(null) // Optional: Add to back stack to allow back navigation
                .commit()
        }

        completedOrdersButton.setOnClickListener {
            val completedOrdersFragment = CompletedOrdersFragment()
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.nav_host_fragment_activity_admin_main, completedOrdersFragment)
                .addToBackStack(null)
                .commit()
        }

        orderSummaryButton.setOnClickListener {
            val summaryFragment = SummaryFragment()
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.nav_host_fragment_activity_admin_main, summaryFragment)
                .addToBackStack(null)
                .commit()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}