package com.example.weatherapp.Alerts.viewmodel

import com.example.weatherapp.data.model.WeatherAlert
import kotlinx.coroutines.Job

interface IAlertsViewModel {
    fun addAlert(alert: WeatherAlert): Job
    fun deleteAlert(alert: WeatherAlert): Job
    fun scheduleAlarm(alert: WeatherAlert)
    fun cancelAlarm(id: Int)
}