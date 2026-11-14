package com.taboor.features.home.data.repository

import android.content.Context
import android.util.Log
import com.taboor.core.constants.ApiEndpoints
import com.taboor.core.services.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

/** Selects JSON seed data for development or the Taboor API for live data. */
object ServiceDataConfig {
    @Volatile
    var useMockData: Boolean = true
}

internal interface ServiceDataRepository {
    suspend fun fetchSnapshot(): ServiceSeedData
}

internal class JsonSeedServiceDataRepository(private val context: Context) : ServiceDataRepository {
    fun loadSync(): ServiceSeedData = ServiceSeedDataLoader.load(context)
        ?: throw IllegalStateException("Could not load service seed JSON assets")

    override suspend fun fetchSnapshot(): ServiceSeedData = withContext(Dispatchers.IO) {
        delay(150)
        loadSync()
    }
}

/** API adapter mirrors the repository backed by ApiClient on iOS. */
internal class ApiServiceDataRepository : ServiceDataRepository {
    override suspend fun fetchSnapshot(): ServiceSeedData = withContext(Dispatchers.IO) {
        val places = get(ApiEndpoints.url(ApiEndpoints.ALL_PLACES))
        val tickets = get(ApiEndpoints.url(ApiEndpoints.ACTIVE_TICKETS))
        val alerts = get(ApiEndpoints.url(ApiEndpoints.USER_ALERTS))
        val history = get(ApiEndpoints.url(ApiEndpoints.TICKET_HISTORY))
        ServiceSeedDataLoader.decode(places, tickets, alerts, history)
    }

    private fun get(url: String): String {
        val connection = URL(url).openConnection() as HttpURLConnection
        try {
            connection.requestMethod = "GET"
            connection.connectTimeout = 15_000
            connection.readTimeout = 20_000
            connection.setRequestProperty("Accept", "application/json")
            connection.setRequestProperty("X-Client-Type", "mobile-android")
            SessionManager.token?.takeIf(String::isNotBlank)?.let {
                connection.setRequestProperty("Authorization", "Bearer $it")
            }

            val status = connection.responseCode
            val stream = if (status in 200..299) connection.inputStream else connection.errorStream
            val body = stream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty()
            if (status !in 200..299) {
                val message = runCatching { JSONObject(body).optString("message") }.getOrNull()
                throw IllegalStateException(message?.takeIf(String::isNotBlank) ?: "API request failed ($status)")
            }
            val root = runCatching { JSONObject(body) }.getOrNull()
            if (root?.has("success") == true && !root.optBoolean("success", true)) {
                throw IllegalStateException(root.optString("message", "API request failed"))
            }
            return body
        } finally {
            connection.disconnect()
        }
    }
}

internal object ServiceDataRepositoryFactory {
    fun seed(context: Context) = JsonSeedServiceDataRepository(context.applicationContext)

    fun create(context: Context): ServiceDataRepository = if (ServiceDataConfig.useMockData) {
        seed(context)
    } else {
        ApiServiceDataRepository()
    }
}

internal fun logServiceDataError(error: Throwable) {
    Log.e("ServiceDataRepository", "Unable to refresh service data", error)
}
