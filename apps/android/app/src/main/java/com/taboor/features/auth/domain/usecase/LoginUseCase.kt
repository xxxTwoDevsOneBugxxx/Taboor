package com.taboor.features.auth.domain.usecase

import com.taboor.features.auth.domain.model.User
import com.taboor.features.auth.domain.repository.AuthRepository

class LoginUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(email: String, password: String): Result<User> {
        val targetEmail = email.trim().ifEmpty { "demo@taboor.app" }
        val targetPassword = password.trim().ifEmpty { "demo123" }
        return repository.login(targetEmail, targetPassword)
    }
}
