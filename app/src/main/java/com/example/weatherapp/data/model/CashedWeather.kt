package com.example.weatherapp.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cached_weather")
data class CachedWeather(
    @PrimaryKey val cityName: String,
    val lat: Double,
    val lon: Double,
    val data: String,
    val lastUpdated: Long
)
