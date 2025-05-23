package com.example.weatherapp.Settings.model

import android.content.Context

class SettingsRepository(private val context: Context) {
    val prefs = context.getSharedPreferences("settings_prefs", Context.MODE_PRIVATE)

    fun saveSettings(settings: SettingsData) {
        prefs.edit().apply {
            putString("location_mode", settings.locationMode.name)
            putString("temp_unit", settings.temperatureUnit.name)
            putString("wind_unit", settings.windSpeedUnit.name)
            putString("language", settings.language.name)
            putBoolean("notifications_enabled", settings.notificationsEnabled)
        }.apply()
    }

    fun loadSettings(): SettingsData {
        val locationMode = prefs.getString("location_mode", LocationMode.GPS.name)?.let { LocationMode.valueOf(it) } ?: LocationMode.GPS
        val tempUnit = prefs.getString("temp_unit", TemperatureUnit.CELSIUS.name)?.let { TemperatureUnit.valueOf(it) } ?: TemperatureUnit.CELSIUS
        val windUnit = prefs.getString("wind_unit", WindSpeedUnit.M_S.name)?.let { WindSpeedUnit.valueOf(it) } ?: WindSpeedUnit.M_S
        val language = prefs.getString("language", Language.ENGLISH.name)?.let { Language.valueOf(it) } ?: Language.ENGLISH
        val notifications = prefs.getBoolean("notifications_enabled", true)
        return SettingsData(locationMode, tempUnit, windUnit, language, notifications)
    }
}
