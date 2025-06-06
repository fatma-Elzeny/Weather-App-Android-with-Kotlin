package com.example.weatherapp.data.model


data class City(
    val id: Int,
    val name: String,
    val coord: Coord,
    val country: String,
    val population: Int,
    val timezone: Int,
    val sunrise: Long,
    val sunset: Long,
    val local_names: Map<String, String>?
)