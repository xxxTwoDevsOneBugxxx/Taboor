package com.taboor

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.core.tween
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalView
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import org.maplibre.android.MapLibre
import com.taboor.features.auth.presentation.forgotpassword.ForgotPasswordScreen
import com.taboor.features.auth.presentation.login.LoginScreen
import com.taboor.features.auth.presentation.login.LoginViewModel
import com.taboor.features.auth.presentation.register.RegisterScreen
import com.taboor.features.auth.presentation.register.RegisterViewModel
import com.taboor.features.home.data.repository.ServiceRepository
import com.taboor.features.home.domain.model.ServiceCategory
import com.taboor.features.home.domain.model.ServicePlace
import com.taboor.features.home.presentation.HomeBottomTab
import com.taboor.features.home.presentation.HomeScreen
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
import com.taboor.core.theme.TaboorTheme

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
                val navController = rememberNavController()
                var currentHomeTab by rememberSaveable { mutableStateOf(HomeBottomTab.HOME) }
                var selectedPlace by remember { mutableStateOf<ServicePlace?>(null) }
                var selectedCategory by rememberSaveable { mutableStateOf<ServiceCategory?>(null) }
                val currentRoute by navController.currentBackStackEntryAsState()
                val view = LocalView.current

                SideEffect {
                    val window = (view.context as android.app.Activity).window
                    val insetsController = androidx.core.view.WindowCompat.getInsetsController(window, view)
                    val isDarkScreen = currentRoute?.destination?.route == AppDestination.QR_SCANNER.name
                    insetsController.isAppearanceLightStatusBars = !isDarkScreen
                    insetsController.isAppearanceLightNavigationBars = !isDarkScreen
                }

                NavHost(
                    navController = navController,
                    startDestination = AppDestination.SPLASH.name,
                    enterTransition = {
                        fadeIn(tween(160)) + scaleIn(initialScale = 0.985f, animationSpec = tween(160))
                    },
                    exitTransition = { fadeOut(tween(120)) },
                    popEnterTransition = {
                        fadeIn(tween(160)) + scaleIn(initialScale = 1.015f, animationSpec = tween(160))
                    },
                    popExitTransition = { fadeOut(tween(120)) }
                ) {
                    composable(AppDestination.SPLASH.name) {
                        SplashScreen(
                            onNavigateToOnboarding = { navController.navigateTo(AppDestination.ONBOARDING) },
                            onNavigateToLogin = { navController.navigateTo(AppDestination.LOGIN) },
                            onNavigateToHome = { navController.navigateAsRoot(AppDestination.HOME) }
                        )
                    }

                    composable(AppDestination.ONBOARDING.name) {
                        OnboardingScreen(
                            onFinishOnboarding = { navController.navigateAsRoot(AppDestination.LOGIN) }
                        )
                    }

                    composable(AppDestination.LOGIN.name) {
                        LoginScreen(
                            viewModel = loginViewModel,
                            onForgotPasswordClick = { navController.navigateTo(AppDestination.FORGOT_PASSWORD) },
                            onCreateAccountClick = { navController.navigateTo(AppDestination.REGISTER) },
                            onLoginSuccess = { navController.navigateAsRoot(AppDestination.HOME) }
                        )
                    }

                    composable(AppDestination.REGISTER.name) {
                        RegisterScreen(
                            viewModel = registerViewModel,
                            onNavigateToLogin = { navController.popBackStack() },
                            onRegisterSuccess = { navController.navigateAsRoot(AppDestination.HOME) }
                        )
                    }

                    composable(AppDestination.FORGOT_PASSWORD.name) {
                        ForgotPasswordScreen(
                            onNavigateBack = { navController.popBackStack() },
                            onResetSuccess = { navController.popBackStack() }
                        )
                    }

                    composable(AppDestination.HOME.name) {
                        HomeScreen(
                            selectedTab = currentHomeTab,
                            onTabChange = { currentHomeTab = it },
                            onNavigateToDetails = { place ->
                                selectedPlace = place
                                navController.navigateTo(AppDestination.SERVICE_DETAILS)
                            },
                            onNavigateToAllServices = { category ->
                                selectedCategory = category
                                navController.navigateTo(AppDestination.ALL_SERVICES)
                            },
                            onNavigateToEditProfile = { navController.navigateTo(AppDestination.EDIT_PROFILE) },
                            onNavigateToChangePassword = { navController.navigateTo(AppDestination.CHANGE_PASSWORD) },
                            onNavigateToHistory = { navController.navigateTo(AppDestination.TICKET_HISTORY) },
                            onNavigateToFavorites = { navController.navigateTo(AppDestination.FAVORITE_PLACES) },
                            onNavigateToAlertPreferences = { navController.navigateTo(AppDestination.SMART_ALERT_PREFERENCES) },
                            onNavigateToLiveTracking = { navController.navigateTo(AppDestination.LIVE_TRACKING) },
                            onOpenQrScanner = { navController.navigateTo(AppDestination.QR_SCANNER) },
                            onSignOut = { navController.navigateAsRoot(AppDestination.LOGIN) }
                        )
                    }

                    composable(AppDestination.ALL_SERVICES.name) {
                        AllServicesScreen(
                            initialCategory = selectedCategory,
                            onNavigateBack = { navController.popBackStack() },
                            onNavigateToDetails = { place ->
                                selectedPlace = place
                                navController.navigateTo(AppDestination.SERVICE_DETAILS)
                            }
                        )
                    }

                    composable(AppDestination.SERVICE_DETAILS.name) {
                        val place = selectedPlace ?: ServiceRepository.places.firstOrNull()
                        if (place != null) {
                            ServiceDetailsScreen(
                                place = place,
                                onNavigateBack = { navController.popBackStack() },
                                onJoinedQueue = {
                                    currentHomeTab = HomeBottomTab.QUEUE
                                    if (!navController.popBackStack(AppDestination.HOME.name, inclusive = false)) {
                                        navController.navigateAsRoot(AppDestination.HOME)
                                    }
                                }
                            )
                        } else {
                            BackHandler { navController.popBackStack() }
                        }
                    }

                    composable(AppDestination.EDIT_PROFILE.name) {
                        EditProfileScreen(onNavigateBack = { navController.popBackStack() })
                    }

                    composable(AppDestination.CHANGE_PASSWORD.name) {
                        ChangePasswordScreen(onNavigateBack = { navController.popBackStack() })
                    }

                    composable(AppDestination.QR_SCANNER.name) {
                        QrScannerScreen(
                            onNavigateBack = { navController.popBackStack() },
                            onScanSuccess = {
                                currentHomeTab = HomeBottomTab.QUEUE
                                navController.popBackStack()
                            }
                        )
                    }

                    composable(AppDestination.LIVE_TRACKING.name) {
                        LiveQueueTrackingScreen(onNavigateBack = { navController.popBackStack() })
                    }

                    composable(AppDestination.TICKET_HISTORY.name) {
                        TicketHistoryScreen(onNavigateBack = { navController.popBackStack() })
                    }

                    composable(AppDestination.FAVORITE_PLACES.name) {
                        FavoritePlacesScreen(
                            onNavigateBack = { navController.popBackStack() },
                            onNavigateToDetails = { place ->
                                selectedPlace = place
                                navController.navigateTo(AppDestination.SERVICE_DETAILS)
                            }
                        )
                    }

                    composable(AppDestination.SMART_ALERT_PREFERENCES.name) {
                        SmartAlertPreferencesScreen(onNavigateBack = { navController.popBackStack() })
                    }
                }
            }
        }
    }
}

private fun NavHostController.navigateTo(destination: AppDestination) {
    navigate(destination.name) {
        launchSingleTop = true
    }
}

private fun NavHostController.navigateAsRoot(destination: AppDestination) {
    navigate(destination.name) {
        popUpTo(graph.id) { inclusive = false }
        launchSingleTop = true
    }
}
