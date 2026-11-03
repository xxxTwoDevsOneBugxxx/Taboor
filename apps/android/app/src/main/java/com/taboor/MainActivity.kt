package com.taboor

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.taboor.core.theme.TaboorTheme
import com.taboor.features.auth.domain.model.User
import com.taboor.features.auth.domain.repository.AuthRepository
import com.taboor.features.auth.domain.usecase.LoginUseCase
import com.taboor.features.auth.presentation.login.LoginScreen
import com.taboor.features.auth.presentation.login.LoginViewModel
import com.taboor.features.auth.presentation.register.RegisterScreen
import com.taboor.features.auth.presentation.register.RegisterViewModel
import com.taboor.features.onboarding.presentation.OnboardingScreen
import com.taboor.features.splash.presentation.SplashScreen

enum class AppDestination {
    SPLASH,
    ONBOARDING,
    LOGIN,
    REGISTER
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val mockRepository = object : AuthRepository {
            override suspend fun login(email: String, password: String): Result<User> {
                return Result.success(
                    User(
                        id = "user_1",
                        email = email,
                        fullName = "Taboor User",
                        token = "taboor_token"
                    )
                )
            }
        }
        val loginUseCase = LoginUseCase(mockRepository)
        val loginViewModel = LoginViewModel(loginUseCase)

        setContent {
            TaboorTheme {
                var currentDestination by remember { mutableStateOf(AppDestination.SPLASH) }

                Crossfade(
                    targetState = currentDestination,
                    animationSpec = tween(durationMillis = 400),
                    label = "ScreenTransition"
                ) { destination ->
                    when (destination) {
                        AppDestination.SPLASH -> {
                            SplashScreen(
                                onNavigateToOnboarding = {
                                    currentDestination = AppDestination.ONBOARDING
                                },
                                onNavigateToLogin = {
                                    currentDestination = AppDestination.LOGIN
                                }
                            )
                        }
                        AppDestination.ONBOARDING -> {
                            OnboardingScreen(
                                onFinishOnboarding = {
                                    currentDestination = AppDestination.LOGIN
                                }
                            )
                        }
                        AppDestination.LOGIN -> {
                            LoginScreen(
                                viewModel = loginViewModel,
                                onForgotPasswordClick = { /* Handle Forgot Password */ },
                                onCreateAccountClick = {
                                    currentDestination = AppDestination.REGISTER
                                }
                            )
                        }
                        AppDestination.REGISTER -> {
                            val registerViewModel: RegisterViewModel = viewModel()
                            RegisterScreen(
                                viewModel = registerViewModel,
                                onNavigateToLogin = {
                                    currentDestination = AppDestination.LOGIN
                                },
                                onRegisterSuccess = {
                                    currentDestination = AppDestination.LOGIN
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
