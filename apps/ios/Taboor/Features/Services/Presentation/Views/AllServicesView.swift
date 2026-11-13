import SwiftUI
import MapKit

// MARK: - All Services Screen (Native iOS 26 Apple HIG Standard)

public struct AllServicesView: View {
    public let initialCategory: ServiceCategory?
    public let onSelectPlace: (ServicePlace) -> Void

    @StateObject private var dataService = QueueDataService.shared
    @ObservedObject private var localization = LocalizationManager.shared
    @ObservedObject private var locationService = UserLocationService.shared
    @Environment(\.dismiss) private var dismiss
    @Environment(\.colorScheme) private var colorScheme

    @State private var selectedCategory: ServiceCategory?
    @State private var selectedFilter: ServiceSortFilter = .all
    @State private var searchQuery = ""
    @State private var isMapMode = false
    @State private var isNationwideMode = false
    @State private var selectedPlaceOnMap: ServicePlace? = nil
    @State private var selectedBranchOnMap: ServiceBranch? = nil
    @State private var isCardDismissed = false
    @State private var isRefreshing = false

    // Dynamic User Location region
    @State private var cameraPosition: MapCameraPosition = .region(
        MKCoordinateRegion(
            center: UserLocationService.shared.effectiveCoordinate,
            span: MKCoordinateSpan(latitudeDelta: 0.12, longitudeDelta: 0.12)
        )
    )

    public struct MapBranchItem: Identifiable {
        public var id: String { "\(place.id)_\(branch.id)" }
        public let place: ServicePlace
        public let branch: ServiceBranch
    }

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
            filter: selectedFilter,
            scopeToCurrentCity: !isNationwideMode
        )
    }

    private var mapBranchItems: [MapBranchItem] {
        let userCoord = locationService.effectiveCoordinate
        return filteredPlaces.flatMap { place in
            let local = place.localBranches(around: userCoord)
            let chosen = (isNationwideMode || local.isEmpty) ? place.branches : local
            return chosen.map { MapBranchItem(place: place, branch: $0) }
        }
    }

    private var currentSelectedPlace: ServicePlace? {
        if isCardDismissed {
            return selectedPlaceOnMap
        }
        return selectedPlaceOnMap ?? filteredPlaces.first
    }

    private var currentSelectedBranch: ServiceBranch? {
        if let branch = selectedBranchOnMap {
            return branch
        }
        return currentSelectedPlace?.primaryBranch
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
                    dataService.isTabBarHidden = false
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
                        if isMapMode {
                            let userCoord = UserLocationService.shared.effectiveCoordinate
                            cameraPosition = .region(
                                MKCoordinateRegion(
                                    center: userCoord,
                                    span: MKCoordinateSpan(latitudeDelta: 0.08, longitudeDelta: 0.08)
                                )
                            )
                        }
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
        .environment(\.layoutDirection, localization.layoutDirection)
        .onAppear {
            let userCoord = locationService.effectiveCoordinate
            cameraPosition = .region(
                MKCoordinateRegion(
                    center: userCoord,
                    span: MKCoordinateSpan(latitudeDelta: 0.12, longitudeDelta: 0.12)
                )
            )
        }
        .onChange(of: locationService.effectiveCoordinate.latitude) { _, _ in
            let userCoord = locationService.effectiveCoordinate
            withAnimation(.spring(response: 0.45, dampingFraction: 0.8)) {
                cameraPosition = .region(
                    MKCoordinateRegion(
                        center: userCoord,
                        span: MKCoordinateSpan(latitudeDelta: 0.12, longitudeDelta: 0.12)
                    )
                )
            }
        }
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
                if isRefreshing {
                    LazyVStack(spacing: 12) {
                        ForEach(0..<4, id: \.self) { _ in
                            ServiceCardSkeleton()
                        }
                    }
                } else if filteredPlaces.isEmpty {
                    VStack(spacing: 16) {
                        ZStack {
                            Circle()
                                .fill(AppColors.softTeal)
                                .frame(width: 76, height: 76)

                            Circle()
                                .strokeBorder(AppColors.teal.opacity(0.2), lineWidth: 1.5)
                                .frame(width: 92, height: 92)

                            Image(systemName: "magnifyingglass")
                                .font(.system(size: 30, weight: .semibold))
                                .foregroundColor(AppColors.teal)
                        }
                        .padding(.top, 10)

                        VStack(spacing: 6) {
                            Text(localization.isArabic ? "لم نجد أي خدمات مطابقة" : "No Matching Services")
                                .font(.app(17.5, .bold, isArabic: localization.isArabic))
                                .foregroundColor(AppColors.textPrimary)

                            Text(localization.isArabic
                                 ? "تأكد من كتابة الاسم بشكل صحيح، أو جرّب إزالة التصفية لاكتشاف جميع الأماكن المتاحة."
                                 : "Double-check your spelling, or reset filters to browse all available places.")
                                .font(.app(13, .regular, isArabic: localization.isArabic))
                                .foregroundColor(AppColors.textSecondary)
                                .multilineTextAlignment(.center)
                                .lineSpacing(3)
                                .padding(.horizontal, 24)
                        }

                        if !searchQuery.isEmpty || selectedCategory != nil || selectedFilter != .all {
                            Button(action: {
                                UIImpactFeedbackGenerator(style: .light).impactOccurred()
                                withAnimation(.spring(response: 0.35, dampingFraction: 0.8)) {
                                    searchQuery = ""
                                    selectedCategory = nil
                                    selectedFilter = .all
                                }
                            }) {
                                HStack(spacing: 6) {
                                    Image(systemName: "arrow.counterclockwise")
                                        .font(.system(size: 11, weight: .bold))
                                    Text(localization.isArabic ? "إعادة ضبط التصفية والبحث" : "Reset Filters & Search")
                                        .font(.app(12.5, .bold, isArabic: localization.isArabic))
                                }
                                .foregroundColor(AppColors.teal)
                                .padding(.horizontal, 16)
                                .padding(.vertical, 8)
                                .background(AppColors.softTeal)
                                .clipShape(Capsule())
                            }
                            .padding(.top, 6)
                        }
                    }
                    .padding(.vertical, 36)
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
            withAnimation(.easeInOut(duration: 0.2)) { isRefreshing = true }
            try? await Task.sleep(nanoseconds: 700_000_000)
            withAnimation(.easeInOut(duration: 0.25)) { isRefreshing = false }
        }
    }

    // MARK: - 2. Native Apple Map Content View
    private var mapContentView: some View {
        ZStack(alignment: .top) {
            // Interactive Apple Map with Category Markers for All Nationwide Branches
            Map(position: $cameraPosition) {
                ForEach(mapBranchItems) { item in
                    let isSelected = (currentSelectedPlace?.id == item.place.id && currentSelectedBranch?.id == item.branch.id)
                    Annotation(
                        item.place.localizedName(isArabic: localization.isArabic),
                        coordinate: CLLocationCoordinate2D(latitude: item.branch.lat, longitude: item.branch.lng)
                    ) {
                        Button(action: {
                            UIImpactFeedbackGenerator(style: .medium).impactOccurred()
                            withAnimation(.spring(response: 0.35, dampingFraction: 0.8)) {
                                isCardDismissed = false
                                selectedPlaceOnMap = item.place
                                selectedBranchOnMap = item.branch
                                cameraPosition = .region(
                                    MKCoordinateRegion(
                                        center: CLLocationCoordinate2D(latitude: item.branch.lat, longitude: item.branch.lng),
                                        span: MKCoordinateSpan(latitudeDelta: 0.035, longitudeDelta: 0.035)
                                    )
                                )
                            }
                        }) {
                            VStack(spacing: 2) {
                                ZStack {
                                    Circle()
                                        .fill(isSelected ? AppColors.teal : item.place.color)
                                        .frame(
                                            width: isSelected ? 42 : 32,
                                            height: isSelected ? 42 : 32
                                        )
                                        .shadow(color: .black.opacity(0.28), radius: 4, x: 0, y: 2)
                                        .overlay(
                                            Circle()
                                                .strokeBorder(Color.white, lineWidth: isSelected ? 2.5 : 1.2)
                                        )

                                    Image(systemName: item.place.iconName)
                                        .font(.system(size: isSelected ? 17 : 13, weight: .bold))
                                        .foregroundColor(.white)
                                }

                                Image(systemName: "triangle.fill")
                                    .font(.system(size: 7))
                                    .foregroundColor(isSelected ? AppColors.teal : item.place.color)
                                    .rotationEffect(.degrees(180))
                                    .offset(y: -3)
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

                    ZStack(alignment: .leading) {
                        if searchQuery.isEmpty {
                            Text(localization.isArabic ? "ابحث على الخريطة..." : "Search on map...")
                                .font(.app(14, .regular, isArabic: localization.isArabic))
                                .foregroundColor(Color(UIColor.placeholderText))
                                .frame(maxWidth: .infinity, alignment: .leading)
                        }

                        TextField("", text: $searchQuery)
                            .font(.app(14, .regular, isArabic: localization.isArabic))
                            .multilineTextAlignment(.leading)
                            .autocorrectionDisabled()
                    }

                    if !searchQuery.isEmpty {
                        Button(action: {
                            UIImpactFeedbackGenerator(style: .light).impactOccurred()
                            searchQuery = ""
                        }) {
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
        let branch = currentSelectedBranch ?? place.primaryBranch

        return VStack(spacing: 12) {
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

                    if let branch = branch {
                        HStack(spacing: 4) {
                            Text(branch.localizedLabel(isArabic: localization.isArabic))
                                .font(.app(12, .medium, isArabic: localization.isArabic))
                                .foregroundColor(AppColors.textSecondary)
                                .lineLimit(1)

                            Text("•")
                                .font(.system(size: 10))
                                .foregroundColor(AppColors.textTertiary)

                            Text(branch.realDistanceFormatted(isArabic: localization.isArabic))
                                .font(.app(11.5, .bold, isArabic: localization.isArabic))
                                .foregroundColor(AppColors.teal)

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
                        selectedBranchOnMap = nil
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
                let waitingCount = branch?.waiting ?? place.totalWaiting
                HStack(spacing: 5) {
                    Circle()
                        .fill(waitingCount <= 2 ? AppColors.accentGreen : AppColors.amber)
                        .frame(width: 7, height: 7)

                    Text(localization.isArabic
                         ? "\(waitingCount) في الانتظار"
                         : "\(waitingCount) waiting")
                        .font(.app(12, .bold, isArabic: localization.isArabic))
                        .foregroundColor(waitingCount <= 2 ? AppColors.accentGreen : AppColors.amber)

                    if let branch = branch {
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
                    (waitingCount <= 2 ? AppColors.accentGreen : AppColors.amber)
                        .opacity(0.12)
                )
                .clipShape(Capsule())

                Spacer()

                // Book / View Details CTA
                Button(action: {
                    UIImpactFeedbackGenerator(style: .medium).impactOccurred()
                    var targetPlace = place
                    if let branch = branch,
                       let idx = targetPlace.branches.firstIndex(where: { $0.id == branch.id }) {
                        let chosen = targetPlace.branches.remove(at: idx)
                        targetPlace.branches.insert(chosen, at: 0)
                    }
                    onSelectPlace(targetPlace)
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

            ZStack(alignment: localization.isArabic ? .leading : .leading) {
                if searchQuery.isEmpty {
                    Text(localization.isArabic ? "ابحث عن عيادة، دكتور، صالون..." : "Search clinic, doctor, salon...")
                        .font(.app(14, .regular, isArabic: localization.isArabic))
                        .foregroundColor(Color(UIColor.placeholderText))
                        .frame(maxWidth: .infinity, alignment: localization.isArabic ? .leading : .leading)
                }

                TextField("", text: $searchQuery)
                    .font(.app(14, .regular, isArabic: localization.isArabic))
                    .multilineTextAlignment(localization.isArabic ? .leading : .leading)
                    .autocorrectionDisabled()
            }

            if !searchQuery.isEmpty {
                Button(action: {
                    UIImpactFeedbackGenerator(style: .light).impactOccurred()
                    searchQuery = ""
                }) {
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
                .strokeBorder(AppColors.cardBorder, lineWidth: 1)
        )
        .shadow(color: Color.black.opacity(colorScheme == .dark ? 0 : 0.03), radius: 4, x: 0, y: 1.5)
    }

    // MARK: - Category Filter Pills (Full Width Edge-to-Edge with RTL Support)
    private var categoriesBar: some View {
        ScrollViewReader { proxy in
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
                            Capsule().strokeBorder(selectedCategory == nil ? Color.clear : AppColors.cardBorder, lineWidth: 1)
                        )
                        .shadow(color: Color.black.opacity(selectedCategory == nil ? 0.15 : 0.03), radius: 3, x: 0, y: 1.5)
                    }
                    .id("category_all")

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
                        .id(cat.id)
                    }
                }
                .padding(.horizontal, 16)
                .padding(.vertical, 2)
            }
            .defaultScrollAnchor(.leading)
            .flipsForRightToLeftLayoutDirection(false)
            .onAppear {
                DispatchQueue.main.asyncAfter(deadline: .now() + 0.05) {
                    if let category = selectedCategory {
                        proxy.scrollTo(category.id, anchor: .center)
                    } else {
                        proxy.scrollTo("category_all", anchor: .leading)
                    }
                }
            }
            .onChange(of: selectedCategory) { _, newCategory in
                if let cat = newCategory {
                    withAnimation(.spring(response: 0.35, dampingFraction: 0.8)) {
                        proxy.scrollTo(cat.id, anchor: .center)
                    }
                } else {
                    withAnimation(.spring(response: 0.35, dampingFraction: 0.8)) {
                        proxy.scrollTo("category_all", anchor: .leading)
                    }
                }
            }
        }
        .padding(.horizontal, -16)
    }

    // MARK: - Results Summary Bar
    private var resultsBar: some View {
        HStack(alignment: .center, spacing: 8) {
            HStack(spacing: 4) {
                Image(systemName: isNationwideMode ? "globe.europe.africa.fill" : "location.fill")
                    .font(.system(size: 11, weight: .bold))
                    .foregroundColor(AppColors.teal)

                Text(localization.isArabic
                     ? "\(filteredPlaces.count) أماكن في \(isNationwideMode ? "جميع المحافظات" : locationService.effectiveAddress)"
                     : "\(filteredPlaces.count) places in \(isNationwideMode ? "Nationwide" : locationService.effectiveAddress)")
                    .font(.app(12.5, .bold, isArabic: localization.isArabic))
                    .foregroundColor(AppColors.textPrimary)
                    .lineLimit(1)
            }

            Spacer()

            // Scope Toggle (Local vs All Egypt)
            Button(action: {
                UIImpactFeedbackGenerator(style: .light).impactOccurred()
                withAnimation(.spring(response: 0.35, dampingFraction: 0.8)) {
                    isNationwideMode.toggle()
                }
            }) {
                HStack(spacing: 3) {
                    Image(systemName: isNationwideMode ? "mappin.and.ellipse" : "globe")
                        .font(.system(size: 10, weight: .bold))
                    Text(localization.isArabic
                         ? (isNationwideMode ? "مدينتي فقط" : "كل المحافظات")
                         : (isNationwideMode ? "My City" : "All Egypt"))
                        .font(.app(10.5, .bold, isArabic: localization.isArabic))
                }
                .foregroundColor(AppColors.indigo)
                .padding(.horizontal, 8)
                .padding(.vertical, 3.5)
                .background(AppColors.indigo.opacity(0.08))
                .clipShape(Capsule())
            }

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

            if selectedCategory != nil || selectedFilter != .all || !searchQuery.isEmpty {
                Button(action: {
                    UIImpactFeedbackGenerator(style: .light).impactOccurred()
                    withAnimation(.spring) {
                        searchQuery = ""
                        selectedCategory = nil
                        selectedFilter = .all
                    }
                }) {
                    Text(localization.isArabic ? "مسح" : "Clear")
                        .font(.app(11.5, .bold, isArabic: localization.isArabic))
                        .foregroundColor(AppColors.teal)
                }
            }
        }
        .padding(.horizontal, 4)
    }
}
