package emy.partners.lawapp.domain.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

@Composable
fun NavHost(
    navigator: Navigator,
    content: @Composable (AppRoute) -> Unit
) {
    val entries = navigator.state.toDecoratedEntries()

    CompositionLocalProvider(LocalNavigator provides navigator) {
        entries.lastOrNull()?.let { entry ->
            entry.Render {
                content(entry.route)
            }
        }
    }
}
