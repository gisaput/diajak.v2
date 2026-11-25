package com.example

import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DiajakComponentSize
import com.example.ui.theme.DiajakHeaderSpec
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Pengaman otomatis Aturan Mutlak Header Halaman Diajak (DESIGN_SYSTEM.md §7).
 *
 * Test ini akan otomatis GAGAL jika di masa depan ada perubahan atau penambahan halaman baru yang:
 * 1. Mengubah dimensi / ukuran font baku pada [DiajakHeaderSpec].
 * 2. Memakai `TopAppBar`, `CenterAlignedTopAppBar`, `MediumTopAppBar`, atau `LargeTopAppBar`.
 * 3. Membuat layar baru di `ui/screens/` (selain Beranda & Detail Aktivitas) tanpa memakai `DiajakPageScaffold`.
 * 4. Mengubah `DiajakPageHeader` sehingga tidak lagi 1 baris (`maxLines = 1`) dan rata tengah (`TextAlign.Center`).
 */
class HeaderStandardGuardTest {

    private val exemptScreenFiles = setOf(
        "HomeScreen.kt",
        "ActivityDetailScreen.kt"
    )

    private val forbiddenTopBarTokens = listOf(
        "TopAppBar(",
        "CenterAlignedTopAppBar(",
        "MediumTopAppBar(",
        "LargeTopAppBar("
    )

    private fun resolveProjectDir(relativePath: String): File {
        val candidates = listOf(
            File(relativePath),
            File("app/$relativePath"),
            File("../$relativePath")
        )
        return candidates.firstOrNull { it.exists() }
            ?: error("Direktori sumber tidak ditemukan untuk '$relativePath' (cwd=${File(".").absolutePath})")
    }

    @Test
    fun headerSpecTokensAreLockedToHomeBaseline() {
        assertEquals("PaddingTop header harus terkunci 14.dp", 14.dp, DiajakHeaderSpec.PaddingTop)
        assertEquals("PaddingBottom header harus terkunci 12.dp", 12.dp, DiajakHeaderSpec.PaddingBottom)
        assertEquals(
            "Tinggi slot konten header harus sama persis dengan diameter bulatan tombol 48.dp",
            DiajakComponentSize.ActionCircle,
            DiajakHeaderSpec.ContentHeight
        )
        assertEquals(
            "Diameter bulatan tombol header di semua halaman harus seragam 48.dp (Opsi 1 / Android M3)",
            DiajakComponentSize.ActionCircle,
            DiajakHeaderSpec.ButtonSize
        )
        assertEquals("ContentTopGap harus terkunci 4.dp", 4.dp, DiajakHeaderSpec.ContentTopGap)
        assertEquals(
            "Total jarak visual bawah slot 48.dp ke konten pertama harus terkunci 16.dp (patokan Beranda)",
            16.dp,
            DiajakHeaderSpec.VisualHeaderToContentGap
        )
        assertEquals("Ukuran font semua judul halaman harus seragam 22.sp (sama dengan Halo Username)", 22.sp, DiajakHeaderSpec.TitleFontSize)
        assertEquals("LineHeight judul halaman harus terkunci 25.sp", 25.sp, DiajakHeaderSpec.TitleLineHeight)
        assertEquals("LetterSpacing judul halaman harus terkunci -0.25.sp", (-0.25).sp, DiajakHeaderSpec.TitleLetterSpacing)
    }

    @Test
    fun allStandardScreensUseDiajakPageScaffoldWithoutManualTopAppBar() {
        val screensDir = resolveProjectDir("src/main/java/com/example/ui/screens")
        val screenFiles = screensDir.listFiles { file -> file.isFile && file.extension == "kt" }
            ?.sortedBy { it.name }
            .orEmpty()

        assertTrue("Daftar file layar tidak boleh kosong", screenFiles.isNotEmpty())

        val violations = mutableListOf<String>()

        for (file in screenFiles) {
            val content = file.readText()

            // Aturan 1: Nol TopAppBar manual di seluruh layar
            for (token in forbiddenTopBarTokens) {
                if (content.contains(token)) {
                    violations += "${file.name}: Dilarang memakai '$token'. Gunakan DiajakPageScaffold."
                }
            }

            if (file.name in exemptScreenFiles) continue

            // Aturan 2: Semua file layar non-pengecualian wajib memakai DiajakPageScaffold
            if (!content.contains("DiajakPageScaffold(")) {
                violations += "${file.name}: Wajib memakai DiajakPageScaffold agar header & jarak konten terkunci."
            }

            // Aturan 3: Tidak boleh memanggil Scaffold mentah secara langsung di layar standar
            if (Regex("""(?<![A-Za-z0-9_])Scaffold\s*\(""").containsMatchIn(content)) {
                violations += "${file.name}: Dilarang memakai Scaffold mentah; gunakan DiajakPageScaffold."
            }
        }

        assertTrue(
            "Pelanggaran Aturan Mutlak Header Halaman ditemukan:\n" + violations.joinToString("\n"),
            violations.isEmpty()
        )
    }

    @Test
    fun diajakPageHeaderEnforcesSingleLineCenteredTitle() {
        val commonComponentsFile = resolveProjectDir("src/main/java/com/example/ui/components/CommonComponents.kt")
        val content = commonComponentsFile.readText()
        val headerStart = content.indexOf("fun DiajakPageHeader(")
        val scaffoldStart = content.indexOf("fun DiajakPageScaffold(")

        assertTrue("DiajakPageHeader harus ada di CommonComponents.kt", headerStart >= 0)
        assertTrue("DiajakPageScaffold harus ada di CommonComponents.kt", scaffoldStart > headerStart)

        val headerBody = content.substring(headerStart, scaffoldStart)
        assertTrue("DiajakPageHeader wajib memakai textAlign = TextAlign.Center", headerBody.contains("textAlign = TextAlign.Center"))
        assertTrue("DiajakPageHeader wajib 1 baris (maxLines = 1)", headerBody.contains("maxLines = 1"))
        assertTrue("DiajakPageHeader wajib Alignment.Center", headerBody.contains(".align(Alignment.Center)"))
        assertTrue("DiajakPageHeader wajib memakai DiajakHeaderSpec.TitleFontSize", headerBody.contains("DiajakHeaderSpec.TitleFontSize"))
        assertTrue("DiajakPageHeader wajib memakai DiajakHeaderSpec.ContentHeight", headerBody.contains("DiajakHeaderSpec.ContentHeight"))
        assertTrue("DiajakPageHeader wajib memakai DiajakHeaderSpec.ButtonSize", headerBody.contains("DiajakHeaderSpec.ButtonSize"))
    }

    @Test
    fun homeAndDetailHeaderButtonsMatchUnifiedStandard() {
        val homeContent = resolveProjectDir("src/main/java/com/example/ui/screens/HomeScreen.kt").readText()
        assertTrue("HomeScreen greeting wajib memakai DiajakHeaderSpec.TitleFontSize", homeContent.contains("DiajakHeaderSpec.TitleFontSize"))
        assertTrue("HomeScreen lonceng wajib memakai DiajakHeaderSpec.ButtonSize", homeContent.contains("DiajakHeaderSpec.ButtonSize"))
        assertTrue("HomeScreen greeting Column wajib fillMaxHeight() agar rata atas-bawah dengan lonceng", homeContent.contains(".fillMaxHeight()"))
        assertTrue("HomeScreen greeting Column wajib Arrangement.SpaceBetween agar sejajar atas & bawah lonceng", homeContent.contains("Arrangement.SpaceBetween"))

        val detailContent = resolveProjectDir("src/main/java/com/example/ui/screens/ActivityDetailScreen.kt").readText()
        assertTrue("ActivityDetailScreen top bar wajib memakai DiajakHeaderSpec.PaddingTop", detailContent.contains("DiajakHeaderSpec.PaddingTop"))
        assertTrue("ActivityDetailScreen top bar wajib memakai DiajakHeaderSpec.ContentHeight", detailContent.contains("DiajakHeaderSpec.ContentHeight"))
        assertTrue("ActivityDetailScreen tombol Back & Love wajib memakai DiajakHeaderSpec.ButtonSize (48.dp)", detailContent.contains("DiajakHeaderSpec.ButtonSize"))
        assertTrue("ActivityDetailScreen tidak boleh memakai padding 16.dp/12.dp manual pada top bar", !detailContent.contains(".padding(horizontal = 16.dp, vertical = 12.dp)"))
    }

    @Test
    fun standardHeadersScrollWithContentExceptChatRoom() {
        val commonContent = resolveProjectDir("src/main/java/com/example/ui/components/CommonComponents.kt").readText()
        val scaffoldStart = commonContent.indexOf("fun DiajakPageScaffold(")
        assertTrue("DiajakPageScaffold harus ada di CommonComponents.kt", scaffoldStart >= 0)
        val scaffoldBody = commonContent.substring(scaffoldStart, scaffoldStart + 5000)

        assertTrue(
            "DiajakPageScaffold wajib memiliki default pinnedHeader: Boolean = false (Opsi A ikut konten murni)",
            scaffoldBody.contains("pinnedHeader: Boolean = false")
        )
        assertTrue(
            "DiajakPageScaffold wajib memakai NestedScrollConnection agar header ikut naik & tenggelam bersama konten",
            scaffoldBody.contains("NestedScrollConnection")
        )
        assertTrue(
            "DiajakPageScaffold wajib menangani onPostScroll agar header baru muncul kembali setelah konten kembali ke paling atas (Opsi A)",
            scaffoldBody.contains("override fun onPostScroll(")
        )

        val screensDir = resolveProjectDir("src/main/java/com/example/ui/screens")
        val screenFiles = screensDir.listFiles { file -> file.isFile && file.extension == "kt" }
            ?.sortedBy { it.name }
            .orEmpty()

        val pinnedViolations = mutableListOf<String>()
        for (file in screenFiles) {
            val content = file.readText()
            val matches = Regex("""pinnedHeader\s*=\s*true""").findAll(content).count()
            if (file.name == "MessagesScreen.kt") {
                if (matches != 1) {
                    pinnedViolations += "MessagesScreen.kt harus memiliki tepat 1 pinnedHeader = true (khusus ChatRoomScreen), ditemukan $matches."
                }
            } else if (matches > 0) {
                pinnedViolations += "${file.name}: Dilarang memakai pinnedHeader = true; hanya Ruang Chat (ChatRoomScreen) yang boleh menempel."
            }
        }

        assertTrue(
            "Pelanggaran aturan scroll header ditemukan:\n" + pinnedViolations.joinToString("\n"),
            pinnedViolations.isEmpty()
        )
    }
}
