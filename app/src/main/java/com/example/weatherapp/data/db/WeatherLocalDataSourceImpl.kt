package com.example.weatherapp.data.db

import androidx.lifecycle.LiveData
import com.example.weatherapp.data.model.CachedWeather
import com.example.weatherapp.data.model.FavoriteLocation
import com.example.weatherapp.data.model.WeatherAlert

class WeatherLocalDataSourceImpl(private val dao: WeatherDAO) : WeatherLocalDataSource {
    // ----- Favorites -----
    override suspend fun insertFavorite(location: FavoriteLocation) = dao.insertFavorite(location)

    override suspend fun deleteFavorite(location: FavoriteLocation) = dao.deleteFavorite(location)

    override fun getAllFavorites(): LiveData<List<FavoriteLocation>> = dao.getAllFavorites()

    // ----- Alerts -----
    override suspend fun insertAlert(alert: WeatherAlert) = dao.insertAlert(alert)

    override suspend fun deleteAlert(alert: WeatherAlert) = dao.deleteAlert(alert)

    override fun getAllAlerts(): LiveData<List<WeatherAlert>> = dao.getAllAlerts()

    // ----- Cached Weather -----
    override suspend fun insertCachedWeather(weather: CachedWeather) = dao.insertCachedWeather(weather)

    override suspend fun getCachedWeather(city: String): CachedWeather? = dao.getCachedWeather(city)

    override suspend fun deleteCachedWeather(city: String) = dao.deleteCachedWeather(city)
}