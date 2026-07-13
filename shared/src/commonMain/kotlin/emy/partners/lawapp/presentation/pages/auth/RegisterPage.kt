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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import emy.partners.lawapp.data.remote.auth.AuthRepository
import emy.partners.lawapp.data.remote.auth.UserRegisterRequest
import kotlinx.coroutines.launch

private data class RegisterPopup(
    val title: String,
    val message: String,
)

@Composable
fun RegisterPage(
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    onLoginClick: () -> Unit = {},
    onGoogleClick: () -> Unit = {},
    onRegisterSuccess: (email: String) -> Unit = {},
) {
    RegisterBuild(
        modifier = modifier,
        onBack = onBack,
        onLoginClick = onLoginClick,
        onGoogleClick = onGoogleClick,
        onRegisterSuccess = onRegisterSuccess,
    )
}

@Composable
fun RegisterBuild(
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    onLoginClick: () -> Unit = {},
    onGoogleClick: () -> Unit = {},
    onRegisterSuccess: (email: String) -> Unit = {},
) {
    var nom by remember { mutableStateOf("") }
    var prenom by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var pseudo by remember { mutableStateOf("") }
    var telephone by remember { mutableStateOf("") }
    var ville by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("Masculin") }
    var typeCompte by remember { mutableStateOf("Etudiant") }
    var isLoading by remember { mutableStateOf(false) }
    var popup by remember { mutableStateOf<RegisterPopup?>(null) }
    val scope = rememberCoroutineScope()

    AuthLoadingDialog(visible = isLoading, message = "Creation du compte...")
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
            Spacer(Modifier.height(12.dp))
            AuthTextField(
                value = ville,
                onValueChange = { ville = it },
                label = "Ville"
            )
            Spacer(Modifier.height(12.dp))
            AuthTextField(
                value = password,
                onValueChange = { password = it },
                label = "Mot de passe",
                isPassword = true
            )
            Spacer(Modifier.height(12.dp))
            AuthTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = "Confirmer le mot de passe",
                isPassword = true
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
            Spacer(Modifier.height(16.dp))
            AuthPrimaryButton(
                text = "Creer mon compte",
                enabled = !isLoading,
                onClick = {
                    when {
                        nom.isBlank() || prenom.isBlank() || email.isBlank() || ville.isBlank() ||
                            password.isBlank() || confirmPassword.isBlank() -> {
                            popup = RegisterPopup(
                                title = "Champs requis",
                                message = "Merci de remplir tous les champs obligatoires."
                            )
                        }
                        password != confirmPassword -> {
                            popup = RegisterPopup(
                                title = "Mot de passe",
                                message = "Les mots de passe ne correspondent pas."
                            )
                        }
                        else -> {
                            isLoading = true
                            scope.launch {
                                val result = AuthRepository.register(
                                    UserRegisterRequest(
                                        email = email.trim(),
                                        password = password,
                                        confirmPassword = confirmPassword,
                                        firstName = nom.trim(),
                                        lastName = prenom.trim(),
                                        city = ville.trim(),
                                        pseudo = pseudo.trim().ifBlank { null },
                                        phone = telephone.trim().ifBlank { null },
                                    )
                                )
                                isLoading = false
                                result.onSuccess {
                                    onRegisterSuccess(email.trim())
                                }.onFailure { error ->
                                    popup = RegisterPopup(
                                        title = "Echec d'inscription",
                                        message = error.message ?: "Inscription impossible"
                                    )
                                }
                            }
                        }
                    }
                }
            )
            Spacer(Modifier.height(14.dp))
            AuthFooterLink(
                prefix = "Deja un compte ?",
                action = "Se connecter",
                onClick = onLoginClick
            )
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
@Preview(showBackground = true, widthDp = 390, heightDp = 844)
fun RegisterPreview() {
    MaterialTheme {
        RegisterBuild(onBack = {})
    }
}
