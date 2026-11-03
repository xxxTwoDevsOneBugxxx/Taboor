package com.taboor.features.auth.domain.usecase

import com.taboor.features.auth.domain.model.User
import com.taboor.features.auth.domain.repository.AuthRepository

class RegisterUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(
        fullName: String,
        email: String,
        phone: String,
        password: String
    ): Result<User> {
        if (fullName.isBlank()) {
            return Result.failure(IllegalArgumentException("Name cannot be empty"))
        }
        if (email.isBlank()) {
            return Result.failure(IllegalArgumentException("Email cannot be empty"))
        }
        if (password.length < 6) {
            return Result.failure(IllegalArgumentException("Password must be at least 6 characters"))
        }
        return repository.register(fullName.trim(), email.trim(), phone.trim(), password)
    }
}
