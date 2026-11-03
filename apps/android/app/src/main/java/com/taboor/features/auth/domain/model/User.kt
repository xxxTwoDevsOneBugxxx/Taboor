package com.taboor.features.auth.domain.model

// Domain Entity (Clean Architecture)
data class User(
    val id: String,
    val email: String,
    val fullName: String,
    val token: String
)
