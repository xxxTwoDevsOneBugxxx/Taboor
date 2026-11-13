import Foundation
import CoreLocation
import Combine
import SwiftUI

// MARK: - Native CoreLocation Service for Real-Time GPS & Geocoding

public final class UserLocationService: NSObject, ObservableObject, CLLocationManagerDelegate {
    public static let shared = UserLocationService()

    @Published public var userCoordinate: CLLocationCoordinate2D?
    @Published public var selectedCustomCoordinate: CLLocationCoordinate2D?
    @Published public var selectedCustomArea: String?
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

    private static let keyLat = "taboor_last_known_lat"
    private static let keyLng = "taboor_last_known_lng"
    private static let keyArea = "taboor_last_known_area"
    private static let keyAddress = "taboor_last_known_address"

    public var isManualOverride: Bool {
        selectedCustomCoordinate != nil
    }

    public var effectiveCoordinate: CLLocationCoordinate2D {
        selectedCustomCoordinate ?? userCoordinate ?? defaultCairoCoordinate
    }

    public var effectiveAddress: String {
        selectedCustomArea ?? currentAddress
    }

    @MainActor
    public func setCustomLocation(coordinate: CLLocationCoordinate2D, areaName: String) {
        self.selectedCustomCoordinate = coordinate
        self.selectedCustomArea = areaName
        QueueDataService.shared.syncActiveTicketsWithUserLocation(coordinate)
    }

    @MainActor
    public func resetToGps() {
        self.selectedCustomCoordinate = nil
        self.selectedCustomArea = nil
        if isAuthorized {
            startTracking()
        }
    }

    private override init() {
        super.init()
        locationManager.delegate = self
        locationManager.desiredAccuracy = kCLLocationAccuracyNearestTenMeters
        locationManager.distanceFilter = 50 // Updates every 50 meters

        // Restore cached location from previous session for instant zero-latency start
        let def = UserDefaults.standard
        let savedLat = def.double(forKey: Self.keyLat)
        let savedLng = def.double(forKey: Self.keyLng)
        if savedLat != 0 && savedLng != 0 {
            self.userCoordinate = CLLocationCoordinate2D(latitude: savedLat, longitude: savedLng)
            if let area = def.string(forKey: Self.keyArea), !area.isEmpty {
                self.currentArea = area
            }
            if let addr = def.string(forKey: Self.keyAddress), !addr.isEmpty {
                self.currentAddress = addr
            }
        }

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
            } else if status == .denied || status == .restricted {
                self.fetchIpLocationFallback()
            }
        }
    }

    /// IP-based approximate geolocation fallback when GPS permission is denied
    public func fetchIpLocationFallback() {
        guard let url = URL(string: "https://ipapi.co/json/") else { return }
        Task {
            do {
                var request = URLRequest(url: url)
                request.timeoutInterval = 3.5
                let (data, response) = try await URLSession.shared.data(for: request)
                guard (response as? HTTPURLResponse)?.statusCode == 200 else { return }

                if let json = try? JSONSerialization.jsonObject(with: data) as? [String: Any],
                   let lat = json["latitude"] as? Double,
                   let lon = json["longitude"] as? Double,
                   let city = json["city"] as? String {
                    await MainActor.run {
                        if self.selectedCustomCoordinate == nil && self.userCoordinate == nil {
                            self.userCoordinate = CLLocationCoordinate2D(latitude: lat, longitude: lon)
                            self.currentArea = city
                            self.currentAddress = city
                        }
                    }
                }
            } catch {
                // Silently fallback to defaultCairoCoordinate
            }
        }
    }

    public func locationManager(_ manager: CLLocationManager, didUpdateLocations locations: [CLLocation]) {
        guard let location = locations.last else { return }

        Task { @MainActor in
            self.userCoordinate = location.coordinate
            self.isLocating = false

            // Persist coordinates for zero-latency instant startup on subsequent launches
            let def = UserDefaults.standard
            def.set(location.coordinate.latitude, forKey: Self.keyLat)
            def.set(location.coordinate.longitude, forKey: Self.keyLng)

            self.reverseGeocode(location: location)
            QueueDataService.shared.syncActiveTicketsWithUserLocation(location.coordinate)
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

                let def = UserDefaults.standard
                def.set(self.currentArea, forKey: Self.keyArea)
                def.set(self.currentAddress, forKey: Self.keyAddress)
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

    /// Formats driving duration cleanly (e.g. "ساعة و ١٠ دقائق", "ساعتان", "٢٥ دقيقة")
    public func formattedDriveTime(minutes: Int, isArabic: Bool) -> String {
        if minutes < 60 {
            return isArabic ? "\(max(1, minutes)) دقيقة" : "\(max(1, minutes)) min"
        }
        let hours = minutes / 60
        let remainingMins = minutes % 60
        if isArabic {
            let hourStr: String
            if hours == 1 {
                hourStr = "ساعة"
            } else if hours == 2 {
                hourStr = "ساعتان"
            } else if hours >= 3 && hours <= 10 {
                hourStr = "\(hours) ساعات"
            } else {
                hourStr = "\(hours) ساعة"
            }

            if remainingMins == 0 {
                return hourStr
            } else if remainingMins == 1 {
                return "\(hourStr) ودقيقة"
            } else if remainingMins == 2 {
                return "\(hourStr) ودقيقتان"
            } else if remainingMins >= 3 && remainingMins <= 10 {
                return "\(hourStr) و \(remainingMins) دقائق"
            } else {
                return "\(hourStr) و \(remainingMins) دقيقة"
            }
        } else {
            if remainingMins == 0 {
                return "\(hours) hr"
            }
            return "\(hours)h \(remainingMins)m"
        }
    }
}
