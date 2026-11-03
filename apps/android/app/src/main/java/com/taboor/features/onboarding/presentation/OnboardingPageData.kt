package com.taboor.features.onboarding.presentation

import androidx.compose.ui.graphics.Color

data class OnboardingPageData(
    val type: OnboardingIllustrationType,
    val title: String,
    val subtitle: String,
    val accent: Color
)
