package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.CallMissed
import androidx.compose.material.icons.rounded.Forum
import androidx.compose.material.icons.rounded.MarkChatUnread
import androidx.compose.material.icons.rounded.PhoneCallback
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CallHistoryEntry
import com.example.data.ChatMessage
import com.example.data.ChatSection
import com.example.data.DirectMessage
import com.example.data.DirectThread
import com.example.data.InfiniteGigGenerator
import com.example.ui.theme.AmberAlert
import com.example.ui.theme.BoltEmerald
import com.example.ui.theme.CoralAlert
import com.example.ui.theme.JetBrainsMonoFontFamily

private val QUICK_CHAT_PHRASES = listOf(
    "Тоже +15₽ на лечение прилетело 😂",
    "У меня рейтинг снова пробил дно",
    "Кто на шкаф подписывался?",
    "Проверяю пылесос в DNS на тягу"
)

@Composable
fun CourierChatScreen(
    selectedSection: ChatSection,
    messages: List<ChatMessage>,
    typingCourierName: String?,
    courierRating: Double,
    directThreads: List<DirectThread>,
    selectedDmThreadId: String?,
    unreadDmCount: Int,
    callHistory: List<CallHistoryEntry>,
    hapticsEnabled: Boolean,
    onSelectSection: (ChatSection) -> Unit,
    onOpenDmThread: (String?) -> Unit,
    onSendMessage: (String) -> Unit,
    onSendDmMessage: (String, String) -> Unit,
    onTriggerComplaintCall: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activeDmThread = remember(directThreads, selectedDmThreadId) {
        directThreads.firstOrNull { it.id == selectedDmThreadId }
    }

    BackHandler(enabled = selectedSection == ChatSection.DIRECT && activeDmThread != null) {
        onOpenDmThread(null)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .imePadding(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 640.dp)
                .fillMaxSize()
        ) {
            // Top section switcher: Public Guild Chat vs Angry Direct Messages
            Surface(
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                tonalElevation = 2.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SingleChoiceSegmentedButtonRow(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        SegmentedButton(
                            selected = selectedSection == ChatSection.PUBLIC,
                            onClick = { onSelectSection(ChatSection.PUBLIC) },
                            shape = SegmentedButtonDefaults.itemShape(index = 0, count = 3),
                            modifier = Modifier.testTag("chat_tab_public")
                        ) {
                            Text("Общий чат", maxLines = 1, fontSize = 12.sp)
                        }
                        SegmentedButton(
                            selected = selectedSection == ChatSection.DIRECT,
                            onClick = { onSelectSection(ChatSection.DIRECT) },
                            shape = SegmentedButtonDefaults.itemShape(index = 1, count = 3),
                            modifier = Modifier.testTag("chat_tab_direct")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Text("ЛС", maxLines = 1, fontSize = 12.sp)
                                if (unreadDmCount > 0) {
                                    Badge(containerColor = CoralAlert, contentColor = Color.White) {
                                        Text("$unreadDmCount")
                                    }
                                }
                            }
                        }
                        SegmentedButton(
                            selected = selectedSection == ChatSection.CALLS,
                            onClick = { onSelectSection(ChatSection.CALLS) },
                            shape = SegmentedButtonDefaults.itemShape(index = 2, count = 3),
                            modifier = Modifier.testTag("chat_tab_calls")
                        ) {
                            Text("Звонки", maxLines = 1, fontSize = 12.sp)
                        }
                    }

                    when {
                        selectedSection == ChatSection.PUBLIC -> PublicChatHeader(courierRating = courierRating)
                        selectedSection == ChatSection.CALLS -> CallsHeader(callCount = callHistory.size)
                        activeDmThread != null -> DmThreadHeader(
                            thread = activeDmThread,
                            onBack = { onOpenDmThread(null) }
                        )
                        else -> DmInboxHeader(threadCount = directThreads.size)
                    }
                }
            }

            when (selectedSection) {
                ChatSection.PUBLIC -> PublicChatBody(
                    messages = messages,
                    typingCourierName = typingCourierName,
                    hapticsEnabled = hapticsEnabled,
                    onSendMessage = onSendMessage,
                    modifier = Modifier.weight(1f)
                )

                ChatSection.DIRECT -> {
                    if (activeDmThread == null) {
                        DmInboxList(
                            threads = directThreads,
                            onOpenThread = { onOpenDmThread(it.id) },
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        DmConversationBody(
                            thread = activeDmThread,
                            hapticsEnabled = hapticsEnabled,
                            onSendDm = { text -> onSendDmMessage(activeDmThread.id, text) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                ChatSection.CALLS -> CallLogSectionBody(
                    callHistory = callHistory,
                    hapticsEnabled = hapticsEnabled,
                    onTriggerComplaintCall = onTriggerComplaintCall,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun PublicChatHeader(courierRating: Double) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Forum,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
            }
            Column {
                Text(
                    text = "Флудилка курьеров",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "14 на линии • Ваш рейтинг ${String.format("%.1f★", courierRating)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = BoltEmerald
                )
            }
        }

        Surface(
            color = BoltEmerald.copy(alpha = 0.16f),
            shape = RoundedCornerShape(50)
        ) {
            Text(
                text = "LIVE",
                fontFamily = JetBrainsMonoFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                color = BoltEmerald,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
            )
        }
    }
}

@Composable
private fun DmInboxHeader(threadCount: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(CoralAlert.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.MarkChatUnread,
                    contentDescription = null,
                    tint = CoralAlert,
                    modifier = Modifier.size(18.dp)
                )
            }
            Column {
                Text(
                    text = "Личные сообщения (жалобы)",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "$threadCount собеседников • все чем-то недовольны",
                    style = MaterialTheme.typography.labelSmall,
                    color = AmberAlert
                )
            }
        }
    }
}

@Composable
private fun DmThreadHeader(
    thread: DirectThread,
    onBack: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .size(44.dp)
                .testTag("dm_back_button")
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                contentDescription = "Назад к списку ЛС",
                tint = MaterialTheme.colorScheme.onSurface
            )
        }

        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(Color(thread.avatarColorHex).copy(alpha = 0.22f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = thread.personName.take(1),
                style = MaterialTheme.typography.titleMedium,
                color = Color(thread.avatarColorHex)
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = thread.personName,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = thread.personRole,
                style = MaterialTheme.typography.labelSmall,
                color = CoralAlert,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun PublicChatBody(
    messages: List<ChatMessage>,
    typingCourierName: String?,
    hapticsEnabled: Boolean,
    onSendMessage: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var draft by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val haptics = LocalHapticFeedback.current

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.lastIndex)
        }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .testTag("courier_chat_list"),
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(messages, key = { it.id }) { msg ->
                ChatBubble(message = msg)
            }
        }

        AnimatedVisibility(visible = typingCourierName != null) {
            Text(
                text = "${typingCourierName ?: ""} печатает...",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )
        }

        LazyRow(
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(QUICK_CHAT_PHRASES) { phrase ->
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .clickable {
                            if (hapticsEnabled) {
                                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            }
                            onSendMessage(phrase)
                        },
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    shape = RoundedCornerShape(50)
                ) {
                    Text(
                        text = phrase,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                    )
                }
            }
        }

        ChatInputRow(
            draft = draft,
            placeholder = "Написать коллегам в чат...",
            hapticsEnabled = hapticsEnabled,
            onDraftChange = { draft = it },
            onSend = {
                if (draft.isNotBlank()) {
                    onSendMessage(draft.trim())
                    draft = ""
                }
            }
        )
    }
}

@Composable
private fun DmInboxList(
    threads: List<DirectThread>,
    onOpenThread: (DirectThread) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxWidth()
            .testTag("dm_inbox_list"),
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(threads, key = { it.id }) { thread ->
            val lastMessage = thread.messages.lastOrNull()
            val avatarColor = Color(thread.avatarColorHex)

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .clickable { onOpenThread(thread) }
                    .testTag("dm_thread_${thread.id}"),
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surfaceContainer,
                border = BorderStroke(
                    width = 1.dp,
                    color = if (thread.unreadCount > 0) AmberAlert.copy(alpha = 0.65f) else MaterialTheme.colorScheme.outlineVariant
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(avatarColor.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = thread.personName.take(1),
                            style = MaterialTheme.typography.titleMedium,
                            color = avatarColor
                        )
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = thread.personName,
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = lastMessage?.timestampLabel ?: "",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = thread.personRole,
                            style = MaterialTheme.typography.labelSmall,
                            color = CoralAlert
                        )
                        Text(
                            text = lastMessage?.text ?: "",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    if (thread.unreadCount > 0) {
                        Badge(
                            containerColor = CoralAlert,
                            contentColor = Color.White
                        ) {
                            Text("${thread.unreadCount}")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DmConversationBody(
    thread: DirectThread,
    hapticsEnabled: Boolean,
    onSendDm: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var draft by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val haptics = LocalHapticFeedback.current

    LaunchedEffect(thread.messages.size) {
        if (thread.messages.isNotEmpty()) {
            listState.animateScrollToItem(thread.messages.lastIndex)
        }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .testTag("dm_messages_list"),
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(thread.messages, key = { it.id }) { msg ->
                DmBubble(
                    message = msg,
                    senderName = thread.personName
                )
            }
        }

        val threadExcuses = remember(thread.id) {
            InfiniteGigGenerator.quickDmExcusesForThread(thread.id)
        }

        LazyRow(
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(threadExcuses) { excuse ->
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .clickable {
                            if (hapticsEnabled) {
                                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            }
                            onSendDm(excuse)
                        },
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    shape = RoundedCornerShape(50)
                ) {
                    Text(
                        text = excuse,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                    )
                }
            }
        }

        ChatInputRow(
            draft = draft,
            placeholder = "Оправдаться в ЛС...",
            hapticsEnabled = hapticsEnabled,
            onDraftChange = { draft = it },
            onSend = {
                if (draft.isNotBlank()) {
                    onSendDm(draft.trim())
                    draft = ""
                }
            }
        )
    }
}

@Composable
private fun ChatInputRow(
    draft: String,
    placeholder: String,
    hapticsEnabled: Boolean,
    onDraftChange: (String) -> Unit,
    onSend: () -> Unit
) {
    val haptics = LocalHapticFeedback.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedTextField(
            value = draft,
            onValueChange = onDraftChange,
            placeholder = { Text(placeholder) },
            singleLine = true,
            shape = RoundedCornerShape(24.dp),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
            keyboardActions = KeyboardActions(onSend = { onSend() }),
            modifier = Modifier
                .weight(1f)
                .testTag("chat_message_input")
        )

        FilledIconButton(
            onClick = {
                if (draft.isNotBlank()) {
                    if (hapticsEnabled) {
                        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    }
                    onSend()
                }
            },
            enabled = draft.isNotBlank(),
            modifier = Modifier
                .size(48.dp)
                .testTag("chat_send_button"),
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.Send,
                contentDescription = "Отправить сообщение"
            )
        }
    }
}

@Composable
private fun ChatBubble(message: ChatMessage) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isFromMe) Arrangement.End else Arrangement.Start
    ) {
        Surface(
            modifier = Modifier.widthIn(max = 310.dp),
            shape = RoundedCornerShape(
                topStart = 18.dp,
                topEnd = 18.dp,
                bottomStart = if (message.isFromMe) 18.dp else 4.dp,
                bottomEnd = if (message.isFromMe) 4.dp else 18.dp
            ),
            color = if (message.isFromMe) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceContainer
            },
            border = BorderStroke(
                width = 1.dp,
                color = if (message.isFromMe) {
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.45f)
                } else {
                    MaterialTheme.colorScheme.outlineVariant
                }
            )
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 13.dp, vertical = 9.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (!message.isFromMe) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = message.senderName,
                            style = MaterialTheme.typography.labelLarge,
                            color = BoltEmerald
                        )
                        Surface(
                            color = AmberAlert.copy(alpha = 0.18f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = message.senderBadge,
                                style = MaterialTheme.typography.labelSmall,
                                color = AmberAlert,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                            )
                        }
                    }
                }

                Text(
                    text = message.text,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        text = message.timestampLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun DmBubble(
    message: DirectMessage,
    senderName: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isFromMe) Arrangement.End else Arrangement.Start
    ) {
        Surface(
            modifier = Modifier.widthIn(max = 310.dp),
            shape = RoundedCornerShape(
                topStart = 18.dp,
                topEnd = 18.dp,
                bottomStart = if (message.isFromMe) 18.dp else 4.dp,
                bottomEnd = if (message.isFromMe) 4.dp else 18.dp
            ),
            color = if (message.isFromMe) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                CoralAlert.copy(alpha = 0.11f)
            },
            border = BorderStroke(
                width = 1.dp,
                color = if (message.isFromMe) {
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.45f)
                } else {
                    CoralAlert.copy(alpha = 0.5f)
                }
            )
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 13.dp, vertical = 9.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (!message.isFromMe) {
                    Text(
                        text = senderName,
                        style = MaterialTheme.typography.labelLarge,
                        color = CoralAlert
                    )
                }

                Text(
                    text = message.text,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        text = message.timestampLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun CallsHeader(callCount: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(CoralAlert.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.PhoneCallback,
                    contentDescription = null,
                    tint = CoralAlert,
                    modifier = Modifier.size(18.dp)
                )
            }
            Column {
                Text(
                    text = "Входящие жалобы",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Клиенты звонят сами во время смены",
                    style = MaterialTheme.typography.labelSmall,
                    color = CoralAlert
                )
            }
        }

        Surface(
            color = CoralAlert.copy(alpha = 0.16f),
            shape = RoundedCornerShape(50)
        ) {
            Text(
                text = "$callCount вызовов",
                fontFamily = JetBrainsMonoFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                color = CoralAlert,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
            )
        }
    }
}

@Composable
private fun CallLogSectionBody(
    callHistory: List<CallHistoryEntry>,
    hapticsEnabled: Boolean,
    onTriggerComplaintCall: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptics = LocalHapticFeedback.current

    LazyColumn(
        modifier = modifier
            .fillMaxWidth()
            .testTag("call_history_list"),
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                border = BorderStroke(1.dp, CoralAlert.copy(alpha = 0.45f))
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Горячая линия недовольных клиентов",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Клиенты регулярно дозваниваются с жалобами на шкаф, пылесос, 5-ю передачу и поломки Trek. Можно принять звонок прямо сейчас:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Button(
                        onClick = {
                            if (hapticsEnabled) {
                                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            }
                            onTriggerComplaintCall()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("simulate_complaint_call_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CoralAlert,
                            contentColor = Color.White
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Call,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Принять звонок с жалобой",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
        }

        items(callHistory, key = { it.id }) { entry ->
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceContainer,
                border = BorderStroke(
                    1.dp,
                    if (entry.wasAnswered) BoltEmerald.copy(alpha = 0.3f) else CoralAlert.copy(alpha = 0.45f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(13.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(
                                if (entry.wasAnswered) BoltEmerald.copy(alpha = 0.16f)
                                else CoralAlert.copy(alpha = 0.16f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (entry.wasAnswered) Icons.Rounded.PhoneCallback else Icons.Rounded.CallMissed,
                            contentDescription = null,
                            tint = if (entry.wasAnswered) BoltEmerald else CoralAlert,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = entry.callerName,
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = entry.timestampLabel,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = entry.callerRole,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = entry.summary,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = entry.outcomeLabel,
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (entry.wasAnswered) BoltEmerald else CoralAlert
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(74.dp))
        }
    }
}
