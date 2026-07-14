package emy.partners.lawapp.presentation.components.basics

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.foundation.layout.size

@Composable
actual fun ProfilePhotoAvatar(
    uri: String?,
    initials: String,
    size: Dp,
    modifier: Modifier,
) {
    val context = LocalContext.current
    val bitmap = remember(uri) {
        runCatching {
            if (uri.isNullOrBlank()) return@runCatching null
            context.contentResolver.openInputStream(Uri.parse(uri))?.use { stream ->
                BitmapFactory.decodeStream(stream)
            }
        }.getOrNull()
    }

    if (bitmap != null) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = modifier
                .size(size)
                .clip(CircleShape)
        )
    } else {
        InitialsAvatar(initials = initials, size = size, modifier = modifier)
    }
}
