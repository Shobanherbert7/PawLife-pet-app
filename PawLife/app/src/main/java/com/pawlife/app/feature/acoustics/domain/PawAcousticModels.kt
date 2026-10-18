package com.pawlife.app.feature.acoustics.domain

enum class CanineMoodState(
    val displayName: String,
    val emoji: String,
    val description: String,
    val colorHex: Long,
    val advice: String
) {
    CALM(
        displayName = "Calm & Content",
        emoji = "💚",
        description = "Regular breathing, absence of vocal stress indicators.",
        colorHex = 0xFF10B981,
        advice = "Your pet is relaxed and at ease. Great time for restful bonding."
    ),
    PLAYFUL(
        displayName = "Playful & Joyful",
        emoji = "🎾",
        description = "High variable pitch, short bouncy bark bursts with tail wags.",
        colorHex = 0xFF0EA5E9,
        advice = "High excitement! Engage in interactive play, fetch, or agility training."
    ),
    ALERT(
        displayName = "Alert & Territorial",
        emoji = "🚨",
        description = "Lower pitch staccato barking, focused attention on perimeter.",
        colorHex = 0xFFF59E0B,
        advice = "Pet detects something outside. Redirect focus calmly with positive reinforcement."
    ),
    ANXIETY(
        displayName = "Separation Anxiety",
        emoji = "🥺",
        description = "Continuous high-pitched whines, ascending tones, pacing sounds.",
        colorHex = 0xFFFB7185,
        advice = "Pet feels isolated or stressed. Activate anti-anxiety sound therapy."
    ),
    PAIN(
        displayName = "Pain / Distress",
        emoji = "🩹",
        description = "Strained yelps, abrupt whimpers, high distress markers.",
        colorHex = 0xFFEF4444,
        advice = "Potential physical discomfort detected. Check paws/joints and consult tele-vet."
    )
}

data class BarkEpisode(
    val id: String = "",
    val petId: Long = 1L,
    val petName: String = "Max",
    val timestamp: Long = System.currentTimeMillis(),
    val moodState: CanineMoodState = CanineMoodState.CALM,
    val confidenceScore: Float = 0.92f,
    val peakDecibels: Int = 72,
    val fundamentalPitchHz: Int = 480,
    val collarHeartRateBpm: Int = 88,
    val stressIndex: Int = 35, // 0 - 100
    val audioDurationSeconds: Int = 4,
    val isTherapyActive: Boolean = false,
    val summary: String = ""
)

data class SoothingAudioTrack(
    val id: String,
    val title: String,
    val category: String,
    val frequencyLabel: String,
    val description: String,
    val iconEmoji: String,
    val baseFrequencyHz: Float
)
