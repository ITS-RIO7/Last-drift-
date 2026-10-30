package com.example.game.storage

import android.content.Context
import android.content.SharedPreferences
import com.example.game.model.Car

class GameStorage(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("drift_horizon_prefs", Context.MODE_PRIVATE)

    var coins: Int
        get() = prefs.getInt("player_coins", 1500) // start with 1500 coins to try upgrades right away!
        set(value) = prefs.edit().putInt("player_coins", value).apply()

    var equippedCarId: String
        get() = prefs.getString("equipped_car_id", "car_rod42") ?: "car_rod42"
        set(value) = prefs.edit().putString("equipped_car_id", value).apply()

    var selectedTrackId: String
        get() = prefs.getString("selected_track_id", "track_rod_city") ?: "track_rod_city"
        set(value) = prefs.edit().putString("selected_track_id", value).apply()

    var soundEnabled: Boolean
        get() = prefs.getBoolean("sound_enabled", true)
        set(value) = prefs.edit().putBoolean("sound_enabled", value).apply()

    var hapticsEnabled: Boolean
        get() = prefs.getBoolean("haptics_enabled", true)
        set(value) = prefs.edit().putBoolean("haptics_enabled", value).apply()

    var steeringSensitivity: Float
        get() = prefs.getFloat("steering_sensitivity", 1.0f)
        set(value) = prefs.edit().putFloat("steering_sensitivity", value).apply()

    var cameraModeIndex: Int
        get() = prefs.getInt("camera_mode_idx", 0)
        set(value) = prefs.edit().putInt("camera_mode_idx", value).apply()

    fun isCarUnlocked(carId: String): Boolean {
        if (carId == "car_rod42" || carId == "car_86") return true
        return prefs.getBoolean("car_unlocked_$carId", false)
    }

    fun unlockCar(carId: String) {
        prefs.edit().putBoolean("car_unlocked_$carId", true).apply()
    }

    fun getCarEngineLevel(carId: String): Int = prefs.getInt("car_eng_$carId", 0)
    fun setCarEngineLevel(carId: String, lvl: Int) = prefs.edit().putInt("car_eng_$carId", lvl).apply()

    fun getCarDriftKitLevel(carId: String): Int = prefs.getInt("car_drift_$carId", 0)
    fun setCarDriftKitLevel(carId: String, lvl: Int) = prefs.edit().putInt("car_drift_$carId", lvl).apply()

    fun getCarTireLevel(carId: String): Int = prefs.getInt("car_tire_$carId", 0)
    fun setCarTireLevel(carId: String, lvl: Int) = prefs.edit().putInt("car_tire_$carId", lvl).apply()

    fun getCarNitroLevel(carId: String): Int = prefs.getInt("car_nitro_$carId", 0)
    fun setCarNitroLevel(carId: String, lvl: Int) = prefs.edit().putInt("car_nitro_$carId", lvl).apply()

    fun getCarCustomPaint(carId: String, defaultColor: Long): Long =
        prefs.getLong("car_paint_$carId", defaultColor)
    fun setCarCustomPaint(carId: String, color: Long) =
        prefs.edit().putLong("car_paint_$carId", color).apply()

    fun getCarSmokeColor(carId: String, defaultColor: Long): Long =
        prefs.getLong("car_smoke_$carId", defaultColor)
    fun setCarSmokeColor(carId: String, color: Long) =
        prefs.edit().putLong("car_smoke_$carId", color).apply()

    fun getCarUnderglowColor(carId: String, defaultColor: Long): Long =
        prefs.getLong("car_underglow_$carId", defaultColor)
    fun setCarUnderglowColor(carId: String, color: Long) =
        prefs.edit().putLong("car_underglow_$carId", color).apply()

    fun getHighScore(trackId: String, modeName: String): Int =
        prefs.getInt("high_score_${trackId}_$modeName", 0)

    fun recordScore(trackId: String, modeName: String, score: Int): Boolean {
        val current = getHighScore(trackId, modeName)
        if (score > current) {
            prefs.edit().putInt("high_score_${trackId}_$modeName", score).apply()
            return true
        }
        return false
    }

    var bestSingleDrift: Float
        get() = prefs.getFloat("best_single_drift", 0f)
        set(value) = prefs.edit().putFloat("best_single_drift", value).apply()

    var careerDriftScore: Long
        get() = prefs.getLong("career_drift_score", 0L)
        set(value) = prefs.edit().putLong("career_drift_score", value).apply()

    fun loadCarWithUpgrades(baseCar: Car): Car {
        val unlocked = isCarUnlocked(baseCar.id)
        val eng = getCarEngineLevel(baseCar.id)
        val drift = getCarDriftKitLevel(baseCar.id)
        val tire = getCarTireLevel(baseCar.id)
        val nitro = getCarNitroLevel(baseCar.id)
        val customColor = getCarCustomPaint(baseCar.id, baseCar.bodyColor)
        val customSmoke = getCarSmokeColor(baseCar.id, baseCar.smokeColor)
        val customUnderglow = getCarUnderglowColor(baseCar.id, baseCar.underglowColor)

        return baseCar.copy(
            isUnlocked = unlocked,
            engineLevel = eng,
            driftKitLevel = drift,
            tireLevel = tire,
            nitroLevel = nitro,
            bodyColor = customColor,
            smokeColor = customSmoke,
            underglowColor = customUnderglow
        )
    }
}
