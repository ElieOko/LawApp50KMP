package emy.partners.lawapp.presentation.pages.auth

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
fun RegisterPage(
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    onLoginClick: () -> Unit = {},
    onGoogleClick: () -> Unit = {},
    onRegisterClick: () -> Unit = {},
) {
    RegisterBuild(
        modifier = modifier,
        onBack = onBack,
        onLoginClick = onLoginClick,
        onGoogleClick = onGoogleClick,
        onRegisterClick = onRegisterClick,
    )
}

@Composable
fun RegisterBuild(
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    onLoginClick: () -> Unit = {},
    onGoogleClick: () -> Unit = {},
    onRegisterClick: () -> Unit = {},
) {
    var nom by remember { mutableStateOf("") }
    var prenom by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var pseudo by remember { mutableStateOf("") }
    var telephone by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("Masculin") }
    var typeCompte by remember { mutableStateOf("Etudiant") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 14.dp)
            .padding(bottom = 90.dp)
    ) {
        AuthBrandHeader(
            title = "Inscription",
            subtitle = "Creez un compte enseignant ou etudiant pour suivre et diffuser des contenus juridiques.",
            onBack = onBack,
        )
        Spacer(Modifier.height(14.dp))
        AuthFormPanel {
            GoogleSignInButton(
                text = "S'inscrire avec Google",
                onClick = onGoogleClick
            )
            Spacer(Modifier.height(14.dp))
            AuthOrDivider()
            Spacer(Modifier.height(14.dp))

            AuthTextField(value = nom, onValueChange = { nom = it }, label = "Nom")
            Spacer(Modifier.height(12.dp))
            AuthTextField(value = prenom, onValueChange = { prenom = it }, label = "Prenom")
            Spacer(Modifier.height(12.dp))
            AuthTextField(
                value = email,
                onValueChange = { email = it },
                label = "Email",
                keyboardType = KeyboardType.Email
            )
            Spacer(Modifier.height(12.dp))
            AuthTextField(value = pseudo, onValueChange = { pseudo = it }, label = "Pseudo")
            Spacer(Modifier.height(12.dp))
            AuthTextField(
                value = telephone,
                onValueChange = { telephone = it },
                label = "Telephone",
                keyboardType = KeyboardType.Phone
            )
            Spacer(Modifier.height(16.dp))
            AuthChoiceChips(
                label = "Genre",
                options = listOf("Masculin", "Feminin"),
                selected = gender,
                onSelected = { gender = it }
            )
            Spacer(Modifier.height(14.dp))
            AuthChoiceChips(
                label = "Type de compte",
                options = listOf("Enseignant", "Etudiant"),
                selected = typeCompte,
                onSelected = { typeCompte = it }
            )
            Spacer(Modifier.height(18.dp))
            AuthPrimaryButton(
                text = "Creer mon compte",
                onClick = onRegisterClick
            )
            Spacer(Modifier.height(14.dp))
            AuthFooterLink(
                prefix = "Deja un compte ?",
                action = "Se connecter",
                onClick = onLoginClick
            )
        }
    }
}

@Composable
@Preview(showBackground = true)
fun RegisterPreview() {
    RegisterBuild()
}
