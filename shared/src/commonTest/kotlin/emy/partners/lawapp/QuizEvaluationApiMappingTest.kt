package emy.partners.lawapp

import emy.partners.lawapp.data.remote.AppJson
import emy.partners.lawapp.data.remote.evaluation.EvaluationSessionDto
import emy.partners.lawapp.data.remote.evaluation.toCreateRequest
import emy.partners.lawapp.data.remote.evaluation.toSession
import emy.partners.lawapp.data.remote.evaluation.toTakeSheet
import emy.partners.lawapp.data.remote.extractArray
import emy.partners.lawapp.data.remote.quiz.QuizDetailDto
import emy.partners.lawapp.data.remote.quiz.QuizSummaryDto
import emy.partners.lawapp.data.remote.quiz.toPlayContent
import emy.partners.lawapp.data.remote.quiz.toSummary
import emy.partners.lawapp.data.remote.toApiDate
import emy.partners.lawapp.domain.models.EvaluationDAO
import emy.partners.lawapp.domain.models.EvaluationStatus
import emy.partners.lawapp.domain.models.Question
import emy.partners.lawapp.domain.models.QuestionOption
import emy.partners.lawapp.domain.models.QuestionOptionDAO
import kotlinx.serialization.json.decodeFromJsonElement
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class QuizEvaluationApiMappingTest {

    private val json = AppJson

    @Test
    fun parsesPublicEvaluationsAndBuildsTakeSheet() {
        val body = """
            {
              "message":"Liste des évaluations",
              "session":[
                {
                  "id":4,
                  "title":"Evaluation Kotlin",
                  "description":"string",
                  "compteur":0,
                  "fileContent":"string",
                  "startDate":"2026-04-03",
                  "endDate":"2026-04-05",
                  "option":[
                    {
                      "question":{"id":4,"evaluationId":4,"title":"Kotlin mot clé pour declarer des variables","point":5.0},
                      "questionOption":[
                        {"id":8,"questionId":4,"option":"val et var","isValid":true},
                        {"id":9,"questionId":4,"option":"interface et mut","isValid":false},
                        {"id":10,"questionId":4,"option":"list et array","isValid":false}
                      ]
                    }
                  ],
                  "ouverte":[],
                  "caseStudy":[]
                }
              ]
            }
        """.trimIndent()

        val items = json.extractArray(body, "session", "evaluations", "data")
            .map { json.decodeFromJsonElement(EvaluationSessionDto.serializer(), it) }
        assertEquals(1, items.size)
        val dto = items.first()
        val session = dto.toSession(canAnswer = true, alreadySubmitted = false)
        assertNotNull(session)
        assertEquals(4L, session.id)
        assertEquals("Evaluation Kotlin", session.title)
        assertEquals(EvaluationStatus.InProgress, session.status)
        assertEquals(1, session.questionCount)
        assertTrue(session.canAnswer)

        val sheet = dto.toTakeSheet()
        assertNotNull(sheet)
        assertEquals(1, sheet.questions.size)
        assertEquals(4L, sheet.questions.first().id)
        assertEquals(3, sheet.questions.first().options.size)
        assertEquals(8L, sheet.questions.first().options.first().id)
    }

    @Test
    fun mapsCreatedEvaluationToApiRequest() {
        val dao = EvaluationDAO(
            title = "Interro 3",
            description = "Droit civil",
            compteur = 30,
            startDate = "13/08/2026",
            endDate = "14/08/2026",
            option = listOf(
                QuestionOptionDAO(
                    question = Question(title = "Un contrat valide repose sur ?"),
                    questionOption = listOf(
                        QuestionOption(title = "Le consentement", isCorrect = true),
                        QuestionOption(title = "Un temoin", isCorrect = false),
                    )
                )
            )
        )
        val request = dao.toCreateRequest(
            startDateApi = toApiDate(dao.startDate),
            endDateApi = toApiDate(dao.endDate),
        )
        assertEquals("Interro 3", request.title)
        assertEquals("2026-08-13", request.startDate)
        assertEquals("2026-08-14", request.endDate)
        assertEquals(1, request.option.size)
        assertEquals("Le consentement", request.option.first().questionOption.first().option)
        assertTrue(request.option.first().questionOption.first().goal)
    }

    @Test
    fun parsesQuizListAndDetail() {
        val listBody = """
            {
              "message":"Liste des quiz",
              "quiz":[
                {
                  "id":12,
                  "title":"Quiz droit civil",
                  "description":"Contrats",
                  "userId":1,
                  "notionTypeId":2,
                  "createdAt":"2026-04-01T00:00:00",
                  "isActive":true
                }
              ]
            }
        """.trimIndent()
        val quizzes = json.extractArray(listBody, "quiz", "quizzes", "data")
            .mapNotNull {
                json.decodeFromJsonElement(QuizSummaryDto.serializer(), it).toSummary()
            }
        assertEquals(1, quizzes.size)
        assertEquals(12L, quizzes.first().id)
        assertEquals("Quiz droit civil", quizzes.first().title)

        val detailBody = """
            {
              "quiz":{
                "id":12,
                "title":"Quiz droit civil",
                "description":"Contrats",
                "notionTypeId":2,
                "notionType":{"id":2,"code":"DROIT","label":"Droit"},
                "levels":[
                  {
                    "id":1,
                    "title":"Niveau 1",
                    "levelOrder":1,
                    "questions":[
                      {
                        "id":7,
                        "title":"Quel element rend un contrat valable ?",
                        "point":1.0,
                        "options":[
                          {"id":1,"option":"Le consentement","valid":true},
                          {"id":2,"option":"Un avocat","valid":false}
                        ]
                      }
                    ]
                  }
                ]
              }
            }
        """.trimIndent()
        val detailObj = json.extractArray(detailBody, "quiz", "data").first()
        val detail = json.decodeFromJsonElement(QuizDetailDto.serializer(), detailObj).toPlayContent()
        assertNotNull(detail)
        assertEquals(1, detail.levels.size)
        assertEquals("Le consentement", detail.levels.first().questions.first().options.first())
        assertEquals(0, detail.levels.first().questions.first().correctIndex)
    }

    @Test
    fun convertsDisplayDateToApiDate() {
        assertEquals("2026-08-13", toApiDate("13/08/2026"))
        assertEquals("2026-04-03", toApiDate("2026-04-03"))
    }
}
