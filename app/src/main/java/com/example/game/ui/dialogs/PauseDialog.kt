package com.example.game.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonOrange
import com.example.ui.theme.RacingCard
import com.example.ui.theme.RacingDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary

@Composable
fun PauseDialog(
    soundEnabled: Boolean,
    hapticsEnabled: Boolean,
    onResume: () -> Unit,
    onRestart: () -> Unit,
    onToggleSound: (Boolean) -> Unit,
    onToggleHaptics: (Boolean) -> Unit,
    onExitToMenu: () -> Unit
) {
    Dialog(onDismissRequest = onResume) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .clip(RoundedCornerShape(24.dp))
                .background(RacingDark)
                .border(2.dp, NeonOrange, RoundedCornerShape(24.dp))
                .padding(24.dp)
                .testTag("pause_dialog")
        ) {
            Text(
                text = "GAME PAUSED",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Black
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Sound and Haptics toggles
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(RacingCard)
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (soundEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                        contentDescription = "Sound FX",
                        tint = NeonCyan
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(text = "Sound FX", color = TextPrimary, fontWeight = FontWeight.Bold)
                }
                Switch(
                    checked = soundEnabled,
                    onCheckedChange = onToggleSound,
                    colors = SwitchDefaults.colors(checkedThumbColor = NeonOrange, checkedTrackColor = Color(0xFF663000)),
                    modifier = Modifier.testTag("pause_sound_switch")
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(RacingCard)
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Vibration,
                        contentDescription = "Haptics",
                        tint = NeonCyan
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(text = "Haptic Vibration", color = TextPrimary, fontWeight = FontWeight.Bold)
                }
                Switch(
                    checked = hapticsEnabled,
                    onCheckedChange = onToggleHaptics,
                    colors = SwitchDefaults.colors(checkedThumbColor = NeonOrange, checkedTrackColor = Color(0xFF663000)),
                    modifier = Modifier.testTag("pause_haptics_switch")
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Resume Button
            Button(
                onClick = onResume,
                colors = ButtonDefaults.buttonColors(containerColor = NeonOrange),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("pause_resume_button")
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.Black)
                Spacer(modifier = Modifier.width(8.dp))
                Text("RESUME", color = Color.Black, fontWeight = FontWeight.Black)
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Restart Button
            OutlinedButton(
                onClick = onRestart,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("pause_restart_button")
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null, tint = TextPrimary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("RESTART SESSION", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Exit to Menu
            OutlinedButton(
                onClick = onExitToMenu,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextMuted),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("pause_exit_button")
            ) {
                Text("MAIN MENU", fontWeight = FontWeight.Bold)
            }
        }
    }
}
