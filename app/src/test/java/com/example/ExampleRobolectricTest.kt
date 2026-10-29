package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.game.model.Car
import com.example.game.model.Track
import com.example.game.physics.DriftPhysicsEngine
import com.example.game.scoring.DriftScoringSystem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Drift Horizon", appName)
    }

    @Test
    fun `test drift physics initialization and car update`() {
        val car = Car.DEFAULT_CARS[0]
        val track = Track.createNeonCityTrack()
        val physics = DriftPhysicsEngine(car = car, track = track)

        assertEquals(track.startX, physics.posX)
        assertEquals(track.startY, physics.posY)

        // Simulate 0.5s of full throttle and full steer
        for (i in 0 until 30) {
            physics.update(
                dt = 0.016f,
                steerInput = 1.0f,
                throttleInput = 1.0f,
                brakeInput = 0f,
                handbrakeInput = false,
                nitroInput = false
            )
        }

        assertTrue("Car should gain speed under throttle", physics.speed > 0f)
    }

    @Test
    fun `test scoring system reset and banking`() {
        val scoring = DriftScoringSystem()
        assertEquals(0, scoring.totalScore)
        assertEquals(1.0f, scoring.multiplier)

        scoring.currentComboScore = 500f
        scoring.multiplier = 2.0f
        scoring.finalizeScore()

        assertEquals(1000, scoring.totalScore)
        assertEquals(0f, scoring.currentComboScore)
    }
}
