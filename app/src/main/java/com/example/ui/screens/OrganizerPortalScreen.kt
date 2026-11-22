package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Chat
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Badge
import androidx.compose.material.icons.rounded.ConfirmationNumber
import androidx.compose.material.icons.rounded.EventNote
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.RateReview
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.ActivityItem
import com.example.data.DiajakCatalog
import com.example.data.UiState
import com.example.ui.DiajakViewModel
import com.example.ui.components.DiajakBackButton
import com.example.ui.components.DiajakFlatSecondaryButton
import com.example.ui.components.DiajakFlatTextField
import com.example.ui.components.DiajakPhoto
import com.example.ui.components.EmptyStatePanel
import com.example.ui.components.NichePickerBottomSheet
import com.example.ui.theme.DiajakComponentSize
import com.example.ui.theme.DiajakIconSize
import com.example.ui.theme.DiajakRadius
import com.example.ui.theme.DiajakSpacing
import com.example.ui.theme.VerifiedGreen
import com.example.ui.theme.VerifiedGreenSurface
import com.example.ui.theme.WarningAmber
import com.example.ui.theme.WarningAmberSurface

enum class OrganizerModuleRoute(val route: String, val title: String, val subtitle: String) {
    ACTIVITIES("org_activities", "Kelola Aktivitas", "Buat, draft & terbitkan"),
    REGISTRATIONS("org_registrations", "Kelola Pendaftaran", "Peserta & check-in tiket"),
    MESSAGES("org_messages", "Kelola Pesan", "Inbox & grup peserta"),
    BALANCE("org_balance", "Kelola Saldo", "Pendapatan & penarikan"),
    REVIEWS("org_reviews", "Kelola Ulasan", "Rating & balas ulasan"),
    DOCUMENTS("org_documents", "Kelola Dokumen", "Verifikasi KTP wajib")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrganizerPortalScreen(
    viewModel: DiajakViewModel,
    onBack: () -> Unit,
    onOpenModule: (OrganizerModuleRoute) -> Unit
) {
    val profileState by viewModel.userProfileState.collectAsStateWithLifecycle()
    val myActivitiesState by viewModel.organizerActivitiesState.collectAsStateWithLifecycle()
    val myBookingsState by viewModel.organizerBookingsState.collectAsStateWithLifecycle()
    val walletBalance by viewModel.availableWalletBalance.collectAsStateWithLifecycle()

    val isVerified = (profileState as? UiState.Success)?.data?.organizerVerified == true
    val myActivities = (myActivitiesState as? UiState.Success)?.data ?: emptyList()
    val myBookings = (myBookingsState as? UiState.Success)?.data ?: emptyList()
    val activeCount = myActivities.count { it.status == "PUBLISHED" }
    val totalRegistrants = myBookings.sumOf { it.ticketCount }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Portal Penyelenggara", style = MaterialTheme.typography.titleLarge)
                        Text(
                            text = "diajak.com Organizer Center",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    DiajakBackButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("organizer_portal_back_button")
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(DiajakSpacing.ScreenHorizontal),
            verticalArrangement = Arrangement.spacedBy(DiajakSpacing.SectionGap)
        ) {
            // KYC Verification Gate Banner
            item {
                Surface(
                    onClick = { onOpenModule(OrganizerModuleRoute.DOCUMENTS) },
                    shape = DiajakRadius.Card,
                    color = if (isVerified) VerifiedGreenSurface else WarningAmberSurface,
                    tonalElevation = 0.dp,
                    shadowElevation = 0.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("organizer_kyc_banner")
                ) {
                    Row(
                        modifier = Modifier.padding(DiajakSpacing.CardPadding),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isVerified) Icons.Rounded.Verified else Icons.Rounded.WarningAmber,
                            contentDescription = null,
                            tint = if (isVerified) VerifiedGreen else WarningAmber,
                            modifier = Modifier.size(DiajakIconSize.Primary)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isVerified) "Penyelenggara Terverifikasi KTP" else "Verifikasi Dokumen KTP Diperlukan",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isVerified) VerifiedGreen else WarningAmber
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (isVerified)
                                    "Kamu bebas menerbitkan aktivitas gratis maupun berbayar ke seluruh peserta diajak."
                                else
                                    "Unggah KTP di Kelola Dokumen terlebih dahulu agar aktivitas yang kamu buat dapat diterbitkan.",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isVerified) VerifiedGreen else WarningAmber
                            )
                        }
                    }
                }
            }

            // KPI Summary Row (2x2 clean gray cards ala Shopee Seller Center)
            item {
                Column(verticalArrangement = Arrangement.spacedBy(DiajakSpacing.ItemGap)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(DiajakSpacing.ItemGap)
                    ) {
                        OrganizerKpiCard(
                            label = "Aktivitas Aktif",
                            value = "$activeCount",
                            modifier = Modifier.weight(1f)
                        )
                        OrganizerKpiCard(
                            label = "Total Pendaftar",
                            value = "$totalRegistrants",
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(DiajakSpacing.ItemGap)
                    ) {
                        OrganizerKpiCard(
                            label = "Saldo Tersedia",
                            value = DiajakCatalog.formatRupiah(walletBalance).replace("Gratis", "Rp 0"),
                            modifier = Modifier.weight(1f)
                        )
                        OrganizerKpiCard(
                            label = "Total Draft",
                            value = "${myActivities.count { it.status == "DRAFT" }}",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // 6 Complete Organizer Modules Grid
            item {
                Text(
                    text = "Menu Penyelenggara",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            item {
                val modules = listOf(
                    Triple(OrganizerModuleRoute.ACTIVITIES, Icons.Rounded.EventNote, "${myActivities.size} acara"),
                    Triple(OrganizerModuleRoute.REGISTRATIONS, Icons.Rounded.ConfirmationNumber, "$totalRegistrants tiket"),
                    Triple(OrganizerModuleRoute.MESSAGES, Icons.AutoMirrored.Rounded.Chat, "Inbox & Grup"),
                    Triple(OrganizerModuleRoute.BALANCE, Icons.Rounded.AccountBalanceWallet, DiajakCatalog.formatRupiah(walletBalance).replace("Gratis", "Rp 0")),
                    Triple(OrganizerModuleRoute.REVIEWS, Icons.Rounded.RateReview, "Balas ulasan"),
                    Triple(OrganizerModuleRoute.DOCUMENTS, Icons.Rounded.Badge, if (isVerified) "Terverifikasi" else "Wajib KTP")
                )

                Column(verticalArrangement = Arrangement.spacedBy(DiajakSpacing.ItemGap)) {
                    modules.chunked(2).forEach { pair ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(DiajakSpacing.ItemGap)
                        ) {
                            pair.forEach { (route, icon, badge) ->
                                OrganizerModuleTile(
                                    title = route.title,
                                    subtitle = route.subtitle,
                                    badge = badge,
                                    icon = icon,
                                    onClick = { onOpenModule(route) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("organizer_module_${route.route}")
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OrganizerKpiCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(DiajakRadius.Card)
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(DiajakSpacing.CardPadding)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun OrganizerModuleTile(
    title: String,
    subtitle: String,
    badge: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = DiajakRadius.Card,
        color = MaterialTheme.colorScheme.surfaceContainer,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(DiajakSpacing.CardPadding),
            verticalArrangement = Arrangement.spacedBy(DiajakSpacing.ChipGap)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(DiajakComponentSize.IconCircle)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(DiajakIconSize.Primary)
                    )
                }
                Surface(
                    shape = DiajakRadius.Pill,
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    tonalElevation = 0.dp,
                    shadowElevation = 0.dp
                ) {
                    Text(
                        text = badge,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

// --- MODULE 1: KELOLA AKTIVITAS (LIST + CREATE/EDIT WITH KYC PUBLISH GATE) ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageActivitiesScreen(
    viewModel: DiajakViewModel,
    onBack: () -> Unit,
    onGoToDocuments: () -> Unit
) {
    val profileState by viewModel.userProfileState.collectAsStateWithLifecycle()
    val activitiesState by viewModel.organizerActivitiesState.collectAsStateWithLifecycle()
    val isVerified = (profileState as? UiState.Success)?.data?.organizerVerified == true
    val activities = (activitiesState as? UiState.Success)?.data ?: emptyList()

    var editingActivity by remember { mutableStateOf<ActivityItem?>(null) }
    var showForm by remember { mutableStateOf(false) }

    if (showForm) {
        ActivityEditorFormScreen(
            existing = editingActivity,
            isVerified = isVerified,
            viewModel = viewModel,
            onBack = {
                showForm = false
                editingActivity = null
            },
            onGoToDocuments = {
                showForm = false
                editingActivity = null
                onGoToDocuments()
            }
        )
        return
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Kelola Aktivitas", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    DiajakBackButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("manage_activities_back_button")
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    editingActivity = null
                    showForm = true
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                elevation = FloatingActionButtonDefaults.elevation(0.dp, 0.dp, 0.dp, 0.dp),
                modifier = Modifier.testTag("create_activity_fab")
            ) {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = "Buat aktivitas baru",
                    modifier = Modifier.size(DiajakIconSize.Primary)
                )
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(DiajakSpacing.ScreenHorizontal),
            verticalArrangement = Arrangement.spacedBy(DiajakSpacing.ItemGap)
        ) {
            if (!isVerified) {
                item {
                    Surface(
                        onClick = onGoToDocuments,
                        shape = DiajakRadius.Card,
                        color = WarningAmberSurface,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Akun belum terverifikasi KTP. Kamu bisa membuat Draft sekarang, lalu unggah KTP di Kelola Dokumen untuk menerbitkan ke Beranda.",
                            style = MaterialTheme.typography.bodySmall,
                            color = WarningAmber,
                            modifier = Modifier.padding(DiajakSpacing.CardPadding)
                        )
                    }
                }
            }

            if (activities.isEmpty()) {
                item {
                    EmptyStatePanel(
                        icon = Icons.Rounded.EventNote,
                        title = "Belum ada aktivitas buatanmu",
                        subtitle = "Tekan tombol + untuk membuat aktivitas sosial, olahraga, atau lokakarya pertamamu.",
                        actionLabel = "Buat Aktivitas",
                        onAction = {
                            editingActivity = null
                            showForm = true
                        }
                    )
                }
            } else {
                items(activities, key = { it.id }) { act ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(DiajakRadius.Card)
                            .background(MaterialTheme.colorScheme.surfaceContainer)
                            .padding(DiajakSpacing.CardPadding),
                        verticalArrangement = Arrangement.spacedBy(DiajakSpacing.ChipGap)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            DiajakPhoto(
                                photoKey = act.photoKeys.firstOrNull() ?: "img_activity_running",
                                contentDescription = act.title,
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(DiajakRadius.Small)
                            )
                            Spacer(modifier = Modifier.width(DiajakSpacing.ItemGap))
                            Column(modifier = Modifier.weight(1f)) {
                                Surface(
                                    shape = DiajakRadius.Pill,
                                    color = when (act.status) {
                                        "PUBLISHED" -> VerifiedGreenSurface
                                        "DRAFT" -> WarningAmberSurface
                                        else -> MaterialTheme.colorScheme.surfaceContainerHigh
                                    }
                                ) {
                                    Text(
                                        text = when (act.status) {
                                            "PUBLISHED" -> "Terbit"
                                            "DRAFT" -> "Draft"
                                            else -> "Ditutup"
                                        },
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = when (act.status) {
                                            "PUBLISHED" -> VerifiedGreen
                                            "DRAFT" -> WarningAmber
                                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                                        },
                                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = act.title,
                                    style = MaterialTheme.typography.titleSmall,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${DiajakCatalog.formatRupiah(act.priceIdr)} • ${act.joinedCount}/${act.quota} peserta",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceContainerHigh)

                        Row(horizontalArrangement = Arrangement.spacedBy(DiajakSpacing.SmallGap)) {
                            DiajakFlatSecondaryButton(
                                onClick = {
                                    editingActivity = act
                                    showForm = true
                                },
                                nested = true,
                                shape = DiajakRadius.Small,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Edit", style = MaterialTheme.typography.labelMedium)
                            }
                            if (act.status == "DRAFT") {
                                Button(
                                    onClick = {
                                        if (isVerified) viewModel.changeActivityStatus(act.id, "PUBLISHED")
                                        else onGoToDocuments()
                                    },
                                    shape = DiajakRadius.Small,
                                    elevation = ButtonDefaults.buttonElevation(0.dp, 0.dp, 0.dp, 0.dp, 0.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(if (isVerified) "Terbitkan" else "Verifikasi KTP", style = MaterialTheme.typography.labelMedium)
                                }
                            } else if (act.status == "PUBLISHED") {
                                DiajakFlatSecondaryButton(
                                    onClick = { viewModel.changeActivityStatus(act.id, "CLOSED") },
                                    nested = true,
                                    shape = DiajakRadius.Small,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Tutup Acara", style = MaterialTheme.typography.labelMedium)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ActivityEditorFormScreen(
    existing: ActivityItem?,
    isVerified: Boolean,
    viewModel: DiajakViewModel,
    onBack: () -> Unit,
    onGoToDocuments: () -> Unit
) {
    BackHandler(onBack = onBack)
    val isBusy by viewModel.isBusy.collectAsStateWithLifecycle()
    var title by remember { mutableStateOf(existing?.title ?: "") }
    var description by remember { mutableStateOf(existing?.description ?: "") }
    var selectedNiche by remember {
        mutableStateOf(
            DiajakCatalog.allNiches.firstOrNull { it.name == existing?.category }
                ?: DiajakCatalog.allNiches.first { it.name == "Lari" }
        )
    }
    var selectedCity by remember {
        mutableStateOf(DiajakCatalog.findCityByName(existing?.city ?: "Jakarta Selatan"))
    }
    var locationName by remember { mutableStateOf(existing?.locationName ?: "") }
    var daysAhead by remember { mutableIntStateOf(5) }
    var timeLabel by remember { mutableStateOf(existing?.timeLabel ?: "07:00 WIB") }
    var priceText by remember { mutableStateOf(if ((existing?.priceIdr ?: 0L) > 0L) existing?.priceIdr.toString() else "0") }
    var quotaText by remember { mutableStateOf((existing?.quota ?: 30L).toString()) }
    var benefitsText by remember {
        mutableStateOf(
            existing?.benefits?.joinToString("\n")
                ?: "Akses penuh sesi aktivitas\nMinuman penyegar\nDokumentasi foto bersama"
        )
    }
    var selectedPhotoKey by remember {
        mutableStateOf(existing?.photoKeys?.firstOrNull() ?: "img_activity_running")
    }
    var showNicheSheet by remember { mutableStateOf(false) }

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) selectedPhotoKey = uri.toString()
    }

    val submit: (Boolean) -> Unit = { publishNow ->
        val price = priceText.filter { it.isDigit() }.toLongOrNull() ?: 0L
        val quota = (quotaText.filter { it.isDigit() }.toLongOrNull() ?: 20L).coerceIn(1L, 10000L)
        val eventMillis = existing?.eventDateMillis?.takeIf { it > System.currentTimeMillis() }
            ?: (System.currentTimeMillis() + daysAhead * 86_400_000L)
        val benefitList = benefitsText.lines().map { it.trim() }.filter { it.isNotBlank() }

        viewModel.saveOrganizerActivity(
            existingId = existing?.id,
            title = title,
            description = description,
            niche = selectedNiche,
            cityOption = selectedCity,
            locationName = locationName,
            eventDateMillis = eventMillis,
            timeLabel = timeLabel,
            priceIdr = price,
            quota = quota,
            photoKeys = listOf(selectedPhotoKey),
            benefits = benefitList,
            publishNow = publishNow,
            onSuccess = onBack
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (existing == null) "Buat Aktivitas" else "Edit Aktivitas",
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    DiajakBackButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("activity_editor_back_button")
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        bottomBar = {
            Surface(color = MaterialTheme.colorScheme.surface) {
                Column(modifier = Modifier.navigationBarsPadding()) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = DiajakSpacing.ScreenHorizontal, vertical = DiajakSpacing.ItemGap),
                        horizontalArrangement = Arrangement.spacedBy(DiajakSpacing.ChipGap)
                    ) {
                        DiajakFlatSecondaryButton(
                            onClick = { submit(false) },
                            enabled = !isBusy,
                            shape = DiajakRadius.Control,
                            modifier = Modifier
                                .weight(1f)
                                .height(DiajakComponentSize.PrimaryButtonHeight)
                                .testTag("save_draft_button")
                        ) {
                            Text("Simpan Draft", style = MaterialTheme.typography.labelLarge)
                        }
                        Button(
                            onClick = {
                                if (isVerified) submit(true) else onGoToDocuments()
                            },
                            enabled = !isBusy,
                            shape = DiajakRadius.Control,
                            elevation = ButtonDefaults.buttonElevation(0.dp, 0.dp, 0.dp, 0.dp, 0.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(DiajakComponentSize.PrimaryButtonHeight)
                                .testTag("publish_activity_button")
                        ) {
                            Text(if (isVerified) "Terbitkan" else "Verifikasi KTP", style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(DiajakSpacing.ScreenHorizontal),
            verticalArrangement = Arrangement.spacedBy(DiajakSpacing.SectionGap)
        ) {
            if (!isVerified) {
                item {
                    Surface(
                        onClick = onGoToDocuments,
                        shape = DiajakRadius.Card,
                        color = WarningAmberSurface,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Penerbitan publik terkunci sampai KTP diverifikasi. Kamu tetap bisa menyimpan Draft sekarang.",
                            style = MaterialTheme.typography.bodySmall,
                            color = WarningAmber,
                            modifier = Modifier.padding(DiajakSpacing.CardPadding)
                        )
                    }
                }
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Foto Sampul Aktivitas", style = MaterialTheme.typography.titleSmall)
                    DiajakPhoto(
                        photoKey = selectedPhotoKey,
                        contentDescription = "Preview foto aktivitas",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(170.dp)
                            .clip(DiajakRadius.Card)
                    )
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(DiajakSpacing.SmallGap)) {
                        item {
                            DiajakFlatSecondaryButton(
                                onClick = {
                                    photoPicker.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                                shape = DiajakRadius.Small
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Image,
                                    contentDescription = null,
                                    modifier = Modifier.size(DiajakIconSize.Inline)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Dari Galeri", style = MaterialTheme.typography.labelMedium)
                            }
                        }
                        items(DiajakCatalog.presetPhotoKeys) { (key, label) ->
                            Surface(
                                onClick = { selectedPhotoKey = key },
                                shape = DiajakRadius.Small,
                                color = if (selectedPhotoKey == key) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainer,
                                tonalElevation = 0.dp,
                                shadowElevation = 0.dp
                            ) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (selectedPhotoKey == key) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
                                )
                            }
                        }
                    }
                }
            }

            item {
                DiajakFlatTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = "Judul Aktivitas",
                    placeholder = "Contoh: Weekend Padel Social Match",
                    singleLine = true,
                    shape = DiajakRadius.Control,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                Surface(
                    onClick = { showNicheSheet = true },
                    shape = DiajakRadius.Control,
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    tonalElevation = 0.dp,
                    shadowElevation = 0.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Kategori / Niche (69 Pilihan)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${selectedNiche.emoji} ${selectedNiche.name}", style = MaterialTheme.typography.titleSmall)
                        }
                        Text("Ubah", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Kota & Jadwal Pelaksanaan", style = MaterialTheme.typography.titleSmall)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(DiajakCatalog.cities) { c ->
                            val active = selectedCity.name == c.name
                            Surface(
                                onClick = { selectedCity = c },
                                shape = DiajakRadius.Pill,
                                color = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainer,
                                tonalElevation = 0.dp,
                                shadowElevation = 0.dp
                            ) {
                                Text(
                                    text = c.name,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (active) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(DiajakSpacing.SmallGap)) {
                        listOf(2 to "2 Hari Lagi", 5 to "5 Hari Lagi", 10 to "10 Hari Lagi", 14 to "2 Minggu").forEach { (d, lbl) ->
                            val active = daysAhead == d
                            Surface(
                                onClick = { daysAhead = d },
                                shape = DiajakRadius.Small,
                                color = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainer,
                                tonalElevation = 0.dp,
                                shadowElevation = 0.dp,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = lbl,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (active) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }

            item {
                DiajakFlatTextField(
                    value = locationName,
                    onValueChange = { locationName = it },
                    label = "Nama Venue / Titik Kumpul",
                    placeholder = "Contoh: GBK Senayan, Pintu 7",
                    singleLine = true,
                    shape = DiajakRadius.Control,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(DiajakSpacing.ChipGap)) {
                    DiajakFlatTextField(
                        value = timeLabel,
                        onValueChange = { timeLabel = it },
                        label = "Jam Mulai",
                        singleLine = true,
                        shape = DiajakRadius.Control,
                        modifier = Modifier.weight(1f)
                    )
                    DiajakFlatTextField(
                        value = quotaText,
                        onValueChange = { quotaText = it.filter { ch -> ch.isDigit() } },
                        label = "Kuota",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = DiajakRadius.Control,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                DiajakFlatTextField(
                    value = priceText,
                    onValueChange = { priceText = it.filter { ch -> ch.isDigit() } },
                    label = "Harga Tiket (Rp — isi 0 jika Gratis)",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = DiajakRadius.Control,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                DiajakFlatTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = "Tentang Aktivitas",
                    placeholder = "Jelaskan agenda, level peserta, dan perlengkapan yang perlu dibawa...",
                    singleLine = false,
                    minLines = 3,
                    maxLines = 6,
                    shape = DiajakRadius.Control,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                DiajakFlatTextField(
                    value = benefitsText,
                    onValueChange = { benefitsText = it },
                    label = "Yang Didapat (1 poin per baris)",
                    singleLine = false,
                    minLines = 3,
                    maxLines = 6,
                    shape = DiajakRadius.Control,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }

    if (showNicheSheet) {
        NichePickerBottomSheet(
            selectedNicheName = selectedNiche.name,
            allowClearAll = false,
            onSelectNiche = { picked -> if (picked != null) selectedNiche = picked },
            onDismiss = { showNicheSheet = false }
        )
    }
}
