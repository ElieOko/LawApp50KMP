package emy.partners.lawapp.domain.models

data class EvaluationSession(
    val id: Long,
    val title: String,
    val domain: String,
    val description: String,
    val status: EvaluationStatus,
    val progress: Float,
    val score: Int?,
    val questionCount: Int,
    val completedQuestions: Int,
    val duration: String,
    val updatedAt: String,
    val level: String
)

enum class EvaluationStatus {
    InProgress,
    Completed
}

data class QuizQuestion(
    val id: Long,
    val title: String,
    val category: String,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String
)
