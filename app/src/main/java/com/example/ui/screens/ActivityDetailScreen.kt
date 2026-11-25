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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Chat
import androidx.compose.material.icons.rounded.AccessTime
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.RateReview
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.ActivityItem
import com.example.data.ActivityReview
import com.example.data.DiajakCatalog
import com.example.ui.DiajakViewModel
import com.example.ui.components.DiajakFlatSecondaryButton
import com.example.ui.components.DiajakFlatTextField
import com.example.ui.components.DiajakPhoto
import com.example.ui.components.InitialAvatar
import com.example.ui.components.RatingStars
import com.example.ui.components.dismissKeyboardOnOutsideTouch
import com.example.ui.theme.DiajakComponentSize
import com.example.ui.theme.DiajakHeaderSpec
import com.example.ui.theme.DiajakIconSize
import com.example.ui.theme.DiajakPhotoOverlay
import com.example.ui.theme.DiajakRadius
import com.example.ui.theme.DiajakSpacing
import com.example.ui.theme.HeartRed
import com.example.ui.theme.StarGold
import com.example.ui.theme.VerifiedBlue
import com.example.ui.theme.VerifiedGreen
import com.example.ui.theme.VerifiedGreenSurface
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityDetailScreen(
    activity: ActivityItem,
    viewModel: DiajakViewModel,
    onBack: () -> Unit,
    onBookActivity: (ActivityItem) -> Unit,
    onToggleFavorite: (ActivityItem) -> Unit,
    onChatOrganizer: (ActivityItem) -> Unit,
    onRequireAuthForReview: () -> Unit
) {
    val favoriteIds by viewModel.favoriteIdsState.collectAsStateWithLifecycle()
    val isFavorited = favoriteIds.contains(activity.id)
    val reviewsFlow = remember(activity.id) { viewModel.observeReviews(activity.id) }
    val reviews by reviewsFlow.collectAsStateWithLifecycle(initialValue = emptyList())

    val photos = remember(activity.photoKeys) {
        activity.photoKeys.ifEmpty { listOf("img_activity_running") }
    }
    val pagerState = rememberPagerState(pageCount = { photos.size })

    var showParticipantsSheet by remember { mutableStateOf(false) }
    var showReviewDialog by remember { mutableStateOf(false) }
    val seatsLeft = (activity.quota - activity.joinedCount).coerceAtLeast(0L)

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        // NON-FLOATING Material Design docked bottom bar (like Airbnb)
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 0.dp
            ) {
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
                                text = DiajakCatalog.formatRupiah(activity.priceIdr),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (seatsLeft > 0) "Sisa $seatsLeft slot tersedia" else "Kuota penuh",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Button(
                            onClick = { onBookActivity(activity) },
                            enabled = seatsLeft > 0 && activity.status == "PUBLISHED",
                            shape = DiajakRadius.Control,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            elevation = ButtonDefaults.buttonElevation(0.dp, 0.dp, 0.dp, 0.dp, 0.dp),
                            modifier = Modifier
                                .height(DiajakComponentSize.PrimaryButtonHeight)
                                .testTag("detail_join_button")
                        ) {
                            Text(
                                text = "Ikuti",
                                style = MaterialTheme.typography.labelLarge,
                                modifier = Modifier.padding(horizontal = 24.dp)
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding()),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            // 1. HERO SWIPEABLE PHOTO CAROUSEL WITH OVERLAY TITLE, DATE & LOCATION PILLS (matches reference image)
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(430.dp)
                        .background(DiajakPhotoOverlay.Scrim)
                ) {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize()
                    ) { page ->
                        DiajakPhoto(
                            photoKey = photos[page],
                            contentDescription = "${activity.title} foto ${page + 1}",
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    // Bottom gradient scrim for sharp white title + pills readability
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    0.0f to DiajakPhotoOverlay.Scrim.copy(alpha = 0.35f),
                                    0.35f to Color.Transparent,
                                    0.58f to DiajakPhotoOverlay.Scrim.copy(alpha = 0.25f),
                                    1.0f to DiajakPhotoOverlay.Scrim.copy(alpha = 0.78f)
                                )
                            )
                    )

                    // Top bar controls (Back left, Heart right) — 100% identik posisi & diameter 48.dp dengan Beranda & DiajakPageHeader
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(
                                start = DiajakSpacing.ScreenHorizontal,
                                end = DiajakSpacing.ScreenHorizontal,
                                top = DiajakHeaderSpec.PaddingTop,
                                bottom = DiajakHeaderSpec.PaddingBottom
                            )
                            .height(DiajakHeaderSpec.ContentHeight)
                            .align(Alignment.TopCenter),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            onClick = onBack,
                            shape = CircleShape,
                            color = DiajakPhotoOverlay.ChipSurface.copy(alpha = 0.94f),
                            modifier = Modifier
                                .size(DiajakHeaderSpec.ButtonSize)
                                .testTag("detail_back_button")
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                    contentDescription = "Kembali",
                                    tint = DiajakPhotoOverlay.OnChipSurface,
                                    modifier = Modifier.size(DiajakIconSize.Primary)
                                )
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Surface(
                                onClick = { onToggleFavorite(activity) },
                                shape = CircleShape,
                                color = DiajakPhotoOverlay.ChipSurface.copy(alpha = 0.94f),
                                modifier = Modifier
                                    .size(DiajakHeaderSpec.ButtonSize)
                                    .testTag("detail_favorite_button")
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = if (isFavorited) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                                        contentDescription = "Favorit",
                                        tint = if (isFavorited) HeartRed else DiajakPhotoOverlay.OnChipSurface,
                                        modifier = Modifier.size(DiajakIconSize.Primary)
                                    )
                                }
                            }
                        }
                    }

                    // Bottom overlay inside hero photo: Title, Date pill + Location pill, Pager dots
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomStart)
                            .padding(horizontal = DiajakSpacing.ScreenHorizontal, vertical = 20.dp)
                    ) {
                        Text(
                            text = activity.title,
                            style = MaterialTheme.typography.headlineMedium,
                            color = DiajakPhotoOverlay.OnPhotoText
                        )

                        Spacer(modifier = Modifier.height(DiajakSpacing.ItemGap))

                        // Date Pill + Location Pill directly below title on photo
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(DiajakSpacing.SmallGap),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = DiajakRadius.Pill,
                                color = DiajakPhotoOverlay.ChipSurface
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.AccessTime,
                                        contentDescription = null,
                                        tint = DiajakPhotoOverlay.OnChipSurface,
                                        modifier = Modifier.size(DiajakIconSize.Inline)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = DiajakCatalog.formatDatePill(activity.eventDateMillis, activity.timeLabel),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = DiajakPhotoOverlay.OnChipSurface
                                    )
                                }
                            }

                            Surface(
                                shape = DiajakRadius.Pill,
                                color = DiajakPhotoOverlay.ChipSurface,
                                modifier = Modifier.weight(1f, fill = false)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.LocationOn,
                                        contentDescription = null,
                                        tint = DiajakPhotoOverlay.OnChipSurface,
                                        modifier = Modifier.size(DiajakIconSize.Inline)
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text(
                                        text = activity.locationName,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = DiajakPhotoOverlay.OnChipSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }

                        if (photos.size > 1) {
                            Spacer(modifier = Modifier.height(14.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                repeat(photos.size) { index ->
                                    val selected = pagerState.currentPage == index
                                    Box(
                                        modifier = Modifier
                                            .padding(horizontal = 3.dp)
                                            .height(6.dp)
                                            .width(if (selected) 20.dp else 6.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (selected) DiajakPhotoOverlay.OnPhotoText
                                                else DiajakPhotoOverlay.OnPhotoText.copy(alpha = 0.45f)
                                            )
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 2. TENTANG AKTIVITAS
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 20.dp)
                ) {
                    Text(
                        text = "Tentang Aktivitas",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(DiajakRadius.Card)
                            .background(MaterialTheme.colorScheme.surfaceContainer)
                            .padding(DiajakSpacing.CardPadding)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(
                                text = activity.description,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceContainerHigh)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Tanggal: ${DiajakCatalog.formatFullDate(activity.eventDateMillis)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "${activity.joinedCount}/${activity.quota} Peserta",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }

            // 3. YANG DIDAPAT
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = DiajakSpacing.ScreenHorizontal)
                        .padding(bottom = 20.dp)
                ) {
                    Text(
                        text = "Yang Didapat",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(DiajakRadius.Card)
                            .background(MaterialTheme.colorScheme.surfaceContainer)
                            .padding(DiajakSpacing.CardPadding),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        val benefits = activity.benefits.ifEmpty { listOf("Akses penuh sesi aktivitas & grup peserta") }
                        benefits.forEach { benefit ->
                            Row(
                                verticalAlignment = Alignment.Top,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.CheckCircle,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier
                                        .padding(top = 2.dp)
                                        .size(DiajakIconSize.Inline)
                                )
                                Text(
                                    text = benefit,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }

            // 4. AVATAR ORGANIZER (Clickable -> Direct Chat Peserta & Organizer!)
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = DiajakSpacing.ScreenHorizontal)
                        .padding(bottom = 20.dp)
                ) {
                    Text(
                        text = "Penyelenggara",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        onClick = { onChatOrganizer(activity) },
                        shape = DiajakRadius.Card,
                        color = MaterialTheme.colorScheme.surfaceContainer,
                        tonalElevation = 0.dp,
                        shadowElevation = 0.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("detail_organizer_chat_card")
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            InitialAvatar(
                                name = activity.organizerName,
                                photoKey = activity.organizerAvatarKey,
                                size = 54.dp
                            )
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                                ) {
                                    Text(
                                        text = activity.organizerName,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    if (activity.organizerVerified) {
                                        Icon(
                                            imageVector = Icons.Rounded.Verified,
                                            contentDescription = "Penyelenggara terverifikasi",
                                            tint = VerifiedBlue,
                                            modifier = Modifier.size(DiajakIconSize.Inline)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = "Ketuk untuk chat langsung dengan penyelenggara",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                                modifier = Modifier.size(DiajakComponentSize.ActionCircle)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Rounded.Chat,
                                        contentDescription = "Chat penyelenggara",
                                        tint = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.size(DiajakIconSize.Primary)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 5. PESERTA BERGABUNG
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = DiajakSpacing.ScreenHorizontal)
                        .padding(bottom = 20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Peserta Bergabung (${activity.joinedCount})",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Lihat Semua",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier
                                .clip(DiajakRadius.Small)
                                .clickable { showParticipantsSheet = true }
                                .padding(horizontal = 6.dp, vertical = 4.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    if (activity.joinedUserNames.isEmpty()) {
                        Text(
                            text = "Jadilah peserta pertama yang bergabung di aktivitas ini!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                            items(activity.joinedUserNames.take(8)) { person ->
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.width(68.dp)
                                ) {
                                    InitialAvatar(name = person, size = 54.dp, nested = false)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = person.substringBefore(" "),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 6. DAFTAR ULASAN AKTIVITAS
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = DiajakSpacing.ScreenHorizontal)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "Ulasan Aktivitas",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (activity.reviewCount > 0) {
                                Surface(
                                    shape = DiajakRadius.Pill,
                                    color = MaterialTheme.colorScheme.surfaceContainer
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Star,
                                            contentDescription = null,
                                            tint = StarGold,
                                            modifier = Modifier.size(DiajakIconSize.Inline)
                                        )
                                        Text(
                                            text = String.format(Locale("in", "ID"), "%.1f (%d)", activity.averageRating, activity.reviewCount),
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                        DiajakFlatSecondaryButton(
                            onClick = {
                                if (viewModel.isSignedIn) {
                                    showReviewDialog = true
                                } else {
                                    onRequireAuthForReview()
                                }
                            },
                            shape = DiajakRadius.Small,
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("write_review_button")
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.RateReview,
                                contentDescription = null,
                                modifier = Modifier.size(DiajakIconSize.Inline)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Tulis Ulasan", style = MaterialTheme.typography.labelMedium)
                        }
                    }

                    Spacer(modifier = Modifier.height(DiajakSpacing.ItemGap))

                    if (reviews.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(DiajakRadius.Card)
                                .background(MaterialTheme.colorScheme.surfaceContainer)
                                .padding(DiajakSpacing.CardPadding)
                        ) {
                            Text(
                                text = "Belum ada ulasan tertulis. Bagikan pengalamanmu setelah mengikuti aktivitas ini!",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            reviews.forEach { rev ->
                                ReviewItemCard(review = rev)
                            }
                        }
                    }
                }
            }
        }
    }

    // Participant Group Sheet (inspired by 3rd panel in reference image)
    if (showParticipantsSheet) {
        ModalBottomSheet(
            onDismissRequest = { showParticipantsSheet = false },
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 0.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 28.dp)
            ) {
                Text(
                    text = "Grup Peserta",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${activity.title} • ${activity.joinedCount} peserta bergabung",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
                val names = activity.joinedUserNames.ifEmpty { listOf(activity.organizerName) }
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    names.forEachIndexed { idx, name ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(DiajakRadius.Control)
                                .background(MaterialTheme.colorScheme.surfaceContainer)
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            InitialAvatar(name = name, size = 44.dp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = name,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = if (idx == 0) "Aktif di komunitas ${activity.category}" else "Siap seru-seruan bareng di ${activity.city}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Surface(
                                shape = DiajakRadius.Pill,
                                color = VerifiedGreenSurface
                            ) {
                                Text(
                                    text = "Terdaftar",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = VerifiedGreen,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showReviewDialog) {
        WriteReviewDialog(
            onDismiss = { showReviewDialog = false },
            onSubmit = { rating, comment ->
                viewModel.submitReview(activity, rating, comment) {
                    showReviewDialog = false
                }
            }
        )
    }
}

@Composable
private fun ReviewItemCard(review: ActivityReview) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(DiajakRadius.Card)
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                InitialAvatar(name = review.authorName, size = 34.dp)
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = review.authorName,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            RatingStars(rating = review.rating.toInt(), starSize = DiajakIconSize.Inline)
        }
        Text(
            text = review.comment,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        if (review.organizerReply.isNotBlank()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(DiajakRadius.Small)
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                    .padding(10.dp)
            ) {
                Text(
                    text = "Balasan Penyelenggara",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = review.organizerReply,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun WriteReviewDialog(
    onDismiss: () -> Unit,
    onSubmit: (Int, String) -> Unit
) {
    var rating by remember { mutableIntStateOf(5) }
    var comment by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.dismissKeyboardOnOutsideTouch(),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp,
        title = {
            Text(text = "Tulis Ulasan Aktivitas", style = MaterialTheme.typography.titleLarge)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Beri penilaian pengalamanmu:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                RatingStars(
                    rating = rating,
                    onRatingSelected = { rating = it },
                    starSize = DiajakIconSize.Primary
                )
                DiajakFlatTextField(
                    value = comment,
                    onValueChange = { comment = it },
                    label = "Ulasan kamu",
                    placeholder = "Ceritakan keseruan aktivitas & penyelenggaranya...",
                    singleLine = false,
                    minLines = 3,
                    maxLines = 5,
                    shape = DiajakRadius.Control,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSubmit(rating, comment) },
                enabled = comment.trim().length >= 2,
                shape = DiajakRadius.Control,
                elevation = ButtonDefaults.buttonElevation(0.dp, 0.dp, 0.dp, 0.dp, 0.dp)
            ) {
                Text("Kirim")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}
