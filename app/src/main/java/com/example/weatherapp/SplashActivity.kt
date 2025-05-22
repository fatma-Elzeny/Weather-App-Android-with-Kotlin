package com.example.weatherapp

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.preference.PreferenceManager
import android.view.WindowManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.weatherapp.Home.view.MainActivity
import com.example.weatherapp.LocationPermission.view.LocationPermissionActivity
class SplashActivity : AppCompatActivity() {

    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                goToMain()
            } else {
                startActivity(Intent(this, LocationPermissionActivity::class.java))
                finish()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        setContentView(R.layout.activity_splash)

        Handler(Looper.getMainLooper()).postDelayed({
            val prefs = PreferenceManager.getDefaultSharedPreferences(this)
            val locationMode = prefs.getString("location_mode", null)

            if (locationMode == null) {
                showInitialSetup()
            } else {
                goToMain()
            }
        }, 1500)
    }

    private fun showInitialSetup() {
        InitialSetupDialog(
            context = this,
            onRequestPermission = {
                permissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
            },
            onComplete = {
                goToMain()
            }
        ).show()
    }

    private fun goToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}