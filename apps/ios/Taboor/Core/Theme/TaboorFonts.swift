import SwiftUI
import CoreText

// MARK: - Programmatic Font Registration (Guarantees Custom TTFs Load in All Environments)

public enum FontRegistration {
    private static var hasRegistered = false

    public static func registerCustomFonts() {
        guard !hasRegistered else { return }
        hasRegistered = true

        let fontFiles = [
            "Tajawal-Regular.ttf",
            "Tajawal-Medium.ttf",
            "Tajawal-Bold.ttf",
            "Tajawal-ExtraBold.ttf",
            "Inter-Regular.ttf",
            "Inter-Medium.ttf",
            "Inter-SemiBold.ttf",
            "Inter-Bold.ttf"
        ]

        for fontName in fontFiles {
            let filename = (fontName as NSString).deletingPathExtension
            let ext = (fontName as NSString).pathExtension

            // Try bundle direct or in Fonts folder
            if let url = Bundle.main.url(forResource: filename, withExtension: ext) ??
                         Bundle.main.url(forResource: fontName, withExtension: nil) {
                var error: Unmanaged<CFError>?
                CTFontManagerRegisterFontsForURL(url as CFURL, .process, &error)
            }
        }
    }
}

// MARK: - Font Extensions

extension Font {
    public enum TajawalWeight {
        case regular
        case medium
        case bold
        case extraBold

        var fontName: String {
            switch self {
            case .regular: return "Tajawal-Regular"
            case .medium: return "Tajawal-Medium"
            case .bold: return "Tajawal-Bold"
            case .extraBold: return "Tajawal-ExtraBold"
            }
        }
    }

    public enum InterWeight {
        case regular
        case medium
        case semiBold
        case bold

        var fontName: String {
            switch self {
            case .regular: return "Inter-Regular"
            case .medium: return "Inter-Medium"
            case .semiBold: return "Inter-SemiBold"
            case .bold: return "Inter-Bold"
            }
        }
    }

    public static func tajawal(_ size: CGFloat, _ weight: TajawalWeight = .regular) -> Font {
        FontRegistration.registerCustomFonts()
        return Font.custom(weight.fontName, size: size)
    }

    public static func inter(_ size: CGFloat, _ weight: InterWeight = .regular) -> Font {
        FontRegistration.registerCustomFonts()
        return Font.custom(weight.fontName, size: size)
    }

    /// Adaptive font: Tajawal for Arabic, Inter for English
    public static func app(_ size: CGFloat, _ weight: TajawalWeight = .regular, isArabic: Bool = true) -> Font {
        if isArabic {
            return tajawal(size, weight)
        } else {
            let interWeight: InterWeight = {
                switch weight {
                case .regular: return .regular
                case .medium: return .medium
                case .bold, .extraBold: return .bold
                }
            }()
            return inter(size, interWeight)
        }
    }
}
