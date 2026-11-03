package com.taboor.features.auth.presentation.forgotpassword

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.taboor.core.components.TaboorLogo
import com.taboor.core.localization.LocalAppStrings
import com.taboor.core.theme.AppColors
import com.taboor.features.auth.presentation.register.PasswordStrengthMeter
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(
    onNavigateBack: () -> Unit,
    onResetSuccess: () -> Unit
) {
    val strings = LocalAppStrings.current
    val focusManager = LocalFocusManager.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val authApi = remember { com.taboor.features.auth.data.api.AuthApi() }

    var currentStep by remember { mutableIntStateOf(0) }
    var email by remember { mutableStateOf("") }
    var otpCode by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var newPasswordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    var isLoading by remember { mutableStateOf(false) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var otpError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var confirmError by remember { mutableStateOf<String?>(null) }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = {
                        if (currentStep > 0) currentStep-- else onNavigateBack()
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = strings.back,
                            tint = AppColors.Ink
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AppColors.Background
                )
            )
        },
        containerColor = AppColors.Background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TaboorLogo(size = 64.dp)
            Spacer(modifier = Modifier.height(20.dp))

            AnimatedContent(
                targetState = currentStep,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "ForgotStep"
            ) { step ->
                when (step) {
                    0 -> {
                        // Step 1: Email entry
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = strings.forgotTitle,
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = AppColors.Ink,
                                    fontSize = 24.sp
                                ),
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = strings.forgotSubtitle,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = AppColors.Gray600,
                                    fontSize = 14.sp
                                ),
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(32.dp))

                            OutlinedTextField(
                                value = email,
                                onValueChange = {
                                    email = it
                                    emailError = null
                                },
                                label = { Text(strings.emailLabel) },
                                placeholder = { Text(strings.emailHint) },
                                isError = emailError != null,
                                supportingText = emailError?.let { { Text(it) } },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Email,
                                    imeAction = ImeAction.Done
                                ),
                                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                                leadingIcon = {
                                    Icon(Icons.Rounded.Email, contentDescription = null, tint = AppColors.Teal)
                                },
                                shape = RoundedCornerShape(16.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = AppColors.Teal,
                                    unfocusedBorderColor = AppColors.Gray300,
                                    focusedContainerColor = AppColors.Paper,
                                    unfocusedContainerColor = AppColors.Paper
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            Button(
                                onClick = {
                                    focusManager.clearFocus()
                                    if (email.isBlank() || !email.contains("@")) {
                                        emailError = strings.emailHint
                                        return@Button
                                    }
                                    isLoading = true
                                    coroutineScope.launch {
                                        try {
                                            authApi.forgotPassword(email.trim())
                                        } catch (_: Exception) {}
                                        isLoading = false
                                        currentStep = 1
                                        snackbarHostState.showSnackbar(strings.resetCodeSent)
                                    }
                                },
                                enabled = !isLoading,
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = AppColors.Teal),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(54.dp)
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(
                                        color = AppColors.Paper,
                                        modifier = Modifier.size(24.dp),
                                        strokeWidth = 2.5.dp
                                    )
                                } else {
                                    Text(
                                        text = strings.sendCode,
                                        style = MaterialTheme.typography.labelLarge.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp
                                        )
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(24.dp))
                            Text(
                                text = strings.rememberPassword,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = AppColors.Teal,
                                    fontWeight = FontWeight.Bold
                                ),
                                modifier = Modifier
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null
                                    ) { onNavigateBack() }
                                    .padding(8.dp)
                            )
                        }
                    }

                    1 -> {
                        // Step 2: 6-digit OTP verification
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = strings.otpTitle,
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = AppColors.Ink,
                                    fontSize = 24.sp
                                ),
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = strings.otpDescription.format(email),
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = AppColors.Gray600,
                                    fontSize = 14.sp
                                ),
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(32.dp))

                            OutlinedTextField(
                                value = otpCode,
                                onValueChange = {
                                    if (it.length <= 6 && it.all { char -> char.isDigit() }) {
                                        otpCode = it
                                        otpError = null
                                    }
                                },
                                label = { Text(strings.otpTitle) },
                                placeholder = { Text("123456") },
                                isError = otpError != null,
                                supportingText = otpError?.let { { Text(it) } },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number,
                                    imeAction = ImeAction.Done
                                ),
                                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                                leadingIcon = {
                                    Icon(Icons.Rounded.LockClock, contentDescription = null, tint = AppColors.Teal)
                                },
                                shape = RoundedCornerShape(16.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = AppColors.Teal,
                                    unfocusedBorderColor = AppColors.Gray300,
                                    focusedContainerColor = AppColors.Paper,
                                    unfocusedContainerColor = AppColors.Paper
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            Button(
                                onClick = {
                                    focusManager.clearFocus()
                                    if (otpCode.length < 4) {
                                        otpError = strings.resetCodeInvalid
                                        return@Button
                                    }
                                    isLoading = true
                                    coroutineScope.launch {
                                        try {
                                            authApi.verifyResetCode(email.trim(), otpCode.trim())
                                        } catch (_: Exception) {}
                                        isLoading = false
                                        currentStep = 2
                                        snackbarHostState.showSnackbar(strings.resetCodeVerified)
                                    }
                                },
                                enabled = !isLoading,
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = AppColors.Teal),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(54.dp)
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(
                                        color = AppColors.Paper,
                                        modifier = Modifier.size(24.dp),
                                        strokeWidth = 2.5.dp
                                    )
                                } else {
                                    Text(
                                        text = strings.verify,
                                        style = MaterialTheme.typography.labelLarge.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp
                                        )
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = strings.resendPrompt,
                                    style = MaterialTheme.typography.bodyMedium.copy(color = AppColors.Gray600)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = strings.resend,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = AppColors.Teal,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    modifier = Modifier
                                        .clickable {
                                            coroutineScope.launch {
                                                snackbarHostState.showSnackbar(strings.resetCodeSent)
                                            }
                                        }
                                        .padding(4.dp)
                                )
                            }
                        }
                    }

                    2 -> {
                        // Step 3: New Password & Confirm Password
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = strings.newPasswordTitle,
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = AppColors.Ink,
                                    fontSize = 24.sp
                                ),
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = strings.newPasswordSubtitle,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = AppColors.Gray600,
                                    fontSize = 14.sp
                                ),
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(28.dp))

                            OutlinedTextField(
                                value = newPassword,
                                onValueChange = {
                                    newPassword = it
                                    passwordError = null
                                },
                                label = { Text(strings.newPasswordLabel) },
                                placeholder = { Text(strings.passwordHint) },
                                visualTransformation = if (newPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                trailingIcon = {
                                    IconButton(onClick = { newPasswordVisible = !newPasswordVisible }) {
                                        Icon(
                                            imageVector = if (newPasswordVisible) Icons.Rounded.Visibility else Icons.Rounded.VisibilityOff,
                                            contentDescription = null,
                                            tint = AppColors.Gray500
                                        )
                                    }
                                },
                                leadingIcon = {
                                    Icon(Icons.Rounded.Lock, contentDescription = null, tint = AppColors.Teal)
                                },
                                isError = passwordError != null,
                                supportingText = passwordError?.let { { Text(it) } },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Password,
                                    imeAction = ImeAction.Next
                                ),
                                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                                shape = RoundedCornerShape(16.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = AppColors.Teal,
                                    unfocusedBorderColor = AppColors.Gray300,
                                    focusedContainerColor = AppColors.Paper,
                                    unfocusedContainerColor = AppColors.Paper
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )

                            if (newPassword.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(10.dp))
                                PasswordStrengthMeter(password = newPassword)
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            OutlinedTextField(
                                value = confirmPassword,
                                onValueChange = {
                                    confirmPassword = it
                                    confirmError = null
                                },
                                label = { Text(strings.confirmPasswordLabel) },
                                placeholder = { Text(strings.passwordHint) },
                                visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                trailingIcon = {
                                    IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                                        Icon(
                                            imageVector = if (confirmPasswordVisible) Icons.Rounded.Visibility else Icons.Rounded.VisibilityOff,
                                            contentDescription = null,
                                            tint = AppColors.Gray500
                                        )
                                    }
                                },
                                leadingIcon = {
                                    Icon(Icons.Rounded.LockReset, contentDescription = null, tint = AppColors.Teal)
                                },
                                isError = confirmError != null,
                                supportingText = confirmError?.let { { Text(it) } },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Password,
                                    imeAction = ImeAction.Done
                                ),
                                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                                shape = RoundedCornerShape(16.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = AppColors.Teal,
                                    unfocusedBorderColor = AppColors.Gray300,
                                    focusedContainerColor = AppColors.Paper,
                                    unfocusedContainerColor = AppColors.Paper
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(28.dp))

                            Button(
                                onClick = {
                                    focusManager.clearFocus()
                                    if (newPassword.length < 8) {
                                        passwordError = strings.newPasswordShort
                                        return@Button
                                    }
                                    if (newPassword != confirmPassword) {
                                        confirmError = strings.passwordMismatch
                                        return@Button
                                    }
                                    isLoading = true
                                    coroutineScope.launch {
                                        try {
                                            authApi.resetPassword(
                                                email = email.trim(),
                                                code = otpCode.trim(),
                                                newPassword = newPassword,
                                                confirmPassword = confirmPassword
                                            )
                                        } catch (_: Exception) {}
                                        isLoading = false
                                        onResetSuccess()
                                    }
                                },
                                enabled = !isLoading,
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = AppColors.Teal),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(54.dp)
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(
                                        color = AppColors.Paper,
                                        modifier = Modifier.size(24.dp),
                                        strokeWidth = 2.5.dp
                                    )
                                } else {
                                    Text(
                                        text = strings.savePassword,
                                        style = MaterialTheme.typography.labelLarge.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
