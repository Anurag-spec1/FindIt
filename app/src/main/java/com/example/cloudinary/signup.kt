package com.example.cloudinary

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.cloudinary.databinding.ActivityMainBinding
import com.example.cloudinary.databinding.ActivitySignupBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class signup : AppCompatActivity() {
    private lateinit var binding: ActivitySignupBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private var key: String="User"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        val pattern = "^[a-zA-Z0-9._-]+@kiet\\.edu$"

        binding.ButtonSignup.setOnClickListener {
            val email1 = binding.UserEmail2.text.toString().trim()
            val password1 = binding.UserPass2.text.toString().trim()
            val name1 = binding.UserName2.text.toString().trim()

            if (email1.isNotEmpty() && password1.isNotEmpty() && name1.isNotEmpty() && email1.matches(pattern.toRegex())) {
                firebaseAuth.createUserWithEmailAndPassword(email1, password1)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            saveUserData(name1, email1,password1,key)
                        } else {
                            Toast.makeText(this, "Account creation failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
            } else {
                Toast.makeText(this, "Enter a valid KIET email ID", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveUserData(name: String, email: String,pass: String,access: String) {
        val userId = firebaseAuth.currentUser?.uid
        val user = User(name, email,pass,access) // Password is not saved

        userId?.let {
            FirebaseDatabase.getInstance().getReference("Users")
                .child(userId)
                .setValue(user)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Account created successfully", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, opening::class.java))
                        finish()
                    } else {
                        Toast.makeText(this, "Failed to save user data: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }
}

data class User(
    val name: String = "",
    val email: String = "",
    val pass: String="",
    val access: String=""
)
