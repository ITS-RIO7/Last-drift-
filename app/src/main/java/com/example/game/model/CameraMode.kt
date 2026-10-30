package com.example.game.model

enum class CameraMode(
    val title: String,
    val subtitle: String,
    val description: String
) {
    CHASE(
        title = "CHASE",
        subtitle = "DYNAMIC CAM",
        description = "Dynamic 3rd-person trailing drift camera"
    ),
    HOOD(
        title = "HOOD",
        subtitle = "COCKPIT CAM",
        description = "Bonnet cockpit view for track precision"
    ),
    TOP_DOWN(
        title = "DRONE",
        subtitle = "AERIAL CAM",
        description = "Overhead bird's-eye view of curves"
    ),
    BUMPER(
        title = "BUMPER",
        subtitle = "SPEED CAM",
        description = "Low asphalt asphalt-rush perspective"
    ),
    DRIFT_ACTION(
        title = "ACTION",
        subtitle = "DRIFT CAM",
        description = "Cinematic angled side-view of countersteer"
    );

    fun next(): CameraMode {
        val values = entries
        val nextIdx = (ordinal + 1) % values.size
        return values[nextIdx]
    }
}
