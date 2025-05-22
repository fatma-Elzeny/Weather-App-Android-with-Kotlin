package com.example.weatherapp.data.model

import com.google.gson.annotations.SerializedName

data class MainData(
    val temp: Double,
    val humidity: Int,
    val pressure: Int,
    @SerializedName("temp_min") val tempMin: Double,
    @SerializedName("temp_max") val tempMax: Double
)