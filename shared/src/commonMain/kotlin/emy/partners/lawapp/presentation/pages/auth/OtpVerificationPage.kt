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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun OtpVerificationPage(
    modifier: Modifier = Modifier,
    destinationLabel: String = "votre contact",
    onBack: () -> Unit = {},
    onResendClick: () -> Unit = {},
    onVerifyClick: (otp: String) -> Unit = {},
) {
    OtpVerificationBuild(
        modifier = modifier,
        destinationLabel = destinationLabel,
        onBack = onBack,
        onResendClick = onResendClick,
        onVerifyClick = onVerifyClick,
    )
}

@Composable
fun OtpVerificationBuild(
    modifier: Modifier = Modifier,
    destinationLabel: String = "votre contact",
    onBack: () -> Unit = {},
    onResendClick: () -> Unit = {},
    onVerifyClick: (otp: String) -> Unit = {},
) {
    var otp by remember { mutableStateOf("") }

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
            subtitle = "Entrez le code a 6 caracteres envoye a $destinationLabel.",
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
            Spacer(Modifier.height(12.dp))
            AuthOtpInput(
                value = otp,
                onValueChange = { otp = it },
                length = 6
            )
            Spacer(Modifier.height(8.dp))
            TextButton(
                onClick = onResendClick,
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
                onClick = {
                    if (otp.length == 6) {
                        onVerifyClick(otp)
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
            onBack = {},
            destinationLabel = "email@lawapp50.com"
        )
    }
}
