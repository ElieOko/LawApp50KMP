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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun LoginPage(
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    onRegisterClick: () -> Unit = {},
    onGoogleClick: () -> Unit = {},
    onLoginClick: () -> Unit = {},
) {
    LoginBuild(
        modifier = modifier,
        onBack = onBack,
        onRegisterClick = onRegisterClick,
        onGoogleClick = onGoogleClick,
        onLoginClick = onLoginClick,
    )
}

@Composable
fun LoginBuild(
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    onRegisterClick: () -> Unit = {},
    onGoogleClick: () -> Unit = {},
    onLoginClick: () -> Unit = {},
) {
    var email by remember { mutableStateOf("") }
    var motDePasse by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFE8EEF7))
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 14.dp, vertical = 12.dp)
            .padding(bottom = 90.dp)
    ) {
        AuthBrandHeader(
            title = "Connexion",
            subtitle = "Accedez a vos evaluations, quiz et contenus juridiques pedagogiques.",
            onBack = onBack,
        )
        Spacer(Modifier.height(14.dp))
        AuthFormPanel {
            GoogleSignInButton(
                text = "Continuer avec Google",
                onClick = onGoogleClick
            )
            Spacer(Modifier.height(14.dp))
            AuthOrDivider()
            Spacer(Modifier.height(14.dp))
            AuthTextField(
                value = email,
                onValueChange = { email = it },
                label = "Email",
                keyboardType = KeyboardType.Email
            )
            Spacer(Modifier.height(12.dp))
            AuthTextField(
                value = motDePasse,
                onValueChange = { motDePasse = it },
                label = "Mot de passe",
                isPassword = true
            )
            Spacer(Modifier.height(18.dp))
            AuthPrimaryButton(
                text = "Se connecter",
                onClick = onLoginClick
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
