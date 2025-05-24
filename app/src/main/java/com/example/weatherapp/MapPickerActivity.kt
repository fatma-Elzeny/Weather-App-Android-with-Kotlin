package com.example.weatherapp

import android.annotation.SuppressLint
import android.content.Intent
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.view.MotionEvent
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import java.util.Locale

class MapPickerActivity : AppCompatActivity() {

    private lateinit var mapView: MapView
    private var selectedGeoPoint: GeoPoint? = null
    private lateinit var marker: Marker

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map_picker)

        // Configure OSMDroid
        Configuration.getInstance().load(
            applicationContext,
            PreferenceManager.getDefaultSharedPreferences(applicationContext)
        )
        Configuration.getInstance().userAgentValue = packageName

        initializeMap()
        setupConfirmButton()
    }

    private fun initializeMap() {
        mapView = findViewById(R.id.mapview)
        mapView.setTileSource(TileSourceFactory.MAPNIK)
        mapView.setMultiTouchControls(true)

        // Set initial position to Cairo
        val mapController = mapView.controller
        mapController.setZoom(12.0)
        mapController.setCenter(GeoPoint(30.0444, 31.2357))

        setupMarker()
        setupMapClickListener()
    }

    private fun setupMarker() {
        marker = Marker(mapView).apply {
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            setTitle("Selected Location")
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupMapClickListener() {
        mapView.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                handleMapTap(event)
                true
            } else {
                false
            }
        }
    }

    private fun handleMapTap(event: MotionEvent) {
        val projection = mapView.projection
        selectedGeoPoint = projection.fromPixels(event.x.toInt(), event.y.toInt()) as GeoPoint

        // Update marker position
        marker.position = selectedGeoPoint
        mapView.overlays.clear()
        mapView.overlays.add(marker)
        mapView.invalidate()

        Log.d("MapPicker", "Selected: ${selectedGeoPoint?.latitude}, ${selectedGeoPoint?.longitude}")
    }

    private fun setupConfirmButton() {
        findViewById<Button>(R.id.btnSelectLocation).setOnClickListener {
            selectedGeoPoint?.let { geoPoint ->
                getLocationName(geoPoint) { name ->
                    returnResultWithLocation(geoPoint, name)
                }
            } ?: showLocationNotSelectedError()
        }
    }

    private fun getLocationName(geoPoint: GeoPoint, callback: (String) -> Unit) {
        try {
            val geocoder = Geocoder(this, Locale.getDefault())
            val addresses: List<Address>? = geocoder.getFromLocation(
                geoPoint.latitude,
                geoPoint.longitude,
                1
            )

            val name = addresses?.firstOrNull()?.let { addr ->
                when {
                    !addr.locality.isNullOrEmpty() -> addr.locality!!
                    !addr.adminArea.isNullOrEmpty() -> addr.adminArea!!
                    else -> generateFallbackName(geoPoint)
                }
            } ?: generateFallbackName(geoPoint)

            callback(name)
        } catch (e: Exception) {
            Log.e("MapPicker", "Geocoding error: ${e.message}")
            callback(generateFallbackName(geoPoint))
        }
    }

    private fun generateFallbackName(geoPoint: GeoPoint): String {
        return "Location ${geoPoint.latitude.format(2)}, ${geoPoint.longitude.format(2)}"
    }

    private fun returnResultWithLocation(geoPoint: GeoPoint, name: String) {
        Intent().apply {
            putExtra("lat", geoPoint.latitude)
            putExtra("lon", geoPoint.longitude)
            putExtra("name", name)
        }.let {
            setResult(RESULT_OK, it)
            finish()
        }
    }

    private fun showLocationNotSelectedError() {
        Toast.makeText(this, "Please tap the map to select a location", Toast.LENGTH_SHORT).show()
    }

    private fun Double.format(digits: Int) = "%.${digits}f".format(this)

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }
}