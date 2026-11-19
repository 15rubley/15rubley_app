package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.BikeBreakdownAlert
import com.example.data.BikeModItem
import com.example.data.BrakePadItem
import com.example.data.CallDialogueLine
import com.example.data.CallHistoryEntry
import com.example.data.CallResponseOption
import com.example.data.CallStatus
import com.example.data.CallStoryEngine
import com.example.data.ChatMessage
import com.example.data.ChatSection
import com.example.data.CourierPreferencesRepository
import com.example.data.CourierTab
import com.example.data.CourierUiState
import com.example.data.DeliveryOrder
import com.example.data.DirectMessage
import com.example.data.DirectMessagePopup
import com.example.data.GarageTrack
import com.example.data.InfiniteGigGenerator
import com.example.data.MemeStudioState
import com.example.data.OrderCategory
import com.example.data.OrderFilter
import com.example.data.OrderStage
import com.example.data.OrderSurgeMode
import com.example.data.OverlayPosition
import com.example.data.TransferEvent
import com.example.util.CourierSoundEngine
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt
import kotlin.random.Random

class CourierViewModel(application: Application) : AndroidViewModel(application) {

    private val prefsRepo = CourierPreferencesRepository(application.applicationContext)
    private val soundEngine = CourierSoundEngine(application.applicationContext)

    private var orderSeq = 100
    private var nextEventId = 2L
    private var nextReviewId = 10L
    private var nextChatId = 10L
    private var nextDmMsgId = 2000L
    private var nextBreakdownId = 1L
    private var nextDmPopupId = 1L
    private var nextCallId = 10L
    private var currentAbsurdity = 1
    private var lastBreakdownAtMs = 0L

    private val _uiState = MutableStateFlow(
        CourierUiState(
            simulatedOffer = nextRandomOrder(OrderSurgeMode.NORMAL, OrderFilter.ALL),
            availableOrders = nextOrderBatch(OrderSurgeMode.NORMAL, OrderFilter.ALL),
            clientReviews = InfiniteGigGenerator.initialReviews(currentTimeLabel()),
            chatMessages = InfiniteGigGenerator.initialChatMessages(currentTimeLabel()),
            directThreads = InfiniteGigGenerator.initialDirectThreads(currentTimeLabel()),
            callHistory = InfiniteGigGenerator.initialCallHistory(currentTimeLabel()),
            transferHistory = listOf(
                TransferEvent(
                    id = 1L,
                    subtitle = "Входящий перевод",
                    reason = "На лечение",
                    amountRub = 15,
                    timestampLabel = currentTimeLabel()
                )
            )
        )
    )
    val uiState: StateFlow<CourierUiState> = _uiState.asStateFlow()

    private var bannerDismissJob: Job? = null
    private var chatReplyJob: Job? = null
    private var dmReplyJob: Job? = null
    private var dmPopupDismissJob: Job? = null
    private var breakdownDismissJob: Job? = null
    private var callTickerJob: Job? = null
    private var callRingTimeoutJob: Job? = null
    private var callReplyJob: Job? = null
    private var callAutoCloseJob: Job? = null

    private var shiftAccumulatedMs = 0L
    private var shiftActiveSinceMs = -1L
    private var shiftFirstStartedMs = -1L

    init {
        soundEngine.onTrackAutoAdvanced = { nextTrack ->
            viewModelScope.launch {
                _uiState.update { it.copy(currentGarageTrack = nextTrack) }
                prefsRepo.setGarageTrackId(nextTrack.id)
            }
        }

        viewModelScope.launch {
            val initial = prefsRepo.preferences.first()
            val now = System.currentTimeMillis()
            shiftAccumulatedMs = initial.shiftAccumulatedMs.coerceAtLeast(0L)
            shiftActiveSinceMs = if (initial.isOnShift) {
                if (initial.shiftActiveSinceMs > 0L) initial.shiftActiveSinceMs else now
            } else {
                -1L
            }
            shiftFirstStartedMs = when {
                initial.shiftFirstStartedMs > 0L -> initial.shiftFirstStartedMs
                initial.isOnShift -> shiftActiveSinceMs
                else -> -1L
            }

            if (initial.isOnShift && (initial.shiftActiveSinceMs <= 0L || initial.shiftFirstStartedMs <= 0L)) {
                prefsRepo.saveShiftTimerState(
                    isOnShift = true,
                    accumulatedMs = shiftAccumulatedMs,
                    activeSinceMs = shiftActiveSinceMs,
                    firstStartedMs = shiftFirstStartedMs
                )
            }

            syncTimerState(isOnShift = initial.isOnShift, nowMs = now)

            prefsRepo.preferences.collect { prefs ->
                soundEngine.isEnabled = prefs.soundsEnabled
                val selectedTrack = GarageTrack.fromId(prefs.garageTrackId)
                soundEngine.autoMixEnabled = prefs.garageAutoMix
                soundEngine.setGarageTrack(selectedTrack)
                val restoredBreakdowns = InfiniteGigGenerator.deserializeBreakdowns(prefs.accumulatedBreakdownsRaw)
                val maxId = restoredBreakdowns.maxOfOrNull { it.id } ?: 0L
                if (maxId >= nextBreakdownId) {
                    nextBreakdownId = maxId + 1L
                }
                _uiState.update { state ->
                    state.copy(
                        isDarkTheme = prefs.isDarkTheme,
                        hapticsEnabled = prefs.hapticsEnabled,
                        soundsEnabled = prefs.soundsEnabled,
                        garageMusicEnabled = prefs.garageMusicEnabled,
                        currentGarageTrack = selectedTrack,
                        garageAutoMix = prefs.garageAutoMix,
                        showGearboxWidget = prefs.showGearboxWidget,
                        treatmentJarRub = prefs.lifetimeTreatmentRub,
                        spentRub = prefs.spentRub,
                        purchasedPadIds = prefs.purchasedPadIds,
                        equippedPadId = prefs.equippedPadId,
                        brakeHealthPercent = prefs.brakeHealthPercent,
                        ownedBikeModIds = prefs.ownedBikeModIds,
                        equippedBikeModIds = prefs.equippedBikeModIds,
                        accumulatedBreakdowns = restoredBreakdowns,
                        deliveriesCompleted = prefs.lifetimeDeliveries
                    )
                }
                syncGarageMusic()
            }
        }

        // Real-time 250ms timer sync loop
        viewModelScope.launch {
            while (true) {
                delay(250L)
                if (_uiState.value.isOnShift && shiftActiveSinceMs > 0L) {
                    syncTimerState(isOnShift = true, nowMs = System.currentTimeMillis())
                }
            }
        }

        // Ambient courier chat chatter every ~28 seconds
        viewModelScope.launch {
            while (true) {
                delay(28000L)
                val ambient = InfiniteGigGenerator.generateAmbientChat(
                    timeLabel = currentTimeLabel(),
                    nextId = nextChatId++
                )
                _uiState.update { state ->
                    val onPublicChat = state.selectedTab == CourierTab.CHAT &&
                        state.selectedChatSection == ChatSection.PUBLIC
                    val unread = if (onPublicChat) 0 else state.unreadChatCount + 1
                    state.copy(
                        chatMessages = (state.chatMessages + ambient).takeLast(60),
                        unreadChatCount = unread
                    )
                }
            }
        }

        // Occasional incoming angry Direct Messages (faster if bike has unfixed breakdowns!)
        viewModelScope.launch {
            delay(18000L)
            while (true) {
                if (_uiState.value.isOnShift) {
                    triggerIncomingAngryDm()
                }
                val hasIssues = _uiState.value.accumulatedBreakdowns.isNotEmpty()
                val nextDelay = if (hasIssues) {
                    Random.nextLong(22000L, 34000L)
                } else {
                    Random.nextLong(38000L, 52000L)
                }
                delay(nextDelay)
            }
        }

        // Infrequent bike breakdown alerts (~55-75s while on shift, respecting cooldown & durability mods)
        viewModelScope.launch {
            delay(30000L)
            while (true) {
                if (_uiState.value.isOnShift) {
                    val shield = _uiState.value.totalDurabilityShieldPercent
                    if (Random.nextInt(100) >= shield) {
                        maybeTriggerBikeBreakdown(forceFromWear = false)
                    }
                }
                delay(Random.nextLong(55000L, 75000L))
            }
        }

        // Periodic incoming client complaint calls (~44-68s while on shift)
        viewModelScope.launch {
            delay(26000L)
            while (true) {
                if (_uiState.value.isOnShift && _uiState.value.activeCall == null) {
                    triggerIncomingComplaintCall()
                }
                val hasIssues = _uiState.value.accumulatedBreakdowns.isNotEmpty()
                delay(if (hasIssues) Random.nextLong(38000L, 52000L) else Random.nextLong(50000L, 72000L))
            }
        }
    }

    override fun onCleared() {
        soundEngine.release()
        super.onCleared()
    }

    private fun syncTimerState(isOnShift: Boolean, nowMs: Long) {
        val activeDelta = if (isOnShift && shiftActiveSinceMs > 0L) {
            (nowMs - shiftActiveSinceMs).coerceAtLeast(0L)
        } else {
            0L
        }
        val totalSeconds = ((shiftAccumulatedMs + activeDelta) / 1000L).coerceAtLeast(0L)
        val startLabel = if (shiftFirstStartedMs > 0L) {
            formatTimeLabel(shiftFirstStartedMs)
        } else {
            null
        }
        _uiState.update {
            it.copy(
                isOnShift = isOnShift,
                shiftSeconds = totalSeconds,
                shiftStartTimeLabel = startLabel
            )
        }
    }

    fun selectTab(tab: CourierTab) {
        _uiState.update { state ->
            val clearPublicUnread = tab == CourierTab.CHAT && state.selectedChatSection == ChatSection.PUBLIC
            state.copy(
                selectedTab = tab,
                unreadChatCount = if (clearPublicUnread) 0 else state.unreadChatCount
            )
        }
        syncGarageMusic()
    }

    fun toggleShift() {
        val now = System.currentTimeMillis()
        val currentlyOnShift = _uiState.value.isOnShift
        if (currentlyOnShift) {
            if (shiftActiveSinceMs > 0L) {
                shiftAccumulatedMs += (now - shiftActiveSinceMs).coerceAtLeast(0L)
            }
            shiftActiveSinceMs = -1L
            syncTimerState(isOnShift = false, nowMs = now)
            viewModelScope.launch {
                prefsRepo.saveShiftTimerState(
                    isOnShift = false,
                    accumulatedMs = shiftAccumulatedMs,
                    activeSinceMs = -1L,
                    firstStartedMs = shiftFirstStartedMs
                )
            }
        } else {
            shiftActiveSinceMs = now
            if (shiftFirstStartedMs <= 0L) {
                shiftFirstStartedMs = now
            }
            syncTimerState(isOnShift = true, nowMs = now)
            viewModelScope.launch {
                prefsRepo.saveShiftTimerState(
                    isOnShift = true,
                    accumulatedMs = shiftAccumulatedMs,
                    activeSinceMs = shiftActiveSinceMs,
                    firstStartedMs = shiftFirstStartedMs
                )
            }
        }
    }

    fun resetShiftTimer() {
        val now = System.currentTimeMillis()
        shiftAccumulatedMs = 0L
        shiftActiveSinceMs = -1L
        shiftFirstStartedMs = -1L
        syncTimerState(isOnShift = false, nowMs = now)
        viewModelScope.launch {
            prefsRepo.saveShiftTimerState(
                isOnShift = false,
                accumulatedMs = 0L,
                activeSinceMs = -1L,
                firstStartedMs = -1L
            )
        }
    }

    fun toggleDarkTheme() {
        val next = !_uiState.value.isDarkTheme
        viewModelScope.launch {
            prefsRepo.setDarkTheme(next)
        }
    }

    fun toggleHaptics() {
        val next = !_uiState.value.hapticsEnabled
        viewModelScope.launch {
            prefsRepo.setHapticsEnabled(next)
        }
    }

    fun setSoundsEnabled(enabled: Boolean) {
        soundEngine.isEnabled = enabled
        if (!enabled) {
            soundEngine.stopIncomingRingtone()
        }
        viewModelScope.launch {
            prefsRepo.setSoundsEnabled(enabled)
        }
        syncGarageMusic()
    }

    fun toggleGarageMusic() {
        val next = !_uiState.value.garageMusicEnabled
        _uiState.update { it.copy(garageMusicEnabled = next) }
        viewModelScope.launch {
            prefsRepo.setGarageMusicEnabled(next)
        }
        syncGarageMusic()
    }

    fun selectGarageTrack(track: GarageTrack) {
        _uiState.update { it.copy(currentGarageTrack = track, garageMusicEnabled = true) }
        soundEngine.setGarageTrack(track)
        viewModelScope.launch {
            prefsRepo.setGarageTrackId(track.id)
            prefsRepo.setGarageMusicEnabled(true)
        }
        syncGarageMusic()
    }

    fun nextGarageTrack() {
        selectGarageTrack(_uiState.value.currentGarageTrack.next())
    }

    fun previousGarageTrack() {
        selectGarageTrack(_uiState.value.currentGarageTrack.previous())
    }

    fun toggleGarageAutoMix() {
        val next = !_uiState.value.garageAutoMix
        soundEngine.autoMixEnabled = next
        _uiState.update { it.copy(garageAutoMix = next) }
        viewModelScope.launch {
            prefsRepo.setGarageAutoMix(next)
        }
    }

    private fun syncGarageMusic() {
        val state = _uiState.value
        val shouldPlay = state.selectedTab == CourierTab.BIKE &&
            state.soundsEnabled &&
            state.garageMusicEnabled &&
            state.activeCall == null
        if (shouldPlay) {
            soundEngine.startGarageMusic()
        } else {
            soundEngine.stopGarageMusic()
        }
        if (state.isGarageMusicPlaying != shouldPlay) {
            _uiState.update { it.copy(isGarageMusicPlaying = shouldPlay) }
        }
    }

    fun triggerIncomingComplaintCall(orderTitle: String? = null) {
        if (_uiState.value.activeCall != null) return
        val state = _uiState.value
        val call = InfiniteGigGenerator.generateComplaintCall(
            id = nextCallId++,
            orderTitle = orderTitle ?: state.activeOrder?.restaurant,
            accumulatedBreakdowns = state.accumulatedBreakdowns,
            currentGear = state.currentGear
        )
        callAutoCloseJob?.cancel()
        callReplyJob?.cancel()
        callTickerJob?.cancel()
        _uiState.update { it.copy(activeCall = call) }
        syncGarageMusic()
        soundEngine.startIncomingRingtone()

        callRingTimeoutJob?.cancel()
        callRingTimeoutJob = viewModelScope.launch {
            delay(18000L)
            val current = _uiState.value.activeCall
            if (current != null && current.id == call.id && current.status == CallStatus.RINGING) {
                finishCallWithOutcome(
                    outcomeLabel = "Пропущен • -0.3★",
                    ratingDelta = -0.3,
                    penaltyReason = "Пропустили звонок клиента ${current.callerName}: -0.3★",
                    wasAnswered = false
                )
            }
        }
    }

    fun answerIncomingCall() {
        val current = _uiState.value.activeCall ?: return
        if (current.status != CallStatus.RINGING) return
        callRingTimeoutJob?.cancel()
        val updated = current.copy(status = CallStatus.ACTIVE, durationSeconds = 0)
        _uiState.update { it.copy(activeCall = updated) }
        soundEngine.playCallAnswered(current.openingComplaint)

        callTickerJob?.cancel()
        callTickerJob = viewModelScope.launch {
            while (true) {
                delay(1000L)
                val active = _uiState.value.activeCall
                if (active == null || active.status != CallStatus.ACTIVE) break
                _uiState.update { state ->
                    state.activeCall?.let { c ->
                        state.copy(activeCall = c.copy(durationSeconds = c.durationSeconds + 1))
                    } ?: state
                }
            }
        }
    }

    fun declineIncomingCall() {
        val current = _uiState.value.activeCall ?: return
        callRingTimeoutJob?.cancel()
        callReplyJob?.cancel()
        soundEngine.playCallHangUp()
        finishCallWithOutcome(
            outcomeLabel = "Сброшен • -0.4★",
            ratingDelta = -0.4,
            penaltyReason = "Сбросили звонок клиента ${current.callerName}! За дерзость -0.4★",
            wasAnswered = false
        )
    }

    fun selectCallResponse(option: CallResponseOption) {
        val current = _uiState.value.activeCall ?: return
        if (current.status != CallStatus.ACTIVE || current.isClientReacting) return

        val courierText = option.spokenReply.ifBlank { option.label }
        val nextLineId = (current.dialogueHistory.maxOfOrNull { it.id } ?: 0L) + 1L
        val interimHistory = current.dialogueHistory + CallDialogueLine(
            id = nextLineId,
            speakerName = "Вы",
            text = courierText,
            isFromCourier = true,
            angerDelta = option.angerDelta
        )

        val deltaText = "${if (option.ratingDelta >= 0) "+" else ""}${String.format(Locale.US, "%.1f", option.ratingDelta)}★"
        _uiState.update { state ->
            val nextRating = clampCursedRating(state.courierRating + option.ratingDelta)
            state.copy(
                activeCall = current.copy(
                    courierLine = courierText,
                    dialogueHistory = interimHistory,
                    isClientReacting = true
                ),
                courierRating = nextRating,
                lastRatingDelta = option.ratingDelta,
                ratingPenaltyReason = "Реплика клиенту «${current.callerName}»: $deltaText"
            )
        }

        if (option.tipRub > 0) {
            triggerIncomingTransfer(
                subtitle = "Жалобный бонус",
                reason = "На лечение нервов от ${current.callerName}",
                amountRub = option.tipRub
            )
        }

        callReplyJob?.cancel()
        callReplyJob = viewModelScope.launch {
            delay(650L)
            val latest = _uiState.value.activeCall ?: return@launch
            if (latest.id != current.id || latest.status != CallStatus.ACTIVE) return@launch

            val stateSnapshot = _uiState.value
            val resolution = CallStoryEngine.advanceCallTurn(
                call = current,
                chosenOption = option,
                accumulatedBreakdowns = stateSnapshot.accumulatedBreakdowns,
                currentGear = stateSnapshot.currentGear
            )

            val resolvedCall = resolution.updatedCall.copy(
                durationSeconds = latest.durationSeconds
            )

            if (!resolution.didClientSnap) {
                _uiState.update { state ->
                    state.copy(activeCall = resolvedCall)
                }
                soundEngine.playClientFollowUpLine(
                    lineText = resolution.clientSpokenLine,
                    angerPercent = resolvedCall.clientAngerPercent
                )
            } else {
                callTickerJob?.cancel()
                soundEngine.playClientRageHangUp(resolution.clientSpokenLine)

                val totalRating = resolvedCall.accumulatedRatingDelta
                val totalDeltaText = "${if (totalRating >= 0) "+" else ""}${String.format(Locale.US, "%.1f", totalRating)}★"
                val tipSuffix = if (resolvedCall.accumulatedTipRub > 0) " • +${resolvedCall.accumulatedTipRub}₽" else ""

                _uiState.update { state ->
                    val nextRating = clampCursedRating(state.courierRating + resolution.snapRatingPenalty)
                    val entry = CallHistoryEntry(
                        id = current.id,
                        callerName = current.callerName,
                        callerRole = current.callerRole,
                        summary = "${current.openingComplaint} (реплик: ${resolvedCall.turnNumber})",
                        outcomeLabel = "Клиент бросил трубку • $totalDeltaText$tipSuffix",
                        timestampLabel = currentTimeLabel(),
                        wasAnswered = true
                    )
                    state.copy(
                        activeCall = resolvedCall,
                        callHistory = (listOf(entry) + state.callHistory).take(20),
                        courierRating = nextRating,
                        lastRatingDelta = totalRating,
                        ratingPenaltyReason = "${current.callerName} выбесился и бросил трубку ($totalDeltaText)"
                    )
                }

                callAutoCloseJob?.cancel()
                callAutoCloseJob = viewModelScope.launch {
                    delay(3600L)
                    if (_uiState.value.activeCall?.id == current.id) {
                        _uiState.update { it.copy(activeCall = null) }
                        syncGarageMusic()
                    }
                }
            }
        }
    }

    fun hangUpCall() {
        val current = _uiState.value.activeCall ?: return
        callRingTimeoutJob?.cancel()
        callReplyJob?.cancel()
        callTickerJob?.cancel()
        callAutoCloseJob?.cancel()

        if (current.status == CallStatus.ENDED) {
            _uiState.update { it.copy(activeCall = null) }
            syncGarageMusic()
            return
        }

        soundEngine.playCallHangUp()
        val totalDelta = current.accumulatedRatingDelta - 0.2
        val deltaStr = String.format(Locale.US, "%.1f", totalDelta)
        finishCallWithOutcome(
            outcomeLabel = "Бросили трубку • $deltaStr★",
            ratingDelta = -0.2,
            penaltyReason = "Положили трубку, не дослушав ${current.callerName}: -0.2★",
            wasAnswered = current.status == CallStatus.ACTIVE
        )
    }

    private fun finishCallWithOutcome(
        outcomeLabel: String,
        ratingDelta: Double,
        penaltyReason: String,
        wasAnswered: Boolean
    ) {
        val current = _uiState.value.activeCall ?: return
        soundEngine.stopIncomingRingtone()
        _uiState.update { state ->
            val nextRating = clampCursedRating(state.courierRating + ratingDelta)
            val entry = CallHistoryEntry(
                id = current.id,
                callerName = current.callerName,
                callerRole = current.callerRole,
                summary = current.openingComplaint,
                outcomeLabel = outcomeLabel,
                timestampLabel = currentTimeLabel(),
                wasAnswered = wasAnswered
            )
            state.copy(
                activeCall = null,
                callHistory = (listOf(entry) + state.callHistory).take(20),
                courierRating = nextRating,
                lastRatingDelta = ratingDelta,
                ratingPenaltyReason = penaltyReason
            )
        }
        syncGarageMusic()
    }

    fun setShowGearboxWidget(enabled: Boolean) {
        viewModelScope.launch {
            prefsRepo.setShowGearboxWidget(enabled)
        }
    }

    fun setGear(gear: Int) {
        val clamped = gear.coerceIn(1, 5)
        val state = _uiState.value
        if (clamped >= 4 && state.highGearsBlocked) {
            soundEngine.playGearJam()
            _uiState.update {
                it.copy(
                    currentGear = 3,
                    ratingPenaltyReason = "4–5 передача клинит! Почините велосипед во вкладке «Велик»"
                )
            }
            return
        }

        val prevGear = state.currentGear
        _uiState.update { it.copy(currentGear = clamped) }
        soundEngine.playGearShift(clamped)

        // Downshifting sharply from 5th or riding fast with worn brakes triggers brake screech & slight wear
        val currentHealth = state.brakeHealthPercent
        if ((prevGear >= 4 && clamped <= 2) || (clamped == 5 && currentHealth < 45)) {
            soundEngine.playBrakeSqueak(currentHealth)
            val newHealth = (currentHealth - 2).coerceAtLeast(5)
            viewModelScope.launch {
                prefsRepo.setBrakeHealth(newHealth)
            }
        }
    }

    fun testBrakes() {
        val currentHealth = _uiState.value.brakeHealthPercent
        soundEngine.playBrakeSqueak(currentHealth)
        val nextHealth = (currentHealth - 3).coerceAtLeast(5)
        viewModelScope.launch {
            prefsRepo.setBrakeHealth(nextHealth)
        }
        if (nextHealth <= 18 && Random.nextInt(100) < 35) {
            maybeTriggerBikeBreakdown(forceFromWear = true)
        }
    }

    fun setSettingsOpen(open: Boolean) {
        _uiState.update { it.copy(isSettingsOpen = open) }
    }

    fun inspectOrder(order: DeliveryOrder?) {
        _uiState.update { it.copy(inspectedOrder = order) }
    }

    fun performInspectedOrderAction() {
        val target = _uiState.value.inspectedOrder ?: return
        val active = _uiState.value.activeOrder
        val simOffer = _uiState.value.simulatedOffer

        when {
            active != null && active.id == target.id -> {
                advanceActiveOrder()
            }
            simOffer != null && simOffer.id == target.id -> {
                acceptSimulatedOrder()
            }
            active == null -> {
                acceptOrder(target.id)
            }
            else -> {
                // Keep in queue if another order is already active
            }
        }
        _uiState.update { it.copy(inspectedOrder = null) }
    }

    fun buyOrEquipBrakePad(pad: BrakePadItem) {
        val state = _uiState.value
        val alreadyOwned = state.purchasedPadIds.contains(pad.id)
        if (alreadyOwned) {
            soundEngine.playOrderAccepted()
            viewModelScope.launch {
                prefsRepo.equipPad(pad.id, pad.durabilityBoost)
            }
            return
        }

        if (state.walletBalanceRub < pad.priceRub) return

        soundEngine.playOrderAccepted()
        val purchaseEvent = TransferEvent(
            id = nextEventId++,
            subtitle = "ТормозМаркет",
            reason = "Покупка: ${pad.name}",
            amountRub = pad.priceRub,
            timestampLabel = currentTimeLabel(),
            isOrderPayout = true
        )

        _uiState.update { current ->
            current.copy(
                transferHistory = (listOf(purchaseEvent) + current.transferHistory).take(25)
            )
        }

        viewModelScope.launch {
            prefsRepo.purchaseAndEquipPad(
                padId = pad.id,
                priceRub = pad.priceRub,
                newHealth = pad.durabilityBoost
            )
        }
    }

    fun attemptBoostRating() {
        val excuses = listOf(
            "Обнаружена попытка поднять рейтинг вручную: штраф -0.3★",
            "Вы нажали на кнопку слишком уверенно: -0.2★ за дерзость",
            "Алгоритм решил, что 1★ вам идёт больше: -0.4★",
            "Жалоба от неизвестного прохожего на цвет кроссовок: -0.2★"
        )
        val drop = -Random.nextDouble(0.2, 0.5)
        val roundedDrop = (drop * 10.0).roundToInt() / 10.0
        _uiState.update { state ->
            val nextRating = clampCursedRating(state.courierRating + roundedDrop)
            state.copy(
                courierRating = nextRating,
                lastRatingDelta = roundedDrop,
                ratingPenaltyReason = excuses[Random.nextInt(excuses.size)]
            )
        }
    }

    fun triggerIncomingTransfer(
        subtitle: String = "Входящий перевод",
        reason: String = "На лечение",
        amountRub: Int = 15,
        isOrderPayout: Boolean = false
    ) {
        val event = TransferEvent(
            id = nextEventId++,
            subtitle = subtitle.ifBlank { "Входящий перевод" },
            reason = reason.ifBlank { "На лечение" },
            amountRub = amountRub.coerceAtLeast(1),
            timestampLabel = currentTimeLabel(),
            isOrderPayout = isOrderPayout
        )

        _uiState.update { state ->
            state.copy(
                shiftEarnedRub = state.shiftEarnedRub + event.amountRub,
                transferHistory = (listOf(event) + state.transferHistory).take(25),
                activeBanner = event
            )
        }
        soundEngine.playIncomingTransfer()

        viewModelScope.launch {
            prefsRepo.addTreatmentRub(event.amountRub)
        }

        scheduleBannerDismiss()
    }

    fun showPreviewBanner(subtitle: String, reason: String, amountText: String) {
        val parsedAmount = amountText.filter { it.isDigit() }.toIntOrNull() ?: 15
        val event = TransferEvent(
            id = nextEventId++,
            subtitle = subtitle.ifBlank { "Входящий перевод" },
            reason = reason.ifBlank { "На лечение" },
            amountRub = parsedAmount,
            timestampLabel = currentTimeLabel()
        )
        _uiState.update { it.copy(activeBanner = event) }
        scheduleBannerDismiss()
    }

    fun dismissBanner() {
        bannerDismissJob?.cancel()
        _uiState.update { it.copy(activeBanner = null) }
    }

    private fun scheduleBannerDismiss() {
        bannerDismissJob?.cancel()
        bannerDismissJob = viewModelScope.launch {
            delay(3600L)
            _uiState.update { it.copy(activeBanner = null) }
        }
    }

    fun setSurgeMode(mode: OrderSurgeMode) {
        val filter = _uiState.value.orderFilter
        _uiState.update { state ->
            state.copy(
                surgeMode = mode,
                simulatedOffer = nextRandomOrder(mode, filter)
            )
        }
    }

    fun setOrderFilter(filter: OrderFilter) {
        val mode = _uiState.value.surgeMode
        _uiState.update { state ->
            state.copy(
                orderFilter = filter,
                simulatedOffer = nextRandomOrder(mode, filter),
                availableOrders = nextOrderBatch(mode, filter)
            )
        }
    }

    fun generateSimulatedOrder() {
        val mode = _uiState.value.surgeMode
        val filter = _uiState.value.orderFilter
        currentAbsurdity += 1
        _uiState.update { state ->
            state.copy(
                absurdityLevel = currentAbsurdity,
                simulatedOffer = nextRandomOrder(mode, filter)
            )
        }
    }

    fun acceptSimulatedOrder() {
        val offer = _uiState.value.simulatedOffer ?: return
        val mode = _uiState.value.surgeMode
        val filter = _uiState.value.orderFilter
        currentAbsurdity += 1
        soundEngine.playOrderAccepted()
        if (_uiState.value.activeOrder == null) {
            if (!_uiState.value.isOnShift) {
                toggleShift()
            }
            _uiState.update { state ->
                state.copy(
                    absurdityLevel = currentAbsurdity,
                    activeOrder = offer.copy(stage = OrderStage.PICKING_UP),
                    simulatedOffer = nextRandomOrder(mode, filter)
                )
            }
        } else {
            _uiState.update { state ->
                state.copy(
                    absurdityLevel = currentAbsurdity,
                    availableOrders = listOf(offer.copy(stage = OrderStage.AVAILABLE)) + state.availableOrders,
                    simulatedOffer = nextRandomOrder(mode, filter)
                )
            }
        }
    }

    fun acceptOrder(orderId: String) {
        if (_uiState.value.activeOrder != null) return
        val order = _uiState.value.availableOrders.find { it.id == orderId } ?: return
        soundEngine.playOrderAccepted()
        if (!_uiState.value.isOnShift) {
            toggleShift()
        }
        _uiState.update { state ->
            state.copy(
                activeOrder = order.copy(stage = OrderStage.PICKING_UP),
                availableOrders = state.availableOrders.filterNot { it.id == orderId }
            )
        }
    }

    fun advanceActiveOrder() {
        val current = _uiState.value.activeOrder ?: return
        when (current.stage) {
            OrderStage.PICKING_UP -> {
                _uiState.update {
                    it.copy(activeOrder = current.copy(stage = OrderStage.DELIVERING))
                }
            }
            OrderStage.DELIVERING -> {
                val currentState = _uiState.value
                val adjustedPay = (current.basePayRub * currentState.effectivePayMultiplier)
                    .roundToInt()
                    .coerceAtLeast(35)

                val bonusReasons = if (current.category == OrderCategory.SIDE_GIG) {
                    listOf(
                        "На лечение" to 15,
                        "На мазь для спины" to 15,
                        "За проверку модели" to 50,
                        "За спасённый шкаф" to 100,
                        "На лечение" to 15
                    )
                } else {
                    listOf(
                        "На лечение" to 15,
                        "На лечение" to 15,
                        "За быстрый вел" to 30,
                        "На чай и грелку" to 50,
                        "На лечение" to 15
                    )
                }
                val (tipReason, tipAmount) = bonusReasons[Random.nextInt(bonusReasons.size)]
                val payoutEvent = TransferEvent(
                    id = nextEventId++,
                    subtitle = if (currentState.accumulatedBreakdowns.isNotEmpty()) {
                        "Выплата (-${currentState.totalPayPenaltyPercent}% за поломки)"
                    } else if (current.category == OrderCategory.SIDE_GIG) {
                        "Оплата халтурки"
                    } else {
                        "Выплата за заказ"
                    },
                    reason = current.restaurant,
                    amountRub = adjustedPay,
                    timestampLabel = currentTimeLabel(),
                    isOrderPayout = true
                )

                val (review, penaltyReason) = InfiniteGigGenerator.generateCursedReview(
                    id = nextReviewId++,
                    orderTitle = current.restaurant,
                    currentRating = currentState.courierRating,
                    timestampLabel = currentTimeLabel(),
                    accumulatedBreakdowns = currentState.accumulatedBreakdowns
                )

                currentAbsurdity += 1

                _uiState.update { state ->
                    val refreshedQueue = if (state.availableOrders.size <= 1) {
                        state.availableOrders + nextOrderBatch(state.surgeMode, state.orderFilter)
                    } else {
                        state.availableOrders
                    }
                    val nextRating = clampCursedRating(state.courierRating + review.ratingDelta)
                    state.copy(
                        absurdityLevel = currentAbsurdity,
                        activeOrder = null,
                        availableOrders = refreshedQueue,
                        shiftEarnedRub = state.shiftEarnedRub + adjustedPay,
                        transferHistory = (listOf(payoutEvent) + state.transferHistory).take(25),
                        courierRating = nextRating,
                        lastRatingDelta = review.ratingDelta,
                        ratingPenaltyReason = penaltyReason,
                        clientReviews = (listOf(review) + state.clientReviews).take(20)
                    )
                }

                val currentHealth = currentState.brakeHealthPercent
                soundEngine.playBrakeSqueak(currentHealth)

                viewModelScope.launch {
                    prefsRepo.addTreatmentRub(adjustedPay)
                    prefsRepo.incrementDeliveries()
                    val baseWear = if (currentState.currentGear >= 4) 9 else 5
                    val extraIssueWear = currentState.accumulatedBreakdowns.size * 2
                    val shieldFactor = (100 - currentState.totalDurabilityShieldPercent) / 100.0
                    val wearCost = ((baseWear + extraIssueWear) * shieldFactor).roundToInt().coerceAtLeast(2)
                    val nextHealth = (currentHealth - wearCost).coerceAtLeast(5)
                    prefsRepo.setBrakeHealth(nextHealth)
                    delay(320L)
                    triggerIncomingTransfer(
                        subtitle = "Входящий перевод",
                        reason = tipReason,
                        amountRub = tipAmount
                    )
                    if (nextHealth <= 25 && Random.nextInt(100) < 45) {
                        delay(1200L)
                        maybeTriggerBikeBreakdown(forceFromWear = true)
                    } else if (_uiState.value.activeCall == null && Random.nextInt(100) < 35) {
                        delay(2600L)
                        triggerIncomingComplaintCall(orderTitle = current.restaurant)
                    }
                }
            }
            else -> Unit
        }
    }

    fun cancelActiveOrder() {
        val current = _uiState.value.activeOrder ?: return
        _uiState.update { state ->
            val nextRating = clampCursedRating(state.courierRating - 0.3)
            state.copy(
                activeOrder = null,
                availableOrders = listOf(current.copy(stage = OrderStage.AVAILABLE)) + state.availableOrders,
                courierRating = nextRating,
                lastRatingDelta = -0.3,
                ratingPenaltyReason = "Штраф -0.3★ за отмену заказа на полпути"
            )
        }
    }

    fun refreshOrders() {
        currentAbsurdity += 1
        _uiState.update { state ->
            state.copy(
                absurdityLevel = currentAbsurdity,
                availableOrders = nextOrderBatch(state.surgeMode, state.orderFilter)
            )
        }
    }

    fun sendChatMessage(text: String) {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return

        val myMessage = ChatMessage(
            id = nextChatId++,
            senderName = "Вы",
            senderBadge = "${String.format("%.1f★", _uiState.value.courierRating)}",
            text = trimmed,
            timestampLabel = currentTimeLabel(),
            isFromMe = true
        )

        val peerReply = InfiniteGigGenerator.generatePeerReply(
            userText = trimmed,
            timeLabel = currentTimeLabel(),
            nextId = nextChatId++
        )

        _uiState.update { state ->
            state.copy(
                chatMessages = (state.chatMessages + myMessage).takeLast(60),
                typingCourierName = peerReply.senderName
            )
        }

        chatReplyJob?.cancel()
        chatReplyJob = viewModelScope.launch {
            delay(1100L)
            _uiState.update { state ->
                val onPublicChat = state.selectedTab == CourierTab.CHAT &&
                    state.selectedChatSection == ChatSection.PUBLIC
                val unread = if (onPublicChat) 0 else state.unreadChatCount + 1
                state.copy(
                    typingCourierName = null,
                    chatMessages = (state.chatMessages + peerReply).takeLast(60),
                    unreadChatCount = unread
                )
            }
        }
    }

    fun selectChatSection(section: ChatSection) {
        _uiState.update { state ->
            state.copy(
                selectedChatSection = section,
                unreadChatCount = if (section == ChatSection.PUBLIC) 0 else state.unreadChatCount
            )
        }
    }

    fun openDmThread(threadId: String?) {
        _uiState.update { state ->
            val updatedThreads = if (threadId != null) {
                state.directThreads.map { t ->
                    if (t.id == threadId) t.copy(unreadCount = 0) else t
                }
            } else {
                state.directThreads
            }
            state.copy(
                selectedChatSection = ChatSection.DIRECT,
                selectedDmThreadId = threadId,
                directThreads = updatedThreads
            )
        }
    }

    fun sendDmMessage(threadId: String, text: String) {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return

        val myMsg = DirectMessage(
            id = nextDmMsgId++,
            text = trimmed,
            timestampLabel = currentTimeLabel(),
            isFromMe = true
        )

        val snapshotState = _uiState.value
        val currentThread = snapshotState.directThreads.firstOrNull { it.id == threadId }
        val opponentMessages = currentThread?.messages?.filter { !it.isFromMe }.orEmpty()
        val lastOpponentText = opponentMessages.lastOrNull()?.text.orEmpty()
        val recentOpponentTexts = opponentMessages.map { it.text }.toSet()

        _uiState.update { state ->
            state.copy(
                directThreads = state.directThreads.map { thread ->
                    if (thread.id == threadId) {
                        thread.copy(
                            unreadCount = 0,
                            messages = (thread.messages + myMsg).takeLast(50)
                        )
                    } else {
                        thread
                    }
                }
            )
        }

        val replyMsg = InfiniteGigGenerator.generateAngryDmReply(
            threadId = threadId,
            userText = trimmed,
            nextMsgId = nextDmMsgId++,
            timeLabel = currentTimeLabel(),
            lastOpponentText = lastOpponentText,
            recentOpponentTexts = recentOpponentTexts,
            accumulatedBreakdowns = snapshotState.accumulatedBreakdowns,
            currentGear = snapshotState.currentGear,
            courierRating = snapshotState.courierRating
        )

        dmReplyJob?.cancel()
        dmReplyJob = viewModelScope.launch {
            delay(950L)
            var senderName = ""
            var senderRole = ""
            var showPopup = false

            _uiState.update { state ->
                val viewingThisThread = state.selectedTab == CourierTab.CHAT &&
                    state.selectedChatSection == ChatSection.DIRECT &&
                    state.selectedDmThreadId == threadId
                showPopup = !viewingThisThread
                val nextThreads = state.directThreads.map { thread ->
                    if (thread.id == threadId) {
                        senderName = thread.personName
                        senderRole = thread.personRole
                        thread.copy(
                            unreadCount = if (viewingThisThread) 0 else thread.unreadCount + 1,
                            messages = (thread.messages + replyMsg).takeLast(50)
                        )
                    } else {
                        thread
                    }
                }
                state.copy(
                    directThreads = nextThreads,
                    activeDmPopup = if (showPopup) {
                        DirectMessagePopup(
                            id = nextDmPopupId++,
                            threadId = threadId,
                            senderName = senderName,
                            senderRole = senderRole,
                            previewText = replyMsg.text
                        )
                    } else {
                        state.activeDmPopup
                    }
                )
            }
            soundEngine.playIncomingDm()
            if (showPopup) {
                scheduleDmPopupDismiss()
            }
        }
    }

    fun openDmFromPopup(popup: DirectMessagePopup) {
        dmPopupDismissJob?.cancel()
        _uiState.update { state ->
            state.copy(
                selectedTab = CourierTab.CHAT,
                selectedChatSection = ChatSection.DIRECT,
                selectedDmThreadId = popup.threadId,
                activeDmPopup = null,
                directThreads = state.directThreads.map { t ->
                    if (t.id == popup.threadId) t.copy(unreadCount = 0) else t
                }
            )
        }
    }

    fun dismissDmPopup() {
        dmPopupDismissJob?.cancel()
        _uiState.update { it.copy(activeDmPopup = null) }
    }

    fun dismissBreakdown(repairNow: Boolean) {
        breakdownDismissJob?.cancel()
        val breakdown = _uiState.value.activeBreakdown
        _uiState.update { it.copy(activeBreakdown = null) }
        if (repairNow && breakdown != null) {
            if (_uiState.value.walletBalanceRub >= breakdown.repairCostRub) {
                repairBreakdown(breakdown.id)
            } else {
                selectTab(CourierTab.BIKE)
            }
        }
    }

    fun repairBreakdown(breakdownId: Long) {
        val state = _uiState.value
        val target = state.accumulatedBreakdowns.firstOrNull { it.id == breakdownId } ?: return
        if (state.walletBalanceRub < target.repairCostRub) return

        soundEngine.playRepairSound()
        val remaining = state.accumulatedBreakdowns.filterNot { it.id == breakdownId }
        val repairEvent = TransferEvent(
            id = nextEventId++,
            subtitle = "Велогараж • Ремонт",
            reason = "Починка: ${target.title}",
            amountRub = target.repairCostRub,
            timestampLabel = currentTimeLabel(),
            isOrderPayout = true
        )
        val restoredHealth = (state.brakeHealthPercent + 15).coerceAtMost(100)

        _uiState.update { current ->
            current.copy(
                accumulatedBreakdowns = remaining,
                activeBreakdown = if (current.activeBreakdown?.id == breakdownId) null else current.activeBreakdown,
                transferHistory = (listOf(repairEvent) + current.transferHistory).take(25)
            )
        }

        viewModelScope.launch {
            prefsRepo.spendAndRestoreBrakes(target.repairCostRub, restoredHealth)
            prefsRepo.setAccumulatedBreakdownsRaw(InfiniteGigGenerator.serializeBreakdowns(remaining))
        }
    }

    fun repairAllBreakdowns() {
        val state = _uiState.value
        val list = state.accumulatedBreakdowns
        if (list.isEmpty()) return
        val totalCost = state.totalRepairAllCostRub
        if (state.walletBalanceRub < totalCost) return

        soundEngine.playRepairSound()
        val repairEvent = TransferEvent(
            id = nextEventId++,
            subtitle = "Велогараж • Капремонт",
            reason = "Устранено поломок: ${list.size} шт.",
            amountRub = totalCost,
            timestampLabel = currentTimeLabel(),
            isOrderPayout = true
        )
        val restoredHealth = (state.brakeHealthPercent + 35).coerceAtMost(100)

        _uiState.update { current ->
            current.copy(
                accumulatedBreakdowns = emptyList(),
                activeBreakdown = null,
                transferHistory = (listOf(repairEvent) + current.transferHistory).take(25)
            )
        }

        viewModelScope.launch {
            prefsRepo.spendAndRestoreBrakes(totalCost, restoredHealth)
            prefsRepo.setAccumulatedBreakdownsRaw("")
        }
    }

    fun buyOrToggleBikeMod(mod: BikeModItem) {
        val state = _uiState.value
        val owned = state.ownedBikeModIds.contains(mod.id)
        if (owned) {
            soundEngine.playTuningInstallSound()
            viewModelScope.launch {
                prefsRepo.toggleBikeModEquipped(mod.id)
            }
            return
        }

        if (state.walletBalanceRub < mod.priceRub) return
        soundEngine.playTuningInstallSound()

        val modEvent = TransferEvent(
            id = nextEventId++,
            subtitle = "Тюнинг-ателье Trek",
            reason = "Тюнинг: ${mod.name}",
            amountRub = mod.priceRub,
            timestampLabel = currentTimeLabel(),
            isOrderPayout = true
        )

        _uiState.update { current ->
            current.copy(
                transferHistory = (listOf(modEvent) + current.transferHistory).take(25)
            )
        }

        viewModelScope.launch {
            prefsRepo.purchaseAndEquipBikeMod(mod.id, mod.priceRub)
        }
    }

    fun triggerDiagnosticBreakdownTest() {
        lastBreakdownAtMs = 0L
        maybeTriggerBikeBreakdown(forceFromWear = true)
    }

    private fun triggerIncomingAngryDm() {
        val state = _uiState.value
        val threads = state.directThreads
        if (threads.isEmpty()) return
        val target = threads[Random.nextInt(threads.size)]
        val recentOpponentTexts = target.messages
            .filter { !it.isFromMe }
            .map { it.text }
            .toSet()
        val incoming = InfiniteGigGenerator.generateAngryDm(
            threadId = target.id,
            nextMsgId = nextDmMsgId++,
            timeLabel = currentTimeLabel(),
            accumulatedBreakdowns = state.accumulatedBreakdowns,
            recentOpponentTexts = recentOpponentTexts
        )

        val viewingThisThread = state.selectedTab == CourierTab.CHAT &&
            state.selectedChatSection == ChatSection.DIRECT &&
            state.selectedDmThreadId == target.id

        _uiState.update { current ->
            current.copy(
                directThreads = current.directThreads.map { thread ->
                    if (thread.id == target.id) {
                        thread.copy(
                            unreadCount = if (viewingThisThread) 0 else thread.unreadCount + 1,
                            messages = (thread.messages + incoming).takeLast(50)
                        )
                    } else {
                        thread
                    }
                },
                activeDmPopup = if (viewingThisThread) {
                    current.activeDmPopup
                } else {
                    DirectMessagePopup(
                        id = nextDmPopupId++,
                        threadId = target.id,
                        senderName = target.personName,
                        senderRole = target.personRole,
                        previewText = incoming.text
                    )
                }
            )
        }
        soundEngine.playIncomingDm()
        if (!viewingThisThread) {
            scheduleDmPopupDismiss()
        }
    }

    private fun maybeTriggerBikeBreakdown(forceFromWear: Boolean) {
        val now = System.currentTimeMillis()
        if (now - lastBreakdownAtMs < 40000L) return
        if (!forceFromWear && _uiState.value.activeBreakdown != null) return

        val state = _uiState.value
        if (state.accumulatedBreakdowns.size >= 7) return

        lastBreakdownAtMs = now
        val existingCodes = state.accumulatedBreakdowns.map { it.code }.toSet()
        val alert = InfiniteGigGenerator.generateBikeBreakdown(
            id = nextBreakdownId++,
            currentGear = state.currentGear,
            brakeHealthPercent = state.brakeHealthPercent,
            existingCodes = existingCodes,
            timeLabel = currentTimeLabel()
        )
        val updatedBreakdowns = state.accumulatedBreakdowns + alert
        val nextHealth = (state.brakeHealthPercent - alert.severityWear).coerceAtLeast(5)
        val dropGear = (alert.blocksHighGears || updatedBreakdowns.size >= 3) && state.currentGear >= 4

        _uiState.update {
            it.copy(
                activeBreakdown = alert,
                accumulatedBreakdowns = updatedBreakdowns,
                currentGear = if (dropGear) 3 else it.currentGear
            )
        }
        soundEngine.playBreakdownAlert()
        viewModelScope.launch {
            prefsRepo.setBrakeHealth(nextHealth)
            prefsRepo.setAccumulatedBreakdownsRaw(InfiniteGigGenerator.serializeBreakdowns(updatedBreakdowns))
        }
        scheduleBreakdownDismiss()
    }

    private fun scheduleDmPopupDismiss() {
        dmPopupDismissJob?.cancel()
        dmPopupDismissJob = viewModelScope.launch {
            delay(4600L)
            _uiState.update { it.copy(activeDmPopup = null) }
        }
    }

    private fun scheduleBreakdownDismiss() {
        breakdownDismissJob?.cancel()
        breakdownDismissJob = viewModelScope.launch {
            delay(6200L)
            _uiState.update { it.copy(activeBreakdown = null) }
        }
    }

    fun updateStudioSubtitle(value: String) {
        _uiState.update { it.copy(memeStudio = it.memeStudio.copy(subtitle = value)) }
    }

    fun updateStudioReason(value: String) {
        _uiState.update { it.copy(memeStudio = it.memeStudio.copy(reason = value)) }
    }

    fun updateStudioAmount(value: String) {
        _uiState.update { it.copy(memeStudio = it.memeStudio.copy(amountText = value)) }
    }

    fun updateStudioPosition(position: OverlayPosition) {
        _uiState.update { it.copy(memeStudio = it.memeStudio.copy(position = position)) }
    }

    fun updateStudioBadge(show: Boolean) {
        _uiState.update { it.copy(memeStudio = it.memeStudio.copy(showBankBadge = show)) }
    }

    fun updateStudioScale(scale: Float) {
        _uiState.update { it.copy(memeStudio = it.memeStudio.copy(badgeScale = scale)) }
    }

    fun setStudioPhotoUri(uriString: String?) {
        _uiState.update { it.copy(memeStudio = it.memeStudio.copy(selectedPhotoUri = uriString)) }
    }

    fun resetStudioPreset() {
        _uiState.update {
            it.copy(
                memeStudio = MemeStudioState(
                    selectedPhotoUri = it.memeStudio.selectedPhotoUri
                )
            )
        }
    }

    private fun nextOrderBatch(
        surgeMode: OrderSurgeMode,
        filter: OrderFilter
    ): List<DeliveryOrder> {
        return when (filter) {
            OrderFilter.FOOD -> List(4) {
                InfiniteGigGenerator.generateFoodOrder("ord_${orderSeq++}", surgeMode)
            }
            OrderFilter.SIDE_GIG -> List(4) {
                InfiniteGigGenerator.generateInfiniteSideGig("gig_${orderSeq++}", surgeMode, currentAbsurdity++)
            }
            OrderFilter.ALL -> listOf(
                InfiniteGigGenerator.generateInfiniteSideGig("gig_${orderSeq++}", surgeMode, currentAbsurdity++),
                InfiniteGigGenerator.generateFoodOrder("ord_${orderSeq++}", surgeMode),
                InfiniteGigGenerator.generateInfiniteSideGig("gig_${orderSeq++}", surgeMode, currentAbsurdity++),
                InfiniteGigGenerator.generateFoodOrder("ord_${orderSeq++}", surgeMode)
            ).shuffled()
        }
    }

    private fun nextRandomOrder(
        surgeMode: OrderSurgeMode,
        filter: OrderFilter
    ): DeliveryOrder {
        return when (filter) {
            OrderFilter.FOOD -> InfiniteGigGenerator.generateFoodOrder("ord_${orderSeq++}", surgeMode)
            OrderFilter.SIDE_GIG -> InfiniteGigGenerator.generateInfiniteSideGig(
                "gig_${orderSeq++}",
                surgeMode,
                currentAbsurdity
            )
            OrderFilter.ALL -> if (Random.nextInt(100) < 60) {
                InfiniteGigGenerator.generateInfiniteSideGig("gig_${orderSeq++}", surgeMode, currentAbsurdity)
            } else {
                InfiniteGigGenerator.generateFoodOrder("ord_${orderSeq++}", surgeMode)
            }
        }
    }

    private fun clampCursedRating(raw: Double): Double {
        val clamped = raw.coerceIn(1.0, 3.4)
        val rounded = (clamped * 10.0).roundToInt() / 10.0
        return if (rounded >= 3.3) 2.2 else rounded
    }

    private fun currentTimeLabel(): String = formatTimeLabel(System.currentTimeMillis())

    private fun formatTimeLabel(epochMs: Long): String {
        val fmt = SimpleDateFormat("HH:mm", Locale.getDefault())
        return fmt.format(Date(epochMs))
    }
}
