package com.example.data

import kotlin.math.roundToInt
import kotlin.random.Random

/**
 * Infinite combinatorial & escalating comedy generator for side-gig odd jobs ("халтурки"),
 * food orders, sarcastic client reviews, rating sabotage penalties, and courier chat banter.
 */
object InfiniteGigGenerator {

    private val seenGigTitles = ArrayDeque<String>()

    private data class GigCore(
        val title: String,
        val detail: String,
        val minPay: Int,
        val maxPay: Int,
        val hint: String
    )

    private val starterGigs = listOf(
        GigCore(
            title = "Купить пылесос и проверить модель",
            detail = "DNS / М.Видео • Сверить серийник, включить на месте, проверить тягу на чеке и довезти",
            minPay = 460,
            maxPay = 820,
            hint = "Доплата за тест мощности всасывания прямо на кассе"
        ),
        GigCore(
            title = "Перенести шкаф днём и вернуть на ночь",
            detail = "Перетащить советский шкаф в гостиную к приезду тёщи, а ровно в 23:00 занести обратно",
            minPay = 680,
            maxPay = 1190,
            hint = "После шкафа точно переведут +15₽ «На лечение спины»"
        ),
        GigCore(
            title = "Проверить б/у видеокарту с Авито",
            detail = "Прогнать FurMark 10 минут у продавца, понюхать на запах гари и довезти в терморюкзаке",
            minPay = 540,
            maxPay = 940,
            hint = "Есть с собой отвёртка? Обещают бонус за диагностику"
        ),
        GigCore(
            title = "Постоять в очереди в МФЦ вместо клиента",
            detail = "Взять талончик, посидеть 40 минут в зелёной куртке и уступить место по звонку",
            minPay = 340,
            maxPay = 580,
            hint = "Ноги отдыхают, но могут попросить ещё оплатить госпошлину"
        )
    )

    private val actions = listOf(
        "Купить и протестировать на месте",
        "Перенести на руках до вечера, а ночью вернуть",
        "Проверить по чек-листу и снять видеоотчёт про",
        "Срочно выкупить у подозрительного продавца",
        "Временно посторожить у подъезда",
        "Аккуратно довезти на раме велосипеда",
        "Отнести в ремонт, поспорить с мастером и забрать",
        "Сделать вид, что ты разбираешься, и оценить",
        "Перепрятать на балконе до отъезда гостей",
        "Распаковать на кассе, понюхать и проверить",
        "Вынести по лестнице без лифта и поставить в угол",
        "Обменять по гарантии без чека",
        "Забрать с Авито, поторговаться до слёз и привезти",
        "Убедить консьержку пропустить в грузовой лифт",
        "Отмыть на автомойке самообслуживания и вернуть"
    )

    private val objects = listOf(
        "моющий робот-пылесос с Алисой",
        "трёхстворчатый шкаф из 1979 года",
        "холодильник «Бирюса» с банками огурцов",
        "игровое кресло со сломанным газлифтом",
        "бабушкин ковёр весом с электросамокат",
        "б/у кофемашину, которая варит только кипяток",
        "трёхметровый фикус по имени Геннадий",
        "майнинг-ферму на четырёх шумных кулерах",
        "аквариум с одним наглым карасём",
        "чугунную ванну с 5 этажа в 3-й подъезд",
        "чемодан зимних кабачков с дачи",
        "стеллаж из ИКЕА без трёх болтов и инструкции",
        "коллекцию кактусов в открытой коробке",
        "стиральную машину, которая прыгает по ванной",
        "5 кг подозрительно дешёвых мандаринов",
        "диван-книжку с сюрпризом внутри"
    )

    private val conditions = listOf(
        "притворившись дальним родственником из Сызрани",
        "пока тёща не уехала на вечернюю электричку",
        "сверяя серийный номер под лупой прямо при кассире",
        "не разбудив соседа с перфоратором на 4-м этаже",
        "записывая кружочек в Telegram каждые 3 минуты",
        "потому что «грузчики слишком дорогие, а вы на велике»",
        "и ровно в полночь повторить всё в обратном порядке",
        "сохраняя максимально невозмутимое лицо",
        "если спросят на вахте — сказать, что вы из ЖЭКа",
        "обязательно взвесить на глаз и скинуть фото чека",
        "держа термосумку для вида на одном плече",
        "успеть до закрытия шлагбаума и смены караула",
        "попросить скидку за царапину, даже если её нет",
        "не уронив ни одной банки с крышкой"
    )

    private val escalatingTwists = listOf(
        "Клиент смотрит в глазок и оценивает технику приседа.",
        "Лифт работает, но останавливается только на нечётных этажах.",
        "В комплекте инструкция только на древнескандинавском.",
        "На месте ждёт кот Борис, который не доверяет зелёным курткам.",
        "Обещают оплату сразу, если не будете дышать слишком громко.",
        "Прошлый курьер ушёл в отпуск посреди подъезда.",
        "Если справитесь быстрее 20 минут — поставят 2 звезды вместо 1.",
        "Бонус: разрешат потрогать проверяемый пылесос.",
        "Придётся доказать продавцу, что вы не бот.",
        "Особое условие: вернуть шкаф ровно под тем же углом к обоям."
    )

    private val venues = listOf(
        "Шаурма у Вокзала 24/7" to listOf(
            "2× Классическая в лаваше, айран",
            "Шаурма на углях XL, чесночный соус",
            "Мега-ролл без лука (лук всё равно положили)"
        ),
        "Бургер Кинг • ТЦ Галерея" to listOf(
            "Воппер комбо, картошка XL, кола без льда",
            "Двойной чизбургер, наггетсы 9 шт",
            "Кинг сет с двумя соусами и надеждой"
        ),
        "Суши Wok" to listOf(
            "Сет Филадельфия, 4 пары палочек на одного",
            "Вок с курицей терияки, том-ям острый",
            "Запечённый сет Калифорния (просили горячим)"
        ),
        "Кофемания Express" to listOf(
            "2× Раф лавандовый на миндальном, круассан",
            "Матча латте, сэндвич с лососем",
            "Двойной эспрессо без стаканчика (шутка, в стакане)"
        ),
        "Додо Пицца" to listOf(
            "Пепперони 35см, сырные палочки",
            "Мясная 30см, додстер",
            "Четыре сезона 35см, не трясти по брусчатке"
        ),
        "Теремок" to listOf(
            "Блин с сёмгой, борщ, сбитень",
            "2× Илья Муромец, морс клюквенный",
            "Картошка с грибами и грустным взглядом"
        )
    )

    private val streets = listOf(
        "ул. Ленина",
        "пр. Мира",
        "Набережная",
        "ул. Пушкина",
        "ул. Гагарина",
        "Советский пр.",
        "ул. Кирова",
        "Заводской проезд",
        "ул. Чехова",
        "ул. Строителей"
    )

    private val districts = listOf("Центр", "Северный", "Речной", "Старый город", "Южный", "Промзона")

    fun generateFoodOrder(orderId: String, surgeMode: OrderSurgeMode): DeliveryOrder {
        val (restaurant, dishes) = venues[Random.nextInt(venues.size)]
        val itemSummary = dishes[Random.nextInt(dishes.size)]
        val street = streets[Random.nextInt(streets.size)]
        val house = Random.nextInt(2, 128)
        val apt = Random.nextInt(1, 95)
        val district = districts[Random.nextInt(districts.size)]

        val rawDist = Random.nextDouble(0.5, 4.9)
        val distanceKm = (rawDist * 10.0).roundToInt() / 10.0

        val basePickupFee = Random.nextInt(75, 115)
        val distancePay = (distanceKm * Random.nextInt(42, 62)).roundToInt()
        val randomBonus = Random.nextInt(0, 45)
        val rawTotal = ((basePickupFee + distancePay + randomBonus) * surgeMode.multiplier).roundToInt()
        val roundedPay = ((rawTotal + 2) / 5) * 5

        val hints = listOf(
            "Часто кидают +15₽ «На лечение»",
            "5 этаж без лифта • высокий шанс 1 звезды",
            "Домофон не работает, кричать под окном",
            "Офисный заказ • оценят цвет вашей куртки",
            "Встречный ветер • повышенная выплата"
        )

        return DeliveryOrder(
            id = orderId,
            restaurant = restaurant,
            itemSummary = itemSummary,
            dropoffAddress = "$street, $house, кв. $apt",
            district = district,
            distanceKm = distanceKm,
            basePayRub = roundedPay,
            tipHint = hints[Random.nextInt(hints.size)],
            absurdityLevel = 1,
            category = OrderCategory.FOOD
        )
    }

    /**
     * Generates an infinite stream of unique, progressively funnier side-gig tasks.
     */
    fun generateInfiniteSideGig(
        orderId: String,
        surgeMode: OrderSurgeMode,
        absurdityLevel: Int
    ): DeliveryOrder {
        val street = streets[Random.nextInt(streets.size)]
        val house = Random.nextInt(3, 110)
        val apt = Random.nextInt(1, 88)
        val district = districts[Random.nextInt(districts.size)]
        val rawDist = Random.nextDouble(0.4, 5.8)
        val distanceKm = (rawDist * 10.0).roundToInt() / 10.0

        // First few rolls guarantee iconic starter gigs if unseen, then infinite combinatorial escalation
        val unusedStarter = starterGigs.firstOrNull { !seenGigTitles.contains(it.title) }
        if (absurdityLevel <= 4 && unusedStarter != null) {
            recordTitle(unusedStarter.title)
            val rawPay = Random.nextInt(unusedStarter.minPay, unusedStarter.maxPay + 1)
            val pay = (((rawPay * surgeMode.multiplier).roundToInt() + 4) / 10) * 10
            return DeliveryOrder(
                id = orderId,
                restaurant = unusedStarter.title,
                itemSummary = unusedStarter.detail,
                dropoffAddress = "$street, $house, кв. $apt",
                district = district,
                distanceKm = distanceKm,
                basePayRub = pay,
                tipHint = unusedStarter.hint,
                absurdityLevel = absurdityLevel,
                category = OrderCategory.SIDE_GIG
            )
        }

        // Combinatorial infinite generator that escalates with absurdityLevel
        var title: String
        var obj: String
        var attempts = 0
        do {
            val action = actions[Random.nextInt(actions.size)]
            obj = objects[Random.nextInt(objects.size)]
            title = "$action $obj"
            attempts++
        } while (seenGigTitles.contains(title) && attempts < 12)

        recordTitle(title)

        val cond1 = conditions[Random.nextInt(conditions.size)]
        val cond2 = conditions.filter { it != cond1 }[Random.nextInt(conditions.size - 1)]
        val twist = escalatingTwists[Random.nextInt(escalatingTwists.size)]

        val detail = when {
            absurdityLevel >= 10 -> "Уровень хаоса $absurdityLevel: нужно выполнить $cond1, затем $cond2. $twist"
            absurdityLevel >= 6 -> "Задача: $cond1, а если что — $cond2. $twist"
            else -> "Условие: $cond1. $twist"
        }

        val levelBonus = (absurdityLevel.coerceAtMost(25)) * 28
        val basePay = Random.nextInt(360, 790) + levelBonus
        val scaled = (basePay * surgeMode.multiplier).roundToInt()
        val roundedPay = ((scaled + 4) / 10) * 10

        val hints = listOf(
            "Уровень абсурда Lv.$absurdityLevel • высокий шанс +15₽ «На лечение»",
            "После такого задания рейтинг всё равно снизят",
            "Обещали доплатить за актёрскую игру",
            "Чат курьеров уже ставит лайки на этот заказ",
            "Рекомендация: прихватить изоленту и терпение"
        )

        return DeliveryOrder(
            id = orderId,
            restaurant = title,
            itemSummary = detail,
            dropoffAddress = "$street, $house, кв. $apt",
            district = district,
            distanceKm = distanceKm,
            basePayRub = roundedPay,
            tipHint = hints[Random.nextInt(hints.size)],
            absurdityLevel = absurdityLevel,
            category = OrderCategory.SIDE_GIG
        )
    }

    private fun recordTitle(title: String) {
        seenGigTitles.addLast(title)
        if (seenGigTitles.size > 40) {
            seenGigTitles.removeFirst()
        }
    }

    /**
     * Generates a sarcastic 1-2 star client review and calculates a cursed rating delta
     * so the courier's rating NEVER stays good.
     */
    fun generateCursedReview(
        id: Long,
        orderTitle: String,
        currentRating: Double,
        timestampLabel: String,
        accumulatedBreakdowns: List<BikeBreakdownAlert> = emptyList()
    ): Pair<ClientReview, String> {
        val authors = listOf(
            "Елена В.", "Игорь П.", "Тёща Антонина", "Максим из 44-й",
            "Ольга К.", "Аноним с глазком", "Сергей DNS", "Консьержка Зинаида"
        )
        val brokenComplaints = listOf(
            "У курьера на велосипеде всё гремит и скрипит как старый трактор! Пакет трясло всю дорогу!",
            "Цепь отвалилась прямо у подъезда, курьер заносил еду весь в мазуте. Срочно в ремонт!",
            "Восьмёрка на колесе такая, что пицца перемешалась в рулет. Отношение к технике ужасное — 1★.",
            "Колодки дымились так, что сработал датчик дыма в холле! Больше с этим велосипедом не приезжайте!"
        )
        val complaints = listOf(
            "Всё привёз вовремя и даже проверил модель, но слишком громко дышал в подъезде. 1 звезда.",
            "Шкаф перенесли идеально, гости не заметили. Но в 23:02 поставили на 2 мм левее! Ужас.",
            "Пылесос тянет отлично, курьер вежливый, но почему глаза такие грустные? Скинула 15₽ на лечение.",
            "Доставил за 8 минут, горячее. Но на велосипеде крыло скрипит — я чуть не проснулся. 2 звезды.",
            "Всё чётко, спасибо! Ставлю 1 звезду случайно, потому что экран мокрый, менять лень.",
            "Слишком быстро приехал, я ещё не успел одеться. Психологическая травма.",
            "Термосумка слишком ярко-зелёная, испортила эстетику моего коридора. Больше не заказываю!",
            "Кот Борис покормлен, фикус полит, но курьер посмотрел на фикус без уважения."
        )
        val systemPenalties = listOf(
            "Алгоритм: штраф за подозрительно ровную езду",
            "Жалоба: слишком яркая зелёная куртка в сумерках",
            "Корректировка: клиент моргнул в момент передачи пакета",
            "Штраф за попытку иметь рейтинг выше 3.4★",
            "Автоснижение: велосипед не прошёл фейс-контроль у шлагбаума"
        )

        val hasBreakdowns = accumulatedBreakdowns.isNotEmpty()
        val stars = if (hasBreakdowns || Random.nextInt(100) < 70) 1 else 2
        val extraBreakdownDrop = accumulatedBreakdowns.sumOf { it.ratingPenaltyExtra }.coerceAtMost(0.6)
        val baseDrop = if (currentRating > 3.2) {
            -Random.nextDouble(0.5, 1.1)
        } else if (currentRating < 1.3 && !hasBreakdowns) {
            if (Random.nextBoolean()) 0.2 else -0.1
        } else {
            -Random.nextDouble(0.2, 0.5)
        }
        val delta = ((baseDrop - extraBreakdownDrop) * 10.0).roundToInt() / 10.0

        val commentText = if (hasBreakdowns && Random.nextInt(100) < 75) {
            brokenComplaints[Random.nextInt(brokenComplaints.size)]
        } else {
            complaints[Random.nextInt(complaints.size)]
        }

        val review = ClientReview(
            id = id,
            author = authors[Random.nextInt(authors.size)],
            orderTitle = orderTitle,
            stars = stars,
            comment = commentText,
            ratingDelta = delta,
            timestampLabel = timestampLabel
        )
        val penaltyReason = if (hasBreakdowns) {
            "Поломки велосипеда (${accumulatedBreakdowns.size} шт.): клиенты жалуются на грохот и масло"
        } else {
            systemPenalties[Random.nextInt(systemPenalties.size)]
        }
        return review to penaltyReason
    }

    fun initialReviews(timeLabel: String): List<ClientReview> = listOf(
        ClientReview(
            id = 1L,
            author = "Аноним с 5 этажа",
            orderTitle = "Шаурма у Вокзала 24/7",
            stars = 1,
            comment = "Привёз за 10 минут, но в глазах читалась боль. Перевёл 15₽ на лечение и поставил 1 звезду для тонуса.",
            ratingDelta = -0.4,
            timestampLabel = timeLabel
        ),
        ClientReview(
            id = 2L,
            author = "Тёща Антонина",
            orderTitle = "Перенести шкаф днём и вернуть на ночь",
            stars = 2,
            comment = "Шкаф стоит ровно, но курьер вытер лоб рукой без перчаток.",
            ratingDelta = -0.3,
            timestampLabel = timeLabel
        )
    )

    fun initialChatMessages(timeLabel: String): List<ChatMessage> = listOf(
        ChatMessage(
            id = 1L,
            senderName = "Колян • Trek",
            senderBadge = "Вело",
            text = "Ребят, кому сегодня тоже +15₽ «На лечение» прилетело? Я уже на третью пачку дошика так собрал 😂",
            timestampLabel = timeLabel
        ),
        ChatMessage(
            id = 2L,
            senderName = "Вадик Без Тормозов",
            senderBadge = "Халтурщик",
            text = "Утро началось бодро: мужик попросил перенести шкаф в гостиную к приезду тёщи, а в 23:00 занести обратно. Зато +850₽",
            timestampLabel = timeLabel
        ),
        ChatMessage(
            id = 3L,
            senderName = "Лёха Термосумка",
            senderBadge = "1.9★",
            text = "Моему рейтингу конец. Доставил кофе идеально, так клиент написал «слишком пристальный взгляд» и влепил 1 звезду",
            timestampLabel = timeLabel
        )
    )

    fun generatePeerReply(userText: String, timeLabel: String, nextId: Long): ChatMessage {
        val lower = userText.lowercase()
        val peers = listOf(
            "Колян • Trek" to "Вело",
            "Вадик Без Тормозов" to "Халтурщик",
            "Марина Самокат" to "Pro",
            "Диспетчер Слава" to "Штаб",
            "Лёха Термосумка" to "1.6★"
        )

        val (name, badge, reply) = when {
            "шкаф" in lower || "мебел" in lower -> Triple(
                "Вадик Без Тормозов",
                "Халтурщик",
                "О, брат, я тоже сегодня этот шкаф видел! Главное не перепутай, в какую сторону дверцы открываются, а то -0.8★ прилетит"
            )
            "пылесос" in lower || "dns" in lower || "модел" in lower -> Triple(
                "Марина Самокат",
                "Pro",
                "В DNS меня уже узнают как «та с зелёным рюкзаком, которая проверяет тягу у пылесосов на кассе» 😅"
            )
            "рейтинг" in lower || "звезд" in lower || "отзыв" in lower -> Triple(
                "Лёха Термосумка",
                "1.6★",
                "Забей на рейтинг, выше 3.5★ тут только легенды на электровелах держатся, и то до первого дождя"
            )
            "15" in lower || "лечен" in lower -> Triple(
                "Колян • Trek",
                "Вело",
                "+15₽ На лечение — это уже официальная валюта нашего района. Ещё 100 таких переводов и куплю новые колодки!"
            )
            else -> {
                val (pName, pBadge) = peers[Random.nextInt(peers.size)]
                val genericReplies = listOf(
                    "Жиза! Только что везли чугунную сковородку через весь центр, а клиент скинул +15₽ «На лечение» и ушёл в закат",
                    "Держись, брат! Я сейчас в МФЦ очередь держу вместо клиента, тут хоть посидеть можно",
                    "Мне только что рейтинг снизили на 0.3★ за «слишком бодрый звонок в домофон» 💀",
                    "Кстати, в дождь тариф ×1.6 реально кормит, главное телефон в пакет убрать",
                    "Кто на Южном — аккуратнее, там бабка на 4-м этаже просит фикус пересадить за 300₽"
                )
                Triple(pName, pBadge, genericReplies[Random.nextInt(genericReplies.size)])
            }
        }

        return ChatMessage(
            id = nextId,
            senderName = name,
            senderBadge = badge,
            text = reply,
            timestampLabel = timeLabel
        )
    }

    fun generateAmbientChat(timeLabel: String, nextId: Long): ChatMessage {
        val chatter = listOf(
            Triple("Димон с Диваном", "Халтурщик", "Кто-нибудь брал заказ «проверить б/у видеокарту»? Продавец оказался моим бывшим одноклассником 😂"),
            Triple("Марина Самокат", "Pro", "Сейчас бы +15₽ на лечение и горячий раф, а не вот эти вот ваши 5 этажи без лифта"),
            Triple("Диспетчер Слава", "Штаб", "Напоминание: не забывайте проверять серийный номер пылесоса до выхода из магазина!"),
            Triple("Колян • Trek", "Вело", "Рейтинг снова 1.9★, зато душа спокойна и цепь смазана")
        )
        val (name, badge, text) = chatter[Random.nextInt(chatter.size)]
        return ChatMessage(
            id = nextId,
            senderName = name,
            senderBadge = badge,
            text = text,
            timestampLabel = timeLabel
        )
    }

    fun initialDirectThreads(timeLabel: String): List<DirectThread> = listOf(
        DirectThread(
            id = "dm_antonina",
            personName = "Тёща Антонина",
            personRole = "Заказчица шкафа",
            avatarColorHex = 0xFFE53935L,
            unreadCount = 1,
            messages = listOf(
                DirectMessage(
                    id = 1001L,
                    text = "Алло!! Вы почему шкаф вчера поставили на 3 миллиметра ближе к телевизору?! Я всё замерила линейкой, это вредительство!",
                    timestampLabel = timeLabel,
                    isFromMe = false
                )
            )
        ),
        DirectThread(
            id = "dm_vacuum_guy",
            personName = "Максим из 44-й",
            personRole = "Клиент DNS • пылесос",
            avatarColorHex = 0xFFD81B60L,
            unreadCount = 0,
            messages = listOf(
                DirectMessage(
                    id = 1002L,
                    text = "Вы точно проверяли тягу пылесоса в магазине? Он теперь всасывает только мои нервы и кота! Требую 15₽ компенсации!",
                    timestampLabel = timeLabel,
                    isFromMe = false
                )
            )
        ),
        DirectThread(
            id = "dm_neighbor",
            personName = "Сосед с 4 этажа",
            personRole = "Жертва скрипа тормозов",
            avatarColorHex = 0xFFFB8C00L,
            unreadCount = 1,
            messages = listOf(
                DirectMessage(
                    id = 1003L,
                    text = "Это опять вы у подъезда визжали тормозами и орали «ROTATE» на 5-й передаче?! У меня сервант затрясся!",
                    timestampLabel = timeLabel,
                    isFromMe = false
                )
            )
        ),
        DirectThread(
            id = "dm_dispatcher",
            personName = "Диспетчер Слава",
            personRole = "Штаб • Разбор жалоб",
            avatarColorHex = 0xFF00A859L,
            unreadCount = 0,
            messages = listOf(
                DirectMessage(
                    id = 1004L,
                    text = "На тебя снова прилетела жалоба: «курьер ехал слишком стремительно, как Боинг на взлёте». Снижаем рейтинг профилактически.",
                    timestampLabel = timeLabel,
                    isFromMe = false
                )
            )
        ),
        DirectThread(
            id = "dm_concierge",
            personName = "Консьержка Зинаида",
            personRole = "Вахта • Грузовой лифт",
            avatarColorHex = 0xFF8E24AAL,
            unreadCount = 0,
            messages = listOf(
                DirectMessage(
                    id = 1005L,
                    text = "Опять зелёным рюкзаком все стены в холле протёрли! Вы мне за прошлый фикус ещё не ответили!",
                    timestampLabel = timeLabel,
                    isFromMe = false
                )
            )
        )
    )

    fun generateAngryDm(
        threadId: String,
        nextMsgId: Long,
        timeLabel: String,
        accumulatedBreakdowns: List<BikeBreakdownAlert> = emptyList(),
        recentOpponentTexts: Set<String> = emptySet()
    ): DirectMessage {
        if (accumulatedBreakdowns.isNotEmpty() && Random.nextInt(100) < 60) {
            val issue = accumulatedBreakdowns[Random.nextInt(accumulatedBreakdowns.size)]
            val breakdownPool = when (threadId) {
                "dm_neighbor" -> listOf(
                    "У вас уже ${accumulatedBreakdowns.size} поломки на велике («${issue.title}»)! Этот грохот слышен даже через закрытые стеклопакеты, чините немедленно!",
                    "Опять «${issue.title}» звенит на весь двор! У меня кот под диван залез и отказывается выходить!"
                )
                "dm_dispatcher" -> listOf(
                    "Диспетчерская видит ${accumulatedBreakdowns.size} нечиненых поломки! Клиенты жалуются на скрип и опоздания — режем выплаты до ремонта в гараже!",
                    "По телеметрии у тебя «${issue.title}». Рейтинг падает быстрее, чем ты доезжаешь до подъезда — зайди в Велик!"
                )
                "dm_concierge" -> listOf(
                    "Вы своим сломанным Trek весь холл маслом закапали («${issue.title}»)! Пока не почините — в подъезд пешком ходите!",
                    "У вас там «${issue.title}», а вы на этом драндулете прямо по моему чистому коврику проехали!"
                )
                "dm_antonina" -> listOf(
                    "Сначала шкаф криво поставили, а теперь ещё на полуразваленном велосипеде грохочете под окнами! Позорище!",
                    "До меня дошли слухи про ваш «${issue.title}»! На таком транспорте даже табуретку возить опасно!"
                )
                else -> listOf(
                    "Мой заказ растрясло из-за вашей поломки «${issue.title}»! Зайдите уже во вкладку Велик и почините корыто!",
                    "Я слышал, как ваш велик хрустит («${issue.title}») ещё с остановки. Неудивительно, что пылесос теперь чихает!"
                )
            }
            val picked = pickNonRepeating(breakdownPool, recentOpponentTexts)
            return DirectMessage(
                id = nextMsgId,
                text = picked,
                timestampLabel = timeLabel,
                isFromMe = false
            )
        }

        val pool = when (threadId) {
            "dm_antonina" -> listOf(
                "Я снова перемерила шкаф! Он точно сдвинулся, признавайтесь, вы его ночью двигали?!",
                "Почему дверца шкафа скрипит точно так же, как ваши тормоза на велосипеде?!",
                "Если в 23:00 шкаф не вернётся на место миллиметр в миллиметр — поставлю 1 звезду с пяти аккаунтов!",
                "Зять приехал, посмотрел на ваш угол постановки шкафа и молча ушёл пить валерьянку. Вы довольны?!",
                "От вашей зелёной куртки на полировке шкафа остался моральный след! Приезжайте с тряпкой!",
                "Я засекла секундомер: до ночного выноса шкафа осталось чуть-чуть. Только попробуйте опоздать!"
            )
            "dm_vacuum_guy" -> listOf(
                "Почему на чеке из DNS отпечаток вашей велосипедной цепи?! Вы пылесос на раме везли?!",
                "Он не сосёт пыль, он издаёт звуки взлетающего самолёта! Вы его сломали по дороге!",
                "Я видел ваш рейтинг в приложении — всё понятно, вы специально подсунули витринный образец!",
                "Алиса в этом пылесосе теперь говорит голосом диспетчера и просит 15₽ на лечение. Что вы с ним сделали?!",
                "Почему внутри пылесборника уже лежат три берёзовых листика?! Вы им тротуар по дороге убирали?!",
                "Я включил турбо-режим, и у меня засосало штору! Верните нормальную модель из магазина!"
            )
            "dm_neighbor" -> listOf(
                "Хватит гонять по тротуару с криками «V1! ROTATE!» — у нас тут не Внуково, а спальный район!",
                "Скрип ваших колодок слышно с третьего квартала! Купите уже нормальные тормоза, невозможно спать!",
                "Вы только что пронеслись мимо лавочки и чуть не сдули голубей! Я уже звоню в ЖЭК!",
                "У меня сервант с хрусталём вибрирует каждый раз, когда вы под окнами сабвуфер на велике включаете!",
                "Ваши следы от торможения на плитке похожи на взлётно-посадочную полосу! Участковый уже интересуется!",
                "Если ещё раз услышу этот металлический визг в тихий час — выйду с рулеткой и жалобой!"
            )
            "dm_dispatcher" -> listOf(
                "Клиент жалуется, что вы смотрели на пакет с пиццей слишком осуждающе. Объяснительную срочно!",
                "Система зафиксировала 5-ю передачу на пешеходном переходе. Вам +15₽ на лечение и -0.4★ к рейтингу.",
                "Почему вы опять везёте чей-то советский ковёр вместо шаурмы?! Кто разрешил?!",
                "В штаб пришло фото, где вы на весах DNS проверяете чужой пылесос. Это входит в тариф?",
                "Рейтинг держится на честном слове и изоленте. Не сбрасывай звонки клиентов — алгоритм всё пишет!",
                "Тут заказчик спрашивает, почему курьер пошёл стоять в МФЦ вместо доставки рафа. Ты там как?"
            )
            else -> listOf(
                "Я всё записала в журнал вахты! Следующий раз в лифт не пущу даже без велосипеда!",
                "От ваших тормозных колодок на плитке чёрные полосы остались! Кто отмывать будет, Пушкин?!",
                "Куда вы понесли чужую стиральную машину в 11 вечера?! Я участкового вызываю!",
                "Почему ваш зелёный рюкзак застрял между створками грузового лифта и распугал жильцов?!",
                "Вы опять занесли на этаж чей-то гигантский фикус без бахил! Весь пол в земле!",
                "Без паспорта и сменной обуви к домофону больше не подходите — я вас по спицам узнаю!"
            )
        }
        return DirectMessage(
            id = nextMsgId,
            text = pickNonRepeating(pool, recentOpponentTexts),
            timestampLabel = timeLabel,
            isFromMe = false
        )
    }

    fun generateAngryDmReply(
        threadId: String,
        userText: String,
        nextMsgId: Long,
        timeLabel: String,
        lastOpponentText: String = "",
        recentOpponentTexts: Set<String> = emptySet(),
        accumulatedBreakdowns: List<BikeBreakdownAlert> = emptyList(),
        currentGear: Int = 1,
        courierRating: Double = 2.0
    ): DirectMessage {
        val lower = userText.lowercase()
        val ctxLower = lastOpponentText.lowercase()

        val candidates: List<String> = when {
            // 1. Money / 15₽ compensation excuses (tailored per character!)
            "15" in lower || "успокои" in lower || "перев" in lower || "корвалол" in lower ||
                "компенс" in lower || "рубл" in lower || "денег" in lower -> when (threadId) {
                "dm_antonina" -> listOf(
                    "Какие 15₽?! На валерьянку и валидол после вашего шкафа уйдёт минимум 400! Но перевод я уже открыла, жду.",
                    "Вы думаете, бабку можно купить за 15 рублей?! Ладно, на пакетик чая хватит, но шкаф в 23:00 проверю лично!",
                    "15₽ — это на один ватный диск от стресса! За такие миллиметры меньше чем с доплатой не возвращайтесь!"
                )
                "dm_vacuum_guy" -> listOf(
                    "На 15₽ я даже фильтр HEPA не понюхаю! Хотя на антидепрессант коту как раз пойдёт.",
                    "Вот это щедрость — целых 15₽! Ладно, кота успокою, но отзыв про запах мазута уже в черновике.",
                    "За 15₽ пылесос лучше тянуть не стал, но за оперативность скидываю респект (и всё равно 1★)."
                )
                "dm_neighbor" -> listOf(
                    "За 15₽ я себе даже беруши из поролона не куплю! Но на таблетку от мигрени ладно, закидывай.",
                    "Думаешь, 15₽ заглушат твой визг колодок и сабвуфер?! Хотя для профилактики скидывай, посмотрю.",
                    "15₽ принял, в ЖЭК жалобу пока придержал. Но если ещё раз взлетишь под окном — докину к делу!"
                )
                "dm_dispatcher" -> listOf(
                    "Откуда у тебя лишние 15₽, если мы тебе за поломки выплату урезали?! Ладно, зачтём как аванс на лечение.",
                    "В штабе взятки по 15₽ не принимаются! Но на кофе в ночную смену я это, конечно, спишу.",
                    "Ха, «на лечение» он скинул! Алгоритм не подкупить — максимум на 0.1★ медленнее упадёшь."
                )
                else -> listOf(
                    "Мне ваши 15₽ к чаю как мёртвому припарка, когда весь холл в следах от шин! Хотя конфет куплю, ладно.",
                    "За 15₽ я только один коврик у лифта вытряхну! За второй отрабатывайте вежливостью!",
                    "Ой, ну хоть кто-то про вахту вспомнил! За 15₽ пущу на лифт, но только с вытертыми колёсами!"
                )
            }

            // 2. Wind / bumps / blaming physics
            "ветер" in lower || "сквозняк" in lower || "кочк" in lower || "пол крив" in lower ||
                "не я" in lower || "сам" in lower -> when (threadId) {
                "dm_antonina" -> listOf(
                    "Ага, сквозняк! Такой сильный, что дубовый шкаф 1979 года на 3 миллиметра развернуло?! Сказки зятю рассказывай!",
                    "Пол у меня ровный по уровню с советских времён! Это у вас глазомер на велосипеде сбился!",
                    "«Шкаф сам подвинулся» — ну да, и дверца сама скрипнула голосом ваших тормозов!"
                )
                "dm_vacuum_guy" -> listOf(
                    "Ветер задул мазут внутрь коробки пылесоса?! Вы хоть сами слышите, что несёте?!",
                    "В DNS пол ровный, это вы его по бордюрам везли как дрова на раме!",
                    "Ну конечно, пылесос сам заскочил на ваш Trek и разогнался до 5-й передачи!"
                )
                "dm_neighbor" -> listOf(
                    "Какой ветер в безветренный вторник?! Это вы с горки накатом несётесь без тормозов!",
                    "Не надо на кочки сваливать! У нас асфальт три года назад переложили, а визжите только вы!",
                    "«Это не я» — да во всём квартале только один зелёный терморюкзак так свистит!"
                )
                "dm_dispatcher" -> listOf(
                    "Все курьеры почему-то на ветер жалуются, а по GPS у тебя 42 км/ч по тротуару!",
                    "Отмазка «встречный ветер» в системе уже использована 14 раз за смену. Придумай поновее!",
                    "Если бы кочки сами так гремели, у нас бы весь район в гараж уехал!"
                )
                else -> listOf(
                    "Ветер вам колёса перед вахтой не вытрет! И шлагбаум сам перед вами не прогнётся!",
                    "Ой, не смешите мои тапочки! «Грузовой лифт сам позвал» — я сейчас кнопку вызова вообще отключу!",
                    "У нас сквозняк только в голове у тех, кто в холл с великом заезжает без стука!"
                )
            }

            // 3. Gearbox / Boeing V1 / ROTATE / speed
            "передач" in lower || "v1" in lower || "rotate" in lower || "боинг" in lower ||
                "взлёт" in lower || "взлет" in lower || "5" in lower -> when (threadId) {
                "dm_antonina" -> listOf(
                    "Какой ещё Боинг?! У меня тут гости чай пьют, а не аэропорт Шереметьево! Несите шкаф тихо!",
                    "Если вы на 5-й передаче будете шкаф заносить, я сама вам шасси подкручу!",
                    "Вот до чего ваши авиационные фантазии доводят — уже углы в комнате путаете!"
                )
                "dm_vacuum_guy" -> listOf(
                    "Теперь понятно, почему пылесос свистит как турбина — он заразился от вашей 5-й передачи!",
                    "Вы на нём «ROTATE» делали прямо через лежачий полицейский?! То-то шланг загудел!",
                    "Заберите свой авиационный юмор обратно и привезите гарантийный талон!"
                )
                "dm_neighbor" -> listOf(
                    "Ещё раз услышу «V1 — ROTATE» под моим балконом — я сам вам диспетчерскую вышку устрою!",
                    "Какая 5-я передача на тротуаре шириной полтора метра?! У меня пенсионеры в кусты отпрыгивают!",
                    "На Боинге хотя бы шасси тише скрипят, чем ваш Trek при торможении!"
                )
                "dm_dispatcher" -> listOf(
                    "Слушай, командир экипажа, пока у тебя рейтинг ${String.format(java.util.Locale.US, "%.1f★", courierRating)} (передача ${currentGear}), летай пониже к асфальту!",
                    "В инструкции курьера нет пункта «орать V1 при пересечении зебры». Внёс пометку в личное дело.",
                    "На 5-й передаче ты долетаешь быстрее, но заказы превращаются в омлет. Сбавь тягу!"
                )
                else -> listOf(
                    "Вот и катитесь на своём Боинге мимо нашего подъезда, пока я швабру не достала!",
                    "У меня в холле не взлётно-посадочная полоса! На 1-й передаче и шёпотом!",
                    "Слышу ваше «ROTATE» ещё от шлагбаума — значит, через минуту опять стены рюкзаком заденете!"
                )
            }

            // 4. Garage / repair / brakes / bike condition
            "гараж" in lower || "почин" in lower || "ремонт" in lower || "колод" in lower ||
                "тормоз" in lower || "велик" in lower || "цеп" in lower -> {
                if (accumulatedBreakdowns.isNotEmpty()) {
                    val issue = accumulatedBreakdowns.first()
                    when (threadId) {
                        "dm_neighbor" -> listOf(
                            "Вот и отлично, езжайте в гараж прямо сейчас! У вас «${issue.title}» уже весь двор будит!",
                            "Пока не увижу чек за ремонт «${issue.title}» — буду снимать каждый проезд на камеру!"
                        )
                        "dm_dispatcher" -> listOf(
                            "Давно пора! После ремонта «${issue.title}» вернём нормальный коэффициент на выплаты.",
                            "В гараже заодно проверь тормоза — у нас тут ещё 3 тикета про твой визг колодок."
                        )
                        "dm_concierge" -> listOf(
                            "Вот и хорошо! Почините свой «${issue.title}», помойте колёса, тогда и заходите как человек!",
                            "Да вы про этот гараж ещё с прошлой смены обещаете, а масло до сих пор на плитке!"
                        )
                        else -> listOf(
                            "Сначала реально почините «${issue.title}», а потом уже берите заказы на хрупкое!",
                            "Надеюсь, в гараже вам ещё и сабвуфер потише скрутят, а не только «${issue.title}» починят!"
                        )
                    }
                } else {
                    when (threadId) {
                        "dm_neighbor" -> listOf(
                            "Ого, неужели колодки сменили?! Ладно, проверю завтра утром с балкона на слух!",
                            "Если реально перестанете визжать у подъезда — лично отменю звонок в ЖЭК!"
                        )
                        "dm_dispatcher" -> listOf(
                            "Молодец, чистый велик — меньше поломок. Только рейтинг всё равно выше 3.4★ не жди.",
                            "За гараж лайк, но колодки долго на 5-й не живут — береги резину!"
                        )
                        else -> listOf(
                            "Ну слава богу, взялись за ум! Заедете после ремонта — проверю результат лично!",
                            "Вот это по-нашему! Только потом на мокрый асфальт не вылетайте сразу!"
                        )
                    }
                }
            }

            // 5. Apologies / polite promises
            "извин" in lower || "прост" in lower || "виноват" in lower || "обеща" in lower ||
                "больше не" in lower || "исправ" in lower || "аккурат" in lower -> when (threadId) {
                "dm_antonina" -> listOf(
                    "Извинения к делу не пришьёшь! Но раз признали ошибку — в 23:00 ждём вас на повторный замер шкафа.",
                    "Ну хоть воспитание есть... Ладно, поставлю 2 звезды вместо одной, если тапки наденете!"
                )
                "dm_vacuum_guy" -> listOf(
                    "Ладно, проехали, я сам в DNS злой был. Но если кот снова испугается — напишу в техподдержку!",
                    "Окей, принято. В следующий раз заказываю пылесос с проверкой при мне на пороге."
                )
                "dm_neighbor" -> listOf(
                    "Вот так бы сразу! Если по двору будете катить тихо — голуби вас простят, и я тоже.",
                    "Принято, соседский мир восстановлен. Только музыку под окнами не врубайте!"
                )
                "dm_dispatcher" -> listOf(
                    "Объяснительная принята и подшита. Штраф с 0.4★ снизил до 0.1★ — беги дальше на смену.",
                    "Уважуха за честность, но алгоритм всё равно злой. Держись там!"
                )
                else -> listOf(
                    "Ну вот, другое дело! Приятно слышать вежливого курьера — проходите, только рюкзак прижмите!",
                    "Ладно, на первый раз в журнал вахты записывать не буду. Доброй смены, сынок!"
                )
            }

            // 6. Specific domain topics (wardrobe, vacuum, cat, elevator, rating, order)
            "шкаф" in lower || "23:00" in lower || "мебел" in lower -> listOf(
                "Только попробуйте в 23:00 приехать без уровня и рулетки — я всё перепроверю!",
                "Тёща уже с секундомером у прихожей дежурит! Двигайте обратно с ювелирной точностью!",
                "Главное дверцу не поцарапайте о свой зелёный рюкзак, он у вас как наждачка!"
            )

            "пылесос" in lower || "dns" in lower || "тяг" in lower || "кот" in lower -> listOf(
                "Кот до сих пор сидит на карнизе и подозрительно смотрит на розетку! Это вы виноваты!",
                "Да, на чеке тянул, а дома начал урчать как дизельный генератор! Жду замену!",
                "Чек вымятый, коробка в смазке — в DNS над вами уже весь отдел бытовой техники смеётся!"
            )

            "лифт" in lower || "тряпк" in lower || "чист" in lower -> listOf(
                "Тряпку могу выдать под роспись на вахте! Вытираем колёса — потом едем на этаж!",
                "Грузовой лифт сегодня только по пропускам вежливости! Покажите чистые педали!",
                "И не забудьте придержать двери — на прошлом заказе весь холл в следах был!"
            )

            "рейтинг" in lower || "звезд" in lower || "алгоритм" in lower -> listOf(
                "Рейтинг 3.5★ — это миф, как и идеальные дороги! Смирись и крути педали дальше.",
                "У всех курьеров тут 1–2★, это знак качества нашей реальности!",
                "Даже если привезёшь золото, кто-нибудь поставит 1★ за слишком грустный взгляд!"
            )

            // 7. Short/generic user message reacting to what the opponent just said
            else -> {
                val contextPool = when {
                    "шкаф" in ctxLower || "миллиметр" in ctxLower -> listOf(
                        "И не надо отшучиваться! Я этот шкаф с 1979 года знаю — он так не стоял!",
                        "Линейка у меня всегда наготове! Жду отчёта по перестановке!",
                        "Ваши оправдания зять уже на диктофон записывает — готовьтесь!"
                    )
                    "пылесос" in ctxLower || "dns" in ctxLower || "кот" in ctxLower -> listOf(
                        "Кот подтверждает каждую букву моей жалобы! Пылесос явно пережил приключение по дороге!",
                        "Я уже звоню на горячую линию DNS — скажу, что курьер тестировал тягу на улице!",
                        "Ну-ну, посмотрим, что вы скажете, когда я приложу фото чека в мазуте!"
                    )
                    "скрип" in ctxLower || "колод" in ctxLower || "тормоз" in ctxLower -> listOf(
                        "У меня видеозапись с балкона со звуком есть! Там чистые 95 децибел при торможении!",
                        "Не спорьте, весь 4-й этаж подпрыгивает, когда вы к подъезду подкатываете!",
                        "Если не смените колодки — скинемся всем подъездом вам на вело-звонок потише!"
                    )
                    "лифт" in ctxLower || "холл" in ctxLower || "плитк" in ctxLower -> listOf(
                        "Я на вахте с 8 утра сижу и каждый ваш заезд в блокнотик заношу!",
                        "Тут вам не велотрек! Заходите тихо, рюкзаком стены не цепляйте!",
                        "Ещё одно слово — и ключ от грузового лифта уйдёт в отпуск до понедельника!"
                    )
                    else -> emptyList()
                }

                val personaPool = when (threadId) {
                    "dm_antonina" -> listOf(
                        "Оправдания не принимаются! Линейка не врёт — 2 миллиметра украли!",
                        "Вот только не надо со старшими спорить! У меня опыт расстановки мебели 40 лет!",
                        "Я уже накатала отзыв на 4 абзаца — жду хотя бы внятного объяснения!",
                        "Не переводите тему! Почему чай на столе задрожал, когда вы заходили в подъезд?",
                        "Смотрите у меня: ещё одна кривая доставка, и я напишу лично вашему диспетчеру Славе!"
                    )
                    "dm_vacuum_guy" -> listOf(
                        "Вот только не надо умничать! Возвращайтесь и проверяйте пылесос при мне ещё раз!",
                        "У меня всё зафиксировано: время доставки, звук мотора и грустный взгляд кота!",
                        "Я в отзывах так и напишу: «курьер харизматичный, но техника после него с характером»!",
                        "Слушайте, а почему от коробки пахнет не новым пластиком, а шаурмой с вокзала?!",
                        "Давайте без отговорок — или объясняйте, почему шланг свистит, или жду +15₽ компенсации!"
                    )
                    "dm_neighbor" -> listOf(
                        "Я всё фиксирую на видеорегистратор с балкона! Скрип тормозов приобщу к делу!",
                        "Вы меня в покое оставите сегодня или опять устроите форсаж под окнами?!",
                        "Соседка с 3-го этажа тоже уже жалуется — говорит, вы её герань потоком ветра сдули!",
                        "Можете не спорить, весь чат дома уже обсуждает ваш зелёный болид!",
                        "Короче, если сегодня вечером будет тихо — забудем. Если нет — звоню на линию жалоб!"
                    )
                    "dm_dispatcher" -> listOf(
                        "Ладно, тикет пока открыт, но алгоритм тебе рейтинг всё равно подрежет. Ничего личного.",
                        "Слушай, у меня таких сообщений от курьеров по сто штук в час. Крути педали дальше!",
                        "В общем, жалобу я зафиксировал. Хочешь реабилитироваться — почини велик и возьми пару халтурок.",
                        "Хорошая попытка оправдаться, но клиент уже прислал 1★ с припиской «на лечение».",
                        "Записал твой комментарий в карточку смены. Не нарушай скоростной режим у подъездов!"
                    )
                    else -> listOf(
                        "И нечего тут спорить со старшими! Вытирайте колёса перед входом!",
                        "У меня зоркий глаз, от камеры у лифта ещё ни один терморюкзак не ушёл!",
                        "Вы бы лучше за чистотой следили так, как сообщения строчите!",
                        "Ладно, посмотрим на ваше поведение на следующем заказе. Я всё вижу!",
                        "Никаких «случайно»! В подъезде должен быть порядок и тишина!"
                    )
                }

                contextPool + personaPool
            }
        }

        val chosen = pickNonRepeating(candidates, recentOpponentTexts)
        return DirectMessage(
            id = nextMsgId,
            text = chosen,
            timestampLabel = timeLabel,
            isFromMe = false
        )
    }

    fun quickDmExcusesForThread(threadId: String): List<String> = when (threadId) {
        "dm_antonina" -> listOf(
            "Верну шкаф ровно в 23:00",
            "Это не миллиметры, это кривой пол",
            "Перевёл 15₽ на корвалол",
            "Извините, больше на глаз не ставлю"
        )
        "dm_vacuum_guy" -> listOf(
            "В DNS на чеке проверял — тянул!",
            "Мазут — это заводская смазка",
            "Кот сам прыгнул под турбину",
            "Перевёл 15₽ на чистку кота"
        )
        "dm_neighbor" -> listOf(
            "Колодки прогревались перед посадкой",
            "Это не Боинг, это накат с горки",
            "Уже еду в гараж за ремонтом",
            "Перевёл 15₽ вам на беруши"
        )
        "dm_dispatcher" -> listOf(
            "Алгоритм завидует моему стилю",
            "Доставил быстрее, чем на Боинге V1",
            "Все поломки уже чиню в гараже",
            "Скинулся сам себе 15₽ на лечение"
        )
        else -> listOf(
            "Я протру колёса тряпкой, честно!",
            "Зелёный рюкзак — это новый тренд",
            "Несу вам 15₽ к чаю на вахту",
            "Грузовой лифт сам меня позвал"
        )
    }

    private fun pickNonRepeating(pool: List<String>, recentTexts: Set<String>): String {
        val fresh = pool.filterNot { recentTexts.contains(it) }
        val source = if (fresh.isNotEmpty()) fresh else pool
        return source[Random.nextInt(source.size)]
    }

    private data class BreakdownTemplate(
        val code: String,
        val title: String,
        val description: String,
        val workImpact: String,
        val attitudeImpact: String,
        val severityWear: Int,
        val repairCostRub: Int,
        val blocksHighGears: Boolean,
        val payPenaltyPercent: Int,
        val ratingPenaltyExtra: Double
    )

    private val breakdownTemplates = listOf(
        BreakdownTemplate(
            code = "smoke_pads",
            title = "Колодки дымятся и визжат!",
            description = "При торможении искры и дым летят в терморюкзак — весь двор оборачивается.",
            workImpact = "-12% к выплате за заказы",
            attitudeImpact = "-0.2★ за доставку • жалобы на дым",
            severityWear = 8,
            repairCostRub = 45,
            blocksHighGears = false,
            payPenaltyPercent = 12,
            ratingPenaltyExtra = 0.2
        ),
        BreakdownTemplate(
            code = "torn_rubber",
            title = "Оторвало тормозную резинку",
            description = "Приходится подтормаживать левым кроссовком об асфальт.",
            workImpact = "-15% к выплате • ускоренный износ",
            attitudeImpact = "-0.3★ • консьержка ругается на следы",
            severityWear = 10,
            repairCostRub = 60,
            blocksHighGears = false,
            payPenaltyPercent = 15,
            ratingPenaltyExtra = 0.3
        ),
        BreakdownTemplate(
            code = "chain_drop",
            title = "Слетела и перекосилась цепь",
            description = "После агрессивного манёвра V1 / ROTATE цепь клинит на больших звёздах.",
            workImpact = "4–5 передача заблокированы • -15% оплата",
            attitudeImpact = "-0.2★ • мазут на руках и пакетах",
            severityWear = 6,
            repairCostRub = 40,
            blocksHighGears = true,
            payPenaltyPercent = 15,
            ratingPenaltyExtra = 0.2
        ),
        BreakdownTemplate(
            code = "loose_pedal",
            title = "Открутилась правая педаль",
            description = "Резьба сорвана — крутить педали можно только вполсилы, без разгона.",
            workImpact = "-10% к выплате за заказы",
            attitudeImpact = "-0.2★ • клиенты ждут дольше",
            severityWear = 6,
            repairCostRub = 35,
            blocksHighGears = false,
            payPenaltyPercent = 10,
            ratingPenaltyExtra = 0.2
        ),
        BreakdownTemplate(
            code = "wheel_eight",
            title = "Восьмёрка на заднем колесе",
            description = "Бордюр оказался выше, чем выглядел. Колесо чиркает по колодке и трясёт заказы.",
            workImpact = "-14% к выплате за заказы",
            attitudeImpact = "-0.3★ • пицца приезжает кашей",
            severityWear = 7,
            repairCostRub = 55,
            blocksHighGears = false,
            payPenaltyPercent = 14,
            ratingPenaltyExtra = 0.3
        ),
        BreakdownTemplate(
            code = "bent_derailleur",
            title = "Погнулся петух переключателя",
            description = "Трансмиссия трещит и сбрасывает передачи выше 3-й.",
            workImpact = "4–5 передача заблокированы • -12% оплата",
            attitudeImpact = "-0.2★ • треск пугает голубей",
            severityWear = 5,
            repairCostRub = 50,
            blocksHighGears = true,
            payPenaltyPercent = 12,
            ratingPenaltyExtra = 0.2
        ),
        BreakdownTemplate(
            code = "flat_tire",
            title = "Спустило переднее колесо на Trek",
            description = "Саморез около подъезда. Ехать тяжело, обод стучит по плитке.",
            workImpact = "-14% к выплате за заказы",
            attitudeImpact = "-0.2★ • грохот во дворе",
            severityWear = 6,
            repairCostRub = 40,
            blocksHighGears = false,
            payPenaltyPercent = 14,
            ratingPenaltyExtra = 0.2
        )
    )

    fun generateBikeBreakdown(
        id: Long,
        currentGear: Int,
        brakeHealthPercent: Int,
        existingCodes: Set<String> = emptySet(),
        timeLabel: String = ""
    ): BikeBreakdownAlert {
        val unpicked = breakdownTemplates.filterNot { existingCodes.contains(it.code) }
        val pool = if (unpicked.isNotEmpty()) unpicked else breakdownTemplates
        val preferred = when {
            brakeHealthPercent < 35 -> pool.filter { it.code == "smoke_pads" || it.code == "torn_rubber" }
            currentGear >= 4 -> pool.filter { it.code == "chain_drop" || it.code == "bent_derailleur" }
            else -> emptyList()
        }
        val chosen = if (preferred.isNotEmpty()) {
            preferred[Random.nextInt(preferred.size)]
        } else {
            pool[Random.nextInt(pool.size)]
        }
        return BikeBreakdownAlert(
            id = id,
            code = chosen.code,
            title = chosen.title,
            description = chosen.description,
            workImpact = chosen.workImpact,
            attitudeImpact = chosen.attitudeImpact,
            severityWear = chosen.severityWear,
            repairCostRub = chosen.repairCostRub,
            blocksHighGears = chosen.blocksHighGears,
            payPenaltyPercent = chosen.payPenaltyPercent,
            ratingPenaltyExtra = chosen.ratingPenaltyExtra,
            detectedAtLabel = timeLabel
        )
    }

    fun serializeBreakdowns(list: List<BikeBreakdownAlert>): String {
        return list.joinToString(separator = ";") { "${it.id}|${it.code}|${it.detectedAtLabel}" }
    }

    fun deserializeBreakdowns(raw: String): List<BikeBreakdownAlert> {
        if (raw.isBlank()) return emptyList()
        return raw.split(";").mapNotNull { token ->
            val parts = token.split("|")
            if (parts.size < 2) return@mapNotNull null
            val id = parts[0].toLongOrNull() ?: return@mapNotNull null
            val code = parts[1]
            val time = parts.getOrNull(2).orEmpty()
            val tpl = breakdownTemplates.firstOrNull { it.code == code } ?: return@mapNotNull null
            BikeBreakdownAlert(
                id = id,
                code = tpl.code,
                title = tpl.title,
                description = tpl.description,
                workImpact = tpl.workImpact,
                attitudeImpact = tpl.attitudeImpact,
                severityWear = tpl.severityWear,
                repairCostRub = tpl.repairCostRub,
                blocksHighGears = tpl.blocksHighGears,
                payPenaltyPercent = tpl.payPenaltyPercent,
                ratingPenaltyExtra = tpl.ratingPenaltyExtra,
                detectedAtLabel = time
            )
        }
    }

    fun generateComplaintCall(
        id: Long,
        orderTitle: String?,
        accumulatedBreakdowns: List<BikeBreakdownAlert> = emptyList(),
        currentGear: Int = 1
    ): IncomingComplaintCall = CallStoryEngine.generateComplaintCall(
        id = id,
        orderTitle = orderTitle,
        accumulatedBreakdowns = accumulatedBreakdowns,
        currentGear = currentGear
    )

    fun advanceComplaintCall(
        call: IncomingComplaintCall,
        chosenOption: CallResponseOption,
        accumulatedBreakdowns: List<BikeBreakdownAlert> = emptyList(),
        currentGear: Int = 1
    ): CallTurnResolution = CallStoryEngine.advanceCallTurn(
        call = call,
        chosenOption = chosenOption,
        accumulatedBreakdowns = accumulatedBreakdowns,
        currentGear = currentGear
    )

    fun initialCallHistory(timeLabel: String): List<CallHistoryEntry> =
        CallStoryEngine.initialCallHistory(timeLabel)
}
