import Foundation

// MARK: - Alerts Repository Protocol
public protocol AlertsRepositoryProtocol {
    func getAlerts() async throws -> [AppAlertItem]
    func markAsRead(alertId: String) async throws
    func markAllAsRead() async throws
}

// MARK: - Alerts Repository Implementation (Mock-Ready & Real API Connected)
public final class AlertsRepository: AlertsRepositoryProtocol {
    public static let shared = AlertsRepository()

    private let apiClient: ApiClient
    private let mockLoader: MockDataLoader

    public init(apiClient: ApiClient = .shared, mockLoader: MockDataLoader = .shared) {
        self.apiClient = apiClient
        self.mockLoader = mockLoader
    }

    public func getAlerts() async throws -> [AppAlertItem] {
        if AppConstants.API.useMockData {
            let dtos: [AlertItemDTO] = try await mockLoader.load(filename: "alerts_seed.json", simulateDelay: 0.15)
            return dtos.map { $0.toDomain() }
        } else {
            let dtos: [AlertItemDTO] = try await apiClient.request(
                endpoint: AppConstants.API.Endpoints.alerts,
                method: .get,
                requiresAuth: true
            )
            return dtos.map { $0.toDomain() }
        }
    }

    public func markAsRead(alertId: String) async throws {
        if AppConstants.API.useMockData {
            try? await Task.sleep(nanoseconds: 50_000_000)
        } else {
            let _: EmptyPayload = try await apiClient.request(
                endpoint: AppConstants.API.Endpoints.markAlertRead(alertId),
                method: .post,
                requiresAuth: true
            )
        }
    }

    public func markAllAsRead() async throws {
        if AppConstants.API.useMockData {
            try? await Task.sleep(nanoseconds: 50_000_000)
        } else {
            let _: EmptyPayload = try await apiClient.request(
                endpoint: AppConstants.API.Endpoints.markAllAlertsRead,
                method: .post,
                requiresAuth: true
            )
        }
    }
}
