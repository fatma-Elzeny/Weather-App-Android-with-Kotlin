package com.example.weatherapp.viewModelTest

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.weatherapp.data.model.City
import com.example.weatherapp.data.model.Clouds
import com.example.weatherapp.data.model.Coord
import com.example.weatherapp.data.model.FavoriteLocation
import com.example.weatherapp.data.model.ForecastItem
import com.example.weatherapp.data.model.MainData
import com.example.weatherapp.data.model.Weather
import com.example.weatherapp.data.model.WeatherAlert
import com.example.weatherapp.data.model.WeatherResponse
import com.example.weatherapp.data.model.Wind
import com.example.weatherapp.data.repo.WeatherRepository

class FakeWeatherRepository : WeatherRepository {
    val insertedAlerts = mutableListOf<WeatherAlert>()
    val deletedAlerts = mutableListOf<WeatherAlert>()
    var shouldFail = false
    var dummyForecast = WeatherResponse(
        city = City(1,"TestCity", Coord(0.0, 0.0), "TestCountry", 0, 0, 0, 0, null),
        list = listOf(
            ForecastItem(
                timestamp = 123456789,
                main = MainData(25.0, 23.0, 65, 1013, 20.0, 28.0),
                weather = listOf(Weather(1, "Clear", "clear sky", "01d")),
                wind = Wind(5.0, 180, 6.0),
                dateTime = "2025-05-28 12:00:00",
                Clouds(20)
            )
        )
    )

    override suspend fun getWeatherForecast(
        lat: Double, lon: Double, units: String, lang: String
    ): WeatherResponse {
        if (shouldFail) throw Exception("Network error")
        return dummyForecast
    }

    override suspend fun insertFavorite(location: FavoriteLocation) = Unit
    override suspend fun deleteFavorite(location: FavoriteLocation) = Unit
    override fun getAllFavorites(): LiveData<List<FavoriteLocation>> = MutableLiveData(emptyList())
    override fun getAllAlerts(): LiveData<List<WeatherAlert>> {
        return MutableLiveData(insertedAlerts)
    }

    override suspend fun getAllAlertsOnce(): List<WeatherAlert> {
        return insertedAlerts
    }

    override suspend fun insertAlert(alert: WeatherAlert) {
        insertedAlerts.add(alert)
    }

    override suspend fun deleteAlert(alert: WeatherAlert) {
        deletedAlerts.add(alert)
        insertedAlerts.remove(alert)
    }

    override suspend fun saveCachedWeather(response: WeatherResponse) = Unit
    override suspend fun getCachedWeatherByCoord(lat: Double, lon: Double): WeatherResponse? = null
    override suspend fun getCachedWeather(city: String): WeatherResponse? = null
}
