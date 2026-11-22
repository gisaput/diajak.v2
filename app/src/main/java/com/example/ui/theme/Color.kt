package com.example.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * PEDOMAN RESMI DESAIN DIAJAK (Brand Coral #FF5A36 + Single Surface Gray #E5E5EA)
 * -------------------------------------------------------------------------------
 * 1. Background Layar & Sheet : PureWhite (#FFFFFF)
 * 2. Satu-Satunya Abu-Abu Permukaan : DiajakSurfaceGray (#E5E5EA) — dipakai untuk
 *    SEMUA kartu, kontainer, kolom pencarian, text field, chip tidak aktif,
 *    tombol sekunder, pembungkus ikon di atas putih, segmented control track, & divider.
 * 3. Elemen di Dalam Kartu (#E5E5EA) : PureWhite (#FFFFFF) via `surfaceContainerHigh`
 *    untuk pembungkus ikon bulat/tombol sekunder di dalam kartu abu-abu (mis. "Lihat QR",
 *    ikon Empty State, tombol Chat penyelenggara).
 * 4. Warna Brand / Elemen Aktif / CTA Utama : DiajakBrandOrange (#FF5A36) dengan teks PureWhite (#FFFFFF).
 *    DILARANG dipakai untuk pembungkus/bulatan ikon.
 * 5. Teks Utama & Ikon Besar / Struktural : JetBlack (#111111).
 * 6. Teks Sekunder : MutedTextGray (#6E6E73).
 * 7. Ikon Kecil Semantik : StarGold (#F59E0B), VerifiedBlue (#1D9BF0), HeartRed (#E11D48),
 *    serta emoji kategori berwarna asli.
 */
val PureWhite = Color(0xFFFFFFFF)
val DiajakSurfaceGray = Color(0xFFE5E5EA)

val DiajakBrandOrange = Color(0xFFFF5A36)
val JetBlack = Color(0xFF111111)
val CharcoalDark = Color(0xFF222222)
val MutedTextGray = Color(0xFF6E6E73)

// Semantic functional icon & status colors
val HeartRed = Color(0xFFE11D48)
val StarGold = Color(0xFFF59E0B)
val VerifiedBlue = Color(0xFF1D9BF0)
val VerifiedGreen = Color(0xFF166534)
val VerifiedGreenSurface = Color(0xFFECFDF3)
val WarningAmber = Color(0xFFB45309)
val WarningAmberSurface = Color(0xFFFFFBEB)
val ErrorRed = Color(0xFFB91C1C)
val ErrorRedSurface = Color(0xFFFEF2F2)
