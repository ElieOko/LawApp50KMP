package emy.partners.lawapp.domain.navigation

import androidx.compose.runtime.staticCompositionLocalOf
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.filter
import kotlinx.serialization.Serializable

class Navigator(
    val state: NavState,
    val topLevelBackEnabled: Boolean,
) {
    private val _tabReselections = MutableSharedFlow<TopLevelRoute>(
        extraBufferCapacity = 1,
        onBufferOverflow = kotlinx.coroutines.channels.BufferOverflow.DROP_LATEST,
    )

    fun tabReselections(forRoute: TopLevelRoute): Flow<TopLevelRoute> =
        _tabReselections.filter { it == forRoute }

    fun goBack() {
        val currentBackstack = state.currentBackstack

        if (state.topLevelRoute == null) {
            // We're using the default stack, remove an entry if possible
            if (currentBackstack.size > 1) {
                currentBackstack.removeLastOrNull()
            }
            return
        }
        
        if (currentBackstack.size > 1) {
            currentBackstack.removeLastOrNull()
        } else if (state.topLevelRoute != state.primaryTopLevelRoute) {
             if (topLevelBackEnabled) {
                state.topLevelRoute = state.primaryTopLevelRoute
            }
        }
    }

    fun add(route: AppRoute) {
        if (route is TopLevelRoute) {
            activate(route)
        } else {
            state.currentBackstack.add(route)
        }
    }

    fun set(route: AppRoute) {
        state.currentBackstack.clear()
        add(route)
    }

    fun activate(route: TopLevelRoute, withReselection: Boolean = true) {
        if (withReselection && route == state.topLevelRoute) {
            val currentBackstack = state.currentBackstack

            // Reselected the current top-level route, clear to root
            if (currentBackstack.size > 1) {
                currentBackstack.removeRange(1, currentBackstack.size)
            } else {
                // Already at root, signal reselection for scroll-to-top
                _tabReselections.tryEmit(route)
            }
            return
        }
        state.topLevelRoute = route
    }
}

val LocalNavigator = staticCompositionLocalOf<Navigator> {
    error("No Navigator provided")
}
