package com.example.game.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.storage.GameStorage
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonOrange
import com.example.ui.theme.RacingCard
import com.example.ui.theme.RacingDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary

@Composable
fun SettingsScreen(
    storage: GameStorage,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }

    var soundEnabled by remember { mutableStateOf(storage.soundEnabled) }
    var hapticsEnabled by remember { mutableStateOf(storage.hapticsEnabled) }
    var sensitivity by remember { mutableFloatStateOf(storage.steeringSensitivity) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(RacingDark)
            .testTag("settings_screen")
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp, vertical = 24.dp)
        ) {
            // Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(RacingCard)
                            .testTag("settings_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "GAME SETTINGS",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Audio, haptics & steering physics",
                            color = TextMuted,
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }

            // Audio & Feedback Section
            item {
                Text(
                    text = "AUDIO & HAPTICS",
                    color = TextMuted,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp,
                    modifier = Modifier.padding(bottom = 10.dp)
                )

                Card(
                    colors = CardDefaults.cardColors(containerColor = RacingCard),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x1AFFFFFF)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.VolumeUp, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text("Sound Effects & Engine Synth", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text("Realistic RPMs & tire screeches", color = TextMuted, fontSize = 11.sp)
                                }
                            }
                            Switch(
                                checked = soundEnabled,
                                onCheckedChange = {
                                    soundEnabled = it
                                    storage.soundEnabled = it
                                },
                                colors = SwitchDefaults.colors(checkedThumbColor = NeonOrange, checkedTrackColor = Color(0xFF663000)),
                                modifier = Modifier.testTag("setting_sound_switch")
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Vibration, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text("Haptic Vibration", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text("Tire slip, wall scrape & nitro pulses", color = TextMuted, fontSize = 11.sp)
                                }
                            }
                            Switch(
                                checked = hapticsEnabled,
                                onCheckedChange = {
                                    hapticsEnabled = it
                                    storage.hapticsEnabled = it
                                },
                                colors = SwitchDefaults.colors(checkedThumbColor = NeonOrange, checkedTrackColor = Color(0xFF663000)),
                                modifier = Modifier.testTag("setting_haptics_switch")
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }

            // Controls & Steering Sensitivity
            item {
                Text(
                    text = "CONTROLS & CALIBRATION",
                    color = TextMuted,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp,
                    modifier = Modifier.padding(bottom = 10.dp)
                )

                Card(
                    colors = CardDefaults.cardColors(containerColor = RacingCard),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x1AFFFFFF)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Tune, contentDescription = null, tint = NeonOrange, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(10.dp))
                                Text("Steering Sensitivity", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                            val label = when {
                                sensitivity > 1.3f -> "Ultra Fast"
                                sensitivity > 1.1f -> "Sharp"
                                sensitivity < 0.85f -> "Smooth"
                                else -> "Balanced (1.0x)"
                            }
                            Text(text = label, color = NeonOrange, fontWeight = FontWeight.Black, fontSize = 12.sp)
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Slider(
                            value = sensitivity,
                            onValueChange = {
                                sensitivity = it
                                storage.steeringSensitivity = it
                            },
                            valueRange = 0.6f..1.8f,
                            colors = SliderDefaults.colors(
                                thumbColor = NeonOrange,
                                activeTrackColor = NeonOrange,
                                inactiveTrackColor = Color(0x33FFFFFF)
                            ),
                            modifier = Modifier.testTag("steering_sensitivity_slider")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }

            // How To Drift Guide
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.HelpOutline, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "HOW TO DRIFT LIKE A PRO",
                        color = TextMuted,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Card(
                    colors = CardDefaults.cardColors(containerColor = RacingCard),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x1AFFFFFF)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        GuideStep(
                            step = "1",
                            title = "INITIATE THE SLIDE",
                            desc = "Build speed with GAS. Steer toward the corner apex and tap HANDBRAKE to kick out the rear tail."
                        )
                        GuideStep(
                            step = "2",
                            title = "COUNTER-STEER",
                            desc = "As the tail swings, steer OPPOSITE to the turn. Opposite-lock stabilizes your vehicle and prevents spin-outs."
                        )
                        GuideStep(
                            step = "3",
                            title = "FEATHER THE GAS & NITRO",
                            desc = "Hold throttle to keep wheelspin high. Tap NITRO to extend long wide-arc drifts."
                        )
                        GuideStep(
                            step = "4",
                            title = "CLIP THE APEX",
                            desc = "Slide close to the glowing cyan poles for instant +400 Apex Clipping points and multiplier increases!"
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GuideStep(step: String, title: String, desc: String) {
    Row(verticalAlignment = Alignment.Top) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(26.dp)
                .clip(CircleShape)
                .background(NeonOrange.copy(alpha = 0.2f))
        ) {
            Text(text = step, color = NeonOrange, fontSize = 12.sp, fontWeight = FontWeight.Black)
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(text = title, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Black)
            Text(text = desc, color = TextMuted, fontSize = 12.sp, lineHeight = 16.sp)
        }
    }
}
