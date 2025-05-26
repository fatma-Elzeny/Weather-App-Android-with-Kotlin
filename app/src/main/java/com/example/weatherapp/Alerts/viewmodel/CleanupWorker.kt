package com.example.weatherapp.Alerts.viewmodel

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.weatherapp.data.db.WeatherDatabase

class CleanupWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val currentTime = System.currentTimeMillis()
        WeatherDatabase.getInstance(applicationContext)
            .weatherDao()
            .deleteExpiredAlerts(currentTime)
        return Result.success()
    }
}