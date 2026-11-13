import Foundation
import CoreLocation
import MapKit

// MARK: - Calculated Driving Route Info (Real Turn-by-Turn Apple Maps Data)

public struct CalculatedRouteInfo: Equatable {
    public let coordinates: [CLLocationCoordinate2D]
    public let distanceMeters: CLLocationDistance
    public let travelTimeSeconds: TimeInterval
    public let primaryInstruction: String?
    public let stepDistanceMeters: CLLocationDistance?

    public init(
        coordinates: [CLLocationCoordinate2D],
        distanceMeters: CLLocationDistance,
        travelTimeSeconds: TimeInterval,
        primaryInstruction: String? = nil,
        stepDistanceMeters: CLLocationDistance? = nil
    ) {
        self.coordinates = coordinates
        self.distanceMeters = distanceMeters
        self.travelTimeSeconds = travelTimeSeconds
        self.primaryInstruction = primaryInstruction
        self.stepDistanceMeters = stepDistanceMeters
    }

    public func formattedDistance(isArabic: Bool) -> String {
        if distanceMeters < 1000 {
            let rounded = Int(distanceMeters / 50.0) * 50
            return isArabic ? "\(max(50, rounded)) متر" : "\(max(50, rounded))m"
        } else {
            let km = distanceMeters / 1000.0
            let formatted = String(format: "%.1f", km)
            return isArabic ? "\(formatted) كم" : "\(formatted) km"
        }
    }

    public func formattedDuration(isArabic: Bool) -> String {
        let minutes = max(1, Int(ceil(travelTimeSeconds / 60.0)))
        return UserLocationService.shared.formattedDriveTime(minutes: minutes, isArabic: isArabic)
    }

    public static func == (lhs: CalculatedRouteInfo, rhs: CalculatedRouteInfo) -> Bool {
        lhs.distanceMeters == rhs.distanceMeters &&
        lhs.travelTimeSeconds == rhs.travelTimeSeconds &&
        lhs.coordinates.count == rhs.coordinates.count
    }
}

// MARK: - Route Directions Service (Apple MKDirections Integration)

public final class RouteDirectionsService: ObservableObject {
    public static let shared = RouteDirectionsService()

    private var cache: [String: (route: CalculatedRouteInfo, timestamp: Date)] = [:]
    private let cacheTtl: TimeInterval = 120.0 // Cache valid for 2 minutes

    private init() {}

    /// Calculates real driving directions along streets & bridges using Apple Maps
    public func fetchRoute(
        from start: CLLocationCoordinate2D,
        to destination: CLLocationCoordinate2D
    ) async -> CalculatedRouteInfo? {
        let key = "\(String(format: "%.4f", start.latitude)),\(String(format: "%.4f", start.longitude))->\(String(format: "%.4f", destination.latitude)),\(String(format: "%.4f", destination.longitude))"

        if let cached = cache[key], Date().timeIntervalSince(cached.timestamp) < cacheTtl {
            return cached.route
        }

        let request = MKDirections.Request()
        request.source = MKMapItem(placemark: MKPlacemark(coordinate: start))
        request.destination = MKMapItem(placemark: MKPlacemark(coordinate: destination))
        request.transportType = .automobile

        let directions = MKDirections(request: request)
        do {
            let response = try await directions.calculate()
            if let mkRoute = response.routes.first {
                var coords = [CLLocationCoordinate2D](repeating: kCLLocationCoordinate2DInvalid, count: mkRoute.polyline.pointCount)
                mkRoute.polyline.getCoordinates(&coords, range: NSRange(location: 0, length: mkRoute.polyline.pointCount))

                var firstInstruction: String? = nil
                var firstStepDist: CLLocationDistance? = nil
                for step in mkRoute.steps {
                    let text = step.instructions.trimmingCharacters(in: .whitespacesAndNewlines)
                    if !text.isEmpty {
                        firstInstruction = text
                        firstStepDist = step.distance
                        break
                    }
                }

                let info = CalculatedRouteInfo(
                    coordinates: coords,
                    distanceMeters: mkRoute.distance,
                    travelTimeSeconds: mkRoute.expectedTravelTime,
                    primaryInstruction: firstInstruction,
                    stepDistanceMeters: firstStepDist
                )

                self.cache[key] = (info, Date())
                return info
            }
        } catch {
            print("[RouteDirectionsService] MKDirections error: \(error.localizedDescription)")
        }

        // Fallback: If network/Apple Maps fails, return a smooth fallback route
        return fallbackRoute(from: start, to: destination)
    }

    private func fallbackRoute(
        from start: CLLocationCoordinate2D,
        to destination: CLLocationCoordinate2D
    ) -> CalculatedRouteInfo {
        let userLoc = CLLocation(latitude: start.latitude, longitude: start.longitude)
        let destLoc = CLLocation(latitude: destination.latitude, longitude: destination.longitude)
        let straightMeters = userLoc.distance(from: destLoc)
        // Realistic city driving factor: ~1.35x road distance vs straight line
        let estimatedRoadMeters = straightMeters * 1.35
        let km = estimatedRoadMeters / 1000.0
        let seconds = (km * 2.5 + 2.0) * 60.0

        return CalculatedRouteInfo(
            coordinates: [start, destination],
            distanceMeters: estimatedRoadMeters,
            travelTimeSeconds: seconds,
            primaryInstruction: nil,
            stepDistanceMeters: nil
        )
    }
}
