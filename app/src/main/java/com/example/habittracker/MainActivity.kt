package com.example.habittracker

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        val txtWelcome =
            findViewById<TextView>(R.id.txtWelcome)

        val btnLogout =
            findViewById<Button>(R.id.btnLogout)

        val uid =
            FirebaseAuth.getInstance()
                .currentUser!!.uid

        FirebaseDatabase.getInstance()
            .getReference("users")
            .child(uid)
            .get()
            .addOnSuccessListener {

                val fullname =
                    it.child("fullname")
                        .value.toString()

                txtWelcome.text =
                    "Welcome $fullname"

            }

        btnLogout.setOnClickListener {

            FirebaseAuth.getInstance().signOut()

            startActivity(
                Intent(
                    this,
                    LoginActivity::class.java
                )
            )

            finish()
        }
    }
}