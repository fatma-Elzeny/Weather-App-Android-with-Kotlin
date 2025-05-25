package com.example.weatherapp.data.network

import com.example.weatherapp.data.model.WeatherResponse

interface WeatherRemoteDataSource {
    suspend fun getWeatherForecast(
        lat: Double,
        lon: Double,
        units: String,
        apiKey: String
    ): WeatherResponse


}