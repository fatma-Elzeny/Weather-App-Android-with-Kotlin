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
import com.example.weatherapp.data.model.ForecastItem
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HourlyForecastAdapter : ListAdapter<ForecastItem, HourlyForecastAdapter.HourlyViewHolder>(
    object : DiffUtil.ItemCallback<ForecastItem>() {
        override fun areItemsTheSame(old: ForecastItem, new: ForecastItem) =
            old.dateTime == new.dateTime

        override fun areContentsTheSame(old: ForecastItem, new: ForecastItem) =
            old == new
    }
) {
    class HourlyViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        fun bind(item: ForecastItem) {
            val time = SimpleDateFormat("HH:mm", Locale.getDefault())
                .format(Date(item.timestamp * 1000))
            view.findViewById<TextView>(R.id.tvHour).text = time
            view.findViewById<TextView>(R.id.tvHourlyTemp).text = "${item.main.temp.toInt()}°"

            val iconUrl = "https://openweathermap.org/img/wn/${item.weather[0].icon}@2x.png"
            Glide.with(view).load(iconUrl).into(view.findViewById(R.id.imgHourlyIcon))
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HourlyViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_hourly_forecast, parent, false)
        return HourlyViewHolder(view)
    }

    override fun onBindViewHolder(holder: HourlyViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}