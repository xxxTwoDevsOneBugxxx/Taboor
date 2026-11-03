package com.taboor.features.auth.presentation.register

data class RegisterUiState(
    val currentStep: Int = 0,
    val email: String = "",
    val otpCode: String = "",
    val fullName: String = "",
    val phoneNumber: String = "",
    val countryDialCode: String = "+20",
    val countryFlag: String = "🇪🇬",
    val password: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    val isGoogleLoading: Boolean = false,
    val emailError: String? = null,
    val otpError: String? = null,
    val fullNameError: String? = null,
    val phoneError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null,
    val isSuccess: Boolean = false
)
