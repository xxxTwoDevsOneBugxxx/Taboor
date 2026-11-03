package com.taboor.core.utils

import android.util.Base64
import org.json.JSONObject
import java.nio.charset.StandardCharsets

/**
 * Minimal JWT payload decoder without requiring external heavy JWT libraries.
 * Safely parses claims such as expiration (`exp`), user ID (`sub`), email, and roles.
 */
object JwtDecoder {

    fun getExpiryMillis(token: String?): Long? {
        if (token.isNullOrBlank()) return null
        val parts = token.split(".")
        if (parts.size < 2) return null

        return try {
            val payload = decodePayloadJson(parts[1])
            if (payload.has("exp")) {
                val exp = payload.getLong("exp")
                exp * 1000L
            } else {
                null
            }
        } catch (_: Exception) {
            null
        }
    }

    fun isExpired(token: String?, currentTimeMillis: Long = System.currentTimeMillis()): Boolean {
        val expiry = getExpiryMillis(token) ?: return false
        return expiry < currentTimeMillis
    }

    fun decodePayload(token: String?): Map<String, Any> {
        if (token.isNullOrBlank()) return emptyMap()
        val parts = token.split(".")
        if (parts.size < 2) return emptyMap()

        return try {
            val json = decodePayloadJson(parts[1])
            val map = mutableMapOf<String, Any>()
            val keys = json.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                map[key] = json.get(key)
            }
            map
        } catch (_: Exception) {
            emptyMap()
        }
    }

    private fun decodePayloadJson(base64Payload: String): JSONObject {
        val normalized = base64Payload.replace("-", "+").replace("_", "/")
        val padded = normalized.padEnd(normalized.length + ((4 - normalized.length % 4) % 4), '=')
        val bytes = Base64.decode(padded, Base64.DEFAULT)
        val jsonString = String(bytes, StandardCharsets.UTF_8)
        return JSONObject(jsonString)
    }
}
