package com.example.weatherapp.data.network

object RetrofitClient {
    private const val BASE_URL = "https://api.openweathermap.org/"

    val forecastApi: ForecastApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ForecastApiService::class.java)
    }
}