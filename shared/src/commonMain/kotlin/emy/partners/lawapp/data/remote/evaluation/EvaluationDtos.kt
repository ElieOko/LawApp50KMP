package emy.partners.lawapp.data.remote.evaluation

import emy.partners.lawapp.domain.models.EvaluationDAO
import emy.partners.lawapp.domain.models.EvaluationSession
import emy.partners.lawapp.domain.models.EvaluationStatus
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames

@Serializable
data class EvaluationQuestionDto(
    val id: Long? = null,
    val evaluationId: Long? = null,
    val title: String? = null,
    val point: Double? = null,
)

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class EvaluationOptionItemDto(
    val id: Long? = null,
    val questionId: Long? = null,
    val option: String? = null,
    @JsonNames("isValid", "valid", "goal")
    val valid: Boolean? = null,
)

@Serializable
data class EvaluationOptionBlockDto(
    val question: EvaluationQuestionDto? = null,
    val questionOption: List<EvaluationOptionItemDto> = emptyList(),
)

@Serializable
data class EvaluationOpenItemDto(
    val id: Long? = null,
    val title: String? = null,
    val fileContent: String? = null,
)

@Serializable
data class EvaluationOpenBlockDto(
    val question: EvaluationQuestionDto? = null,
    val questionOuverte: List<EvaluationOpenItemDto> = emptyList(),
)

@Serializable
data class EvaluationCaseItemDto(
    val id: Long? = null,
    val title: String? = null,
    val fileContent: String? = null,
)

@Serializable
data class EvaluationCaseBlockDto(
    val question: EvaluationQuestionDto? = null,
    val questionCaseStudy: List<EvaluationCaseItemDto> = emptyList(),
)

@Serializable
data class EvaluationSessionDto(
    val id: Long? = null,
    val title: String? = null,
    val description: String? = null,
    val compteur: Long? = null,
    val fileContent: String? = null,
    val startDate: String? = null,
    val endDate: String? = null,
    val option: List<EvaluationOptionBlockDto> = emptyList(),
    val ouverte: List<EvaluationOpenBlockDto> = emptyList(),
    val caseStudy: List<EvaluationCaseBlockDto> = emptyList(),
)

@Serializable
data class EvaluationAnswerableSummaryDto(
    val id: Long? = null,
    val title: String? = null,
    val description: String? = null,
    val startDate: String? = null,
    val endDate: String? = null,
)

@Serializable
data class EvaluationOptionPassageDto(
    val id: Long? = null,
    val option: String? = null,
)

@Serializable
data class EvaluationQuestionPassageDto(
    val id: Long? = null,
    val title: String? = null,
    val point: Double? = null,
    val kind: String? = null,
    val options: List<EvaluationOptionPassageDto> = emptyList(),
    val promptTitle: String? = null,
    val promptFileContent: String? = null,
)

@Serializable
data class EvaluationPassageDto(
    val id: Long? = null,
    val title: String? = null,
    val description: String? = null,
    val startDate: String? = null,
    val endDate: String? = null,
    val questions: List<EvaluationQuestionPassageDto> = emptyList(),
)

@Serializable
data class EvaluationAnswerSubmitRequest(
    val answers: List<QuestionAnswerItemRequest>,
)

@Serializable
data class QuestionAnswerItemRequest(
    val questionId: Long,
    val selectedOptionId: Long? = null,
    val textResponse: String? = null,
)

@Serializable
data class EvaluationQuestionRequestDto(
    val title: String,
    val point: Double,
)

@Serializable
data class EvaluationOptionRequestDto(
    val option: String,
    val goal: Boolean = false,
)

@Serializable
data class EvaluationOptionBlockRequestDto(
    val question: EvaluationQuestionRequestDto,
    val questionOption: List<EvaluationOptionRequestDto>,
)

@Serializable
data class EvaluationOpenItemRequestDto(
    val title: String,
    val fileContent: String? = null,
)

@Serializable
data class EvaluationOpenBlockRequestDto(
    val question: EvaluationQuestionRequestDto,
    val questionOuverte: List<EvaluationOpenItemRequestDto>,
)

@Serializable
data class EvaluationCaseItemRequestDto(
    val title: String,
    val fileContent: String? = null,
)

@Serializable
data class EvaluationCaseBlockRequestDto(
    val question: EvaluationQuestionRequestDto,
    val questionCaseStudy: List<EvaluationCaseItemRequestDto>,
)

@Serializable
data class EvaluationCreateRequest(
    val title: String,
    val description: String? = null,
    val compteur: Long? = null,
    val fileContent: String? = null,
    val startDate: String? = null,
    val endDate: String? = null,
    val option: List<EvaluationOptionBlockRequestDto> = emptyList(),
    val ouverte: List<EvaluationOpenBlockRequestDto> = emptyList(),
    val caseStudy: List<EvaluationCaseBlockRequestDto> = emptyList(),
)

enum class EvaluationQuestionKind {
    Option,
    Open,
    CaseStudy,
}

data class EvaluationTakeQuestion(
    val id: Long,
    val title: String,
    val point: Double,
    val kind: EvaluationQuestionKind,
    val options: List<EvaluationTakeOption> = emptyList(),
    val prompt: String? = null,
)

data class EvaluationTakeOption(
    val id: Long,
    val label: String,
)

data class EvaluationTakeSheet(
    val id: Long,
    val title: String,
    val description: String,
    val startDate: String?,
    val endDate: String?,
    val questions: List<EvaluationTakeQuestion>,
)

data class EvaluationAnswerInput(
    val questionId: Long,
    val selectedOptionId: Long? = null,
    val textResponse: String? = null,
)

data class EvaluationSubmitResult(
    val message: String,
    val rawResponse: String,
)

fun EvaluationSessionDto.questionCount(): Int =
    option.size + ouverte.size + caseStudy.size

fun EvaluationSessionDto.toSession(
    canAnswer: Boolean,
    alreadySubmitted: Boolean,
): EvaluationSession? {
    val evaluationId = id ?: return null
    val resolvedTitle = title?.takeIf { it.isNotBlank() } ?: "Evaluation"
    val count = questionCount()
    val completed = alreadySubmitted
    val minutes = compteur?.takeIf { it > 0 }
    return EvaluationSession(
        id = evaluationId,
        title = resolvedTitle,
        domain = if (canAnswer) "Ouverte" else if (completed) "Terminee" else "Session",
        description = description?.takeIf { it.isNotBlank() && it != "string" }
            ?: "Evaluation disponible du ${startDate.orEmpty()} au ${endDate.orEmpty()}".trim(),
        status = if (completed) EvaluationStatus.Completed else EvaluationStatus.InProgress,
        progress = if (completed) 1f else 0f,
        score = null,
        questionCount = count,
        completedQuestions = if (completed) count else 0,
        duration = minutes?.let { formatMinutes(it) } ?: dateRangeLabel(startDate, endDate),
        updatedAt = endDate ?: startDate ?: "",
        level = "Evaluation",
        canAnswer = canAnswer && !completed,
        alreadySubmitted = completed,
        startDate = startDate,
        endDate = endDate,
    )
}

fun EvaluationSessionDto.toTakeSheet(): EvaluationTakeSheet? {
    val evaluationId = id ?: return null
    val questions = buildList {
        option.forEachIndexed { index, block ->
            val question = block.question ?: return@forEachIndexed
            val questionId = question.id ?: return@forEachIndexed
            val options = block.questionOption.mapIndexedNotNull { optionIndex, item ->
                val label = item.option?.takeIf { it.isNotBlank() } ?: return@mapIndexedNotNull null
                EvaluationTakeOption(
                    id = item.id ?: (optionIndex + 1L),
                    label = label,
                )
            }
            add(
                EvaluationTakeQuestion(
                    id = questionId,
                    title = question.title?.takeIf { it.isNotBlank() } ?: "Question ${index + 1}",
                    point = question.point ?: 1.0,
                    kind = EvaluationQuestionKind.Option,
                    options = options,
                )
            )
        }
        ouverte.forEachIndexed { index, block ->
            val question = block.question ?: return@forEachIndexed
            val questionId = question.id ?: return@forEachIndexed
            val prompt = block.questionOuverte.firstOrNull()?.title
                ?: block.questionOuverte.firstOrNull()?.fileContent
            add(
                EvaluationTakeQuestion(
                    id = questionId,
                    title = question.title?.takeIf { it.isNotBlank() } ?: "Question ouverte ${index + 1}",
                    point = question.point ?: 1.0,
                    kind = EvaluationQuestionKind.Open,
                    prompt = prompt,
                )
            )
        }
        caseStudy.forEachIndexed { index, block ->
            val question = block.question ?: return@forEachIndexed
            val questionId = question.id ?: return@forEachIndexed
            val prompt = block.questionCaseStudy.firstOrNull()?.title
                ?: block.questionCaseStudy.firstOrNull()?.fileContent
            add(
                EvaluationTakeQuestion(
                    id = questionId,
                    title = question.title?.takeIf { it.isNotBlank() } ?: "Cas pratique ${index + 1}",
                    point = question.point ?: 1.0,
                    kind = EvaluationQuestionKind.CaseStudy,
                    prompt = prompt,
                )
            )
        }
    }
    return EvaluationTakeSheet(
        id = evaluationId,
        title = title?.takeIf { it.isNotBlank() } ?: "Evaluation",
        description = description.orEmpty(),
        startDate = startDate,
        endDate = endDate,
        questions = questions,
    )
}

fun EvaluationPassageDto.toTakeSheet(): EvaluationTakeSheet? {
    val evaluationId = id ?: return null
    val mapped = questions.mapIndexedNotNull { index, question ->
        val questionId = question.id ?: return@mapIndexedNotNull null
        val kind = question.kind.toQuestionKind(question.options.isNotEmpty())
        EvaluationTakeQuestion(
            id = questionId,
            title = question.title?.takeIf { it.isNotBlank() } ?: "Question ${index + 1}",
            point = question.point ?: 1.0,
            kind = kind,
            options = question.options.mapIndexedNotNull { optionIndex, option ->
                val label = option.option?.takeIf { it.isNotBlank() } ?: return@mapIndexedNotNull null
                EvaluationTakeOption(
                    id = option.id ?: (optionIndex + 1L),
                    label = label,
                )
            },
            prompt = question.promptTitle?.takeIf { it.isNotBlank() }
                ?: question.promptFileContent?.takeIf { it.isNotBlank() },
        )
    }
    return EvaluationTakeSheet(
        id = evaluationId,
        title = title?.takeIf { it.isNotBlank() } ?: "Evaluation",
        description = description.orEmpty(),
        startDate = startDate,
        endDate = endDate,
        questions = mapped,
    )
}

fun EvaluationDAO.toCreateRequest(
    startDateApi: String,
    endDateApi: String,
): EvaluationCreateRequest = EvaluationCreateRequest(
    title = title,
    description = description.takeIf { it.isNotBlank() },
    compteur = compteur,
    fileContent = fileContent,
    startDate = startDateApi,
    endDate = endDateApi,
    option = option.orEmpty().mapNotNull { block ->
        val questionTitle = block.question?.title?.takeIf { it.isNotBlank() } ?: return@mapNotNull null
        val options = block.questionOption.mapNotNull { item ->
            item.title.takeIf { it.isNotBlank() }?.let { label ->
                EvaluationOptionRequestDto(option = label, goal = item.isCorrect)
            }
        }
        if (options.isEmpty()) return@mapNotNull null
        EvaluationOptionBlockRequestDto(
            question = EvaluationQuestionRequestDto(
                title = questionTitle,
                point = (block.question.points).toDouble(),
            ),
            questionOption = options,
        )
    },
    ouverte = ouverte.orEmpty().mapNotNull { block ->
        val questionTitle = block.question?.title?.takeIf { it.isNotBlank() } ?: return@mapNotNull null
        val items = block.questionOuverte.map { item ->
            EvaluationOpenItemRequestDto(
                title = item.expectedAnswer,
                fileContent = null,
            )
        }
        EvaluationOpenBlockRequestDto(
            question = EvaluationQuestionRequestDto(
                title = questionTitle,
                point = (block.question.points).toDouble(),
            ),
            questionOuverte = items,
        )
    },
    caseStudy = caseStudy.orEmpty().mapNotNull { block ->
        val questionTitle = block.question?.title?.takeIf { it.isNotBlank() } ?: return@mapNotNull null
        val items = block.questionCaseStudy.map { item ->
            EvaluationCaseItemRequestDto(
                title = item.caseContent,
                fileContent = item.expectedResolution,
            )
        }
        EvaluationCaseBlockRequestDto(
            question = EvaluationQuestionRequestDto(
                title = questionTitle,
                point = (block.question.points).toDouble(),
            ),
            questionCaseStudy = items,
        )
    },
)

fun EvaluationDAO.toLocalSession(index: Int): EvaluationSession = EvaluationSession(
    id = id ?: (1_000L + index),
    title = title,
    domain = "Brouillon",
    description = description,
    status = EvaluationStatus.InProgress,
    progress = 0f,
    score = null,
    questionCount = (option?.size ?: 0) + (ouverte?.size ?: 0) + (caseStudy?.size ?: 0),
    completedQuestions = 0,
    duration = compteur?.takeIf { it > 0 }?.let(::formatMinutes) ?: "A definir",
    updatedAt = "Cree maintenant",
    level = "Personnalise",
    canAnswer = false,
    alreadySubmitted = false,
    startDate = startDate,
    endDate = endDate,
)

private fun String?.toQuestionKind(hasOptions: Boolean): EvaluationQuestionKind {
    val raw = orEmpty().lowercase()
    return when {
        raw.contains("ouverte") || raw.contains("open") -> EvaluationQuestionKind.Open
        raw.contains("case") || raw.contains("cas") -> EvaluationQuestionKind.CaseStudy
        raw.contains("option") || raw.contains("qcm") || raw.contains("choice") -> EvaluationQuestionKind.Option
        hasOptions -> EvaluationQuestionKind.Option
        else -> EvaluationQuestionKind.Open
    }
}

internal fun formatMinutes(totalMinutes: Long): String {
    val safe = totalMinutes.coerceAtLeast(0L)
    val hours = safe / 60
    val minutes = safe % 60
    return if (hours > 0) {
        "${hours}h ${minutes.toString().padStart(2, '0')}m"
    } else {
        "$minutes min"
    }
}

private fun dateRangeLabel(start: String?, end: String?): String {
    val startValue = start?.takeIf { it.isNotBlank() }
    val endValue = end?.takeIf { it.isNotBlank() }
    return when {
        startValue != null && endValue != null -> "$startValue → $endValue"
        endValue != null -> endValue
        startValue != null -> startValue
        else -> "A definir"
    }
}
