package emy.partners.lawapp

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import emy.partners.lawapp.data.Constants
import emy.partners.lawapp.presentation.components.basics.TopBarCustom
import emy.partners.lawapp.domain.models.Blog
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


data class Parent(
    val id : Int,
    val name : String,
    var isActive : Boolean = false,
    val icon : DrawableResource
)
@Composable
@Preview(showBackground = true)
fun App() {
    val liquidState = rememberLiquidState()
    val liquidState2 = rememberLiquidState()
    val scrollVertical = rememberScrollState()
    val selectedBlog = remember { mutableStateOf<Blog?>(null) }
    val selectedEvaluation = remember { mutableStateOf<EvaluationSession?>(null) }
    val isCreatingEvaluation = remember { mutableStateOf(false) }
    val createdEvaluations = remember { mutableStateListOf<EvaluationSession>() }
    val listParent = listOf<Parent>(
        Parent(1, stringResource(Res.string.house), icon = Res.drawable.house),
        Parent(2,stringResource(Res.string.discovery), icon = Res.drawable.explore),
        Parent(3,stringResource(Res.string.session), icon = Res.drawable.evaluation),
        Parent(4,stringResource(Res.string.quiz), icon = Res.drawable.quiz),
        Parent(5,stringResource(Res.string.profil), icon = Res.drawable.profil_user),
    )
    val state = remember { mutableIntStateOf(0) }
    MaterialTheme {
        Scaffold(
//            contentWindowInsets = WindowInsets(0),
            bottomBar = {
                //CompositionLocalProvider(LocalRippleConfiguration provides null){
                //Color(0xFF242D2C)
                Box(modifier = Modifier.clip(RoundedCornerShape(9.dp)).liquefiable(liquidState2)){
                    BottomAppBar(containerColor =  Color.White.copy(alpha = 0.5f),modifier = Modifier.background(
                            Color.White.copy(alpha = 0.5f)
                        )) {
                        listParent.forEachIndexed { i, parent ->
                            NavigationBarItem(
                                interactionSource = remember { MutableInteractionSource() },
                                colors =  NavigationBarItemDefaults.colors(
                                    indicatorColor =  Color.White.copy(alpha = 0.65f),
                                    selectedTextColor = Color(0xFf2563EB),
                                    selectedIconColor = Color(0xFf2563EB),
                                    unselectedIconColor = Color.Black.copy(0.6f),
                                    unselectedTextColor = Color.Black.copy(0.6f),
                                ),
                                selected = i == state.intValue,
                                onClick = {
                                    state.intValue = i
                                    selectedBlog.value = null
                                    selectedEvaluation.value = null
                                    isCreatingEvaluation.value = false
                                },
                                icon = {
                                    Icon(
                                        painter = painterResource(parent.icon),null, modifier = Modifier.size(28.dp),
                                    )
                                },
                                label = {
                                    Text(parent.name, fontWeight = FontWeight.Bold, fontSize = 12.sp)
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
                Column {
                    when (state.intValue) {
                        0 -> HomePage(Modifier.padding(bottom = it.calculateBottomPadding()))
                        1 -> {
                            val blog = selectedBlog.value
                            if (blog == null) {
                                ExplorePage(
                                    modifier = Modifier.padding(
                                        top = it.calculateTopPadding(),
                                        bottom = it.calculateBottomPadding()
                                    ),
                                    scrollVertical = scrollVertical,
                                    onBlogClick = { selectedBlog.value = it }
                                )
                            } else {
                                ExploreDetailPage(
                                    blog = blog,
                                    modifier = Modifier.padding(
                                        top = it.calculateTopPadding(),
                                        bottom = it.calculateBottomPadding()
                                    ),
                                    onBack = { selectedBlog.value = null }
                                )
                            }
                        }
                        2 -> {
                            val evaluation = selectedEvaluation.value
                            if (isCreatingEvaluation.value) {
                                EvaluationCreatePage(
                                    modifier = Modifier.padding(
                                        top = it.calculateTopPadding(),
                                        bottom = it.calculateBottomPadding()
                                    ),
                                    onBack = { isCreatingEvaluation.value = false },
                                    onSave = { evaluation ->
                                        createdEvaluations.add(evaluation.toSession(createdEvaluations.size))
                                        isCreatingEvaluation.value = false
                                    }
                                )
                            } else if (evaluation == null) {
                                EvaluationPage(
                                    evaluations = Constants.evaluations + createdEvaluations,
                                    modifier = Modifier.padding(
                                        top = it.calculateTopPadding(),
                                        bottom = it.calculateBottomPadding()
                                    ),
                                    onEvaluationClick = { selectedEvaluation.value = it },
                                    onCreateClick = { isCreatingEvaluation.value = true }
                                )
                            } else {
                                EvaluationDetailPage(
                                    evaluation = evaluation,
                                    modifier = Modifier.padding(
                                        top = it.calculateTopPadding(),
                                        bottom = it.calculateBottomPadding()
                                    ),
                                    onBack = { selectedEvaluation.value = null },
                                    onStartQuiz = {
                                        selectedEvaluation.value = null
                                        state.intValue = 3
                                    }
                                )
                            }
                        }
                        3 -> QuizPage(
                            Modifier.padding(
                                top = it.calculateTopPadding(),
                                bottom = it.calculateBottomPadding()
                            )
                        )
                        4 -> ProfilPage(
                            Modifier.padding(
                                top = it.calculateTopPadding(),
                                bottom = it.calculateBottomPadding()
                            )
                        )
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