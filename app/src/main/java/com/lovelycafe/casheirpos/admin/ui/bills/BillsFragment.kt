package com.lovelycafe.casheirpos.admin.ui.bills

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.lovelycafe.casheirpos.databinding.FragmentBillsBinding

class BillsFragment : Fragment() {

    private var _binding: FragmentBillsBinding? = null
    private val binding get() = _binding!!

    private val billsViewModel: BillsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBillsBinding.inflate(inflater, container, false)

        setupObservers()

        return binding.root
    }

    private fun setupObservers() {
        billsViewModel.text.observe(viewLifecycleOwner) { newText ->
            binding.textBills.text = newText
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}