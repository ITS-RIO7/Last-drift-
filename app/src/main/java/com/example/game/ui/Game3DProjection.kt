package com.example.game.ui

import androidx.compose.ui.geometry.Offset
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

data class Point3D(val x: Float, val y: Float, val z: Float)
data class ScreenPoint(val x: Float, val y: Float, val depth: Float, val isVisible: Boolean)

class Game3DCamera {
    var posX = 0f
    var posY = 0f
    var posZ = 95f // elevation above ground

    var lookAtX = 0f
    var lookAtY = 0f
    var lookAtZ = 16f

    var camYaw = 0f // heading in radians
    var camPitch = -0.32f // look downward slightly
    var camRoll = 0f // bank angle when drifting

    var focalLength = 480f
    var screenWidth = 800f
    var screenHeight = 1200f

    // Camera rotation matrix elements
    private var r11 = 1f; private var r12 = 0f; private var r13 = 0f
    private var r21 = 0f; private var r22 = 1f; private var r23 = 0f
    private var r31 = 0f; private var r32 = 0f; private var r33 = 1f

    fun update(
        carX: Float,
        carY: Float,
        carHeading: Float,
        carVx: Float,
        carVy: Float,
        carSpeed: Float,
        bodyRoll: Float,
        w: Float,
        h: Float,
        dt: Float
    ) {
        screenWidth = w
        screenHeight = h
        focalLength = w * 0.95f

        // Smooth camera follow angle with slight trailing lag for dramatic drift view
        val chaseDistance = 145f + (carSpeed / 600f) * 45f
        val chaseElevation = 90f + (carSpeed / 600f) * 20f

        // Interpolate camera yaw toward car heading, but allowing side view during high angle drifts
        var yawDiff = carHeading - camYaw
        while (yawDiff > PI) yawDiff -= (2 * PI).toFloat()
        while (yawDiff < -PI) yawDiff += (2 * PI).toFloat()
        camYaw += yawDiff * (4.2f * dt).coerceAtMost(0.4f)

        // Target camera position
        val targetCamX = carX - cos(camYaw) * chaseDistance
        val targetCamY = carY - sin(camYaw) * chaseDistance
        val targetCamZ = chaseElevation

        posX += (targetCamX - posX) * (7.5f * dt).coerceAtMost(1f)
        posY += (targetCamY - posY) * (7.5f * dt).coerceAtMost(1f)
        posZ += (targetCamZ - posZ) * (7.5f * dt).coerceAtMost(1f)

        // Target look-at point: car position + slight forward lead
        val leadDist = (carSpeed / 600f) * 55f
        lookAtX = carX + cos(carHeading) * leadDist
        lookAtY = carY + sin(carHeading) * leadDist
        lookAtZ = 18f

        // Roll camera with body roll for immersion
        val targetRoll = -bodyRoll * 0.65f
        camRoll += (targetRoll - camRoll) * (8f * dt).coerceAtMost(1f)

        // Compute forward vector from Cam to LookAt
        val fwdX = lookAtX - posX
        val fwdY = lookAtY - posY
        val fwdZ = lookAtZ - posZ
        val fwdLen = sqrt(fwdX * fwdX + fwdY * fwdY + fwdZ * fwdZ).coerceAtLeast(0.001f)

        val fx = fwdX / fwdLen
        val fy = fwdY / fwdLen
        val fz = fwdZ / fwdLen

        // World up is (0, 0, 1)
        // Right = Forward x Up
        val rxRaw = fy * 1f - fz * 0f
        val ryRaw = fz * 0f - fx * 1f
        val rzRaw = fx * 0f - fy * 0f // 0
        val rLen = sqrt(rxRaw * rxRaw + ryRaw * ryRaw).coerceAtLeast(0.001f)
        var rx = rxRaw / rLen
        var ry = ryRaw / rLen
        var rz = 0f

        // True Up = Right x Forward
        var ux = ry * fz - rz * fy
        var uy = rz * fx - rx * fz
        var uz = rx * fy - ry * fx

        // Apply roll rotation around forward axis
        if (camRoll != 0f) {
            val cosR = cos(camRoll)
            val sinR = sin(camRoll)

            val newRx = rx * cosR + ux * sinR
            val newRy = ry * cosR + uy * sinR
            val newRz = rz * cosR + uz * sinR

            val newUx = -rx * sinR + ux * cosR
            val newUy = -ry * sinR + uy * cosR
            val newUz = -rz * sinR + uz * cosR

            rx = newRx; ry = newRy; rz = newRz
            ux = newUx; uy = newUy; uz = newUz
        }

        // Camera matrix (maps world relative to right, up, forward)
        r11 = rx; r12 = ry; r13 = rz
        r21 = ux; r22 = uy; r23 = uz
        r31 = fx; r32 = fy; r33 = fz
    }

    fun project(wx: Float, wy: Float, wz: Float): ScreenPoint {
        val dx = wx - posX
        val dy = wy - posY
        val dz = wz - posZ

        // Transform into camera local coordinates
        val cx = r11 * dx + r12 * dy + r13 * dz
        val cy = r21 * dx + r22 * dy + r23 * dz
        val cz = r31 * dx + r32 * dy + r33 * dz

        if (cz <= 3f) {
            return ScreenPoint(0f, 0f, cz, isVisible = false)
        }

        val sx = (cx / cz) * focalLength + (screenWidth * 0.5f)
        val sy = -(cy / cz) * focalLength + (screenHeight * 0.44f) // horizon line slightly above center

        val inBounds = sx >= -250f && sx <= screenWidth + 250f && sy >= -250f && sy <= screenHeight + 250f
        return ScreenPoint(sx, sy, cz, isVisible = inBounds)
    }

    val horizonScreenY: Float
        get() = screenHeight * 0.44f
}
