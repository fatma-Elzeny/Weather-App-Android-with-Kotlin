package com.example.weatherapp.Settings.viewmodel

import com.example.weatherapp.Settings.model.Language
import com.example.weatherapp.Settings.model.LocationMode
import com.example.weatherapp.Settings.model.SettingsData
import com.example.weatherapp.Settings.model.TemperatureUnit
import com.example.weatherapp.Settings.model.WindSpeedUnit

interface ISettingsViewModel {
    fun updateLocationMode(mode: LocationMode)
    fun updateTemperatureUnit(unit: TemperatureUnit)
    fun updateWindSpeedUnit(unit: WindSpeedUnit)
    fun updateLanguage(language: Language)
    fun updateNotifications(enabled: Boolean)
    fun saveAndPost(settings: SettingsData)
}