package emy.partners.lawapp.domain.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisallowComposableCalls
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.SaveableStateHolder
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.runtime.toMutableStateList

class NavEntry<out T : AppRoute>(
    val route: T,
    private val decorators: List<NavEntryDecorator>,
) {
    @Composable
    fun Render(content: @Composable () -> Unit) {
        val decoratedContent = decorators.fold(content) { acc, decorator ->
            { decorator.Decorate(this, acc) }
        }
        CompositionLocalProvider(LocalNavEntry provides this) {
            decoratedContent()
        }
    }
}

val LocalNavEntry = staticCompositionLocalOf<NavEntry<*>> {
    error("No NavEntry provided")
}

interface NavEntryDecorator {
    @Composable
    fun Decorate(entry: NavEntry<*>, content: @Composable () -> Unit)
}

@Composable
fun <T : AppRoute> rememberSaveableStateHolderNavEntryDecorator(): NavEntryDecorator {
    val holder = rememberSaveableStateHolder()
    return remember(holder) { SaveableStateHolderNavEntryDecorator(holder) }
}

private class SaveableStateHolderNavEntryDecorator(
    private val holder: SaveableStateHolder
) : NavEntryDecorator {
    @Composable
    override fun Decorate(entry: NavEntry<*>, content: @Composable () -> Unit) {
        holder.SaveableStateProvider(entry.route.saveableKey()) {
            content()
        }
    }
}

@Composable
fun rememberViewModelStoreNavEntryDecorator(): NavEntryDecorator {
    return remember {
        object : NavEntryDecorator {
            @Composable
            override fun Decorate(entry: NavEntry<*>, content: @Composable () -> Unit) {
                // Simplified for now, usually handles ViewModelStore per entry
                content()
            }
        }
    }
}

@Composable
fun <T : AppRoute> rememberDecoratedNavEntries(
    backStack: List<T>,
    entryDecorators: List<NavEntryDecorator>,
    entryProvider: (T) -> NavEntry<T> = { route -> NavEntry(route, entryDecorators) }
): List<NavEntry<T>> {
    return backStack.map { route ->
        key(route.saveableKey()) {
            remember(route, entryDecorators) {
                entryProvider(route)
            }
        }
    }
}
