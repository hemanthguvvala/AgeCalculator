package com.hkgroups.agecalculator.util

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.runtime.compositionLocalOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

/**
 * CosmicFeedback — single entry point for haptics and chimes.
 *
 * Centralizing this prevents drift (one screen using 30ms ticks, another
 * using 50ms heavy clicks). Patterns map to user intent, not raw durations.
 *
 * Both haptics and chimes can be disabled via SettingsRepository — read by
 * the caller and passed in via [hapticsEnabled] / [chimesEnabled] hot
 * lambdas so we don't need to plumb the repo here.
 */
class CosmicFeedback(
    context: Context,
    private val scope: CoroutineScope,
    private val hapticsEnabled: () -> Boolean,
    private val chimesEnabled: () -> Boolean
) {
    private val vibrator: Vibrator? = run {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            (context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager)
                ?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    private var lastChimeJob: Job? = null

    enum class Cue { Tap, Select, Swipe, Success, Milestone }

    fun fire(cue: Cue) {
        if (hapticsEnabled()) vibrate(cue)
        if (chimesEnabled() && (cue == Cue.Success || cue == Cue.Milestone)) playChime(cue)
    }

    private fun vibrate(cue: Cue) {
        val v = vibrator ?: return
        if (!v.hasVibrator()) return

        val effect = when (cue) {
            Cue.Tap -> VibrationEffect.createOneShot(8, 60)
            Cue.Select -> VibrationEffect.createOneShot(14, 110)
            Cue.Swipe -> VibrationEffect.createOneShot(6, 40)
            Cue.Success -> VibrationEffect.createWaveform(
                longArrayOf(0, 18, 40, 26),
                intArrayOf(0, 140, 0, 200),
                -1
            )
            Cue.Milestone -> VibrationEffect.createWaveform(
                longArrayOf(0, 24, 30, 24, 30, 38),
                intArrayOf(0, 120, 0, 160, 0, 220),
                -1
            )
        }
        v.vibrate(effect)
    }

    /** Plays a short two-tone bell. Generated once per call, written to AudioTrack on IO. */
    private fun playChime(cue: Cue) {
        // Cancel any in-flight chime so rapid milestones don't queue forever.
        lastChimeJob?.cancel()
        lastChimeJob = scope.launch(Dispatchers.IO) {
            val pcm = if (cue == Cue.Milestone) milestoneChime else successChime
            playPcm(pcm)
        }
    }

    private val sampleRate = 44100

    /** Two sine partials with exponential envelope — a soft glassy ping. */
    private fun synthesizeChime(
        durationMs: Int,
        freqHz: Float,
        partialHz: Float = freqHz * 1.5f,
        decay: Float = 4f
    ): ShortArray {
        val samples = (sampleRate * durationMs / 1000)
        val out = ShortArray(samples)
        for (i in 0 until samples) {
            val t = i.toFloat() / sampleRate
            val env = exp(-decay * t)
            val s = (sin(2 * PI * freqHz * t) * 0.7 + sin(2 * PI * partialHz * t) * 0.3) * env
            out[i] = (s * Short.MAX_VALUE * 0.55).toInt().toShort()
        }
        return out
    }

    private val successChime: ShortArray by lazy { synthesizeChime(420, 880f, 1320f, 5f) }
    private val milestoneChime: ShortArray by lazy {
        // Two-note arpeggio: C5 → G5
        val a = synthesizeChime(280, 523.25f, 783.99f, 5.5f)
        val b = synthesizeChime(420, 783.99f, 1174.66f, 4.5f)
        val gap = ShortArray(sampleRate * 30 / 1000) // 30ms gap
        a + gap + b
    }

    private suspend fun playPcm(data: ShortArray) = withContext(Dispatchers.IO) {
        val track = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION_EVENT)
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
            .setBufferSizeInBytes(data.size * 2)
            .setTransferMode(AudioTrack.MODE_STATIC)
            .build()
        try {
            track.write(data, 0, data.size)
            track.play()
            // Sleep for the duration so we can release the track cleanly.
            val durationMs = (data.size * 1000L) / sampleRate
            kotlinx.coroutines.delay(durationMs + 80)
        } finally {
            try { track.stop() } catch (_: IllegalStateException) {}
            track.release()
        }
    }
}

/** Composition-local hook so any composable can fire feedback without DI. */
val LocalCosmicFeedback = compositionLocalOf<CosmicFeedback?> { null }
