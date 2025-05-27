package com.example.weatherapp

import android.content.Context
import java.util.Locale

object LocaleHelper {

    fun wrap(context: Context, langCode: String): Context {
        val locale = Locale(langCode)
        Locale.setDefault(locale)

        val config = context.resources.configuration
        config.setLocale(locale)
        config.setLayoutDirection(locale)
        return context.createConfigurationContext(config)
    }

    fun getLanguage(context: Context): String {
        return context.getSharedPreferences("settings_prefs", Context.MODE_PRIVATE)
            .getString("language_code", "en") ?: "en"
    }

    fun setLanguage(context: Context, lang: String) {
        context.getSharedPreferences("settings_prefs", Context.MODE_PRIVATE)
            .edit()
            .putString("language_code", lang)
            .apply()
    }
}
