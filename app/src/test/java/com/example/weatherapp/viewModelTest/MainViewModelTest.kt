package com.example.weatherapp.viewModelTest

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.weatherapp.Home.viewmodel.MainViewModel
import com.example.weatherapp.getOrAwaitValue
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class MainViewModelTest {

    private lateinit var viewModel: MainViewModel
    private lateinit var fakeRepo: FakeWeatherRepository

    @get:Rule
    val rule = InstantTaskExecutorRule() // For LiveData

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fakeRepo = FakeWeatherRepository()
        viewModel = MainViewModel(fakeRepo)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun fetchForecast_emitsforecastandclearserror() = runTest {
        viewModel.fetchForecast(30.0, 31.0, "metric", "en")
        advanceUntilIdle() // Let coroutine finish

        val forecast = viewModel.forecast.getOrAwaitValue()
        assertEquals("TestCity", forecast.city.name)
        assertNull(viewModel.error.value)
        assertFalse(viewModel.loading.getOrAwaitValue())
    }

    @Test
    fun fetchForecast_witherrorsetserrormessage() = runTest {
        fakeRepo.shouldFail = true

        viewModel.fetchForecast(30.0, 31.0, "metric", "en")
        advanceUntilIdle()

        assertNotNull(viewModel.error.value)
        assertTrue(viewModel.error.value!!.contains("Network error"))
        assertFalse(viewModel.loading.getOrAwaitValue())
    }
}
