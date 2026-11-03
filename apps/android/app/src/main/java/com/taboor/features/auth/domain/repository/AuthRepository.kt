package com.taboor.features.auth.domain.repository

import com.taboor.features.auth.domain.model.User

interface AuthRepository {
    suspend fun login(email: String, password: String): Result<User>
}
