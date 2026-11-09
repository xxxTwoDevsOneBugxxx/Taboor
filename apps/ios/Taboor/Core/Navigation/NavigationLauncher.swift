import SwiftUI
import MapKit

// MARK: - External Maps & Turn-by-Turn Navigation Launcher

public enum NavigationApp: String, CaseIterable, Identifiable {
    case appleMaps
    case googleMaps

    public var id: String { rawValue }

    public func title(isArabic: Bool) -> String {
        switch self {
        case .appleMaps: return isArabic ? "خرائط آبل (Apple Maps)" : "Apple Maps"
        case .googleMaps: return isArabic ? "خرائط جوجل (Google Maps)" : "Google Maps"
        }
    }

    public var iconName: String {
        switch self {
        case .appleMaps: return "map.fill"
        case .googleMaps: return "location.fill"
        }
    }
}

public final class NavigationLauncher {
    public static let shared = NavigationLauncher()

    private init() {}

    /// Checks if a navigation app is installed on the device
    public func isAvailable(_ app: NavigationApp) -> Bool {
        switch app {
        case .appleMaps:
            return true
        case .googleMaps:
            guard let url = URL(string: "comgooglemaps://") else { return false }
            return UIApplication.shared.canOpenURL(url)
        }
    }

    /// Opens the specified coordinates in the chosen app
    public func open(app: NavigationApp, latitude: Double, longitude: Double, destinationName: String = "الوجهة") {
        switch app {
        case .appleMaps:
            let coordinate = CLLocationCoordinate2D(latitude: latitude, longitude: longitude)
            let placemark = MKPlacemark(coordinate: coordinate)
            let mapItem = MKMapItem(placemark: placemark)
            mapItem.name = destinationName
            let launchOptions = [
                MKLaunchOptionsDirectionsModeKey: MKLaunchOptionsDirectionsModeDriving
            ]
            mapItem.openInMaps(launchOptions: launchOptions)

        case .googleMaps:
            if let googleAppUrl = URL(string: "comgooglemaps://?daddr=\(latitude),\(longitude)&directionsmode=driving"),
               UIApplication.shared.canOpenURL(googleAppUrl) {
                UIApplication.shared.open(googleAppUrl)
            } else if let webUrl = URL(string: "https://www.google.com/maps/dir/?api=1&destination=\(latitude),\(longitude)&travelmode=driving") {
                UIApplication.shared.open(webUrl)
            }
        }
    }
}
