package com.example.weatherapp.data.repo

import androidx.lifecycle.LiveData
import com.example.weatherapp.BuildConfig
import com.example.weatherapp.data.db.WeatherLocalDataSource
import com.example.weatherapp.data.model.CachedWeather
import com.example.weatherapp.data.model.FavoriteLocation
import com.example.weatherapp.data.model.WeatherAlert
import com.example.weatherapp.data.model.WeatherResponse
import com.example.weatherapp.data.network.WeatherRemoteDataSource
import com.google.gson.Gson

class WeatherRepositoryImpl(
    private val remote: WeatherRemoteDataSource,
    private val local: WeatherLocalDataSource
) : WeatherRepository {

    private val gson = Gson()

    override suspend fun getWeatherForecast(
        lat: Double,
        lon: Double,
        units: String
    ): WeatherResponse {
        val response = remote.getWeatherForecast(
            lat, lon, units, "0227d528304276aa7b3f837f13e1fd21"
        )
        saveCachedWeather(response)
        return response
    }

    // ---------------- Favorites ----------------
    override suspend fun insertFavorite(location: FavoriteLocation) =
        local.insertFavorite(location)

    override suspend fun deleteFavorite(location: FavoriteLocation) =
        local.deleteFavorite(location)

    override fun getAllFavorites(): LiveData<List<FavoriteLocation>> =
        local.getAllFavorites()

    // ---------------- Alerts ----------------
    override suspend fun insertAlert(alert: WeatherAlert) =
        local.insertAlert(alert)

    override suspend fun deleteAlert(alert: WeatherAlert) =
        local.deleteAlert(alert)

    override fun getAllAlerts(): LiveData<List<WeatherAlert>> =
        local.getAllAlerts()

    // ---------------- Cached Weather ----------------
    override suspend fun saveCachedWeather(response: WeatherResponse) {
        val city = response.city.name
        val json = gson.toJson(response)
        val cachedWeather = CachedWeather(
            cityName = city,
            lat = response.city.coord.lat,
            lon = response.city.coord.lon,
            data = json,
            lastUpdated = System.currentTimeMillis()
        )
        local.insertCachedWeather(cachedWeather)
    }

    override suspend fun getCachedWeather(city: String): WeatherResponse? {
        val cached = local.getCachedWeather(city)
        return cached?.let {
            gson.fromJson(it.data, WeatherResponse::class.java)
        }
    }
    suspend fun getCachedUpdateTime(city: String): Long? {
        return local.getCachedWeather(city)?.lastUpdated
    }
}
