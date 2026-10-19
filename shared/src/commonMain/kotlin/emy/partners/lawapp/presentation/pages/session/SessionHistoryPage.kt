package emy.partners.lawapp.presentation.pages.session

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import emy.partners.lawapp.data.remote.evaluation.EvaluationRepository
import emy.partners.lawapp.data.remote.quiz.QuizRepository
import emy.partners.lawapp.domain.models.EvaluationSession
import emy.partners.lawapp.presentation.pages.auth.AuthColors
import emy.partners.lawapp.presentation.pages.auth.AuthFormPanel

@Composable
fun SessionHistoryPage(
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    onEvaluationClick: (EvaluationSession) -> Unit = {},
    onQuizClick: (Long) -> Unit = {},
    scrollVertical: ScrollState = rememberScrollState(),
) {
    val evaluations = remember { EvaluationRepository.submittedHistory() }
    val quizzes = remember { QuizRepository.history() }

    Column(
        modifier
            .fillMaxSize()
            .background(Color(0xFFE8EEF7))
            .verticalScroll(scrollVertical)
            .padding(14.dp)
    ) {
        Text(
            "< Retour",
            color = AuthColors.AccentBright,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .clip(RoundedCornerShape(30.dp))
                .background(Color.White)
                .clickable(onClick = onBack)
                .padding(horizontal = 14.dp, vertical = 9.dp),
        )
        Spacer(Modifier.height(14.dp))
        AuthFormPanel {
            Text("Historique", color = AuthColors.TextPrimary, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp)
            Spacer(Modifier.height(6.dp))
            Text(
                "Soumissions d'evaluations et progression des quiz enregistrees pour ce profil.",
                color = AuthColors.TextSecondary,
                fontSize = 13.sp,
            )
        }
        Spacer(Modifier.height(14.dp))
        AuthFormPanel {
            Text("Evaluations soumises", color = AuthColors.TextPrimary, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
            Spacer(Modifier.height(10.dp))
            if (evaluations.isEmpty()) {
                Text("Aucune soumission pour le moment.", color = AuthColors.TextSecondary)
            } else {
                evaluations.forEach { session ->
                    Column(
                        Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFFF8FAFC))
                            .clickable { onEvaluationClick(session) }
                            .padding(12.dp)
                    ) {
                        Text(session.title, fontWeight = FontWeight.ExtraBold, color = AuthColors.TextPrimary)
                        Text(
                            "${session.domain} · ${session.completedQuestions}/${session.questionCount} questions",
                            color = AuthColors.TextSecondary,
                            fontSize = 12.sp,
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
        Spacer(Modifier.height(14.dp))
        AuthFormPanel {
            Text("Quiz en cours / termines", color = AuthColors.TextPrimary, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
            Spacer(Modifier.height(10.dp))
            if (quizzes.isEmpty()) {
                Text("Aucun quiz commence.", color = AuthColors.TextSecondary)
            } else {
                quizzes.forEach { quiz ->
                    Column(
                        Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFFF8FAFC))
                            .clickable { onQuizClick(quiz.id) }
                            .padding(12.dp)
                    ) {
                        Text(quiz.title, fontWeight = FontWeight.ExtraBold, color = AuthColors.TextPrimary)
                        Text(
                            "${quiz.notionTypeLabel} · niveau ${quiz.highestCompletedLevelOrder}/${quiz.totalLevels}",
                            color = AuthColors.TextSecondary,
                            fontSize = 12.sp,
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
        Spacer(Modifier.height(80.dp))
    }
}
