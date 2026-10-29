package com.example.game.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.PI
import kotlin.math.sin
import kotlin.random.Random

class DriftAudioSynth(val context: Context) {

    private val sampleRate = 22050
    private val bufferSize = AudioTrack.getMinBufferSize(
        sampleRate,
        AudioFormat.CHANNEL_OUT_MONO,
        AudioFormat.ENCODING_PCM_16BIT
    ).coerceAtLeast(2048)

    private var audioTrack: AudioTrack? = null
    private val isRunning = AtomicBoolean(false)
    private var audioThread: Thread? = null

    // Audio parameters modulated by physics
    @Volatile var enginePitchHz: Float = 90f
    @Volatile var engineVolume: Float = 0.35f
    @Volatile var screechVolume: Float = 0.0f
    @Volatile var isSoundEnabled: Boolean = true
    @Volatile var isHapticsEnabled: Boolean = true

    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vm?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    fun start() {
        if (isRunning.get()) return
        isRunning.set(true)

        try {
            audioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
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
                .setBufferSizeInBytes(bufferSize)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()

            audioTrack?.play()

            audioThread = Thread({
                val pcmBuffer = ShortArray(bufferSize / 2)
                var phaseEngine = 0.0
                var phaseEngineHarmonic = 0.0

                while (isRunning.get()) {
                    if (!isSoundEnabled) {
                        pcmBuffer.fill(0)
                    } else {
                        val currentPitch = enginePitchHz
                        val currentEngVol = engineVolume
                        val currentScreechVol = screechVolume

                        val phaseInc = (2.0 * PI * currentPitch) / sampleRate
                        val phaseIncHarmonic = phaseInc * 1.5

                        for (i in pcmBuffer.indices) {
                            // Synthesize engine: fundamental + 2nd harmonic + rough saturation
                            val wave1 = sin(phaseEngine)
                            val wave2 = sin(phaseEngineHarmonic) * 0.45
                            val engineSample = ((wave1 + wave2) * currentEngVol * 0.55).coerceIn(-1.0, 1.0)

                            // Synthesize tire screech: bandpass filtered white noise
                            val noise = (Random.nextFloat() * 2f - 1f) * currentScreechVol * 0.65

                            val mixed = (engineSample + noise).coerceIn(-1.0, 1.0)
                            pcmBuffer[i] = (mixed * 32767).toInt().toShort()

                            phaseEngine = (phaseEngine + phaseInc) % (2.0 * PI)
                            phaseEngineHarmonic = (phaseEngineHarmonic + phaseIncHarmonic) % (2.0 * PI)
                        }
                    }

                    audioTrack?.write(pcmBuffer, 0, pcmBuffer.size)
                }
            }, "DriftAudioThread").apply {
                priority = Thread.MAX_PRIORITY
                start()
            }
        } catch (_: Exception) {
            // Graceful fallback if device audio cannot initialize
        }
    }

    fun updateTelemetry(speed: Float, maxSpeed: Float, throttle: Float, isDrifting: Boolean, driftAngle: Float, isNitro: Boolean) {
        // Engine RPM calculation
        val speedFraction = (speed / maxSpeed).coerceIn(0f, 1.2f)
        val basePitch = 85f + (speedFraction * 240f) + (throttle * 80f) + (if (isNitro) 70f else 0f)
        enginePitchHz = basePitch
        engineVolume = (0.2f + throttle * 0.4f + speedFraction * 0.25f).coerceIn(0.1f, 0.7f)

        // Tire screech calculation
        if (isDrifting && speed > 50f) {
            val angleFactor = (driftAngle / 45f).coerceIn(0.2f, 1.0f)
            screechVolume = (angleFactor * 0.6f).coerceIn(0f, 0.75f)
        } else {
            screechVolume = 0f
        }
    }

    fun triggerWallTapHaptic(strong: Boolean = false) {
        if (!isHapticsEnabled || vibrator == null || !vibrator.hasVibrator()) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val duration = if (strong) 85L else 35L
                val amplitude = if (strong) 255 else 140
                vibrator.vibrate(VibrationEffect.createOneShot(duration, amplitude))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(40L)
            }
        } catch (_: Exception) {}
    }

    fun triggerDriftHaptic() {
        if (!isHapticsEnabled || vibrator == null || !vibrator.hasVibrator()) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(20L, 75))
            }
        } catch (_: Exception) {}
    }

    fun stop() {
        isRunning.set(false)
        try {
            audioThread?.join(300)
            audioThread = null
            audioTrack?.stop()
            audioTrack?.release()
            audioTrack = null
        } catch (_: Exception) {}
    }
}
