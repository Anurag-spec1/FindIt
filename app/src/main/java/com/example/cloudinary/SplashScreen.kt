package com.example.cloudinary

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.github.ybq.android.spinkit.SpinKitView


class SplashActivity : AppCompatActivity() {

    private lateinit var loadingIndicator: SpinKitView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        loadingIndicator = findViewById(R.id.spin_kit)
        loadingIndicator.visibility = View.VISIBLE // Optional (it's visible by default)

        // Simulate loading and move to MainActivity after delay
        Handler(Looper.getMainLooper()).postDelayed({
            loadingIndicator.visibility = View.GONE
            startActivity(Intent(this, Login::class.java))
            finish()
        }, 3000)
    }
}