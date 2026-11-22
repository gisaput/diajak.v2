package com.example.ui.screens

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ConfirmationNumber
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.QrCode2
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.ActivityItem
import com.example.data.BookingTicket
import com.example.data.DiajakCatalog
import com.example.data.UiState
import com.example.ui.DiajakViewModel
import com.example.ui.components.ActivityFeedCard
import com.example.ui.components.DiajakFlatSecondaryButton
import com.example.ui.components.DiajakPhoto
import com.example.ui.components.EmptyStatePanel
import com.example.ui.components.TicketQrCanvas
import com.example.ui.theme.DiajakComponentSize
import com.example.ui.theme.DiajakIconSize
import com.example.ui.theme.DiajakRadius
import com.example.ui.theme.DiajakSpacing
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.ErrorRedSurface
import com.example.ui.theme.VerifiedGreen
import com.example.ui.theme.VerifiedGreenSurface
import com.example.ui.theme.WarningAmber
import com.example.ui.theme.WarningAmberSurface

@Composable
fun FavoritesScreen(
    viewModel: DiajakViewModel,
    onActivityClick: (ActivityItem) -> Unit,
    onExploreClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activitiesState by viewModel.publishedActivitiesState.collectAsStateWithLifecycle()
    val favoriteIds by viewModel.favoriteIdsState.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp)) {
            Text(
                text = "Favorit",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Aktivitas yang kamu sukai tersimpan rapi di sini.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        when (val state = activitiesState) {
            is UiState.Loading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }
            is UiState.Error -> {
                EmptyStatePanel(
                    icon = Icons.Rounded.FavoriteBorder,
                    title = "Gagal memuat favorit",
                    subtitle = state.message
                )
            }
            is UiState.Success -> {
                val favActivities = remember(state.data, favoriteIds) {
                    state.data.filter { favoriteIds.contains(it.id) }
                }
                if (favActivities.isEmpty()) {
                    EmptyStatePanel(
                        icon = Icons.Rounded.FavoriteBorder,
                        title = "Belum ada aktivitas favorit",
                        subtitle = "Ketuk ikon hati pada kartu aktivitas di Beranda agar mudah kamu temukan kembali.",
                        actionLabel = "Jelajahi Beranda",
                        onAction = onExploreClick
                    )
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(favActivities, key = { it.id }) { activity ->
                            ActivityFeedCard(
                                activity = activity,
                                distanceKm = viewModel.distanceToActivityKm(activity),
                                favorited = true,
                                onCardClick = { onActivityClick(activity) },
                                onToggleFavorite = { viewModel.toggleFavorite(activity) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TicketsScreen(
    viewModel: DiajakViewModel,
    onExploreClick: () -> Unit,
    onOpenGroupChat: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val bookingsState by viewModel.participantBookingsState.collectAsStateWithLifecycle()
    var selectedTicket by remember { mutableStateOf<BookingTicket?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp)) {
            Text(
                text = "Tiket",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Bukti booking dan kode QR kehadiran aktivitasmu.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        when (val state = bookingsState) {
            is UiState.Loading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }
            is UiState.Error -> {
                EmptyStatePanel(
                    icon = Icons.Rounded.ConfirmationNumber,
                    title = "Gagal memuat tiket",
                    subtitle = state.message
                )
            }
            is UiState.Success -> {
                if (state.data.isEmpty()) {
                    EmptyStatePanel(
                        icon = Icons.Rounded.ConfirmationNumber,
                        title = "Belum ada E-Tiket aktif",
                        subtitle = "Pilih aktivitas di Beranda lalu tekan Ikuti untuk memesan tiket dan bergabung ke grup peserta.",
                        actionLabel = "Cari Aktivitas",
                        onAction = onExploreClick
                    )
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        items(state.data, key = { it.id }) { ticket ->
                            TicketCard(
                                ticket = ticket,
                                onClick = { selectedTicket = ticket }
                            )
                        }
                    }
                }
            }
        }
    }

    val activeTicket = selectedTicket
    if (activeTicket != null) {
        ModalBottomSheet(
            onDismissRequest = { selectedTicket = null },
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 0.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TicketStatusPill(status = activeTicket.status)
                Text(
                    text = activeTicket.activityTitle,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${DiajakCatalog.formatFullDate(activeTicket.activityDateMillis)} • ${activeTicket.activityTimeLabel}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = activeTicket.activityLocation,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Box(
                    modifier = Modifier
                        .clip(DiajakRadius.Card)
                        .background(MaterialTheme.colorScheme.surfaceContainer)
                        .padding(14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    TicketQrCanvas(
                        code = activeTicket.ticketCode,
                        modifier = Modifier.size(190.dp)
                    )
                }

                Text(
                    text = activeTicket.ticketCode,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = "${activeTicket.ticketCount} Tiket • ${activeTicket.paymentMethod} • ${DiajakCatalog.formatRupiah(activeTicket.totalPriceIdr)}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(4.dp))

                Button(
                    onClick = {
                        val groupId = "group_${activeTicket.activityId}".take(128)
                        selectedTicket = null
                        onOpenGroupChat(groupId)
                    },
                    shape = DiajakRadius.Control,
                    elevation = ButtonDefaults.buttonElevation(0.dp, 0.dp, 0.dp, 0.dp, 0.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(DiajakComponentSize.PrimaryButtonHeight)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Groups,
                        contentDescription = null,
                        modifier = Modifier.size(DiajakIconSize.Inline)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Buka Grup Chat Aktivitas", style = MaterialTheme.typography.labelLarge)
                }

                if (activeTicket.status == "CONFIRMED") {
                    DiajakFlatSecondaryButton(
                        onClick = {
                            viewModel.cancelParticipantTicket(activeTicket.id)
                            selectedTicket = null
                        },
                        contentColor = ErrorRed,
                        shape = DiajakRadius.Control,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(DiajakComponentSize.PrimaryButtonHeight)
                    ) {
                        Text("Batalkan Tiket", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }
    }
}

@Composable
private fun TicketCard(
    ticket: BookingTicket,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(DiajakRadius.Card)
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .clickable(onClick = onClick)
            .padding(DiajakSpacing.CardPadding)
            .testTag("ticket_card_${ticket.id}"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            DiajakPhoto(
                photoKey = ticket.activityPhotoKey.ifBlank { "img_activity_running" },
                contentDescription = ticket.activityTitle,
                modifier = Modifier
                    .size(72.dp)
                    .clip(DiajakRadius.Small)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                TicketStatusPill(status = ticket.status)
                Spacer(modifier = Modifier.height(5.dp))
                Text(
                    text = ticket.activityTitle,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(3.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.LocationOn,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(DiajakIconSize.Inline)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = ticket.activityLocation,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceContainerHigh)
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "${DiajakCatalog.formatFullDate(ticket.activityDateMillis)} • ${ticket.activityTimeLabel}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Kode: ${ticket.ticketCode} (${ticket.ticketCount} tiket)",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            DiajakFlatSecondaryButton(
                onClick = onClick,
                nested = true,
                shape = DiajakRadius.Small,
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.QrCode2,
                    contentDescription = null,
                    modifier = Modifier.size(DiajakIconSize.Inline)
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text("Lihat QR", style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

@Composable
fun TicketStatusPill(status: String) {
    val (bg, fg, label) = when (status) {
        "CHECKED_IN" -> Triple(VerifiedGreenSurface, VerifiedGreen, "Sudah Check-in")
        "CANCELLED" -> Triple(ErrorRedSurface, ErrorRed, "Dibatalkan")
        else -> Triple(WarningAmberSurface, WarningAmber, "Terkonfirmasi")
    }
    Surface(
        shape = DiajakRadius.Pill,
        color = bg
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = fg,
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp)
        )
    }
}
