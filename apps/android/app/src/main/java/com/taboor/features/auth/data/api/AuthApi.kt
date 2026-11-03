package com.taboor.features.auth.data.api

import android.util.Log
import com.taboor.core.constants.ApiEndpoints
import com.taboor.core.network.ApiResponse
import com.taboor.features.auth.domain.model.ExternalLoginResponse
import com.taboor.features.auth.domain.model.LoginResponse
import com.taboor.features.auth.domain.model.TokenResponse
import com.taboor.features.auth.domain.model.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets

/**
 * Live HTTP Client for Taboor Auth API.
 * Communicates directly with ASP.NET Core backend at https://taboorapi.runasp.net/api/
 */
class AuthApi {

    companion object {
        private const val TAG = "AuthApi"
        private const val CONNECT_TIMEOUT_MS = 15000
        private const val READ_TIMEOUT_MS = 20000
    }

    suspend fun login(email: String, password: String): ApiResponse<LoginResponse> =
        withContext(Dispatchers.IO) {
            val body = JSONObject().apply {
                put("email", email)
                put("password", password)
            }
            post(ApiEndpoints.url(ApiEndpoints.LOGIN), body) { json ->
                LoginResponse.fromJson(json)
            }
        }

    suspend fun register(
        fullName: String,
        email: String,
        phoneNumber: String,
        password: String,
        confirmPassword: String = password
    ): ApiResponse<User> = withContext(Dispatchers.IO) {
        val body = JSONObject().apply {
            put("fullName", fullName)
            put("email", email)
            put("phoneNumber", phoneNumber)
            put("password", password)
            put("confirmPassword", confirmPassword)
        }
        post(ApiEndpoints.url(ApiEndpoints.REGISTER), body) { json ->
            User.fromJson(json)
        }
    }

    suspend fun sendCode(email: String): ApiResponse<Unit> = withContext(Dispatchers.IO) {
        val body = JSONObject().apply { put("email", email) }
        postEmpty(ApiEndpoints.url(ApiEndpoints.SEND_CODE), body)
    }

    suspend fun verifyEmail(email: String, code: String): ApiResponse<Unit> =
        withContext(Dispatchers.IO) {
            val body = JSONObject().apply {
                put("email", email)
                put("code", code)
            }
            postEmpty(ApiEndpoints.url(ApiEndpoints.VERIFY_EMAIL), body)
        }

    suspend fun forgotPassword(email: String): ApiResponse<Unit> = withContext(Dispatchers.IO) {
        val body = JSONObject().apply { put("email", email) }
        postEmpty(ApiEndpoints.url(ApiEndpoints.FORGOT_PASSWORD), body)
    }

    suspend fun verifyResetCode(email: String, code: String): ApiResponse<Unit> =
        withContext(Dispatchers.IO) {
            val body = JSONObject().apply {
                put("email", email)
                put("code", code)
            }
            postEmpty(ApiEndpoints.url(ApiEndpoints.VERIFY_RESET_CODE), body)
        }

    suspend fun resetPassword(
        email: String,
        code: String,
        newPassword: String,
        confirmPassword: String = newPassword
    ): ApiResponse<Unit> = withContext(Dispatchers.IO) {
        val body = JSONObject().apply {
            put("email", email)
            put("code", code)
            put("newPassword", newPassword)
            put("confirmPassword", confirmPassword)
        }
        postEmpty(ApiEndpoints.url(ApiEndpoints.RESET_PASSWORD), body)
    }

    suspend fun refreshToken(
        accessToken: String,
        refreshToken: String
    ): ApiResponse<TokenResponse> = withContext(Dispatchers.IO) {
        val body = JSONObject().apply {
            put("accessToken", accessToken)
            put("refreshToken", refreshToken)
        }
        post(ApiEndpoints.url(ApiEndpoints.REFRESH_TOKEN), body) { json ->
            TokenResponse.fromJson(json)
        }
    }

    suspend fun externalLogin(idToken: String): ApiResponse<ExternalLoginResponse> =
        withContext(Dispatchers.IO) {
            val body = JSONObject().apply {
                put("idToken", idToken)
            }
            post(ApiEndpoints.url(ApiEndpoints.GOOGLE_MOBILE), body) { json ->
                ExternalLoginResponse.fromJson(json)
            }
        }

    suspend fun facebookMobile(accessToken: String): ApiResponse<ExternalLoginResponse> =
        withContext(Dispatchers.IO) {
            val body = JSONObject().apply {
                put("accessToken", accessToken)
            }
            post(ApiEndpoints.url(ApiEndpoints.FACEBOOK_MOBILE), body) { json ->
                ExternalLoginResponse.fromJson(json)
            }
        }

    suspend fun revokeToken(
        accessToken: String?,
        refreshToken: String
    ): ApiResponse<Unit> = withContext(Dispatchers.IO) {
        val body = JSONObject().apply {
            put("refreshToken", refreshToken)
        }
        val headers = mutableMapOf<String, String>()
        if (!accessToken.isNullOrBlank()) {
            headers["Authorization"] = "Bearer $accessToken"
        }
        post(
            urlStr = ApiEndpoints.url(ApiEndpoints.REVOKE_TOKEN),
            body = body,
            customHeaders = headers
        ) { Unit }
    }

    // ==========================================
    // Internal HTTP Connection Helpers
    // ==========================================

    private fun <T> post(
        urlStr: String,
        body: JSONObject,
        customHeaders: Map<String, String>? = null,
        parser: (JSONObject) -> T
    ): ApiResponse<T> {
        var connection: HttpURLConnection? = null
        return try {
            val url = URL(urlStr)
            connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = CONNECT_TIMEOUT_MS
                readTimeout = READ_TIMEOUT_MS
                doInput = true
                doOutput = true
                setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                setRequestProperty("Accept", "application/json")
                customHeaders?.forEach { (k, v) -> setRequestProperty(k, v) }
            }

            OutputStreamWriter(connection.outputStream, StandardCharsets.UTF_8).use { writer ->
                writer.write(body.toString())
                writer.flush()
            }

            val statusCode = connection.responseCode
            val responseText = readResponse(connection)

            parseResponse(statusCode, responseText, parser)
        } catch (e: Exception) {
            Log.e(TAG, "HTTP POST failed: ${e.message}", e)
            ApiResponse(
                statusCode = 0,
                isSuccess = false,
                message = e.localizedMessage ?: "Network error. Please check your connection."
            )
        } finally {
            connection?.disconnect()
        }
    }

    private fun postEmpty(urlStr: String, body: JSONObject): ApiResponse<Unit> {
        return post(urlStr, body, null) { Unit }
    }

    private fun <T> parseResponse(
        statusCode: Int,
        responseText: String,
        parser: (JSONObject) -> T
    ): ApiResponse<T> {
        return try {
            val json = if (responseText.isNotBlank()) JSONObject(responseText) else JSONObject()
            val isSuccess = json.optBoolean("isSuccess", statusCode in 200..299)
            val message = json.optString("message").takeIf { it.isNotBlank() }

            val errorsList = mutableListOf<String>()
            val errorsJson = json.optJSONArray("errors")
            if (errorsJson != null) {
                for (i in 0 until errorsJson.length()) {
                    errorsList.add(errorsJson.getString(i))
                }
            }

            val dataObj = json.optJSONObject("data")
            val data: T? = if (dataObj != null) {
                parser(dataObj)
            } else if (json.has("data") && !json.isNull("data") && parser(json) != null) {
                try { parser(json) } catch (_: Exception) { null }
            } else {
                null
            }

            ApiResponse(
                statusCode = statusCode,
                isSuccess = isSuccess,
                message = message,
                data = data,
                errors = errorsList.ifEmpty { null }
            )
        } catch (e: Exception) {
            ApiResponse(
                statusCode = statusCode,
                isSuccess = statusCode in 200..299,
                message = responseText.ifBlank { e.localizedMessage }
            )
        }
    }

    private fun readResponse(connection: HttpURLConnection): String {
        val stream = if (connection.responseCode in 200..299) {
            connection.inputStream
        } else {
            connection.errorStream ?: connection.inputStream
        } ?: return ""

        return BufferedReader(InputStreamReader(stream, StandardCharsets.UTF_8)).use { reader ->
            val sb = java.lang.StringBuilder()
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                sb.append(line)
            }
            sb.toString()
        }
    }
}
