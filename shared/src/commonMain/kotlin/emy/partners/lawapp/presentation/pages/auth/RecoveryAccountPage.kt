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
import androidx.compose.ui.unit.sp

enum class RecoveryContactType {
    Email,
    Telephone,
}

@Composable
fun RecoveryAccountPage(
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    onContinueClick: (contactType: RecoveryContactType, value: String) -> Unit = { _, _ -> },
) {
    RecoveryAccountBuild(
        modifier = modifier,
        onBack = onBack,
        onContinueClick = onContinueClick,
    )
}

@Composable
fun RecoveryAccountBuild(
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    onContinueClick: (contactType: RecoveryContactType, value: String) -> Unit = { _, _ -> },
) {
    var contactType by remember { mutableStateOf(RecoveryContactType.Email) }
    var email by remember { mutableStateOf("") }
    var telephone by remember { mutableStateOf("") }

    val contactValue = when (contactType) {
        RecoveryContactType.Email -> email.trim()
        RecoveryContactType.Telephone -> telephone.trim()
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
            title = "Mot de passe oublie",
            subtitle = "Indiquez votre email ou votre numero de telephone pour recevoir un code OTP.",
            onBack = onBack,
        )
        Spacer(Modifier.height(18.dp))
        AuthFormPanel {
            AuthChoiceChips(
                label = "Recevoir le code via",
                options = listOf("Email", "Telephone"),
                selected = if (contactType == RecoveryContactType.Email) "Email" else "Telephone",
                onSelected = { option ->
                    contactType = if (option == "Email") {
                        RecoveryContactType.Email
                    } else {
                        RecoveryContactType.Telephone
                    }
                }
            )
            Spacer(Modifier.height(14.dp))
            when (contactType) {
                RecoveryContactType.Email -> AuthTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = "Adresse email",
                    keyboardType = KeyboardType.Email
                )
                RecoveryContactType.Telephone -> AuthTextField(
                    value = telephone,
                    onValueChange = { telephone = it },
                    label = "Numero de telephone",
                    keyboardType = KeyboardType.Phone
                )
            }
            Spacer(Modifier.height(10.dp))
            Text(
                text = "Nous vous enverrons un code a 6 caracteres pour verifier votre identite.",
                color = AuthColors.TextSecondary,
                fontSize = 13.sp,
                lineHeight = 18.sp
            )
            Spacer(Modifier.height(18.dp))
            AuthPrimaryButton(
                text = "Envoyer le code OTP",
                onClick = {
                    if (contactValue.isNotEmpty()) {
                        onContinueClick(contactType, contactValue)
                    }
                }
            )
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
@Preview(showBackground = true, widthDp = 390, heightDp = 844)
fun RecoveryAccountPreview() {
    MaterialTheme {
        RecoveryAccountBuild(onBack = {})
    }
}
