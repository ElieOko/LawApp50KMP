package emy.partners.lawapp.presentation.pages.auth

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow

enum class AuthDestination {
    Login,
    Register,
    Recovery,
    Otp,
}

/**
 * Actions globales pour ouvrir les pages auth depuis n'importe quel ecran.
 */
data class AuthActions(
    val openLogin: () -> Unit = {},
    val openRegister: () -> Unit = {},
)

val LocalAuthActions = staticCompositionLocalOf { AuthActions() }

private data class AuthFlowState(
    val destination: AuthDestination = AuthDestination.Login,
    val recoveryContactType: RecoveryContactType = RecoveryContactType.Email,
    val recoveryContactValue: String = "",
    val otpBackDestination: AuthDestination = AuthDestination.Recovery,
)

/**
 * Point d'entree public des pages auth.
 * Utilisable en dehors de [emy.partners.lawapp.App] tout en conservant
 * la structure des pages Login, Register, Recovery et OTP.
 */
@Composable
fun AuthEntry(
    modifier: Modifier = Modifier,
    initialDestination: AuthDestination = AuthDestination.Login,
    onAuthenticated: () -> Unit = {},
    onDismiss: (() -> Unit)? = null,
) {
    var state by remember(initialDestination) {
        mutableStateOf(AuthFlowState(destination = initialDestination))
    }

    when (state.destination) {
        AuthDestination.Login -> LoginPage(
            modifier = modifier,
            onBack = { onDismiss?.invoke() },
            onRegisterClick = {
                state = state.copy(destination = AuthDestination.Register)
            },
            onForgotPasswordClick = {
                state = state.copy(destination = AuthDestination.Recovery)
            },
            onGoogleClick = onAuthenticated,
            onLoginSuccess = onAuthenticated,
        )
        AuthDestination.Register -> RegisterPage(
            modifier = modifier,
            onBack = { state = state.copy(destination = AuthDestination.Login) },
            onLoginClick = { state = state.copy(destination = AuthDestination.Login) },
            onGoogleClick = onAuthenticated,
            onRegisterSuccess = { email ->
                state = state.copy(
                    destination = AuthDestination.Otp,
                    recoveryContactType = RecoveryContactType.Email,
                    recoveryContactValue = email,
                    otpBackDestination = AuthDestination.Login,
                )
            },
        )
        AuthDestination.Recovery -> RecoveryAccountPage(
            modifier = modifier,
            onBack = { state = state.copy(destination = AuthDestination.Login) },
            onContinueClick = { contactType, value ->
                state = state.copy(
                    destination = AuthDestination.Otp,
                    recoveryContactType = contactType,
                    recoveryContactValue = value,
                    otpBackDestination = AuthDestination.Recovery,
                )
            },
        )
        AuthDestination.Otp -> OtpVerificationPage(
            modifier = modifier,
            identifier = state.recoveryContactValue,
            destinationLabel = state.recoveryContactValue.ifBlank { "votre email" },
            onBack = { state = state.copy(destination = state.otpBackDestination) },
            onVerifySuccess = {
                state = state.copy(destination = AuthDestination.Login)
            },
        )
    }
}

/**
 * Ecran Voyager public pour ouvrir la connexion depuis l'exterieur de App.
 * Conserve la structure de [LoginPage].
 */
data class LoginAuthScreen(
    val onAuthenticated: () -> Unit = {},
) : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        AuthEntry(
            initialDestination = AuthDestination.Login,
            onAuthenticated = {
                if (navigator.canPop) {
                    navigator.pop()
                }
                onAuthenticated()
            },
            onDismiss = {
                if (navigator.canPop) {
                    navigator.pop()
                }
            },
        )
    }
}

/**
 * Ecran Voyager public pour ouvrir l'inscription depuis l'exterieur de App.
 * Conserve la structure de [RegisterPage].
 */
data class RegisterAuthScreen(
    val onAuthenticated: () -> Unit = {},
) : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        AuthEntry(
            initialDestination = AuthDestination.Register,
            onAuthenticated = {
                if (navigator.canPop) {
                    navigator.pop()
                }
                onAuthenticated()
            },
            onDismiss = {
                if (navigator.canPop) {
                    navigator.pop()
                } else {
                    navigator.replace(LoginAuthScreen(onAuthenticated))
                }
            },
        )
    }
}

@Composable
@Preview(showBackground = true, widthDp = 390, heightDp = 844)
fun AuthEntryLoginPreview() {
    MaterialTheme {
        AuthEntry(initialDestination = AuthDestination.Login)
    }
}

@Composable
@Preview(showBackground = true, widthDp = 390, heightDp = 844)
fun AuthEntryRegisterPreview() {
    MaterialTheme {
        AuthEntry(initialDestination = AuthDestination.Register)
    }
}
