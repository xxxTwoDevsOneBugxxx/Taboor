package com.taboor.features.auth.domain.model

import org.json.JSONObject

data class User(
    val id: String,
    val email: String,
    val fullName: String,
    val token: String = "",
    val phoneNumber: String = "01012345678",
    val preferredLanguage: String? = "ar",
    val role: String = "Customer"
) {
    fun toJson(): JSONObject {
        return JSONObject().apply {
            put("id", id)
            put("email", email)
            put("fullName", fullName)
            put("token", token)
            put("phoneNumber", phoneNumber)
            put("preferredLanguage", preferredLanguage ?: "ar")
            put("role", role)
        }
    }

    companion object {
        fun fromJson(json: JSONObject): User {
            return User(
                id = json.optString("id", ""),
                email = json.optString("email", ""),
                fullName = json.optString("fullName", ""),
                token = json.optString("token", ""),
                phoneNumber = json.optString("phoneNumber", "01012345678"),
                preferredLanguage = json.optString("preferredLanguage", "ar"),
                role = json.optString("role", "Customer")
            )
        }
    }
}
