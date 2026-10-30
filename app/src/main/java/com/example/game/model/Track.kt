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

data class CityBuilding(
    val id: String,
    val x: Float,
    val y: Float,
    val width: Float,
    val length: Float,
    val height: Float,
    val rotationDeg: Float = 0f,
    val rooftopSignText: String = "",
    val midSignText: String = "",
    val signColor: Long = 0xFF00E5FF,
    val isParkingDeck: Boolean = false,
    val facadeColor: Long = 0xFF2D3748,
    val windowGlowColor: Long = 0xFFFFD54F
)

data class CityStreetLamp(
    val x: Float,
    val y: Float,
    val height: Float = 38f,
    val armAngleDeg: Float = 0f,
    val lightColor: Long = 0xFFFFF59D
)

data class CitySkybridge(
    val startX: Float,
    val startY: Float,
    val endX: Float,
    val endY: Float,
    val height: Float = 90f,
    val beamWidth: Float = 26f
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
    val buildings: List<CityBuilding> = emptyList(),
    val streetLamps: List<CityStreetLamp> = emptyList(),
    val skybridges: List<CitySkybridge> = emptyList(),
    val worldWidth: Float = 2400f,
    val worldHeight: Float = 2400f,
    val ambientColor: Long = 0xFF0D0F14,
    val roadColor: Long = 0xFF1C202A,
    val curbColor: Long = 0xFFFF1744,
    val barrierGlowColor: Long = 0xFF00E5FF
) {
    companion object {
        // Track 0: Rod City Expressway (Flagship Track from reference image)
        fun createRodCityTrack(): Track {
            val center = listOf(
                TrackPoint(450f, 400f),
                TrackPoint(950f, 400f),
                TrackPoint(1350f, 420f),
                TrackPoint(1700f, 560f),
                TrackPoint(1960f, 950f),
                TrackPoint(1900f, 1500f),
                TrackPoint(1550f, 1900f),
                TrackPoint(1050f, 1950f),
                TrackPoint(650f, 1650f),
                TrackPoint(400f, 1150f),
                TrackPoint(450f, 400f)
            )

            val inner = listOf(
                TrackPoint(450f, 480f),
                TrackPoint(950f, 480f),
                TrackPoint(1310f, 495f),
                TrackPoint(1620f, 620f),
                TrackPoint(1880f, 960f),
                TrackPoint(1820f, 1440f),
                TrackPoint(1510f, 1820f),
                TrackPoint(1050f, 1870f),
                TrackPoint(720f, 1590f),
                TrackPoint(480f, 1150f),
                TrackPoint(450f, 480f)
            )

            val outer = listOf(
                TrackPoint(450f, 320f),
                TrackPoint(950f, 320f),
                TrackPoint(1390f, 345f),
                TrackPoint(1780f, 500f),
                TrackPoint(2040f, 940f),
                TrackPoint(1980f, 1560f),
                TrackPoint(1590f, 1980f),
                TrackPoint(1050f, 2030f),
                TrackPoint(580f, 1710f),
                TrackPoint(320f, 1150f),
                TrackPoint(450f, 320f)
            )

            val clips = listOf(
                ClippingPoint("rod_clip_1", 1350f, 420f, 115f, "Rod Arch Apex"),
                ClippingPoint("rod_clip_2", 1700f, 560f, 120f, "Speed Loop Entry"),
                ClippingPoint("rod_clip_3", 1960f, 950f, 125f, "Skybridge Wall Tap"),
                ClippingPoint("rod_clip_4", 1900f, 1500f, 120f, "Arcade Carousel"),
                ClippingPoint("rod_clip_5", 1550f, 1900f, 115f, "Transition Chicane"),
                ClippingPoint("rod_clip_6", 650f, 1650f, 115f, "Home Hairpin")
            )

            val buildings = listOf(
                CityBuilding(
                    id = "rod_arch_tower",
                    x = 1260f,
                    y = 210f,
                    width = 240f,
                    length = 200f,
                    height = 240f,
                    rooftopSignText = "ROD CITY ARCHITECTURE",
                    midSignText = "ROD CITY",
                    signColor = 0xFFFFFFFF,
                    facadeColor = 0xFF334155,
                    windowGlowColor = 0xFFFFF176
                ),
                CityBuilding(
                    id = "speed_loop_parking",
                    x = 1880f,
                    y = 440f,
                    width = 230f,
                    length = 180f,
                    height = 125f,
                    rooftopSignText = "SPEED LOOP PARKING",
                    isParkingDeck = true,
                    signColor = 0xFFFFD54F,
                    facadeColor = 0xFF475569,
                    windowGlowColor = 0xFFFFD54F
                ),
                CityBuilding(
                    id = "neon_drift_arcade_1",
                    x = 280f,
                    y = 480f,
                    width = 160f,
                    length = 130f,
                    height = 95f,
                    rooftopSignText = "NEON DRIFT ARCADE",
                    signColor = 0xFFFF007F,
                    facadeColor = 0xFF1E293B,
                    windowGlowColor = 0xFFFF007F
                ),
                CityBuilding(
                    id = "neon_drift_arcade_2",
                    x = 2080f,
                    y = 800f,
                    width = 170f,
                    length = 140f,
                    height = 110f,
                    rooftopSignText = "NEON DRIFT",
                    signColor = 0xFF00F5FF,
                    facadeColor = 0xFF1E293B,
                    windowGlowColor = 0xFF00F5FF
                ),
                CityBuilding(
                    id = "metropolis_plaza",
                    x = 2060f,
                    y = 1420f,
                    width = 210f,
                    length = 180f,
                    height = 200f,
                    rooftopSignText = "METROPOLIS TOWER",
                    signColor = 0xFFA855F7,
                    facadeColor = 0xFF1E293B,
                    windowGlowColor = 0xFFE0E7FF
                ),
                CityBuilding(
                    id = "skyline_residence",
                    x = 280f,
                    y = 1150f,
                    width = 190f,
                    length = 170f,
                    height = 170f,
                    rooftopSignText = "SKYLINE DECK",
                    signColor = 0xFFFF6D00,
                    facadeColor = 0xFF334155,
                    windowGlowColor = 0xFFFFF176
                )
            )

            val skybridges = listOf(
                CitySkybridge(
                    startX = 1260f,
                    startY = 210f,
                    endX = 1880f,
                    endY = 440f,
                    height = 110f,
                    beamWidth = 28f
                )
            )

            val streetLamps = listOf(
                CityStreetLamp(650f, 310f, height = 36f, armAngleDeg = 90f),
                CityStreetLamp(1050f, 310f, height = 36f, armAngleDeg = 90f),
                CityStreetLamp(1450f, 340f, height = 36f, armAngleDeg = 110f),
                CityStreetLamp(1820f, 490f, height = 36f, armAngleDeg = 140f),
                CityStreetLamp(2060f, 940f, height = 36f, armAngleDeg = 180f),
                CityStreetLamp(1990f, 1560f, height = 36f, armAngleDeg = 210f),
                CityStreetLamp(1590f, 2000f, height = 36f, armAngleDeg = 260f),
                CityStreetLamp(1050f, 2050f, height = 36f, armAngleDeg = 270f),
                CityStreetLamp(570f, 1720f, height = 36f, armAngleDeg = 310f),
                CityStreetLamp(310f, 1150f, height = 36f, armAngleDeg = 0f),
                CityStreetLamp(440f, 310f, height = 36f, armAngleDeg = 90f)
            )

            return Track(
                id = "track_rod_city",
                name = "Rod City Expressway",
                location = "Rod City Skyway Overpass",
                difficulty = "Balanced",
                description = "Elevated multi-lane expressway winding around the iconic Rod City Architecture skyscraper, speed loop parking deck, and vibrant neon arcade storefronts.",
                roadWidth = 160f,
                startX = 500f,
                startY = 400f,
                startAngleDeg = 0f,
                centerLine = center,
                innerBoundary = inner,
                outerBoundary = outer,
                checkpoints = center,
                clippingPoints = clips,
                buildings = buildings,
                streetLamps = streetLamps,
                skybridges = skybridges,
                worldWidth = 2500f,
                worldHeight = 2500f,
                ambientColor = 0xFF141A24,
                roadColor = 0xFF1A1F29,
                curbColor = 0xFFCBD5E1,
                barrierGlowColor = 0xFFFF9100
            )
        }

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
            createRodCityTrack(),
            createNeonCityTrack(),
            createTougeTrack(),
            createDockyardTrack()
        )
    }
}
