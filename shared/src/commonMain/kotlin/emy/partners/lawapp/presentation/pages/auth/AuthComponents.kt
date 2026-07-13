package emy.partners.lawapp.presentation.pages.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import emy.partners.lawapp.presentation.themes.BlueDark

internal object AuthColors {
    val Panel = Color(0xFFF8FAFC)
    val TextPrimary = Color(0xFF0F172A)
    val TextSecondary = Color(0xFF475569)
    val Field = Color.White
    val Border = Color(0xFFCBD5E1)
    val AccentBright = Color(0xFF2563EB)
}

private val ButtonShape = RoundedCornerShape(18.dp)
private val FieldShape = RoundedCornerShape(16.dp)
private val ChipShape = RoundedCornerShape(16.dp)
private val PanelShape = RoundedCornerShape(28.dp)

@Composable
internal fun AuthBrandHeader(
    title: String,
    subtitle: String,
    onBack: (() -> Unit)? = null,
) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(PanelShape)
            .background(
                Brush.linearGradient(
                    listOf(
                        BlueDark.copy(alpha = 0.96f),
                        Color(0xFF08092B).copy(alpha = 0.94f)
                    )
                )
            )
            .padding(18.dp)
    ) {
        if (onBack != null) {
            Text(
                text = "Retour",
                color = Color.White.copy(alpha = 0.9f),
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White.copy(alpha = 0.16f))
                    .clickable(onClick = onBack)
                    .padding(horizontal = 12.dp, vertical = 7.dp)
            )
            Spacer(Modifier.height(14.dp))
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "LA",
                    color = BlueDark,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp
                )
            }
            Column {
                Text(
                    text = "LawApp50",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = "Evaluation et contenus juridiques",
                    color = Color.White.copy(alpha = 0.72f),
                    fontSize = 12.sp
                )
            }
        }

        Spacer(Modifier.height(16.dp))
        Text(
            text = title,
            color = Color.White,
            fontSize = 26.sp,
            fontWeight = FontWeight.ExtraBold
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = subtitle,
            color = Color.White.copy(alpha = 0.75f),
            fontSize = 14.sp,
            lineHeight = 19.sp
        )
    }
}

@Composable
internal fun AuthFormPanel(content: @Composable () -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(PanelShape)
            .background(AuthColors.Panel)
            .border(1.dp, AuthColors.Border, PanelShape)
            .padding(18.dp)
    ) {
        content()
    }
}

@Composable
internal fun GoogleSignInButton(
    text: String = "Continuer avec Google",
    onClick: () -> Unit = {},
) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = ButtonShape,
        border = BorderStroke(1.dp, AuthColors.Border),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = Color.White,
            contentColor = AuthColors.TextPrimary
        )
    ) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(Color(0xFFF1F5F9)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "G",
                color = Color(0xFF4285F4),
                fontWeight = FontWeight.Black,
                fontSize = 14.sp
            )
        }
        Spacer(Modifier.width(10.dp))
        Text(text = text, fontSize = 15.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
internal fun AuthOrDivider() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        HorizontalDivider(modifier = Modifier.weight(1f), color = AuthColors.Border)
        Text("ou", color = AuthColors.TextSecondary, fontSize = 13.sp)
        HorizontalDivider(modifier = Modifier.weight(1f), color = AuthColors.Border)
    }
}

@Composable
internal fun AuthPrimaryButton(
    text: String,
    onClick: () -> Unit = {},
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = ButtonShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = AuthColors.AccentBright,
            contentColor = Color.White
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
    ) {
        Text(text = text, fontSize = 15.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
internal fun AuthTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text,
    isPassword: Boolean = false,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = true,
        shape = FieldShape,
        visualTransformation = if (isPassword) {
            PasswordVisualTransformation()
        } else {
            VisualTransformation.None
        },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = AuthColors.TextPrimary,
            unfocusedTextColor = AuthColors.TextPrimary,
            focusedBorderColor = AuthColors.AccentBright,
            unfocusedBorderColor = AuthColors.Border,
            cursorColor = AuthColors.AccentBright,
            focusedLabelColor = AuthColors.AccentBright,
            unfocusedLabelColor = AuthColors.TextSecondary,
            focusedContainerColor = AuthColors.Field,
            unfocusedContainerColor = AuthColors.Field,
        ),
        modifier = modifier.fillMaxWidth()
    )
}

@Composable
internal fun AuthChoiceChips(
    label: String,
    options: List<String>,
    selected: String,
    onSelected: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = label,
            color = AuthColors.TextSecondary,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            options.forEach { option ->
                val isSelected = option == selected
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .clip(ChipShape)
                        .background(
                            if (isSelected) AuthColors.AccentBright.copy(alpha = 0.12f)
                            else Color.White
                        )
                        .border(
                            width = 1.dp,
                            color = if (isSelected) AuthColors.AccentBright else AuthColors.Border,
                            shape = ChipShape
                        )
                        .clickable { onSelected(option) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = option,
                        color = if (isSelected) AuthColors.AccentBright else AuthColors.TextSecondary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
internal fun AuthFooterLink(
    prefix: String,
    action: String,
    onClick: () -> Unit = {},
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        Text(text = prefix, color = AuthColors.TextSecondary, fontSize = 14.sp)
        Spacer(Modifier.width(4.dp))
        Text(
            text = action,
            color = AuthColors.AccentBright,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.clickable(onClick = onClick)
        )
    }
}
