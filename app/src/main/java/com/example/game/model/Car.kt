package com.example.game.model

data class Car(
    val id: String,
    val name: String,
    val series: String,
    val description: String,
    val basePrice: Int,
    val isUnlocked: Boolean = false,
    val bodyColor: Long, // Hex color
    val accentColor: Long,
    val baseTopSpeed: Float, // max velocity
    val baseAcceleration: Float, // engine force
    val baseHandling: Float, // steering agility
    val baseDriftFactor: Float, // lateral slip ratio
    val engineLevel: Int = 0,
    val driftKitLevel: Int = 0,
    val tireLevel: Int = 0,
    val nitroLevel: Int = 0,
    val smokeColor: Long = 0xFF00F5FF, // Colorful tire smoke
    val underglowColor: Long = 0xFFFF007F // Vibrant neon ground underglow
) {
    val topSpeed: Float get() = baseTopSpeed * (1f + engineLevel * 0.08f)
    val acceleration: Float get() = baseAcceleration * (1f + engineLevel * 0.10f)
    val handling: Float get() = baseHandling * (1f + driftKitLevel * 0.08f)
    val driftFactor: Float get() = baseDriftFactor * (1f + driftKitLevel * 0.12f)
    val gripBalance: Float get() = 1f + tireLevel * 0.07f
    val nitroDurationSec: Float get() = 3.5f + nitroLevel * 1.0f

    companion object {
        val DEFAULT_CARS = listOf(
            Car(
                id = "car_86",
                name = "Hachi-86 FR",
                series = "Touge Icon",
                description = "Lightweight rear-wheel-drive legend. Exceptional balance and forgiving drift transitions.",
                basePrice = 0,
                isUnlocked = true,
                bodyColor = 0xFFECEFF1, // Panda white
                accentColor = 0xFF212121, // Panda black hood/sides
                baseTopSpeed = 390f,
                baseAcceleration = 330f,
                baseHandling = 1.05f,
                baseDriftFactor = 1.05f,
                smokeColor = 0xFF00F5FF, // Neon cyan smoke
                underglowColor = 0xFF00F5FF
            ),
            Car(
                id = "car_s15",
                name = "Silvia S-Apex",
                series = "D1 Spec",
                description = "Aggressive turbo coupe built for long, wide-angle high-speed slides and quick snap-backs.",
                basePrice = 3000,
                isUnlocked = false,
                bodyColor = 0xFF00F5FF, // Electric cyan
                accentColor = 0xFFFF6D00, // Orange racing vinyl
                baseTopSpeed = 450f,
                baseAcceleration = 380f,
                baseHandling = 1.18f,
                baseDriftFactor = 1.25f,
                smokeColor = 0xFFFF007F, // Neon magenta smoke
                underglowColor = 0xFFFF6D00
            ),
            Car(
                id = "car_rx7",
                name = "Rotary RX-Spirit",
                series = "Midnight Club",
                description = "High-revving twin turbo rotary powerhouse. Phenomenal mid-slide throttle control.",
                basePrice = 7500,
                isUnlocked = false,
                bodyColor = 0xFFFFD600, // Sunburst yellow
                accentColor = 0xFFFF1744,
                baseTopSpeed = 490f,
                baseAcceleration = 420f,
                baseHandling = 1.28f,
                baseDriftFactor = 1.38f,
                smokeColor = 0xFFFFD600, // Neon gold smoke
                underglowColor = 0xFFFF1744
            ),
            Car(
                id = "car_gtr",
                name = "Godzilla GT-R34",
                series = "Track Dominator",
                description = "Unstoppable straightaway acceleration with massive momentum for high-speed wall scrapes.",
                basePrice = 14000,
                isUnlocked = false,
                bodyColor = 0xFF1E88E5, // Electric blue
                accentColor = 0xFF00E676, // Toxic green accents
                baseTopSpeed = 540f,
                baseAcceleration = 470f,
                baseHandling = 1.15f,
                baseDriftFactor = 1.30f,
                smokeColor = 0xFF00E676, // Toxic green smoke
                underglowColor = 0xFF1E88E5
            ),
            Car(
                id = "car_cyber",
                name = "Viper 2077 Hyperdrift",
                series = "Prototype X",
                description = "Futuristic electric drift hypercar featuring active torque-vectoring and instant NOS refill.",
                basePrice = 25000,
                isUnlocked = false,
                bodyColor = 0xFFA855F7, // Neon purple
                accentColor = 0xFFFF007F, // Hot pink neon underglow
                baseTopSpeed = 600f,
                baseAcceleration = 540f,
                baseHandling = 1.45f,
                baseDriftFactor = 1.55f,
                smokeColor = 0xFFFF1493, // Hot pink smoke
                underglowColor = 0xFFFF007F
            )
        )
    }
}
