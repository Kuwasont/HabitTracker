package com.example.habittracker

import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.habittracker.databinding.ActivityMainBinding
import com.google.android.material.card.MaterialCardView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private lateinit var currentAdapter: HabitAdapter
    private lateinit var completedAdapter: HabitAdapter
    private var habitsListener: ListenerRegistration? = null

    data class Habit(
        val id: String,
        val name: String,
        val done: Int,
        val goal: Int,
        val color: String
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        currentAdapter = HabitAdapter { habit ->
            markHabitDone(habit)
        }

        completedAdapter = HabitAdapter(
            onDoubleClick = { habit ->
                deleteCompletedHabit(habit)
            }
        )

        binding.rvCurrentHabits.layoutManager = LinearLayoutManager(this)
        binding.rvCurrentHabits.adapter = currentAdapter

        binding.rvCompletedHabits.layoutManager = LinearLayoutManager(this)
        binding.rvCompletedHabits.adapter = completedAdapter

        binding.imgStats.setOnClickListener {
            startActivity(Intent(this, StatisticsActivity::class.java))
        }

        binding.addHabit.setOnClickListener {
            startActivity(Intent(this, AddHabitActivity::class.java))
        }

        binding.btnLogout.setOnClickListener {
            auth.signOut()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    override fun onStart() {
        super.onStart()
        listenToHabits()
    }

    override fun onStop() {
        super.onStop()
        habitsListener?.remove()
    }

    private fun listenToHabits() {
        val userId = auth.currentUser?.uid ?: return

        habitsListener = db.collection("users")
            .document(userId)
            .collection("habits")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener

                val habits = snapshot.documents.map { doc ->
                    Habit(
                        id = doc.id,
                        name = doc.getString("name")
                            ?: doc.getString("habitName")
                            ?: "Untitled Habit",
                        done = (doc.getLong("done") ?: 0).toInt(),
                        goal = (doc.getLong("goal")
                            ?: doc.getLong("targetHours")
                            ?: 0).toInt(),
                        color = doc.getString("color") ?: "#B8E3C4"
                    )
                }

                val currentHabits = habits.filter { it.goal == 0 || it.done < it.goal }
                val completedHabits = habits.filter { it.goal > 0 && it.done >= it.goal }

                currentAdapter.submitList(currentHabits)
                completedAdapter.submitList(completedHabits)

                binding.rvCurrentHabits.visibility =
                    if (currentHabits.isEmpty()) View.GONE else View.VISIBLE

                binding.tvEmptyCurrent.visibility =
                    if (currentHabits.isEmpty()) View.VISIBLE else View.GONE

                binding.tvCompleted.visibility =
                    if (completedHabits.isEmpty()) View.GONE else View.VISIBLE

                binding.rvCompletedHabits.visibility =
                    if (completedHabits.isEmpty()) View.GONE else View.VISIBLE
            }
    }

    private fun markHabitDone(habit: Habit) {
        val userId = auth.currentUser?.uid ?: return

        if (habit.goal > 0 && habit.done >= habit.goal) return

        db.collection("users")
            .document(userId)
            .collection("habits")
            .document(habit.id)
            .update("done", FieldValue.increment(1))
    }

    private fun deleteCompletedHabit(habit: Habit) {
        val userId = auth.currentUser?.uid ?: return

        db.collection("users")
            .document(userId)
            .collection("habits")
            .document(habit.id)
            .delete()
    }

    class HabitAdapter(
        private val onClick: ((Habit) -> Unit)? = null,
        private val onDoubleClick: ((Habit) -> Unit)? = null
    ) : RecyclerView.Adapter<HabitAdapter.HabitViewHolder>() {

        private val habits = mutableListOf<Habit>()

        fun submitList(newHabits: List<Habit>) {
            habits.clear()
            habits.addAll(newHabits)
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HabitViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_habit, parent, false)

            return HabitViewHolder(view)
        }

        override fun onBindViewHolder(holder: HabitViewHolder, position: Int) {
            val habit = habits[position]
            val isCompleted = habit.goal > 0 && habit.done >= habit.goal

            holder.cardHabit.setCardBackgroundColor(Color.parseColor(habit.color))
            holder.name.text = habit.name
            holder.progress.text = "${habit.done}/${habit.goal}"

            if (isCompleted) {
                holder.circle.setBackgroundResource(R.drawable.bg_circle_filled)
                holder.name.paintFlags =
                    holder.name.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            } else {
                holder.circle.setBackgroundResource(R.drawable.bg_circle_outline)
                holder.name.paintFlags =
                    holder.name.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            }

            var lastClickTime = 0L

            holder.itemView.setOnClickListener {
                val clickTime = System.currentTimeMillis()

                if (clickTime - lastClickTime < 300) {
                    onDoubleClick?.invoke(habit)
                } else {
                    onClick?.invoke(habit)
                }

                lastClickTime = clickTime
            }
        }

        override fun getItemCount(): Int = habits.size

        class HabitViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val cardHabit: MaterialCardView = itemView.findViewById(R.id.cardHabit)
            val circle: View = itemView.findViewById(R.id.viewCircle)
            val name: TextView = itemView.findViewById(R.id.tvHabitName)
            val progress: TextView = itemView.findViewById(R.id.tvHabitProgress)
        }
    }
}