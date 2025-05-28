package com.example.weatherapp.data.model

import com.google.gson.annotations.SerializedName

data class ForecastItem(
    @SerializedName("dt") val timestamp: Long,
    @SerializedName("main") val main: MainData,
    @SerializedName("weather") val weather: List<Weather>,
    @SerializedName("wind") val wind: Wind,
    @SerializedName("dt_txt") val dateTime: String,
    val clouds: Clouds,
)