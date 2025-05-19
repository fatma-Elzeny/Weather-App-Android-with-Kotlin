package com.example.weatherapp.data.network

interface WeatherRemoteDataSource {
    suspend fun getWeatherForecast(
        lat: Double,
        lon: Double,
        units: String,
        apiKey: String
    ): WeatherResponse
}