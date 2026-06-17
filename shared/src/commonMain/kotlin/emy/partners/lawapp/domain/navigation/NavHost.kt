package emy.partners.lawapp.domain.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

@Composable
fun NavHost(
    navigator: Navigator,
    content: @Composable (AppRoute) -> Unit
) {
    val entries = navigator.state.toDecoratedEntries { route ->
        NavEntry(route, emptyList()) // Decorators are already handled in toDecoratedEntries
    }

    CompositionLocalProvider(LocalNavigator provides navigator) {
        entries.lastOrNull()?.let { entry ->
            entry.Render {
                content(entry.route)
            }
        }
    }
}
