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

class DailyForecastAdapter : ListAdapter<ForecastItem, DailyForecastAdapter.DailyViewHolder>(
    object : DiffUtil.ItemCallback<ForecastItem>() {
        override fun areItemsTheSame(old: ForecastItem, new: ForecastItem) =
            old.dateTime == new.dateTime

        override fun areContentsTheSame(old: ForecastItem, new: ForecastItem) =
            old == new
    }
) {
    class DailyViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        fun bind(item: ForecastItem) {
            val date = SimpleDateFormat("EEE", Locale.getDefault())
                .format(Date(item.timestamp * 1000))
            view.findViewById<TextView>(R.id.tvDay).text = date
            view.findViewById<TextView>(R.id.tvDailyTemp).text =
                "${item.main.tempMin.toInt()}° / ${item.main.tempMax.toInt()}°"

            val iconUrl = "https://openweathermap.org/img/wn/${item.weather[0].icon}@2x.png"
            Glide.with(view).load(iconUrl).into(view.findViewById(R.id.imgDailyIcon))
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DailyViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_daily_forcast, parent, false)
        return DailyViewHolder(view)
    }

    override fun onBindViewHolder(holder: DailyViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}
