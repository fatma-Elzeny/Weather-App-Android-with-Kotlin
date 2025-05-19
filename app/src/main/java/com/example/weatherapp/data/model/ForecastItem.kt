package com.example.weatherapp.data.model

data class ForecastItem (
    val dt: Long, // Timestamp
    val main: MainData,
    val weather: List<Weather>,
    val wind: Wind
)