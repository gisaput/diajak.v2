package com.example.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Chat
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.Badge
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ConfirmationNumber
import androidx.compose.material.icons.rounded.RateReview
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.UploadFile
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.ActivityItem
import com.example.data.ActivityReview
import com.example.data.DiajakCatalog
import com.example.data.UiState
import com.example.ui.DiajakViewModel
import com.example.ui.components.DiajakFlatSecondaryButton
import com.example.ui.components.DiajakFlatTextField
import com.example.ui.components.DiajakPageScaffold
import com.example.ui.components.DiajakPhoto
import com.example.ui.components.EmptyStatePanel
import com.example.ui.components.RatingStars
import com.example.ui.components.dismissKeyboardOnOutsideTouch
import com.example.ui.theme.DiajakComponentSize
import com.example.ui.theme.DiajakIconSize
import com.example.ui.theme.DiajakRadius
import com.example.ui.theme.DiajakSpacing
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.VerifiedGreen
import com.example.ui.theme.VerifiedGreenSurface
import com.example.ui.theme.WarningAmber
import com.example.ui.theme.WarningAmberSurface

// --- MODULE 2: KELOLA PENDAFTARAN ---

@Composable
fun ManageRegistrationsScreen(
    viewModel: DiajakViewModel,
    onBack: () -> Unit
) {
    val bookingsState by viewModel.organizerBookingsState.collectAsStateWithLifecycle()
    val bookings = (bookingsState as? UiState.Success)?.data ?: emptyList()
    var query by remember { mutableStateOf("") }

    val filtered = remember(bookings, query) {
        val q = query.trim()
        if (q.isEmpty()) bookings
        else bookings.filter {
            it.userName.contains(q, ignoreCase = true) ||
                it.ticketCode.contains(q, ignoreCase = true) ||
                it.activityTitle.contains(q, ignoreCase = true)
        }
    }

    DiajakPageScaffold(
        title = "Kelola Pendaftaran",
        onBack = onBack,
        backButtonTestTag = "manage_registrations_back_button",
        titleTestTag = "manage_registrations_header_title"
    ) { contentPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = contentPadding,
            verticalArrangement = Arrangement.spacedBy(DiajakSpacing.ItemGap)
        ) {
            item {
                DiajakFlatTextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = "Cari nama peserta atau kode tiket DJK-...",
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Rounded.Search,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(DiajakIconSize.Primary)
                        )
                    },
                    singleLine = true,
                    shape = DiajakRadius.Control,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (filtered.isEmpty()) {
                item {
                    EmptyStatePanel(
                        icon = Icons.Rounded.ConfirmationNumber,
                        title = "Belum ada pendaftar",
                        subtitle = "Peserta yang memesan tiket aktivitasmu akan muncul secara real-time di sini untuk proses check-in.",
                        outerPadding = PaddingValues(0.dp)
                    )
                }
            } else {
                items(filtered, key = { it.id }) { ticket ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(DiajakRadius.Card)
                            .background(MaterialTheme.colorScheme.surfaceContainer)
                            .padding(DiajakSpacing.CardPadding),
                        verticalArrangement = Arrangement.spacedBy(DiajakSpacing.SmallGap)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = ticket.userName,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            TicketStatusPill(status = ticket.status)
                        }
                        Text(
                            text = ticket.activityTitle,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Kode: ${ticket.ticketCode} • ${ticket.ticketCount} tiket • ${ticket.paymentMethod} (${DiajakCatalog.formatRupiah(ticket.totalPriceIdr)})",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (ticket.status == "CONFIRMED") {
                            Button(
                                onClick = { viewModel.checkInParticipantTicket(ticket.id) },
                                shape = DiajakRadius.Small,
                                elevation = ButtonDefaults.buttonElevation(0.dp, 0.dp, 0.dp, 0.dp, 0.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.CheckCircle,
                                    contentDescription = null,
                                    modifier = Modifier.size(DiajakIconSize.Inline)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Verifikasi & Check-in Tiket", style = MaterialTheme.typography.labelLarge)
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- MODULE 3: KELOLA PESAN ---

@Composable
fun ManageOrganizerMessagesScreen(
    viewModel: DiajakViewModel,
    onBack: () -> Unit,
    onOpenConversation: (String) -> Unit
) {
    val convState by viewModel.conversationsState.collectAsStateWithLifecycle()
    val convs = (convState as? UiState.Success)?.data ?: emptyList()
    DiajakPageScaffold(
        title = "Kelola Pesan",
        onBack = onBack,
        backButtonTestTag = "manage_messages_back_button",
        titleTestTag = "manage_messages_header_title"
    ) { contentPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = contentPadding,
            verticalArrangement = Arrangement.spacedBy(DiajakSpacing.ItemGap)
        ) {
            if (convs.isEmpty()) {
                item {
                    EmptyStatePanel(
                        icon = Icons.AutoMirrored.Rounded.Chat,
                        title = "Belum ada pesan masuk",
                        subtitle = "Pertanyaan peserta dan grup chat aktivitas yang kamu kelola akan tampil di sini.",
                        outerPadding = PaddingValues(0.dp)
                    )
                }
            } else {
                items(convs, key = { it.id }) { conv ->
                    ConversationRowCard(
                        conversation = conv,
                        onClick = { onOpenConversation(conv.id) }
                    )
                }
            }
        }
    }
}

// --- MODULE 4: KELOLA SALDO ---

@Composable
fun ManageBalanceScreen(
    viewModel: DiajakViewModel,
    onBack: () -> Unit
) {
    val balance by viewModel.availableWalletBalance.collectAsStateWithLifecycle()
    val txs by viewModel.walletTransactionsState.collectAsStateWithLifecycle()
    val isBusy by viewModel.isBusy.collectAsStateWithLifecycle()
    var showWithdrawDialog by remember { mutableStateOf(false) }
    DiajakPageScaffold(
        title = "Kelola Saldo",
        onBack = onBack,
        backButtonTestTag = "manage_balance_back_button",
        titleTestTag = "manage_balance_header_title"
    ) { contentPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = contentPadding,
            verticalArrangement = Arrangement.spacedBy(DiajakSpacing.SectionGap)
        ) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(DiajakRadius.Card)
                        .background(MaterialTheme.colorScheme.primary)
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(DiajakSpacing.ChipGap)
                ) {
                    Text(
                        text = "Saldo Penyelenggara Tersedia",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                    )
                    Text(
                        text = DiajakCatalog.formatRupiah(balance).replace("Gratis", "Rp 0"),
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Button(
                        onClick = { showWithdrawDialog = true },
                        enabled = balance >= 10_000L && !isBusy,
                        shape = DiajakRadius.Control,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        ),
                        elevation = ButtonDefaults.buttonElevation(0.dp, 0.dp, 0.dp, 0.dp, 0.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(DiajakComponentSize.PrimaryButtonHeight)
                            .testTag("withdraw_balance_button")
                    ) {
                        Text("Tarik Saldo ke Bank / E-Wallet", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }

            item {
                Text("Riwayat Mutasi Saldo", style = MaterialTheme.typography.titleMedium)
            }

            if (txs.isEmpty()) {
                item {
                    EmptyStatePanel(
                        icon = Icons.Rounded.AccountBalanceWallet,
                        title = "Belum ada mutasi saldo",
                        subtitle = "Pendapatan dari setiap tiket aktivitas berbayar yang dipesan peserta otomatis masuk ke saldo ini.",
                        outerPadding = PaddingValues(0.dp)
                    )
                }
            } else {
                items(txs, key = { it.id }) { tx ->
                    val isIncome = tx.type == "INCOME"
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(DiajakRadius.Card)
                            .background(MaterialTheme.colorScheme.surfaceContainer)
                            .padding(DiajakSpacing.CardPadding),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isIncome) Icons.Rounded.ArrowDownward else Icons.Rounded.ArrowUpward,
                            contentDescription = null,
                            tint = if (isIncome) VerifiedGreen else ErrorRed,
                            modifier = Modifier.size(DiajakIconSize.Inline)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(tx.title, style = MaterialTheme.typography.titleSmall)
                            Text(
                                text = "${tx.referenceId} • ${tx.status}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = (if (isIncome) "+" else "-") + DiajakCatalog.formatRupiah(tx.amount),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = if (isIncome) VerifiedGreen else ErrorRed
                        )
                    }
                }
            }
        }
    }

    if (showWithdrawDialog) {
        var amountStr by remember { mutableStateOf(balance.toString()) }
        var dest by remember { mutableStateOf("BCA / GoPay a.n. Penyelenggara") }
        AlertDialog(
            onDismissRequest = { showWithdrawDialog = false },
            modifier = Modifier.dismissKeyboardOnOutsideTouch(),
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 0.dp,
            title = { Text("Tarik Saldo Penyelenggara", style = MaterialTheme.typography.titleLarge) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(DiajakSpacing.ItemGap)) {
                    Text(
                        text = "Saldo tersedia: ${DiajakCatalog.formatRupiah(balance)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    DiajakFlatTextField(
                        value = amountStr,
                        onValueChange = { amountStr = it.filter { c -> c.isDigit() } },
                        label = "Nominal Penarikan (Rp)",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = DiajakRadius.Control,
                        modifier = Modifier.fillMaxWidth()
                    )
                    DiajakFlatTextField(
                        value = dest,
                        onValueChange = { dest = it },
                        label = "Rekening Bank / E-Wallet Tujuan",
                        singleLine = true,
                        shape = DiajakRadius.Control,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amt = amountStr.toLongOrNull() ?: 0L
                        viewModel.withdrawBalance(amt, dest) { showWithdrawDialog = false }
                    },
                    shape = DiajakRadius.Control,
                    elevation = ButtonDefaults.buttonElevation(0.dp, 0.dp, 0.dp, 0.dp, 0.dp)
                ) {
                    Text("Proses Penarikan", style = MaterialTheme.typography.labelLarge)
                }
            },
            dismissButton = {
                TextButton(onClick = { showWithdrawDialog = false }) { Text("Batal") }
            }
        )
    }
}

// --- MODULE 5: KELOLA ULASAN ---

@Composable
fun ManageReviewsScreen(
    viewModel: DiajakViewModel,
    onBack: () -> Unit
) {
    val myActivitiesState by viewModel.organizerActivitiesState.collectAsStateWithLifecycle()
    val myActivities = (myActivitiesState as? UiState.Success)?.data ?: emptyList()
    DiajakPageScaffold(
        title = "Kelola Ulasan",
        onBack = onBack,
        backButtonTestTag = "manage_reviews_back_button",
        titleTestTag = "manage_reviews_header_title"
    ) { contentPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = contentPadding,
            verticalArrangement = Arrangement.spacedBy(DiajakSpacing.SectionGap)
        ) {
            if (myActivities.isEmpty()) {
                item {
                    EmptyStatePanel(
                        icon = Icons.Rounded.RateReview,
                        title = "Belum ada ulasan masuk",
                        subtitle = "Terbitkan aktivitas pertamamu agar peserta dapat memberikan rating dan ulasan yang bisa kamu balas.",
                        outerPadding = PaddingValues(0.dp)
                    )
                }
            } else {
                items(myActivities, key = { it.id }) { activity ->
                    OrganizerActivityReviewsSection(
                        activity = activity,
                        viewModel = viewModel
                    )
                }
            }
        }
    }
}

@Composable
private fun OrganizerActivityReviewsSection(
    activity: ActivityItem,
    viewModel: DiajakViewModel
) {
    val reviewsFlow = remember(activity.id) { viewModel.observeReviews(activity.id) }
    val reviews by reviewsFlow.collectAsStateWithLifecycle(initialValue = emptyList())
    var replyingReview by remember { mutableStateOf<ActivityReview?>(null) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(DiajakRadius.Card)
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(DiajakSpacing.CardPadding),
        verticalArrangement = Arrangement.spacedBy(DiajakSpacing.ChipGap)
    ) {
        Text(text = activity.title, style = MaterialTheme.typography.titleMedium)
        if (reviews.isEmpty()) {
            Text(
                text = "Belum ada ulasan dari peserta pada aktivitas ini.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            reviews.forEach { rev ->
                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceContainerHigh)
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(rev.authorName, style = MaterialTheme.typography.titleSmall)
                        RatingStars(rating = rev.rating.toInt(), starSize = DiajakIconSize.Inline)
                    }
                    Text(rev.comment, style = MaterialTheme.typography.bodySmall)
                    if (rev.organizerReply.isNotBlank()) {
                        Text(
                            text = "Balasanmu: ${rev.organizerReply}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        DiajakFlatSecondaryButton(
                            onClick = { replyingReview = rev },
                            nested = true,
                            shape = DiajakRadius.Small,
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Text("Balas Ulasan", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }
        }
    }

    val targetRev = replyingReview
    if (targetRev != null) {
        var replyText by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { replyingReview = null },
            modifier = Modifier.dismissKeyboardOnOutsideTouch(),
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 0.dp,
            title = { Text("Balas Ulasan ${targetRev.authorName}", style = MaterialTheme.typography.titleLarge) },
            text = {
                DiajakFlatTextField(
                    value = replyText,
                    onValueChange = { replyText = it },
                    label = "Balasan Penyelenggara",
                    singleLine = false,
                    minLines = 2,
                    shape = DiajakRadius.Control,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.submitOrganizerReply(activity.id, targetRev.id, replyText) {
                            replyingReview = null
                        }
                    },
                    enabled = replyText.trim().isNotEmpty(),
                    shape = DiajakRadius.Control,
                    elevation = ButtonDefaults.buttonElevation(0.dp, 0.dp, 0.dp, 0.dp, 0.dp)
                ) {
                    Text("Kirim Balasan", style = MaterialTheme.typography.labelLarge)
                }
            },
            dismissButton = {
                TextButton(onClick = { replyingReview = null }) { Text("Batal") }
            }
        )
    }
}

// --- MODULE 6: KELOLA DOKUMEN (KTP KYC VERIFICATION GATE) ---

@Composable
fun ManageDocumentsScreen(
    viewModel: DiajakViewModel,
    onBack: () -> Unit
) {
    val profileState by viewModel.userProfileState.collectAsStateWithLifecycle()
    val kycState by viewModel.kycDocumentState.collectAsStateWithLifecycle()
    val isBusy by viewModel.isBusy.collectAsStateWithLifecycle()

    val isVerified = (profileState as? UiState.Success)?.data?.organizerVerified == true
    val existingKyc = (kycState as? UiState.Success)?.data

    var fullName by remember(existingKyc) { mutableStateOf(existingKyc?.fullName ?: "") }
    var nikNumber by remember(existingKyc) { mutableStateOf(existingKyc?.nikNumber ?: "") }
    var docPhotoUri by remember(existingKyc) { mutableStateOf(existingKyc?.documentPhotoUri ?: "") }

    val ktpPhotoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) docPhotoUri = uri.toString()
    }

    DiajakPageScaffold(
        title = "Kelola Dokumen (KTP)",
        onBack = onBack,
        backButtonTestTag = "manage_documents_back_button",
        titleTestTag = "manage_documents_header_title"
    ) { contentPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = contentPadding,
            verticalArrangement = Arrangement.spacedBy(DiajakSpacing.SectionGap)
        ) {
            item {
                Surface(
                    shape = DiajakRadius.Card,
                    color = if (isVerified) VerifiedGreenSurface else WarningAmberSurface,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(DiajakSpacing.CardPadding),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isVerified) Icons.Rounded.Verified else Icons.Rounded.Badge,
                            contentDescription = null,
                            tint = if (isVerified) VerifiedGreen else WarningAmber,
                            modifier = Modifier.size(DiajakIconSize.Primary)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = if (isVerified) "Dokumen KTP Terverifikasi" else "Wajib Verifikasi KTP",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isVerified) VerifiedGreen else WarningAmber
                            )
                            Text(
                                text = if (isVerified)
                                    "Identitas penyelenggara telah disetujui. Semua aktivitas yang kamu buat dapat langsung diterbitkan."
                                else
                                    "Lengkapi nama lengkap, 16 digit NIK, dan unggah foto KTP agar aktivitasmu dapat diterbitkan ke publik.",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isVerified) VerifiedGreen else WarningAmber
                            )
                        }
                    }
                }
            }

            item {
                DiajakFlatTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = "Nama Lengkap Sesuai KTP",
                    singleLine = true,
                    shape = DiajakRadius.Control,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("kyc_name_input")
                )
            }

            item {
                DiajakFlatTextField(
                    value = nikNumber,
                    onValueChange = { nikNumber = it.filter { ch -> ch.isDigit() }.take(16) },
                    label = "NIK (16 Digit Angka)",
                    supportingText = "${nikNumber.length}/16 digit",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = DiajakRadius.Control,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("kyc_nik_input")
                )
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(DiajakSpacing.ChipGap)) {
                    Text("Foto Dokumen KTP", style = MaterialTheme.typography.titleSmall)
                    if (docPhotoUri.isNotBlank()) {
                        DiajakPhoto(
                            photoKey = docPhotoUri,
                            contentDescription = "Pratinjau foto KTP",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                                .clip(DiajakRadius.Card)
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(140.dp)
                                .clip(DiajakRadius.Card)
                                .background(MaterialTheme.colorScheme.surfaceContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Rounded.UploadFile,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(DiajakIconSize.Hero)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Belum ada foto KTP dipilih",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(DiajakSpacing.ChipGap)) {
                        DiajakFlatSecondaryButton(
                            onClick = {
                                ktpPhotoPicker.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            shape = DiajakRadius.Control,
                            modifier = Modifier
                                .weight(1f)
                                .height(DiajakComponentSize.PrimaryButtonHeight)
                                .testTag("kyc_pick_photo_button")
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.UploadFile,
                                contentDescription = null,
                                modifier = Modifier.size(DiajakIconSize.Inline)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Pilih Foto KTP", style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }
            }

            item {
                Button(
                    onClick = {
                        viewModel.submitKyc(
                            fullName = fullName,
                            nikNumber = nikNumber,
                            documentPhotoUri = docPhotoUri,
                            onSuccess = onBack
                        )
                    },
                    enabled = !isBusy && fullName.trim().length >= 2 && nikNumber.length == 16 && docPhotoUri.isNotBlank(),
                    shape = DiajakRadius.Control,
                    elevation = ButtonDefaults.buttonElevation(0.dp, 0.dp, 0.dp, 0.dp, 0.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(DiajakComponentSize.PrimaryButtonHeight)
                        .testTag("kyc_submit_button")
                ) {
                    Text(
                        text = if (isVerified) "Perbarui Dokumen KTP" else "Ajukan & Verifikasi KTP",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}
