package emy.partners.lawapp.presentation.components.basics

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
actual fun PlatformPdfViewer(
    uri: String,
    modifier: Modifier
) {
    val context = LocalContext.current
    var holder by remember(uri) { mutableStateOf<PdfHolder?>(null) }
    var errorMessage by remember(uri) { mutableStateOf<String?>(null) }
    var pageIndex by remember(uri) { mutableIntStateOf(0) }
    var scale by remember(uri) { mutableFloatStateOf(1f) }
    var offsetX by remember(uri) { mutableFloatStateOf(0f) }
    var offsetY by remember(uri) { mutableFloatStateOf(0f) }

    DisposableEffect(uri) {
        try {
            val descriptor = context.contentResolver.openFileDescriptor(Uri.parse(uri), "r")
            if (descriptor == null) {
                errorMessage = "Impossible d'ouvrir le PDF."
            } else {
                val renderer = PdfRenderer(descriptor)
                holder = PdfHolder(renderer = renderer, descriptor = descriptor)
                errorMessage = null
            }
        } catch (_: Throwable) {
            errorMessage = "Lecture PDF indisponible pour ce fichier."
        }

        onDispose {
            holder?.renderer?.close()
            holder?.descriptor?.close()
            holder = null
        }
    }

    val pdf = holder
    when {
        errorMessage != null -> {
            Text(
                text = errorMessage ?: "",
                color = Color(0xFFB91C1C),
                fontSize = 12.sp,
                modifier = modifier
            )
        }

        pdf == null -> {
            Text(
                text = "Chargement du PDF...",
                color = Color(0xFF334155),
                fontSize = 12.sp,
                modifier = modifier
            )
        }

        else -> {
            val pageCount = pdf.renderer.pageCount.coerceAtLeast(1)
            if (pageIndex >= pageCount) {
                pageIndex = pageCount - 1
            }
            LaunchedEffect(pageIndex) {
                scale = 1f
                offsetX = 0f
                offsetY = 0f
            }
            val bitmap = remember(pdf.renderer, pageIndex) {
                renderPdfPage(pdf.renderer, pageIndex)
            }
            Column(
                modifier = modifier,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White)
                        .padding(6.dp)
                ) {
                    if (bitmap != null) {
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = "Apercu PDF",
                            modifier = Modifier
                                .fillMaxWidth()
                                .graphicsLayer(
                                    scaleX = scale,
                                    scaleY = scale,
                                    translationX = offsetX,
                                    translationY = offsetY
                                )
                                .pointerInput(pageIndex) {
                                    detectTransformGestures { _, pan, zoom, _ ->
                                        val newScale = (scale * zoom).coerceIn(1f, 5f)
                                        if (newScale <= 1f) {
                                            offsetX = 0f
                                            offsetY = 0f
                                        } else {
                                            offsetX += pan.x
                                            offsetY += pan.y
                                        }
                                        scale = newScale
                                    }
                                }
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .size(120.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Page introuvable", color = Color(0xFF64748B), fontSize = 12.sp)
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = { pageIndex = (pageIndex - 1).coerceAtLeast(0) },
                        enabled = pageIndex > 0
                    ) {
                        Text("Page -")
                    }
                    Text(
                        text = "Page ${pageIndex + 1}/$pageCount",
                        color = Color(0xFF334155),
                        fontWeight = FontWeight.Bold
                    )
                    OutlinedButton(
                        onClick = { pageIndex = (pageIndex + 1).coerceAtMost(pageCount - 1) },
                        enabled = pageIndex < pageCount - 1
                    ) {
                        Text("Page +")
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = { scale = (scale - 0.25f).coerceAtLeast(1f) }
                    ) {
                        Text("Zoom -")
                    }
                    Text(
                        text = "Zoom ${(scale * 100).toInt()}%",
                        color = Color(0xFF334155),
                        fontWeight = FontWeight.Bold
                    )
                    OutlinedButton(
                        onClick = {
                            scale = 1f
                            offsetX = 0f
                            offsetY = 0f
                        }
                    ) {
                        Text("Reset")
                    }
                    OutlinedButton(
                        onClick = { scale = (scale + 0.25f).coerceAtMost(5f) }
                    ) {
                        Text("Zoom +")
                    }
                }
            }
        }
    }
}

private data class PdfHolder(
    val renderer: PdfRenderer,
    val descriptor: ParcelFileDescriptor
)

private fun renderPdfPage(renderer: PdfRenderer, index: Int): Bitmap? {
    if (index !in 0 until renderer.pageCount) {
        return null
    }
    val page = renderer.openPage(index)
    try {
        val width = (page.width * 2).coerceAtLeast(1)
        val height = (page.height * 2).coerceAtLeast(1)
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        bitmap.eraseColor(android.graphics.Color.WHITE)
        page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
        return bitmap
    } finally {
        page.close()
    }
}

