package com.taboor.features.auth.domain.model

import org.json.JSONObject

data class LoginResponse(
    val user: User? = null,
    val token: String? = null,
    val refreshToken: String? = null,
    val refreshTokenExpiry: String? = null,
    val isLockedOut: Boolean = false,
    val isBlocked: Boolean = false
) {
    companion object {
        fun fromJson(json: JSONObject): LoginResponse {
            val userObj = json.optJSONObject("user")
            return LoginResponse(
                user = userObj?.let { User.fromJson(it) },
                token = json.optString("token").takeIf { it.isNotBlank() },
                refreshToken = json.optString("refreshToken").takeIf { it.isNotBlank() },
                refreshTokenExpiry = json.optString("refreshTokenExpiry").takeIf { it.isNotBlank() },
                isLockedOut = json.optBoolean("isLockedOut", false),
                isBlocked = json.optBoolean("isBlocked", false)
            )
        }
    }
}

data class TokenResponse(
    val accessToken: String,
    val refreshToken: String,
    val refreshTokenExpiry: String? = null
) {
    companion object {
        fun fromJson(json: JSONObject): TokenResponse {
            return TokenResponse(
                accessToken = json.optString("accessToken", ""),
                refreshToken = json.optString("refreshToken", ""),
                refreshTokenExpiry = json.optString("refreshTokenExpiry").takeIf { it.isNotBlank() }
            )
        }
    }
}

data class ExternalLoginResponse(
    val email: String? = null,
    val isNewUser: Boolean = false,
    val hasPassword: Boolean = false,
    val token: String = "",
    val refreshToken: String = "",
    val refreshTokenExpiry: String? = null,
    val user: User? = null
) {
    companion object {
        fun fromJson(json: JSONObject): ExternalLoginResponse {
            val userObj = json.optJSONObject("user")
            return ExternalLoginResponse(
                email = json.optString("email").takeIf { it.isNotBlank() },
                isNewUser = json.optBoolean("isNewUser", false),
                hasPassword = json.optBoolean("hasPassword", false),
                token = json.optString("token", ""),
                refreshToken = json.optString("refreshToken", ""),
                refreshTokenExpiry = json.optString("refreshTokenExpiry").takeIf { it.isNotBlank() },
                user = userObj?.let { User.fromJson(it) }
            )
        }
    }

    fun toUser(): User {
        return user ?: User(
            id = "ext_${System.currentTimeMillis() % 1000}",
            email = email ?: "user@taboor.app",
            fullName = email?.substringBefore("@")?.replaceFirstChar { it.uppercase() } ?: "مستخدم خارجي",
            token = token
        )
    }
}
