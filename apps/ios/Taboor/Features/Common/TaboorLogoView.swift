import SwiftUI

// MARK: - Exact Taboor SVG Vector Shape (1:1 from android/ic_taboor_symbol.xml & dashboard/logo.tsx)

struct TaboorLogoShape: Shape {
    func path(in rect: CGRect) -> Path {
        var path = Path()
        let sx = rect.width / 1024.0
        let sy = rect.height / 1024.0

        func p(_ x: Double, _ y: Double) -> CGPoint {
            CGPoint(x: rect.minX + x * sx, y: rect.minY + y * sy)
        }

        // -------------------------------------------------------------
        // Subpath 1: Outer lower loop
        // -------------------------------------------------------------
        path.move(to: p(808, 537))
        path.addLine(to: p(802, 525))
        path.addLine(to: p(785, 502))
        path.addLine(to: p(766, 487))
        path.addLine(to: p(746, 477))
        path.addLine(to: p(726, 471))
        path.addLine(to: p(715, 471))
        path.addLine(to: p(711, 469))
        path.addLine(to: p(683, 471))
        path.addLine(to: p(680, 473))
        path.addLine(to: p(667, 475))
        path.addLine(to: p(639, 489))
        path.addLine(to: p(615, 511))
        path.addLine(to: p(600, 532))
        path.addLine(to: p(591, 541))
        path.addLine(to: p(584, 552))
        path.addLine(to: p(575, 561))
        path.addLine(to: p(554, 590))
        path.addLine(to: p(546, 598))
        path.addLine(to: p(540, 608))
        path.addLine(to: p(531, 617))
        path.addLine(to: p(525, 627))
        path.addLine(to: p(514, 638))
        path.addLine(to: p(495, 639))
        path.addLine(to: p(487, 643))
        path.addLine(to: p(480, 650))
        path.addLine(to: p(475, 661))
        path.addLine(to: p(477, 674))
        path.addLine(to: p(487, 686))
        path.addLine(to: p(495, 690))
        path.addLine(to: p(720, 690))
        path.addLine(to: p(748, 682))
        path.addLine(to: p(765, 673))
        path.addLine(to: p(781, 660))
        path.addLine(to: p(795, 645))
        path.addLine(to: p(806, 626))
        path.addLine(to: p(816, 591))
        path.addLine(to: p(816, 569))
        path.addLine(to: p(814, 566))
        path.addLine(to: p(814, 557))
        path.closeSubpath()

        // -------------------------------------------------------------
        // Subpath 2: Inner loop cutout
        // -------------------------------------------------------------
        path.move(to: p(757, 552))
        path.addLine(to: p(762, 570))
        path.addLine(to: p(761, 598))
        path.addLine(to: p(748, 620))
        path.addLine(to: p(742, 626))
        path.addLine(to: p(727, 635))
        path.addLine(to: p(712, 639))
        path.addLine(to: p(590, 639))
        path.addLine(to: p(584, 637))
        path.addLine(to: p(661, 539))
        path.addLine(to: p(669, 531))
        path.addLine(to: p(680, 525))
        path.addLine(to: p(695, 520))
        path.addLine(to: p(709, 520))
        path.addLine(to: p(724, 524))
        path.addLine(to: p(740, 533))
        path.closeSubpath()

        // -------------------------------------------------------------
        // Subpath 3: Main "T" letter structure (100% exact points from Android/Mobile)
        // -------------------------------------------------------------
        path.move(to: p(540, 323))
        path.addLine(to: p(527, 319))
        path.addLine(to: p(288, 319))
        path.addLine(to: p(285, 317))
        path.addLine(to: p(276, 317))
        path.addLine(to: p(273, 319))
        path.addLine(to: p(257, 319))
        path.addLine(to: p(254, 321))
        path.addLine(to: p(249, 321))
        path.addLine(to: p(231, 329))
        path.addLine(to: p(214, 343))
        path.addLine(to: p(205, 353))
        path.addLine(to: p(195, 373))
        path.addLine(to: p(195, 379))
        path.addLine(to: p(191, 389))
        path.addLine(to: p(191, 448))
        path.addLine(to: p(199, 468))
        path.addLine(to: p(207, 476))
        path.addLine(to: p(215, 480))
        path.addLine(to: p(227, 483))
        path.addLine(to: p(281, 484))
        path.addLine(to: p(281, 512))
        path.addLine(to: p(283, 515))
        path.addLine(to: p(281, 520))
        path.addLine(to: p(281, 621))
        path.addLine(to: p(287, 644))
        path.addLine(to: p(293, 656))
        path.addLine(to: p(311, 676))
        path.addLine(to: p(340, 689))
        path.addLine(to: p(353, 691))
        path.addLine(to: p(374, 690))
        path.addLine(to: p(388, 686))
        path.addLine(to: p(405, 677))
        path.addLine(to: p(423, 659))
        path.addLine(to: p(432, 642))
        path.addLine(to: p(436, 624))
        path.addLine(to: p(436, 484))
        path.addLine(to: p(481, 483))
        path.addLine(to: p(492, 478))
        path.addLine(to: p(500, 468))
        path.addLine(to: p(502, 462))
        path.addLine(to: p(502, 449))
        path.addLine(to: p(496, 437))
        path.addLine(to: p(484, 429))
        path.addLine(to: p(450, 427))
        path.addLine(to: p(447, 429))
        path.addLine(to: p(425, 429))
        path.addLine(to: p(414, 433))
        path.addLine(to: p(405, 439))
        path.addLine(to: p(391, 453))
        path.addLine(to: p(381, 475))
        path.addLine(to: p(381, 620))
        path.addLine(to: p(377, 630))
        path.addLine(to: p(370, 636))
        path.addLine(to: p(364, 639))
        path.addLine(to: p(355, 639))
        path.addLine(to: p(343, 631))
        path.addLine(to: p(336, 616))
        path.addLine(to: p(336, 465))
        path.addLine(to: p(340, 449))
        path.addLine(to: p(349, 429))
        path.addLine(to: p(345, 427))
        path.addLine(to: p(246, 427))
        path.addLine(to: p(244, 422))
        path.addLine(to: p(244, 401))
        path.addLine(to: p(249, 388))
        path.addLine(to: p(259, 377))
        path.addLine(to: p(271, 372))
        path.addLine(to: p(514, 372))
        path.addLine(to: p(522, 375))
        path.addLine(to: p(532, 383))
        path.addLine(to: p(538, 399))
        path.addLine(to: p(539, 572))
        path.addLine(to: p(583, 517))
        path.addLine(to: p(592, 503))
        path.addLine(to: p(592, 387))
        path.addLine(to: p(590, 384))
        path.addLine(to: p(590, 377))
        path.addLine(to: p(578, 351))
        path.addLine(to: p(562, 335))
        path.closeSubpath()

        return path
    }
}

// MARK: - Logo Icon (1:1 Android TaboorLogoIcon / Flutter _LogoIcon)

struct TaboorLogoIcon: View {
    var size: CGFloat = 80

    var body: some View {
        let boxSize = size * (95.0 / 110.0)
        let cornerRadius = size * (28.0 / 110.0)
        let offset = size * (15.0 / 110.0)

        // Strict LTR coordinate locking to preserve geometry
        ZStack(alignment: .topLeading) {
            // Orange/Amber background squircle at bottom-right
            RoundedRectangle(cornerRadius: cornerRadius, style: .continuous)
                .fill(AppColors.amber)
                .frame(width: boxSize, height: boxSize)
                .offset(x: offset, y: offset)

            // Primary Ink container at top-left with White SVG Glyph
            RoundedRectangle(cornerRadius: cornerRadius, style: .continuous)
                .fill(AppColors.ink)
                .frame(width: boxSize, height: boxSize)
                .overlay {
                    TaboorLogoShape()
                        .fill(style: FillStyle(eoFill: true))
                        .foregroundColor(.white)
                        .padding(boxSize * 0.08)
                }
        }
        .frame(width: size, height: size)
        .environment(\.layoutDirection, .leftToRight)
    }
}

// MARK: - Logo View with Wordmark ("taboor.")

struct TaboorLogoView: View {
    var iconSize: CGFloat = 80
    var showWordmark: Bool = true
    var wordmarkColor: Color? = nil

    @Environment(\.colorScheme) private var colorScheme

    private var effectiveWordmarkColor: Color {
        if let wordmarkColor {
            return wordmarkColor
        }
        return colorScheme == .dark ? .white : AppColors.ink
    }

    var body: some View {
        HStack(spacing: iconSize * 0.22) {
            TaboorLogoIcon(size: iconSize)

            if showWordmark {
                HStack(spacing: 0) {
                    Text("taboor")
                        .font(.inter(iconSize * 0.55, .bold))
                        .tracking(-1.5)
                        .foregroundColor(effectiveWordmarkColor)
                    Text(".")
                        .font(.inter(iconSize * 0.55, .bold))
                        .tracking(-1.5)
                        .foregroundColor(AppColors.amber)
                }
            }
        }
        .environment(\.layoutDirection, .leftToRight)
    }
}

#Preview("Logo - Light & Dark") {
    VStack(spacing: 40) {
        TaboorLogoView(iconSize: 80, showWordmark: true)
            .padding()
            .background(AppColors.splashBackground)
            .preferredColorScheme(.light)

        TaboorLogoView(iconSize: 80, showWordmark: true)
            .padding()
            .background(AppColors.splashBackground)
            .preferredColorScheme(.dark)
    }
}
