package com.taboor.features.auth.presentation.register

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.*
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
fun RegisterScreen(
    viewModel: RegisterViewModel,
    onNavigateToLogin: () -> Unit,
    onRegisterSuccess: () -> Unit
) {
    val strings = LocalAppStrings.current
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            onRegisterSuccess()
        }
    }

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
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // Top Navigation & Step Indicator Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {
                    if (viewModel.goBack()) {
                        onNavigateToLogin()
                    }
                }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = strings.back,
                        tint = AppColors.DeepTeal
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                StepIndicator(current = uiState.currentStep)

                Spacer(modifier = Modifier.weight(1f))

                // Language toggle pill
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = AppColors.Paper,
                    border = androidx.compose.foundation.BorderStroke(1.2.dp, AppColors.Gray200),
                    modifier = Modifier.clickable { LocaleManager.toggleLanguage() }
                ) {
                    Text(
                        text = strings.languageToggleText,
                        fontFamily = Tajawal,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.DeepTeal,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }

            // Animated step content
            AnimatedContent(
                targetState = uiState.currentStep,
                transitionSpec = {
                    fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(300))
                },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                label = "RegisterStepAnimation"
            ) { step ->
                when (step) {
                    0 -> StepEmail(viewModel, uiState, onNavigateToLogin)
                    1 -> StepOtp(viewModel, uiState)
                    else -> StepDetails(viewModel, uiState)
                }
            }
        }
    }
}

// =================================================================
// STEP 0: Email
// =================================================================
@Composable
private fun StepEmail(
    viewModel: RegisterViewModel,
    uiState: RegisterUiState,
    onNavigateToLogin: () -> Unit
) {
    val strings = LocalAppStrings.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
            .padding(bottom = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        TaboorLogo(size = 90.dp, showWordmark = false)

        Spacer(modifier = Modifier.height(20.dp))

        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(AppColors.Amber)
                .shadow(elevation = 6.dp, shape = CircleShape, spotColor = AppColors.Amber)
        )

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = strings.createYourAccount,
            fontFamily = Tajawal,
            fontSize = 25.sp,
            fontWeight = FontWeight.ExtraBold,
            color = AppColors.DeepTeal,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = strings.registerSubtitle,
            fontFamily = Tajawal,
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal,
            color = AppColors.Gray600,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Email Field
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(text = strings.emailLabel, style = AppTextStyles.fieldLabel())
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = uiState.email,
                onValueChange = viewModel::onEmailChanged,
                placeholder = { Text(text = strings.emailHint, style = AppTextStyles.fieldHint()) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Email,
                        contentDescription = null,
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

        uiState.emailError?.let { err ->
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = err,
                fontFamily = Tajawal,
                fontSize = 13.sp,
                color = AppColors.Error,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Send Code Button
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
                .background(Brush.horizontalGradient(listOf(AppColors.DeepTeal, AppColors.Teal)))
                .clickable(enabled = !uiState.isLoading) {
                    viewModel.sendCode(strings.emailLabel)
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
                    text = strings.sendCode,
                    fontFamily = Tajawal,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.Paper
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

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
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = AppColors.Paper,
                border = androidx.compose.foundation.BorderStroke(1.5.dp, AppColors.Gray200),
                modifier = Modifier
                    .weight(1f)
                    .height(54.dp)
                    .clickable { /* Google Sign Up */ }
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(text = "G", fontFamily = Inter, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFFEA4335))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Google", fontFamily = Tajawal, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = AppColors.DeepTeal)
                }
            }

            Surface(
                shape = RoundedCornerShape(14.dp),
                color = AppColors.Paper,
                border = androidx.compose.foundation.BorderStroke(1.5.dp, AppColors.Gray200),
                modifier = Modifier
                    .weight(1f)
                    .height(54.dp)
                    .clickable { /* Facebook Sign Up */ }
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(text = "f", fontFamily = Inter, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1877F2))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Facebook", fontFamily = Tajawal, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = AppColors.DeepTeal)
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Footer: Already have an account? Login
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(text = strings.alreadyHaveAccount, fontFamily = Tajawal, fontSize = 14.sp, color = AppColors.Gray600)
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = strings.loginButton,
                fontFamily = Tajawal,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = AppColors.DeepTeal,
                modifier = Modifier.clickable { onNavigateToLogin() }
            )
        }
    }
}

// =================================================================
// STEP 1: OTP Verification
// =================================================================
@Composable
private fun StepOtp(
    viewModel: RegisterViewModel,
    uiState: RegisterUiState
) {
    val strings = LocalAppStrings.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
            .padding(top = 20.dp, bottom = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Mail Icon Circle
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(AppColors.SoftTeal),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.MarkEmailRead,
                contentDescription = null,
                tint = AppColors.Teal,
                modifier = Modifier.size(42.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = strings.otpTitle,
            fontFamily = Tajawal,
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold,
            color = AppColors.DeepTeal,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = String.format(strings.otpDescription, uiState.email),
            fontFamily = Tajawal,
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal,
            color = AppColors.Gray600,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp
        )

        Spacer(modifier = Modifier.height(36.dp))

        // 6-digit OTP Input Boxes
        OtpBoxes(
            otp = uiState.otpCode,
            onOtpChange = viewModel::onOtpChanged
        )

        uiState.otpError?.let { err ->
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = err,
                fontFamily = Tajawal,
                fontSize = 13.sp,
                color = AppColors.Error,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Verify Button
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
                .background(Brush.horizontalGradient(listOf(AppColors.DeepTeal, AppColors.Teal)))
                .clickable(enabled = !uiState.isLoading) {
                    viewModel.verifyOtp("كود التحقق غير مكتمل")
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
                    text = strings.verify,
                    fontFamily = Tajawal,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.Paper
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Resend row
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(text = strings.resendPrompt, fontFamily = Tajawal, fontSize = 14.sp, color = AppColors.Gray600)
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = strings.resend,
                fontFamily = Tajawal,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = AppColors.Teal,
                modifier = Modifier.clickable {
                    viewModel.sendCode(strings.emailLabel)
                }
            )
        }
    }
}

// =================================================================
// STEP 2: Details & Password
// =================================================================
@Composable
private fun StepDetails(
    viewModel: RegisterViewModel,
    uiState: RegisterUiState
) {
    val strings = LocalAppStrings.current
    var isPasswordVisible by remember { mutableStateOf(false) }
    var isConfirmVisible by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
            .padding(top = 10.dp, bottom = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = strings.completeDetails,
            fontFamily = Tajawal,
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold,
            color = AppColors.DeepTeal,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = strings.detailsSubtitle,
            fontFamily = Tajawal,
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal,
            color = AppColors.Gray600,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(28.dp))

        // Full Name Field
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(text = strings.fullNameLabel, style = AppTextStyles.fieldLabel())
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = uiState.fullName,
                onValueChange = viewModel::onFullNameChanged,
                placeholder = { Text(text = strings.fullNameHint, style = AppTextStyles.fieldHint()) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Person,
                        contentDescription = null,
                        tint = AppColors.Teal,
                        modifier = Modifier.size(20.dp)
                    )
                },
                singleLine = true,
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
            uiState.fullNameError?.let {
                Text(text = it, fontFamily = Tajawal, fontSize = 12.sp, color = AppColors.Error)
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Phone Number Field with Country Code Pill
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(text = strings.phoneLabel, style = AppTextStyles.fieldLabel())
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = uiState.phoneNumber,
                onValueChange = viewModel::onPhoneChanged,
                placeholder = { Text(text = strings.phoneHint, style = AppTextStyles.fieldHint()) },
                leadingIcon = {
                    Row(
                        modifier = Modifier.padding(start = 12.dp, end = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = uiState.countryFlag, fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = uiState.countryDialCode,
                            fontFamily = Tajawal,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppColors.DeepTeal
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                singleLine = true,
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
            uiState.phoneError?.let {
                Text(text = it, fontFamily = Tajawal, fontSize = 12.sp, color = AppColors.Error)
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Password Field
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(text = strings.passwordLabel, style = AppTextStyles.fieldLabel())
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = uiState.password,
                onValueChange = viewModel::onPasswordChanged,
                placeholder = { Text(text = strings.passwordHint, style = AppTextStyles.fieldHint()) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Lock,
                        contentDescription = null,
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
            Spacer(modifier = Modifier.height(6.dp))
            PasswordStrengthMeter(password = uiState.password)
            uiState.passwordError?.let {
                Text(text = it, fontFamily = Tajawal, fontSize = 12.sp, color = AppColors.Error)
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Confirm Password Field
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(text = strings.confirmPasswordLabel, style = AppTextStyles.fieldLabel())
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = uiState.confirmPassword,
                onValueChange = viewModel::onConfirmPasswordChanged,
                placeholder = { Text(text = strings.passwordHint, style = AppTextStyles.fieldHint()) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Lock,
                        contentDescription = null,
                        tint = AppColors.Teal,
                        modifier = Modifier.size(20.dp)
                    )
                },
                trailingIcon = {
                    IconButton(onClick = { isConfirmVisible = !isConfirmVisible }) {
                        Icon(
                            imageVector = if (isConfirmVisible) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
                            contentDescription = null,
                            tint = AppColors.Gray500,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                visualTransformation = if (isConfirmVisible) VisualTransformation.None else PasswordVisualTransformation(),
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
            Spacer(modifier = Modifier.height(6.dp))
            PasswordMatchIndicator(password = uiState.password, confirm = uiState.confirmPassword)
            uiState.confirmPasswordError?.let {
                Text(text = it, fontFamily = Tajawal, fontSize = 12.sp, color = AppColors.Error)
            }
        }

        Spacer(modifier = Modifier.height(30.dp))

        // Submit Button: "إنشاء الحساب"
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
                .background(Brush.horizontalGradient(listOf(AppColors.DeepTeal, AppColors.Teal)))
                .clickable(enabled = !uiState.isLoading) {
                    viewModel.createAccount(
                        nameRequired = "اكتب اسمك بالكامل",
                        phoneRequired = "اكتب رقم هاتفك",
                        passwordWeakMsg = strings.passwordWeak,
                        mismatchMsg = strings.passwordMismatch
                    )
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
                    text = strings.createAccountSubmit,
                    fontFamily = Tajawal,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.Paper
                )
            }
        }
    }
}
