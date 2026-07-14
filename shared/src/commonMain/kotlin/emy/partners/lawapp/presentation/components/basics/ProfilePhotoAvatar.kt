package emy.partners.lawapp.presentation.components.basics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import emy.partners.lawapp.presentation.themes.BlueDark

@Composable
expect fun ProfilePhotoAvatar(
    uri: String?,
    initials: String,
    size: Dp = 64.dp,
    modifier: Modifier = Modifier,
)

@Composable
internal fun InitialsAvatar(
    initials: String,
    size: Dp,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initials.ifBlank { "LA" },
            color = BlueDark,
            fontWeight = FontWeight.ExtraBold,
            fontSize = (size.value * 0.32f).sp
        )
    }
}
