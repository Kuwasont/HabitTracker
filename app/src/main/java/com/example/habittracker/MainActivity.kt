package com.example.habittracker

import android.content.Intent
import android.os.Bundle
import android.widget.SearchView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.habittracker.databinding.ActivityMainBinding
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: ProductAdapter

    private val allProducts = mutableListOf<Product>()
    private val filteredList = mutableListOf<Product>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = ProductAdapter(filteredList)
        binding.rvProducts.adapter = adapter

        binding.fabAddProduct.setOnClickListener {
            val intent = Intent(this, AddProductActivity::class.java)
            startActivity(intent)
        }


        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterList(newText)
                return true
            }
        })

        listenToProductChanges()
    }

    private fun listenToProductChanges() {
        Firebase.firestore.collection("products")
            .orderBy("createdAt")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Toast.makeText(this, "Failed to load items", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    allProducts.clear()
                    for (doc in snapshot.documents) {
                        val product = doc.toObject(Product::class.java)
                        if (product != null) {
                            allProducts.add(product)
                        }
                    }

                    filterList(binding.searchView.query?.toString())
                }
            }
    }


    private fun filterList(text: String?) {
        filteredList.clear()
        if (text.isNullOrEmpty()) {
            filteredList.addAll(allProducts)
        } else {
            for (product in allProducts) {

                if (product.productName.lowercase().contains(text.lowercase())) {
                    filteredList.add(product)
                }
            }
        }
        adapter.updateList(filteredList)
    }
}
