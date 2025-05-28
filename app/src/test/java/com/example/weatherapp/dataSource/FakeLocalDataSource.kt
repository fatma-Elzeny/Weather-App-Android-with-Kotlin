package com.example.weatherapp.dataSource

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.weatherapp.data.db.WeatherLocalDataSource
import com.example.weatherapp.data.model.CachedWeather
import com.example.weatherapp.data.model.FavoriteLocation
import com.example.weatherapp.data.model.WeatherAlert

class FakeLocalDataSource : WeatherLocalDataSource {

    val favorites = mutableListOf<FavoriteLocation>()
    var cachedWeather: CachedWeather? = null

    override suspend fun insertFavorite(location: FavoriteLocation) {
        favorites.add(location)
    }

    override suspend fun deleteFavorite(location: FavoriteLocation) {
        favorites.remove(location)
    }

    override fun getAllFavorites(): LiveData<List<FavoriteLocation>> {
        return MutableLiveData(favorites)
    }

    override suspend fun insertAlert(alert: WeatherAlert) {}
    override suspend fun deleteAlert(alert: WeatherAlert) {}
    override fun getAllAlerts(): LiveData<List<WeatherAlert>> = MutableLiveData()
    override suspend fun getAllAlertsOnce(): List<WeatherAlert> = emptyList()

    override suspend fun insertCachedWeather(weather: CachedWeather) {
        cachedWeather = weather
    }

    override suspend fun getCachedWeather(city: String): CachedWeather? = cachedWeather
    override suspend fun deleteCachedWeather(city: String) {
        TODO("Not yet implemented")
    }

    override suspend fun getCachedWeatherByCoord(lat: Double, lon: Double): CachedWeather? = cachedWeather
}
