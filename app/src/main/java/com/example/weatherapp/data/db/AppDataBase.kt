package com.example.weatherapp.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.weatherapp.data.model.CachedWeather
import com.example.weatherapp.data.model.FavoriteLocation
import com.example.weatherapp.data.model.WeatherAlert

@Database(
    entities = [FavoriteLocation::class, WeatherAlert::class, CachedWeather::class],
    version = 1,
    exportSchema = false
)
abstract class WeatherDatabase : RoomDatabase() {
    abstract fun weatherDao(): WeatherDAO

    companion object {
        @Volatile private var INSTANCE: WeatherDatabase? = null

        fun getInstance(context: Context): WeatherDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    WeatherDatabase::class.java,
                    "weather_db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
