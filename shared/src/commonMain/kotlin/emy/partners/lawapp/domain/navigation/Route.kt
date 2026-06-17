package emy.partners.lawapp.domain.navigation

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed interface AppRoute

@Serializable
sealed interface TopLevelRoute : AppRoute

@Serializable
sealed interface AuthRoute : AppRoute

@Serializable
sealed interface DetailRoute : AppRoute

@Serializable
@SerialName("login")
data object AuthLoginScreen : AuthRoute

@Serializable
@SerialName("forgotPassword")
data object AuthForgotPasswordScreen : AuthRoute

@Serializable
@SerialName("register")
data object AuthRegisterScreen : AuthRoute

@Serializable
@SerialName("home")
data object HomeScreen : TopLevelRoute

@Serializable
@SerialName("profile")
data object ProfilScreen : TopLevelRoute

@Serializable
@SerialName("explores")
data object ExploreScreen : TopLevelRoute

@Serializable
@SerialName("evaluations")
data object EvaluationScreen : TopLevelRoute

@Serializable
@SerialName("settings")
data object SettingScreen : DetailRoute

@Serializable
@SerialName("quiz")
data object QuizScreen : TopLevelRoute

@Serializable
@SerialName("exploreDetail")
data class ExploreDetailScreen(val blogId: Long) : DetailRoute

@Serializable
@SerialName("evaluationCreate")
data object EvaluationCreateScreen : DetailRoute

@Serializable
@SerialName("evaluationDetail")
data class EvaluationDetailScreen (val evaluationId: Long) : DetailRoute

internal fun AppRoute.saveableKey(): String = when (this) {
    AuthLoginScreen -> "auth/login"
    AuthForgotPasswordScreen -> "auth/forgot-password"
    AuthRegisterScreen -> "auth/register"
    HomeScreen -> "home"
    ProfilScreen -> "profile"
    ExploreScreen -> "explore"
    EvaluationScreen -> "evaluations"
    SettingScreen -> "settings"
    QuizScreen -> "quiz"
    is ExploreDetailScreen -> "explore/detail/$blogId"
    EvaluationCreateScreen -> "evaluations/create"
    is EvaluationDetailScreen -> "evaluations/detail/$evaluationId"
}
