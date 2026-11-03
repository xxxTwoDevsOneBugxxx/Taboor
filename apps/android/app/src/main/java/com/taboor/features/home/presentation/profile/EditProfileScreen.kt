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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.taboor.core.localization.LocalAppStrings
import com.taboor.core.localization.LocaleManager
import com.taboor.core.services.SessionManager
import com.taboor.core.theme.AppColors
import com.taboor.core.theme.Tajawal
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Edit Profile Screen.
 * Pixel-perfect reproduction of Flutter's `edit_profile_screen.dart`.
 */
@OptIn(ExperimentalMaterial3Api::class)
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
            TopAppBar(
                title = {
                    Text(
                        text = strings.editProfile,
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
            // White Form Card Container
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = AppColors.Paper,
                border = androidx.compose.foundation.BorderStroke(1.dp, AppColors.Gray200)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    // Full Name Field
                    Text(
                        text = strings.fullNameLabel,
                        fontFamily = Tajawal,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AppColors.DeepTeal
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = fullName,
                        onValueChange = {
                            fullName = it
                            if (nameError != null) nameError = null
                        },
                        placeholder = { Text(strings.fullNameHint, fontFamily = Tajawal, color = AppColors.Gray400, fontSize = 14.sp) },
                        leadingIcon = {
                            Icon(Icons.Rounded.Badge, contentDescription = null, tint = AppColors.Teal, modifier = Modifier.size(20.dp))
                        },
                        isError = nameError != null,
                        supportingText = nameError?.let { { Text(it, color = AppColors.Error, fontFamily = Tajawal, fontSize = 12.sp) } },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Next),
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

                    Spacer(modifier = Modifier.height(16.dp))

                    // Email Field
                    Text(
                        text = strings.emailLabel,
                        fontFamily = Tajawal,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AppColors.DeepTeal
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            if (emailError != null) emailError = null
                        },
                        placeholder = { Text(strings.emailHint, fontFamily = Tajawal, color = AppColors.Gray400, fontSize = 14.sp) },
                        leadingIcon = {
                            Icon(Icons.Rounded.MailOutline, contentDescription = null, tint = AppColors.Teal, modifier = Modifier.size(20.dp))
                        },
                        isError = emailError != null,
                        supportingText = emailError?.let { { Text(it, color = AppColors.Error, fontFamily = Tajawal, fontSize = 12.sp) } },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
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

                    Spacer(modifier = Modifier.height(16.dp))

                    // Phone Field
                    Text(
                        text = strings.phoneLabel,
                        fontFamily = Tajawal,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AppColors.DeepTeal
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        placeholder = { Text(strings.phoneHint, fontFamily = Tajawal, color = AppColors.Gray400, fontSize = 14.sp) },
                        leadingIcon = {
                            Icon(Icons.Rounded.Phone, contentDescription = null, tint = AppColors.Teal, modifier = Modifier.size(20.dp))
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AppColors.Teal,
                            unfocusedBorderColor = AppColors.Gray200,
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
                            snackbarHostState.showSnackbar(strings.editProfileSaved)
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
                        text = strings.editProfileSave,
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
