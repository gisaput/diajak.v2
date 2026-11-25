package com.example.ui.screens

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.AnimationState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.animateDecay
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.rememberSplineBasedDecay
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.rememberScrollableState
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Notifications
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
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.ActivityItem
import com.example.data.DiajakCatalog
import com.example.data.UiState
import com.example.ui.DiajakViewModel
import com.example.ui.components.ActivityFeedCard
import com.example.ui.components.ActivityFeedCardSkeleton
import com.example.ui.components.DiajakFlatTextField
import com.example.ui.components.EmptyStatePanel
import com.example.ui.components.InstagramSpinner
import com.example.ui.components.NicheChipItem
import com.example.ui.components.NichePickerBottomSheet
import com.example.ui.components.rememberDiajakShimmerProgress
import com.example.ui.theme.DiajakHeaderSpec
import com.example.ui.theme.DiajakIconSize
import com.example.ui.theme.DiajakRadius
import com.example.ui.theme.DiajakSpacing
import com.example.ui.theme.HeartRed
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: DiajakViewModel,
    onActivityClick: (ActivityItem) -> Unit,
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
    val decaySpec = rememberSplineBasedDecay<Float>()
    val greetingHeightHolder = remember { FloatArray(1) }
    var headerOffsetPx by remember { mutableFloatStateOf(0f) }

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

    // Restore greeting automatically if filtered/empty feed cannot scroll
    LaunchedEffect(
        pagedFeedState,
        listState.canScrollForward,
        listState.canScrollBackward,
        listState.isScrollInProgress
    ) {
        if (!listState.isScrollInProgress &&
            !listState.canScrollForward &&
            listState.firstVisibleItemIndex == 0 &&
            listState.firstVisibleItemScrollOffset == 0 &&
            headerOffsetPx < 0f
        ) {
            animate(
                initialValue = headerOffsetPx,
                targetValue = 0f
            ) { value, _ ->
                headerOffsetPx = value
            }
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

    val headerNestedScrollConnection = remember(decaySpec) {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val maxCollapse = greetingHeightHolder[0]
                val delta = available.y
                if (delta < 0f && maxCollapse > 0f && headerOffsetPx > -maxCollapse) {
                    val newOffset = (headerOffsetPx + delta).coerceIn(-maxCollapse, 0f)
                    val consumed = newOffset - headerOffsetPx
                    headerOffsetPx = newOffset
                    return Offset(0f, consumed)
                }
                return Offset.Zero
            }

            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                val maxCollapse = greetingHeightHolder[0]
                val delta = available.y
                if (delta > 0f && maxCollapse > 0f && headerOffsetPx < 0f) {
                    val newOffset = (headerOffsetPx + delta).coerceIn(-maxCollapse, 0f)
                    val consumedY = newOffset - headerOffsetPx
                    headerOffsetPx = newOffset
                    return Offset(0f, consumedY)
                }
                return Offset.Zero
            }

            override suspend fun onPreFling(available: Velocity): Velocity {
                val maxCollapse = greetingHeightHolder[0]
                val velocityY = available.y
                if (maxCollapse > 0f && velocityY < 0f && headerOffsetPx > -maxCollapse) {
                    var remainingVelocity = velocityY
                    AnimationState(
                        initialValue = headerOffsetPx,
                        initialVelocity = velocityY
                    ).animateDecay(decaySpec) {
                        headerOffsetPx = value.coerceIn(-maxCollapse, 0f)
                        remainingVelocity = velocity
                        if (value <= -maxCollapse) {
                            headerOffsetPx = -maxCollapse
                            cancelAnimation()
                        }
                    }
                    return Velocity(0f, velocityY - remainingVelocity)
                }
                return Velocity.Zero
            }

            override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                val maxCollapse = greetingHeightHolder[0]
                val velocityY = available.y
                if (maxCollapse > 0f && velocityY > 0f && headerOffsetPx < 0f) {
                    var remainingVelocity = velocityY
                    AnimationState(
                        initialValue = headerOffsetPx,
                        initialVelocity = velocityY
                    ).animateDecay(decaySpec) {
                        headerOffsetPx = value.coerceIn(-maxCollapse, 0f)
                        remainingVelocity = velocity
                        if (value >= 0f) {
                            headerOffsetPx = 0f
                            cancelAnimation()
                        }
                    }
                    return Velocity(0f, velocityY - remainingVelocity)
                }
                return Velocity.Zero
            }
        }
    }

    val headerScrollForwarder = rememberScrollableState { delta ->
        -listState.dispatchRawDelta(-delta)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .clipToBounds()
            .pullToRefresh(
                isRefreshing = isRefreshingFeed,
                state = pullToRefreshState,
                threshold = pullThreshold,
                onRefresh = viewModel::refreshDynamicFeed
            )
            .nestedScroll(headerNestedScrollConnection)
    ) {
        Layout(
            content = {
                // 0: COLLAPSIBLE GREETING HEADER ("Halo Username" + "Mau diajak kemana?" + Lonceng 48.dp)
                // Sisi atas & bawah teks sejajar dengan bulatan lonceng 48.dp; ikut naik & tenggelam saat scroll
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.background)
                        .scrollable(
                            state = headerScrollForwarder,
                            orientation = Orientation.Vertical,
                            reverseDirection = false
                        )
                        .padding(
                            start = DiajakSpacing.ScreenHorizontal,
                            end = DiajakSpacing.ScreenHorizontal,
                            top = DiajakHeaderSpec.PaddingTop,
                            bottom = 2.dp
                        )
                        .height(DiajakHeaderSpec.ContentHeight),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(DiajakSpacing.ItemGap)
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Halo $firstName",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontSize = DiajakHeaderSpec.TitleFontSize,
                                lineHeight = DiajakHeaderSpec.TitleLineHeight,
                                letterSpacing = DiajakHeaderSpec.TitleLetterSpacing,
                                platformStyle = PlatformTextStyle(includeFontPadding = false),
                                lineHeightStyle = LineHeightStyle(
                                    alignment = LineHeightStyle.Alignment.Top,
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
                                fontSize = 13.sp,
                                lineHeight = 16.sp,
                                platformStyle = PlatformTextStyle(includeFontPadding = false),
                                lineHeightStyle = LineHeightStyle(
                                    alignment = LineHeightStyle.Alignment.Bottom,
                                    trim = LineHeightStyle.Trim.Both
                                )
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.testTag("home_greeting_subtitle")
                        )
                    }

                    // Notification Bell (48.dp — identik posisi & diameter dengan Back & Love Detail serta header lain)
                    Box(
                        modifier = Modifier
                            .size(DiajakHeaderSpec.ButtonSize)
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

                // 1: PULL-TO-REFRESH SPINNER SLOT (di bawah sapaan, tepat di atas kolom pencarian)
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    if (effectivePullFraction > 0.02f || isRefreshingFeed) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(pullSlotHeight * effectivePullFraction)
                                .clipToBounds()
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
                }

                // 2: STICKY FLAT SEARCH BAR HEADER (menempel putih polos flat tanpa garis/bayangan di bawah status bar)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.background)
                        .scrollable(
                            state = headerScrollForwarder,
                            orientation = Orientation.Vertical,
                            reverseDirection = false
                        )
                        .padding(
                            start = DiajakSpacing.ScreenHorizontal,
                            end = DiajakSpacing.ScreenHorizontal,
                            top = DiajakHeaderSpec.PaddingTop,
                            bottom = DiajakHeaderSpec.PaddingBottom
                        )
                ) {
                    DiajakFlatTextField(
                        value = filters.searchQuery,
                        onValueChange = viewModel::updateSearchQuery,
                        placeholder = stringResource(R.string.home_search_placeholder),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Rounded.Search,
                                contentDescription = "Cari aktivitas",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier
                                    .padding(start = 4.dp)
                                    .size(DiajakIconSize.Primary)
                            )
                        },
                        trailingIcon = {
                            if (filters.searchQuery.isNotEmpty()) {
                                Icon(
                                    imageVector = Icons.Rounded.Close,
                                    contentDescription = "Hapus pencarian",
                                    tint = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier
                                        .padding(end = 4.dp)
                                        .size(DiajakIconSize.Inline)
                                        .clickable { viewModel.updateSearchQuery("") }
                                )
                            }
                        },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        shape = DiajakRadius.Pill,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("home_search_input")
                    )
                }

                // 3: SCROLLABLE FEED AREA (Chip Kategori + Feed Aktivitas)
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(top = DiajakHeaderSpec.ContentTopGap, bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(DiajakSpacing.SectionGap)
                ) {

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
                                color = Color.White,
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
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
                            val shimmerProgress = rememberDiajakShimmerProgress()
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = DiajakSpacing.ScreenHorizontal)
                                    .testTag("home_feed_skeleton"),
                                verticalArrangement = Arrangement.spacedBy(DiajakSpacing.SectionGap)
                            ) {
                                repeat(2) {
                                    ActivityFeedCardSkeleton(shimmerProgress = shimmerProgress)
                                }
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

                            // SATU SPINNER BAWAH PERMANEN: memuat batch berikutnya dan terus memantau konten baru real-time
                            item(key = "feed_load_more_footer") {
                                LaunchedEffect(paged.items.size, paged.totalCount, paged.hasMore) {
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
                                        contentDescription = "Memuat atau mendeteksi aktivitas baru"
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        ) { measurables, constraints ->
            val unbounded = constraints.copy(minHeight = 0, maxHeight = Constraints.Infinity)
            val greetingPlaceable = measurables[0].measure(unbounded)
            val spinnerPlaceable = measurables[1].measure(unbounded)
            val searchPlaceable = measurables[2].measure(unbounded)

            val greetingHeight = greetingPlaceable.height
            greetingHeightHolder[0] = greetingHeight.toFloat()

            val offsetInt = headerOffsetPx
                .coerceIn(-greetingHeight.toFloat(), 0f)
                .roundToInt()
            val visibleGreetingHeight = (greetingHeight + offsetInt).coerceIn(0, greetingHeight)
            val spinnerHeight = spinnerPlaceable.height
            val searchTopY = visibleGreetingHeight + spinnerHeight
            val feedTopY = searchTopY + searchPlaceable.height
            val feedHeight = (constraints.maxHeight - feedTopY).coerceAtLeast(0)

            val feedPlaceable = measurables[3].measure(
                constraints.copy(minHeight = feedHeight, maxHeight = feedHeight)
            )

            layout(constraints.maxWidth, constraints.maxHeight) {
                feedPlaceable.placeRelative(0, feedTopY)
                if (spinnerHeight > 0) {
                    spinnerPlaceable.placeRelative(0, visibleGreetingHeight)
                }
                searchPlaceable.placeRelative(0, searchTopY)
                if (visibleGreetingHeight > 0) {
                    greetingPlaceable.placeRelative(0, offsetInt)
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

