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
import emy.partners.lawapp.domain.models.EvaluationSession
import emy.partners.lawapp.presentation.pages.auth.AuthColors
import emy.partners.lawapp.presentation.pages.auth.AuthFormPanel

@Composable
fun EvaluationCorrectionPage(
    evaluation: EvaluationSession,
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    scrollVertical: ScrollState = rememberScrollState(),
) {
    val answerKey = remember(evaluation.id) { EvaluationRepository.correctionAnswerKey(evaluation.id) }
    val questions = remember(evaluation.id) { EvaluationRepository.correctionQuestions(evaluation.id) }

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
            Text("Correction", color = AuthColors.TextPrimary, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp)
            Spacer(Modifier.height(6.dp))
            Text(evaluation.title, color = AuthColors.TextPrimary, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text(
                "Corrige a partir du bareme serveur (options valides). Les copies etudiants ne sont pas exposees par l'API actuelle.",
                color = AuthColors.TextSecondary,
                fontSize = 13.sp,
            )
            if (!evaluation.accessCode.isNullOrBlank()) {
                Spacer(Modifier.height(10.dp))
                Text(
                    "Code d'acces : ${evaluation.accessCode}",
                    color = AuthColors.AccentBright,
                    fontWeight = FontWeight.ExtraBold,
                )
            }
        }
        Spacer(Modifier.height(14.dp))
        AuthFormPanel {
            Text("Bareme QCM", color = AuthColors.TextPrimary, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
            Spacer(Modifier.height(10.dp))
            if (answerKey.isEmpty() && questions.isEmpty()) {
                Text(
                    "Aucune question avec bonne reponse n'est disponible sur ce detail. Ouvrez le passage ou republiez l'evaluation.",
                    color = AuthColors.TextSecondary,
                )
            } else {
                answerKey.forEachIndexed { index, (question, correct) ->
                    Column(
                        Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFFF8FAFC))
                            .padding(12.dp)
                    ) {
                        Text(
                            "${index + 1}. ${question.title}",
                            fontWeight = FontWeight.ExtraBold,
                            color = AuthColors.TextPrimary,
                        )
                        question.options.forEach { option ->
                            val isCorrect = correct != null && option.label == correct
                            Text(
                                (if (isCorrect) "✓ " else "• ") + option.label,
                                color = if (isCorrect) Color(0xFF059669) else AuthColors.TextSecondary,
                                fontSize = 13.sp,
                            )
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }
                if (answerKey.isEmpty()) {
                    questions.forEachIndexed { index, question ->
                        Text(
                            "${index + 1}. ${question.title}",
                            fontWeight = FontWeight.SemiBold,
                            color = AuthColors.TextPrimary,
                        )
                        Spacer(Modifier.height(6.dp))
                    }
                }
            }
        }
        Spacer(Modifier.height(80.dp))
    }
}
