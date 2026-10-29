package com.example.game.model

enum class GameMode(
    val title: String,
    val subtitle: String,
    val description: String,
    val defaultTimeLimitSec: Float,
    val targetLaps: Int
) {
    DRIFT_ATTACK(
        title = "Drift Attack",
        subtitle = "90s Score Rush",
        description = "Chamber maximum angle, build huge combo multipliers, and hit clipping apexes before the clock runs out!",
        defaultTimeLimitSec = 90f,
        targetLaps = 0
    ),
    TIME_CIRCUIT(
        title = "Tandem Circuit",
        subtitle = "2 Fast Laps",
        description = "Complete 2 high-octane laps as fast as possible. Every 1,000 drift points subtracts 1 second from your total time!",
        defaultTimeLimitSec = 180f,
        targetLaps = 2
    ),
    FREE_PRACTICE(
        title = "Freestyle Practice",
        subtitle = "Endless Slide",
        description = "Infinite time with instant NOS refills to master counter-steering, weight transfer, and donut entries.",
        defaultTimeLimitSec = Float.POSITIVE_INFINITY,
        targetLaps = 0
    )
}
