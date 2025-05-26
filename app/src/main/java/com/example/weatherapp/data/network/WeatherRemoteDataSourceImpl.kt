package com.example.weatherapp.data.network

import com.example.weatherapp.data.model.WeatherResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class WeatherRemoteDataSourceImpl(private val apiService: WeatherApiService) : WeatherRemoteDataSource {
    override suspend fun getWeatherForecast(
        lat: Double,
        lon: Double,
        units: String,
        apiKey: String
    ): WeatherResponse {
        return withContext(Dispatchers.IO) {
            apiService.getForecast(lat, lon, units, apiKey)
        }

    }
}