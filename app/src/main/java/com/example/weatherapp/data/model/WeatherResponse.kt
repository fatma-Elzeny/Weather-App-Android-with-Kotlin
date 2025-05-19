package com.example.weatherapp.data.model

data class WeatherResponse (
    val list: List<ForecastItem>,
    val city: City
)