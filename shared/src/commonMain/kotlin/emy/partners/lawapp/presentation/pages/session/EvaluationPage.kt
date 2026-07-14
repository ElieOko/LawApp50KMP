package emy.partners.lawapp.presentation.pages.session

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import emy.partners.lawapp.presentation.pages.auth.AuthColors
import emy.partners.lawapp.presentation.pages.auth.AuthFormPanel
import emy.partners.lawapp.presentation.settings.LocalAppUiController
import emy.partners.lawapp.presentation.themes.BlueDark
import emy.partners.lawapp.presentation.themes.BlueDarkEffect

private val PageBgLight = Color(0xFFE8EEF7)
private val PageBgDark = Color(0xFF0B1220)

@Composable
fun EvaluationPage(
    modifier: Modifier = Modifier,
    evaluations: List<EvaluationSession> = Constants.evaluations,
    onEvaluationClick: (EvaluationSession) -> Unit = {},
    onCreateClick: () -> Unit = {},
    scrollVertical: ScrollState = rememberScrollState(),
) {
    EvaluationBuild(
        modifier = modifier,
        evaluations = evaluations,
        onEvaluationClick = onEvaluationClick,
        onCreateClick = onCreateClick,
        scrollVertical = scrollVertical,
    )
}

@Composable
fun EvaluationBuild(
    modifier: Modifier = Modifier,
    evaluations: List<EvaluationSession> = Constants.evaluations,
    onEvaluationClick: (EvaluationSession) -> Unit = {},
    onCreateClick: () -> Unit = {},
    scrollVertical: ScrollState = rememberScrollState(),
) {
    val ui = LocalAppUiController.current
    val pageBg = if (ui.settings.darkMode) PageBgDark else PageBgLight
    var activeFilter by remember { mutableStateOf<EvaluationStatus?>(null) }
    val visibleEvaluations = evaluations.filter { activeFilter == null || it.status == activeFilter }
    val completedCount = evaluations.count { it.status == EvaluationStatus.Completed }
    val progressCount = evaluations.count { it.status == EvaluationStatus.InProgress }

    Column(
        modifier
            .fillMaxSize()
            .background(pageBg)
            .verticalScroll(scrollVertical)
            .padding(horizontal = 14.dp)
            .padding(top = 8.dp, bottom = 96.dp)
    ) {
        EvaluationHero(
            total = evaluations.size,
            completed = completedCount,
            inProgress = progressCount,
            onCreateClick = onCreateClick,
        )
        Spacer(Modifier.height(14.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            EvaluationFilterChip(
                label = "Tous",
                count = evaluations.size,
                selected = activeFilter == null,
                onClick = { activeFilter = null },
            )
            EvaluationFilterChip(
                label = "En cours",
                count = progressCount,
                selected = activeFilter == EvaluationStatus.InProgress,
                onClick = { activeFilter = EvaluationStatus.InProgress },
            )
            EvaluationFilterChip(
                label = "Terminees",
                count = completedCount,
                selected = activeFilter == EvaluationStatus.Completed,
                onClick = { activeFilter = EvaluationStatus.Completed },
            )
        }

        Spacer(Modifier.height(16.dp))
        AuthFormPanel {
            Text(
                text = "Mes sessions",
                color = AuthColors.TextPrimary,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 18.sp,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = if (visibleEvaluations.isEmpty()) {
                    "Aucune evaluation pour ce filtre."
                } else {
                    "${visibleEvaluations.size} evaluation(s)"
                },
                color = AuthColors.TextSecondary,
                fontSize = 13.sp,
            )
            Spacer(Modifier.height(14.dp))
            visibleEvaluations.forEachIndexed { index, evaluation ->
                EvaluationCard(
                    evaluation = evaluation,
                    onClick = { onEvaluationClick(evaluation) },
                )
                if (index != visibleEvaluations.lastIndex) {
                    Spacer(Modifier.height(10.dp))
                }
            }
        }
    }
}

@Composable
private fun EvaluationHero(
    total: Int,
    completed: Int,
    inProgress: Int,
    onCreateClick: () -> Unit,
) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(
                Brush.linearGradient(
                    listOf(
                        BlueDark.copy(alpha = 0.96f),
                        Color(0xFF2563EB).copy(alpha = 0.9f),
                        BlueDarkEffect.copy(alpha = 0.94f),
                    )
                )
            )
            .padding(18.dp)
    ) {
        Text(
            text = "Centre d'evaluation",
            color = Color.White,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 26.sp,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = "Reprends une session en cours ou consulte tes resultats termines.",
            color = Color.White.copy(alpha = 0.75f),
            fontSize = 14.sp,
            lineHeight = 19.sp,
        )
        Spacer(Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            EvaluationMetric("Total", total.toString(), Modifier.weight(1f))
            EvaluationMetric("En cours", inProgress.toString(), Modifier.weight(1f))
            EvaluationMetric("Faites", completed.toString(), Modifier.weight(1f))
        }
        Spacer(Modifier.height(14.dp))
        Row(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(Color.White)
                .clickable(onClick = onCreateClick)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text("Creer une evaluation", color = BlueDark, fontWeight = FontWeight.ExtraBold)
                Text(
                    "Questions, compteur, dates et contenu",
                    color = AuthColors.TextSecondary,
                    fontSize = 12.sp,
                )
            }
            Text("+", color = BlueDark, fontWeight = FontWeight.ExtraBold, fontSize = 26.sp)
        }
    }
}

@Composable
private fun EvaluationMetric(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.14f))
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(value, color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
        Text(label, color = Color.White.copy(alpha = 0.72f), fontSize = 11.sp)
    }
}

@Composable
private fun EvaluationFilterChip(
    label: String,
    count: Int,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(if (selected) AuthColors.AccentBright else Color.White)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            color = if (selected) Color.White else AuthColors.TextPrimary,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = count.toString(),
            color = if (selected) AuthColors.AccentBright else Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            modifier = Modifier
                .clip(RoundedCornerShape(30.dp))
                .background(if (selected) Color.White else AuthColors.AccentBright)
                .padding(horizontal = 7.dp, vertical = 2.dp),
        )
    }
}

@Composable
private fun EvaluationCard(
    evaluation: EvaluationSession,
    onClick: () -> Unit,
) {
    val completed = evaluation.status == EvaluationStatus.Completed
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
                    text = evaluation.domain,
                    color = AuthColors.AccentBright,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                )
                Text(
                    text = evaluation.title,
                    color = AuthColors.TextPrimary,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 17.sp,
                    lineHeight = 21.sp,
                )
            }
            StatusPill(completed = completed)
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = evaluation.description,
            color = AuthColors.TextSecondary,
            fontSize = 13.sp,
            lineHeight = 18.sp,
        )
        Spacer(Modifier.height(12.dp))
        LinearProgressIndicator(
            progress = { evaluation.progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(50.dp)),
            color = if (completed) Color(0xFF10B981) else AuthColors.AccentBright,
            trackColor = Color.Black.copy(alpha = 0.08f),
        )
        Spacer(Modifier.height(12.dp))
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            EvaluationSmallInfo(
                label = "Questions",
                value = "${evaluation.completedQuestions}/${evaluation.questionCount}",
            )
            EvaluationSmallInfo(label = "Duree", value = evaluation.duration)
            EvaluationSmallInfo(
                label = if (completed) "Score" else "Maj",
                value = if (completed) "${evaluation.score}%" else evaluation.updatedAt,
            )
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
            .padding(horizontal = 10.dp, vertical = 6.dp),
    )
}

@Composable
private fun EvaluationSmallInfo(label: String, value: String) {
    Column {
        Text(label, color = AuthColors.TextSecondary, fontSize = 11.sp)
        Text(value, color = AuthColors.TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
    }
}

@Composable
@Preview(showBackground = true)
fun EvaluationPreview() {
    EvaluationBuild()
}
