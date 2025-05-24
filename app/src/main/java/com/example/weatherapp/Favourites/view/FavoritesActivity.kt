package com.example.weatherapp.Favourites.view

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.weatherapp.AlertsActivity
import com.example.weatherapp.Favourites.viewmodel.FavoritesViewModel
import com.example.weatherapp.Home.view.MainActivity
import com.example.weatherapp.MapPickerActivity
import com.example.weatherapp.R
import com.example.weatherapp.Settings.view.SettingsActivity
import com.example.weatherapp.data.model.FavoriteLocation
import com.example.weatherapp.databinding.ActivityFavoritesBinding

class FavoritesActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFavoritesBinding
    private val viewModel: FavoritesViewModel by viewModels()
    private lateinit var adapter: FavoritesAdapter

    private val mapPickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val data = result.data
            if (data != null) {
                val lat = data.getDoubleExtra("lat", 0.0)
                val lon = data.getDoubleExtra("lon", 0.0)
                val name = data?.getStringExtra("name") ?: "Location ${lat.format(2)}, ${lon.format(2)}"
                viewModel.addFavorite(FavoriteLocation(name = name, lat = lat, lon = lon))
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFavoritesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupNavigationDrawer()

        adapter = FavoritesAdapter(
            onItemClick = { location ->
                val intent = Intent(this, MainActivity::class.java)
                intent.putExtra("lat", location.lat)
                intent.putExtra("lon", location.lon)
                intent.putExtra("name", location.name)
                startActivity(intent)
            },
            onDeleteClick = { location ->
                AlertDialog.Builder(this)
                    .setTitle("Delete")
                    .setMessage("Are you sure you want to delete ${location.name}?")
                    .setPositiveButton("Yes") { _, _ -> viewModel.removeFavorite(location) }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
        )
        binding.recyclerFavorites.layoutManager = LinearLayoutManager(this)
        binding.recyclerFavorites.adapter = adapter

        binding.fabAddFavorite.setOnClickListener {
            mapPickerLauncher.launch(Intent(this, MapPickerActivity::class.java))
        }

        viewModel.favorites.observe(this) { list ->
            adapter.submitList(list)
            binding.tvNoPlaces.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    private fun setupNavigationDrawer() {
        setSupportActionBar(binding.toolbar)
        val toggle = ActionBarDrawerToggle(this, binding.drawerLayout, binding.toolbar, R.string.open, R.string.close)
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        binding.navigationView.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.nav_home -> startActivity(Intent(this, MainActivity::class.java))
                R.id.nav_alerts -> startActivity(Intent(this, AlertsActivity::class.java))
                R.id.nav_settings -> startActivity(Intent(this, SettingsActivity::class.java))
            }
            true
        }
    }

    private fun Double.format(digits: Int) = "%.${digits}f".format(this)
}
