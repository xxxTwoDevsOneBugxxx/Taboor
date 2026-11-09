import SwiftUI

// MARK: - Native Apple-Grade Saved / Favorite Places View

public struct FavoritePlacesView: View {
    @Environment(\.dismiss) private var dismiss
    @StateObject private var dataService = QueueDataService.shared
    @ObservedObject private var localization = LocalizationManager.shared

    @State private var selectedCategory: ServiceCategory? = nil
    @State private var selectedPlaceForBooking: ServicePlace? = nil
    @State private var showClearConfirmation = false

    public let onDismiss: (() -> Void)?

    public init(onDismiss: (() -> Void)? = nil) {
        self.onDismiss = onDismiss
    }

    private var favoritePlaces: [ServicePlace] {
        let allFavorites = dataService.favoritePlaces
        if let category = selectedCategory {
            return allFavorites.filter { $0.category == category }
        }
        return allFavorites
    }

    public var body: some View {
        ScrollView(showsIndicators: false) {
            VStack(spacing: 16) {
                // Subtitle Header
                Text(localization.isArabic ? "وصول فوري وحجز سريع لأماكنك المعتادة" : "Instant queue booking for your go-to places")
                    .font(.app(12, .medium, isArabic: localization.isArabic))
                    .foregroundColor(AppColors.textSecondary)
                    .frame(maxWidth: .infinity, alignment: .leading)
                    .padding(.horizontal, 20)
                    .padding(.top, 4)

                // Category Filters (if there are favorites)
                if !dataService.favoritePlaces.isEmpty {
                    categoryFilterBar
                }

                // Content List or Empty State
                if favoritePlaces.isEmpty {
                    emptyFavoritesState
                } else {
                    LazyVStack(spacing: 14) {
                        ForEach(favoritePlaces) { place in
                            favoritePlaceCard(place: place)
                        }
                    }
                    .padding(.horizontal, 20)
                }
            }
            .padding(.top, 6)
            .padding(.bottom, 36)
        }
        .background(AppColors.screenBackground.ignoresSafeArea())
        .navigationBarBackButtonHidden(true)
        .toolbar(.visible, for: .navigationBar)
        .toolbar {
            ToolbarItem(placement: .topBarLeading) {
                Button(action: {
                    UIImpactFeedbackGenerator(style: .light).impactOccurred()
                    dataService.isTabBarHidden = false
                    dismiss()
                    onDismiss?()
                }) {
                    Image(systemName: localization.isArabic ? "chevron.right" : "chevron.left")
                        .font(.system(size: 16, weight: .bold))
                        .foregroundColor(AppColors.textPrimary)
                }
            }

            ToolbarItem(placement: .principal) {
                Text(localization.isArabic ? "الأماكن والعيادات المفضلة" : "Favorite Places")
                    .font(.app(17, .bold, isArabic: localization.isArabic))
                    .foregroundColor(AppColors.textPrimary)
            }

            ToolbarItem(placement: .topBarTrailing) {
                Menu {
                    Button(action: {
                        UIImpactFeedbackGenerator(style: .medium).impactOccurred()
                        Task { await dataService.refreshAll() }
                    }) {
                        Label(localization.isArabic ? "تحديث الأماكن المفضلة" : "Refresh Favorites", systemImage: "arrow.clockwise")
                    }

                    if !dataService.favoritePlaces.isEmpty {
                        Divider()
                        Button(role: .destructive, action: {
                            UIImpactFeedbackGenerator(style: .heavy).impactOccurred()
                            showClearConfirmation = true
                        }) {
                            Label(localization.isArabic ? "إفراغ قائمة المفضلة" : "Clear All Favorites", systemImage: "trash")
                        }
                    }
                } label: {
                    ZStack(alignment: .topTrailing) {
                        Image(systemName: "heart.fill")
                            .font(.system(size: 16, weight: .semibold))
                            .foregroundColor(.pink)

                        if !dataService.favoritePlaces.isEmpty {
                            Text("\(dataService.favoritePlaces.count)")
                                .font(.system(size: 9, weight: .heavy, design: .rounded))
                                .foregroundColor(.white)
                                .frame(minWidth: 14, minHeight: 14)
                                .background(Color.pink)
                                .clipShape(Circle())
                                .offset(x: 8, y: -7)
                        }
                    }
                }
            }
        }
        .confirmationDialog(
            localization.isArabic ? "هل أنت متأكد من رغبتك في إفراغ المفضلة؟" : "Clear all favorites?",
            isPresented: $showClearConfirmation,
            titleVisibility: .visible
        ) {
            Button(localization.isArabic ? "مسح الكل" : "Clear All", role: .destructive) {
                withAnimation {
                    dataService.favoritePlaceIds.removeAll()
                    UserDefaults.standard.removeObject(forKey: "taboor_favorite_places")
                }
            }
            Button(localization.isArabic ? "إلغاء" : "Cancel", role: .cancel) { }
        }
        .sheet(item: $selectedPlaceForBooking) { place in
            NavigationStack {
                ServiceDetailsView(place: place) {
                    selectedPlaceForBooking = nil
                    dataService.isTabBarHidden = false
                }
            }
        }
        .environment(\.layoutDirection, localization.layoutDirection)
    }

    // MARK: - Category Filter Bar
    private var categoryFilterBar: some View {
        ScrollView(.horizontal, showsIndicators: false) {
            HStack(spacing: 8) {
                // All Chip
                Button(action: {
                    UIImpactFeedbackGenerator(style: .light).impactOccurred()
                    withAnimation(.spring(response: 0.3, dampingFraction: 0.75)) {
                        selectedCategory = nil
                    }
                }) {
                    Text(localization.isArabic ? "الكل" : "All")
                        .font(.app(12.5, selectedCategory == nil ? .bold : .medium, isArabic: localization.isArabic))
                        .foregroundColor(selectedCategory == nil ? .white : AppColors.textSecondary)
                        .padding(.horizontal, 16)
                        .padding(.vertical, 7.5)
                        .background(
                            selectedCategory == nil
                                ? AnyView(AppColors.teal)
                                : AnyView(Color.white)
                        )
                        .clipShape(Capsule())
                        .overlay(
                            Capsule().strokeBorder(
                                selectedCategory == nil ? Color.clear : AppColors.cardBorder,
                                lineWidth: 1
                            )
                        )
                }

                ForEach(ServiceCategory.allCases) { cat in
                    Button(action: {
                        UIImpactFeedbackGenerator(style: .light).impactOccurred()
                        withAnimation(.spring(response: 0.3, dampingFraction: 0.75)) {
                            selectedCategory = cat
                        }
                    }) {
                        HStack(spacing: 5) {
                            Image(systemName: cat.iconName)
                                .font(.system(size: 11, weight: .bold))
                            Text(cat.title(isArabic: localization.isArabic))
                                .font(.app(12.5, selectedCategory == cat ? .bold : .medium, isArabic: localization.isArabic))
                        }
                        .foregroundColor(selectedCategory == cat ? .white : AppColors.textSecondary)
                        .padding(.horizontal, 14)
                        .padding(.vertical, 7.5)
                        .background(
                            selectedCategory == cat
                                ? AnyView(AppColors.teal)
                                : AnyView(Color.white)
                        )
                        .clipShape(Capsule())
                        .overlay(
                            Capsule().strokeBorder(
                                selectedCategory == cat ? Color.clear : AppColors.cardBorder,
                                lineWidth: 1
                            )
                        )
                    }
                }
            }
            .padding(.horizontal, 20)
        }
    }

    // MARK: - Favorite Place Card
    private func favoritePlaceCard(place: ServicePlace) -> some View {
        VStack(spacing: 14) {
            // Header Row: Icon, Place Info, Heart Button
            HStack(alignment: .top, spacing: 12) {
                // Category Squircle Icon
                ZStack {
                    RoundedRectangle(cornerRadius: 15, style: .continuous)
                        .fill(place.color.opacity(0.14))
                        .frame(width: 48, height: 48)

                    Image(systemName: place.iconName)
                        .font(.system(size: 20, weight: .bold))
                        .foregroundColor(place.color)
                }

                VStack(alignment: .leading, spacing: 4) {
                    HStack(spacing: 6) {
                        Text(place.localizedName(isArabic: localization.isArabic))
                            .font(.app(15.5, .bold, isArabic: localization.isArabic))
                            .foregroundColor(AppColors.textPrimary)
                            .lineLimit(1)
                    }

                    if let branch = place.primaryBranch {
                        HStack(spacing: 4) {
                            Image(systemName: "mappin.circle.fill")
                                .font(.system(size: 11, weight: .medium))
                                .foregroundColor(AppColors.textTertiary)
                            Text(localization.isArabic ? branch.labelAr : branch.label)
                                .font(.app(12, .regular, isArabic: localization.isArabic))
                                .foregroundColor(AppColors.textSecondary)
                                .lineLimit(1)
                        }
                    }

                    // Rating & Category
                    HStack(spacing: 8) {
                        HStack(spacing: 3) {
                            Image(systemName: "star.fill")
                                .font(.system(size: 10, weight: .bold))
                                .foregroundColor(AppColors.amber)
                            Text(String(format: "%.1f", place.starRating))
                                .font(.system(size: 11.5, weight: .bold, design: .rounded))
                                .foregroundColor(AppColors.textPrimary)
                        }

                        Text("•")
                            .font(.system(size: 9))
                            .foregroundColor(AppColors.textTertiary)

                        Text(place.category.title(isArabic: localization.isArabic))
                            .font(.app(11, .medium, isArabic: localization.isArabic))
                            .foregroundColor(place.color)
                    }
                }

                Spacer()

                // Heart Toggle Button
                Button(action: {
                    UIImpactFeedbackGenerator(style: .medium).impactOccurred()
                    withAnimation(.spring(response: 0.35, dampingFraction: 0.75)) {
                        dataService.toggleFavorite(placeId: place.id)
                    }
                }) {
                    ZStack {
                        Circle()
                            .fill(Color.pink.opacity(0.12))
                            .frame(width: 36, height: 36)

                        Image(systemName: "heart.fill")
                            .font(.system(size: 16, weight: .bold))
                            .foregroundColor(.pink)
                    }
                }
                .buttonStyle(ScaleButtonStyle(scale: 0.9, pressedOpacity: 0.85))
            }

            Divider()
                .overlay(AppColors.cardBorder.opacity(0.6))

            // Footer Row: Queue status & Action Button
            HStack(alignment: .center) {
                if let branch = place.primaryBranch {
                    HStack(spacing: 5) {
                        Circle()
                            .fill(branch.isOpen ? AppColors.accentGreen : AppColors.accentRed)
                            .frame(width: 7, height: 7)

                        Text(branch.isOpen
                             ? (localization.isArabic ? "مفتوح الآن • \(branch.waiting) في الانتظار" : "Open now • \(branch.waiting) in line")
                             : (localization.isArabic ? "مغلق حالياً" : "Closed currently"))
                            .font(.app(11.5, .medium, isArabic: localization.isArabic))
                            .foregroundColor(AppColors.textSecondary)
                    }
                }

                Spacer()

                // Book / Details Button
                Button(action: {
                    UIImpactFeedbackGenerator(style: .medium).impactOccurred()
                    selectedPlaceForBooking = place
                }) {
                    HStack(spacing: 5) {
                        Image(systemName: "ticket.fill")
                            .font(.system(size: 11, weight: .bold))
                        Text(localization.isArabic ? "حجز دور الآن" : "Join Queue")
                            .font(.app(12, .bold, isArabic: localization.isArabic))
                    }
                    .foregroundColor(.white)
                    .padding(.horizontal, 14)
                    .padding(.vertical, 7)
                    .background(
                        LinearGradient(
                            colors: [AppColors.teal, AppColors.deepTeal],
                            startPoint: .leading,
                            endPoint: .trailing
                        )
                    )
                    .clipShape(Capsule())
                    .shadow(color: AppColors.teal.opacity(0.3), radius: 5, y: 2)
                }
                .buttonStyle(ScaleButtonStyle(scale: 0.94, pressedOpacity: 0.85))
            }
        }
        .padding(16)
        .background(Color.white)
        .clipShape(RoundedRectangle(cornerRadius: 20, style: .continuous))
        .overlay(
            RoundedRectangle(cornerRadius: 20, style: .continuous)
                .strokeBorder(AppColors.cardBorder, lineWidth: 1)
        )
        .shadow(color: Color.black.opacity(0.04), radius: 8, y: 3)
    }

    // MARK: - Empty Favorites State
    private var emptyFavoritesState: some View {
        VStack(spacing: 16) {
            ZStack {
                Circle()
                    .fill(Color.pink.opacity(0.12))
                    .frame(width: 80, height: 80)

                Image(systemName: "heart.slash.fill")
                    .font(.system(size: 34, weight: .medium))
                    .foregroundColor(.pink)
            }
            .padding(.top, 40)

            VStack(spacing: 6) {
                Text(localization.isArabic ? "ما عندكش أماكن مفضلة بعد ✨" : "No favorite places yet")
                    .font(.app(17, .bold, isArabic: localization.isArabic))
                    .foregroundColor(AppColors.textPrimary)

                Text(localization.isArabic
                     ? "اضغط على علامة القلب في أي عيادة أو صالون أو ورشة لحفظها والوصول إليها وحجز دورك بضغطة واحدة."
                     : "Tap the heart icon on any place or clinic to quickly access and join queues in one tap.")
                    .font(.app(13, .regular, isArabic: localization.isArabic))
                    .foregroundColor(AppColors.textSecondary)
                    .multilineTextAlignment(.center)
                    .padding(.horizontal, 32)
            }

            Button(action: {
                UIImpactFeedbackGenerator(style: .medium).impactOccurred()
                dataService.isTabBarHidden = false
                dismiss()
            }) {
                HStack(spacing: 6) {
                    Image(systemName: "magnifyingglass")
                        .font(.system(size: 13, weight: .bold))
                    Text(localization.isArabic ? "استكشاف العيادات والخدمات" : "Explore Clinics & Services")
                        .font(.app(13.5, .bold, isArabic: localization.isArabic))
                }
                .foregroundColor(.white)
                .padding(.horizontal, 22)
                .padding(.vertical, 12)
                .background(
                    LinearGradient(
                        colors: [AppColors.ink, AppColors.deepTeal],
                        startPoint: .leading,
                        endPoint: .trailing
                    )
                )
                .clipShape(Capsule())
                .shadow(color: AppColors.ink.opacity(0.2), radius: 8, y: 3)
            }
            .buttonStyle(ScaleButtonStyle(scale: 0.96, pressedOpacity: 0.85))
            .padding(.top, 8)
        }
        .frame(maxWidth: .infinity)
        .padding(.vertical, 20)
    }
}
