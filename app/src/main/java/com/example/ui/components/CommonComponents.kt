package com.example.ui.components

import androidx.compose.animation.core.AnimationState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.animateDecay
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.rememberSplineBasedDecay
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.MyLocation
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.CityOption
import com.example.data.DiajakCatalog
import com.example.data.NicheCategory
import com.example.ui.theme.DiajakComponentSize
import com.example.ui.theme.DiajakHeaderSpec
import com.example.ui.theme.DiajakIconSize
import com.example.ui.theme.DiajakPhotoOverlay
import com.example.ui.theme.DiajakRadius
import com.example.ui.theme.DiajakSpacing
import com.example.ui.theme.StarGold
import com.example.ui.theme.DiajakSurfaceGray
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * Tombol ikon bulat baku Diajak sesuai Aturan Kontras Pembungkus Ikon:
 * - Di atas latar putih (`nested = false`): bulatan abu-abu #F2F2F2 (`surfaceContainer`),
 *   diameter 48.dp (`ActionCircle`), ikon JetBlack 24.dp (`Primary`) — identik dengan Lonceng Beranda.
 * - Di dalam kartu abu-abu (`nested = true`): bulatan putih #FFFFFF (`surfaceContainerHigh`),
 *   diameter 48.dp (`ActionCircle`), ikon JetBlack 24.dp (`Primary`).
 */
@Composable
fun DiajakCircleIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    nested: Boolean = false,
    enabled: Boolean = true,
    iconTint: Color = MaterialTheme.colorScheme.onSurface,
    size: Dp = DiajakComponentSize.ActionCircle
) {
    val containerColor = if (nested) {
        MaterialTheme.colorScheme.surfaceContainerHigh
    } else {
        MaterialTheme.colorScheme.surfaceContainer
    }
    Surface(
        onClick = onClick,
        enabled = enabled,
        shape = CircleShape,
        color = containerColor,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        modifier = modifier.size(size)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = if (enabled) iconTint else iconTint.copy(alpha = 0.4f),
                modifier = Modifier.size(DiajakIconSize.Primary)
            )
        }
    }
}

/**
 * Tombol Kembali (Back) baku di atas latar/header putih (#FFFFFF).
 * Memakai bulatan abu-abu #F2F2F2 (48.dp) + ikon ArrowBack hitam (#111111, 24.dp).
 * Saat dipakai di slot `navigationIcon` pada TopAppBar (`inTopAppBar = true`),
 * posisi bulatan disejajarkan rapi dengan margin layar 20.dp dan jarak 8.dp sebelum judul.
 */
@Composable
fun DiajakBackButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentDescription: String = "Kembali",
    inTopAppBar: Boolean = true
) {
    if (inTopAppBar) {
        Box(modifier = Modifier.padding(start = 16.dp, end = 8.dp)) {
            DiajakCircleIconButton(
                icon = Icons.AutoMirrored.Rounded.ArrowBack,
                contentDescription = contentDescription,
                onClick = onClick,
                nested = false,
                modifier = modifier
            )
        }
    } else {
        DiajakCircleIconButton(
            icon = Icons.AutoMirrored.Rounded.ArrowBack,
            contentDescription = contentDescription,
            onClick = onClick,
            nested = false,
            modifier = modifier
        )
    }
}

/**
 * Header halaman baku Diajak (aturan mutlak untuk seluruh halaman selain sapaan Beranda):
 * - Dimensi & posisi tombol bulat 100% identik dengan Header Beranda & Detail Aktivitas:
 *   statusBarsPadding() + padding(start/end = 20.dp, top = 14.dp, bottom = 12.dp)
 *   + tinggi slot konten tepat 48.dp (total 74.dp di bawah status bar).
 * - Judul selalu 1 baris tanpa subtitle, ukuran baku 22.sp Bold (identik dengan "Halo Username"
 *   di Beranda), rata tengah (Center) horizontal & vertikal tepat sejajar titik tengah bulatan
 *   tombol 48.dp (gaya Instagram & Shopee).
 * - Posisi pusat judul 100% sama baik pada halaman bertombol Back maupun tanpa tombol Back.
 */
@Composable
fun DiajakPageHeader(
    title: String,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
    backButtonTestTag: String? = null,
    titleTestTag: String? = null,
    trailingAction: (@Composable BoxScope.() -> Unit)? = null,
    applyStatusBarsPadding: Boolean = true
) {
    val sideSlotWidth = DiajakHeaderSpec.ButtonSize
    val hasSideAction = onBack != null || trailingAction != null
    val titleHorizontalInset = if (hasSideAction) sideSlotWidth + DiajakSpacing.SmallGap else 0.dp

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .then(if (applyStatusBarsPadding) Modifier.statusBarsPadding() else Modifier)
            .padding(
                start = DiajakSpacing.ScreenHorizontal,
                end = DiajakSpacing.ScreenHorizontal,
                top = DiajakHeaderSpec.PaddingTop,
                bottom = DiajakHeaderSpec.PaddingBottom
            )
            .height(DiajakHeaderSpec.ContentHeight)
    ) {
        if (onBack != null) {
            DiajakBackButton(
                onClick = onBack,
                inTopAppBar = false,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .then(
                        if (backButtonTestTag != null) Modifier.testTag(backButtonTestTag) else Modifier
                    )
            )
        }

        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall.copy(
                fontSize = DiajakHeaderSpec.TitleFontSize,
                lineHeight = DiajakHeaderSpec.TitleLineHeight,
                letterSpacing = DiajakHeaderSpec.TitleLetterSpacing,
                platformStyle = PlatformTextStyle(includeFontPadding = false),
                lineHeightStyle = LineHeightStyle(
                    alignment = LineHeightStyle.Alignment.Center,
                    trim = LineHeightStyle.Trim.Both
                )
            ),
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .padding(horizontal = titleHorizontalInset)
                .then(
                    if (titleTestTag != null) Modifier.testTag(titleTestTag) else Modifier
                )
        )

        if (trailingAction != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .size(sideSlotWidth),
                contentAlignment = Alignment.Center,
                content = trailingAction
            )
        }
    }
}

/**
 * Wadah halaman baku Diajak (ATURAN MUTLAK untuk seluruh halaman saat ini & halaman baru di masa depan,
 * kecuali tata letak khusus `HomeScreen` Beranda & `ActivityDetailScreen` Detail Aktivitas yang tetap
 * memakai dimensi & posisi bulatan tombol `DiajakHeaderSpec` yang identik):
 * - Memasang [DiajakPageHeader] secara otomatis (judul 1 baris, rata tengah horizontal & vertikal,
 *   font baku `22.sp` Bold identik dengan "Halo Username" di Beranda, koordinat identik baik dengan maupun tanpa tombol Back).
 * - Perilaku scroll baku (`pinnedHeader = false` — Opsi A / ikut konten murni): header ikut naik &
 *   tenggelam ke atas bersama konten saat layar discroll, dan baru muncul kembali saat konten kembali
 *   ke posisi paling atas. Area status bar tetap putih solid agar konten tidak menabrak ikon status bar.
 * - Khusus Ruang Chat (`ChatRoomScreen`), gunakan `pinnedHeader = true` agar header tetap menempel.
 * - Mengunci `contentWindowInsets = WindowInsets(0, 0, 0, 0)` agar inset status bar tidak pernah ganda.
 * - Menyediakan `contentPadding` terkunci (`start/end = 20.dp`, `top = DiajakHeaderSpec.ContentTopGap 4.dp`,
 *   `bottom = extraBottomPadding + navBarInset`) sehingga jarak visual dari bawah slot header `48.dp`
 *   ke konten pertama selalu persis `16.dp` di seluruh layar — identik dengan Beranda.
 */
@Composable
fun DiajakPageScaffold(
    title: String,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
    backButtonTestTag: String? = null,
    titleTestTag: String? = null,
    trailingAction: (@Composable BoxScope.() -> Unit)? = null,
    bottomBar: @Composable () -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    applyNavigationBarBottomPadding: Boolean = true,
    extraBottomPadding: Dp = 24.dp,
    pinnedHeader: Boolean = false,
    headerResetKey: Any? = null,
    content: @Composable (contentPadding: PaddingValues) -> Unit
) {
    val navBarBottom = if (applyNavigationBarBottomPadding) {
        WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    } else {
        0.dp
    }
    val contentPaddingValues = PaddingValues(
        start = DiajakSpacing.ScreenHorizontal,
        end = DiajakSpacing.ScreenHorizontal,
        top = DiajakHeaderSpec.ContentTopGap,
        bottom = extraBottomPadding + navBarBottom
    )

    if (pinnedHeader) {
        Scaffold(
            modifier = modifier,
            containerColor = MaterialTheme.colorScheme.background,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            topBar = {
                DiajakPageHeader(
                    title = title,
                    onBack = onBack,
                    backButtonTestTag = backButtonTestTag,
                    titleTestTag = titleTestTag,
                    trailingAction = trailingAction,
                    applyStatusBarsPadding = true
                )
            },
            bottomBar = bottomBar,
            floatingActionButton = floatingActionButton
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                content(contentPaddingValues)
            }
        }
        return
    }

    val decaySpec = rememberSplineBasedDecay<Float>()
    val headerHeightHolder = remember { FloatArray(1) }
    var headerOffsetPx by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(title, headerResetKey) {
        if (headerOffsetPx != 0f) {
            animate(
                initialValue = headerOffsetPx,
                targetValue = 0f
            ) { value, _ ->
                headerOffsetPx = value
            }
        }
    }

    val nestedScrollConnection = remember(decaySpec) {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val maxCollapse = headerHeightHolder[0]
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
                val maxCollapse = headerHeightHolder[0]
                val delta = available.y
                if (delta > 0f && maxCollapse > 0f && headerOffsetPx < 0f) {
                    val newOffset = (headerOffsetPx + delta).coerceIn(-maxCollapse, 0f)
                    val consumedY = newOffset - headerOffsetPx
                    headerOffsetPx = newOffset
                    return Offset(0f, consumedY)
                }
                return Offset.Zero
            }

            override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                val maxCollapse = headerHeightHolder[0]
                val velocityY = available.y
                if (maxCollapse <= 0f) return Velocity.Zero

                if (velocityY > 0f && headerOffsetPx < 0f) {
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

                if (velocityY < 0f && headerOffsetPx > -maxCollapse) {
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
        }
    }

    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = bottomBar,
        floatingActionButton = floatingActionButton
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .statusBarsPadding()
                .padding(innerPadding)
                .nestedScroll(nestedScrollConnection)
                .clipToBounds()
        ) {
            Layout(
                content = {
                    DiajakPageHeader(
                        title = title,
                        onBack = onBack,
                        backButtonTestTag = backButtonTestTag,
                        titleTestTag = titleTestTag,
                        trailingAction = trailingAction,
                        applyStatusBarsPadding = false
                    )
                    Box(modifier = Modifier.fillMaxSize()) {
                        content(contentPaddingValues)
                    }
                }
            ) { measurables, constraints ->
                val headerPlaceable = measurables[0].measure(
                    constraints.copy(minHeight = 0, maxHeight = Constraints.Infinity)
                )
                val measuredHeaderHeight = headerPlaceable.height
                headerHeightHolder[0] = measuredHeaderHeight.toFloat()

                val clampedOffset = headerOffsetPx.coerceIn(-measuredHeaderHeight.toFloat(), 0f)
                val offsetInt = clampedOffset.roundToInt()
                val visibleHeaderHeight = (measuredHeaderHeight + offsetInt)
                    .coerceIn(0, measuredHeaderHeight)
                val contentHeight = (constraints.maxHeight - visibleHeaderHeight).coerceAtLeast(0)

                val contentPlaceable = measurables[1].measure(
                    constraints.copy(minHeight = contentHeight, maxHeight = contentHeight)
                )

                layout(constraints.maxWidth, constraints.maxHeight) {
                    contentPlaceable.placeRelative(0, visibleHeaderHeight)
                    if (visibleHeaderHeight > 0) {
                        headerPlaceable.placeRelative(0, offsetInt)
                    }
                }
            }
        }
    }
}

/**
 * Komponen TextField monokrom flat standar Diajak:
 * - Latar abu-abu tunggal #F2F2F2 (atau putih #FFFFFF jika berada di dalam kartu abu-abu).
 * - 100% tanpa border dan tanpa bayangan.
 */
@Composable
fun DiajakFlatTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    label: String? = null,
    supportingText: String? = null,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
    singleLine: Boolean = true,
    minLines: Int = 1,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    shape: Shape = DiajakRadius.Control,
    nested: Boolean = false,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions? = null
) {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val effectiveKeyboardActions = keyboardActions ?: remember(singleLine, focusManager, keyboardController) {
        if (singleLine) {
            KeyboardActions(
                onDone = {
                    focusManager.clearFocus(force = true)
                    keyboardController?.hide()
                },
                onSearch = {
                    focusManager.clearFocus(force = true)
                    keyboardController?.hide()
                }
            )
        } else {
            KeyboardActions.Default
        }
    }
    val containerColor = if (nested) {
        MaterialTheme.colorScheme.surfaceContainerHigh
    } else {
        MaterialTheme.colorScheme.surfaceContainer
    }
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = placeholder?.let {
            {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        label = label?.let {
            {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        supportingText = supportingText?.let {
            {
                Text(
                    text = it,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        singleLine = singleLine,
        minLines = minLines,
        maxLines = maxLines,
        keyboardOptions = keyboardOptions,
        keyboardActions = effectiveKeyboardActions,
        shape = shape,
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedContainerColor = containerColor,
            focusedContainerColor = containerColor,
            disabledContainerColor = containerColor,
            errorContainerColor = containerColor,
            unfocusedBorderColor = Color.Transparent,
            focusedBorderColor = Color.Transparent,
            disabledBorderColor = Color.Transparent,
            errorBorderColor = Color.Transparent,
            focusedTextColor = MaterialTheme.colorScheme.onSurface,
            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
            cursorColor = MaterialTheme.colorScheme.primary
        ),
        modifier = modifier.keepKeyboardOpenOnTouch(trackFocus = true)
    )
}

/**
 * Tombol sekunder monokrom flat standar Diajak (menggantikan seluruh OutlinedButton):
 * - Latar #F2F2F2 di atas latar putih, atau putih #FFFFFF jika berada di dalam kartu #F2F2F2.
 * - 100% tanpa border dan tanpa shadow.
 */
@Composable
fun DiajakFlatSecondaryButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    nested: Boolean = false,
    enabled: Boolean = true,
    shape: Shape = DiajakRadius.Control,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    content: @Composable RowScope.() -> Unit
) {
    val container = if (nested) {
        MaterialTheme.colorScheme.surfaceContainerHigh
    } else {
        MaterialTheme.colorScheme.surfaceContainer
    }
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = shape,
        colors = ButtonDefaults.buttonColors(
            containerColor = container,
            contentColor = contentColor,
            disabledContainerColor = container,
            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 0.dp,
            pressedElevation = 0.dp,
            focusedElevation = 0.dp,
            hoveredElevation = 0.dp,
            disabledElevation = 0.dp
        ),
        contentPadding = contentPadding,
        modifier = modifier,
        content = content
    )
}

@Composable
fun DiajakPhoto(
    photoKey: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop
) {
    val isUriOrUrl = photoKey.startsWith("http://") ||
        photoKey.startsWith("https://") ||
        photoKey.startsWith("content://") ||
        photoKey.startsWith("file://")
    if (isUriOrUrl) {
        AsyncImage(
            model = photoKey,
            contentDescription = contentDescription,
            contentScale = contentScale,
            modifier = modifier
        )
    } else {
        Image(
            painter = painterResource(id = DiajakCatalog.resolveDrawableRes(photoKey)),
            contentDescription = contentDescription,
            contentScale = contentScale,
            modifier = modifier
        )
    }
}

@Composable
fun InitialAvatar(
    name: String,
    photoKey: String = "",
    size: Dp = 40.dp,
    nested: Boolean = true,
    modifier: Modifier = Modifier
) {
    val initials = remember(name) {
        name.trim().split(" ")
            .filter { it.isNotBlank() }
            .take(2)
            .joinToString("") { it.first().uppercase() }
            .ifBlank { "D" }
    }
    val avatarBg = if (nested) {
        MaterialTheme.colorScheme.surfaceContainerHigh
    } else {
        MaterialTheme.colorScheme.surfaceContainer
    }
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(avatarBg),
        contentAlignment = Alignment.Center
    ) {
        if (photoKey.startsWith("http") || photoKey.startsWith("content://") || photoKey.startsWith("img_")) {
            DiajakPhoto(
                photoKey = photoKey,
                contentDescription = "Avatar $name",
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Text(
                text = initials,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun EmptyStatePanel(
    icon: ImageVector,
    title: String,
    subtitle: String,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    outerPadding: PaddingValues = PaddingValues(DiajakSpacing.ScreenHorizontal)
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(outerPadding)
            .clip(DiajakRadius.Card)
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceContainerHigh),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(DiajakIconSize.Hero)
            )
        }
        Spacer(modifier = Modifier.height(14.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        if (actionLabel != null && onAction != null) {
            Spacer(modifier = Modifier.height(18.dp))
            Button(
                onClick = onAction,
                shape = DiajakRadius.Control,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                elevation = ButtonDefaults.buttonElevation(0.dp, 0.dp, 0.dp, 0.dp, 0.dp)
            ) {
                Text(text = actionLabel, style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

@Composable
fun TicketQrCanvas(
    code: String,
    modifier: Modifier = Modifier
) {
    val gridSize = 21
    val matrix = remember(code) {
        val hash = code.hashCode()
        Array(gridSize) { r ->
            BooleanArray(gridSize) { c ->
                val inTopLeft = r < 7 && c < 7
                val inTopRight = r < 7 && c >= gridSize - 7
                val inBottomLeft = r >= gridSize - 7 && c < 7
                when {
                    inTopLeft -> isFinderCell(r, c)
                    inTopRight -> isFinderCell(r, c - (gridSize - 7))
                    inBottomLeft -> isFinderCell(r - (gridSize - 7), c)
                    else -> ((hash xor (r * 73 + c * 37 + code.length * 19)) and 1) == 0 ||
                        ((r * 13 + c * 7 + hash) % 3 == 0)
                }
            }
        }
    }
    Canvas(
        modifier = modifier
            .clip(DiajakRadius.Control)
            .background(DiajakPhotoOverlay.QrBackground)
            .padding(14.dp)
    ) {
        val cellW = size.width / gridSize
        val cellH = size.height / gridSize
        for (r in 0 until gridSize) {
            for (c in 0 until gridSize) {
                if (matrix[r][c]) {
                    drawRect(
                        color = DiajakPhotoOverlay.QrModule,
                        topLeft = Offset(c * cellW, r * cellH),
                        size = Size(cellW + 0.5f, cellH + 0.5f)
                    )
                }
            }
        }
    }
}

private fun isFinderCell(r: Int, c: Int): Boolean {
    if (r == 0 || r == 6 || c == 0 || c == 6) return true
    if (r in 2..4 && c in 2..4) return true
    return false
}

@Composable
fun RatingStars(
    rating: Int,
    onRatingSelected: ((Int) -> Unit)? = null,
    starSize: Dp = DiajakIconSize.Inline
) {
    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
        for (i in 1..5) {
            val filled = i <= rating
            Icon(
                imageVector = if (filled) Icons.Rounded.Star else Icons.Rounded.StarOutline,
                contentDescription = "Bintang $i",
                tint = if (filled) StarGold else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .size(starSize)
                    .then(
                        if (onRatingSelected != null) {
                            Modifier.clickable { onRatingSelected(i) }
                        } else Modifier
                    )
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun NichePickerBottomSheet(
    selectedNicheName: String?,
    allowClearAll: Boolean = true,
    onSelectNiche: (NicheCategory?) -> Unit,
    onDismiss: () -> Unit
) {
    var query by remember { mutableStateOf("") }
    val filtered = remember(query) {
        val q = query.trim()
        if (q.isEmpty()) DiajakCatalog.allNiches
        else DiajakCatalog.allNiches.filter { it.name.contains(q, ignoreCase = true) }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .dismissKeyboardOnOutsideTouch()
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp)
        ) {
            Text(
                text = "Semua Kategori (${DiajakCatalog.allNiches.size} Niche)",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(12.dp))
            DiajakFlatTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = "Cari kategori: Lari, Kopi, Esport...",
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Rounded.Search,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(DiajakIconSize.Primary)
                    )
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                shape = DiajakRadius.Control,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("niche_search_input")
            )
            Spacer(modifier = Modifier.height(DiajakSpacing.SectionGap))
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(360.dp)
            ) {
                item {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(DiajakSpacing.ChipGap),
                        verticalArrangement = Arrangement.spacedBy(DiajakSpacing.ChipGap)
                    ) {
                        if (allowClearAll && query.isBlank()) {
                            val active = selectedNicheName == null
                            NicheChipItem(
                                label = "Semua",
                                selected = active,
                                onClick = {
                                    onSelectNiche(null)
                                    onDismiss()
                                }
                            )
                        }
                        filtered.forEach { niche ->
                            val active = selectedNicheName.equals(niche.name, ignoreCase = true)
                            NicheChipItem(
                                label = niche.name,
                                emoji = niche.emoji,
                                selected = active,
                                onClick = {
                                    onSelectNiche(niche)
                                    onDismiss()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NicheChipItem(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    emoji: String? = null,
    nested: Boolean = false
) {
    val backgroundColor = if (selected) {
        DiajakSurfaceGray
    } else {
        Color.White
    }
    val border = if (!selected) {
        BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    } else {
        null
    }
    Surface(
        onClick = onClick,
        shape = DiajakRadius.Pill,
        color = backgroundColor,
        border = border,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 11.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (!emoji.isNullOrBlank()) {
                val emojiFontSize = with(LocalDensity.current) { 15.dp.toSp() }
                val emojiLineHeight = with(LocalDensity.current) { DiajakIconSize.Inline.toSp() }
                Box(
                    modifier = Modifier.size(DiajakIconSize.Inline),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = emoji,
                        fontSize = emojiFontSize,
                        lineHeight = emojiLineHeight,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            platformStyle = PlatformTextStyle(includeFontPadding = false),
                            lineHeightStyle = LineHeightStyle(
                                alignment = LineHeightStyle.Alignment.Center,
                                trim = LineHeightStyle.Trim.Both
                            )
                        )
                    )
                }
            }
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CityLocationBottomSheet(
    currentCity: String,
    isGpsActive: Boolean,
    onRequestGps: () -> Unit,
    onSelectCity: (CityOption) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
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
                text = "Pilih Lokasi Aktivitas",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Gunakan GPS perangkat atau pilih kota manual untuk mencari aktivitas terdekat.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(14.dp))

            Surface(
                onClick = {
                    onRequestGps()
                    onDismiss()
                },
                shape = DiajakRadius.Control,
                color = MaterialTheme.colorScheme.primary,
                tonalElevation = 0.dp,
                shadowElevation = 0.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("detect_gps_button")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.MyLocation,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(DiajakIconSize.Inline)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isGpsActive) "Perbarui Lokasi GPS Saat Ini" else "Deteksi Lokasi Otomatis (GPS)",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(DiajakSpacing.SectionGap))
            Text(
                text = "Atau Pilih Kota Manual",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(DiajakSpacing.SmallGap))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                verticalArrangement = Arrangement.spacedBy(DiajakSpacing.SmallGap)
            ) {
                items(DiajakCatalog.cities) { city ->
                    val selected = currentCity.equals(city.name, ignoreCase = true)
                    Surface(
                        onClick = {
                            onSelectCity(city)
                            onDismiss()
                        },
                        shape = DiajakRadius.Control,
                        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainer,
                        tonalElevation = 0.dp,
                        shadowElevation = 0.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Rounded.LocationOn,
                                    contentDescription = null,
                                    tint = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(DiajakIconSize.Inline)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = city.name,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                                    color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                )
                            }
                            if (selected) {
                                Icon(
                                    imageVector = Icons.Rounded.Check,
                                    contentDescription = "Terpilih",
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(DiajakIconSize.Inline)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Indikator loading radial 8-garis bergaya Instagram / iOS Cupertino:
 * - Tanpa lingkaran latar putih dan tanpa bayangan.
 * - Saat `isSpinning = false`, 8 garis muncul bertahap mengikuti `progress` (0f..1f) saat layar ditarik.
 * - Saat `isSpinning = true`, gradasi transparansi berputar searah jarum jam dengan ritme 8-step yang halus.
 */
@Composable
fun InstagramSpinner(
    modifier: Modifier = Modifier,
    isSpinning: Boolean = true,
    progress: Float = 1f,
    size: Dp = 26.dp,
    color: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    contentDescription: String = "Memuat"
) {
    val spokeCount = 8
    val animatedPhase = if (isSpinning) {
        val transition = rememberInfiniteTransition(label = "instagram_spinner")
        transition.animateFloat(
            initialValue = 0f,
            targetValue = spokeCount.toFloat(),
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 760, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "instagram_spinner_phase"
        ).value
    } else {
        0f
    }

    Canvas(
        modifier = modifier
            .size(size)
            .semantics { this.contentDescription = contentDescription }
    ) {
        val radius = this.size.minDimension / 2f
        val strokeWidth = (this.size.minDimension * 0.105f).coerceAtLeast(2f)
        val innerRadius = radius * 0.44f
        val outerRadius = (radius - strokeWidth / 2f).coerceAtLeast(innerRadius + 1f)
        val center = Offset(this.size.width / 2f, this.size.height / 2f)

        if (isSpinning) {
            val activeHead = floor(animatedPhase).toInt() % spokeCount
            for (i in 0 until spokeCount) {
                val angleRad = (-90.0 + i * (360.0 / spokeCount)) * (PI / 180.0)
                val cosA = cos(angleRad).toFloat()
                val sinA = sin(angleRad).toFloat()
                val stepsBehind = (activeHead - i + spokeCount) % spokeCount
                val alpha = (0.90f - (stepsBehind / (spokeCount - 1f)) * 0.72f)
                    .coerceIn(0.18f, 0.90f)
                drawLine(
                    color = color.copy(alpha = alpha),
                    start = Offset(center.x + cosA * innerRadius, center.y + sinA * innerRadius),
                    end = Offset(center.x + cosA * outerRadius, center.y + sinA * outerRadius),
                    strokeWidth = strokeWidth,
                    cap = StrokeCap.Round
                )
            }
        } else {
            val clamped = progress.coerceIn(0f, 1f)
            for (i in 0 until spokeCount) {
                val spokeThreshold = 0.08f + (i.toFloat() / spokeCount) * 0.80f
                val spokeFade = ((clamped - spokeThreshold) / 0.14f).coerceIn(0f, 1f)
                if (spokeFade > 0.01f) {
                    val angleRad = (-90.0 + i * (360.0 / spokeCount)) * (PI / 180.0)
                    val cosA = cos(angleRad).toFloat()
                    val sinA = sin(angleRad).toFloat()
                    val baseAlpha = 0.28f + 0.56f * (i.toFloat() / (spokeCount - 1f))
                    drawLine(
                        color = color.copy(alpha = (spokeFade * baseAlpha).coerceIn(0f, 0.88f)),
                        start = Offset(center.x + cosA * innerRadius, center.y + sinA * innerRadius),
                        end = Offset(center.x + cosA * outerRadius, center.y + sinA * outerRadius),
                        strokeWidth = strokeWidth,
                        cap = StrokeCap.Round
                    )
                }
            }
        }
    }
}

/**
 * Progres animasi shimmer terpusat (0f..1f) agar seluruh elemen skeleton di layar
 * tersapu gelombang cahaya secara serempak dan halus bergaya Instagram.
 */
@Composable
fun rememberDiajakShimmerProgress(): Float {
    val transition = rememberInfiniteTransition(label = "diajak_shimmer_transition")
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1280, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "diajak_shimmer_progress"
    )
    return progress
}

/**
 * Modifier latar shimmer baku Diajak — 100% tanpa warna abu-abu baru:
 * - Di atas latar putih (`nested = false`): memakai `DiajakSurfaceGray` (#F2F2F2) penuh (alpha 1.0f)
 *   yang disapu gelombang pudar (alpha 0.35f) sehingga bertransisi mulus menuju latar putih (#FFFFFF).
 * - Di dalam kartu abu-abu (`nested = true`): memakai `PureWhite` (#FFFFFF) semi-transparan
 *   (0.70f <-> 0.38f) mengikuti aturan lapisan kontras resmi Diajak.
 */
fun Modifier.diajakShimmerSurface(
    progress: Float,
    baseColor: Color,
    nested: Boolean = false
): Modifier = drawBehind {
    val width = size.width
    val height = size.height
    if (width <= 0f || height <= 0f) return@drawBehind

    val bandWidth = (width * 0.72f).coerceAtLeast(120f)
    val startX = -bandWidth + (width + bandWidth * 2f) * progress
    val endX = startX + bandWidth

    val colors = if (nested) {
        listOf(
            baseColor.copy(alpha = 0.70f),
            baseColor.copy(alpha = 0.38f),
            baseColor.copy(alpha = 0.70f)
        )
    } else {
        listOf(
            baseColor.copy(alpha = 1.0f),
            baseColor.copy(alpha = 0.35f),
            baseColor.copy(alpha = 1.0f)
        )
    }

    drawRect(
        brush = Brush.linearGradient(
            colors = colors,
            start = Offset(startX, 0f),
            end = Offset(endX, height * 0.25f)
        )
    )
}

/**
 * Blok rangka Skeleton / Shimmer standar Diajak yang secara otomatis memakai
 * `#F2F2F2` (`surfaceContainer`) di atas layar putih atau `#FFFFFF` (`surfaceContainerHigh`)
 * saat berada di dalam kartu skeleton.
 */
@Composable
fun DiajakShimmerBlock(
    progress: Float,
    modifier: Modifier = Modifier,
    shape: Shape = DiajakRadius.Pill,
    nested: Boolean = false
) {
    val baseColor = if (nested) {
        MaterialTheme.colorScheme.surfaceContainerHigh
    } else {
        MaterialTheme.colorScheme.surfaceContainer
    }
    Box(
        modifier = modifier
            .clip(shape)
            .diajakShimmerSurface(
                progress = progress,
                baseColor = baseColor,
                nested = nested
            )
    )
}
