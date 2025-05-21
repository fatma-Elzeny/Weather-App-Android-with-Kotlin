package com.example.weatherapp

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.preference.PreferenceManager
import android.view.WindowManager
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Optional: make full screen
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        setContentView(R.layout.activity_splash)

        // Delay to simulate splash (or load config, check login, etc.)
        Handler(Looper.getMainLooper()).postDelayed({
            val prefs = PreferenceManager.getDefaultSharedPreferences(this)
            if (!prefs.contains("location_mode")) {
                // First-time setup
                InitialSetupDialog(this) {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }.show()
            } else {
                // Already set up
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
        }, 1500)
    }
}
