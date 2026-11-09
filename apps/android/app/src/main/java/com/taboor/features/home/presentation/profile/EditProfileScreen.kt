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
import androidx.compose.ui.unit.dp
import com.taboor.core.components.TaboorButton
import com.taboor.core.components.TaboorCard
import com.taboor.core.components.TaboorTextField
import com.taboor.core.components.TaboorTopAppBar
import com.taboor.core.localization.LocalAppStrings
import com.taboor.core.localization.LocaleManager
import com.taboor.core.services.SessionManager
import com.taboor.core.theme.AppColors
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Edit Profile Screen.
 * Fully aligned with Taboor's official Warm Cream design system:
 * - Material 3 TaboorTopAppBar with standard back navigation
 * - Unified TaboorTextField inputs with Tajawal typography
 * - Standardized TaboorCard and TaboorButton
 */
@Composable
fun EditProfileScreen(
    onNavigateBack: () -> Unit
) {
    val strings = LocalAppStrings.current
    val isArabic = LocaleManager.isArabic
    val focusManager = LocalFocusManager.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val currentUser by SessionManager.currentUser.collectAsState()

    androidx.activity.compose.BackHandler(onBack = onNavigateBack)

    var fullName by remember { mutableStateOf(currentUser?.fullName ?: if (isArabic) "محمد أحمد" else "Mohamed Ahmed") }
    var email by remember { mutableStateOf(currentUser?.email ?: "customer@taboor.app") }
    var phone by remember { mutableStateOf(currentUser?.phoneNumber ?: "01098765432") }

    var isSaving by remember { mutableStateOf(false) }
    var nameError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }

    fun validate(): Boolean {
        var isValid = true
        if (fullName.trim().isEmpty()) {
            nameError = strings.fullNameRequired
            isValid = false
        } else {
            nameError = null
        }

        if (email.trim().isEmpty()) {
            emailError = strings.emailRequired
            isValid = false
        } else if (!email.contains("@") || !email.contains(".")) {
            emailError = strings.invalidEmail
            isValid = false
        } else {
            emailError = null
        }
        return isValid
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TaboorTopAppBar(
                title = strings.editProfile,
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
            // White Form Card Container
            TaboorCard(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = 20.dp
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Full Name Field
                    TaboorTextField(
                        value = fullName,
                        onValueChange = {
                            fullName = it
                            if (nameError != null) nameError = null
                        },
                        label = strings.fullNameLabel,
                        hint = strings.fullNameHint,
                        leadingIcon = Icons.Rounded.Badge,
                        isError = nameError != null,
                        errorMessage = nameError,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                        containerColor = AppColors.Background
                    )

                    // Email Field
                    TaboorTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            if (emailError != null) emailError = null
                        },
                        label = strings.emailLabel,
                        hint = strings.emailHint,
                        leadingIcon = Icons.Rounded.MailOutline,
                        isError = emailError != null,
                        errorMessage = emailError,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                        containerColor = AppColors.Background
                    )

                    // Phone Field
                    TaboorTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = strings.phoneLabel,
                        hint = strings.phoneHint,
                        leadingIcon = Icons.Rounded.Phone,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                        containerColor = AppColors.Background
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Save CTA Button
            TaboorButton(
                text = strings.editProfileSave,
                onClick = {
                    if (validate() && !isSaving) {
                        isSaving = true
                        coroutineScope.launch {
                            delay(500)
                            isSaving = false
                            snackbarHostState.showSnackbar(strings.editProfileSaved)
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
