package emy.partners.lawapp.data.remote.contenu

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.Foundation.NSData
import platform.Foundation.NSURL
import platform.Foundation.dataWithContentsOfURL
import platform.posix.memcpy

@OptIn(ExperimentalForeignApi::class)
actual fun readUriBytes(uri: String): ByteArray? {
    return runCatching {
        val url = NSURL.URLWithString(uri) ?: NSURL.fileURLWithPath(uri)
        val data: NSData = NSData.dataWithContentsOfURL(url) ?: return null
        val length = data.length.toInt()
        if (length <= 0) return ByteArray(0)
        ByteArray(length).also { bytes ->
            bytes.usePinned { pinned ->
                memcpy(pinned.addressOf(0), data.bytes, data.length)
            }
        }
    }.getOrNull()
}
