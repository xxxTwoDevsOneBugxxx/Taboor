package com.taboor.core.di

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.taboor.features.auth.data.repository.AuthRepositoryImpl
import com.taboor.features.auth.domain.repository.AuthRepository
import com.taboor.features.auth.domain.usecase.ForgotPasswordUseCase
import com.taboor.features.auth.domain.usecase.LoginUseCase
import com.taboor.features.auth.domain.usecase.RegisterUseCase
import com.taboor.features.auth.presentation.login.LoginViewModel
import com.taboor.features.auth.presentation.register.RegisterViewModel
import com.taboor.features.home.data.repository.ServiceRepository
import com.taboor.features.home.domain.repository.IServiceRepository

/**
 * Dependency Container (Service Locator) for Taboor.
 * Supplies singletons and repositories following Clean Architecture without boilerplate.
 */
class AppContainer(private val context: Context) {

    // Repositories
    val authRepository: AuthRepository by lazy {
        AuthRepositoryImpl()
    }

    val serviceRepository: IServiceRepository by lazy {
        ServiceRepository
    }

    // Auth Use Cases
    val loginUseCase: LoginUseCase by lazy {
        LoginUseCase(authRepository)
    }

    val registerUseCase: RegisterUseCase by lazy {
        RegisterUseCase(authRepository)
    }

    val forgotPasswordUseCase: ForgotPasswordUseCase by lazy {
        ForgotPasswordUseCase(authRepository)
    }

    // ViewModel Factories
    fun provideLoginViewModelFactory(): ViewModelProvider.Factory =
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
                    return LoginViewModel(loginUseCase) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
            }
        }

    fun provideRegisterViewModelFactory(): ViewModelProvider.Factory =
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(RegisterViewModel::class.java)) {
                    return RegisterViewModel(registerUseCase) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
            }
        }
}
