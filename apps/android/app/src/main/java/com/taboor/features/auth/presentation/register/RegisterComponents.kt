package com.taboor.features.auth.presentation.register

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.taboor.core.localization.LocalAppStrings
import com.taboor.core.theme.AppColors
import com.taboor.core.theme.Tajawal

@Composable
fun StepIndicator(current: Int, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 0 until 3) {
            val isActive = i <= current
            val width by animateDpAsState(
                targetValue = if (i == current) 24.dp else 8.dp,
                animationSpec = tween(300),
                label = "stepWidth"
            )
            val color by animateColorAsState(
                targetValue = if (isActive) AppColors.Teal else AppColors.Gray300,
                animationSpec = tween(300),
                label = "stepColor"
            )

            Box(
                modifier = Modifier
                    .size(width = width, height = 7.dp)
                    .clip(CircleShape)
                    .background(color)
            )
        }
    }
}

enum class PasswordStrength(val level: Int, val color: Color) {
    WEAK(1, AppColors.AccentRed),
    MEDIUM(2, AppColors.Amber),
    STRONG(3, AppColors.Teal),
    VERY_STRONG(4, AppColors.Indigo)
}

fun calculatePasswordStrength(password: String): PasswordStrength {
    if (password.isEmpty()) return PasswordStrength.WEAK
    var score = 0
    if (password.length >= 8) score++
    if (password.any { it.isUpperCase() }) score++
    if (password.any { it.isDigit() }) score++
    if (password.any { !it.isLetterOrDigit() }) score++

    return when {
        score <= 1 -> PasswordStrength.WEAK
        score == 2 -> PasswordStrength.MEDIUM
        score == 3 -> PasswordStrength.STRONG
        else -> PasswordStrength.VERY_STRONG
    }
}

@Composable
fun PasswordStrengthMeter(password: String, modifier: Modifier = Modifier) {
    val strings = LocalAppStrings.current
    val strength = calculatePasswordStrength(password)

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                for (i in 0 until 4) {
                    val isFilled = password.isNotEmpty() && i < strength.level
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(6.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (isFilled) strength.color else AppColors.Gray200)
                    )
                }
            }

            if (password.isNotEmpty()) {
                Spacer(modifier = Modifier.width(10.dp))
                val label = when (strength) {
                    PasswordStrength.WEAK -> strings.passwordWeakLabel
                    PasswordStrength.MEDIUM -> strings.passwordMediumLabel
                    PasswordStrength.STRONG -> strings.passwordStrongLabel
                    PasswordStrength.VERY_STRONG -> strings.passwordVeryStrongLabel
                }
                Text(
                    text = label,
                    fontFamily = Tajawal,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = strength.color
                )
            }
        }
    }
}

@Composable
fun PasswordMatchIndicator(password: String, confirm: String, modifier: Modifier = Modifier) {
    val strings = LocalAppStrings.current
    if (confirm.isEmpty()) return

    val isMatch = password == confirm
    val tint = if (isMatch) AppColors.Success else AppColors.AccentRed

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (isMatch) Icons.Default.CheckCircle else Icons.Default.ErrorOutline,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(15.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = if (isMatch) "كلمتا المرور متطابقتين" else strings.passwordMismatch,
            fontFamily = Tajawal,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = tint
        )
    }
}

@Composable
fun OtpBoxes(
    otp: String,
    onOtpChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    BasicTextField(
        value = otp,
        onValueChange = {
            if (it.length <= 6 && it.all { char -> char.isDigit() }) {
                onOtpChange(it)
            }
        },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = modifier,
        decorationBox = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                for (i in 0 until 6) {
                    val char = otp.getOrNull(i)?.toString() ?: ""
                    val isFocused = otp.length == i

                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(AppColors.Paper)
                            .border(
                                width = if (isFocused) 2.dp else 1.2.dp,
                                color = if (isFocused) AppColors.Teal else if (char.isNotEmpty()) AppColors.DeepTeal else AppColors.Gray200,
                                shape = RoundedCornerShape(12.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = char,
                            fontFamily = Tajawal,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppColors.DeepTeal,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    )
}
