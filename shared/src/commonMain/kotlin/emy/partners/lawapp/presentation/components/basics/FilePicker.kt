package emy.partners.lawapp.presentation.components.basics

import androidx.compose.runtime.Composable

data class PickedFile(
    val name: String,
    val uri: String,
    val mimeType: String? = null
)

@Composable
expect fun rememberFilePickerLauncher(
    onFilesPicked: (List<PickedFile>) -> Unit
): () -> Unit

