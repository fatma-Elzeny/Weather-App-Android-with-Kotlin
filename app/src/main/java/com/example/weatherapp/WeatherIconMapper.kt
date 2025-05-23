package com.example.weatherapp

// Create a new file: WeatherIconMapper.kt
object WeatherIconMapper {
    // Map API icon codes (e.g., "01d") to your app's drawable resources
    fun getIconResource(iconCode: String): Int {
        return when (iconCode) {
            "01d" -> R.drawable.ic_sunny
            "01n" -> R.drawable.ic_night_clear
            "02d", "02n", "03d", "03n", "04d", "04n" -> R.drawable.ic_cloudy
            "09d", "09n", "10d", "10n" -> R.drawable.ic_rain
            "11d", "11n" -> R.drawable.ic_storm
            "13d", "13n" -> R.drawable.ic_snow
            "50d", "50n" -> R.drawable.ic_fog
            else -> R.drawable.ic_default // Fallback icon
        }
    }
}