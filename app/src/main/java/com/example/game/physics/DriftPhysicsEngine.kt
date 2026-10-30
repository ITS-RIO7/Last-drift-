package com.example.game.physics

import com.example.game.model.Car
import com.example.game.model.Track
import com.example.game.model.TrackObstacle
import com.example.game.model.TrackPoint
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.random.Random

data class CarWheelPositions(
    val rearLeft: Vector2D,
    val rearRight: Vector2D,
    val frontLeft: Vector2D,
    val frontRight: Vector2D
)

class DriftPhysicsEngine(
    val car: Car,
    val track: Track
) {
    var posX = track.startX
    var posY = track.startY
    var heading = track.startAngleDeg * (PI.toFloat() / 180f) // radians
    var vx = 0f
    var vy = 0f
    var angularVelocity = 0f
    var steerAngle = 0f // radians

    var nitroCharge = 1.0f // 0.0 to 1.0
    var isNitroActive = false
    var isDrifting = false
    var driftAngleDeg = 0f
    var speed = 0f // pixels / sec
    var isSpunOut = false
    var bodyRollAngle = 0f
    var bodyPitchAngle = 0f

    // Particle buffers
    val smokeParticles = mutableListOf<SmokeParticle>()
    val sparkParticles = mutableListOf<SparkParticle>()
    val skidSegments = mutableListOf<SkidSegment>()
    val floatPopups = mutableListOf<FloatScorePopup>()

    // Checkpoints & Laps
    var currentCheckpointIndex = 0
    var completedLaps = 0
    var lapStartTime = System.currentTimeMillis()
    var currentLapTime = 0f
    var bestLapTime = Float.MAX_VALUE

    // Collision state
    var justCrashed = false
    var justWallTapped = false

    private var prevRearLeft: Vector2D? = null
    private var prevRearRight: Vector2D? = null

    val carLength = 54f
    val carWidth = 28f

    fun reset(startX: Float = track.startX, startY: Float = track.startY, angleDeg: Float = track.startAngleDeg) {
        posX = startX
        posY = startY
        heading = angleDeg * (PI.toFloat() / 180f)
        vx = 0f
        vy = 0f
        angularVelocity = 0f
        steerAngle = 0f
        nitroCharge = 1.0f
        isNitroActive = false
        isDrifting = false
        driftAngleDeg = 0f
        speed = 0f
        isSpunOut = false
        smokeParticles.clear()
        sparkParticles.clear()
        skidSegments.clear()
        floatPopups.clear()
        currentCheckpointIndex = 0
        completedLaps = 0
        lapStartTime = System.currentTimeMillis()
        currentLapTime = 0f
        prevRearLeft = null
        prevRearRight = null
    }

    fun update(
        dt: Float,
        steerInput: Float, // -1 (left) to +1 (right)
        throttleInput: Float, // 0 to 1
        brakeInput: Float, // 0 to 1
        handbrakeInput: Boolean,
        nitroInput: Boolean
    ) {
        justCrashed = false
        justWallTapped = false

        // 1. Target steer angle
        val maxSteer = 0.58f * car.handling // ~33 degrees
        val steerRate = 8.0f // fast responsive wheel turning
        val targetSteer = steerInput.coerceIn(-1f, 1f) * maxSteer
        steerAngle += (targetSteer - steerAngle) * (steerRate * dt).coerceAtMost(1f)

        // 2. Heading and Right vectors
        val forwardX = cos(heading)
        val forwardY = sin(heading)
        val rightX = -sin(heading)
        val rightY = cos(heading)

        // 3. Local velocities
        var vLong = vx * forwardX + vy * forwardY
        var vLat = vx * rightX + vy * rightY
        speed = hypot(vx, vy)

        // 4. Nitro mechanics
        isNitroActive = nitroInput && nitroCharge > 0.05f && throttleInput > 0.1f
        var nitroPower = 1.0f
        if (isNitroActive) {
            nitroCharge = (nitroCharge - (dt / car.nitroDurationSec)).coerceAtLeast(0f)
            nitroPower = 1.75f
        } else {
            // Passive slow refill, or faster while drifting
            val refillRate = if (isDrifting) 0.08f else 0.03f
            nitroCharge = (nitroCharge + dt * refillRate).coerceAtMost(1f)
        }

        // 5. Longitudinal forces (Engine, Brake, Drag)
        val engineForce = car.acceleration * throttleInput * nitroPower
        val brakeForce = 450f * brakeInput
        val rollingResistance = 0.85f
        val aerodynamicDrag = 0.0012f * vLong * abs(vLong)

        val forwardDir = if (vLong > 0f) 1f else if (vLong < 0f) -1f else 0f
        val netLongAccel = engineForce - (brakeForce * forwardDir) - (vLong * rollingResistance) - aerodynamicDrag
        vLong += netLongAccel * dt

        // Top speed clamp
        val currentMaxSpeed = car.topSpeed * (if (isNitroActive) 1.25f else 1.0f)
        vLong = vLong.coerceIn(-120f, currentMaxSpeed)

        // 6. Lateral Grip & Drift Physics
        val baseLateralGrip = 7.5f * car.gripBalance
        val rearSlideFactor = if (handbrakeInput) 0.18f else if (isDrifting) 0.42f else 0.95f
        val effectiveGrip = baseLateralGrip * rearSlideFactor

        // Lateral speed reduces with grip
        vLat -= vLat * (effectiveGrip * dt).coerceAtMost(0.92f)

        // 7. Steering & Drift Yaw (Oversteer vs Counter-steer)
        val speedFactor = (speed / 280f).coerceIn(0f, 1.2f)
        val turnSpeed = 4.2f * car.handling

        if (handbrakeInput && speed > 50f) {
            // Handbrake induces aggressive rotation in the direction of steering (or slide)
            val kickDirection: Float = if (abs(steerAngle) > 0.05f) {
                if (steerAngle > 0f) 1f else -1f
            } else if (abs(vLat) > 5f) {
                if (vLat < 0f) 1f else -1f
            } else 1f
            angularVelocity += kickDirection * 6.5f * car.driftFactor * dt
        } else {
            // Normal steering yaw torque
            val steerYaw = steerAngle * turnSpeed * speedFactor
            angularVelocity += (steerYaw - angularVelocity) * (6.0f * dt).coerceAtMost(1f)
        }

        // Counter-steering stabilization:
        // If car is sliding, the slide momentum tries to spin it, but counter-steering stabilizes it
        val lateralSlipRatio = vLat / (abs(vLong) + 30f)
        if (abs(lateralSlipRatio) > 0.15f && speed > 70f) {
            // Car is sliding sideways!
            val oversteerTorque = -lateralSlipRatio * 7.5f * car.driftFactor
            val counterSteerStabilize = steerAngle * 8.5f // Player correcting with opposite lock

            angularVelocity += (oversteerTorque + counterSteerStabilize) * dt

            // Damping to prevent chaotic oscillations
            angularVelocity *= (1.0f - 2.8f * dt).coerceAtLeast(0.1f)
        } else {
            // Angular velocity dampens quickly when driving straight
            angularVelocity *= (1.0f - 4.5f * dt).coerceAtLeast(0.1f)
        }

        // Spin-out detection (if angle is over ~75 degrees and angular velocity is too high)
        heading += angularVelocity * dt

        // Reconstruct world velocity from local components
        vx = vLong * forwardX + vLat * rightX
        vy = vLong * forwardY + vLat * rightY

        // 8. Update position
        posX += vx * dt
        posY += vy * dt

        // 9. Calculate drift angle & drift state
        val motionAngle = atan2(vy, vx)
        var angleDiff = abs(heading - motionAngle)
        while (angleDiff > PI) angleDiff -= (2 * PI).toFloat()
        driftAngleDeg = abs(angleDiff) * (180f / PI.toFloat())

        val minDriftSpeed = 55f
        val driftThresholdAngle = 14f
        isDrifting = (speed > minDriftSpeed && driftAngleDeg > driftThresholdAngle) || (handbrakeInput && speed > 40f)

        if (driftAngleDeg > 78f && speed > 100f && abs(angularVelocity) > 3.0f) {
            isSpunOut = true
            // Spin-out slows down vehicle
            vx *= 0.94f
            vy *= 0.94f
        } else {
            isSpunOut = false
        }

        // Body Roll (chassis tilts laterally during slide) and Pitch (squat/dive)
        val targetRoll = (-vLat / 260f).coerceIn(-0.16f, 0.16f)
        bodyRollAngle += (targetRoll - bodyRollAngle) * (10f * dt).coerceAtMost(1f)

        val targetPitch = (netLongAccel / 1200f).coerceIn(-0.08f, 0.08f)
        bodyPitchAngle += (targetPitch - bodyPitchAngle) * (10f * dt).coerceAtMost(1f)

        // 10. Tire Wheel Positions
        val wheelPositions = computeWheelPositions(posX, posY, heading)

        // 11. Generate Smoke & Skid Marks
        if (isDrifting || handbrakeInput || isNitroActive) {
            spawnDriftEffects(wheelPositions, dt)
        }

        // 12. Check Boundary Collisions
        handleCollisions(dt)

        // 13. Checkpoint & Lap progression
        updateCheckpoints(dt)

        // 14. Update active particles
        updateParticles(dt)
    }

    private fun computeWheelPositions(cx: Float, cy: Float, h: Float): CarWheelPositions {
        val cosH = cos(h)
        val sinH = sin(h)

        val rearOffset = -carLength * 0.38f
        val frontOffset = carLength * 0.38f
        val halfW = carWidth * 0.5f

        fun transform(forwardOffset: Float, rightOffset: Float): Vector2D {
            val wx = cx + (cosH * forwardOffset - sinH * rightOffset)
            val wy = cy + (sinH * forwardOffset + cosH * rightOffset)
            return Vector2D(wx, wy)
        }

        return CarWheelPositions(
            rearLeft = transform(rearOffset, -halfW),
            rearRight = transform(rearOffset, halfW),
            frontLeft = transform(frontOffset, -halfW),
            frontRight = transform(frontOffset, halfW)
        )
    }

    private fun spawnDriftEffects(wheels: CarWheelPositions, dt: Float) {
        val intensity = (driftAngleDeg / 45f).coerceIn(0.4f, 1.6f)

        // Skidmarks
        prevRearLeft?.let { prev ->
            skidSegments.add(SkidSegment(prev.x, prev.y, wheels.rearLeft.x, wheels.rearLeft.y, 0.65f))
        }
        prevRearRight?.let { prev ->
            skidSegments.add(SkidSegment(prev.x, prev.y, wheels.rearRight.x, wheels.rearRight.y, 0.65f))
        }
        prevRearLeft = wheels.rearLeft
        prevRearRight = wheels.rearRight

        if (skidSegments.size > 400) {
            skidSegments.removeAt(0)
            skidSegments.removeAt(0)
        }

        // 3D Colorful Smoke particles
        if (smokeParticles.size < 120 && Random.nextFloat() < 0.75f) {
            val spawnLeft = Random.nextBoolean()
            val spawnPos = if (spawnLeft) wheels.rearLeft else wheels.rearRight

            val spread = 12f
            smokeParticles.add(
                SmokeParticle(
                    x = spawnPos.x + Random.nextFloat() * spread - spread / 2,
                    y = spawnPos.y + Random.nextFloat() * spread - spread / 2,
                    z = 4f + Random.nextFloat() * 4f,
                    vx = -vx * 0.12f + (Random.nextFloat() - 0.5f) * 18f,
                    vy = -vy * 0.12f + (Random.nextFloat() - 0.5f) * 18f,
                    vz = 14f + Random.nextFloat() * 12f,
                    radius = 9f * intensity,
                    alpha = 0.75f,
                    life = 0.65f,
                    maxLife = 0.65f,
                    color = car.smokeColor
                )
            )
        }
    }

    private fun handleCollisions(dt: Float) {
        // Test track concrete barriers (inner and outer guardrails)
        checkBarrierCollision(track.innerBoundary)
        checkBarrierCollision(track.outerBoundary)

        // Test world boundaries
        val margin = 50f
        if (posX < margin) {
            posX = margin; vx = abs(vx) * 0.4f; triggerWallTap()
        }
        if (posX > track.worldWidth - margin) {
            posX = track.worldWidth - margin; vx = -abs(vx) * 0.4f; triggerWallTap()
        }
        if (posY < margin) {
            posY = margin; vy = abs(vy) * 0.4f; triggerWallTap()
        }
        if (posY > track.worldHeight - margin) {
            posY = track.worldHeight - margin; vy = -abs(vy) * 0.4f; triggerWallTap()
        }

        // Test track obstacles (e.g. shipping containers / boxes)
        for (obs in track.obstacles) {
            val halfW = obs.width / 2f + carWidth / 2f
            val halfH = obs.height / 2f + carLength / 2f
            if (abs(posX - obs.x) < halfW && abs(posY - obs.y) < halfH) {
                // Collision pushback
                val overlapX = halfW - abs(posX - obs.x)
                val overlapY = halfH - abs(posY - obs.y)
                if (overlapX < overlapY) {
                    posX += if (posX > obs.x) overlapX else -overlapX
                    vx = -vx * 0.4f
                } else {
                    posY += if (posY > obs.y) overlapY else -overlapY
                    vy = -vy * 0.4f
                }
                triggerWallTap()
            }
        }
    }

    private fun checkBarrierCollision(boundary: List<TrackPoint>) {
        if (boundary.size < 2) return
        val carRadius = carWidth * 0.55f

        for (i in 0 until boundary.size - 1) {
            val p1 = boundary[i]
            val p2 = boundary[i + 1]

            val segDx = p2.x - p1.x
            val segDy = p2.y - p1.y
            val segLenSq = segDx * segDx + segDy * segDy
            if (segLenSq < 1f) continue

            val t = (((posX - p1.x) * segDx + (posY - p1.y) * segDy) / segLenSq).coerceIn(0f, 1f)
            val closestX = p1.x + t * segDx
            val closestY = p1.y + t * segDy

            val distDx = posX - closestX
            val distDy = posY - closestY
            val distSq = distDx * distDx + distDy * distDy

            if (distSq < carRadius * carRadius) {
                val dist = hypot(distDx, distDy).coerceAtLeast(0.001f)
                val normalX = distDx / dist
                val normalY = distDy / dist
                val penetration = carRadius - dist

                posX += normalX * penetration
                posY += normalY * penetration

                val vDotN = vx * normalX + vy * normalY
                if (vDotN < 0f) {
                    vx -= (1f + 0.35f) * vDotN * normalX
                    vy -= (1f + 0.35f) * vDotN * normalY
                    vx *= 0.94f
                    vy *= 0.94f
                    triggerWallTap()
                }
            }
        }
    }

    private fun triggerWallTap() {
        justWallTapped = true
        speed = hypot(vx, vy)
        if (speed > 160f) {
            justCrashed = true
        }
        // Spawn 3D sparks
        for (i in 0 until 10) {
            val sparkAngle = Random.nextFloat() * 2 * PI.toFloat()
            val sparkSpeed = 80f + Random.nextFloat() * 140f
            sparkParticles.add(
                SparkParticle(
                    x = posX,
                    y = posY,
                    z = 8f,
                    vx = cos(sparkAngle) * sparkSpeed,
                    vy = sin(sparkAngle) * sparkSpeed,
                    vz = 30f + Random.nextFloat() * 40f,
                    life = 0.45f,
                    maxLife = 0.45f,
                    color = if (Random.nextBoolean()) 0xFFFFD600 else 0xFFFF007F
                )
            )
        }
    }

    private fun updateCheckpoints(dt: Float) {
        currentLapTime += dt
        if (track.checkpoints.isEmpty()) return

        val targetCheckpoint = track.checkpoints[currentCheckpointIndex]
        val dist = hypot(posX - targetCheckpoint.x, posY - targetCheckpoint.y)

        if (dist < 220f) {
            currentCheckpointIndex = (currentCheckpointIndex + 1) % track.checkpoints.size
            if (currentCheckpointIndex == 0) {
                completedLaps++
                if (currentLapTime < bestLapTime) {
                    bestLapTime = currentLapTime
                }
                currentLapTime = 0f
            }
        }
    }

    private fun updateParticles(dt: Float) {
        // Smoke in 3D
        val smokeIterator = smokeParticles.iterator()
        while (smokeIterator.hasNext()) {
            val p = smokeIterator.next()
            p.life -= dt
            p.x += p.vx * dt
            p.y += p.vy * dt
            p.z += p.vz * dt
            p.vz *= (1f - 1.2f * dt).coerceAtLeast(0.1f)
            p.radius += 20f * dt
            p.alpha = (p.life / p.maxLife) * 0.75f
            if (p.life <= 0f) {
                smokeIterator.remove()
            }
        }

        // Sparks in 3D (with gravity)
        val sparkIterator = sparkParticles.iterator()
        while (sparkIterator.hasNext()) {
            val s = sparkIterator.next()
            s.life -= dt
            s.x += s.vx * dt
            s.y += s.vy * dt
            s.z = (s.z + s.vz * dt).coerceAtLeast(0f)
            s.vz -= 85f * dt // gravity
            if (s.life <= 0f) {
                sparkIterator.remove()
            }
        }

        // Float Popups
        val popupIterator = floatPopups.iterator()
        while (popupIterator.hasNext()) {
            val pop = popupIterator.next()
            pop.offsetY -= 45f * dt
            pop.alpha -= 0.9f * dt
            if (pop.alpha <= 0f) {
                popupIterator.remove()
            }
        }
    }

    fun addPopup(text: String, x: Float, y: Float, color: Long = 0xFF00E5FF) {
        floatPopups.add(
            FloatScorePopup(
                id = System.nanoTime(),
                text = text,
                x = x,
                y = y,
                color = color
            )
        )
    }
}
