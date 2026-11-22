package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

/**
 * ==============================================================================
 * ATURAN BAKU DESIGN SYSTEM DIAJAK (Single Source of Truth — Wajib Dipatuhi)
 * ==============================================================================
 * Seluruh halaman & komponen (termasuk fitur baru di masa depan) WAJIB mematuhi
 * 5 pilar desain berikut + token di file ini + MaterialTheme.colorScheme:
 *
 * 1. HIERARKI PERMUKAAN (100% Flat — Tanpa Border & Tanpa Bayangan):
 *    - Latar layar & modal            : PureWhite (#FFFFFF) (`background` / `surface`).
 *    - Permukaan netral tunggal       : DiajakSurfaceGray (#E5E5EA) (`surfaceContainer`)
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
 *      -> WAJIB dibungkus bulatan abu-abu #E5E5EA (`surfaceContainer`, 48.dp `ActionCircle`)
 *         + ikon JetBlack (#111111, 24.dp `Primary`), identik dengan tombol Lonceng Beranda.
 *         Gunakan `DiajakBackButton` untuk seluruh tombol Back di TopAppBar / header putih,
 *         dan `DiajakCircleIconButton(nested = false)` untuk aksi bulat lainnya di atas putih.
 *    - Ikon berdiri sendiri di atas kartu abu-abu (#E5E5EA) atau kartu brand (#FF5A36)
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
 *      judul TopAppBar & AlertDialog wajib `MaterialTheme.typography.titleLarge`.
 *    - Radius ([DiajakRadius]): Small 12.dp, Control 14.dp, Card 20.dp, Hero 24.dp, Pill 50%.
 *    - Jarak ([DiajakSpacing]): margin layar 20.dp, antar seksi 16.dp, tinggi CTA 50.dp.
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

object DiajakPhotoOverlay {
    val Scrim = JetBlack
    val ChipSurface = PureWhite
    val OnChipSurface = JetBlack
    val OnPhotoText = PureWhite
    val QrBackground = PureWhite
    val QrModule = JetBlack
}

/**
 * DiajakTheme — Brand Coral (#FF5A36) + Single Surface Gray (#E5E5EA)
 *
 * Pemetaan token Material 3 yang dipatenkan:
 * - background / surface            -> PureWhite (#FFFFFF)
 * - surfaceContainerLow / Container -> DiajakSurfaceGray (#E5E5EA) untuk semua elemen di atas putih
 * - surfaceContainerHigh / Highest  -> PureWhite (#FFFFFF) untuk elemen di dalam kontainer #E5E5EA
 * - primary                         -> DiajakBrandOrange (#FF5A36) dengan onPrimary PureWhite (#FFFFFF)
 * - onBackground / onSurface        -> JetBlack (#111111) untuk teks & ikon utama
 * - outline / outlineVariant        -> DiajakSurfaceGray (#E5E5EA) hanya untuk garis pemisah struktural tipis
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
