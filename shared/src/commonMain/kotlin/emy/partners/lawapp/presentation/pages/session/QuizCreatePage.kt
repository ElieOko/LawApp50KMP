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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import emy.partners.lawapp.data.remote.quiz.QuizLevelRequestDto
import emy.partners.lawapp.data.remote.quiz.QuizNotionTypeDto
import emy.partners.lawapp.data.remote.quiz.QuizOptionRequestDto
import emy.partners.lawapp.data.remote.quiz.QuizQuestionRequestDto
import emy.partners.lawapp.data.remote.quiz.QuizRepository
import emy.partners.lawapp.presentation.pages.auth.AuthColors
import emy.partners.lawapp.presentation.pages.auth.AuthFormPanel
import emy.partners.lawapp.presentation.pages.auth.AuthLoadingDialog
import emy.partners.lawapp.presentation.pages.auth.AuthMessageDialog
import emy.partners.lawapp.presentation.pages.auth.AuthPrimaryButton
import emy.partners.lawapp.presentation.pages.auth.AuthTextField
import kotlinx.coroutines.launch

@Composable
fun QuizCreatePage(
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    onCreated: () -> Unit = {},
    scrollVertical: ScrollState = rememberScrollState(),
) {
    val scope = rememberCoroutineScope()
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var notionLabel by remember { mutableStateOf("") }
    var notionTypes by remember { mutableStateOf<List<QuizNotionTypeDto>>(emptyList()) }
    var selectedTypeId by remember { mutableStateOf<Long?>(null) }
    var questionTitle by remember { mutableStateOf("") }
    val options = remember { mutableStateListOf("", "", "", "") }
    var correctIndex by remember { mutableIntStateOf(0) }
    val questions = remember { mutableStateListOf<QuizQuestionRequestDto>() }
    var isSaving by remember { mutableStateOf(false) }
    var dialogTitle by remember { mutableStateOf<String?>(null) }
    var dialogMessage by remember { mutableStateOf<String?>(null) }
    var created by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        QuizRepository.loadNotionTypes().onSuccess { types ->
            notionTypes = types
            if (selectedTypeId == null) selectedTypeId = types.firstOrNull()?.id
            if (notionLabel.isBlank()) notionLabel = types.firstOrNull()?.label.orEmpty()
        }
    }

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
            Text("Creer un quiz", color = AuthColors.TextPrimary, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp)
            Spacer(Modifier.height(6.dp))
            Text(
                "Reserve aux enseignants. Niveaux et questions sont envoyes au backend.",
                color = AuthColors.TextSecondary,
                fontSize = 13.sp,
            )
            Spacer(Modifier.height(14.dp))
            AuthTextField(value = title, onValueChange = { title = it }, label = "Titre *")
            Spacer(Modifier.height(10.dp))
            AuthTextField(value = description, onValueChange = { description = it }, label = "Description")
            Spacer(Modifier.height(10.dp))
            AuthTextField(
                value = notionLabel,
                onValueChange = { notionLabel = it },
                label = "Type de notion (Droit, Procedure...)",
            )
            if (notionTypes.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    notionTypes.take(4).forEach { type ->
                        val selected = selectedTypeId == type.id
                        Text(
                            type.label ?: type.code.orEmpty(),
                            color = if (selected) Color.White else AuthColors.AccentBright,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (selected) AuthColors.AccentBright else Color(0xFFEFF6FF))
                                .clickable {
                                    selectedTypeId = type.id
                                    notionLabel = type.label ?: type.code.orEmpty()
                                }
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                        )
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
            Text("Question QCM", color = AuthColors.TextPrimary, fontWeight = FontWeight.ExtraBold)
            Spacer(Modifier.height(8.dp))
            AuthTextField(value = questionTitle, onValueChange = { questionTitle = it }, label = "Intitule")
            options.forEachIndexed { index, value ->
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = value,
                    onValueChange = { options[index] = it },
                    label = { Text(if (index == correctIndex) "Option ${index + 1} (bonne reponse)" else "Option ${index + 1}") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { correctIndex = index },
                    singleLine = true,
                )
            }
            Spacer(Modifier.height(10.dp))
            AuthPrimaryButton(
                text = "Ajouter la question (${questions.size} deja)",
                onClick = {
                    val labels = options.map { it.trim() }.filter { it.isNotBlank() }
                    if (questionTitle.isBlank() || labels.size < 2) {
                        dialogTitle = "Question incomplete"
                        dialogMessage = "Saisissez un intitule et au moins deux options."
                        return@AuthPrimaryButton
                    }
                    questions.add(
                        QuizQuestionRequestDto(
                            title = questionTitle.trim(),
                            point = 1.0,
                            options = options.mapIndexed { index, option ->
                                QuizOptionRequestDto(option = option.trim(), goal = index == correctIndex)
                            }.filter { it.option.isNotBlank() },
                        )
                    )
                    questionTitle = ""
                    repeat(options.size) { options[it] = "" }
                    correctIndex = 0
                },
            )
            Spacer(Modifier.height(16.dp))
            AuthPrimaryButton(
                text = if (isSaving) "Publication..." else "Publier le quiz",
                enabled = !isSaving && title.isNotBlank() && questions.isNotEmpty(),
                onClick = {
                    isSaving = true
                    scope.launch {
                        QuizRepository.createQuiz(
                            title = title,
                            description = description,
                            notionTypeId = selectedTypeId,
                            notionLabel = notionLabel,
                            levels = listOf(
                                QuizLevelRequestDto(
                                    title = "Niveau 1",
                                    levelOrder = 1,
                                    questions = questions.toList(),
                                )
                            ),
                        ).onSuccess {
                            created = true
                            dialogTitle = "Quiz publie"
                            dialogMessage = "Le quiz est disponible pour les etudiants."
                        }.onFailure {
                            dialogTitle = "Publication impossible"
                            dialogMessage = it.message ?: "La creation du quiz a echoue."
                        }
                        isSaving = false
                    }
                },
            )
        }
        AuthLoadingDialog(visible = isSaving, message = "Publication du quiz...")
        if (dialogTitle != null && dialogMessage != null) {
            AuthMessageDialog(
                title = dialogTitle.orEmpty(),
                message = dialogMessage.orEmpty(),
                onConfirm = {
                    dialogTitle = null
                    dialogMessage = null
                    if (created) onCreated()
                },
            )
        }
        Spacer(Modifier.height(80.dp))
    }
}
