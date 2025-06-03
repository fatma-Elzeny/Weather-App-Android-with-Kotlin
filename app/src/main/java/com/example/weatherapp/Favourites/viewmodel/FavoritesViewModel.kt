package com.example.weatherapp.Favourites.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.weatherapp.data.db.WeatherDAO
import com.example.weatherapp.data.db.WeatherDatabase
import com.example.weatherapp.data.model.FavoriteLocation
import kotlinx.coroutines.launch

class FavoritesViewModel(application: Application,private val dao: WeatherDAO = WeatherDatabase.getInstance(application).weatherDao()) : AndroidViewModel(application),
    IFavoritesViewModel {

    val favorites: LiveData<List<FavoriteLocation>> = dao.getAllFavorites()

    override fun addFavorite(location: FavoriteLocation) = viewModelScope.launch {
        dao.insertFavorite(location)
    }

    override fun removeFavorite(location: FavoriteLocation) = viewModelScope.launch {
        dao.deleteFavorite(location)
    }
}
