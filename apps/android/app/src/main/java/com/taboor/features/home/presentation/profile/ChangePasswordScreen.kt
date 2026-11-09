package com.taboor.features.home.presentation.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.taboor.core.components.TaboorButton
import com.taboor.core.components.TaboorCard
import com.taboor.core.components.TaboorTextField
import com.taboor.core.components.TaboorTopAppBar
import com.taboor.core.localization.LocalAppStrings
import com.taboor.core.theme.AppColors
import com.taboor.features.auth.presentation.register.PasswordStrengthMeter
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Change Password Screen.
 * Fully aligned with Taboor's official Warm Cream design system:
 * - Material 3 TaboorTopAppBar with standard back navigation
 * - Unified TaboorTextField inputs with Tajawal typography
 * - Standardized TaboorCard and TaboorButton
 */
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
            TaboorTopAppBar(
                title = strings.profileChangePassword,
                onNavigateBack = onNavigateBack
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
            TaboorCard(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = 20.dp
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Old Password Field
                    TaboorTextField(
                        value = oldPassword,
                        onValueChange = {
                            oldPassword = it
                            if (oldError != null) oldError = null
                        },
                        label = strings.oldPasswordLabel,
                        hint = strings.oldPasswordHint,
                        visualTransformation = if (oldObscure) PasswordVisualTransformation() else VisualTransformation.None,
                        leadingIcon = Icons.Rounded.Lock,
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
                        errorMessage = oldError,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                        containerColor = AppColors.Background
                    )

                    // New Password Field
                    TaboorTextField(
                        value = newPassword,
                        onValueChange = {
                            newPassword = it
                            if (newError != null) newError = null
                        },
                        label = strings.newPasswordLabel2,
                        hint = strings.passwordHint,
                        visualTransformation = if (newObscure) PasswordVisualTransformation() else VisualTransformation.None,
                        leadingIcon = Icons.Rounded.LockReset,
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
                        errorMessage = newError,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                        containerColor = AppColors.Background
                    )

                    // Password Strength Meter
                    if (newPassword.isNotEmpty()) {
                        PasswordStrengthMeter(password = newPassword)
                    }

                    // Confirm Password Field
                    TaboorTextField(
                        value = confirmPassword,
                        onValueChange = {
                            confirmPassword = it
                            if (confirmError != null) confirmError = null
                        },
                        label = strings.confirmPasswordLabel,
                        hint = strings.passwordHint,
                        visualTransformation = if (confirmObscure) PasswordVisualTransformation() else VisualTransformation.None,
                        leadingIcon = Icons.Rounded.Lock,
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
                        errorMessage = confirmError,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                        containerColor = AppColors.Background
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Save CTA Button
            TaboorButton(
                text = strings.savePassword,
                onClick = {
                    if (validate() && !isSaving) {
                        isSaving = true
                        coroutineScope.launch {
                            delay(500)
                            isSaving = false
                            snackbarHostState.showSnackbar(strings.changePasswordDone)
                            delay(350)
                            onNavigateBack()
                        }
                    }
                },
                isLoading = isSaving,
                enabled = !isSaving,
                height = 54.dp
            )
        }
    }
}
