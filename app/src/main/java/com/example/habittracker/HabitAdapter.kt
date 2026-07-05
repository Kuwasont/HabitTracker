package com.example.habittracker

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.habittracker.databinding.ItemHabitBinding

class HabitAdapter(
    private var habitList: List<Habit>
) : RecyclerView.Adapter<HabitAdapter.HabitViewHolder>() {

    inner class HabitViewHolder(
        val binding: ItemHabitBinding
    ) : RecyclerView.ViewHolder(binding.root)

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

        holder.binding.tvProgress.text =
            "${habit.completedHours}/${habit.targetHours}"

    }

    override fun getItemCount(): Int {
        return habitList.size
    }

    fun updateList(newList: List<Habit>) {
        habitList = newList
        notifyDataSetChanged()
    }
}