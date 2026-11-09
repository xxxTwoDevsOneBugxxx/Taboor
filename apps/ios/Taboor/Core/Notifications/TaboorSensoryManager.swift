import Foundation
import UIKit
import AVFoundation
import CoreHaptics

// MARK: - Selectable Signature Taboor Chimes
public enum TaboorChimeOption: String, CaseIterable, Identifiable {
    case modernBell = "modern_bell"
    case crystalGlass = "crystal_glass"
    case softMarimba = "soft_marimba"
    case zenBell = "zen_bell"
    case dreamyCelestial = "dreamy_celestial"

    public var id: String { rawValue }

    public var titleAr: String {
        switch self {
        case .modernBell: return "جرس طابور الكلاسيكي"
        case .crystalGlass: return "رنين الزجاج السائل (Glass)"
        case .softMarimba: return "ماريمبا خشبية هادئة"
        case .zenBell: return "هدوء التأمل (Zen)"
        case .dreamyCelestial: return "نغم أثيري ناعم (Dreamy)"
        }
    }

    public var titleEn: String {
        switch self {
        case .modernBell: return "Classic Taboor Bell"
        case .crystalGlass: return "Liquid Glass Ping"
        case .softMarimba: return "Warm Marimba"
        case .zenBell: return "Zen Temple Bell"
        case .dreamyCelestial: return "Dreamy Celestial"
        }
    }

    public var subtitleAr: String {
        switch self {
        case .modernBell: return "نغمات صاعدة واضحة للأماكن المزدحمة"
        case .crystalGlass: return "نقرة زجاجية نقية متطابقة مع هوية التطبيق"
        case .softMarimba: return "رنين خشبي دافئ ومريح مناسب للعيادات"
        case .zenBell: return "رنة هادئة ومريحة للأعصاب"
        case .dreamyCelestial: return "كورد ناعم وخافت للمكاتب وأماكن العمل"
        }
    }

    public var subtitleEn: String {
        switch self {
        case .modernBell: return "Clear ascending chime for busy areas"
        case .crystalGlass: return "Pure crystal glass ping matching Taboor UI"
        case .softMarimba: return "Warm woody chime, ideal for clinics"
        case .zenBell: return "Calm and peaceful meditative bell"
        case .dreamyCelestial: return "Ethereal gentle chord for offices"
        }
    }

    public var icon: String {
        switch self {
        case .modernBell: return "bell.fill"
        case .crystalGlass: return "drop.fill"
        case .softMarimba: return "music.note"
        case .zenBell: return "leaf.fill"
        case .dreamyCelestial: return "sparkles"
        }
    }

    public var resourceName: String {
        switch self {
        case .modernBell: return "taboor_turn_chime"
        case .crystalGlass: return "taboor_glass_chime"
        case .softMarimba: return "taboor_marimba_chime"
        case .zenBell: return "taboor_zen_chime"
        case .dreamyCelestial: return "taboor_dreamy_chime"
        }
    }
}

// MARK: - Native Apple-Grade Sensory & Haptic Manager
public final class TaboorSensoryManager: NSObject, ObservableObject {
    public static let shared = TaboorSensoryManager()

    // Key for UserDefaults
    private let chimeStorageKey = "taboor_selected_chime"

    // Published selected chime
    @Published public var selectedChime: TaboorChimeOption {
        didSet {
            UserDefaults.standard.set(selectedChime.rawValue, forKey: chimeStorageKey)
        }
    }

    // MARK: - Core Haptics Engine
    private var hapticEngine: CHHapticEngine?
    private var isHapticsSupported: Bool = false

    // MARK: - Preloaded Audio Players
    private var players: [TaboorChimeOption: AVAudioPlayer] = [:]
    private var successPlayer: AVAudioPlayer?

    private override init() {
        if let saved = UserDefaults.standard.string(forKey: chimeStorageKey),
           let option = TaboorChimeOption(rawValue: saved),
           option != .modernBell {
            self.selectedChime = option
        } else {
            self.selectedChime = .softMarimba
        }

        super.init()
        setupHaptics()
        preloadAudio()
    }

    // MARK: - Setup Haptics Engine
    private func setupHaptics() {
        isHapticsSupported = CHHapticEngine.capabilitiesForHardware().supportsHaptics
        guard isHapticsSupported else { return }

        do {
            hapticEngine = try CHHapticEngine()
            hapticEngine?.resetHandler = { [weak self] in
                do {
                    try self?.hapticEngine?.start()
                } catch {
                    print("[SensoryManager] Failed to restart haptic engine: \(error)")
                }
            }
            hapticEngine?.stoppedHandler = { reason in
                print("[SensoryManager] Haptic engine stopped: \(reason)")
            }
            try hapticEngine?.start()
        } catch {
            print("[SensoryManager] Failed to initialize CoreHaptics engine: \(error)")
        }
    }

    // MARK: - Preload Audio Files
    private func preloadAudio() {
        do {
            try AVAudioSession.sharedInstance().setCategory(.ambient, mode: .default, options: [.mixWithOthers])
            try AVAudioSession.sharedInstance().setActive(true)
        } catch {
            print("[SensoryManager] AudioSession config error: \(error)")
        }

        // Preload all 4 selectable chimes
        for chime in TaboorChimeOption.allCases {
            if let url = Bundle.main.url(forResource: chime.resourceName, withExtension: "caf") ??
                         Bundle.main.url(forResource: chime.resourceName, withExtension: "wav") {
                do {
                    let player = try AVAudioPlayer(contentsOf: url)
                    player.prepareToPlay()
                    player.volume = 1.0
                    players[chime] = player
                } catch {
                    print("[SensoryManager] Failed to load audio for \(chime.resourceName): \(error)")
                }
            }
        }

        // Success Booking Chime
        if let url = Bundle.main.url(forResource: "taboor_success_chime", withExtension: "caf") ??
                     Bundle.main.url(forResource: "taboor_success_chime", withExtension: "wav") {
            do {
                successPlayer = try AVAudioPlayer(contentsOf: url)
                successPlayer?.prepareToPlay()
                successPlayer?.volume = 0.95
            } catch {
                print("[SensoryManager] Success player init error: \(error)")
            }
        }
    }

    // MARK: - Public Sensory Triggers

    /// Previews a specific chime with sound + matching tactile feedback
    public func previewChime(_ chime: TaboorChimeOption) {
        if let player = players[chime] {
            playAudio(player: player)
        }
        playChimePreviewHaptics(for: chime)
    }

    /// Plays the currently selected signature Taboor Turn Calling Chime + rich CoreHaptics alert
    public func playTurnCallingAlert(includeSound: Bool = true, includeHaptics: Bool = true) {
        if includeSound {
            if let player = players[selectedChime] {
                playAudio(player: player)
            }
        }

        if includeHaptics {
            playTurnCallingHaptics()
        }
    }

    /// Plays the uplifting Success Booking Chime + crisp double-tap haptic confirmation
    public func playSuccessAlert(includeSound: Bool = true, includeHaptics: Bool = true) {
        if includeSound {
            playAudio(player: successPlayer)
        }

        if includeHaptics {
            playSuccessHaptics()
        }
    }

    public func playSelectionClick() {
        UIImpactFeedbackGenerator(style: .light).impactOccurred()
    }

    // MARK: - Private Haptic Patterns

    private func playChimePreviewHaptics(for chime: TaboorChimeOption) {
        guard isHapticsSupported, let engine = hapticEngine else {
            UIImpactFeedbackGenerator(style: .medium).impactOccurred()
            return
        }

        do {
            try engine.start()
            let sharpness: Float
            let intensity: Float

            switch chime {
            case .modernBell:
                sharpness = 0.8
                intensity = 0.9
            case .crystalGlass:
                sharpness = 0.95
                intensity = 0.85
            case .softMarimba:
                sharpness = 0.4
                intensity = 0.75
            case .zenBell:
                sharpness = 0.3
                intensity = 0.8
            case .dreamyCelestial:
                sharpness = 0.55
                intensity = 0.7
            }

            let tap = CHHapticEvent(
                eventType: .hapticTransient,
                parameters: [
                    CHHapticEventParameter(parameterID: .hapticIntensity, value: intensity),
                    CHHapticEventParameter(parameterID: .hapticSharpness, value: sharpness)
                ],
                relativeTime: 0.0
            )

            let pattern = try CHHapticPattern(events: [tap], parameters: [])
            let player = try engine.makePlayer(with: pattern)
            try player.start(atTime: CHHapticTimeImmediate)
        } catch {
            UIImpactFeedbackGenerator(style: .light).impactOccurred()
        }
    }

    private func playTurnCallingHaptics() {
        guard isHapticsSupported, let engine = hapticEngine else {
            let generator = UINotificationFeedbackGenerator()
            generator.notificationOccurred(.warning)
            DispatchQueue.main.asyncAfter(deadline: .now() + 0.25) {
                generator.notificationOccurred(.success)
            }
            return
        }

        do {
            try engine.start()

            let tap1 = CHHapticEvent(
                eventType: .hapticTransient,
                parameters: [
                    CHHapticEventParameter(parameterID: .hapticIntensity, value: 0.7),
                    CHHapticEventParameter(parameterID: .hapticSharpness, value: 0.5)
                ],
                relativeTime: 0.0
            )

            let swell = CHHapticEvent(
                eventType: .hapticContinuous,
                parameters: [
                    CHHapticEventParameter(parameterID: .hapticIntensity, value: 0.75),
                    CHHapticEventParameter(parameterID: .hapticSharpness, value: 0.6)
                ],
                relativeTime: 0.12,
                duration: 0.28
            )

            let finalStrike = CHHapticEvent(
                eventType: .hapticTransient,
                parameters: [
                    CHHapticEventParameter(parameterID: .hapticIntensity, value: 1.0),
                    CHHapticEventParameter(parameterID: .hapticSharpness, value: 0.9)
                ],
                relativeTime: 0.42
            )

            let pattern = try CHHapticPattern(events: [tap1, swell, finalStrike], parameters: [])
            let player = try engine.makePlayer(with: pattern)
            try player.start(atTime: CHHapticTimeImmediate)
        } catch {
            UINotificationFeedbackGenerator().notificationOccurred(.success)
        }
    }

    private func playSuccessHaptics() {
        guard isHapticsSupported, let engine = hapticEngine else {
            UINotificationFeedbackGenerator().notificationOccurred(.success)
            return
        }

        do {
            try engine.start()

            let pulse1 = CHHapticEvent(
                eventType: .hapticTransient,
                parameters: [
                    CHHapticEventParameter(parameterID: .hapticIntensity, value: 0.65),
                    CHHapticEventParameter(parameterID: .hapticSharpness, value: 0.7)
                ],
                relativeTime: 0.0
            )

            let pulse2 = CHHapticEvent(
                eventType: .hapticTransient,
                parameters: [
                    CHHapticEventParameter(parameterID: .hapticIntensity, value: 0.95),
                    CHHapticEventParameter(parameterID: .hapticSharpness, value: 0.85)
                ],
                relativeTime: 0.09
            )

            let pattern = try CHHapticPattern(events: [pulse1, pulse2], parameters: [])
            let player = try engine.makePlayer(with: pattern)
            try player.start(atTime: CHHapticTimeImmediate)
        } catch {
            UINotificationFeedbackGenerator().notificationOccurred(.success)
        }
    }

    private func playAudio(player: AVAudioPlayer?) {
        guard let player = player else { return }
        if player.isPlaying {
            player.stop()
            player.currentTime = 0
        }
        player.play()
    }
}
