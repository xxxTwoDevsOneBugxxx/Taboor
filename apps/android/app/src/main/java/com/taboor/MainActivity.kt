package com.taboor

import android.os.Bundle
import androidx.activity.ComponentActivity
import org.maplibre.android.MapLibre
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.runtime.*
import com.taboor.core.constants.AppConstants
import com.taboor.core.theme.TaboorTheme
import com.taboor.features.auth.presentation.forgotpassword.ForgotPasswordScreen
import com.taboor.features.auth.presentation.login.LoginScreen
import com.taboor.features.auth.presentation.login.LoginViewModel
import com.taboor.features.auth.presentation.register.RegisterScreen
import com.taboor.features.auth.presentation.register.RegisterViewModel
import com.taboor.features.home.data.repository.ServiceRepository
import com.taboor.features.home.domain.model.ServiceCategory
import com.taboor.features.home.domain.model.ServicePlace
import com.taboor.features.home.presentation.HomeScreen
import com.taboor.features.home.presentation.HomeBottomTab
import com.taboor.features.home.presentation.allservices.AllServicesScreen
import com.taboor.features.home.presentation.details.ServiceDetailsScreen
import com.taboor.features.home.presentation.favorites.FavoritePlacesScreen
import com.taboor.features.home.presentation.history.TicketHistoryScreen
import com.taboor.features.home.presentation.profile.ChangePasswordScreen
import com.taboor.features.home.presentation.profile.EditProfileScreen
import com.taboor.features.home.presentation.qr.QrScannerScreen
import com.taboor.features.home.presentation.settings.SmartAlertPreferencesScreen
import com.taboor.features.home.presentation.tracking.LiveQueueTrackingScreen
import com.taboor.features.onboarding.presentation.OnboardingScreen
import com.taboor.features.splash.presentation.SplashScreen

enum class AppDestination {
    SPLASH,
    ONBOARDING,
    LOGIN,
    REGISTER,
    FORGOT_PASSWORD,
    HOME,
    ALL_SERVICES,
    SERVICE_DETAILS,
    EDIT_PROFILE,
    CHANGE_PASSWORD,
    QR_SCANNER,
    LIVE_TRACKING,
    TICKET_HISTORY,
    FAVORITE_PLACES,
    SMART_ALERT_PREFERENCES
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MapLibre.getInstance(this)
        enableEdgeToEdge()

        val appContainer = (application as TaboorApplication).appContainer
        val loginViewModel: LoginViewModel by viewModels { appContainer.provideLoginViewModelFactory() }
        val registerViewModel: RegisterViewModel by viewModels { appContainer.provideRegisterViewModelFactory() }

        setContent {
            TaboorTheme {
                var currentDestination by remember { mutableStateOf(AppDestination.SPLASH) }
                var currentHomeTab by remember { mutableStateOf(HomeBottomTab.HOME) }
                var selectedPlace by remember { mutableStateOf<ServicePlace?>(null) }
                var selectedCategory by remember { mutableStateOf<ServiceCategory?>(null) }

                // Native Android Dynamic System Bars (Light/Dark Status & Nav bar icons)
                val view = androidx.compose.ui.platform.LocalView.current
                if (!view.isInEditMode) {
                    SideEffect {
                        val window = (view.context as android.app.Activity).window
                        val insetsController = androidx.core.view.WindowCompat.getInsetsController(window, view)
                        val isDarkScreen = currentDestination == AppDestination.QR_SCANNER
                        insetsController.isAppearanceLightStatusBars = !isDarkScreen
                        insetsController.isAppearanceLightNavigationBars = !isDarkScreen
                    }
                }

                // System Back Button Handling
                BackHandler(enabled = currentDestination != AppDestination.SPLASH && currentDestination != AppDestination.HOME && currentDestination != AppDestination.LOGIN) {
                    when (currentDestination) {
                        AppDestination.REGISTER,
                        AppDestination.FORGOT_PASSWORD -> currentDestination = AppDestination.LOGIN
                        AppDestination.SERVICE_DETAILS,
                        AppDestination.ALL_SERVICES,
                        AppDestination.EDIT_PROFILE,
                        AppDestination.CHANGE_PASSWORD,
                        AppDestination.QR_SCANNER,
                        AppDestination.LIVE_TRACKING,
                        AppDestination.TICKET_HISTORY,
                        AppDestination.FAVORITE_PLACES,
                        AppDestination.SMART_ALERT_PREFERENCES -> currentDestination = AppDestination.HOME
                        else -> {}
                    }
                }

                Crossfade(
                    targetState = currentDestination,
                    animationSpec = tween(durationMillis = AppConstants.SCREEN_TRANSITION_MS),
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
                                },
                                onNavigateToHome = {
                                    currentDestination = AppDestination.HOME
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
                                onForgotPasswordClick = {
                                    currentDestination = AppDestination.FORGOT_PASSWORD
                                },
                                onCreateAccountClick = {
                                    currentDestination = AppDestination.REGISTER
                                },
                                onLoginSuccess = {
                                    currentDestination = AppDestination.HOME
                                }
                            )
                        }

                        AppDestination.REGISTER -> {
                            RegisterScreen(
                                viewModel = registerViewModel,
                                onNavigateToLogin = {
                                    currentDestination = AppDestination.LOGIN
                                },
                                onRegisterSuccess = {
                                    currentDestination = AppDestination.HOME
                                }
                            )
                        }

                        AppDestination.FORGOT_PASSWORD -> {
                            ForgotPasswordScreen(
                                onNavigateBack = {
                                    currentDestination = AppDestination.LOGIN
                                },
                                onResetSuccess = {
                                    currentDestination = AppDestination.LOGIN
                                }
                            )
                        }

                        AppDestination.HOME -> {
                            HomeScreen(
                                selectedTab = currentHomeTab,
                                onTabChange = { currentHomeTab = it },
                                onNavigateToDetails = { place ->
                                    selectedPlace = place
                                    currentDestination = AppDestination.SERVICE_DETAILS
                                },
                                onNavigateToAllServices = { category ->
                                    selectedCategory = category
                                    currentDestination = AppDestination.ALL_SERVICES
                                },
                                onNavigateToEditProfile = {
                                    currentDestination = AppDestination.EDIT_PROFILE
                                },
                                onNavigateToChangePassword = {
                                    currentDestination = AppDestination.CHANGE_PASSWORD
                                },
                                onNavigateToHistory = {
                                    currentDestination = AppDestination.TICKET_HISTORY
                                },
                                onNavigateToFavorites = {
                                    currentDestination = AppDestination.FAVORITE_PLACES
                                },
                                onNavigateToAlertPreferences = {
                                    currentDestination = AppDestination.SMART_ALERT_PREFERENCES
                                },
                                onNavigateToLiveTracking = {
                                    currentDestination = AppDestination.LIVE_TRACKING
                                },
                                onOpenQrScanner = {
                                    currentDestination = AppDestination.QR_SCANNER
                                },
                                onSignOut = {
                                    currentDestination = AppDestination.LOGIN
                                }
                            )
                        }

                        AppDestination.SERVICE_DETAILS -> {
                            val place = selectedPlace ?: ServiceRepository.places.first()
                            ServiceDetailsScreen(
                                place = place,
                                onNavigateBack = {
                                    currentDestination = AppDestination.HOME
                                },
                                onJoinedQueue = {
                                    currentHomeTab = HomeBottomTab.QUEUE
                                    currentDestination = AppDestination.HOME
                                }
                            )
                        }

                        AppDestination.ALL_SERVICES -> {
                            AllServicesScreen(
                                initialCategory = selectedCategory,
                                onNavigateBack = {
                                    currentDestination = AppDestination.HOME
                                },
                                onNavigateToDetails = { place ->
                                    selectedPlace = place
                                    currentDestination = AppDestination.SERVICE_DETAILS
                                }
                            )
                        }

                        AppDestination.EDIT_PROFILE -> {
                            EditProfileScreen(
                                onNavigateBack = {
                                    currentDestination = AppDestination.HOME
                                }
                            )
                        }

                        AppDestination.CHANGE_PASSWORD -> {
                            ChangePasswordScreen(
                                onNavigateBack = {
                                    currentDestination = AppDestination.HOME
                                }
                            )
                        }

                        AppDestination.QR_SCANNER -> {
                            QrScannerScreen(
                                onNavigateBack = {
                                    currentDestination = AppDestination.HOME
                                },
                                onScanSuccess = {
                                    currentDestination = AppDestination.HOME
                                }
                            )
                        }

                        AppDestination.LIVE_TRACKING -> {
                            LiveQueueTrackingScreen(
                                onNavigateBack = {
                                    currentDestination = AppDestination.HOME
                                }
                            )
                        }

                        AppDestination.TICKET_HISTORY -> {
                            TicketHistoryScreen(
                                onNavigateBack = {
                                    currentDestination = AppDestination.HOME
                                }
                            )
                        }

                        AppDestination.FAVORITE_PLACES -> {
                            FavoritePlacesScreen(
                                onNavigateBack = {
                                    currentDestination = AppDestination.HOME
                                },
                                onNavigateToDetails = { place ->
                                    selectedPlace = place
                                    currentDestination = AppDestination.SERVICE_DETAILS
                                }
                            )
                        }

                        AppDestination.SMART_ALERT_PREFERENCES -> {
                            SmartAlertPreferencesScreen(
                                onNavigateBack = {
                                    currentDestination = AppDestination.HOME
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
