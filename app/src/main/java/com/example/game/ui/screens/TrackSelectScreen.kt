package com.example.game.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.model.GameMode
import com.example.game.model.Track
import com.example.game.storage.GameStorage
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonOrange
import com.example.ui.theme.NeonYellow
import com.example.ui.theme.RacingCard
import com.example.ui.theme.RacingDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary

@Composable
fun TrackSelectScreen(
    storage: GameStorage,
    selectedTrack: Track,
    selectedMode: GameMode,
    onTrackSelect: (Track) -> Unit,
    onModeSelect: (GameMode) -> Unit,
    onStartGame: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }

    val tracks = Track.ALL_TRACKS
    val modes = GameMode.values()
    var selectedModeIndex by remember { mutableIntStateOf(modes.indexOf(selectedMode).coerceAtLeast(0)) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(RacingDark)
            .testTag("track_select_screen")
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(RacingCard)
                        .testTag("track_back_button")
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
                        text = "TRACKS & GAME MODES",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Choose your circuit and drift rules",
                        color = TextMuted,
                        fontSize = 12.sp
                    )
                }
            }

            // Mode Selector Tabs
            TabRow(
                selectedTabIndex = selectedModeIndex,
                containerColor = RacingCard,
                contentColor = NeonCyan,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedModeIndex]),
                        color = NeonCyan,
                        height = 3.dp
                    )
                },
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                modes.forEachIndexed { index, mode ->
                    Tab(
                        selected = selectedModeIndex == index,
                        onClick = {
                            selectedModeIndex = index
                            onModeSelect(mode)
                        },
                        text = {
                            Text(
                                text = mode.title,
                                color = if (selectedModeIndex == index) NeonCyan else TextMuted,
                                fontSize = 12.sp,
                                fontWeight = if (selectedModeIndex == index) FontWeight.Black else FontWeight.Bold
                            )
                        },
                        modifier = Modifier.testTag("mode_tab_${mode.name}")
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Track List
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 16.dp)
            ) {
                items(tracks) { track ->
                    val isSelected = track.id == selectedTrack.id
                    val currentHighScore = storage.getHighScore(track.id, modes[selectedModeIndex].name)

                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) Color(0xFF1D2230) else RacingCard
                        ),
                        shape = RoundedCornerShape(18.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            2.dp,
                            if (isSelected) NeonCyan else Color(0x22FFFFFF)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                storage.selectedTrackId = track.id
                                onTrackSelect(track)
                            }
                            .testTag("track_card_${track.id}")
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Surface(
                                            color = Color(track.curbColor),
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text(
                                                text = track.difficulty,
                                                color = Color.Black,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Black,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = track.location,
                                            color = TextMuted,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Text(
                                        text = track.name,
                                        color = TextPrimary,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }

                                // Mini Track Vector Preview
                                Box(
                                    modifier = Modifier
                                        .size(68.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color(0x33000000))
                                        .border(1.dp, Color(0x22FFFFFF), RoundedCornerShape(10.dp))
                                ) {
                                    Canvas(modifier = Modifier.fillMaxSize().padding(6.dp)) {
                                        val sx = size.width / track.worldWidth
                                        val sy = size.height / track.worldHeight
                                        if (track.centerLine.isNotEmpty()) {
                                            val p = Path().apply {
                                                moveTo(track.centerLine[0].x * sx, track.centerLine[0].y * sy)
                                                for (i in 1 until track.centerLine.size) {
                                                    lineTo(track.centerLine[i].x * sx, track.centerLine[i].y * sy)
                                                }
                                            }
                                            drawPath(
                                                path = p,
                                                color = Color(track.barrierGlowColor),
                                                style = Stroke(width = 3.5f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = track.description,
                                color = TextMuted,
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0x22FFFFFF))
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = NeonYellow, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(text = "RECORD", color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                                Text(
                                    text = "$currentHighScore PTS",
                                    color = NeonYellow,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                    }
                }
            }

            // Launch Button
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
                Button(
                    onClick = onStartGame,
                    colors = ButtonDefaults.buttonColors(containerColor = NeonOrange),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("launch_race_button")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.Black, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "LAUNCH ${selectedTrack.name.uppercase()}",
                            color = Color.Black,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
        }
    }
}
