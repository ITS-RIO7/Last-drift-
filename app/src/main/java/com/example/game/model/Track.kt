package com.example.game.model

data class TrackPoint(val x: Float, val y: Float)

data class ClippingPoint(
    val id: String,
    val x: Float,
    val y: Float,
    val radius: Float = 110f,
    val name: String = "Apex"
)

data class TrackObstacle(
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float,
    val rotationDeg: Float = 0f,
    val label: String = ""
)

data class Track(
    val id: String,
    val name: String,
    val location: String,
    val difficulty: String,
    val description: String,
    val roadWidth: Float,
    val startX: Float,
    val startY: Float,
    val startAngleDeg: Float,
    val centerLine: List<TrackPoint>,
    val innerBoundary: List<TrackPoint>,
    val outerBoundary: List<TrackPoint>,
    val checkpoints: List<TrackPoint>,
    val clippingPoints: List<ClippingPoint>,
    val obstacles: List<TrackObstacle> = emptyList(),
    val worldWidth: Float = 2400f,
    val worldHeight: Float = 2400f,
    val ambientColor: Long = 0xFF0D0F14,
    val roadColor: Long = 0xFF1C202A,
    val curbColor: Long = 0xFFFF1744,
    val barrierGlowColor: Long = 0xFF00E5FF
) {
    companion object {
        // Track 1: Neon City Circuit
        fun createNeonCityTrack(): Track {
            val center = listOf(
                TrackPoint(400f, 400f),
                TrackPoint(1200f, 400f),
                TrackPoint(1800f, 600f),
                TrackPoint(2000f, 1200f),
                TrackPoint(1800f, 1800f),
                TrackPoint(1200f, 2000f),
                TrackPoint(800f, 1600f),
                TrackPoint(800f, 1100f),
                TrackPoint(400f, 1100f),
                TrackPoint(400f, 400f)
            )
            // Boundaries offset by 100 on inside and outside
            val inner = listOf(
                TrackPoint(500f, 500f),
                TrackPoint(1150f, 500f),
                TrackPoint(1680f, 680f),
                TrackPoint(1880f, 1200f),
                TrackPoint(1700f, 1700f),
                TrackPoint(1200f, 1880f),
                TrackPoint(920f, 1550f),
                TrackPoint(920f, 1000f),
                TrackPoint(500f, 1000f),
                TrackPoint(500f, 500f)
            )
            val outer = listOf(
                TrackPoint(300f, 300f),
                TrackPoint(1250f, 300f),
                TrackPoint(1920f, 520f),
                TrackPoint(2120f, 1200f),
                TrackPoint(1900f, 1900f),
                TrackPoint(1200f, 2120f),
                TrackPoint(680f, 1650f),
                TrackPoint(680f, 1200f),
                TrackPoint(300f, 1200f),
                TrackPoint(300f, 300f)
            )
            val clips = listOf(
                ClippingPoint("clip_1", 1200f, 400f, 110f, "Entry Hairpin"),
                ClippingPoint("clip_2", 1800f, 600f, 115f, "Sweeper Apex"),
                ClippingPoint("clip_3", 2000f, 1200f, 120f, "Wall Tap Zone"),
                ClippingPoint("clip_4", 1200f, 2000f, 110f, "Transition Chicane"),
                ClippingPoint("clip_5", 800f, 1600f, 105f, "Inner Pin"),
                ClippingPoint("clip_6", 400f, 1100f, 115f, "Final Turn")
            )
            return Track(
                id = "track_neon_city",
                name = "Neon Metropolis",
                location = "Neo-Tokyo, Underground",
                difficulty = "Balanced",
                description = "Neon-drenched city circuit with wide sweeping corners, tight street hairpins, and wall-tap barriers.",
                roadWidth = 140f,
                startX = 450f,
                startY = 400f,
                startAngleDeg = 0f,
                centerLine = center,
                innerBoundary = inner,
                outerBoundary = outer,
                checkpoints = center,
                clippingPoints = clips,
                worldWidth = 2400f,
                worldHeight = 2400f,
                roadColor = 0xFF181C26,
                curbColor = 0xFFFF1744,
                barrierGlowColor = 0xFF00E5FF
            )
        }

        // Track 2: Mount Touge Pass (Downhill Mountain Hairpins)
        fun createTougeTrack(): Track {
            val center = listOf(
                TrackPoint(350f, 350f),
                TrackPoint(1800f, 350f),
                TrackPoint(2050f, 650f),
                TrackPoint(1600f, 850f),
                TrackPoint(500f, 850f),
                TrackPoint(350f, 1150f),
                TrackPoint(1900f, 1150f),
                TrackPoint(2050f, 1450f),
                TrackPoint(1600f, 1750f),
                TrackPoint(400f, 1750f),
                TrackPoint(350f, 2050f),
                TrackPoint(2050f, 2050f),
                TrackPoint(2150f, 1200f),
                TrackPoint(2150f, 350f),
                TrackPoint(350f, 350f)
            )
            val inner = listOf(
                TrackPoint(450f, 450f),
                TrackPoint(1700f, 450f),
                TrackPoint(1900f, 650f),
                TrackPoint(1550f, 750f),
                TrackPoint(550f, 750f),
                TrackPoint(450f, 1050f),
                TrackPoint(1800f, 1050f),
                TrackPoint(1920f, 1450f),
                TrackPoint(1550f, 1650f),
                TrackPoint(500f, 1650f),
                TrackPoint(450f, 1950f),
                TrackPoint(1950f, 1950f),
                TrackPoint(2050f, 1200f),
                TrackPoint(2050f, 450f),
                TrackPoint(450f, 450f)
            )
            val outer = listOf(
                TrackPoint(250f, 250f),
                TrackPoint(1900f, 250f),
                TrackPoint(2200f, 650f),
                TrackPoint(1650f, 950f),
                TrackPoint(450f, 950f),
                TrackPoint(250f, 1250f),
                TrackPoint(2000f, 1250f),
                TrackPoint(2180f, 1450f),
                TrackPoint(1650f, 1850f),
                TrackPoint(300f, 1850f),
                TrackPoint(250f, 2150f),
                TrackPoint(2150f, 2150f),
                TrackPoint(2250f, 1200f),
                TrackPoint(2250f, 250f),
                TrackPoint(250f, 250f)
            )
            val clips = listOf(
                ClippingPoint("touge_1", 1800f, 350f, 110f, "Hairpin 1"),
                ClippingPoint("touge_2", 1600f, 850f, 110f, "Switchback A"),
                ClippingPoint("touge_3", 500f, 850f, 110f, "Hairpin 2"),
                ClippingPoint("touge_4", 1900f, 1150f, 110f, "Switchback B"),
                ClippingPoint("touge_5", 400f, 1750f, 110f, "Cliff Apex"),
                ClippingPoint("touge_6", 2050f, 2050f, 110f, "Final S-Bend")
            )
            return Track(
                id = "track_touge",
                name = "Mount Akina Touge",
                location = "Gunma Prefecture",
                difficulty = "Hard",
                description = "Infamous winding mountain downhill pass. Demands precise counter-steering and high-angle drift entries.",
                roadWidth = 130f,
                startX = 400f,
                startY = 350f,
                startAngleDeg = 0f,
                centerLine = center,
                innerBoundary = inner,
                outerBoundary = outer,
                checkpoints = center,
                clippingPoints = clips,
                worldWidth = 2500f,
                worldHeight = 2400f,
                roadColor = 0xFF141820,
                curbColor = 0xFFFF6D00,
                barrierGlowColor = 0xFFFFD600
            )
        }

        // Track 3: Dockyard Gymkhana Playground
        fun createDockyardTrack(): Track {
            val center = listOf(
                TrackPoint(400f, 400f),
                TrackPoint(1800f, 400f),
                TrackPoint(1800f, 1800f),
                TrackPoint(400f, 1800f),
                TrackPoint(400f, 400f)
            )
            val inner = listOf(
                TrackPoint(700f, 700f),
                TrackPoint(1500f, 700f),
                TrackPoint(1500f, 1500f),
                TrackPoint(700f, 1500f),
                TrackPoint(700f, 700f)
            )
            val outer = listOf(
                TrackPoint(200f, 200f),
                TrackPoint(2000f, 200f),
                TrackPoint(2000f, 2000f),
                TrackPoint(200f, 2000f),
                TrackPoint(200f, 200f)
            )
            val obstacles = listOf(
                TrackObstacle(1100f, 1100f, 140f, 140f, 45f, "Center Donut Box"),
                TrackObstacle(600f, 1100f, 120f, 60f, 0f, "Container A"),
                TrackObstacle(1600f, 1100f, 120f, 60f, 0f, "Container B"),
                TrackObstacle(1100f, 600f, 60f, 120f, 0f, "Container C"),
                TrackObstacle(1100f, 1600f, 60f, 120f, 0f, "Container D")
            )
            val clips = listOf(
                ClippingPoint("dock_1", 1100f, 1100f, 130f, "Donut Target"),
                ClippingPoint("dock_2", 1800f, 400f, 120f, "North Pier Clip"),
                ClippingPoint("dock_3", 1800f, 1800f, 120f, "Crane Apex"),
                ClippingPoint("dock_4", 400f, 1800f, 120f, "South Pier Clip"),
                ClippingPoint("dock_5", 400f, 400f, 120f, "Warehouse Slalom")
            )
            return Track(
                id = "track_dockyard",
                name = "Dockyard Gymkhana",
                location = "Industrial Port Basin",
                difficulty = "Freestyle",
                description = "Spacious open industrial arena equipped with shipping containers, 360-degree donut pins, and high-speed slalom zones.",
                roadWidth = 260f,
                startX = 500f,
                startY = 400f,
                startAngleDeg = 0f,
                centerLine = center,
                innerBoundary = inner,
                outerBoundary = outer,
                checkpoints = center,
                clippingPoints = clips,
                obstacles = obstacles,
                worldWidth = 2200f,
                worldHeight = 2200f,
                roadColor = 0xFF191D24,
                curbColor = 0xFFFFD600,
                barrierGlowColor = 0xFFA855F7
            )
        }

        val ALL_TRACKS = listOf(
            createNeonCityTrack(),
            createTougeTrack(),
            createDockyardTrack()
        )
    }
}
