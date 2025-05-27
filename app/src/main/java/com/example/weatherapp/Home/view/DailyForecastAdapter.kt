package com.example.weatherapp.Home.view

import android.content.Context
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
import com.example.weatherapp.Settings.model.Language
import com.example.weatherapp.Settings.model.SettingsRepository
import com.example.weatherapp.WeatherIconMapper
import com.example.weatherapp.data.model.ForecastItem
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DailyForecastAdapter(
    private val context: Context
) : ListAdapter<ForecastItem, DailyForecastAdapter.DailyViewHolder>(
    object : DiffUtil.ItemCallback<ForecastItem>() {
        override fun areItemsTheSame(old: ForecastItem, new: ForecastItem) =
            old.dateTime == new.dateTime

        override fun areContentsTheSame(old: ForecastItem, new: ForecastItem) =
            old == new
    }
) {
    inner class DailyViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        fun bind(item: ForecastItem) {
            // 🌐 Get language setting and create locale
            val lang = SettingsRepository(context).loadSettings().language
            val locale = if (lang == Language.ARABIC) Locale("ar") else Locale("en")

            // 🕐 Format day name with correct locale (e.g., الأحد instead of Sun)
            val dayName = SimpleDateFormat("EEEE", locale).format(Date(item.timestamp * 1000))
            view.findViewById<TextView>(R.id.tvDay).text = dayName

            // 🌡️ Format temperature range
            val tempMin = item.main.tempMin.toInt()
            val tempMax = item.main.tempMax.toInt()
            view.findViewById<TextView>(R.id.tvDailyTemp).text = "$tempMin° / $tempMax°"

            val desc = item.weather.firstOrNull()?.description ?: ""
            view.findViewById<TextView>(R.id.tvDailyDesc).text = desc.replaceFirstChar {
                if (it.isLowerCase()) it.titlecase(locale) else it.toString()
            }

            // 🌤️ Weather icon
            val iconCode = item.weather.firstOrNull()?.icon ?: "01d"
            val iconRes = WeatherIconMapper.getIconResource(iconCode)
            view.findViewById<ImageView>(R.id.imgDailyIcon).setImageResource(iconRes)
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
