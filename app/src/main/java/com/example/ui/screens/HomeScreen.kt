package com.example.ui.screens

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.pullToRefresh
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.ActivityItem
import com.example.data.DiajakCatalog
import com.example.data.UiState
import com.example.ui.DiajakViewModel
import com.example.ui.components.ActivityFeedCard
import com.example.ui.components.DiajakFlatTextField
import com.example.ui.components.DiajakPhoto
import com.example.ui.components.EmptyStatePanel
import com.example.ui.components.InstagramSpinner
import com.example.ui.components.NicheChipItem
import com.example.ui.components.NichePickerBottomSheet
import com.example.ui.theme.DiajakComponentSize
import com.example.ui.theme.DiajakIconSize
import com.example.ui.theme.DiajakRadius
import com.example.ui.theme.DiajakSpacing
import com.example.ui.theme.HeartRed
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: DiajakViewModel,
    onActivityClick: (ActivityItem) -> Unit,
    onOpenProfile: () -> Unit,
    onOpenNotifications: () -> Unit,
    onToggleFavorite: (ActivityItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val profileState by viewModel.userProfileState.collectAsStateWithLifecycle()
    val pagedFeedState by viewModel.pagedFeedState.collectAsStateWithLifecycle()
    val filters by viewModel.filters.collectAsStateWithLifecycle()
    val favoriteIds by viewModel.favoriteIdsState.collectAsStateWithLifecycle()
    val unreadNotifCount by viewModel.unreadNotificationCount.collectAsStateWithLifecycle()
    val isRefreshingFeed by viewModel.isRefreshingFeed.collectAsStateWithLifecycle()

    var showNicheSheet by remember { mutableStateOf(false) }
    val pullToRefreshState = rememberPullToRefreshState()
    val listState = rememberLazyListState()

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            viewModel.detectGpsLocation(silent = true)
        }
    }

    // Automatically detect user location as soon as the app opens
    LaunchedEffect(Unit) {
        if (viewModel.hasLocationPermission()) {
            viewModel.detectGpsLocation(silent = true)
        } else if (viewModel.shouldRequestAutoLocationPermission()) {
            viewModel.markAutoLocationPermissionRequested()
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
        }
    }

    val profileData = (profileState as? UiState.Success)?.data
    val displayName = remember(viewModel.isSignedIn, profileData) {
        if (!viewModel.isSignedIn) {
            ""
        } else {
            profileData?.displayName?.takeIf { it.isNotBlank() }
                ?: Firebase.auth.currentUser?.displayName.orEmpty()
        }
    }
    val firstName = remember(viewModel.isSignedIn, displayName) {
        if (!viewModel.isSignedIn) {
            "Sobat"
        } else {
            displayName.trim().substringBefore(" ").ifBlank { "Sobat" }
        }
    }
    val avatarInitials = remember(displayName) {
        displayName.trim().split(" ")
            .filter { it.isNotBlank() }
            .take(2)
            .joinToString("") { it.first().uppercase() }
            .ifBlank { "D" }
    }
    val userPhotoUrl = remember(viewModel.isSignedIn, profileData) {
        if (!viewModel.isSignedIn) {
            ""
        } else {
            profileData?.photoUrl?.takeIf { it.isNotBlank() }
                ?: Firebase.auth.currentUser?.photoUrl?.toString().orEmpty()
        }
    }
    val hasProfilePhoto = userPhotoUrl.startsWith("http://") ||
        userPhotoUrl.startsWith("https://") ||
        userPhotoUrl.startsWith("content://") ||
        userPhotoUrl.startsWith("file://") ||
        userPhotoUrl.startsWith("img_")

    val refreshHoldFraction by animateFloatAsState(
        targetValue = if (isRefreshingFeed) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "home_refresh_hold_fraction"
    )
    val pullSlotHeight = 52.dp
    val pullThreshold = 72.dp
    val effectivePullFraction = maxOf(
        pullToRefreshState.distanceFraction.coerceIn(0f, 1.35f),
        refreshHoldFraction
    )
    val density = LocalDensity.current
    val contentOffsetPx = with(density) { (pullSlotHeight * effectivePullFraction).toPx() }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        // PINNED HEADER: Avatar (48dp), 2-line greeting (48dp), & Notification Bell (48dp)
        // sejajar rata atas-bawah serta rata tepi kiri-kanan dengan kolom pencarian
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
                .padding(
                    start = DiajakSpacing.ScreenHorizontal,
                    end = DiajakSpacing.ScreenHorizontal,
                    top = 14.dp,
                    bottom = 12.dp
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(DiajakSpacing.ItemGap)
        ) {
            // Profile Avatar (ukuran sama persis dengan bulatan ikon notifikasi lonceng)
            Box(
                modifier = Modifier
                    .size(DiajakComponentSize.ActionCircle)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceContainer)
                    .clickable(onClick = onOpenProfile)
                    .testTag("home_profile_avatar"),
                contentAlignment = Alignment.Center
            ) {
                if (viewModel.isSignedIn) {
                    Text(
                        text = avatarInitials,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (hasProfilePhoto) {
                        DiajakPhoto(
                            photoKey = userPhotoUrl,
                            contentDescription = stringResource(R.string.cd_profile_avatar),
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                } else {
                    Icon(
                        imageVector = Icons.Outlined.PersonOutline,
                        contentDescription = stringResource(R.string.cd_profile_avatar),
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(DiajakIconSize.Primary)
                    )
                }
            }

            // Two-line welcome text: sapaan tebal hitam di atas & subjudul kecil abu-abu sangat rapat di bawah
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                Text(
                    text = "Halo $firstName",
                    style = MaterialTheme.typography.titleLarge.copy(
                        lineHeight = 20.sp,
                        platformStyle = PlatformTextStyle(includeFontPadding = false),
                        lineHeightStyle = LineHeightStyle(
                            alignment = LineHeightStyle.Alignment.Center,
                            trim = LineHeightStyle.Trim.Both
                        )
                    ),
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.testTag("home_greeting_text")
                )

                Text(
                    text = stringResource(R.string.home_greeting_subtitle),
                    style = MaterialTheme.typography.bodySmall.copy(
                        lineHeight = 15.sp,
                        platformStyle = PlatformTextStyle(includeFontPadding = false),
                        lineHeightStyle = LineHeightStyle(
                            alignment = LineHeightStyle.Alignment.Center,
                            trim = LineHeightStyle.Trim.Both
                        )
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.testTag("home_greeting_subtitle")
                )
            }

            // Notification Bell
            Box(
                modifier = Modifier
                    .size(DiajakComponentSize.ActionCircle)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceContainer)
                    .clickable(onClick = onOpenNotifications)
                    .testTag("home_notifications_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Notifications,
                    contentDescription = "Notifikasi",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(DiajakIconSize.Primary)
                )
                if (unreadNotifCount > 0) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(10.dp)
                            .size(9.dp)
                            .clip(CircleShape)
                            .background(HeartRed)
                    )
                }
            }
        }

        // SCROLLABLE FEED AREA DENGAN INSTAGRAM PUSH-DOWN PULL-TO-REFRESH
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clipToBounds()
                .pullToRefresh(
                    isRefreshing = isRefreshingFeed,
                    state = pullToRefreshState,
                    threshold = pullThreshold,
                    onRefresh = viewModel::refreshDynamicFeed
                )
        ) {
            if (effectivePullFraction > 0.02f || isRefreshingFeed) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(pullSlotHeight * effectivePullFraction)
                        .align(Alignment.TopCenter)
                        .testTag("home_pull_refresh_spinner"),
                    contentAlignment = Alignment.Center
                ) {
                    InstagramSpinner(
                        isSpinning = isRefreshingFeed,
                        progress = effectivePullFraction.coerceIn(0f, 1f),
                        size = 26.dp,
                        contentDescription = "Menyegarkan beranda"
                    )
                }
            }

            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { translationY = contentOffsetPx },
                contentPadding = PaddingValues(top = 4.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(DiajakSpacing.SectionGap)
            ) {
                // SEARCH BAR
                item(key = "home_search_bar") {
                    DiajakFlatTextField(
                        value = filters.searchQuery,
                        onValueChange = viewModel::updateSearchQuery,
                        placeholder = stringResource(R.string.home_search_placeholder),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Rounded.Search,
                                contentDescription = "Cari aktivitas",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(DiajakIconSize.Primary)
                            )
                        },
                        trailingIcon = {
                            if (filters.searchQuery.isNotEmpty()) {
                                Icon(
                                    imageVector = Icons.Rounded.Close,
                                    contentDescription = "Hapus pencarian",
                                    tint = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier
                                        .size(DiajakIconSize.Inline)
                                        .clickable { viewModel.updateSearchQuery("") }
                                )
                            }
                        },
                        shape = DiajakRadius.Control,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = DiajakSpacing.ScreenHorizontal)
                            .testTag("home_search_input")
                    )
                }

                // NICHE CATEGORY STRIP (69 Niches via All button + Quick Horizontal Chips)
                item(key = "home_niche_strip") {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = DiajakSpacing.ScreenHorizontal),
                        horizontalArrangement = Arrangement.spacedBy(DiajakSpacing.ChipGap)
                    ) {
                        item {
                            NicheChipItem(
                                label = "Semua",
                                selected = filters.selectedNiche == null,
                                onClick = { viewModel.selectNiche(null) },
                                modifier = Modifier.testTag("niche_chip_all")
                            )
                        }
                        item {
                            Surface(
                                onClick = { showNicheSheet = true },
                                shape = DiajakRadius.Pill,
                                color = MaterialTheme.colorScheme.surfaceContainer,
                                tonalElevation = 0.dp,
                                shadowElevation = 0.dp,
                                modifier = Modifier.testTag("open_all_niches_button")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 11.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.GridView,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.size(DiajakIconSize.Inline)
                                    )
                                    Text(
                                        text = "69 Kategori",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                        // If user picked a niche from the 69 sheet that isn't in quickFilterNiches, show it pinned first
                        val selectedCustom = filters.selectedNiche?.let { sel ->
                            if (DiajakCatalog.quickFilterNiches.none { it.name.equals(sel, ignoreCase = true) }) {
                                DiajakCatalog.allNiches.firstOrNull { it.name.equals(sel, ignoreCase = true) }
                            } else null
                        }
                        if (selectedCustom != null) {
                            item {
                                NicheChipItem(
                                    label = selectedCustom.name,
                                    emoji = selectedCustom.emoji,
                                    selected = true,
                                    onClick = { viewModel.selectNiche(null) }
                                )
                            }
                        }
                        items(DiajakCatalog.quickFilterNiches) { niche ->
                            val active = filters.selectedNiche.equals(niche.name, ignoreCase = true)
                            NicheChipItem(
                                label = niche.name,
                                emoji = niche.emoji,
                                selected = active,
                                onClick = { viewModel.selectNiche(if (active) null else niche.name) }
                            )
                        }
                    }
                }

                // VERTICAL ACTIVITY FEED (Dynamic TikTok / Instagram-style vertical card stream)
                when (val state = pagedFeedState) {
                    is UiState.Loading -> {
                        item(key = "feed_initial_loading") {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 64.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                InstagramSpinner(
                                    isSpinning = true,
                                    size = 28.dp,
                                    contentDescription = "Memuat aktivitas"
                                )
                            }
                        }
                    }
                    is UiState.Error -> {
                        item(key = "feed_error_state") {
                            EmptyStatePanel(
                                icon = Icons.Rounded.Explore,
                                title = "Gagal memuat aktivitas",
                                subtitle = state.message
                            )
                        }
                    }
                    is UiState.Success -> {
                        val paged = state.data
                        if (paged.items.isEmpty()) {
                            item(key = "feed_empty_state") {
                                EmptyStatePanel(
                                    icon = Icons.Rounded.Explore,
                                    title = "Belum ada aktivitas cocok",
                                    subtitle = "Coba ubah kategori atau kata kunci pencarianmu.",
                                    actionLabel = "Reset Pencarian",
                                    onAction = {
                                        viewModel.updateSearchQuery("")
                                        viewModel.selectNiche(null)
                                    }
                                )
                            }
                        } else {
                            items(paged.items, key = { it.id }) { activity ->
                                ActivityFeedCard(
                                    activity = activity,
                                    distanceKm = viewModel.distanceToActivityKm(activity),
                                    favorited = favoriteIds.contains(activity.id),
                                    onCardClick = {
                                        viewModel.recordActivityInteraction(activity)
                                        onActivityClick(activity)
                                    },
                                    onToggleFavorite = { onToggleFavorite(activity) },
                                    modifier = Modifier.padding(horizontal = 20.dp)
                                )
                            }

                            // INSTAGRAM-STYLE BOTTOM LOADING SPINNER UNTUK BATCH BERIKUTNYA
                            if (paged.hasMore || paged.isLoadingMore) {
                                item(key = "feed_load_more_footer") {
                                    LaunchedEffect(paged.items.size, paged.hasMore) {
                                        if (paged.hasMore) {
                                            viewModel.loadMoreFeedActivities()
                                        }
                                    }
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 22.dp)
                                            .testTag("home_bottom_load_more_spinner"),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        InstagramSpinner(
                                            isSpinning = true,
                                            size = 26.dp,
                                            contentDescription = "Memuat aktivitas lainnya"
                                        )
                                    }
                                }
                            } else {
                                item(key = "feed_end_of_list") {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 8.dp, bottom = 12.dp)
                                            .testTag("home_feed_end_indicator"),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "Semua aktivitas telah ditampilkan",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showNicheSheet) {
        NichePickerBottomSheet(
            selectedNicheName = filters.selectedNiche,
            allowClearAll = true,
            onSelectNiche = { viewModel.selectNiche(it?.name) },
            onDismiss = { showNicheSheet = false }
        )
    }
}
