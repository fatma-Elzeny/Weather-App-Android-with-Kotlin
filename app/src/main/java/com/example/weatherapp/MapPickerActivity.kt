package com.example.weatherapp

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
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

class MapPickerActivity : AppCompatActivity() {

    private lateinit var mapView: MapView
    private var selectedGeoPoint: GeoPoint? = null
    private lateinit var marker: Marker

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map_picker)

        // Load OSM configuration
        Configuration.getInstance().load(applicationContext, PreferenceManager.getDefaultSharedPreferences(applicationContext))
        Configuration.getInstance().userAgentValue = packageName

        mapView = findViewById(R.id.mapview)
        mapView.setTileSource(TileSourceFactory.MAPNIK)
        mapView.setMultiTouchControls(true)

        val mapController = mapView.controller
        mapController.setZoom(6.0)
        mapController.setCenter(GeoPoint(30.0444, 31.2357)) // Default to Cairo

        // Initialize marker
        marker = Marker(mapView).apply {
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        }

        // Map click listener to select location and place marker
        mapView.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                val projection = mapView.projection
                val geoPoint = projection.fromPixels(event.x.toInt(), event.y.toInt()) as GeoPoint
                selectedGeoPoint = geoPoint

                // Update marker
                marker.position = geoPoint
                mapView.overlays.clear()
                mapView.overlays.add(marker)
                mapView.invalidate()

                Toast.makeText(this, "Selected: ${geoPoint.latitude}, ${geoPoint.longitude}", Toast.LENGTH_SHORT).show()
            }
            false
        }

        // Button to confirm location selection
        findViewById<Button>(R.id.btnSelectLocation).setOnClickListener {
            selectedGeoPoint?.let {
                val resultIntent = Intent().apply {
                    putExtra("lat", it.latitude)
                    putExtra("lon", it.longitude)
                }
                setResult(RESULT_OK, resultIntent)
                finish()
            } ?: Toast.makeText(this, "Please tap to select a location", Toast.LENGTH_SHORT).show()
        }
    }
}

