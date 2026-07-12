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
        val color: String,
        val durationMinutes: Int
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
                        goal = (doc.getLong("goal") ?: 1).toInt(),
                        color = doc.getString("color") ?: "#8DB79F",

                        durationMinutes = (
                                doc.getLong("durationMinutes") ?:0
                                ).toInt()
                    )
                }

                updateStatistics(habits)
            }
    }

    private fun updateStatistics(habits: List<Habit>) {
        val txtHours = findViewById<TextView>(R.id.txtHours)
        val txtCompleted = findViewById<TextView>(R.id.txtCompleted)
        val progressList = findViewById<LinearLayout>(R.id.progressList)

        val totalMinutes = habits.sumOf{
            it.durationMinutes
        }

        val completedMinutes =
            habits.sumOf{ habit ->
                if(habit.goal > 0){
                    val completedAmount =
                        habit.done.coerceIn(0, habit.goal)
                    (habit.durationMinutes * completedAmount) / habit.goal
                }else {
                    0
                }
            }

        val remainingMinutes =
            (totalMinutes - completedMinutes).coerceAtLeast(0)

        val completedHabits =
            habits.count{
                it.goal > 0 && it.done >= it.goal
            }

        txtHours.text = "${formatHours(remainingMinutes)}/" + formatHours(totalMinutes)

        txtCompleted.text = "$completedHabits/${habits.size}"

        progressList.removeAllViews()

        habits.forEach { habit ->
            addDurationRow(
                progressList,
                habit.name,
                habit.durationMinutes
            )
        }
    }

    private fun addDurationRow(
        parent: LinearLayout,
        name: String,
        durationMinutes: Int
    ){
        val row = LinearLayout(this).apply{
            orientation = LinearLayout.HORIZONTAL

            layoutParams =
                LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    dp(42)
                )
            gravity = android.view.Gravity.CENTER_VERTICAL
        }

        val habitName = TextView(this).apply{
            text = name
            textSize = 16f
            setTextColor(Color.parseColor("#435B50"))

            setTypeface(null, Typeface.BOLD)

            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        }

        val habitDuration = TextView(this).apply{
            text = formatDuration(durationMinutes)
            textSize = 16f
            setTextColor(Color.parseColor("#435B50"))

            setTypeface(null, Typeface.BOLD)
        }
        row.addView(habitName)
        row.addView(habitDuration)
        parent.addView(row)
    }

    private fun formatHours(
        minutes: Int
    ): String{
        val hours =
            minutes / 60.0

        return if(
            minutes % 60 == 0
        ) {
            hours.toInt().toString()
        } else {
            String.format(java.util.Locale.getDefault(), "%.1f", hours)
        }
    }

    private fun formatDuration(
        minutes: Int
    ): String{
        val hours = minutes / 60
        val remainingMinutes = minutes % 60

        return when{
            hours > 0 && remainingMinutes > 0 -> {
                "$hours hr $remainingMinutes min"
            }
            hours == 1 -> {
                "1 hour"
            }
            hours > 1 ->{
                "$hours hours"
            }else ->{
                "$remainingMinutes min"
            }
        }
    }

    private fun dp(value: Int):Int{
        return(value * resources.displayMetrics.density).toInt()
    }
}