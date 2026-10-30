package com.example.game.ui

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import com.example.game.model.CameraMode
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
    var posZ = 85f // elevation above ground

    var lookAtX = 0f
    var lookAtY = 0f
    var lookAtZ = 14f

    var camYaw = 0f // heading in radians
    var camPitch = -0.32f // look downward slightly
    var camRoll = 0f // bank angle when drifting

    var focalLength = 480f
    var screenWidth = 800f
    var screenHeight = 1200f

    var mode: CameraMode = CameraMode.CHASE
    var customHorizonRatio: Float = 0.40f

    // Camera rotation matrix elements
    var r11 = 1f; var r12 = 0f; var r13 = 0f
    var r21 = 0f; var r22 = 1f; var r23 = 0f
    var r31 = 0f; var r32 = 0f; var r33 = 1f

    private var initialized = false

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
        dt: Float,
        cameraMode: CameraMode = CameraMode.CHASE
    ) {
        mode = cameraMode
        screenWidth = w
        screenHeight = h

        val speedRatio = (carSpeed / 600f).coerceIn(0f, 1.5f)

        when (cameraMode) {
            CameraMode.CHASE -> {
                // Dynamic Chase view - realistic rear trailing angle
                focalLength = w * 0.95f
                customHorizonRatio = 0.40f

                val chaseDistance = 142f + speedRatio * 38f
                val chaseElevation = 78f + speedRatio * 16f

                var yawDiff = carHeading - camYaw
                while (yawDiff > PI) yawDiff -= (2 * PI).toFloat()
                while (yawDiff < -PI) yawDiff += (2 * PI).toFloat()
                camYaw += yawDiff * (4.5f * dt).coerceAtMost(0.45f)

                val targetCamX = carX - cos(camYaw) * chaseDistance
                val targetCamY = carY - sin(camYaw) * chaseDistance
                val targetCamZ = chaseElevation

                val lerpRate = if (!initialized) 1f else (8f * dt).coerceAtMost(1f)
                posX += (targetCamX - posX) * lerpRate
                posY += (targetCamY - posY) * lerpRate
                posZ += (targetCamZ - posZ) * lerpRate

                val leadDist = speedRatio * 55f + 25f
                lookAtX = carX + cos(carHeading) * leadDist
                lookAtY = carY + sin(carHeading) * leadDist
                lookAtZ = 14f

                val targetRoll = -bodyRoll * 0.55f
                camRoll += (targetRoll - camRoll) * (8f * dt).coerceAtMost(1f)
            }
            CameraMode.HOOD -> {
                // Cockpit / Bonnet view - mounted on the hood
                focalLength = w * 1.05f
                customHorizonRatio = 0.45f

                val fwdOffset = 18f
                val upOffset = 14f

                val targetCamX = carX + cos(carHeading) * fwdOffset
                val targetCamY = carY + sin(carHeading) * fwdOffset
                val targetCamZ = upOffset

                camYaw = carHeading
                val lerpRate = if (!initialized) 1f else (18f * dt).coerceAtMost(1f)
                posX += (targetCamX - posX) * lerpRate
                posY += (targetCamY - posY) * lerpRate
                posZ += (targetCamZ - posZ) * lerpRate

                lookAtX = carX + cos(carHeading) * 160f
                lookAtY = carY + sin(carHeading) * 160f
                lookAtZ = 11f

                val targetRoll = -bodyRoll * 0.75f
                camRoll += (targetRoll - camRoll) * (14f * dt).coerceAtMost(1f)
            }
            CameraMode.TOP_DOWN -> {
                // Overhead Aerial Drone view - birds-eye view of corners and smoke
                focalLength = w * 0.85f
                customHorizonRatio = 0.22f

                val droneDist = 65f
                val droneElevation = 260f

                var yawDiff = carHeading - camYaw
                while (yawDiff > PI) yawDiff -= (2 * PI).toFloat()
                while (yawDiff < -PI) yawDiff += (2 * PI).toFloat()
                camYaw += yawDiff * (3.8f * dt).coerceAtMost(0.38f)

                val targetCamX = carX - cos(camYaw) * droneDist
                val targetCamY = carY - sin(camYaw) * droneDist
                val targetCamZ = droneElevation

                val lerpRate = if (!initialized) 1f else (6.5f * dt).coerceAtMost(1f)
                posX += (targetCamX - posX) * lerpRate
                posY += (targetCamY - posY) * lerpRate
                posZ += (targetCamZ - posZ) * lerpRate

                lookAtX = carX + cos(carHeading) * 40f
                lookAtY = carY + sin(carHeading) * 40f
                lookAtZ = 2f

                camRoll = 0f
            }
            CameraMode.BUMPER -> {
                // Low asphalt rush view
                focalLength = w * 1.15f
                customHorizonRatio = 0.44f

                val fwdOffset = 30f
                val upOffset = 6.0f

                val targetCamX = carX + cos(carHeading) * fwdOffset
                val targetCamY = carY + sin(carHeading) * fwdOffset
                val targetCamZ = upOffset

                camYaw = carHeading
                val lerpRate = if (!initialized) 1f else (20f * dt).coerceAtMost(1f)
                posX += (targetCamX - posX) * lerpRate
                posY += (targetCamY - posY) * lerpRate
                posZ += (targetCamZ - posZ) * lerpRate

                lookAtX = carX + cos(carHeading) * 220f
                lookAtY = carY + sin(carHeading) * 220f
                lookAtZ = 4f

                val targetRoll = -bodyRoll * 0.4f
                camRoll += (targetRoll - camRoll) * (12f * dt).coerceAtMost(1f)
            }
            CameraMode.DRIFT_ACTION -> {
                // Cinematic side drift view
                focalLength = w * 0.90f
                customHorizonRatio = 0.38f

                val sideSign = if (bodyRoll > 0f) 1f else -1f
                val sideAngleOffset = sideSign * 0.45f
                val actionYawTarget = carHeading - sideAngleOffset

                var yawDiff = actionYawTarget - camYaw
                while (yawDiff > PI) yawDiff -= (2 * PI).toFloat()
                while (yawDiff < -PI) yawDiff += (2 * PI).toFloat()
                camYaw += yawDiff * (4f * dt).coerceAtMost(0.4f)

                val chaseDist = 145f
                val targetCamX = carX - cos(camYaw) * chaseDist
                val targetCamY = carY - sin(camYaw) * chaseDist
                val targetCamZ = 68f

                val lerpRate = if (!initialized) 1f else (7.5f * dt).coerceAtMost(1f)
                posX += (targetCamX - posX) * lerpRate
                posY += (targetCamY - posY) * lerpRate
                posZ += (targetCamZ - posZ) * lerpRate

                lookAtX = carX
                lookAtY = carY
                lookAtZ = 14f

                val targetRoll = -bodyRoll * 0.5f
                camRoll += (targetRoll - camRoll) * (8f * dt).coerceAtMost(1f)
            }
        }

        initialized = true

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
        val rLen = sqrt(rxRaw * rxRaw + ryRaw * ryRaw).coerceAtLeast(0.001f)
        var rx = if (rLen > 0.001f) rxRaw / rLen else 1f
        var ry = if (rLen > 0.001f) ryRaw / rLen else 0f
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

    fun toCamSpaceX(wx: Float, wy: Float, wz: Float): Float {
        return r11 * (wx - posX) + r12 * (wy - posY) + r13 * (wz - posZ)
    }

    fun toCamSpaceY(wx: Float, wy: Float, wz: Float): Float {
        return r21 * (wx - posX) + r22 * (wy - posY) + r23 * (wz - posZ)
    }

    fun toCamSpaceZ(wx: Float, wy: Float, wz: Float): Float {
        return r31 * (wx - posX) + r32 * (wy - posY) + r33 * (wz - posZ)
    }

    fun camToScreen(cx: Float, cy: Float, cz: Float): Offset {
        val z = cz.coerceAtLeast(1.0f)
        val sx = (cx / z) * focalLength + (screenWidth * 0.5f)
        val sy = -(cy / z) * focalLength + (screenHeight * customHorizonRatio)
        return Offset(sx, sy)
    }

    fun project(wx: Float, wy: Float, wz: Float): ScreenPoint {
        val cx = toCamSpaceX(wx, wy, wz)
        val cy = toCamSpaceY(wx, wy, wz)
        val cz = toCamSpaceZ(wx, wy, wz)

        if (cz <= 1.5f) {
            return ScreenPoint(0f, 0f, cz, isVisible = false)
        }

        val sx = (cx / cz) * focalLength + (screenWidth * 0.5f)
        val sy = -(cy / cz) * focalLength + (screenHeight * customHorizonRatio)

        val margin = 400f
        val inBounds = sx >= -margin && sx <= screenWidth + margin && sy >= -margin && sy <= screenHeight + margin
        return ScreenPoint(sx, sy, cz, isVisible = inBounds)
    }

    // Projects a 3D line segment with near-plane clipping
    fun projectLineClipped(
        p1x: Float, p1y: Float, p1z: Float,
        p2x: Float, p2y: Float, p2z: Float,
        nearPlane: Float = 2.0f
    ): Pair<Offset, Offset>? {
        val c1x = toCamSpaceX(p1x, p1y, p1z)
        val c1y = toCamSpaceY(p1x, p1y, p1z)
        val c1z = toCamSpaceZ(p1x, p1y, p1z)

        val c2x = toCamSpaceX(p2x, p2y, p2z)
        val c2y = toCamSpaceY(p2x, p2y, p2z)
        val c2z = toCamSpaceZ(p2x, p2y, p2z)

        if (c1z < nearPlane && c2z < nearPlane) return null

        var px1 = c1x; var py1 = c1y; var pz1 = c1z
        var px2 = c2x; var py2 = c2y; var pz2 = c2z

        if (pz1 < nearPlane) {
            val t = (nearPlane - pz1) / (pz2 - pz1)
            px1 = px1 + t * (px2 - px1)
            py1 = py1 + t * (py2 - py1)
            pz1 = nearPlane
        } else if (pz2 < nearPlane) {
            val t = (nearPlane - pz2) / (pz1 - pz2)
            px2 = px2 + t * (px1 - px2)
            py2 = py2 + t * (py1 - py2)
            pz2 = nearPlane
        }

        return Pair(camToScreen(px1, py1, pz1), camToScreen(px2, py2, pz2))
    }

    // Projects a 3D quad with near-plane clipping into an existing Path
    fun projectQuadClipped(
        p0x: Float, p0y: Float, p0z: Float,
        p1x: Float, p1y: Float, p1z: Float,
        p2x: Float, p2y: Float, p2z: Float,
        p3x: Float, p3y: Float, p3z: Float,
        outPath: Path,
        nearPlane: Float = 2.0f
    ): Boolean {
        val c0x = toCamSpaceX(p0x, p0y, p0z)
        val c0y = toCamSpaceY(p0x, p0y, p0z)
        val c0z = toCamSpaceZ(p0x, p0y, p0z)

        val c1x = toCamSpaceX(p1x, p1y, p1z)
        val c1y = toCamSpaceY(p1x, p1y, p1z)
        val c1z = toCamSpaceZ(p1x, p1y, p1z)

        val c2x = toCamSpaceX(p2x, p2y, p2z)
        val c2y = toCamSpaceY(p2x, p2y, p2z)
        val c2z = toCamSpaceZ(p2x, p2y, p2z)

        val c3x = toCamSpaceX(p3x, p3y, p3z)
        val c3y = toCamSpaceY(p3x, p3y, p3z)
        val c3z = toCamSpaceZ(p3x, p3y, p3z)

        if (c0z < nearPlane && c1z < nearPlane && c2z < nearPlane && c3z < nearPlane) {
            return false
        }

        var q0x = c0x; var q0y = c0y; var q0z = c0z
        var q1x = c1x; var q1y = c1y; var q1z = c1z
        var q2x = c2x; var q2y = c2y; var q2z = c2z
        var q3x = c3x; var q3y = c3y; var q3z = c3z

        // Clip edge 0->1
        if (q0z < nearPlane && q1z >= nearPlane) {
            val t = (nearPlane - q0z) / (q1z - q0z)
            q0x = q0x + t * (q1x - q0x)
            q0y = q0y + t * (q1y - q0y)
            q0z = nearPlane
        } else if (q1z < nearPlane && q0z >= nearPlane) {
            val t = (nearPlane - q1z) / (q0z - q1z)
            q1x = q1x + t * (q0x - q1x)
            q1y = q1y + t * (q0y - q1y)
            q1z = nearPlane
        }

        // Clip edge 3->2
        if (q3z < nearPlane && q2z >= nearPlane) {
            val t = (nearPlane - q3z) / (q2z - q3z)
            q3x = q3x + t * (q2x - q3x)
            q3y = q3y + t * (q2y - q3y)
            q3z = nearPlane
        } else if (q2z < nearPlane && q3z >= nearPlane) {
            val t = (nearPlane - q2z) / (q3z - q2z)
            q2x = q2x + t * (q3x - q2x)
            q2y = q2y + t * (q3y - q2y)
            q2z = nearPlane
        }

        if (q0z < nearPlane || q1z < nearPlane || q2z < nearPlane || q3z < nearPlane) {
            return false
        }

        val s0 = camToScreen(q0x, q0y, q0z)
        val s1 = camToScreen(q1x, q1y, q1z)
        val s2 = camToScreen(q2x, q2y, q2z)
        val s3 = camToScreen(q3x, q3y, q3z)

        outPath.reset()
        outPath.moveTo(s0.x, s0.y)
        outPath.lineTo(s1.x, s1.y)
        outPath.lineTo(s2.x, s2.y)
        outPath.lineTo(s3.x, s3.y)
        outPath.close()

        return true
    }

    val horizonScreenY: Float
        get() = screenHeight * customHorizonRatio
}
