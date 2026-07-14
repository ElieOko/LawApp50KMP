package emy.partners.lawapp.presentation.components.basics

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp

@Composable
actual fun ProfilePhotoAvatar(
    uri: String?,
    initials: String,
    size: Dp,
    modifier: Modifier,
) {
    InitialsAvatar(initials = initials, size = size, modifier = modifier)
}
