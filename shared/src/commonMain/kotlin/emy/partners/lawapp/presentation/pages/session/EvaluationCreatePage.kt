package emy.partners.lawapp.presentation.pages.session

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import emy.partners.lawapp.convertMillisToDate
import emy.partners.lawapp.data.Constants.typeEvaluationItems
import emy.partners.lawapp.domain.models.EvaluationDAO
import emy.partners.lawapp.domain.models.Question
import emy.partners.lawapp.domain.models.QuestionCaseStudy
import emy.partners.lawapp.domain.models.QuestionCaseStudyDAO
import emy.partners.lawapp.domain.models.QuestionOption
import emy.partners.lawapp.domain.models.QuestionOptionDAO
import emy.partners.lawapp.domain.models.QuestionOuverte
import emy.partners.lawapp.domain.models.QuestionOuverteDAO
import emy.partners.lawapp.presentation.components.basics.InputFieldCompose
import emy.partners.lawapp.presentation.components.basics.StepPager
import emy.partners.lawapp.presentation.themes.BlueDark
import emy.partners.lawapp.presentation.themes.BlueDarkEffect
import lawapp.shared.generated.resources.Res
import lawapp.shared.generated.resources.date
import kotlin.time.Clock

@Composable
fun EvaluationCreatePage(
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    onSave: (EvaluationDAO) -> Unit = {},
    scrollVertical: ScrollState = rememberScrollState()
) {
    EvaluationCreateBuild(
        modifier = modifier,
        onBack = onBack,
        onSave = onSave,
        scrollVertical = scrollVertical
    )
}

@Composable
fun EvaluationCreateBuild(
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    onSave: (EvaluationDAO) -> Unit = {},
    scrollVertical: ScrollState = rememberScrollState()
) {
    val stepLabels = listOf("Informations", "Questions", "Validation")
    var currentStep by remember { mutableIntStateOf(0) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showDatePicker2 by remember { mutableStateOf(false) }
    var title by remember { mutableStateOf("") }
    var typeEvaluation by remember { mutableStateOf("") }
    var matiere by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var instructions by remember { mutableStateOf("") }
    var fileContent by remember { mutableStateOf("") }
    var compteur by remember { mutableStateOf("") }
    var selectedQuestionType by remember { mutableStateOf(CreationQuestionType.Option) }
    var questionTitle by remember { mutableStateOf("") }
    var openExpectedAnswer by remember { mutableStateOf("") }
    var caseContent by remember { mutableStateOf("") }
    var caseResolution by remember { mutableStateOf("") }
    var savedMessage by remember { mutableStateOf<String?>(null) }
    val correctIndex = remember { mutableIntStateOf(0) }
    val optionValues = remember { mutableStateListOf("", "", "", "") }
    val optionQuestions = remember { mutableStateListOf<QuestionOptionDAO>() }
    val openQuestions = remember { mutableStateListOf<QuestionOuverteDAO>() }
    val caseStudyQuestions = remember { mutableStateListOf<QuestionCaseStudyDAO>() }
    val totalQuestions = optionQuestions.size + openQuestions.size + caseStudyQuestions.size
    val effectiveCounter = compteur.toLongOrNull() ?: totalQuestions.toLong()
    val datePickerState = rememberDatePickerState()
    val datePickerState2 = rememberDatePickerState()
    var startDate by remember {
        mutableStateOf(convertMillisToDate(Clock.System.now().toEpochMilliseconds()))
    }
    var endDate by remember {
        mutableStateOf(convertMillisToDate(Clock.System.now().toEpochMilliseconds()))
    }
    val canContinueFromInfo = title.isNotBlank() && matiere.isNotBlank() && typeEvaluation.isNotBlank()
    val canContinueFromQuestions = totalQuestions > 0
    val canSave = canContinueFromInfo && canContinueFromQuestions
    val canGoNext = when (currentStep) {
        0 -> canContinueFromInfo
        1 -> canContinueFromQuestions
        else -> canSave
    }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(scrollVertical)
            .padding(16.dp)
    ) {
        Column(modifier) {
            Text(
                text = "< Retour",
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
                        Brush.linearGradient(
                            listOf(
                                Color(0xFF2563EB).copy(alpha = 0.94f),
                                BlueDarkEffect.copy(alpha = 0.92f)
                            )
                        )
                    )
                    .padding(20.dp)
            ) {
                Column {
                    Text(
                        "Creer une evaluation",
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 28.sp,
                        lineHeight = 31.sp
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Organise la creation en etapes pour un flux clair, moderne et rapide.",
                        color = Color.White.copy(alpha = 0.74f),
                        lineHeight = 20.sp
                    )
                    Spacer(Modifier.height(18.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        CreationMetric("Compteur", effectiveCounter.toString(), Modifier.weight(1f))
                        CreationMetric("Options", optionQuestions.size.toString(), Modifier.weight(1f))
                        CreationMetric("Ouvertes", openQuestions.size.toString(), Modifier.weight(1f))
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
            Row(Modifier.fillMaxWidth()) {
                StepPager(
                    steps = stepLabels,
                    currentStep = currentStep
                )
            }
            Spacer(Modifier.height(16.dp))
            Box(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(32.dp))
                    .background(
                        Color.White
                    )
                    .padding(16.dp)
            ) {
                Column {
                    when (currentStep) {
                        0 -> {
                            CreationSection("Informations generales")
                            CreationSection("Titre *", size = 16)
                            CreationField(
                                "Titre",
                                title,
                                { title = it },
                                singleLine = true,
                                placeHolder = "Interro 3, chapitre 5"
                            )

                            CreationSection("Matiere *", size = 16)
                            CreationField(
                                "Matiere",
                                matiere,
                                { matiere = it },
                                singleLine = true,
                                placeHolder = "Droit civil"
                            )

                            CreationSection("Type d'evaluation *", size = 16)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                typeEvaluationItems.forEach { item ->
                                    QuestionTypeChip(
                                        label = item.name,
                                        selected = typeEvaluation == item.name,
                                        onClick = { typeEvaluation = item.name },
                                        selectedContainer = Color(0xFF2563EB),
                                        selectedText = Color.White,
                                        unselectedContainer = Color(0xFFF1F5F9),
                                        unselectedText = Color(0xFF1E293B)
                                    )
                                }
                            }
                            Spacer(Modifier.height(14.dp))

                            CreationSection("Description", size = 16)
                            CreationField(
                                "Description",
                                description,
                                { description = it },
                                minHeight = 96,
                                placeHolder = "Description de l'evaluation"
                            )

                            CreationSection("Instructions pour les etudiants", size = 16)
                            CreationField(
                                "Instructions",
                                instructions,
                                { instructions = it },
                                minHeight = 96,
                                placeHolder = "Consignes optionnelles"
                            )

                            CreationSection("Compteur manuel (optionnel)", size = 16)
                            CreationField(
                                "Compteur",
                                compteur,
                                { compteur = it.filter { char -> char.isDigit() } },
                                singleLine = true,
                                placeHolder = "Laisse vide pour auto"
                            )

                            CreationSection("Lien ou contenu de support (optionnel)", size = 16)
                            CreationField(
                                "Contenu",
                                fileContent,
                                { fileContent = it },
                                minHeight = 78,
                                placeHolder = "URL, texte de reference, etc."
                            )

                            Row(Modifier.fillMaxWidth()) {
                                Column(Modifier.weight(1f)) {
                                    CreationSection("Date debut", size = 16)
                                    InputFieldCompose(
                                        value = startDate,
                                        onValueChange = { startDate = it },
                                        onclickLastIcon = { showDatePicker = !showDatePicker },
                                        isSingle = true,
                                        iconLast = Res.drawable.date
                                    )
                                }
                                Spacer(Modifier.width(12.dp))
                                Column(Modifier.weight(1f)) {
                                    CreationSection("Date de fin", size = 16)
                                    InputFieldCompose(
                                        value = endDate,
                                        onValueChange = { endDate = it },
                                        onclickLastIcon = { showDatePicker2 = !showDatePicker2 },
                                        isSingle = true,
                                        iconLast = Res.drawable.date
                                    )
                                }
                            }
                        }

                        1 -> {
                            CreationSection("Banque de questions")
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                CreationMetric("Options", optionQuestions.size.toString(), Modifier.weight(1f))
                                CreationMetric("Ouvertes", openQuestions.size.toString(), Modifier.weight(1f))
                                CreationMetric("Cas", caseStudyQuestions.size.toString(), Modifier.weight(1f))
                            }
                            Spacer(Modifier.height(12.dp))
                            Text(
                                "Ajoute au moins une question pour continuer.",
                                color = Color(0xFF475569),
                                fontSize = 14.sp
                            )
                            Spacer(Modifier.height(12.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                QuestionTypeChip(
                                    label = "Option",
                                    selected = selectedQuestionType == CreationQuestionType.Option,
                                    onClick = { selectedQuestionType = CreationQuestionType.Option },
                                    selectedContainer = Color(0xFF2563EB),
                                    selectedText = Color.White,
                                    unselectedContainer = Color(0xFFF1F5F9),
                                    unselectedText = Color(0xFF1E293B)
                                )
                                QuestionTypeChip(
                                    label = "Ouverte",
                                    selected = selectedQuestionType == CreationQuestionType.Open,
                                    onClick = { selectedQuestionType = CreationQuestionType.Open },
                                    selectedContainer = Color(0xFF2563EB),
                                    selectedText = Color.White,
                                    unselectedContainer = Color(0xFFF1F5F9),
                                    unselectedText = Color(0xFF1E293B)
                                )
                                QuestionTypeChip(
                                    label = "Cas",
                                    selected = selectedQuestionType == CreationQuestionType.CaseStudy,
                                    onClick = { selectedQuestionType = CreationQuestionType.CaseStudy },
                                    selectedContainer = Color(0xFF2563EB),
                                    selectedText = Color.White,
                                    unselectedContainer = Color(0xFFF1F5F9),
                                    unselectedText = Color(0xFF1E293B)
                                )
                            }
                            Spacer(Modifier.height(10.dp))
                            OutlinedCard(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(24.dp)
                            ) {
                                Column(Modifier.padding(14.dp)) {
                                    CreationField("Question", questionTitle, { questionTitle = it }, minHeight = 76)
                                    when (selectedQuestionType) {
                                        CreationQuestionType.Option -> {
                                            optionValues.forEachIndexed { index, option ->
                                                CreationField(
                                                    label = "Option ${index + 1}",
                                                    value = option,
                                                    onValueChange = { optionValues[index] = it },
                                                    singleLine = true
                                                )
                                            }
                                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                optionValues.forEachIndexed { index, _ ->
                                                    QuestionTypeChip(
                                                        label = "Bonne ${index + 1}",
                                                        selected = correctIndex.intValue == index,
                                                        onClick = { correctIndex.intValue = index },
                                                        selectedContainer = Color(0xFF0F172A),
                                                        selectedText = Color.White,
                                                        unselectedContainer = Color(0xFFE2E8F0),
                                                        unselectedText = Color(0xFF334155)
                                                    )
                                                }
                                            }
                                            Spacer(Modifier.height(10.dp))
                                            Button(
                                                onClick = {
                                                    val answers = optionValues
                                                        .mapIndexedNotNull { index, option ->
                                                            option.takeIf { it.isNotBlank() }?.let {
                                                                QuestionOption(
                                                                    title = it,
                                                                    isCorrect = index == correctIndex.intValue
                                                                )
                                                            }
                                                        }
                                                    if (questionTitle.isNotBlank() && answers.isNotEmpty()) {
                                                        optionQuestions.add(
                                                            QuestionOptionDAO(
                                                                question = Question(title = questionTitle),
                                                                questionOption = answers
                                                            )
                                                        )
                                                        questionTitle = ""
                                                        optionValues.indices.forEach { optionValues[it] = "" }
                                                        savedMessage = "Question option ajoutee"
                                                    }
                                                },
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(52.dp),
                                                shape = RoundedCornerShape(18.dp),
                                                colors = ButtonDefaults.buttonColors(containerColor = BlueDark)
                                            ) {
                                                Text("Ajouter la question option", fontWeight = FontWeight.Bold)
                                            }
                                        }

                                        CreationQuestionType.Open -> {
                                            CreationField(
                                                "Reponse attendue",
                                                openExpectedAnswer,
                                                { openExpectedAnswer = it },
                                                minHeight = 90
                                            )
                                            Button(
                                                onClick = {
                                                    if (questionTitle.isNotBlank() && openExpectedAnswer.isNotBlank()) {
                                                        openQuestions.add(
                                                            QuestionOuverteDAO(
                                                                question = Question(title = questionTitle),
                                                                questionOuverte = listOf(
                                                                    QuestionOuverte(expectedAnswer = openExpectedAnswer)
                                                                )
                                                            )
                                                        )
                                                        questionTitle = ""
                                                        openExpectedAnswer = ""
                                                        savedMessage = "Question ouverte ajoutee"
                                                    }
                                                },
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(52.dp),
                                                shape = RoundedCornerShape(18.dp),
                                                colors = ButtonDefaults.buttonColors(containerColor = BlueDark)
                                            ) {
                                                Text("Ajouter la question ouverte", fontWeight = FontWeight.Bold)
                                            }
                                        }

                                        CreationQuestionType.CaseStudy -> {
                                            CreationField(
                                                "Enonce du cas",
                                                caseContent,
                                                { caseContent = it },
                                                minHeight = 92
                                            )
                                            CreationField(
                                                "Resolution attendue",
                                                caseResolution,
                                                { caseResolution = it },
                                                minHeight = 92
                                            )
                                            Button(
                                                onClick = {
                                                    if (questionTitle.isNotBlank() && caseContent.isNotBlank() && caseResolution.isNotBlank()) {
                                                        caseStudyQuestions.add(
                                                            QuestionCaseStudyDAO(
                                                                question = Question(title = questionTitle),
                                                                questionCaseStudy = listOf(
                                                                    QuestionCaseStudy(
                                                                        caseContent = caseContent,
                                                                        expectedResolution = caseResolution
                                                                    )
                                                                )
                                                            )
                                                        )
                                                        questionTitle = ""
                                                        caseContent = ""
                                                        caseResolution = ""
                                                        savedMessage = "Cas pratique ajoute"
                                                    }
                                                },
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(52.dp),
                                                shape = RoundedCornerShape(18.dp),
                                                colors = ButtonDefaults.buttonColors(containerColor = BlueDark)
                                            ) {
                                                Text("Ajouter le cas pratique", fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        2 -> {
                            CreationSection("Apercu avant publication")
                            DraftSummaryRow("Titre", title.ifBlank { "-" })
                            DraftSummaryRow("Matiere", matiere.ifBlank { "-" })
                            DraftSummaryRow("Type", typeEvaluation.ifBlank { "-" })
                            DraftSummaryRow("Date debut", startDate)
                            DraftSummaryRow("Date de fin", endDate)
                            DraftSummaryRow("Compteur final", effectiveCounter.toString())
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "Repartition des questions",
                                color = Color(0xFF0F172A),
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 16.sp
                            )
                            Spacer(Modifier.height(8.dp))
                            DraftCounterRow("Questions a choix", optionQuestions.size, darkMode = false)
                            DraftCounterRow("Questions ouvertes", openQuestions.size, darkMode = false)
                            DraftCounterRow("Cas pratiques", caseStudyQuestions.size, darkMode = false)
                            Spacer(Modifier.height(8.dp))
                            Text(
                                if (canSave) {
                                    "Ton evaluation est complete. Tu peux maintenant enregistrer."
                                } else {
                                    "Complete les etapes precedentes pour activer l'enregistrement."
                                },
                                color = if (canSave) Color(0xFF166534) else Color(0xFFB91C1C),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(10.dp))
            if (savedMessage != null) {
                Text(
                    savedMessage ?: "",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White.copy(alpha = 0.16f))
                        .padding(12.dp)
                )
                Spacer(Modifier.height(10.dp))
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(
                    onClick = {
                        if (currentStep == 0) {
                            onBack()
                        } else {
                            currentStep -= 1
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Text(
                        if (currentStep == 0) "Annuler" else "Precedent",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
                Button(
                    onClick = {
                        if (currentStep < stepLabels.lastIndex) {
                            if (canGoNext) {
                                currentStep += 1
                            } else {
                                savedMessage = when (currentStep) {
                                    0 -> "Completer titre, matiere et type pour continuer."
                                    1 -> "Ajoute au moins une question pour continuer."
                                    else -> "Verifie les informations avant de continuer."
                                }
                            }
                        } else {
                            val finalDescription = if (instructions.isNotBlank()) {
                                "$description\n\nInstructions:\n$instructions".trim()
                            } else {
                                description
                            }
                            val dao = EvaluationDAO(
                                title = title.ifBlank { "Nouvelle evaluation" },
                                description = finalDescription,
                                compteur = effectiveCounter,
                                fileContent = fileContent.takeIf { it.isNotBlank() },
                                startDate = startDate,
                                endDate = endDate,
                                option = optionQuestions.toList(),
                                ouverte = openQuestions.toList(),
                                caseStudy = caseStudyQuestions.toList()
                            )
                            onSave(dao)
                            savedMessage = "Evaluation enregistree avec $effectiveCounter question(s)"
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    shape = RoundedCornerShape(18.dp),
                    enabled = canGoNext,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = BlueDark,
                        disabledContainerColor = Color.White.copy(alpha = 0.45f),
                        disabledContentColor = BlueDark.copy(alpha = 0.56f)
                    )
                ) {
                    Text(
                        if (currentStep == stepLabels.lastIndex) "Enregistrer" else "Suivant",
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
            Spacer(Modifier.height(90.dp))
            if (showDatePicker) {
                Popup(
                    onDismissRequest = { showDatePicker = false },
                    alignment = Alignment.TopStart
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .offset(y = 64.dp)
                            .shadow(elevation = 4.dp)
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(16.dp)
                    ) {
                        DatePicker(
                            state = datePickerState,
                            showModeToggle = false,
                            headline = {
                                Button(
                                    onClick = {
                                        datePickerState.selectedDateMillis?.let {
                                            startDate = convertMillisToDate(it)
                                        }
                                        showDatePicker = false
                                    },
                                    modifier = Modifier.padding(10.dp)
                                ) {
                                    Text("Valider")
                                }
                            }
                        )
                    }
                }
            }
            if (showDatePicker2) {
                Popup(
                    onDismissRequest = { showDatePicker2 = false },
                    alignment = Alignment.TopStart
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .offset(y = 64.dp)
                            .shadow(elevation = 4.dp)
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(16.dp)
                    ) {
                        DatePicker(
                            state = datePickerState2,
                            showModeToggle = false,
                            headline = {
                                Button(
                                    onClick = {
                                        datePickerState2.selectedDateMillis?.let {
                                            endDate = convertMillisToDate(it)
                                        }
                                        showDatePicker2 = false
                                    },
                                    modifier = Modifier.padding(10.dp)
                                ) {
                                    Text("Valider")
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CreationSection(title: String, size: Int = 21) {
    Text(title, color = Color(0xFF0F172A), fontWeight = FontWeight.ExtraBold, fontSize = size.sp)
    Spacer(Modifier.height(10.dp))
}

@Composable
private fun CreationMetric(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFFF1F5F9))
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(value, color = Color(0xFF0F172A), fontWeight = FontWeight.ExtraBold, fontSize = 21.sp)
        Text(label, color = Color(0xFF64748B), fontSize = 11.sp)
    }
}

@Composable
private fun CreationField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    singleLine: Boolean = false,
    minHeight: Int = 56,
    placeHolder: String = ""
) {
    OutlinedTextField(
        value = value,
        label = { if (label.isNotBlank()) Text(label) },
        placeholder = { Text(placeHolder) },
        onValueChange = onValueChange,
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedBorderColor = Color.Black.copy(0.2f),
            focusedBorderColor = Color(0xFF2563EB).copy(alpha = 0.7f)
        ),
        singleLine = singleLine,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = minHeight.dp)
            .padding(bottom = 10.dp),
        shape = RoundedCornerShape(18.dp)
    )
}

@Composable
private fun QuestionTypeChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    selectedContainer: Color,
    selectedText: Color,
    unselectedContainer: Color,
    unselectedText: Color
) {
    Text(
        text = label,
        color = if (selected) selectedText else unselectedText,
        fontWeight = FontWeight.Bold,
        fontSize = 12.sp,
        modifier = Modifier
            .clip(RoundedCornerShape(40.dp))
            .background(if (selected) selectedContainer else unselectedContainer)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    )
}

@Composable
private fun DraftCounterRow(label: String, count: Int, darkMode: Boolean = true) {
    val rowBackground = if (darkMode) Color.White.copy(alpha = 0.16f) else Color(0xFFF8FAFC)
    val labelColor = if (darkMode) Color.White else Color(0xFF334155)
    val countBackground = if (darkMode) Color.White else Color(0xFFE2E8F0)
    val countColor = if (darkMode) BlueDark else Color(0xFF0F172A)

    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(rowBackground)
            .padding(14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = labelColor, fontWeight = FontWeight.Bold)
        Text(
            count.toString(),
            color = countColor,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(50.dp))
                .background(countBackground)
                .padding(top = 6.dp),
            fontSize = 14.sp
        )
    }
    Spacer(Modifier.height(8.dp))
}

@Composable
private fun DraftSummaryRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = Color(0xFF64748B), fontWeight = FontWeight.Medium)
        Text(value, color = Color(0xFF0F172A), fontWeight = FontWeight.ExtraBold)
    }
}

private enum class CreationQuestionType {
    Option,
    Open,
    CaseStudy
}

@Composable
@Preview(showBackground = true)
fun EvaluationCreatePreview() {
    EvaluationCreateBuild()
}
