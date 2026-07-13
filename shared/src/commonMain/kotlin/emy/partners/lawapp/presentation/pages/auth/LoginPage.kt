package emy.partners.lawapp.presentation.pages.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import emy.partners.lawapp.data.remote.auth.AuthRepository
import kotlinx.coroutines.launch

private data class AuthPopup(
    val title: String,
    val message: String,
)

@Composable
fun LoginPage(
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    onRegisterClick: () -> Unit = {},
    onForgotPasswordClick: () -> Unit = {},
    onGoogleClick: () -> Unit = {},
    onLoginSuccess: () -> Unit = {},
) {
    LoginBuild(
        modifier = modifier,
        onBack = onBack,
        onRegisterClick = onRegisterClick,
        onForgotPasswordClick = onForgotPasswordClick,
        onGoogleClick = onGoogleClick,
        onLoginSuccess = onLoginSuccess,
    )
}

@Composable
fun LoginBuild(
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    onRegisterClick: () -> Unit = {},
    onForgotPasswordClick: () -> Unit = {},
    onGoogleClick: () -> Unit = {},
    onLoginSuccess: () -> Unit = {},
) {
    var identifiant by remember { mutableStateOf("") }
    var motDePasse by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var popup by remember { mutableStateOf<AuthPopup?>(null) }
    val scope = rememberCoroutineScope()

    AuthLoadingDialog(visible = isLoading, message = "Connexion en cours...")
    popup?.let { dialog ->
        AuthMessageDialog(
            title = dialog.title,
            message = dialog.message,
            onConfirm = { popup = null }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFE8EEF7))
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 14.dp)
            .padding(top = 36.dp, bottom = 24.dp)
    ) {
        AuthBrandHeader(
            title = "Connexion",
            subtitle = "Accedez a vos evaluations, quiz et contenus juridiques pedagogiques.",
            onBack = onBack,
        )
        Spacer(Modifier.height(18.dp))
        AuthFormPanel {
            GoogleSignInButton(
                text = "Continuer avec Google",
                onClick = onGoogleClick
            )
            Spacer(Modifier.height(14.dp))
            AuthOrDivider()
            Spacer(Modifier.height(14.dp))
            AuthTextField(
                value = identifiant,
                onValueChange = { identifiant = it },
                label = "Email ou pseudo",
                keyboardType = KeyboardType.Email
            )
            Spacer(Modifier.height(12.dp))
            AuthTextField(
                value = motDePasse,
                onValueChange = { motDePasse = it },
                label = "Mot de passe",
                isPassword = true
            )
            TextButton(
                onClick = onForgotPasswordClick,
                modifier = Modifier.align(Alignment.End)
            ) {
                Text(
                    text = "Mot de passe oublie ?",
                    color = AuthColors.AccentBright,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
            Spacer(Modifier.height(8.dp))
            AuthPrimaryButton(
                text = "Se connecter",
                enabled = !isLoading,
                onClick = {
                    if (identifiant.isBlank() || motDePasse.isBlank()) {
                        popup = AuthPopup(
                            title = "Champs requis",
                            message = "Veuillez renseigner votre identifiant et votre mot de passe."
                        )
                        return@AuthPrimaryButton
                    }
                    isLoading = true
                    scope.launch {
                        val result = AuthRepository.login(
                            identifiant = identifiant,
                            password = motDePasse,
                        )
                        isLoading = false
                        result.onSuccess {
                            onLoginSuccess()
                        }.onFailure { error ->
                            popup = AuthPopup(
                                title = "Echec de connexion",
                                message = error.message ?: "Connexion impossible"
                            )
                        }
                    }
                }
            )
            Spacer(Modifier.height(14.dp))
            AuthFooterLink(
                prefix = "Pas encore de compte ?",
                action = "S'inscrire",
                onClick = onRegisterClick
            )
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
@Preview(showBackground = true, widthDp = 390, heightDp = 844)
fun LoginPreview() {
    MaterialTheme {
        LoginBuild(onBack = {})
    }
}
