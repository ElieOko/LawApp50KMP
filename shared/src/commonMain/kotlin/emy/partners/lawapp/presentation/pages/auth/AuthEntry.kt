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
}

/**
 * Actions globales pour ouvrir les pages auth depuis n'importe quel ecran.
 */
data class AuthActions(
    val openLogin: () -> Unit = {},
    val openRegister: () -> Unit = {},
)

val LocalAuthActions = staticCompositionLocalOf { AuthActions() }

/**
 * Point d'entree public des pages auth.
 * Utilisable en dehors de [emy.partners.lawapp.App] tout en conservant
 * la structure de [LoginPage] et [RegisterPage].
 */
@Composable
fun AuthEntry(
    modifier: Modifier = Modifier,
    initialDestination: AuthDestination = AuthDestination.Login,
    onAuthenticated: () -> Unit = {},
    onDismiss: (() -> Unit)? = null,
) {
    var destination by remember(initialDestination) {
        mutableStateOf(initialDestination)
    }

    when (destination) {
        AuthDestination.Login -> LoginPage(
            modifier = modifier,
            onBack = { onDismiss?.invoke() },
            onRegisterClick = { destination = AuthDestination.Register },
            onGoogleClick = onAuthenticated,
            onLoginClick = onAuthenticated,
        )
        AuthDestination.Register -> RegisterPage(
            modifier = modifier,
            onBack = { destination = AuthDestination.Login },
            onLoginClick = { destination = AuthDestination.Login },
            onGoogleClick = onAuthenticated,
            onRegisterClick = onAuthenticated,
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
