package com.example.data

enum class CourierTab {
    SHIFT,
    ORDERS,
    BIKE,
    SHOP,
    CHAT,
    STUDIO
}

enum class ChatSection {
    PUBLIC,
    DIRECT,
    CALLS
}

enum class OrderStage {
    AVAILABLE,
    PICKING_UP,
    DELIVERING,
    COMPLETED
}

enum class OverlayPosition(val label: String) {
    TOP_LEFT("Сверху слева"),
    TOP_CENTER("Сверху по центру"),
    BOTTOM_LEFT("Снизу слева")
}

enum class OrderSurgeMode(val label: String, val multiplier: Double) {
    NORMAL("Обычный", 1.0),
    RUSH_HOUR("Час пик ×1.3", 1.3),
    RAIN("Ливень ×1.6", 1.6)
}

enum class OrderCategory(val badgeLabel: String) {
    FOOD("Доставка еды"),
    SIDE_GIG("Халтурка")
}

enum class OrderFilter(val label: String) {
    ALL("Все"),
    FOOD("Еда"),
    SIDE_GIG("Доп. заказы")
}

data class BrakePadItem(
    val id: String,
    val name: String,
    val subtitle: String,
    val description: String,
    val priceRub: Int,
    val gripScore: Int,
    val durabilityBoost: Int
)

data class TransferEvent(
    val id: Long,
    val subtitle: String,
    val reason: String,
    val amountRub: Int,
    val timestampLabel: String,
    val isOrderPayout: Boolean = false
)

data class DeliveryOrder(
    val id: String,
    val restaurant: String,
    val itemSummary: String,
    val dropoffAddress: String,
    val district: String,
    val distanceKm: Double,
    val basePayRub: Int,
    val tipHint: String,
    val absurdityLevel: Int = 1,
    val category: OrderCategory = OrderCategory.FOOD,
    val stage: OrderStage = OrderStage.AVAILABLE
)

data class ClientReview(
    val id: Long,
    val author: String,
    val orderTitle: String,
    val stars: Int,
    val comment: String,
    val ratingDelta: Double,
    val timestampLabel: String
)

data class ChatMessage(
    val id: Long,
    val senderName: String,
    val senderBadge: String,
    val text: String,
    val timestampLabel: String,
    val isFromMe: Boolean = false,
    val isSystemNotice: Boolean = false
)

data class DirectMessage(
    val id: Long,
    val text: String,
    val timestampLabel: String,
    val isFromMe: Boolean
)

data class DirectThread(
    val id: String,
    val personName: String,
    val personRole: String,
    val avatarColorHex: Long,
    val unreadCount: Int = 0,
    val messages: List<DirectMessage> = emptyList()
)

enum class BikeModVisualType {
    NEON,
    SUBWOOFER,
    SPOILER,
    GOLD_CHAIN,
    SPINNERS,
    BOEING_EXHAUST
}

enum class GarageTrack(
    val id: String,
    val title: String,
    val shortLabel: String,
    val genreBadge: String,
    val bpm: Int,
    val accentColorHex: Long
) {
    GET_LOW_CRUNK(
        id = "get_low",
        title = "Lil Jon — Get Low (Crunk Remix)",
        shortLabel = "Get Low",
        genreBadge = "Crunk • 808 Sub",
        bpm = 101,
        accentColorHex = 0xFF00D26A
    ),
    DRIFT_PHONK(
        id = "drift_phonk",
        title = "Tokyo Drift • Cowbell Phonk",
        shortLabel = "Phonk",
        genreBadge = "Phonk • 808 Cowbell",
        bpm = 126,
        accentColorHex = 0xFFFF6D42
    ),
    MIDNIGHT_SYNTHWAVE(
        id = "synthwave",
        title = "Midnight Trek • Outrun Synth",
        shortLabel = "Synthwave",
        genreBadge = "Outrun • Analog Arp",
        bpm = 116,
        accentColorHex = 0xFF00E5FF
    ),
    BOEING_DNB(
        id = "boeing_dnb",
        title = "Boeing V1 • Liquid Breaks",
        shortLabel = "DnB V1",
        genreBadge = "Liquid DnB • Reese",
        bpm = 168,
        accentColorHex = 0xFFFFB300
    );

    fun next(): GarageTrack {
        val all = entries
        return all[(ordinal + 1) % all.size]
    }

    fun previous(): GarageTrack {
        val all = entries
        return all[(ordinal - 1 + all.size) % all.size]
    }

    companion object {
        fun fromId(id: String?): GarageTrack {
            return entries.firstOrNull { it.id == id } ?: GET_LOW_CRUNK
        }
    }
}

data class BikeModItem(
    val id: String,
    val name: String,
    val subtitle: String,
    val description: String,
    val effectBadge: String,
    val priceRub: Int,
    val payBonusPercent: Int,
    val durabilityShieldPercent: Int,
    val styleScore: Int,
    val visualType: BikeModVisualType
)

data class BikeBreakdownAlert(
    val id: Long,
    val code: String,
    val title: String,
    val description: String,
    val workImpact: String,
    val attitudeImpact: String,
    val severityWear: Int,
    val repairCostRub: Int,
    val blocksHighGears: Boolean = false,
    val payPenaltyPercent: Int = 12,
    val ratingPenaltyExtra: Double = 0.2,
    val detectedAtLabel: String = ""
)

data class DirectMessagePopup(
    val id: Long,
    val threadId: String,
    val senderName: String,
    val senderRole: String,
    val previewText: String
)

enum class CallStatus {
    RINGING,
    ACTIVE,
    ENDED
}

data class CallDialogueLine(
    val id: Long,
    val speakerName: String,
    val text: String,
    val isFromCourier: Boolean,
    val angerDelta: Int = 0
)

data class CallResponseOption(
    val id: String,
    val label: String,
    val spokenReply: String = label,
    val replyLine: String = "",
    val styleTag: String = "",
    val angerDelta: Int = 25,
    val ratingDelta: Double,
    val tipRub: Int = 0
)

data class IncomingComplaintCall(
    val id: Long,
    val plotId: String = "",
    val callerName: String,
    val callerRole: String,
    val phoneLabel: String,
    val orderContext: String,
    val avatarColorHex: Long,
    val openingComplaint: String,
    val currentClientLine: String,
    val courierLine: String? = null,
    val dialogueHistory: List<CallDialogueLine> = emptyList(),
    val turnNumber: Int = 1,
    val maxTurns: Int = 4,
    val clientAngerPercent: Int = 25,
    val isClientReacting: Boolean = false,
    val droppedByClient: Boolean = false,
    val accumulatedRatingDelta: Double = 0.0,
    val accumulatedTipRub: Int = 0,
    val usedOptionIds: Set<String> = emptySet(),
    val usedOptionLabels: Set<String> = emptySet(),
    val activeTwistLabel: String = "",
    val activeWitnessLabel: String = "",
    val status: CallStatus = CallStatus.RINGING,
    val durationSeconds: Int = 0,
    val resolutionNote: String? = null,
    val responseOptions: List<CallResponseOption> = emptyList()
)

data class CallHistoryEntry(
    val id: Long,
    val callerName: String,
    val callerRole: String,
    val summary: String,
    val outcomeLabel: String,
    val timestampLabel: String,
    val wasAnswered: Boolean
)

data class TreatmentGoal(
    val id: String,
    val title: String,
    val subtitle: String,
    val targetRub: Int
)

data class MemeStudioState(
    val selectedPhotoUri: String? = null,
    val subtitle: String = "Входящий перевод",
    val reason: String = "На лечение",
    val amountText: String = "+ 15₽",
    val position: OverlayPosition = OverlayPosition.TOP_LEFT,
    val showBankBadge: Boolean = true,
    val badgeScale: Float = 1.0f
)

data class CourierUiState(
    val selectedTab: CourierTab = CourierTab.SHIFT,
    val isOnShift: Boolean = true,
    val shiftSeconds: Long = 0L,
    val shiftStartTimeLabel: String? = null,
    val shiftEarnedRub: Int = 15,
    val treatmentJarRub: Int = 15,
    val spentRub: Int = 0,
    val deliveriesCompleted: Int = 0,
    val courierRating: Double = 2.7,
    val lastRatingDelta: Double = -0.4,
    val ratingPenaltyReason: String = "Слишком зелёный рюкзак на фоне светофора",
    val clientReviews: List<ClientReview> = emptyList(),
    val absurdityLevel: Int = 1,
    val activeOrder: DeliveryOrder? = null,
    val simulatedOffer: DeliveryOrder? = null,
    val inspectedOrder: DeliveryOrder? = null,
    val surgeMode: OrderSurgeMode = OrderSurgeMode.NORMAL,
    val orderFilter: OrderFilter = OrderFilter.ALL,
    val availableOrders: List<DeliveryOrder> = emptyList(),
    val transferHistory: List<TransferEvent> = emptyList(),
    val activeBanner: TransferEvent? = null,
    val chatMessages: List<ChatMessage> = emptyList(),
    val typingCourierName: String? = null,
    val unreadChatCount: Int = 2,
    val selectedChatSection: ChatSection = ChatSection.PUBLIC,
    val directThreads: List<DirectThread> = emptyList(),
    val selectedDmThreadId: String? = null,
    val activeDmPopup: DirectMessagePopup? = null,
    val activeCall: IncomingComplaintCall? = null,
    val callHistory: List<CallHistoryEntry> = emptyList(),
    val activeBreakdown: BikeBreakdownAlert? = null,
    val accumulatedBreakdowns: List<BikeBreakdownAlert> = emptyList(),
    val brakeCatalog: List<BrakePadItem> = defaultBrakePads(),
    val purchasedPadIds: Set<String> = setOf("stock_rubber"),
    val equippedPadId: String = "stock_rubber",
    val brakeHealthPercent: Int = 38,
    val bikeModCatalog: List<BikeModItem> = defaultBikeMods(),
    val ownedBikeModIds: Set<String> = emptySet(),
    val equippedBikeModIds: Set<String> = emptySet(),
    val garageMusicEnabled: Boolean = true,
    val isGarageMusicPlaying: Boolean = false,
    val currentGarageTrack: GarageTrack = GarageTrack.GET_LOW_CRUNK,
    val garageAutoMix: Boolean = true,
    val showGearboxWidget: Boolean = true,
    val currentGear: Int = 1,
    val isSettingsOpen: Boolean = false,
    val memeStudio: MemeStudioState = MemeStudioState(),
    val isDarkTheme: Boolean = true,
    val hapticsEnabled: Boolean = true,
    val soundsEnabled: Boolean = true,
    val goals: List<TreatmentGoal> = defaultGoals()
) {
    val walletBalanceRub: Int
        get() = (treatmentJarRub - spentRub).coerceAtLeast(0)

    val unreadDmCount: Int
        get() = directThreads.sumOf { it.unreadCount }

    val totalUnreadMessages: Int
        get() = unreadChatCount + unreadDmCount

    val highGearsBlocked: Boolean
        get() = accumulatedBreakdowns.any { it.blocksHighGears } || accumulatedBreakdowns.size >= 3

    val totalPayPenaltyPercent: Int
        get() = accumulatedBreakdowns.sumOf { it.payPenaltyPercent }.coerceAtMost(65)

    val totalModPayBonusPercent: Int
        get() = bikeModCatalog.filter { equippedBikeModIds.contains(it.id) }.sumOf { it.payBonusPercent }

    val totalDurabilityShieldPercent: Int
        get() = bikeModCatalog.filter { equippedBikeModIds.contains(it.id) }.sumOf { it.durabilityShieldPercent }.coerceAtMost(75)

    val totalBikeStyleScore: Int
        get() = 15 + bikeModCatalog.filter { equippedBikeModIds.contains(it.id) }.sumOf { it.styleScore }

    val effectivePayMultiplier: Double
        get() = ((100 - totalPayPenaltyPercent + totalModPayBonusPercent).coerceAtLeast(35)) / 100.0

    val bikeConditionPercent: Int
        get() = (brakeHealthPercent - accumulatedBreakdowns.size * 14).coerceIn(5, 100)

    val publicAttitudeLabel: String
        get() = when {
            accumulatedBreakdowns.size >= 4 -> "Враг подъезда №1 • вызывают ЖЭК"
            accumulatedBreakdowns.size == 3 -> "Косятся и звонят диспетчеру"
            accumulatedBreakdowns.size in 1..2 -> "Раздражены скрипом и грохотом"
            totalBikeStyleScore >= 70 -> "Респект на районе (но всё равно 1★)"
            else -> "Терпят зелёный рюкзак"
        }

    val totalRepairAllCostRub: Int
        get() = if (accumulatedBreakdowns.size <= 1) {
            accumulatedBreakdowns.sumOf { it.repairCostRub }
        } else {
            ((accumulatedBreakdowns.sumOf { it.repairCostRub }) * 0.85).toInt()
        }
}

fun defaultBrakePads(): List<BrakePadItem> = listOf(
    BrakePadItem(
        id = "stock_rubber",
        name = "Стёртые штатные резинки",
        subtitle = "Стояли на Trek с завода",
        description = "Тормозят примерно как доброе слово. В дождь работают исключительно как смазка.",
        priceRub = 0,
        gripScore = 25,
        durabilityBoost = 25
    ),
    BrakePadItem(
        id = "sneaker_sole",
        name = "Подошва старого кроссовка",
        subtitle = "Бюджетный хендмейд • 42 размер",
        description = "Вырезано из левого кроссовка и прикручено изолентой. Надёжно, пока не протрётся носок.",
        priceRub = 45,
        gripScore = 45,
        durabilityBoost = 40
    ),
    BrakePadItem(
        id = "squeak_vbrake",
        name = "V-Brake «Сирена в подъезде»",
        subtitle = "Ультразвуковой компаунд",
        description = "Издают такой пронзительный визг, что пешеходы отпрыгивают на газон за 150 метров.",
        priceRub = 150,
        gripScore = 65,
        durabilityBoost = 60
    ),
    BrakePadItem(
        id = "shimano_semi",
        name = "Полуметалл Shimano B01S",
        subtitle = "Настоящая велоклассика",
        description = "Останавливают даже на мокрой брусчатке, когда клиент просит доставить суп не расплескав.",
        priceRub = 390,
        gripScore = 82,
        durabilityBoost = 80
    ),
    BrakePadItem(
        id = "wardrobe_ceramic",
        name = "Керамика «Анти-Шкаф Pro»",
        subtitle = "Усиленная серия для халтурок",
        description = "Рассчитаны на спуск с горки с советским шкафом и роботом-пылесосом на багажнике.",
        priceRub = 750,
        gripScore = 94,
        durabilityBoost = 95
    ),
    BrakePadItem(
        id = "titanium_immortal",
        name = "Карбон-титан «Бессмертие»",
        subtitle = "Спецзаказ для 5-й передачи",
        description = "Огнеупорные колодки. Переживут и реагенты, и бордюры, и ваш рейтинг 1.2★.",
        priceRub = 1490,
        gripScore = 100,
        durabilityBoost = 100
    )
)

fun defaultGoals(): List<TreatmentGoal> = listOf(
    TreatmentGoal(
        id = "ramen",
        title = "Горячий доширак и чай",
        subtitle = "Базовый минимум на обочине",
        targetRub = 150
    ),
    TreatmentGoal(
        id = "brakes",
        title = "Новые тормозные колодки",
        subtitle = "Чтобы не тормозить кроссовками",
        targetRub = 750
    ),
    TreatmentGoal(
        id = "chain",
        title = "Титановая цепь на Trek",
        subtitle = "Против дождя и реагентов",
        targetRub = 1500
    ),
    TreatmentGoal(
        id = "spa",
        title = "Реабилитация спины",
        subtitle = "После 12 часов с терморюкзаком",
        targetRub = 3000
    )
)

fun defaultBikeMods(): List<BikeModItem> = listOf(
    BikeModItem(
        id = "mod_neon",
        name = "Неон под раму «Underground»",
        subtitle = "Изумрудно-лаймовая LED-подсветка",
        description = "Подсвечивает асфальт под Trek как в Need for Speed. Пешеходы фотографируют вместо того, чтобы ругаться.",
        effectBadge = "+8% к выплатам • +25 стиля",
        priceRub = 180,
        payBonusPercent = 8,
        durabilityShieldPercent = 0,
        styleScore = 25,
        visualType = BikeModVisualType.NEON
    ),
    BikeModItem(
        id = "mod_subwoofer",
        name = "Сабвуфер «Get Low Bass Box»",
        subtitle = "Крепится прямо под термосумку",
        description = "Качает басы на весь квартал под Lil Jon. Голуби взлетают заранее, но сосед с 4 этажа уже пишет в ЛС.",
        effectBadge = "+10% к выплатам • +30 стиля",
        priceRub = 290,
        payBonusPercent = 10,
        durabilityShieldPercent = 0,
        styleScore = 30,
        visualType = BikeModVisualType.SUBWOOFER
    ),
    BikeModItem(
        id = "mod_spoiler",
        name = "Карбон-спойлер «Downforce»",
        subtitle = "Аэродинамика на терморюкзак",
        description = "Прижимает заднее колесо к плитке, когда вы кричите V1 и ROTATE на 4–5 передаче.",
        effectBadge = "+15% к выплатам • +20 стиля",
        priceRub = 420,
        payBonusPercent = 15,
        durabilityShieldPercent = 10,
        styleScore = 20,
        visualType = BikeModVisualType.SPOILER
    ),
    BikeModItem(
        id = "mod_gold_chain",
        name = "Усиленная золотая цепь KMC",
        subtitle = "Защита трансмиссии и звёзд",
        description = "Больше не слетает при резком переключении на 5-ю передачу и защищает от поломок.",
        effectBadge = "-30% поломок и износа • +15 стиля",
        priceRub = 550,
        payBonusPercent = 5,
        durabilityShieldPercent = 30,
        styleScore = 15,
        visualType = BikeModVisualType.GOLD_CHAIN
    ),
    BikeModItem(
        id = "mod_spinners",
        name = "Хром-спиннеры и диски",
        subtitle = "26-дюймовый тюнинг ободов",
        description = "Колёса блестят даже после луж, а тормозные диски меньше перегреваются.",
        effectBadge = "-25% износа • +12% выплат",
        priceRub = 780,
        payBonusPercent = 12,
        durabilityShieldPercent = 25,
        styleScore = 25,
        visualType = BikeModVisualType.SPINNERS
    ),
    BikeModItem(
        id = "mod_boeing_exhaust",
        name = "Турбо-выпуск «Boeing Stage 3»",
        subtitle = "Двойные титановые трубы под седлом",
        description = "Максимальный пафос для 5-й передачи ROTATE. Свистит при разгоне как настоящий авиалайнер.",
        effectBadge = "+25% к выплатам • +35 стиля",
        priceRub = 1250,
        payBonusPercent = 25,
        durabilityShieldPercent = 10,
        styleScore = 35,
        visualType = BikeModVisualType.BOEING_EXHAUST
    )
)
