package com.example.weatherapp.viewModelTest

import android.app.Application
import com.example.weatherapp.Favourites.viewmodel.FavoritesViewModel
import com.example.weatherapp.data.db.WeatherDAO
import com.example.weatherapp.data.model.FavoriteLocation
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FavoritesViewModelTest {

    private lateinit var viewModel: FavoritesViewModel
    private lateinit var dao: WeatherDAO
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        dao = mockk(relaxed = true)
        val app = mockk<Application>(relaxed = true)
        viewModel = FavoritesViewModel(app, dao)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun addFavorite_shouldcalldaoinsertFavorite() = runTest {
        val location = FavoriteLocation(name = "Alexandria", lat = 31.2, lon = 29.9)

        viewModel.addFavorite(location)
        advanceUntilIdle()

        coVerify { dao.insertFavorite(location) }
    }

    @Test
    fun removeFavorite_shouldcalldaodeleteFavorite() = runTest {
        val location = FavoriteLocation(name = "Giza", lat = 30.0, lon = 31.2)

        viewModel.removeFavorite(location)
        advanceUntilIdle()

        coVerify { dao.deleteFavorite(location) }
    }
}
