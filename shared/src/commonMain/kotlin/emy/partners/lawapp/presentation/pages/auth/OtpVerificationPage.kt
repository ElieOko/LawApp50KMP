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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import emy.partners.lawapp.data.remote.auth.AuthRepository
import kotlinx.coroutines.launch

private data class OtpPopup(
    val title: String,
    val message: String,
)

@Composable
fun OtpVerificationPage(
    modifier: Modifier = Modifier,
    identifier: String,
    destinationLabel: String = identifier,
    onBack: () -> Unit = {},
    onVerifySuccess: () -> Unit = {},
) {
    OtpVerificationBuild(
        modifier = modifier,
        identifier = identifier,
        destinationLabel = destinationLabel,
        onBack = onBack,
        onVerifySuccess = onVerifySuccess,
    )
}

@Composable
fun OtpVerificationBuild(
    modifier: Modifier = Modifier,
    identifier: String,
    destinationLabel: String = identifier,
    onBack: () -> Unit = {},
    onVerifySuccess: () -> Unit = {},
) {
    var otp by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var loadingMessage by remember { mutableStateOf("Verification en cours...") }
    var popup by remember { mutableStateOf<OtpPopup?>(null) }
    val scope = rememberCoroutineScope()

    AuthLoadingDialog(visible = isLoading, message = loadingMessage)
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
            title = "Verification OTP",
            subtitle = "Saisissez le code a 6 caracteres envoye a $destinationLabel. Pensez aussi a verifier vos spams.",
            onBack = onBack,
        )
        Spacer(Modifier.height(18.dp))
        AuthFormPanel {
            Text(
                text = "Code de verification",
                color = AuthColors.TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Le code a ete envoye automatiquement par email. Il peut arriver dans la boite de reception ou dans les spams.",
                color = AuthColors.TextSecondary,
                fontSize = 13.sp,
                lineHeight = 18.sp
            )
            Spacer(Modifier.height(12.dp))
            AuthOtpInput(
                value = otp,
                onValueChange = { otp = it },
                length = 6
            )
            Spacer(Modifier.height(8.dp))
            TextButton(
                onClick = {
                    if (identifier.isBlank()) {
                        popup = OtpPopup(
                            title = "Email manquant",
                            message = "Impossible de renvoyer le code sans adresse email."
                        )
                        return@TextButton
                    }
                    loadingMessage = "Renvoi du code..."
                    isLoading = true
                    scope.launch {
                        val result = AuthRepository.generateOtp(identifier)
                        isLoading = false
                        result.onSuccess {
                            popup = OtpPopup(
                                title = "Code renvoye",
                                message = "Un nouveau code a ete envoye a $destinationLabel. Verifiez aussi vos spams."
                            )
                        }.onFailure { error ->
                            popup = OtpPopup(
                                title = "Renvoi impossible",
                                message = error.message ?: "Impossible de renvoyer le code"
                            )
                        }
                    }
                },
                enabled = !isLoading,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text(
                    text = "Renvoyer le code",
                    color = AuthColors.AccentBright,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
            Spacer(Modifier.height(10.dp))
            AuthPrimaryButton(
                text = "Verifier le code",
                enabled = !isLoading,
                onClick = {
                    when {
                        otp.length != 6 -> {
                            popup = OtpPopup(
                                title = "Code incomplet",
                                message = "Le code OTP doit contenir exactement 6 caracteres."
                            )
                        }
                        identifier.isBlank() -> {
                            popup = OtpPopup(
                                title = "Email manquant",
                                message = "Impossible de valider le code sans adresse email."
                            )
                        }
                        else -> {
                            loadingMessage = "Verification en cours..."
                            isLoading = true
                            scope.launch {
                                val result = AuthRepository.validateOtp(
                                    identifier = identifier,
                                    code = otp,
                                )
                                isLoading = false
                                result.onSuccess {
                                    onVerifySuccess()
                                }.onFailure { error ->
                                    popup = OtpPopup(
                                        title = "Code invalide",
                                        message = error.message ?: "Validation OTP impossible"
                                    )
                                }
                            }
                        }
                    }
                }
            )
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
@Preview(showBackground = true, widthDp = 390, heightDp = 844)
fun OtpVerificationPreview() {
    MaterialTheme {
        OtpVerificationBuild(
            identifier = "email@lawapp50.com",
            destinationLabel = "email@lawapp50.com",
            onBack = {},
        )
    }
}
