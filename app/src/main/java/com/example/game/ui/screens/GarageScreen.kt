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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Upgrade
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.model.Car
import com.example.game.storage.GameStorage
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonOrange
import com.example.ui.theme.NeonYellow
import com.example.ui.theme.NitroBlue
import com.example.ui.theme.RacingCard
import com.example.ui.theme.RacingDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun GarageScreen(
    storage: GameStorage,
    onBack: () -> Unit,
    onEquipCar: (Car) -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }

    // Load available cars from storage
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
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 32.dp)
        ) {
            // Top Bar
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(RacingCard)
                                .testTag("garage_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back to Menu",
                                tint = TextPrimary
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "DRIFT GARAGE",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                    }

                    // Coins Pill
                    Surface(
                        color = RacingCard,
                        shape = RoundedCornerShape(20.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, NeonYellow.copy(alpha = 0.6f))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.MonetizationOn, contentDescription = null, tint = NeonYellow, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "$playerCoins", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Black)
                        }
                    }
                }
            }

            // Horizontal Car Carousel Tabs
            item {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(allCars.indices.toList()) { index ->
                        val car = allCars[index]
                        val isSelected = index == selectedCarIndex
                        val isEquipped = car.id == equippedId

                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) Color(0xFF261D15) else RacingCard
                            ),
                            shape = RoundedCornerShape(14.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                1.5.dp,
                                if (isSelected) NeonOrange else Color(0x22FFFFFF)
                            ),
                            modifier = Modifier
                                .clickable { selectedCarIndex = index }
                                .testTag("car_tab_${car.id}")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                            ) {
                                if (!car.isUnlocked) {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = "Locked",
                                        tint = TextMuted,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                } else if (isEquipped) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Equipped",
                                        tint = NeonGreen,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                }

                                Text(
                                    text = car.name,
                                    color = if (isSelected) NeonOrange else TextPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // Car Visual Render Showcase
            item {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = RacingCard),
                        shape = RoundedCornerShape(22.dp),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0x33FFFFFF)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            // Canvas Car Showcase
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val cx = size.width / 2f
                                val cy = size.height / 2f

                                // Showcase podium spotlight
                                drawOval(
                                    color = Color(selectedCar.accentColor).copy(alpha = 0.25f),
                                    topLeft = Offset(cx - 130f, cy - 65f),
                                    size = Size(260f, 130f)
                                )

                                // Car shadow
                                drawRoundRect(
                                    color = Color(0x99000000),
                                    topLeft = Offset(cx - 85f, cy - 40f),
                                    size = Size(170f, 80f),
                                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(16f, 16f)
                                )

                                // Underglow
                                drawRoundRect(
                                    color = Color(selectedCar.accentColor).copy(alpha = 0.6f),
                                    topLeft = Offset(cx - 85f, cy - 44f),
                                    size = Size(170f, 88f),
                                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(20f, 20f)
                                )

                                // Wheels
                                drawRoundRect(
                                    color = Color(0xFF111317),
                                    topLeft = Offset(cx - 65f, cy - 48f),
                                    size = Size(32f, 14f),
                                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
                                )
                                drawRoundRect(
                                    color = Color(0xFF111317),
                                    topLeft = Offset(cx - 65f, cy + 34f),
                                    size = Size(32f, 14f),
                                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
                                )
                                drawRoundRect(
                                    color = Color(0xFF111317),
                                    topLeft = Offset(cx + 33f, cy - 48f),
                                    size = Size(32f, 14f),
                                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
                                )
                                drawRoundRect(
                                    color = Color(0xFF111317),
                                    topLeft = Offset(cx + 33f, cy + 34f),
                                    size = Size(32f, 14f),
                                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
                                )

                                // Car body
                                drawRoundRect(
                                    color = Color(selectedCar.bodyColor),
                                    topLeft = Offset(cx - 80f, cy - 40f),
                                    size = Size(160f, 80f),
                                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(18f, 18f)
                                )

                                // Racing stripe
                                drawRect(
                                    color = Color(selectedCar.accentColor),
                                    topLeft = Offset(cx - 75f, cy - 14f),
                                    size = Size(150f, 28f)
                                )

                                // Windshields
                                drawRoundRect(
                                    color = Color(0xFF1E293B),
                                    topLeft = Offset(cx + 8f, cy - 30f),
                                    size = Size(36f, 60f),
                                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f, 8f)
                                )
                                drawRoundRect(
                                    color = Color(0xFF1E293B),
                                    topLeft = Offset(cx - 52f, cy - 28f),
                                    size = Size(28f, 56f),
                                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f, 8f)
                                )

                                // Spoiler
                                drawRoundRect(
                                    color = Color(selectedCar.accentColor),
                                    topLeft = Offset(cx - 84f, cy - 38f),
                                    size = Size(10f, 76f),
                                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
                                )
                            }

                            // Car Series Tag
                            Surface(
                                color = Color(0x99000000),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .padding(12.dp)
                            ) {
                                Text(
                                    text = selectedCar.series,
                                    color = NeonCyan,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = selectedCar.name,
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = selectedCar.description,
                        color = TextMuted,
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                }
            }

            // Paint Shop (Livery Color Picker)
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Palette, contentDescription = null, tint = NeonOrange, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "CUSTOM PAINT FINISH",
                            color = TextMuted,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(paintColors) { (colorHex, name) ->
                            val isCurrentColor = selectedCar.bodyColor == colorHex
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(Color(colorHex))
                                    .border(
                                        2.dp,
                                        if (isCurrentColor) NeonOrange else Color(0x44FFFFFF),
                                        CircleShape
                                    )
                                    .clickable {
                                        storage.setCarCustomPaint(selectedCar.id, colorHex)
                                        playerCoins = storage.coins
                                    }
                                    .testTag("paint_color_${colorHex}")
                            ) {
                                if (isCurrentColor) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = name,
                                        tint = if (colorHex == 0xFFECEFF1 || colorHex == 0xFFFFD600 || colorHex == 0xFF76FF03) Color.Black else Color.White,
                                        modifier = Modifier
                                            .size(20.dp)
                                            .align(Alignment.Center)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Underglow Neon Color Selector
                    Text(
                        text = "NEON UNDERGLOW",
                        color = TextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(underglowColors) { (colorHex, name) ->
                            val isCurrentUnderglow = selectedCar.underglowColor == colorHex
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(colorHex))
                                    .border(
                                        2.dp,
                                        if (isCurrentUnderglow) Color.White else Color(0x33FFFFFF),
                                        CircleShape
                                    )
                                    .clickable {
                                        storage.setCarUnderglowColor(selectedCar.id, colorHex)
                                        playerCoins = storage.coins
                                    }
                                    .testTag("underglow_color_${colorHex}")
                            ) {
                                if (isCurrentUnderglow) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = name,
                                        tint = if (colorHex == 0xFFFFD600 || colorHex == 0xFF00E676) Color.Black else Color.White,
                                        modifier = Modifier.size(18.dp).align(Alignment.Center)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Tire Smoke Color Selector
                    Text(
                        text = "TIRE SMOKE COLOR",
                        color = TextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(smokeColors) { (colorHex, name) ->
                            val isCurrentSmoke = selectedCar.smokeColor == colorHex
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(colorHex))
                                    .border(
                                        2.dp,
                                        if (isCurrentSmoke) Color.White else Color(0x33FFFFFF),
                                        CircleShape
                                    )
                                    .clickable {
                                        storage.setCarSmokeColor(selectedCar.id, colorHex)
                                        playerCoins = storage.coins
                                    }
                                    .testTag("smoke_color_${colorHex}")
                            ) {
                                if (isCurrentSmoke) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = name,
                                        tint = if (colorHex == 0xFFFFFFFF || colorHex == 0xFFFFD600) Color.Black else Color.White,
                                        modifier = Modifier.size(18.dp).align(Alignment.Center)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Performance Telemetry Stats
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                    Text(
                        text = "PERFORMANCE SPECS",
                        color = TextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    StatBar(name = "TOP SPEED", value = selectedCar.topSpeed, maxValue = 650f, color = NeonOrange)
                    StatBar(name = "ACCELERATION", value = selectedCar.acceleration, maxValue = 600f, color = NeonYellow)
                    StatBar(name = "DRIFT ANGLE FACTOR", value = selectedCar.driftFactor, maxValue = 2.0f, color = NeonCyan)
                    StatBar(name = "NITRO DURATION", value = selectedCar.nitroDurationSec, maxValue = 7.0f, color = NitroBlue)
                }
            }

            // Upgrades Tuning Station
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Upgrade, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "PERFORMANCE UPGRADES",
                            color = TextMuted,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    val upgradeCostBase = 500

                    UpgradeRow(
                        title = "TURBO & ENGINE",
                        description = "+8% Top Speed, +10% Acceleration",
                        currentLevel = selectedCar.engineLevel,
                        cost = (upgradeCostBase * (selectedCar.engineLevel + 1)),
                        canAfford = playerCoins >= (upgradeCostBase * (selectedCar.engineLevel + 1)),
                        isMax = selectedCar.engineLevel >= 3,
                        onUpgrade = {
                            val cost = upgradeCostBase * (selectedCar.engineLevel + 1)
                            if (playerCoins >= cost && selectedCar.engineLevel < 3) {
                                storage.coins -= cost
                                storage.setCarEngineLevel(selectedCar.id, selectedCar.engineLevel + 1)
                                playerCoins = storage.coins
                            }
                        },
                        testTag = "upgrade_engine"
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    UpgradeRow(
                        title = "DRIFT STEERING KIT",
                        description = "+12% Slip Angle, +8% Counter-Steer Agility",
                        currentLevel = selectedCar.driftKitLevel,
                        cost = (upgradeCostBase * (selectedCar.driftKitLevel + 1)),
                        canAfford = playerCoins >= (upgradeCostBase * (selectedCar.driftKitLevel + 1)),
                        isMax = selectedCar.driftKitLevel >= 3,
                        onUpgrade = {
                            val cost = upgradeCostBase * (selectedCar.driftKitLevel + 1)
                            if (playerCoins >= cost && selectedCar.driftKitLevel < 3) {
                                storage.coins -= cost
                                storage.setCarDriftKitLevel(selectedCar.id, selectedCar.driftKitLevel + 1)
                                playerCoins = storage.coins
                            }
                        },
                        testTag = "upgrade_drift_kit"
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    UpgradeRow(
                        title = "DRIFT COMPOUND TIRES",
                        description = "+Grip Balance & High-Speed Slide Control",
                        currentLevel = selectedCar.tireLevel,
                        cost = (upgradeCostBase * (selectedCar.tireLevel + 1)),
                        canAfford = playerCoins >= (upgradeCostBase * (selectedCar.tireLevel + 1)),
                        isMax = selectedCar.tireLevel >= 3,
                        onUpgrade = {
                            val cost = upgradeCostBase * (selectedCar.tireLevel + 1)
                            if (playerCoins >= cost && selectedCar.tireLevel < 3) {
                                storage.coins -= cost
                                storage.setCarTireLevel(selectedCar.id, selectedCar.tireLevel + 1)
                                playerCoins = storage.coins
                            }
                        },
                        testTag = "upgrade_tires"
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    UpgradeRow(
                        title = "NITROUS SYSTEM (NOS)",
                        description = "+1.0s Boost Duration & Rapid Recharge",
                        currentLevel = selectedCar.nitroLevel,
                        cost = (upgradeCostBase * (selectedCar.nitroLevel + 1)),
                        canAfford = playerCoins >= (upgradeCostBase * (selectedCar.nitroLevel + 1)),
                        isMax = selectedCar.nitroLevel >= 3,
                        onUpgrade = {
                            val cost = upgradeCostBase * (selectedCar.nitroLevel + 1)
                            if (playerCoins >= cost && selectedCar.nitroLevel < 3) {
                                storage.coins -= cost
                                storage.setCarNitroLevel(selectedCar.id, selectedCar.nitroLevel + 1)
                                playerCoins = storage.coins
                            }
                        },
                        testTag = "upgrade_nitro"
                    )
                }
            }

            // Bottom Action (Equip or Unlock)
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                    if (selectedCar.isUnlocked) {
                        val isEquipped = selectedCar.id == equippedId
                        Button(
                            onClick = {
                                storage.equippedCarId = selectedCar.id
                                equippedId = selectedCar.id
                                onEquipCar(selectedCar)
                            },
                            enabled = !isEquipped,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isEquipped) Color(0xFF2E7D32) else NeonOrange,
                                disabledContainerColor = Color(0xFF1B5E20)
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .testTag("garage_equip_button")
                        ) {
                            Text(
                                text = if (isEquipped) "CURRENTLY EQUIPPED" else "EQUIP CAR",
                                color = if (isEquipped) Color.White else Color.Black,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black
                            )
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
                                    onEquipCar(selectedCar.copy(isUnlocked = true))
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
                                    text = "UNLOCK FOR ${selectedCar.basePrice} COINS",
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
    }
}

@Composable
private fun StatBar(name: String, value: Float, maxValue: Float, color: Color) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = name, color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Text(text = "${(value).toInt()}", color = color, fontSize = 11.sp, fontWeight = FontWeight.Black)
        }
        Spacer(modifier = Modifier.height(3.dp))
        LinearProgressIndicator(
            progress = { (value / maxValue).coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = color,
            trackColor = Color(0x22FFFFFF)
        )
    }
}

@Composable
private fun UpgradeRow(
    title: String,
    description: String,
    currentLevel: Int,
    cost: Int,
    canAfford: Boolean,
    isMax: Boolean,
    onUpgrade: () -> Unit,
    testTag: String
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = RacingCard),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x1AFFFFFF)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = title, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Black)
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        color = NeonCyan.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = if (isMax) "MAX" else "LV $currentLevel/3",
                            color = NeonCyan,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(text = description, color = TextMuted, fontSize = 10.sp)
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = onUpgrade,
                enabled = !isMax && canAfford,
                colors = ButtonDefaults.buttonColors(containerColor = NeonOrange),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .height(38.dp)
                    .testTag(testTag)
            ) {
                if (isMax) {
                    Text("MAXED", color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.MonetizationOn, contentDescription = null, tint = Color.Black, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("$cost", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Black)
                    }
                }
            }
        }
    }
}
