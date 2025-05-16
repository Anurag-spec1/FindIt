package com.example.cloudinary

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.cloudinary.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth

class Login : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        binding= ActivityLoginBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        firebaseAuth= FirebaseAuth.getInstance()
        sharedPreferences = getSharedPreferences("loginPrefs", MODE_PRIVATE)

        val rememberMe = sharedPreferences.getBoolean("learn", false)

        if (rememberMe && firebaseAuth.currentUser != null) {
            val intent = Intent(this, opening::class.java)
            startActivity(intent)
            finish()
        }

      binding.ButtonLogin.setOnClickListener{
          val em1=binding.UserEmail.text.toString().trim()
          val pas1=binding.UserPass.text.toString().trim()
          val isRememberMeChecked = binding.yadrakh.isChecked

          if (em1.isNotEmpty()&& pas1.isNotEmpty()){
              firebaseAuth.signInWithEmailAndPassword(em1,pas1).addOnCompleteListener{
                  if (it.isSuccessful){


                      val editor = sharedPreferences.edit()
                      editor.putBoolean("learn", isRememberMeChecked) // isremeberchecked store either true or false
                      editor.apply()

                      val userId = firebaseAuth.currentUser?.uid
                      val intent = Intent(this, opening::class.java)
                      intent.putExtra("USER_ID", userId)
                      startActivity(intent)
                      finish()
                  }else{

                      Toast.makeText(this,"INVALID EMAIL OR PASS", Toast.LENGTH_SHORT).show()
                  }
              }
          }
      }

binding.SignUpText.setOnClickListener{
        val intent = Intent(this, signup::class.java)
        startActivity(intent)
        finish()
    }

    }
}