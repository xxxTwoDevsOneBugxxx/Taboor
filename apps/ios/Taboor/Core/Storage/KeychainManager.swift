import Foundation
import Security

// MARK: - Secure Keychain Manager for Sensitive Credentials (JWT Tokens)

public final class KeychainManager {
    public static let shared = KeychainManager()
    private init() {}

    private let service = AppConstants.AppInfo.bundleId

    @discardableResult
    public func save(key: String, value: String) -> Bool {
        guard let data = value.data(using: .utf8) else { return false }

        // Remove old item before saving
        delete(key: key)

        let query: [String: Any] = [
            kSecClass as String: kSecClassGenericPassword,
            kSecAttrService as String: service,
            kSecAttrAccount as String: key,
            kSecValueData as String: data,
            kSecAttrAccessible as String: kSecAttrAccessibleAfterFirstUnlock
        ]

        let status = SecItemAdd(query as CFDictionary, nil)
        return status == errSecSuccess
    }

    public func get(key: String) -> String? {
        let query: [String: Any] = [
            kSecClass as String: kSecClassGenericPassword,
            kSecAttrService as String: service,
            kSecAttrAccount as String: key,
            kSecReturnData as String: true,
            kSecMatchLimit as String: kSecMatchLimitOne
        ]

        var dataTypeRef: AnyObject?
        let status = SecItemCopyMatching(query as CFDictionary, &dataTypeRef)

        guard status == errSecSuccess, let data = dataTypeRef as? Data else {
            return nil
        }

        return String(data: data, encoding: .utf8)
    }

    @discardableResult
    public func delete(key: String) -> Bool {
        let query: [String: Any] = [
            kSecClass as String: kSecClassGenericPassword,
            kSecAttrService as String: service,
            kSecAttrAccount as String: key
        ]

        let status = SecItemDelete(query as CFDictionary)
        return status == errSecSuccess || status == errSecItemNotFound
    }

    public func clearAllAuthTokens() {
        delete(key: AppConstants.Storage.accessToken)
        delete(key: AppConstants.Storage.refreshToken)
    }

    public var hasAccessToken: Bool {
        return get(key: AppConstants.Storage.accessToken) != nil
    }
}
