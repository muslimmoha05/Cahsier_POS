package com.lovelycafe.casheirpos.admin.ui.dashboard

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.lovelycafe.casheirpos.R
import com.lovelycafe.casheirpos.api.AddWaiterRequest
import com.lovelycafe.casheirpos.api.AddWaiterResponse
import com.lovelycafe.casheirpos.api.DeleteWaiterRequest
import com.lovelycafe.casheirpos.api.Waiter
import com.lovelycafe.casheirpos.api.RetrofitWaiter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import androidx.recyclerview.widget.GridLayoutManager
import com.lovelycafe.casheirpos.adapter.ManageWaitersAdapter
import java.text.SimpleDateFormat
import java.util.Locale

class ManageWaitersFragment : Fragment() {

    private lateinit var adapter: ManageWaitersAdapter
    private lateinit var waitersRecyclerView: RecyclerView
    private lateinit var addWaiterButton: MaterialButton
    private val waiterList: MutableList<Waiter> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_manage_waiters, container, false)

        waitersRecyclerView = view.findViewById(R.id.waitersRecyclerView)
        val gridLayoutManager = GridLayoutManager(requireContext(), 2) // 2 columns
        waitersRecyclerView.layoutManager = gridLayoutManager

        addWaiterButton = view.findViewById(R.id.addWaiterButton)

        adapter = ManageWaitersAdapter(
            waiterList,
            context = requireContext(),
            onDeleteClickListener = { deleteRequest -> confirmDeleteWaiter(deleteRequest) }
        )

        waitersRecyclerView.setLayoutManager(LinearLayoutManager(requireContext()))
        waitersRecyclerView.setAdapter(adapter)

        addWaiterButton.setOnClickListener { addWaiterDialog() }

        fetchWaiters()

        return view
    }

    private fun fetchWaiters() {
        adapter.fetchWaiters {
            Toast.makeText(requireContext(), "Waiters updated", Toast.LENGTH_SHORT).show() }
    }

    private fun addWaiterDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_waiter, null)
        val waiterNameInput = dialogView.findViewById<EditText>(R.id.waiterNameInput)
        val hireDateInput = dialogView.findViewById<EditText>(R.id.hireDateInput)

        val dialogBuilder = MaterialAlertDialogBuilder(requireContext())
        dialogBuilder.setTitle("Add Waiter")
            .setView(dialogView)
            .setPositiveButton("Add") { _, _ ->
                val waiterName = waiterNameInput.text.toString()
                val hireDate = hireDateInput.text.toString()

                if (hireDate.isNotEmpty()) {
                    val inputDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    try {
                        inputDateFormat.parse(hireDate)
                        addWaiter(waiterName, hireDate)
                    } catch (e: Exception) {
                        Toast.makeText(requireContext(), "Please enter a valid date (yyyy-MM-dd)", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(requireContext(), "Please enter a valid date", Toast.LENGTH_SHORT)
                        .show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun addWaiter(waiterName: String, hireDate: String) {
        val request = AddWaiterRequest(waiterName, hireDate)

        RetrofitWaiter.instance.addWaiter(request)
            .enqueue(object : Callback<AddWaiterResponse?> {
                override fun onResponse(
                    call: Call<AddWaiterResponse?>,
                    response: Response<AddWaiterResponse?>
                ) {
                    if (response.isSuccessful && response.body() != null) {
                        if ("success" == response.body()!!.status) {
                            fetchWaiters()
                            Toast.makeText(
                                requireContext(),
                                response.body()!!.message,
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            Toast.makeText(
                                requireContext(),
                                "Failed: " + response.body()!!.message,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }

                override fun onFailure(call: Call<AddWaiterResponse?>, t: Throwable) {
                    Toast.makeText(
                        requireContext(),
                        "Error: " + t.message,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    private fun confirmDeleteWaiter(deleteRequest: DeleteWaiterRequest) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Delete Waiter")
            .setMessage("Are you sure you want to remove this Waiter?")
            .setPositiveButton("Delete") { _, _ ->
                deleteWaiter(deleteRequest) }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteWaiter(deleteRequest: DeleteWaiterRequest) {
        adapter.removeWaiter(deleteRequest) {
            Toast.makeText(requireContext(),
                "Waiter deleted",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}