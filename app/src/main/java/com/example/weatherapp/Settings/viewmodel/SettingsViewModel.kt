package com.example.weatherapp.Settings.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.weatherapp.Settings.model.Language
import com.example.weatherapp.Settings.model.LocationMode
import com.example.weatherapp.Settings.model.SettingsData
import com.example.weatherapp.Settings.model.SettingsRepository
import com.example.weatherapp.Settings.model.TemperatureUnit
import com.example.weatherapp.Settings.model.WindSpeedUnit

class SettingsViewModel(application: Application) : AndroidViewModel(application),
    ISettingsViewModel {
    private val repository = SettingsRepository(application.applicationContext)

    private val _settingsLiveData = MutableLiveData<SettingsData>()
    val settingsLiveData: LiveData<SettingsData> = _settingsLiveData

    init {
        _settingsLiveData.value = repository.loadSettings()
    }
    private val _pickedLocation = MutableLiveData<Pair<Double, Double>?>()
    val pickedLocation: LiveData<Pair<Double, Double>?> = _pickedLocation

    fun updatePickedLocation(lat: Double, lon: Double) {
        _pickedLocation.value = Pair(lat, lon)

    }

    override fun updateLocationMode(mode: LocationMode) {
        val current = _settingsLiveData.value ?: return
        val updated = current.copy(locationMode = mode)
        saveAndPost(updated)
    }

    override fun updateTemperatureUnit(unit: TemperatureUnit) {
        val current = _settingsLiveData.value ?: return
        val updated = current.copy(temperatureUnit = unit)
        saveAndPost(updated)
    }

    override fun updateWindSpeedUnit(unit: WindSpeedUnit) {
        val current = _settingsLiveData.value ?: return
        val updated = current.copy(windSpeedUnit = unit)
        saveAndPost(updated)
    }

    override fun updateLanguage(language: Language) {
        val current = _settingsLiveData.value ?: return
        val updated = current.copy(language = language)
        saveAndPost(updated)
    }

    override fun updateNotifications(enabled: Boolean) {
        val current = _settingsLiveData.value ?: return
        val updated = current.copy(notificationsEnabled = enabled)
        saveAndPost(updated)
    }

    override fun saveAndPost(settings: SettingsData) {
        repository.saveSettings(settings)
        _settingsLiveData.value = settings
    }
}
