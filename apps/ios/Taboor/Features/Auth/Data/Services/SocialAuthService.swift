import Foundation
import AuthenticationServices
import CryptoKit

// MARK: - Social Auth Errors
public enum SocialAuthError: LocalizedError {
    case userCancelled
    case missingToken(String)
    case invalidRedirectURL
    case configurationMissing(String)
    case custom(String)

    public var errorDescription: String? {
        let isArabic = LocalizationManager.shared.isArabic
        switch self {
        case .userCancelled:
            return isArabic ? "تم إلغاء تسجيل الدخول" : "Sign in was cancelled"
        case .missingToken(let provider):
            return isArabic ? "تعذر الحصول على رمز الدخول من \(provider)" : "Failed to retrieve access token from \(provider)"
        case .invalidRedirectURL:
            return isArabic ? "رابط الاستجابة غير صالح" : "Invalid redirect URL returned"
        case .configurationMissing(let item):
            return isArabic ? "إعدادات \(item) غير مكتملة" : "Configuration missing for \(item)"
        case .custom(let message):
            return message
        }
    }
}

// MARK: - Native iOS Social Authentication Service
/// Implements standard Web OAuth 2.0 (ASWebAuthenticationSession) for Google & Facebook
/// matching Mahfazti's architecture exactly, without requiring heavy external SDKs.
@MainActor
public final class SocialAuthService: NSObject, ObservableObject, ASWebAuthenticationPresentationContextProviding {
    public static let shared = SocialAuthService()

    // MARK: - Dynamic Configuration from Info.plist with Sensible Defaults
    public var facebookAppId: String {
        (Bundle.main.object(forInfoDictionaryKey: "FacebookAppID") as? String)?
            .trimmingCharacters(in: .whitespacesAndNewlines)
            .isEmpty == false
            ? Bundle.main.object(forInfoDictionaryKey: "FacebookAppID") as! String
            : "2241479906769476"
    }

    public var facebookRedirectUri: String {
        "https://taboorapi.runasp.net/api/auth/facebook-callback"
    }

    public var googleClientId: String {
        if let gid = Bundle.main.object(forInfoDictionaryKey: "GIDClientID") as? String, !gid.isEmpty {
            return gid
        }
        if let gClientId = Bundle.main.object(forInfoDictionaryKey: "GoogleClientID") as? String, !gClientId.isEmpty {
            return gClientId
        }
        return "619082841226-c65esvlhc7h2p9arqiq2jl00i6d1qsor.apps.googleusercontent.com"
    }

    public var googleCallbackScheme: String {
        "com.googleusercontent.apps.619082841226-c65esvlhc7h2p9arqiq2jl00i6d1qsor"
    }

    public var callbackScheme: String {
        "taboor"
    }

    private var activeSession: ASWebAuthenticationSession?

    private override init() {
        super.init()
    }

    // MARK: - Facebook Web OAuth 2.0 (Matches Mahfazti)
    /// Signs in with Facebook using ASWebAuthenticationSession.
    /// Extracts access_token and returns it for backend validation.
    public func signInWithFacebook() async throws -> String {
        var components = URLComponents(string: "https://www.facebook.com/v19.0/dialog/oauth")!
        components.queryItems = [
            URLQueryItem(name: "client_id", value: facebookAppId),
            URLQueryItem(name: "redirect_uri", value: facebookRedirectUri),
            URLQueryItem(name: "scope", value: "email,public_profile"),
            URLQueryItem(name: "response_type", value: "token"),
            URLQueryItem(name: "display", value: "touch")
        ]

        guard let authUrl = components.url else {
            throw SocialAuthError.invalidRedirectURL
        }

        let callbackUrl = try await startAuthentication(url: authUrl, callbackScheme: callbackScheme)
        return try extractToken(from: callbackUrl, tokenKey: "access_token", providerName: "Facebook")
    }

    // MARK: - Google Web OAuth 2.0 / PKCE Flow (RFC 7636 & RFC 8252)
    /// Signs in with Google using native PKCE authorization code grant via ASWebAuthenticationSession.
    /// Exchanges authorization code for id_token directly with Google token endpoint.
    public func signInWithGoogle() async throws -> String {
        let codeVerifier = generateCodeVerifier()
        let codeChallenge = generateCodeChallenge(from: codeVerifier)
        let state = UUID().uuidString
        let redirectUri = "\(googleCallbackScheme):/oauth2redirect"

        var components = URLComponents(string: "https://accounts.google.com/o/oauth2/v2/auth")!
        components.queryItems = [
            URLQueryItem(name: "client_id", value: googleClientId),
            URLQueryItem(name: "response_type", value: "code"),
            URLQueryItem(name: "redirect_uri", value: redirectUri),
            URLQueryItem(name: "scope", value: "openid email profile"),
            URLQueryItem(name: "code_challenge", value: codeChallenge),
            URLQueryItem(name: "code_challenge_method", value: "S256"),
            URLQueryItem(name: "state", value: state),
            URLQueryItem(name: "prompt", value: "select_account")
        ]

        guard let authUrl = components.url else {
            throw SocialAuthError.invalidRedirectURL
        }

        let callbackUrl = try await startAuthentication(url: authUrl, callbackScheme: googleCallbackScheme)
        let code = try extractToken(from: callbackUrl, tokenKey: "code", providerName: "Google")

        return try await exchangeGoogleCodeForToken(code: code, codeVerifier: codeVerifier, redirectUri: redirectUri)
    }

    // MARK: - Google PKCE Helpers
    private func generateCodeVerifier() -> String {
        var bytes = [UInt8](repeating: 0, count: 32)
        _ = SecRandomCopyBytes(kSecRandomDefault, bytes.count, &bytes)
        return Data(bytes).base64URLEncoded()
    }

    private func generateCodeChallenge(from verifier: String) -> String {
        let data = Data(verifier.utf8)
        let hashed = SHA256.hash(data: data)
        return Data(hashed).base64URLEncoded()
    }

    private struct GoogleTokenResponse: Decodable {
        let id_token: String
        let access_token: String?
        let expires_in: Int?
        let token_type: String?
    }

    private func exchangeGoogleCodeForToken(code: String, codeVerifier: String, redirectUri: String) async throws -> String {
        guard let tokenUrl = URL(string: "https://oauth2.googleapis.com/token") else {
            throw SocialAuthError.custom("Invalid token endpoint URL")
        }

        var request = URLRequest(url: tokenUrl)
        request.httpMethod = "POST"
        request.setValue("application/x-www-form-urlencoded", forHTTPHeaderField: "Content-Type")

        let parameters: [String: String] = [
            "client_id": googleClientId,
            "code": code,
            "code_verifier": codeVerifier,
            "grant_type": "authorization_code",
            "redirect_uri": redirectUri
        ]

        let bodyString = parameters
            .map { key, value in
                let escapedKey = key.addingPercentEncoding(withAllowedCharacters: .urlQueryAllowed) ?? key
                let escapedValue = value.addingPercentEncoding(withAllowedCharacters: .urlQueryAllowed) ?? value
                return "\(escapedKey)=\(escapedValue)"
            }
            .joined(separator: "&")

        request.httpBody = bodyString.data(using: .utf8)

        let (data, response) = try await URLSession.shared.data(for: request)

        guard let httpResponse = response as? HTTPURLResponse else {
            throw SocialAuthError.custom("Invalid response from Google token service")
        }

        guard (200...299).contains(httpResponse.statusCode) else {
            if let errorJson = try? JSONSerialization.jsonObject(with: data) as? [String: Any],
               let errorDesc = errorJson["error_description"] as? String ?? errorJson["error"] as? String {
                throw SocialAuthError.custom("Google auth failed: \(errorDesc)")
            }
            throw SocialAuthError.custom("Google token exchange failed with status \(httpResponse.statusCode)")
        }

        let tokenResponse = try JSONDecoder().decode(GoogleTokenResponse.self, from: data)
        return tokenResponse.id_token
    }

    // MARK: - ASWebAuthenticationSession Presentation
    private func startAuthentication(url: URL, callbackScheme: String) async throws -> URL {
        try await withCheckedThrowingContinuation { continuation in
            let session = ASWebAuthenticationSession(url: url, callbackURLScheme: callbackScheme) { callbackURL, error in
                self.activeSession = nil

                if let error = error {
                    if let authError = error as? ASWebAuthenticationSessionError, authError.code == .canceledLogin {
                        continuation.resume(throwing: SocialAuthError.userCancelled)
                    } else {
                        continuation.resume(throwing: SocialAuthError.custom(error.localizedDescription))
                    }
                    return
                }

                guard let callbackURL = callbackURL else {
                    continuation.resume(throwing: SocialAuthError.invalidRedirectURL)
                    return
                }

                continuation.resume(returning: callbackURL)
            }

            session.presentationContextProvider = self
            session.prefersEphemeralWebBrowserSession = false
            self.activeSession = session

            guard session.start() else {
                continuation.resume(throwing: SocialAuthError.custom("Failed to start authentication session"))
                return
            }
        }
    }

    // MARK: - Token Parsing (Query & Fragment)
    private func extractToken(from url: URL, tokenKey: String, providerName: String) throws -> String {
        // 1. Try fragment: scheme://callback#access_token=...&...
        if let fragment = url.fragment, !fragment.isEmpty {
            let params = parseQueryString(fragment)
            if let token = params[tokenKey], !token.isEmpty {
                return token
            }
        }

        // 2. Try standard query: scheme://callback?id_token=...
        if let components = URLComponents(url: url, resolvingAgainstBaseURL: false),
           let items = components.queryItems {
            if let token = items.first(where: { $0.name == tokenKey })?.value, !token.isEmpty {
                return token
            }
        }

        throw SocialAuthError.missingToken(providerName)
    }

    private func parseQueryString(_ query: String) -> [String: String] {
        var params: [String: String] = [:]
        for pair in query.components(separatedBy: "&") {
            let parts = pair.components(separatedBy: "=")
            if parts.count == 2,
               let key = parts[0].removingPercentEncoding,
               let value = parts[1].removingPercentEncoding {
                params[key] = value
            }
        }
        return params
    }

    // MARK: - ASWebAuthenticationPresentationContextProviding
    public func presentationAnchor(for session: ASWebAuthenticationSession) -> ASPresentationAnchor {
        guard let windowScene = UIApplication.shared.connectedScenes.first as? UIWindowScene,
              let window = windowScene.windows.first(where: { $0.isKeyWindow }) ?? windowScene.windows.first else {
            return ASPresentationAnchor()
        }
        return window
    }
}

// MARK: - Data Base64URL Encoding Extension
extension Data {
    fileprivate func base64URLEncoded() -> String {
        return self.base64EncodedString()
            .replacingOccurrences(of: "+", with: "-")
            .replacingOccurrences(of: "/", with: "_")
            .trimmingCharacters(in: CharacterSet(charactersIn: "="))
    }
}
