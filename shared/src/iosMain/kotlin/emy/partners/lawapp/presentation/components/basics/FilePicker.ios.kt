package emy.partners.lawapp.presentation.components.basics

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
actual fun rememberFilePickerLauncher(
    onFilesPicked: (List<PickedFile>) -> Unit
): () -> Unit = remember {
    {
        onFilesPicked(emptyList())
    }
}

