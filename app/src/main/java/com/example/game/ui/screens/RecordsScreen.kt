package com.example.game.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
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
import com.example.game.model.Car
import com.example.game.model.GameMode
import com.example.game.model.Track
import com.example.game.storage.GameStorage
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonOrange
import com.example.ui.theme.NeonYellow
import com.example.ui.theme.RacingCard
import com.example.ui.theme.RacingDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

data class Achievement(
    val title: String,
    val description: String,
    val isUnlocked: Boolean,
    val rewardCoins: Int
)

@Composable
fun RecordsScreen(
    storage: GameStorage,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }

    val careerScore = storage.careerDriftScore
    val bestDrift = storage.bestSingleDrift
    val unlockedCarsCount = Car.DEFAULT_CARS.count { storage.isCarUnlocked(it.id) }

    val achievements = listOf(
        Achievement(
            title = "FIRST SLIDE",
            description = "Complete your first drift session",
            isUnlocked = careerScore > 0,
            rewardCoins = 300
        ),
        Achievement(
            title = "TIRE SHREDDER",
            description = "Hold a continuous drift for 5+ seconds",
            isUnlocked = bestDrift >= 5.0f,
            rewardCoins = 500
        ),
        Achievement(
            title = "DRIFT DEMON",
            description = "Hold a continuous drift for 10+ seconds",
            isUnlocked = bestDrift >= 10.0f,
            rewardCoins = 1000
        ),
        Achievement(
            title = "GARAGE COLLECTOR",
            description = "Unlock 3 or more drift cars",
            isUnlocked = unlockedCarsCount >= 3,
            rewardCoins = 1500
        ),
        Achievement(
            title = "DRIFT LEGEND",
            description = "Reach over 100,000 career drift points",
            isUnlocked = careerScore >= 100000,
            rewardCoins = 3000
        )
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(RacingDark)
            .testTag("records_screen")
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
                            .testTag("records_back_button")
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
                            text = "CAREER & RECORDS",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Personal milestones & track records",
                            color = TextMuted,
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }

            // Career Highlights Cards
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    CareerStatCard(
                        title = "CAREER POINTS",
                        value = "$careerScore",
                        accentColor = NeonCyan,
                        modifier = Modifier.weight(1f)
                    )
                    CareerStatCard(
                        title = "LONGEST DRIFT",
                        value = String.format("%.1fs", bestDrift),
                        accentColor = NeonOrange,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    CareerStatCard(
                        title = "CARS OWNED",
                        value = "$unlockedCarsCount / 5",
                        accentColor = NeonYellow,
                        modifier = Modifier.weight(1f)
                    )
                    CareerStatCard(
                        title = "DRIVER TIER",
                        value = if (careerScore > 50000) "PRO DRIFTER" else "STREET ROOKIE",
                        accentColor = NeonGreen,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(26.dp))
            }

            // Circuit High Scores List
            item {
                Text(
                    text = "CIRCUIT HIGH SCORES",
                    color = TextMuted,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }

            items(Track.ALL_TRACKS) { track ->
                val attackScore = storage.getHighScore(track.id, GameMode.DRIFT_ATTACK.name)
                val circuitScore = storage.getHighScore(track.id, GameMode.TIME_CIRCUIT.name)

                Card(
                    colors = CardDefaults.cardColors(containerColor = RacingCard),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x1AFFFFFF)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = track.name,
                                color = TextPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Black
                            )
                            Surface(
                                color = Color(track.curbColor).copy(alpha = 0.2f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = track.difficulty,
                                    color = Color(track.curbColor),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Drift Attack (90s):", color = TextMuted, fontSize = 12.sp)
                            Text(text = "$attackScore PTS", color = NeonCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Tandem Circuit (2 Laps):", color = TextMuted, fontSize = 12.sp)
                            Text(text = "$circuitScore PTS", color = NeonOrange, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Driver Achievements
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "DRIVER ACHIEVEMENTS",
                    color = TextMuted,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }

            items(achievements) { ach ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = RacingCard),
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (ach.isUnlocked) NeonGreen.copy(alpha = 0.5f) else Color(0x1AFFFFFF)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(if (ach.isUnlocked) NeonGreen.copy(alpha = 0.2f) else Color(0x1AFFFFFF))
                        ) {
                            Icon(
                                imageVector = if (ach.isUnlocked) Icons.Default.CheckCircle else Icons.Default.Lock,
                                contentDescription = null,
                                tint = if (ach.isUnlocked) NeonGreen else TextMuted,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = ach.title,
                                color = if (ach.isUnlocked) TextPrimary else TextMuted,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                text = ach.description,
                                color = TextMuted,
                                fontSize = 11.sp
                            )
                        }

                        Surface(
                            color = Color(0x22FFFFFF),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "+${ach.rewardCoins}C",
                                color = NeonYellow,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CareerStatCard(
    title: String,
    value: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = RacingCard),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x1AFFFFFF)),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(text = title, color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = value, color = accentColor, fontSize = 20.sp, fontWeight = FontWeight.Black)
        }
    }
}
