package com.example.weatherapp.Home.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.weatherapp.R
import com.example.weatherapp.WeatherIconMapper
import com.example.weatherapp.data.model.ForecastItem
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class HourlyForecastAdapter(private val timezoneOffsetSeconds: Int) :
    ListAdapter<ForecastItem, HourlyForecastAdapter.HourlyViewHolder>(
        object : DiffUtil.ItemCallback<ForecastItem>() {
            override fun areItemsTheSame(old: ForecastItem, new: ForecastItem) =
                old.dateTime == new.dateTime
            override fun areContentsTheSame(old: ForecastItem, new: ForecastItem) = old == new
        }
    ) {
    class HourlyViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        fun bind(item: ForecastItem, timezoneOffsetSeconds: Int) {
            val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault()).apply {
                timeZone = TimeZone.getTimeZone("UTC").apply {
                    // Apply the city's timezone offset
                    setRawOffset((timezoneOffsetSeconds * 1000).toInt())
                }
            }
            val time = timeFormat.format(Date(item.timestamp * 1000))
            view.findViewById<TextView>(R.id.tvHour).text = time
            view.findViewById<TextView>(R.id.tvHourlyTemp).text =
                "${item.main.temp.toInt()}°"
            val iconCode = item.weather.firstOrNull()?.icon ?: "01d"
            val iconRes = WeatherIconMapper.getIconResource(iconCode)
            view.findViewById<ImageView>(R.id.imgHourlyIcon).setImageResource(iconRes)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HourlyViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_hourly_forecast, parent, false)
        return HourlyViewHolder(view)
    }

    override fun onBindViewHolder(holder: HourlyViewHolder, position: Int) {
        holder.bind(getItem(position), timezoneOffsetSeconds)
    }
}