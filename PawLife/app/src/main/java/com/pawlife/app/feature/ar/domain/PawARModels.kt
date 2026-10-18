package com.pawlife.app.feature.ar.domain

enum class AgilityObstacleType(
    val displayName: String,
    val iconEmoji: String,
    val xpValue: Int,
    val description: String
) {
    CONE(
        displayName = "Slalom Agility Cone",
        iconEmoji = "🟧",
        xpValue = 15,
        description = "Weave in and out to build canine lateral agility and focus."
    ),
    HURDLE(
        displayName = "Jump Hurdle Bar",
        iconEmoji = "🚧",
        xpValue = 25,
        description = "Adjustable height hurdle for spring conditioning."
    ),
    TARGET_PAD(
        displayName = "Focus Target Mat",
        iconEmoji = "🎯",
        xpValue = 20,
        description = "Touch-point mat for precision pause and recall control."
    ),
    HOOP_JUMP(
        displayName = "Agility Ring Hoop",
        iconEmoji = "⭕",
        xpValue = 30,
        description = "Elevated ring jump for advanced aerial coordination."
    ),
    TUNNEL(
        displayName = "Agility Chute Tunnel",
        iconEmoji = "🎪",
        xpValue = 35,
        description = "Dark tunnel run building bravery and speed."
    )
}

data class PlacedObstacle(
    val id: String,
    val type: AgilityObstacleType,
    val xPos: Float,
    val yPos: Float,
    val isCleared: Boolean = false,
    val clearedAt: Long? = null
)

enum class TrickDifficulty(val label: String) {
    BEGINNER("Beginner"),
    INTERMEDIATE("Intermediate"),
    ADVANCED("Advanced")
}

data class CanineTrick(
    val id: String,
    val name: String,
    val difficulty: TrickDifficulty,
    val iconEmoji: String,
    val cuePhrase: String,
    val xpReward: Int,
    val stepByStepInstructions: List<String>,
    val repsCompleted: Int = 0,
    val isMastered: Boolean = false
)

data class AgilityTrainingSession(
    val id: String,
    val petId: Long,
    val timestamp: Long,
    val obstaclesCleared: Int,
    val repsCount: Int,
    val xpEarned: Int,
    val durationSeconds: Int,
    val tricksPracticed: List<String>
)

data class AgilityMasteryLevel(
    val level: Int,
    val title: String,
    val currentXp: Int,
    val targetXp: Int,
    val badgeName: String,
    val badgeIcon: String
) {
    val progressPercent: Float
        get() = (currentXp.toFloat() / targetXp.toFloat()).coerceIn(0f, 1f)
}
