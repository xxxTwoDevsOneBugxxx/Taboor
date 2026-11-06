import SwiftUI

// MARK: - Native iOS 26 QR Scanner View for Quick Queue Check-in

public struct QRScannerView: View {
    public let onQueueJoined: () -> Void

    @StateObject private var dataService = QueueDataService.shared
    private let localization = LocalizationManager.shared
    @Environment(\.dismiss) private var dismiss
    @Environment(\.colorScheme) private var colorScheme

    @State private var isTorchOn = false
    @State private var scanLineOffset: CGFloat = -110
    @State private var manualCode = ""
    @State private var isProcessing = false
    @State private var showManualInput = false
    @State private var scannedPlace: ServicePlace? = nil
    @State private var showSuccessAlert = false

    public init(onQueueJoined: @escaping () -> Void) {
        self.onQueueJoined = onQueueJoined
    }

    public var body: some View {
        NavigationStack {
            ZStack {
                // Dark Camera Background Simulation
                Color.black.ignoresSafeArea()

                VStack(spacing: 24) {
                    // Header Guidance Text
                    VStack(spacing: 6) {
                        Text(localization.isArabic ? "امسح رمز QR الخاص بالمكان" : "Scan Provider QR Code")
                            .font(.app(18, .bold, isArabic: localization.isArabic))
                            .foregroundColor(.white)

                        Text(localization.isArabic
                             ? "وجّه الكاميرا نحو رمز QR عند مدخل العيادة أو المحل للحجز الفوري"
                             : "Point camera at the QR code at the entrance for instant check-in")
                            .font(.app(13, .regular, isArabic: localization.isArabic))
                            .foregroundColor(Color.white.opacity(0.75))
                            .multilineTextAlignment(.center)
                            .padding(.horizontal, 32)
                    }
                    .padding(.top, 16)

                    Spacer()

                    // Viewfinder Reticle Frame
                    viewfinderFrame

                    Spacer()

                    // Bottom Quick Actions
                    bottomControls
                }
            }
            .navigationTitle(localization.isArabic ? "مسح كود الطابور" : "QR Queue Scanner")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .topBarLeading) {
                    Button(action: {
                        isTorchOn.toggle()
                        UIImpactFeedbackGenerator(style: .light).impactOccurred()
                    }) {
                        Image(systemName: isTorchOn ? "flashlight.on.fill" : "flashlight.off.fill")
                            .font(.system(size: 14, weight: .semibold))
                            .foregroundColor(isTorchOn ? AppColors.amber : .white)
                    }
                }

                ToolbarItem(placement: .topBarTrailing) {
                    Button(action: {
                        UIImpactFeedbackGenerator(style: .light).impactOccurred()
                        dismiss()
                    }) {
                        Image(systemName: "xmark")
                            .font(.system(size: 13, weight: .bold))
                            .foregroundColor(.white)
                    }
                }
            }
            .sheet(isPresented: $showManualInput) {
                manualCodeSheet
            }
            .alert(localization.isArabic ? "تم الانضمام للطابور بنجاح! 🎉" : "Successfully Joined Queue! 🎉", isPresented: $showSuccessAlert) {
                Button(localization.isArabic ? "عرض التذكرة" : "View Ticket") {
                    dismiss()
                    onQueueJoined()
                }
                Button(localization.isArabic ? "إغلاق" : "Close", role: .cancel) {
                    dismiss()
                }
            } message: {
                if let place = scannedPlace {
                    Text(localization.isArabic
                         ? "تم إصدار تذكرتك في \(place.nameAr). تجد تفاصيلها في تبويب طابوري."
                         : "Your ticket at \(place.name) is ready. Check My Queue tab for live status.")
                }
            }
            .onAppear {
                withAnimation(.easeInOut(duration: 1.8).repeatForever(autoreverses: true)) {
                    scanLineOffset = 110
                }
            }
        }
    }

    // MARK: - Viewfinder Reticle Frame
    private var viewfinderFrame: some View {
        ZStack {
            // Darkened Vignette Outside
            RoundedRectangle(cornerRadius: 28, style: .continuous)
                .strokeBorder(Color.white.opacity(0.15), lineWidth: 2)
                .frame(width: 250, height: 250)

            // Animated Laser Sweep Line
            Rectangle()
                .fill(
                    LinearGradient(
                        colors: [AppColors.teal.opacity(0), AppColors.teal, AppColors.teal.opacity(0)],
                        startPoint: .leading,
                        endPoint: .trailing
                    )
                )
                .frame(width: 230, height: 3)
                .offset(y: scanLineOffset)
                .shadow(color: AppColors.teal, radius: 8)

            // Corner Bracket Accents
            reticleCorners
        }
    }

    private var reticleCorners: some View {
        ZStack {
            // Top Left
            cornerPiece
                .rotationEffect(.degrees(0))
                .offset(x: -115, y: -115)

            // Top Right
            cornerPiece
                .rotationEffect(.degrees(90))
                .offset(x: 115, y: -115)

            // Bottom Right
            cornerPiece
                .rotationEffect(.degrees(180))
                .offset(x: 115, y: 115)

            // Bottom Left
            cornerPiece
                .rotationEffect(.degrees(270))
                .offset(x: -115, y: 115)
        }
    }

    private var cornerPiece: some View {
        Path { path in
            path.move(to: CGPoint(x: 0, y: 24))
            path.addLine(to: CGPoint(x: 0, y: 8))
            path.addArc(
                center: CGPoint(x: 8, y: 8),
                radius: 8,
                startAngle: .degrees(180),
                endAngle: .degrees(270),
                clockwise: false
            )
            path.addLine(to: CGPoint(x: 24, y: 0))
        }
        .stroke(AppColors.teal, lineWidth: 4)
        .frame(width: 24, height: 24)
    }

    // MARK: - Bottom Controls
    private var bottomControls: some View {
        VStack(spacing: 16) {
            // Simulated Test Scans for instant demo
            VStack(spacing: 8) {
                Text(localization.isArabic ? "تجربة مسح كود سريع:" : "Simulate Instant Scan:")
                    .font(.app(11, .medium, isArabic: localization.isArabic))
                    .foregroundColor(Color.white.opacity(0.6))

                HStack(spacing: 10) {
                    testScanButton(titleAr: "عيادة د. سارة", titleEn: "Dr. Sara Clinic", placeId: "place_1")
                    testScanButton(titleAr: "صالون ميرور", titleEn: "Mirror Salon", placeId: "place_2")
                }
            }

            HStack(spacing: 14) {
                // Manual Code Button
                Button(action: {
                    showManualInput = true
                }) {
                    HStack(spacing: 6) {
                        Image(systemName: "keyboard")
                        Text(localization.isArabic ? "إدخال كود يدوياً" : "Enter Code")
                    }
                    .font(.app(13, .bold, isArabic: localization.isArabic))
                    .foregroundColor(.white)
                    .padding(.horizontal, 16)
                    .padding(.vertical, 12)
                }
                .buttonStyle(.glass)
                .buttonBorderShape(.capsule)

                // Photos Album Button
                Button(action: {
                    triggerSimulatedScan(placeId: "place_3")
                }) {
                    HStack(spacing: 6) {
                        Image(systemName: "photo.on.rectangle")
                        Text(localization.isArabic ? "من الصور" : "Photos")
                    }
                    .font(.app(13, .bold, isArabic: localization.isArabic))
                    .foregroundColor(.white)
                    .padding(.horizontal, 16)
                    .padding(.vertical, 12)
                }
                .buttonStyle(.glass)
                .buttonBorderShape(.capsule)
            }
            .padding(.bottom, 28)
        }
    }

    private func testScanButton(titleAr: String, titleEn: String, placeId: String) -> some View {
        Button(action: {
            triggerSimulatedScan(placeId: placeId)
        }) {
            Text(localization.isArabic ? titleAr : titleEn)
                .font(.app(11, .bold, isArabic: localization.isArabic))
                .foregroundColor(AppColors.teal)
                .padding(.horizontal, 12)
                .padding(.vertical, 6)
                .background(Color.white.opacity(0.12))
                .clipShape(Capsule())
        }
    }

    // MARK: - Manual Code Sheet
    private var manualCodeSheet: some View {
        VStack(spacing: 16) {
            HStack {
                Spacer()
                SheetCloseButton {
                    showManualInput = false
                }
            }
            .padding(.top, 14)
            .padding(.horizontal, 4)

            ZStack {
                Circle()
                    .fill(AppColors.teal.opacity(0.12))
                    .frame(width: 56, height: 56)
                Image(systemName: "keyboard")
                    .font(.system(size: 24))
                    .foregroundColor(AppColors.teal)
            }

            VStack(spacing: 6) {
                Text(localization.isArabic ? "إدخال كود الطابور يدوياً" : "Enter Queue Code Manually")
                    .font(.app(17, .bold, isArabic: localization.isArabic))
                    .foregroundColor(AppColors.textPrimary)

                Text(localization.isArabic
                     ? "اكتب الرمز المطبوع أسفل QR عند مدخل الفرع"
                     : "Type the code printed below the QR code at the branch entrance")
                    .font(.app(13, .regular, isArabic: localization.isArabic))
                    .foregroundColor(AppColors.textSecondary)
                    .multilineTextAlignment(.center)
            }

            TextField(localization.isArabic ? "مثال: TABOOR-SARA-01" : "e.g., TABOOR-SARA-01", text: $manualCode)
                .font(.app(15, .medium, isArabic: localization.isArabic))
                .padding(14)
                .background(AppColors.subtleFill)
                .clipShape(RoundedRectangle(cornerRadius: 14, style: .continuous))
                .overlay(
                    RoundedRectangle(cornerRadius: 14, style: .continuous)
                        .strokeBorder(AppColors.cardBorder, lineWidth: 1)
                )

            Button(action: {
                showManualInput = false
                triggerSimulatedScan(placeId: "place_1")
            }) {
                Text(localization.isArabic ? "تأكيد الكود وحجز الدور" : "Confirm Code & Check-in")
                    .font(.app(15, .bold, isArabic: localization.isArabic))
                    .foregroundColor(.white)
                    .frame(maxWidth: .infinity)
                    .padding(.vertical, 14)
                    .background(AppColors.teal)
                    .clipShape(Capsule())
            }
            .disabled(manualCode.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty)
            .opacity(manualCode.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty ? 0.6 : 1.0)
        }
        .padding(24)
        .presentationDetents([.fraction(0.48)])
        .presentationDragIndicator(.visible)
    }

    private func triggerSimulatedScan(placeId: String) {
        UIImpactFeedbackGenerator(style: .medium).impactOccurred()
        guard let place = dataService.places.first(where: { $0.id == placeId }) ?? dataService.places.first,
              let branch = place.primaryBranch else { return }

        dataService.joinQueue(place: place, branch: branch)
        scannedPlace = place
        UINotificationFeedbackGenerator().notificationOccurred(.success)
        showSuccessAlert = true
    }
}
