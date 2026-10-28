package com.example.safenumber.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.sin

/**
 * Procedural offline sound synthesizer for minimal, crisp mathematical audio cues.
 * Completely self-contained; requires no audio assets or internet connection.
 */
class SoundPlayer {

    private val scope = CoroutineScope(Dispatchers.Default)
    private val sampleRate = 44100

    @Volatile
    var isEnabled: Boolean = true

    /**
     * Short clean tick for countdown (e.g. 600 Hz, 60ms)
     */
    fun playCountdownTick(isFinal: Boolean = false) {
        if (!isEnabled) return
        scope.launch {
            val freq = if (isFinal) 880.0 else 587.33 // A5 or D5
            val durationMs = if (isFinal) 140 else 70
            playTone(freq, durationMs, 0.45f)
        }
    }

    /**
     * Pleasant rising chord/chime for safe tap
     */
    fun playSafeSound() {
        if (!isEnabled) return
        scope.launch {
            // Quick two-tone cheerful chime (E5 -> G5)
            playTone(659.25, 65, 0.4f)
            playTone(783.99, 90, 0.45f)
        }
    }

    /**
     * Sharp warning/danger sound
     */
    fun playDangerSound() {
        if (!isEnabled) return
        scope.launch {
            // Low dual dissonance (196 Hz & 207 Hz)
            playDualTone(196.0, 207.65, 260, 0.6f)
        }
    }

    /**
     * Milestone streak celebration sound (ascending arpeggio)
     */
    fun playMilestoneSound() {
        if (!isEnabled) return
        scope.launch {
            val notes = listOf(523.25, 659.25, 783.99, 1046.50) // C5, E5, G5, C6
            for (note in notes) {
                playTone(note, 65, 0.4f)
            }
        }
    }

    /**
     * Subtle warning tick when timer <= 3s
     */
    fun playWarningTick() {
        if (!isEnabled) return
        scope.launch {
            playTone(987.77, 45, 0.35f) // B5 quick alert
        }
    }

    /**
     * Game over descending tone
     */
    fun playGameOverSound() {
        if (!isEnabled) return
        scope.launch {
            playTone(440.0, 100, 0.4f)
            playTone(349.23, 160, 0.45f)
        }
    }

    private fun playTone(frequency: Double, durationMs: Int, volume: Float) {
        try {
            val numSamples = (sampleRate * durationMs) / 1000
            val samples = ShortArray(numSamples)

            for (i in 0 until numSamples) {
                val t = i.toDouble() / sampleRate
                val raw = sin(2.0 * Math.PI * frequency * t)

                // Smooth attack and release envelope to prevent audible clicks
                val attackSamples = (sampleRate * 0.008).toInt().coerceAtLeast(1)
                val releaseSamples = (sampleRate * 0.015).toInt().coerceAtLeast(1)
                val env = when {
                    i < attackSamples -> i.toFloat() / attackSamples
                    i > numSamples - releaseSamples -> (numSamples - i).toFloat() / releaseSamples
                    else -> 1f
                }

                samples[i] = (raw * Short.MAX_VALUE * volume * env).toInt().toShort()
            }

            val bufferSize = samples.size * 2
            val audioTrack = AudioTrack.Builder()
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
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            audioTrack.write(samples, 0, samples.size)
            audioTrack.play()
            Thread.sleep(durationMs.toLong() + 20)
            audioTrack.stop()
            audioTrack.release()
        } catch (_: Exception) {
            // AudioTrack failure fallback silently
        }
    }

    private fun playDualTone(freq1: Double, freq2: Double, durationMs: Int, volume: Float) {
        try {
            val numSamples = (sampleRate * durationMs) / 1000
            val samples = ShortArray(numSamples)

            for (i in 0 until numSamples) {
                val t = i.toDouble() / sampleRate
                val raw1 = sin(2.0 * Math.PI * freq1 * t)
                val raw2 = sin(2.0 * Math.PI * freq2 * t)
                val combined = (raw1 + raw2) * 0.5

                val attackSamples = (sampleRate * 0.01).toInt().coerceAtLeast(1)
                val releaseSamples = (sampleRate * 0.02).toInt().coerceAtLeast(1)
                val env = when {
                    i < attackSamples -> i.toFloat() / attackSamples
                    i > numSamples - releaseSamples -> (numSamples - i).toFloat() / releaseSamples
                    else -> 1f
                }

                samples[i] = (combined * Short.MAX_VALUE * volume * env).toInt().toShort()
            }

            val bufferSize = samples.size * 2
            val audioTrack = AudioTrack.Builder()
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
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            audioTrack.write(samples, 0, samples.size)
            audioTrack.play()
            Thread.sleep(durationMs.toLong() + 30)
            audioTrack.stop()
            audioTrack.release()
        } catch (_: Exception) {
            // Ignore
        }
    }
}
