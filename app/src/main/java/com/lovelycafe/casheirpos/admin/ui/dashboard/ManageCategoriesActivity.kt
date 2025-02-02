package com.lovelycafe.casheirpos.admin.ui.dashboard

import android.net.Uri
import android.util.Log
import android.os.Bundle
import android.provider.MediaStore
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.lovelycafe.casheirpos.R
import com.lovelycafe.casheirpos.api.*
import com.lovelycafe.casheirpos.adapter.ManageCategoriesAdapter
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream

class ManageCategoriesActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var addCategoryButton: Button
    private lateinit var categoriesAdapter: ManageCategoriesAdapter
    private val categoriesList = mutableListOf<Image>()
    private var imageUri: Uri? = null
    private lateinit var selectedImageView: ImageView

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            imageUri = it
            Glide.with(this).load(it).into(selectedImageView)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_categories)

        // Initialize RecyclerView and Button
        recyclerView = findViewById(R.id.categoriesRecyclerView)
        addCategoryButton = findViewById(R.id.addCategoryButton)

        // Set up RecyclerView layout manager
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Initialize the adapter with the current categoriesList (empty initially
        categoriesAdapter = ManageCategoriesAdapter(
            this@ManageCategoriesActivity,
            categoriesList,
            onEditClick = { category -> showEditCategoryDialog(category) },
            onDeleteClick = { category -> confirmDeleteCategory(category) }
        )
        recyclerView.adapter = categoriesAdapter

        // Set up Add Category button listener
        addCategoryButton.setOnClickListener {
            showAddCategoryDialog()
        }

        // Load categories from the API
        loadCategories()
    }

    private fun loadCategories() {
        RetrofitCategory.instance.getImages().enqueue(object : Callback<List<Image>> {
            override fun onResponse(call: Call<List<Image>>, response: Response<List<Image>>) {

                if (response.isSuccessful) {
                    response.body()?.let { newCategories ->
                        val oldSize = categoriesList.size
                        categoriesList.clear()
                        categoriesAdapter.notifyItemRangeRemoved(0, oldSize)

                        if (newCategories.isNotEmpty()) {
                            categoriesList.addAll(newCategories)
                            categoriesAdapter.notifyItemRangeInserted(0, newCategories.size)
                        } else {
                            Toast.makeText(
                                this@ManageCategoriesActivity,
                                "No categories found.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                } else {
                    Toast.makeText(
                        this@ManageCategoriesActivity,
                        "Failed to load categories",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<List<Image>>, t: Throwable) {
                Log.e("ManageCategories", "Error loading categories", t)
                Toast.makeText(this@ManageCategoriesActivity, "Error: ${t.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showAddCategoryDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_category, null)
        selectedImageView = dialogView.findViewById(R.id.categoryImagePreview)

        val uploadImageButton = dialogView.findViewById<Button>(R.id.uploadImageButton)
        uploadImageButton.setOnClickListener { pickImageLauncher.launch("image/*") }

        val dialog = AlertDialog.Builder(this)
            .setTitle("Add Category")
            .setView(dialogView)
            .setPositiveButton("Add") { _, _ ->
                val nameInput = dialogView.findViewById<EditText>(R.id.categoryNameInput).text.toString()
                val translatedNameInput = dialogView.findViewById<EditText>(R.id.categoryTranslatedNameInput).text.toString()
                addCategory(nameInput, translatedNameInput)
            }
            .setNegativeButton("Cancel", null)
            .create()
        dialog.show()
    }

    private fun addCategory(name: String, translated: String) {
        try {
            val categoryName = name.toRequestBody("text/plain".toMediaType())
            val categoryTranslated = translated.toRequestBody("text/plain".toMediaType())

            imageUri?.let { uri ->
                val imageFile = createFileFromUri(uri)
                val requestFile = imageFile.readBytes().toRequestBody("image/*".toMediaType())
                val imagePart = MultipartBody.Part.createFormData("image", imageFile.name, requestFile)

                RetrofitCategory.instance.addCategory(imagePart, categoryName, categoryTranslated)
                    .enqueue(object : Callback<AddCategoryResponse> {
                        override fun onResponse(call: Call<AddCategoryResponse>, response: Response<AddCategoryResponse>) {
                            if (response.isSuccessful && response.body()?.success == true) {
                                Toast.makeText(this@ManageCategoriesActivity, "Category added", Toast.LENGTH_SHORT).show()
                                imageUri = null
                                selectedImageView.setImageResource(R.drawable.placeholder_image)
                                loadCategories()
                            } else {
                                Toast.makeText(this@ManageCategoriesActivity, "Failed to add category", Toast.LENGTH_SHORT).show()
                            }
                        }

                        override fun onFailure(call: Call<AddCategoryResponse>, t: Throwable) {
                            Toast.makeText(this@ManageCategoriesActivity, "Error: ${t.localizedMessage}", Toast.LENGTH_SHORT).show()
                        }
                    })
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error processing image: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showEditCategoryDialog(category: Image) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_category, null)
        selectedImageView = dialogView.findViewById(R.id.categoryImagePreview)

        val nameInput = dialogView.findViewById<EditText>(R.id.categoryNameInput)
        val translatedNameInput = dialogView.findViewById<EditText>(R.id.categoryTranslatedNameInput)
        nameInput.setText(category.name)
        translatedNameInput.setText(category.translated)

        Glide.with(this).load(category.path).into(selectedImageView)

        val uploadImageButton = dialogView.findViewById<Button>(R.id.uploadImageButton)
        uploadImageButton.setOnClickListener { pickImageLauncher.launch("image/*") }

        val dialog = AlertDialog.Builder(this)
            .setTitle("Edit Category")
            .setView(dialogView)
            .setPositiveButton("Update") { _, _ ->
                val updatedName = nameInput.text.toString()
                val updatedTranslatedName = translatedNameInput.text.toString()
                updateCategory(category.id, updatedName, updatedTranslatedName)
            }
            .setNegativeButton("Cancel", null)
            .create()
        dialog.show()
    }

    private fun updateCategory(id: Int, name: String, translated: String) {
        try {
            val categoryName = name.toRequestBody("text/plain".toMediaType())
            val categoryTranslated = translated.toRequestBody("text/plain".toMediaType())
            val idBody = id.toString().toRequestBody("text/plain".toMediaType())

            val imagePart = imageUri?.let { uri ->
                val imageFile = createFileFromUri(uri)
                val requestFile = imageFile.readBytes().toRequestBody("image/*".toMediaType())
                MultipartBody.Part.createFormData("image", imageFile.name, requestFile)
            }

            RetrofitCategory.instance.updateCategory(imagePart, categoryName, categoryTranslated, idBody)
                .enqueue(object : Callback<UpdateCategoryResponse> {
                    override fun onResponse(call: Call<UpdateCategoryResponse>, response: Response<UpdateCategoryResponse>) {
                        if (response.isSuccessful && response.body()?.success == true) {
                            Toast.makeText(this@ManageCategoriesActivity, "Category updated", Toast.LENGTH_SHORT).show()
                            imageUri = null
                            selectedImageView.setImageResource(R.drawable.placeholder_image)
                            loadCategories()
                        } else {
                            Toast.makeText(this@ManageCategoriesActivity, "Failed to update category", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<UpdateCategoryResponse>, t: Throwable) {
                        Toast.makeText(this@ManageCategoriesActivity, "Error: ${t.localizedMessage}", Toast.LENGTH_SHORT).show()
                    }
                })
        } catch (e: Exception) {
            Toast.makeText(this, "Error processing image: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun confirmDeleteCategory(category: Image) {
        AlertDialog.Builder(this)
            .setTitle("Delete Category")
            .setMessage("Are you sure you want to delete ${category.name}?")
            .setPositiveButton("Yes") { _, _ -> deleteCategory(category.id) }
            .setNegativeButton("No", null)
            .show()
    }

    private fun deleteCategory(id: Int) {
        RetrofitCategory.instance.deleteCategory(DeleteCategoryRequest(id)).enqueue(object : Callback<DeleteCategoryResponse> {
            override fun onResponse(call: Call<DeleteCategoryResponse>, response: Response<DeleteCategoryResponse>) {
                if (response.isSuccessful && response.body()?.success == true) {
                    Toast.makeText(this@ManageCategoriesActivity, "Category deleted", Toast.LENGTH_SHORT).show()
                    loadCategories()
                } else {
                    Toast.makeText(this@ManageCategoriesActivity, "Failed to delete category", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<DeleteCategoryResponse>, t: Throwable) {
                Toast.makeText(this@ManageCategoriesActivity, "Error: ${t.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun createFileFromUri(uri: Uri): File {
        val inputStream = contentResolver.openInputStream(uri)
        val fileName = getFileNameFromUri(uri) ?: "temp_image_${System.currentTimeMillis()}.jpg"
        val tempFile = File(cacheDir, fileName)

        tempFile.createNewFile()

        FileOutputStream(tempFile).use { outputStream ->
            inputStream?.use { input ->
                input.copyTo(outputStream)
            }
        }

        return tempFile
    }

    private fun getFileNameFromUri(uri: Uri): String? {
        var fileName: String? = null
        contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME)
            cursor.moveToFirst()
            fileName = cursor.getString(nameIndex)
        }
        return fileName
    }
}