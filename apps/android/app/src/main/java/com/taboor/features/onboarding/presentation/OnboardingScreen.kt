package com.taboor.features.onboarding.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.taboor.core.localization.LocalAppStrings
import com.taboor.core.localization.LocaleManager
import com.taboor.core.theme.AppColors
import com.taboor.core.theme.Tajawal
import kotlinx.coroutines.launch

/**
 * Taboor Onboarding Screen
 * Exact port from Flutter: `lib/features/onboarding/presentation/screens/onboarding_screen.dart`
 */
@Composable
fun OnboardingScreen(
    onFinishOnboarding: () -> Unit
) {
    val strings = LocalAppStrings.current
    val coroutineScope = rememberCoroutineScope()

    val pages = remember(strings) {
        listOf(
            OnboardingPageData(
                type = OnboardingIllustrationType.TICKET,
                title = strings.onboardingOneTitle,
                subtitle = strings.onboardingOneSubtitle,
                accent = AppColors.Teal
            ),
            OnboardingPageData(
                type = OnboardingIllustrationType.LIVE,
                title = strings.onboardingTwoTitle,
                subtitle = strings.onboardingTwoSubtitle,
                accent = AppColors.Amber
            ),
            OnboardingPageData(
                type = OnboardingIllustrationType.ALERT,
                title = strings.onboardingThreeTitle,
                subtitle = strings.onboardingThreeSubtitle,
                accent = AppColors.Indigo
            )
        )
    }

    val pagerState = rememberPagerState(pageCount = { pages.size })
    val isLastPage = pagerState.currentPage == pages.size - 1

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.Background)
    ) {
        // Ambient background circles
        OnboardingBackground()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // Top Bar with Language Toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.End
            ) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = AppColors.Paper,
                    border = androidx.compose.foundation.BorderStroke(1.2.dp, AppColors.Gray200),
                    modifier = Modifier.clickable {
                        LocaleManager.toggleLanguage()
                    }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "🌐", fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = strings.languageToggleText,
                            fontFamily = Tajawal,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppColors.DeepTeal
                        )
                    }
                }
            }

            // Pager
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) { pageIndex ->
                val pageData = pages[pageIndex]
                OnboardingPageContent(data = pageData)
            }

            // Dots indicator
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                pages.forEachIndexed { index, pageData ->
                    val isSelected = pagerState.currentPage == index
                    val dotWidth by animateDpAsState(
                        targetValue = if (isSelected) 28.dp else 8.dp,
                        animationSpec = tween(durationMillis = 300),
                        label = "dotWidth"
                    )

                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .size(width = dotWidth, height = 8.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) pageData.accent else AppColors.Gray300)
                    )
                }
            }

            // Bottom Action Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 18.dp)
            ) {
                val buttonBrush = if (isLastPage) {
                    Brush.horizontalGradient(listOf(AppColors.DeepTeal, AppColors.Teal))
                } else {
                    Brush.horizontalGradient(listOf(AppColors.Teal, AppColors.Teal))
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .shadow(
                            elevation = 16.dp,
                            shape = RoundedCornerShape(16.dp),
                            spotColor = AppColors.Teal.copy(alpha = 0.35f)
                        )
                        .clip(RoundedCornerShape(16.dp))
                        .background(buttonBrush)
                        .clickable {
                            if (isLastPage) {
                                LocaleManager.setFirstTime(false)
                                onFinishOnboarding()
                            } else {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                }
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = if (isLastPage) strings.startNow else strings.next,
                            fontFamily = Tajawal,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppColors.Paper
                        )
                        if (!isLastPage) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = AppColors.Paper,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OnboardingPageContent(data: OnboardingPageData) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Animated Illustration
        OnboardingIllustration(type = data.type)

        Spacer(modifier = Modifier.height(16.dp))

        // Center Amber indicator strip
        Box(
            modifier = Modifier
                .size(width = 40.dp, height = 5.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(AppColors.Amber)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Title
        Text(
            text = data.title,
            fontFamily = Tajawal,
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold,
            color = AppColors.DeepTeal,
            textAlign = TextAlign.Center,
            lineHeight = 36.sp
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Subtitle
        Text(
            text = data.subtitle,
            fontFamily = Tajawal,
            fontSize = 15.5.sp,
            fontWeight = FontWeight.Normal,
            color = AppColors.Gray600,
            textAlign = TextAlign.Center,
            lineHeight = 26.sp
        )
    }
}

@Composable
private fun OnboardingBackground() {
    Box(modifier = Modifier.fillMaxSize()) {
        // Top-left soft teal circle
        Box(
            modifier = Modifier
                .offset(x = (-70).dp, y = (-60).dp)
                .size(220.dp)
                .clip(CircleShape)
                .background(AppColors.SoftTeal.copy(alpha = 0.5f))
        )

        // Bottom-right indigo circle
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = (70).dp, y = (-60).dp)
                .size(200.dp)
                .clip(CircleShape)
                .background(AppColors.Indigo.copy(alpha = 0.08f))
        )

        // Bottom-left amber circle
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset(x = (20).dp, y = (50).dp)
                .size(160.dp)
                .clip(CircleShape)
                .background(AppColors.Amber.copy(alpha = 0.1f))
        )
    }
}
