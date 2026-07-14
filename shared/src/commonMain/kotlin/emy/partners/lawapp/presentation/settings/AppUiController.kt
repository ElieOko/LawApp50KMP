package emy.partners.lawapp.presentation.settings

import androidx.compose.runtime.staticCompositionLocalOf
import emy.partners.lawapp.data.local.AppLanguage
import emy.partners.lawapp.data.local.AppUiSettings

data class AppUiController(
    val settings: AppUiSettings = AppUiSettings(),
    val updateSettings: (AppUiSettings) -> Unit = {},
)

val LocalAppUiController = staticCompositionLocalOf { AppUiController() }

fun AppUiSettings.t(fr: String, en: String): String =
    if (language == AppLanguage.French) fr else en
