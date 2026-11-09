package com.taboor.features.home.data.repository

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import com.taboor.core.theme.AppColors
import com.taboor.features.home.domain.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

import com.taboor.features.home.domain.repository.IServiceRepository

object ServiceRepository : IServiceRepository {

    private val _activeTicket = MutableStateFlow<UserTicket?>(UserTicket())
    override val activeTicket: StateFlow<UserTicket?> = _activeTicket.asStateFlow()

    private val _alerts = MutableStateFlow<List<AppAlert>>(generateSeededAlerts())
    override val alerts: StateFlow<List<AppAlert>> = _alerts.asStateFlow()

    private val _history = MutableStateFlow<List<TicketHistoryItem>>(generateSeededHistory())
    override val history: StateFlow<List<TicketHistoryItem>> = _history.asStateFlow()

    private val _favoritePlaceIds = MutableStateFlow<Set<String>>(setOf("place_1", "place_3"))
    override val favoritePlaceIds: StateFlow<Set<String>> = _favoritePlaceIds.asStateFlow()

    private val _smartAlertPreferences = MutableStateFlow(SmartAlertPreferences())
    override val smartAlertPreferences: StateFlow<SmartAlertPreferences> = _smartAlertPreferences.asStateFlow()

    private val _selectedLocation = MutableStateFlow("المعادي، شارع النصر")
    override val selectedLocation: StateFlow<String> = _selectedLocation.asStateFlow()

    override val availableAppointmentSlots: List<AppointmentSlot> = listOf(
        AppointmentSlot(id = "slot_1", time = "10:00 ص", timeEn = "10:00 AM", isRecommended = false),
        AppointmentSlot(id = "slot_2", time = "11:30 ص", timeEn = "11:30 AM", isRecommended = true),
        AppointmentSlot(id = "slot_3", time = "01:00 م", timeEn = "01:00 PM", isBooked = true),
        AppointmentSlot(id = "slot_4", time = "02:30 م", timeEn = "02:30 PM", isRecommended = false),
        AppointmentSlot(id = "slot_5", time = "04:00 م", timeEn = "04:00 PM", isRecommended = false),
        AppointmentSlot(id = "slot_6", time = "05:30 م", timeEn = "05:30 PM", isRecommended = false),
        AppointmentSlot(id = "slot_7", time = "07:00 م", timeEn = "07:00 PM", isRecommended = false)
    )

    private fun generateSeededHistory(): List<TicketHistoryItem> {
        return listOf(
            TicketHistoryItem(
                ticketNumber = "B-04",
                serviceName = "Mirror Beauty & Hair Salon",
                serviceNameAr = "صالون ميرور للتجميل والعناية",
                branchLabel = "Downtown Branch",
                branchLabelAr = "فرع وسط البلد",
                dateString = "أمس، 04:30 م",
                status = TicketStatus.COMPLETED,
                durationMinutes = 28
            ),
            TicketHistoryItem(
                ticketNumber = "C-19",
                serviceName = "Cairo Fix Auto Repair",
                serviceNameAr = "مركز كايرو فيكس لصيانة السيارات",
                branchLabel = "Mohandessin Branch",
                branchLabelAr = "فرع المهندسين",
                dateString = "منذ 3 أيام، 11:15 ص",
                status = TicketStatus.COMPLETED,
                durationMinutes = 45
            ),
            TicketHistoryItem(
                ticketNumber = "A-09",
                serviceName = "Dr. Sara Dental Clinic",
                serviceNameAr = "عيادة د. سارة للأسنان",
                branchLabel = "Nasr City Branch",
                branchLabelAr = "فرع مدينة نصر",
                dateString = "الأسبوع الماضي، 06:00 م",
                status = TicketStatus.COMPLETED,
                durationMinutes = 18
            ),
            TicketHistoryItem(
                ticketNumber = "D-11",
                serviceName = "Smart Business Notary",
                serviceNameAr = "مكتب التوثيق الذكي للمعاملات",
                branchLabel = "New Cairo Branch",
                branchLabelAr = "فرع التجمع الخامس",
                dateString = "منذ أسبوعين",
                status = TicketStatus.CANCELLED,
                durationMinutes = 0
            )
        )
    }

    override val places: List<ServicePlace> = listOf(
        ServicePlace(
            id = "place_1",
            name = "Dr. Sara Dental Clinic",
            nameAr = "عيادة د. سارة لطب وجراحة الأسنان",
            icon = Icons.Rounded.MedicalServices,
            color = AppColors.Teal,
            category = ServiceCategory.CLINICS,
            starRating = 4.8,
            keywords = listOf(
                "اسنان", "أسنان", "طبيب", "دكتور", "سارة", "عيادة", "تقويم", "تبييض", "حشو",
                "dental", "teeth", "clinic", "doctor", "sara", "dentist", "braces"
            ),
            about = "A modern dental clinic offering checkups, braces and whitening with a friendly queue system so you never wait long.",
            aboutAr = "عيادة أسنان حديثة تقدم الفحوصات والتقويم وتبييض الأسنان مع نظام طابور ذكي يتيح لك الانتظار في منزلك.",
            workingHours = "9:00 AM – 9:00 PM",
            branches = listOf(
                ServiceBranch(
                    id = "branch_1_1",
                    label = "branchMain",
                    labelAr = "الفرع الرئيسي - مدينة نصر",
                    distanceKm = "18.6",
                    waiting = 4,
                    isOpen = true,
                    address = "Nasr City, Makram Ebeid St.",
                    addressAr = "مدينة نصر، شارع مكرم عبيد",
                    lat = 30.0585,
                    lng = 31.3466
                ),
                ServiceBranch(
                    id = "branch_1_2",
                    label = "branchDowntown",
                    labelAr = "فرع وسط البلد - التحرير",
                    distanceKm = "3.0",
                    waiting = 2,
                    isOpen = true,
                    address = "Downtown Cairo, Talaat Harb St.",
                    addressAr = "وسط البلد، شارع طلعت حرب",
                    lat = 30.0480,
                    lng = 31.2378
                ),
                ServiceBranch(
                    id = "branch_1_3",
                    label = "branchEast",
                    labelAr = "فرع التجمع الخامس - التسعين",
                    distanceKm = "33.3",
                    waiting = 0,
                    isOpen = false,
                    address = "New Cairo, 90th Street",
                    addressAr = "التجمع الخامس، شارع التسعين الجنوبي",
                    lat = 30.0270,
                    lng = 31.4766
                )
            )
        ),
        ServicePlace(
            id = "place_2",
            name = "Cairo Fix Auto Repair",
            nameAr = "مركز كايرو فيكس لصيانة السيارات",
            icon = Icons.Rounded.CarRepair,
            color = AppColors.Amber,
            category = ServiceCategory.GARAGES,
            starRating = 4.2,
            keywords = listOf(
                "سيارات", "صيانة", "ميكانيكي", "كايرو فيكس", "زيوت", "اطارات", "فرامل", "كهربائي",
                "auto", "car", "repair", "garage", "mechanic", "cairo fix", "oil"
            ),
            about = "Rapid car maintenance and diagnostics. Book your slot, drop the keys, and pick the car when your turn arrives.",
            aboutAr = "مركز صيانة سيارات سريع وفحص كمبيوتر. احجز دورك وسلّم المفاتيح واستلم سيارتك في موعدك المحدد.",
            workingHours = "8:00 AM – 8:00 PM",
            branches = listOf(
                ServiceBranch(
                    id = "branch_3_1",
                    label = "branchMain",
                    labelAr = "الفرع الرئيسي - المهندسين",
                    distanceKm = "3.5",
                    waiting = 7,
                    isOpen = true,
                    address = "Mohandessin, Shehab St.",
                    addressAr = "المهندسين، شارع شهاب",
                    lat = 30.0520,
                    lng = 31.2010
                ),
                ServiceBranch(
                    id = "branch_3_2",
                    label = "branchNorth",
                    labelAr = "فرع المعادي - اللاسلكي",
                    distanceKm = "14.6",
                    waiting = 5,
                    isOpen = true,
                    address = "Maadi, Wireless St.",
                    addressAr = "المعادي، شارع اللاسلكي",
                    lat = 29.9752,
                    lng = 31.2785
                ),
                ServiceBranch(
                    id = "branch_3_3",
                    label = "branchAirport",
                    labelAr = "فرع مصر الجديدة",
                    distanceKm = "15.0",
                    waiting = 1,
                    isOpen = false,
                    address = "Heliopolis, Merghany St.",
                    addressAr = "مصر الجديدة، شارع الميرغني",
                    lat = 30.0894,
                    lng = 31.3200
                )
            )
        ),
        ServicePlace(
            id = "place_3",
            name = "Mirror Beauty & Barber Salon",
            nameAr = "صالون ميرور للحلاقة والتجميل",
            icon = Icons.Rounded.ContentCut,
            color = AppColors.Indigo,
            category = ServiceCategory.SALONS,
            starRating = 4.6,
            keywords = listOf(
                "حلاقة", "صالون", "تجميل", "شعر", "قص", "سشوار", "ميرور", "كوافير",
                "salon", "barber", "hair", "beauty", "cut", "mirror"
            ),
            about = "Hair, nails and skincare in a calm place. Check the live queue and arrive exactly on time.",
            aboutAr = "عناية كاملة بالشعر والبشرة وقصات عصرية في أجواء راقية، مع طابور مباشر لتصل في دورك تماماً.",
            workingHours = "10:00 AM – 11:00 PM",
            branches = listOf(
                ServiceBranch(
                    id = "branch_2_1",
                    label = "branchMain",
                    labelAr = "الفرع الرئيسي - الزمالك",
                    distanceKm = "5.8",
                    waiting = 2,
                    isOpen = true,
                    address = "Zamalek, 26th July St.",
                    addressAr = "الزمالك، شارع 26 يوليو",
                    lat = 30.0584,
                    lng = 31.2341
                ),
                ServiceBranch(
                    id = "branch_2_2",
                    label = "branchWest",
                    labelAr = "فرع الشيخ زايد - كابيتال بيزنس",
                    distanceKm = "28.6",
                    waiting = 3,
                    isOpen = true,
                    address = "Sheikh Zayed, Capital Business Park",
                    addressAr = "الشيخ زايد، كابيتال بيزنس بارك",
                    lat = 30.0163,
                    lng = 30.9837
                )
            )
        ),
        ServicePlace(
            id = "place_4",
            name = "New Cairo Eye & Lasik Center",
            nameAr = "مركز القاهرة الجديدة للعيون والليزك",
            icon = Icons.Rounded.RemoveRedEye,
            color = AppColors.Teal,
            category = ServiceCategory.CLINICS,
            starRating = 4.4,
            keywords = listOf(
                "عيون", "ليزك", "نظارات", "بصريات", "طبيب", "عيادة", "فحص",
                "eye", "lasik", "vision", "clinic", "optics", "doctor"
            ),
            about = "Comprehensive eye examinations, glasses fitting and laser surgeries.",
            aboutAr = "فحوصات شاملة للعين والقرنية وعمليات الليزك وتفصيل النظارات الطبية والشمسية.",
            workingHours = "9:00 AM – 7:00 PM",
            branches = listOf(
                ServiceBranch(
                    id = "branch_4_1",
                    label = "branchMain",
                    labelAr = "فرع التجمع الخامس - التسعين",
                    distanceKm = "33.3",
                    waiting = 1,
                    isOpen = true,
                    address = "New Cairo, 90th Street North",
                    addressAr = "التجمع الخامس، شارع التسعين الشمالي",
                    lat = 30.0270,
                    lng = 31.4766
                )
            )
        ),
        ServicePlace(
            id = "place_5",
            name = "Downtown Notary & Law Office",
            nameAr = "مكتب وسط البلد للاستشارات القانونية والتوثيق",
            icon = Icons.Rounded.Business,
            color = AppColors.Ink,
            category = ServiceCategory.OFFICES,
            starRating = 4.1,
            keywords = listOf(
                "محاماة", "قانون", "توثيق", "عقود", "مكتب", "استشارات", "شهر عقاري",
                "lawyer", "legal", "notary", "office", "contracts", "law"
            ),
            about = "Notary services, contracts and legal consultations over a short and predictable queue.",
            aboutAr = "خدمات التوثيق وصياغة العقود والاستشارات القانونية المتخصصة عبر طابور رقمي منظم.",
            workingHours = "10:00 AM – 6:00 PM",
            branches = listOf(
                ServiceBranch(
                    id = "branch_5_1",
                    label = "branchDowntown",
                    labelAr = "فرع وسط البلد - قصر النيل",
                    distanceKm = "2.7",
                    waiting = 3,
                    isOpen = true,
                    address = "Downtown, Qasr El Nil St.",
                    addressAr = "وسط البلد، شارع قصر النيل",
                    lat = 30.0477,
                    lng = 31.2316
                )
            )
        ),
        ServicePlace(
            id = "place_6",
            name = "Glow Beauty & Spa Salon",
            nameAr = "صالون وسبا جلو بيوتي للسيدات",
            icon = Icons.Rounded.Face,
            color = AppColors.Indigo,
            category = ServiceCategory.SALONS,
            starRating = 4.7,
            keywords = listOf(
                "سبا", "تجميل", "مكياج", "ميك اب", "بشرة", "اظافر", "سيدات", "صالون",
                "glow", "beauty", "spa", "makeup", "nails", "skincare"
            ),
            about = "Makeup, lashes, nails, and skincare packages for every occasion.",
            aboutAr = "أحدث صيحات المكياج والعناية بالبشرة والأظافر والمساج للسيدات.",
            workingHours = "11:00 AM – 11:00 PM",
            branches = listOf(
                ServiceBranch(
                    id = "branch_6_1",
                    label = "branchMain",
                    labelAr = "فرع الدقي - شارع مصدق",
                    distanceKm = "1.3",
                    waiting = 5,
                    isOpen = true,
                    address = "Dokki, Mossadak St.",
                    addressAr = "الدقي، شارع مصدق",
                    lat = 30.0392,
                    lng = 31.2079
                ),
                ServiceBranch(
                    id = "branch_6_2",
                    label = "branchWest",
                    labelAr = "فرع التجمع الأول",
                    distanceKm = "25.0",
                    waiting = 0,
                    isOpen = false,
                    address = "First Settlement, Banafseg Area",
                    addressAr = "التجمع الأول، منطقة البنفسج",
                    lat = 30.0500,
                    lng = 31.4600
                )
            )
        )
    )

    private fun generateSeededAlerts(): List<AppAlert> {
        val now = System.currentTimeMillis()
        return listOf(
            AppAlert(
                id = "alert_1",
                type = AlertType.TURN,
                titleAr = "اقترب دورك في عيادة د. سارة!",
                titleEn = "Your turn is approaching!",
                descAr = "أنت الآن الثاني في طابور عيادة الأسنان (#A-12). يرجى التوجه لغرفة 3.",
                descEn = "You are 2nd in line (#A-12) for Dr. Sara Dental Clinic. Please head to Room 3.",
                timeAgoAr = "منذ دقيقتين",
                timeAgoEn = "2m ago",
                read = false,
                service = "عيادة د. سارة لطب وجراحة الأسنان",
                number = "A-12",
                timestampMillis = now - 2 * 60 * 1000
            ),
            AppAlert(
                id = "alert_2",
                type = AlertType.JOIN,
                titleAr = "تم إصدار تذكرتك بنجاح",
                titleEn = "Ticket Reserved Successfully",
                descAr = "تذكرتك رقم #A-12 محجوزة لدى عيادة د. سارة للأسنان (فرع مدينة نصر).",
                descEn = "Ticket #A-12 confirmed at Dr. Sara Dental Clinic (Nasr City).",
                timeAgoAr = "منذ 10 دقائق",
                timeAgoEn = "10m ago",
                read = false,
                service = "عيادة د. سارة لطب وجراحة الأسنان",
                number = "A-12",
                timestampMillis = now - 10 * 60 * 1000
            ),
            AppAlert(
                id = "alert_3",
                type = AlertType.OPEN,
                titleAr = "صالون ميرور بدأ استقبال العملاء",
                titleEn = "Mirror Salon Queue Now Open",
                descAr = "بدأ استقبال الطابور الصباحي في فرع الزمالك. متوسط الانتظار 10 دقائق.",
                descEn = "Morning queue is now open at Zamalek branch. Average wait is 10 mins.",
                timeAgoAr = "منذ ساعة",
                timeAgoEn = "1h ago",
                read = true,
                service = "صالون ميرور للحلاقة والتجميل",
                number = "M-04",
                timestampMillis = now - 60 * 60 * 1000
            ),
            AppAlert(
                id = "alert_4",
                type = AlertType.GO,
                titleAr = "سيارتك جاهزة للاستلام",
                titleEn = "Your Car Is Ready for Pickup",
                descAr = "انتهت صيانة سيارتك في مركز كايرو فيكس. شكراً لاختيارك طابور.",
                descEn = "Service completed at Cairo Fix Auto. Thank you for using Taboor.",
                timeAgoAr = "أمس",
                timeAgoEn = "Yesterday",
                read = true,
                service = "مركز كايرو فيكس لصيانة السيارات",
                number = "C-19",
                timestampMillis = now - 26 * 3600 * 1000
            ),
            AppAlert(
                id = "alert_5",
                type = AlertType.THANKS,
                titleAr = "شكراً لزيارتك صالون ميرور",
                titleEn = "Thank you for visiting Mirror Salon",
                descAr = "نتمنى أن تكون تجربتك كانت مميزة. يرجى تقييم الخدمة لمساعدتنا على التحسين.",
                descEn = "We hope you enjoyed your service. Please rate your experience.",
                timeAgoAr = "منذ 3 أيام",
                timeAgoEn = "3d ago",
                read = true,
                service = "صالون ميرور للحلاقة والتجميل",
                number = "M-02",
                timestampMillis = now - 3 * 86400 * 1000
            )
        )
    }

    override fun delayActiveTicket(minutes: Int) {
        _activeTicket.value = _activeTicket.value?.let {
            it.copy(
                estMinutes = it.estMinutes + minutes,
                waitingAhead = it.waitingAhead + 1
            )
        }
    }

    fun delayTicket() = delayActiveTicket(10)

    override fun cancelActiveTicket() {
        val current = _activeTicket.value
        if (current != null) {
            val historyItem = TicketHistoryItem(
                ticketNumber = current.ticketNumber,
                serviceName = current.serviceName,
                serviceNameAr = current.serviceNameAr,
                branchLabel = current.branchLabel,
                branchLabelAr = current.branchLabelAr,
                dateString = "اليوم",
                status = TicketStatus.CANCELLED,
                durationMinutes = 0
            )
            _history.value = listOf(historyItem) + _history.value
        }
        _activeTicket.value = null
    }

    fun cancelTicket() = cancelActiveTicket()

    fun setDemoTicket() {
        _activeTicket.value = UserTicket(
            ticketNumber = "A-12",
            serviceName = "Dar Al Fouad Hospital",
            serviceNameAr = "مستشفى دار الفؤاد - العيادات الخارجية",
            branchLabel = "Internal Medicine Clinic",
            branchLabelAr = "عيادة الباطنة والقلب • د. أحمد شاكر",
            counterLabel = "Counter 3",
            counterLabelAr = "شباك رقم 3",
            waitingAhead = 2,
            estMinutes = 8,
            currentServing = "A-10",
            progress = 0.75f,
            isActive = true,
            branchId = "branch_1_1",
            latitude = 30.0585,
            longitude = 31.3466
        )
    }

    override fun joinQueue(place: ServicePlace, branch: ServiceBranch): UserTicket {
        val ticket = UserTicket(
            ticketNumber = "A-${(10..99).random()}",
            serviceName = place.name,
            serviceNameAr = place.nameAr ?: place.name,
            branchLabel = branch.label,
            branchLabelAr = branch.labelAr ?: branch.label,
            counterLabel = "عيادة 2 — الدور الأول",
            counterLabelAr = "عيادة 2 — الدور الأول",
            waitingAhead = branch.waiting,
            estMinutes = (branch.waiting * 5).coerceAtLeast(5),
            currentServing = "A-01",
            progress = 0.25f,
            isActive = true,
            bookingType = TicketBookingType.LIVE_QUEUE,
            placeId = place.id,
            branchId = branch.id,
            latitude = branch.lat ?: 30.0585,
            longitude = branch.lng ?: 31.3466
        )
        _activeTicket.value = ticket
        return ticket
    }

    override fun bookScheduledTicket(
        place: ServicePlace,
        branch: ServiceBranch,
        date: String,
        slot: AppointmentSlot
    ): UserTicket {
        val ticket = UserTicket(
            ticketNumber = "S-${(10..99).random()}",
            serviceName = place.name,
            serviceNameAr = place.nameAr ?: place.name,
            branchLabel = branch.label,
            branchLabelAr = branch.labelAr ?: branch.label,
            counterLabel = "موعد مسبق مؤكد",
            counterLabelAr = "موعد مسبق مؤكد",
            waitingAhead = 0,
            estMinutes = 0,
            currentServing = "—",
            progress = 0.1f,
            isActive = true,
            bookingType = TicketBookingType.SCHEDULED,
            scheduledDate = date,
            scheduledTime = slot.time,
            placeId = place.id,
            branchId = branch.id,
            latitude = branch.lat ?: 30.0585,
            longitude = branch.lng ?: 31.3466
        )
        _activeTicket.value = ticket
        return ticket
    }

    override fun isFavorite(placeId: String): Boolean {
        return _favoritePlaceIds.value.contains(placeId)
    }

    override fun toggleFavorite(placeId: String) {
        val current = _favoritePlaceIds.value.toMutableSet()
        if (current.contains(placeId)) {
            current.remove(placeId)
        } else {
            current.add(placeId)
        }
        _favoritePlaceIds.value = current
    }

    override fun updateAlertPreferences(prefs: SmartAlertPreferences) {
        _smartAlertPreferences.value = prefs
    }

    override fun updateSelectedLocation(location: String) {
        _selectedLocation.value = location
    }

    override fun dismissAlert(alertId: String) {
        _alerts.value = _alerts.value.filterNot { it.id == alertId }
    }

    override fun markAlertRead(alertId: String) {
        _alerts.value = _alerts.value.map {
            if (it.id == alertId) it.copy(read = true) else it
        }
    }

    override fun markAllAlertsRead() {
        _alerts.value = _alerts.value.map { it.copy(read = true) }
    }

    override fun clearAllAlerts() {
        _alerts.value = emptyList()
    }

    override fun getPlaceById(id: String): ServicePlace? {
        return places.firstOrNull { it.id == id }
    }

    override fun filterPlaces(query: String, category: ServiceCategory?): List<ServicePlace> {
        return places.filter { place ->
            val matchesCategory = category == null || place.category == category
            val cleanQuery = query.trim().lowercase()
            val matchesQuery = cleanQuery.isEmpty() ||
                place.name.lowercase().contains(cleanQuery) ||
                (place.nameAr?.lowercase()?.contains(cleanQuery) == true) ||
                place.keywords.any { it.lowercase().contains(cleanQuery) }
            matchesCategory && matchesQuery
        }
    }
}
