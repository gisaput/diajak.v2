package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * ==============================================================================
 * ATURAN BAKU DESIGN SYSTEM DIAJAK (Single Source of Truth — Wajib Dipatuhi)
 * ==============================================================================
 * Seluruh halaman & komponen (termasuk fitur baru di masa depan) WAJIB mematuhi
 * 5 pilar desain berikut + token di file ini + MaterialTheme.colorScheme:
 *
 * 1. HIERARKI PERMUKAAN (100% Flat — Tanpa Border & Tanpa Bayangan):
 *    - Latar layar & modal            : PureWhite (#FFFFFF) (`background` / `surface`).
 *    - Permukaan netral tunggal       : DiajakSurfaceGray (#F2F2F2) (`surfaceContainer`)
 *                                       untuk kartu, search bar, input form, chip nonaktif,
 *                                       tombol sekunder di atas putih, & pembungkus ikon di atas putih.
 *    - Lapisan di dalam kartu abu-abu : PureWhite (#FFFFFF) (`surfaceContainerHigh`)
 *                                       agar selalu ada kontras bersih antar lapisan.
 *
 * 2. WARNA BRAND SELEKTIF (`primary` = DiajakBrandOrange #FF5A36):
 *    - Hanya dipakai untuk CTA utama (Ikuti, Jelajahi Beranda, Terbitkan E-Tiket,
 *      Masuk dengan Google, FAB +), tab/chip aktif, ikon & label Bottom Nav aktif,
 *      badge jumlah peserta (35+/89+), bubble chat terkirim, & kartu sorotan penyelenggara.
 *    - DILARANG dipakai untuk teks isi/judul, QR code, atau pembungkus/bulatan ikon.
 *
 * 3. ATURAN KONTRAS PEMBUNGKUS BULATAN IKON (Wajib Konsisten):
 *    - Ikon berdiri sendiri di atas latar putih (#FFFFFF)
 *      -> WAJIB dibungkus bulatan abu-abu #F2F2F2 (`surfaceContainer`, 48.dp `ActionCircle`)
 *         + ikon JetBlack (#111111, 24.dp `Primary`), identik dengan tombol Lonceng Beranda.
 *         Gunakan `DiajakBackButton` untuk seluruh tombol Back di TopAppBar / header putih,
 *         dan `DiajakCircleIconButton(nested = false)` untuk aksi bulat lainnya di atas putih.
 *    - Ikon berdiri sendiri di atas kartu abu-abu (#F2F2F2) atau kartu brand (#FF5A36)
 *      -> WAJIB dibungkus bulatan putih #FFFFFF (`surfaceContainerHigh`) + ikon JetBlack (#111111)
 *         (contoh: Lihat QR, Empty State, Chat penyelenggara, ikon grup Pesan, menu Profil, tile Organizer).
 *    - Ikon di atas foto aktivitas
 *      -> WAJIB dibungkus bulatan putih #FFFFFF (`DiajakPhotoOverlay.ChipSurface`) + ikon JetBlack (#111111).
 *    - Pengecualian resmi: Bottom Navigation Bar, ikon di dalam tombol penuh/pil/FAB,
 *      dan ikon di dalam banner status berwarna semantik.
 *
 * 4. 3 TINGKAT UKURAN IKON BAKU ([DiajakIconSize]):
 *    - Hero    (32.dp) : Empty State, Sukses Booking, area unggah dokumen.
 *    - Primary (24.dp) : Semua ikon navigasi, tombol Back, tombol aksi bulat (48.dp),
 *                        pencarian, kirim pesan, menu, & FAB.
 *    - Inline  (18.dp) : Semua ikon kecil pendamping teks TERMASUK emoji kategori
 *                        (dibungkus kotak optik 18.dp agar sejajar dengan ikon vektor).
 *
 * 5. ATURAN WARNA IKON & TIPOGRAFI:
 *    - Ikon besar (Hero 32.dp & Primary 24.dp): selalu JetBlack (#111111), kecuali
 *      Bottom Nav (aktif #FF5A36, nonaktif #6E6E73), ikon di atas tombol #FF5A36 (#FFFFFF),
 *      dan Love aktif (#E11D48).
 *    - Ikon kecil (Inline 18.dp):
 *      * Struktural/utilitas (lokasi, waktu, QR, grid, ulasan, centang benefit, +/-): JetBlack (#111111).
 *      * Semantik/identitas: warna asli (emoji kategori penuh warna, StarGold #F59E0B,
 *        VerifiedBlue #1D9BF0, HeartRed #E11D48).
 *    - Tipografi ([DiajakTypography]): dilarang menimpa `.copy(fontSize = ...)` sembarangan;
 *      judul AlertDialog wajib `MaterialTheme.typography.titleLarge`.
 *    - Radius ([DiajakRadius]): Small 12.dp, Control 14.dp, Card 20.dp, Hero 24.dp, Pill 50%.
 *    - Jarak ([DiajakSpacing]): margin layar 20.dp, antar seksi 16.dp, tinggi CTA 50.dp.
 *
 * 6. ATURAN MUTLAK & PERMANEN HEADER HALAMAN (`DiajakPageScaffold`, `DiajakPageHeader`, & [DiajakHeaderSpec]):
 *    - WAJIB dipatuhi untuk SELURUH halaman saat ini maupun halaman/fitur baru di masa depan
 *      (pengecualian tata letak hanya 2: `HomeScreen` Beranda & `ActivityDetailScreen` Detail Aktivitas,
 *       namun KEDUANYA tetap wajib memakai dimensi & koordinat bulatan tombol `DiajakHeaderSpec` yang 100% sama):
 *      1) WAJIB membungkus layar standar dengan `DiajakPageScaffold(...)` agar header, jarak konten,
 *         dan `WindowInsets` terkunci otomatis tanpa bisa melenceng.
 *      2) Judul halaman WAJIB 1 baris saja (dilarang subtitle / 2 baris), rata tengah (`Center`)
 *         horizontal & vertikal tepat di titik tengah bulatan tombol `48.dp` (gaya Instagram),
 *         dengan ukuran font baku terkunci **`22.sp` Bold** (`DiajakHeaderSpec.TitleFontSize`,
 *         `lineHeight = 25.sp`, `letterSpacing = -0.25.sp`) — **100% sama dengan teks "Halo Username" di Beranda**.
 *      3) Koordinat pusat `(X, Y)` judul WAJIB 100% identik baik pada halaman bertombol Back
 *         maupun halaman tanpa tombol Back (`statusBar + 14.dp + 24.dp = statusBar + 38.dp`).
 *      4) Ukuran & posisi bulatan tombol header (Lonceng Beranda, Back & Love Detail Aktivitas,
 *         serta tombol Back / aksi kanan di seluruh halaman lain) WAJIB 100% identik:
 *         diameter **`48.dp`** (`DiajakHeaderSpec.ButtonSize`), `top = 14.dp` di bawah status bar,
 *         margin kiri/kanan **`20.dp`** (`DiajakSpacing.ScreenHorizontal`) agar transisi antar halaman presisi.
 *      5) Di Beranda (`HomeScreen`), kolom 2 baris ("Halo Username" + "Mau diajak kemana?") wajib
 *         memakai `.fillMaxHeight()` + `Arrangement.SpaceBetween` di dalam slot `48.dp` agar sisi atas
 *         "Halo Username" sejajar atas bulatan lonceng dan sisi bawah "Mau diajak kemana?" sejajar lurus
 *         dengan sisi bawah bulatan lonceng tepat di atas kolom pencarian.
 *      6) Jarak header-ke-konten WAJIB konsisten `16.dp` dari sisi bawah slot `48.dp` ke elemen
 *         pertama di bawahnya (`PaddingBottom 12.dp + ContentTopGap 4.dp = 16.dp`, identik dengan
 *         jarak bawah bulatan lonceng ke kolom pencarian di Beranda) termasuk `EmptyStatePanel`.
 *      7) Perilaku scroll header (`pinnedHeader = false` secara baku — Opsi A / ikut konten murni):
 *         di seluruh halaman standar (selain Beranda & Detail Aktivitas), header TIDAK menempel,
 *         ikut naik & tenggelam ke atas bersama konten saat discroll, dan baru muncul kembali saat
 *         konten kembali ke posisi paling atas (dengan area status bar tetap putih solid).
 *         Pengecualian header tetap menempel (`pinnedHeader = true`) HANYA berlaku untuk
 *         Ruang Chat (`ChatRoomScreen`).
 *    - DILARANG KERAS memakai `TopAppBar`, `CenterAlignedTopAppBar`, `MediumTopAppBar`, atau
 *      `LargeTopAppBar` Material 3 di layar mana pun. Aturan ini dijaga oleh `HeaderStandardGuardTest`.
 */
object DiajakIconSize {
    val Hero = 32.dp
    val Primary = 24.dp
    val Inline = 18.dp
}

object DiajakRadius {
    val Small = RoundedCornerShape(12.dp)
    val Control = RoundedCornerShape(14.dp)
    val Card = RoundedCornerShape(20.dp)
    val Hero = RoundedCornerShape(24.dp)
    val Pill = RoundedCornerShape(50)
}

object DiajakSpacing {
    val ScreenHorizontal = 20.dp
    val SectionGap = 16.dp
    val CardPadding = 16.dp
    val ItemGap = 12.dp
    val ChipGap = 10.dp
    val SmallGap = 8.dp
}

object DiajakComponentSize {
    val PrimaryButtonHeight = 50.dp
    val ActionCircle = 48.dp
    val IconCircle = 44.dp
}

object DiajakHeaderSpec {
    val PaddingTop = 14.dp
    val PaddingBottom = 12.dp
    val ContentHeight = DiajakComponentSize.ActionCircle
    val ButtonSize = DiajakComponentSize.ActionCircle
    val ContentTopGap = 4.dp
    val VisualHeaderToContentGap = PaddingBottom + ContentTopGap // 16.dp (patokan Beranda)
    val TitleFontSize = 22.sp
    val TitleLineHeight = 25.sp
    val TitleLetterSpacing = (-0.25).sp
}

object DiajakPhotoOverlay {
    val Scrim = JetBlack
    val ChipSurface = PureWhite
    val OnChipSurface = JetBlack
    val OnPhotoText = PureWhite
    val QrBackground = PureWhite
    val QrModule = JetBlack
}

/**
 * DiajakTheme — Brand Coral (#FF5A36) + Single Surface Gray (#F2F2F2)
 *
 * Pemetaan token Material 3 yang dipatenkan:
 * - background / surface            -> PureWhite (#FFFFFF)
 * - surfaceContainerLow / Container -> DiajakSurfaceGray (#F2F2F2) untuk semua elemen di atas putih
 * - surfaceContainerHigh / Highest  -> PureWhite (#FFFFFF) untuk elemen di dalam kontainer #F2F2F2
 * - primary                         -> DiajakBrandOrange (#FF5A36) dengan onPrimary PureWhite (#FFFFFF)
 * - onBackground / onSurface        -> JetBlack (#111111) untuk teks & ikon utama
 * - outline / outlineVariant        -> DiajakSurfaceGray (#F2F2F2) hanya untuk garis pemisah struktural tipis
 */
private val DiajakLightColorScheme = lightColorScheme(
    primary = DiajakBrandOrange,
    onPrimary = PureWhite,
    primaryContainer = DiajakSurfaceGray,
    onPrimaryContainer = JetBlack,
    secondary = CharcoalDark,
    onSecondary = PureWhite,
    secondaryContainer = PureWhite,
    onSecondaryContainer = JetBlack,
    tertiary = MutedTextGray,
    onTertiary = PureWhite,
    background = PureWhite,
    onBackground = JetBlack,
    surface = PureWhite,
    onSurface = JetBlack,
    surfaceVariant = DiajakSurfaceGray,
    onSurfaceVariant = MutedTextGray,
    surfaceContainerLowest = PureWhite,
    surfaceContainerLow = DiajakSurfaceGray,
    surfaceContainer = DiajakSurfaceGray,
    surfaceContainerHigh = PureWhite,
    surfaceContainerHighest = PureWhite,
    outline = DiajakSurfaceGray,
    outlineVariant = DiajakSurfaceGray,
    error = ErrorRed,
    onError = PureWhite,
    errorContainer = ErrorRedSurface,
    onErrorContainer = ErrorRed
)

private val DiajakShapes = Shapes(
    extraSmall = DiajakRadius.Small,
    small = DiajakRadius.Small,
    medium = DiajakRadius.Control,
    large = DiajakRadius.Card,
    extraLarge = DiajakRadius.Hero
)

@Composable
fun DiajakTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DiajakLightColorScheme,
        typography = DiajakTypography,
        shapes = DiajakShapes,
        content = content
    )
}
