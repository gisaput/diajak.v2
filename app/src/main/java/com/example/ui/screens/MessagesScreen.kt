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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Chat
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.NotificationsNone
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.AppNotification
import com.example.data.Conversation
import com.example.data.UiState
import com.example.ui.DiajakViewModel
import com.example.ui.components.DiajakBackButton
import com.example.ui.components.DiajakFlatTextField
import com.example.ui.components.EmptyStatePanel
import com.example.ui.components.InitialAvatar
import com.example.ui.theme.DiajakComponentSize
import com.example.ui.theme.DiajakIconSize
import com.example.ui.theme.DiajakRadius
import com.example.ui.theme.DiajakSpacing
import com.example.ui.theme.HeartRed

@Composable
fun MessagesScreen(
    viewModel: DiajakViewModel,
    initialTab: Int = 0,
    onOpenConversation: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val conversationsState by viewModel.conversationsState.collectAsStateWithLifecycle()
    val notifications by viewModel.notificationsState.collectAsStateWithLifecycle()
    var selectedTab by remember(initialTab) { mutableIntStateOf(initialTab) }

    val tabs = listOf("Penyelenggara", "Grup Aktivitas", "Notifikasi")

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        Column(modifier = Modifier.padding(horizontal = DiajakSpacing.ScreenHorizontal, vertical = 14.dp)) {
            Text(
                text = "Pesan",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Chat penyelenggara, grup peserta aktivitas, dan notifikasi.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(14.dp))

            // Flat Monochrome segmented tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(DiajakRadius.Control)
                    .background(MaterialTheme.colorScheme.surfaceContainer)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                tabs.forEachIndexed { idx, label ->
                    val selected = selectedTab == idx
                    Surface(
                        onClick = { selectedTab = idx },
                        shape = DiajakRadius.Small,
                        color = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent,
                        tonalElevation = 0.dp,
                        shadowElevation = 0.dp,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("messages_tab_$idx")
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                            color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(vertical = 10.dp, horizontal = 6.dp)
                        )
                    }
                }
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        if (selectedTab == 2) {
            if (notifications.isEmpty()) {
                EmptyStatePanel(
                    icon = Icons.Rounded.NotificationsNone,
                    title = "Belum ada notifikasi",
                    subtitle = "Informasi booking tiket, verifikasi KTP, dan pesan terbaru akan muncul di sini."
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(DiajakSpacing.ScreenHorizontal),
                    verticalArrangement = Arrangement.spacedBy(DiajakSpacing.ItemGap)
                ) {
                    items(notifications, key = { it.id }) { notif ->
                        NotificationRowCard(
                            notif = notif,
                            onClick = {
                                if (!notif.readState) viewModel.markNotificationRead(notif.id)
                            }
                        )
                    }
                }
            }
        } else {
            when (val state = conversationsState) {
                is UiState.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }
                is UiState.Error -> {
                    EmptyStatePanel(
                        icon = Icons.AutoMirrored.Rounded.Chat,
                        title = "Gagal memuat percakapan",
                        subtitle = state.message
                    )
                }
                is UiState.Success -> {
                    val targetType = if (selectedTab == 0) "DIRECT" else "GROUP"
                    val filtered = remember(state.data, targetType) {
                        state.data.filter { it.type == targetType }
                    }
                    if (filtered.isEmpty()) {
                        EmptyStatePanel(
                            icon = if (selectedTab == 0) Icons.AutoMirrored.Rounded.Chat else Icons.Rounded.Groups,
                            title = if (selectedTab == 0) "Belum ada chat penyelenggara" else "Belum ada grup aktivitas",
                            subtitle = if (selectedTab == 0)
                                "Ketuk avatar penyelenggara di halaman detail aktivitas untuk mulai bertanya langsung."
                            else
                                "Pesan tiket pada suatu aktivitas untuk otomatis bergabung dengan grup chat pesertanya."
                        )
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(DiajakSpacing.ScreenHorizontal),
                            verticalArrangement = Arrangement.spacedBy(DiajakSpacing.ItemGap)
                        ) {
                            items(filtered, key = { it.id }) { conv ->
                                ConversationRowCard(
                                    conversation = conv,
                                    onClick = { onOpenConversation(conv.id) }
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
fun ConversationRowCard(
    conversation: Conversation,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = DiajakRadius.Card,
        color = MaterialTheme.colorScheme.surfaceContainer,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("conversation_row_${conversation.id}")
    ) {
        Row(
            modifier = Modifier.padding(DiajakSpacing.CardPadding),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (conversation.type == "GROUP") {
                Box(
                    modifier = Modifier
                        .size(DiajakComponentSize.ActionCircle)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Groups,
                        contentDescription = "Grup aktivitas",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(DiajakIconSize.Primary)
                    )
                }
            } else {
                InitialAvatar(name = conversation.title, size = DiajakComponentSize.ActionCircle)
            }
            Spacer(modifier = Modifier.width(DiajakSpacing.ItemGap))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = conversation.title,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    if (conversation.type == "GROUP") {
                        Text(
                            text = "${conversation.participantUids.size} anggota",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                if (conversation.activityTitle.isNotBlank() && conversation.type == "DIRECT") {
                    Text(
                        text = conversation.activityTitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = if (conversation.lastMessage.isNotBlank()) conversation.lastMessage else "Ketuk untuk membuka percakapan",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun NotificationRowCard(
    notif: AppNotification,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = DiajakRadius.Card,
        color = MaterialTheme.colorScheme.surfaceContainer,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(DiajakSpacing.CardPadding),
            verticalAlignment = Alignment.Top
        ) {
            if (!notif.readState) {
                Box(
                    modifier = Modifier
                        .padding(top = 6.dp, end = 10.dp)
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(HeartRed)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = notif.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = if (notif.readState) FontWeight.Medium else FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = notif.body,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatRoomScreen(
    conversation: Conversation,
    viewModel: DiajakViewModel,
    onBack: () -> Unit
) {
    val messagesFlow = remember(conversation.id) { viewModel.observeMessages(conversation.id) }
    val messages by messagesFlow.collectAsStateWithLifecycle(initialValue = emptyList())
    var draft by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.lastIndex)
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = conversation.title,
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = if (conversation.type == "GROUP")
                                "Grup Aktivitas • ${conversation.participantUids.size} anggota"
                            else
                                conversation.activityTitle.ifBlank { "Chat Penyelenggara" },
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                },
                navigationIcon = {
                    DiajakBackButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("chat_room_back_button")
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        bottomBar = {
            Surface(color = MaterialTheme.colorScheme.surface) {
                Column(
                    modifier = Modifier
                        .navigationBarsPadding()
                        .imePadding()
                ) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(DiajakSpacing.SmallGap)
                    ) {
                        DiajakFlatTextField(
                            value = draft,
                            onValueChange = { draft = it },
                            placeholder = "Tulis pesan...",
                            singleLine = false,
                            maxLines = 3,
                            shape = DiajakRadius.Card,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("chat_message_input")
                        )
                        Surface(
                            onClick = {
                                val text = draft.trim()
                                if (text.isNotEmpty()) {
                                    draft = ""
                                    viewModel.sendMessage(conversation, text)
                                }
                            },
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surfaceContainer,
                            tonalElevation = 0.dp,
                            shadowElevation = 0.dp,
                            modifier = Modifier
                                .size(DiajakComponentSize.ActionCircle)
                                .testTag("chat_send_button")
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.Send,
                                    contentDescription = "Kirim pesan",
                                    tint = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(DiajakIconSize.Primary)
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(DiajakSpacing.CardPadding),
            verticalArrangement = Arrangement.spacedBy(DiajakSpacing.ChipGap)
        ) {
            if (messages.isEmpty()) {
                item {
                    EmptyStatePanel(
                        icon = Icons.AutoMirrored.Rounded.Chat,
                        title = "Mulai percakapan",
                        subtitle = "Tanyakan jadwal, titik kumpul, atau perlengkapan yang perlu dibawa."
                    )
                }
            }
            items(messages, key = { it.id }) { msg ->
                val isMine = msg.senderId == viewModel.currentUserId
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (isMine) Arrangement.End else Arrangement.Start
                ) {
                    Column(
                        horizontalAlignment = if (isMine) Alignment.End else Alignment.Start,
                        modifier = Modifier.widthIn(max = 290.dp)
                    ) {
                        if (!isMine) {
                            Text(
                                text = msg.senderName,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(start = 6.dp, bottom = 3.dp)
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(
                                topStart = 20.dp,
                                topEnd = 20.dp,
                                bottomStart = if (isMine) 20.dp else 4.dp,
                                bottomEnd = if (isMine) 4.dp else 20.dp
                            ),
                            color = if (isMine) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainer
                        ) {
                            Text(
                                text = msg.text,
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (isMine) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
