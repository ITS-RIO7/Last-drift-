package com.example.game.ui

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.game.model.Track
import com.example.game.physics.DriftPhysicsEngine
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonMagenta
import com.example.ui.theme.NeonOrange
import com.example.ui.theme.NeonYellow
import com.example.ui.theme.NitroBlue
import com.example.ui.theme.RacingDark
import com.example.ui.theme.SynthwaveSkyHorizon
import com.example.ui.theme.SynthwaveSkyMid
import com.example.ui.theme.SynthwaveSkyTop
import com.example.ui.theme.SynthwaveSunGlow
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

class GameCanvasRenderer {

    val camera3D = Game3DCamera()
    private var lastTimeNanos = 0L

    // Reusable Path objects to prevent Garbage Collection pauses and eliminate lag completely
    private val reusableRoadPath = Path()
    private val reusableCarPath = Path()
    private val reusableBeamPath = Path()
    private val reusableBoxPath = Path()

    fun draw(
        drawScope: DrawScope,
        physics: DriftPhysicsEngine,
        textMeasurer: TextMeasurer
    ) {
        val now = System.nanoTime()
        val dt = if (lastTimeNanos == 0L) 0.016f else ((now - lastTimeNanos) / 1_000_000_000f).coerceIn(0.001f, 0.04f)
        lastTimeNanos = now

        with(drawScope) {
            val w = size.width
            val h = size.height

            // Update 3D Chase Camera
            camera3D.update(
                carX = physics.posX,
                carY = physics.posY,
                carHeading = physics.heading,
                carVx = physics.vx,
                carVy = physics.vy,
                carSpeed = physics.speed,
                bodyRoll = physics.bodyRollAngle,
                w = w,
                h = h,
                dt = dt
            )

            // 1. Draw 3D Cyberpunk Synthwave Sky & City Skyline Horizon
            drawCyberpunkSkyline(w, h, camera3D)

            // 2. Draw 3D Ground Floor (Asphalt Plain)
            draw3DGroundPlane(w, h, camera3D)

            // 3. Draw 3D Track Road Ribbon & Curbs (with Distance Culling & zero allocations)
            draw3DTrackRoad(physics.track, camera3D)

            // 4. Draw 3D Skidmarks (capped to nearby recent marks)
            draw3DSkidmarks(physics, camera3D)

            // 5. Draw 3D Apex Beacons & Clipping Targets
            draw3DApexBeacons(physics.track, camera3D)

            // 6. Draw 3D Obstacles (Containers)
            draw3DObstacles(physics.track, camera3D)

            // 7. Draw 3D Tire Smoke Particles (billowing from ground)
            draw3DSmokeParticles(physics, camera3D)

            // 8. Draw 3D Car Model with physical body roll, chassis, wheels & nitro flames
            draw3DCarModel(physics, camera3D)

            // 9. Draw 3D Wall Sparks
            draw3DSparks(physics, camera3D)

            // 10. Draw 3D Floating Score Popups
            draw3DScorePopups(physics, camera3D, textMeasurer)
        }
    }

    private fun DrawScope.drawCyberpunkSkyline(w: Float, h: Float, cam: Game3DCamera) {
        val horizonY = cam.horizonScreenY

        // Sky Gradient
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    SynthwaveSkyTop,
                    SynthwaveSkyMid,
                    SynthwaveSkyHorizon
                ),
                startY = 0f,
                endY = horizonY
            ),
            topLeft = Offset.Zero,
            size = Size(w, horizonY)
        )

        // Synthwave Glowing Sun on Horizon
        val sunRadius = 65f
        val sunCenter = Offset(w * 0.5f, horizonY - 15f)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFFFFF176), SynthwaveSunGlow, Color(0x00FF007F)),
                center = sunCenter,
                radius = sunRadius * 1.4f
            ),
            radius = sunRadius * 1.4f,
            center = sunCenter
        )
        drawCircle(
            color = Color(0xFFFFD54F),
            radius = sunRadius,
            center = sunCenter
        )
        // Horizontal stripes through sun
        for (i in 1..5) {
            val yOffset = sunCenter.y + (i * 9f) - 8f
            if (yOffset < horizonY) {
                drawLine(
                    color = SynthwaveSkyMid,
                    start = Offset(sunCenter.x - sunRadius, yOffset),
                    end = Offset(sunCenter.x + sunRadius, yOffset),
                    strokeWidth = 2.5f
                )
            }
        }

        // Distant Cyberpunk Skyscrapers & Neon Skyline
        val camAngleDeg = cam.camYaw * (180f / PI.toFloat())
        val parallaxOffset = -(camAngleDeg * 4.5f) % w

        val buildings = listOf(
            Triple(40f, 110f, 0xFF00F5FF),
            Triple(90f, 150f, 0xFFFF007F),
            Triple(140f, 85f, 0xFFFFD600),
            Triple(190f, 135f, 0xFFA855F7),
            Triple(250f, 100f, 0xFF00E676),
            Triple(310f, 160f, 0xFF00F5FF),
            Triple(370f, 120f, 0xFFFF6D00),
            Triple(430f, 145f, 0xFFFF007F),
            Triple(490f, 105f, 0xFF00E5FF),
            Triple(550f, 130f, 0xFFA855F7)
        )

        for (copy in -1..2) {
            val baseShift = parallaxOffset + (copy * 600f)
            for ((bx, bh, glowColor) in buildings) {
                val screenBx = baseShift + bx
                if (screenBx in -80f..w + 80f) {
                    val topY = horizonY - bh
                    drawRect(
                        color = Color(0xFF0C0816),
                        topLeft = Offset(screenBx, topY),
                        size = Size(36f, bh)
                    )
                    drawLine(
                        color = Color(glowColor),
                        start = Offset(screenBx, topY),
                        end = Offset(screenBx + 36f, topY),
                        strokeWidth = 2f
                    )
                }
            }
        }
    }

    private fun DrawScope.draw3DGroundPlane(w: Float, h: Float, cam: Game3DCamera) {
        val horizonY = cam.horizonScreenY
        drawRect(
            color = RacingDark,
            topLeft = Offset(0f, horizonY),
            size = Size(w, h - horizonY)
        )
    }

    private fun DrawScope.draw3DTrackRoad(track: Track, cam: Game3DCamera) {
        val center = track.centerLine
        val inner = track.innerBoundary
        val outer = track.outerBoundary
        val numSegments = minOf(center.size, inner.size, outer.size)
        if (numSegments < 2) return

        val curbColors = listOf(NeonMagenta, Color.White, NeonCyan, NeonYellow)
        val maxDistSq = 1100f * 1100f

        for (i in 0 until numSegments - 1) {
            val nextIdx = (i + 1) % numSegments

            // Distance culling: skip far segments to eliminate lag
            val dx = center[i].x - cam.posX
            val dy = center[i].y - cam.posY
            if (dx * dx + dy * dy > maxDistSq) continue

            val o1 = cam.project(outer[i].x, outer[i].y, 0f)
            val o2 = cam.project(outer[nextIdx].x, outer[nextIdx].y, 0f)
            val i1 = cam.project(inner[i].x, inner[i].y, 0f)
            val i2 = cam.project(inner[nextIdx].x, inner[nextIdx].y, 0f)

            if (!o1.isVisible && !o2.isVisible && !i1.isVisible && !i2.isVisible) continue

            // 3D Road Quad using reusable Path (ZERO heap allocation!)
            reusableRoadPath.reset()
            reusableRoadPath.moveTo(o1.x, o1.y)
            reusableRoadPath.lineTo(o2.x, o2.y)
            reusableRoadPath.lineTo(i2.x, i2.y)
            reusableRoadPath.lineTo(i1.x, i1.y)
            reusableRoadPath.close()

            drawPath(
                path = reusableRoadPath,
                color = Color(track.roadColor)
            )

            // Center Dash Line
            val c1 = cam.project(center[i].x, center[i].y, 0.5f)
            val c2 = cam.project(center[nextIdx].x, center[nextIdx].y, 0.5f)
            if (c1.isVisible && c2.isVisible) {
                drawLine(
                    color = Color(0x66FFFFFF),
                    start = Offset(c1.x, c1.y),
                    end = Offset(c2.x, c2.y),
                    strokeWidth = (220f / ((c1.depth + c2.depth) * 0.5f)).coerceIn(1.5f, 6f)
                )
            }

            // Outer 3D Curb
            val curbColor = curbColors[(i / 2) % curbColors.size]
            if (o1.isVisible && o2.isVisible) {
                val curbWidth = (340f / o1.depth).coerceIn(2.5f, 12f)
                drawLine(
                    color = curbColor,
                    start = Offset(o1.x, o1.y),
                    end = Offset(o2.x, o2.y),
                    strokeWidth = curbWidth,
                    cap = StrokeCap.Round
                )
            }

            // Inner 3D Curb
            if (i1.isVisible && i2.isVisible) {
                val curbWidth = (340f / i1.depth).coerceIn(2.5f, 12f)
                drawLine(
                    color = Color(track.curbColor),
                    start = Offset(i1.x, i1.y),
                    end = Offset(i2.x, i2.y),
                    strokeWidth = curbWidth,
                    cap = StrokeCap.Round
                )
            }
        }
    }

    private fun DrawScope.draw3DSkidmarks(physics: DriftPhysicsEngine, cam: Game3DCamera) {
        val total = physics.skidSegments.size
        val startIdx = if (total > 65) total - 65 else 0

        for (idx in startIdx until total) {
            val seg = physics.skidSegments[idx]
            val p1 = cam.project(seg.x1, seg.y1, 0.4f)
            val p2 = cam.project(seg.x2, seg.y2, 0.4f)
            if (p1.isVisible && p2.isVisible) {
                val strokeW = (320f / p1.depth).coerceIn(2f, 8f)
                drawLine(
                    color = Color(0xFF0A0D14).copy(alpha = seg.alpha),
                    start = Offset(p1.x, p1.y),
                    end = Offset(p2.x, p2.y),
                    strokeWidth = strokeW,
                    cap = StrokeCap.Round
                )
            }
        }
    }

    private fun DrawScope.draw3DApexBeacons(track: Track, cam: Game3DCamera) {
        val pulse = (sin(System.currentTimeMillis() / 200.0).toFloat() + 1f) * 0.5f

        for (clip in track.clippingPoints) {
            val groundPt = cam.project(clip.x, clip.y, 0f)
            val topPt = cam.project(clip.x, clip.y, 65f)

            if (groundPt.isVisible) {
                val baseRadius = (clip.radius * cam.focalLength / groundPt.depth).coerceIn(12f, 140f)

                drawCircle(
                    color = NeonCyan.copy(alpha = 0.2f + pulse * 0.25f),
                    radius = baseRadius,
                    center = Offset(groundPt.x, groundPt.y),
                    style = Fill
                )
                drawCircle(
                    color = NeonCyan,
                    radius = baseRadius,
                    center = Offset(groundPt.x, groundPt.y),
                    style = Stroke(width = (160f / groundPt.depth).coerceIn(2f, 5f))
                )

                if (topPt.isVisible) {
                    drawLine(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color.Transparent, NeonCyan, Color.White),
                            startY = topPt.y,
                            endY = groundPt.y
                        ),
                        start = Offset(topPt.x, topPt.y),
                        end = Offset(groundPt.x, groundPt.y),
                        strokeWidth = (220f / groundPt.depth).coerceIn(3f, 10f)
                    )
                }
            }
        }
    }

    private fun DrawScope.draw3DObstacles(track: Track, cam: Game3DCamera) {
        for (obs in track.obstacles) {
            val h = 28f
            val cosR = cos(obs.rotationDeg * (PI.toFloat() / 180f))
            val sinR = sin(obs.rotationDeg * (PI.toFloat() / 180f))
            val hw = obs.width * 0.5f
            val hh = obs.height * 0.5f

            fun getCorner(ox: Float, oy: Float, oz: Float): ScreenPoint {
                val wx = obs.x + (ox * cosR - oy * sinR)
                val wy = obs.y + (ox * sinR + oy * cosR)
                return cam.project(wx, wy, oz)
            }

            val b1 = getCorner(-hw, -hh, 0f)
            val b2 = getCorner(hw, -hh, 0f)
            val t1 = getCorner(-hw, -hh, h)
            val t2 = getCorner(hw, -hh, h)
            val t3 = getCorner(hw, hh, h)
            val t4 = getCorner(-hw, hh, h)

            if (t1.isVisible && t2.isVisible && t3.isVisible && t4.isVisible) {
                reusableBoxPath.reset()
                reusableBoxPath.moveTo(t1.x, t1.y)
                reusableBoxPath.lineTo(t2.x, t2.y)
                reusableBoxPath.lineTo(t3.x, t3.y)
                reusableBoxPath.lineTo(t4.x, t4.y)
                reusableBoxPath.close()

                drawPath(path = reusableBoxPath, color = Color(0xFF475569))
                drawPath(path = reusableBoxPath, color = NeonOrange, style = Stroke(width = 2f))
            }

            if (b1.isVisible && b2.isVisible && t2.isVisible && t1.isVisible) {
                reusableBoxPath.reset()
                reusableBoxPath.moveTo(b1.x, b1.y)
                reusableBoxPath.lineTo(b2.x, b2.y)
                reusableBoxPath.lineTo(t2.x, t2.y)
                reusableBoxPath.lineTo(t1.x, t1.y)
                reusableBoxPath.close()

                drawPath(path = reusableBoxPath, color = Color(0xFF334155))
            }
        }
    }

    private fun DrawScope.draw3DSmokeParticles(physics: DriftPhysicsEngine, cam: Game3DCamera) {
        val total = physics.smokeParticles.size
        val start = if (total > 50) total - 50 else 0

        for (i in start until total) {
            val p = physics.smokeParticles[i]
            val sp = cam.project(p.x, p.y, p.z)
            if (sp.isVisible) {
                val screenRadius = (p.radius * cam.focalLength / sp.depth).coerceIn(4f, 75f)
                drawCircle(
                    color = Color(p.color).copy(alpha = (p.alpha * 0.65f).coerceIn(0f, 1f)),
                    radius = screenRadius,
                    center = Offset(sp.x, sp.y)
                )
            }
        }
    }

    private fun DrawScope.draw3DCarModel(physics: DriftPhysicsEngine, cam: Game3DCamera) {
        val car = physics.car
        val cx = physics.posX
        val cy = physics.posY
        val heading = physics.heading
        val bodyRoll = physics.bodyRollAngle
        val bodyPitch = physics.bodyPitchAngle

        val length = physics.carLength
        val width = physics.carWidth
        val height = 15f

        val cosH = cos(heading)
        val sinH = sin(heading)
        val cosRoll = cos(bodyRoll)
        val sinRoll = sin(bodyRoll)
        val cosPitch = cos(bodyPitch)
        val sinPitch = sin(bodyPitch)

        fun projectCarPoint(localFwd: Float, localRight: Float, localUp: Float): ScreenPoint {
            val rolledRight = localRight * cosRoll - localUp * sinRoll
            val rolledUp = localRight * sinRoll + localUp * cosRoll
            val pitchedForward = localFwd * cosPitch - rolledUp * sinPitch
            val pitchedUp = localFwd * sinPitch + rolledUp * cosPitch
            val wx = cx + (pitchedForward * cosH - rolledRight * sinH)
            val wy = cy + (pitchedForward * sinH + rolledRight * cosH)
            val wz = (pitchedUp).coerceAtLeast(0.5f)
            return cam.project(wx, wy, wz)
        }

        // 1. 3D Underglow on road surface
        val ug1 = cam.project(cx - cosH * length * 0.55f - sinH * width * 0.65f, cy - sinH * length * 0.55f + cosH * width * 0.65f, 0.2f)
        val ug2 = cam.project(cx + cosH * length * 0.55f - sinH * width * 0.65f, cy + sinH * length * 0.55f + cosH * width * 0.65f, 0.2f)
        val ug3 = cam.project(cx + cosH * length * 0.55f + sinH * width * 0.65f, cy + sinH * length * 0.55f - cosH * width * 0.65f, 0.2f)
        val ug4 = cam.project(cx - cosH * length * 0.55f + sinH * width * 0.65f, cy - sinH * length * 0.55f - cosH * width * 0.65f, 0.2f)

        if (ug1.isVisible && ug2.isVisible && ug3.isVisible && ug4.isVisible) {
            reusableCarPath.reset()
            reusableCarPath.moveTo(ug1.x, ug1.y)
            reusableCarPath.lineTo(ug2.x, ug2.y)
            reusableCarPath.lineTo(ug3.x, ug3.y)
            reusableCarPath.lineTo(ug4.x, ug4.y)
            reusableCarPath.close()
            drawPath(path = reusableCarPath, color = Color(car.underglowColor).copy(alpha = 0.6f))
        }

        // 2. Headlight Beams in 3D
        val hlLeft = projectCarPoint(length * 0.5f, -width * 0.35f, 4f)
        val hlRight = projectCarPoint(length * 0.5f, width * 0.35f, 4f)
        val hlBeamFwd = length * 0.5f + 160f
        val hlLeftEnd = projectCarPoint(hlBeamFwd, -width * 1.5f, 0.5f)
        val hlRightEnd = projectCarPoint(hlBeamFwd, width * 1.5f, 0.5f)

        if (hlLeft.isVisible && hlLeftEnd.isVisible && hlRightEnd.isVisible && hlRight.isVisible) {
            reusableBeamPath.reset()
            reusableBeamPath.moveTo(hlLeft.x, hlLeft.y)
            reusableBeamPath.lineTo(hlLeftEnd.x, hlLeftEnd.y)
            reusableBeamPath.lineTo(hlRightEnd.x, hlRightEnd.y)
            reusableBeamPath.lineTo(hlRight.x, hlRight.y)
            reusableBeamPath.close()

            drawPath(
                path = reusableBeamPath,
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0x4500F5FF), Color(0x1000F5FF), Color.Transparent),
                    startY = minOf(hlLeft.y, hlRight.y),
                    endY = maxOf(hlLeftEnd.y, hlRightEnd.y)
                )
            )
        }

        // 3. 3D Wheels
        val wheelRadius = 6.2f
        val wheelWidth = 3.6f
        val rearFwd = -length * 0.35f
        val frontFwd = length * 0.35f
        val sideOut = width * 0.52f

        draw3DWheel(rearFwd, -sideOut, 0f, wheelRadius, wheelWidth, ::projectCarPoint)
        draw3DWheel(rearFwd, sideOut, 0f, wheelRadius, wheelWidth, ::projectCarPoint)
        draw3DWheel(frontFwd, -sideOut, physics.steerAngle, wheelRadius, wheelWidth, ::projectCarPoint)
        draw3DWheel(frontFwd, sideOut, physics.steerAngle, wheelRadius, wheelWidth, ::projectCarPoint)

        // 4. 3D Lower Chassis Body Points
        val fL_bot = projectCarPoint(length * 0.5f, -width * 0.46f, 3f)
        val fR_bot = projectCarPoint(length * 0.5f, width * 0.46f, 3f)
        val rR_bot = projectCarPoint(-length * 0.48f, width * 0.46f, 3.5f)
        val rL_bot = projectCarPoint(-length * 0.48f, -width * 0.46f, 3.5f)

        val fL_mid = projectCarPoint(length * 0.48f, -width * 0.46f, 8.5f)
        val fR_mid = projectCarPoint(length * 0.48f, width * 0.46f, 8.5f)
        val rR_mid = projectCarPoint(-length * 0.46f, width * 0.46f, 9f)
        val rL_mid = projectCarPoint(-length * 0.46f, -width * 0.46f, 9f)

        // Cabin Roof & Windshield
        val cabFL = projectCarPoint(length * 0.12f, -width * 0.36f, height)
        val cabFR = projectCarPoint(length * 0.12f, width * 0.36f, height)
        val cabRR = projectCarPoint(-length * 0.26f, width * 0.36f, height)
        val cabRL = projectCarPoint(-length * 0.26f, -width * 0.36f, height)

        val bodyColor = Color(car.bodyColor)
        val accentColor = Color(car.accentColor)

        // Rear Bumper Face
        if (rL_bot.isVisible && rR_bot.isVisible && rR_mid.isVisible && rL_mid.isVisible) {
            reusableCarPath.reset()
            reusableCarPath.moveTo(rL_bot.x, rL_bot.y)
            reusableCarPath.lineTo(rR_bot.x, rR_bot.y)
            reusableCarPath.lineTo(rR_mid.x, rR_mid.y)
            reusableCarPath.lineTo(rL_mid.x, rL_mid.y)
            reusableCarPath.close()
            drawPath(path = reusableCarPath, color = bodyColor.copy(alpha = 0.9f))

            drawLine(
                color = Color(0xFFFF1744),
                start = Offset((rL_mid.x * 0.8f + rR_mid.x * 0.2f), (rL_mid.y * 0.8f + rR_mid.y * 0.2f)),
                end = Offset((rL_mid.x * 0.2f + rR_mid.x * 0.8f), (rL_mid.y * 0.2f + rR_mid.y * 0.8f)),
                strokeWidth = 3.5f
            )
        }

        // Left Body Side Face
        if (fL_bot.isVisible && rL_bot.isVisible && rL_mid.isVisible && fL_mid.isVisible) {
            reusableCarPath.reset()
            reusableCarPath.moveTo(fL_bot.x, fL_bot.y)
            reusableCarPath.lineTo(rL_bot.x, rL_bot.y)
            reusableCarPath.lineTo(rL_mid.x, rL_mid.y)
            reusableCarPath.lineTo(fL_mid.x, fL_mid.y)
            reusableCarPath.close()
            drawPath(path = reusableCarPath, color = bodyColor)
        }

        // Right Body Side Face
        if (fR_bot.isVisible && rR_bot.isVisible && rR_mid.isVisible && fR_mid.isVisible) {
            reusableCarPath.reset()
            reusableCarPath.moveTo(fR_bot.x, fR_bot.y)
            reusableCarPath.lineTo(rR_bot.x, rR_bot.y)
            reusableCarPath.lineTo(rR_mid.x, rR_mid.y)
            reusableCarPath.lineTo(fR_mid.x, fR_mid.y)
            reusableCarPath.close()
            drawPath(path = reusableCarPath, color = bodyColor)
        }

        // Hood Top Deck
        if (fL_mid.isVisible && fR_mid.isVisible && cabFR.isVisible && cabFL.isVisible) {
            reusableCarPath.reset()
            reusableCarPath.moveTo(fL_mid.x, fL_mid.y)
            reusableCarPath.lineTo(fR_mid.x, fR_mid.y)
            reusableCarPath.lineTo(cabFR.x, cabFR.y)
            reusableCarPath.lineTo(cabFL.x, cabFL.y)
            reusableCarPath.close()
            drawPath(path = reusableCarPath, color = bodyColor)

            val hoodCenterStart = Offset((fL_mid.x + fR_mid.x) * 0.5f, (fL_mid.y + fR_mid.y) * 0.5f)
            val hoodCenterEnd = Offset((cabFL.x + cabFR.x) * 0.5f, (cabFL.y + cabFR.y) * 0.5f)
            drawLine(color = accentColor, start = hoodCenterStart, end = hoodCenterEnd, strokeWidth = 7f)
        }

        // Rear Trunk Deck
        if (cabRL.isVisible && cabRR.isVisible && rR_mid.isVisible && rL_mid.isVisible) {
            reusableCarPath.reset()
            reusableCarPath.moveTo(cabRL.x, cabRL.y)
            reusableCarPath.lineTo(cabRR.x, cabRR.y)
            reusableCarPath.lineTo(rR_mid.x, rR_mid.y)
            reusableCarPath.lineTo(rL_mid.x, rL_mid.y)
            reusableCarPath.close()
            drawPath(path = reusableCarPath, color = bodyColor)
        }

        // Cabin Roof
        if (cabFL.isVisible && cabFR.isVisible && cabRR.isVisible && cabRL.isVisible) {
            reusableCarPath.reset()
            reusableCarPath.moveTo(cabFL.x, cabFL.y)
            reusableCarPath.lineTo(cabFR.x, cabFR.y)
            reusableCarPath.lineTo(cabRR.x, cabRR.y)
            reusableCarPath.lineTo(cabRL.x, cabRL.y)
            reusableCarPath.close()
            drawPath(path = reusableCarPath, color = bodyColor)

            drawLine(
                color = accentColor,
                start = Offset((cabFL.x + cabFR.x) * 0.5f, (cabFL.y + cabFR.y) * 0.5f),
                end = Offset((cabRL.x + cabRR.x) * 0.5f, (cabRL.y + cabRR.y) * 0.5f),
                strokeWidth = 7f
            )
        }

        // 5. 3D Elevated Rear Spoiler (Wing)
        val wingHeight = height + 3.5f
        val wingL = projectCarPoint(-length * 0.5f, -width * 0.48f, wingHeight)
        val wingR = projectCarPoint(-length * 0.5f, width * 0.48f, wingHeight)
        val strutL_bot = projectCarPoint(-length * 0.44f, -width * 0.24f, 9f)
        val strutR_bot = projectCarPoint(-length * 0.44f, width * 0.24f, 9f)
        val strutL_top = projectCarPoint(-length * 0.44f, -width * 0.24f, wingHeight)
        val strutR_top = projectCarPoint(-length * 0.44f, width * 0.24f, wingHeight)

        if (wingL.isVisible && wingR.isVisible) {
            if (strutL_bot.isVisible && strutL_top.isVisible) {
                drawLine(color = Color(0xFF1E293B), start = Offset(strutL_bot.x, strutL_bot.y), end = Offset(strutL_top.x, strutL_top.y), strokeWidth = 2.5f)
            }
            if (strutR_bot.isVisible && strutR_top.isVisible) {
                drawLine(color = Color(0xFF1E293B), start = Offset(strutR_bot.x, strutR_bot.y), end = Offset(strutR_top.x, strutR_top.y), strokeWidth = 2.5f)
            }
            drawLine(color = accentColor, start = Offset(wingL.x, wingL.y), end = Offset(wingR.x, wingR.y), strokeWidth = 4.5f, cap = StrokeCap.Round)
        }

        // 6. Dual 3D Nitro Exhaust Flames
        if (physics.isNitroActive) {
            val exL = projectCarPoint(-length * 0.5f, -width * 0.22f, 4f)
            val exR = projectCarPoint(-length * 0.5f, width * 0.22f, 4f)
            val flameFwd = -length * 0.5f - (32f + sin(System.currentTimeMillis() / 25.0).toFloat() * 10f)
            val flameEndL = projectCarPoint(flameFwd, -width * 0.22f, 4f)
            val flameEndR = projectCarPoint(flameFwd, width * 0.22f, 4f)

            if (exL.isVisible && flameEndL.isVisible) {
                drawLine(color = NitroBlue, start = Offset(exL.x, exL.y), end = Offset(flameEndL.x, flameEndL.y), strokeWidth = 6f, cap = StrokeCap.Round)
                drawLine(color = Color.White, start = Offset(exL.x, exL.y), end = Offset(flameEndL.x, flameEndL.y), strokeWidth = 2.5f, cap = StrokeCap.Round)
            }
            if (exR.isVisible && flameEndR.isVisible) {
                drawLine(color = NitroBlue, start = Offset(exR.x, exR.y), end = Offset(flameEndR.x, flameEndR.y), strokeWidth = 6f, cap = StrokeCap.Round)
                drawLine(color = Color.White, start = Offset(exR.x, exR.y), end = Offset(flameEndR.x, flameEndR.y), strokeWidth = 2.5f, cap = StrokeCap.Round)
            }
        }
    }

    private fun DrawScope.draw3DWheel(
        fwd: Float,
        side: Float,
        steerAngle: Float,
        radius: Float,
        width: Float,
        projectFn: (Float, Float, Float) -> ScreenPoint
    ) {
        val cosS = cos(steerAngle)
        val sinS = sin(steerAngle)

        val w1 = projectFn(fwd - radius * cosS, side, radius)
        val w2 = projectFn(fwd + radius * cosS, side, radius)

        if (w1.isVisible && w2.isVisible) {
            val tireWidth = (width * 300f / w1.depth).coerceIn(3.5f, 14f)
            drawLine(
                color = Color(0xFF0F1116),
                start = Offset(w1.x, w1.y),
                end = Offset(w2.x, w2.y),
                strokeWidth = tireWidth,
                cap = StrokeCap.Round
            )
            val midX = (w1.x + w2.x) * 0.5f
            val midY = (w1.y + w2.y) * 0.5f
            drawCircle(
                color = Color(0xFFCBD5E1),
                radius = (tireWidth * 0.35f).coerceAtLeast(1.5f),
                center = Offset(midX, midY)
            )
        }
    }

    private fun DrawScope.draw3DSparks(physics: DriftPhysicsEngine, cam: Game3DCamera) {
        for (s in physics.sparkParticles) {
            val pt = cam.project(s.x, s.y, s.z)
            if (pt.isVisible) {
                val sparkRadius = (60f / pt.depth).coerceIn(2f, 5f)
                drawCircle(
                    color = Color(s.color),
                    radius = sparkRadius,
                    center = Offset(pt.x, pt.y)
                )
            }
        }
    }

    private fun DrawScope.draw3DScorePopups(physics: DriftPhysicsEngine, cam: Game3DCamera, textMeasurer: TextMeasurer) {
        for (pop in physics.floatPopups) {
            val pt = cam.project(pop.x, pop.y, pop.z + pop.offsetY * -0.4f)
            if (pt.isVisible) {
                val fontSize = (18f * cam.focalLength / pt.depth).coerceIn(11f, 28f)
                val style = TextStyle(
                    color = Color(pop.color).copy(alpha = pop.alpha.coerceIn(0f, 1f)),
                    fontSize = fontSize.sp,
                    fontWeight = FontWeight.Black
                )
                val result = textMeasurer.measure(pop.text, style)
                drawText(
                    textLayoutResult = result,
                    topLeft = Offset(pt.x - result.size.width * 0.5f, pt.y - result.size.height * 0.5f)
                )
            }
        }
    }
}
