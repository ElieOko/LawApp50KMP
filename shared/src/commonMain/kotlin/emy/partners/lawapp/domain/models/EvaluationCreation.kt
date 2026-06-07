package emy.partners.lawapp.domain.models

data class EvaluationCreation(
    val id: Long? = null,
    val title: String,
    val description: String,
    var fileContent: String? = null,
    val userId: Long,
    val compteur: Long? = 0,
    val startDate: String,
    val endDate: String
)

data class EvaluationEntity(
    val id: Long? = null,
    val title: String,
    val description: String,
    var fileContent: String? = null,
    val userId: Long,
    val compteur: Long? = 0,
    val startDate: String,
    val endDate: String
)

fun EvaluationCreation.toEntity() = EvaluationEntity(
    id = this.id,
    title = this.title,
    description = this.description,
    fileContent = this.fileContent,
    userId = this.userId,
    compteur = this.compteur,
    startDate = this.startDate,
    endDate = this.endDate
)

data class EvaluationDAO(
    val id: Long? = null,
    val title: String,
    val description: String,
    val compteur: Long? = null,
    var fileContent: String? = null,
    val startDate: String,
    val endDate: String,
    val option: List<QuestionOptionDAO>? = emptyList(),
    val ouverte: List<QuestionOuverteDAO>? = emptyList(),
    val caseStudy: List<QuestionCaseStudyDAO>? = emptyList()
)

data class Question(
    val id: Long? = null,
    val title: String,
    val points: Int = 1
)

data class QuestionOption(
    val id: Long? = null,
    val title: String,
    val isCorrect: Boolean = false
)

data class QuestionOuverte(
    val id: Long? = null,
    val expectedAnswer: String,
    val maxLength: Int? = null
)

data class QuestionCaseStudy(
    val id: Long? = null,
    val caseContent: String,
    val expectedResolution: String
)

data class QuestionOptionDAO(
    val question: Question? = null,
    val questionOption: List<QuestionOption> = emptyList()
)

data class QuestionOuverteDAO(
    val question: Question? = null,
    val questionOuverte: List<QuestionOuverte> = emptyList()
)

data class QuestionCaseStudyDAO(
    val question: Question? = null,
    val questionCaseStudy: List<QuestionCaseStudy> = emptyList()
)
