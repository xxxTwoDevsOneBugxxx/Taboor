package com.taboor.features.auth.presentation.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.taboor.features.auth.data.api.AuthApi
import com.taboor.features.auth.domain.model.Country
import com.taboor.features.auth.domain.usecase.RegisterUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RegisterViewModel(
    private val registerUseCase: RegisterUseCase? = null,
    private val authRepository: com.taboor.features.auth.domain.repository.AuthRepository =
        com.taboor.features.auth.data.repository.AuthRepositoryImpl()
) : ViewModel() {

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

    fun onCountrySelected(country: Country) {
        _uiState.update {
            it.copy(
                countryDialCode = country.dialCode,
                countryFlag = country.flag
            )
        }
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
            val result = authRepository.sendVerificationCode(email)
            result.fold(
                onSuccess = {
                    _uiState.update { it.copy(isLoading = false, currentStep = 1) }
                },
                onFailure = { error ->
                    _uiState.update { it.copy(isLoading = false, emailError = error.message) }
                }
            )
        }
    }

    fun verifyOtp(invalidCodeMsg: String) {
        val otp = _uiState.value.otpCode.trim()
        val email = _uiState.value.email.trim()
        if (otp.length < 4) {
            _uiState.update { it.copy(otpError = invalidCodeMsg) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, otpError = null) }
            val result = authRepository.verifyEmail(email, otp)
            result.fold(
                onSuccess = {
                    _uiState.update { it.copy(isLoading = false, currentStep = 2) }
                },
                onFailure = { error ->
                    _uiState.update { it.copy(isLoading = false, otpError = error.message ?: invalidCodeMsg) }
                }
            )
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
        val phone = "${state.countryDialCode}${state.phoneNumber.trim().removePrefix("0")}"
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
        if (state.phoneNumber.trim().isEmpty()) {
            phoneErr = phoneRequired
            hasError = true
        }
        if (password.length < 6) {
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
            val result = registerUseCase?.invoke(name, state.email, phone, password)
            if (result == null || result.isSuccess) {
                _uiState.update { it.copy(isLoading = false, isSuccess = true) }
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        fullNameError = result.exceptionOrNull()?.message
                    )
                }
            }
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
