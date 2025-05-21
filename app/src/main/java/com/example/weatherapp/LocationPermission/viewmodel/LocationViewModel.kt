package com.example.weatherapp.LocationPermission.viewmodel

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.location.LocationServices

class LocationViewModel(private val context: Context) : ViewModel() {

    private val _locationResult = MutableLiveData<Location?>()
    val locationResult: LiveData<Location?> get() = _locationResult

    private val fusedClient = LocationServices.getFusedLocationProviderClient(context)

    @SuppressLint("MissingPermission")
    fun fetchLocation() {
        fusedClient.lastLocation
            .addOnSuccessListener { location ->
                _locationResult.value = location
            }
            .addOnFailureListener {
                _locationResult.value = null
            }
    }
}
