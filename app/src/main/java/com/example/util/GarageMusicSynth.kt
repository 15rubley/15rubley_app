package com.example.util

import com.example.data.GarageTrack
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin
import kotlin.math.tanh
import kotlin.random.Random

/**
 * Multi-genre 8-bar evolving PCM synthesizer for the Bike Tuning Garage boombox.
 * Each track features A/B sections, fills, harmonic progressions, and analog-style soft saturation.
 */
object GarageMusicSynth {

    fun buildTrackLoop(track: GarageTrack, sampleRate: Int = 22050): ShortArray {
        return when (track) {
            GarageTrack.GET_LOW_CRUNK -> buildGetLowCrunk(sampleRate)
            GarageTrack.DRIFT_PHONK -> buildDriftPhonk(sampleRate)
            GarageTrack.MIDNIGHT_SYNTHWAVE -> buildMidnightSynthwave(sampleRate)
            GarageTrack.BOEING_DNB -> buildBoeingDnb(sampleRate)
        }
    }

    private fun buildGetLowCrunk(sampleRate: Int): ShortArray {
        // 8 bars at 101 BPM = 128 sixteenth-note steps
        val stepSamples = (sampleRate * (60.0 / 101.0 / 4.0)).toInt()
        val totalSteps = 128
        val mix = DoubleArray(stepSamples * totalSteps)
        val rng = Random(101)

        val baseKickOffsets = setOf(0, 3, 6, 10)
        val bassRoots = doubleArrayOf(41.20, 41.20, 43.65, 41.20, 41.20, 43.65, 49.00, 38.89) // E1, F1, G1, D#1

        val barRiffA = listOf(0 to (329.63 to 3), 3 to (349.23 to 2), 6 to (329.63 to 2), 8 to (293.66 to 3), 12 to (329.63 to 2), 14 to (349.23 to 2))
        val barRiffB = listOf(0 to (329.63 to 3), 3 to (392.00 to 2), 6 to (349.23 to 2), 8 to (329.63 to 3), 12 to (293.66 to 2), 14 to (246.94 to 2))

        for (bar in 0 until 8) {
            val barStartStep = bar * 16
            val root = bassRoots[bar]
            val isBSection = bar >= 4
            val isFillBar = bar == 3 || bar == 7

            // 808 Kicks & Sub-Bass
            for (localStep in 0 until 16) {
                val step = barStartStep + localStep
                if (localStep in baseKickOffsets || (isBSection && localStep == 14)) {
                    val f = if (localStep == 3 || localStep == 14) root * 1.06 else root
                    add808Kick(mix, step * stepSamples, sampleRate, f, durSec = 0.38, gain = 0.48)
                }

                // Claps on 2 & 4 + extra turnaround clap on fill bar
                if (localStep == 4 || localStep == 12 || (isFillBar && localStep in setOf(14, 15))) {
                    addCrunkClap(mix, step * stepSamples, sampleRate, rng, gain = 0.28)
                }

                // Crisp hats with double-time 32nd rolls on B-section & fills
                if (localStep % 2 == 0 || (isBSection && localStep in setOf(7, 11, 13, 15))) {
                    val accent = if (localStep % 4 == 0) 1.0 else 0.62
                    addHiHat(mix, step * stepSamples, sampleRate, rng, durSec = 0.042, gain = 0.11 * accent)
                }
                if (isFillBar && localStep >= 12) {
                    addHiHat(mix, step * stepSamples + stepSamples / 2, sampleRate, rng, durSec = 0.03, gain = 0.09)
                }
            }

            // Phrygian Crunk Lead (A/B alternation + octave layer in bars 5..8)
            val riff = if (bar % 2 == 0) barRiffA else barRiffB
            for ((localStep, note) in riff) {
                val (freq, lenSteps) = note
                val step = barStartStep + localStep
                addCrunkLead(mix, step * stepSamples, sampleRate, freq, lenSteps * stepSamples * 0.9, gain = 0.17)
                if (isBSection) {
                    addCrunkLead(mix, step * stepSamples, sampleRate, freq * 2.0, lenSteps * stepSamples * 0.65, gain = 0.07)
                }
            }
        }

        // Subtle riser into bar 5 and bar 8
        addNoiseRiser(mix, 56 * stepSamples, 8 * stepSamples, sampleRate, rng, gain = 0.10)
        addNoiseRiser(mix, 120 * stepSamples, 8 * stepSamples, sampleRate, rng, gain = 0.12)

        return finalizeMix(mix, sampleRate)
    }

    private fun buildDriftPhonk(sampleRate: Int): ShortArray {
        // 8 bars at 126 BPM = 128 sixteenth-note steps
        val stepSamples = (sampleRate * (60.0 / 126.0 / 4.0)).toInt()
        val totalSteps = 128
        val mix = DoubleArray(stepSamples * totalSteps)
        val rng = Random(808)

        // C# minor / Phrygian Memphis Phonk cowbell pitches
        val cowbellA = listOf(
            0 to 554.37, 2 to 554.37, 4 to 659.25, 6 to 622.25,
            8 to 554.37, 10 to 493.88, 12 to 554.37, 14 to 415.30
        )
        val cowbellB = listOf(
            0 to 659.25, 2 to 739.99, 4 to 830.61, 6 to 739.99,
            8 to 659.25, 10 to 622.25, 12 to 554.37, 14 to 493.88
        )
        val bassRoots = doubleArrayOf(34.65, 34.65, 27.50, 30.87, 34.65, 41.20, 27.50, 30.87) // C#1, A0, B0, E1

        for (bar in 0 until 8) {
            val barStartStep = bar * 16
            val root = bassRoots[bar]
            val isDrop = bar != 4 // Brief breather drop at bar 5 start
            val isFill = bar == 3 || bar == 7

            for (localStep in 0 until 16) {
                val step = barStartStep + localStep
                val sampleIdx = step * stepSamples

                // Saturated Memphis 808 kick
                if (isDrop && localStep in setOf(0, 6, 10, 14)) {
                    add808Kick(mix, sampleIdx, sampleRate, root, durSec = 0.34, gain = 0.54, drive = 2.2)
                } else if (!isDrop && localStep in setOf(0, 10)) {
                    add808Kick(mix, sampleIdx, sampleRate, root, durSec = 0.42, gain = 0.52, drive = 2.2)
                }

                // Punchy Phonk Snare on 4 & 12 (+ fill)
                if (localStep == 4 || localStep == 12 || (isFill && localStep in setOf(14, 15))) {
                    addPunchySnare(mix, sampleIdx, sampleRate, rng, toneHz = 210.0, durSec = 0.14, gain = 0.30)
                }

                // Crisp rapid hi-hats
                if (localStep % 2 == 0 || localStep in setOf(3, 11, 15)) {
                    addHiHat(mix, sampleIdx, sampleRate, rng, durSec = 0.036, gain = 0.10)
                }
                if (bar >= 4 && localStep in setOf(7, 13)) {
                    addHiHat(mix, sampleIdx + stepSamples / 2, sampleRate, rng, durSec = 0.025, gain = 0.08)
                }
            }

            // Iconic 808 Cowbell Lead
            val melody = if (bar >= 4 && bar % 2 == 1) cowbellB else cowbellA
            for ((localStep, freq) in melody) {
                val step = barStartStep + localStep
                add808Cowbell(mix, step * stepSamples, sampleRate, freq, durSec = 0.21, gain = 0.21)
                if (bar >= 5 && localStep % 4 == 0) {
                    // Subtle lower octave cowbell shadow
                    add808Cowbell(mix, step * stepSamples, sampleRate, freq * 0.5, durSec = 0.18, gain = 0.09)
                }
            }
        }

        addNoiseRiser(mix, 118 * stepSamples, 10 * stepSamples, sampleRate, rng, gain = 0.11)
        return finalizeMix(mix, sampleRate)
    }

    private fun buildMidnightSynthwave(sampleRate: Int): ShortArray {
        // 8 bars at 116 BPM = 128 sixteenth-note steps
        val stepSamples = (sampleRate * (60.0 / 116.0 / 4.0)).toInt()
        val totalSteps = 128
        val mix = DoubleArray(stepSamples * totalSteps)
        val rng = Random(1984)

        // Classic Outrun chord progression: Am -> F -> C -> G (repeated twice with B-section uplift)
        val bassRoots = doubleArrayOf(
            55.00, 43.65, 65.41, 49.00, // A1, F1, C2, G1
            55.00, 43.65, 65.41, 49.00
        )
        val arpChords = listOf(
            doubleArrayOf(220.0, 261.63, 329.63, 440.0), // Am
            doubleArrayOf(174.61, 220.0, 261.63, 349.23), // F
            doubleArrayOf(261.63, 329.63, 392.00, 523.25), // C
            doubleArrayOf(196.00, 246.94, 293.66, 392.00)  // G
        )

        for (bar in 0 until 8) {
            val barStartStep = bar * 16
            val root = bassRoots[bar]
            val chord = arpChords[bar % 4]
            val isBSection = bar >= 4

            // Warm analog pad chord on bar downbeat
            addWarmPadChord(
                mix = mix,
                startIdx = barStartStep * stepSamples,
                sampleRate = sampleRate,
                freqs = chord,
                durSamples = 15 * stepSamples,
                gain = if (isBSection) 0.12 else 0.09
            )

            for (localStep in 0 until 16) {
                val step = barStartStep + localStep
                val sampleIdx = step * stepSamples

                // Four-on-the-floor Outrun kick
                if (localStep % 4 == 0) {
                    add808Kick(mix, sampleIdx, sampleRate, root, durSec = 0.24, gain = 0.42, drive = 1.3)
                }

                // Gated 80s snare on 4 & 12
                if (localStep == 4 || localStep == 12 || (bar == 7 && localStep in setOf(14, 15))) {
                    addGatedSnare(mix, sampleIdx, sampleRate, rng, gain = 0.27)
                }

                // Offbeat open/closed hat shimmer
                if (localStep % 2 == 1) {
                    val dur = if (localStep % 4 == 2) 0.07 else 0.04
                    addHiHat(mix, sampleIdx, sampleRate, rng, durSec = dur, gain = 0.085)
                }

                // Rolling 16th-note analog saw bassline (octave jump on offbeats)
                val bassFreq = if (localStep % 4 == 2) root * 2.0 else root
                addAnalogBassStep(mix, sampleIdx, sampleRate, bassFreq, durSamples = (stepSamples * 0.88).toInt(), gain = 0.23)

                // Shimmering 16th-note arpeggio
                val noteFreq = chord[localStep % chord.size] * (if (isBSection && localStep >= 8) 2.0 else 1.0)
                if (localStep % 2 == 0 || isBSection) {
                    addSynthPluck(mix, sampleIdx, sampleRate, noteFreq, durSec = 0.16, gain = 0.13)
                }
            }
        }

        return finalizeMix(mix, sampleRate)
    }

    private fun buildBoeingDnb(sampleRate: Int): ShortArray {
        // 8 bars at 168 BPM = 128 sixteenth-note steps (fast energetic Liquid DnB)
        val stepSamples = (sampleRate * (60.0 / 168.0 / 4.0)).toInt()
        val totalSteps = 128
        val mix = DoubleArray(stepSamples * totalSteps)
        val rng = Random(747)

        // Fm -> Db -> Ab -> Eb progression
        val bassRoots = doubleArrayOf(
            43.65, 34.65, 51.91, 38.89,
            43.65, 34.65, 51.91, 38.89
        )
        val chords = listOf(
            doubleArrayOf(174.61, 207.65, 261.63, 311.13), // Fm7
            doubleArrayOf(138.59, 174.61, 207.65, 261.63), // DbMaj7
            doubleArrayOf(207.65, 261.63, 311.13, 392.00), // AbMaj7
            doubleArrayOf(155.56, 196.00, 233.08, 293.66)  // Eb6
        )

        for (bar in 0 until 8) {
            val barStartStep = bar * 16
            val root = bassRoots[bar]
            val chord = chords[bar % 4]
            val isBSection = bar >= 4

            // Reese sub-bass rolling for the bar
            addReeseBass(
                mix = mix,
                startIdx = barStartStep * stepSamples,
                sampleRate = sampleRate,
                freq = root,
                durSamples = 16 * stepSamples,
                gain = 0.30
            )

            // Liquid chord stabs (syncopated)
            for (stabStep in listOf(0, 6, 10)) {
                addWarmPadChord(
                    mix = mix,
                    startIdx = (barStartStep + stabStep) * stepSamples,
                    sampleRate = sampleRate,
                    freqs = chord,
                    durSamples = (if (stabStep == 10) 5 else 4) * stepSamples,
                    gain = if (isBSection) 0.13 else 0.10
                )
            }

            // Two-step / Amen breakbeat drum pattern
            for (localStep in 0 until 16) {
                val step = barStartStep + localStep
                val sampleIdx = step * stepSamples

                if (localStep == 0 || localStep == 10 || (bar % 2 == 1 && localStep == 6)) {
                    add808Kick(mix, sampleIdx, sampleRate, root * 1.35, durSec = 0.16, gain = 0.44, drive = 1.4)
                }

                // Crisp main DnB snare on 4 & 12 + ghost snares on 7 & 9
                when (localStep) {
                    4, 12 -> addPunchySnare(mix, sampleIdx, sampleRate, rng, toneHz = 235.0, durSec = 0.11, gain = 0.30)
                    7, 9 -> addPunchySnare(mix, sampleIdx, sampleRate, rng, toneHz = 250.0, durSec = 0.06, gain = 0.11)
                }

                // Fast liquid ride/hat pulse
                val hatGain = if (localStep % 2 == 0) 0.095 else 0.055
                addHiHat(mix, sampleIdx, sampleRate, rng, durSec = 0.032, gain = hatGain)
            }

            // Aviation-inspired synth lead motif in B-section
            if (isBSection) {
                val motif = listOf(0 to chord[3], 3 to chord[2], 6 to chord[1], 10 to chord[2], 14 to chord[3] * 1.5)
                for ((localStep, f) in motif) {
                    addSynthPluck(mix, (barStartStep + localStep) * stepSamples, sampleRate, f, durSec = 0.18, gain = 0.12)
                }
            }
        }

        // Jet-turbine sweep on bar 8 turnaround
        addNoiseRiser(mix, 118 * stepSamples, 10 * stepSamples, sampleRate, rng, gain = 0.11)
        return finalizeMix(mix, sampleRate)
    }

    // --- Shared Procedural DSP Building Blocks ---

    private fun add808Kick(
        mix: DoubleArray,
        startIdx: Int,
        sampleRate: Int,
        baseFreq: Double,
        durSec: Double,
        gain: Double,
        drive: Double = 1.5
    ) {
        val dur = (sampleRate * durSec).toInt()
        for (i in 0 until dur) {
            val idx = startIdx + i
            if (idx >= mix.size) break
            val t = i.toDouble() / sampleRate
            val pitch = 118.0 * exp(-t * 44.0) + baseFreq
            val env = exp(-t * 6.2)
            val raw = sin(2.0 * PI * pitch * t) * 0.78 + sin(4.0 * PI * pitch * t) * 0.16
            val shaped = tanh(raw * drive) / tanh(drive)
            mix[idx] += shaped * env * gain
        }
    }

    private fun addCrunkClap(
        mix: DoubleArray,
        startIdx: Int,
        sampleRate: Int,
        rng: Random,
        gain: Double
    ) {
        val dur = (sampleRate * 0.15).toInt()
        for (i in 0 until dur) {
            val idx = startIdx + i
            if (idx >= mix.size) break
            val t = i.toDouble() / sampleRate
            val noise = rng.nextDouble() * 2.0 - 1.0
            val slapEnv = exp(-t * 28.0) + 0.45 * exp(-((t - 0.015).coerceAtLeast(0.0)) * 34.0)
            val body = sin(2.0 * PI * 195.0 * t) * exp(-t * 32.0) * 0.42
            mix[idx] += (noise * 0.56 * slapEnv + body) * gain
        }
    }

    private fun addPunchySnare(
        mix: DoubleArray,
        startIdx: Int,
        sampleRate: Int,
        rng: Random,
        toneHz: Double,
        durSec: Double,
        gain: Double
    ) {
        val dur = (sampleRate * durSec).toInt()
        for (i in 0 until dur) {
            val idx = startIdx + i
            if (idx >= mix.size) break
            val t = i.toDouble() / sampleRate
            val body = sin(2.0 * PI * (toneHz - t * 380.0) * t) * exp(-t * 24.0)
            val noise = (rng.nextDouble() * 2.0 - 1.0) * exp(-t * 18.0)
            mix[idx] += (body * 0.55 + noise * 0.50) * gain
        }
    }

    private fun addGatedSnare(
        mix: DoubleArray,
        startIdx: Int,
        sampleRate: Int,
        rng: Random,
        gain: Double
    ) {
        val dur = (sampleRate * 0.19).toInt()
        for (i in 0 until dur) {
            val idx = startIdx + i
            if (idx >= mix.size) break
            val t = i.toDouble() / sampleRate
            val gate = if (t < 0.14) 1.0 else exp(-(t - 0.14) * 55.0)
            val body = sin(2.0 * PI * 180.0 * t) * exp(-t * 18.0)
            val noise = (rng.nextDouble() * 2.0 - 1.0) * 0.65 * gate
            mix[idx] += (body * 0.48 + noise * 0.58) * gain
        }
    }

    private fun addHiHat(
        mix: DoubleArray,
        startIdx: Int,
        sampleRate: Int,
        rng: Random,
        durSec: Double,
        gain: Double
    ) {
        val dur = (sampleRate * durSec).toInt()
        var prev = 0.0
        for (i in 0 until dur) {
            val idx = startIdx + i
            if (idx >= mix.size) break
            val t = i.toDouble() / sampleRate
            val raw = rng.nextDouble() * 2.0 - 1.0
            val hp = raw - prev
            prev = raw * 0.85
            mix[idx] += hp * exp(-t * (3.8 / durSec)) * gain
        }
    }

    private fun addCrunkLead(
        mix: DoubleArray,
        startIdx: Int,
        sampleRate: Int,
        freq: Double,
        durSamplesDouble: Double,
        gain: Double
    ) {
        val dur = durSamplesDouble.toInt()
        for (i in 0 until dur) {
            val idx = startIdx + i
            if (idx >= mix.size) break
            val t = i.toDouble() / sampleRate
            val env = (1.0 - exp(-t * 95.0)) * exp(-t * 4.8)
            val osc1 = sin(2.0 * PI * freq * t)
            val osc2 = sin(2.0 * PI * (freq * 1.006) * t)
            val harm3 = sin(6.0 * PI * freq * t) * 0.34
            val harm5 = sin(10.0 * PI * freq * t) * 0.15
            mix[idx] += (osc1 * 0.55 + osc2 * 0.45 + harm3 + harm5) * env * gain
        }
    }

    private fun add808Cowbell(
        mix: DoubleArray,
        startIdx: Int,
        sampleRate: Int,
        freq: Double,
        durSec: Double,
        gain: Double
    ) {
        val dur = (sampleRate * durSec).toInt()
        val f2 = freq * 1.482 // Metallic 808 cowbell ratio
        for (i in 0 until dur) {
            val idx = startIdx + i
            if (idx >= mix.size) break
            val t = i.toDouble() / sampleRate
            val env = (1.0 - exp(-t * 250.0)) * (0.65 * exp(-t * 22.0) + 0.35 * exp(-t * 9.0))
            val sq1 = tanh(sin(2.0 * PI * freq * t) * 3.0)
            val sq2 = tanh(sin(2.0 * PI * f2 * t) * 3.0)
            mix[idx] += (sq1 * 0.52 + sq2 * 0.48) * env * gain
        }
    }

    private fun addAnalogBassStep(
        mix: DoubleArray,
        startIdx: Int,
        sampleRate: Int,
        freq: Double,
        durSamples: Int,
        gain: Double
    ) {
        for (i in 0 until durSamples) {
            val idx = startIdx + i
            if (idx >= mix.size) break
            val t = i.toDouble() / sampleRate
            val env = (1.0 - exp(-t * 140.0)) * exp(-t * 7.0)
            val saw = sin(2.0 * PI * freq * t) * 0.65 +
                sin(4.0 * PI * freq * t) * 0.32 +
                sin(6.0 * PI * freq * t) * 0.16
            mix[idx] += tanh(saw * 1.4) * env * gain
        }
    }

    private fun addReeseBass(
        mix: DoubleArray,
        startIdx: Int,
        sampleRate: Int,
        freq: Double,
        durSamples: Int,
        gain: Double
    ) {
        for (i in 0 until durSamples) {
            val idx = startIdx + i
            if (idx >= mix.size) break
            val t = i.toDouble() / sampleRate
            val env = (1.0 - exp(-t * 35.0)) * (1.0 - exp(-((durSamples - i).toDouble() / sampleRate) * 28.0))
            val o1 = sin(2.0 * PI * freq * t)
            val o2 = sin(2.0 * PI * (freq * 1.012) * t)
            val o3 = sin(4.0 * PI * (freq * 0.994) * t) * 0.42
            mix[idx] += tanh((o1 + o2 + o3) * 1.3) * 0.55 * env * gain
        }
    }

    private fun addWarmPadChord(
        mix: DoubleArray,
        startIdx: Int,
        sampleRate: Int,
        freqs: DoubleArray,
        durSamples: Int,
        gain: Double
    ) {
        for (i in 0 until durSamples) {
            val idx = startIdx + i
            if (idx >= mix.size) break
            val t = i.toDouble() / sampleRate
            val release = ((durSamples - i).toDouble() / sampleRate).coerceAtLeast(0.0)
            val env = (1.0 - exp(-t * 18.0)) * (1.0 - exp(-release * 22.0))
            var sum = 0.0
            for (f in freqs) {
                sum += sin(2.0 * PI * f * t) * 0.65 + sin(2.0 * PI * (f * 1.004) * t) * 0.35
            }
            mix[idx] += (sum / freqs.size) * env * gain
        }
    }

    private fun addSynthPluck(
        mix: DoubleArray,
        startIdx: Int,
        sampleRate: Int,
        freq: Double,
        durSec: Double,
        gain: Double
    ) {
        val dur = (sampleRate * durSec).toInt()
        for (i in 0 until dur) {
            val idx = startIdx + i
            if (idx >= mix.size) break
            val t = i.toDouble() / sampleRate
            val env = (1.0 - exp(-t * 160.0)) * exp(-t * 10.5)
            val osc = sin(2.0 * PI * freq * t) * 0.65 +
                sin(4.0 * PI * freq * t) * 0.25 +
                sin(6.0 * PI * freq * t) * 0.12
            mix[idx] += osc * env * gain
        }
    }

    private fun addNoiseRiser(
        mix: DoubleArray,
        startIdx: Int,
        durSamples: Int,
        sampleRate: Int,
        rng: Random,
        gain: Double
    ) {
        var lp = 0.0
        for (i in 0 until durSamples) {
            val idx = startIdx + i
            if (idx >= mix.size) break
            val frac = i.toDouble() / durSamples
            val raw = rng.nextDouble() * 2.0 - 1.0
            lp += (raw - lp) * (0.06 + 0.45 * frac)
            val sweepTone = sin(2.0 * PI * (240.0 + 960.0 * frac * frac) * (i.toDouble() / sampleRate)) * 0.3
            mix[idx] += (lp * 0.7 + sweepTone) * (frac * frac) * gain
        }
    }

    private fun finalizeMix(mix: DoubleArray, sampleRate: Int): ShortArray {
        val fadeSamples = (sampleRate * 0.008).toInt().coerceAtLeast(1)
        val n = mix.size
        return ShortArray(n) { i ->
            val edgeEnv = when {
                i < fadeSamples -> i.toDouble() / fadeSamples
                i > n - fadeSamples -> (n - i).toDouble() / fadeSamples
                else -> 1.0
            }
            val soft = tanh(mix[i] * 1.15) * 0.90 * edgeEnv
            (soft.coerceIn(-0.98, 0.98) * Short.MAX_VALUE).toInt().toShort()
        }
    }
}
