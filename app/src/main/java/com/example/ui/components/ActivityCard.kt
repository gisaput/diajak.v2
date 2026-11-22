package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.ActivityItem
import com.example.data.DiajakCatalog
import com.example.ui.theme.DiajakComponentSize
import com.example.ui.theme.DiajakIconSize
import com.example.ui.theme.DiajakPhotoOverlay
import com.example.ui.theme.DiajakRadius
import com.example.ui.theme.DiajakSpacing
import com.example.ui.theme.HeartRed

@Composable
fun ActivityFeedCard(
    activity: ActivityItem,
    @Suppress("UNUSED_PARAMETER") distanceKm: Double,
    favorited: Boolean,
    onCardClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coverKey = activity.photoKeys.firstOrNull() ?: "img_activity_running"
    val dayText = DiajakCatalog.formatDayNumber(activity.eventDateMillis)
    val monthText = DiajakCatalog.formatShortMonth(activity.eventDateMillis)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(310.dp)
            .clip(DiajakRadius.Hero)
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .clickable(onClick = onCardClick)
            .testTag("activity_card_${activity.id}")
    ) {
        // Full-bleed activity photography
        DiajakPhoto(
            photoKey = coverKey,
            contentDescription = activity.title,
            modifier = Modifier.fillMaxSize()
        )

        // Editorial top & bottom scrim for ultra-crisp legibility without brand tint
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0.0f to DiajakPhotoOverlay.Scrim.copy(alpha = 0.56f),
                        0.38f to Color.Transparent,
                        0.72f to Color.Transparent,
                        1.0f to DiajakPhotoOverlay.Scrim.copy(alpha = 0.36f)
                    )
                )
        )

        // TOP ROW: Circular white Date Badge + Title + Heart Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopStart)
                .padding(DiajakSpacing.CardPadding),
            verticalAlignment = Alignment.Top
        ) {
            // Circular solid white date badge (e.g., 12 / OKT)
            Surface(
                shape = CircleShape,
                color = DiajakPhotoOverlay.ChipSurface,
                modifier = Modifier.size(54.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = dayText,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = DiajakPhotoOverlay.OnChipSurface
                    )
                    Text(
                        text = monthText,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = DiajakPhotoOverlay.OnChipSurface.copy(alpha = 0.72f)
                    )
                }
            }

            Spacer(modifier = Modifier.width(DiajakSpacing.ItemGap))

            // Bold white title
            Text(
                text = activity.title,
                style = MaterialTheme.typography.headlineSmall,
                color = DiajakPhotoOverlay.OnPhotoText,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .weight(1f)
                    .padding(top = 2.dp)
            )

            Spacer(modifier = Modifier.width(DiajakSpacing.SmallGap))

            // Love / Favorite button
            Surface(
                onClick = onToggleFavorite,
                shape = CircleShape,
                color = DiajakPhotoOverlay.ChipSurface.copy(alpha = 0.92f),
                modifier = Modifier
                    .size(DiajakComponentSize.IconCircle)
                    .testTag("favorite_button_${activity.id}")
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (favorited) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                        contentDescription = if (favorited) "Hapus dari favorit" else "Simpan ke favorit",
                        tint = if (favorited) HeartRed else DiajakPhotoOverlay.OnChipSurface,
                        modifier = Modifier.size(DiajakIconSize.Primary)
                    )
                }
            }
        }

        // BOTTOM: Overlapping Joined User Avatars
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(DiajakSpacing.CardPadding)
        ) {
            JoinedAvatarsBadge(
                names = activity.joinedUserNames,
                totalJoined = activity.joinedCount
            )
        }
    }
}

@Composable
fun JoinedAvatarsBadge(
    names: List<String>,
    totalJoined: Long
) {
    val previewNames = names.take(2)
    val extraCount = (totalJoined - previewNames.size).coerceAtLeast(0L)
    Box(
        modifier = Modifier.height(34.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        previewNames.forEachIndexed { index, name ->
            Box(
                modifier = Modifier
                    .offset(x = (index * 20).dp)
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceContainer)
                    .border(2.dp, DiajakPhotoOverlay.ChipSurface, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = name.firstOrNull()?.uppercase() ?: "P",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
        val countOffset = previewNames.size * 20
        Box(
            modifier = Modifier
                .offset(x = countOffset.dp)
                .size(32.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary)
                .border(2.dp, DiajakPhotoOverlay.ChipSurface, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (extraCount > 0) "${extraCount}+" else "$totalJoined",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
        // Spacer to reserve total width
        Spacer(modifier = Modifier.width((countOffset + 34).dp))
    }
}
