package com.example.weatherapp.Settings.view

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.weatherapp.Alerts.view.AlertsActivity
import com.example.weatherapp.Favourites.view.FavoritesActivity
import com.example.weatherapp.Home.view.MainActivity
import com.example.weatherapp.LocaleHelper
import com.example.weatherapp.MapPickerActivity
import com.example.weatherapp.R
import com.example.weatherapp.Settings.model.Language
import com.example.weatherapp.Settings.model.LocationMode
import com.example.weatherapp.Settings.model.TemperatureUnit
import com.example.weatherapp.Settings.model.WindSpeedUnit
import com.example.weatherapp.Settings.viewmodel.SettingsViewModel
import com.example.weatherapp.WeatherNotificationWorker
import com.example.weatherapp.databinding.ActivitySettingsBinding
import com.google.android.material.navigation.NavigationView
import java.util.Locale
import java.util.concurrent.TimeUnit

class SettingsActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySettingsBinding
    private val viewModel: SettingsViewModel by viewModels()
    private val MAP_PICKER_REQUEST_CODE = 123

    private val requestNotificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Toast.makeText(this, "Notification permission granted", Toast.LENGTH_SHORT).show()
            scheduleWeatherNotifications()
        } else {
            Toast.makeText(this, "Notification permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    override fun attachBaseContext(newBase: Context?) {
        val langCode = LocaleHelper.getLanguage(newBase!!)
        val context = LocaleHelper.wrap(newBase, langCode)
        super.attachBaseContext(context)
    }

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
            refreshSpinners(viewModel.settingsLiveData.value?.language ?: Language.ENGLISH)

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

            // 👇 Update locale without recreate
            val locale = if (lang == Language.ARABIC) Locale("ar") else Locale("en")
            Locale.setDefault(locale)
            val config = resources.configuration
            config.setLocale(locale)
            resources.updateConfiguration(config, resources.displayMetrics)
            setupNavigationDrawer()
            refreshSpinners(lang)
            // Rebind strings manually if needed (optional)
            binding.toolbar.title = getString(R.string.settings_toolbar)
            binding.notificationsSwitch.text = getString(R.string.enable_notifications)
        }

        binding.notificationsSwitch.setOnCheckedChangeListener { _, isChecked ->
            viewModel.updateNotifications(isChecked)
            handleNotifications(isChecked)
        }
    }

    private fun setupNavigationDrawer() {
        val drawerLayout = findViewById<DrawerLayout>(R.id.drawerLayout)
        val navView = findViewById<NavigationView>(R.id.navigationView)
        navView.menu.clear() // 🧹 Clear existing menu
        navView.inflateMenu(R.menu.drawer_menu) // 🔄 Re-inflate to apply new language

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar.title = getString(R.string.app_name)
        val toggle = ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open, R.string.close)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        navView.setNavigationItemSelectedListener {
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

  /*  private fun applyLanguage(lang: Language) {
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
    }*/

    private fun handleNotifications(enabled: Boolean) {
        if (enabled) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(
                        this,
                        android.Manifest.permission.POST_NOTIFICATIONS
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    requestNotificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                    return
                }
            }
            scheduleWeatherNotifications()
        } else {
            cancelWeatherNotifications()
        }
    }

    private fun scheduleWeatherNotifications() {
        val request = PeriodicWorkRequestBuilder<WeatherNotificationWorker>(1, TimeUnit.HOURS).build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "weather_alerts",
            ExistingPeriodicWorkPolicy.REPLACE,
            request
        )
    }
    private fun cancelWeatherNotifications() {
        WorkManager.getInstance(this).cancelUniqueWork("weather_alerts")
    }
    private fun refreshSpinners(language: Language) {
        val locale = if (language == Language.ARABIC) Locale("ar") else Locale("en")
        val config = Configuration(resources.configuration)
        config.setLocale(locale)
        val localizedContext = createConfigurationContext(config)

        val tempAdapter = ArrayAdapter.createFromResource(
            localizedContext,
            R.array.temperature_units,
            android.R.layout.simple_spinner_item
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }

        val windAdapter = ArrayAdapter.createFromResource(
            localizedContext,
            R.array.wind_speed_units,
            android.R.layout.simple_spinner_item
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }

        binding.tempUnitSpinner.adapter = tempAdapter
        binding.windUnitSpinner.adapter = windAdapter

        // Keep the current selections
        val settings = viewModel.settingsLiveData.value
        if (settings != null) {
            binding.tempUnitSpinner.setSelection(settings.temperatureUnit.ordinal)
            binding.windUnitSpinner.setSelection(settings.windSpeedUnit.ordinal)
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
