package com.example.weatherapp.data.model

import com.google.gson.annotations.SerializedName

data class ForecastItem (
    val dt: Long,
    val main: MainData,
    val weather: List<Weather>,
    val clouds: Clouds,
    val wind: Wind,
    val visibility: Int,
    val pop: Double,
    val sys: Sys,
    @SerializedName("dt_txt") val dateTime: String
)