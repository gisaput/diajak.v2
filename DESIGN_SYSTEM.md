# Aturan Baku Design System — Diajak (`diajak.com`)

Dokumen ini adalah panduan desain resmi dan permanen untuk seluruh halaman, komponen, dan fitur baru di aplikasi Android **Diajak**. Setiap perubahan UI wajib mematuhi seluruh aturan di bawah ini agar tampilan tetap konsisten di semua layar.

---

## 1. Hierarki Permukaan (100% Flat — Tanpa Border & Tanpa Bayangan)
- **Latar layar, header, & modal:** Putih murni (`PureWhite` `#FFFFFF` — `MaterialTheme.colorScheme.background` / `surface`).
- **Permukaan netral tunggal:** Abu-abu lembut (`DiajakSurfaceGray` `#F2F2F2` — `MaterialTheme.colorScheme.surfaceContainer`) untuk kartu, search bar, input form, chip nonaktif, tombol sekunder di atas latar putih, dan pembungkus ikon di atas latar putih.
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
| **Di atas latar / header putih (`#FFFFFF`)** | Abu-abu `#F2F2F2` (`surfaceContainer`) | `48.dp` (`ActionCircle` / `DiajakHeaderSpec.ButtonSize`) / `64.dp` (Hero) | Hitam `#111111` (`24.dp` / `32.dp`) | `DiajakBackButton`, `DiajakCircleIconButton(nested = false)` |
| **Di dalam kartu abu-abu (`#F2F2F2`) atau kartu brand (`#FF5A36`)** | Putih `#FFFFFF` (`surfaceContainerHigh`) | `48.dp` (`ActionCircle`) / `44.dp` (`IconCircle` dekoratif) | Hitam `#111111` (`24.dp` / `32.dp`) | `DiajakCircleIconButton(nested = true)` |
| **Di atas foto aktivitas (Header Detail & Kartu Feed)** | Putih `#FFFFFF` (`DiajakPhotoOverlay.ChipSurface`) | **`48.dp`** (`ActionCircle` / `DiajakHeaderSpec.ButtonSize`) | Hitam `#111111` (`24.dp`, atau `#E11D48` saat favorit aktif) | Overlay foto & Header Detail |

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
- **Ukuran Sentuh (`DiajakComponentSize`):** Tombol penuh `50.dp`, bulatan tombol interaktif & header **`48.dp`** (`ActionCircle` / `DiajakHeaderSpec.ButtonSize`, memenuhi standar aksesibilitas Android M3 `48.dp`), bulatan ikon dekoratif sekunder di dalam kartu menu `44.dp` (`IconCircle`).
- **Tipografi (`DiajakTypography`):** Gunakan skala `MaterialTheme.typography` tanpa menimpa `fontSize` secara ad-hoc; judul `AlertDialog` wajib `titleLarge`, sedangkan seluruh judul header halaman wajib memakai `DiajakPageHeader` (**`22.sp` Bold**, 1 baris, Center — identik dengan teks `"Halo Username"` di Beranda).

---

## 7. Aturan Mutlak & Permanen Header Halaman (`DiajakPageScaffold`, `DiajakPageHeader`, & `DiajakHeaderSpec`)
Seluruh halaman di aplikasi Diajak — termasuk **seluruh halaman baru maupun perubahan di masa depan** — **WAJIB** mematuhi aturan header tunggal berikut agar posisi bulatan tombol, ukuran judul, letak judul, dan jarak ke konten selalu 100% presisi saat berpindah antar layar:

- **Wadah Wajib untuk Setiap Halaman Standar & Halaman Baru:**
  - Panggil `DiajakPageScaffold(title = "...", onBack = ..., ...) { contentPadding -> ... }` lalu teruskan `contentPadding` langsung ke `LazyColumn(contentPadding = contentPadding)` atau ke elemen konten pertama (termasuk `EmptyStatePanel(outerPadding = contentPadding)`).
  - Untuk `EmptyStatePanel` yang berada **di dalam** `LazyColumn` yang sudah memakai `contentPadding`, wajib set `outerPadding = PaddingValues(0.dp)` agar tidak terjadi padding ganda.
  - Untuk layar tab utama yang sudah berada di dalam `DiajakMainShell` (memiliki Bottom Navigation Bar), set `applyNavigationBarBottomPadding = false`.
- **Dimensi & Koordinat Bulatan Tombol Header Terkunci (100% Identik di SEMUA Halaman — Beranda, Detail Aktivitas, & Seluruh Halaman Lain):**
  - Inset atas: `statusBarsPadding()` (dengan `Scaffold(contentWindowInsets = WindowInsets(0, 0, 0, 0))` otomatis oleh `DiajakPageScaffold`).
  - Padding layar: kiri-kanan **`20.dp`** (`DiajakSpacing.ScreenHorizontal`), atas **`14.dp`** (`DiajakHeaderSpec.PaddingTop`), bawah **`12.dp`** (`DiajakHeaderSpec.PaddingBottom`).
  - Diameter semua bulatan tombol header (Lonceng Beranda, Back & Love di Detail Aktivitas, serta Back & aksi kanan di seluruh halaman lain): **`48.dp`** (`DiajakHeaderSpec.ButtonSize` = `DiajakHeaderSpec.ContentHeight` = `DiajakComponentSize.ActionCircle`), sehingga **total tinggi header selalu `74.dp`** di bawah status bar.
  - Koordinat tombol bulat terkunci di seluruh layar:
    - Tombol kiri (Back): `x = 20.dp`, `y = statusBar + 14.dp`, ukuran `48.dp × 48.dp`.
    - Tombol kanan (Lonceng Beranda / Love Detail / aksi kanan): `x = width - 68.dp` (`end = 20.dp`), `y = statusBar + 14.dp`, ukuran `48.dp × 48.dp`.
  - Jarak konten pertama di bawah header: **`4.dp`** (`DiajakHeaderSpec.ContentTopGap`), menghasilkan jarak visual seragam **`16.dp`** (`DiajakHeaderSpec.VisualHeaderToContentGap = 12.dp + 4.dp`) dari tepi bawah slot header `48.dp` ke elemen pertama di setiap layar (identik dengan jarak bawah bulatan Lonceng ke kolom pencarian di Beranda).
  - Tanpa garis pemisah (`HorizontalDivider`) di bawah header; latar putih murni `#FFFFFF`.
- **Format & Perataan Judul (Gaya Instagram — Ukuran Font Disamakan dengan `"Halo Username"`):**
  - **Semua halaman selain Beranda & Detail Aktivitas** (baik tab utama tanpa tombol Back seperti *Favorit, Tiket, Pesan, Profil* maupun sub-halaman bertombol Back seperti *Konfirmasi Booking, E-Tiket Diterbitkan, Ruang Chat, Portal Penyelenggara, Kelola Aktivitas, Buat/Edit Aktivitas, Kelola Pendaftaran, Kelola Pesan, Kelola Saldo, Kelola Ulasan, Kelola Dokumen*, serta seluruh halaman baru di masa depan):
    - **Wajib 1 baris saja (dilarang keras menambahkan subtitle / judul 2 baris).**
    - **Rata tengah (`Center`)** secara horizontal dan vertikal tepat di titik tengah slot `48.dp` (sejajar pusat bulatan tombol Back `48.dp`).
    - **Ukuran font baku terkunci (sama persis dengan `"Halo Username"` di Beranda):** **`22.sp`** (`DiajakHeaderSpec.TitleFontSize`, `PlusJakartaSansFamily`, Bold, `lineHeight = 25.sp`, `letterSpacing = -0.25.sp`, `includeFontPadding = false`, `LineHeightStyle.Trim.Both`, `maxLines = 1`, `TextOverflow.Ellipsis`).
    - Koordinat pusat `(X, Y)` judul **100% identik** baik pada halaman bertombol Back maupun tanpa tombol Back (menggunakan inset simetris kiri-kanan `56.dp` saat ada tombol aksi di sisi header).
  - **Pengecualian tata letak khusus (hanya 2, namun tetap wajib mematuhi koordinat & diameter tombol `DiajakHeaderSpec`):**
    1. **Halaman Beranda (`HomeScreen`):** Menggunakan sapaan 2 baris rata kiri (`Halo [Nama]` **`22.sp`** via `DiajakHeaderSpec.TitleFontSize` + `Mau diajak kemana?` `13.sp`) dengan `Column(Modifier.weight(1f).fillMaxHeight(), verticalArrangement = Arrangement.SpaceBetween)` di dalam slot `48.dp`, sehingga **sisi atas `"Halo Username"` sejajar lurus dengan sisi atas bulatan Lonceng `48.dp`**, dan **sisi bawah `"Mau diajak kemana?"` sejajar lurus dengan sisi bawah bulatan Lonceng `48.dp`** tepat `16.dp` di atas kolom pencarian.
    2. **Halaman Detail Aktivitas (`ActivityDetailScreen`):** Menggunakan foto hero penuh (*full-bleed*) dengan tombol Back & Love putih mengambang di atas foto, tetapi **WAJIB** memakai `statusBarsPadding()` + `padding(start/end = 20.dp, top = 14.dp, bottom = 12.dp)` + `height(48.dp)` + diameter **`48.dp`** (`DiajakHeaderSpec.ButtonSize`) agar posisi tombol Back & Love 100% presisi saat transisi dari/ke Beranda maupun halaman lain.
- **Perilaku Scroll Header (Opsi A — Ikut Konten Murni):**
  - Pada seluruh halaman standar (`DiajakPageScaffold`, default `pinnedHeader = false`), header **tidak menempel** (*non-sticky*): saat layar discroll ke bawah, header ikut naik 1:1 dan tenggelam ke atas bersama konten, lalu baru muncul kembali ketika layar discroll balik hingga menyentuh bagian paling atas konten.
  - Area tepat di belakang status bar selalu tertutup latar putih solid (`#FFFFFF`) agar konten yang naik tidak pernah bertabrakan dengan ikon jam/baterai status bar.
  - Saat halaman belum discroll (posisi awal), seluruh ukuran judul (`22.sp` Bold), posisi pusat judul, diameter & koordinat bulatan tombol (`48.dp`), serta jarak ke konten (`16.dp`) tetap **100% identik** tanpa perubahan sedikit pun.
  - **Pengecualian resmi perilaku header:**
    - **Beranda (`HomeScreen`)**: Tata letak khusus sapaan (*"Halo [Nama]"* + *"Mau diajak kemana?"* + tombol lonceng `48.dp`) yang ikut naik & tenggelam ke atas bersama konten saat discroll, sedangkan **kolom pencarian tetap menempel (*sticky*) dengan latar putih polos *flat*** di bawah status bar (lalu kembali ke posisi semula saat scroll mentok paling atas); *pull-to-refresh* terbuka tepat di bawah sapaan dan di atas kolom pencarian.
    - **Detail Aktivitas (`ActivityDetailScreen`)** (tata letak khusus tombol mengambang di atas foto hero).
    - **Ruang Chat (`ChatRoomScreen`)** menggunakan `DiajakPageScaffold(..., pinnedHeader = true)` agar nama lawan bicara & tombol Back tetap menempel saat membaca/mengetik percakapan panjang.
- **Penegakan Otomatis (*Automated Guard*):**
  - **DILARANG KERAS** membuat `TopAppBar`, `CenterAlignedTopAppBar`, `MediumTopAppBar`, `LargeTopAppBar`, atau header teks manual di layar mana pun.
  - Seluruh aturan di atas dijaga secara otomatis oleh unit test **`HeaderStandardGuardTest`** (`gradle :app:testDebugUnitTest`) yang akan menggagalkan build test jika ada halaman baru/perubahan yang melanggar standar header Diajak.


