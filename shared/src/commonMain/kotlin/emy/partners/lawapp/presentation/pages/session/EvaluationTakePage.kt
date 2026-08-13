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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import emy.partners.lawapp.data.remote.evaluation.EvaluationAnswerInput
import emy.partners.lawapp.data.remote.evaluation.EvaluationQuestionKind
import emy.partners.lawapp.data.remote.evaluation.EvaluationRepository
import emy.partners.lawapp.data.remote.evaluation.EvaluationTakeQuestion
import emy.partners.lawapp.data.remote.evaluation.EvaluationTakeSheet
import emy.partners.lawapp.presentation.pages.auth.AuthColors
import emy.partners.lawapp.presentation.pages.auth.AuthFormPanel
import emy.partners.lawapp.presentation.pages.auth.AuthLoadingDialog
import emy.partners.lawapp.presentation.pages.auth.AuthMessageDialog
import emy.partners.lawapp.presentation.pages.auth.AuthPrimaryButton
import emy.partners.lawapp.presentation.settings.LocalAppUiController
import emy.partners.lawapp.presentation.themes.BlueDark
import emy.partners.lawapp.presentation.themes.BlueDarkEffect
import kotlinx.coroutines.launch

private val PageBgLight = Color(0xFFE8EEF7)
private val PageBgDark = Color(0xFF0B1220)

@Composable
fun EvaluationTakePage(
    evaluationId: Long,
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    onSubmitted: () -> Unit = {},
    scrollVertical: ScrollState = rememberScrollState(),
) {
    val ui = LocalAppUiController.current
    val pageBg = if (ui.settings.darkMode) PageBgDark else PageBgLight
    val scope = rememberCoroutineScope()
    var sheet by remember { mutableStateOf<EvaluationTakeSheet?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isSubmitting by remember { mutableStateOf(false) }
    var resultTitle by remember { mutableStateOf<String?>(null) }
    var resultMessage by remember { mutableStateOf<String?>(null) }
    val currentIndex = remember { mutableIntStateOf(0) }
    val selectedOptions = remember { mutableStateMapOf<Long, Long>() }
    val textAnswers = remember { mutableStateMapOf<Long, String>() }

    LaunchedEffect(evaluationId) {
        isLoading = true
        EvaluationRepository.getTakeSheet(evaluationId)
            .onSuccess {
                sheet = it
                errorMessage = null
            }
            .onFailure {
                errorMessage = it.message ?: "Impossible d'ouvrir cette evaluation"
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
            text = "< Retour",
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
            sheet == null || sheet!!.questions.isEmpty() -> {
                AuthFormPanel {
                    Text("Cette evaluation n'a pas encore de questions.", color = AuthColors.TextPrimary)
                }
            }
            else -> {
                val currentSheet = sheet!!
                val question = currentSheet.questions[currentIndex.intValue.coerceIn(0, currentSheet.questions.lastIndex)]
                val answeredCount = currentSheet.questions.count { item ->
                    when (item.kind) {
                        EvaluationQuestionKind.Option -> selectedOptions.containsKey(item.id)
                        else -> !textAnswers[item.id].isNullOrBlank()
                    }
                }
                val progress = (answeredCount.toFloat() / currentSheet.questions.size).coerceIn(0f, 1f)
                val isLast = currentIndex.intValue == currentSheet.questions.lastIndex
                val canAdvance = when (question.kind) {
                    EvaluationQuestionKind.Option -> selectedOptions.containsKey(question.id)
                    else -> !textAnswers[question.id].isNullOrBlank()
                }

                EvaluationTakeHeader(
                    title = currentSheet.title,
                    answered = answeredCount,
                    total = currentSheet.questions.size,
                    progress = progress,
                )
                Spacer(Modifier.height(14.dp))
                AuthFormPanel {
                    TakeQuestionCard(
                        question = question,
                        current = currentIndex.intValue + 1,
                        total = currentSheet.questions.size,
                        selectedOptionId = selectedOptions[question.id],
                        textValue = textAnswers[question.id].orEmpty(),
                        onSelectOption = { selectedOptions[question.id] = it },
                        onTextChange = { textAnswers[question.id] = it },
                    )
                    Spacer(Modifier.height(14.dp))
                    AuthPrimaryButton(
                        text = if (isLast) "Soumettre l'evaluation" else "Question suivante",
                        enabled = canAdvance && !isSubmitting,
                        onClick = {
                            if (!isLast) {
                                currentIndex.intValue += 1
                                return@AuthPrimaryButton
                            }
                            val answers = currentSheet.questions.mapNotNull { item ->
                                when (item.kind) {
                                    EvaluationQuestionKind.Option -> {
                                        val optionId = selectedOptions[item.id] ?: return@mapNotNull null
                                        EvaluationAnswerInput(
                                            questionId = item.id,
                                            selectedOptionId = optionId,
                                        )
                                    }
                                    else -> {
                                        val text = textAnswers[item.id]?.trim().orEmpty()
                                        if (text.isBlank()) return@mapNotNull null
                                        EvaluationAnswerInput(
                                            questionId = item.id,
                                            textResponse = text,
                                        )
                                    }
                                }
                            }
                            isSubmitting = true
                            scope.launch {
                                EvaluationRepository.submitAnswers(currentSheet.id, answers)
                                    .onSuccess { result ->
                                        resultTitle = "Evaluation soumise"
                                        resultMessage = result.message
                                    }
                                    .onFailure {
                                        resultTitle = "Soumission impossible"
                                        resultMessage = it.message ?: "La soumission a echoue"
                                    }
                                isSubmitting = false
                            }
                        },
                    )
                }
            }
        }
    }

    AuthLoadingDialog(visible = isSubmitting, message = "Soumission en cours...")
    if (resultTitle != null && resultMessage != null) {
        AuthMessageDialog(
            title = resultTitle.orEmpty(),
            message = resultMessage.orEmpty(),
            onConfirm = {
                val success = resultTitle == "Evaluation soumise"
                resultTitle = null
                resultMessage = null
                if (success) onSubmitted()
            },
        )
    }
}

@Composable
private fun EvaluationTakeHeader(
    title: String,
    answered: Int,
    total: Int,
    progress: Float,
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
        Text("Passer l'evaluation", color = Color.White.copy(alpha = 0.75f), fontWeight = FontWeight.Bold, fontSize = 13.sp)
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
        Spacer(Modifier.height(12.dp))
        Text(
            "$answered/$total reponse(s)",
            color = Color.White.copy(alpha = 0.8f),
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun TakeQuestionCard(
    question: EvaluationTakeQuestion,
    current: Int,
    total: Int,
    selectedOptionId: Long?,
    textValue: String,
    onSelectOption: (Long) -> Unit,
    onTextChange: (String) -> Unit,
) {
    Column(Modifier.fillMaxWidth()) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = when (question.kind) {
                    EvaluationQuestionKind.Option -> "QCM"
                    EvaluationQuestionKind.Open -> "Question ouverte"
                    EvaluationQuestionKind.CaseStudy -> "Cas pratique"
                },
                color = AuthColors.AccentBright,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
            )
            Text("$current/$total", color = AuthColors.TextSecondary, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(12.dp))
        Text(
            question.title,
            color = AuthColors.TextPrimary,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 22.sp,
            lineHeight = 28.sp,
        )
        if (!question.prompt.isNullOrBlank()) {
            Spacer(Modifier.height(8.dp))
            Text(question.prompt, color = AuthColors.TextSecondary, fontSize = 13.sp, lineHeight = 18.sp)
        }
        Spacer(Modifier.height(16.dp))
        when (question.kind) {
            EvaluationQuestionKind.Option -> {
                question.options.forEachIndexed { index, option ->
                    val selected = selectedOptionId == option.id
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (selected) AuthColors.AccentBright else Color(0xFFF8FAFC))
                            .clickable { onSelectOption(option.id) }
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            Modifier
                                .size(30.dp)
                                .clip(RoundedCornerShape(50))
                                .background(
                                    if (selected) Color.White.copy(alpha = 0.2f)
                                    else AuthColors.AccentBright.copy(alpha = 0.12f)
                                ),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                ('A' + index).toString(),
                                color = if (selected) Color.White else AuthColors.TextPrimary,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                        Spacer(Modifier.size(12.dp))
                        Text(
                            option.label,
                            color = if (selected) Color.White else AuthColors.TextPrimary,
                            fontWeight = FontWeight.SemiBold,
                            lineHeight = 18.sp,
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                }
            }
            else -> {
                OutlinedTextField(
                    value = textValue,
                    onValueChange = onTextChange,
                    placeholder = { Text("Ta reponse") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 120.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color.Black.copy(0.2f),
                        focusedBorderColor = Color(0xFF2563EB).copy(alpha = 0.7f),
                    ),
                )
            }
        }
    }
}