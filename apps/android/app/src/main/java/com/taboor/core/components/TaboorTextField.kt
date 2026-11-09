package com.taboor.core.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.taboor.core.theme.AppColors
import com.taboor.core.theme.Tajawal

/**
 * Standardized Text Field for Taboor Android.
 * Follows the 12-14dp corner radius standard, Tajawal typography, and Warm Cream palette.
 */
@Composable
fun TaboorTextField(
    value: String,
    onValueChange: (String) -> Unit,
    hint: String,
    modifier: Modifier = Modifier,
    label: String? = null,
    leadingIcon: ImageVector? = null,
    leadingContent: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
    isError: Boolean = false,
    errorMessage: String? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    singleLine: Boolean = true,
    cornerRadius: Dp = 12.dp,
    containerColor: Color = AppColors.Paper
) {
    Column(modifier = modifier.fillMaxWidth()) {
        if (!label.isNullOrBlank()) {
            Text(
                text = label,
                fontFamily = Tajawal,
                fontSize = 13.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = AppColors.DeepTeal
            )
            Spacer(modifier = Modifier.height(6.dp))
        }
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = {
                Text(
                    text = hint,
                    fontFamily = Tajawal,
                    fontSize = 13.5.sp,
                    color = AppColors.Gray400
                )
            },
            leadingIcon = when {
                leadingContent != null -> leadingContent
                leadingIcon != null -> {
                    {
                        Icon(
                            imageVector = leadingIcon,
                            contentDescription = null,
                            tint = AppColors.Teal,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                else -> null
            },
            trailingIcon = trailingIcon,
            visualTransformation = visualTransformation,
            singleLine = singleLine,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            isError = isError,
            shape = RoundedCornerShape(cornerRadius),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = containerColor,
                unfocusedContainerColor = containerColor,
                focusedBorderColor = AppColors.Teal,
                unfocusedBorderColor = if (isError) AppColors.Error else AppColors.Gray200,
                focusedTextColor = AppColors.DeepTeal,
                unfocusedTextColor = AppColors.DeepTeal,
                errorBorderColor = AppColors.Error,
                cursorColor = AppColors.Teal
            ),
            textStyle = TextStyle(
                fontFamily = Tajawal,
                fontSize = 14.5.sp,
                fontWeight = FontWeight.Medium,
                color = AppColors.DeepTeal
            ),
            modifier = Modifier.fillMaxWidth()
        )
        if (!errorMessage.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = errorMessage,
                fontFamily = Tajawal,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = AppColors.Error,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }
    }
}
