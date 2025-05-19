package com.example.weatherapp.data.network

class WeatherRemoteDataSourceImpl(private val apiService: WeatherApiService) : WeatherRemoteDataSource {
    override suspend fun getWeatherForecast(
        lat: Double,
        lon: Double,
        units: String,
        apiKey: String
    ): WeatherResponse = apiService.getForecast(lat, lon, units, apiKey)
}