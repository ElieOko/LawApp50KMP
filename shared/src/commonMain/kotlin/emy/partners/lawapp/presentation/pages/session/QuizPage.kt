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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import emy.partners.lawapp.data.Constants
import emy.partners.lawapp.domain.models.QuizQuestion
import emy.partners.lawapp.presentation.themes.BlueDark
import emy.partners.lawapp.presentation.themes.BlueDarkEffect

@Composable
fun QuizPage(
    modifier: Modifier = Modifier,
    scrollVertical: ScrollState = rememberScrollState()
) {
    QuizBuild(modifier, scrollVertical)
}

@Composable
fun QuizBuild(
    modifier: Modifier = Modifier,
    scrollVertical: ScrollState = rememberScrollState()
) {
    val questions = remember { Constants.quizQuestions }
    val currentIndex = remember { mutableIntStateOf(0) }
    val answers = remember { mutableStateMapOf<Long, Int>() }
    val question = questions[currentIndex.intValue]
    val selectedIndex = answers[question.id]
    val score = answers.count { (questionId, answerIndex) ->
        questions.first { it.id == questionId }.correctIndex == answerIndex
    }
    val progress = (answers.size.toFloat() / questions.size).coerceIn(0f, 1f)

    Column(
        modifier
            .fillMaxSize()
            .verticalScroll(scrollVertical)
            .padding(16.dp)
    ) {
        QuizHeader(
            answered = answers.size,
            total = questions.size,
            score = score,
            progress = progress
        )
        Spacer(Modifier.height(16.dp))
        QuestionCard(
            question = question,
            current = currentIndex.intValue + 1,
            total = questions.size,
            selectedIndex = selectedIndex,
            onSelect = { answers[question.id] = it }
        )
        Spacer(Modifier.height(14.dp))
        Button(
            onClick = {
                currentIndex.intValue = if (currentIndex.intValue == questions.lastIndex) {
                    0
                } else {
                    currentIndex.intValue + 1
                }
            },
            modifier = Modifier.fillMaxWidth().height(54.dp),
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = BlueDark)
        ) {
            Text(
                text = if (currentIndex.intValue == questions.lastIndex) "Recommencer le tour" else "Question suivante",
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(Modifier.height(90.dp))
    }
}

@Composable
private fun QuizHeader(
    answered: Int,
    total: Int,
    score: Int,
    progress: Float
) {
    Box(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(32.dp))
            .background(
                Brush.linearGradient(
                    listOf(
                        Color(0xFF2563EB).copy(alpha = 0.92f),
                        BlueDarkEffect.copy(alpha = 0.92f)
                    )
                )
            )
            .padding(20.dp)
    ) {
        Column {
            Text(
                "Quiz juridique",
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Spacer(Modifier.height(6.dp))
            Text(
                "Entraine-toi avec des questions courtes et un feedback direct.",
                color = Color.White.copy(alpha = 0.72f)
            )
            Spacer(Modifier.height(18.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(9.dp).clip(RoundedCornerShape(40.dp)),
                color = Color.White,
                trackColor = Color.White.copy(alpha = 0.18f)
            )
            Spacer(Modifier.height(14.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                QuizMetric("Repondu", "$answered/$total", Modifier.weight(1f))
                QuizMetric("Score", "$score/$total", Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun QuizMetric(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White.copy(alpha = 0.14f))
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(value, color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp)
        Text(label, color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
    }
}

@Composable
private fun QuestionCard(
    question: QuizQuestion,
    current: Int,
    total: Int,
    selectedIndex: Int?,
    onSelect: (Int) -> Unit
) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(30.dp))
            .background(Color.White.copy(alpha = 0.9f))
            .padding(18.dp)
    ) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(question.category, color = BlueDark, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Text("$current/$total", color = Color.Black.copy(alpha = 0.45f), fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(12.dp))
        Text(
            question.title,
            color = Color.Black.copy(alpha = 0.88f),
            fontWeight = FontWeight.ExtraBold,
            fontSize = 24.sp,
            lineHeight = 28.sp
        )
        Spacer(Modifier.height(18.dp))
        question.options.forEachIndexed { index, option ->
            AnswerOption(
                label = option,
                index = index,
                correctIndex = question.correctIndex,
                selectedIndex = selectedIndex,
                onClick = { onSelect(index) }
            )
            Spacer(Modifier.height(10.dp))
        }
        if (selectedIndex != null) {
            Spacer(Modifier.height(6.dp))
            Text(
                text = if (selectedIndex == question.correctIndex) "Bonne reponse" else "A revoir",
                color = if (selectedIndex == question.correctIndex) Color(0xFF10B981) else Color(0xFFEF4444),
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = question.explanation,
                color = Color.Black.copy(alpha = 0.58f),
                fontSize = 13.sp,
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
private fun AnswerOption(
    label: String,
    index: Int,
    correctIndex: Int,
    selectedIndex: Int?,
    onClick: () -> Unit
) {
    val wasSelected = selectedIndex == index
    val hasAnswer = selectedIndex != null
    val color = when {
        hasAnswer && index == correctIndex -> Color(0xFF10B981)
        wasSelected -> Color(0xFFEF4444)
        else -> Color.White
    }
    val contentColor = when {
        hasAnswer && index == correctIndex -> Color.White
        wasSelected -> Color.White
        else -> Color.Black.copy(alpha = 0.76f)
    }

    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(color)
            .clickable(enabled = !hasAnswer, onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(30.dp)
                .clip(RoundedCornerShape(50))
                .background(if (hasAnswer) Color.White.copy(alpha = 0.2f) else BlueDark.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Text(('A' + index).toString(), color = contentColor, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.size(12.dp))
        Text(label, color = contentColor, fontWeight = FontWeight.Bold, lineHeight = 18.sp)
    }
}

@Composable
@Preview(showBackground = true)
fun QuizPreview() {
    QuizBuild()
}
