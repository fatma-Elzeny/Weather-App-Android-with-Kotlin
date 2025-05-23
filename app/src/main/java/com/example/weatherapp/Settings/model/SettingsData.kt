package com.example.weatherapp.Settings.model

data class SettingsData(
    val locationMode: LocationMode = LocationMode.GPS,
    val temperatureUnit: TemperatureUnit = TemperatureUnit.CELSIUS,
    val windSpeedUnit: WindSpeedUnit = WindSpeedUnit.M_S,
    val language: Language = Language.ENGLISH,
    val notificationsEnabled: Boolean = true
)

enum class LocationMode { GPS, MAP }
enum class TemperatureUnit { CELSIUS, FAHRENHEIT }
enum class WindSpeedUnit { M_S, KM_H, MPH }
enum class Language { ENGLISH, ARABIC }
