package com.taboor.features.auth.domain.repository

import com.taboor.features.auth.domain.model.User

interface AuthRepository {
    suspend fun login(email: String, password: String): Result<User>
    suspend fun register(fullName: String, email: String, phone: String, password: String): Result<User>
    suspend fun sendVerificationCode(email: String): Result<Unit>
    suspend fun verifyEmail(email: String, code: String): Result<Unit>
    suspend fun sendPasswordResetCode(email: String): Result<Unit>
    suspend fun verifyResetCode(email: String, code: String): Result<Unit>
    suspend fun resetPassword(email: String, code: String, newPassword: String): Result<Unit>
    suspend fun googleLogin(idToken: String): Result<User>
    suspend fun facebookLogin(accessToken: String): Result<User>
    suspend fun logout(): Result<Unit>
    fun getCurrentUser(): User?
}
