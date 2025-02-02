package com.lovelycafe.casheirpos.client.ui.bill

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.lovelycafe.casheirpos.databinding.FragmentBillBinding

class BillFragment : Fragment() {

    private var _binding: FragmentBillBinding? = null
    private val binding get() = _binding!!

    private val billViewModel: BillViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBillBinding.inflate(inflater, container, false)

        setupObservers()

        return binding.root
    }

    private fun setupObservers() {
        billViewModel.text.observe(viewLifecycleOwner) { newText ->
            binding.textBill.text = newText
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}