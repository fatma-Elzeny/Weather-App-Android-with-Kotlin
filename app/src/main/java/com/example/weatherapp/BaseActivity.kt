package com.example.weatherapp

import android.content.Context
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatActivity
import com.example.weatherapp.Settings.model.Language
import com.example.weatherapp.Settings.model.SettingsRepository

import java.util.Locale

// BaseActivity.kt
open class BaseActivity : AppCompatActivity() {
    override fun attachBaseContext(newBase: Context) {
        val lang = SettingsRepository(newBase).loadSettings().language
        val locale = if (lang == Language.ARABIC) Locale("ar") else Locale("en")
        val config = Configuration()
        config.setLocale(locale)
        val context = newBase.createConfigurationContext(config)
        super.attachBaseContext(context)
    }
}
