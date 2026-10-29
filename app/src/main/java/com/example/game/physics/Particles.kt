package com.example.game.physics

data class SmokeParticle(
    var x: Float,
    var y: Float,
    var z: Float = 0f,
    var vx: Float,
    var vy: Float,
    var vz: Float = 12f,
    var radius: Float,
    var alpha: Float,
    var life: Float,
    val maxLife: Float,
    val color: Long = 0xFF00F5FF
)

data class SparkParticle(
    var x: Float,
    var y: Float,
    var z: Float = 5f,
    var vx: Float,
    var vy: Float,
    var vz: Float = 25f,
    var life: Float,
    val maxLife: Float,
    val color: Long = 0xFFFFEA00
)

data class SkidSegment(
    val x1: Float,
    val y1: Float,
    val x2: Float,
    val y2: Float,
    var alpha: Float = 0.65f,
    val color: Long = 0xFF0B0E14
)

data class FloatScorePopup(
    val id: Long,
    val text: String,
    val x: Float,
    val y: Float,
    val z: Float = 35f,
    var alpha: Float = 1.0f,
    var offsetY: Float = 0f,
    val color: Long = 0xFF00F5FF
)
