package emy.partners.lawapp.presentation.pages.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
    onLoginClick: () -> Unit = {},
    onGoogleClick: () -> Unit = {},
    onRegisterClick: () -> Unit = {},
) {
    RegisterBuild(
        onLoginClick = onLoginClick,
        onGoogleClick = onGoogleClick,
        onRegisterClick = onRegisterClick,
    )
}

@Composable
fun RegisterBuild(
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
        modifier = Modifier
            .fillMaxSize()
            .background(AuthColors.Black)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 28.dp, vertical = 28.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        AuthBrandMark()
        Spacer(Modifier.height(28.dp))
        AuthHeroTitle(
            title = "Rejoignez\nLawApp50",
            subtitle = "Créez votre compte en une minute."
        )

        Spacer(Modifier.height(28.dp))
        GoogleSignInButton(
            text = "S'inscrire avec Google",
            onClick = onGoogleClick
        )
        Spacer(Modifier.height(16.dp))
        AuthOrDivider()
        Spacer(Modifier.height(16.dp))

        AuthTextField(
            value = nom,
            onValueChange = { nom = it },
            label = "Nom"
        )
        Spacer(Modifier.height(12.dp))
        AuthTextField(
            value = prenom,
            onValueChange = { prenom = it },
            label = "Prénom"
        )
        Spacer(Modifier.height(12.dp))
        AuthTextField(
            value = email,
            onValueChange = { email = it },
            label = "Email",
            keyboardType = KeyboardType.Email
        )
        Spacer(Modifier.height(12.dp))
        AuthTextField(
            value = pseudo,
            onValueChange = { pseudo = it },
            label = "Pseudo"
        )
        Spacer(Modifier.height(12.dp))
        AuthTextField(
            value = telephone,
            onValueChange = { telephone = it },
            label = "Téléphone",
            keyboardType = KeyboardType.Phone
        )

        Spacer(Modifier.height(18.dp))
        AuthChoiceChips(
            label = "Genre",
            options = listOf("Masculin", "Féminin"),
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

        Spacer(Modifier.height(24.dp))
        AuthPrimaryButton(
            text = "Créer mon compte",
            onClick = onRegisterClick,
            filled = true
        )
        Spacer(Modifier.height(18.dp))
        AuthFooterLink(
            prefix = "Vous avez déjà un compte ?",
            action = "Se connecter",
            onClick = onLoginClick
        )
        Spacer(Modifier.height(12.dp))
    }
}

@Composable
@Preview(showBackground = true, backgroundColor = 0xFF000000)
fun RegisterPreview() {
    RegisterBuild()
}
