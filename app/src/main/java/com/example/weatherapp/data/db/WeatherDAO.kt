package com.example.weatherapp.data.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
@Dao
class WeatherDAO {
    // Favorites
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(location: FavoriteLocation)


    @Delete
    suspend fun deleteFavorite(location: FavoriteLocation)

    @Query("SELECT * FROM favorites")
    fun getAllFavorites(): LiveData<List<FavoriteLocation>>

    // Alerts
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlert(alert: WeatherAlert)

    @Delete
    suspend fun deleteAlert(alert: WeatherAlert)

    @Query("SELECT * FROM alerts")
    fun getAllAlerts(): LiveData<List<WeatherAlert>>

}
}