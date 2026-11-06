import Foundation

// MARK: - Network Errors (Localized for Arabic and English)

public enum NetworkError: LocalizedError {
    case invalidURL
    case noInternet
    case unauthorized
    case forbidden
    case notFound
    case conflict(String?)
    case serverError(statusCode: Int, message: String?)
    case decodingError(String)
    case custom(String)
    case unknown

    public var errorDescription: String? {
        let isArabic = LocalizationManager.shared.isArabic
        switch self {
        case .invalidURL:
            return isArabic ? "عنوان الرابط غير صالح" : "Invalid URL"
        case .noInternet:
            return isArabic ? "لا يوجد اتصال بالإنترنت" : "No internet connection"
        case .unauthorized:
            return isArabic ? "انتهت الجلسة أو بيانات الدخول غير صحيحة" : "Unauthorized or session expired"
        case .forbidden:
            return isArabic ? "ليس لديك صلاحية لتنفيذ هذا الإجراء" : "Access forbidden"
        case .notFound:
            return isArabic ? "البيانات المطلوبة غير موجودة" : "Resource not found"
        case .conflict(let msg):
            return msg ?? (isArabic ? "يوجد تعارض في البيانات المدخلة" : "Data conflict")
        case .serverError(let code, let msg):
            return msg ?? (isArabic ? "خطأ في السيرفر (\(code))" : "Server error (\(code))")
        case .decodingError(let details):
            return isArabic ? "فشل معالجة بيانات السيرفر: \(details)" : "Data parsing error: \(details)"
        case .custom(let msg):
            return msg
        case .unknown:
            return isArabic ? "حدث خطأ غير متوقع، يرجى المحاولة لاحقاً" : "An unexpected error occurred"
        }
    }
}
