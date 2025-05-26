package com.example.weatherapp.Alerts.viewmodel

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

// DismissReceiver.kt
class DismissReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationId = intent.getIntExtra("id", 0)
        notificationManager.cancel(notificationId)
    }
}
