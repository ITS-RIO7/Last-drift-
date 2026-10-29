package com.example.game.scoring

import com.example.game.model.ClippingPoint
import com.example.game.physics.DriftPhysicsEngine
import kotlin.math.hypot
import kotlin.math.max

data class ScoringEvent(
    val title: String,
    val bonusPoints: Int,
    val color: Long
)

class DriftScoringSystem {
    var totalScore: Int = 0
    var currentComboScore: Float = 0f
    var multiplier: Float = 1.0f
    var comboTimeout: Float = 0f // grace period in seconds
    val maxComboTimeout: Float = 1.6f

    var continuousDriftTime: Float = 0f
    var longestDriftSec: Float = 0f
    var maxDriftAngle: Float = 0f
    var clipsHitCount: Int = 0
    var wallTapCount: Int = 0

    // Prevent repeated hits on the same clipping point within a short interval
    private val visitedClips = mutableMapOf<String, Long>()

    var lastEvent: ScoringEvent? = null

    fun reset() {
        totalScore = 0
        currentComboScore = 0f
        multiplier = 1.0f
        comboTimeout = 0f
        continuousDriftTime = 0f
        longestDriftSec = 0f
        maxDriftAngle = 0f
        clipsHitCount = 0
        wallTapCount = 0
        visitedClips.clear()
        lastEvent = null
    }

    fun update(dt: Float, physics: DriftPhysicsEngine): ScoringEvent? {
        lastEvent = null

        if (physics.isDrifting && !physics.isSpunOut) {
            // Active drift
            continuousDriftTime += dt
            if (continuousDriftTime > longestDriftSec) {
                longestDriftSec = continuousDriftTime
            }
            if (physics.driftAngleDeg > maxDriftAngle) {
                maxDriftAngle = physics.driftAngleDeg
            }

            // Points per second based on angle and speed
            val rate = (physics.driftAngleDeg * 14f + physics.speed * 0.8f)
            currentComboScore += rate * dt

            // Scale multiplier
            val targetMultiplier = when {
                continuousDriftTime > 11.0f -> 10.0f
                continuousDriftTime > 8.5f -> 8.0f
                continuousDriftTime > 6.0f -> 5.0f
                continuousDriftTime > 4.0f -> 3.5f
                continuousDriftTime > 2.5f -> 2.5f
                continuousDriftTime > 1.0f -> 1.5f
                else -> 1.0f
            }
            multiplier = max(multiplier, targetMultiplier)

            // Reset combo grace timeout
            comboTimeout = maxComboTimeout

            // Check clipping points
            val now = System.currentTimeMillis()
            for (clip in physics.track.clippingPoints) {
                val dist = hypot(physics.posX - clip.x, physics.posY - clip.y)
                val lastHit = visitedClips[clip.id] ?: 0L
                if (dist < clip.radius && now - lastHit > 2500L) {
                    visitedClips[clip.id] = now
                    clipsHitCount++
                    val clipBonus = (400 * multiplier).toInt()
                    currentComboScore += 400
                    multiplier = (multiplier + 0.5f).coerceAtMost(10.0f)
                    physics.addPopup("+400 APEX CLIP!", physics.posX, physics.posY - 40f, 0xFF00E5FF)
                    lastEvent = ScoringEvent("APEX CLIPPING POINT!", clipBonus, 0xFF00E5FF)
                }
            }

            // Wall tap bonus
            if (physics.justWallTapped && !physics.justCrashed) {
                wallTapCount++
                currentComboScore += 300
                physics.addPopup("+300 WALL TAP!", physics.posX, physics.posY - 40f, 0xFFFF6D00)
                lastEvent = ScoringEvent("WALL TAP BONUS!", (300 * multiplier).toInt(), 0xFFFF6D00)
            }
        } else {
            // Not drifting
            continuousDriftTime = 0f

            if (physics.isSpunOut || physics.justCrashed) {
                // Combo broken
                if (currentComboScore > 0f) {
                    physics.addPopup("SPUN OUT!", physics.posX, physics.posY - 40f, 0xFFFF1744)
                    lastEvent = ScoringEvent("SPUN OUT! COMBO LOST", 0, 0xFFFF1744)
                }
                currentComboScore = 0f
                multiplier = 1.0f
                comboTimeout = 0f
            } else if (comboTimeout > 0f) {
                // Grace period ticking down
                comboTimeout -= dt
                if (comboTimeout <= 0f) {
                    // Bank score cleanly!
                    val banked = (currentComboScore * multiplier).toInt()
                    if (banked > 0) {
                        totalScore += banked
                        physics.addPopup("+$banked BANKED", physics.posX, physics.posY - 40f, 0xFF00E676)
                        lastEvent = ScoringEvent("DRIFT BANKED!", banked, 0xFF00E676)
                    }
                    currentComboScore = 0f
                    multiplier = 1.0f
                }
            }
        }

        return lastEvent
    }

    fun finalizeScore() {
        val banked = (currentComboScore * multiplier).toInt()
        totalScore += banked
        currentComboScore = 0f
        multiplier = 1.0f
    }

    val coinsEarned: Int get() = (totalScore / 120).coerceAtLeast(0)
}
