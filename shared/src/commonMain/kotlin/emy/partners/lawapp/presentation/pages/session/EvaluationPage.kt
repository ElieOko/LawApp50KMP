package emy.partners.lawapp.presentation.pages.session

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import emy.partners.lawapp.data.Constants
import emy.partners.lawapp.domain.models.EvaluationSession
import emy.partners.lawapp.domain.models.EvaluationStatus
import emy.partners.lawapp.presentation.themes.BlueDark
import emy.partners.lawapp.presentation.themes.BlueDarkEffect

@Composable
fun EvaluationPage(
    modifier: Modifier = Modifier,
    onEvaluationClick: (EvaluationSession) -> Unit = {}
) {
    EvaluationBuild(
        modifier = modifier,
        onEvaluationClick = onEvaluationClick
    )
}

@Composable
fun EvaluationBuild(
    modifier: Modifier = Modifier,
    onEvaluationClick: (EvaluationSession) -> Unit = {}
) {
    var activeFilter by remember { mutableStateOf<EvaluationStatus?>(null) }
    val evaluations = remember { Constants.evaluations }
    val visibleEvaluations = evaluations.filter { activeFilter == null || it.status == activeFilter }
    val completedCount = evaluations.count { it.status == EvaluationStatus.Completed }
    val progressCount = evaluations.count { it.status == EvaluationStatus.InProgress }

    Column(
        modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        EvaluationHero(
            total = evaluations.size,
            completed = completedCount,
            inProgress = progressCount
        )
        Spacer(Modifier.height(18.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            EvaluationFilterChip(
                label = "Tous",
                count = evaluations.size,
                selected = activeFilter == null,
                onClick = { activeFilter = null }
            )
            EvaluationFilterChip(
                label = "En cours",
                count = progressCount,
                selected = activeFilter == EvaluationStatus.InProgress,
                onClick = { activeFilter = EvaluationStatus.InProgress }
            )
            EvaluationFilterChip(
                label = "Terminees",
                count = completedCount,
                selected = activeFilter == EvaluationStatus.Completed,
                onClick = { activeFilter = EvaluationStatus.Completed }
            )
        }
        Spacer(Modifier.height(18.dp))
        Text(
            text = "Mes evaluations",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp
        )
        Spacer(Modifier.height(12.dp))
        visibleEvaluations.forEach { evaluation ->
            EvaluationCard(
                evaluation = evaluation,
                onClick = { onEvaluationClick(evaluation) }
            )
            Spacer(Modifier.height(12.dp))
        }
        Spacer(Modifier.height(90.dp))
    }
}

@Composable
private fun EvaluationHero(
    total: Int,
    completed: Int,
    inProgress: Int
) {
    Box(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(30.dp))
            .background(
                Brush.linearGradient(
                    listOf(
                        BlueDark.copy(alpha = 0.94f),
                        Color(0xFF2563EB).copy(alpha = 0.88f),
                        BlueDarkEffect.copy(alpha = 0.88f)
                    )
                )
            )
            .padding(20.dp)
    ) {
        Column {
            Text(
                text = "Centre d'evaluation",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 26.sp
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Reprends une evaluation en cours ou consulte tes resultats deja termines.",
                color = Color.White.copy(alpha = 0.78f),
                lineHeight = 19.sp
            )
            Spacer(Modifier.height(18.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                EvaluationMetric("Total", total.toString(), Modifier.weight(1f))
                EvaluationMetric("En cours", inProgress.toString(), Modifier.weight(1f))
                EvaluationMetric("Faites", completed.toString(), Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun EvaluationMetric(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White.copy(alpha = 0.16f))
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(value, color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 23.sp)
        Text(label, color = Color.White.copy(alpha = 0.72f), fontSize = 12.sp)
    }
}

@Composable
private fun EvaluationFilterChip(
    label: String,
    count: Int,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        Modifier
            .clip(RoundedCornerShape(50.dp))
            .background(if (selected) Color.White else Color.White.copy(alpha = 0.16f))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = if (selected) BlueDark else Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = count.toString(),
            color = if (selected) Color.White else BlueDark,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .clip(RoundedCornerShape(30.dp))
                .background(if (selected) BlueDark else Color.White)
                .padding(horizontal = 8.dp, vertical = 2.dp),
            fontSize = 12.sp
        )
    }
}

@Composable
private fun EvaluationCard(
    evaluation: EvaluationSession,
    onClick: () -> Unit
) {
    val completed = evaluation.status == EvaluationStatus.Completed
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(26.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.88f))
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        text = evaluation.domain,
                        color = BlueDark,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                    Text(
                        text = evaluation.title,
                        color = Color.Black.copy(alpha = 0.86f),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 19.sp,
                        lineHeight = 22.sp
                    )
                }
                StatusPill(completed = completed)
            }
            Spacer(Modifier.height(10.dp))
            Text(
                text = evaluation.description,
                color = Color.Black.copy(alpha = 0.56f),
                fontSize = 13.sp,
                lineHeight = 18.sp
            )
            Spacer(Modifier.height(14.dp))
            LinearProgressIndicator(
                progress = { evaluation.progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(50.dp)),
                color = if (completed) Color(0xFF10B981) else Color(0xFF2563EB),
                trackColor = Color.Black.copy(alpha = 0.08f)
            )
            Spacer(Modifier.height(12.dp))
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                EvaluationSmallInfo(
                    label = "Questions",
                    value = "${evaluation.completedQuestions}/${evaluation.questionCount}"
                )
                EvaluationSmallInfo(label = "Duree", value = evaluation.duration)
                EvaluationSmallInfo(
                    label = if (completed) "Score" else "Maj",
                    value = if (completed) "${evaluation.score}%" else evaluation.updatedAt
                )
            }
        }
    }
}

@Composable
private fun StatusPill(completed: Boolean) {
    val color = if (completed) Color(0xFF10B981) else Color(0xFFF59E0B)
    Text(
        text = if (completed) "Fait" else "En cours",
        color = color,
        fontWeight = FontWeight.Bold,
        fontSize = 12.sp,
        modifier = Modifier
            .clip(RoundedCornerShape(30.dp))
            .background(color.copy(alpha = 0.12f))
            .padding(horizontal = 10.dp, vertical = 6.dp)
    )
}

@Composable
private fun EvaluationSmallInfo(label: String, value: String) {
    Column {
        Text(label, color = Color.Black.copy(alpha = 0.42f), fontSize = 11.sp)
        Text(value, color = Color.Black.copy(alpha = 0.82f), fontWeight = FontWeight.Bold, fontSize = 13.sp)
    }
}

@Composable
@Preview(showBackground = true)
fun EvaluationPreview() {
    EvaluationBuild()
}
