package emy.partners.lawapp

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

@Composable
actual fun PlatformBackButtonHandler(enabled: Boolean, onBack: () -> Unit) {
    BackHandler(enabled = enabled, onBack = onBack)
}

@Composable
actual fun rememberApplicationExitController(): ApplicationExitController {
    val context = LocalContext.current
    return remember(context) {
        AndroidApplicationExitController(context)
    }
}

private class AndroidApplicationExitController(
    private val context: Context,
) : ApplicationExitController {
    override fun showExitPrompt() {
        Toast.makeText(
            context,
            "Appuyez encore une fois pour quitter",
            Toast.LENGTH_SHORT,
        ).show()
    }

    override fun exitApplication() {
        context.findActivity()?.finish()
    }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
