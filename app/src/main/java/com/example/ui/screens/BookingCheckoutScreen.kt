package com.example.ui.screens

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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.QrCode2
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.ActivityItem
import com.example.data.BookingTicket
import com.example.data.DiajakCatalog
import com.example.data.PaymentMethodOption
import com.example.ui.DiajakViewModel
import com.example.ui.components.DiajakFlatSecondaryButton
import com.example.ui.components.DiajakPageScaffold
import com.example.ui.components.DiajakPhoto
import com.example.ui.components.TicketQrCanvas
import com.example.ui.theme.DiajakComponentSize
import com.example.ui.theme.DiajakIconSize
import com.example.ui.theme.DiajakRadius
import com.example.ui.theme.DiajakSpacing
import com.example.ui.theme.VerifiedGreen
import com.example.ui.theme.VerifiedGreenSurface

@Composable
fun BookingCheckoutScreen(
    activity: ActivityItem,
    viewModel: DiajakViewModel,
    onBack: () -> Unit,
    onGoToTickets: () -> Unit,
    onGoToMessages: () -> Unit
) {
    val isBusy by viewModel.isBusy.collectAsStateWithLifecycle()
    val maxTickets = (activity.quota - activity.joinedCount).coerceIn(1L, 10L).toInt()
    var ticketCount by remember { mutableIntStateOf(1) }
    var selectedPayment by remember { mutableStateOf(PaymentMethodOption.QRIS) }
    var issuedTicket by remember { mutableStateOf<BookingTicket?>(null) }

    val totalAmount = activity.priceIdr * ticketCount
    val isFree = totalAmount == 0L

    DiajakPageScaffold(
        title = if (issuedTicket == null) "Konfirmasi Booking" else "E-Tiket Diterbitkan",
        onBack = onBack,
        backButtonTestTag = "checkout_back_button",
        titleTestTag = "checkout_header_title",
        applyNavigationBarBottomPadding = issuedTicket != null,
        bottomBar = {
            if (issuedTicket == null) {
                Surface(color = MaterialTheme.colorScheme.surface) {
                    Column(modifier = Modifier.navigationBarsPadding()) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "Total Pembayaran",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = DiajakCatalog.formatRupiah(totalAmount),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Button(
                                onClick = {
                                    viewModel.bookActivity(
                                        activity = activity,
                                        ticketCount = ticketCount,
                                        paymentMethodCode = if (isFree) "GRATIS" else selectedPayment.code,
                                        onBooked = { ticket -> issuedTicket = ticket }
                                    )
                                },
                                enabled = !isBusy,
                                shape = DiajakRadius.Control,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                ),
                                elevation = ButtonDefaults.buttonElevation(0.dp, 0.dp, 0.dp, 0.dp, 0.dp),
                                modifier = Modifier
                                    .height(DiajakComponentSize.PrimaryButtonHeight)
                                    .testTag("checkout_confirm_button")
                            ) {
                                if (isBusy) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Text(
                                        text = if (isFree) "Terbitkan E-Tiket" else "Bayar & Terbitkan Tiket",
                                        style = MaterialTheme.typography.labelLarge
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    ) { contentPadding ->
        val ticket = issuedTicket
        if (ticket != null) {
            IssuedTicketSuccessView(
                ticket = ticket,
                onGoToTickets = onGoToTickets,
                onGoToMessages = onGoToMessages,
                contentPadding = contentPadding,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = contentPadding,
                verticalArrangement = Arrangement.spacedBy(DiajakSpacing.SectionGap)
            ) {
                // Activity Summary Card
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(DiajakRadius.Card)
                            .background(MaterialTheme.colorScheme.surfaceContainer)
                            .padding(DiajakSpacing.CardPadding),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        DiajakPhoto(
                            photoKey = activity.photoKeys.firstOrNull() ?: "img_activity_running",
                            contentDescription = activity.title,
                            modifier = Modifier
                                .size(76.dp)
                                .clip(DiajakRadius.Small)
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${activity.categoryEmoji} ${activity.category}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = activity.title,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${DiajakCatalog.formatDatePill(activity.eventDateMillis, activity.timeLabel)} • ${activity.locationName}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Ticket Count Stepper
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(DiajakRadius.Card)
                            .background(MaterialTheme.colorScheme.surfaceContainer)
                            .padding(DiajakSpacing.CardPadding),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Jumlah Tiket",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${DiajakCatalog.formatRupiah(activity.priceIdr)} / orang",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Surface(
                                onClick = { if (ticketCount > 1) ticketCount-- },
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                                tonalElevation = 0.dp,
                                shadowElevation = 0.dp,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Rounded.Remove,
                                        contentDescription = "Kurangi tiket",
                                        tint = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.size(DiajakIconSize.Inline)
                                    )
                                }
                            }
                            Text(
                                text = "$ticketCount",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Surface(
                                onClick = { if (ticketCount < maxTickets) ticketCount++ },
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                                tonalElevation = 0.dp,
                                shadowElevation = 0.dp,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Rounded.Add,
                                        contentDescription = "Tambah tiket",
                                        tint = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.size(DiajakIconSize.Inline)
                                    )
                                }
                            }
                        }
                    }
                }

                // Simulated Payment Method Selection (only for paid activities)
                if (!isFree) {
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(
                                text = "Metode Pembayaran (Simulasi Instan)",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            PaymentMethodOption.entries.forEach { method ->
                                val selected = selectedPayment == method
                                Surface(
                                    onClick = { selectedPayment = method },
                                    shape = DiajakRadius.Card,
                                    color = MaterialTheme.colorScheme.surfaceContainer,
                                    tonalElevation = 0.dp,
                                    shadowElevation = 0.dp,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        RadioButton(
                                            selected = selected,
                                            onClick = { selectedPayment = method }
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = method.label,
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = method.subtitle,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        if (method == PaymentMethodOption.QRIS) {
                                            Icon(
                                                imageVector = Icons.Rounded.QrCode2,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.onSurface,
                                                modifier = Modifier.size(DiajakIconSize.Primary)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    item {
                        Surface(
                            shape = DiajakRadius.Card,
                            color = VerifiedGreenSurface,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(DiajakSpacing.CardPadding),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.CheckCircle,
                                    contentDescription = null,
                                    tint = VerifiedGreen,
                                    modifier = Modifier.size(DiajakIconSize.Inline)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Aktivitas ini Gratis! E-Tiket langsung terbit dan kamu otomatis tergabung dalam Grup Chat peserta.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = VerifiedGreen
                                )
                            }
                        }
                    }
                }

                // Price Breakdown
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(DiajakRadius.Card)
                            .background(MaterialTheme.colorScheme.surfaceContainer)
                            .padding(DiajakSpacing.CardPadding),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Rincian Biaya",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Tiket ($ticketCount x ${DiajakCatalog.formatRupiah(activity.priceIdr)})", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(DiajakCatalog.formatRupiah(totalAmount), style = MaterialTheme.typography.bodyMedium)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Biaya Layanan", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("Gratis", style = MaterialTheme.typography.bodyMedium)
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceContainerHigh)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Total", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Text(DiajakCatalog.formatRupiah(totalAmount), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun IssuedTicketSuccessView(
    ticket: BookingTicket,
    onGoToTickets: () -> Unit,
    onGoToMessages: () -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = contentPadding,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceContainer,
                modifier = Modifier.size(64.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Rounded.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(DiajakIconSize.Hero)
                    )
                }
            }
        }
        item {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Booking Berhasil!",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "E-Tiket tersimpan di menu Tiket & kamu sudah masuk ke Grup Chat aktivitas.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(DiajakRadius.Card)
                    .background(MaterialTheme.colorScheme.surfaceContainer)
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = ticket.activityTitle,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "${DiajakCatalog.formatFullDate(ticket.activityDateMillis)} • ${ticket.activityTimeLabel}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                TicketQrCanvas(
                    code = ticket.ticketCode,
                    modifier = Modifier.size(180.dp)
                )
                Text(
                    text = ticket.ticketCode,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = "${ticket.ticketCount} Tiket • ${ticket.paymentMethod} • ${DiajakCatalog.formatRupiah(ticket.totalPriceIdr)}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onGoToTickets,
                    shape = DiajakRadius.Control,
                    elevation = ButtonDefaults.buttonElevation(0.dp, 0.dp, 0.dp, 0.dp, 0.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(DiajakComponentSize.PrimaryButtonHeight)
                        .testTag("ticket_go_tickets_button")
                ) {
                    Text("Lihat di Menu Tiket", style = MaterialTheme.typography.labelLarge)
                }
                DiajakFlatSecondaryButton(
                    onClick = onGoToMessages,
                    shape = DiajakRadius.Control,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(DiajakComponentSize.PrimaryButtonHeight)
                ) {
                    Text("Buka Grup Chat Aktivitas", style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}
