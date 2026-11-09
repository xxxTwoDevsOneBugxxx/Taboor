import Foundation

// MARK: - Places & Services Repository Protocol
public protocol PlacesRepositoryProtocol {
    func getPlaces() async throws -> [ServicePlace]
    func getPlace(id: String) async throws -> ServicePlace?
    func toggleFavorite(placeId: String) async throws
}

// MARK: - Places Repository Implementation (Mock-Ready & Real API Connected)
public final class PlacesRepository: PlacesRepositoryProtocol {
    public static let shared = PlacesRepository()

    private let apiClient: ApiClient
    private let mockLoader: MockDataLoader

    public init(apiClient: ApiClient = .shared, mockLoader: MockDataLoader = .shared) {
        self.apiClient = apiClient
        self.mockLoader = mockLoader
    }

    public func getPlaces() async throws -> [ServicePlace] {
        if AppConstants.API.useMockData {
            let dtos: [PlaceDTO] = try await mockLoader.load(filename: "places_seed.json", simulateDelay: 0.15)
            return dtos.map { $0.toDomain() }
        } else {
            let dtos: [PlaceDTO] = try await apiClient.request(
                endpoint: AppConstants.API.Endpoints.places,
                method: .get,
                requiresAuth: true
            )
            return dtos.map { $0.toDomain() }
        }
    }

    public func getPlace(id: String) async throws -> ServicePlace? {
        if AppConstants.API.useMockData {
            let places = try await getPlaces()
            return places.first(where: { $0.id == id })
        } else {
            let dto: PlaceDTO = try await apiClient.request(
                endpoint: AppConstants.API.Endpoints.placeDetails(id),
                method: .get,
                requiresAuth: true
            )
            return dto.toDomain()
        }
    }

    public func toggleFavorite(placeId: String) async throws {
        if AppConstants.API.useMockData {
            // Emulate backend confirmation
            try? await Task.sleep(nanoseconds: 50_000_000)
        } else {
            let _: EmptyPayload = try await apiClient.request(
                endpoint: AppConstants.API.Endpoints.toggleFavoritePlace(placeId),
                method: .post,
                requiresAuth: true
            )
        }
    }
}
