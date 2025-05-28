package com.example.weatherapp.dataSource

import com.example.weatherapp.Settings.model.Language
import com.example.weatherapp.Settings.model.LocationMode
import com.example.weatherapp.Settings.model.SettingsData
import com.example.weatherapp.Settings.model.SettingsRepository
import com.example.weatherapp.Settings.model.TemperatureUnit
import com.example.weatherapp.Settings.model.WindSpeedUnit
import com.example.weatherapp.data.model.City
import com.example.weatherapp.data.model.Clouds
import com.example.weatherapp.data.model.Coord
import com.example.weatherapp.data.model.FavoriteLocation
import com.example.weatherapp.data.model.ForecastItem
import com.example.weatherapp.data.model.MainData
import com.example.weatherapp.data.model.Weather
import com.example.weatherapp.data.model.WeatherResponse
import com.example.weatherapp.data.model.Wind
import com.example.weatherapp.data.repo.WeatherRepositoryImpl
import io.mockk.every
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class WeatherRepositoryImplTest {

    private lateinit var repository: WeatherRepositoryImpl
    private lateinit var fakeLocal: FakeLocalDataSource
    private lateinit var fakeRemote: FakeRemoteDataSource
    private lateinit var fakeSettings: SettingsRepository

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        // Dummy forecast to be returned by fake remote
        val dummyForecast = WeatherResponse(
            city = City(
                id = 1,
                name = "Cairo",
                coord = Coord(30.0, 31.0),
                country = "EG",
                population = 0,
                timezone = 0,
                sunrise = 1680000000,
                sunset = 1680050000,
                local_names = mapOf("ar" to "القاهرة", "en" to "Cairo")
            ),
            list = listOf(
                ForecastItem(
                    timestamp = 1680086400,
                    main = MainData(22.0, 24.0, 60, 1012, 20.0, 26.0),
                    weather = listOf(Weather(800, "Clear", "clear sky", "01d")),
                    wind = Wind(3.5, 100, 0.0),
                    dateTime = "2025-05-28 15:00:00",
                    Clouds(20)
                )
            )
        )

        fakeLocal = FakeLocalDataSource()
        fakeRemote = FakeRemoteDataSource(dummyForecast)
        fakeSettings = mockk(relaxed = true)

        every { fakeSettings.loadSettings() } returns SettingsData(
            locationMode = LocationMode.GPS,
            temperatureUnit = TemperatureUnit.CELSIUS,
            windSpeedUnit = WindSpeedUnit.KM_H,
            language = Language.ENGLISH,
            notificationsEnabled = false
        )

        repository = WeatherRepositoryImpl(
            remote = fakeRemote,
            local = fakeLocal,
            settingsRepo = fakeSettings
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ✅ TEST 1: insertFavorite
    @Test
    fun insertFavorite_addsLocationToFavoritesList() = runTest {
        val fav = FavoriteLocation(1,"Giza", 29.98, 31.14)

        repository.insertFavorite(fav)

        assertTrue(fakeLocal.favorites.contains(fav))
    }

    // ✅ TEST 2: deleteFavorite
    @Test
    fun deleteFavorite_removesLocationFromFavoritesList() = runTest {
        val fav = FavoriteLocation(1,"Giza", 29.98, 31.14)
        fakeLocal.favorites.add(fav)

        repository.deleteFavorite(fav)

        assertFalse(fakeLocal.favorites.contains(fav))
    }

    // ✅ TEST 3: getWeatherForecast returns remote and caches it
    @Test
    fun getWeatherForecast_returnsRemoteDataAndCaches() = runTest {
        val result = repository.getWeatherForecast(30.0, 31.0, "metric", "en")

        assertEquals("Cairo", result.city.name)
        assertEquals(24.0, result.list.first().main.temp, 0.01)

        // ✅ Assert it cached
        assertNotNull(fakeLocal.cachedWeather)
        assertEquals("Cairo", fakeLocal.cachedWeather?.cityName)
    }
}
