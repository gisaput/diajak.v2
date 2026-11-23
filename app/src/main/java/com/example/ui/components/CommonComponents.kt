package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.data.CityOption
import com.example.data.DiajakCatalog
import com.example.data.NicheCategory
import com.example.ui.theme.DiajakComponentSize
import com.example.ui.theme.DiajakIconSize
import com.example.ui.theme.DiajakPhotoOverlay
import com.example.ui.theme.DiajakRadius
import com.example.ui.theme.DiajakSpacing
import com.example.ui.theme.StarGold
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.sin

/**
 * Tombol ikon bulat baku Diajak sesuai Aturan Kontras Pembungkus Ikon:
 * - Di atas latar putih (`nested = false`): bulatan abu-abu #E5E5EA (`surfaceContainer`),
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
 * Memakai bulatan abu-abu #E5E5EA (48.dp) + ikon ArrowBack hitam (#111111, 24.dp).
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
 * Komponen TextField monokrom flat standar Diajak:
 * - Latar abu-abu tunggal #E5E5EA (atau putih #FFFFFF jika berada di dalam kartu abu-abu).
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
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default
) {
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
        modifier = modifier
    )
}

/**
 * Tombol sekunder monokrom flat standar Diajak (menggantikan seluruh OutlinedButton):
 * - Latar #E5E5EA di atas latar putih, atau putih #FFFFFF jika berada di dalam kartu #E5E5EA.
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
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(DiajakSpacing.ScreenHorizontal)
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
    val unselectedColor = if (nested) {
        MaterialTheme.colorScheme.surfaceContainerHigh
    } else {
        MaterialTheme.colorScheme.surfaceContainer
    }
    Surface(
        onClick = onClick,
        shape = DiajakRadius.Pill,
        color = if (selected) MaterialTheme.colorScheme.primary else unselectedColor,
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
                color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
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
 * - Di atas latar putih (`nested = false`): memakai `DiajakSurfaceGray` (#E5E5EA) penuh (alpha 1.0f)
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
 * `#E5E5EA` (`surfaceContainer`) di atas layar putih atau `#FFFFFF` (`surfaceContainerHigh`)
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
