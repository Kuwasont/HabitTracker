package com.example.habittracker

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.habittracker.databinding.ActivityAddProductBinding
import com.google.firebase.Firebase
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.firestore

class AddProductActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddProductBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        

        binding = ActivityAddProductBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnSave.setOnClickListener {
            val name = binding.etProductName.text.toString().trim()
            val description = binding.etDescription.text.toString().trim()

            if (name.isEmpty()) {
                Toast.makeText(this, "Please enter a habit name", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            saveHabit(name, description)
        }
    }

    private fun saveHabit(name: String, description: String) {

        val habit = Product(
            productName = name,
            description = description,
            createdAt = FieldValue.serverTimestamp()
        )

        // Save to Firestore
        Firebase.firestore.collection("products")
            .add(habit)
            .addOnSuccessListener {
                Toast.makeText(this, "Habit added successfully", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error adding habit: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
