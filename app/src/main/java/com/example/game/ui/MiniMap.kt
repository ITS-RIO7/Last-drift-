package com.example.game.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.game.model.Track
import com.example.game.physics.DriftPhysicsEngine
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonOrange
import com.example.ui.theme.NeonYellow
import com.example.ui.theme.RacingCard
import kotlin.math.PI

@Composable
fun MiniMap(
    physics: DriftPhysicsEngine,
    track: Track,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(105.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(RacingCard.copy(alpha = 0.85f))
            .border(1.5.dp, Color(0x33FFFFFF), RoundedCornerShape(14.dp))
            .testTag("game_minimap")
    ) {
        val trackPath = remember { Path() }
        val arrowPath = remember { Path() }

        Canvas(modifier = Modifier.matchParentSize()) {
            val scaleX = size.width / track.worldWidth
            val scaleY = size.height / track.worldHeight

            // Draw track path
            if (track.centerLine.isNotEmpty()) {
                trackPath.reset()
                val first = track.centerLine[0]
                trackPath.moveTo(first.x * scaleX, first.y * scaleY)
                for (i in 1 until track.centerLine.size) {
                    val pt = track.centerLine[i]
                    trackPath.lineTo(pt.x * scaleX, pt.y * scaleY)
                }
                drawPath(
                    path = trackPath,
                    color = Color(0x66FFFFFF),
                    style = Stroke(width = 4f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                )
            }

            // Draw clipping points
            for (clip in track.clippingPoints) {
                drawCircle(
                    color = NeonCyan,
                    radius = 3.5f,
                    center = Offset(clip.x * scaleX, clip.y * scaleY)
                )
            }

            // Draw Player Car as a small arrow
            val playerMapX = physics.posX * scaleX
            val playerMapY = physics.posY * scaleY
            val headingDeg = physics.heading * (180f / PI.toFloat())

            rotate(headingDeg, Offset(playerMapX, playerMapY)) {
                arrowPath.reset()
                arrowPath.moveTo(playerMapX + 6f, playerMapY)
                arrowPath.lineTo(playerMapX - 4f, playerMapY - 4f)
                arrowPath.lineTo(playerMapX - 2f, playerMapY)
                arrowPath.lineTo(playerMapX - 4f, playerMapY + 4f)
                arrowPath.close()

                drawPath(path = arrowPath, color = NeonOrange, style = Fill)
                drawCircle(
                    color = NeonYellow,
                    radius = 2.5f,
                    center = Offset(playerMapX, playerMapY)
                )
            }
        }
    }
}
