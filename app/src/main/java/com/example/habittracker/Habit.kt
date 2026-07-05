package com.example.habittracker

data class Habit(
    var habitName: String = "",
    var targetHours: Int = 0,
    var completedHours: Int = 0,
    var completed: Boolean = false,
    var createdAt: Long = System.currentTimeMillis()
)