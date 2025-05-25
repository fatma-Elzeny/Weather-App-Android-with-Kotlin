package com.example.weatherapp.data.repo

import androidx.lifecycle.LiveData
import com.example.weatherapp.data.model.FavoriteLocation
import com.example.weatherapp.data.model.WeatherAlert
import com.example.weatherapp.data.model.WeatherResponse

interface WeatherRepository {
    suspend fun getWeatherForecast(
        lat: Double,
        lon: Double,
        units: String
    ): WeatherResponse



    // Favorites
    suspend fun insertFavorite(location: FavoriteLocation)
    suspend fun deleteFavorite(location: FavoriteLocation)
    fun getAllFavorites(): LiveData<List<FavoriteLocation>>

    // Alerts
    suspend fun insertAlert(alert: WeatherAlert)
    suspend fun deleteAlert(alert: WeatherAlert)
    fun getAllAlerts(): LiveData<List<WeatherAlert>>

    // Cached Weather
    suspend fun saveCachedWeather(response: WeatherResponse)
    suspend fun getCachedWeather(city: String): WeatherResponse?
}