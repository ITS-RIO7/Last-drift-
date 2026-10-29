package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.game.model.Car
import com.example.game.model.GameMode
import com.example.game.model.Track
import com.example.game.storage.GameStorage
import com.example.game.ui.screens.GameScreen
import com.example.game.ui.screens.GarageScreen
import com.example.game.ui.screens.MainMenuScreen
import com.example.game.ui.screens.RecordsScreen
import com.example.game.ui.screens.SettingsScreen
import com.example.game.ui.screens.TrackSelectScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.RacingDark

enum class AppScreen {
    MAIN_MENU,
    GAME,
    GARAGE,
    TRACK_SELECT,
    RECORDS,
    SETTINGS
}

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val storage = GameStorage(this)

        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = RacingDark
                ) {
                    DriftHorizonApp(storage = storage)
                }
            }
        }
    }
}

@Composable
fun DriftHorizonApp(storage: GameStorage) {
    var currentScreen by remember { mutableStateOf(AppScreen.MAIN_MENU) }

    // Load active car
    var equippedCar by remember {
        val baseCar = Car.DEFAULT_CARS.firstOrNull { it.id == storage.equippedCarId } ?: Car.DEFAULT_CARS[0]
        mutableStateOf(storage.loadCarWithUpgrades(baseCar))
    }

    // Load active track
    var selectedTrack by remember {
        val track = Track.ALL_TRACKS.firstOrNull { it.id == storage.selectedTrackId } ?: Track.ALL_TRACKS[0]
        mutableStateOf(track)
    }

    // Load active game mode
    var selectedMode by remember { mutableStateOf(GameMode.DRIFT_ATTACK) }

    when (currentScreen) {
        AppScreen.MAIN_MENU -> {
            MainMenuScreen(
                currentCar = equippedCar,
                currentTrack = selectedTrack,
                currentMode = selectedMode,
                storage = storage,
                onStartDrift = { currentScreen = AppScreen.GAME },
                onOpenGarage = { currentScreen = AppScreen.GARAGE },
                onOpenTracks = { currentScreen = AppScreen.TRACK_SELECT },
                onOpenRecords = { currentScreen = AppScreen.RECORDS },
                onOpenSettings = { currentScreen = AppScreen.SETTINGS }
            )
        }

        AppScreen.GAME -> {
            GameScreen(
                car = equippedCar,
                track = selectedTrack,
                mode = selectedMode,
                storage = storage,
                onExitGame = {
                    // Reload car state in case stats or paints changed
                    val baseCar = Car.DEFAULT_CARS.firstOrNull { it.id == storage.equippedCarId } ?: Car.DEFAULT_CARS[0]
                    equippedCar = storage.loadCarWithUpgrades(baseCar)
                    currentScreen = AppScreen.MAIN_MENU
                }
            )
        }

        AppScreen.GARAGE -> {
            GarageScreen(
                storage = storage,
                onBack = {
                    val baseCar = Car.DEFAULT_CARS.firstOrNull { it.id == storage.equippedCarId } ?: Car.DEFAULT_CARS[0]
                    equippedCar = storage.loadCarWithUpgrades(baseCar)
                    currentScreen = AppScreen.MAIN_MENU
                },
                onEquipCar = { newCar ->
                    equippedCar = newCar
                }
            )
        }

        AppScreen.TRACK_SELECT -> {
            TrackSelectScreen(
                storage = storage,
                selectedTrack = selectedTrack,
                selectedMode = selectedMode,
                onTrackSelect = { selectedTrack = it },
                onModeSelect = { selectedMode = it },
                onStartGame = { currentScreen = AppScreen.GAME },
                onBack = { currentScreen = AppScreen.MAIN_MENU }
            )
        }

        AppScreen.RECORDS -> {
            RecordsScreen(
                storage = storage,
                onBack = { currentScreen = AppScreen.MAIN_MENU }
            )
        }

        AppScreen.SETTINGS -> {
            SettingsScreen(
                storage = storage,
                onBack = { currentScreen = AppScreen.MAIN_MENU }
            )
        }
    }
}
