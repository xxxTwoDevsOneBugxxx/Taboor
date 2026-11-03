import SwiftUI

extension Font {
    enum TajawalWeight {
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

    enum InterWeight {
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

    static func tajawal(_ size: CGFloat, _ weight: TajawalWeight = .regular) -> Font {
        return Font.custom(weight.fontName, size: size)
    }

    static func inter(_ size: CGFloat, _ weight: InterWeight = .regular) -> Font {
        return Font.custom(weight.fontName, size: size)
    }
}
