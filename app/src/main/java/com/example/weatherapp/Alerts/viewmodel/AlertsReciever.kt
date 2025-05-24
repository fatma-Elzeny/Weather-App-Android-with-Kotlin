package com.example.weatherapp.Alerts.viewmodel

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.weatherapp.R

class AlertReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val isAlarm = intent.getBooleanExtra("isAlarm", false)
        val id = intent.getIntExtra("id", 0)

        val channelId = "alert_channel"
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId, "Weather Alerts", NotificationManager.IMPORTANCE_HIGH
            )
            manager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setContentTitle("Weather Alert")
            .setContentText("Weather is fine. This is your alert.")
            .setSmallIcon(R.drawable.ic_alert)
            .setAutoCancel(true)
            .apply {
                if (isAlarm) setDefaults(Notification.DEFAULT_ALL)
            }
            .build()

        manager.notify(id, notification)
    }
}
