package com.example.game.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Upgrade
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
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
import com.example.ui.theme.NeonMagenta
import com.example.ui.theme.NeonOrange
import com.example.ui.theme.NeonYellow
import com.example.ui.theme.NitroBlue
import com.example.ui.theme.RacingCard
import com.example.ui.theme.RacingDark
import com.example.ui.theme.RacingSurface
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun GarageScreen(
    storage: GameStorage,
    selectedTrack: Track,
    selectedMode: GameMode,
    onTrackChange: (Track) -> Unit,
    onModeChange: (GameMode) -> Unit,
    onStartDrift: (Car, Track, GameMode) -> Unit,
    onOpenRecords: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    var playerCoins by remember { mutableIntStateOf(storage.coins) }
    var equippedId by remember { mutableStateOf(storage.equippedCarId) }

    val allCars = remember(playerCoins, equippedId) {
        Car.DEFAULT_CARS.map { storage.loadCarWithUpgrades(it) }
    }

    var selectedCarIndex by remember {
        val initialIdx = Car.DEFAULT_CARS.indexOfFirst { it.id == storage.equippedCarId }.coerceAtLeast(0)
        mutableIntStateOf(initialIdx)
    }

    val selectedCar = allCars[selectedCarIndex]

    // Turntable smooth auto-rotation
    val infiniteTransition = rememberInfiniteTransition(label = "podium_spin")
    val autoAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 16000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "podium_rot"
    )
    var dragAngleOffset by remember { mutableFloatStateOf(0f) }

    val paintColors = listOf(
        0xFFECEFF1 to "Panda White",
        0xFF00F5FF to "Electric Cyan",
        0xFFFFD600 to "Sunburst Yellow",
        0xFFFF1744 to "Tokyo Red",
        0xFFA855F7 to "Midnight Purple",
        0xFF76FF03 to "Acid Lime",
        0xFFFF1493 to "Hot Pink",
        0xFF21252B to "Stealth Carbon"
    )

    val underglowColors = listOf(
        0xFF00F5FF to "Cyan",
        0xFFFF007F to "Magenta",
        0xFFFFD600 to "Amber",
        0xFF00E676 to "Green",
        0xFFA855F7 to "Violet",
        0xFFFF1744 to "Crimson"
    )

    val smokeColors = listOf(
        0xFF00F5FF to "Cyan",
        0xFFFF007F to "Magenta",
        0xFFFFD600 to "Yellow",
        0xFF00E676 to "Toxic",
        0xFFFFFFFF to "White"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(RacingDark)
            .testTag("garage_screen")
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // ================= TOP BAR (Landscape Compact) =================
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .background(RacingSurface)
                    .padding(horizontal = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Title
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Bolt,
                        contentDescription = null,
                        tint = NeonOrange,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "DRIFT HORIZON",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                }

                // Center: Track & Mode Quick Selector Pill
                Surface(
                    color = RacingCard,
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan.copy(alpha = 0.5f)),
                    modifier = Modifier.clickable {
                        // Cycle through tracks on click
                        val curIdx = Track.ALL_TRACKS.indexOfFirst { it.id == selectedTrack.id }
                        val nextTrack = Track.ALL_TRACKS[(curIdx + 1) % Track.ALL_TRACKS.size]
                        storage.selectedTrackId = nextTrack.id
                        onTrackChange(nextTrack)
                    }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "🏁 ${selectedTrack.name}",
                            color = NeonCyan,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "• ${selectedMode.title}",
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                    }
                }

                // Right Actions: Coins, Records, Settings
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = RacingCard,
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, NeonYellow.copy(alpha = 0.5f))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Icon(Icons.Default.MonetizationOn, contentDescription = null, tint = NeonYellow, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "$playerCoins", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Black)
                        }
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    IconButton(
                        onClick = onOpenRecords,
                        modifier = Modifier.size(32.dp).testTag("garage_records_button")
                    ) {
                        Icon(Icons.Default.EmojiEvents, contentDescription = "Records", tint = TextPrimary, modifier = Modifier.size(18.dp))
                    }

                    IconButton(
                        onClick = onOpenSettings,
                        modifier = Modifier.size(32.dp).testTag("garage_settings_button")
                    ) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings", tint = TextPrimary, modifier = Modifier.size(18.dp))
                    }
                }
            }

            // ================= MAIN LANDSCAPE BODY (3 COLUMNS) =================
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // ================= LEFT COLUMN: CARS & UPGRADES =================
                Column(
                    modifier = Modifier
                        .width(235.dp)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(14.dp))
                        .background(RacingSurface)
                        .border(1.dp, Color(0x22FFFFFF), RoundedCornerShape(14.dp))
                        .padding(8.dp)
                ) {
                    Text(
                        text = "GARAGE CARS",
                        color = TextMuted,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Car selection list
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(allCars.indices.toList()) { index ->
                            val car = allCars[index]
                            val isSelected = index == selectedCarIndex
                            val isEquipped = car.id == equippedId

                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) Color(0xFF281C14) else RacingCard
                                ),
                                shape = RoundedCornerShape(10.dp),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isSelected) NeonOrange else Color(0x1AFFFFFF)
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedCarIndex = index
                                        if (car.isUnlocked) {
                                            storage.equippedCarId = car.id
                                            equippedId = car.id
                                        }
                                    }
                                    .testTag("car_card_${car.id}")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = car.name,
                                            color = if (isSelected) NeonOrange else TextPrimary,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Black
                                        )
                                        Text(
                                            text = car.series,
                                            color = TextMuted,
                                            fontSize = 9.sp
                                        )
                                    }

                                    if (isEquipped) {
                                        Surface(color = NeonGreen.copy(alpha = 0.2f), shape = RoundedCornerShape(4.dp)) {
                                            Text("EQUIPPED", color = NeonGreen, fontSize = 8.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
                                        }
                                    } else if (!car.isUnlocked) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Lock, contentDescription = null, tint = TextMuted, modifier = Modifier.size(12.dp))
                                            Spacer(modifier = Modifier.width(2.dp))
                                            Text("${car.basePrice}", color = NeonYellow, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }

                        // Tuning Station Header
                        item {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "PERFORMANCE TUNING",
                                color = TextMuted,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                        }

                        // Tuning rows
                        item {
                            val upgradeCostBase = 500
                            LandscapeUpgradeItem(
                                title = "ENGINE / TURBO",
                                level = selectedCar.engineLevel,
                                cost = upgradeCostBase * (selectedCar.engineLevel + 1),
                                canAfford = playerCoins >= (upgradeCostBase * (selectedCar.engineLevel + 1)),
                                onUpgrade = {
                                    val cost = upgradeCostBase * (selectedCar.engineLevel + 1)
                                    if (playerCoins >= cost && selectedCar.engineLevel < 3) {
                                        storage.coins -= cost
                                        storage.setCarEngineLevel(selectedCar.id, selectedCar.engineLevel + 1)
                                        playerCoins = storage.coins
                                    }
                                }
                            )
                        }

                        item {
                            val upgradeCostBase = 500
                            LandscapeUpgradeItem(
                                title = "DRIFT ANGLE KIT",
                                level = selectedCar.driftKitLevel,
                                cost = upgradeCostBase * (selectedCar.driftKitLevel + 1),
                                canAfford = playerCoins >= (upgradeCostBase * (selectedCar.driftKitLevel + 1)),
                                onUpgrade = {
                                    val cost = upgradeCostBase * (selectedCar.driftKitLevel + 1)
                                    if (playerCoins >= cost && selectedCar.driftKitLevel < 3) {
                                        storage.coins -= cost
                                        storage.setCarDriftKitLevel(selectedCar.id, selectedCar.driftKitLevel + 1)
                                        playerCoins = storage.coins
                                    }
                                }
                            )
                        }

                        item {
                            val upgradeCostBase = 500
                            LandscapeUpgradeItem(
                                title = "DRIFT TIRES",
                                level = selectedCar.tireLevel,
                                cost = upgradeCostBase * (selectedCar.tireLevel + 1),
                                canAfford = playerCoins >= (upgradeCostBase * (selectedCar.tireLevel + 1)),
                                onUpgrade = {
                                    val cost = upgradeCostBase * (selectedCar.tireLevel + 1)
                                    if (playerCoins >= cost && selectedCar.tireLevel < 3) {
                                        storage.coins -= cost
                                        storage.setCarTireLevel(selectedCar.id, selectedCar.tireLevel + 1)
                                        playerCoins = storage.coins
                                    }
                                }
                            )
                        }

                        item {
                            val upgradeCostBase = 500
                            LandscapeUpgradeItem(
                                title = "NITROUS (NOS)",
                                level = selectedCar.nitroLevel,
                                cost = upgradeCostBase * (selectedCar.nitroLevel + 1),
                                canAfford = playerCoins >= (upgradeCostBase * (selectedCar.nitroLevel + 1)),
                                onUpgrade = {
                                    val cost = upgradeCostBase * (selectedCar.nitroLevel + 1)
                                    if (playerCoins >= cost && selectedCar.nitroLevel < 3) {
                                        storage.coins -= cost
                                        storage.setCarNitroLevel(selectedCar.id, selectedCar.nitroLevel + 1)
                                        playerCoins = storage.coins
                                    }
                                }
                            )
                        }
                    }
                }

                // ================= CENTER COLUMN: 3D CAR & PLAY BUTTON =================
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Car Title Header
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Surface(
                            color = NeonOrange,
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier.padding(end = 6.dp)
                        ) {
                            Text(
                                text = selectedCar.series.uppercase(),
                                color = Color.Black,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                            )
                        }
                        Text(
                            text = selectedCar.name,
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black
                        )
                    }

                    // 3D Rotating Turntable Stage
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .pointerInput(Unit) {
                                detectDragGestures { _, dragAmount ->
                                    dragAngleOffset += dragAmount.x * 0.5f
                                }
                            }
                    ) {
                        val totalAngle = autoAngle + dragAngleOffset

                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val cx = size.width * 0.5f
                            val cy = size.height * 0.55f

                            // Illuminated Neon Turntable Podium
                            drawOval(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        Color(selectedCar.underglowColor).copy(alpha = 0.5f),
                                        Color(0x22000000),
                                        Color.Transparent
                                    ),
                                    center = Offset(cx, cy),
                                    radius = 160f
                                ),
                                topLeft = Offset(cx - 150f, cy - 65f),
                                size = Size(300f, 130f)
                            )

                            // Podium Rim
                            drawOval(
                                color = Color(selectedCar.underglowColor),
                                topLeft = Offset(cx - 130f, cy - 50f),
                                size = Size(260f, 100f),
                                style = Stroke(width = 2.5f)
                            )

                            // Draw 3D Isometric Rotating Car
                            draw3DShowcaseCar(
                                cx = cx,
                                cy = cy,
                                rotAngleDeg = totalAngle,
                                car = selectedCar
                            )
                        }

                        // Touch hint
                        Text(
                            text = "DRAG TO SPIN 360°",
                            color = TextMuted.copy(alpha = 0.5f),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .padding(top = 4.dp)
                        )
                    }

                    // Car Specs Summary Pills
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        SpecPill("SPEED", "${selectedCar.topSpeed.toInt()} KM/H", NeonOrange)
                        SpecPill("ACCEL", "${selectedCar.acceleration.toInt()}", NeonYellow)
                        SpecPill("DRIFT", "${String.format("%.2f", selectedCar.driftFactor)}x", NeonCyan)
                        SpecPill("NITRO", "${String.format("%.1f", selectedCar.nitroDurationSec)}s", NitroBlue)
                    }

                    // ================= BIG GLOWING PLAY BUTTON =================
                    if (selectedCar.isUnlocked) {
                        Button(
                            onClick = {
                                storage.equippedCarId = selectedCar.id
                                onStartDrift(selectedCar, selectedTrack, selectedMode)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = NeonOrange
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .border(2.dp, Color(0xFFFFA000), RoundedCornerShape(16.dp))
                                .testTag("start_drift_button")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    tint = Color.Black,
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "START DRIFT",
                                    color = Color.Black,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.sp
                                )
                            }
                        }
                    } else {
                        val canUnlock = playerCoins >= selectedCar.basePrice
                        Button(
                            onClick = {
                                if (canUnlock) {
                                    storage.coins -= selectedCar.basePrice
                                    storage.unlockCar(selectedCar.id)
                                    storage.equippedCarId = selectedCar.id
                                    equippedId = selectedCar.id
                                    playerCoins = storage.coins
                                }
                            },
                            enabled = canUnlock,
                            colors = ButtonDefaults.buttonColors(containerColor = NeonYellow),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .testTag("garage_unlock_button")
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Lock, contentDescription = null, tint = Color.Black)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "UNLOCK CAR FOR ${selectedCar.basePrice} COINS",
                                    color = Color.Black,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                    }
                }

                // ================= RIGHT COLUMN: COLOR & TRACK CUSTOMIZER =================
                Column(
                    modifier = Modifier
                        .width(225.dp)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(14.dp))
                        .background(RacingSurface)
                        .border(1.dp, Color(0x22FFFFFF), RoundedCornerShape(14.dp))
                        .padding(8.dp)
                ) {
                    Text(
                        text = "CUSTOM FINISH & CIRCUIT",
                        color = TextMuted,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // 1. Paint Finishes
                        item {
                            Text("BODY PAINT", color = TextPrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                items(paintColors) { (colorHex, name) ->
                                    val isSelected = selectedCar.bodyColor == colorHex
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(Color(colorHex))
                                            .border(1.5.dp, if (isSelected) NeonOrange else Color(0x33FFFFFF), CircleShape)
                                            .clickable {
                                                storage.setCarCustomPaint(selectedCar.id, colorHex)
                                                playerCoins = storage.coins
                                            }
                                    ) {
                                        if (isSelected) {
                                            Icon(Icons.Default.Check, contentDescription = null, tint = if (colorHex == 0xFFECEFF1 || colorHex == 0xFFFFD600) Color.Black else Color.White, modifier = Modifier.size(14.dp).align(Alignment.Center))
                                        }
                                    }
                                }
                            }
                        }

                        // 2. Underglow
                        item {
                            Text("NEON UNDERGLOW", color = TextPrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                items(underglowColors) { (colorHex, name) ->
                                    val isSelected = selectedCar.underglowColor == colorHex
                                    Box(
                                        modifier = Modifier
                                            .size(26.dp)
                                            .clip(CircleShape)
                                            .background(Color(colorHex))
                                            .border(1.5.dp, if (isSelected) Color.White else Color(0x33FFFFFF), CircleShape)
                                            .clickable {
                                                storage.setCarUnderglowColor(selectedCar.id, colorHex)
                                                playerCoins = storage.coins
                                            }
                                    ) {
                                        if (isSelected) {
                                            Icon(Icons.Default.Check, contentDescription = null, tint = Color.Black, modifier = Modifier.size(13.dp).align(Alignment.Center))
                                        }
                                    }
                                }
                            }
                        }

                        // 3. Smoke Color
                        item {
                            Text("TIRE SMOKE", color = TextPrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                items(smokeColors) { (colorHex, name) ->
                                    val isSelected = selectedCar.smokeColor == colorHex
                                    Box(
                                        modifier = Modifier
                                            .size(26.dp)
                                            .clip(CircleShape)
                                            .background(Color(colorHex))
                                            .border(1.5.dp, if (isSelected) Color.White else Color(0x33FFFFFF), CircleShape)
                                            .clickable {
                                                storage.setCarSmokeColor(selectedCar.id, colorHex)
                                                playerCoins = storage.coins
                                            }
                                    ) {
                                        if (isSelected) {
                                            Icon(Icons.Default.Check, contentDescription = null, tint = Color.Black, modifier = Modifier.size(13.dp).align(Alignment.Center))
                                        }
                                    }
                                }
                            }
                        }

                        // 4. Track Selection list
                        item {
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("CIRCUITS", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                        }

                        items(Track.ALL_TRACKS) { trk ->
                            val isTrkSelected = trk.id == selectedTrack.id
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isTrkSelected) Color(0xFF1F2533) else RacingCard
                                ),
                                shape = RoundedCornerShape(8.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, if (isTrkSelected) NeonCyan else Color(0x1AFFFFFF)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        storage.selectedTrackId = trk.id
                                        onTrackChange(trk)
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(trk.name, color = if (isTrkSelected) NeonCyan else TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Black)
                                        Text(trk.difficulty, color = Color(trk.curbColor), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    }
                                    if (isTrkSelected) {
                                        Icon(Icons.Default.Check, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(14.dp))
                                    }
                                }
                            }
                        }

                        // 5. Game Mode Selection
                        item {
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("GAME MODE", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                        }

                        items(GameMode.values()) { gm ->
                            val isModeSelected = gm == selectedMode
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isModeSelected) Color(0xFF261D15) else RacingCard
                                ),
                                shape = RoundedCornerShape(8.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, if (isModeSelected) NeonOrange else Color(0x1AFFFFFF)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onModeChange(gm) }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(gm.title, color = if (isModeSelected) NeonOrange else TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Black)
                                        Text(gm.subtitle, color = TextMuted, fontSize = 9.sp)
                                    }
                                    if (isModeSelected) {
                                        Icon(Icons.Default.Check, contentDescription = null, tint = NeonOrange, modifier = Modifier.size(14.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LandscapeUpgradeItem(
    title: String,
    level: Int,
    cost: Int,
    canAfford: Boolean,
    onUpgrade: () -> Unit
) {
    val isMax = level >= 3
    Card(
        colors = CardDefaults.cardColors(containerColor = RacingCard),
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x1AFFFFFF)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = TextPrimary, fontSize = 10.sp, fontWeight = FontWeight.Black)
                Text(if (isMax) "MAX" else "LV $level/3", color = NeonCyan, fontSize = 9.sp, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = onUpgrade,
                enabled = !isMax && canAfford,
                colors = ButtonDefaults.buttonColors(containerColor = NeonOrange),
                shape = RoundedCornerShape(6.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                modifier = Modifier.height(28.dp)
            ) {
                Text(if (isMax) "MAX" else "$cost", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Black)
            }
        }
    }
}

@Composable
private fun SpecPill(label: String, value: String, color: Color) {
    Surface(
        color = RacingCard,
        shape = RoundedCornerShape(6.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.4f))
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Text(label, color = TextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.width(4.dp))
            Text(value, color = color, fontSize = 10.sp, fontWeight = FontWeight.Black)
        }
    }
}

private fun DrawScope.draw3DShowcaseCar(
    cx: Float,
    cy: Float,
    rotAngleDeg: Float,
    car: Car
) {
    val rad = rotAngleDeg * (PI.toFloat() / 180f)
    val cosR = cos(rad)
    val sinR = sin(rad)

    val scaleX = 1.0f
    val scaleY = 0.52f // isometric perspective tilt

    fun isoProject(x: Float, y: Float, z: Float): Offset {
        val rx = x * cosR - y * sinR
        val ry = x * sinR + y * cosR
        val screenX = cx + rx * scaleX
        val screenY = cy + ry * scaleY - z
        return Offset(screenX, screenY)
    }

    val length = 75f
    val width = 38f
    val bodyH = 14f
    val roofH = 26f

    val bodyColor = Color(car.bodyColor)
    val accentColor = Color(car.accentColor)

    // Shadow & Underglow
    drawOval(
        color = Color(car.underglowColor).copy(alpha = 0.6f),
        topLeft = Offset(cx - 75f, cy - 25f),
        size = Size(150f, 50f)
    )

    // 3D Wheels
    val wheelOffsets = listOf(
        Pair(length * 0.35f, -width * 0.52f),
        Pair(length * 0.35f, width * 0.52f),
        Pair(-length * 0.35f, -width * 0.52f),
        Pair(-length * 0.35f, width * 0.52f)
    )
    for ((wx, wy) in wheelOffsets) {
        val wp = isoProject(wx, wy, 0f)
        drawCircle(color = Color(0xFF11141A), radius = 7f, center = wp)
        drawCircle(color = Color(0xFFCBD5E1), radius = 2.5f, center = wp)
    }

    // Body bottom corners
    val bFL = isoProject(length * 0.5f, -width * 0.5f, 4f)
    val bFR = isoProject(length * 0.5f, width * 0.5f, 4f)
    val bRR = isoProject(-length * 0.5f, width * 0.5f, 4f)
    val bRL = isoProject(-length * 0.5f, -width * 0.5f, 4f)

    // Body waist corners
    val mFL = isoProject(length * 0.48f, -width * 0.48f, bodyH)
    val mFR = isoProject(length * 0.48f, width * 0.48f, bodyH)
    val mRR = isoProject(-length * 0.48f, width * 0.48f, bodyH)
    val mRL = isoProject(-length * 0.48f, -width * 0.48f, bodyH)

    // Cabin roof corners
    val cFL = isoProject(length * 0.12f, -width * 0.38f, roofH)
    val cFR = isoProject(length * 0.12f, width * 0.38f, roofH)
    val cRR = isoProject(-length * 0.26f, width * 0.38f, roofH)
    val cRL = isoProject(-length * 0.26f, -width * 0.38f, roofH)

    // Draw Body Lower Faces
    val path = Path()

    // Front Face
    path.reset()
    path.moveTo(bFL.x, bFL.y); path.lineTo(bFR.x, bFR.y); path.lineTo(mFR.x, mFR.y); path.lineTo(mFL.x, mFL.y); path.close()
    drawPath(path, accentColor) // Cyan front splitter

    // Side Faces (Lower in cyan, upper in orange)
    path.reset()
    path.moveTo(bFR.x, bFR.y); path.lineTo(bRR.x, bRR.y); path.lineTo(mRR.x, mRR.y); path.lineTo(mFR.x, mFR.y); path.close()
    drawPath(path, accentColor)

    path.reset()
    path.moveTo(bFL.x, bFL.y); path.lineTo(bRL.x, bRL.y); path.lineTo(mRL.x, mRL.y); path.lineTo(mFL.x, mFL.y); path.close()
    drawPath(path, accentColor)

    // Rear Face (Diffuser in cyan with red LED tail light)
    path.reset()
    path.moveTo(bRL.x, bRL.y); path.lineTo(bRR.x, bRR.y); path.lineTo(mRR.x, mRR.y); path.lineTo(mRL.x, mRL.y); path.close()
    drawPath(path, accentColor.copy(alpha = 0.85f))
    drawLine(Color(0xFFFF1744), Offset(mRL.x * 0.9f + mRR.x * 0.1f, mRL.y * 0.9f + mRR.y * 0.1f), Offset(mRL.x * 0.1f + mRR.x * 0.9f, mRL.y * 0.1f + mRR.y * 0.9f), strokeWidth = 3f)

    // Hood Deck (Metallic Sunset Orange)
    path.reset()
    path.moveTo(mFL.x, mFL.y); path.lineTo(mFR.x, mFR.y); path.lineTo(cFR.x, cFR.y); path.lineTo(cFL.x, cFL.y); path.close()
    drawPath(path, bodyColor)
    // Cyan center stripe
    drawLine(accentColor, Offset((mFL.x + mFR.x) * 0.5f, (mFL.y + mFR.y) * 0.5f), Offset((cFL.x + cFR.x) * 0.5f, (cFL.y + cFR.y) * 0.5f), strokeWidth = 5f)

    // Windshield (Tinted Glass)
    path.reset()
    path.moveTo(mFL.x * 0.7f + cFL.x * 0.3f, mFL.y * 0.7f + cFL.y * 0.3f)
    path.lineTo(mFR.x * 0.7f + cFR.x * 0.3f, mFR.y * 0.7f + cFR.y * 0.3f)
    path.lineTo(cFR.x, cFR.y)
    path.lineTo(cFL.x, cFL.y)
    path.close()
    drawPath(path, Color(0xFF0F172A))

    // Roof (Sunset Orange)
    path.reset()
    path.moveTo(cFL.x, cFL.y); path.lineTo(cFR.x, cFR.y); path.lineTo(cRR.x, cRR.y); path.lineTo(cRL.x, cRL.y); path.close()
    drawPath(path, bodyColor)

    // Rear Trunk Deck
    path.reset()
    path.moveTo(cRL.x, cRL.y); path.lineTo(cRR.x, cRR.y); path.lineTo(mRR.x, mRR.y); path.lineTo(mRL.x, mRL.y); path.close()
    drawPath(path, bodyColor)

    // Carbon-Fiber Rear Spoiler Wing
    val sL_bot = isoProject(-length * 0.44f, -width * 0.24f, bodyH)
    val sL_top = isoProject(-length * 0.44f, -width * 0.24f, roofH + 5f)
    val sR_bot = isoProject(-length * 0.44f, width * 0.24f, bodyH)
    val sR_top = isoProject(-length * 0.44f, width * 0.24f, roofH + 5f)
    val sL = isoProject(-length * 0.52f, -width * 0.52f, roofH + 5f)
    val sR = isoProject(-length * 0.52f, width * 0.52f, roofH + 5f)

    drawLine(Color(0xFF0F172A), sL_bot, sL_top, strokeWidth = 2.5f)
    drawLine(Color(0xFF0F172A), sR_bot, sR_top, strokeWidth = 2.5f)
    drawLine(accentColor, sL, sR, strokeWidth = 5f, cap = StrokeCap.Round)
}
