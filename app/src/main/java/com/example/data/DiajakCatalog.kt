package com.example.data

import com.example.R
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

object DiajakCatalog {

    val allNiches: List<NicheCategory> = listOf(
        NicheCategory("Alam", "🌲"),
        NicheCategory("Amal", "💝"),
        NicheCategory("Bahasa", "🗣️"),
        NicheCategory("Bazar", "🎪"),
        NicheCategory("Belanja", "🛍️"),
        NicheCategory("Bimbingan", "🧭"),
        NicheCategory("Bisnis", "💼"),
        NicheCategory("Budaya", "🎭"),
        NicheCategory("Busana", "👗"),
        NicheCategory("Diskusi", "💬"),
        NicheCategory("Esport", "🎮"),
        NicheCategory("Festival", "🎉"),
        NicheCategory("Film", "🎬"),
        NicheCategory("Fotografi", "📸"),
        NicheCategory("Gelarwicara", "🎙️"),
        NicheCategory("Hewan", "🐾"),
        NicheCategory("Hiburan", "🎡"),
        NicheCategory("Hobi", "🧩"),
        NicheCategory("Identitas", "🪪"),
        NicheCategory("Investasi", "📈"),
        NicheCategory("Jejaring", "🌐"),
        NicheCategory("Karier", "👔"),
        NicheCategory("Kebugaran", "💪"),
        NicheCategory("Kecantikan", "💄"),
        NicheCategory("Keluarga", "👨‍👩‍👧"),
        NicheCategory("Kemah", "🏕️"),
        NicheCategory("Kesehatan", "🩺"),
        NicheCategory("Kesejahteraan", "🌿"),
        NicheCategory("Keuangan", "💰"),
        NicheCategory("Komedi", "😂"),
        NicheCategory("Komunitas", "🤝"),
        NicheCategory("Konser", "🎸"),
        NicheCategory("Kopi", "☕"),
        NicheCategory("Kriya", "🧵"),
        NicheCategory("Kuliner", "🍲"),
        NicheCategory("Lari", "🏃"),
        NicheCategory("Lingkungan", "🌱"),
        NicheCategory("Lokakarya", "🔨"),
        NicheCategory("Media", "📰"),
        NicheCategory("Meditasi", "🧘"),
        NicheCategory("Menari", "💃"),
        NicheCategory("Menulis", "✍️"),
        NicheCategory("Musik", "🎵"),
        NicheCategory("Nongkrong", "🛋️"),
        NicheCategory("Olahraga", "⚽"),
        NicheCategory("Otomotif", "🚗"),
        NicheCategory("Pameran", "🖼️"),
        NicheCategory("Pelatihan", "📚"),
        NicheCategory("Pendidikan", "🎓"),
        NicheCategory("Pendakian", "🧗"),
        NicheCategory("Pengembangan", "🚀"),
        NicheCategory("Perjalanan", "✈️"),
        NicheCategory("Permainan", "🎲"),
        NicheCategory("Pesta", "🥳"),
        NicheCategory("Petualangan", "🗺️"),
        NicheCategory("Piknik", "🧺"),
        NicheCategory("Relaksasi", "💆"),
        NicheCategory("Relawan", "🤲"),
        NicheCategory("Religi", "🕊️"),
        NicheCategory("Sains", "🔬"),
        NicheCategory("Seminar", "🎤"),
        NicheCategory("Seni", "🎨"),
        NicheCategory("Sepeda", "🚴"),
        NicheCategory("Sosial", "👥"),
        NicheCategory("Spiritualitas", "✨"),
        NicheCategory("Teater", "🎟️"),
        NicheCategory("Teknologi", "💻"),
        NicheCategory("Wisata", "🏖️"),
        NicheCategory("Yoga", "🧘‍♂️")
    )

    val quickFilterNiches: List<NicheCategory> = listOf(
        NicheCategory("Kebugaran", "💪"),
        NicheCategory("Lari", "🏃"),
        NicheCategory("Olahraga", "⚽"),
        NicheCategory("Esport", "🎮"),
        NicheCategory("Fotografi", "📸"),
        NicheCategory("Kopi", "☕"),
        NicheCategory("Yoga", "🧘‍♂️"),
        NicheCategory("Komunitas", "🤝"),
        NicheCategory("Lokakarya", "🔨"),
        NicheCategory("Kuliner", "🍲")
    )

    val cities: List<CityOption> = listOf(
        CityOption("Jakarta Selatan", -6.2615, 106.8106),
        CityOption("Jakarta Pusat", -6.1865, 106.8341),
        CityOption("Tangerang Selatan", -6.2886, 106.7179),
        CityOption("Bandung", -6.9175, 107.6191),
        CityOption("Bali (Canggu)", -8.6478, 115.1385),
        CityOption("Surabaya", -7.2575, 112.7521),
        CityOption("Yogyakarta", -7.7956, 110.3695),
        CityOption("Semarang", -6.9667, 110.4167),
        CityOption("Medan", 3.5952, 98.6722),
        CityOption("Makassar", -5.1477, 119.4327)
    )

    val presetPhotoKeys: List<Pair<String, String>> = listOf(
        "img_activity_hyrox" to "Hyrox & Kebugaran",
        "img_activity_running" to "Lari Komunitas",
        "img_activity_padel" to "Padel & Olahraga",
        "img_activity_photowalk" to "Fotografi & Kopi"
    )

    fun resolveDrawableRes(key: String): Int = when (key) {
        "img_activity_hyrox" -> R.drawable.img_activity_hyrox
        "img_activity_running" -> R.drawable.img_activity_running
        "img_activity_padel" -> R.drawable.img_activity_padel
        "img_activity_photowalk" -> R.drawable.img_activity_photowalk
        else -> R.drawable.img_activity_running
    }

    fun findCityByName(name: String): CityOption =
        cities.firstOrNull { it.name.equals(name, ignoreCase = true) } ?: cities.first()

    fun findNearestCity(lat: Double, lng: Double): CityOption =
        cities.minByOrNull { distanceKm(lat, lng, it.latitude, it.longitude) } ?: cities.first()

    fun distanceKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val earthRadiusKm = 6371.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2).pow(2.0) +
            cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLon / 2).pow(2.0)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return earthRadiusKm * c
    }

    fun formatRupiah(amount: Long): String {
        if (amount <= 0L) return "Gratis"
        val formatter = NumberFormat.getNumberInstance(Locale("in", "ID"))
        return "Rp ${formatter.format(amount)}"
    }

    fun formatDayNumber(millis: Long): String {
        val sdf = SimpleDateFormat("dd", Locale("in", "ID"))
        return sdf.format(Date(millis))
    }

    fun formatShortMonth(millis: Long): String {
        val sdf = SimpleDateFormat("MMM", Locale("in", "ID"))
        return sdf.format(Date(millis)).uppercase(Locale("in", "ID"))
    }

    fun formatFullDate(millis: Long): String {
        val sdf = SimpleDateFormat("EEE, dd MMM yyyy", Locale("in", "ID"))
        return sdf.format(Date(millis))
    }

    fun formatDatePill(millis: Long, timeLabel: String): String {
        val sdf = SimpleDateFormat("dd MMM", Locale("in", "ID"))
        return "${sdf.format(Date(millis))} · $timeLabel"
    }

    fun buildShowcaseSeedActivities(nowMillis: Long = System.currentTimeMillis()): List<Pair<String, Map<String, Any>>> {
        val dayMs = 86_400_000L
        return listOf(
            "sample_hyrox_jkt" to mapOf(
                "organizerId" to "community_hyrox_id",
                "organizerName" to "Hyrox Jakarta Collective",
                "organizerAvatarKey" to "img_activity_hyrox",
                "organizerVerified" to true,
                "title" to "Hyrox Simulation & Sled Push Camp",
                "description" to "Sesi simulasi lengkap Hyrox berisi lari interval 1km, sled push, ski erg, dan wall balls bersama coach tersertifikasi. Terbuka untuk pemula hingga atlet race prep yang ingin merasakan atmosfer komunitas kebugaran suportif.",
                "category" to "Kebugaran",
                "categoryEmoji" to "💪",
                "city" to "Jakarta Selatan",
                "locationName" to "Kemang Industrial Gym, Jakarta",
                "latitude" to -6.2607,
                "longitude" to 106.8133,
                "eventDateMillis" to (nowMillis + dayMs * 3),
                "timeLabel" to "07:00 WIB",
                "priceIdr" to 165000L,
                "quota" to 40L,
                "joinedCount" to 34L,
                "favoriteCount" to 128L,
                "viralScore" to 960L,
                "photoKeys" to listOf("img_activity_hyrox", "img_activity_running"),
                "benefits" to listOf(
                    "Sesi coaching & timing chip resmi",
                    "Electrolyte drink & cold towel",
                    "Dokumentasi foto HD oleh fotografer komunitas",
                    "Akses grup komunitas latihan mingguan"
                ),
                "joinedUserNames" to listOf("Raka Pratama", "Nadia Kusuma", "Dimas Arya", "Sinta Dewi", "Bagas Wijaya"),
                "status" to "PUBLISHED",
                "averageRating" to 4.9,
                "reviewCount" to 18L,
                "seededSample" to true
            ),
            "sample_running_gbk" to mapOf(
                "organizerId" to "community_senayan_run",
                "organizerName" to "Senayan Dawn Runners",
                "organizerAvatarKey" to "img_activity_running",
                "organizerVerified" to true,
                "title" to "Sunday Morning Social 5K & 10K Run",
                "description" to "Lari pagi santai bareng ratusan pelari komunitas mengelilingi ring road GBK dengan pilihan pace 6:00, 6:45, dan easy conversational pace 7:30. Setelah selesai kita ngopi bareng di area taman!",
                "category" to "Lari",
                "categoryEmoji" to "🏃",
                "city" to "Jakarta Pusat",
                "locationName" to "Pintu 7 GBK Senayan, Jakarta",
                "latitude" to -6.2186,
                "longitude" to 106.8025,
                "eventDateMillis" to (nowMillis + dayMs * 2),
                "timeLabel" to "06:00 WIB",
                "priceIdr" to 0L,
                "quota" to 120L,
                "joinedCount" to 89L,
                "favoriteCount" to 214L,
                "viralScore" to 990L,
                "photoKeys" to listOf("img_activity_running", "img_activity_photowalk"),
                "benefits" to listOf(
                    "Gratis bergabung untuk semua level",
                    "Pacer komunitas resmi di setiap grup pace",
                    "Water station & buah segar di garis finis",
                    "Foto candid sepanjang rute lari"
                ),
                "joinedUserNames" to listOf("Aulia Rahman", "Tania Putri", "Kevin Santoso", "Rizky Fajar", "Maya Lestari"),
                "status" to "PUBLISHED",
                "averageRating" to 4.9,
                "reviewCount" to 42L,
                "seededSample" to true
            ),
            "sample_padel_kemang" to mapOf(
                "organizerId" to "community_padel_club",
                "organizerName" to "Southside Padel Social",
                "organizerAvatarKey" to "img_activity_padel",
                "organizerVerified" to true,
                "title" to "Sunset Padel Americano Social Match",
                "description" to "Main padel format Americano seru 2 jam penuh dengan rotasi partner otomatis biar makin banyak kenalan baru! Cocok untuk level beginner-upper sampai intermediate.",
                "category" to "Olahraga",
                "categoryEmoji" to "⚽",
                "city" to "Jakarta Selatan",
                "locationName" to "Bintaro & Kemang Padel Arena",
                "latitude" to -6.2712,
                "longitude" to 106.7984,
                "eventDateMillis" to (nowMillis + dayMs * 4),
                "timeLabel" to "17:00 WIB",
                "priceIdr" to 185000L,
                "quota" to 16L,
                "joinedCount" to 13L,
                "favoriteCount" to 95L,
                "viralScore" to 920L,
                "photoKeys" to listOf("img_activity_padel", "img_activity_hyrox"),
                "benefits" to listOf(
                    "2 jam sewa 4 lapangan outdoor premium",
                    "Bola padel baru & skor Americano live",
                    "Pinjaman raket gratis untuk pemula",
                    "Minuman isotonik dingin & snack ringan"
                ),
                "joinedUserNames" to listOf("Alvin Chandra", "Clara Wijaya", "Hendra Gunawan", "Felicia Tan"),
                "status" to "PUBLISHED",
                "averageRating" to 4.8,
                "reviewCount" to 21L,
                "seededSample" to true
            ),
            "sample_photowalk_blokm" to mapOf(
                "organizerId" to "community_lensa_kopi",
                "organizerName" to "Lensa & Kopi Collective",
                "organizerAvatarKey" to "img_activity_photowalk",
                "organizerVerified" to true,
                "title" to "Blok M Street Photowalk & Slow Bar Coffee",
                "description" to "Hunting foto street photography sore hari di lorong ikonik Blok M dan Melawai menggunakan kamera mirrorless, analog, maupun smartphone, ditutup sesi kurasi foto & ngopi bareng.",
                "category" to "Fotografi",
                "categoryEmoji" to "📸",
                "city" to "Jakarta Selatan",
                "locationName" to "M Bloc & Blok M, Jakarta",
                "latitude" to -6.2442,
                "longitude" to 106.7981,
                "eventDateMillis" to (nowMillis + dayMs * 5),
                "timeLabel" to "15:30 WIB",
                "priceIdr" to 65000L,
                "quota" to 25L,
                "joinedCount" to 19L,
                "favoriteCount" to 88L,
                "viralScore" to 875L,
                "photoKeys" to listOf("img_activity_photowalk", "img_activity_running"),
                "benefits" to listOf(
                    "1 gelas manual brew / iced latte pilihan",
                    "Mentoring komposisi street photography",
                    "Sesi photo sharing & feedback santai",
                    "Preset warna eksklusif komunitas"
                ),
                "joinedUserNames" to listOf("Bima Sakti", "Dara Puspita", "Fikri Hakim", "Larasati"),
                "status" to "PUBLISHED",
                "averageRating" to 4.9,
                "reviewCount" to 15L,
                "seededSample" to true
            ),
            "sample_esport_scbd" to mapOf(
                "organizerId" to "community_arena_esport",
                "organizerName" to "Nusantara E-Sport Hub",
                "organizerAvatarKey" to "img_activity_hyrox",
                "organizerVerified" to true,
                "title" to "Community E-Sport Sparring Night & Watch Party",
                "description" to "Mabar dan mini-tournament komunitas Valorant & Mobile Legends offline di gaming lounge SCBD. Bawa tim sendiri atau gabung tim acak di lokasi untuk bertemu teman mabar baru!",
                "category" to "Esport",
                "categoryEmoji" to "🎮",
                "city" to "Jakarta Selatan",
                "locationName" to "SCBD Gaming Lounge, Jakarta",
                "latitude" to -6.2253,
                "longitude" to 106.8099,
                "eventDateMillis" to (nowMillis + dayMs * 6),
                "timeLabel" to "19:00 WIB",
                "priceIdr" to 50000L,
                "quota" to 40L,
                "joinedCount" to 29L,
                "favoriteCount" to 112L,
                "viralScore" to 905L,
                "photoKeys" to listOf("img_activity_hyrox", "img_activity_padel"),
                "benefits" to listOf(
                    "Akses PC/area mabar berkecepatan tinggi",
                    "Doorprize gaming gear & voucher",
                    "Snack bar & minuman sepuasnya",
                    "Koneksi langsung dengan komunitas gamer Jakarta"
                ),
                "joinedUserNames" to listOf("Reza Mahendra", "Yusuf Akmal", "Gaby Lim", "Farhan"),
                "status" to "PUBLISHED",
                "averageRating" to 4.7,
                "reviewCount" to 11L,
                "seededSample" to true
            ),
            "sample_coffee_bdg" to mapOf(
                "organizerId" to "community_braga_brew",
                "organizerName" to "Bandung Coffee Circle",
                "organizerAvatarKey" to "img_activity_photowalk",
                "organizerVerified" to true,
                "title" to "Manual Brew Cupping & Komunitas Kopi Braga",
                "description" to "Kenali karakter biji kopi single origin Jawa Barat bersama roaster lokal. Sesi cupping interaktif, diskusi profil rasa, dan jejaring santai antar penikmat kopi.",
                "category" to "Kopi",
                "categoryEmoji" to "☕",
                "city" to "Bandung",
                "locationName" to "Jalan Braga, Bandung",
                "latitude" to -6.9178,
                "longitude" to 107.6095,
                "eventDateMillis" to (nowMillis + dayMs * 7),
                "timeLabel" to "10:00 WIB",
                "priceIdr" to 0L,
                "quota" to 30L,
                "joinedCount" to 22L,
                "favoriteCount" to 74L,
                "viralScore" to 840L,
                "photoKeys" to listOf("img_activity_photowalk", "img_activity_padel"),
                "benefits" to listOf(
                    "Tasting 5 single origin pilihan roastery",
                    "Panduan teknik seduh V60 untuk pemula",
                    "Jejaring komunitas kreatif Bandung"
                ),
                "joinedUserNames" to listOf("Galih Permana", "Nisa Aulia", "Tegar Prasetyo"),
                "status" to "PUBLISHED",
                "averageRating" to 4.8,
                "reviewCount" to 9L,
                "seededSample" to true
            )
        )
    }

    @Suppress("UNCHECKED_CAST")
    fun buildShowcaseActivityItems(nowMillis: Long = System.currentTimeMillis()): List<ActivityItem> =
        buildShowcaseSeedActivities(nowMillis).map { (docId, map) ->
            ActivityItem(
                id = docId,
                organizerId = map["organizerId"] as? String ?: "",
                organizerName = map["organizerName"] as? String ?: "",
                organizerAvatarKey = map["organizerAvatarKey"] as? String ?: "",
                organizerVerified = map["organizerVerified"] as? Boolean ?: false,
                title = map["title"] as? String ?: "",
                description = map["description"] as? String ?: "",
                category = map["category"] as? String ?: "",
                categoryEmoji = map["categoryEmoji"] as? String ?: "",
                city = map["city"] as? String ?: "",
                locationName = map["locationName"] as? String ?: "",
                latitude = (map["latitude"] as? Number)?.toDouble() ?: -6.2615,
                longitude = (map["longitude"] as? Number)?.toDouble() ?: 106.8106,
                eventDateMillis = (map["eventDateMillis"] as? Number)?.toLong() ?: nowMillis,
                timeLabel = map["timeLabel"] as? String ?: "",
                priceIdr = (map["priceIdr"] as? Number)?.toLong() ?: 0L,
                quota = (map["quota"] as? Number)?.toLong() ?: 20L,
                joinedCount = (map["joinedCount"] as? Number)?.toLong() ?: 0L,
                favoriteCount = (map["favoriteCount"] as? Number)?.toLong() ?: 0L,
                viralScore = (map["viralScore"] as? Number)?.toLong() ?: 0L,
                photoKeys = (map["photoKeys"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                benefits = (map["benefits"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                joinedUserNames = (map["joinedUserNames"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                status = map["status"] as? String ?: "PUBLISHED",
                averageRating = (map["averageRating"] as? Number)?.toDouble() ?: 0.0,
                reviewCount = (map["reviewCount"] as? Number)?.toLong() ?: 0L,
                seededSample = map["seededSample"] as? Boolean ?: true
            )
        }
}
