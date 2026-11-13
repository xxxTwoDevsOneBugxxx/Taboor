import SwiftUI
import AVFoundation

// MARK: - Native iOS 26 QR Scanner View for Quick Queue Check-in

public struct QRScannerView: View {
    public let onQueueJoined: () -> Void

    @StateObject private var cameraManager = QRCameraManager()
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
                // Camera Preview or Fallback
                cameraFeedLayer

                // Dark Vignette Overlay
                Color.black.opacity(0.35).ignoresSafeArea()

                VStack(spacing: 20) {
                    // Header Guidance Text
                    headerGuidance

                    Spacer()

                    // Viewfinder Reticle Frame or Permission Request Card
                    if cameraManager.permissionStatus == .denied || cameraManager.permissionStatus == .restricted {
                        permissionDeniedCard
                    } else {
                        viewfinderFrame
                    }

                    Spacer()

                    // Bottom Quick Actions
                    bottomControls
                }
            }
            .navigationTitle("")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .topBarLeading) {
                    if cameraManager.permissionStatus == .authorized && cameraManager.isCameraAvailable {
                        Button(action: {
                            isTorchOn.toggle()
                            cameraManager.toggleTorch(isOn: isTorchOn)
                            UIImpactFeedbackGenerator(style: .medium).impactOccurred()
                        }) {
                            Image(systemName: isTorchOn ? "flashlight.on.fill" : "flashlight.off.fill")
                                .font(.system(size: 14, weight: .semibold))
                                .foregroundColor(isTorchOn ? AppColors.amber : .white)
                        }
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
                cameraManager.onCodeScanned = { code in
                    handleScannedCode(code)
                }
                cameraManager.requestPermissionIfNeeded()
            }
            .onDisappear {
                cameraManager.stopSession()
            }
        }
    }

    // MARK: - Camera Feed Layer
    @ViewBuilder
    private var cameraFeedLayer: some View {
        if cameraManager.permissionStatus == .authorized && cameraManager.isCameraAvailable {
            CameraPreviewView(session: cameraManager.session)
                .ignoresSafeArea()
        } else {
            // Dark Camera Simulation Background
            Color.black.ignoresSafeArea()
        }
    }

    // MARK: - Header Guidance Text
    private var headerGuidance: some View {
        VStack(spacing: 6) {
            Text(localization.isArabic ? "امسح رمز QR الخاص بالمكان" : "Scan Provider QR Code")
                .font(.app(18, .bold, isArabic: localization.isArabic))
                .foregroundColor(.white)

            Text(localization.isArabic
                 ? "وجّه الكاميرا نحو رمز QR عند مدخل العيادة أو المحل للحجز الفوري"
                 : "Point camera at the QR code at the entrance for instant check-in")
                .font(.app(13, .regular, isArabic: localization.isArabic))
                .foregroundColor(Color.white.opacity(0.85))
                .multilineTextAlignment(.center)
                .padding(.horizontal, 32)
        }
        .padding(.top, 14)
    }

    // MARK: - Permission Denied Card
    private var permissionDeniedCard: some View {
        VStack(spacing: 16) {
            ZStack {
                Circle()
                    .fill(AppColors.amber.opacity(0.18))
                    .frame(width: 72, height: 72)

                Image(systemName: "camera.badge.ellipsis")
                    .font(.system(size: 32, weight: .semibold))
                    .foregroundColor(AppColors.amber)
            }

            VStack(spacing: 8) {
                Text(localization.isArabic ? "صلاحية الكاميرا مطلوبة" : "Camera Access Required")
                    .font(.app(17, .bold, isArabic: localization.isArabic))
                    .foregroundColor(.white)

                Text(localization.isArabic
                     ? "يرجى السماح للتطبيق باستخدام الكاميرا من إعدادات الآيفون حتى تتمكن من مسح رموز QR وحجز دورك مباشرةً."
                     : "Please allow camera access in iPhone Settings to scan QR codes and join live queues.")
                    .font(.app(13, .regular, isArabic: localization.isArabic))
                    .foregroundColor(Color.white.opacity(0.8))
                    .multilineTextAlignment(.center)
                    .padding(.horizontal, 24)
            }

            Button(action: {
                UIImpactFeedbackGenerator(style: .medium).impactOccurred()
                if let url = URL(string: UIApplication.openSettingsURLString) {
                    UIApplication.shared.open(url)
                }
            }) {
                HStack(spacing: 8) {
                    Image(systemName: "gearshape.fill")
                        .font(.system(size: 15, weight: .bold))
                    Text(localization.isArabic ? "فتح إعدادات الجهاز" : "Open Settings")
                        .font(.app(14, .bold, isArabic: localization.isArabic))
                }
                .foregroundColor(.white)
                .padding(.horizontal, 24)
                .padding(.vertical, 13)
                .background(
                    LinearGradient(
                        colors: [AppColors.teal, AppColors.deepTeal],
                        startPoint: .topLeading,
                        endPoint: .bottomTrailing
                    )
                )
                .clipShape(Capsule())
                .shadow(color: AppColors.teal.opacity(0.4), radius: 8, y: 3)
            }
            .buttonStyle(ScaleButtonStyle(scale: 0.96))
            .padding(.top, 4)
        }
        .padding(24)
        .background(
            RoundedRectangle(cornerRadius: 24, style: .continuous)
                .fill(Color(UIColor.secondarySystemBackground).opacity(0.25))
                .overlay(
                    RoundedRectangle(cornerRadius: 24, style: .continuous)
                        .strokeBorder(Color.white.opacity(0.15), lineWidth: 1)
                )
        )
        .padding(.horizontal, 28)
    }

    // MARK: - Viewfinder Reticle Frame
    private var viewfinderFrame: some View {
        ZStack {
            // Rounded Viewfinder Box
            RoundedRectangle(cornerRadius: 28, style: .continuous)
                .strokeBorder(Color.white.opacity(0.2), lineWidth: 2)
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
        VStack(spacing: 14) {
            HStack(spacing: 18) {
                // Torch Button
                if cameraManager.permissionStatus == .authorized && cameraManager.isCameraAvailable {
                    Button(action: {
                        isTorchOn.toggle()
                        cameraManager.toggleTorch(isOn: isTorchOn)
                        UIImpactFeedbackGenerator(style: .medium).impactOccurred()
                    }) {
                        Image(systemName: isTorchOn ? "flashlight.on.fill" : "flashlight.off.fill")
                            .font(.system(size: 19, weight: .semibold))
                            .foregroundColor(isTorchOn ? AppColors.amber : .white)
                            .frame(width: 48, height: 48)
                    }
                    .buttonStyle(.glass)
                    .buttonBorderShape(.circle)
                }

                // Primary Center Button: Manual Code
                Button(action: {
                    UIImpactFeedbackGenerator(style: .light).impactOccurred()
                    showManualInput = true
                }) {
                    HStack(spacing: 8) {
                        Image(systemName: "keyboard.fill")
                            .font(.system(size: 15, weight: .semibold))
                            .foregroundColor(AppColors.teal)

                        Text(localization.isArabic ? "إدخال الرمز يدوياً" : "Enter Code Manually")
                            .font(.app(14, .bold, isArabic: localization.isArabic))
                            .foregroundColor(.white)
                    }
                    .padding(.horizontal, 18)
                    .padding(.vertical, 12)
                }
                .buttonStyle(.glass)
                .buttonBorderShape(.capsule)

                // Quick Demo / Test Scan Button
                Button(action: {
                    UIImpactFeedbackGenerator(style: .medium).impactOccurred()
                    triggerSimulatedScan(placeId: "place_1")
                }) {
                    Image(systemName: "sparkles")
                        .font(.system(size: 19, weight: .semibold))
                        .foregroundColor(AppColors.amber)
                        .frame(width: 48, height: 48)
                }
                .buttonStyle(.glass)
                .buttonBorderShape(.circle)
            }
            .padding(.bottom, 26)
        }
    }

    // MARK: - Manual Code Sheet
    private var manualCodeSheet: some View {
        VStack(spacing: 18) {
            // Header Row
            HStack {
                Text(localization.isArabic ? "إدخال كود الطابور" : "Enter Queue Code")
                    .font(.app(18, .bold, isArabic: localization.isArabic))
                    .foregroundColor(AppColors.textPrimary)

                Spacer()

                SheetCloseButton {
                    showManualInput = false
                }
            }
            .padding(.top, 14)

            // Subtitle Description
            Text(localization.isArabic
                 ? "اكتب الرمز المطبوع أسفل QR عند مدخل الفرع أو اختر من الأماكن المقترحة أدناه"
                 : "Type the code printed below the QR code or select one of the suggested places")
                .font(.app(12.5, .regular, isArabic: localization.isArabic))
                .foregroundColor(AppColors.textSecondary)
                .multilineTextAlignment(.leading)
                .frame(maxWidth: .infinity, alignment: .leading)

            // Full-Width Suggestions Filter Grid (2x2)
            LazyVGrid(columns: [GridItem(.flexible(), spacing: 10), GridItem(.flexible(), spacing: 10)], spacing: 10) {
                suggestionChip(titleAr: "دار الفؤاد", titleEn: "Dar Al Fouad", icon: "cross.case.fill", code: "place_1")
                suggestionChip(titleAr: "مغربي للعيون", titleEn: "Magrabi Eye", icon: "eye.fill", code: "place_2")
                suggestionChip(titleAr: "صالون ميرور", titleEn: "Mirror Salon", icon: "scissors", code: "place_3")
                suggestionChip(titleAr: "أوتو فيكس", titleEn: "AutoFix", icon: "wrench.and.screwdriver.fill", code: "place_4")
            }
            .frame(maxWidth: .infinity)

            // Input Field
            HStack(spacing: 12) {
                Image(systemName: "qrcode")
                    .font(.system(size: 18, weight: .semibold))
                    .foregroundColor(AppColors.teal)

                TextField(localization.isArabic ? "مثال: TABOOR-FOUAD-01 أو place_1" : "e.g., TABOOR-FOUAD-01", text: $manualCode)
                    .font(.app(15, .medium, isArabic: localization.isArabic))

                if !manualCode.isEmpty {
                    Button(action: {
                        manualCode = ""
                    }) {
                        Image(systemName: "xmark.circle.fill")
                            .foregroundColor(AppColors.textTertiary)
                    }
                }
            }
            .padding(14)
            .background(AppColors.subtleFill)
            .clipShape(RoundedRectangle(cornerRadius: 14, style: .continuous))
            .overlay(
                RoundedRectangle(cornerRadius: 14, style: .continuous)
                    .strokeBorder(AppColors.cardBorder, lineWidth: 1)
            )

            // Primary Confirm Button
            Button(action: {
                showManualInput = false
                handleScannedCode(manualCode)
            }) {
                HStack(spacing: 8) {
                    Image(systemName: "checkmark.circle.fill")
                        .font(.system(size: 16, weight: .bold))

                    Text(localization.isArabic ? "تأكيد الكود وحجز الدور" : "Confirm Code & Check-in")
                        .font(.app(15, .bold, isArabic: localization.isArabic))
                }
                .foregroundColor(.white)
                .frame(maxWidth: .infinity)
                .padding(.vertical, 14)
                .background(
                    LinearGradient(
                        colors: [AppColors.teal, AppColors.deepTeal],
                        startPoint: .topLeading,
                        endPoint: .bottomTrailing
                    )
                )
                .clipShape(Capsule())
                .shadow(color: AppColors.teal.opacity(0.35), radius: 8, y: 3)
            }
            .disabled(manualCode.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty)
            .opacity(manualCode.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty ? 0.5 : 1.0)
            .buttonStyle(ScaleButtonStyle(scale: 0.98))
        }
        .padding(24)
        .presentationDetents([.fraction(0.56), .large])
        .presentationDragIndicator(.visible)
    }

    private func suggestionChip(titleAr: String, titleEn: String, icon: String, code: String) -> some View {
        let isSelected = manualCode.trimmingCharacters(in: .whitespacesAndNewlines) == code
        return Button(action: {
            UIImpactFeedbackGenerator(style: .light).impactOccurred()
            withAnimation(.spring(response: 0.25, dampingFraction: 0.8)) {
                manualCode = code
            }
        }) {
            HStack(spacing: 7) {
                Image(systemName: icon)
                    .font(.system(size: 12, weight: .bold))
                    .foregroundColor(isSelected ? .white : AppColors.teal)

                Text(localization.isArabic ? titleAr : titleEn)
                    .font(.app(12.5, .bold, isArabic: localization.isArabic))
                    .foregroundColor(isSelected ? .white : AppColors.textPrimary)
                    .lineLimit(1)
            }
            .frame(maxWidth: .infinity)
            .padding(.vertical, 11)
            .padding(.horizontal, 8)
            .background(isSelected ? AppColors.teal : AppColors.subtleFill)
            .clipShape(RoundedRectangle(cornerRadius: 12, style: .continuous))
            .overlay(
                RoundedRectangle(cornerRadius: 12, style: .continuous)
                    .strokeBorder(isSelected ? AppColors.teal : AppColors.cardBorder, lineWidth: 1)
            )
        }
        .buttonStyle(ScaleButtonStyle(scale: 0.96))
    }

    // MARK: - Scan Actions
    private func handleScannedCode(_ code: String) {
        guard !isProcessing else { return }
        isProcessing = true

        UIImpactFeedbackGenerator(style: .medium).impactOccurred()

        let matched = dataService.scanQRCode(code: code)
        guard let (place, branch) = matched ?? (dataService.places.first.flatMap { p in p.primaryBranch.map { (p, $0) } }) else {
            isProcessing = false
            return
        }

        dataService.joinQueue(place: place, branch: branch)
        scannedPlace = place
        UINotificationFeedbackGenerator().notificationOccurred(.success)
        showSuccessAlert = true
        isProcessing = false
    }

    private func triggerSimulatedScan(placeId: String) {
        handleScannedCode(placeId)
    }
}

// MARK: - Native AVFoundation Camera Manager

public final class QRCameraManager: NSObject, ObservableObject, AVCaptureMetadataOutputObjectsDelegate {
    @Published public var permissionStatus: AVAuthorizationStatus = .notDetermined
    @Published public var isCameraAvailable: Bool = true
    @Published public var isRunning: Bool = false

    public var onCodeScanned: ((String) -> Void)?

    public let session = AVCaptureSession()
    private let sessionQueue = DispatchQueue(label: "com.taboor.camera.session.queue")
    private var isConfigured = false
    private var lastScannedCode: String?
    private var lastScanTime: Date = .distantPast

    public override init() {
        super.init()
        checkStatus()
    }

    public func checkStatus() {
        let status = AVCaptureDevice.authorizationStatus(for: .video)
        DispatchQueue.main.async {
            self.permissionStatus = status
        }
        if status == .authorized {
            setupAndStart()
        }
    }

    public func requestPermissionIfNeeded() {
        let status = AVCaptureDevice.authorizationStatus(for: .video)
        DispatchQueue.main.async {
            self.permissionStatus = status
        }

        switch status {
        case .notDetermined:
            AVCaptureDevice.requestAccess(for: .video) { [weak self] granted in
                DispatchQueue.main.async {
                    guard let self = self else { return }
                    self.permissionStatus = granted ? .authorized : .denied
                    if granted {
                        self.setupAndStart()
                    }
                }
            }
        case .authorized:
            setupAndStart()
        default:
            break
        }
    }

    public func setupAndStart() {
        sessionQueue.async { [weak self] in
            guard let self = self else { return }
            guard !self.isConfigured else {
                if !self.session.isRunning {
                    self.session.startRunning()
                    DispatchQueue.main.async { self.isRunning = true }
                }
                return
            }

            self.session.beginConfiguration()
            self.session.sessionPreset = .high

            guard let videoDevice = AVCaptureDevice.default(.builtInWideAngleCamera, for: .video, position: .back),
                  let videoInput = try? AVCaptureDeviceInput(device: videoDevice),
                  self.session.canAddInput(videoInput) else {
                self.session.commitConfiguration()
                DispatchQueue.main.async {
                    self.isCameraAvailable = false
                }
                return
            }

            self.session.addInput(videoInput)

            let metadataOutput = AVCaptureMetadataOutput()
            if self.session.canAddOutput(metadataOutput) {
                self.session.addOutput(metadataOutput)
                metadataOutput.setMetadataObjectsDelegate(self, queue: DispatchQueue.main)
                metadataOutput.metadataObjectTypes = [.qr, .ean8, .ean13, .code128, .pdf417]
            } else {
                self.session.commitConfiguration()
                return
            }

            self.session.commitConfiguration()
            self.isConfigured = true
            self.session.startRunning()

            DispatchQueue.main.async {
                self.isRunning = true
                self.isCameraAvailable = true
            }
        }
    }

    public func stopSession() {
        sessionQueue.async { [weak self] in
            guard let self = self, self.session.isRunning else { return }
            self.session.stopRunning()
            DispatchQueue.main.async {
                self.isRunning = false
            }
        }
    }

    public func toggleTorch(isOn: Bool) {
        guard let device = AVCaptureDevice.default(for: .video), device.hasTorch else { return }
        do {
            try device.lockForConfiguration()
            device.torchMode = isOn ? .on : .off
            device.unlockForConfiguration()
        } catch {
            print("[QRCameraManager] Torch error: \(error)")
        }
    }

    // MARK: - AVCaptureMetadataOutputObjectsDelegate
    public func metadataOutput(
        _ output: AVCaptureMetadataOutput,
        didOutput metadataObjects: [AVMetadataObject],
        from connection: AVCaptureConnection
    ) {
        guard let metadata = metadataObjects.first as? AVMetadataMachineReadableCodeObject,
              let stringVal = metadata.stringValue,
              !stringVal.isEmpty else { return }

        // Debounce scan calls within 2.5 seconds
        let now = Date()
        guard now.timeIntervalSince(lastScanTime) > 2.5 || lastScannedCode != stringVal else { return }
        lastScanTime = now
        lastScannedCode = stringVal

        DispatchQueue.main.async {
            self.onCodeScanned?(stringVal)
        }
    }
}

// MARK: - CameraPreviewView Representable

public struct CameraPreviewView: UIViewRepresentable {
    public let session: AVCaptureSession

    public init(session: AVCaptureSession) {
        self.session = session
    }

    public func makeUIView(context: Context) -> CameraPreviewUIView {
        let view = CameraPreviewUIView()
        view.videoPreviewLayer.session = session
        view.videoPreviewLayer.videoGravity = .resizeAspectFill
        return view
    }

    public func updateUIView(_ uiView: CameraPreviewUIView, context: Context) {
        uiView.videoPreviewLayer.session = session
    }
}

public final class CameraPreviewUIView: UIView {
    public override class var layerClass: AnyClass {
        AVCaptureVideoPreviewLayer.self
    }

    public var videoPreviewLayer: AVCaptureVideoPreviewLayer {
        layer as! AVCaptureVideoPreviewLayer
    }
}
