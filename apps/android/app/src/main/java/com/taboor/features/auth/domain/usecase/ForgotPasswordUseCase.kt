package com.taboor.features.auth.domain.usecase

import com.taboor.core.constants.AppConstants
import com.taboor.features.auth.domain.repository.AuthRepository

class ForgotPasswordUseCase(private val repository: AuthRepository) {
    suspend fun sendResetCode(email: String): Result<Unit> {
        val trimmed = email.trim()
        if (trimmed.isBlank() || !AppConstants.EMAIL_REGEX.matches(trimmed)) {
            return Result.failure(IllegalArgumentException("Invalid email address"))
        }
        return repository.sendPasswordResetCode(trimmed)
    }

    suspend fun verifyCode(email: String, code: String): Result<Unit> {
        if (code.trim().length < 4) {
            return Result.failure(IllegalArgumentException("Invalid verification code"))
        }
        return repository.verifyResetCode(email.trim(), code.trim())
    }

    suspend fun resetPassword(email: String, code: String, newPass: String): Result<Unit> {
        if (newPass.length < AppConstants.MIN_PASSWORD_LENGTH) {
            return Result.failure(IllegalArgumentException("Password must be at least 6 characters"))
        }
        return repository.resetPassword(email.trim(), code.trim(), newPass)
    }
}
