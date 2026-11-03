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
                    label = "branchMain",
                    labelAr = "الفرع الرئيسي - مدينة نصر",
                    distanceKm = "0.8",
                    waiting = 4,
                    isOpen = true,
                    address = "Nasr City, Makram Ebeid St.",
                    addressAr = "مدينة نصر، شارع مكرم عبيد",
                    lat = 30.0525,
                    lng = 31.2408
                ),
                ServiceBranch(
                    label = "branchDowntown",
                    labelAr = "فرع وسط البلد - التحرير",
                    distanceKm = "1.9",
                    waiting = 2,
                    isOpen = true,
                    address = "Downtown Cairo, Talaat Harb St.",
                    addressAr = "وسط البلد، شارع طلعت حرب",
                    lat = 30.0501,
                    lng = 31.2426
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
                    label = "branchMain",
                    labelAr = "الفرع الرئيسي - المهندسين",
                    distanceKm = "1.5",
                    waiting = 7,
                    isOpen = true,
                    address = "Mohandessin, Shehab St.",
                    addressAr = "المهندسين، شارع شهاب",
                    lat = 30.0672,
                    lng = 31.2358
                ),
                ServiceBranch(
                    label = "branchNorth",
                    labelAr = "فرع المعادي - اللاسلكي",
                    distanceKm = "2.8",
                    waiting = 5,
                    isOpen = true,
                    address = "Maadi, Wireless St.",
                    addressAr = "المعادي، شارع اللاسلكي",
                    lat = 30.0894,
                    lng = 31.2421
                ),
                ServiceBranch(
                    label = "branchAirport",
                    labelAr = "فرع مصر الجديدة",
                    distanceKm = "6.2",
                    waiting = 1,
                    isOpen = false,
                    address = "Heliopolis, Merghany St.",
                    addressAr = "مصر الجديدة، شارع الميرغني",
                    lat = 30.1219,
                    lng = 31.4056
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
                    label = "branchMain",
                    labelAr = "الفرع الرئيسي - الزمالك",
                    distanceKm = "0.4",
                    waiting = 2,
                    isOpen = true,
                    address = "Zamalek, 26th July St.",
                    addressAr = "الزمالك، شارع 26 يوليو",
                    lat = 30.0584,
                    lng = 31.2341
                ),
                ServiceBranch(
                    label = "branchWest",
                    labelAr = "فرع الشيخ زايد - كابيتال بيزنس",
                    distanceKm = "3.1",
                    waiting = 3,
                    isOpen = true,
                    address = "Sheikh Zayed, Capital Business Park",
                    addressAr = "الشيخ زايد، كابيتال بيزنس بارك",
                    lat = 30.0375,
                    lng = 31.2135
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
                    label = "branchMain",
                    labelAr = "فرع التجمع الخامس - التسعين",
                    distanceKm = "2.1",
                    waiting = 1,
                    isOpen = true,
                    address = "New Cairo, 90th Street North",
                    addressAr = "التجمع الخامس، شارع التسعين الشمالي",
                    lat = 30.0187,
                    lng = 31.4725
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
                    label = "branchDowntown",
                    labelAr = "فرع وسط البلد - قصر النيل",
                    distanceKm = "3.0",
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
                    label = "branchMain",
                    labelAr = "فرع الدقي - شارع مصدق",
                    distanceKm = "1.2",
                    waiting = 5,
                    isOpen = true,
                    address = "Dokki, Mossadak St.",
                    addressAr = "الدقي، شارع مصدق",
                    lat = 30.0646,
                    lng = 31.2601
                ),
                ServiceBranch(
                    label = "branchWest",
                    labelAr = "فرع التجمع الأول",
                    distanceKm = "4.0",
                    waiting = 0,
                    isOpen = false,
                    address = "First Settlement, Banafseg Area",
                    addressAr = "التجمع الأول، منطقة البنفسج",
                    lat = 30.0311,
                    lng = 31.2037
                )
            )
        )
    )

    private fun generateSeededAlerts(): List<AppAlert> {
        val now = System.currentTimeMillis()
        return listOf(
            AppAlert(
                id = "alert_1",
                type = AlertType.JOIN,
                service = "Dr. Sara Dental Clinic",
                number = "A-12",
                timestampMillis = now - 2 * 60 * 1000,
                read = false
            ),
            AppAlert(
                id = "alert_2",
                type = AlertType.OPEN,
                service = "Mirror Salon",
                number = "M-04",
                timestampMillis = now - 55 * 60 * 1000,
                read = false
            ),
            AppAlert(
                id = "alert_3",
                type = AlertType.TURN,
                service = "Cairo Fix Auto",
                number = "C-19",
                timestampMillis = now - 3 * 3600 * 1000,
                read = true
            ),
            AppAlert(
                id = "alert_4",
                type = AlertType.GO,
                service = "Mirror Salon",
                number = "M-02",
                timestampMillis = now - 26 * 3600 * 1000,
                read = true
            ),
            AppAlert(
                id = "alert_5",
                type = AlertType.THANKS,
                service = "Dr. Sara Dental Clinic",
                number = "A-09",
                timestampMillis = now - 3 * 86400 * 1000,
                read = true
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
        _activeTicket.value = null
    }

    fun cancelTicket() = cancelActiveTicket()

    override fun joinQueue(place: ServicePlace, branch: ServiceBranch): UserTicket {
        val ticket = UserTicket(
            ticketNumber = "A-${(10..99).random()}",
            serviceName = place.name,
            serviceNameAr = place.nameAr ?: place.name,
            branchLabel = branch.label,
            branchLabelAr = branch.labelAr ?: branch.label,
            waitingAhead = branch.waiting,
            estMinutes = (branch.waiting * 5).coerceAtLeast(5),
            currentServing = "A-01",
            progress = 0.25f,
            isActive = true
        )
        _activeTicket.value = ticket
        return ticket
    }

    override fun dismissAlert(alertId: String) {
        _alerts.value = _alerts.value.filterNot { it.id == alertId }
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
