package com.example.weatherapp.data.network


import com.example.weatherapp.BuildConfig
import com.example.weatherapp.data.model.WeatherResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApiService {
    @GET("data/2.5/forecast")
    suspend fun getForecast(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("units") units: String,
        @Query("lang") lang: String,
        @Query("appid") apiKey: String ="0227d528304276aa7b3f837f13e1fd21"
    ): WeatherResponse

}
