package emy.partners.lawapp.data.remote.contenu

import android.net.Uri
import emy.partners.lawapp.data.local.AndroidAppContext
import java.io.File

actual fun readUriBytes(uri: String): ByteArray? {
    return runCatching {
        val context = AndroidAppContext.getOrNull() ?: return null
        val parsed = Uri.parse(uri)
        when (parsed.scheme?.lowercase()) {
            "content", "android.resource" -> {
                context.contentResolver.openInputStream(parsed)?.use { it.readBytes() }
            }
            "file" -> {
                val path = parsed.path ?: return null
                File(path).takeIf { it.exists() }?.readBytes()
            }
            else -> {
                // Absolute filesystem path fallback.
                File(uri).takeIf { it.exists() }?.readBytes()
            }
        }
    }.getOrNull()
}
