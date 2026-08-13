package emy.partners.lawapp.presentation.pages.session

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import emy.partners.lawapp.data.remote.quiz.QuizRepository
import emy.partners.lawapp.data.remote.quiz.QuizSummary
import emy.partners.lawapp.presentation.pages.auth.AuthColors
import emy.partners.lawapp.presentation.pages.auth.AuthFormPanel
import emy.partners.lawapp.presentation.pages.auth.AuthPrimaryButton
import emy.partners.lawapp.presentation.settings.LocalAppUiController
import emy.partners.lawapp.presentation.themes.BlueDark
import emy.partners.lawapp.presentation.themes.BlueDarkEffect
import kotlinx.coroutines.launch

private val PageBgLight = Color(0xFFE8EEF7)
private val PageBgDark = Color(0xFF0B1220)

@Composable
fun QuizPage(
    modifier: Modifier = Modifier,
    onQuizClick: (QuizSummary) -> Unit = {},
    scrollVertical: ScrollState = rememberScrollState(),
) {
    val scope = rememberCoroutineScope()
    var quizzes by remember { mutableStateOf(QuizRepository.cachedQuizzes()) }
    var isInitialLoading by remember { mutableStateOf(quizzes.isEmpty()) }
    var isRefreshing by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        val cached = QuizRepository.cachedQuizzes()
        if (cached.isNotEmpty()) {
            quizzes = cached
            isInitialLoading = false
        }
        val loader = if (cached.isEmpty()) {
            QuizRepository.loadQuizzes()
        } else {
            QuizRepository.refreshQuizzes()
        }
        loader
            .onSuccess {
                quizzes = it
                errorMessage = null
            }
            .onFailure {
                if (quizzes.isEmpty()) {
                    errorMessage = it.message ?: "Impossible de charger les quiz"
                }
            }
        isInitialLoading = false
    }

    QuizBuild(
        modifier = modifier,
        quizzes = quizzes,
        isLoading = isInitialLoading,
        isRefreshing = isRefreshing,
        errorMessage = errorMessage,
        onRetry = {
            isRefreshing = true
            scope.launch {
                QuizRepository.refreshQuizzes()
                    .onSuccess {
                        quizzes = it
                        errorMessage = null
                    }
                    .onFailure {
                        if (quizzes.isEmpty()) {
                            errorMessage = it.message ?: "Impossible de charger les quiz"
                        }
                    }
                isRefreshing = false
            }
        },
        onQuizClick = onQuizClick,
        scrollVertical = scrollVertical,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizBuild(
    modifier: Modifier = Modifier,
    quizzes: List<QuizSummary> = emptyList(),
    isLoading: Boolean = false,
    isRefreshing: Boolean = false,
    errorMessage: String? = null,
    onRetry: () -> Unit = {},
    onQuizClick: (QuizSummary) -> Unit = {},
    scrollVertical: ScrollState = rememberScrollState(),
) {
    val ui = LocalAppUiController.current
    val pageBg = if (ui.settings.darkMode) PageBgDark else PageBgLight
    val pullState = rememberPullToRefreshState()
    val completedCount = quizzes.count { it.completed }

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRetry,
        state = pullState,
        modifier = modifier.fillMaxSize().background(pageBg),
    ) {
        when {
            isLoading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = AuthColors.AccentBright)
                }
            }
            errorMessage != null && quizzes.isEmpty() -> {
                Column(
                    Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(
                        errorMessage,
                        color = AuthColors.TextPrimary,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Spacer(Modifier.height(12.dp))
                    AuthPrimaryButton(text = "Reessayer", onClick = onRetry)
                }
            }
            else -> {
                Column(
                    Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollVertical)
                        .padding(horizontal = 14.dp)
                        .padding(top = 8.dp, bottom = 96.dp)
                ) {
                    QuizListHeader(
                        total = quizzes.size,
                        completed = completedCount,
                    )
                    Spacer(Modifier.height(14.dp))
                    AuthFormPanel {
                        Text(
                            text = "Quiz du serveur",
                            color = AuthColors.TextPrimary,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp,
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = if (quizzes.isEmpty()) {
                                "Aucun quiz n'a encore ete publie."
                            } else {
                                "${quizzes.size} quiz disponible(s)"
                            },
                            color = AuthColors.TextSecondary,
                            fontSize = 13.sp,
                        )
                        Spacer(Modifier.height(14.dp))
                        quizzes.forEachIndexed { index, quiz ->
                            QuizCard(quiz = quiz, onClick = { onQuizClick(quiz) })
                            if (index != quizzes.lastIndex) {
                                Spacer(Modifier.height(10.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuizListHeader(
    total: Int,
    completed: Int,
) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(
                Brush.linearGradient(
                    listOf(
                        Color(0xFF2563EB).copy(alpha = 0.95f),
                        BlueDark.copy(alpha = 0.94f),
                        BlueDarkEffect.copy(alpha = 0.94f),
                    )
                )
            )
            .padding(18.dp)
    ) {
        Text(
            "Quiz juridique",
            color = Color.White,
            fontSize = 26.sp,
            fontWeight = FontWeight.ExtraBold,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            "Les quiz crees sur le serveur, organises par niveaux.",
            color = Color.White.copy(alpha = 0.75f),
            fontSize = 14.sp,
            lineHeight = 19.sp,
        )
        Spacer(Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            QuizMetric("Disponibles", total.toString(), Modifier.weight(1f))
            QuizMetric("Termines", completed.toString(), Modifier.weight(1f))
        }
    }
}

@Composable
private fun QuizMetric(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.14f))
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(value, color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
        Text(label, color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
    }
}

@Composable
private fun QuizCard(
    quiz: QuizSummary,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFFF8FAFC))
            .clickable(onClick = onClick)
            .padding(14.dp)
    ) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = quiz.notionTypeLabel,
                    color = AuthColors.AccentBright,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                )
                Text(
                    text = quiz.title,
                    color = AuthColors.TextPrimary,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 17.sp,
                    lineHeight = 21.sp,
                )
            }
            Text(
                text = if (quiz.completed) "Fait" else "Jouer",
                color = if (quiz.completed) Color(0xFF10B981) else AuthColors.AccentBright,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                modifier = Modifier
                    .clip(RoundedCornerShape(30.dp))
                    .background(
                        (if (quiz.completed) Color(0xFF10B981) else AuthColors.AccentBright)
                            .copy(alpha = 0.12f)
                    )
                    .padding(horizontal = 10.dp, vertical = 6.dp),
            )
        }
        if (quiz.description.isNotBlank()) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = quiz.description,
                color = AuthColors.TextSecondary,
                fontSize = 13.sp,
                lineHeight = 18.sp,
            )
        }
        Spacer(Modifier.height(12.dp))
        LinearProgressIndicator(
            progress = { quiz.progressPercent },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(50.dp)),
            color = if (quiz.completed) Color(0xFF10B981) else AuthColors.AccentBright,
            trackColor = Color.Black.copy(alpha = 0.08f),
        )
        Spacer(Modifier.height(10.dp))
        Text(
            text = if (quiz.totalLevels > 0) {
                "Niveau ${quiz.highestCompletedLevelOrder}/${quiz.totalLevels}"
            } else {
                "Ouvrir le quiz"
            },
            color = AuthColors.TextSecondary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
@Preview(showBackground = true)
fun QuizPreview() {
    QuizBuild(
        quizzes = listOf(
            QuizSummary(
                id = 1,
                title = "Quiz droit civil",
                description = "Contrats et obligations",
                notionTypeId = 1,
                notionTypeLabel = "Droit",
                totalLevels = 3,
                highestCompletedLevelOrder = 1,
                progressPercent = 0.33f,
            )
        )
    )
}