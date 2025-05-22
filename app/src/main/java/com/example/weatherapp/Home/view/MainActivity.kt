package com.example.weatherapp.Home.view

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.weatherapp.Home.viewmodel.MainViewModel
import com.example.weatherapp.Home.viewmodel.MainViewModelFactory
import com.example.weatherapp.Home.viewmodel.MainViewModelInterface
import com.example.weatherapp.R
import com.example.weatherapp.data.db.WeatherDatabase
import com.example.weatherapp.data.db.WeatherLocalDataSourceImpl
import com.example.weatherapp.data.model.ForecastItem
import com.example.weatherapp.data.model.WeatherResponse
import com.example.weatherapp.data.network.RetrofitClient
import com.example.weatherapp.data.network.WeatherRemoteDataSourceImpl
import com.example.weatherapp.data.repo.WeatherRepositoryImpl
import com.example.weatherapp.databinding.ActivityMainBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.math.roundToInt
// MainActivity.kt
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainViewModel
    private lateinit var hourlyAdapter: HourlyForecastAdapter
    private lateinit var dailyAdapter: DailyForecastAdapter

    // Location permission launcher
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

        setupAdapters()
        val repository = WeatherRepositoryImpl(
            remote = WeatherRemoteDataSourceImpl(RetrofitClient.api),
            local = WeatherLocalDataSourceImpl(WeatherDatabase.getInstance(this).weatherDao())
        )

        val factory = MainViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[MainViewModel::class.java]

        setupObservers()
        checkLocationPermission()
    }

    private fun setupAdapters() {
        hourlyAdapter = HourlyForecastAdapter()
        dailyAdapter = DailyForecastAdapter()

        binding.rvHourly.apply {
            layoutManager =
                LinearLayoutManager(this@MainActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = hourlyAdapter
        }

        binding.rvDaily.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = dailyAdapter
        }
    }

    private fun setupObservers() {
        viewModel.forecast.observe(this) { response ->
            response?.let {
                updateCurrentWeather(it)
                updateForecastLists(it.list)
            }
        }

        viewModel.error.observe(this) { error ->
            error?.let {
                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun checkLocationPermission() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                getCurrentLocation()
            }

            else -> {
                requestPermissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }

    private fun getCurrentLocation() {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
        location?.let {
            viewModel.fetchForecast(it.latitude, it.longitude, "metric")
        } ?: run {
            Toast.makeText(this, "Enable location services", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateCurrentWeather(response: WeatherResponse) {
        val current = response.list.firstOrNull() ?: return

        // Location and time
        binding.tvCity.text = response.city.name
        binding.tvDateTime.text = formatDateTime(current.timestamp, "EEEE, MMM d • HH:mm")

        // Temperature and description
        binding.tvTemp.text = getString(R.string.temperature_format, current.main.temp.toInt())
        binding.tvWeatherDesc.text = current.weather.firstOrNull()?.description?.capitalize()

        // Weather stats
        binding.tvHumidityValue.text = getString(R.string.percentage_format, current.main.humidity)
        binding.tvPressureValue.text = getString(R.string.pressure_format, current.main.pressure)
        binding.tvWindValue.text = getString(R.string.speed_format, current.wind.speed.toInt())

        // Weather icon
        current.weather.firstOrNull()?.icon?.let { icon ->
            Glide.with(this)
                .load("https://openweathermap.org/img/wn/${icon}@4x.png")
                .placeholder(R.drawable.ic_placeholder)
                .into(binding.imgWeatherIcon)
        }
    }

    private fun updateForecastLists(items: List<ForecastItem>) {
        val currentTime = System.currentTimeMillis() / 1000

        // Filter all items for today (local time)
        val hourlyItems = items.filter { isToday(it.timestamp) }
        hourlyAdapter.submitList(hourlyItems)

        // Daily forecast logic remains the same
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

    private fun isToday(timestamp: Long): Boolean {
        val calendar = Calendar.getInstance(TimeZone.getDefault()) // Use device's time zone
        val today = calendar.apply { time = Date() }.get(Calendar.DAY_OF_YEAR)

        calendar.time = Date(timestamp * 1000)
        val itemDay = calendar.get(Calendar.DAY_OF_YEAR)

        return today == itemDay
    }

    private fun formatDateTime(timestamp: Long, pattern: String): String {
        return SimpleDateFormat(pattern, Locale.getDefault()).apply {
            timeZone = TimeZone.getDefault()
        }.format(Date(timestamp * 1000))
    }
}