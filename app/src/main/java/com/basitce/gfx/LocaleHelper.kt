package com.basitce.gfx

import android.content.Context
import android.content.res.Configuration
import java.util.Locale

object LocaleHelper {
    private const val SELECTED_LANGUAGE = "Locale.Helper.Selected.Language"

    fun setLocale(context: Context, language: String): Context {
        val normalizedLanguage = normalizeSupportedLanguageTag(language)
        persist(context, normalizedLanguage)
        return updateResources(context, normalizedLanguage)
    }

    private fun persist(context: Context, language: String) {
        val preferences = context.getSharedPreferences("basitce.gfx_prefs", Context.MODE_PRIVATE)
        preferences.edit().putString(SELECTED_LANGUAGE, language).apply()
    }

    fun getLanguage(context: Context): String {
        val preferences = context.getSharedPreferences("basitce.gfx_prefs", Context.MODE_PRIVATE)
        // Eğer kullanıcı henüz bir dil seçmediyse telefonun sistem dilini kullan
        val storedLanguage = preferences.getString(SELECTED_LANGUAGE, null)
        val systemLanguage = Locale.getDefault().toLanguageTag()
        return normalizeSupportedLanguageTag(storedLanguage ?: systemLanguage)
    }

    private fun updateResources(context: Context, language: String): Context {
        val locale = Locale.forLanguageTag(normalizeSupportedLanguageTag(language))
        Locale.setDefault(locale)

        val configuration = context.resources.configuration
        configuration.setLocale(locale)
        configuration.setLayoutDirection(locale)

        return context.createConfigurationContext(configuration)
    }

    fun wrapContext(context: Context): Context {
        val lang = getLanguage(context)
        return updateResources(context, lang)
    }
}
