package com.example.weatherapp

import android.app.Application
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.weatherapp.Alerts.viewmodel.CleanupWorker
import java.util.concurrent.TimeUnit

class WeatherApp : Application() {
    override fun onCreate() {
        super.onCreate()
        scheduleCleanupWork()
    }

    private fun scheduleCleanupWork() {
        val cleanupRequest = PeriodicWorkRequestBuilder<CleanupWorker>(
            1, // Repeat every 1 day
            TimeUnit.DAYS
        ).build()
        WorkManager.getInstance(this).enqueue(cleanupRequest)
    }
}