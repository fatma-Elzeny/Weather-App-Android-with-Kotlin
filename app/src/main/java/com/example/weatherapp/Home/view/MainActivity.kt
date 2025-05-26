package com.example.weatherapp.Home.view

import android.content.Context
import com.google.android.gms.location.LocationServices
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.ActionBarDrawerToggle

import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.weatherapp.Alerts.view.AlertsActivity
import com.example.weatherapp.Favourites.view.FavoritesActivity
import com.example.weatherapp.Home.viewmodel.MainViewModel
import com.example.weatherapp.Home.viewmodel.MainViewModelFactory
import com.example.weatherapp.R
import com.example.weatherapp.Settings.model.Language
import com.example.weatherapp.Settings.model.LocationMode
import com.example.weatherapp.Settings.model.SettingsData
import com.example.weatherapp.Settings.model.SettingsRepository
import com.example.weatherapp.Settings.model.TemperatureUnit
import com.example.weatherapp.Settings.model.WindSpeedUnit
import com.example.weatherapp.Settings.view.SettingsActivity
import com.example.weatherapp.WeatherIconMapper
import com.example.weatherapp.data.db.WeatherDatabase
import com.example.weatherapp.data.db.WeatherLocalDataSourceImpl
import com.example.weatherapp.data.model.ForecastItem
import com.example.weatherapp.data.model.WeatherResponse
import com.example.weatherapp.data.network.RetrofitClient
import com.example.weatherapp.data.network.WeatherRemoteDataSourceImpl
import com.example.weatherapp.data.repo.WeatherRepositoryImpl
import com.example.weatherapp.databinding.ActivityMainBinding
import com.google.android.material.navigation.NavigationView
import com.google.gson.Gson
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

// MainActivity.kt
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainViewModel
    private lateinit var hourlyAdapter: HourlyForecastAdapter
    private lateinit var dailyAdapter: DailyForecastAdapter
    private lateinit var settingsRepo: SettingsRepository

    private var overrideLat: Double? = null
    private var overrideLon: Double? = null

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            getCurrentLocation()
        } else {
            Toast.makeText(this, "Location permission required", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbarDrawer()
        settingsRepo = SettingsRepository(applicationContext)
        val currentSettings = settingsRepo.loadSettings()
       // applyLanguage(currentSettings.language)

        setupAdapters()
       // setupToolbarDrawer()

        val repository = WeatherRepositoryImpl(
            remote = WeatherRemoteDataSourceImpl(RetrofitClient.api),
            local = WeatherLocalDataSourceImpl(WeatherDatabase.getInstance(this).weatherDao())
        )
        val factory = MainViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[MainViewModel::class.java]

        setupObservers()

        overrideLat = intent.getDoubleExtra("lat", Double.NaN)
        overrideLon = intent.getDoubleExtra("lon", Double.NaN)

        if (!overrideLat!!.isNaN() && !overrideLon!!.isNaN()) {
            viewModel.fetchForecast(
                overrideLat!!,
                overrideLon!!,
                getUnit(currentSettings.temperatureUnit)
            )
            return
        }

        when (currentSettings.locationMode) {
            LocationMode.GPS -> checkLocationPermission()
            LocationMode.MAP -> {
                val lat = settingsRepo.getMapLat()
                val lon = settingsRepo.getMapLon()
                viewModel.fetchForecast(lat, lon, getUnit(currentSettings.temperatureUnit))
            }
        }
    }

    private fun setupToolbarDrawer() {
        val drawerLayout = findViewById<DrawerLayout>(R.id.drawerLayout)
        val navView = findViewById<NavigationView>(R.id.navigationView)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar.title = "Air Cast"
        val toggle = ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open, R.string.close)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        navView.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.nav_home -> drawerLayout.closeDrawers()
                R.id.nav_favorites -> startActivity(Intent(this, FavoritesActivity::class.java))
                R.id.nav_alerts -> startActivity(Intent(this, AlertsActivity::class.java))
                R.id.nav_settings -> startActivity(Intent(this, SettingsActivity::class.java))
            }
            true
        }
    }

    private fun setupAdapters() {
        hourlyAdapter = HourlyForecastAdapter(0)
        dailyAdapter = DailyForecastAdapter()
        binding.rvHourly.adapter = hourlyAdapter
        binding.rvDaily.adapter = dailyAdapter
    }

    private fun setupObservers() {
        viewModel.forecast.observe(this) { response ->
            response?.let {
                val settings = settingsRepo.loadSettings()
                updateCurrentWeather(it, settings)
                updateForecastLists(it.list, it.city.timezone)
                showLastUpdated(System.currentTimeMillis())
            }
        }

        viewModel.error.observe(this) { error ->
            error?.let {
                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                this, android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            getCurrentLocation()
        } else {
            requestPermissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private fun getCurrentLocation() {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            val settings = settingsRepo.loadSettings()
            if (location != null) {
                viewModel.fetchForecast(location.latitude, location.longitude, getUnit(settings.temperatureUnit))
            } else {
                fetchFromCacheOrNotify(settings)
            }
        }.addOnFailureListener {
            fetchFromCacheOrNotify(settingsRepo.loadSettings())
        }
    }

    private fun fetchFromCacheOrNotify(settings: SettingsData) {
        lifecycleScope.launch {
            val prefs = getSharedPreferences("prefs", MODE_PRIVATE)
            val city = prefs.getString("last_city", null)
            val cached = city?.let {
                WeatherDatabase.getInstance(this@MainActivity)
                    .weatherDao()
                    .getCachedWeather(it)
            }

            if (cached != null) {
                val response = Gson().fromJson(cached.data, WeatherResponse::class.java)
                updateCurrentWeather(response, settings)
                updateForecastLists(response.list, response.city.timezone)
                showLastUpdated(cached.lastUpdated)
            } else {
                viewModel.setError("No cached data") // ✅ Cleaner if using wrapper method

            }
        }
    }


    private fun updateCurrentWeather(response: WeatherResponse, settings: SettingsData) {
        val current = response.list.firstOrNull() ?: return
        binding.tvCity.text = response.city.name
        getSharedPreferences("prefs", MODE_PRIVATE)
            .edit()
            .putString("last_city", response.city.name)
            .apply()
        binding.tvDateTime.text = SimpleDateFormat("EEEE, MMM d • HH:mm", Locale.getDefault())
            .apply { timeZone = TimeZone.getDefault() }
            .format(Date(System.currentTimeMillis()))

        val tempUnitSymbol = when (settings.temperatureUnit) {
            TemperatureUnit.CELSIUS -> "°C"
            TemperatureUnit.FAHRENHEIT -> "°F"
        }
        binding.tvTemp.text = "${current.main.temp.toInt()}$tempUnitSymbol"
        binding.tvWeatherDesc.text = current.weather.firstOrNull()?.description?.replaceFirstChar {
            if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
        }

        binding.tvHumidityValue.text = "${current.main.humidity}%"
        binding.tvPressureValue.text = "${current.main.pressure} hPa"

        val windSpeed = when (settings.windSpeedUnit) {
            WindSpeedUnit.M_S -> "${current.wind.speed} m/s"
            WindSpeedUnit.KM_H -> "${current.wind.speed * 3.6} km/h"
            WindSpeedUnit.MPH -> "${current.wind.speed * 2.23694} mph"
        }
        binding.tvWindValue.text = windSpeed

        val iconCode = current.weather.firstOrNull()?.icon ?: "01d"
        val iconRes = WeatherIconMapper.getIconResource(iconCode)
        binding.imgWeatherIcon.setImageResource(iconRes)
    }

    private fun updateForecastLists(items: List<ForecastItem>, timezoneOffsetSeconds: Int) {
        hourlyAdapter = HourlyForecastAdapter(timezoneOffsetSeconds)
        binding.rvHourly.adapter = hourlyAdapter
        hourlyAdapter.submitList(items.take(8))
        dailyAdapter.submitList(processDailyForecast(items))
    }

    private fun processDailyForecast(items: List<ForecastItem>): List<ForecastItem> {
        return items.groupBy { it.dateTime.substring(0, 10) }
            .mapNotNull { (_, group) ->
                if (group.isNotEmpty()) {
                    val minTemp = group.minOf { it.main.tempMin }
                    val maxTemp = group.maxOf { it.main.tempMax }
                    group.first().copy(
                        main = group.first().main.copy(
                            tempMin = minTemp,
                            tempMax = maxTemp
                        )
                    )
                } else null
            }
            .sortedBy { it.timestamp }
    }

    private fun getUnit(unit: TemperatureUnit): String {
        return when (unit) {
            TemperatureUnit.CELSIUS -> "metric"
            TemperatureUnit.FAHRENHEIT -> "imperial"
        }
    }

    private fun showLastUpdated(timestampMillis: Long) {
        val sdf = SimpleDateFormat("MMM dd, yyyy • HH:mm", Locale.getDefault())
        val formatted = sdf.format(Date(timestampMillis))
        binding.tvLastUpdated.text = "Last updated: $formatted"
    }

    private var currentLanguage: Language? = null

  /*  private fun applyLanguage(lang: Language) {
        if (currentLanguage == lang) return
        currentLanguage = lang
        val locale = if (lang == Language.ARABIC) Locale("ar") else Locale("en")
        Locale.setDefault(locale)
        val config = resources.configuration
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)
        if (!isFinishing && !isDestroyed) {
            recreate()
        }
    }*/

    override fun onResume() {
        super.onResume()

        if (!overrideLat!!.isNaN() && !overrideLon!!.isNaN()) return

        val currentSettings = settingsRepo.loadSettings()
     //   applyLanguage(currentSettings.language)

        when (currentSettings.locationMode) {
            LocationMode.GPS -> checkLocationPermission()
            LocationMode.MAP -> {
                val lat = settingsRepo.getMapLat()
                val lon = settingsRepo.getMapLon()
                viewModel.fetchForecast(lat, lon, getUnit(currentSettings.temperatureUnit))
            }
        }
    }
}