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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import emy.partners.lawapp.data.remote.quiz.QuizPlayContent
import emy.partners.lawapp.data.remote.quiz.QuizRepository
import emy.partners.lawapp.domain.models.QuizQuestion
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
fun QuizPlayPage(
    quizId: Long,
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    scrollVertical: ScrollState = rememberScrollState(),
) {
    val ui = LocalAppUiController.current
    val pageBg = if (ui.settings.darkMode) PageBgDark else PageBgLight
    val scope = rememberCoroutineScope()
    var content by remember { mutableStateOf<QuizPlayContent?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    val currentLevelIndex = remember { mutableIntStateOf(0) }
    val currentQuestionIndex = remember { mutableIntStateOf(0) }
    val answers = remember { mutableStateMapOf<Long, Int>() }

    LaunchedEffect(quizId) {
        isLoading = true
        QuizRepository.getQuizDetail(quizId)
            .onSuccess {
                content = it
                errorMessage = null
            }
            .onFailure {
                errorMessage = it.message ?: "Impossible d'ouvrir ce quiz"
            }
        isLoading = false
    }

    Column(
        modifier
            .fillMaxSize()
            .background(pageBg)
            .verticalScroll(scrollVertical)
            .padding(horizontal = 14.dp)
            .padding(top = 8.dp, bottom = 96.dp)
    ) {
        Text(
            text = "< Retour aux quiz",
            color = AuthColors.TextPrimary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .clip(RoundedCornerShape(30.dp))
                .background(Color.White)
                .clickable(onClick = onBack)
                .padding(horizontal = 14.dp, vertical = 9.dp)
        )
        Spacer(Modifier.height(14.dp))

        when {
            isLoading -> {
                Box(Modifier.fillMaxWidth().height(220.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = AuthColors.AccentBright)
                }
            }
            errorMessage != null -> {
                AuthFormPanel {
                    Text(errorMessage.orEmpty(), color = AuthColors.TextPrimary, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(12.dp))
                    AuthPrimaryButton(text = "Retour", onClick = onBack)
                }
            }
            content == null || content!!.levels.isEmpty() -> {
                AuthFormPanel {
                    Text("Ce quiz n'a pas encore de questions.", color = AuthColors.TextPrimary)
                }
            }
            else -> {
                val quiz = content!!
                val level = quiz.levels[currentLevelIndex.intValue.coerceIn(0, quiz.levels.lastIndex)]
                val question = level.questions[currentQuestionIndex.intValue.coerceIn(0, level.questions.lastIndex)]
                val selectedIndex = answers[question.id]
                val answeredInLevel = level.questions.count { answers.containsKey(it.id) }
                val score = level.questions.count { item ->
                    answers[item.id] == item.correctIndex
                }
                val progress = (answeredInLevel.toFloat() / level.questions.size).coerceIn(0f, 1f)
                val isLastQuestion = currentQuestionIndex.intValue == level.questions.lastIndex
                val isLastLevel = currentLevelIndex.intValue == quiz.levels.lastIndex

                QuizPlayHeader(
                    title = quiz.title,
                    levelTitle = level.title,
                    answered = answeredInLevel,
                    total = level.questions.size,
                    score = score,
                    progress = progress,
                )
                Spacer(Modifier.height(14.dp))
                AuthFormPanel {
                    QuestionCard(
                        question = question,
                        current = currentQuestionIndex.intValue + 1,
                        total = level.questions.size,
                        selectedIndex = selectedIndex,
                        onSelect = { answers[question.id] = it },
                    )
                    Spacer(Modifier.height(14.dp))
                    if (!statusMessage.isNullOrBlank()) {
                        Text(
                            statusMessage.orEmpty(),
                            color = AuthColors.AccentBright,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                        )
                        Spacer(Modifier.height(10.dp))
                    }
                    AuthPrimaryButton(
                        text = when {
                            !isLastQuestion -> "Question suivante"
                            !isLastLevel -> "Niveau suivant"
                            else -> "Terminer le quiz"
                        },
                        enabled = selectedIndex != null,
                        onClick = {
                            if (!isLastQuestion) {
                                currentQuestionIndex.intValue += 1
                                return@AuthPrimaryButton
                            }
                            val levelScore = score.toDouble()
                            scope.launch {
                                QuizRepository.recordLevelProgress(
                                    quizId = quiz.id,
                                    levelOrder = level.levelOrder,
                                    score = levelScore,
                                ).onSuccess {
                                    statusMessage = "Niveau ${level.levelOrder} enregistre"
                                }.onFailure {
                                    statusMessage = it.message ?: "Progression non enregistree"
                                }
                                if (!isLastLevel) {
                                    currentLevelIndex.intValue += 1
                                    currentQuestionIndex.intValue = 0
                                }
                            }
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun QuizPlayHeader(
    title: String,
    levelTitle: String,
    answered: Int,
    total: Int,
    score: Int,
    progress: Float,
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
        Text(levelTitle, color = Color.White.copy(alpha = 0.75f), fontWeight = FontWeight.Bold, fontSize = 13.sp)
        Text(title, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
        Spacer(Modifier.height(16.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(9.dp)
                .clip(RoundedCornerShape(40.dp)),
            color = Color.White,
            trackColor = Color.White.copy(alpha = 0.18f),
        )
        Spacer(Modifier.height(14.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            QuizPlayMetric("Repondu", "$answered/$total", Modifier.weight(1f))
            QuizPlayMetric("Score", "$score/$total", Modifier.weight(1f))
        }
    }
}

@Composable
private fun QuizPlayMetric(label: String, value: String, modifier: Modifier = Modifier) {
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
private fun QuestionCard(
    question: QuizQuestion,
    current: Int,
    total: Int,
    selectedIndex: Int?,
    onSelect: (Int) -> Unit,
) {
    Column(Modifier.fillMaxWidth()) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(question.category, color = AuthColors.AccentBright, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Text(
                "$current/$total",
                color = AuthColors.TextSecondary,
                fontWeight = FontWeight.Bold,
            )
        }
        Spacer(Modifier.height(12.dp))
        Text(
            question.title,
            color = AuthColors.TextPrimary,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 22.sp,
            lineHeight = 28.sp,
        )
        Spacer(Modifier.height(16.dp))
        question.options.forEachIndexed { index, option ->
            AnswerOption(
                label = option,
                index = index,
                correctIndex = question.correctIndex,
                selectedIndex = selectedIndex,
                onClick = { onSelect(index) },
            )
            Spacer(Modifier.height(10.dp))
        }
        if (selectedIndex != null) {
            Spacer(Modifier.height(4.dp))
            Text(
                text = if (selectedIndex == question.correctIndex) "Bonne reponse" else "A revoir",
                color = if (selectedIndex == question.correctIndex) Color(0xFF10B981) else Color(0xFFEF4444),
                fontWeight = FontWeight.ExtraBold,
            )
            if (question.explanation.isNotBlank()) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = question.explanation,
                    color = AuthColors.TextSecondary,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                )
            }
        }
    }
}

@Composable
private fun AnswerOption(
    label: String,
    index: Int,
    correctIndex: Int,
    selectedIndex: Int?,
    onClick: () -> Unit,
) {
    val wasSelected = selectedIndex == index
    val hasAnswer = selectedIndex != null
    val color = when {
        hasAnswer && index == correctIndex -> Color(0xFF10B981)
        wasSelected -> Color(0xFFEF4444)
        else -> Color(0xFFF8FAFC)
    }
    val contentColor = when {
        hasAnswer && index == correctIndex -> Color.White
        wasSelected -> Color.White
        else -> AuthColors.TextPrimary
    }

    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(color)
            .clickable(enabled = !hasAnswer, onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .size(30.dp)
                .clip(RoundedCornerShape(50))
                .background(
                    if (hasAnswer) Color.White.copy(alpha = 0.2f) else AuthColors.AccentBright.copy(alpha = 0.12f)
                ),
            contentAlignment = Alignment.Center,
        ) {
            Text(('A' + index).toString(), color = contentColor, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.size(12.dp))
        Text(label, color = contentColor, fontWeight = FontWeight.SemiBold, lineHeight = 18.sp)
    }
}
