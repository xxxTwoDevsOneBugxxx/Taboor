package com.taboor.core.network

/**
 * Universal Response Envelope mirroring Taboor Backend:
 * `Taboor_Application.Common.Models.ApiResponse<T>`
 */
data class ApiResponse<T>(
    val statusCode: Int = 200,
    val isSuccess: Boolean = true,
    val message: String? = null,
    val data: T? = null,
    val errors: List<String>? = null
)

/**
 * Clean UI State representation for asynchronous network operations.
 */
sealed interface Resource<out T> {
    data object Idle : Resource<Nothing>
    data object Loading : Resource<Nothing>
    data class Success<out T>(val data: T) : Resource<T>
    data class Error(val message: String, val cause: Throwable? = null) : Resource<Nothing>

    val isLoading: Boolean get() = this is Loading
    val isSuccess: Boolean get() = this is Success
    fun dataOrNull(): T? = (this as? Success)?.data
}
