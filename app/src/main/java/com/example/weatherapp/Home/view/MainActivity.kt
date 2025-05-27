package com.example.weatherapp.Home.view

import android.annotation.SuppressLint
import com.google.android.gms.location.LocationServices
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Bundle
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
import java.text.NumberFormat
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
    private var currentLanguage: Language? = null

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            getCurrentLocation()
        } else {
            Toast.makeText(this, getString(R.string.location_permission_required), Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbarDrawer()
        settingsRepo = SettingsRepository(applicationContext)
        val currentSettings = settingsRepo.loadSettings()
        currentLanguage = currentSettings.language
        applyLanguage(currentSettings.language)

        setupAdapters()

        val repository = WeatherRepositoryImpl(
            remote = WeatherRemoteDataSourceImpl(RetrofitClient.api),
            local = WeatherLocalDataSourceImpl(WeatherDatabase.getInstance(this).weatherDao()),
            this
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
                getUnit(currentSettings.temperatureUnit),
                getLang(currentSettings.language)
            )
            return
        }

        when (currentSettings.locationMode) {
            LocationMode.GPS -> checkLocationPermission()
            LocationMode.MAP -> {
                val lat = settingsRepo.getMapLat()
                val lon = settingsRepo.getMapLon()
                viewModel.fetchForecast(lat, lon, getUnit(currentSettings.temperatureUnit), getLang(currentSettings.language))
            }
        }
    }

    private fun setupToolbarDrawer() {
        val drawerLayout = findViewById<DrawerLayout>(R.id.drawerLayout)
        val navView = findViewById<NavigationView>(R.id.navigationView)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar.title = getString(R.string.app_name)
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
        dailyAdapter = DailyForecastAdapter(this)
        binding.rvHourly.adapter = hourlyAdapter
        binding.rvDaily.adapter = dailyAdapter
    }

    private fun setupObservers() {
        viewModel.forecast.observe(this) { response ->
            response?.let {
                val settings = settingsRepo.loadSettings()
                updateCurrentWeather(it, settings)
                updateForecastLists(it.list, it.city.timezone)
                showLastUpdated(System.currentTimeMillis(), settings.language)
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
            != PackageManager.PERMISSION_GRANTED) return

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            val settings = settingsRepo.loadSettings()
            if (location != null) {
                viewModel.fetchForecast(location.latitude, location.longitude, getUnit(settings.temperatureUnit), getLang(settings.language))
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
                showLastUpdated(cached.lastUpdated, settings.language)
            } else {
                viewModel.setError(getString(R.string.no_cached_data))
            }
        }
    }

    private fun updateCurrentWeather(response: WeatherResponse, settings: SettingsData) {
        val current = response.list.firstOrNull() ?: return

        val lang = settings.language
        val locale = if (lang == Language.ARABIC) Locale("ar") else Locale("en")

        val langCode = if (lang == Language.ARABIC) "ar" else "en"
        val localizedCity = response.city.local_names?.get(langCode) ?: response.city.name
        binding.tvCity.text = localizedCity

        // 🕒 Format current local date/time
        binding.tvDateTime.text = SimpleDateFormat("EEEE، d MMM • HH:mm", locale)
            .apply { timeZone = TimeZone.getDefault() }
            .format(Date(System.currentTimeMillis()))
        val numberFormat = NumberFormat.getInstance(locale)
        // 🌡️ Temperature
        val tempUnit = when (settings.temperatureUnit) {
            TemperatureUnit.CELSIUS -> if (lang == Language.ARABIC) "درجة مئوية" else "°C"
            TemperatureUnit.FAHRENHEIT -> if (lang == Language.ARABIC) "فهرنهايت" else "°F"
        }
        val temp = numberFormat.format(current.main.temp.toInt())

        binding.tvTemp.text = "$temp $tempUnit"

        // ☁️ Weather description
        val weatherDescription = current.weather.firstOrNull()?.description?.replaceFirstChar {
            if (it.isLowerCase()) it.titlecase(locale) else it.toString()
        } ?: ""
        binding.tvWeatherDesc.text = weatherDescription

        // 💧 Humidity (already percentage-based)
        binding.tvHumidityValue.text =
            if (lang == Language.ARABIC) "${current.main.humidity}% رطوبة" else "${current.main.humidity}%"

        // 📊 Pressure
        binding.tvPressureValue.text =
            if (lang == Language.ARABIC) "${current.main.pressure} هيكتوباسكال" else "${current.main.pressure} hPa"

        // 🌬️ Wind speed
        val windSpeed = when (settings.windSpeedUnit) {
            WindSpeedUnit.M_S -> if (lang == Language.ARABIC) "${current.wind.speed} م/ث" else "${current.wind.speed} m/s"
            WindSpeedUnit.KM_H -> if (lang == Language.ARABIC) "${current.wind.speed * 3.6} كم/س" else "${current.wind.speed * 3.6} km/h"
            WindSpeedUnit.MPH -> if (lang == Language.ARABIC) "${current.wind.speed * 2.23694} ميل/س" else "${current.wind.speed * 2.23694} mph"
        }
        binding.tvWindValue.text = windSpeed

        // 🌤️ Weather icon
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
        return if (unit == TemperatureUnit.CELSIUS) "metric" else "imperial"
    }

    private fun getLang(language: Language): String {
        return if (language == Language.ARABIC) "ar" else "en"
    }

    @SuppressLint("StringFormatInvalid")
    private fun showLastUpdated(timestampMillis: Long, lang: Language) {
        val locale = if (lang == Language.ARABIC) Locale("ar") else Locale("en")
        val sdf = SimpleDateFormat("MMM dd, yyyy • HH:mm", locale)
        binding.tvLastUpdated.text = getString(R.string.last_updated, sdf.format(Date(timestampMillis)))
    }

    private fun applyLanguage(lang: Language) {
        val locale = if (lang == Language.ARABIC) Locale("ar") else Locale("en")
        val config = resources.configuration
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)
    }

    override fun onResume() {
        super.onResume()

        val currentSettings = settingsRepo.loadSettings()
        if (currentLanguage != currentSettings.language) {
            currentLanguage = currentSettings.language
            applyLanguage(currentSettings.language)
            // optionally re-fetch data if language changed
        }

        if (!overrideLat!!.isNaN() && !overrideLon!!.isNaN()) return

        when (currentSettings.locationMode) {
            LocationMode.GPS -> checkLocationPermission()
            LocationMode.MAP -> {
                val lat = settingsRepo.getMapLat()
                val lon = settingsRepo.getMapLon()
                viewModel.fetchForecast(lat, lon, getUnit(currentSettings.temperatureUnit), getLang(currentSettings.language))
            }
        }
    }
}
