import Foundation
import CoreLocation
import Combine
import SwiftUI

// MARK: - Native CoreLocation Service for Real-Time GPS & Geocoding

public final class UserLocationService: NSObject, ObservableObject, CLLocationManagerDelegate {
    public static let shared = UserLocationService()

    @Published public var userCoordinate: CLLocationCoordinate2D?
    @Published public var currentArea: String = "المعادي، القاهرة"
    @Published public var currentAddress: String = "شارع النصر، المعادي"
    @Published public var isAuthorized: Bool = false
    @Published public var authorizationStatus: CLAuthorizationStatus = .notDetermined
    @Published public var isLocating: Bool = false

    private let locationManager = CLLocationManager()
    private let geocoder = CLGeocoder()
    private var cancellables = Set<AnyCancellable>()

    // Default Fallback Coordinate (Dokki / Central Cairo)
    public let defaultCairoCoordinate = CLLocationCoordinate2D(latitude: 30.0380153, longitude: 31.2130705)

    public var effectiveCoordinate: CLLocationCoordinate2D {
        userCoordinate ?? defaultCairoCoordinate
    }

    private override init() {
        super.init()
        locationManager.delegate = self
        locationManager.desiredAccuracy = kCLLocationAccuracyNearestTenMeters
        locationManager.distanceFilter = 50 // Updates every 50 meters
        self.authorizationStatus = locationManager.authorizationStatus
        checkInitialStatus()
    }

    // MARK: - Permission & Tracking Controls

    public func requestPermission() {
        if authorizationStatus == .notDetermined {
            locationManager.requestWhenInUseAuthorization()
        } else if authorizationStatus == .authorizedWhenInUse || authorizationStatus == .authorizedAlways {
            startTracking()
        }
    }

    public func startTracking() {
        guard CLLocationManager.locationServicesEnabled() else { return }
        isLocating = true
        locationManager.startUpdatingLocation()
    }

    public func stopTracking() {
        locationManager.stopUpdatingLocation()
        isLocating = false
    }

    private func checkInitialStatus() {
        let status = locationManager.authorizationStatus
        self.authorizationStatus = status
        self.isAuthorized = (status == .authorizedWhenInUse || status == .authorizedAlways)
        if isAuthorized {
            startTracking()
        }
    }

    // MARK: - CLLocationManagerDelegate

    public func locationManagerDidChangeAuthorization(_ manager: CLLocationManager) {
        Task { @MainActor in
            let status = manager.authorizationStatus
            self.authorizationStatus = status
            self.isAuthorized = (status == .authorizedWhenInUse || status == .authorizedAlways)

            if self.isAuthorized {
                self.startTracking()
            }
        }
    }

    public func locationManager(_ manager: CLLocationManager, didUpdateLocations locations: [CLLocation]) {
        guard let location = locations.last else { return }

        Task { @MainActor in
            self.userCoordinate = location.coordinate
            self.isLocating = false
            self.reverseGeocode(location: location)
        }
    }

    public func locationManager(_ manager: CLLocationManager, didFailWithError error: Error) {
        Task { @MainActor in
            self.isLocating = false
            print("[CoreLocation] Error: \(error.localizedDescription)")
        }
    }

    // MARK: - Reverse Geocoding (Coordinates to Real Arabic / English Area Name)

    private func reverseGeocode(location: CLLocation) {
        // Geocode in Arabic locale for native display
        let locale = Locale(identifier: "ar_EG")
        geocoder.reverseGeocodeLocation(location, preferredLocale: locale) { [weak self] placemarks, error in
            guard let self = self, let placemark = placemarks?.first, error == nil else {
                return
            }

            Task { @MainActor in
                let subLocality = placemark.subLocality ?? placemark.name ?? ""
                let locality = placemark.locality ?? ""
                let thoroughfare = placemark.thoroughfare ?? ""

                if !subLocality.isEmpty && !locality.isEmpty {
                    self.currentArea = "\(subLocality)، \(locality)"
                } else if !locality.isEmpty {
                    self.currentArea = locality
                } else if !subLocality.isEmpty {
                    self.currentArea = subLocality
                }

                if !thoroughfare.isEmpty && !subLocality.isEmpty {
                    self.currentAddress = "\(thoroughfare)، \(subLocality)"
                } else {
                    self.currentAddress = self.currentArea
                }
            }
        }
    }

    // MARK: - Distance & Driving Calculations

    /// Returns the real distance in meters between current user position and target
    public func distance(to target: CLLocationCoordinate2D) -> CLLocationDistance {
        let userLoc = CLLocation(latitude: effectiveCoordinate.latitude, longitude: effectiveCoordinate.longitude)
        let targetLoc = CLLocation(latitude: target.latitude, longitude: target.longitude)
        return userLoc.distance(from: targetLoc)
    }

    /// Formats distance human-readably (e.g., "850 م" or "2.4 كم")
    public func formattedDistance(to target: CLLocationCoordinate2D, isArabic: Bool) -> String {
        let meters = distance(to: target)
        if meters < 1000 {
            let rounded = Int(meters / 50.0) * 50
            return isArabic ? "\(max(50, rounded)) متر" : "\(max(50, rounded))m"
        } else {
            let km = meters / 1000.0
            let formatted = String(format: "%.1f", km)
            return isArabic ? "\(formatted) كم" : "\(formatted) km"
        }
    }

    /// Calculates realistic driving time based on real distance in traffic (avg 25 km/h in city)
    public func estimatedDriveMinutes(to target: CLLocationCoordinate2D) -> Int {
        let meters = distance(to: target)
        let km = meters / 1000.0
        // approx 2.5 minutes per km + 2 min buffer
        let mins = Int(km * 2.5) + 2
        return max(2, mins)
    }
}
