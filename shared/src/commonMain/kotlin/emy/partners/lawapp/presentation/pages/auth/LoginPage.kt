package emy.partners.lawapp.presentation.pages.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun LoginPage(
    onRegisterClick: () -> Unit = {},
    onGoogleClick: () -> Unit = {},
    onLoginClick: () -> Unit = {},
) {
    LoginBuild(
        onRegisterClick = onRegisterClick,
        onGoogleClick = onGoogleClick,
        onLoginClick = onLoginClick,
    )
}

@Composable
fun LoginBuild(
    onRegisterClick: () -> Unit = {},
    onGoogleClick: () -> Unit = {},
    onLoginClick: () -> Unit = {},
) {
    var email by remember { mutableStateOf("") }
    var motDePasse by remember { mutableStateOf("") }

    Box(modifier = Modifier.fillMaxSize()) {
        AuthScreenBackground(Modifier.fillMaxSize())

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 32.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                AuthBrandMark()
                Spacer(Modifier.height(28.dp))
                AuthHeroTitle(
                    title = "Bon retour",
                    subtitle = "Accédez à votre espace LawApp50."
                )
            }

            Column {
                GoogleSignInButton(
                    text = "Continuer avec Google",
                    onClick = onGoogleClick
                )
                Spacer(Modifier.height(16.dp))
                AuthOrDivider()
                Spacer(Modifier.height(16.dp))

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
                Spacer(Modifier.height(20.dp))
                AuthPrimaryButton(
                    text = "Se connecter",
                    onClick = onLoginClick,
                    filled = true
                )
                Spacer(Modifier.height(10.dp))
                AuthPrimaryButton(
                    text = "Mot de passe oublié ?",
                    onClick = {},
                    filled = false
                )
            }

            Column {
                Spacer(Modifier.height(24.dp))
                AuthFooterLink(
                    prefix = "Pas encore de compte ?",
                    action = "S'inscrire",
                    onClick = onRegisterClick
                )
            }
        }
    }
}

@Composable
@Preview(showBackground = true, backgroundColor = 0xFF07111F)
fun LoginPreview() {
    LoginBuild()
}
