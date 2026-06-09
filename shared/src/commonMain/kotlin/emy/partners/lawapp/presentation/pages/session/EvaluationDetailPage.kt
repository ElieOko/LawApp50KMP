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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import emy.partners.lawapp.domain.models.EvaluationSession
import emy.partners.lawapp.domain.models.EvaluationStatus
import emy.partners.lawapp.presentation.themes.BlueDark
import emy.partners.lawapp.presentation.themes.BlueDarkEffect

@Composable
fun EvaluationDetailPage(
    evaluation: EvaluationSession,
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    onStartQuiz: () -> Unit = {},
    scrollVertical: ScrollState = rememberScrollState()
) {
    EvaluationDetailBuild(
        evaluation = evaluation,
        modifier = modifier,
        onBack = onBack,
        onStartQuiz = onStartQuiz,
        scrollVertical
    )
}

@Composable
fun EvaluationDetailBuild(
    evaluation: EvaluationSession,
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    onStartQuiz: () -> Unit = {},
    scrollVertical: ScrollState = rememberScrollState()
) {
    val completed = evaluation.status == EvaluationStatus.Completed
    val remaining = evaluation.questionCount - evaluation.completedQuestions

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(scrollVertical)
            .padding(16.dp)
    ) {
        Column(modifier) {
            Text(
                text = "< Retour aux evaluations",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .clip(RoundedCornerShape(30.dp))
                    .background(Color.White.copy(alpha = 0.14f))
                    .clickable(onClick = onBack)
                    .padding(horizontal = 14.dp, vertical = 9.dp)
            )
            Spacer(Modifier.height(14.dp))
            Box(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(32.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                BlueDark.copy(alpha = 0.95f),
                                Color(0xFF0F172A).copy(alpha = 0.94f)
                            )
                        )
                    )
                    .padding(20.dp)
            ) {
                Column {
                    Text(evaluation.domain, color = Color.White.copy(alpha = 0.7f), fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = evaluation.title,
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 28.sp,
                        lineHeight = 31.sp
                    )
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = evaluation.description,
                        color = Color.White.copy(alpha = 0.75f),
                        lineHeight = 20.sp
                    )
                    Spacer(Modifier.height(22.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        DetailMetric(
                            label = if (completed) "Score final" else "Progression",
                            value = if (completed) "${evaluation.score}%" else "${(evaluation.progress * 100).toInt()}%",
                            modifier = Modifier.weight(1f)
                        )
                        DetailMetric(
                            label = "Compteur",
                            value = if (completed) "0 restant" else "$remaining restant",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
            Spacer(Modifier.height(14.dp))
            Card(
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.9f))
            ) {
                Column(Modifier.padding(18.dp)) {
                    Text(
                        text = "Parcours",
                        color = Color.Black.copy(alpha = 0.86f),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 21.sp
                    )
                    Spacer(Modifier.height(12.dp))
                    LinearProgressIndicator(
                        progress = { evaluation.progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(50.dp)),
                        color = if (completed) Color(0xFF10B981) else Color(0xFF2563EB),
                        trackColor = Color.Black.copy(alpha = 0.08f)
                    )
                    Spacer(Modifier.height(14.dp))
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        DetailLine("Niveau", evaluation.level)
                        DetailLine("Questions", "${evaluation.completedQuestions}/${evaluation.questionCount}")
                        DetailLine("Duree", evaluation.duration)
                    }
                    Spacer(Modifier.height(18.dp))
                    Text(
                        text = if (completed) {
                            "Evaluation terminee. Consulte ton score, puis relance un quiz pour consolider tes acquis."
                        } else {
                            "Tu peux reprendre exactement ou tu t'es arrete. Le compteur t'indique les questions restantes."
                        },
                        color = Color.Black.copy(alpha = 0.56f),
                        lineHeight = 19.sp
                    )
                    Spacer(Modifier.height(18.dp))
                    Button(
                        onClick = onStartQuiz,
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BlueDark)
                    ) {
                        Text(
                            text = if (completed) "Reviser avec un quiz" else "Continuer l'evaluation",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            Spacer(Modifier.height(14.dp))
            Text(
                text = "Recommandations",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 21.sp
            )
            Spacer(Modifier.height(10.dp))
            RecommendationCard("Relis les fiches de ${evaluation.domain}", "Priorite elevee")
            Spacer(Modifier.height(10.dp))
            RecommendationCard("Fais un quiz chronometre de 5 questions", "Entrainement")
            Spacer(Modifier.height(90.dp))
        }
    }
}

@Composable
private fun DetailMetric(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier
            .clip(RoundedCornerShape(22.dp))
            .background(Color.White.copy(alpha = 0.14f))
            .padding(14.dp)
    ) {
        Text(value, color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 24.sp)
        Text(label, color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
    }
}

@Composable
private fun DetailLine(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = Color.Black.copy(alpha = 0.42f), fontSize = 11.sp)
        Text(value, color = Color.Black.copy(alpha = 0.85f), fontWeight = FontWeight.Bold, fontSize = 13.sp)
    }
}

@Composable
private fun RecommendationCard(title: String, tag: String) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White.copy(alpha = 0.16f))
            .padding(14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, color = Color.White, fontWeight = FontWeight.Bold)
            Text(tag, color = Color.White.copy(alpha = 0.62f), fontSize = 12.sp)
        }
        Spacer(Modifier.width(12.dp))
        Text(">", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 22.sp)
    }
}

@Composable
@Preview(showBackground = true)
fun EvaluationDetailPreview() {
    EvaluationDetailBuild(Constants.evaluations.first())
}
