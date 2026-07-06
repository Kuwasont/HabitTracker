package com.example.habittracker

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class StatisticsActivity : AppCompatActivity() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    data class Habit(
        val name: String,
        val done: Int,
        val goal: Int,
        val color: String
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_statistics)

        findViewById<ImageView>(R.id.imgBack).setOnClickListener {
            finish()
        }

        findViewById<ImageView>(R.id.home_nav).setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        findViewById<FloatingActionButton>(R.id.add_habit).setOnClickListener {
            startActivity(Intent(this, AddHabitActivity::class.java))
        }

        loadStatisticsFromFirebase()
    }

    private fun loadStatisticsFromFirebase() {
        val userId = auth.currentUser?.uid ?: return

        db.collection("users")
            .document(userId)
            .collection("habits")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener

                val habits = snapshot.documents.map { doc ->
                    Habit(
                        name = doc.getString("name")
                            ?: doc.getString("habitName")
                            ?: "Untitled Habit",
                        done = (doc.getLong("done") ?: 0).toInt(),
                        goal = (doc.getLong("goal")
                            ?: doc.getLong("targetHours")
                            ?: 0).toInt(),
                        color = doc.getString("color") ?: "#8DB79F"
                    )
                }

                updateStatistics(habits)
            }
    }

    private fun updateStatistics(habits: List<Habit>) {
        val txtHours = findViewById<TextView>(R.id.txtHours)
        val txtCompleted = findViewById<TextView>(R.id.txtCompleted)
        val txtOverall = findViewById<TextView>(R.id.txtOverall)
        val progressList = findViewById<LinearLayout>(R.id.progressList)

        val totalGoal = habits.sumOf { it.goal }
        val totalDone = habits.sumOf { it.done.coerceAtMost(it.goal) }
        val remaining = (totalGoal - totalDone).coerceAtLeast(0)

        val completedHabits = habits.count { it.goal > 0 && it.done >= it.goal }

        val remainingPercent = if (totalGoal > 0) {
            (remaining * 100) / totalGoal
        } else {
            0
        }

        txtHours.text = "$remaining/$totalGoal"
        txtCompleted.text = "$completedHabits/${habits.size}"
        txtOverall.text = "$remainingPercent%"

        progressList.removeAllViews()

        habits.forEach { habit ->
            val percent = if (habit.goal > 0) {
                ((habit.done.coerceAtMost(habit.goal) * 100) / habit.goal)
            } else {
                0
            }

            addProgressRow(progressList, habit.name, percent, habit.color)
        }
    }

    private fun addProgressRow(
        parent: LinearLayout,
        name: String,
        percent: Int,
        color: String
    ) {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dp(26)
            }
        }

        val habitName = TextView(this).apply {
            text = name
            textSize = 20f
            setTextColor(Color.parseColor("#435B50"))
            setTypeface(null, Typeface.BOLD)
            layoutParams = LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1f
            )
        }

        val habitPercent = TextView(this).apply {
            text = "$percent%"
            textSize = 20f
            setTextColor(Color.parseColor("#435B50"))
            setTypeface(null, Typeface.BOLD)
        }

        row.addView(habitName)
        row.addView(habitPercent)

        val progressBar = ProgressBar(
            this,
            null,
            android.R.attr.progressBarStyleHorizontal
        ).apply {
            max = 100
            progress = percent
            progressTintList = ColorStateList.valueOf(Color.parseColor(color))
            progressBackgroundTintList = ColorStateList.valueOf(Color.parseColor("#E8DDD8"))
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(12)
            ).apply {
                topMargin = dp(6)
            }
        }

        parent.addView(row)
        parent.addView(progressBar)
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }
}