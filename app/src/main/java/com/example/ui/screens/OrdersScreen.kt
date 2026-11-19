package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DirectionsBike
import androidx.compose.material.icons.rounded.Handyman
import androidx.compose.material.icons.rounded.LocalShipping
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.OpenInFull
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.CourierUiState
import com.example.data.DeliveryOrder
import com.example.data.OrderCategory
import com.example.data.OrderFilter
import com.example.data.OrderStage
import com.example.data.OrderSurgeMode
import com.example.ui.components.OrderSimulatorCard
import com.example.ui.theme.AmberAlert
import com.example.ui.theme.BoltEmerald
import com.example.ui.theme.CoralAlert
import com.example.ui.theme.JetBrainsMonoFontFamily

@Composable
fun OrdersScreen(
    state: CourierUiState,
    onSelectSurgeMode: (OrderSurgeMode) -> Unit,
    onSelectOrderFilter: (OrderFilter) -> Unit,
    onGenerateSimulatedOrder: () -> Unit,
    onAcceptSimulatedOrder: () -> Unit,
    onAcceptOrder: (String) -> Unit,
    onAdvanceActiveOrder: () -> Unit,
    onCancelActiveOrder: () -> Unit,
    onRefreshOrders: () -> Unit,
    onOpenOrderDetails: (DeliveryOrder) -> Unit,
    onOpenBikeGarage: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptics = LocalHapticFeedback.current

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .widthIn(max = 640.dp)
                .fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Лента заказов",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "Доставляй заказы и лови донаты на лечение",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    FilledTonalButton(
                        onClick = onRefreshOrders,
                        modifier = Modifier.testTag("refresh_orders_button"),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Refresh,
                            contentDescription = stringResource(R.string.refresh_orders),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Обновить", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }

            if (state.accumulatedBreakdowns.isNotEmpty()) {
                item {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("orders_breakdown_warning"),
                        shape = RoundedCornerShape(16.dp),
                        color = CoralAlert.copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, CoralAlert.copy(alpha = 0.65f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 11.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.WarningAmber,
                                    contentDescription = null,
                                    tint = CoralAlert,
                                    modifier = Modifier.size(22.dp)
                                )
                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Text(
                                        text = "Поломок Trek: ${state.accumulatedBreakdowns.size} • Выплата -${state.totalPayPenaltyPercent}%",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = CoralAlert
                                    )
                                    Text(
                                        text = if (state.highGearsBlocked) {
                                            "КПП 4–5 заклинило • ${state.publicAttitudeLabel}"
                                        } else {
                                            state.publicAttitudeLabel
                                        },
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            FilledTonalButton(
                                onClick = onOpenBikeGarage,
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier.testTag("orders_fix_bike_button")
                            ) {
                                Text("Чинить", style = MaterialTheme.typography.labelLarge)
                            }
                        }
                    }
                }
            }

            // Interactive Randomized Order Simulator Component
            item {
                OrderSimulatorCard(
                    simulatedOffer = state.simulatedOffer,
                    surgeMode = state.surgeMode,
                    orderFilter = state.orderFilter,
                    hasActiveOrder = state.activeOrder != null,
                    onSelectSurgeMode = { mode ->
                        if (state.hapticsEnabled) {
                            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        }
                        onSelectSurgeMode(mode)
                    },
                    onSelectOrderFilter = { filter ->
                        if (state.hapticsEnabled) {
                            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        }
                        onSelectOrderFilter(filter)
                    },
                    onGenerateOrder = {
                        if (state.hapticsEnabled) {
                            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        }
                        onGenerateSimulatedOrder()
                    },
                    onAcceptSimulatedOrder = {
                        if (state.hapticsEnabled) {
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        }
                        onAcceptSimulatedOrder()
                    },
                    onOpenDetails = onOpenOrderDetails
                )
            }

            item {
                AnimatedVisibility(
                    visible = state.activeOrder != null,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    state.activeOrder?.let { active ->
                        ActiveDeliveryCard(
                            order = active,
                            onOpenDetails = { onOpenOrderDetails(active) },
                            onAdvance = {
                                if (state.hapticsEnabled) {
                                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                }
                                onAdvanceActiveOrder()
                            },
                            onCancel = onCancelActiveOrder
                        )
                    }
                }
            }

            item {
                Text(
                    text = if (state.activeOrder == null) {
                        "Ближайшие заказы (${state.availableOrders.size})"
                    } else {
                        "Очередь после текущего заказа"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            if (state.availableOrders.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.DirectionsBike,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(40.dp)
                            )
                            Text(
                                text = "Пока тихо в этом районе",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Button(onClick = onRefreshOrders) {
                                Text("Найти заказы")
                            }
                        }
                    }
                }
            } else {
                items(state.availableOrders, key = { it.id }) { order ->
                    AvailableOrderCard(
                        order = order,
                        canAccept = state.activeOrder == null,
                        onOpenDetails = { onOpenOrderDetails(order) },
                        onAccept = {
                            if (state.hapticsEnabled) {
                                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            }
                            onAcceptOrder(order.id)
                        }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(76.dp))
            }
        }
    }
}

@Composable
private fun ActiveDeliveryCard(
    order: DeliveryOrder,
    onOpenDetails: () -> Unit,
    onAdvance: () -> Unit,
    onCancel: () -> Unit
) {
    val isPickingUp = order.stage == OrderStage.PICKING_UP
    val isSideGig = order.category == OrderCategory.SIDE_GIG
    val stepProgress = if (isPickingUp) 0.5f else 0.92f
    val accentColor = if (isSideGig) AmberAlert else MaterialTheme.colorScheme.primary

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .clickable(onClick = onOpenDetails)
            .testTag("active_order_card"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSideGig) {
                AmberAlert.copy(alpha = 0.14f)
            } else {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)
            }
        ),
        border = BorderStroke(1.5.dp, accentColor)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = accentColor,
                    shape = RoundedCornerShape(50)
                ) {
                    Text(
                        text = when {
                            isSideGig && isPickingUp -> "ШАГ 1/2 • ЕДЕМ НА ХАЛТУРКУ"
                            isSideGig -> "ШАГ 2/2 • ВЫПОЛНЯЕМ ЗАДАНИЕ"
                            isPickingUp -> "ШАГ 1/2 • ЕДЕМ В РЕСТОРАН"
                            else -> "ШАГ 2/2 • ВЕЗЁМ КЛИЕНТУ"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isSideGig) androidx.compose.ui.graphics.Color(0xFF2B1B00) else MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
                IconButton(
                    onClick = onCancel,
                    modifier = Modifier.testTag("cancel_order_button")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Отменить заказ",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = order.restaurant,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = order.itemSummary,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.LocationOn,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "${order.dropoffAddress} (${order.distanceKm} км)",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            LinearProgressIndicator(
                progress = { stepProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(50)),
                color = accentColor,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                strokeCap = StrokeCap.Round
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (isSideGig) "Оплата халтурки" else "Гарант. выплата",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "+${order.basePayRub}₽ + чай",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = BoltEmerald
                    )
                }

                Button(
                    onClick = onAdvance,
                    modifier = Modifier.testTag("advance_order_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Icon(
                        imageVector = if (isPickingUp) Icons.Rounded.LocalShipping else Icons.Rounded.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = when {
                            isSideGig && isPickingUp -> "На месте"
                            isSideGig -> "Готово!"
                            isPickingUp -> "Забрал заказ"
                            else -> "Доставлено!"
                        },
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}

@Composable
private fun AvailableOrderCard(
    order: DeliveryOrder,
    canAccept: Boolean,
    onOpenDetails: () -> Unit,
    onAccept: () -> Unit
) {
    val isSideGig = order.category == OrderCategory.SIDE_GIG
    val borderColor = if (isSideGig) AmberAlert.copy(alpha = 0.55f) else MaterialTheme.colorScheme.outlineVariant

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onOpenDetails)
            .testTag("order_card_${order.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        border = BorderStroke(1.dp, borderColor)
    ) {
        Column(
            modifier = Modifier.padding(15.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
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
                                if (isSideGig) {
                                    AmberAlert.copy(alpha = 0.16f)
                                } else {
                                    MaterialTheme.colorScheme.surfaceVariant
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isSideGig) Icons.Rounded.Handyman else Icons.Rounded.Restaurant,
                            contentDescription = null,
                            tint = if (isSideGig) AmberAlert else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        if (isSideGig) {
                            Text(
                                text = "ХАЛТУРКА • ДОП. ЗАКАЗ",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = AmberAlert
                            )
                        }
                        Text(
                            text = order.restaurant,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${order.district} • ${order.distanceKm} км",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "${order.basePayRub}₽",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = BoltEmerald
                )
            }

            Text(
                text = "${order.itemSummary} → ${order.dropoffAddress}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.OpenInFull,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(13.dp)
                )
                Text(
                    text = "Открыть полностью",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }

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
                        text = order.tipHint,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                if (canAccept) {
                    Button(
                        onClick = onAccept,
                        modifier = Modifier.testTag("accept_order_${order.id}"),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text("Принять", style = MaterialTheme.typography.labelLarge)
                    }
                } else {
                    OutlinedButton(
                        onClick = {},
                        enabled = false,
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text("В очереди", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }
    }
}
