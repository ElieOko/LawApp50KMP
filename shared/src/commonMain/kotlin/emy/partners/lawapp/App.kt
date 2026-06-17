package emy.partners.lawapp

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import emy.partners.lawapp.presentation.components.basics.TopBarCustom
import emy.partners.lawapp.domain.models.EvaluationDAO
import emy.partners.lawapp.domain.models.EvaluationSession
import emy.partners.lawapp.domain.models.EvaluationStatus
import emy.partners.lawapp.presentation.pages.ProfilPage
import emy.partners.lawapp.presentation.pages.explore.ExploreDetailPage
import emy.partners.lawapp.presentation.pages.explore.ExplorePage
import emy.partners.lawapp.presentation.pages.home.HomePage
import emy.partners.lawapp.presentation.pages.session.EvaluationCreatePage
import emy.partners.lawapp.presentation.pages.session.EvaluationDetailPage
import emy.partners.lawapp.presentation.pages.session.EvaluationPage
import emy.partners.lawapp.presentation.pages.session.QuizPage
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
}

private val LocalLawAppNavigationContext = staticCompositionLocalOf<LawAppNavigationContext> {
    error("No LawAppNavigationContext provided")
}

private class LawAppState(
    val createdEvaluations: SnapshotStateList<EvaluationSession>,
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
        HomePage(Modifier.padding(bottom = context.contentPadding.calculateBottomPadding()))
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
        val scrollVertical = rememberPageScrollState(pageStateKey, topLevelDestinationKind)

        QuizPage(
            modifier = Modifier.padding(top = context.contentPadding.calculateTopPadding()),
            scrollVertical = scrollVertical,
        )
    }
}

private class ProfileScreen : UniqueLawAppScreen() {
    override val topLevelDestinationKind: TopLevelDestinationKind = TopLevelDestinationKind.Profile
    override val pageStateKey: String = "profile"

    @Composable
    override fun Content() {
        val context = LocalLawAppNavigationContext.current
        val scrollVertical = rememberPageScrollState(pageStateKey, topLevelDestinationKind)

        ProfilPage(
            modifier = Modifier.padding(top = context.contentPadding.calculateTopPadding()),
            scrollVertical = scrollVertical,
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
        onBlogClick = { navigator.push(ExploreDetailScreen(it.id)) },
    )
}

@Composable
private fun EvaluationRootContent(pageStateKey: String) {
    val context = LocalLawAppNavigationContext.current
    val navigator = LocalNavigator.currentOrThrow
    val scrollVertical = rememberPageScrollState(pageStateKey, TopLevelDestinationKind.Evaluation)

    EvaluationPage(
        evaluations = context.evaluations,
        modifier = Modifier.padding(top = context.contentPadding.calculateTopPadding()),
        onEvaluationClick = { navigator.push(EvaluationDetailScreen(it.id)) },
        onCreateClick = { navigator.push(EvaluationCreateScreen()) },
        scrollVertical = scrollVertical,
    )
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
    val appState = remember(createdEvaluations) { LawAppState(createdEvaluations) }
    val defaultTopBarScrollState = rememberScrollState()
    val topLevelDestinations = listOf(
        TopLevelDestination(TopLevelDestinationKind.Home, ::HomeScreen, stringResource(Res.string.house), Res.drawable.house),
        TopLevelDestination(TopLevelDestinationKind.Explore, ::ExploreScreen, stringResource(Res.string.discovery), Res.drawable.explore),
        TopLevelDestination(TopLevelDestinationKind.Evaluation, ::EvaluationScreen, stringResource(Res.string.session), Res.drawable.evaluation),
        TopLevelDestination(TopLevelDestinationKind.Quiz, ::QuizScreen, stringResource(Res.string.quiz), Res.drawable.quiz),
        TopLevelDestination(TopLevelDestinationKind.Profile, ::ProfileScreen, stringResource(Res.string.profil), Res.drawable.profil_user),
    )
    MaterialTheme(typography = tekoTypography()) {
        Navigator(HomeScreen()) { navigator ->
            val topBarScrollState = appState.currentPageState?.scrollState ?: defaultTopBarScrollState
            Scaffold(
                //            contentWindowInsets = WindowInsets(0),
                bottomBar = {
                    //CompositionLocalProvider(LocalRippleConfiguration provides null){
                    //Color(0xFF242D2C)
                    val selectedTopLevel = (navigator.lastItem as? LawAppScreen)
                        ?.topLevelDestinationKind
                        ?: TopLevelDestinationKind.Home
                    Box(modifier = Modifier.clip(RoundedCornerShape(9.dp)).liquefiable(liquidState2)){
                        BottomAppBar(containerColor =  Color.White.copy(alpha = 0.5f),modifier = Modifier.background(
                            Color.White.copy(alpha = 0.5f)
                        )) {
                            topLevelDestinations.forEach { destination ->
                                NavigationBarItem(
                                    interactionSource = remember { MutableInteractionSource() },
                                    colors =  NavigationBarItemDefaults.colors(
                                        indicatorColor =  Color.White.copy(alpha = 0.65f),
                                        selectedTextColor = Color(0xFf2563EB),
                                        selectedIconColor = Color(0xFf2563EB),
                                        unselectedIconColor = Color.Black.copy(0.6f),
                                        unselectedTextColor = Color.Black.copy(0.6f),
                                    ),
                                    selected = destination.kind == selectedTopLevel,
                                    onClick = { navigator.replaceAll(destination.createScreen()) },
                                    icon = {
                                        Icon(
                                            painter = painterResource(destination.icon),null, modifier = Modifier.size(28.dp),
                                        )
                                    },
                                    label = {
                                        Text(destination.name, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }
                                )
                            }
                        }
                    }

                    //}
                },
                topBar = {TopBarCustom(topBarScrollState)}
                //            contentWindowInsets = WindowInsets(0, 0, 0, 0) // Désactive les insets par défaut
            ) {

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .liquefiable(liquidState)
                ) {
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

                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .liquid(liquidState)
                            .background(
                                Color.White.copy(alpha = 0.15f)
                            )
                    )
                    val navigationContext = LawAppNavigationContext(
                        contentPadding = it,
                        state = appState,
                    )
                    Column {
                        CompositionLocalProvider(LocalLawAppNavigationContext provides navigationContext) {
                            CurrentScreen()
                        }
                    }
                }



            }
        }
    }
}

@Composable
expect fun PlatformVideoPlayer(url: String, modifier: Modifier, isPlaying: Boolean)

private fun EvaluationDAO.toSession(index: Int) = EvaluationSession(
    id = id ?: (1_000L + index),
    title = title,
    domain = "Brouillon",
    description = description,
    status = EvaluationStatus.InProgress,
    progress = 0f,
    score = null,
    questionCount = compteur?.toInt() ?: 0,
    completedQuestions = 0,
    duration = "A definir",
    updatedAt = "Cree maintenant",
    level = "Personnalise"
)