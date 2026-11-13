import SwiftUI

// MARK: - Reusable Service Card Component (Pure Apple HIG Standard)

public struct ServiceCardView: View {
    public let place: ServicePlace
    public let onSelect: () -> Void

    private let localization = LocalizationManager.shared
    @Environment(\.colorScheme) private var colorScheme

    public init(place: ServicePlace, onSelect: @escaping () -> Void) {
        self.place = place
        self.onSelect = onSelect
    }

    public var body: some View {
        Button(action: onSelect) {
            VStack(alignment: .leading, spacing: 12) {
                // Top Row: Icon + Place Information (Full Width, No Truncation)
                HStack(alignment: .center, spacing: 12) {
                    // Place Icon (Apple-style Squircle)
                    ZStack {
                        RoundedRectangle(cornerRadius: 14, style: .continuous)
                            .fill(place.color.opacity(colorScheme == .dark ? 0.22 : 0.12))
                            .frame(width: 48, height: 48)

                        Image(systemName: place.iconName)
                            .font(.system(size: 20, weight: .semibold))
                            .foregroundColor(place.color)
                    }

                    // Titles & Location
                    VStack(alignment: .leading, spacing: 4) {
                        HStack(spacing: 5) {
                            Text(place.localizedName(isArabic: localization.isArabic))
                                .font(.app(15.5, .bold, isArabic: localization.isArabic))
                                .foregroundColor(AppColors.textPrimary)
                                .lineLimit(2)
                                .fixedSize(horizontal: false, vertical: true)

                            Image(systemName: "checkmark.seal.fill")
                                .font(.system(size: 13, weight: .semibold))
                                .foregroundColor(AppColors.teal)
                        }

                        if let branch = place.primaryBranch {
                            HStack(spacing: 5) {
                                // Rating Pill inside subtitle
                                HStack(spacing: 2.5) {
                                    Image(systemName: "star.fill")
                                        .font(.system(size: 10, weight: .bold))
                                        .foregroundColor(AppColors.amber)

                                    Text(String(format: "%.1f", place.starRating))
                                        .font(.system(size: 12, weight: .bold, design: .rounded))
                                        .foregroundColor(AppColors.textPrimary)
                                }

                                Text("•")
                                    .font(.system(size: 10))
                                    .foregroundColor(AppColors.textTertiary)

                                Text(branch.localizedLabel(isArabic: localization.isArabic))
                                    .font(.app(12, .medium, isArabic: localization.isArabic))
                                    .foregroundColor(AppColors.textSecondary)
                                    .lineLimit(1)

                                Text("•")
                                    .font(.system(size: 10))
                                    .foregroundColor(AppColors.textTertiary)

                                Text(branch.realDistanceFormatted(isArabic: localization.isArabic))
                                    .font(.app(12, .bold, isArabic: localization.isArabic))
                                    .foregroundColor(AppColors.teal)
                            }
                        }
                    }

                    Spacer()
                }

                // Bottom Row: Unified Live Queue Status Pill + Booking CTA
                HStack(alignment: .center) {
                    // Unified Queue Status Pill (Wait Count + Est Time)
                    HStack(spacing: 6) {
                        Circle()
                            .fill(place.totalWaiting <= 2 ? AppColors.accentGreen : AppColors.amber)
                            .frame(width: 7, height: 7)

                        Text(localization.isArabic
                             ? "\(place.totalWaiting) في الانتظار"
                             : "\(place.totalWaiting) waiting")
                            .font(.app(12, .bold, isArabic: localization.isArabic))
                            .foregroundColor(place.totalWaiting <= 2 ? AppColors.accentGreen : AppColors.amber)

                        if let branch = place.primaryBranch {
                            Text("•")
                                .font(.system(size: 10))
                                .foregroundColor(Color(UIColor.tertiaryLabel))

                            Text(localization.isArabic
                                 ? "~\(branch.estWaitMinutes) دقيقة"
                                 : "~\(branch.estWaitMinutes)m")
                                .font(.app(11.5, .medium, isArabic: localization.isArabic))
                                .foregroundColor(AppColors.textSecondary)
                        }
                    }
                    .padding(.horizontal, 10)
                    .padding(.vertical, 6)
                    .background(
                        (place.totalWaiting <= 2 ? AppColors.accentGreen : AppColors.amber)
                            .opacity(colorScheme == .dark ? 0.18 : 0.08)
                    )
                    .clipShape(Capsule())

                    Spacer()

                    // Clean CTA Action Arrow
                    HStack(spacing: 4) {
                        Text(localization.isArabic ? "حجز دور" : "Book")
                            .font(.app(12, .bold, isArabic: localization.isArabic))
                            .foregroundColor(AppColors.teal)

                        Image(systemName: localization.isArabic ? "chevron.left" : "chevron.right")
                            .font(.system(size: 11, weight: .bold))
                            .foregroundColor(AppColors.teal)
                    }
                    .padding(.horizontal, 10)
                    .padding(.vertical, 5)
                    .background(AppColors.teal.opacity(colorScheme == .dark ? 0.18 : 0.08))
                    .clipShape(Capsule())
                }
            }
            .padding(14)
            .background(AppColors.cardBackground)
            .clipShape(RoundedRectangle(cornerRadius: 16, style: .continuous))
            .overlay(
                RoundedRectangle(cornerRadius: 16, style: .continuous)
                    .strokeBorder(AppColors.cardBorder, lineWidth: 1)
            )
            .shadow(color: Color.black.opacity(colorScheme == .dark ? 0 : 0.03), radius: 6, x: 0, y: 2)
        }
        .buttonStyle(ScaleButtonStyle(scale: 0.98, pressedOpacity: 0.92))
    }
}

// MARK: - Category Pill Component

public struct CategoryFilterPill: View {
    public let category: ServiceCategory
    public let isSelected: Bool
    public let onSelect: () -> Void

    private let localization = LocalizationManager.shared
    @Environment(\.colorScheme) private var colorScheme

    public var body: some View {
        Button(action: onSelect) {
            HStack(spacing: 6) {
                Image(systemName: category.iconName)
                    .font(.system(size: 12, weight: .semibold))

                Text(category.title(isArabic: localization.isArabic))
                    .font(.app(12.5, isSelected ? .bold : .medium, isArabic: localization.isArabic))
                    .fixedSize(horizontal: true, vertical: false)
            }
            .foregroundColor(isSelected ? .white : AppColors.textPrimary)
            .padding(.horizontal, 14)
            .padding(.vertical, 8)
            .background(
                isSelected
                    ? AppColors.teal
                    : AppColors.cardBackground
            )
            .clipShape(Capsule())
            .overlay(
                Capsule()
                    .strokeBorder(
                        isSelected ? Color.clear : AppColors.cardBorder,
                        lineWidth: 1
                    )
            )
            .shadow(color: isSelected ? AppColors.teal.opacity(0.25) : Color.clear, radius: 4, y: 2)
        }
        .buttonStyle(ScaleButtonStyle(scale: 0.96, pressedOpacity: 0.85))
    }
}

// MARK: - Standard Native Modal Sheet Close Button
public struct SheetCloseButton: View {
    public let action: () -> Void
    public var isDarkBackground: Bool

    public init(isDarkBackground: Bool = false, action: @escaping () -> Void) {
        self.isDarkBackground = isDarkBackground
        self.action = action
    }

    public var body: some View {
        Button(action: {
            UIImpactFeedbackGenerator(style: .light).impactOccurred()
            action()
        }) {
            Image(systemName: "xmark")
                .font(.system(size: 13, weight: .semibold))
                .foregroundColor(isDarkBackground ? .white : AppColors.textSecondary)
                .frame(width: 22, height: 22)
        }
        .buttonStyle(.glass)
        .buttonBorderShape(.circle)
        .accessibilityLabel("Close")
    }
}

