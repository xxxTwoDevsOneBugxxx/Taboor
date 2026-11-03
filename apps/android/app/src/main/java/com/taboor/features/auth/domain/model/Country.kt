package com.taboor.features.auth.domain.model

data class Country(
    val name: String,
    val nameAr: String,
    val dialCode: String,
    val code: String,
    val flag: String
) {
    companion object {
        val Egypt = Country("Egypt", "مصر", "+20", "EG", "🇪🇬")
        val SaudiArabia = Country("Saudi Arabia", "السعودية", "+966", "SA", "🇸🇦")
        val UAE = Country("United Arab Emirates", "الإمارات", "+971", "AE", "🇦🇪")
        val Kuwait = Country("Kuwait", "الكويت", "+965", "KW", "🇰🇼")
        val Qatar = Country("Qatar", "قطر", "+974", "QA", "🇶🇦")
        val Bahrain = Country("Bahrain", "البحرين", "+973", "BH", "🇧🇭")
        val Oman = Country("Oman", "عُمان", "+968", "OM", "🇴🇲")
        val Jordan = Country("Jordan", "الأردن", "+962", "JO", "🇯🇴")
        val Lebanon = Country("Lebanon", "لبنان", "+961", "LB", "🇱🇧")
        val Iraq = Country("Iraq", "العراق", "+964", "IQ", "🇮🇶")
        val UnitedKingdom = Country("United Kingdom", "المملكة المتحدة", "+44", "GB", "🇬🇧")
        val UnitedStates = Country("United States", "الولايات المتحدة", "+1", "US", "🇺🇸")

        val allCountries: List<Country> = listOf(
            Egypt,
            SaudiArabia,
            UAE,
            Kuwait,
            Qatar,
            Bahrain,
            Oman,
            Jordan,
            Lebanon,
            Iraq,
            UnitedKingdom,
            UnitedStates
        )
    }
}
