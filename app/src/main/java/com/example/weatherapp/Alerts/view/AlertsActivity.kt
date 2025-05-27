package com.example.weatherapp.Alerts.view

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.weatherapp.Alerts.viewmodel.AlertsViewModel
import com.example.weatherapp.Alerts.viewmodel.AlertsViewModelFactory
import com.example.weatherapp.Favourites.view.FavoritesActivity
import com.example.weatherapp.Home.view.MainActivity
import com.example.weatherapp.R
import com.example.weatherapp.Settings.view.SettingsActivity
import com.example.weatherapp.data.db.WeatherDatabase
import com.example.weatherapp.data.db.WeatherLocalDataSourceImpl
import com.example.weatherapp.data.model.WeatherAlert
import com.example.weatherapp.data.network.RetrofitClient
import com.example.weatherapp.data.network.WeatherRemoteDataSourceImpl
import com.example.weatherapp.data.repo.WeatherRepository
import com.example.weatherapp.data.repo.WeatherRepositoryImpl
import com.example.weatherapp.databinding.ActivityAlertsBinding
import kotlinx.coroutines.launch

class AlertsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAlertsBinding
    private lateinit var viewModel: AlertsViewModel
    private lateinit var adapter: AlertListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAlertsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val repository = WeatherRepositoryImpl(
            remote = WeatherRemoteDataSourceImpl(RetrofitClient.api),
            local = WeatherLocalDataSourceImpl(WeatherDatabase.getInstance(this).weatherDao()),
            this
        )
        val factory = AlertsViewModelFactory(repository, application)
        viewModel = ViewModelProvider(this, factory).get(AlertsViewModel::class.java)
        // Setup drawer if used
        setSupportActionBar(binding.toolbar)
        val toggle = ActionBarDrawerToggle(this, binding.drawerLayout, binding.toolbar, R.string.open, R.string.close)
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        binding.navigationView.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.nav_home -> startActivity(Intent(this, MainActivity::class.java))
                R.id.nav_favorites -> startActivity(Intent(this, FavoritesActivity::class.java))
                R.id.nav_settings -> startActivity(Intent(this, SettingsActivity::class.java))
            }
            true
        }
        lifecycleScope.launch {
            viewModel.cleanUpExpiredAlertsOnce()
        }


        // Setup RecyclerView
        adapter = AlertListAdapter { alert ->
            AlertDialog.Builder(this)
                .setTitle("Delete")
                .setMessage("Delete this alert?")
                .setPositiveButton("Yes") { _, _ -> viewModel.deleteAlert(alert) }
                .setNegativeButton("Cancel", null)
                .show()
        }
        binding.recyclerAlerts.adapter = adapter
        binding.recyclerAlerts.layoutManager = LinearLayoutManager(this)

        // Observe alerts
        viewModel.alerts.observe(this) { list ->
            adapter.submitList(list)
            binding.tvNoAlerts.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
        }

        // FAB to add alert
        binding.fabAddAlert.setOnClickListener {
            AddAlertDialog(this) { alert ->
                viewModel.addAlert(alert) // Simplified callback
            }.show()
        }
    }
}
