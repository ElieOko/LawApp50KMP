package emy.partners.lawapp.presentation.components.basics

import android.content.ContentResolver
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

@Composable
actual fun rememberFilePickerLauncher(
    onFilesPicked: (List<PickedFile>) -> Unit
): () -> Unit {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        val files = uris.map { uri ->
            PickedFile(
                name = uri.readDisplayName(context.contentResolver),
                uri = uri.toString(),
                mimeType = context.contentResolver.getType(uri)
            )
        }
        if (files.isNotEmpty()) {
            onFilesPicked(files)
        }
    }

    return remember(launcher) {
        {
            launcher.launch(
                arrayOf(
                    "image/*",
                    "application/pdf",
                    "text/*",
                    "*/*"
                )
            )
        }
    }
}

private fun Uri.readDisplayName(contentResolver: ContentResolver): String {
    var result = "fichier"
    contentResolver.query(this, null, null, null, null)?.use { cursor ->
        val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        if (nameIndex != -1 && cursor.moveToFirst()) {
            result = cursor.getString(nameIndex)
        }
    }
    return result
}

