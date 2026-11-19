package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.AmberAlert
import com.example.ui.theme.BoltEmerald
import com.example.ui.theme.JetBrainsMonoFontFamily

/**
 * Dedicated real-time Shift Timer ("Таймер смены") component.
 * Displays elapsed HH:MM:SS duration since the courier started working,
 * a live 60-second circular sweep dial, shift start timestamp, hourly pace,
 * and Start/Pause/Finish controls.
 */
@Composable
fun ShiftTimerCard(
    isOnShift: Boolean,
    shiftSeconds: Long,
    shiftStartTimeLabel: String?,
    shiftEarnedRub: Int,
    onToggleShift: () -> Unit,
    onResetTimer: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hasProgress = shiftSeconds > 0L || isOnShift
    val formattedTime = formatShiftClock(shiftSeconds)

    val infiniteTransition = rememberInfiniteTransition(label = "timerPulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "timerPulseAlpha"
    )

    val secondsInMinute = (shiftSeconds % 60L).toFloat()
    val animatedDialFraction by animateFloatAsState(
        targetValue = if (isOnShift && shiftSeconds > 0L && secondsInMinute == 0f) {
            1f
        } else {
            secondsInMinute / 60f
        },
        animationSpec = tween(durationMillis = 280),
        label = "dialSweep"
    )

    val borderColor by animateColorAsState(
        targetValue = when {
            isOnShift -> MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
            shiftSeconds > 0L -> AmberAlert.copy(alpha = 0.55f)
            else -> MaterialTheme.colorScheme.outlineVariant
        },
        label = "timerBorder"
    )

    val hourlyPaceRub = if (shiftSeconds >= 10L) {
        ((shiftEarnedRub * 3600.0) / shiftSeconds).toInt()
    } else {
        shiftEarnedRub
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("shift_timer_card"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        border = BorderStroke(1.5.dp, borderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Title & live status badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(9.dp)
                            .alpha(if (isOnShift) pulseAlpha else 0.9f)
                            .clip(CircleShape)
                            .background(
                                when {
                                    isOnShift -> BoltEmerald
                                    shiftSeconds > 0L -> AmberAlert
                                    else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                }
                            )
                    )
                    Text(
                        text = stringResource(R.string.shift_timer_title),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Surface(
                    color = when {
                        isOnShift -> MaterialTheme.colorScheme.primaryContainer
                        shiftSeconds > 0L -> AmberAlert.copy(alpha = 0.16f)
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    },
                    shape = RoundedCornerShape(50)
                ) {
                    Text(
                        text = when {
                            isOnShift -> stringResource(R.string.shift_timer_running)
                            shiftSeconds > 0L -> stringResource(R.string.shift_timer_paused)
                            else -> stringResource(R.string.shift_timer_idle)
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = when {
                            isOnShift -> MaterialTheme.colorScheme.primary
                            shiftSeconds > 0L -> AmberAlert
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            // Dial + Large Monospace Clock + Shift Meta
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                SecondsSweepDial(
                    fraction = animatedDialFraction,
                    isOnShift = isOnShift,
                    hasProgress = shiftSeconds > 0L,
                    secondsRemainder = (shiftSeconds % 60L).toInt()
                )

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = formattedTime,
                        fontFamily = JetBrainsMonoFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 32.sp,
                        letterSpacing = 0.5.sp,
                        color = if (isOnShift) {
                            MaterialTheme.colorScheme.onSurface
                        } else {
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.78f)
                        },
                        modifier = Modifier.testTag("shift_timer_readout")
                    )

                    if (shiftStartTimeLabel != null) {
                        Text(
                            text = "Начало работы: $shiftStartTimeLabel • Темп ~$hourlyPaceRub ₽/ч",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Text(
                            text = "Нажмите «Начать», чтобы запустить отсчёт смены",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Start / Pause & Finish Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onToggleShift,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("shift_timer_toggle_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isOnShift) {
                            MaterialTheme.colorScheme.surfaceVariant
                        } else {
                            MaterialTheme.colorScheme.primary
                        },
                        contentColor = if (isOnShift) {
                            MaterialTheme.colorScheme.onSurface
                        } else {
                            MaterialTheme.colorScheme.onPrimary
                        }
                    ),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Icon(
                        imageVector = if (isOnShift) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = when {
                            isOnShift -> "Пауза"
                            shiftSeconds > 0L -> "Продолжить"
                            else -> "Начать смену"
                        },
                        style = MaterialTheme.typography.labelLarge
                    )
                }

                OutlinedButton(
                    onClick = onResetTimer,
                    enabled = hasProgress,
                    modifier = Modifier
                        .height(48.dp)
                        .testTag("shift_timer_reset_button"),
                    shape = RoundedCornerShape(14.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Stop,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Завершить",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}

@Composable
private fun SecondsSweepDial(
    fraction: Float,
    isOnShift: Boolean,
    hasProgress: Boolean,
    secondsRemainder: Int
) {
    val trackColor = MaterialTheme.colorScheme.surfaceVariant
    val activeColor = when {
        isOnShift -> BoltEmerald
        hasProgress -> AmberAlert
        else -> MaterialTheme.colorScheme.outline
    }
    val cd = stringResource(R.string.shift_timer_dial_cd, secondsRemainder)

    Box(
        modifier = Modifier
            .size(64.dp)
            .semantics { contentDescription = cd },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(64.dp)) {
            val strokePx = 6.dp.toPx()
            drawArc(
                color = trackColor,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )
            if (fraction > 0f) {
                drawArc(
                    color = activeColor,
                    startAngle = -90f,
                    sweepAngle = (fraction * 360f).coerceIn(4f, 360f),
                    useCenter = false,
                    style = Stroke(width = strokePx, cap = StrokeCap.Round)
                )
            }
        }
        Icon(
            imageVector = Icons.Rounded.Timer,
            contentDescription = null,
            tint = activeColor,
            modifier = Modifier.size(24.dp)
        )
    }
}

fun formatShiftClock(totalSeconds: Long): String {
    val safe = totalSeconds.coerceAtLeast(0L)
    val hrs = safe / 3600L
    val mins = (safe % 3600L) / 60L
    val secs = safe % 60L
    return String.format("%02d:%02d:%02d", hrs, mins, secs)
}
