package emy.partners.lawapp.data.remote

import io.ktor.client.request.HttpRequestBuilder
import io.ktor.http.HttpHeaders
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

internal val AppJson = Json {
    ignoreUnknownKeys = true
    isLenient = true
    explicitNulls = false
}

internal fun HttpRequestBuilder.withBearerToken(token: String) {
    val clean = token
        .trim()
        .removePrefix("Bearer")
        .removePrefix("bearer")
        .trim()
    require(clean.isNotBlank()) { "Token Bearer manquant" }
    headers.remove(HttpHeaders.Authorization)
    headers.append(HttpHeaders.Authorization, "Bearer $clean")
}

internal fun Json.extractMessage(bodyText: String): String? {
    if (bodyText.isBlank()) return null
    return runCatching {
        parseToJsonElement(bodyText).jsonObject["message"]?.jsonPrimitive?.content
            ?.takeIf { it.isNotBlank() }
    }.getOrNull()
}

internal fun Json.extractArray(
    bodyText: String,
    vararg preferredKeys: String,
): List<JsonElement> {
    if (bodyText.isBlank()) return emptyList()
    val root = runCatching { parseToJsonElement(bodyText) }.getOrNull() ?: return emptyList()
    if (root is JsonArray) return root
    val obj = root as? JsonObject ?: return emptyList()
    for (key in preferredKeys) {
        when (val value = obj[key]) {
            is JsonArray -> return value
            is JsonObject -> if (value["id"] != null) return listOf(value)
            else -> Unit
        }
    }
    obj.values.firstOrNull { it is JsonArray }?.let { return it.jsonArray }
    if (obj["id"] != null) return listOf(obj)
    return emptyList()
}

internal fun Json.extractObject(
    bodyText: String,
    vararg preferredKeys: String,
): JsonObject? {
    if (bodyText.isBlank()) return null
    val root = runCatching { parseToJsonElement(bodyText) }.getOrNull() ?: return null
    if (root is JsonObject && root["id"] != null) return root
    val obj = root as? JsonObject ?: return null
    for (key in preferredKeys) {
        when (val value = obj[key]) {
            is JsonObject -> return value
            is JsonArray -> return value.firstOrNull()?.let { runCatching { it.jsonObject }.getOrNull() }
            else -> Unit
        }
    }
    obj.values.filterIsInstance<JsonObject>().firstOrNull { it["id"] != null }?.let { return it }
    obj.values.filterIsInstance<JsonArray>()
        .firstOrNull()
        ?.firstOrNull()
        ?.let { return runCatching { it.jsonObject }.getOrNull() }
    return obj.takeIf { it.isNotEmpty() }
}

internal fun toApiDate(display: String): String {
    val raw = display.trim()
    if (raw.isBlank()) return raw
    val parts = raw.split("/", "-")
    if (parts.size != 3) return raw.replace("/", "-")
    val first = parts[0]
    val second = parts[1].padStart(2, '0')
    val third = parts[2].padStart(2, '0')
    return if (first.length == 4) {
        "$first-$second-$third"
    } else {
        "$third-$second-${first.padStart(2, '0')}"
    }
}
