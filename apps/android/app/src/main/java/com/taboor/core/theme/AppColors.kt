package com.taboor.core.theme

import androidx.compose.ui.graphics.Color

/**
 * Taboor Brand Palette
 * Directly mirrored from iOS `AppColors.swift` & Flutter `app_colors.dart`
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
    val Paper = Color(0xFFFFFFFF) // Pure White matching iOS cardBackground & elevatedCard
    val Pink = Color(0xFFE91E63)

    // ===== Cohesive Slate & Neutral Palette (mirrored from iOS) =====
    val Gray100 = Color(0xFFF5F5F5)
    val Gray200 = Color(0xFFEAEAEA) // Soft light neutral gray (never transparent to prevent .copy(alpha) black traps)
    val Gray300 = Color(0xFFE0E0E0)
    val Gray400 = Color(0xFF829FA5) // Muted Slate Teal for captions and tertiary
    val Gray500 = Color(0xFF608289)
    val Gray600 = Color(0xFF4B6E74) // Slate Teal for secondary text
    val Gray700 = Color(0xFF2C565E)
    val Gray800 = Color(0xFF15444C)
    val Gray900 = Color(0xFF07333B)

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

    // ===== Semantic aliases matching iOS exactly =====
    val Primary = Ink
    val PrimaryDark = DeepTeal
    val Accent = Amber
    val Background = Cream
    val Surface = Paper
    val PrimaryBlack = Color(0xFF000000)
    val PrimaryWhite = Color(0xFFFFFFFF)

    // Dynamic / Adaptive Semantic Colors (1:1 with iOS AppColors.swift)
    val TextPrimary = Color(0xFF07333B) // Deep Ink: crisp, premium contrast
    val TextSecondary = Color(0xFF4B6E74) // Slate Teal: warm, cohesive secondary text
    val TextTertiary = Color(0xFF829FA5) // Muted Slate Teal
    val CardBackground = Color(0xFFFFFFFF) // Pure White
    val CardBorder = Color.Transparent // Borderless clean design
    val RowDivider = Color(0x0E0B4C56) // Ultra-faint slate separator (~5.5% Ink), perfectly matching iOS
    val SubtleFill = Color(0xFFEDE7DC) // Warm subtle fill for inactive chips and badges
    val ScreenBackground = Cream
    val TealLight = SoftTeal
}
