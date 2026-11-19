package com.example.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateOffsetAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AmberAlert
import com.example.ui.theme.BoltEmerald
import com.example.ui.theme.JetBrainsMonoFontFamily
import kotlin.math.roundToInt

/**
 * Always-visible floating 5-speed H-pattern gearbox lever widget.
 * Users can tap any gear 1..5 to shift with spring animation + haptics,
 * or drag the widget anywhere on screen. Can be disabled in Settings.
 */
@Composable
fun GearboxFloatingWidget(
    currentGear: Int,
    hapticsEnabled: Boolean,
    highGearsBlocked: Boolean = false,
    onSelectGear: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptics = LocalHapticFeedback.current
    var dragX by remember { mutableFloatStateOf(0f) }
    var dragY by remember { mutableFloatStateOf(0f) }

    val gearHint = when {
        highGearsBlocked && currentGear == 3 -> "КЛИН 4-5"
        currentGear == 1 -> "В горку"
        currentGear == 2 -> "Разгон"
        currentGear == 3 -> "Город"
        currentGear == 4 -> "V1"
        else -> "ROTATE"
    }

    Surface(
        modifier = modifier
            .offset { IntOffset(dragX.roundToInt(), dragY.roundToInt()) }
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    dragX = (dragX + dragAmount.x).coerceIn(-620f, 80f)
                    dragY = (dragY + dragAmount.y).coerceIn(-900f, 60f)
                }
            }
            .width(116.dp)
            .shadow(12.dp, RoundedCornerShape(18.dp))
            .testTag("gearbox_floating_widget")
            .semantics {
                contentDescription = "Рычаг 5-ступенчатой коробки передач, выбрана $currentGear передача"
            },
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.96f),
        border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.6f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 7.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "КПП 5ст",
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 9.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${currentGear}•$gearHint",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 9.sp,
                    color = if (currentGear >= 4) AmberAlert else BoltEmerald
                )
            }

            // 5-Speed H-Gate + Animated Lever Knob
            HGateShiftBoard(
                currentGear = currentGear,
                highGearsBlocked = highGearsBlocked,
                onSelectGear = { gear ->
                    if (hapticsEnabled) {
                        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    }
                    onSelectGear(gear)
                }
            )
        }
    }
}

@Composable
private fun HGateShiftBoard(
    currentGear: Int,
    highGearsBlocked: Boolean,
    onSelectGear: (Int) -> Unit
) {
    // Coordinates inside a 96x64 dp area (fractional 0..1)
    // Top row: 1 (0.18, 0.22), 3 (0.50, 0.22), 5 (0.82, 0.22)
    // Bottom row: 2 (0.18, 0.78), 4 (0.50, 0.78)
    val targetFraction = when (currentGear) {
        1 -> Offset(0.18f, 0.22f)
        2 -> Offset(0.18f, 0.78f)
        3 -> Offset(0.50f, 0.22f)
        4 -> Offset(0.50f, 0.78f)
        else -> Offset(0.82f, 0.22f)
    }

    val animatedFraction by animateOffsetAsState(
        targetValue = targetFraction,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "gearLeverPos"
    )

    val gateLineColor = MaterialTheme.colorScheme.outline
    val knobColor = if (currentGear == 5) AmberAlert else BoltEmerald

    Box(
        modifier = Modifier
            .size(width = 96.dp, height = 64.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLowest)
    ) {
        // Draw H-pattern slots + shift shaft + knob
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val col1 = w * 0.18f
            val col2 = w * 0.50f
            val col3 = w * 0.82f
            val topY = h * 0.22f
            val midY = h * 0.50f
            val botY = h * 0.78f
            val gateStroke = 4.dp.toPx()

            // Horizontal neutral bar
            drawLine(
                color = gateLineColor,
                start = Offset(col1, midY),
                end = Offset(col3, midY),
                strokeWidth = gateStroke,
                cap = StrokeCap.Round
            )
            // Leg 1-2
            drawLine(
                color = gateLineColor,
                start = Offset(col1, topY),
                end = Offset(col1, botY),
                strokeWidth = gateStroke,
                cap = StrokeCap.Round
            )
            // Leg 3-4
            drawLine(
                color = gateLineColor,
                start = Offset(col2, topY),
                end = Offset(col2, botY),
                strokeWidth = gateStroke,
                cap = StrokeCap.Round
            )
            // Leg 5 (half leg top to mid)
            drawLine(
                color = gateLineColor,
                start = Offset(col3, topY),
                end = Offset(col3, midY),
                strokeWidth = gateStroke,
                cap = StrokeCap.Round
            )

            // Lever shaft from center neutral pivot to current knob
            val pivot = Offset(w * 0.50f, midY)
            val knobCenter = Offset(w * animatedFraction.x, h * animatedFraction.y)
            drawLine(
                color = Color(0xFFB0BEC5),
                start = pivot,
                end = knobCenter,
                strokeWidth = 3.dp.toPx(),
                cap = StrokeCap.Round
            )

            // Glowing shift knob
            drawCircle(
                color = knobColor,
                radius = 9.5.dp.toPx(),
                center = knobCenter
            )
            drawCircle(
                color = Color.White.copy(alpha = 0.85f),
                radius = 3.5.dp.toPx(),
                center = knobCenter
            )
        }

        // Clickable Gear Labels 1..5
        GearSlotLabel(
            gear = 1,
            selected = currentGear == 1,
            onClick = { onSelectGear(1) },
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 6.dp, top = 2.dp)
        )
        GearSlotLabel(
            gear = 3,
            selected = currentGear == 3,
            onClick = { onSelectGear(3) },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 2.dp)
        )
        GearSlotLabel(
            gear = 5,
            selected = currentGear == 5,
            blocked = highGearsBlocked,
            onClick = { onSelectGear(5) },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(end = 6.dp, top = 2.dp)
        )
        GearSlotLabel(
            gear = 2,
            selected = currentGear == 2,
            onClick = { onSelectGear(2) },
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 6.dp, bottom = 2.dp)
        )
        GearSlotLabel(
            gear = 4,
            selected = currentGear == 4,
            blocked = highGearsBlocked,
            onClick = { onSelectGear(4) },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 2.dp)
        )
    }
}

@Composable
private fun GearSlotLabel(
    gear: Int,
    selected: Boolean,
    blocked: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(22.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick)
            .testTag("gear_slot_$gear"),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "$gear",
            fontFamily = JetBrainsMonoFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            color = when {
                selected -> Color(0xFF042110)
                blocked -> Color(0xFFFF5A5F)
                else -> MaterialTheme.colorScheme.onSurface
            }
        )
    }
}
