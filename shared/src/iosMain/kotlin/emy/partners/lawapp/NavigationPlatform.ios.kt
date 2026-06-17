package emy.partners.lawapp

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
actual fun PlatformBackButtonHandler(enabled: Boolean, onBack: () -> Unit) = Unit

@Composable
actual fun rememberApplicationExitController(): ApplicationExitController {
    return remember {
        object : ApplicationExitController {
            override fun showExitPrompt() = Unit
            override fun exitApplication() = Unit
        }
    }
}
