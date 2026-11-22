# Aturan Baku Design System — Diajak (`diajak.com`)

Dokumen ini adalah panduan desain resmi dan permanen untuk seluruh halaman, komponen, dan fitur baru di aplikasi Android **Diajak**. Setiap perubahan UI wajib mematuhi seluruh aturan di bawah ini agar tampilan tetap konsisten di semua layar.

---

## 1. Hierarki Permukaan (100% Flat — Tanpa Border & Tanpa Bayangan)
- **Latar layar, header, & modal:** Putih murni (`PureWhite` `#FFFFFF` — `MaterialTheme.colorScheme.background` / `surface`).
- **Permukaan netral tunggal:** Abu-abu lembut (`DiajakSurfaceGray` `#E5E5EA` — `MaterialTheme.colorScheme.surfaceContainer`) untuk kartu, search bar, input form, chip nonaktif, tombol sekunder di atas latar putih, dan pembungkus ikon di atas latar putih.
- **Lapisan di dalam kartu abu-abu (`nested = true`):** Putih murni (`PureWhite` `#FFFFFF` — `MaterialTheme.colorScheme.surfaceContainerHigh`) agar selalu ada kontras bersih antar lapisan.
- **Elevasi & border:** `tonalElevation = 0.dp`, `shadowElevation = 0.dp`, tanpa garis tepi dekoratif.

---

## 2. Warna Brand Selektif (`DiajakBrandOrange` `#FF5A36`)
- **Wajib dipakai untuk:**
  - CTA utama (*Ikuti*, *Jelajahi Beranda*, *Terbitkan E-Tiket*, *Masuk dengan Google*, FAB `+`).
  - Tab, filter, dan chip yang sedang aktif (*Semua*, tab Pesan, pilihan aktif).
  - Ikon dan label aktif di Bottom Navigation Bar.
  - Badge jumlah peserta (`35+` / `89+`).
  - Bubble chat terkirim milik pengguna (`isMine`).
  - Kartu sorotan penyelenggara (*Halaman Penyelenggara*, kartu Saldo).
- **DILARANG dipakai untuk:**
  - Teks judul/isi (tetap `JetBlack` `#111111` atau `MutedTextGray` `#6E6E73`).
  - QR Code atau scrim foto.
  - **Pembungkus / bulatan ikon** (baik di atas latar putih maupun di dalam kartu abu-abu).

---

## 3. Aturan Kontras Pembungkus Bulatan Ikon
Setiap ikon yang berdiri sendiri wajib mengikuti aturan kontras permukaan berikut:

| Lokasi Ikon | Warna Pembungkus Bulatan | Ukuran Bulatan | Warna & Ukuran Ikon | Komponen Baku |
| :--- | :--- | :--- | :--- | :--- |
| **Di atas latar / header putih (`#FFFFFF`)** | Abu-abu `#E5E5EA` (`surfaceContainer`) | `48.dp` (`ActionCircle`) / `64.dp` (Hero) | Hitam `#111111` (`24.dp` / `32.dp`) | `DiajakBackButton`, `DiajakCircleIconButton(nested = false)` |
| **Di dalam kartu abu-abu (`#E5E5EA`) atau kartu brand (`#FF5A36`)** | Putih `#FFFFFF` (`surfaceContainerHigh`) | `48.dp` (`ActionCircle`) / `44.dp` (`IconCircle`) | Hitam `#111111` (`24.dp` / `32.dp`) | `DiajakCircleIconButton(nested = true)` |
| **Di atas foto aktivitas** | Putih `#FFFFFF` (`DiajakPhotoOverlay.ChipSurface`) | `44.dp` (`IconCircle`) | Hitam `#111111` (`24.dp`, atau `#E11D48` saat favorit aktif) | Overlay foto |

**Pengecualian resmi (tidak dibungkus bulatan):**
- **Bottom Navigation Bar** (Beranda, Favorit, Tiket, Pesan, Profil): ikon bar standar (aktif `#FF5A36`, nonaktif `#6E6E73`).
- **Ikon di dalam tombol penuh / pil / FAB**: sudah terbungkus oleh tombolnya sendiri.
- **Ikon di dalam banner status berwarna** (kuning `WarningAmberSurface` / hijau `VerifiedGreenSurface`).

---

## 4. 3 Tingkat Ukuran Ikon Baku (`DiajakIconSize`)
Tidak boleh ada ukuran ikon acak di luar 3 tingkat berikut:
1. **`Hero` (`32.dp`):** Empty state, sukses booking E-Tiket, area unggah dokumen.
2. **`Primary` (`24.dp`):** Seluruh ikon navigasi, tombol Back, tombol aksi bulat (`48.dp`), pencarian, kirim pesan, menu, dan FAB.
3. **`Inline` (`18.dp`):** Seluruh ikon kecil pendamping teks **termasuk emoji kategori** (emoji dibungkus kotak optik `18.dp` agar sejajar sempurna dengan ikon vektor seperti *69 Kategori*).

---

## 5. Aturan Warna Ikon
- **Ikon Besar (`Hero 32.dp` & `Primary 24.dp`):** Selalu **hitam (`JetBlack` `#111111`)**, kecuali:
  - Ikon di atas tombol brand `#FF5A36` → **Putih (`#FFFFFF`)**.
  - Bottom Navigation Bar → aktif `#FF5A36`, nonaktif `#6E6E73`.
  - Ikon Love aktif → **Merah Hati (`HeartRed` `#E11D48`)**.
- **Ikon Kecil (`Inline 18.dp`):**
  - **Struktural / utilitas** (lokasi, waktu, QR, grid, ulasan, centang benefit, stepper `+/-`) → **Hitam (`#111111`)**.
  - **Semantik / identitas** → **Warna aslinya**:
    - Emoji kategori penuh warna
    - Bintang rating: **Emas (`StarGold` `#F59E0B`)**
    - Centang verifikasi: **Biru (`VerifiedBlue` `#1D9BF0`)**
    - Hati favorit: **Merah (`HeartRed` `#E11D48`)**
    - Mutasi saldo: Hijau (`VerifiedGreen`) / Merah (`ErrorRed`)

---

## 6. Token Tata Letak & Tipografi
- **Radius (`DiajakRadius`):** `Small = 12.dp`, `Control = 14.dp`, `Card = 20.dp`, `Hero = 24.dp`, `Pill = 50%`.
- **Spasi (`DiajakSpacing`):** Margin horizontal layar `20.dp`, antar seksi `16.dp`, padding kartu `16.dp`, antar item `12.dp`, antar chip `10.dp`, jarak kecil `8.dp`.
- **Ukuran Sentuh (`DiajakComponentSize`):** Tombol penuh `50.dp`, bulatan aksi utama `48.dp` (`ActionCircle`), bulatan ikon sekunder/overlay `44.dp` (`IconCircle`).
- **Tipografi (`DiajakTypography`):** Gunakan skala `MaterialTheme.typography` tanpa menimpa `fontSize` secara ad-hoc; seluruh judul `TopAppBar` & `AlertDialog` wajib `titleLarge`.
