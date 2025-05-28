package com.example.weatherapp.Alerts.reciever

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.net.Uri
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
            val soundUri = Uri.parse("android.resource://${context.packageName}/${R.raw.weather_alarm}")

            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .build()

            val channel = NotificationChannel(
                channelId, "Weather Alerts", NotificationManager.IMPORTANCE_HIGH
            ).apply {

                setSound(soundUri, audioAttributes) // ✅ set custom sound
                enableVibration(true)
                enableLights(true)
            }
            manager.createNotificationChannel(channel)
        }
        val dismissIntent = Intent(context, DismissReceiver::class.java).apply {
            putExtra("id", id)
        }
        val dismissPendingIntent = PendingIntent.getBroadcast(
            context, id, dismissIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val soundUri = Uri.parse("android.resource://${context.packageName}/{R.raw.weather_alarm}")

        val notification = NotificationCompat.Builder(context, channelId)
            .setContentTitle("Weather Alert")
            .setContentText("Weather is fine. This is your alert.")
            .setSmallIcon(R.drawable.ic_alert)
            .setAutoCancel(true)
            .setSound(soundUri)
            .addAction(R.drawable.ic_dismiss, "Dismiss", dismissPendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH) // Ensures heads-up notification
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .apply {
                if (isAlarm) setDefaults(Notification.DEFAULT_ALL)
            }
            .build()

        manager.notify(id, notification)

      /*  // ✅ DELETE alert by ID after trigger (New logic)
        CoroutineScope(Dispatchers.IO).launch {
            WeatherDatabase.getInstance(context).weatherDao().deleteAlertById(id)
        }*/
    }
}
