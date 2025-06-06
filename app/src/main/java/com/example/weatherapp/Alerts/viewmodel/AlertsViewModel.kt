package com.example.weatherapp.Alerts.viewmodel

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.Application
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.example.weatherapp.Alerts.reciever.AlertReceiver
import com.example.weatherapp.data.model.WeatherAlert
import com.example.weatherapp.data.repo.WeatherRepository
import kotlinx.coroutines.launch


class AlertsViewModel(
    private val repository: WeatherRepository,
    private val app: Application
) : AndroidViewModel(app), IAlertsViewModel {
    val alerts: LiveData<List<WeatherAlert>> = repository.getAllAlerts()
    // ✅ LiveData filtered to exclude expired alerts from UI
    val cleanedAlerts: LiveData<List<WeatherAlert>> = alerts.map { list ->
        list.filter { it.toTime >= System.currentTimeMillis() }
    }


    // ✅ Delete expired alerts from DB (called once on screen load)
    suspend fun cleanUpExpiredAlertsOnce() {
        val expired = repository.getAllAlertsOnce().filter { it.toTime < System.currentTimeMillis() }
        expired.forEach {
            repository.deleteAlert(it)
            cancelAlarm(it.id)
        }
    }

    override fun addAlert(alert: WeatherAlert) = viewModelScope.launch {
        repository.insertAlert(alert)
        scheduleAlarm(alert)
    }

    override fun deleteAlert(alert: WeatherAlert) = viewModelScope.launch {
        repository.deleteAlert(alert)
        cancelAlarm(alert.id)
    }

    @SuppressLint("ScheduleExactAlarm")
    override fun scheduleAlarm(alert: WeatherAlert) {
        val now = System.currentTimeMillis()
        if (alert.fromTime < now) return // ✅ Skip if already expired

        val context = app.applicationContext
        val intent = Intent(context, AlertReceiver::class.java).apply {
            putExtra("id", alert.id)
            putExtra("isAlarm", alert.isActive)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context, alert.id, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, alert.fromTime, pendingIntent)
    }

    override fun cancelAlarm(id: Int) {
        val context = app.applicationContext
        val intent = Intent(context, AlertReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context, id, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(pendingIntent)
    }
}
