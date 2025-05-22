package com.example.weatherapp.Home.viewmodel

import androidx.lifecycle.LiveData
import com.example.weatherapp.data.model.WeatherResponse

interface MainViewModelInterface {
    val forecast: LiveData<WeatherResponse>
    val loading: LiveData<Boolean>
    val error: LiveData<String?>
    fun fetchForecast(lat: Double, lon: Double, units: String)
}