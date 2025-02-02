package com.lovelycafe.casheirpos.client.ui.menu

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.lovelycafe.casheirpos.databinding.FragmentMenuBinding
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import com.lovelycafe.casheirpos.api.Image
import com.lovelycafe.casheirpos.api.RetrofitCategory
import com.lovelycafe.casheirpos.adapter.CategoryAdapter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MenuFragment : Fragment() {

    private var _binding: FragmentMenuBinding? = null
    private val binding get() = _binding!!

    private lateinit var imageAdapter: CategoryAdapter
    private val imageList = mutableListOf<Image>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMenuBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set up RecyclerView
        val numberOfColumns = if (resources.configuration.screenWidthDp >= 600) 2 else 1
        binding.recyclerView.layoutManager = GridLayoutManager(requireContext(), numberOfColumns)

        imageAdapter = CategoryAdapter(imageList) { image ->
            // Handle category click using NavController
            val action = MenuFragmentDirections.goFromNavigationMenuToNavigationFoodList(image.id)
            findNavController().navigate(action)
        }
        binding.recyclerView.adapter = imageAdapter

        // Setup observers for ViewModel
        setupObservers()

        // Fetch images from the server
        fetchImages()
    }

    private fun setupObservers() {
    }

    private fun fetchImages() {
        RetrofitCategory.instance.getImages().enqueue(object : Callback<List<Image>> {
            override fun onResponse(call: Call<List<Image>>, response: Response<List<Image>>) {
                if (response.isSuccessful) {
                    response.body()?.let { newImages ->
                        val oldSize = imageList.size
                        imageList.clear()
                        imageAdapter.notifyItemRangeRemoved(0, oldSize)

                        imageList.addAll(newImages)
                        imageAdapter.notifyItemRangeInserted(0, newImages.size)
                    }
                } else {
                    Toast.makeText(requireContext(), "Failed to fetch images", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<Image>>, t: Throwable) {
                Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}