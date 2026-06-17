package emy.partners.lawapp

import androidx.compose.runtime.mutableStateListOf
import emy.partners.lawapp.domain.navigation.AppRoute
import emy.partners.lawapp.domain.navigation.ExploreDetailScreen
import emy.partners.lawapp.domain.navigation.ExploreScreen
import emy.partners.lawapp.domain.navigation.HomeScreen
import emy.partners.lawapp.domain.navigation.NavState
import emy.partners.lawapp.domain.navigation.Navigator
import emy.partners.lawapp.domain.navigation.QuizScreen
import emy.partners.lawapp.domain.navigation.SettingScreen
import emy.partners.lawapp.domain.navigation.TopLevelRoute
import emy.partners.lawapp.domain.navigation.saveableKey
import kotlin.test.Test
import kotlin.test.assertEquals

class SharedCommonTest {

    @Test
    fun example() {
        assertEquals(3, 1 + 2)
    }

    @Test
    fun switchingTabsPreservesPreviousTopLevelBackStack() {
        val navigator = testNavigator(startRoute = ExploreScreen)

        navigator.add(ExploreDetailScreen(blogId = 3))
        assertEquals(
            listOf(ExploreScreen, ExploreDetailScreen(blogId = 3)),
            navigator.state.currentBackstack.toList()
        )

        navigator.activate(QuizScreen)
        assertEquals(listOf(QuizScreen), navigator.state.currentBackstack.toList())

        navigator.activate(ExploreScreen)
        assertEquals(
            listOf(ExploreScreen, ExploreDetailScreen(blogId = 3)),
            navigator.state.currentBackstack.toList()
        )
    }

    @Test
    fun reselectingCurrentTabClearsItToRoot() {
        val navigator = testNavigator(startRoute = ExploreScreen)

        navigator.add(ExploreDetailScreen(blogId = 3))
        navigator.activate(ExploreScreen)

        assertEquals(listOf(ExploreScreen), navigator.state.currentBackstack.toList())
    }

    @Test
    fun settingsRouteIsPushedOnCurrentBackStack() {
        val navigator = testNavigator(startRoute = HomeScreen)

        navigator.add(SettingScreen)

        assertEquals(listOf(HomeScreen, SettingScreen), navigator.state.currentBackstack.toList())
    }

    @Test
    fun navigationRoutesExposeBundleSafeSaveableKeys() {
        assertEquals("home", HomeScreen.saveableKey())
        assertEquals("explore/detail/3", ExploreDetailScreen(blogId = 3).saveableKey())
        assertEquals("settings", SettingScreen.saveableKey())
    }

    private fun testNavigator(startRoute: TopLevelRoute): Navigator {
        val topLevelRoutes = setOf(HomeScreen, ExploreScreen, QuizScreen)
        val topLevelBackStacks = topLevelRoutes.associateWith { route ->
            mutableStateListOf<AppRoute>(route)
        }
        val state = NavState(
            topLevelBackStacks = topLevelBackStacks,
            defaultBackstack = mutableStateListOf(),
            primaryTopLevelRoute = HomeScreen,
            currentBackstack = mutableStateListOf(startRoute),
        )
        return Navigator(state = state, topLevelBackEnabled = true)
    }
}