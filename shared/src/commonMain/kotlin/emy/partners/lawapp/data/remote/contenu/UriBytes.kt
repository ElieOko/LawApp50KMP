package emy.partners.lawapp.data.remote.contenu

/**
 * Reads the bytes of a local/content URI for multipart upload.
 */
expect fun readUriBytes(uri: String): ByteArray?
