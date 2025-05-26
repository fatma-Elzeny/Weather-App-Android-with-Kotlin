package com.example.weatherapp.data.db

import androidx.lifecycle.LiveData
import com.example.weatherapp.data.model.CachedWeather
import com.example.weatherapp.data.model.FavoriteLocation
import com.example.weatherapp.data.model.WeatherAlert

interface WeatherLocalDataSource {
    // Favorites
    suspend fun insertFavorite(location: FavoriteLocation)
    suspend fun deleteFavorite(location: FavoriteLocation)
    fun getAllFavorites(): LiveData<List<FavoriteLocation>>

    // Alerts
    suspend fun insertAlert(alert: WeatherAlert)
    suspend fun deleteAlert(alert: WeatherAlert)
    fun getAllAlerts(): LiveData<List<WeatherAlert>>

    // Cached Weather
    suspend fun insertCachedWeather(weather: CachedWeather)
    suspend fun getCachedWeather(city: String): CachedWeather?
    suspend fun deleteCachedWeather(city: String)
    suspend fun getAllAlertsOnce(): List<WeatherAlert>
}