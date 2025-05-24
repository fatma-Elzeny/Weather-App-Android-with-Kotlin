package com.example.weatherapp.Settings.view

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.AdapterView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import com.example.weatherapp.Alerts.view.AlertsActivity
import com.example.weatherapp.Favourites.view.FavoritesActivity
import com.example.weatherapp.Home.view.MainActivity
import com.example.weatherapp.MapPickerActivity
import com.example.weatherapp.R
import com.example.weatherapp.Settings.model.Language
import com.example.weatherapp.Settings.model.LocationMode
import com.example.weatherapp.Settings.model.TemperatureUnit
import com.example.weatherapp.Settings.model.WindSpeedUnit
import com.example.weatherapp.Settings.viewmodel.SettingsViewModel
import com.example.weatherapp.databinding.ActivitySettingsBinding
import java.util.Locale

class SettingsActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySettingsBinding
    private val viewModel: SettingsViewModel by viewModels()
    private val MAP_PICKER_REQUEST_CODE = 123
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupNavigationDrawer()
        // Observe current settings and update UI
        viewModel.settingsLiveData.observe(this) { settings ->
            // Update UI selections based on settings
            when (settings.locationMode) {
                LocationMode.GPS -> binding.radioGps.isChecked = true
                LocationMode.MAP -> binding.radioMap.isChecked = true
            }
            binding.tempUnitSpinner.setSelection(settings.temperatureUnit.ordinal)
            binding.windUnitSpinner.setSelection(settings.windSpeedUnit.ordinal)
            binding.languageSwitch.isChecked = (settings.language == Language.ARABIC)
            binding.notificationsSwitch.isChecked = settings.notificationsEnabled
        }

        binding.radioGroupLocation.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.radioGps -> viewModel.updateLocationMode(LocationMode.GPS)
                R.id.radioMap -> {
                    viewModel.updateLocationMode(LocationMode.MAP)
                    openMapToPickLocation()
                }
            }
        }

        binding.tempUnitSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
                val unit = TemperatureUnit.values()[pos]
                viewModel.updateTemperatureUnit(unit)
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        binding.windUnitSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
                val unit = WindSpeedUnit.values()[pos]
                viewModel.updateWindSpeedUnit(unit)
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        binding.languageSwitch.setOnCheckedChangeListener { _, isChecked ->
            val lang = if (isChecked) Language.ARABIC else Language.ENGLISH
            viewModel.updateLanguage(lang)
            applyLanguage(lang)
        }

        binding.notificationsSwitch.setOnCheckedChangeListener { _, isChecked ->
            viewModel.updateNotifications(isChecked)
            handleNotifications(isChecked)
        }
    }

    private fun setupNavigationDrawer() {
        setSupportActionBar(binding.toolbar)
        val toggle = ActionBarDrawerToggle(this, binding.drawerLayout, binding.toolbar, R.string.open, R.string.close)
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        binding.navigationView.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.nav_home -> startActivity(Intent(this, MainActivity::class.java))
                R.id.nav_alerts -> startActivity(Intent(this, AlertsActivity::class.java))
                R.id.nav_favorites -> startActivity(Intent(this, FavoritesActivity::class.java))
            }
            true
        }
    }

    private fun openMapToPickLocation() {
        val intent = Intent(this, MapPickerActivity::class.java)
        startActivityForResult(intent, MAP_PICKER_REQUEST_CODE)
    }

    private fun applyLanguage(lang: Language) {
        val locale = if (lang == Language.ARABIC) Locale("ar") else Locale("en")
        Locale.setDefault(locale)
        val config = resources.configuration
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)

        // Delay recreate slightly to allow any UI events to settle
        Handler(Looper.getMainLooper()).post {
            if (!isFinishing && !isDestroyed) {
                recreate()
            }
        }
    }

    private fun handleNotifications(enabled: Boolean) {
        if (enabled) {
            // Register notifications or schedule
        } else {
            // Cancel notifications
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == MAP_PICKER_REQUEST_CODE && resultCode == RESULT_OK) {
            val lat = data?.getDoubleExtra("lat", 0.0) ?: 0.0
            val lon = data?.getDoubleExtra("lon", 0.0) ?: 0.0
            // Save picked location in ViewModel or repository
            // Example: ViewModel has a method updatePickedLocation(lat, lon)
            viewModel.updatePickedLocation(lat, lon)
            Toast.makeText(this, "Location saved: $lat, $lon", Toast.LENGTH_SHORT).show()
        }
    }
}
