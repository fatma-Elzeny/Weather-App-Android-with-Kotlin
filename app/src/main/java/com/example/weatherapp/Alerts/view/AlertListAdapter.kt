package com.example.weatherapp.Alerts.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.weatherapp.R
import com.example.weatherapp.data.model.WeatherAlert
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// AlertListAdapter.kt
class AlertListAdapter(
    private val onDeleteClick: (WeatherAlert) -> Unit
) : ListAdapter<WeatherAlert, AlertListAdapter.AlertViewHolder>(
    object : DiffUtil.ItemCallback<WeatherAlert>() {
        override fun areItemsTheSame(oldItem: WeatherAlert, newItem: WeatherAlert) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: WeatherAlert, newItem: WeatherAlert) =
            oldItem == newItem
    }
) {
    inner class AlertViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val timeRange: TextView = itemView.findViewById(R.id.tvTimeRange)
        private val type: TextView = itemView.findViewById(R.id.tvType)
        private val btnOptions: ImageButton = itemView.findViewById(R.id.btnOptions)

        fun bind(alert: WeatherAlert) {
            val formatter = SimpleDateFormat("hh:mm a", Locale.getDefault())
            val start = formatter.format(Date(alert.fromTime))
            val end = formatter.format(Date(alert.toTime))
            timeRange.text = "$start - $end"
            type.text = if (alert.isActive as Boolean) "Alarm" else "Notification"

            btnOptions.setOnClickListener { onDeleteClick(alert) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlertViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_alert, parent, false)
        return AlertViewHolder(view)
    }

    override fun onBindViewHolder(holder: AlertViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}
