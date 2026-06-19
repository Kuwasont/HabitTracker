package com.example.habittracker

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()

        val txtName = findViewById<EditText>(R.id.txtName)
        val txtEmail = findViewById<EditText>(R.id.txtEmail)
        val txtPassword = findViewById<EditText>(R.id.txtPassword)
        val btnRegister = findViewById<Button>(R.id.btnRegister)

        btnRegister.setOnClickListener {

            val name = txtName.text.toString()
            val email = txtEmail.text.toString()
            val password = txtPassword.text.toString()

            auth.createUserWithEmailAndPassword(email,password)
                .addOnCompleteListener {

                    if(it.isSuccessful){

                        val uid = auth.currentUser!!.uid

                        val user = User(
                            name,
                            email,
                            "Student"
                        )

                        FirebaseDatabase.getInstance()
                            .getReference("users")
                            .child(uid)
                            .setValue(user)

                        Toast.makeText(
                            this,
                            "Registration Successful",
                            Toast.LENGTH_SHORT
                        ).show()

                        startActivity(
                            Intent(
                                this,
                                LoginActivity::class.java
                            )
                        )

                        finish()

                    }else{

                        Toast.makeText(
                            this,
                            it.exception?.message,
                            Toast.LENGTH_LONG
                        ).show()

                    }
                }
        }
    }
}