package com.example.habittracker

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.habittracker.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        binding.rvCurrentHabits.visibility = View.GONE
        binding.rvCompletedHabits.visibility = View.GONE
        binding.tvCompleted.visibility = View.GONE


        binding.tvEmptyCurrent.visibility = View.VISIBLE


        binding.imgStats.setOnClickListener {

            startActivity(
                Intent(
                    this,
                    StatisticsActivity::class.java
                )
            )

        }

    }
}