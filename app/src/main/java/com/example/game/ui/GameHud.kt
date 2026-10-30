package com.example.game.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.model.CameraMode
import com.example.game.model.GameMode
import com.example.game.model.Track
import com.example.game.physics.DriftPhysicsEngine
import com.example.game.scoring.DriftScoringSystem
import com.example.game.scoring.ScoringEvent
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonOrange
import com.example.ui.theme.NeonRed
import com.example.ui.theme.NeonYellow
import com.example.ui.theme.NitroBlue
import com.example.ui.theme.NitroGlow
import com.example.ui.theme.RacingCard
import com.example.ui.theme.RacingDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import kotlin.math.roundToInt

@Composable
fun GameHud(
    physics: DriftPhysicsEngine,
    scoring: DriftScoringSystem,
    track: Track,
    mode: GameMode,
    sessionRemainingTime: Float,
    cameraMode: CameraMode = CameraMode.CHASE,
    onCameraModeClick: () -> Unit = {},
    onPauseClick: () -> Unit,
    // Controller state setters
    onSteerLeftChange: (Boolean) -> Unit,
    onSteerRightChange: (Boolean) -> Unit,
    onThrottleChange: (Boolean) -> Unit,
    onBrakeChange: (Boolean) -> Unit,
    onHandbrakeChange: (Boolean) -> Unit,
    onNitroChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("game_hud_overlay")
    ) {
        // ================= TOP BAR =================
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            // Left: Pause & Session Time / Laps
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onPauseClick,
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(RacingCard.copy(alpha = 0.85f))
                        .border(1.dp, Color(0x33FFFFFF), CircleShape)
                        .testTag("pause_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Pause,
                        contentDescription = "Pause Game",
                        tint = TextPrimary
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    val timeDisplay = if (mode.defaultTimeLimitSec.isInfinite()) {
                        "PRACTICE"
                    } else {
                        val remaining = sessionRemainingTime.coerceAtLeast(0f).toInt()
                        val mins = remaining / 60
                        val secs = remaining % 60
                        String.format("%02d:%02d", mins, secs)
                    }

                    Text(
                        text = timeDisplay,
                        color = if (sessionRemainingTime < 15f && !mode.defaultTimeLimitSec.isInfinite()) NeonRed else NeonCyan,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black
                    )

                    if (mode.targetLaps > 0) {
                        Text(
                            text = "LAP ${physics.completedLaps + 1}/${mode.targetLaps}",
                            color = TextMuted,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    } else {
                        Text(
                            text = mode.title,
                            color = TextMuted,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // Center: Banked Score
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "DRIFT SCORE",
                    color = TextMuted,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "${scoring.totalScore}",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            // Right: Camera Switcher & MiniMap
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Interactive Camera Mode Toggle Button
                Surface(
                    onClick = onCameraModeClick,
                    shape = RoundedCornerShape(12.dp),
                    color = RacingCard.copy(alpha = 0.85f),
                    border = androidx.compose.foundation.BorderStroke(1.2.dp, NeonCyan),
                    modifier = Modifier
                        .height(44.dp)
                        .testTag("camera_mode_button")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Videocam,
                            contentDescription = "Change Camera View",
                            tint = NeonCyan,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(
                                text = "CAM: ${cameraMode.title}",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                text = cameraMode.subtitle,
                                color = NeonYellow,
                                fontSize = 8.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                MiniMap(physics = physics, track = track, modifier = Modifier.size(76.dp))
            }
        }

        // ================= CENTER DRIFT COMBO BANNER =================
        AnimatedVisibility(
            visible = scoring.currentComboScore > 10f || scoring.comboTimeout > 0f,
            enter = fadeIn() + scaleIn(initialScale = 0.85f),
            exit = fadeOut() + scaleOut(targetScale = 0.85f),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 54.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clip(RoundedCornerShape(18.dp))
                    .background(RacingDark.copy(alpha = 0.85f))
                    .border(1.5.dp, NeonOrange, RoundedCornerShape(18.dp))
                    .padding(horizontal = 24.dp, vertical = 12.dp)
                    .testTag("drift_combo_card")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "+${scoring.currentComboScore.roundToInt()}",
                        color = NeonCyan,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    Surface(
                        color = NeonOrange,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "x${String.format("%.1f", scoring.multiplier)}",
                            color = Color.Black,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Text(
                        text = "${physics.driftAngleDeg.roundToInt()}° ANGLE",
                        color = NeonYellow,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "•",
                        color = TextMuted,
                        fontSize = 13.sp
                    )
                    val label = when {
                        scoring.multiplier >= 8.0f -> "DRIFT KING!"
                        scoring.multiplier >= 5.0f -> "ULTRA SLIDE!"
                        scoring.multiplier >= 3.0f -> "MEGA COMBO!"
                        scoring.multiplier >= 2.0f -> "SICK DRIFT!"
                        else -> "DRIFTING"
                    }
                    Text(
                        text = label,
                        color = if (scoring.multiplier >= 5.0f) NeonRed else NeonOrange,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                // Combo timeout bar
                if (scoring.comboTimeout > 0f) {
                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { (scoring.comboTimeout / scoring.maxComboTimeout).coerceIn(0f, 1f) },
                        modifier = Modifier
                            .width(140.dp)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        color = NeonCyan,
                        trackColor = Color(0x33FFFFFF)
                    )
                }
            }
        }

        // ================= BOTTOM CONTROLS & GAUGES =================
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 20.dp)
        ) {
            // LEFT: Steering Buttons
            Row(
                modifier = Modifier.align(Alignment.BottomStart),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Steer Left Button
                HoldButton(
                    onPressChange = onSteerLeftChange,
                    modifier = Modifier
                        .size(68.dp)
                        .testTag("steer_left_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowLeft,
                        contentDescription = "Steer Left",
                        tint = Color.White,
                        modifier = Modifier.size(42.dp)
                    )
                }

                // Steer Right Button
                HoldButton(
                    onPressChange = onSteerRightChange,
                    modifier = Modifier
                        .size(68.dp)
                        .testTag("steer_right_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowRight,
                        contentDescription = "Steer Right",
                        tint = Color.White,
                        modifier = Modifier.size(42.dp)
                    )
                }
            }

            // CENTER: Speedometer & Nitro NOS
            Column(
                modifier = Modifier.align(Alignment.BottomCenter),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Speed Readout
                Text(
                    text = "${(physics.speed * 0.35f).roundToInt()}",
                    color = Color.White,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = "KM/H",
                    color = TextMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Nitro NOS Button
                HoldButton(
                    onPressChange = onNitroChange,
                    activeColor = NitroBlue,
                    idleColor = RacingCard.copy(alpha = 0.9f),
                    borderColor = if (physics.nitroCharge > 0.1f) NitroBlue else Color.Gray,
                    modifier = Modifier
                        .width(110.dp)
                        .height(44.dp)
                        .testTag("nitro_button")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = "Nitro Boost",
                            tint = if (physics.isNitroActive) Color.White else NitroGlow,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "NITRO",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }

                // Nitro Gauge Bar
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { physics.nitroCharge },
                    modifier = Modifier
                        .width(110.dp)
                        .height(5.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = NitroBlue,
                    trackColor = Color(0x3300B0FF)
                )
            }

            // RIGHT: Pedals (Handbrake, Brake, Gas)
            Row(
                modifier = Modifier.align(Alignment.BottomEnd),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                // Handbrake (E-Brake) Button
                HoldButton(
                    onPressChange = onHandbrakeChange,
                    activeColor = NeonRed,
                    idleColor = Color(0xFF3B151A),
                    borderColor = NeonRed,
                    modifier = Modifier
                        .size(62.dp)
                        .testTag("handbrake_button")
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "HAND",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = "BRAKE",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Foot Brake / Reverse Button
                HoldButton(
                    onPressChange = onBrakeChange,
                    activeColor = Color(0xFFC62828),
                    idleColor = RacingCard.copy(alpha = 0.9f),
                    borderColor = Color(0x55FFFFFF),
                    modifier = Modifier
                        .width(52.dp)
                        .height(72.dp)
                        .testTag("brake_button")
                ) {
                    Text(
                        text = "REV\nBRK",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Throttle / Gas Pedal Button
                HoldButton(
                    onPressChange = onThrottleChange,
                    activeColor = NeonGreen,
                    idleColor = Color(0xFF0F3820),
                    borderColor = NeonGreen,
                    modifier = Modifier
                        .width(62.dp)
                        .height(86.dp)
                        .testTag("gas_pedal_button")
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "GAS",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = "ACCEL",
                            color = Color(0xFFB9F6CA),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HoldButton(
    onPressChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    activeColor: Color = NeonOrange,
    idleColor: Color = RacingCard.copy(alpha = 0.9f),
    borderColor: Color = Color(0x44FFFFFF),
    content: @Composable () -> Unit
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(idleColor)
            .border(2.dp, borderColor, RoundedCornerShape(16.dp))
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        onPressChange(true)
                        tryAwaitRelease()
                        onPressChange(false)
                    }
                )
            }
    ) {
        content()
    }
}
