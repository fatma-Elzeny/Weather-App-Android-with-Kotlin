package com.example.weatherapp.dataSource

import com.example.weatherapp.data.model.WeatherResponse
import com.example.weatherapp.data.network.WeatherRemoteDataSource

class FakeRemoteDataSource(private val forecast: WeatherResponse) : WeatherRemoteDataSource {
    override suspend fun getWeatherForecast(
        lat: Double,
        lon: Double,
        units: String,
        lang: String,
        apiKey: String
    ): WeatherResponse = forecast
}
