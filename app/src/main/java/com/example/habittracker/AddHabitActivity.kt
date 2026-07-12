package com.example.habittracker

import android.app.TimePickerDialog
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.habittracker.databinding.ActivityAddHabitBinding
import com.google.android.material.card.MaterialCardView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar
import java.util.Locale

class AddHabitActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddHabitBinding

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private var selectedColor = "#B8E3C4"
    private var frequency = "Daily"
    private var startTime = ""
    private var endTime = ""

    private var startTimeMinutes: Int? = null
    private var endTimeMinutes: Int? = null

    private val selectedDays = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAddHabitBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.imgBack.setOnClickListener {
            finish()
        }

        // -------------------------
        // Habit Dropdown
        // -------------------------

        val habits = listOf(
            "Reading",
            "Running",
            "Exercise",
            "Cycling",
            "Cooking",
            "Studying",
            "Meditation",
            "Drink Water",
            "Walking",
            "Sleep Early"
        )

        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_dropdown_item_1line,
            habits
        )

        binding.dropHabit.setAdapter(adapter)

        // -------------------------
        // Frequency
        // -------------------------

        frequency = "Daily"

        binding.layoutDays.visibility = View.GONE
        binding.tvWeekDays.visibility = View.GONE

        binding.btnDaily.backgroundTintList =
            ColorStateList.valueOf(Color.parseColor("#F9F4E7"))
        binding.btnWeekly.backgroundTintList =
            ColorStateList.valueOf(Color.parseColor("#C9DFD1"))

        binding.btnDaily.setTextColor(Color.parseColor("#555555"))
        binding.btnWeekly.setTextColor(Color.parseColor("#8A8A8A"))

        binding.btnDaily.setOnClickListener {
            frequency = "Daily"

            binding.layoutDays.visibility = View.GONE
            binding.tvWeekDays.visibility = View.GONE

            binding.btnDaily.backgroundTintList =
                ColorStateList.valueOf(Color.parseColor("#F9F4E7"))
            binding.btnWeekly.backgroundTintList =
                ColorStateList.valueOf(Color.parseColor("#C9DFD1"))

            binding.btnDaily.setTextColor(Color.parseColor("#555555"))
            binding.btnWeekly.setTextColor(Color.parseColor("#8A8A8A"))
        }

        binding.btnWeekly.setOnClickListener {
            frequency = "Weekly"

            binding.layoutDays.visibility = View.VISIBLE
            binding.tvWeekDays.visibility = View.VISIBLE

            binding.btnWeekly.backgroundTintList =
                ColorStateList.valueOf(Color.parseColor("#F9F4E7"))
            binding.btnDaily.backgroundTintList =
                ColorStateList.valueOf(Color.parseColor("#C9DFD1"))

            binding.btnWeekly.setTextColor(Color.parseColor("#555555"))
            binding.btnDaily.setTextColor(Color.parseColor("#8A8A8A"))
        }

        // -------------------------
        // Week Days
        // -------------------------

        binding.tvMon.setOnClickListener { toggleDay(binding.tvMon, "Mon") }
        binding.tvTue.setOnClickListener { toggleDay(binding.tvTue, "Tue") }
        binding.tvWed.setOnClickListener { toggleDay(binding.tvWed, "Wed") }
        binding.tvThu.setOnClickListener { toggleDay(binding.tvThu, "Thu") }
        binding.tvFri.setOnClickListener { toggleDay(binding.tvFri, "Fri") }
        binding.tvSat.setOnClickListener { toggleDay(binding.tvSat, "Sat") }
        binding.tvSun.setOnClickListener { toggleDay(binding.tvSun, "Sun") }

        // -------------------------
        // Color Picker
        // -------------------------

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

        binding.etStartTime.setOnClickListener {
            showTimePicker(true)
        }


        binding.etEndTime.setOnClickListener {
            showTimePicker(false)
        }

        binding.btnCreateHabit.setOnClickListener {
            saveHabitToFirestore()
        }
    }

    private fun showTimePicker(selectingStartTime: Boolean) {
        val calendar = Calendar.getInstance()
        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
        val currentMinute = calendar.get(Calendar.MINUTE)

        val timePicker = TimePickerDialog(
            this,
            { _, selectedHour, selectedMinute ->

                val selectedTotalMinutes = selectedHour * 60 + selectedMinute
                val formattedTime = formatTime(selectedHour, selectedMinute)

                if (selectingStartTime) {

                    startTime = formattedTime
                    startTimeMinutes = selectedTotalMinutes
                    binding.etStartTime.setText(formattedTime)
                } else {
                    endTime = formattedTime
                    endTimeMinutes = selectedTotalMinutes
                    binding.etEndTime.setText(formattedTime)
                }
            },
            currentHour,
            currentMinute,
            false
        )
        timePicker.show()
    }


    private fun formatTime(hour: Int, minute: Int): String {
        val period = if (hour >= 12) "PM" else "AM"
        val displayHour = when {
            hour == 0 -> 12
            hour > 12 -> hour - 12
            else -> hour
        }

        return String.format(
            Locale.getDefault(),
            "%02d:%02d %s",
            displayHour,
            minute,
            period
        )
    }

    private fun saveHabitToFirestore() {
        val name = binding.dropHabit.text.toString().trim()

        if (name.isEmpty()) {
            Toast.makeText(this, "Please select a habit", Toast.LENGTH_SHORT).show()
            return
        }

        val userId = auth.currentUser?.uid
        if (userId == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }


        if (startTimeMinutes == null) {
            Toast.makeText(this, "Please select start time", Toast.LENGTH_SHORT).show()
            return
        }
        if (endTimeMinutes == null) {
            Toast.makeText(this, "Please select end time", Toast.LENGTH_SHORT).show()
            return
        }


        val startMinutes = startTimeMinutes ?: return
        val endMinutes = endTimeMinutes ?: return

        val durationMinutes = if (endMinutes >= startMinutes) {
            endMinutes - startMinutes
        } else {
            24 * 60 - startMinutes + endMinutes
        }


        val habit = hashMapOf(
            "id" to "",
            "name" to name,
            "habitName" to name,
            "done" to 0,
            "goal" to 1,
            "color" to selectedColor,
            "frequency" to frequency,
            "days" to selectedDays,
            "startTime" to startTime,
            "endTime" to endTime,
            "startTimeMinutes" to startMinutes,
            "endTimeMinutes" to endMinutes,
            "durationMinutes" to durationMinutes,
            "reminder" to binding.swReminder.isChecked,
            "createdAt" to FieldValue.serverTimestamp()
        )

        db.collection("users")
            .document(userId)
            .collection("habits")
            .add(habit)
            .addOnSuccessListener { documentReference ->
                documentReference.update("id", documentReference.id)

                Toast.makeText(
                    this,
                    "Habit created successfully!",
                    Toast.LENGTH_SHORT
                ).show()

                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    this,
                    "Error: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
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

    private fun toggleDay(view: TextView, day: String) {
        if (selectedDays.contains(day)) {
            selectedDays.remove(day)
            view.background = getDrawable(R.drawable.bg_day_unselected)
            view.setTextColor(Color.parseColor("#8DB493"))
        } else {
            selectedDays.add(day)
            view.background = getDrawable(R.drawable.bg_day_selected)
            view.setTextColor(Color.WHITE)
        }
    }
}