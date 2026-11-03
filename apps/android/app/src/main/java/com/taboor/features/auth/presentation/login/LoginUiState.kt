package com.taboor.features.auth.presentation.login

import com.taboor.features.auth.domain.model.User

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val user: User? = null
)
