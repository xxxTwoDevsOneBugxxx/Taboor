package com.taboor.core.theme

import androidx.compose.ui.graphics.Color

/**
 * Taboor Brand Palette
 * Directly mirrored from Flutter: `lib/core/themes/app_colors.dart`
 */
object AppColors {
    // ===== Taboor Brand Palette =====
    val Ink = Color(0xFF0B4C56)
    val DeepTeal = Color(0xFF103F4B)
    val Teal = Color(0xFF159F99)
    val SoftTeal = Color(0xFFE3F3EE)
    val Indigo = Color(0xFF6B72CD)
    val Amber = Color(0xFFF6A253)
    val Cream = Color(0xFFF5F0E7)
    val Paper = Color(0xFFFFFDF8)

    // ===== Semantic aliases =====
    val Primary = Ink
    val PrimaryDark = DeepTeal
    val Accent = Amber
    val Background = Cream
    val Surface = Paper
    val PrimaryBlack = Color(0xFF000000)
    val PrimaryWhite = Color(0xFFFFFFFF)

    // ===== Secondary / Grayscale =====
    val Gray100 = Color(0xFFF5F5F5)
    val Gray200 = Color(0xFFEEEEEE)
    val Gray300 = Color(0xFFE0E0E0)
    val Gray400 = Color(0xFFBDBDBD)
    val Gray500 = Color(0xFF9E9E9E)
    val Gray600 = Color(0xFF757575)
    val Gray700 = Color(0xFF616161)
    val Gray800 = Color(0xFF424242)
    val Gray900 = Color(0xFF212121)

    // ===== Accent Colors =====
    val AccentBlue = Indigo
    val AccentGreen = Color(0xFF00C853)
    val AccentRed = Color(0xFFD32F2F)
    val AccentYellow = Color(0xFFFFD600)

    // ===== Semantic Colors =====
    val Success = Color(0xFF4CAF50)
    val Warning = Color(0xFFFF9800)
    val Error = Color(0xFFF44336)
    val Info = Color(0xFF2196F3)
}
