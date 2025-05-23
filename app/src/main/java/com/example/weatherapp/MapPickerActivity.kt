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

class MapPickerActivity : AppCompatActivity() {

    private lateinit var mapView: org.osmdroid.views.MapView
    private var selectedGeoPoint: GeoPoint? = null

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map_picker)
        Configuration.getInstance().load(applicationContext, PreferenceManager.getDefaultSharedPreferences(applicationContext))
        Configuration.getInstance().userAgentValue = packageName
        mapView = findViewById(R.id.mapview)
        mapView.setTileSource(TileSourceFactory.MAPNIK)
        mapView.setMultiTouchControls(true)

        // Center map on default location (e.g., world center)
        val mapController = mapView.controller
        mapController.setZoom(5.0)
        mapController.setCenter(GeoPoint(20.0, 0.0))

        // Listener to get location on map click
        mapView.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                val projection = mapView.projection
                val geoPoint = projection.fromPixels(event.x.toInt(), event.y.toInt()) as GeoPoint
                selectedGeoPoint = geoPoint
                Toast.makeText(this, "Selected: ${geoPoint.latitude}, ${geoPoint.longitude}", Toast.LENGTH_SHORT).show()
                // Optionally place a marker or update UI
                mapView.invalidate()
            }
            false
        }

        findViewById<Button>(R.id.btnSelectLocation).setOnClickListener {
            selectedGeoPoint?.let {
                val resultIntent = Intent()
                resultIntent.putExtra("lat", it.latitude)
                resultIntent.putExtra("lon", it.longitude)
                setResult(RESULT_OK, resultIntent)
                finish()
            } ?: Toast.makeText(this, "Please tap to select a location", Toast.LENGTH_SHORT).show()
        }
    }
}
