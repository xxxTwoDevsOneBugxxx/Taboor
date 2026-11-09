# Taboor iOS Application — Comprehensive Technical & UI/UX Audit Report

**Date of Audit:** November 9, 2026  
**Audited Target:** `Taboor.xcodeproj` (`apps/ios`)  
**Deployment Target:** iOS 26.0  
**Bundle Identifier:** `com.taboor.ios`  
**Architecture:** Native Swift 6.0 / SwiftUI (Zero Third-Party Dependencies)  
**Audit Type:** Strict Read-Only Technical & Ergonomic Evaluation  

---

## 1. Executive Summary

A comprehensive, read-only architectural, security, and UI/UX audit was conducted on the native iOS application for **Taboor (طابور)** located in `apps/ios`. The audit evaluated the application lifecycle, screen implementations, SwiftUI state management, network client and DTO mapping, Keychain and local persistence, native iOS 26 design integrations (materials, dynamic styling, Liquid Glass), Arabic RTL ergonomics, and build integrity.

### Key Takeaways
1. **Zero External Dependencies:** The application is built entirely using first-party Apple frameworks (`SwiftUI`, `Foundation`, `Combine`, `CoreLocation`, `MapKit`, `CoreHaptics`, `AudioToolbox`, `EventKit`, `UserNotifications`, `ActivityKit`, `AppIntents`). No CocoaPods, Carthage, or Swift Package Manager (SPM) packages are installed, ensuring clean binary size and zero third-party supply chain vulnerabilities.
2. **Hybrid Architecture Maturity:** While the Authentication flow (`Taboor/Features/Auth`) adheres to modern Clean Architecture with dedicated Use Cases (`LoginUseCase`, `RegisterUseCase`), ViewModels (`LoginViewModel`), and `@Observable` state handling, the core transactional flows (`HomeTabView`, `QueueTabView`, `ProfileTabView`, `ServiceDetailsView`) are structured as massive monolithic "God Views" (exceeding 1,300 lines of code each) coupled to a central 50-property singleton (`QueueDataService`).
3. **Backend Integration Readiness:** The client contains a fully functional, production-ready networking client (`ApiClient.swift`) integrated with the Taboor ASP.NET Core backend for Authentication (Login, Register, OTP verification, Password Reset). All other business domains (Places, Active Queues, Historical Tickets, Smart Alerts) are bridged via repository patterns reading from decoupled JSON seeds with simulated asynchronous network latencies.
4. **Dead Code Isolation:** A dedicated showcase module (`Features/LiquidGlassGallery/` containing 32 Swift files and ~3,500 lines of code) is compiled into the project target but has zero call sites in the user-facing navigation flow.
5. **Testing Deficit:** The Xcode project has 0 unit tests, 0 integration tests, and 0 UI tests configured.
6. **Build Integrity:** The project builds cleanly with **0 compiler warnings and 0 errors** on Xcode 26 for iOS 26 simulator targets.

---

## 2. Project Structure & Architecture

### 2.1 Monorepo & Directory Organization

The iOS project resides inside the Taboor monorepo:
```text
Taboor/
├── apps/
│   ├── api/                     # ASP.NET Core (.NET 10) Web API
│   ├── dashboard/               # TanStack React Web Management Portal
│   ├── mobile/                  # Flutter Client (Cross-platform)
│   └── ios/                     # Native iOS Client (Audited Target)
│       ├── project.yml          # XcodeGen specification
│       ├── Taboor.xcodeproj     # Generated Xcode Project
│       └── Taboor/              # Application Source Code
│           ├── App/             # Entry point & App lifecycle
│           ├── Core/            # Network, Storage, Navigation, Sensory, Utils
│           ├── Features/        # Domain features (Auth, Home, Queue, Profile, etc.)
│           ├── Shared/          # Reusable UI components & Design System
│           └── Resources/       # Assets.xcassets, Fonts, JSON Seeds
```

### 2.2 Architectural Layers

```
┌────────────────────────────────────────────────────────┐
│ App Layer: TaboorApp.swift                             │
│ Dynamic root state switching & deep link router        │
└───────────────────────────┬────────────────────────────┘
                            │
                            ▼
┌────────────────────────────────────────────────────────┐
│ Presentation Layer (SwiftUI Views & ViewModels)        │
│ Auth: Clean MVVM (@Observable)                         │
│ Core Tabs: Monolithic Views coupled to QueueDataService│
└───────────────────────────┬────────────────────────────┘
                            │
                            ▼
┌────────────────────────────────────────────────────────┐
│ Domain & Business Services                             │
│ QueueDataService, UserLocationService, SensoryManager, │
│ NotificationService, CalendarManager, LiveActivityMgr  │
└───────────────────────────┬────────────────────────────┘
                            │
                            ▼
┌────────────────────────────────────────────────────────┐
│ Data Access Layer (Repositories & Seed Bridges)        │
│ AuthRepository (Live REST API)                         │
│ Places / Queue / Alerts Repositories (JSON Seeded)     │
└───────────────────────────┬────────────────────────────┘
                            │
                            ▼
┌────────────────────────────────────────────────────────┐
│ Infrastructure & Foundation                            │
│ ApiClient (URLSession), KeychainManager (Security),    │
│ UserDefaultsManager (Foundation)                       │
└────────────────────────────────────────────────────────┘
```

### 2.3 Dependency Injection & State Management Assessment
- **Authentication Domain:** Modern, idiomatic Swift 6 architecture using `@Observable` macro, constructor-based dependency injection, clear separation between request/response DTOs and domain entities.
- **Main Application Domain:** Relies heavily on a central singleton `QueueDataService.shared` declared as an `ObservableObject`. This service contains over 50 `@Published` properties spanning active tickets, favorites, visit history, mock service places, alert counters, search filters, and booking transaction states. Any modification to a minor property triggers broad view re-evaluations across `HomeTabView`, `QueueTabView`, and `ProfileTabView`.

---

## 3. Complete Screen Inventory

Below is an exhaustive audit of all 24 identified screens, views, and modal sheets within the Taboor iOS project:

| # | Screen Name | Source File | Responsibility | UI/UX Status | Architecture Status | Backend Status |
|:---|:---|:---|:---|:---|:---|:---|
| 1 | **Splash Screen** | `Taboor/Features/Splash/SplashScreenView.swift` | App bootstrapper, branding animation, auto-session validation | High polish, fluid transition | Clean, lightweight | Live (Validates token) |
| 2 | **Onboarding** | `Taboor/Features/Onboarding/OnboardingView.swift` | 3-step value proposition carousel for first-time users | Native page tabs, smooth button scaling | Clean, isolated | Local state |
| 3 | **Login** | `Taboor/Features/Auth/Presentation/Views/LoginView.swift` | User sign-in with phone/email and password | Polished, floating labels, validation states | Strict Clean Architecture / MVVM | Live (REST API) |
| 4 | **Register** | `Taboor/Features/Auth/Presentation/Views/RegisterView.swift` | Account registration with full name, phone, email, password | Polished, inline field validation | Strict Clean Architecture / MVVM | Live (REST API) |
| 5 | **Forgot Password** | `Taboor/Features/Auth/Presentation/Views/ForgotPasswordView.swift` | Password recovery initiation via phone/email OTP | Polished, step-driven | Clean MVVM | Live (REST API) |
| 6 | **Main Tab Container** | `Taboor/Shared/Components/MainTabView.swift` | Floating Liquid Glass bottom navigation bar | Floating pill design, badge counts, haptics | Decoupled container | Local state |
| 7 | **Home Dashboard** | `Taboor/Features/Home/HomeTabView.swift` | Main discovery hub: active ticket card, categories, places | High polish, rich hero banners, location header | **Monolithic (1,360 LOC)** | Mock (Seeds) |
| 8 | **Queue Central** | `Taboor/Features/Queue/QueueTabView.swift` | Active queue status, waiting metrics, ticket actions | Fluid progress ring, live updates | **Monolithic (880 LOC)** | Mock (Seeds) |
| 9 | **Live Route Tracking** | `Taboor/Features/Queue/LiveQueueTrackingView.swift` | Turn-by-turn navigation overlay to clinic/branch | Native MapKit route polyline, live turn simulation | Self-contained sheet | Mock GPS coords |
| 10 | **Alerts & Notifications** | `Taboor/Features/Alerts/AlertsTabView.swift` | Chronological notifications feed with read/unread flags | Segmented filters, contextual action icons | Clean, modular subviews | Mock (Seeds) |
| 11 | **User Profile** | `Taboor/Features/Profile/ProfileTabView.swift` | User identity, preferences, sensory settings, legal links | Rich settings list, native system icons | **Monolithic (1,349 LOC)** | Mock / Local |
| 12 | **Edit Profile** | `Taboor/Features/Profile/ProfileTabView.swift` (Embedded) | Updating user avatar, display name, phone, and city | Clean form fields with validation | Subview inside Profile | Local state |
| 13 | **Change Password** | `Taboor/Features/Profile/ProfileTabView.swift` (Embedded) | Secure password rotation with security requirement hints | Secure fields with visibility toggles | Subview inside Profile | Live (Auth API) |
| 14 | **Sensory & Chimes Sheet**| `Taboor/Features/Profile/ProfileTabView.swift` (Embedded) | Auditory chime selection & CoreHaptics vibration preview | Native audio previews, checkmark pills | Decoupled via SensoryManager | Local settings |
| 15 | **All Services Catalog** | `Taboor/Features/Home/AllServicesView.swift` | Searchable catalog of all clinics, salons, repair shops | Dual List/Map toggle, category filter chips | Modular, dedicated ViewModel | Mock (Seeds) |
| 16 | **Service / Place Details**| `Taboor/Features/Home/ServiceDetailsView.swift` | Branch details, services list, working hours, booking | High visual polish, branch switcher, map pinned | **Monolithic (1,374 LOC)** | Mock (Seeds) |
| 17 | **Booking Confirmation** | `Taboor/Features/Home/ServiceDetailsView.swift` (Embedded) | Modal appointment booking sheet with slot selector | Native sheet presentation, haptic confirm | Embedded in ServiceDetails | Mock (Seeds) |
| 18 | **Favorite Places** | `Taboor/Features/Home/FavoritePlacesView.swift` | Bookmarked clinics and centers with quick action cards | Native back navigation, empty state illustration | Well-structured view | Mock (Seeds) |
| 19 | **Visit & Ticket History** | `Taboor/Features/Home/TicketHistoryView.swift` | Archived ticket records with status tags and re-booking | Filterable list, status badges, date groups | Well-structured view | Mock (Seeds) |
| 20 | **Smart Alert Settings** | `Taboor/Features/Alerts/SmartAlertPreferencesView.swift` | Proximity & turn notifications configuration | Native toggles, distance slider | Modular view | Local settings |
| 21 | **Business Partner Sheet**| `Taboor/Features/Home/BusinessPartnerSheetView.swift` | Lead generation modal for business owners to join | Clean modal form with submission feedback | Independent sheet | Local feedback |
| 22 | **QR Code Scanner** | `Taboor/Features/Queue/QRScannerView.swift` | Camera-based barcode scanner for on-site check-in | AVFoundation camera feed, target reticle | Modular controller wrapper | Hardware / Local |
| 23 | **Help & Support** | `Taboor/Features/Profile/ProfileTabView.swift` (Embedded) | FAQ accordion, support contact links, ticket dispute | Clean native disclosure groups | Subview inside Profile | Static content |
| 24 | **Liquid Glass Showcase**| `Taboor/Features/LiquidGlassGallery/` (32 views) | Architectural design system showcase | High material polish, interactive controls | **Dormant (Unreachable)** | None (Demo) |

---

## 4. UI/UX Consistency Audit

### 4.1 Typography & Visual Hierarchy
- **Primary Typography:** Uses Apple's native system typography (`San Francisco` and `SF Arabic`) with dynamic scale variants (`.largeTitle`, `.title2`, `.headline`, `.subheadline`, `.caption`).
- **Hierarchy:** Headers, section titles, and action triggers maintain distinct font weight tiers (`.bold`, `.semibold`, `.regular`).
- **Inconsistency Identified:** Several views hardcode fixed pixel sizes (e.g. `.font(.system(size: 13, weight: .medium))`) instead of using dynamic type categories (`.caption`, `.footnote`). This impairs accessibility when users configure larger Dynamic Type sizes in iOS Settings.

### 4.2 Color System & Surfaces
- **Brand Colors:** Centered around Taboor Emerald Teal (`Color(hex: "0D9488")`), Navy (`Color(hex: "0F172A")`), Soft Gray backgrounds (`Color(hex: "F8FAFC")`), and semantic status tones (Success Green, Warning Amber, Destructive Crimson).
- **Surface Elevation:** Cards and panels utilize subtle shadows (`color: .black.opacity(0.04), radius: 10, y: 4`) with unified border radii (16pt to 20pt for cards, 24pt for bottom sheets, 999pt for capsule pills).
- **Dark Mode Limitation:** The application project defines `UIUserInterfaceStyle: Light` in `project.yml` and enforces `.preferredColorScheme(.light)` at the root `TaboorApp.swift`. Semantic colors are not backed by dynamic asset catalog slots (`Assets.xcassets` contains hardcoded light variations only).

### 4.3 Arabic RTL (Right-to-Left) & Localization
- **Layout Direction:** Layouts dynamically honor `LayoutDirection.rightToLeft` when Arabic is active.
- **Pills and Icon Anchors:** Back buttons, disclosure arrows, and chevron icons properly invert using `.flipsForRightToLeftLayoutDirection(true)`.
- **Search Bar Alignment:** Search placeholders and inputs in `HomeTabView` and `AllServicesView` respect text alignment rules.
- **Localization Infrastructure:** The project uses programmatic localization dictionaries (`L10n.swift`) rather than native Apple String Catalogs (`.xcstrings`). While functional, it lacks Xcode's compile-time string validation and plural rule resolution (`.stringsdict`).

### 4.4 Touch Targets & Accessibility
- **Minimum Target Size:** Primary buttons (`TaboorPrimaryButton`) comply with Apple's Human Interface Guidelines (minimum 44×44pt tap target).
- **Accessibility Labels:** Icon-only buttons (such as the navigation back button, heart bookmark button, and filter chips) lack explicit `.accessibilityLabel(...)` modifiers, forcing VoiceOver to read raw system SF Symbol names (e.g., "chevron.backward").

---

## 5. iOS 26 & Liquid Glass Audit

### 5.1 Native iOS 26 System Materials
- **Bottom Navigation Bar:** Implemented as a floating capsule in `MainTabView.swift` using `.background(.ultraThinMaterial)` with a subtle inner border highlight (`Color.white.opacity(0.2)`). This provides a modern iOS 26 frosted glass aesthetic while maintaining legibility over scrolling content.
- **Modals & Sheets:** Modal sheets (`LiveQueueTrackingView`, `BusinessPartnerSheetView`, `BookingSheet`) leverage native SwiftUI presentation detents (`.presentationDetents([.medium, .large])`) with background blur.

### 5.2 Liquid Glass Gallery Module Isolation
- Inside `Taboor/Features/LiquidGlassGallery/`, 32 Swift files showcase sophisticated shaders, light refractions, specular highlights, and fluid chromatic aberrations.
- **Problem:** This entire gallery is isolated. There are no navigation triggers, settings entries, or developer flags linking to `LiquidGlassGalleryView.swift`.
- **Impact:** Approximately 3,500 lines of dead code and associated assets are compiled into the production binary without serving user-facing functionality.

---

## 6. Navigation Audit

### 6.1 Routing Architecture
- **Root Router:** `TaboorApp.swift` utilizes an application-level state enum:
  ```swift
  enum AppState {
      case splash
      case onboarding
      case login
      case register
      case home
  }
  ```
- **Tab Switching:** Managed via `SelectedTab` enum (`.home`, `.services`, `.queue`, `.alerts`, `.profile`).
- **Sub-screen Navigation:** Implemented using `.navigationDestination(isPresented:)` and `.sheet(isPresented:)` modifiers.

### 6.2 Deep Linking & External Triggers
The application implements URL scheme handling (`taboor://`) and Siri User Activities:
- `taboor://ticket` ➔ Opens `QueueTabView` with active ticket selection.
- `taboor://history` ➔ Pushes `TicketHistoryView`.
- `taboor://favorites` ➔ Pushes `FavoritePlacesView`.
- `taboor://alerts-settings` ➔ Pushes `SmartAlertPreferencesView`.
- `taboor://services?category={id}` ➔ Opens `AllServicesView` filtered by category.

### 6.3 Navigation Inconsistencies
1. **Root Navigation Stacks:** Several screens embed their own `NavigationStack` while simultaneously being pushed from a parent `NavigationStack`. This can cause double navigation bars or navigation title clipping during back gestures.
2. **Sheet Dismissal Latency:** When dismissing full-screen modal sheets (`LiveQueueTrackingView`), state variables in `QueueDataService` are reset asynchronously after view disappearance, occasionally resulting in temporary visual glitches on the underlying queue screen.

---

## 7. Networking & API Readiness

### 7.1 Implemented & Working Endpoints (Authentication)
The Taboor ASP.NET Core API at `https://taboorapi.runasp.net/api/` is actively integrated for all authentication use cases:
- `POST /api/auth/login` (Handled via `AuthRepository.login(...)`)
- `POST /api/auth/register` (Handled via `AuthRepository.register(...)`)
- `POST /api/auth/verify-otp` (Handled via `AuthRepository.verifyOtp(...)`)
- `POST /api/auth/forgot-password` (Handled via `AuthRepository.forgotPassword(...)`)
- `POST /api/auth/reset-password` (Handled via `AuthRepository.resetPassword(...)`)
- `POST /api/auth/refresh-token` (Handled via `AuthRepository.refreshToken(...)`)
- `POST /api/auth/revoke-token` (Handled via `AuthRepository.revokeToken(...)`)

### 7.2 Mock Data & Decoupled Repositories
Because backend endpoints for Places, Queues, Tickets, and Alerts are currently under construction in the backend roadmap, the iOS app employs mock repositories that read from local JSON resources:
- `places_seed.json` ➔ Decoded into `[ServicePlaceDTO]` via `PlacesRepository.swift`.
- `active_tickets_seed.json` ➔ Decoded into `[ActiveTicketDTO]` via `QueueRepository.swift`.
- `history_seed.json` ➔ Decoded into `[TicketHistoryDTO]` via `QueueRepository.swift`.
- `alerts_seed.json` ➔ Decoded into `[AlertNotificationDTO]` via `AlertsRepository.swift`.
- `user_profile_seed.json` ➔ Decoded into `UserProfileDTO` via `UserRepository.swift`.

**Design Readiness Assessment:**  
The repository layer cleanly abstracts data retrieval. When backend endpoints become available, switching to live networking requires altering only the `AppConstants.useMockData` flag and providing endpoint path bindings within `PlacesRepository` and `QueueRepository`, with **zero modifications** required in the presentation layer.

### 7.3 Token Expiration & Silent Refresh Defect
In `ApiClient.swift`:
```swift
if httpResponse.statusCode == 401 {
    await KeychainManager.shared.clearAuthTokens()
    throw ApiError.unauthorized
}
```
**Defect:** When an access token expires (HTTP 401), `ApiClient` immediately wipes the stored authentication credentials and throws an error rather than intercepting the 401, issuing a `POST /api/auth/refresh-token` request using the stored refresh token, and retrying the failed request.

---

## 8. Security & Reliability Audit

### 8.1 Keychain & Sensitive Storage
- **Token Storage:** Access tokens and refresh tokens are stored securely in the iOS Keychain via `KeychainManager.swift` using `kSecClassGenericPassword` with `kSecAttrAccessibleAfterFirstUnlock`.
- **Security Flaw (User Profile in UserDefaults):** The complete `User` domain model (containing full name, phone number, email address, and user identifier) is serialized into JSON and stored in unencrypted `UserDefaults` via `UserDefaultsManager.swift`. Sensitive PII should either be stored in the Keychain or held in volatile in-memory storage.

### 8.2 Transport Security & Secrets
- **App Transport Security (ATS):** The application communicates strictly over HTTPS (`https://taboorapi.runasp.net`). No insecure HTTP exceptions are configured in `Info.plist`.
- **API Secrets:** No private API keys or certificates are hardcoded into the client codebase.

### 8.3 Force Unwrapping & Crash Vulnerabilities
- The codebase exhibits good Swift safety practices, generally avoiding force unwraps (`!`).
- **Identified Risk:** In `MockDataLoader.swift`, bundle resource resolution relies on force-unwrapped URLs:
  ```swift
  let url = Bundle.main.url(forResource: filename, withExtension: "json")!
  ```
  If a seed JSON file is missing from the Xcode target's Copy Bundle Resources phase during a build, the application will crash immediately on boot.

---

## 9. Code Quality Findings

### 9.1 Massive Monolithic Views ("God Views")
Several views violate single-responsibility principles and contain excessive lines of code:
1. `HomeTabView.swift`: **1,360 lines** (combines location fetching, category carousels, active ticket cards, promotional banners, branch search, sheet modals).
2. `ServiceDetailsView.swift`: **1,374 lines** (combines gallery hero, branch switcher, service selection, calendar date picking, time slot calculation, booking submission).
3. `ProfileTabView.swift`: **1,349 lines** (combines settings navigation, edit profile form, change password sheet, chimes picker, help FAQ).
4. `QueueTabView.swift`: **880 lines** (combines progress calculation, multi-ticket carousel, delay alerts, action dispatchers).

### 9.2 God Object State Pattern (`QueueDataService`)
`QueueDataService.swift` is an `ObservableObject` with **52 published properties**. This creates high coupling where views listening to `QueueDataService` are invalidated and re-rendered when unrelated properties change.

### 9.3 Duplicated Formatting Logic
Date and currency formatting functions are duplicated across `HomeTabView`, `QueueTabView`, and `TicketHistoryView` instead of utilizing centralized formatters.

---

## 10. Build & Testing Results

### 10.1 Build Execution
The project build was executed via `xcodebuild` targeting an iOS 26 Simulator:
```bash
xcodebuild -project Taboor.xcodeproj -scheme Taboor -destination "platform=iOS Simulator,id=A2D8B351-1169-4BE6-B46D-1BE5D0E81117" build
```
- **Result:** `** BUILD SUCCEEDED **`
- **Compiler Warnings:** 0 warnings
- **Compiler Errors:** 0 errors

### 10.2 Testing Coverage
- **Unit Test Targets:** 0 configured.
- **UI Test Targets:** 0 configured.
- **Code Coverage:** 0%.
- **Impact:** Regression testing relies entirely on manual interaction. Changes to models or formatting logic cannot be verified automatically.

---

## 11. Issues Classified by Severity

### 11.1 Critical Severity (0 Issues)
*No security vulnerabilities, data loss scenarios, or persistent crash bugs were detected.*

---

### 11.2 High Severity (3 Issues)

#### [H-01] Missing Automatic Silent Token Refresh Interceptor
- **Severity:** High
- **Affected Component:** `ApiClient`
- **File:** `Taboor/Core/Network/ApiClient.swift` (Lines 82–89)
- **Problem:** Upon receiving an HTTP 401 Unauthorized response, `ApiClient` immediately purges tokens from the Keychain and throws an error, logging the user out.
- **Why It Matters:** Users are abruptly signed out when their short-lived access token expires, even though a valid refresh token exists in the Keychain.
- **Recommended Solution:** Implement an automatic token refresh retry loop in `ApiClient.request(...)` using an asynchronous Swift Concurrency actor or lock to refresh the token and replay the original request.

#### [H-02] Monolithic Views Violating SwiftUI Lifecycle & Single Responsibility
- **Severity:** High
- **Affected Components:** `HomeTabView`, `ServiceDetailsView`, `ProfileTabView`, `QueueTabView`
- **Files:**
  - `Taboor/Features/Home/HomeTabView.swift` (1,360 LOC)
  - `Taboor/Features/Home/ServiceDetailsView.swift` (1,374 LOC)
  - `Taboor/Features/Profile/ProfileTabView.swift` (1,349 LOC)
  - `Taboor/Features/Queue/QueueTabView.swift` (880 LOC)
- **Problem:** Core views contain upwards of 1,000 lines combining state, subviews, business logic, and navigation bindings.
- **Why It Matters:** High risk of UI re-render lag, difficult team collaboration, high merge conflict frequency, and near-impossible unit testability.
- **Recommended Solution:** Extract subviews into distinct components (`HomeHeroHeaderView`, `ServiceBranchPickerView`, `EditProfileSheet`) and extract state/event handling into dedicated `@Observable` ViewModels.

#### [H-03] Overloaded Central Singleton (`QueueDataService`)
- **Severity:** High
- **Affected Component:** State Management
- **File:** `Taboor/Core/Services/QueueDataService.swift`
- **Problem:** A single `ObservableObject` manages 50+ published properties across disparate domains (Queues, Places, Alerts, User Preferences).
- **Why It Matters:** Triggers unnecessary view updates across the entire application hierarchy on minor state changes.
- **Recommended Solution:** Decompose `QueueDataService` into focused domain stores: `ActiveQueueStore`, `FavoritesStore`, `AlertsStore`, and `PreferencesStore`.

---

### 11.3 Medium Severity (4 Issues)

#### [M-01] Unencrypted PII Storage in UserDefaults
- **Severity:** Medium
- **Affected Component:** Local Persistence
- **File:** `Taboor/Core/Storage/UserDefaultsManager.swift` (Lines 45–58)
- **Problem:** Full user profiles containing phone number, email, and user ID are stored as plain JSON in `UserDefaults`.
- **Why It Matters:** `UserDefaults` is stored in unencrypted plist files on disk, making sensitive personal information readable on jailbroken devices or unencrypted local backups.
- **Recommended Solution:** Move user profile persistence into the iOS Keychain or encrypt the serialized payload with Apple's `CryptoKit`.

#### [M-02] Dormant Dead Code Module (`LiquidGlassGallery`)
- **Severity:** Medium
- **Affected Component:** Project Structure & Binary Size
- **File:** `Taboor/Features/LiquidGlassGallery/` (32 Swift files)
- **Problem:** 32 views and shader experiments are compiled into the binary but are completely unreachable from the user interface.
- **Why It Matters:** Increases binary size, extends compilation times, and clutters the codebase.
- **Recommended Solution:** Either integrate the gallery into a Developer Settings screen in debug builds or extract it into a separate showcase repository/target.

#### [M-03] Complete Lack of Automated Test Suites
- **Severity:** Medium
- **Affected Component:** Quality Assurance & Testing
- **File:** `Taboor.xcodeproj`
- **Problem:** Zero unit or UI test targets exist in the Xcode configuration.
- **Why It Matters:** Critical calculation logic (waiting time calculations, ticket sequence sorting, validation rules) cannot be automatically validated against regression.
- **Recommended Solution:** Add `TaboorTests` target covering `AuthRepository`, `QueueDataService`, `ValidationRules`, and date formatters.

#### [M-04] Crash Vulnerability in Seed Data Bundle Loading
- **Severity:** Medium
- **Affected Component:** Mock Data Loading
- **File:** `Taboor/Core/Utils/MockDataLoader.swift` (Line 18)
- **Problem:** Resource loading uses a force-unwrapped bundle URL: `Bundle.main.url(forResource: filename, withExtension: "json")!`.
- **Why It Matters:** A missing resource file causes an immediate runtime crash on application launch.
- **Recommended Solution:** Replace force-unwrapping with `guard let url = Bundle.main.url(...) else { throw MockDataError.fileNotFound }`.

---

### 11.4 Low Severity (3 Issues)

#### [L-01] Hardcoded Light Mode Enforcement
- **Severity:** Low
- **Affected Component:** UI/UX Theming
- **File:** `Taboor/App/TaboorApp.swift` (Line 64) and `project.yml`
- **Problem:** App forces `.preferredColorScheme(.light)`, disabling iOS Dark Mode.
- **Why It Matters:** Prevents users who prefer Dark Mode from having a consistent system experience.
- **Recommended Solution:** Introduce dynamic semantic color tokens in `Assets.xcassets` and remove the hardcoded light scheme constraint.

#### [L-02] Missing Accessibility VoiceOver Labels on Action Icons
- **Severity:** Low
- **Affected Component:** Accessibility (a11y)
- **File:** `Taboor/Shared/Components/TaboorHeaderBar.swift`, `FavoritePlacesView.swift`
- **Problem:** Icon-only navigation buttons do not declare `.accessibilityLabel(...)`.
- **Why It Matters:** Visually impaired users relying on VoiceOver hear raw symbol identifiers instead of descriptive actions.
- **Recommended Solution:** Add explicit `.accessibilityLabel("الرجوع")` and `.accessibilityLabel("إضافة للمفضلة")` modifiers.

#### [L-03] Programmatic Localization Instead of Native String Catalogs
- **Severity:** Low
- **Affected Component:** Internationalization
- **File:** `Taboor/Core/Utils/L10n.swift`
- **Problem:** Localization uses manual Swift structs instead of Apple's modern `.xcstrings` format.
- **Why It Matters:** Misses out on compile-time key validation, automatic pluralization rules, and native localization tooling.
- **Recommended Solution:** Migrate string keys to an Apple String Catalog (`Localizable.xcstrings`).

---

## 12. Recommended Implementation Roadmap

```
Phase 1: Security & Networking Resilience (Week 1)
├── [H-01] Implement 401 silent token refresh loop in ApiClient
├── [M-01] Migrate user profile storage from UserDefaults to Keychain
└── [M-04] Guard bundle file resolution in MockDataLoader

Phase 2: Architectural Modularization (Weeks 2–3)
├── [H-03] Decompose QueueDataService into domain-specific stores
├── [H-02] Refactor HomeTabView into modular subviews & ViewModel
├── [H-02] Refactor ServiceDetailsView into modular subviews & ViewModel
└── [H-02] Refactor ProfileTabView into modular subviews & ViewModel

Phase 3: Clean Up & Testing Infrastructure (Week 4)
├── [M-03] Configure TaboorTests target & add unit test suite
├── [M-02] Remove or conditionalize LiquidGlassGallery
├── [L-01] Implement native Dark Mode color palettes
└── [L-02] Add VoiceOver accessibility labels
```

---

## 13. Overall Project Health Assessment

| Dimension | Grade | Assessment Summary |
|:---|:---:|:---|
| **Build Stability** | **A+** | Compiles with 0 warnings and 0 errors on Xcode 26 / iOS 26 target. |
| **Dependencies** | **A+** | Pure native first-party implementation (0 external packages). |
| **Visual Polish & Ergonomics** | **A** | Exceptional visual presentation, fluid materials, and strong Arabic RTL handling. |
| **Security Foundation** | **B+** | Strong Keychain token handling; minor flaw in UserDefaults PII storage. |
| **API & Backend Readiness** | **B+** | Live Auth integration; mock repositories cleanly decoupled for future API drop-in. |
| **Code Architecture & Structure**| **C+** | High coupling in core views (1,000+ LOC each) and monolithic state singleton. |
| **Testing Coverage** | **F** | 0% automated test coverage (0 unit/UI tests). |
| **OVERALL RATING** | **B** | **Production-grade visual quality with architectural refactoring needed for scale.** |

---
*Report compiled autonomously via read-only inspection protocol.*
