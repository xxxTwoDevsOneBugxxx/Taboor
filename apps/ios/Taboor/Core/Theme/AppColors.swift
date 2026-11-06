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
    public static let splashBackground = Color(uiColor: UIColor { trait in
        trait.userInterfaceStyle == .dark
            ? UIColor(red: 0.059, green: 0.071, blue: 0.090, alpha: 1.0) // Matches dark background (#0F1217)
            : UIColor(red: 0.961, green: 0.941, blue: 0.906, alpha: 1.0) // Taboor Cream (#F5F0E7)
    })

    public static let cardBackground = Color(uiColor: UIColor { trait in
        trait.userInterfaceStyle == .dark
            ? UIColor(red: 0.11, green: 0.13, blue: 0.16, alpha: 1.0)
            : UIColor.white
    })

    public static let elevatedCard = Color(uiColor: UIColor { trait in
        trait.userInterfaceStyle == .dark
            ? UIColor(red: 0.15, green: 0.17, blue: 0.20, alpha: 1.0)
            : UIColor.white
    })

    public static let screenBackground = Color(uiColor: UIColor { trait in
        trait.userInterfaceStyle == .dark
            ? UIColor(red: 0.059, green: 0.071, blue: 0.090, alpha: 1.0) // Matches Splash & Dark Identity (#0F1217)
            : UIColor(red: 0.965, green: 0.969, blue: 0.975, alpha: 1.0) // Crisp Clean Modern iOS Tone (#F6F7F9)
    })

    public static let cardBorder = Color(uiColor: UIColor { trait in
        trait.userInterfaceStyle == .dark
            ? UIColor(white: 1.0, alpha: 0.10)
            : UIColor(red: 0.92, green: 0.92, blue: 0.93, alpha: 1.0)
    })

    public static let textPrimary = Color(uiColor: UIColor { trait in
        trait.userInterfaceStyle == .dark
            ? UIColor.white
            : UIColor(red: 0.06, green: 0.25, blue: 0.29, alpha: 1.0) // deepTeal
    })

    public static let textSecondary = Color(uiColor: UIColor { trait in
        trait.userInterfaceStyle == .dark
            ? UIColor(white: 0.70, alpha: 1.0)
            : UIColor(red: 0.46, green: 0.46, blue: 0.46, alpha: 1.0) // gray600
    })

    public static let textTertiary = Color(uiColor: UIColor { trait in
        trait.userInterfaceStyle == .dark
            ? UIColor(white: 0.50, alpha: 1.0)
            : UIColor(red: 0.68, green: 0.68, blue: 0.68, alpha: 1.0)
    })

    public static let subtleFill = Color(uiColor: UIColor { trait in
        trait.userInterfaceStyle == .dark
            ? UIColor(white: 1.0, alpha: 0.07)
            : UIColor(red: 0.96, green: 0.96, blue: 0.97, alpha: 1.0)
    })
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
