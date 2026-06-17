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
    val scrollVertical: ScrollState,
    val createdEvaluations: SnapshotStateList<EvaluationSession>,
) {
    val evaluations: List<EvaluationSession>
        get() = Constants.evaluations + createdEvaluations
}

private val LocalLawAppNavigationContext = staticCompositionLocalOf<LawAppNavigationContext> {
    error("No LawAppNavigationContext provided")
}

private interface LawAppScreen : Screen {
    val topLevelDestinationKind: TopLevelDestinationKind
}

private abstract class UniqueLawAppScreen : LawAppScreen {
    override val key: ScreenKey = uniqueScreenKey
}

private class HomeScreen : UniqueLawAppScreen() {
    override val topLevelDestinationKind: TopLevelDestinationKind = TopLevelDestinationKind.Home

    @Composable
    override fun Content() {
        val context = LocalLawAppNavigationContext.current
        HomePage(Modifier.padding(bottom = context.contentPadding.calculateBottomPadding()))
    }
}

private class ExploreScreen : UniqueLawAppScreen() {
    override val topLevelDestinationKind: TopLevelDestinationKind = TopLevelDestinationKind.Explore

    @Composable
    override fun Content() {
        ExploreRootContent()
    }
}

private data class ExploreDetailScreen(val blogId: Long) : UniqueLawAppScreen() {
    override val topLevelDestinationKind: TopLevelDestinationKind = TopLevelDestinationKind.Explore

    @Composable
    override fun Content() {
        val context = LocalLawAppNavigationContext.current
        val navigator = LocalNavigator.currentOrThrow
        val blog = Constants.blog.firstOrNull { it.id == blogId }

        if (blog == null) {
            ExploreRootContent()
            return
        }

        ExploreDetailPage(
            blog = blog,
            modifier = Modifier.padding(top = context.contentPadding.calculateTopPadding()),
            onBack = { navigator.pop() },
            scrollVertical = context.scrollVertical,
        )
    }
}

private class EvaluationScreen : UniqueLawAppScreen() {
    override val topLevelDestinationKind: TopLevelDestinationKind = TopLevelDestinationKind.Evaluation

    @Composable
    override fun Content() {
        EvaluationRootContent()
    }
}

private data class EvaluationDetailScreen(val evaluationId: Long) : UniqueLawAppScreen() {
    override val topLevelDestinationKind: TopLevelDestinationKind = TopLevelDestinationKind.Evaluation

    @Composable
    override fun Content() {
        val context = LocalLawAppNavigationContext.current
        val navigator = LocalNavigator.currentOrThrow
        val evaluation = context.evaluations.firstOrNull { it.id == evaluationId }

        if (evaluation == null) {
            EvaluationRootContent()
            return
        }

        EvaluationDetailPage(
            evaluation = evaluation,
            modifier = Modifier.padding(top = context.contentPadding.calculateTopPadding()),
            onBack = { navigator.pop() },
            onStartQuiz = { navigator.replaceAll(QuizScreen()) },
            scrollVertical = context.scrollVertical,
        )
    }
}

private class EvaluationCreateScreen : UniqueLawAppScreen() {
    override val topLevelDestinationKind: TopLevelDestinationKind = TopLevelDestinationKind.Evaluation

    @Composable
    override fun Content() {
        val context = LocalLawAppNavigationContext.current
        val navigator = LocalNavigator.currentOrThrow

        EvaluationCreatePage(
            modifier = Modifier.padding(top = context.contentPadding.calculateTopPadding()),
            onBack = { navigator.pop() },
            onSave = { evaluation ->
                context.createdEvaluations.add(evaluation.toSession(context.createdEvaluations.size))
                navigator.pop()
            },
            scrollVertical = context.scrollVertical,
        )
    }
}

private class QuizScreen : UniqueLawAppScreen() {
    override val topLevelDestinationKind: TopLevelDestinationKind = TopLevelDestinationKind.Quiz

    @Composable
    override fun Content() {
        val context = LocalLawAppNavigationContext.current

        QuizPage(
            modifier = Modifier.padding(top = context.contentPadding.calculateTopPadding()),
            scrollVertical = context.scrollVertical,
        )
    }
}

private class ProfileScreen : UniqueLawAppScreen() {
    override val topLevelDestinationKind: TopLevelDestinationKind = TopLevelDestinationKind.Profile

    @Composable
    override fun Content() {
        val context = LocalLawAppNavigationContext.current

        ProfilPage(
            modifier = Modifier.padding(top = context.contentPadding.calculateTopPadding()),
            scrollVertical = context.scrollVertical,
        )
    }
}

@Composable
private fun ExploreRootContent() {
    val context = LocalLawAppNavigationContext.current
    val navigator = LocalNavigator.currentOrThrow

    ExplorePage(
        modifier = Modifier.padding(top = context.contentPadding.calculateTopPadding()),
        scrollVertical = context.scrollVertical,
        onBlogClick = { navigator.push(ExploreDetailScreen(it.id)) },
    )
}

@Composable
private fun EvaluationRootContent() {
    val context = LocalLawAppNavigationContext.current
    val navigator = LocalNavigator.currentOrThrow

    EvaluationPage(
        evaluations = context.evaluations,
        modifier = Modifier.padding(top = context.contentPadding.calculateTopPadding()),
        onEvaluationClick = { navigator.push(EvaluationDetailScreen(it.id)) },
        onCreateClick = { navigator.push(EvaluationCreateScreen()) },
        scrollVertical = context.scrollVertical,
    )
}

@Composable
@Preview(showBackground = true)
fun App() {
    val liquidState = rememberLiquidState()
    val liquidState2 = rememberLiquidState()
    val scrollVertical = rememberScrollState()
    val createdEvaluations = remember { mutableStateListOf<EvaluationSession>() }
    val topLevelDestinations = listOf(
        TopLevelDestination(TopLevelDestinationKind.Home, ::HomeScreen, stringResource(Res.string.house), Res.drawable.house),
        TopLevelDestination(TopLevelDestinationKind.Explore, ::ExploreScreen, stringResource(Res.string.discovery), Res.drawable.explore),
        TopLevelDestination(TopLevelDestinationKind.Evaluation, ::EvaluationScreen, stringResource(Res.string.session), Res.drawable.evaluation),
        TopLevelDestination(TopLevelDestinationKind.Quiz, ::QuizScreen, stringResource(Res.string.quiz), Res.drawable.quiz),
        TopLevelDestination(TopLevelDestinationKind.Profile, ::ProfileScreen, stringResource(Res.string.profil), Res.drawable.profil_user),
    )
    MaterialTheme {
        Navigator(HomeScreen()) { navigator ->
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
                topBar = {TopBarCustom(scrollVertical)}
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
                        scrollVertical = scrollVertical,
                        createdEvaluations = createdEvaluations,
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