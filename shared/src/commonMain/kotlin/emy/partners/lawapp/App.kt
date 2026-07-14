package emy.partners.lawapp

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.ui.Alignment
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.snapshots.SnapshotStateList
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.screen.ScreenKey
import cafe.adriel.voyager.core.screen.uniqueScreenKey
import cafe.adriel.voyager.navigator.CurrentScreen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.currentOrThrow
import emy.partners.lawapp.data.Constants
import emy.partners.lawapp.data.local.AppPreferences
import emy.partners.lawapp.data.remote.auth.AuthRepository
import emy.partners.lawapp.domain.models.ContentDestination
import emy.partners.lawapp.domain.models.EvaluationDAO
import emy.partners.lawapp.domain.models.EvaluationSession
import emy.partners.lawapp.domain.models.EvaluationStatus
import emy.partners.lawapp.domain.models.UserGeneratedContent
import emy.partners.lawapp.domain.models.UserGeneratedContentDraft
import emy.partners.lawapp.presentation.components.basics.TopBarCustom
import emy.partners.lawapp.presentation.pages.ProfilPage
import emy.partners.lawapp.presentation.pages.auth.AuthActions
import emy.partners.lawapp.presentation.pages.auth.AuthRequiredPanel
import emy.partners.lawapp.presentation.pages.auth.LocalAuthActions
import emy.partners.lawapp.presentation.pages.auth.LoginPage
import emy.partners.lawapp.presentation.pages.auth.OtpVerificationPage
import emy.partners.lawapp.presentation.pages.auth.RecoveryAccountPage
import emy.partners.lawapp.presentation.pages.auth.RecoveryContactType
import emy.partners.lawapp.presentation.pages.auth.RegisterPage
import emy.partners.lawapp.presentation.pages.content.ContentCreatePage
import emy.partners.lawapp.presentation.pages.explore.ExploreDetailPage
import emy.partners.lawapp.presentation.pages.explore.ExplorePage
import emy.partners.lawapp.presentation.pages.home.HomePage
import emy.partners.lawapp.presentation.pages.session.EvaluationCreatePage
import emy.partners.lawapp.presentation.pages.session.EvaluationDetailPage
import emy.partners.lawapp.presentation.pages.session.EvaluationPage
import emy.partners.lawapp.presentation.pages.session.QuizPage
import emy.partners.lawapp.presentation.pages.settings.LanguageSettingsPage
import emy.partners.lawapp.presentation.pages.settings.ThemeSettingsPage
import emy.partners.lawapp.presentation.settings.AppUiController
import emy.partners.lawapp.presentation.settings.LocalAppUiController
import emy.partners.lawapp.presentation.themes.BlueDark
import emy.partners.lawapp.presentation.themes.tekoTypography
import io.github.fletchmckee.liquid.liquefiable
import io.github.fletchmckee.liquid.liquid
import io.github.fletchmckee.liquid.rememberLiquidState
import lawapp.shared.generated.resources.Res
import lawapp.shared.generated.resources.discovery
import lawapp.shared.generated.resources.evaluation
import lawapp.shared.generated.resources.explore
import lawapp.shared.generated.resources.house
import lawapp.shared.generated.resources.justice
import lawapp.shared.generated.resources.profil
import lawapp.shared.generated.resources.profil_user
import lawapp.shared.generated.resources.quiz
import lawapp.shared.generated.resources.session
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource


private data class TopLevelDestination(
    val kind: TopLevelDestinationKind,
    val createScreen: () -> LawAppScreen,
    val name: String,
    val icon: DrawableResource,
)

private enum class TopLevelDestinationKind {
    Home,
    Explore,
    Evaluation,
    Quiz,
    Profile,
}

private class LawAppNavigationContext(
    val contentPadding: PaddingValues,
    val state: LawAppState,
) {
    val evaluations: List<EvaluationSession>
        get() = Constants.evaluations + state.createdEvaluations
    val createdContents: List<UserGeneratedContent>
        get() = state.createdContents
}

private val LocalLawAppNavigationContext = staticCompositionLocalOf<LawAppNavigationContext> {
    error("No LawAppNavigationContext provided")
}

private class LawAppState(
    val createdEvaluations: SnapshotStateList<EvaluationSession>,
    val createdContents: SnapshotStateList<UserGeneratedContent>,
) {
    private val pageScrollStates = mutableStateMapOf<String, ScrollState>()

    var currentPageState by mutableStateOf<LawAppPageState?>(null)

    fun scrollStateFor(pageKey: String): ScrollState =
        pageScrollStates.getOrPut(pageKey) { ScrollState(initial = 0) }
}

private data class LawAppPageState(
    val key: String,
    val topLevelDestinationKind: TopLevelDestinationKind,
    val scrollState: ScrollState,
)

private interface LawAppScreen : Screen {
    val topLevelDestinationKind: TopLevelDestinationKind
    val pageStateKey: String
    val showsAppChrome: Boolean get() = true
}

private abstract class UniqueLawAppScreen : LawAppScreen {
    override val key: ScreenKey = uniqueScreenKey
}

private class HomeScreen : UniqueLawAppScreen() {
    override val topLevelDestinationKind: TopLevelDestinationKind = TopLevelDestinationKind.Home
    override val pageStateKey: String = "home"

    @Composable
    override fun Content() {
        val context = LocalLawAppNavigationContext.current
        rememberPageScrollState(pageStateKey, topLevelDestinationKind)
        HomePage(
            modifier = Modifier.padding(bottom = context.contentPadding.calculateBottomPadding()),
            createdContents = context.createdContents
        )
    }
}

private class ExploreScreen : UniqueLawAppScreen() {
    override val topLevelDestinationKind: TopLevelDestinationKind = TopLevelDestinationKind.Explore
    override val pageStateKey: String = "explore"

    @Composable
    override fun Content() {
        ExploreRootContent(pageStateKey)
    }
}

private data class ContentCreateScreen(
    val initialDestination: ContentDestination
) : UniqueLawAppScreen() {
    override val topLevelDestinationKind: TopLevelDestinationKind =
        if (initialDestination == ContentDestination.Home) {
            TopLevelDestinationKind.Home
        } else {
            TopLevelDestinationKind.Explore
        }
    override val pageStateKey: String = "content/create/${initialDestination.name.lowercase()}"
    // Page individuelle : pas de topbar / bottombar / fond justice.
    override val showsAppChrome: Boolean = false

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        rememberPageScrollState(pageStateKey, topLevelDestinationKind)

        ContentCreatePage(
            modifier = Modifier.fillMaxSize(),
            initialDestination = initialDestination,
            onBack = { navigator.pop() },
            onPublish = { draft ->
                // Pas d'ajout local : le contenu apparait sur Home apres pull-to-refresh.
                navigator.replaceAll(
                    if (draft.destination == ContentDestination.Home) HomeScreen() else ExploreScreen()
                )
            }
        )
    }
}

private data class ExploreDetailScreen(val blogId: Long) : UniqueLawAppScreen() {
    override val topLevelDestinationKind: TopLevelDestinationKind = TopLevelDestinationKind.Explore
    override val pageStateKey: String = "explore/detail/$blogId"

    @Composable
    override fun Content() {
        val context = LocalLawAppNavigationContext.current
        val navigator = LocalNavigator.currentOrThrow
        val blog = Constants.blog.firstOrNull { it.id == blogId }

        if (blog == null) {
            ExploreRootContent("explore")
            return
        }

        val scrollVertical = rememberPageScrollState(pageStateKey, topLevelDestinationKind)

        ExploreDetailPage(
            blog = blog,
            modifier = Modifier.padding(top = context.contentPadding.calculateTopPadding()),
            onBack = { navigator.pop() },
            scrollVertical = scrollVertical,
        )
    }
}

private class EvaluationScreen : UniqueLawAppScreen() {
    override val topLevelDestinationKind: TopLevelDestinationKind = TopLevelDestinationKind.Evaluation
    override val pageStateKey: String = "evaluation"

    @Composable
    override fun Content() {
        EvaluationRootContent(pageStateKey)
    }
}

private data class EvaluationDetailScreen(val evaluationId: Long) : UniqueLawAppScreen() {
    override val topLevelDestinationKind: TopLevelDestinationKind = TopLevelDestinationKind.Evaluation
    override val pageStateKey: String = "evaluation/detail/$evaluationId"

    @Composable
    override fun Content() {
        val context = LocalLawAppNavigationContext.current
        val navigator = LocalNavigator.currentOrThrow
        val evaluation = context.evaluations.firstOrNull { it.id == evaluationId }

        if (evaluation == null) {
            EvaluationRootContent("evaluation")
            return
        }

        val scrollVertical = rememberPageScrollState(pageStateKey, topLevelDestinationKind)

        EvaluationDetailPage(
            evaluation = evaluation,
            modifier = Modifier.padding(top = context.contentPadding.calculateTopPadding()),
            onBack = { navigator.pop() },
            onStartQuiz = { navigator.replaceAll(QuizScreen()) },
            scrollVertical = scrollVertical,
        )
    }
}

private class EvaluationCreateScreen : UniqueLawAppScreen() {
    override val topLevelDestinationKind: TopLevelDestinationKind = TopLevelDestinationKind.Evaluation
    override val pageStateKey: String = "evaluation/create"

    @Composable
    override fun Content() {
        val context = LocalLawAppNavigationContext.current
        val navigator = LocalNavigator.currentOrThrow
        val scrollVertical = rememberPageScrollState(pageStateKey, topLevelDestinationKind)

        EvaluationCreatePage(
            modifier = Modifier.padding(top = context.contentPadding.calculateTopPadding()),
            onBack = { navigator.pop() },
            onSave = { evaluation ->
                context.state.createdEvaluations.add(evaluation.toSession(context.state.createdEvaluations.size))
                navigator.pop()
            },
            scrollVertical = scrollVertical,
        )
    }
}

private class QuizScreen : UniqueLawAppScreen() {
    override val topLevelDestinationKind: TopLevelDestinationKind = TopLevelDestinationKind.Quiz
    override val pageStateKey: String = "quiz"

    @Composable
    override fun Content() {
        val context = LocalLawAppNavigationContext.current
        val authActions = LocalAuthActions.current
        val scrollVertical = rememberPageScrollState(pageStateKey, topLevelDestinationKind)
        val session = AuthRepository.currentSession
        val isLoggedIn = !session?.accessToken.isNullOrBlank()

        if (!isLoggedIn) {
            AuthRequiredPanel(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = context.contentPadding.calculateTopPadding())
                    .padding(horizontal = 14.dp),
                title = "Quiz reserve aux membres",
                message = "Connectez-vous pour demarrer un quiz juridique.",
                onLogin = authActions.openLogin,
            )
        } else {
            QuizPage(
                modifier = Modifier.padding(top = context.contentPadding.calculateTopPadding()),
                scrollVertical = scrollVertical,
            )
        }
    }
}

private class ProfileScreen : UniqueLawAppScreen() {
    override val topLevelDestinationKind: TopLevelDestinationKind = TopLevelDestinationKind.Profile
    override val pageStateKey: String = "profile"

    @Composable
    override fun Content() {
        val context = LocalLawAppNavigationContext.current
        val authActions = LocalAuthActions.current
        val navigator = LocalNavigator.currentOrThrow
        val scrollVertical = rememberPageScrollState(pageStateKey, topLevelDestinationKind)

        ProfilPage(
            modifier = Modifier.padding(top = context.contentPadding.calculateTopPadding()),
            scrollVertical = scrollVertical,
            session = AuthRepository.currentSession,
            onConnectClick = authActions.openLogin,
            onOpenThemeSettings = { navigator.push(ThemeSettingsScreen()) },
            onOpenLanguageSettings = { navigator.push(LanguageSettingsScreen()) },
        )
    }
}

private class ThemeSettingsScreen : UniqueLawAppScreen() {
    override val topLevelDestinationKind: TopLevelDestinationKind = TopLevelDestinationKind.Profile
    override val pageStateKey: String = "settings/theme"
    override val showsAppChrome: Boolean = false

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        rememberPageScrollState(pageStateKey, topLevelDestinationKind)
        ThemeSettingsPage(onBack = { navigator.pop() })
    }
}

private class LanguageSettingsScreen : UniqueLawAppScreen() {
    override val topLevelDestinationKind: TopLevelDestinationKind = TopLevelDestinationKind.Profile
    override val pageStateKey: String = "settings/language"
    override val showsAppChrome: Boolean = false

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        rememberPageScrollState(pageStateKey, topLevelDestinationKind)
        LanguageSettingsPage(onBack = { navigator.pop() })
    }
}

private class LoginScreen : UniqueLawAppScreen() {
    override val topLevelDestinationKind: TopLevelDestinationKind = TopLevelDestinationKind.Profile
    override val pageStateKey: String = "auth/login"
    override val showsAppChrome: Boolean = false

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        rememberPageScrollState(pageStateKey, topLevelDestinationKind)

        LoginPage(
            onBack = { navigator.pop() },
            onRegisterClick = { navigator.push(RegisterScreen()) },
            onForgotPasswordClick = { navigator.push(RecoveryAccountScreen()) },
            onGoogleClick = { navigator.replaceAll(HomeScreen()) },
            onLoginSuccess = { navigator.replaceAll(HomeScreen()) },
        )
    }
}

private class RegisterScreen : UniqueLawAppScreen() {
    override val topLevelDestinationKind: TopLevelDestinationKind = TopLevelDestinationKind.Profile
    override val pageStateKey: String = "auth/register"
    override val showsAppChrome: Boolean = false

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        rememberPageScrollState(pageStateKey, topLevelDestinationKind)

        RegisterPage(
            onBack = { navigator.pop() },
            onLoginClick = { navigator.pop() },
            onGoogleClick = {
                navigator.popUntil { it is ProfileScreen }
            },
            onRegisterSuccess = { email ->
                navigator.replace(
                    OtpVerificationScreen(
                        contactType = RecoveryContactType.Email,
                        contactValue = email,
                    )
                )
            },
        )
    }
}

private class RecoveryAccountScreen : UniqueLawAppScreen() {
    override val topLevelDestinationKind: TopLevelDestinationKind = TopLevelDestinationKind.Profile
    override val pageStateKey: String = "auth/recovery"
    override val showsAppChrome: Boolean = false

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        rememberPageScrollState(pageStateKey, topLevelDestinationKind)

        RecoveryAccountPage(
            onBack = { navigator.pop() },
            onContinueClick = { contactType, value ->
                navigator.push(
                    OtpVerificationScreen(
                        contactType = contactType,
                        contactValue = value,
                    )
                )
            },
        )
    }
}

private data class OtpVerificationScreen(
    val contactType: RecoveryContactType,
    val contactValue: String,
) : UniqueLawAppScreen() {
    override val topLevelDestinationKind: TopLevelDestinationKind = TopLevelDestinationKind.Profile
    override val pageStateKey: String = "auth/otp/${contactType.name.lowercase()}"
    override val showsAppChrome: Boolean = false

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        rememberPageScrollState(pageStateKey, topLevelDestinationKind)
        val destinationLabel = when (contactType) {
            RecoveryContactType.Email -> contactValue
            RecoveryContactType.Telephone -> contactValue
        }

        OtpVerificationPage(
            identifier = contactValue,
            destinationLabel = destinationLabel,
            onBack = { navigator.pop() },
            onVerifySuccess = {
                navigator.popUntil { it is LoginScreen || it is ProfileScreen }
            },
        )
    }
}

@Composable
private fun ExploreRootContent(pageStateKey: String) {
    val context = LocalLawAppNavigationContext.current
    val navigator = LocalNavigator.currentOrThrow
    val scrollVertical = rememberPageScrollState(pageStateKey, TopLevelDestinationKind.Explore)

    ExplorePage(
        modifier = Modifier.padding(top = context.contentPadding.calculateTopPadding()),
        scrollVertical = scrollVertical,
        createdContents = context.createdContents,
        onBlogClick = { navigator.push(ExploreDetailScreen(it.id)) },
    )
}

@Composable
private fun EvaluationRootContent(pageStateKey: String) {
    val context = LocalLawAppNavigationContext.current
    val navigator = LocalNavigator.currentOrThrow
    val authActions = LocalAuthActions.current
    val scrollVertical = rememberPageScrollState(pageStateKey, TopLevelDestinationKind.Evaluation)
    val session = AuthRepository.currentSession
    val isLoggedIn = !session?.accessToken.isNullOrBlank()
    val isTeacher = session?.profile?.isTeacher == true
    val canCreate = isLoggedIn && isTeacher
    val blockedMessage = if (isLoggedIn && !isTeacher) {
        "Seuls les enseignants peuvent creer des evaluations."
    } else {
        null
    }

    if (!isLoggedIn) {
        AuthRequiredPanel(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = context.contentPadding.calculateTopPadding())
                .padding(horizontal = 14.dp),
            title = "Evaluations reservees aux membres",
            message = "Connectez-vous pour consulter et gerer vos evaluations juridiques.",
            onLogin = authActions.openLogin,
        )
    } else {
        EvaluationPage(
            evaluations = context.evaluations,
            modifier = Modifier.padding(top = context.contentPadding.calculateTopPadding()),
            onEvaluationClick = { navigator.push(EvaluationDetailScreen(it.id)) },
            onCreateClick = {
                if (isTeacher) navigator.push(EvaluationCreateScreen())
            },
            canCreateEvaluations = canCreate,
            createBlockedMessage = blockedMessage,
            scrollVertical = scrollVertical,
        )
    }
}

@Composable
private fun rememberPageScrollState(
    pageStateKey: String,
    topLevelDestinationKind: TopLevelDestinationKind,
): ScrollState {
    val context = LocalLawAppNavigationContext.current
    val scrollState = remember(pageStateKey) {
        context.state.scrollStateFor(pageStateKey)
    }

    DisposableEffect(context.state, pageStateKey, topLevelDestinationKind, scrollState) {
        context.state.currentPageState = LawAppPageState(
            key = pageStateKey,
            topLevelDestinationKind = topLevelDestinationKind,
            scrollState = scrollState,
        )
        onDispose {
            if (context.state.currentPageState?.key == pageStateKey) {
                context.state.currentPageState = null
            }
        }
    }

    return scrollState
}

@Composable
@Preview(showBackground = true)
fun App() {
    val liquidState = rememberLiquidState()
    val liquidState2 = rememberLiquidState()
    val createdEvaluations = remember { mutableStateListOf<EvaluationSession>() }
    val createdContents = remember { mutableStateListOf<UserGeneratedContent>() }
    val appState = remember(createdEvaluations, createdContents) {
        LawAppState(createdEvaluations, createdContents)
    }
    var uiSettings by remember { mutableStateOf(AppPreferences.load()) }
    val uiController = remember(uiSettings) {
        AppUiController(
            settings = uiSettings,
            updateSettings = { next ->
                AppPreferences.save(next)
                uiSettings = next
            }
        )
    }
    val colorScheme = if (uiSettings.darkMode) {
        darkColorScheme(
            primary = Color(0xFF60A5FA),
            onPrimary = Color.White,
            background = Color(0xFF0B1220),
            onBackground = Color(0xFFE2E8F0),
            surface = Color(0xFF111827),
            onSurface = Color(0xFFE2E8F0),
        )
    } else {
        lightColorScheme(
            primary = BlueDark,
            onPrimary = Color.White,
            background = Color(0xFFE8EEF7),
            onBackground = Color(0xFF0F172A),
            surface = Color.White,
            onSurface = Color(0xFF0F172A),
        )
    }
    val defaultTopBarScrollState = rememberScrollState()
    val topLevelDestinations = listOf(
        TopLevelDestination(TopLevelDestinationKind.Home, ::HomeScreen, stringResource(Res.string.house), Res.drawable.house),
        TopLevelDestination(TopLevelDestinationKind.Explore, ::ExploreScreen, stringResource(Res.string.discovery), Res.drawable.explore),
        TopLevelDestination(TopLevelDestinationKind.Evaluation, ::EvaluationScreen, stringResource(Res.string.session), Res.drawable.evaluation),
        TopLevelDestination(TopLevelDestinationKind.Quiz, ::QuizScreen, stringResource(Res.string.quiz), Res.drawable.quiz),
        TopLevelDestination(TopLevelDestinationKind.Profile, ::ProfileScreen, stringResource(Res.string.profil), Res.drawable.profil_user),
    )
    CompositionLocalProvider(LocalAppUiController provides uiController) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = tekoTypography()
        ) {
            Navigator(HomeScreen()) { navigator ->
                val topBarScrollState = appState.currentPageState?.scrollState ?: defaultTopBarScrollState
                val selectedTopLevel = (navigator.lastItem as? LawAppScreen)
                    ?.topLevelDestinationKind
                    ?: TopLevelDestinationKind.Home
                val showsAppChrome = (navigator.lastItem as? LawAppScreen)?.showsAppChrome != false
                Scaffold(
                    //            contentWindowInsets = WindowInsets(0),
                    bottomBar = {
                        if (showsAppChrome) {
                            val isHomeChrome = selectedTopLevel == TopLevelDestinationKind.Home
                            Box(
                                modifier = if (isHomeChrome) {
                                    Modifier
                                } else {
                                    Modifier
                                        .clip(RoundedCornerShape(9.dp))
                                        .liquefiable(liquidState2)
                                }
                            ) {
                                BottomAppBar(
                                    containerColor = if (isHomeChrome) {
                                        Color.Transparent
                                    } else {
                                        Color.White.copy(alpha = 0.5f)
                                    },
                                    modifier = if (isHomeChrome) {
                                        Modifier
                                    } else {
                                        Modifier.background(Color.White.copy(alpha = 0.5f))
                                    },
                                ) {
                                    topLevelDestinations.forEach { destination ->
                                        NavigationBarItem(
                                            interactionSource = remember { MutableInteractionSource() },
                                            colors = NavigationBarItemDefaults.colors(
                                                indicatorColor = Color.White.copy(alpha = 0.65f),
                                                selectedTextColor = Color(0xFf2563EB),
                                                selectedIconColor = Color(0xFf2563EB),
                                                unselectedIconColor = Color.Black.copy(0.6f),
                                                unselectedTextColor = Color.Black.copy(0.6f),
                                            ),
                                            selected = destination.kind == selectedTopLevel,
                                            onClick = { navigator.replaceAll(destination.createScreen()) },
                                            icon = {
                                                Icon(
                                                    painter = painterResource(destination.icon),
                                                    null,
                                                    modifier = Modifier.size(28.dp),
                                                )
                                            },
                                            label = {
                                                Text(
                                                    destination.name,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 12.sp,
                                                )
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    },
                    topBar = {
                        if (showsAppChrome) {
                            TopBarCustom(
                                scrollState = topBarScrollState,
                                onActionClick = {
                                    val loggedIn = !AuthRepository.currentSession?.accessToken.isNullOrBlank()
                                    if (!loggedIn) {
                                        navigator.push(LoginScreen())
                                    } else {
                                        navigator.push(
                                            ContentCreateScreen(
                                                initialDestination = selectedTopLevel.toDestination()
                                            )
                                        )
                                    }
                                }
                            )
                        }
                    }
                    //            contentWindowInsets = WindowInsets(0, 0, 0, 0) // Désactive les insets par défaut
                ) {

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .liquefiable(liquidState)
                    ) {
                        val hideJusticeBackground =
                            !showsAppChrome ||
                                selectedTopLevel == TopLevelDestinationKind.Profile ||
                                selectedTopLevel == TopLevelDestinationKind.Explore ||
                                selectedTopLevel == TopLevelDestinationKind.Quiz ||
                                selectedTopLevel == TopLevelDestinationKind.Evaluation
                        if (!hideJusticeBackground) {
                            Image(
                                painter = painterResource(Res.drawable.justice),
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.matchParentSize(),
                                colorFilter = ColorFilter.colorMatrix(
                                    ColorMatrix().apply {
                                        setToSaturation(0.7f) // 1 = normal, 0 = noir et blanc
                                    }
                                )
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .background(
                                        if (uiSettings.darkMode) Color(0xFF0B1220) else Color(0xFFE8EEF7)
                                    )
                            )
                        }

                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .liquid(liquidState)
                                .background(
                                    if (hideJusticeBackground) {
                                        Color.Transparent
                                    } else {
                                        Color.White.copy(alpha = 0.15f)
                                    }
                                )
                        )
                        val navigationContext = LawAppNavigationContext(
                            contentPadding = it,
                            state = appState,
                        )
                        val authActions = remember(navigator) {
                            AuthActions(
                                openLogin = { navigator.push(LoginScreen()) },
                                openRegister = { navigator.push(RegisterScreen()) },
                            )
                        }
                        Column {
                            CompositionLocalProvider(
                                LocalLawAppNavigationContext provides navigationContext,
                                LocalAuthActions provides authActions,
                            ) {
                                CurrentScreen()
                            }
                        }
                    }



                }
            }
        }
    }
}

@Composable
expect fun PlatformVideoPlayer(
    url: String,
    modifier: Modifier = Modifier,
    isPlaying: Boolean,
    isLooping: Boolean = true,
    showControls: Boolean = true,
)

private fun EvaluationDAO.toSession(index: Int) = EvaluationSession(
    id = id ?: (1_000L + index),
    title = title,
    domain = "Brouillon",
    description = description,
    status = EvaluationStatus.InProgress,
    progress = 0f,
    score = null,
    questionCount = (option?.size ?: 0) + (ouverte?.size ?: 0) + (caseStudy?.size ?: 0),
    completedQuestions = 0,
    duration = compteur?.let(::formatDurationFromMinutes) ?: "A definir",
    updatedAt = "Cree maintenant",
    level = "Personnalise"
)

private fun formatDurationFromMinutes(totalMinutes: Long): String {
    val safeMinutes = totalMinutes.coerceAtLeast(0L)
    val hours = safeMinutes / 60
    val minutes = safeMinutes % 60
    return if (hours > 0) {
        "${hours}h ${minutes.toString().padStart(2, '0')}m"
    } else {
        "${minutes} min"
    }
}

private fun TopLevelDestinationKind.toDestination(): ContentDestination =
    if (this == TopLevelDestinationKind.Explore) {
        ContentDestination.Explore
    } else {
        ContentDestination.Home
    }

private fun UserGeneratedContentDraft.toContent(index: Int): UserGeneratedContent = UserGeneratedContent(
    id = 20_000L + index,
    destination = destination,
    title = title,
    description = description,
    author = author,
    link = link,
    attachment = attachment,
    createdAt = "Maintenant"
)