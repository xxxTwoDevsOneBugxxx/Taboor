using Taboor_Domain.Entities;

namespace Taboor_Seeder.SeedData;

public record TenantSeedItem(
    string Name,
    string Slug,
    string ContactEmail,
    string ContactPhone,
    string TargetPlanName,
    SubscriptionStatus DesiredSubscriptionStatus,
    TenantStatus DesiredTenantStatus,
    int DurationMonths,
    string OwnerEmail,
    string OwnerName,
    string OwnerJobTitle,
    string? StaffEmail = null,
    string? StaffName = null,
    string? StaffJobTitle = null);

public static class TenantSeedData
{
    public static IReadOnlyList<TenantSeedItem> Tenants => new List<TenantSeedItem>
    {
        // -------------------------------------------------------------
        // Medical & Healthcare (10 Tenants)
        // -------------------------------------------------------------
        new(
            Name: "عيادات الشفاء التخصصية",
            Slug: "al-shefaa-clinics",
            ContactEmail: "info@shefaaclinic.com",
            ContactPhone: "01001234567",
            TargetPlanName: "Pro",
            DesiredSubscriptionStatus: SubscriptionStatus.Active,
            DesiredTenantStatus: TenantStatus.Active,
            DurationMonths: 6,
            OwnerEmail: "dr.tariq@shefaaclinic.com",
            OwnerName: "د. طارق المنشاوي",
            OwnerJobTitle: "استشاري الباطنة والمدير الطبي",
            StaffEmail: "nour.reception@shefaaclinic.com",
            StaffName: "نور إبراهيم",
            StaffJobTitle: "مسؤولة الاستقبال الأولى"
        ),
        new(
            Name: "مركز النور لطب وجراحة الأسنان",
            Slug: "al-noor-dental",
            ContactEmail: "contact@noordental.com",
            ContactPhone: "01112345678",
            TargetPlanName: "Pro",
            DesiredSubscriptionStatus: SubscriptionStatus.Active,
            DesiredTenantStatus: TenantStatus.Active,
            DurationMonths: 4,
            OwnerEmail: "dr.hossam@noordental.com",
            OwnerName: "د. حسام عبد الرازق",
            OwnerJobTitle: "أخصائي زراعة الأسنان",
            StaffEmail: "yara.reception@noordental.com",
            StaffName: "يارا مصطفى",
            StaffJobTitle: "منسقة المواعيد والنداء"
        ),
        new(
            Name: "مستشفى السلام الدولي",
            Slug: "al-salam-hospital",
            ContactEmail: "admin@salamhospital.org",
            ContactPhone: "01223456789",
            TargetPlanName: "Enterprise",
            DesiredSubscriptionStatus: SubscriptionStatus.Active,
            DesiredTenantStatus: TenantStatus.Active,
            DurationMonths: 12,
            OwnerEmail: "dr.magdy@salamhospital.org",
            OwnerName: "د. مجدي يعقوب السعدني",
            OwnerJobTitle: "الرئيس التنفيذي للمستشفى",
            StaffEmail: "reception.desk@salamhospital.org",
            StaffName: "كريم شريف",
            StaffJobTitle: "مشرف كاونترات العيادات الخارجية"
        ),
        new(
            Name: "معامل رويال للتحاليل الطبية",
            Slug: "royal-labs",
            ContactEmail: "support@royallabs.eg",
            ContactPhone: "01034567890",
            TargetPlanName: "Enterprise",
            DesiredSubscriptionStatus: SubscriptionStatus.Active,
            DesiredTenantStatus: TenantStatus.Active,
            DurationMonths: 8,
            OwnerEmail: "dr.eman@royallabs.eg",
            OwnerName: "د. إيمان عبد الله",
            OwnerJobTitle: "مدير المعامل والجودة"
        ),
        new(
            Name: "مركز الحياة للأشعة والتصوير الطبي",
            Slug: "al-hayah-radiology",
            ContactEmail: "info@hayah-scan.com",
            ContactPhone: "01145678901",
            TargetPlanName: "Pro",
            DesiredSubscriptionStatus: SubscriptionStatus.PastDue,
            DesiredTenantStatus: TenantStatus.Active,
            DurationMonths: 3,
            OwnerEmail: "dr.ashraf@hayah-scan.com",
            OwnerName: "د. أشرف فوزي",
            OwnerJobTitle: "استشاري الأشعة التشخيصية"
        ),
        new(
            Name: "عيادة د. تامر خليل لجراحة العظام",
            Slug: "dr-tamer-ortho",
            ContactEmail: "dr.tamer@orthoclinic.net",
            ContactPhone: "01256789012",
            TargetPlanName: "Starter",
            DesiredSubscriptionStatus: SubscriptionStatus.Active,
            DesiredTenantStatus: TenantStatus.Active,
            DurationMonths: 2,
            OwnerEmail: "dr.tamer.khalil@taboor.com",
            OwnerName: "د. تامر خليل",
            OwnerJobTitle: "استشاري جراحة العظام والمفاصل"
        ),
        new(
            Name: "مركز المستقبل لطب الأطفال وحديثي الولادة",
            Slug: "mostaqbal-pediatrics",
            ContactEmail: "care@mostaqbal-kids.com",
            ContactPhone: "01067890123",
            TargetPlanName: "Pro",
            DesiredSubscriptionStatus: SubscriptionStatus.Trialing,
            DesiredTenantStatus: TenantStatus.Active,
            DurationMonths: 0,
            OwnerEmail: "dr.salma@mostaqbal-kids.com",
            OwnerName: "د. سلمى الجوهري",
            OwnerJobTitle: "أخصائية طب الأطفال"
        ),
        new(
            Name: "عيادات الصفوة للجلدية والليزر",
            Slug: "al-safwa-derma",
            ContactEmail: "info@alsafwa-derma.com",
            ContactPhone: "01178901234",
            TargetPlanName: "Pro",
            DesiredSubscriptionStatus: SubscriptionStatus.Active,
            DesiredTenantStatus: TenantStatus.Active,
            DurationMonths: 5,
            OwnerEmail: "dr.mona.derma@taboor.com",
            OwnerName: "د. منى زهران",
            OwnerJobTitle: "استشارية الأمراض الجلدية"
        ),
        new(
            Name: "مركز الأمل للعلاج الطبيعي والتأهيل",
            Slug: "alamal-physio",
            ContactEmail: "physio@alamal-rehab.com",
            ContactPhone: "01289012345",
            TargetPlanName: "Starter",
            DesiredSubscriptionStatus: SubscriptionStatus.Canceled,
            DesiredTenantStatus: TenantStatus.Active,
            DurationMonths: 2,
            OwnerEmail: "dr.ahmed.physio@taboor.com",
            OwnerName: "د. أحمد كمال الدين",
            OwnerJobTitle: "أخصائي العلاج الطبيعي"
        ),
        new(
            Name: "مجمع النيل الطبي الشامل",
            Slug: "nile-medical-complex",
            ContactEmail: "admin@nilemedical.org",
            ContactPhone: "01090123456",
            TargetPlanName: "Enterprise",
            DesiredSubscriptionStatus: SubscriptionStatus.PastDue,
            DesiredTenantStatus: TenantStatus.Active,
            DurationMonths: 7,
            OwnerEmail: "ceo@nilemedical.org",
            OwnerName: "د. فاروق الباز السويفي",
            OwnerJobTitle: "رئيس مجلس الإدارة"
        ),

        // -------------------------------------------------------------
        // Beauty, Salons & Barbershops (8 Tenants)
        // -------------------------------------------------------------
        new(
            Name: "صالون ڤوج للتجميل والعناية",
            Slug: "vogue-beauty-salon",
            ContactEmail: "booking@vogue-salon.com",
            ContactPhone: "01101234567",
            TargetPlanName: "Pro",
            DesiredSubscriptionStatus: SubscriptionStatus.Active,
            DesiredTenantStatus: TenantStatus.Active,
            DurationMonths: 5,
            OwnerEmail: "shereen@vogue-salon.com",
            OwnerName: "شيرين عبد المنعم",
            OwnerJobTitle: "مالكة الصالون وخبيرة التجميل",
            StaffEmail: "nada@vogue-salon.com",
            StaffName: "ندى سامي",
            StaffJobTitle: "مصففة شعر رئيسية"
        ),
        new(
            Name: "باربر شوب كينجز للرجال",
            Slug: "kings-barber-shop",
            ContactEmail: "kings@kingsbarbershop.net",
            ContactPhone: "01212345678",
            TargetPlanName: "Starter",
            DesiredSubscriptionStatus: SubscriptionStatus.Active,
            DesiredTenantStatus: TenantStatus.Active,
            DurationMonths: 3,
            OwnerEmail: "mahmoud.kings@taboor.com",
            OwnerName: "محمود الباشا",
            OwnerJobTitle: "كبير الحلاقين والمالك"
        ),
        new(
            Name: "مركز لوتس سبا والعناية بالبشرة",
            Slug: "lotus-spa-center",
            ContactEmail: "spa@lotus-wellness.com",
            ContactPhone: "01023456789",
            TargetPlanName: "Pro",
            DesiredSubscriptionStatus: SubscriptionStatus.Active,
            DesiredTenantStatus: TenantStatus.Active,
            DurationMonths: 4,
            OwnerEmail: "rania@lotus-wellness.com",
            OwnerName: "رانيا النجار",
            OwnerJobTitle: "مديرة السبا"
        ),
        new(
            Name: "صالون برنسيسة المعادي",
            Slug: "princessa-salon-maadi",
            ContactEmail: "contact@princessasalon.com",
            ContactPhone: "01134567890",
            TargetPlanName: "Starter",
            DesiredSubscriptionStatus: SubscriptionStatus.Trialing,
            DesiredTenantStatus: TenantStatus.Active,
            DurationMonths: 0,
            OwnerEmail: "fatma.princessa@taboor.com",
            OwnerName: "فاطمة عزمي",
            OwnerJobTitle: "المالكة"
        ),
        new(
            Name: "ذا في اي بي باربر كلوب",
            Slug: "vip-barber-club",
            ContactEmail: "vip@barberclub.eg",
            ContactPhone: "01245678901",
            TargetPlanName: "Pro",
            DesiredSubscriptionStatus: SubscriptionStatus.Active,
            DesiredTenantStatus: TenantStatus.Active,
            DurationMonths: 6,
            OwnerEmail: "amr.vip@taboor.com",
            OwnerName: "عمرو السعدني",
            OwnerJobTitle: "مدير السلسلة"
        ),
        new(
            Name: "بيوتي سنتر روز جولد",
            Slug: "rose-gold-beauty",
            ContactEmail: "info@rosegold.com",
            ContactPhone: "01056789012",
            TargetPlanName: "Starter",
            DesiredSubscriptionStatus: SubscriptionStatus.Expired,
            DesiredTenantStatus: TenantStatus.Suspended,
            DurationMonths: 1,
            OwnerEmail: "yasmin.rosegold@taboor.com",
            OwnerName: "ياسمين القاضي",
            OwnerJobTitle: "المالكة"
        ),
        new(
            Name: "جنتلمان صالون اند سبا",
            Slug: "gentleman-salon-spa",
            ContactEmail: "hello@gentleman-eg.com",
            ContactPhone: "01167890123",
            TargetPlanName: "Pro",
            DesiredSubscriptionStatus: SubscriptionStatus.Active,
            DesiredTenantStatus: TenantStatus.Active,
            DurationMonths: 2,
            OwnerEmail: "ziad.gentleman@taboor.com",
            OwnerName: "زياد الشناوي",
            OwnerJobTitle: "مدير العمليات"
        ),
        new(
            Name: "صالون لمسات لخبراء التجميل",
            Slug: "lamasat-beauty",
            ContactEmail: "info@lamasat-salon.com",
            ContactPhone: "01278901234",
            TargetPlanName: "Starter",
            DesiredSubscriptionStatus: SubscriptionStatus.Trialing,
            DesiredTenantStatus: TenantStatus.Active,
            DurationMonths: 0,
            OwnerEmail: "marwa.lamasat@taboor.com",
            OwnerName: "مروة الشربيني",
            OwnerJobTitle: "أخصائية التجميل"
        ),

        // -------------------------------------------------------------
        // Automotive, Workshops & Service Centers (5 Tenants)
        // -------------------------------------------------------------
        new(
            Name: "المركز الألماني لصيانة السيارات الفارهة",
            Slug: "german-auto-service",
            ContactEmail: "service@germanauto.com.eg",
            ContactPhone: "01089012345",
            TargetPlanName: "Enterprise",
            DesiredSubscriptionStatus: SubscriptionStatus.Active,
            DesiredTenantStatus: TenantStatus.Active,
            DurationMonths: 9,
            OwnerEmail: "eng.tamer@germanauto.com.eg",
            OwnerName: "م. تامر الجيار",
            OwnerJobTitle: "المدير العام لخدمات الصيانة",
            StaffEmail: "cs@germanauto.com.eg",
            StaffName: "أحمد رشاد",
            StaffJobTitle: "مسؤول استقبال الصيانة"
        ),
        new(
            Name: "مركز النصر لخدمات وصيانة الإطارات والضبط",
            Slug: "al-nasr-tires-auto",
            ContactEmail: "alnasr@autotires.com",
            ContactPhone: "01190123456",
            TargetPlanName: "Starter",
            DesiredSubscriptionStatus: SubscriptionStatus.Active,
            DesiredTenantStatus: TenantStatus.Active,
            DurationMonths: 3,
            OwnerEmail: "sayed.nasr@taboor.com",
            OwnerName: "سيد أبو النصر",
            OwnerJobTitle: "رئيس الورشة"
        ),
        new(
            Name: "إكسبريس لخدمات غسيل السيارات الذكي",
            Slug: "express-car-wash",
            ContactEmail: "info@expresscarwash.eg",
            ContactPhone: "01201234567",
            TargetPlanName: "Starter",
            DesiredSubscriptionStatus: SubscriptionStatus.Trialing,
            DesiredTenantStatus: TenantStatus.Active,
            DurationMonths: 0,
            OwnerEmail: "hany.express@taboor.com",
            OwnerName: "هاني بركات",
            OwnerJobTitle: "مالك المحطة"
        ),
        new(
            Name: "مركز كويك فيكس لميكانيكا وكهرباء السيارات",
            Slug: "quick-fix-auto",
            ContactEmail: "support@quickfix-cars.com",
            ContactPhone: "01012345670",
            TargetPlanName: "Pro",
            DesiredSubscriptionStatus: SubscriptionStatus.PastDue,
            DesiredTenantStatus: TenantStatus.Active,
            DurationMonths: 4,
            OwnerEmail: "eng.ramy@quickfix-cars.com",
            OwnerName: "م. رامي عادل",
            OwnerJobTitle: "مدير الصيانة الفنية"
        ),
        new(
            Name: "مركز التوكيل لفحص وتجديد السيارات",
            Slug: "el-tawkeel-auto-check",
            ContactEmail: "check@eltawkeel.com",
            ContactPhone: "01123456781",
            TargetPlanName: "Pro",
            DesiredSubscriptionStatus: SubscriptionStatus.Active,
            DesiredTenantStatus: TenantStatus.Active,
            DurationMonths: 6,
            OwnerEmail: "khaled.tawkeel@taboor.com",
            OwnerName: "خالد مسعود",
            OwnerJobTitle: "مدير مركز الفحص"
        ),

        // -------------------------------------------------------------
        // Professional Services & Public/Customer Centers (4 Tenants)
        // -------------------------------------------------------------
        new(
            Name: "مجموعة المستشار للخدمات القانونية والتوثيق",
            Slug: "al-mostashar-legal",
            ContactEmail: "info@mostashar-law.com",
            ContactPhone: "01234567892",
            TargetPlanName: "Pro",
            DesiredSubscriptionStatus: SubscriptionStatus.Active,
            DesiredTenantStatus: TenantStatus.Active,
            DurationMonths: 5,
            OwnerEmail: "counsel.ibrahim@mostashar-law.com",
            OwnerName: "المستشار إبراهيم رضوان",
            OwnerJobTitle: "الشريك الإداري للمجموعة"
        ),
        new(
            Name: "مركز خدمات رجال الأعمال والتأشيرات",
            Slug: "business-visa-hub",
            ContactEmail: "visa@businesshub.com.eg",
            ContactPhone: "01045678903",
            TargetPlanName: "Enterprise",
            DesiredSubscriptionStatus: SubscriptionStatus.Active,
            DesiredTenantStatus: TenantStatus.Active,
            DurationMonths: 10,
            OwnerEmail: "director@businesshub.com.eg",
            OwnerName: "شريف الألفي",
            OwnerJobTitle: "مدير المركز التنفيذي",
            StaffEmail: "desk1@businesshub.com.eg",
            StaffName: "مروان شكري",
            StaffJobTitle: "موظف شباك الجوازات"
        ),
        new(
            Name: "أكاديمية إبداع للتدريب والاستشارات",
            Slug: "ebda3-training-academy",
            ContactEmail: "training@ebda3academy.com",
            ContactPhone: "01156789014",
            TargetPlanName: "Starter",
            DesiredSubscriptionStatus: SubscriptionStatus.Trialing,
            DesiredTenantStatus: TenantStatus.Active,
            DurationMonths: 0,
            OwnerEmail: "dr.walaa@ebda3academy.com",
            OwnerName: "د. ولاء الصاوي",
            OwnerJobTitle: "عميد الأكاديمية"
        ),
        new(
            Name: "مكتب مصر للترجمة المعتمدة وشؤون السفر",
            Slug: "misr-certified-translation",
            ContactEmail: "contact@misrtranslation.com",
            ContactPhone: "01267890125",
            TargetPlanName: "Starter",
            DesiredSubscriptionStatus: SubscriptionStatus.Canceled,
            DesiredTenantStatus: TenantStatus.Suspended,
            DurationMonths: 2,
            OwnerEmail: "atef.translation@taboor.com",
            OwnerName: "عاطف نور الدين",
            OwnerJobTitle: "المدير العام"
        )
    };
}
