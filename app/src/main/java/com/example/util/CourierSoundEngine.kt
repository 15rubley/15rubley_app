package com.example.util

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.speech.tts.TextToSpeech
import com.example.data.GarageTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin
import kotlin.random.Random

/**
 * Procedural PCM sound synthesizer + TextToSpeech aviation callout engine +
 * Multi-genre Garage Radio & Auto-DJ looper.
 * Zero external audio assets required; plays offline via AudioTrack + Android TTS.
 */
class CourierSoundEngine(context: Context) {

    private val appContext = context.applicationContext
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    @Volatile
    private var ttsReady = false
    private var tts: TextToSpeech? = null

    @Volatile
    private var ruTtsReady = false
    private var ruTts: TextToSpeech? = null

    private var garageMusicJob: Job? = null
    @Volatile
    private var musicShouldPlay = false

    private var ringtoneJob: Job? = null
    @Volatile
    private var ringtonePlaying = false

    @Volatile
    var currentGarageTrack: GarageTrack = GarageTrack.GET_LOW_CRUNK
        private set

    @Volatile
    var autoMixEnabled: Boolean = true

    var onTrackAutoAdvanced: ((GarageTrack) -> Unit)? = null

    var isEnabled: Boolean = true

    init {
        try {
            tts = TextToSpeech(appContext) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    val engine = tts ?: return@TextToSpeech
                    val res = engine.setLanguage(Locale.US)
                    ttsReady = res != TextToSpeech.LANG_MISSING_DATA &&
                        res != TextToSpeech.LANG_NOT_SUPPORTED
                    engine.setPitch(0.9f)
                    engine.setSpeechRate(1.02f)
                }
            }
            ruTts = TextToSpeech(appContext) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    val engine = ruTts ?: return@TextToSpeech
                    val ruLocale = Locale.forLanguageTag("ru-RU")
                    val res = engine.setLanguage(ruLocale)
                    ruTtsReady = res != TextToSpeech.LANG_MISSING_DATA &&
                        res != TextToSpeech.LANG_NOT_SUPPORTED
                    engine.setPitch(1.06f)
                    engine.setSpeechRate(1.08f)
                }
            }
        } catch (_: Exception) {
            ttsReady = false
            ruTtsReady = false
        }
    }

    fun playGearShift(gear: Int) {
        if (!isEnabled) return
        scope.launch {
            playMechanicalGearClunk(gear)
            when (gear) {
                4 -> speakBoeingCallout("V one")
                5 -> speakBoeingCallout("Rotate")
            }
        }
    }

    fun playGearJam() {
        if (!isEnabled) return
        scope.launch {
            playMetallicSnapAndBeep()
        }
    }

    fun playOrderAccepted() {
        if (!isEnabled) return
        scope.launch {
            playTwoToneChime(freq1 = 587.33, freq2 = 880.0, dur1Ms = 85, dur2Ms = 140, volume = 0.32)
        }
    }

    fun playRepairSound() {
        if (!isEnabled) return
        scope.launch {
            playRatchetAndChime()
        }
    }

    fun playTuningInstallSound() {
        if (!isEnabled) return
        scope.launch {
            playTwoToneChime(freq1 = 440.0, freq2 = 1174.66, dur1Ms = 75, dur2Ms = 180, volume = 0.30)
        }
    }

    fun playIncomingTransfer() {
        if (!isEnabled) return
        scope.launch {
            playTwoToneChime(freq1 = 1046.5, freq2 = 1318.5, dur1Ms = 70, dur2Ms = 150, volume = 0.28)
        }
    }

    fun playIncomingDm() {
        if (!isEnabled) return
        scope.launch {
            playTwoToneChime(freq1 = 740.0, freq2 = 987.77, dur1Ms = 65, dur2Ms = 110, volume = 0.30)
        }
    }

    fun playBreakdownAlert() {
        if (!isEnabled) return
        scope.launch {
            playMetallicSnapAndBeep()
        }
    }

    fun playBrakeSqueak(brakeHealthPercent: Int) {
        if (!isEnabled) return
        scope.launch {
            playSqueakInternal(brakeHealthPercent)
        }
    }

    fun startIncomingRingtone() {
        if (!isEnabled) {
            stopIncomingRingtone()
            return
        }
        if (ringtonePlaying && ringtoneJob?.isActive == true) return
        ringtonePlaying = true
        ringtoneJob?.cancel()
        ringtoneJob = scope.launch(Dispatchers.IO) {
            val sampleRate = 22050
            val ringPattern = buildRingtonePhrase(sampleRate)
            var track: AudioTrack? = null
            try {
                val minBuf = AudioTrack.getMinBufferSize(
                    sampleRate,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT
                ).coerceAtLeast(4096)

                track = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(minBuf * 2)
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .build()

                track.play()
                val chunkSize = 2048
                var offset = 0
                while (isActive && ringtonePlaying && isEnabled) {
                    val end = minOf(offset + chunkSize, ringPattern.size)
                    track.write(ringPattern, offset, end - offset)
                    offset = if (end >= ringPattern.size) 0 else end
                }
            } catch (_: Exception) {
            } finally {
                try {
                    track?.pause()
                    track?.flush()
                    track?.stop()
                    track?.release()
                } catch (_: Exception) {
                }
            }
        }
    }

    fun stopIncomingRingtone() {
        ringtonePlaying = false
        ringtoneJob?.cancel()
        ringtoneJob = null
    }

    fun playCallAnswered(complaintText: String) {
        stopIncomingRingtone()
        if (!isEnabled) return
        scope.launch {
            playTwoToneChime(freq1 = 480.0, freq2 = 720.0, dur1Ms = 45, dur2Ms = 65, volume = 0.24)
            speakComplaintVoice(complaintText, angerPercent = 28)
        }
    }

    fun playClientFollowUpLine(lineText: String, angerPercent: Int) {
        if (!isEnabled) return
        scope.launch {
            speakComplaintVoice(lineText, angerPercent = angerPercent)
        }
    }

    fun playClientRageHangUp(finalText: String) {
        stopIncomingRingtone()
        if (!isEnabled) return
        scope.launch {
            speakComplaintVoice(finalText, angerPercent = 100)
            kotlinx.coroutines.delay(950L)
            playBusyBeepTwice()
        }
    }

    fun playCallHangUp() {
        stopIncomingRingtone()
        try {
            ruTts?.stop()
        } catch (_: Exception) {
        }
        if (!isEnabled) return
        scope.launch {
            playBusyBeepTwice()
        }
    }

    fun setGarageTrack(track: GarageTrack) {
        if (currentGarageTrack == track) return
        currentGarageTrack = track
        if (musicShouldPlay && isEnabled) {
            garageMusicJob?.cancel()
            garageMusicJob = null
            startGarageMusic()
        }
    }

    /**
     * Streams an 8-bar evolving synth arrangement for the selected [GarageTrack].
     * When [autoMixEnabled] is true, seamlessly advances to the next track every 2 cycles.
     */
    fun startGarageMusic() {
        if (!isEnabled) {
            stopGarageMusic()
            return
        }
        if (musicShouldPlay && garageMusicJob?.isActive == true) return
        musicShouldPlay = true
        garageMusicJob?.cancel()
        garageMusicJob = scope.launch(Dispatchers.IO) {
            val sampleRate = 22050
            var activeTrack = currentGarageTrack
            var barLoop = GarageMusicSynth.buildTrackLoop(activeTrack, sampleRate)
            var track: AudioTrack? = null
            try {
                val minBuf = AudioTrack.getMinBufferSize(
                    sampleRate,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT
                ).coerceAtLeast(4096)

                track = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(minBuf * 2)
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .build()

                track.play()
                val chunkSize = 2048
                var offset = 0
                var completedLoops = 0
                while (isActive && musicShouldPlay && isEnabled) {
                    if (activeTrack != currentGarageTrack) {
                        activeTrack = currentGarageTrack
                        barLoop = GarageMusicSynth.buildTrackLoop(activeTrack, sampleRate)
                        offset = 0
                        completedLoops = 0
                    }
                    val end = minOf(offset + chunkSize, barLoop.size)
                    track.write(barLoop, offset, end - offset)
                    if (end >= barLoop.size) {
                        offset = 0
                        completedLoops += 1
                        if (autoMixEnabled && completedLoops >= 2) {
                            val next = activeTrack.next()
                            currentGarageTrack = next
                            activeTrack = next
                            barLoop = GarageMusicSynth.buildTrackLoop(activeTrack, sampleRate)
                            completedLoops = 0
                            onTrackAutoAdvanced?.invoke(next)
                        }
                    } else {
                        offset = end
                    }
                }
            } catch (_: Exception) {
                // Safe fallback on headless / Robolectric environments
            } finally {
                try {
                    track?.pause()
                    track?.flush()
                    track?.stop()
                    track?.release()
                } catch (_: Exception) {
                }
            }
        }
    }

    fun stopGarageMusic() {
        musicShouldPlay = false
        garageMusicJob?.cancel()
        garageMusicJob = null
    }

    private fun speakBoeingCallout(phrase: String) {
        if (ttsReady) {
            try {
                tts?.speak(phrase, TextToSpeech.QUEUE_FLUSH, null, "gear_${System.currentTimeMillis()}")
                return
            } catch (_: Exception) {
                // Fall through to synthesized cockpit tone if TTS fails
            }
        }
        playTwoToneChime(freq1 = 660.0, freq2 = 990.0, dur1Ms = 60, dur2Ms = 90, volume = 0.22)
    }

    private fun playRatchetAndChime() {
        val sampleRate = 22050
        val count = sampleRate * 260 / 1000
        val samples = ShortArray(count)
        for (i in 0 until count) {
            val t = i.toDouble() / sampleRate
            val v = if (i < count / 2) {
                val pulse = ((i * 5) % (count / 2)).toDouble() / (count / 2)
                val click = (Random.nextDouble() * 2.0 - 1.0) * exp(-pulse * 12.0) * 0.45
                val clank = sin(2.0 * PI * 920.0 * t) * exp(-pulse * 16.0) * 0.35
                (click + clank) * 0.45
            } else {
                val t2 = (i - count / 2).toDouble() / sampleRate
                sin(2.0 * PI * 880.0 * t2) * exp(-t2 * 14.0) * 0.32
            }
            samples[i] = (v.coerceIn(-1.0, 1.0) * Short.MAX_VALUE).toInt().toShort()
        }
        writePcm(samples, sampleRate)
    }

    private fun playMechanicalGearClunk(gear: Int) {
        val sampleRate = 22050
        val durationMs = 85
        val count = sampleRate * durationMs / 1000
        val samples = ShortArray(count)
        val baseFreq = 130.0 + gear * 18.0
        for (i in 0 until count) {
            val t = i.toDouble() / sampleRate
            val env = exp(-t * 38.0)
            val thud = sin(2.0 * PI * (baseFreq - t * 320.0) * t)
            val click = if (i < count / 5) (Random.nextDouble() * 2.0 - 1.0) * 0.55 else 0.0
            val v = (thud * 0.75 + click) * env * 0.42
            samples[i] = (v.coerceIn(-1.0, 1.0) * Short.MAX_VALUE).toInt().toShort()
        }
        writePcm(samples, sampleRate)
    }

    private fun playTwoToneChime(
        freq1: Double,
        freq2: Double,
        dur1Ms: Int,
        dur2Ms: Int,
        volume: Double
    ) {
        val sampleRate = 22050
        val n1 = sampleRate * dur1Ms / 1000
        val n2 = sampleRate * dur2Ms / 1000
        val samples = ShortArray(n1 + n2)

        for (i in 0 until n1) {
            val t = i.toDouble() / sampleRate
            val env = sin(PI * i / n1) * exp(-t * 6.0)
            val s = sin(2.0 * PI * freq1 * t) + 0.25 * sin(4.0 * PI * freq1 * t)
            samples[i] = ((s * env * volume).coerceIn(-1.0, 1.0) * Short.MAX_VALUE).toInt().toShort()
        }
        for (i in 0 until n2) {
            val t = i.toDouble() / sampleRate
            val env = exp(-t * 14.0)
            val s = sin(2.0 * PI * freq2 * t) + 0.2 * sin(4.0 * PI * freq2 * t)
            samples[n1 + i] = ((s * env * volume).coerceIn(-1.0, 1.0) * Short.MAX_VALUE).toInt().toShort()
        }
        writePcm(samples, sampleRate)
    }

    private fun playSqueakInternal(brakeHealthPercent: Int) {
        val sampleRate = 22050
        val wearFactor = ((100 - brakeHealthPercent).coerceIn(10, 95)) / 100.0
        val durationMs = (240 + (wearFactor * 260)).toInt()
        val count = sampleRate * durationMs / 1000
        val samples = ShortArray(count)
        val centerFreq = 2500.0 + wearFactor * 1150.0

        for (i in 0 until count) {
            val t = i.toDouble() / sampleRate
            val progress = i.toDouble() / count
            val envelope = sin(PI * progress) * (0.65 + 0.35 * sin(2.0 * PI * 14.0 * t))
            val wobble = 180.0 * sin(2.0 * PI * 22.0 * t)
            val squeal = sin(2.0 * PI * (centerFreq + wobble) * t) * 0.6 +
                sin(2.0 * PI * (centerFreq * 1.48) * t) * 0.25
            val grit = (Random.nextDouble() * 2.0 - 1.0) * 0.16 * wearFactor
            val v = (squeal + grit) * envelope * (0.16 + 0.22 * wearFactor)
            samples[i] = (v.coerceIn(-1.0, 1.0) * Short.MAX_VALUE).toInt().toShort()
        }
        writePcm(samples, sampleRate)
    }

    private fun playMetallicSnapAndBeep() {
        val sampleRate = 22050
        val durationMs = 260
        val count = sampleRate * durationMs / 1000
        val samples = ShortArray(count)

        for (i in 0 until count) {
            val t = i.toDouble() / sampleRate
            val v = if (i < count / 3) {
                val env = exp(-t * 32.0)
                val noise = (Random.nextDouble() * 2.0 - 1.0) * 0.65
                val clang = sin(2.0 * PI * 420.0 * t) * 0.45
                (noise + clang) * env * 0.4
            } else {
                val t2 = (i - count / 3).toDouble() / sampleRate
                val env = exp(-t2 * 10.0)
                val warn = sin(2.0 * PI * 310.0 * t2) * 0.6 + sin(2.0 * PI * 215.0 * t2) * 0.4
                warn * env * 0.32
            }
            samples[i] = (v.coerceIn(-1.0, 1.0) * Short.MAX_VALUE).toInt().toShort()
        }
        writePcm(samples, sampleRate)
    }

    private fun writePcm(samples: ShortArray, sampleRate: Int) {
        try {
            val minBuf = AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )
            val bufBytes = maxOf(minBuf, samples.size * 2)
            val track = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(bufBytes)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            track.write(samples, 0, samples.size)
            track.play()
            val sleepMs = (samples.size * 1000L / sampleRate) + 40L
            Thread.sleep(sleepMs)
            track.stop()
            track.release()
        } catch (_: Exception) {
            // Safe fallback on headless / unit-test environments without audio hardware
        }
    }

    private fun speakComplaintVoice(phrase: String, angerPercent: Int = 30) {
        val normAnger = ((angerPercent - 20).coerceIn(0, 80)) / 80f
        if (ruTtsReady) {
            try {
                ruTts?.setPitch(1.04f + normAnger * 0.20f)
                ruTts?.setSpeechRate(1.06f + normAnger * 0.24f)
                ruTts?.speak(phrase, TextToSpeech.QUEUE_FLUSH, null, "call_${System.currentTimeMillis()}")
                return
            } catch (_: Exception) {
            }
        }
        // Expressive phone chatter tone fallback if Russian TTS isn't available on the device
        playAngryPhoneChatterFallback(angerPercent)
    }

    private fun buildRingtonePhrase(sampleRate: Int): ShortArray {
        // 1.5s marimba-style upbeat courier ringtone phrase + 0.6s pause
        val totalSamples = (sampleRate * 2.1).toInt()
        val mix = DoubleArray(totalSamples)
        val notes = listOf(
            0.00 to 783.99,  // G5
            0.13 to 987.77,  // B5
            0.26 to 1174.66, // D6
            0.39 to 987.77,  // B5
            0.55 to 1318.51, // E6
            0.70 to 1174.66, // D6
            0.90 to 987.77,  // B5
            1.05 to 1174.66  // D6
        )
        for ((startSec, freq) in notes) {
            val start = (startSec * sampleRate).toInt()
            val dur = (sampleRate * 0.20).toInt()
            for (i in 0 until dur) {
                val idx = start + i
                if (idx >= totalSamples) break
                val t = i.toDouble() / sampleRate
                val env = (1.0 - exp(-t * 160.0)) * exp(-t * 15.0)
                val sig = sin(2.0 * PI * freq * t) * 0.65 + sin(4.0 * PI * freq * t) * 0.22
                mix[idx] += sig * env * 0.34
            }
        }
        return ShortArray(totalSamples) { i ->
            (mix[i].coerceIn(-0.95, 0.95) * Short.MAX_VALUE).toInt().toShort()
        }
    }

    private fun playAngryPhoneChatterFallback(angerPercent: Int = 35) {
        val sampleRate = 22050
        val intensity = ((angerPercent.coerceIn(20, 100)) / 100.0)
        val count = (sampleRate * (0.48 + intensity * 0.28)).toInt()
        val samples = ShortArray(count)
        val syllableRate = 7.0 + intensity * 4.5
        val basePitch = 320.0 + intensity * 120.0
        for (i in 0 until count) {
            val t = i.toDouble() / sampleRate
            val syllable = ((t * syllableRate).toInt() % 3)
            val pitch = when (syllable) {
                0 -> basePitch + 95.0 * sin(2.0 * PI * 10.0 * t)
                1 -> basePitch - 35.0
                else -> basePitch + 45.0
            }
            val env = sin(PI * ((t * syllableRate) % 1.0)) * (0.22 + intensity * 0.12)
            val voice = sin(2.0 * PI * pitch * t) * 0.6 + sin(4.0 * PI * pitch * t) * 0.28
            samples[i] = ((voice * env).coerceIn(-0.9, 0.9) * Short.MAX_VALUE).toInt().toShort()
        }
        writePcm(samples, sampleRate)
    }

    private fun playBusyBeepTwice() {
        val sampleRate = 22050
        val count = (sampleRate * 0.34).toInt()
        val samples = ShortArray(count)
        for (i in 0 until count) {
            val t = i.toDouble() / sampleRate
            val active = (t in 0.0..0.11) || (t in 0.18..0.29)
            val v = if (active) sin(2.0 * PI * 425.0 * t) * 0.24 else 0.0
            samples[i] = ((v.coerceIn(-0.9, 0.9)) * Short.MAX_VALUE).toInt().toShort()
        }
        writePcm(samples, sampleRate)
    }

    fun release() {
        stopGarageMusic()
        stopIncomingRingtone()
        try {
            tts?.stop()
            tts?.shutdown()
            ruTts?.stop()
            ruTts?.shutdown()
        } catch (_: Exception) {
        }
    }
}
