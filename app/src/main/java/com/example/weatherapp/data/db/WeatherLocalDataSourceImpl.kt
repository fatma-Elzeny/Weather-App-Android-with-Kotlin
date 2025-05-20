package com.example.weatherapp.data.db

import androidx.lifecycle.LiveData

class WeatherLocalDataSourceImpl(private val dao: WeatherDAO) : WeatherLocalDataSource {
    override suspend fun insertFavorite(location: FavoriteLocation) = dao.insertFavorite(location)
    override suspend fun deleteFavorite(location: FavoriteLocation) = dao.deleteFavorite(location)
    override fun getAllFavorites(): LiveData<List<FavoriteLocation>> = dao.getAllFavorites()

    override suspend fun insertAlert(alert: WeatherAlert) = dao.insertAlert(alert)
    override suspend fun deleteAlert(alert: WeatherAlert) = dao.deleteAlert(alert)
    override fun getAllAlerts(): LiveData<List<WeatherAlert>> = dao.getAllAlerts()
}