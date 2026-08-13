package emy.partners.lawapp.data.remote.quiz

import emy.partners.lawapp.domain.models.QuizQuestion
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class QuizNotionTypeDto(
    val id: Long? = null,
    val code: String? = null,
    val label: String? = null,
    val description: String? = null,
    @JsonNames("isActive")
    val active: Boolean? = null,
)

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class QuizSummaryDto(
    val id: Long? = null,
    val title: String? = null,
    val description: String? = null,
    val userId: Long? = null,
    val notionTypeId: Long? = null,
    val notionType: QuizNotionTypeDto? = null,
    val createdAt: String? = null,
    @JsonNames("isActive")
    val active: Boolean? = null,
)

@Serializable
data class QuizOptionDto(
    val id: Long? = null,
    val option: String? = null,
    val valid: Boolean? = null,
    val goal: Boolean? = null,
)

@Serializable
data class QuizQuestionDto(
    val id: Long? = null,
    val title: String? = null,
    val point: Double? = null,
    val options: List<QuizOptionDto> = emptyList(),
)

@Serializable
data class QuizLevelDto(
    val id: Long? = null,
    val title: String? = null,
    val levelOrder: Int? = null,
    val questions: List<QuizQuestionDto> = emptyList(),
)

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class QuizDetailDto(
    val id: Long? = null,
    val title: String? = null,
    val description: String? = null,
    val userId: Long? = null,
    val notionTypeId: Long? = null,
    val notionType: QuizNotionTypeDto? = null,
    val createdAt: String? = null,
    val levels: List<QuizLevelDto> = emptyList(),
    @JsonNames("isActive")
    val active: Boolean? = null,
)

@Serializable
data class QuizProgressUpdateRequest(
    val levelOrder: Int,
    val score: Double,
)

@Serializable
data class QuizUserProgressItemDto(
    val quizId: Long? = null,
    val quizTitle: String? = null,
    val notionTypeId: Long? = null,
    val highestCompletedLevelOrder: Int? = null,
    val totalLevels: Int? = null,
    val score: Double? = null,
    val progressPercent: Double? = null,
    val completed: Boolean? = null,
)

@Serializable
data class QuizNotionTypeProgressDto(
    val notionType: QuizNotionTypeDto? = null,
    val totalQuizzes: Int? = null,
    val completedQuizzes: Int? = null,
    val totalLevels: Int? = null,
    val completedLevels: Int? = null,
    val totalScore: Double? = null,
    val progressPercent: Double? = null,
    val quizzes: List<QuizUserProgressItemDto> = emptyList(),
)

@Serializable
data class QuizUserProgressOverviewDto(
    val userId: Long? = null,
    val notionTypes: List<QuizNotionTypeProgressDto> = emptyList(),
)

data class QuizSummary(
    val id: Long,
    val title: String,
    val description: String,
    val notionTypeId: Long?,
    val notionTypeLabel: String,
    val totalLevels: Int = 0,
    val highestCompletedLevelOrder: Int = 0,
    val progressPercent: Float = 0f,
    val score: Double? = null,
    val completed: Boolean = false,
)

data class QuizPlayContent(
    val id: Long,
    val title: String,
    val description: String,
    val notionTypeLabel: String,
    val levels: List<QuizPlayLevel>,
)

data class QuizPlayLevel(
    val id: Long,
    val title: String,
    val levelOrder: Int,
    val questions: List<QuizQuestion>,
)

fun QuizSummaryDto.toSummary(
    progress: QuizUserProgressItemDto? = null,
): QuizSummary? {
    val quizId = id ?: return null
    val resolvedTitle = title?.takeIf { it.isNotBlank() } ?: return null
    val label = notionType?.label?.takeIf { it.isNotBlank() }
        ?: notionType?.code?.takeIf { it.isNotBlank() }
        ?: "Quiz"
    return QuizSummary(
        id = quizId,
        title = resolvedTitle,
        description = description.orEmpty(),
        notionTypeId = notionTypeId ?: notionType?.id,
        notionTypeLabel = label,
        totalLevels = progress?.totalLevels ?: 0,
        highestCompletedLevelOrder = progress?.highestCompletedLevelOrder ?: 0,
        progressPercent = ((progress?.progressPercent ?: 0.0) / 100.0).toFloat().coerceIn(0f, 1f),
        score = progress?.score,
        completed = progress?.completed == true,
    )
}

fun QuizDetailDto.toPlayContent(): QuizPlayContent? {
    val quizId = id ?: return null
    val resolvedTitle = title?.takeIf { it.isNotBlank() } ?: return null
    val label = notionType?.label?.takeIf { it.isNotBlank() }
        ?: notionType?.code?.takeIf { it.isNotBlank() }
        ?: "Quiz"
    val playLevels = levels
        .sortedBy { it.levelOrder ?: Int.MAX_VALUE }
        .mapIndexed { index, level ->
            QuizPlayLevel(
                id = level.id ?: (index + 1).toLong(),
                title = level.title?.takeIf { it.isNotBlank() } ?: "Niveau ${level.levelOrder ?: (index + 1)}",
                levelOrder = level.levelOrder ?: (index + 1),
                questions = level.questions.mapIndexedNotNull { questionIndex, question ->
                    question.toQuizQuestion(
                        fallbackId = questionIndex + 1L,
                        category = label,
                    )
                },
            )
        }
        .filter { it.questions.isNotEmpty() }
    return QuizPlayContent(
        id = quizId,
        title = resolvedTitle,
        description = description.orEmpty(),
        notionTypeLabel = label,
        levels = playLevels,
    )
}

fun QuizQuestionDto.toQuizQuestion(
    fallbackId: Long,
    category: String,
): QuizQuestion? {
    val questionTitle = title?.takeIf { it.isNotBlank() } ?: return null
    val labels = options.mapNotNull { it.option?.takeIf { option -> option.isNotBlank() } }
    if (labels.isEmpty()) return null
    val correctIndex = options.indexOfFirst { it.valid == true || it.goal == true }
        .takeIf { it >= 0 }
        ?: 0
    return QuizQuestion(
        id = id ?: fallbackId,
        title = questionTitle,
        category = category,
        options = labels,
        correctIndex = correctIndex,
        explanation = "",
    )
}

fun QuizUserProgressOverviewDto.progressByQuizId(): Map<Long, QuizUserProgressItemDto> =
    notionTypes
        .asSequence()
        .flatMap { it.quizzes.asSequence() }
        .mapNotNull { item -> item.quizId?.let { it to item } }
        .toMap()
