package com.example.habittracker

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

        val txtEmail = findViewById<EditText>(R.id.txtEmail)
        val txtPassword = findViewById<EditText>(R.id.txtPassword)

        val btnLogin = findViewById<TextView>(R.id.btnLogin)
        val btnRegisterPage =
            findViewById<TextView>(R.id.btnRegisterPage)

        btnRegisterPage.setOnClickListener {

            startActivity(
                Intent(
                    this,
                    RegisterActivity::class.java
                )
            )
        }

        btnLogin.setOnClickListener {

            val email = txtEmail.text.toString()
            val password = txtPassword.text.toString()

            auth.signInWithEmailAndPassword(
                email,
                password
            )
                .addOnCompleteListener {

                    if(it.isSuccessful){

                        startActivity(
                            Intent(
                                this,
                                MainActivity::class.java
                            )
                        )

                        finish()

                    }else{

                        Toast.makeText(
                            this,
                            it.exception?.message ?: "Login Failed",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
        }
    }
}