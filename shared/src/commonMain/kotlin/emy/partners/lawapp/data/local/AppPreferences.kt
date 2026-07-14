package emy.partners.lawapp.data.local

enum class AppLanguage(val code: String) {
    French("fr"),
    English("en");

    companion object {
        fun fromCode(code: String?): AppLanguage =
            entries.firstOrNull { it.code.equals(code, ignoreCase = true) } ?: French
    }
}

data class AppUiSettings(
    val darkMode: Boolean = false,
    val language: AppLanguage = AppLanguage.French,
)

object AppPreferences {
    private const val KEY_DARK = "lawapp_dark_mode"
    private const val KEY_LANG = "lawapp_language"

    private val store: LocalStore by lazy { createLocalStore() }

    fun load(): AppUiSettings = AppUiSettings(
        darkMode = store.getString(KEY_DARK)?.toBooleanStrictOrNull() ?: false,
        language = AppLanguage.fromCode(store.getString(KEY_LANG)),
    )

    fun save(settings: AppUiSettings) {
        store.putString(KEY_DARK, settings.darkMode.toString())
        store.putString(KEY_LANG, settings.language.code)
    }

    fun update(block: (AppUiSettings) -> AppUiSettings): AppUiSettings {
        val next = block(load())
        save(next)
        return next
    }
}
