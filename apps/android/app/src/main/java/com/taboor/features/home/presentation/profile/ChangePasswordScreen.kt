package com.taboor.features.home.presentation.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBackIos
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.taboor.core.localization.LocalAppStrings
import com.taboor.core.theme.AppColors
import com.taboor.core.theme.Tajawal
import com.taboor.features.auth.presentation.register.PasswordStrengthMeter
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Change Password Screen.
 * Pixel-perfect reproduction of Flutter's `change_password_screen.dart`.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangePasswordScreen(
    onNavigateBack: () -> Unit
) {
    val strings = LocalAppStrings.current
    val focusManager = LocalFocusManager.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    androidx.activity.compose.BackHandler(onBack = onNavigateBack)

    var oldPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var oldObscure by remember { mutableStateOf(true) }
    var newObscure by remember { mutableStateOf(true) }
    var confirmObscure by remember { mutableStateOf(true) }

    var isSaving by remember { mutableStateOf(false) }
    var oldError by remember { mutableStateOf<String?>(null) }
    var newError by remember { mutableStateOf<String?>(null) }
    var confirmError by remember { mutableStateOf<String?>(null) }

    fun isStrongPassword(p: String): Boolean {
        if (p.length < 8) return false
        if (!p.any { it.isUpperCase() }) return false
        if (!p.any { it.isDigit() }) return false
        return true
    }

    fun validate(): Boolean {
        var valid = true
        if (oldPassword.isEmpty()) {
            oldError = strings.passwordRequired
            valid = false
        } else {
            oldError = null
        }

        if (!isStrongPassword(newPassword)) {
            newError = strings.passwordWeak
            valid = false
        } else {
            newError = null
        }

        if (confirmPassword != newPassword) {
            confirmError = strings.passwordMismatch
            valid = false
        } else {
            confirmError = null
        }
        return valid
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = strings.profileChangePassword,
                        fontFamily = Tajawal,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.Ink
                    )
                },
                navigationIcon = {
                    Box(
                        modifier = Modifier
                            .padding(8.dp)
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(AppColors.Gray100)
                            .clickable { onNavigateBack() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBackIos,
                            contentDescription = strings.profileBack,
                            tint = AppColors.Ink,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AppColors.Paper)
            )
        },
        containerColor = AppColors.Background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            // White Card Container
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = AppColors.Paper,
                border = androidx.compose.foundation.BorderStroke(1.dp, AppColors.Gray200)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    // Old Password Field
                    Text(
                        text = strings.oldPasswordLabel,
                        fontFamily = Tajawal,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AppColors.DeepTeal
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = oldPassword,
                        onValueChange = {
                            oldPassword = it
                            if (oldError != null) oldError = null
                        },
                        placeholder = { Text(strings.oldPasswordHint, fontFamily = Tajawal, color = AppColors.Gray400, fontSize = 14.sp) },
                        visualTransformation = if (oldObscure) PasswordVisualTransformation() else VisualTransformation.None,
                        leadingIcon = {
                            Icon(Icons.Rounded.Lock, contentDescription = null, tint = AppColors.Teal, modifier = Modifier.size(20.dp))
                        },
                        trailingIcon = {
                            IconButton(onClick = { oldObscure = !oldObscure }) {
                                Icon(
                                    imageVector = if (oldObscure) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                                    contentDescription = null,
                                    tint = AppColors.Gray500,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        },
                        isError = oldError != null,
                        supportingText = oldError?.let { { Text(it, color = AppColors.Error, fontFamily = Tajawal, fontSize = 12.sp) } },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AppColors.Teal,
                            unfocusedBorderColor = AppColors.Gray200,
                            errorBorderColor = AppColors.Error,
                            focusedContainerColor = AppColors.Background,
                            unfocusedContainerColor = AppColors.Background
                        ),
                        textStyle = androidx.compose.ui.text.TextStyle(
                            fontFamily = Tajawal,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            color = AppColors.DeepTeal
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // New Password Field
                    Text(
                        text = strings.newPasswordLabel2,
                        fontFamily = Tajawal,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AppColors.DeepTeal
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = {
                            newPassword = it
                            if (newError != null) newError = null
                        },
                        placeholder = { Text(strings.passwordHint, fontFamily = Tajawal, color = AppColors.Gray400, fontSize = 14.sp) },
                        visualTransformation = if (newObscure) PasswordVisualTransformation() else VisualTransformation.None,
                        leadingIcon = {
                            Icon(Icons.Rounded.LockReset, contentDescription = null, tint = AppColors.Teal, modifier = Modifier.size(20.dp))
                        },
                        trailingIcon = {
                            IconButton(onClick = { newObscure = !newObscure }) {
                                Icon(
                                    imageVector = if (newObscure) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                                    contentDescription = null,
                                    tint = AppColors.Gray500,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        },
                        isError = newError != null,
                        supportingText = newError?.let { { Text(it, color = AppColors.Error, fontFamily = Tajawal, fontSize = 12.sp) } },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AppColors.Teal,
                            unfocusedBorderColor = AppColors.Gray200,
                            errorBorderColor = AppColors.Error,
                            focusedContainerColor = AppColors.Background,
                            unfocusedContainerColor = AppColors.Background
                        ),
                        textStyle = androidx.compose.ui.text.TextStyle(
                            fontFamily = Tajawal,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            color = AppColors.DeepTeal
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Password Strength Indicator
                    PasswordStrengthMeter(password = newPassword)

                    Spacer(modifier = Modifier.height(18.dp))

                    // Confirm Password Field
                    Text(
                        text = strings.confirmPasswordLabel,
                        fontFamily = Tajawal,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AppColors.DeepTeal
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = {
                            confirmPassword = it
                            if (confirmError != null) confirmError = null
                        },
                        placeholder = { Text(strings.passwordHint, fontFamily = Tajawal, color = AppColors.Gray400, fontSize = 14.sp) },
                        visualTransformation = if (confirmObscure) PasswordVisualTransformation() else VisualTransformation.None,
                        leadingIcon = {
                            Icon(Icons.Rounded.Lock, contentDescription = null, tint = AppColors.Teal, modifier = Modifier.size(20.dp))
                        },
                        trailingIcon = {
                            IconButton(onClick = { confirmObscure = !confirmObscure }) {
                                Icon(
                                    imageVector = if (confirmObscure) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                                    contentDescription = null,
                                    tint = AppColors.Gray500,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        },
                        isError = confirmError != null,
                        supportingText = confirmError?.let { { Text(it, color = AppColors.Error, fontFamily = Tajawal, fontSize = 12.sp) } },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AppColors.Teal,
                            unfocusedBorderColor = AppColors.Gray200,
                            errorBorderColor = AppColors.Error,
                            focusedContainerColor = AppColors.Background,
                            unfocusedContainerColor = AppColors.Background
                        ),
                        textStyle = androidx.compose.ui.text.TextStyle(
                            fontFamily = Tajawal,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            color = AppColors.DeepTeal
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Save CTA Button (56dp height)
            Button(
                onClick = {
                    if (validate() && !isSaving) {
                        isSaving = true
                        coroutineScope.launch {
                            delay(600)
                            isSaving = false
                            snackbarHostState.showSnackbar(strings.changePasswordDone)
                            delay(400)
                            onNavigateBack()
                        }
                    }
                },
                enabled = !isSaving,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AppColors.Teal),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        color = AppColors.Paper,
                        modifier = Modifier.size(22.dp),
                        strokeWidth = 2.5.dp
                    )
                } else {
                    Text(
                        text = strings.savePassword,
                        fontFamily = Tajawal,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.Paper
                    )
                }
            }
        }
    }
}
