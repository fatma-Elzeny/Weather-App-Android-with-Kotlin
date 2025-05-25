package com.example.weatherapp.Home.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weatherapp.data.model.WeatherResponse
import com.example.weatherapp.data.repo.WeatherRepository
import kotlinx.coroutines.launch

class MainViewModel(private val repository: WeatherRepository) : ViewModel(), MainViewModelInterface {

    private val _forecast = MutableLiveData<WeatherResponse>()
    override val forecast: LiveData<WeatherResponse> = _forecast

    private val _currentWeather = MutableLiveData<WeatherResponse>()
    val currentWeather: LiveData<WeatherResponse> = _currentWeather

    private val _loading = MutableLiveData<Boolean>()
    override val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String?>()
    override val error: LiveData<String?> = _error

    override fun fetchForecast(lat: Double, lon: Double, units: String) {
        viewModelScope.launch {
            _loading.value = true
            try {
                val response = repository.getWeatherForecast(lat, lon, units)
                _forecast.value = response
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Error: ${e.message}"
            } finally {
                _loading.value = false
            }
        }
    }

    }
