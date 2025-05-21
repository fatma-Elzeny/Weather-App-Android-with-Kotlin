package com.example.weatherapp.LocationPermission.view

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.example.weatherapp.LocationPermission.viewmodel.LocationViewModel
import com.example.weatherapp.LocationPermission.viewmodel.LocationViewModelFactory
import com.example.weatherapp.MainActivity
import com.example.weatherapp.R

class LocationPermissionActivity : AppCompatActivity() {

    private lateinit var viewModel: LocationViewModel

    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                viewModel.fetchLocation()
            } else {
                Toast.makeText(this, "Permission is required", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_location_permission)

        viewModel = ViewModelProvider(
            this,
            LocationViewModelFactory(this)
        )[LocationViewModel::class.java]

        val allowBtn = findViewById<Button>(R.id.btnRequestPermission)
        allowBtn.setOnClickListener {
            permissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
        }

        viewModel.locationResult.observe(this) { location ->
            if (location != null) {
                val intent = Intent(this, MainActivity::class.java).apply {
                    putExtra("lat", location.latitude)
                    putExtra("lon", location.longitude)
                }
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "Could not get location", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
