package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesomeMosaic
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.DeliveryDining
import androidx.compose.material.icons.outlined.DirectionsBike
import androidx.compose.material.icons.outlined.Forum
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.rounded.AutoAwesomeMosaic
import androidx.compose.material.icons.rounded.Build
import androidx.compose.material.icons.rounded.DeliveryDining
import androidx.compose.material.icons.rounded.DirectionsBike
import androidx.compose.material.icons.rounded.Forum
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.R
import com.example.data.CourierTab
import com.example.ui.components.BikeBreakdownBanner
import com.example.ui.components.GearboxFloatingWidget
import com.example.ui.components.IncomingCallOverlay
import com.example.ui.components.IncomingDmBanner
import com.example.ui.components.IncomingTransferCard
import com.example.ui.components.OrderDetailsBottomSheet
import com.example.ui.components.SettingsBottomSheet
import com.example.ui.screens.BikeGarageScreen
import com.example.ui.screens.BrakeShopScreen
import com.example.ui.screens.CourierChatScreen
import com.example.ui.screens.MemeStudioScreen
import com.example.ui.screens.OrdersScreen
import com.example.ui.screens.ShiftScreen
import com.example.ui.theme.CoralAlert
import com.example.ui.theme.CourierTheme
import com.example.viewmodel.CourierViewModel

private data class NavDestination(
    val tab: CourierTab,
    val labelRes: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val tag: String
)

private val DESTINATIONS = listOf(
    NavDestination(
        tab = CourierTab.SHIFT,
        labelRes = R.string.tab_shift,
        selectedIcon = Icons.Rounded.DirectionsBike,
        unselectedIcon = Icons.Outlined.DirectionsBike,
        tag = "nav_shift"
    ),
    NavDestination(
        tab = CourierTab.ORDERS,
        labelRes = R.string.tab_orders,
        selectedIcon = Icons.Rounded.DeliveryDining,
        unselectedIcon = Icons.Outlined.DeliveryDining,
        tag = "nav_orders"
    ),
    NavDestination(
        tab = CourierTab.BIKE,
        labelRes = R.string.tab_bike,
        selectedIcon = Icons.Rounded.Tune,
        unselectedIcon = Icons.Outlined.Tune,
        tag = "nav_bike"
    ),
    NavDestination(
        tab = CourierTab.SHOP,
        labelRes = R.string.tab_shop,
        selectedIcon = Icons.Rounded.Build,
        unselectedIcon = Icons.Outlined.Build,
        tag = "nav_shop"
    ),
    NavDestination(
        tab = CourierTab.CHAT,
        labelRes = R.string.tab_chat,
        selectedIcon = Icons.Rounded.Forum,
        unselectedIcon = Icons.Outlined.Forum,
        tag = "nav_chat"
    ),
    NavDestination(
        tab = CourierTab.STUDIO,
        labelRes = R.string.tab_studio,
        selectedIcon = Icons.Rounded.AutoAwesomeMosaic,
        unselectedIcon = Icons.Outlined.AutoAwesomeMosaic,
        tag = "nav_studio"
    )
)

@Composable
fun CourierApp(
    viewModel: CourierViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    BackHandler(enabled = state.activeCall != null) {
        viewModel.hangUpCall()
    }

    BackHandler(enabled = state.activeCall == null && state.selectedTab != CourierTab.SHIFT) {
        viewModel.selectTab(CourierTab.SHIFT)
    }

    CourierTheme(darkTheme = state.isDarkTheme) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val useRail = maxWidth >= 600.dp

            Scaffold(
                modifier = Modifier.fillMaxSize(),
                contentWindowInsets = WindowInsets.safeDrawing,
                bottomBar = {
                    if (!useRail) {
                        NavigationBar(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                        ) {
                            DESTINATIONS.forEach { dest ->
                                val selected = state.selectedTab == dest.tab
                                val showActiveOrderDot = dest.tab == CourierTab.ORDERS && state.activeOrder != null
                                val unreadChat = if (dest.tab == CourierTab.CHAT) state.totalUnreadMessages else 0
                                val bikeIssues = if (dest.tab == CourierTab.BIKE) state.accumulatedBreakdowns.size else 0
                                NavigationBarItem(
                                    selected = selected,
                                    onClick = { viewModel.selectTab(dest.tab) },
                                    modifier = Modifier.testTag(dest.tag),
                                    icon = {
                                        BadgedBox(
                                            badge = {
                                                when {
                                                    bikeIssues > 0 -> Badge(
                                                        containerColor = CoralAlert,
                                                        contentColor = Color.White
                                                    ) { Text("$bikeIssues") }
                                                    unreadChat > 0 -> Badge { Text("$unreadChat") }
                                                    showActiveOrderDot -> Badge()
                                                }
                                            }
                                        ) {
                                            Icon(
                                                imageVector = if (selected) dest.selectedIcon else dest.unselectedIcon,
                                                contentDescription = stringResource(dest.labelRes)
                                            )
                                        }
                                    },
                                    label = {
                                        Text(
                                            text = stringResource(dest.labelRes),
                                            maxLines = 1,
                                            softWrap = false,
                                            fontSize = 10.sp
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            ) { innerPadding ->
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    if (useRail) {
                        NavigationRail(
                            modifier = Modifier.fillMaxHeight(),
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                        ) {
                            DESTINATIONS.forEach { dest ->
                                val selected = state.selectedTab == dest.tab
                                val showActiveOrderDot = dest.tab == CourierTab.ORDERS && state.activeOrder != null
                                val unreadChat = if (dest.tab == CourierTab.CHAT) state.totalUnreadMessages else 0
                                val bikeIssues = if (dest.tab == CourierTab.BIKE) state.accumulatedBreakdowns.size else 0
                                NavigationRailItem(
                                    selected = selected,
                                    onClick = { viewModel.selectTab(dest.tab) },
                                    modifier = Modifier.testTag(dest.tag),
                                    icon = {
                                        BadgedBox(
                                            badge = {
                                                when {
                                                    bikeIssues > 0 -> Badge(
                                                        containerColor = CoralAlert,
                                                        contentColor = Color.White
                                                    ) { Text("$bikeIssues") }
                                                    unreadChat > 0 -> Badge { Text("$unreadChat") }
                                                    showActiveOrderDot -> Badge()
                                                }
                                            }
                                        ) {
                                            Icon(
                                                imageVector = if (selected) dest.selectedIcon else dest.unselectedIcon,
                                                contentDescription = stringResource(dest.labelRes)
                                            )
                                        }
                                    },
                                    label = { Text(stringResource(dest.labelRes)) }
                                )
                            }
                        }
                    }

                    Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                        when (state.selectedTab) {
                            CourierTab.SHIFT -> ShiftScreen(
                                state = state,
                                onToggleShift = viewModel::toggleShift,
                                onResetShiftTimer = viewModel::resetShiftTimer,
                                onAttemptBoostRating = viewModel::attemptBoostRating,
                                onTriggerTip = { reason, amount ->
                                    viewModel.triggerIncomingTransfer(
                                        subtitle = "Входящий перевод",
                                        reason = reason,
                                        amountRub = amount
                                    )
                                },
                                onToggleTheme = viewModel::toggleDarkTheme,
                                onToggleHaptics = viewModel::toggleHaptics,
                                onOpenSettings = { viewModel.setSettingsOpen(true) },
                                onOpenOrders = { viewModel.selectTab(CourierTab.ORDERS) },
                                onTriggerComplaintCall = { viewModel.triggerIncomingComplaintCall() }
                            )

                            CourierTab.ORDERS -> OrdersScreen(
                                state = state,
                                onSelectSurgeMode = viewModel::setSurgeMode,
                                onSelectOrderFilter = viewModel::setOrderFilter,
                                onGenerateSimulatedOrder = viewModel::generateSimulatedOrder,
                                onAcceptSimulatedOrder = viewModel::acceptSimulatedOrder,
                                onAcceptOrder = viewModel::acceptOrder,
                                onAdvanceActiveOrder = viewModel::advanceActiveOrder,
                                onCancelActiveOrder = viewModel::cancelActiveOrder,
                                onRefreshOrders = viewModel::refreshOrders,
                                onOpenOrderDetails = { order -> viewModel.inspectOrder(order) },
                                onOpenBikeGarage = { viewModel.selectTab(CourierTab.BIKE) }
                            )

                            CourierTab.BIKE -> BikeGarageScreen(
                                state = state,
                                onToggleGarageMusic = viewModel::toggleGarageMusic,
                                onSelectGarageTrack = viewModel::selectGarageTrack,
                                onNextGarageTrack = viewModel::nextGarageTrack,
                                onPreviousGarageTrack = viewModel::previousGarageTrack,
                                onToggleGarageAutoMix = viewModel::toggleGarageAutoMix,
                                onRepairBreakdown = viewModel::repairBreakdown,
                                onRepairAllBreakdowns = viewModel::repairAllBreakdowns,
                                onBuyOrToggleMod = viewModel::buyOrToggleBikeMod,
                                onTriggerDiagnosticCheck = viewModel::triggerDiagnosticBreakdownTest
                            )

                            CourierTab.SHOP -> BrakeShopScreen(
                                state = state,
                                onBuyOrEquipPad = viewModel::buyOrEquipBrakePad,
                                onTestBrakes = viewModel::testBrakes
                            )

                            CourierTab.CHAT -> CourierChatScreen(
                                selectedSection = state.selectedChatSection,
                                messages = state.chatMessages,
                                typingCourierName = state.typingCourierName,
                                courierRating = state.courierRating,
                                directThreads = state.directThreads,
                                selectedDmThreadId = state.selectedDmThreadId,
                                unreadDmCount = state.unreadDmCount,
                                callHistory = state.callHistory,
                                hapticsEnabled = state.hapticsEnabled,
                                onSelectSection = viewModel::selectChatSection,
                                onOpenDmThread = viewModel::openDmThread,
                                onSendMessage = viewModel::sendChatMessage,
                                onSendDmMessage = viewModel::sendDmMessage,
                                onTriggerComplaintCall = { viewModel.triggerIncomingComplaintCall() }
                            )

                            CourierTab.STUDIO -> MemeStudioScreen(
                                studioState = state.memeStudio,
                                onSubtitleChange = viewModel::updateStudioSubtitle,
                                onReasonChange = viewModel::updateStudioReason,
                                onAmountChange = viewModel::updateStudioAmount,
                                onPositionChange = viewModel::updateStudioPosition,
                                onBadgeToggle = viewModel::updateStudioBadge,
                                onScaleChange = viewModel::updateStudioScale,
                                onPhotoSelected = viewModel::setStudioPhotoUri,
                                onResetPreset = viewModel::resetStudioPreset,
                                onTestBanner = viewModel::showPreviewBanner
                            )
                        }
                    }
                }
            }

            // Always-Visible Floating 5-Speed Gearbox Lever Widget (Toggleable in Settings)
            AnimatedVisibility(
                visible = state.showGearboxWidget,
                enter = fadeIn() + scaleIn(initialScale = 0.85f),
                exit = fadeOut() + scaleOut(targetScale = 0.85f),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .navigationBarsPadding()
                    .padding(
                        end = 12.dp,
                        bottom = if (useRail) 18.dp else 90.dp
                    )
            ) {
                GearboxFloatingWidget(
                    currentGear = state.currentGear,
                    hapticsEnabled = state.hapticsEnabled,
                    highGearsBlocked = state.highGearsBlocked,
                    onSelectGear = viewModel::setGear
                )
            }

            // Top Stacked Floating Notifications (Incoming Transfer, Angry DM, Bike Breakdown)
            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AnimatedVisibility(
                    visible = state.activeBanner != null,
                    enter = slideInVertically(
                        initialOffsetY = { -it - 40 },
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMediumLow
                        )
                    ) + fadeIn(),
                    exit = slideOutVertically(
                        targetOffsetY = { -it - 40 }
                    ) + fadeOut()
                ) {
                    state.activeBanner?.let { banner ->
                        IncomingTransferCard(
                            subtitle = banner.subtitle,
                            reason = banner.reason,
                            amountText = "+ ${banner.amountRub}₽",
                            onClick = viewModel::dismissBanner,
                            modifier = Modifier.testTag("incoming_transfer_banner")
                        )
                    }
                }

                AnimatedVisibility(
                    visible = state.activeDmPopup != null,
                    enter = slideInVertically(
                        initialOffsetY = { -it - 40 },
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMediumLow
                        )
                    ) + fadeIn(),
                    exit = slideOutVertically(
                        targetOffsetY = { -it - 40 }
                    ) + fadeOut()
                ) {
                    state.activeDmPopup?.let { popup ->
                        IncomingDmBanner(
                            popup = popup,
                            onOpenThread = { viewModel.openDmFromPopup(popup) },
                            onDismiss = viewModel::dismissDmPopup
                        )
                    }
                }

                AnimatedVisibility(
                    visible = state.activeBreakdown != null,
                    enter = slideInVertically(
                        initialOffsetY = { -it - 40 },
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMediumLow
                        )
                    ) + fadeIn(),
                    exit = slideOutVertically(
                        targetOffsetY = { -it - 40 }
                    ) + fadeOut()
                ) {
                    state.activeBreakdown?.let { alert ->
                        BikeBreakdownBanner(
                            alert = alert,
                            accumulatedCount = state.accumulatedBreakdowns.size,
                            walletBalanceRub = state.walletBalanceRub,
                            onRepairNow = { viewModel.dismissBreakdown(repairNow = true) },
                            onDismiss = { viewModel.dismissBreakdown(repairNow = false) }
                        )
                    }
                }
            }

            // Full Order Details Modal Bottom Sheet
            state.inspectedOrder?.let { order ->
                val isCurrentActive = state.activeOrder?.id == order.id
                OrderDetailsBottomSheet(
                    order = order,
                    isActiveOrder = isCurrentActive,
                    hasOtherActiveOrder = state.activeOrder != null && !isCurrentActive,
                    onDismiss = { viewModel.inspectOrder(null) },
                    onPrimaryAction = viewModel::performInspectedOrderAction
                )
            }

            // Settings Modal Bottom Sheet
            if (state.isSettingsOpen) {
                SettingsBottomSheet(
                    showGearboxWidget = state.showGearboxWidget,
                    isDarkTheme = state.isDarkTheme,
                    hapticsEnabled = state.hapticsEnabled,
                    soundsEnabled = state.soundsEnabled,
                    garageMusicEnabled = state.garageMusicEnabled,
                    onToggleGearboxWidget = viewModel::setShowGearboxWidget,
                    onToggleDarkTheme = viewModel::toggleDarkTheme,
                    onToggleHaptics = viewModel::toggleHaptics,
                    onToggleSounds = viewModel::setSoundsEnabled,
                    onToggleGarageMusic = viewModel::toggleGarageMusic,
                    onDismiss = { viewModel.setSettingsOpen(false) }
                )
            }

            // Full-Screen Incoming & Active Client Complaint Call Overlay
            AnimatedVisibility(
                visible = state.activeCall != null,
                enter = fadeIn() + slideInVertically(initialOffsetY = { it / 4 }),
                exit = fadeOut() + slideOutVertically(targetOffsetY = { it / 4 })
            ) {
                state.activeCall?.let { call ->
                    IncomingCallOverlay(
                        call = call,
                        hapticsEnabled = state.hapticsEnabled,
                        onAnswer = viewModel::answerIncomingCall,
                        onDecline = viewModel::declineIncomingCall,
                        onSelectResponse = viewModel::selectCallResponse,
                        onHangUp = viewModel::hangUpCall
                    )
                }
            }
        }
    }
}
