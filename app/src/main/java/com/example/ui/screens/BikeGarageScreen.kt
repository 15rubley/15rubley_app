package com.example.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Build
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.BikeBreakdownAlert
import com.example.data.BikeModItem
import com.example.data.CourierUiState
import com.example.data.GarageTrack
import com.example.ui.theme.AmberAlert
import com.example.ui.theme.BoltEmerald
import com.example.ui.theme.CoralAlert
import com.example.ui.theme.JetBrainsMonoFontFamily
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun BikeGarageScreen(
    state: CourierUiState,
    onToggleGarageMusic: () -> Unit,
    onSelectGarageTrack: (GarageTrack) -> Unit,
    onNextGarageTrack: () -> Unit,
    onPreviousGarageTrack: () -> Unit,
    onToggleGarageAutoMix: () -> Unit,
    onRepairBreakdown: (Long) -> Unit,
    onRepairAllBreakdowns: () -> Unit,
    onBuyOrToggleMod: (BikeModItem) -> Unit,
    onTriggerDiagnosticCheck: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptics = LocalHapticFeedback.current
    val conditionColor = when {
        state.bikeConditionPercent >= 70 -> BoltEmerald
        state.bikeConditionPercent >= 40 -> AmberAlert
        else -> CoralAlert
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .widthIn(max = 640.dp)
                .fillMaxSize()
                .testTag("bike_garage_list"),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Велогараж Trek",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "Ремонт поломок, тюнинг и кач на районе",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.AccountBalanceWallet,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "${state.walletBalanceRub}₽",
                                fontFamily = JetBrainsMonoFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = BoltEmerald,
                                modifier = Modifier.testTag("garage_wallet_balance")
                            )
                        }
                    }
                }
            }

            // Multi-Genre Garage Boombox Radio
            item {
                GarageMusicPlayerCard(
                    currentTrack = state.currentGarageTrack,
                    autoMixEnabled = state.garageAutoMix,
                    isPlaying = state.isGarageMusicPlaying,
                    musicEnabled = state.garageMusicEnabled,
                    soundsEnabled = state.soundsEnabled,
                    onTogglePlay = {
                        if (state.hapticsEnabled) {
                            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        }
                        onToggleGarageMusic()
                    },
                    onSelectTrack = { track ->
                        if (state.hapticsEnabled) {
                            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        }
                        onSelectGarageTrack(track)
                    },
                    onNextTrack = {
                        if (state.hapticsEnabled) {
                            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        }
                        onNextGarageTrack()
                    },
                    onPreviousTrack = {
                        if (state.hapticsEnabled) {
                            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        }
                        onPreviousGarageTrack()
                    },
                    onToggleAutoMix = {
                        if (state.hapticsEnabled) {
                            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        }
                        onToggleGarageAutoMix()
                    }
                )
            }

            // Interactive Trek Bike Canvas Reflecting Tuning & Breakdowns
            item {
                BikeBlueprintStageCard(
                    state = state,
                    conditionColor = conditionColor
                )
            }

            // Work & Attitude Impact Dashboard
            item {
                BikeImpactStatusCard(
                    state = state,
                    conditionColor = conditionColor
                )
            }

            // Accumulated Breakdowns Repair Section
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Накопленные поломки (${state.accumulatedBreakdowns.size})",
                            style = MaterialTheme.typography.titleMedium,
                            color = if (state.accumulatedBreakdowns.isNotEmpty()) CoralAlert else MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = if (state.accumulatedBreakdowns.isEmpty()) {
                                "Всё чисто — клиенты не жалуются на грохот"
                            } else {
                                "Режут выплаты, клинят КПП и портят отношение людей"
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (state.accumulatedBreakdowns.size > 1) {
                        val allCost = state.totalRepairAllCostRub
                        val canAffordAll = state.walletBalanceRub >= allCost
                        Button(
                            onClick = {
                                if (state.hapticsEnabled) {
                                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                }
                                onRepairAllBreakdowns()
                            },
                            enabled = canAffordAll,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CoralAlert,
                                contentColor = Color.White
                            ),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                            modifier = Modifier.testTag("repair_all_button")
                        ) {
                            Text(
                                text = "Всё за ${allCost}₽ (-15%)",
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }
                }
            }

            if (state.accumulatedBreakdowns.isEmpty()) {
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        color = MaterialTheme.colorScheme.surfaceContainer,
                        border = BorderStroke(1.dp, BoltEmerald.copy(alpha = 0.4f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.CheckCircle,
                                    contentDescription = null,
                                    tint = BoltEmerald,
                                    modifier = Modifier.size(24.dp)
                                )
                                Column {
                                    Text(
                                        text = "Trek в полном порядке",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Поломки копятся во время смен и при сильном износе",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            OutlinedButton(
                                onClick = onTriggerDiagnosticCheck,
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                modifier = Modifier.testTag("test_breakdown_button")
                            ) {
                                Text("Тест кочки", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            } else {
                items(state.accumulatedBreakdowns, key = { it.id }) { issue ->
                    AccumulatedBreakdownCard(
                        issue = issue,
                        canAfford = state.walletBalanceRub >= issue.repairCostRub,
                        onRepair = {
                            if (state.hapticsEnabled) {
                                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            }
                            onRepairBreakdown(issue.id)
                        }
                    )
                }
            }

            // Tuning & Modifications Section
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Модификации и тюнинг",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "Установлено: ${state.equippedBikeModIds.size}/${state.bikeModCatalog.size} • Стиль ${state.totalBikeStyleScore}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            items(state.bikeModCatalog, key = { it.id }) { mod ->
                val isOwned = state.ownedBikeModIds.contains(mod.id)
                val isEquipped = state.equippedBikeModIds.contains(mod.id)
                val canAfford = state.walletBalanceRub >= mod.priceRub

                BikeModCard(
                    mod = mod,
                    isOwned = isOwned,
                    isEquipped = isEquipped,
                    canAfford = canAfford,
                    onClick = {
                        if (state.hapticsEnabled) {
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        }
                        onBuyOrToggleMod(mod)
                    }
                )
            }

            item {
                Spacer(modifier = Modifier.height(78.dp))
            }
        }
    }
}

@Composable
private fun GarageMusicPlayerCard(
    currentTrack: GarageTrack,
    autoMixEnabled: Boolean,
    isPlaying: Boolean,
    musicEnabled: Boolean,
    soundsEnabled: Boolean,
    onTogglePlay: () -> Unit,
    onSelectTrack: (GarageTrack) -> Unit,
    onNextTrack: () -> Unit,
    onPreviousTrack: () -> Unit,
    onToggleAutoMix: () -> Unit
) {
    val trackAccent = Color(currentTrack.accentColorHex)
    val beatMs = (60_000 / currentTrack.bpm).coerceIn(280, 700)
    val infiniteTransition = rememberInfiniteTransition(label = "eq")
    val beatPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2f * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = beatMs, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "beatPhase"
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("garage_music_card"),
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFF0E1713),
        border = BorderStroke(
            1.5.dp,
            if (isPlaying) trackAccent.copy(alpha = 0.85f) else MaterialTheme.colorScheme.outline
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            trackAccent.copy(alpha = if (isPlaying) 0.14f else 0.05f),
                            Color.Transparent
                        )
                    )
                )
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(trackAccent.copy(alpha = 0.18f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.MusicNote,
                            contentDescription = null,
                            tint = trackAccent,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = currentTrack.title,
                            style = MaterialTheme.typography.titleSmall,
                            color = Color(0xFFECF6F0),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = when {
                                !soundsEnabled -> "Звуки выключены в настройках"
                                isPlaying -> "${currentTrack.bpm} BPM • ${currentTrack.genreBadge}"
                                !musicEnabled -> "На паузе • ${currentTrack.genreBadge}"
                                else -> "Готов к басам"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isPlaying) trackAccent else Color(0xFF9EB5A8),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // BPM-synced EQ bars
                Row(
                    modifier = Modifier.padding(start = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    repeat(7) { idx ->
                        val frac = if (isPlaying) {
                            (0.28f + 0.72f * ((sin(beatPhase + idx * 0.95f) + 1f) * 0.5f))
                        } else {
                            0.18f
                        }
                        Box(
                            modifier = Modifier
                                .width(3.5.dp)
                                .height((6 + 20 * frac).dp)
                                .clip(RoundedCornerShape(50))
                                .background(if (isPlaying) trackAccent else Color(0xFF4F6659))
                        )
                    }
                }
            }

            // Station selector chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                GarageTrack.entries.forEach { track ->
                    val selected = track == currentTrack
                    val chipAccent = Color(track.accentColorHex)
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onSelectTrack(track) }
                            .testTag("garage_track_${track.id}"),
                        shape = RoundedCornerShape(12.dp),
                        color = if (selected) chipAccent.copy(alpha = 0.22f) else Color(0xFF17241E),
                        border = BorderStroke(
                            1.dp,
                            if (selected) chipAccent else Color(0xFF2D4036)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 11.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(if (selected) chipAccent else Color(0xFF567063))
                            )
                            Text(
                                text = track.shortLabel,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                color = if (selected) chipAccent else Color(0xFFB7C9BF),
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            // Transport controls + Auto-DJ toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onToggleAutoMix() }
                        .testTag("garage_automix_toggle"),
                    shape = RoundedCornerShape(12.dp),
                    color = if (autoMixEnabled) trackAccent.copy(alpha = 0.16f) else Color(0xFF17241E),
                    border = BorderStroke(
                        1.dp,
                        if (autoMixEnabled) trackAccent.copy(alpha = 0.7f) else Color(0xFF2D4036)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Shuffle,
                            contentDescription = null,
                            tint = if (autoMixEnabled) trackAccent else Color(0xFF8EA599),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = if (autoMixEnabled) "Авто-DJ вкл" else "Один трек",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (autoMixEnabled) trackAccent else Color(0xFF8EA599)
                        )
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onPreviousTrack,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("garage_music_prev_button"),
                        colors = IconButtonDefaults.iconButtonColors(
                            contentColor = Color(0xFFDFECE5)
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.SkipPrevious,
                            contentDescription = "Предыдущий трек"
                        )
                    }

                    FilledIconButton(
                        onClick = onTogglePlay,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("garage_music_toggle_button"),
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = trackAccent,
                            contentColor = Color(0xFF06160E)
                        )
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                            contentDescription = if (isPlaying) "Пауза музыки" else "Включить музыку гаража"
                        )
                    }

                    IconButton(
                        onClick = onNextTrack,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("garage_music_next_button"),
                        colors = IconButtonDefaults.iconButtonColors(
                            contentColor = Color(0xFFDFECE5)
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.SkipNext,
                            contentDescription = "Следующий трек"
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BikeBlueprintStageCard(
    state: CourierUiState,
    conditionColor: Color
) {
    val hasNeon = state.equippedBikeModIds.contains("mod_neon")
    val hasSubwoofer = state.equippedBikeModIds.contains("mod_subwoofer")
    val hasSpoiler = state.equippedBikeModIds.contains("mod_spoiler")
    val hasGoldChain = state.equippedBikeModIds.contains("mod_gold_chain")
    val hasSpinners = state.equippedBikeModIds.contains("mod_spinners")
    val hasExhaust = state.equippedBikeModIds.contains("mod_boeing_exhaust")
    val issueCount = state.accumulatedBreakdowns.size

    val infiniteTransition = rememberInfiniteTransition(label = "bikeAnim")
    val wheelSpin by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (state.isGarageMusicPlaying) 1400 else 2800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wheelSpin"
    )
    val bassPulse by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.18f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 297, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bassPulse"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("bike_visual_stage"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0B1410)),
        border = BorderStroke(1.5.dp, if (hasNeon) BoltEmerald else MaterialTheme.colorScheme.outline)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "TREK MARLIN • BOLT SPEC",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = BoltEmerald
                )
                Surface(
                    color = conditionColor.copy(alpha = 0.18f),
                    shape = RoundedCornerShape(50)
                ) {
                    Text(
                        text = "Состояние ${state.bikeConditionPercent}%",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = conditionColor,
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            ) {
                val w = size.width
                val h = size.height
                val groundY = h * 0.83f
                val rearWheelCenter = Offset(w * 0.28f, groundY - h * 0.20f)
                val frontWheelCenter = Offset(w * 0.73f, groundY - h * 0.20f)
                val wheelRadius = h * 0.20f
                val wobbleOffset = if (issueCount > 0) sin(wheelSpin * 0.14f) * (issueCount * 1.4f) else 0f

                // Garage spotlight gradient
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFF163526), Color(0xFF0B1410)),
                        center = Offset(w * 0.5f, h * 0.45f),
                        radius = w * 0.65f
                    )
                )

                // Floor line
                drawLine(
                    color = Color(0xFF2B4236),
                    start = Offset(w * 0.06f, groundY),
                    end = Offset(w * 0.94f, groundY),
                    strokeWidth = 2.dp.toPx()
                )

                // Neon Underglow
                if (hasNeon) {
                    val glowPulse = if (state.isGarageMusicPlaying) bassPulse else 1f
                    drawOval(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                BoltEmerald.copy(alpha = 0.65f),
                                Color(0xFF00E5FF).copy(alpha = 0.28f),
                                Color.Transparent
                            ),
                            center = Offset(w * 0.50f, groundY + 4f),
                            radius = w * 0.34f * glowPulse
                        ),
                        topLeft = Offset(w * 0.16f, groundY - 12f),
                        size = Size(w * 0.68f, 28f)
                    )
                }

                // Wheels
                listOf(rearWheelCenter to wobbleOffset, frontWheelCenter to 0f).forEach { (center, wob) ->
                    val adjustedCenter = Offset(center.x, center.y + wob)
                    // Tire
                    drawCircle(
                        color = Color(0xFF1D2421),
                        radius = wheelRadius,
                        center = adjustedCenter,
                        style = Stroke(width = 7.dp.toPx())
                    )
                    // Rim / Spinner
                    drawCircle(
                        color = if (hasSpinners) Color(0xFFE0F7FA) else Color(0xFF6E8578),
                        radius = wheelRadius - 5.dp.toPx(),
                        center = adjustedCenter,
                        style = Stroke(width = if (hasSpinners) 3.dp.toPx() else 2.dp.toPx())
                    )
                    // Spinning spokes
                    val spokeCount = if (hasSpinners) 8 else 6
                    for (s in 0 until spokeCount) {
                        val ang = (wheelSpin + s * (360f / spokeCount)) * (PI / 180.0)
                        val ex = adjustedCenter.x + (wheelRadius - 6.dp.toPx()) * cos(ang).toFloat()
                        val ey = adjustedCenter.y + (wheelRadius - 6.dp.toPx()) * sin(ang).toFloat()
                        drawLine(
                            color = if (hasSpinners) BoltEmerald.copy(alpha = 0.8f) else Color(0xFF53695D),
                            start = adjustedCenter,
                            end = Offset(ex, ey),
                            strokeWidth = if (hasSpinners) 2.2.dp.toPx() else 1.4.dp.toPx()
                        )
                    }
                    // Brake disc
                    drawCircle(
                        color = if (state.brakeHealthPercent < 30) CoralAlert else AmberAlert,
                        radius = wheelRadius * 0.30f,
                        center = adjustedCenter,
                        style = Stroke(width = 2.dp.toPx())
                    )
                }

                // Frame geometry points
                val crank = Offset(w * 0.47f, groundY - h * 0.19f)
                val seatPostTop = Offset(w * 0.40f, h * 0.34f)
                val headTubeTop = Offset(w * 0.64f, h * 0.33f)
                val headTubeBottom = Offset(w * 0.66f, h * 0.42f)

                val frameColor = Color(0xFF2A3B33)
                val frameStroke = 4.5.dp.toPx()

                // Rear triangle + main frame
                drawLine(frameColor, rearWheelCenter, crank, strokeWidth = frameStroke, cap = StrokeCap.Round)
                drawLine(frameColor, rearWheelCenter, seatPostTop, strokeWidth = frameStroke, cap = StrokeCap.Round)
                drawLine(frameColor, crank, seatPostTop, strokeWidth = frameStroke, cap = StrokeCap.Round)
                drawLine(frameColor, crank, headTubeBottom, strokeWidth = frameStroke, cap = StrokeCap.Round)
                drawLine(frameColor, seatPostTop, headTubeTop, strokeWidth = frameStroke, cap = StrokeCap.Round)
                drawLine(frameColor, headTubeTop, frontWheelCenter, strokeWidth = frameStroke, cap = StrokeCap.Round)

                // Handlebar & Saddle
                drawLine(Color(0xFF90A4AE), headTubeTop, Offset(w * 0.62f, h * 0.25f), strokeWidth = 3.5.dp.toPx(), cap = StrokeCap.Round)
                drawLine(Color(0xFF90A4AE), Offset(w * 0.59f, h * 0.25f), Offset(w * 0.67f, h * 0.24f), strokeWidth = 4.dp.toPx(), cap = StrokeCap.Round)
                drawLine(Color(0xFF90A4AE), seatPostTop, Offset(w * 0.39f, h * 0.28f), strokeWidth = 3.dp.toPx(), cap = StrokeCap.Round)
                drawLine(Color(0xFFCFD8DC), Offset(w * 0.34f, h * 0.28f), Offset(w * 0.43f, h * 0.28f), strokeWidth = 5.dp.toPx(), cap = StrokeCap.Round)

                // Gold or standard chain
                val chainColor = if (hasGoldChain) Color(0xFFFFD54F) else Color(0xFF78909C)
                drawLine(chainColor, rearWheelCenter, crank, strokeWidth = 2.5.dp.toPx())
                drawCircle(chainColor, radius = 7.dp.toPx(), center = crank, style = Stroke(width = 2.5.dp.toPx()))

                // Bolt Food Green Thermal Rack Bag
                drawRoundRect(
                    color = BoltEmerald,
                    topLeft = Offset(w * 0.19f, h * 0.30f),
                    size = Size(w * 0.15f, h * 0.24f),
                    cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
                )
                drawRoundRect(
                    color = Color(0xFF0A2E1B),
                    topLeft = Offset(w * 0.21f, h * 0.36f),
                    size = Size(w * 0.11f, h * 0.08f),
                    cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                )

                // Spoiler on rack
                if (hasSpoiler) {
                    val wing = Path().apply {
                        moveTo(w * 0.19f, h * 0.30f)
                        lineTo(w * 0.11f, h * 0.22f)
                        lineTo(w * 0.24f, h * 0.24f)
                        close()
                    }
                    drawPath(wing, color = Color(0xFF00E676))
                }

                // Subwoofer on frame/rack
                if (hasSubwoofer) {
                    val subCenter = Offset(w * 0.35f, h * 0.44f)
                    val pulseR = if (state.isGarageMusicPlaying) 11.dp.toPx() * bassPulse else 11.dp.toPx()
                    drawCircle(Color(0xFF1A1F1D), radius = 14.dp.toPx(), center = subCenter)
                    drawCircle(AmberAlert, radius = pulseR, center = subCenter, style = Stroke(width = 2.2.dp.toPx()))
                    drawCircle(BoltEmerald, radius = 4.dp.toPx(), center = subCenter)
                }

                // Boeing Stage 3 Exhaust pipes
                if (hasExhaust) {
                    drawLine(
                        color = Color(0xFFB0BEC5),
                        start = Offset(w * 0.38f, h * 0.35f),
                        end = Offset(w * 0.14f, h * 0.46f),
                        strokeWidth = 4.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                    drawCircle(
                        color = AmberAlert.copy(alpha = 0.85f),
                        radius = 4.5.dp.toPx(),
                        center = Offset(w * 0.13f, h * 0.465f)
                    )
                }

                // Breakdown smoke & spark markers
                if (issueCount > 0) {
                    val smokeRise = (wheelSpin / 360f) * 26.dp.toPx()
                    repeat(issueCount.coerceAtMost(4)) { idx ->
                        val sx = rearWheelCenter.x + (idx * 24f) - 10f
                        val sy = rearWheelCenter.y - 12.dp.toPx() - ((smokeRise + idx * 11f) % 34.dp.toPx())
                        drawCircle(
                            color = CoralAlert.copy(alpha = 0.45f),
                            radius = (5 + idx).dp.toPx(),
                            center = Offset(sx, sy)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BikeImpactStatusCard(
    state: CourierUiState,
    conditionColor: Color
) {
    val attitudeColor = when {
        state.accumulatedBreakdowns.size >= 3 -> CoralAlert
        state.accumulatedBreakdowns.isNotEmpty() -> AmberAlert
        else -> BoltEmerald
    }
    val payDelta = state.totalModPayBonusPercent - state.totalPayPenaltyPercent
    val payBadgeText = if (payDelta >= 0) "+$payDelta% к заказам" else "$payDelta% к заказам"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        border = BorderStroke(1.dp, attitudeColor.copy(alpha = 0.45f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Влияние на работу и людей",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Surface(
                    color = if (payDelta >= 0) BoltEmerald.copy(alpha = 0.16f) else CoralAlert.copy(alpha = 0.16f),
                    shape = RoundedCornerShape(50)
                ) {
                    Text(
                        text = payBadgeText,
                        fontFamily = JetBrainsMonoFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = if (payDelta >= 0) BoltEmerald else CoralAlert,
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                    )
                }
            }

            LinearProgressIndicator(
                progress = { (state.bikeConditionPercent / 100f).coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(50)),
                color = conditionColor,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Speed,
                                contentDescription = null,
                                tint = if (state.highGearsBlocked) CoralAlert else BoltEmerald,
                                modifier = Modifier.size(15.dp)
                            )
                            Text(
                                text = "КПП V1 / ROTATE",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = if (state.highGearsBlocked) "4–5 заклинило" else "Все 5 передач",
                            style = MaterialTheme.typography.titleSmall,
                            color = if (state.highGearsBlocked) CoralAlert else BoltEmerald
                        )
                    }
                }

                Surface(
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Text(
                            text = "Отношение людей",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = state.publicAttitudeLabel,
                            style = MaterialTheme.typography.labelLarge,
                            color = attitudeColor,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AccumulatedBreakdownCard(
    issue: BikeBreakdownAlert,
    canAfford: Boolean,
    onRepair: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("accumulated_breakdown_${issue.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        border = BorderStroke(1.2.dp, CoralAlert.copy(alpha = 0.7f))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.WarningAmber,
                        contentDescription = null,
                        tint = CoralAlert,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = issue.title,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                if (issue.detectedAtLabel.isNotBlank()) {
                    Text(
                        text = issue.detectedAtLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Text(
                text = issue.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Surface(
                color = CoralAlert.copy(alpha = 0.1f),
                shape = RoundedCornerShape(10.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 7.dp),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Text(
                        text = "Работа: ${issue.workImpact}",
                        style = MaterialTheme.typography.labelSmall,
                        color = CoralAlert
                    )
                    Text(
                        text = "Люди: ${issue.attitudeImpact}",
                        style = MaterialTheme.typography.labelSmall,
                        color = AmberAlert
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Button(
                    onClick = onRepair,
                    enabled = canAfford,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (canAfford) CoralAlert else MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = Color.White
                    ),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                    modifier = Modifier.testTag("repair_breakdown_${issue.id}")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Build,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (canAfford) "Починить ${issue.repairCostRub}₽" else "Нужно ${issue.repairCostRub}₽",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}

@Composable
private fun BikeModCard(
    mod: BikeModItem,
    isOwned: Boolean,
    isEquipped: Boolean,
    canAfford: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("bike_mod_${mod.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        border = BorderStroke(
            width = if (isEquipped) 1.5.dp else 1.dp,
            color = if (isEquipped) BoltEmerald else MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(
                                if (isEquipped) BoltEmerald.copy(alpha = 0.2f)
                                else MaterialTheme.colorScheme.surfaceVariant
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isEquipped) Icons.Rounded.AutoAwesome else Icons.Rounded.Tune,
                            contentDescription = null,
                            tint = if (isEquipped) BoltEmerald else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column {
                        Text(
                            text = mod.name,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = mod.subtitle,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = if (isOwned) "Куплено" else "${mod.priceRub}₽",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = if (isOwned) BoltEmerald else AmberAlert
                )
            }

            Text(
                text = mod.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    Text(
                        text = mod.effectBadge,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                when {
                    isEquipped -> FilledTonalButton(
                        onClick = onClick,
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text("Установлен", style = MaterialTheme.typography.labelLarge)
                    }
                    isOwned -> OutlinedButton(
                        onClick = onClick,
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text("Установить", style = MaterialTheme.typography.labelLarge)
                    }
                    else -> Button(
                        onClick = onClick,
                        enabled = canAfford,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = if (canAfford) "Поставить" else "Мало ₽",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
        }
    }
}
