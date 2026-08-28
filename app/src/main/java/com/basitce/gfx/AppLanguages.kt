package com.basitce.gfx

import java.util.Locale

const val DEFAULT_LANGUAGE_TAG = "en"

data class LanguageOption(val tag: String, val labelRes: Int)

val SUPPORTED_LANGUAGE_OPTIONS = listOf(
    LanguageOption("tr", R.string.language_turkish),
    LanguageOption("en", R.string.language_english),
    LanguageOption("es", R.string.language_spanish),
    LanguageOption("pt-BR", R.string.language_portuguese_brazil),
    LanguageOption("fr", R.string.language_french),
    LanguageOption("de", R.string.language_german),
    LanguageOption("ar", R.string.language_arabic),
    LanguageOption("ru", R.string.language_russian),
    LanguageOption("hi", R.string.language_hindi),
    LanguageOption("id", R.string.language_indonesian),
    LanguageOption("ja", R.string.language_japanese),
    LanguageOption("ko", R.string.language_korean)
)

val LANGUAGE_TAG_TO_RESOURCE_QUALIFIER = mapOf(
    "tr" to "values-tr",
    "en" to "values",
    "es" to "values-es",
    "pt-BR" to "values-pt-rBR",
    "fr" to "values-fr",
    "de" to "values-de",
    "ar" to "values-ar",
    "ru" to "values-ru",
    "hi" to "values-hi",
    "id" to "values-in",
    "ja" to "values-ja",
    "ko" to "values-ko"
)

fun normalizeSupportedLanguageTag(languageTag: String?): String {
    if (languageTag.isNullOrBlank()) {
        return DEFAULT_LANGUAGE_TAG
    }

    val normalizedTag = languageTag.trim().replace('_', '-')
    SUPPORTED_LANGUAGE_OPTIONS.firstOrNull {
        it.tag.equals(normalizedTag, ignoreCase = true)
    }?.let { return it.tag }

    val locale = Locale.forLanguageTag(normalizedTag)
    val language = locale.language.lowercase(Locale.ROOT)
    return when (language) {
        "pt" -> "pt-BR"
        "in", "id" -> "id"
        else -> SUPPORTED_LANGUAGE_OPTIONS.firstOrNull {
            Locale.forLanguageTag(it.tag).language.equals(language, ignoreCase = true)
        }?.tag ?: DEFAULT_LANGUAGE_TAG
    }
}

fun findSupportedLanguageOption(languageTag: String?): LanguageOption {
    val normalizedTag = normalizeSupportedLanguageTag(languageTag)
    return SUPPORTED_LANGUAGE_OPTIONS.firstOrNull { it.tag == normalizedTag }
        ?: SUPPORTED_LANGUAGE_OPTIONS.first { it.tag == DEFAULT_LANGUAGE_TAG }
}
