package com.example.habittracker

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.habittracker.databinding.ItemHabitBinding

class HabitAdapter(
    private var habitList: List<Habit>,
    private val onHabitDoubleTap: ((Habit) -> Unit)? = null
) : RecyclerView.Adapter<HabitAdapter.HabitViewHolder>() {

    inner class HabitViewHolder(
        val binding: ItemHabitBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        var lastClickTime: Long = 0
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): HabitViewHolder {

        val binding = ItemHabitBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        return HabitViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: HabitViewHolder,
        position: Int
    ) {

        val habit = habitList[position]

        holder.binding.tvHabitName.text = habit.habitName

        holder.binding.tvHabitProgress.text =
            "${habit.completedHours}/${habit.targetHours}"

        holder.itemView.setOnClickListener {
            val currentTime = System.currentTimeMillis()
            if (currentTime - holder.lastClickTime < 300) {
                if (habit.completedHours >= habit.targetHours) {
                    onHabitDoubleTap?.invoke(habit)
                }
            }
            holder.lastClickTime = currentTime
        }

    }

    override fun getItemCount(): Int {
        return habitList.size
    }

    fun updateList(newList: List<Habit>) {
        habitList = newList
        notifyDataSetChanged()
    }
}