package com.example.weatherapp.data.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.weatherapp.data.model.CachedWeather
import com.example.weatherapp.data.model.FavoriteLocation
import com.example.weatherapp.data.model.WeatherAlert

@Dao
interface WeatherDAO {

    // ----- Favorites -----
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(location: FavoriteLocation)

    @Delete
    suspend fun deleteFavorite(location: FavoriteLocation)

    @Query("SELECT * FROM favorites")
    fun getAllFavorites(): LiveData<List<FavoriteLocation>>

    // ----- Alerts -----
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlert(alert: WeatherAlert)

    @Delete
    suspend fun deleteAlert(alert: WeatherAlert)

    @Query("SELECT * FROM alerts")
    fun getAllAlerts(): LiveData<List<WeatherAlert>>

    @Query("DELETE FROM alerts WHERE id = :id")
    suspend fun deleteAlertById(id: Int)
    @Query("DELETE FROM alerts WHERE toTime < :currentTime")
    suspend fun deleteExpiredAlerts(currentTime: Long)

    @Query("SELECT * FROM alerts")
    suspend fun getAllAlertsOnce(): List<WeatherAlert>


    // ----- Cached Weather -----
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCachedWeather(weather: CachedWeather)

    @Query("SELECT * FROM cached_weather WHERE cityName = :city")
    suspend fun getCachedWeather(city: String): CachedWeather?

    @Query("DELETE FROM cached_weather WHERE cityName = :city")
    suspend fun deleteCachedWeather(city: String)

    @Query("SELECT * FROM cached_weather WHERE lat = :lat AND lon = :lon")
    suspend fun getCachedWeatherByCoord(lat: Double, lon: Double): CachedWeather?

}
