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
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import lawapp.shared.generated.resources.Res
import lawapp.shared.generated.resources.google_g
import org.jetbrains.compose.resources.painterResource

internal object AuthColors {
    val Black = Color(0xFF000000)
    val NearBlack = Color(0xFF0A0A0A)
    val Surface = Color(0xFF16181C)
    val Border = Color(0xFF2F3336)
    val Muted = Color(0xFF71767B)
    val White = Color(0xFFFFFFFF)
    val Accent = Color(0xFF1D9BF0)
    val AccentPressed = Color(0xFF1A8CD8)
}

private val PillShape = RoundedCornerShape(999.dp)
private val FieldShape = RoundedCornerShape(12.dp)

@Composable
internal fun AuthBrandMark(modifier: Modifier = Modifier) {
    Text(
        text = "X",
        color = AuthColors.White,
        fontSize = 42.sp,
        fontWeight = FontWeight.Black,
        letterSpacing = (-1).sp,
        modifier = modifier
    )
}

@Composable
internal fun AuthHeroTitle(
    title: String,
    subtitle: String,
) {
    Column {
        Text(
            text = title,
            color = AuthColors.White,
            fontSize = 34.sp,
            lineHeight = 38.sp,
            fontWeight = FontWeight.Black
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = subtitle,
            color = AuthColors.Muted,
            fontSize = 15.sp,
            lineHeight = 20.sp
        )
    }
}

@Composable
internal fun GoogleSignInButton(
    text: String = "Continuer avec Google",
    onClick: () -> Unit = {},
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp),
        shape = PillShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = AuthColors.White,
            contentColor = Color(0xFF0F1419)
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
    ) {
        Icon(
            painter = painterResource(Res.drawable.google_g),
            contentDescription = null,
            tint = Color.Unspecified,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(10.dp))
        Text(
            text = text,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
internal fun AuthOrDivider() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            thickness = 1.dp,
            color = AuthColors.Border
        )
        Text(
            text = "ou",
            color = AuthColors.Muted,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
        )
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            thickness = 1.dp,
            color = AuthColors.Border
        )
    }
}

@Composable
internal fun AuthPrimaryButton(
    text: String,
    onClick: () -> Unit = {},
    filled: Boolean = true,
) {
    if (filled) {
        Button(
            onClick = onClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = PillShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = AuthColors.Accent,
                contentColor = AuthColors.White
            ),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
        ) {
            Text(text = text, fontSize = 15.sp, fontWeight = FontWeight.Bold)
        }
    } else {
        OutlinedButton(
            onClick = onClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = PillShape,
            border = BorderStroke(1.dp, AuthColors.Border),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = Color.Transparent,
                contentColor = AuthColors.White
            )
        ) {
            Text(text = text, fontSize = 15.sp, fontWeight = FontWeight.Bold)
        }
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
            focusedTextColor = AuthColors.White,
            unfocusedTextColor = AuthColors.White,
            focusedBorderColor = AuthColors.Accent,
            unfocusedBorderColor = AuthColors.Border,
            cursorColor = AuthColors.Accent,
            focusedLabelColor = AuthColors.Accent,
            unfocusedLabelColor = AuthColors.Muted,
            focusedContainerColor = AuthColors.Surface,
            unfocusedContainerColor = AuthColors.Surface,
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
            color = AuthColors.Muted,
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
                        .height(42.dp)
                        .clip(PillShape)
                        .background(
                            if (isSelected) AuthColors.Accent.copy(alpha = 0.18f)
                            else AuthColors.Surface
                        )
                        .border(
                            width = 1.dp,
                            color = if (isSelected) AuthColors.Accent else AuthColors.Border,
                            shape = PillShape
                        )
                        .clickable { onSelected(option) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = option,
                        color = if (isSelected) AuthColors.Accent else AuthColors.White,
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
        Text(text = prefix, color = AuthColors.Muted, fontSize = 14.sp)
        Spacer(Modifier.width(4.dp))
        Text(
            text = action,
            color = AuthColors.Accent,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.clickable(onClick = onClick)
        )
    }
}
