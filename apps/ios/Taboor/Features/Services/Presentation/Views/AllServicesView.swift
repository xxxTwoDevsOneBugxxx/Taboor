import SwiftUI
import MapKit

// MARK: - All Services Screen (Native iOS 26 Apple HIG Standard)

public struct AllServicesView: View {
    public let initialCategory: ServiceCategory?
    public let onSelectPlace: (ServicePlace) -> Void

    @StateObject private var dataService = QueueDataService.shared
    @ObservedObject private var localization = LocalizationManager.shared
    @Environment(\.dismiss) private var dismiss
    @Environment(\.colorScheme) private var colorScheme

    @State private var selectedCategory: ServiceCategory?
    @State private var selectedFilter: ServiceSortFilter = .all
    @State private var searchQuery = ""
    @State private var isMapMode = false
    @State private var selectedPlaceOnMap: ServicePlace? = nil
    @State private var isCardDismissed = false

    // Cairo default region
    @State private var cameraPosition: MapCameraPosition = .region(
        MKCoordinateRegion(
            center: CLLocationCoordinate2D(latitude: 30.0444, longitude: 31.2357),
            span: MKCoordinateSpan(latitudeDelta: 0.12, longitudeDelta: 0.12)
        )
    )

    public init(
        initialCategory: ServiceCategory? = nil,
        onSelectPlace: @escaping (ServicePlace) -> Void
    ) {
        self.initialCategory = initialCategory
        self.onSelectPlace = onSelectPlace
        self._selectedCategory = State(initialValue: initialCategory)
    }

    private var filteredPlaces: [ServicePlace] {
        dataService.filterPlaces(
            query: searchQuery,
            category: selectedCategory,
            filter: selectedFilter
        )
    }

    private var currentSelectedPlace: ServicePlace? {
        if isCardDismissed {
            return selectedPlaceOnMap
        }
        return selectedPlaceOnMap ?? filteredPlaces.first
    }

    public var body: some View {
        Group {
            if isMapMode {
                mapContentView
            } else {
                listContentView
            }
        }
        .navigationBarBackButtonHidden(true)
        .toolbar {
            // Leading: Native Apple Back Button
            ToolbarItem(placement: .topBarLeading) {
                Button(action: {
                    UIImpactFeedbackGenerator(style: .light).impactOccurred()
                    dismiss()
                }) {
                    Image(systemName: localization.isArabic ? "chevron.right" : "chevron.left")
                        .font(.system(size: 16, weight: .bold))
                        .foregroundColor(AppColors.textPrimary)
                }
            }

            // Center Principal Title (Dead-Centered)
            ToolbarItem(placement: .principal) {
                Text(localization.isArabic ? "جميع الخدمات" : "All Services")
                    .font(.app(17, .bold, isArabic: localization.isArabic))
                    .foregroundColor(AppColors.textPrimary)
            }

            // Trailing: Map/List Toggle + Sort Filter Menu (Clean Native Header)
            ToolbarItemGroup(placement: .topBarTrailing) {
                // Map / List Toggle
                Button(action: {
                    UIImpactFeedbackGenerator(style: .medium).impactOccurred()
                    withAnimation(.spring(response: 0.35, dampingFraction: 0.8)) {
                        isMapMode.toggle()
                    }
                }) {
                    Image(systemName: isMapMode ? "list.bullet" : "map")
                        .font(.system(size: 16, weight: .bold))
                        .foregroundColor(isMapMode ? AppColors.teal : AppColors.textPrimary)
                }

                // Sort Filter Menu
                Menu {
                    Section(localization.isArabic ? "ترتيب حسب" : "Sort by") {
                        ForEach(ServiceSortFilter.allCases) { filter in
                            Button(action: {
                                UIImpactFeedbackGenerator(style: .light).impactOccurred()
                                withAnimation(.spring(response: 0.35, dampingFraction: 0.8)) {
                                    selectedFilter = filter
                                }
                            }) {
                                Label(
                                    filter.title(isArabic: localization.isArabic),
                                    systemImage: selectedFilter == filter ? "checkmark" : filter.iconName
                                )
                            }
                        }
                    }

                    if selectedFilter != .all || selectedCategory != nil || !searchQuery.isEmpty {
                        Divider()
                        Button(role: .destructive, action: {
                            UIImpactFeedbackGenerator(style: .light).impactOccurred()
                            withAnimation(.spring) {
                                selectedFilter = .all
                                selectedCategory = nil
                                searchQuery = ""
                            }
                        }) {
                            Label(localization.isArabic ? "إعادة ضبط الفلاتر" : "Reset Filters", systemImage: "arrow.counterclockwise")
                        }
                    }
                } label: {
                    Image(systemName: selectedFilter == .all ? "line.3.horizontal.decrease" : "line.3.horizontal.decrease.circle.fill")
                        .font(.system(size: 16, weight: .bold))
                        .foregroundColor(selectedFilter == .all ? AppColors.textPrimary : AppColors.teal)
                }
            }
        }
        .background(AppColors.screenBackground.ignoresSafeArea())
    }

    // MARK: - 1. Native List Content View
    private var listContentView: some View {
        ScrollView(showsIndicators: false) {
            VStack(spacing: 14) {
                // Search Bar
                searchBar

                // Category Filter Pills
                categoriesBar

                // Results Summary & Active Sort Badge
                resultsBar

                // Places List
                if filteredPlaces.isEmpty {
                    ContentUnavailableView.search(text: searchQuery.isEmpty ? (selectedCategory?.title(isArabic: localization.isArabic) ?? "") : searchQuery)
                        .padding(.vertical, 40)
                } else {
                    LazyVStack(spacing: 12) {
                        ForEach(filteredPlaces) { place in
                            ServiceCardView(place: place) {
                                UIImpactFeedbackGenerator(style: .light).impactOccurred()
                                onSelectPlace(place)
                            }
                            .contextMenu {
                                Button {
                                    onSelectPlace(place)
                                } label: {
                                    Label(localization.isArabic ? "عرض التفاصيل والحجز" : "View Details & Book", systemImage: "ticket")
                                }

                                ShareLink(item: "\(place.localizedName(isArabic: localization.isArabic)) - https://taboor.app/place/\(place.id)") {
                                    Label(localization.isArabic ? "مشاركة المكان" : "Share Place", systemImage: "square.and.arrow.up")
                                }
                            }
                        }
                    }
                }

                Spacer().frame(height: 32)
            }
            .padding(.horizontal, 16)
            .padding(.top, 12)
        }
        .refreshable {
            try? await Task.sleep(nanoseconds: 500_000_000)
        }
    }

    // MARK: - 2. Native Apple Map Content View
    private var mapContentView: some View {
        ZStack(alignment: .top) {
            // Interactive Apple Map with Category Markers
            Map(position: $cameraPosition) {
                ForEach(filteredPlaces) { place in
                    if let branch = place.primaryBranch {
                        Annotation(
                            place.localizedName(isArabic: localization.isArabic),
                            coordinate: CLLocationCoordinate2D(latitude: branch.lat, longitude: branch.lng)
                        ) {
                            Button(action: {
                                UIImpactFeedbackGenerator(style: .medium).impactOccurred()
                                withAnimation(.spring(response: 0.35, dampingFraction: 0.8)) {
                                    isCardDismissed = false
                                    selectedPlaceOnMap = place
                                    cameraPosition = .region(
                                        MKCoordinateRegion(
                                            center: CLLocationCoordinate2D(latitude: branch.lat, longitude: branch.lng),
                                            span: MKCoordinateSpan(latitudeDelta: 0.04, longitudeDelta: 0.04)
                                        )
                                    )
                                }
                            }) {
                                VStack(spacing: 2) {
                                    ZStack {
                                        Circle()
                                            .fill(currentSelectedPlace?.id == place.id ? AppColors.teal : place.color)
                                            .frame(
                                                width: currentSelectedPlace?.id == place.id ? 42 : 34,
                                                height: currentSelectedPlace?.id == place.id ? 42 : 34
                                            )
                                            .shadow(color: .black.opacity(0.25), radius: 4, x: 0, y: 2)

                                        Image(systemName: place.iconName)
                                            .font(.system(size: currentSelectedPlace?.id == place.id ? 17 : 14, weight: .bold))
                                            .foregroundColor(.white)
                                    }

                                    Image(systemName: "triangle.fill")
                                        .font(.system(size: 7))
                                        .foregroundColor(currentSelectedPlace?.id == place.id ? AppColors.teal : place.color)
                                        .rotationEffect(.degrees(180))
                                        .offset(y: -3)
                                }
                            }
                        }
                    }
                }
            }
            .mapStyle(.standard(elevation: .realistic))
            .ignoresSafeArea(edges: .bottom)

            // Floating Top Controls (Search & Category Filters) + Bottom Place Card
            VStack(spacing: 8) {
                // Floating Inset Search Field
                HStack(spacing: 8) {
                    Image(systemName: "magnifyingglass")
                        .foregroundColor(Color(UIColor.secondaryLabel))
                    TextField(localization.isArabic ? "ابحث على الخريطة..." : "Search on map...", text: $searchQuery)
                        .font(.app(14, .regular, isArabic: localization.isArabic))
                    if !searchQuery.isEmpty {
                        Button(action: { searchQuery = "" }) {
                            Image(systemName: "xmark.circle.fill")
                                .foregroundColor(Color(UIColor.tertiaryLabel))
                        }
                    }
                }
                .padding(.horizontal, 12)
                .padding(.vertical, 10)
                .background(.ultraThinMaterial)
                .clipShape(RoundedRectangle(cornerRadius: 12, style: .continuous))
                .overlay(
                    RoundedRectangle(cornerRadius: 12, style: .continuous)
                        .strokeBorder(Color.white.opacity(0.3), lineWidth: 0.8)
                )
                .shadow(color: .black.opacity(0.08), radius: 6, x: 0, y: 2)
                .padding(.horizontal, 16)
                .padding(.top, 8)

                // Category Chips Floating over Map
                categoriesBar

                Spacer()

                // Bottom Place Card (only when selected or active)
                if let place = currentSelectedPlace {
                    mapPlaceCard(place)
                        .padding(.horizontal, 16)
                        .padding(.bottom, 40)
                        .transition(.move(edge: .bottom).combined(with: .opacity))
                }
            }
        }
    }

    // MARK: - Native Apple Map Place Card (Compact, Non-Overlapping & Dismissible)
    private func mapPlaceCard(_ place: ServicePlace) -> some View {
        VStack(spacing: 12) {
            // Header Row: Place Info + Rating + Close Button
            HStack(alignment: .center, spacing: 12) {
                // Category Icon
                ZStack {
                    RoundedRectangle(cornerRadius: 14, style: .continuous)
                        .fill(place.color.opacity(colorScheme == .dark ? 0.25 : 0.12))
                        .frame(width: 48, height: 48)

                    Image(systemName: place.iconName)
                        .font(.system(size: 22, weight: .semibold))
                        .foregroundColor(place.color)
                }

                // Title + Branch
                VStack(alignment: .leading, spacing: 3) {
                    HStack(spacing: 4) {
                        Text(place.localizedName(isArabic: localization.isArabic))
                            .font(.app(14.5, .bold, isArabic: localization.isArabic))
                            .foregroundColor(AppColors.textPrimary)
                            .lineLimit(2)
                            .fixedSize(horizontal: false, vertical: true)
                    }

                    if let branch = place.primaryBranch {
                        HStack(spacing: 4) {
                            Text(branch.localizedLabel(isArabic: localization.isArabic))
                                .font(.app(12, .medium, isArabic: localization.isArabic))
                                .foregroundColor(AppColors.textSecondary)

                            Text("•")
                                .font(.system(size: 10))
                                .foregroundColor(AppColors.textTertiary)

                            HStack(spacing: 2) {
                                Image(systemName: "star.fill")
                                    .font(.system(size: 10))
                                    .foregroundColor(AppColors.amber)
                                Text(String(format: "%.1f", place.starRating))
                                    .font(.system(size: 11.5, weight: .bold, design: .rounded))
                                    .foregroundColor(AppColors.textPrimary)
                            }
                        }
                    }
                }

                Spacer()

                // Close Button to dismiss card & view full map
                Button(action: {
                    UIImpactFeedbackGenerator(style: .light).impactOccurred()
                    withAnimation(.spring(response: 0.35, dampingFraction: 0.8)) {
                        selectedPlaceOnMap = nil
                        isCardDismissed = true
                    }
                }) {
                    Image(systemName: "xmark")
                        .font(.system(size: 11, weight: .bold))
                        .foregroundColor(AppColors.textSecondary)
                        .padding(7)
                        .background(Circle().fill(Color(UIColor.secondarySystemFill)))
                }
            }

            // Bottom Row: Queue Pill + Action Button
            HStack(spacing: 10) {
                // Live Queue Status Pill
                HStack(spacing: 5) {
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
                .padding(.vertical, 7)
                .background(
                    (place.totalWaiting <= 2 ? AppColors.accentGreen : AppColors.amber)
                        .opacity(0.12)
                )
                .clipShape(Capsule())

                Spacer()

                // Book / View Details CTA
                Button(action: {
                    UIImpactFeedbackGenerator(style: .medium).impactOccurred()
                    onSelectPlace(place)
                }) {
                    HStack(spacing: 5) {
                        Text(localization.isArabic ? "حجز دور" : "Book")
                            .font(.app(13, .bold, isArabic: localization.isArabic))
                        Image(systemName: localization.isArabic ? "chevron.left" : "chevron.right")
                            .font(.system(size: 10, weight: .bold))
                    }
                    .foregroundColor(.white)
                    .padding(.horizontal, 16)
                    .padding(.vertical, 8)
                    .background(
                        LinearGradient(
                            colors: [AppColors.teal, AppColors.deepTeal],
                            startPoint: .leading,
                            endPoint: .trailing
                        )
                    )
                    .clipShape(Capsule())
                    .shadow(color: AppColors.teal.opacity(0.3), radius: 4, x: 0, y: 2)
                }
            }
        }
        .padding(14)
        .background(
            RoundedRectangle(cornerRadius: 18, style: .continuous)
                .fill(AppColors.cardBackground)
        )
        .overlay(
            RoundedRectangle(cornerRadius: 18, style: .continuous)
                .strokeBorder(Color(UIColor.separator).opacity(colorScheme == .dark ? 0.35 : 0.15), lineWidth: 0.8)
        )
        .shadow(color: .black.opacity(colorScheme == .dark ? 0.3 : 0.1), radius: 12, x: 0, y: 4)
        .onTapGesture {
            UIImpactFeedbackGenerator(style: .light).impactOccurred()
            onSelectPlace(place)
        }
    }

    // MARK: - Search Bar (Apple Inset Style)
    private var searchBar: some View {
        HStack(spacing: 10) {
            Image(systemName: "magnifyingglass")
                .font(.system(size: 15, weight: .medium))
                .foregroundColor(Color(UIColor.secondaryLabel))

            TextField(
                localization.isArabic ? "ابحث عن عيادة، دكتور، صالون..." : "Search clinic, doctor, salon...",
                text: $searchQuery
            )
            .font(.app(14, .regular, isArabic: localization.isArabic))
            .autocorrectionDisabled()

            if !searchQuery.isEmpty {
                Button(action: { searchQuery = "" }) {
                    Image(systemName: "xmark.circle.fill")
                        .font(.system(size: 15))
                        .foregroundColor(Color(UIColor.tertiaryLabel))
                }
            }
        }
        .padding(.horizontal, 14)
        .frame(height: 44)
        .background(AppColors.cardBackground)
        .clipShape(RoundedRectangle(cornerRadius: 12, style: .continuous))
        .overlay(
            RoundedRectangle(cornerRadius: 12, style: .continuous)
                .strokeBorder(Color(UIColor.separator).opacity(colorScheme == .dark ? 0.35 : 0.18), lineWidth: 0.8)
        )
        .shadow(color: Color.black.opacity(colorScheme == .dark ? 0 : 0.03), radius: 4, x: 0, y: 1.5)
    }

    // MARK: - Category Filter Pills (Full Width Edge-to-Edge)
    private var categoriesBar: some View {
        ScrollView(.horizontal, showsIndicators: false) {
            HStack(spacing: 8) {
                // "All" Pill
                Button(action: {
                    UIImpactFeedbackGenerator(style: .light).impactOccurred()
                    withAnimation(.spring(response: 0.3, dampingFraction: 0.75)) {
                        selectedCategory = nil
                    }
                }) {
                    HStack(spacing: 5) {
                        Image(systemName: "square.grid.2x2.fill")
                            .font(.system(size: 11, weight: .semibold))
                        Text(localization.isArabic ? "الكل" : "All")
                            .font(.app(12.5, selectedCategory == nil ? .bold : .medium, isArabic: localization.isArabic))
                            .fixedSize(horizontal: true, vertical: false)
                    }
                    .foregroundColor(selectedCategory == nil ? .white : AppColors.textPrimary)
                    .padding(.horizontal, 14)
                    .padding(.vertical, 8)
                    .background(selectedCategory == nil ? AppColors.teal : AppColors.cardBackground)
                    .clipShape(Capsule())
                    .overlay(
                        Capsule().strokeBorder(selectedCategory == nil ? Color.clear : Color(UIColor.separator).opacity(0.18), lineWidth: 0.8)
                    )
                    .shadow(color: Color.black.opacity(selectedCategory == nil ? 0.15 : 0.03), radius: 3, x: 0, y: 1.5)
                }

                ForEach(ServiceCategory.allCases) { cat in
                    CategoryFilterPill(
                        category: cat,
                        isSelected: selectedCategory == cat,
                        onSelect: {
                            UIImpactFeedbackGenerator(style: .light).impactOccurred()
                            withAnimation(.spring(response: 0.3, dampingFraction: 0.75)) {
                                selectedCategory = (selectedCategory == cat ? nil : cat)
                            }
                        }
                    )
                }
            }
            .padding(.horizontal, 16)
            .padding(.vertical, 2)
        }
        .padding(.horizontal, -16)
    }

    // MARK: - Results Summary Bar
    private var resultsBar: some View {
        HStack(alignment: .center) {
            Text(localization.isArabic
                 ? "\(filteredPlaces.count) أماكن متاحة"
                 : "\(filteredPlaces.count) places available")
                .font(.app(13, .bold, isArabic: localization.isArabic))
                .foregroundColor(AppColors.textPrimary)

            if selectedFilter != .all {
                HStack(spacing: 4) {
                    Image(systemName: selectedFilter.iconName)
                        .font(.system(size: 9, weight: .bold))
                    Text(selectedFilter.title(isArabic: localization.isArabic))
                        .font(.app(11, .bold, isArabic: localization.isArabic))
                }
                .foregroundColor(AppColors.teal)
                .padding(.horizontal, 9)
                .padding(.vertical, 4)
                .background(AppColors.teal.opacity(0.12))
                .clipShape(Capsule())
            }

            Spacer()

            if selectedCategory != nil || selectedFilter != .all || !searchQuery.isEmpty {
                Button(action: {
                    UIImpactFeedbackGenerator(style: .light).impactOccurred()
                    withAnimation(.spring) {
                        searchQuery = ""
                        selectedCategory = nil
                        selectedFilter = .all
                    }
                }) {
                    Text(localization.isArabic ? "مسح الفلاتر" : "Clear filters")
                        .font(.app(11.5, .bold, isArabic: localization.isArabic))
                        .foregroundColor(AppColors.teal)
                }
            }
        }
        .padding(.horizontal, 4)
    }
}
