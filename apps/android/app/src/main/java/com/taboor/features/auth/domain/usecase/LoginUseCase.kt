package com.taboor.features.auth.domain.usecase

import com.taboor.features.auth.domain.model.User
import com.taboor.features.auth.domain.repository.AuthRepository

class LoginUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(email: String, password: String): Result<User> {
        if (email.isBlank() || password.isBlank()) {
            return Result.failure(IllegalArgumentException("Email and password cannot be empty"))
        }
        return repository.login(email, password)
    }
}
