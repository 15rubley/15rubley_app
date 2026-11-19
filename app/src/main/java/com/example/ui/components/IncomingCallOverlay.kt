package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.CallEnd
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.PhoneInTalk
import androidx.compose.material.icons.rounded.RecordVoiceOver
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CallDialogueLine
import com.example.data.CallResponseOption
import com.example.data.CallStatus
import com.example.data.IncomingComplaintCall
import com.example.ui.theme.AmberAlert
import com.example.ui.theme.BoltEmerald
import com.example.ui.theme.CoralAlert
import com.example.ui.theme.JetBrainsMonoFontFamily
import java.util.Locale
import kotlin.math.PI
import kotlin.math.sin

@Composable
fun IncomingCallOverlay(
    call: IncomingComplaintCall,
    hapticsEnabled: Boolean,
    onAnswer: () -> Unit,
    onDecline: () -> Unit,
    onSelectResponse: (CallResponseOption) -> Unit,
    onHangUp: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptics = LocalHapticFeedback.current
    val avatarColor = Color(call.avatarColorHex)

    val infiniteTransition = rememberInfiniteTransition(label = "callPulse")
    val ringScale by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.38f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1050, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ringScale"
    )
    val ringAlpha by infiniteTransition.animateFloat(
        initialValue = 0.65f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1050, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ringAlpha"
    )
    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2f * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 680, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wavePhase"
    )

    val mins = call.durationSeconds / 60
    val secs = call.durationSeconds % 60
    val timerLabel = String.format(Locale.US, "%02d:%02d", mins, secs)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xF60A110D),
                        Color(0xFA111C16),
                        Color(0xFD080D0A)
                    )
                )
            )
            .clickable(enabled = false) {}
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 14.dp)
            .testTag("incoming_call_overlay"),
        contentAlignment = Alignment.Center
    ) {
        if (call.status == CallStatus.RINGING) {
            RingingCallLayout(
                call = call,
                avatarColor = avatarColor,
                ringScale = ringScale,
                ringAlpha = ringAlpha,
                hapticsEnabled = hapticsEnabled,
                onAnswer = onAnswer,
                onDecline = onDecline
            )
        } else {
            ActiveOrEndedCallLayout(
                call = call,
                avatarColor = avatarColor,
                timerLabel = timerLabel,
                wavePhase = wavePhase,
                hapticsEnabled = hapticsEnabled,
                onSelectResponse = onSelectResponse,
                onHangUp = onHangUp
            )
        }
    }
}

@Composable
private fun RingingCallLayout(
    call: IncomingComplaintCall,
    avatarColor: Color,
    ringScale: Float,
    ringAlpha: Float,
    hapticsEnabled: Boolean,
    onAnswer: () -> Unit,
    onDecline: () -> Unit
) {
    val haptics = LocalHapticFeedback.current

    Column(
        modifier = Modifier
            .widthIn(max = 520.dp)
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Surface(
                color = CoralAlert.copy(alpha = 0.20f),
                shape = RoundedCornerShape(50),
                border = BorderStroke(1.dp, CoralAlert.copy(alpha = 0.7f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(7.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.WarningAmber,
                        contentDescription = null,
                        tint = CoralAlert,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "ВХОДЯЩИЙ ВЫЗОВ • ЖАЛОБА",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = Color(0xFFECF6F0)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Box(
                modifier = Modifier.size(124.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .scale(ringScale)
                        .alpha(ringAlpha)
                        .clip(CircleShape)
                        .background(avatarColor)
                )
                Box(
                    modifier = Modifier
                        .size(94.dp)
                        .clip(CircleShape)
                        .background(avatarColor.copy(alpha = 0.25f)),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(78.dp)
                            .clip(CircleShape)
                            .background(avatarColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = call.callerName.firstOrNull()?.toString() ?: "К",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    }
                }
            }

            Text(
                text = call.callerName,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )
            Text(
                text = "${call.callerRole} • ${call.phoneLabel}",
                fontFamily = JetBrainsMonoFontFamily,
                fontSize = 12.sp,
                color = Color(0xFF9EB5A8),
                textAlign = TextAlign.Center
            )

            Surface(
                color = Color(0xFF17251E),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFF2B4135))
            ) {
                Text(
                    text = "Повод: ${call.orderContext}",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color(0xFFD6E6DD),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 18.dp),
            shape = RoundedCornerShape(18.dp),
            color = Color(0xFF151F1A),
            border = BorderStroke(1.dp, CoralAlert.copy(alpha = 0.4f))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "Клиент звонит с новой претензией",
                    style = MaterialTheme.typography.titleSmall,
                    color = Color(0xFFECF6F0)
                )
                Text(
                    text = "Придётся выслушать и отбиваться несколько реплик подряд, пока клиент не выбесится — или сбросить за -0.4★.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF9EB5A8),
                    textAlign = TextAlign.Center
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilledIconButton(
                    onClick = {
                        if (hapticsEnabled) {
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        }
                        onDecline()
                    },
                    modifier = Modifier
                        .size(68.dp)
                        .testTag("call_decline_button"),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = CoralAlert,
                        contentColor = Color.White
                    )
                ) {
                    Icon(
                        imageVector = Icons.Rounded.CallEnd,
                        contentDescription = "Отклонить звонок",
                        modifier = Modifier.size(30.dp)
                    )
                }
                Text(
                    text = "Сбросить (-0.4★)",
                    style = MaterialTheme.typography.labelMedium,
                    color = CoralAlert
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilledIconButton(
                    onClick = {
                        if (hapticsEnabled) {
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        }
                        onAnswer()
                    },
                    modifier = Modifier
                        .size(68.dp)
                        .testTag("call_answer_button"),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = BoltEmerald,
                        contentColor = Color(0xFF042110)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Call,
                        contentDescription = "Ответить на звонок",
                        modifier = Modifier.size(30.dp)
                    )
                }
                Text(
                    text = "Ответить",
                    style = MaterialTheme.typography.labelMedium,
                    color = BoltEmerald
                )
            }
        }
    }
}

@Composable
private fun ActiveOrEndedCallLayout(
    call: IncomingComplaintCall,
    avatarColor: Color,
    timerLabel: String,
    wavePhase: Float,
    hapticsEnabled: Boolean,
    onSelectResponse: (CallResponseOption) -> Unit,
    onHangUp: () -> Unit
) {
    val haptics = LocalHapticFeedback.current
    val transcriptListState = rememberLazyListState()

    val angerFraction by animateFloatAsState(
        targetValue = (call.clientAngerPercent.coerceIn(0, 100) / 100f),
        animationSpec = tween(durationMillis = 420),
        label = "angerFraction"
    )
    val angerColor by animateColorAsState(
        targetValue = when {
            call.clientAngerPercent >= 80 -> CoralAlert
            call.clientAngerPercent >= 50 -> Color(0xFFFF7043)
            else -> AmberAlert
        },
        animationSpec = tween(durationMillis = 350),
        label = "angerColor"
    )

    val moodLabel = when {
        call.status == CallStatus.ENDED && call.droppedByClient -> "ВЫБЕСИЛСЯ И СБРОСИЛ!"
        call.clientAngerPercent >= 80 -> "НА ГРАНИ СРЫВА"
        call.clientAngerPercent >= 55 -> "ЗАКИПАЕТ"
        else -> "РАЗДРАЖЁН"
    }

    LaunchedEffect(call.dialogueHistory.size, call.isClientReacting) {
        val lastIdx = if (call.isClientReacting) {
            call.dialogueHistory.size
        } else {
            (call.dialogueHistory.size - 1).coerceAtLeast(0)
        }
        if (call.dialogueHistory.isNotEmpty()) {
            transcriptListState.animateScrollToItem(lastIdx)
        }
    }

    Column(
        modifier = Modifier
            .widthIn(max = 540.dp)
            .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Compact caller header + status badge
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            color = Color(0xFF15201B),
            border = BorderStroke(
                1.dp,
                if (call.status == CallStatus.ENDED) CoralAlert.copy(alpha = 0.65f) else BoltEmerald.copy(alpha = 0.4f)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(avatarColor),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = call.callerName.firstOrNull()?.toString() ?: "К",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = call.callerName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${call.callerRole} • ${call.orderContext}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF9EB5A8),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Surface(
                    color = if (call.status == CallStatus.ENDED) CoralAlert.copy(alpha = 0.22f) else BoltEmerald.copy(alpha = 0.18f),
                    shape = RoundedCornerShape(50)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Icon(
                            imageVector = if (call.status == CallStatus.ENDED) Icons.Rounded.CallEnd else Icons.Rounded.PhoneInTalk,
                            contentDescription = null,
                            tint = if (call.status == CallStatus.ENDED) CoralAlert else BoltEmerald,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = if (call.status == CallStatus.ENDED) "СБРОС" else timerLabel,
                            fontFamily = JetBrainsMonoFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = Color(0xFFECF6F0)
                        )
                    }
                }
            }
        }

        // Client Anger Meter
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            color = Color(0xFF171E1B),
            border = BorderStroke(1.dp, angerColor.copy(alpha = 0.45f))
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
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
                        Icon(
                            imageVector = Icons.Rounded.LocalFireDepartment,
                            contentDescription = null,
                            tint = angerColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = moodLabel,
                            fontFamily = JetBrainsMonoFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = angerColor
                        )
                    }
                    Text(
                        text = "Ярость ${call.clientAngerPercent}% • Ход ${call.turnNumber}/${call.maxTurns}",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 11.sp,
                        color = Color(0xFFD6E6DD)
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(7.dp)
                        .clip(RoundedCornerShape(50))
                        .background(Color(0xFF28342E))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(angerFraction)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(50))
                            .background(angerColor)
                    )
                }
            }
        }

        // Scrolling Dialogue Transcript
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            shape = RoundedCornerShape(18.dp),
            color = Color(0xFF111A15),
            border = BorderStroke(1.dp, Color(0xFF273A30))
        ) {
            LazyColumn(
                state = transcriptListState,
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("call_dialogue_list"),
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(call.dialogueHistory, key = { it.id }) { line ->
                    CallTranscriptBubble(line = line)
                }

                if (call.isClientReacting) {
                    item(key = "client_reacting_wave") {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = Color(0xFF1E1717),
                            border = BorderStroke(1.dp, CoralAlert.copy(alpha = 0.4f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.RecordVoiceOver,
                                    contentDescription = null,
                                    tint = CoralAlert,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "${call.callerName} набирает воздух...",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = CoralAlert
                                )
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(2.5.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    repeat(5) { idx ->
                                        val amp = 0.35f + 0.65f * ((sin(wavePhase + idx * 1.1f) + 1f) * 0.5f)
                                        Box(
                                            modifier = Modifier
                                                .width(3.dp)
                                                .height((4 + 11 * amp).dp)
                                                .clip(RoundedCornerShape(50))
                                                .background(CoralAlert)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Response options or Ended verdict
        if (call.status == CallStatus.ACTIVE) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                Text(
                    text = if (call.isClientReacting) "Слушаем реакцию клиента..." else "Ваш следующий ответ (варианты каждый раз новые):",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color(0xFF9EB5A8)
                )

                if (!call.isClientReacting) {
                    call.responseOptions.forEach { option ->
                        val deltaStr = "${if (option.ratingDelta >= 0) "+" else ""}${String.format(Locale.US, "%.1f", option.ratingDelta)}★"
                        val tipBadge = if (option.tipRub > 0) " • +${option.tipRub}₽" else ""
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 52.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .clickable {
                                    if (hapticsEnabled) {
                                        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    }
                                    onSelectResponse(option)
                                }
                                .testTag("call_reply_${option.id}"),
                            shape = RoundedCornerShape(14.dp),
                            color = Color(0xFF1A2921),
                            border = BorderStroke(1.dp, BoltEmerald.copy(alpha = 0.42f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 9.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(3.dp)
                                ) {
                                    if (option.styleTag.isNotBlank()) {
                                        Text(
                                            text = option.styleTag.uppercase(),
                                            fontFamily = JetBrainsMonoFontFamily,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = BoltEmerald
                                        )
                                    }
                                    Text(
                                        text = option.label,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFFECF6F0)
                                    )
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "$deltaStr$tipBadge",
                                        fontFamily = JetBrainsMonoFontFamily,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (option.tipRub > 0) BoltEmerald else AmberAlert
                                    )
                                    Text(
                                        text = "+${option.angerDelta}% ярость",
                                        fontFamily = JetBrainsMonoFontFamily,
                                        fontSize = 10.sp,
                                        color = CoralAlert.copy(alpha = 0.9f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        } else {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = CoralAlert.copy(alpha = 0.16f),
                border = BorderStroke(1.dp, CoralAlert.copy(alpha = 0.65f))
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = if (call.droppedByClient) "Клиент выбесился и бросил трубку!" else "Звонок завершён",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = CoralAlert
                    )
                    Text(
                        text = call.resolutionNote ?: "Итог зафиксирован в журнале вызовов",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 12.sp,
                        color = Color(0xFFECF6F0),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        Button(
            onClick = {
                if (hapticsEnabled) {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                }
                onHangUp()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("call_hangup_button"),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (call.status == CallStatus.ENDED) BoltEmerald else CoralAlert,
                contentColor = if (call.status == CallStatus.ENDED) Color(0xFF042110) else Color.White
            )
        ) {
            Icon(
                imageVector = Icons.Rounded.CallEnd,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (call.status == CallStatus.ENDED) "Закрыть вызов" else "Положить трубку первым (-0.2★)",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun CallTranscriptBubble(line: CallDialogueLine) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (line.isFromCourier) Arrangement.End else Arrangement.Start
    ) {
        Surface(
            modifier = Modifier.widthIn(max = 320.dp),
            shape = RoundedCornerShape(
                topStart = 14.dp,
                topEnd = 14.dp,
                bottomStart = if (line.isFromCourier) 14.dp else 4.dp,
                bottomEnd = if (line.isFromCourier) 4.dp else 14.dp
            ),
            color = if (line.isFromCourier) BoltEmerald.copy(alpha = 0.16f) else Color(0xFF1D2421),
            border = BorderStroke(
                1.dp,
                if (line.isFromCourier) BoltEmerald.copy(alpha = 0.45f) else CoralAlert.copy(alpha = 0.45f)
            )
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 11.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text = line.speakerName,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (line.isFromCourier) BoltEmerald else CoralAlert
                )
                Text(
                    text = line.text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFFECF6F0)
                )
            }
        }
    }
}
