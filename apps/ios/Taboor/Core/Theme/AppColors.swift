import SwiftUI

// MARK: - AppColors (Exact Flutter Mirror: AppColors.teal, AppColors.amber, etc.)

public enum AppColors {
    // ===== Taboor Brand Palette =====
    public static let ink = Color(hex: 0x0B4C56)
    public static let deepTeal = Color(hex: 0x103F4B)
    public static let teal = Color(hex: 0x159F99)
    public static let softTeal = Color(hex: 0xE3F3EE)
    public static let indigo = Color(hex: 0x6B72CD)
    public static let amber = Color(hex: 0xF6A253)
    public static let cream = Color(hex: 0xF5F0E7)
    public static let paper = Color(hex: 0xFFFDF8)

    // ===== Semantic aliases =====
    public static let primary = ink
    public static let primaryDark = deepTeal
    public static let accent = amber
    public static let background = cream
    public static let surface = paper

    // Grays
    public static let gray100 = Color(hex: 0xF5F5F5)
    public static let gray200 = Color(hex: 0xEEEEEE)
    public static let gray300 = Color(hex: 0xE0E0E0)
    public static let gray400 = Color(hex: 0xBDBDBD)
    public static let gray500 = Color(hex: 0x9E9E9E)
    public static let gray600 = Color(hex: 0x757575)
    public static let gray700 = Color(hex: 0x616161)
    public static let gray800 = Color(hex: 0x424242)
    public static let gray900 = Color(hex: 0x212121)

    // Accent Colors
    public static let accentBlue = indigo
    public static let accentGreen = Color(hex: 0x00C853)
    public static let accentRed = Color(hex: 0xD32F2F)
    public static let accentYellow = Color(hex: 0xFFFFD600)

    // Semantic Colors
    public static let success = Color(hex: 0x4CAF50)
    public static let warning = Color(hex: 0xFF9800)
    public static let error = Color(hex: 0xF44336)
    public static let info = Color(hex: 0x2196F3)

    // ===== Dynamic / Adaptive Semantic Colors (Light & Dark Mode) =====
    public static let splashBackground = Color(hex: 0xF5F0E7)
    public static let cardBackground = Color.white
    public static let elevatedCard = Color.white
    public static let screenBackground = Color(hex: 0xF5F0E7) // Signature Warm Cream (#F5F0E7)

    // Cohesive, warm card border: Ink at 9% opacity (harmonizes with cream background)
    public static let cardBorder = Color(hex: 0x0B4C56, alpha: 0.09)

    // Deep Ink text: crisp, premium contrast
    public static let textPrimary = Color(hex: 0x07333B)

    // Slate Teal: warm, cohesive secondary text
    public static let textSecondary = Color(hex: 0x4B6E74)

    // Muted Slate Teal: tertiary labels, timestamps, bullets
    public static let textTertiary = Color(hex: 0x829FA5)

    // Warm subtle fill for inactive chips and badges
    public static let subtleFill = Color(hex: 0xEDE7DC)
}

extension Color {
    init(hex: UInt, alpha: Double = 1.0) {
        self.init(
            .sRGB,
            red: Double((hex >> 16) & 0xff) / 255.0,
            green: Double((hex >> 08) & 0xff) / 255.0,
            blue: Double((hex >> 00) & 0xff) / 255.0,
            opacity: alpha
        )
    }
}
