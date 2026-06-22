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
import emy.partners.lawapp.presentation.components.basics.SelectInputField
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
                scrollVertical
    )
}

@Composable
fun EvaluationCreateBuild(
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    onSave: (EvaluationDAO) -> Unit = {},
    scrollVertical: ScrollState = rememberScrollState()
) {
    var showDatePicker by remember { mutableStateOf(false) }
    var showDatePicker2 by remember { mutableStateOf(false) }
    var title by remember { mutableStateOf("") }
    var typeEvaluation by remember { mutableStateOf("") }
    var matiere by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
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
    var startDate = datePickerState.selectedDateMillis?.let {
        convertMillisToDate(it)
    } ?: convertMillisToDate(Clock.System.now().toEpochMilliseconds())
    var endDate = datePickerState2.selectedDateMillis?.let {
        convertMillisToDate(it)
    } ?: convertMillisToDate(Clock.System.now().toEpochMilliseconds())
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
                        "Compose ton evaluation avec des questions a choix, ouvertes et cas pratiques.",
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
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                StepPager()
            }
            Spacer(Modifier.height(16.dp))
            Box(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(32.dp))
                    .background(
                        Color.White
                    )
                    .padding(10.dp)
            ) {
                Column {
                    CreationSection("Informations generales")
                    CreationSection("Titre *", size = 16)
                    CreationField("Titre", title, { title = it }, singleLine = true, placeHolder = "Interro 3, chapitre 5")
                    CreationSection("Description", size = 16)
                    CreationField("Description", description, { description = it }, minHeight = 96, placeHolder = "Description optionnelle")
                    CreationSection("Matiere *", size = 16)
                    CreationField("Matiere", matiere, { matiere = it }, singleLine = true, placeHolder = "Mathematique")
                    CreationSection("Type d'evaluation *", size = 16)
                    SelectInputField(itemList = typeEvaluationItems, textValueIn = typeEvaluation)
//                    CreationSection("Duree *", size = 16)
//                    CreationField("", matiere, { matiere = it }, singleLine = true, placeHolder = "30")
                    CreationSection("Instructions pour les etudiants", size = 16)
                    CreationField("Description", description, { description = it }, minHeight = 96, placeHolder = "Consignes optionnelles")

                    Row(Modifier.fillMaxWidth()) {
                        CreationSection("Date debut", size = 16)
                        Spacer(Modifier.width(80.dp))
                        CreationSection("Date de fin", size = 16)
                    }
                    Row(Modifier.fillMaxWidth()) {
                        InputFieldCompose(
                            modifier = Modifier.width(150.dp),
                            value = startDate,
                            onValueChange = { dataValue ->
                                startDate = dataValue },
                            onclickLastIcon = {
                                showDatePicker = !showDatePicker },
                            isSingle = false,
                            iconLast = Res.drawable.date
                        )
                        Spacer(Modifier.width(10.dp))
                        InputFieldCompose(
                            modifier = Modifier.width(150.dp),
                            value = endDate,
                            onValueChange = { dataValue ->
                                endDate = dataValue },
                            onclickLastIcon = {
                                showDatePicker2 = !showDatePicker2 },
                            isSingle = false,
                            iconLast = Res.drawable.date
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                    Button(
                        onClick = {

                        },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BlueDark)
                    ) {
                        Text("Suivant", fontWeight = FontWeight.Bold)
                    }


//                    CreationField(
//                        label = "Compteur manuel (optionnel)",
//                        value = compteur,
//                        onValueChange = { compteur = it.filter { char -> char.isDigit() } },
//                        singleLine = true
//                    )
//                    CreationField("Contenu fichier / lien", fileContent, { fileContent = it }, minHeight = 78)
                }
            }

            Spacer(Modifier.height(14.dp))
            CreationSection("Questions")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                QuestionTypeChip(
                    label = "Option",
                    selected = selectedQuestionType == CreationQuestionType.Option,
                    onClick = { selectedQuestionType = CreationQuestionType.Option }
                )
                QuestionTypeChip(
                    label = "Ouverte",
                    selected = selectedQuestionType == CreationQuestionType.Open,
                    onClick = { selectedQuestionType = CreationQuestionType.Open }
                )
                QuestionTypeChip(
                    label = "Cas",
                    selected = selectedQuestionType == CreationQuestionType.CaseStudy,
                    onClick = { selectedQuestionType = CreationQuestionType.CaseStudy }
                )
            }
            Spacer(Modifier.height(10.dp))
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
                                        onClick = { correctIndex.intValue = index }
                                    )
                                }
                            }
                            Spacer(Modifier.height(10.dp))
                            Button(
                                onClick = {
                                    val answers = optionValues
                                        .mapIndexedNotNull { index, option ->
                                            option.takeIf { it.isNotBlank() }?.let {
                                                QuestionOption(title = it, isCorrect = index == correctIndex.intValue)
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
                                modifier = Modifier.fillMaxWidth().height(52.dp),
                                shape = RoundedCornerShape(18.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = BlueDark)
                            ) {
                                Text("Ajouter la question option", fontWeight = FontWeight.Bold)
                            }
                        }
                        CreationQuestionType.Open -> {
                            CreationField("Reponse attendue", openExpectedAnswer, { openExpectedAnswer = it }, minHeight = 90)
                            Button(
                                onClick = {
                                    if (questionTitle.isNotBlank() && openExpectedAnswer.isNotBlank()) {
                                        openQuestions.add(
                                            QuestionOuverteDAO(
                                                question = Question(title = questionTitle),
                                                questionOuverte = listOf(QuestionOuverte(expectedAnswer = openExpectedAnswer))
                                            )
                                        )
                                        questionTitle = ""
                                        openExpectedAnswer = ""
                                        savedMessage = "Question ouverte ajoutee"
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().height(52.dp),
                                shape = RoundedCornerShape(18.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = BlueDark)
                            ) {
                                Text("Ajouter la question ouverte", fontWeight = FontWeight.Bold)
                            }
                        }
                        CreationQuestionType.CaseStudy -> {
                            CreationField("Enonce du cas", caseContent, { caseContent = it }, minHeight = 92)
                            CreationField("Resolution attendue", caseResolution, { caseResolution = it }, minHeight = 92)
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
                                modifier = Modifier.fillMaxWidth().height(52.dp),
                                shape = RoundedCornerShape(18.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = BlueDark)
                            ) {
                                Text("Ajouter le cas pratique", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            savedMessage?.let {
                Spacer(Modifier.height(10.dp))
                Text(it, color = Color.White, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(16.dp))
            CreationSection("Apercu du brouillon")
            DraftCounterRow("Questions a choix", optionQuestions.size)
            DraftCounterRow("Questions ouvertes", openQuestions.size)
            DraftCounterRow("Cas pratiques", caseStudyQuestions.size)
            Spacer(Modifier.height(14.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(
                    onClick = onBack,
                    modifier = Modifier.weight(1f).height(52.dp),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Text("Annuler", color = Color.White, fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = {
                        val dao = EvaluationDAO(
                            title = title.ifBlank { "Nouvelle evaluation" },
                            description = description,
                            compteur = effectiveCounter,
                            fileContent = fileContent.takeIf { it.isNotBlank() },
                            startDate = startDate,
                            endDate = endDate,
                            option = optionQuestions.toList(),
                            ouverte = openQuestions.toList(),
                            caseStudy = caseStudyQuestions.toList()
                        )
                        onSave(dao)
                        savedMessage = "Evaluation prete avec $effectiveCounter question(s)"
                    },
                    modifier = Modifier.weight(1f).height(52.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = BlueDark)
                ) {
                    Text("Enregistrer", fontWeight = FontWeight.Bold)
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
                                Button(onClick = {
                                    showDatePicker = false
                                }, modifier = Modifier.padding(10.dp)) {
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
                                Button(onClick = {
                                    showDatePicker2 = false
                                }, modifier = Modifier.padding(10.dp)) {
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
private fun CreationSection(title: String, size : Int = 21) {
    Text(title, color = Color.Black, fontWeight = FontWeight.ExtraBold, fontSize = size.sp)
    Spacer(Modifier.height(10.dp))
}

@Composable
private fun CreationMetric(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White.copy(alpha = 0.14f))
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(value, color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 21.sp)
        Text(label, color = Color.White.copy(alpha = 0.68f), fontSize = 11.sp)
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
    placeHolder : String = ""
) {
    OutlinedTextField(
        value = value,
        placeholder = {Text(placeHolder)},
        onValueChange = onValueChange,
        colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Color.Black.copy(0.4f), focusedBorderColor = Color.Blue.copy(alpha = 0.6f),
        ),
//        label = { Text(label, color = Color.White) },
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
    onClick: () -> Unit
) {
    Text(
        text = label,
        color = if (selected) BlueDark else Color.White,
        fontWeight = FontWeight.Bold,
        fontSize = 12.sp,
        modifier = Modifier
            .clip(RoundedCornerShape(40.dp))
            .background(if (selected) Color.White else Color.White.copy(alpha = 0.16f))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    )
}

@Composable
private fun DraftCounterRow(label: String, count: Int) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(Color.White.copy(alpha = 0.16f))
            .padding(14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = Color.White, fontWeight = FontWeight.Bold)
        Text(
            count.toString(),
            color = BlueDark,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(50.dp))
                .background(Color.White)
                .padding(top = 6.dp),
            fontSize = 14.sp
        )
    }
    Spacer(Modifier.height(8.dp))
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
