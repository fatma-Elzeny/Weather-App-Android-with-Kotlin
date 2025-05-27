package com.example.weatherapp.data.model

import com.google.gson.annotations.SerializedName

data class MainData(
    @SerializedName("feels_like") val feelsLike: Double,
    val temp: Double,
    val humidity: Int,
    val pressure: Int,
    @SerializedName("temp_min") val tempMin: Double,
    @SerializedName("temp_max") val tempMax: Double
)