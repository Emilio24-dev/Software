package com.example.registro

import android.content.Context
import android.content.res.Configuration
import java.util.Locale

object LocalManager {

    private const val PREF_NAME = "sentri_prefs"
    private const val KEY_LANG = "app_lang"

    fun getLanguage(context: Context): String {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        // Español por defecto
        return prefs.getString(KEY_LANG, "es") ?: "es"
    }

    fun setLanguage(context: Context, langCode: String) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_LANG, langCode).apply()
    }

    /**
     * Aplica el idioma guardado al contexto.
     */
    fun updateContextLocale(context: Context): Context {
        val lang = getLanguage(context)
        val locale = Locale(lang)
        Locale.setDefault(locale)

        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)

        // Crea un contexto con la nueva configuración
        return context.createConfigurationContext(config)
    }
}