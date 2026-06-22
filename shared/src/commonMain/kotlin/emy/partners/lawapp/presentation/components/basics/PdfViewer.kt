package emy.partners.lawapp.presentation.components.basics

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun PlatformPdfViewer(
    uri: String,
    modifier: Modifier = Modifier
)

