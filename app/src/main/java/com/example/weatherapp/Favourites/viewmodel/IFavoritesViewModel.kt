package com.example.weatherapp.Favourites.viewmodel

import com.example.weatherapp.data.model.FavoriteLocation
import kotlinx.coroutines.Job

interface IFavoritesViewModel {
    fun addFavorite(location: FavoriteLocation): Job
    fun removeFavorite(location: FavoriteLocation): Job
}