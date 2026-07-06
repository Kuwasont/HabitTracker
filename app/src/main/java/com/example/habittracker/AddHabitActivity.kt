package com.example.habittracker

import android.Manifest // EDITED: added for notification permission
import android.app.AlarmManager // EDITED: added for weekly reminder
import android.app.NotificationChannel // EDITED: added for notification channel
import android.app.NotificationManager // EDITED: added for showing notifications
import android.app.PendingIntent // EDITED: added for reminder intent
import android.content.Context // EDITED: added for system services
import android.content.Intent
import android.content.pm.PackageManager // EDITED: added for permission check
import android.os.Build // EDITED: added for Android version checks
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat // EDITED: added for permission request
import androidx.core.app.NotificationCompat // EDITED: added for notification
import androidx.core.content.ContextCompat // EDITED: added for permission check
import com.example.habittracker.databinding.ActivityAddHabitBinding
import com.google.android.material.card.MaterialCardView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar // EDITED: added for next weekly reminder

class AddHabitActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddHabitBinding
    private var selectedColor = "#B8E3C4"

    // EDITED: stores whether the user turned on reminder
    private var reminderEnabled = false

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    // EDITED: notification constants
    companion object {
        private const val REMINDER_CHANNEL_ID = "habit_reminders"
        private const val NOTIFICATION_PERMISSION_REQUEST_CODE = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAddHabitBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // EDITED: create notification channel when page opens
        createReminderNotificationChannel()

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

        // EDITED: reminder switch logic
        binding.swReminder.setOnCheckedChangeListener { _, isChecked ->
            reminderEnabled = isChecked

            if (isChecked) {
                requestNotificationPermissionIfNeeded()
                showInstantReminderNotification()
            }
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
            "reminderEnabled" to reminderEnabled, // EDITED: save reminder choice
            "createdAt" to FieldValue.serverTimestamp()
        )

        db.collection("users")
            .document(userId)
            .collection("habits")
            .add(habit)
            .addOnSuccessListener { documentReference ->
                documentReference.update("id", documentReference.id)
                    .addOnCompleteListener {

                        // EDITED: schedule weekly reminder after habit is created
                        if (reminderEnabled) {
                            scheduleWeeklyReminder(documentReference.id, name)
                        }

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

    // EDITED: creates notification channel for Android 8+
    private fun createReminderNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                REMINDER_CHANNEL_ID,
                "Habit Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            )

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    // EDITED: asks notification permission for Android 13+
    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val hasPermission = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

            if (!hasPermission) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    NOTIFICATION_PERMISSION_REQUEST_CODE
                )
            }
        }
    }

    // EDITED: show notification immediately when reminder switch is turned on
    private fun showInstantReminderNotification() {
        if (!canShowNotifications()) return

        val habitName = binding.etHabitName.text.toString().trim().ifEmpty {
            "your habit"
        }

        val notification = NotificationCompat.Builder(this, REMINDER_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_add)
            .setContentTitle("Habit reminder")
            .setContentText("Reminder is on for $habitName")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }

    // EDITED: schedules reminder for next week, same day and time
    private fun scheduleWeeklyReminder(habitId: String, habitName: String) {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(this, HabitReminderReceiver::class.java).apply {
            putExtra("habitName", habitName)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            this,
            habitId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val nextWeek = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, 7)
        }

        // Use setAndAllowWhileIdle for reliable notifications even in Doze mode
        alarmManager.setAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            nextWeek.timeInMillis,
            pendingIntent
        )
    }

    // EDITED: checks if notification can be shown
    private fun canShowNotifications(): Boolean {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
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