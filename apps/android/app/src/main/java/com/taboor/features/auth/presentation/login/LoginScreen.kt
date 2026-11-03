package com.taboor.features.auth.presentation.login

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.taboor.core.components.TaboorLogo
import com.taboor.core.localization.LocalAppStrings
import com.taboor.core.localization.LocaleManager
import com.taboor.core.theme.AppColors
import com.taboor.core.theme.AppTextStyles
import com.taboor.core.theme.Inter
import com.taboor.core.theme.Tajawal

@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    onForgotPasswordClick: () -> Unit = {},
    onCreateAccountClick: () -> Unit = {},
    onLoginSuccess: () -> Unit = {}
) {
    val strings = LocalAppStrings.current
    val uiState by viewModel.uiState.collectAsState()
    var isPasswordVisible by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.user) {
        if (uiState.user != null) {
            com.taboor.core.services.SessionManager.setUser(uiState.user)
            onLoginSuccess()
        }
    }

    // Gradient background mirroring Flutter _LoginGradient
    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(
            AppColors.SoftTeal.copy(alpha = 0.6f),
            AppColors.Background,
            AppColors.Paper
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = backgroundBrush)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(top = 16.dp, bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Language Toggle Pill (Top-End)
            Row(
                modifier = Modifier.fillMaxWidth(),
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
                        Text(
                            text = "🌐",
                            fontSize = 14.sp
                        )
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

            Spacer(modifier = Modifier.height(16.dp))

            // Taboor Logo Icon (identical to Flutter)
            TaboorLogo(
                size = 90.dp,
                showWordmark = false
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Glowing Amber Dot
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(AppColors.Amber)
                    .shadow(elevation = 6.dp, shape = CircleShape, spotColor = AppColors.Amber)
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Welcome Title
            Text(
                text = strings.loginWelcomeTitle,
                fontFamily = Tajawal,
                fontSize = 25.sp,
                fontWeight = FontWeight.ExtraBold,
                color = AppColors.DeepTeal,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Subtitle
            Text(
                text = strings.loginWelcomeSubtitle,
                fontFamily = Tajawal,
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                color = AppColors.Gray600,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Email Field
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = strings.emailLabel,
                    style = AppTextStyles.fieldLabel()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = uiState.email,
                    onValueChange = viewModel::onEmailChanged,
                    placeholder = {
                        Text(
                            text = strings.emailHint,
                            style = AppTextStyles.fieldHint()
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.Email,
                            contentDescription = strings.emailLabel,
                            tint = AppColors.Teal,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = AppColors.Paper,
                        unfocusedContainerColor = AppColors.Paper,
                        focusedBorderColor = AppColors.Teal,
                        unfocusedBorderColor = AppColors.Gray200,
                        focusedTextColor = AppColors.DeepTeal,
                        unfocusedTextColor = AppColors.DeepTeal
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Password Field
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = strings.passwordLabel,
                    style = AppTextStyles.fieldLabel()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = uiState.password,
                    onValueChange = viewModel::onPasswordChanged,
                    placeholder = {
                        Text(
                            text = strings.passwordHint,
                            style = AppTextStyles.fieldHint()
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.Lock,
                            contentDescription = strings.passwordLabel,
                            tint = AppColors.Teal,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    trailingIcon = {
                        IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                            Icon(
                                imageVector = if (isPasswordVisible) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
                                contentDescription = null,
                                tint = AppColors.Gray500,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    },
                    visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = AppColors.Paper,
                        unfocusedContainerColor = AppColors.Paper,
                        focusedBorderColor = AppColors.Teal,
                        unfocusedBorderColor = AppColors.Gray200,
                        focusedTextColor = AppColors.DeepTeal,
                        unfocusedTextColor = AppColors.DeepTeal
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Forgot Password
            Spacer(modifier = Modifier.height(10.dp))
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = strings.forgotPassword,
                    fontFamily = Tajawal,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AppColors.Teal,
                    modifier = Modifier.clickable { onForgotPasswordClick() }
                )
            }

            // Error Message (if any)
            uiState.errorMessage?.let { error ->
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = error,
                    fontFamily = Tajawal,
                    fontSize = 13.sp,
                    color = AppColors.Error,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Primary Gradient Login Button
            val buttonGradient = Brush.horizontalGradient(
                colors = listOf(AppColors.DeepTeal, AppColors.Teal)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .shadow(
                        elevation = 12.dp,
                        shape = RoundedCornerShape(16.dp),
                        spotColor = AppColors.Teal.copy(alpha = 0.5f)
                    )
                    .clip(RoundedCornerShape(16.dp))
                    .background(buttonGradient)
                    .clickable(enabled = !uiState.isLoading) {
                        viewModel.login()
                    },
                contentAlignment = Alignment.Center
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = AppColors.Paper,
                        strokeWidth = 2.5.dp
                    )
                } else {
                    Text(
                        text = strings.loginButton,
                        fontFamily = Tajawal,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.Paper
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // "OR" Divider
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f), color = AppColors.Gray200)
                Text(
                    text = strings.or,
                    fontFamily = Tajawal,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = AppColors.Gray500,
                    modifier = Modifier.padding(horizontal = 14.dp)
                )
                HorizontalDivider(modifier = Modifier.weight(1f), color = AppColors.Gray200)
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Social Buttons Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Google Button
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = AppColors.Paper,
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, AppColors.Gray200),
                    modifier = Modifier
                        .weight(1f)
                        .height(54.dp)
                        .clickable { viewModel.loginWithGoogle() }
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "G",
                            fontFamily = Inter,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFEA4335)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Google",
                            fontFamily = Tajawal,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = AppColors.DeepTeal
                        )
                    }
                }

                // Facebook Button
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = AppColors.Paper,
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, AppColors.Gray200),
                    modifier = Modifier
                        .weight(1f)
                        .height(54.dp)
                        .clickable { viewModel.loginWithFacebook() }
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "f",
                            fontFamily = Inter,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1877F2)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Facebook",
                            fontFamily = Tajawal,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = AppColors.DeepTeal
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Create Account Footer
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = strings.noAccountYet,
                    fontFamily = Tajawal,
                    fontSize = 14.sp,
                    color = AppColors.Gray600
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = strings.createAccount,
                    fontFamily = Tajawal,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.DeepTeal,
                    modifier = Modifier.clickable { onCreateAccountClick() }
                )
            }
        }
    }
}
