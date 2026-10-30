package com.example.game.ui

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.game.model.CityBuilding
import com.example.game.model.CitySkybridge
import com.example.game.model.CityStreetLamp
import com.example.game.model.Track
import com.example.game.physics.DriftPhysicsEngine
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonMagenta
import com.example.ui.theme.NeonOrange
import com.example.ui.theme.NeonYellow
import com.example.ui.theme.NitroBlue
import com.example.ui.theme.RacingDark
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

class GameCanvasRenderer {

    val camera3D = Game3DCamera()
    private var lastTimeNanos = 0L

    // Reusable Path instances to completely eliminate Garbage Collection allocations and lag
    private val reusableRoadPath = Path()
    private val reusableBarrierPath = Path()
    private val reusableCarPath = Path()
    private val reusableBeamPath = Path()
    private val reusableBuildingPath = Path()

    fun draw(
        drawScope: DrawScope,
        physics: DriftPhysicsEngine,
        textMeasurer: TextMeasurer,
        cameraMode: com.example.game.model.CameraMode = com.example.game.model.CameraMode.CHASE
    ) {
        val now = System.nanoTime()
        val dt = if (lastTimeNanos == 0L) 0.016f else ((now - lastTimeNanos) / 1_000_000_000f).coerceIn(0.001f, 0.04f)
        lastTimeNanos = now

        with(drawScope) {
            val w = size.width
            val h = size.height

            // Update 3D Camera with selected camera mode
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
                dt = dt,
                cameraMode = cameraMode
            )

            // 1. Draw Realistic Dusk / Sunset City Atmosphere Sky
            drawRealisticTwilightSky(w, h, camera3D)

            // 2. Draw 3D Ground Base
            draw3DGroundPlane(w, h, camera3D)

            // 3. Draw 3D City Buildings & Architecture (Rod City Architecture, Speed Loop Parking, Arcade)
            draw3DCityArchitecture(physics.track, camera3D, textMeasurer)

            // 4. Draw 3D Elevated Highway Road Ribbon (Asphalt, Dashed Lane Lines)
            draw3DElevatedHighwayRoad(physics.track, camera3D)

            // 5. Draw 3D Concrete Parapet Barriers (Guardrails with Amber Warning Reflectors)
            draw3DConcreteParapetBarriers(physics.track, camera3D)

            // 6. Draw 3D Street Lamps along the highway curve
            draw3DStreetLamps(physics.track, camera3D)

            // 7. Draw 3D Skidmarks (Rubber tracks laid down by drifting tires)
            draw3DSkidmarks(physics, camera3D)

            // 8. Draw 3D Drift Clipping Point Beacons
            draw3DApexBeacons(physics.track, camera3D)

            // 9. Draw Volumetric Billowing Tire Smoke
            draw3DVolumetricSmoke(physics, camera3D)

            // 10. Draw 3D Flagship Supercar Model (Aeroblade ROD #42 with dual-tone livery, decals & wing)
            draw3DSupercarModel(physics, camera3D, textMeasurer)

            // 11. Draw 3D Wall Scrape Sparks
            draw3DSparks(physics, camera3D)

            // 12. Draw 3D Floating Score Popups
            draw3DScorePopups(physics, camera3D, textMeasurer)
        }
    }

    // =========================================================================
    // 1. REALISTIC DUSK / SUNSET SKYLINE (Matching Reference Image)
    // =========================================================================
    private fun DrawScope.drawRealisticTwilightSky(w: Float, h: Float, cam: Game3DCamera) {
        val horizonY = cam.horizonScreenY

        // Dusk sky gradient: twilight slate blue to warm golden ivory horizon glow
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF475569), // Slate twilight dusk
                    Color(0xFF64748B),
                    Color(0xFF94A3B8),
                    Color(0xFFCBD5E1),
                    Color(0xFFFED7AA), // Warm golden dusk glow
                    Color(0xFFFFF7ED)  // Soft ivory horizon haze
                ),
                startY = 0f,
                endY = horizonY + 20f
            ),
            topLeft = Offset.Zero,
            size = Size(w, horizonY + 20f)
        )

        // Distant atmospheric horizon silhouettes
        val camAngleDeg = cam.camYaw * (180f / PI.toFloat())
        val parallaxOffset = -(camAngleDeg * 3.5f) % w

        // Distant background skyscrapers silhouette
        val distantTowers = listOf(
            Triple(30f, 95f, 0xFF475569),
            Triple(75f, 130f, 0xFF334155),
            Triple(130f, 80f, 0xFF475569),
            Triple(180f, 150f, 0xFF1E293B),
            Triple(240f, 110f, 0xFF334155),
            Triple(310f, 140f, 0xFF475569),
            Triple(380f, 100f, 0xFF334155),
            Triple(440f, 160f, 0xFF1E293B),
            Triple(510f, 85f, 0xFF475569),
            Triple(560f, 125f, 0xFF334155)
        )

        for (copy in -1..2) {
            val baseShift = parallaxOffset + (copy * 600f)
            for ((bx, bh, colorHex) in distantTowers) {
                val screenBx = baseShift + bx
                if (screenBx in -100f..w + 100f) {
                    val topY = horizonY - bh * 0.75f
                    drawRect(
                        color = Color(colorHex).copy(alpha = 0.5f),
                        topLeft = Offset(screenBx, topY),
                        size = Size(42f, horizonY - topY)
                    )
                }
            }
        }
    }

    private fun DrawScope.draw3DGroundPlane(w: Float, h: Float, cam: Game3DCamera) {
        val horizonY = cam.horizonScreenY
        drawRect(
            color = Color(0xFF0F172A),
            topLeft = Offset(0f, horizonY),
            size = Size(w, h - horizonY)
        )
    }

    // =========================================================================
    // 2. 3D CITY ARCHITECTURE ("ROD CITY ARCHITECTURE", "SPEED LOOP PARKING", ARCADE)
    // =========================================================================
    private fun DrawScope.draw3DCityArchitecture(track: Track, cam: Game3DCamera, textMeasurer: TextMeasurer) {
        // Draw Skybridges
        for (sb in track.skybridges) {
            val sBot = cam.project(sb.startX, sb.startY, sb.height)
            val eBot = cam.project(sb.endX, sb.endY, sb.height)
            val sTop = cam.project(sb.startX, sb.startY, sb.height + sb.beamWidth)
            val eTop = cam.project(sb.endX, sb.endY, sb.height + sb.beamWidth)

            if (sBot.isVisible || eBot.isVisible) {
                reusableBuildingPath.reset()
                reusableBuildingPath.moveTo(sBot.x, sBot.y)
                reusableBuildingPath.lineTo(eBot.x, eBot.y)
                reusableBuildingPath.lineTo(eTop.x, eTop.y)
                reusableBuildingPath.lineTo(sTop.x, sTop.y)
                reusableBuildingPath.close()

                drawPath(path = reusableBuildingPath, color = Color(0xFF334155).copy(alpha = 0.95f))
                drawPath(path = reusableBuildingPath, color = Color(0xFF64748B), style = Stroke(width = 2f))

                // Skybridge windows
                drawLine(
                    color = Color(0xFF00E5FF).copy(alpha = 0.7f),
                    start = Offset(sBot.x * 0.5f + sTop.x * 0.5f, sBot.y * 0.5f + sTop.y * 0.5f),
                    end = Offset(eBot.x * 0.5f + eTop.x * 0.5f, eBot.y * 0.5f + eTop.y * 0.5f),
                    strokeWidth = 3f
                )
            }
        }

        // Draw Buildings
        for (b in track.buildings) {
            draw3DBuilding(b, cam, textMeasurer)
        }
    }

    private fun DrawScope.draw3DBuilding(
        b: CityBuilding,
        cam: Game3DCamera,
        textMeasurer: TextMeasurer
    ) {
        val hw = b.width * 0.5f
        val hl = b.length * 0.5f
        val h = b.height

        val cosR = cos(b.rotationDeg * (PI.toFloat() / 180f))
        val sinR = sin(b.rotationDeg * (PI.toFloat() / 180f))

        fun getCorner(ox: Float, oy: Float, oz: Float): ScreenPoint {
            val wx = b.x + (ox * cosR - oy * sinR)
            val wy = b.y + (ox * sinR + oy * cosR)
            return cam.project(wx, wy, oz)
        }

        // Bottom corners
        val b1 = getCorner(-hw, -hl, 0f)
        val b2 = getCorner(hw, -hl, 0f)
        val b3 = getCorner(hw, hl, 0f)
        val b4 = getCorner(-hw, hl, 0f)

        // Top corners
        val t1 = getCorner(-hw, -hl, h)
        val t2 = getCorner(hw, -hl, h)
        val t3 = getCorner(hw, hl, h)
        val t4 = getCorner(-hw, hl, h)

        if (!t1.isVisible && !t2.isVisible && !t3.isVisible && !t4.isVisible &&
            !b1.isVisible && !b2.isVisible && !b3.isVisible && !b4.isVisible
        ) return

        val baseColor = Color(b.facadeColor)

        // Draw Front Face (b1 - b2 - t2 - t1)
        if (b1.isVisible && b2.isVisible && t2.isVisible && t1.isVisible) {
            reusableBuildingPath.reset()
            reusableBuildingPath.moveTo(b1.x, b1.y)
            reusableBuildingPath.lineTo(b2.x, b2.y)
            reusableBuildingPath.lineTo(t2.x, t2.y)
            reusableBuildingPath.lineTo(t1.x, t1.y)
            reusableBuildingPath.close()

            drawPath(path = reusableBuildingPath, color = baseColor)
            drawPath(path = reusableBuildingPath, color = Color(0x33FFFFFF), style = Stroke(width = 1.5f))

            // Architectural facade window grid / parking levels
            if (b.isParkingDeck) {
                // Parking Garage open floors & guardrails
                for (floor in 1..4) {
                    val ratio = floor / 5f
                    val fLeft = Offset(b1.x * (1f - ratio) + t1.x * ratio, b1.y * (1f - ratio) + t1.y * ratio)
                    val fRight = Offset(b2.x * (1f - ratio) + t2.x * ratio, b2.y * (1f - ratio) + t2.y * ratio)
                    drawLine(color = Color(0xFF1E293B), start = fLeft, end = fRight, strokeWidth = 5f)
                    drawLine(color = Color(0xFFFFD54F).copy(alpha = 0.6f), start = fLeft, end = fRight, strokeWidth = 1.5f)
                }
            } else {
                // Modern Skyscraper lit office window grid
                for (row in 1..7) {
                    val r = row / 9f
                    val wLeft = Offset(b1.x * (1f - r) + t1.x * r, b1.y * (1f - r) + t1.y * r)
                    val wRight = Offset(b2.x * (1f - r) + t2.x * r, b2.y * (1f - r) + t2.y * r)
                    drawLine(
                        color = Color(b.windowGlowColor).copy(alpha = if (row % 2 == 0) 0.65f else 0.4f),
                        start = wLeft,
                        end = wRight,
                        strokeWidth = (220f / t1.depth).coerceIn(1.5f, 4f)
                    )
                }
            }
        }

        // Draw Side Face (b2 - b3 - t3 - t2)
        if (b2.isVisible && b3.isVisible && t3.isVisible && t2.isVisible) {
            reusableBuildingPath.reset()
            reusableBuildingPath.moveTo(b2.x, b2.y)
            reusableBuildingPath.lineTo(b3.x, b3.y)
            reusableBuildingPath.lineTo(t3.x, t3.y)
            reusableBuildingPath.lineTo(t2.x, t2.y)
            reusableBuildingPath.close()

            drawPath(path = reusableBuildingPath, color = baseColor.copy(alpha = 0.85f))
            drawPath(path = reusableBuildingPath, color = Color(0x22FFFFFF), style = Stroke(width = 1.5f))
        }

        // Draw Rooftop Face (t1 - t2 - t3 - t4)
        if (t1.isVisible && t2.isVisible && t3.isVisible && t4.isVisible) {
            reusableBuildingPath.reset()
            reusableBuildingPath.moveTo(t1.x, t1.y)
            reusableBuildingPath.lineTo(t2.x, t2.y)
            reusableBuildingPath.lineTo(t3.x, t3.y)
            reusableBuildingPath.lineTo(t4.x, t4.y)
            reusableBuildingPath.close()

            drawPath(path = reusableBuildingPath, color = Color(0xFF1E293B))
        }

        // Render Illuminated Signage ("ROD CITY ARCHITECTURE", "SPEED LOOP PARKING", "NEON DRIFT ARCADE")
        if (b.rooftopSignText.isNotEmpty() && t1.isVisible && t2.isVisible) {
            val signPt = cam.project(b.x, b.y, h + 8f)
            if (signPt.isVisible) {
                val fontSize = (22f * cam.focalLength / signPt.depth).coerceIn(9f, 24f)
                val signStyle = TextStyle(
                    color = Color(b.signColor),
                    fontSize = fontSize.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
                val layoutResult = textMeasurer.measure(b.rooftopSignText, signStyle)

                // Signboard backing
                val padX = 10f
                val padY = 5f
                val signTopLeft = Offset(signPt.x - layoutResult.size.width * 0.5f, signPt.y - layoutResult.size.height * 0.5f)

                drawRect(
                    color = Color(0xCC0F172A),
                    topLeft = Offset(signTopLeft.x - padX, signTopLeft.y - padY),
                    size = Size(layoutResult.size.width + padX * 2, layoutResult.size.height + padY * 2)
                )
                drawRect(
                    color = Color(b.signColor),
                    topLeft = Offset(signTopLeft.x - padX, signTopLeft.y - padY),
                    size = Size(layoutResult.size.width + padX * 2, layoutResult.size.height + padY * 2),
                    style = Stroke(width = 2f)
                )

                drawText(textLayoutResult = layoutResult, topLeft = signTopLeft)

                // Mid-level signage (e.g. "ROD CITY" on mid-facade)
                if (b.midSignText.isNotEmpty()) {
                    val midPt = cam.project(b.x, b.y, h * 0.45f)
                    if (midPt.isVisible) {
                        val midStyle = TextStyle(
                            color = Color(b.signColor).copy(alpha = 0.9f),
                            fontSize = (fontSize * 0.85f).sp,
                            fontWeight = FontWeight.Bold
                        )
                        val midLayout = textMeasurer.measure(b.midSignText, midStyle)
                        drawText(
                            textLayoutResult = midLayout,
                            topLeft = Offset(midPt.x - midLayout.size.width * 0.5f, midPt.y)
                        )
                    }
                }
            }
        }
    }

    // =========================================================================
    // 3. 3D ELEVATED HIGHWAY ROAD & LANE MARKINGS (Continuous Spline Ribbons)
    // =========================================================================
    private fun DrawScope.draw3DElevatedHighwayRoad(track: Track, cam: Game3DCamera) {
        val slices = track.roadSlices
        val numSlices = slices.size
        if (numSlices < 2) return

        val maxDistSq = 1600f * 1600f
        val roadColor = Color(track.roadColor)
        val asphaltDark = Color(0xFF1E242F)
        val curbRed = Color(track.curbColor)

        // Draw in forward order along the track ribbon
        for (i in 0 until numSlices) {
            val nextIdx = (i + 1) % numSlices
            val s1 = slices[i]
            val s2 = slices[nextIdx]

            val dx = s1.cx - cam.posX
            val dy = s1.cy - cam.posY
            if (dx * dx + dy * dy > maxDistSq) continue

            // 1. Draw Asphalt Surface Quad
            val quadDrawn = cam.projectQuadClipped(
                p0x = s1.ox, p0y = s1.oy, p0z = 0f,
                p1x = s2.ox, p1y = s2.oy, p1z = 0f,
                p2x = s2.ix, p2y = s2.iy, p2z = 0f,
                p3x = s1.ix, p3y = s1.iy, p3z = 0f,
                outPath = reusableRoadPath
            )

            if (quadDrawn) {
                // Alternating subtle asphalt shade for texture feel
                val segColor = if (i % 2 == 0) roadColor else asphaltDark
                drawPath(path = reusableRoadPath, color = segColor)

                // Outer Curb (Alternating Red & White Rumble Strip at apexes)
                if (s1.isApexCurb) {
                    val curbColorVal = if ((i / 2) % 2 == 0) curbRed else Color.White
                    val curbDrawn = cam.projectQuadClipped(
                        p0x = s1.obx, p0y = s1.oby, p0z = 0.5f,
                        p1x = s2.obx, p1y = s2.oby, p1z = 0.5f,
                        p2x = s2.ox, p2y = s2.oy, p2z = 0.5f,
                        p3x = s1.ox, p3y = s1.oy, p3z = 0.5f,
                        outPath = reusableRoadPath
                    )
                    if (curbDrawn) {
                        drawPath(path = reusableRoadPath, color = curbColorVal)
                    }
                }

                // 2. Solid White Boundary Edge Lines
                val outerEdge = cam.projectLineClipped(s1.ox, s1.oy, 0.4f, s2.ox, s2.oy, 0.4f)
                outerEdge?.let { (p1, p2) ->
                    val dist = kotlin.math.hypot(s1.cx - cam.posX, s1.cy - cam.posY).coerceAtLeast(10f)
                    val strokeW = (280f / dist).coerceIn(2.5f, 7f)
                    drawLine(
                        color = Color.White.copy(alpha = 0.95f),
                        start = p1,
                        end = p2,
                        strokeWidth = strokeW
                    )
                }

                val innerEdge = cam.projectLineClipped(s1.ix, s1.iy, 0.4f, s2.ix, s2.iy, 0.4f)
                innerEdge?.let { (p1, p2) ->
                    val dist = kotlin.math.hypot(s1.cx - cam.posX, s1.cy - cam.posY).coerceAtLeast(10f)
                    val strokeW = (280f / dist).coerceIn(2.5f, 7f)
                    drawLine(
                        color = Color.White.copy(alpha = 0.95f),
                        start = p1,
                        end = p2,
                        strokeWidth = strokeW
                    )
                }

                // 3. Dashed White Center Lane Line
                if (s1.isDashed) {
                    val centerLineSeg = cam.projectLineClipped(s1.cx, s1.cy, 0.5f, s2.cx, s2.cy, 0.5f)
                    centerLineSeg?.let { (p1, p2) ->
                        val dist = kotlin.math.hypot(s1.cx - cam.posX, s1.cy - cam.posY).coerceAtLeast(10f)
                        val strokeW = (320f / dist).coerceIn(3f, 8f)
                        drawLine(
                            color = Color.White.copy(alpha = 0.95f),
                            start = p1,
                            end = p2,
                            strokeWidth = strokeW,
                            cap = StrokeCap.Square
                        )
                    }
                }
            }
        }
    }

    // =========================================================================
    // 4. 3D CONCRETE PARAPET BARRIERS (Jersey Barriers & Warning Reflectors)
    // =========================================================================
    private fun DrawScope.draw3DConcreteParapetBarriers(track: Track, cam: Game3DCamera) {
        val barrierH = 10f // height in 3D world units
        val slices = track.roadSlices
        val n = slices.size
        if (n < 2) return

        val maxDistSq = 1400f * 1400f
        val wallColor = Color(0xFFCBD5E1)
        val capColor = Color(0xFFE2E8F0)
        val reflectorColor = Color(0xFFFF9100)

        // Outer Barriers
        for (i in 0 until n) {
            val nextIdx = (i + 1) % n
            val s1 = slices[i]
            val s2 = slices[nextIdx]

            val dx = s1.cx - cam.posX
            val dy = s1.cy - cam.posY
            if (dx * dx + dy * dy > maxDistSq) continue

            // Outer Barrier Wall
            val outerWallDrawn = cam.projectQuadClipped(
                p0x = s1.obx, p0y = s1.oby, p0z = 0f,
                p1x = s2.obx, p1y = s2.oby, p1z = 0f,
                p2x = s2.obx, p2y = s2.oby, p2z = barrierH,
                p3x = s1.obx, p3y = s1.oby, p3z = barrierH,
                outPath = reusableBarrierPath
            )
            if (outerWallDrawn) {
                drawPath(path = reusableBarrierPath, color = wallColor)

                // Top rim
                val topEdge = cam.projectLineClipped(s1.obx, s1.oby, barrierH, s2.obx, s2.oby, barrierH)
                topEdge?.let { (p1, p2) ->
                    val dist = kotlin.math.hypot(s1.cx - cam.posX, s1.cy - cam.posY).coerceAtLeast(10f)
                    drawLine(
                        color = capColor,
                        start = p1,
                        end = p2,
                        strokeWidth = (280f / dist).coerceIn(2.5f, 7f)
                    )
                }

                // Amber warning reflector every 3 slices
                if (i % 3 == 0) {
                    val refPt = cam.project(s1.obx, s1.oby, barrierH * 0.5f)
                    if (refPt.isVisible) {
                        val refR = (130f / refPt.depth).coerceIn(2f, 5.5f)
                        drawCircle(color = reflectorColor, radius = refR, center = Offset(refPt.x, refPt.y))
                    }
                }
            }

            // Inner Barrier Wall
            val innerWallDrawn = cam.projectQuadClipped(
                p0x = s1.ibx, p0y = s1.iby, p0z = 0f,
                p1x = s2.ibx, p1y = s2.iby, p1z = 0f,
                p2x = s2.ibx, p2y = s2.iby, p2z = barrierH,
                p3x = s1.ibx, p3y = s1.iby, p3z = barrierH,
                outPath = reusableBarrierPath
            )
            if (innerWallDrawn) {
                drawPath(path = reusableBarrierPath, color = wallColor.copy(alpha = 0.95f))

                val topEdge = cam.projectLineClipped(s1.ibx, s1.iby, barrierH, s2.ibx, s2.iby, barrierH)
                topEdge?.let { (p1, p2) ->
                    val dist = kotlin.math.hypot(s1.cx - cam.posX, s1.cy - cam.posY).coerceAtLeast(10f)
                    drawLine(
                        color = capColor,
                        start = p1,
                        end = p2,
                        strokeWidth = (280f / dist).coerceIn(2.5f, 7f)
                    )
                }
            }
        }
    }

    // =========================================================================
    // 5. 3D STREET LAMPS ALONG HIGHWAY
    // =========================================================================
    private fun DrawScope.draw3DStreetLamps(track: Track, cam: Game3DCamera) {
        for (lamp in track.streetLamps) {
            val basePt = cam.project(lamp.x, lamp.y, 0f)
            val topPt = cam.project(lamp.x, lamp.y, lamp.height)

            val armAngleRad = lamp.armAngleDeg * (PI.toFloat() / 180f)
            val armLength = 16f
            val armEndPt = cam.project(
                lamp.x + cos(armAngleRad) * armLength,
                lamp.y + sin(armAngleRad) * armLength,
                lamp.height + 2f
            )

            if (basePt.isVisible || topPt.isVisible) {
                val poleWidth = (180f / (basePt.depth.coerceAtLeast(10f))).coerceIn(2f, 6f)

                // Vertical pole
                if (basePt.isVisible && topPt.isVisible) {
                    drawLine(
                        color = Color(0xFF64748B),
                        start = Offset(basePt.x, basePt.y),
                        end = Offset(topPt.x, topPt.y),
                        strokeWidth = poleWidth
                    )
                }

                // Cantilever arched arm & luminaire
                if (topPt.isVisible && armEndPt.isVisible) {
                    drawLine(
                        color = Color(0xFF64748B),
                        start = Offset(topPt.x, topPt.y),
                        end = Offset(armEndPt.x, armEndPt.y),
                        strokeWidth = poleWidth * 0.85f
                    )

                    // Luminous cobra-head LED fixture
                    drawCircle(
                        color = Color(lamp.lightColor),
                        radius = (poleWidth * 1.5f).coerceAtLeast(3f),
                        center = Offset(armEndPt.x, armEndPt.y)
                    )

                    // Warm light illumination pool on ground
                    val groundLightPt = cam.project(
                        lamp.x + cos(armAngleRad) * armLength,
                        lamp.y + sin(armAngleRad) * armLength,
                        0.5f
                    )
                    if (groundLightPt.isVisible) {
                        val poolRadius = (160f * cam.focalLength / groundLightPt.depth).coerceIn(12f, 65f)
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(Color(0x35FFF9C4), Color(0x10FFF9C4), Color.Transparent),
                                center = Offset(groundLightPt.x, groundLightPt.y),
                                radius = poolRadius
                            ),
                            radius = poolRadius,
                            center = Offset(groundLightPt.x, groundLightPt.y)
                        )
                    }
                }
            }
        }
    }

    // =========================================================================
    // 6. 3D SKIDMARKS (Rubber Tracks)
    // =========================================================================
    private fun DrawScope.draw3DSkidmarks(physics: DriftPhysicsEngine, cam: Game3DCamera) {
        val total = physics.skidSegments.size
        val startIdx = if (total > 85) total - 85 else 0

        for (idx in startIdx until total) {
            val seg = physics.skidSegments[idx]
            val p1 = cam.project(seg.x1, seg.y1, 0.4f)
            val p2 = cam.project(seg.x2, seg.y2, 0.4f)
            if (p1.isVisible && p2.isVisible) {
                val strokeW = (360f / p1.depth).coerceIn(2.5f, 10f)
                drawLine(
                    color = Color(0xFF0F172A).copy(alpha = seg.alpha * 0.75f),
                    start = Offset(p1.x, p1.y),
                    end = Offset(p2.x, p2.y),
                    strokeWidth = strokeW,
                    cap = StrokeCap.Round
                )
            }
        }
    }

    // =========================================================================
    // 7. 3D DRIFT CLIPPING POINT BEACONS
    // =========================================================================
    private fun DrawScope.draw3DApexBeacons(track: Track, cam: Game3DCamera) {
        val pulse = (sin(System.currentTimeMillis() / 180.0).toFloat() + 1f) * 0.5f

        for (clip in track.clippingPoints) {
            val groundPt = cam.project(clip.x, clip.y, 0f)
            val topPt = cam.project(clip.x, clip.y, 65f)

            if (groundPt.isVisible) {
                val baseRadius = (clip.radius * cam.focalLength / groundPt.depth).coerceIn(12f, 130f)

                drawCircle(
                    color = NeonCyan.copy(alpha = 0.15f + pulse * 0.2f),
                    radius = baseRadius,
                    center = Offset(groundPt.x, groundPt.y),
                    style = Fill
                )
                drawCircle(
                    color = NeonCyan,
                    radius = baseRadius,
                    center = Offset(groundPt.x, groundPt.y),
                    style = Stroke(width = (160f / groundPt.depth).coerceIn(2f, 4.5f))
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
                        strokeWidth = (220f / groundPt.depth).coerceIn(3f, 8f)
                    )
                }
            }
        }
    }

    // =========================================================================
    // 8. 3D VOLUMETRIC TIRE SMOKE
    // =========================================================================
    private fun DrawScope.draw3DVolumetricSmoke(physics: DriftPhysicsEngine, cam: Game3DCamera) {
        val total = physics.smokeParticles.size
        val start = if (total > 60) total - 60 else 0

        for (i in start until total) {
            val p = physics.smokeParticles[i]
            val sp = cam.project(p.x, p.y, p.z)
            if (sp.isVisible) {
                val screenRadius = (p.radius * cam.focalLength / sp.depth).coerceIn(6f, 85f)
                val alpha = (p.alpha * 0.55f).coerceIn(0f, 1f)

                // Soft outer smoke glow
                drawCircle(
                    color = Color(p.color).copy(alpha = alpha * 0.35f),
                    radius = screenRadius * 1.35f,
                    center = Offset(sp.x, sp.y)
                )
                // Dense smoke core
                drawCircle(
                    color = Color.White.copy(alpha = alpha * 0.65f),
                    radius = screenRadius,
                    center = Offset(sp.x, sp.y)
                )
            }
        }
    }

    // =========================================================================
    // 9. 3D FLAGSHIP SUPERCAR MODEL (AEROBLADE ROD #42 Matching Reference Image)
    // =========================================================================
    private fun DrawScope.draw3DSupercarModel(
        physics: DriftPhysicsEngine,
        cam: Game3DCamera,
        textMeasurer: TextMeasurer
    ) {
        // In Hood or Bumper camera mode, the camera is placed at the front/hood so skip drawing interior/rear body
        if (cam.mode == com.example.game.model.CameraMode.HOOD || cam.mode == com.example.game.model.CameraMode.BUMPER) {
            return
        }

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

        // 1. Neon Underglow on Road Surface
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
            drawPath(path = reusableCarPath, color = Color(car.underglowColor).copy(alpha = 0.5f))
        }

        // 2. Realistic Dual Headlight Cones illuminating road ahead
        val hlLeft = projectCarPoint(length * 0.5f, -width * 0.35f, 4f)
        val hlRight = projectCarPoint(length * 0.5f, width * 0.35f, 4f)
        val hlBeamFwd = length * 0.5f + 180f
        val hlLeftEnd = projectCarPoint(hlBeamFwd, -width * 1.6f, 0.5f)
        val hlRightEnd = projectCarPoint(hlBeamFwd, width * 1.6f, 0.5f)

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
                    colors = listOf(Color(0x5500F5FF), Color(0x1500F5FF), Color.Transparent),
                    startY = minOf(hlLeft.y, hlRight.y),
                    endY = maxOf(hlLeftEnd.y, hlRightEnd.y)
                )
            )
        }

        // 3. 3D Wheels with Glowing Cyan Outer Rims (Matching Reference Image)
        val wheelRadius = 6.4f
        val wheelWidth = 3.8f
        val rearFwd = -length * 0.35f
        val frontFwd = length * 0.35f
        val sideOut = width * 0.52f

        drawRealisticWheel(rearFwd, -sideOut, 0f, wheelRadius, wheelWidth, car.underglowColor, ::projectCarPoint)
        drawRealisticWheel(rearFwd, sideOut, 0f, wheelRadius, wheelWidth, car.underglowColor, ::projectCarPoint)
        drawRealisticWheel(frontFwd, -sideOut, physics.steerAngle, wheelRadius, wheelWidth, car.underglowColor, ::projectCarPoint)
        drawRealisticWheel(frontFwd, sideOut, physics.steerAngle, wheelRadius, wheelWidth, car.underglowColor, ::projectCarPoint)

        // 4. Aerodynamic Chassis Key Points
        val primaryColor = Color(car.bodyColor) // Metallic Sunset Orange
        val secondaryColor = Color(car.accentColor) // Electric Cyan

        // Lower body sill
        val fL_bot = projectCarPoint(length * 0.5f, -width * 0.46f, 3f)
        val fR_bot = projectCarPoint(length * 0.5f, width * 0.46f, 3f)
        val rR_bot = projectCarPoint(-length * 0.48f, width * 0.46f, 3.5f)
        val rL_bot = projectCarPoint(-length * 0.48f, -width * 0.46f, 3.5f)

        // Beltline / Waist
        val fL_mid = projectCarPoint(length * 0.48f, -width * 0.46f, 8.5f)
        val fR_mid = projectCarPoint(length * 0.48f, width * 0.46f, 8.5f)
        val rR_mid = projectCarPoint(-length * 0.46f, width * 0.46f, 9f)
        val rL_mid = projectCarPoint(-length * 0.46f, -width * 0.46f, 9f)

        // Cabin Roof & Windshield
        val cabFL = projectCarPoint(length * 0.12f, -width * 0.36f, height)
        val cabFR = projectCarPoint(length * 0.12f, width * 0.36f, height)
        val cabRR = projectCarPoint(-length * 0.26f, width * 0.36f, height)
        val cabRL = projectCarPoint(-length * 0.26f, -width * 0.36f, height)

        // A. REAR DIFFUSER & BUMPER FACE
        if (rL_bot.isVisible && rR_bot.isVisible && rR_mid.isVisible && rL_mid.isVisible) {
            reusableCarPath.reset()
            reusableCarPath.moveTo(rL_bot.x, rL_bot.y)
            reusableCarPath.lineTo(rR_bot.x, rR_bot.y)
            reusableCarPath.lineTo(rR_mid.x, rR_mid.y)
            reusableCarPath.lineTo(rL_mid.x, rL_mid.y)
            reusableCarPath.close()

            // Lower bumper in electric cyan
            drawPath(path = reusableCarPath, color = secondaryColor)

            // Carbon diffuser aero strakes
            val diffMidX = (rL_bot.x + rR_bot.x) * 0.5f
            val diffMidY = (rL_bot.y + rR_bot.y) * 0.5f
            drawRect(
                color = Color(0xFF0F172A),
                topLeft = Offset(diffMidX - 14f, diffMidY - 6f),
                size = Size(28f, 7f)
            )

            // F1 Center Pulsing Rain/Brake Light
            val rainLightPulse = if (physics.speed > 50f) 1f else 0.5f
            drawCircle(
                color = Color(0xFFFF1744).copy(alpha = rainLightPulse),
                radius = 3.5f,
                center = Offset(diffMidX, diffMidY - 3f)
            )

            // Curved Red LED Tail Light Bar (Wrapping across entire rear)
            val tailLeft = Offset(rL_mid.x * 0.85f + rR_mid.x * 0.15f, rL_mid.y * 0.85f + rR_mid.y * 0.15f)
            val tailRight = Offset(rL_mid.x * 0.15f + rR_mid.x * 0.85f, rL_mid.y * 0.15f + rR_mid.y * 0.85f)
            drawLine(color = Color(0xFFFF1744), start = tailLeft, end = tailRight, strokeWidth = 4.5f, cap = StrokeCap.Round)
            drawLine(color = Color.White, start = tailLeft, end = tailRight, strokeWidth = 1.5f, cap = StrokeCap.Round)

            // Rear Center "AEROBLADE" Badge
            val rearCenter = Offset((rL_mid.x + rR_mid.x) * 0.5f, (rL_mid.y + rR_mid.y) * 0.5f - 3f)
            val badgeStyle = TextStyle(color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Black)
            val badgeLayout = textMeasurer.measure("AEROBLADE", badgeStyle)
            drawText(
                textLayoutResult = badgeLayout,
                topLeft = Offset(rearCenter.x - badgeLayout.size.width * 0.5f, rearCenter.y - badgeLayout.size.height * 0.5f)
            )
        }

        // B. LEFT SIDE FLANK (Dual-tone: Cyan lower skirt + Orange upper)
        if (fL_bot.isVisible && rL_bot.isVisible && rL_mid.isVisible && fL_mid.isVisible) {
            reusableCarPath.reset()
            reusableCarPath.moveTo(fL_bot.x, fL_bot.y)
            reusableCarPath.lineTo(rL_bot.x, rL_bot.y)
            reusableCarPath.lineTo(rL_mid.x, rL_mid.y)
            reusableCarPath.lineTo(fL_mid.x, fL_mid.y)
            reusableCarPath.close()

            drawPath(path = reusableCarPath, color = secondaryColor) // Cyan side skirt

            // #42 Square racing number door badge
            val doorX = (fL_mid.x + rL_mid.x) * 0.5f
            val doorY = (fL_mid.y + rL_mid.y) * 0.5f
            drawRect(
                color = Color.White,
                topLeft = Offset(doorX - 6f, doorY - 4f),
                size = Size(12f, 8f)
            )
            val numStyle = TextStyle(color = Color.Black, fontSize = 6.sp, fontWeight = FontWeight.Black)
            val numLayout = textMeasurer.measure("42", numStyle)
            drawText(textLayoutResult = numLayout, topLeft = Offset(doorX - 4f, doorY - 4f))
        }

        // C. RIGHT SIDE FLANK (Dual-tone)
        if (fR_bot.isVisible && rR_bot.isVisible && rR_mid.isVisible && fR_mid.isVisible) {
            reusableCarPath.reset()
            reusableCarPath.moveTo(fR_bot.x, fR_bot.y)
            reusableCarPath.lineTo(rR_bot.x, rR_bot.y)
            reusableCarPath.lineTo(rR_mid.x, rR_mid.y)
            reusableCarPath.lineTo(fR_mid.x, fR_mid.y)
            reusableCarPath.close()

            drawPath(path = reusableCarPath, color = secondaryColor)
        }

        // D. HOOD TOP DECK (Metallic Sunset Orange)
        if (fL_mid.isVisible && fR_mid.isVisible && cabFR.isVisible && cabFL.isVisible) {
            reusableCarPath.reset()
            reusableCarPath.moveTo(fL_mid.x, fL_mid.y)
            reusableCarPath.lineTo(fR_mid.x, fR_mid.y)
            reusableCarPath.lineTo(cabFR.x, cabFR.y)
            reusableCarPath.lineTo(cabFL.x, cabFL.y)
            reusableCarPath.close()

            drawPath(path = reusableCarPath, color = primaryColor)

            // Dual racing center stripes
            val hStart = Offset((fL_mid.x + fR_mid.x) * 0.5f, (fL_mid.y + fR_mid.y) * 0.5f)
            val hEnd = Offset((cabFL.x + cabFR.x) * 0.5f, (cabFL.y + cabFR.y) * 0.5f)
            drawLine(color = secondaryColor, start = hStart, end = hEnd, strokeWidth = 6f)
        }

        // E. REAR TRUNK DECK (Sunset Orange)
        if (cabRL.isVisible && cabRR.isVisible && rR_mid.isVisible && rL_mid.isVisible) {
            reusableCarPath.reset()
            reusableCarPath.moveTo(cabRL.x, cabRL.y)
            reusableCarPath.lineTo(cabRR.x, cabRR.y)
            reusableCarPath.lineTo(rR_mid.x, rR_mid.y)
            reusableCarPath.lineTo(rL_mid.x, rL_mid.y)
            reusableCarPath.close()

            drawPath(path = reusableCarPath, color = primaryColor)

            // "ROD" & "#42" Decal on trunk lid
            val tCenter = Offset((cabRL.x + cabRR.x + rR_mid.x + rL_mid.x) * 0.25f, (cabRL.y + cabRR.y + rR_mid.y + rL_mid.y) * 0.25f)
            val rodStyle = TextStyle(color = Color.White, fontSize = 7.sp, fontWeight = FontWeight.Black)
            val rodLayout = textMeasurer.measure("ROD #42", rodStyle)
            drawText(textLayoutResult = rodLayout, topLeft = Offset(tCenter.x - rodLayout.size.width * 0.5f, tCenter.y - rodLayout.size.height * 0.5f))
        }

        // F. CABIN ROOF & WINDSHIELD
        if (cabFL.isVisible && cabFR.isVisible && cabRR.isVisible && cabRL.isVisible) {
            reusableCarPath.reset()
            reusableCarPath.moveTo(cabFL.x, cabFL.y)
            reusableCarPath.lineTo(cabFR.x, cabFR.y)
            reusableCarPath.lineTo(cabRR.x, cabRR.y)
            reusableCarPath.lineTo(cabRL.x, cabRL.y)
            reusableCarPath.close()

            // Dark tinted glass + sunset orange roof center
            drawPath(path = reusableCarPath, color = Color(0xFF0F172A))
            drawLine(
                color = primaryColor,
                start = Offset((cabFL.x + cabFR.x) * 0.5f, (cabFL.y + cabFR.y) * 0.5f),
                end = Offset((cabRL.x + cabRR.x) * 0.5f, (cabRL.y + cabRR.y) * 0.5f),
                strokeWidth = 8f
            )
        }

        // 5. CARBON-FIBER GT REAR WING SPOILER (Matching Reference Image)
        val wingH = height + 4.5f
        val wingL = projectCarPoint(-length * 0.52f, -width * 0.52f, wingH)
        val wingR = projectCarPoint(-length * 0.52f, width * 0.52f, wingH)
        val strutL_bot = projectCarPoint(-length * 0.44f, -width * 0.24f, 9f)
        val strutR_bot = projectCarPoint(-length * 0.44f, width * 0.24f, 9f)
        val strutL_top = projectCarPoint(-length * 0.44f, -width * 0.24f, wingH)
        val strutR_top = projectCarPoint(-length * 0.44f, width * 0.24f, wingH)

        if (wingL.isVisible && wingR.isVisible) {
            // Dual black vertical struts
            if (strutL_bot.isVisible && strutL_top.isVisible) {
                drawLine(color = Color(0xFF0F172A), start = Offset(strutL_bot.x, strutL_bot.y), end = Offset(strutL_top.x, strutL_top.y), strokeWidth = 3f)
            }
            if (strutR_bot.isVisible && strutR_top.isVisible) {
                drawLine(color = Color(0xFF0F172A), start = Offset(strutR_bot.x, strutR_bot.y), end = Offset(strutR_top.x, strutR_top.y), strokeWidth = 3f)
            }

            // High-downforce carbon wing blade with cyan tips
            drawLine(
                color = secondaryColor,
                start = Offset(wingL.x, wingL.y),
                end = Offset(wingR.x, wingR.y),
                strokeWidth = 5.5f,
                cap = StrokeCap.Round
            )
            drawLine(
                color = Color(0xFF1E293B),
                start = Offset(wingL.x * 0.85f + wingR.x * 0.15f, wingL.y * 0.85f + wingR.y * 0.15f),
                end = Offset(wingL.x * 0.15f + wingR.x * 0.85f, wingL.y * 0.15f + wingR.y * 0.85f),
                strokeWidth = 3f,
                cap = StrokeCap.Round
            )
        }

        // 6. Dual Nitro Exhaust Flames
        if (physics.isNitroActive) {
            val exL = projectCarPoint(-length * 0.5f, -width * 0.24f, 4f)
            val exR = projectCarPoint(-length * 0.5f, width * 0.24f, 4f)
            val flameFwd = -length * 0.5f - (36f + sin(System.currentTimeMillis() / 20.0).toFloat() * 12f)
            val flameEndL = projectCarPoint(flameFwd, -width * 0.24f, 4f)
            val flameEndR = projectCarPoint(flameFwd, width * 0.24f, 4f)

            if (exL.isVisible && flameEndL.isVisible) {
                drawLine(color = NitroBlue, start = Offset(exL.x, exL.y), end = Offset(flameEndL.x, flameEndL.y), strokeWidth = 7f, cap = StrokeCap.Round)
                drawLine(color = Color.White, start = Offset(exL.x, exL.y), end = Offset(flameEndL.x, flameEndL.y), strokeWidth = 2.5f, cap = StrokeCap.Round)
            }
            if (exR.isVisible && flameEndR.isVisible) {
                drawLine(color = NitroBlue, start = Offset(exR.x, exR.y), end = Offset(flameEndR.x, flameEndR.y), strokeWidth = 7f, cap = StrokeCap.Round)
                drawLine(color = Color.White, start = Offset(exR.x, exR.y), end = Offset(flameEndR.x, flameEndR.y), strokeWidth = 2.5f, cap = StrokeCap.Round)
            }
        }
    }

    private fun DrawScope.drawRealisticWheel(
        fwd: Float,
        side: Float,
        steerAngle: Float,
        radius: Float,
        width: Float,
        rimColorHex: Long,
        projectFn: (Float, Float, Float) -> ScreenPoint
    ) {
        val cosS = cos(steerAngle)
        val sinS = sin(steerAngle)

        val w1 = projectFn(fwd - radius * cosS, side, radius)
        val w2 = projectFn(fwd + radius * cosS, side, radius)

        if (w1.isVisible && w2.isVisible) {
            val tireWidth = (width * 320f / w1.depth).coerceIn(4f, 15f)

            // Black rubber tire
            drawLine(
                color = Color(0xFF0F172A),
                start = Offset(w1.x, w1.y),
                end = Offset(w2.x, w2.y),
                strokeWidth = tireWidth,
                cap = StrokeCap.Round
            )

            val midX = (w1.x + w2.x) * 0.5f
            val midY = (w1.y + w2.y) * 0.5f
            val rimRadius = (tireWidth * 0.42f).coerceAtLeast(2f)

            // Alloy hub
            drawCircle(color = Color(0xFF334155), radius = rimRadius, center = Offset(midX, midY))
            // Glowing cyan rim lip (from reference image)
            drawCircle(color = Color(rimColorHex), radius = rimRadius, center = Offset(midX, midY), style = Stroke(width = 1.5f))
        }
    }

    // =========================================================================
    // 10. 3D WALL SCRAPE SPARKS
    // =========================================================================
    private fun DrawScope.draw3DSparks(physics: DriftPhysicsEngine, cam: Game3DCamera) {
        for (s in physics.sparkParticles) {
            val pt = cam.project(s.x, s.y, s.z)
            if (pt.isVisible) {
                val sparkRadius = (60f / pt.depth).coerceIn(2f, 5f)
                drawCircle(color = Color(s.color), radius = sparkRadius, center = Offset(pt.x, pt.y))
            }
        }
    }

    // =========================================================================
    // 11. 3D FLOATING SCORE POPUPS
    // =========================================================================
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
