package com.taboor.features.auth.presentation.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RegisterViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun onEmailChanged(email: String) {
        _uiState.update { it.copy(email = email, emailError = null) }
    }

    fun onOtpChanged(otp: String) {
        if (otp.length <= 6) {
            _uiState.update { it.copy(otpCode = otp, otpError = null) }
        }
    }

    fun onFullNameChanged(name: String) {
        _uiState.update { it.copy(fullName = name, fullNameError = null) }
    }

    fun onPhoneChanged(phone: String) {
        _uiState.update { it.copy(phoneNumber = phone, phoneError = null) }
    }

    fun onPasswordChanged(password: String) {
        _uiState.update { it.copy(password = password, passwordError = null) }
    }

    fun onConfirmPasswordChanged(confirm: String) {
        _uiState.update { it.copy(confirmPassword = confirm, confirmPasswordError = null) }
    }

    fun sendCode(emailRequiredMsg: String) {
        val email = _uiState.value.email.trim()
        if (email.isEmpty()) {
            _uiState.update { it.copy(emailError = emailRequiredMsg) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, emailError = null) }
            // Simulating API call for sending verification code
            delay(1000)
            _uiState.update { it.copy(isLoading = false, currentStep = 1) }
        }
    }

    fun verifyOtp(invalidCodeMsg: String) {
        val otp = _uiState.value.otpCode.trim()
        if (otp.length < 6) {
            _uiState.update { it.copy(otpError = invalidCodeMsg) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, otpError = null) }
            delay(1000)
            _uiState.update { it.copy(isLoading = false, currentStep = 2) }
        }
    }

    fun createAccount(
        nameRequired: String,
        phoneRequired: String,
        passwordWeakMsg: String,
        mismatchMsg: String
    ) {
        val state = _uiState.value
        val name = state.fullName.trim()
        val phone = state.phoneNumber.trim()
        val password = state.password
        val confirm = state.confirmPassword

        var hasError = false
        var nameErr: String? = null
        var phoneErr: String? = null
        var passErr: String? = null
        var confirmErr: String? = null

        if (name.isEmpty()) {
            nameErr = nameRequired
            hasError = true
        }
        if (phone.isEmpty()) {
            phoneErr = phoneRequired
            hasError = true
        }
        if (password.length < 8 || !password.any { it.isUpperCase() } || !password.any { it.isDigit() }) {
            passErr = passwordWeakMsg
            hasError = true
        }
        if (confirm != password) {
            confirmErr = mismatchMsg
            hasError = true
        }

        if (hasError) {
            _uiState.update {
                it.copy(
                    fullNameError = nameErr,
                    phoneError = phoneErr,
                    passwordError = passErr,
                    confirmPasswordError = confirmErr
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            delay(1200)
            _uiState.update { it.copy(isLoading = false, isSuccess = true) }
        }
    }

    fun goBack(): Boolean {
        if (_uiState.value.currentStep > 0) {
            _uiState.update { it.copy(currentStep = it.currentStep - 1) }
            return false // handled internally
        }
        return true // should pop screen
    }
}
