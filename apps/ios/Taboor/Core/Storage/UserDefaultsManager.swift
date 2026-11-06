import Foundation

// MARK: - User Defaults Manager for Preferences and Cached Non-Sensitive Data

public final class UserDefaultsManager {
    public static let shared = UserDefaultsManager()
    private let defaults = UserDefaults.standard

    private init() {}

    public var hasCompletedOnboarding: Bool {
        get { defaults.bool(forKey: AppConstants.Storage.hasCompletedOnboarding) }
        set { defaults.set(newValue, forKey: AppConstants.Storage.hasCompletedOnboarding) }
    }

    public var appLanguage: String? {
        get { defaults.string(forKey: AppConstants.Storage.appLanguage) }
        set { defaults.set(newValue, forKey: AppConstants.Storage.appLanguage) }
    }

    public func save<T: Encodable>(_ object: T, forKey key: String) {
        if let encoded = try? JSONEncoder().encode(object) {
            defaults.set(encoded, forKey: key)
        }
    }

    public func get<T: Decodable>(_ type: T.Type, forKey key: String) -> T? {
        guard let data = defaults.data(forKey: key) else { return nil }
        return try? JSONDecoder().decode(type, from: data)
    }

    public func remove(forKey key: String) {
        defaults.removeObject(forKey: key)
    }

    public func clearSession() {
        remove(forKey: AppConstants.Storage.savedUser)
    }
}
