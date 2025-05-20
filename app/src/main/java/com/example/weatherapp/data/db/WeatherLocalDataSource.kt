package com.example.weatherapp.data.db

import androidx.lifecycle.LiveData

interface WeatherLocalDataSource {
    suspend fun insertFavorite(location: FavoriteLocation)
    suspend fun deleteFavorite(location: FavoriteLocation)
    fun getAllFavorites(): LiveData<List<FavoriteLocation>>

    suspend fun insertAlert(alert: WeatherAlert)
    suspend fun deleteAlert(alert: WeatherAlert)
    fun getAllAlerts(): LiveData<List<WeatherAlert>>
}