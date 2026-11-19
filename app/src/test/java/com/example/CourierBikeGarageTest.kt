package com.example

import com.example.data.CallStatus
import com.example.data.CourierUiState
import com.example.data.GarageTrack
import com.example.data.InfiniteGigGenerator
import com.example.util.GarageMusicSynth
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs

class CourierBikeGarageTest {

    @Test
    fun `accumulated breakdowns reduce pay, block high gears, and worsen attitude`() {
        val issue1 = InfiniteGigGenerator.generateBikeBreakdown(
            id = 1L,
            currentGear = 5,
            brakeHealthPercent = 50,
            existingCodes = emptySet(),
            timeLabel = "12:00"
        )
        val issue2 = InfiniteGigGenerator.generateBikeBreakdown(
            id = 2L,
            currentGear = 2,
            brakeHealthPercent = 25,
            existingCodes = setOf(issue1.code),
            timeLabel = "12:05"
        )
        val issue3 = InfiniteGigGenerator.generateBikeBreakdown(
            id = 3L,
            currentGear = 3,
            brakeHealthPercent = 20,
            existingCodes = setOf(issue1.code, issue2.code),
            timeLabel = "12:10"
        )

        val cleanState = CourierUiState()
        assertFalse(cleanState.highGearsBlocked)
        assertEquals(1.0, cleanState.effectivePayMultiplier, 0.001)

        val brokenState = CourierUiState(
            accumulatedBreakdowns = listOf(issue1, issue2, issue3)
        )
        assertTrue(brokenState.highGearsBlocked)
        assertTrue(brokenState.totalPayPenaltyPercent > 0)
        assertTrue(brokenState.effectivePayMultiplier < 1.0)
        assertTrue(brokenState.publicAttitudeLabel.contains("Косятся") || brokenState.publicAttitudeLabel.contains("Враг"))
        assertTrue(brokenState.totalRepairAllCostRub < listOf(issue1, issue2, issue3).sumOf { it.repairCostRub })
    }

    @Test
    fun `breakdowns serialize and deserialize cleanly`() {
        val b1 = InfiniteGigGenerator.generateBikeBreakdown(10L, 4, 40, emptySet(), "14:10")
        val b2 = InfiniteGigGenerator.generateBikeBreakdown(11L, 1, 20, setOf(b1.code), "14:15")
        val raw = InfiniteGigGenerator.serializeBreakdowns(listOf(b1, b2))
        val restored = InfiniteGigGenerator.deserializeBreakdowns(raw)
        assertEquals(2, restored.size)
        assertEquals(b1.code, restored[0].code)
        assertEquals(b2.code, restored[1].code)
        assertEquals("14:15", restored[1].detectedAtLabel)
    }

    @Test
    fun `equipped bike tuning mods boost payout multiplier and style`() {
        val moddedState = CourierUiState(
            ownedBikeModIds = setOf("mod_neon", "mod_spoiler", "mod_boeing_exhaust"),
            equippedBikeModIds = setOf("mod_neon", "mod_spoiler", "mod_boeing_exhaust")
        )
        assertTrue(moddedState.totalModPayBonusPercent >= 48)
        assertTrue(moddedState.effectivePayMultiplier > 1.4)
        assertTrue(moddedState.totalBikeStyleScore >= 90)
    }

    @Test
    fun `garage tracks cycle and synthesize distinct multi-bar audio`() {
        assertEquals(4, GarageTrack.entries.size)
        assertEquals(GarageTrack.DRIFT_PHONK, GarageTrack.GET_LOW_CRUNK.next())
        assertEquals(GarageTrack.BOEING_DNB, GarageTrack.GET_LOW_CRUNK.previous())
        assertEquals(GarageTrack.MIDNIGHT_SYNTHWAVE, GarageTrack.fromId("synthwave"))

        val loops = GarageTrack.entries.map { track ->
            val pcm = GarageMusicSynth.buildTrackLoop(track, sampleRate = 8000)
            val peak = pcm.maxOf { abs(it.toInt()) }
            assertTrue("Track ${track.id} should have strong audible signal", peak > 4000)
            pcm
        }
        assertNotEquals(loops[0].size, loops[1].size)
        assertNotEquals(loops[1].size, loops[2].size)
        assertNotEquals(loops[2].size, loops[3].size)
    }

    @Test
    fun `complaint calls generate unrepeatable storylines and escalate across multiple turns until client snaps`() {
        // Sequential calls cycle distinct plotIds without immediate repeats
        val generatedCalls = (1L..8L).map { idx ->
            InfiniteGigGenerator.generateComplaintCall(
                id = idx,
                orderTitle = "Купить пылесос и проверить модель",
                accumulatedBreakdowns = emptyList(),
                currentGear = 5
            )
        }
        val plotIds = generatedCalls.map { it.plotId }
        assertEquals(8, plotIds.distinct().size)

        var currentCall = generatedCalls.first().copy(status = CallStatus.ACTIVE)
        assertEquals(CallStatus.ACTIVE, currentCall.status)
        assertTrue(currentCall.callerName.isNotBlank())
        assertTrue(currentCall.openingComplaint.isNotBlank())
        assertEquals(3, currentCall.responseOptions.size)
        assertEquals(1, currentCall.dialogueHistory.size)

        val seenOptionIds = mutableSetOf<String>()
        val seenOptionLabels = mutableSetOf<String>()
        var turnSteps = 0
        var snapped = false

        while (!snapped && turnSteps < 8) {
            val picked = currentCall.responseOptions.first()
            assertFalse("Option id should not repeat within same call", seenOptionIds.contains(picked.id))
            assertFalse("Option label should not repeat within same call", seenOptionLabels.contains(picked.label))
            currentCall.responseOptions.forEach {
                seenOptionIds.add(it.id)
                seenOptionLabels.add(it.label)
            }

            val resolution = InfiniteGigGenerator.advanceComplaintCall(
                call = currentCall,
                chosenOption = picked,
                accumulatedBreakdowns = emptyList(),
                currentGear = 5
            )
            turnSteps++
            if (turnSteps == 1) {
                assertFalse("Client must continue talking after first reply", resolution.didClientSnap)
                assertEquals(CallStatus.ACTIVE, resolution.updatedCall.status)
                assertEquals(3, resolution.updatedCall.responseOptions.size)
            }
            snapped = resolution.didClientSnap
            currentCall = resolution.updatedCall
        }

        assertTrue("Client should eventually lose patience and snap", snapped)
        assertTrue("Call should last at least 2 turns before snap", turnSteps >= 2)
        assertEquals(CallStatus.ENDED, currentCall.status)
        assertTrue(currentCall.droppedByClient)
        assertEquals(100, currentCall.clientAngerPercent)
        assertTrue(currentCall.dialogueHistory.size >= 5)
    }

    @Test
    fun `dm replies are varied, character-appropriate, and avoid immediate repetition`() {
        val threadIds = listOf(
            "dm_antonina",
            "dm_vacuum_guy",
            "dm_neighbor",
            "dm_dispatcher",
            "dm_concierge"
        )

        // Distinct quick excuses per thread
        val excuseSets = threadIds.map { InfiniteGigGenerator.quickDmExcusesForThread(it) }
        assertTrue(excuseSets.all { it.size == 4 && it.distinct().size == 4 })
        assertNotEquals(excuseSets[0], excuseSets[1])
        assertNotEquals(excuseSets[2], excuseSets[4])

        // Replies to the same compensation prompt should differ across characters
        val compReplies = threadIds.map { id ->
            InfiniteGigGenerator.generateAngryDmReply(
                threadId = id,
                userText = "Перевёл 15₽ на успокоительное",
                nextMsgId = 100L,
                timeLabel = "15:00"
            ).text
        }
        assertEquals(threadIds.size, compReplies.distinct().size)

        // Sequential replies avoid repeating recentOpponentTexts
        val seen = mutableSetOf<String>()
        repeat(4) { idx ->
            val msg = InfiniteGigGenerator.generateAngryDmReply(
                threadId = "dm_neighbor",
                userText = "ну ок",
                nextMsgId = 200L + idx,
                timeLabel = "15:0$idx",
                lastOpponentText = "Скрип ваших колодок слышно с третьего квартала!",
                recentOpponentTexts = seen
            )
            assertFalse("Reply should not repeat until pool exhausted: ${msg.text}", seen.contains(msg.text))
            seen.add(msg.text)
        }
    }
}
