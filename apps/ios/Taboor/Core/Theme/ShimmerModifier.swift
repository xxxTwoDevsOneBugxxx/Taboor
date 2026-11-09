import SwiftUI

// MARK: - Native iOS 18 / 26 Skeleton Shimmer Loading Modifier

public struct ShimmerModifier: ViewModifier {
    @State private var phase: CGFloat = -1.0
    public var duration: Double = 1.3
    public var bounce: Bool = false

    public init(duration: Double = 1.3, bounce: Bool = false) {
        self.duration = duration
        self.bounce = bounce
    }

    public func body(content: Content) -> some View {
        content
            .mask(
                LinearGradient(
                    gradient: Gradient(stops: [
                        .init(color: .black.opacity(0.35), location: phase - 0.35),
                        .init(color: .black, location: phase),
                        .init(color: .black.opacity(0.35), location: phase + 0.35)
                    ]),
                    startPoint: .leading,
                    endPoint: .trailing
                )
            )
            .onAppear {
                withAnimation(
                    .linear(duration: duration)
                    .repeatForever(autoreverses: bounce)
                ) {
                    phase = 1.35
                }
            }
    }
}

public extension View {
    func shimmering(active: Bool = true, duration: Double = 1.3) -> some View {
        Group {
            if active {
                self.modifier(ShimmerModifier(duration: duration))
            } else {
                self
            }
        }
    }
}

// MARK: - Reusable Skeleton Card for Services & Feeds
public struct ServiceCardSkeleton: View {
    public init() {}

    public var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            HStack(spacing: 12) {
                RoundedRectangle(cornerRadius: 15, style: .continuous)
                    .fill(Color(UIColor.systemGray5))
                    .frame(width: 50, height: 50)

                VStack(alignment: .leading, spacing: 7) {
                    RoundedRectangle(cornerRadius: 6, style: .continuous)
                        .fill(Color(UIColor.systemGray5))
                        .frame(width: 170, height: 16)

                    RoundedRectangle(cornerRadius: 5, style: .continuous)
                        .fill(Color(UIColor.systemGray6))
                        .frame(width: 110, height: 12)
                }

                Spacer()
            }

            HStack {
                Capsule()
                    .fill(Color(UIColor.systemGray5))
                    .frame(width: 75, height: 22)

                Capsule()
                    .fill(Color(UIColor.systemGray6))
                    .frame(width: 90, height: 22)

                Spacer()

                Capsule()
                    .fill(Color(UIColor.systemGray5))
                    .frame(width: 70, height: 28)
            }
        }
        .padding(14)
        .background(Color.white)
        .clipShape(RoundedRectangle(cornerRadius: 18, style: .continuous))
        .overlay(
            RoundedRectangle(cornerRadius: 18, style: .continuous)
                .strokeBorder(Color(UIColor.systemGray6), lineWidth: 1)
        )
        .shimmering()
    }
}
