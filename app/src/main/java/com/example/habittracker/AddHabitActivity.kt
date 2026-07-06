package com.example.habittracker

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.habittracker.databinding.ActivityAddHabitBinding
import com.google.android.material.card.MaterialCardView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import android.content.Intent

class AddHabitActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddHabitBinding
    private var selectedColor = "#B8E3C4"

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAddHabitBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.imgBack.setOnClickListener {
            finish()
        }

        highlightSelected(binding.cardGreen)

        binding.cardGreen.setOnClickListener {
            selectedColor = "#B8E3C4"
            highlightSelected(binding.cardGreen)
        }

        binding.cardOrange.setOnClickListener {
            selectedColor = "#FFD3A8"
            highlightSelected(binding.cardOrange)
        }

        binding.cardBlue.setOnClickListener {
            selectedColor = "#B7D7FF"
            highlightSelected(binding.cardBlue)
        }

        binding.cardPink.setOnClickListener {
            selectedColor = "#FFD6E8"
            highlightSelected(binding.cardPink)
        }

        binding.cardPurple.setOnClickListener {
            selectedColor = "#DDB8F4"
            highlightSelected(binding.cardPurple)
        }

        binding.btnCreateHabit.setOnClickListener {
            saveHabitToFirestore()
        }
    }

    private fun saveHabitToFirestore() {
        val name = binding.etHabitName.text.toString().trim()
        val targetText = binding.etTargetHours.text.toString().trim()

        if (name.isEmpty() || targetText.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }

        val targetHours = targetText.toIntOrNull()

        if (targetHours == null || targetHours <= 0) {
            Toast.makeText(this, "Please enter a valid target", Toast.LENGTH_SHORT).show()
            return
        }

        val userId = auth.currentUser?.uid

        if (userId == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        val habit = hashMapOf(
            "id" to "",
            "name" to name,
            "habitName" to name,
            "done" to 0,
            "goal" to targetHours,
            "targetHours" to targetHours,
            "color" to selectedColor,
            "createdAt" to FieldValue.serverTimestamp()
        )

        db.collection("users")
            .document(userId)
            .collection("habits")
            .add(habit)
            .addOnSuccessListener { documentReference ->
                documentReference.update("id", documentReference.id)
                    .addOnCompleteListener {
                        Toast.makeText(this, "Habit created successfully!", Toast.LENGTH_SHORT).show()

                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
            }
            .addOnFailureListener { e ->
                android.util.Log.e("AddHabitActivity", "Error adding habit", e)
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun highlightSelected(selected: MaterialCardView) {
        val cards = listOf(
            binding.cardGreen,
            binding.cardOrange,
            binding.cardBlue,
            binding.cardPink,
            binding.cardPurple
        )

        cards.forEach {
            it.strokeWidth = 0
            it.strokeColor = getColor(android.R.color.transparent)
        }

        selected.strokeWidth = 4
        selected.strokeColor = getColor(R.color.darkgreen)
    }
}