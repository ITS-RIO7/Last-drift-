package com.example.game.physics

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

data class Vector2D(val x: Float = 0f, val y: Float = 0f) {
    operator fun plus(other: Vector2D) = Vector2D(x + other.x, y + other.y)
    operator fun minus(other: Vector2D) = Vector2D(x - other.x, y - other.y)
    operator fun times(scalar: Float) = Vector2D(x * scalar, y * scalar)
    operator fun div(scalar: Float) = if (scalar != 0f) Vector2D(x / scalar, y / scalar) else Vector2D()

    fun length(): Float = sqrt(x * x + y * y)
    fun lengthSquared(): Float = x * x + y * y

    fun normalized(): Vector2D {
        val l = length()
        return if (l > 0.0001f) Vector2D(x / l, y / l) else Vector2D(1f, 0f)
    }

    fun dot(other: Vector2D): Float = x * other.x + y * other.y

    fun rotated(radians: Float): Vector2D {
        val cosA = cos(radians)
        val sinA = sin(radians)
        return Vector2D(x * cosA - y * sinA, x * sinA + y * cosA)
    }

    fun angle(): Float = atan2(y, x)

    companion object {
        fun fromAngle(radians: Float, length: Float = 1f): Vector2D {
            return Vector2D(cos(radians) * length, sin(radians) * length)
        }
    }
}
