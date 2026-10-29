package com.example.game.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.rememberTextMeasurer
import com.example.game.audio.DriftAudioSynth
import com.example.game.model.Car
import com.example.game.model.GameMode
import com.example.game.model.Track
import com.example.game.physics.DriftPhysicsEngine
import com.example.game.scoring.DriftScoringSystem
import com.example.game.storage.GameStorage
import com.example.game.ui.GameCanvasRenderer
import com.example.game.ui.GameHud
import com.example.game.ui.dialogs.PauseDialog
import com.example.game.ui.dialogs.SessionResultDialog

@Composable
fun GameScreen(
    car: Car,
    track: Track,
    mode: GameMode,
    storage: GameStorage,
    onExitGame: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val textMeasurer = rememberTextMeasurer()

    val physics = remember(car, track) {
        DriftPhysicsEngine(car = car, track = track)
    }
    val scoring = remember { DriftScoringSystem() }
    val renderer = remember { GameCanvasRenderer() }
    val audioSynth = remember {
        DriftAudioSynth(context).apply {
            isSoundEnabled = storage.soundEnabled
            isHapticsEnabled = storage.hapticsEnabled
            start()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            audioSynth.stop()
        }
    }

    // Input States
    var steerLeft by remember { mutableStateOf(false) }
    var steerRight by remember { mutableStateOf(false) }
    var throttle by remember { mutableStateOf(false) }
    var brake by remember { mutableStateOf(false) }
    var handbrake by remember { mutableStateOf(false) }
    var nitro by remember { mutableStateOf(false) }

    // Game lifecycle states
    var isPaused by remember { mutableStateOf(false) }
    var isSessionFinished by remember { mutableStateOf(false) }
    var isNewRecord by remember { mutableStateOf(false) }
    var remainingTimeSec by remember { mutableFloatStateOf(mode.defaultTimeLimitSec) }

    // Session reset function
    fun restartSession() {
        physics.reset()
        scoring.reset()
        remainingTimeSec = mode.defaultTimeLimitSec
        isPaused = false
        isSessionFinished = false
        isNewRecord = false
    }

    BackHandler {
        if (!isPaused && !isSessionFinished) {
            isPaused = true
        } else {
            onExitGame()
        }
    }

    // 60FPS Game Loop
    LaunchedEffect(isPaused, isSessionFinished) {
        var lastNanoTime = System.nanoTime()

        while (!isPaused && !isSessionFinished) {
            withFrameNanos { now ->
                val dt = ((now - lastNanoTime) / 1_000_000_000f).coerceIn(0.001f, 0.05f)
                lastNanoTime = now

                // Calculate steering input with user sensitivity preference
                val steerInput = when {
                    steerLeft && !steerRight -> -1f * storage.steeringSensitivity
                    steerRight && !steerLeft -> 1f * storage.steeringSensitivity
                    else -> 0f
                }
                val throttleInput = if (throttle) 1f else 0f
                val brakeInput = if (brake) 1f else 0f

                // Physics update
                physics.update(
                    dt = dt,
                    steerInput = steerInput,
                    throttleInput = throttleInput,
                    brakeInput = brakeInput,
                    handbrakeInput = handbrake,
                    nitroInput = nitro
                )

                // Scoring update
                val event = scoring.update(dt, physics)

                // Audio and Haptics telemetry
                audioSynth.updateTelemetry(
                    speed = physics.speed,
                    maxSpeed = car.topSpeed,
                    throttle = throttleInput,
                    isDrifting = physics.isDrifting,
                    driftAngle = physics.driftAngleDeg,
                    isNitro = physics.isNitroActive
                )

                if (physics.justWallTapped) {
                    audioSynth.triggerWallTapHaptic(strong = physics.justCrashed)
                } else if (physics.isDrifting && (now % 6 == 0L)) {
                    audioSynth.triggerDriftHaptic()
                }

                // Session rules check
                if (!mode.defaultTimeLimitSec.isInfinite()) {
                    remainingTimeSec -= dt
                    if (remainingTimeSec <= 0f) {
                        scoring.finalizeScore()
                        isSessionFinished = true
                    }
                }

                // Lap circuit completion check
                if (mode.targetLaps > 0 && physics.completedLaps >= mode.targetLaps) {
                    scoring.finalizeScore()
                    isSessionFinished = true
                }
            }
        }
    }

    // Handle session completion reward save
    LaunchedEffect(isSessionFinished) {
        if (isSessionFinished) {
            val finalScore = scoring.totalScore
            isNewRecord = storage.recordScore(track.id, mode.name, finalScore)
            storage.coins += scoring.coinsEarned
            storage.careerDriftScore += finalScore
            if (scoring.longestDriftSec > storage.bestSingleDrift) {
                storage.bestSingleDrift = scoring.longestDriftSec
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("game_screen_container")
    ) {
        // World Canvas Rendering
        Canvas(modifier = Modifier.fillMaxSize()) {
            renderer.draw(
                drawScope = this,
                physics = physics,
                textMeasurer = textMeasurer
            )
        }

        // HUD Overlay
        GameHud(
            physics = physics,
            scoring = scoring,
            track = track,
            mode = mode,
            sessionRemainingTime = remainingTimeSec,
            onPauseClick = { isPaused = true },
            onSteerLeftChange = { steerLeft = it },
            onSteerRightChange = { steerRight = it },
            onThrottleChange = { throttle = it },
            onBrakeChange = { brake = it },
            onHandbrakeChange = { handbrake = it },
            onNitroChange = { nitro = it }
        )

        // Pause Menu Dialog
        if (isPaused) {
            PauseDialog(
                soundEnabled = storage.soundEnabled,
                hapticsEnabled = storage.hapticsEnabled,
                onResume = { isPaused = false },
                onRestart = { restartSession() },
                onToggleSound = {
                    storage.soundEnabled = it
                    audioSynth.isSoundEnabled = it
                },
                onToggleHaptics = {
                    storage.hapticsEnabled = it
                    audioSynth.isHapticsEnabled = it
                },
                onExitToMenu = onExitGame
            )
        }

        // Post-Session Result Dialog
        if (isSessionFinished) {
            SessionResultDialog(
                score = scoring.totalScore,
                isNewHighScore = isNewRecord,
                coinsEarned = scoring.coinsEarned,
                longestDriftSec = scoring.longestDriftSec,
                maxDriftAngle = scoring.maxDriftAngle,
                peakMultiplier = scoring.multiplier,
                clipsHit = scoring.clipsHitCount,
                onRetry = { restartSession() },
                onExitToMenu = onExitGame
            )
        }
    }
}
