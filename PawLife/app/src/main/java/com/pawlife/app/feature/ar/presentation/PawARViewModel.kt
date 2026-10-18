package com.pawlife.app.feature.ar.presentation

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.pawlife.app.feature.ar.domain.AgilityMasteryLevel
import com.pawlife.app.feature.ar.domain.AgilityObstacleType
import com.pawlife.app.feature.ar.domain.AgilityTrainingSession
import com.pawlife.app.feature.ar.domain.CanineTrick
import com.pawlife.app.feature.ar.domain.PlacedObstacle
import com.pawlife.app.feature.ar.domain.TrickDifficulty
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class PawARViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()

    // 1. Audio Clicker Tone Generator
    private val toneGenerator: ToneGenerator? = try {
        ToneGenerator(AudioManager.STREAM_MUSIC, 100)
    } catch (_: Exception) {
        null
    }

    // 2. Sub-Tab: 0 = AR Agility Arena, 1 = Trick Coach, 2 = Mastery & XP
    private val _selectedSubTab = MutableStateFlow(0)
    val selectedSubTab: StateFlow<Int> = _selectedSubTab.asStateFlow()

    // 3. Selected Obstacle for Placement
    private val _selectedObstacleType = MutableStateFlow(AgilityObstacleType.CONE)
    val selectedObstacleType: StateFlow<AgilityObstacleType> = _selectedObstacleType.asStateFlow()

    // 4. Placed Obstacles
    private val _placedObstacles = MutableStateFlow<List<PlacedObstacle>>(
        listOf(
            PlacedObstacle(
                id = "obs_1",
                type = AgilityObstacleType.CONE,
                xPos = 0.25f,
                yPos = 0.40f,
                isCleared = true,
                clearedAt = System.currentTimeMillis() - 120_000
            ),
            PlacedObstacle(
                id = "obs_2",
                type = AgilityObstacleType.HURDLE,
                xPos = 0.50f,
                yPos = 0.55f,
                isCleared = false
            ),
            PlacedObstacle(
                id = "obs_3",
                type = AgilityObstacleType.TARGET_PAD,
                xPos = 0.75f,
                yPos = 0.35f,
                isCleared = false
            )
        )
    )
    val placedObstacles: StateFlow<List<PlacedObstacle>> = _placedObstacles.asStateFlow()

    // 5. Trick Curriculum
    private val defaultTricks = listOf(
        CanineTrick(
            id = "trick_sit",
            name = "Sit & Focus Mark",
            difficulty = TrickDifficulty.BEGINNER,
            iconEmoji = "🐕",
            cuePhrase = "Sit!",
            xpReward = 15,
            stepByStepInstructions = listOf(
                "Hold high-value treat close to your dog's nose.",
                "Slowly arc your hand back over their forehead.",
                "As their chin raises, their bottom naturally lowers.",
                "The instant their hips touch the ground: CLICK & TREAT!"
            ),
            repsCompleted = 8,
            isMastered = true
        ),
        CanineTrick(
            id = "trick_stay",
            name = "Anchor Stay",
            difficulty = TrickDifficulty.BEGINNER,
            iconEmoji = "✋",
            cuePhrase = "Stay!",
            xpReward = 20,
            stepByStepInstructions = listOf(
                "Ask for a stable Sit first.",
                "Show an open palm 6 inches from their chest: 'Stay'.",
                "Take 1 step backward, pause 3 seconds, step back.",
                "CLICK & release with 'Yes!' and reward."
            ),
            repsCompleted = 5,
            isMastered = false
        ),
        CanineTrick(
            id = "trick_paw",
            name = "High-Five / Paw",
            difficulty = TrickDifficulty.INTERMEDIATE,
            iconEmoji = "🐾",
            cuePhrase = "Paw!",
            xpReward = 25,
            stepByStepInstructions = listOf(
                "Present a closed fist with a treat inside at chest height.",
                "Dog will sniff, then instinctively paw at your fist.",
                "The second their paw contacts your hand: CLICK!",
                "Open palm and deliver the reward."
            ),
            repsCompleted = 6,
            isMastered = false
        ),
        CanineTrick(
            id = "trick_spin",
            name = "360° Body Spin",
            difficulty = TrickDifficulty.INTERMEDIATE,
            iconEmoji = "🔄",
            cuePhrase = "Spin!",
            xpReward = 30,
            stepByStepInstructions = listOf(
                "Lead dog's nose with a treat in a full circle.",
                "Keep the lure close so they follow smoothly without jumping.",
                "As they complete the full revolution: CLICK & TREAT!",
                "Phase out the lure into a gentle finger spin gesture."
            ),
            repsCompleted = 3,
            isMastered = false
        ),
        CanineTrick(
            id = "trick_hurdle",
            name = "AR Hurdle Leap",
            difficulty = TrickDifficulty.ADVANCED,
            iconEmoji = "🦘",
            cuePhrase = "Over!",
            xpReward = 40,
            stepByStepInstructions = listOf(
                "Place virtual Hurdle Bar at 20 cm height.",
                "Jog alongside dog toward the virtual hurdle.",
                "Signal with cue 'Over!' as front paws clear bar.",
                "CLICK upon clean 4-paw landing!"
            ),
            repsCompleted = 4,
            isMastered = false
        )
    )

    private val _tricks = MutableStateFlow<List<CanineTrick>>(defaultTricks)
    val tricks: StateFlow<List<CanineTrick>> = _tricks.asStateFlow()

    private val _activeTrickId = MutableStateFlow<String>("trick_sit")
    val activeTrickId: StateFlow<String> = _activeTrickId.asStateFlow()

    // 6. Cumulative XP & Mastery Level
    private val _totalXp = MutableStateFlow(420)
    val totalXp: StateFlow<Int> = _totalXp.asStateFlow()

    val masteryLevel: StateFlow<AgilityMasteryLevel> = MutableStateFlow(computeMastery(420)).asStateFlow()

    // 7. Training Timer
    private val _timerSeconds = MutableStateFlow(0)
    val timerSeconds: StateFlow<Int> = _timerSeconds.asStateFlow()

    private val _isTimerRunning = MutableStateFlow(false)
    val isTimerRunning: StateFlow<Boolean> = _isTimerRunning.asStateFlow()
    private var timerJob: Job? = null

    // 8. Event / Notification Message Stream
    private val _eventFlow = MutableSharedFlow<String>()
    val eventFlow = _eventFlow.asSharedFlow()

    // 9. Recent Training Sessions
    private val _recentSessions = MutableStateFlow<List<AgilityTrainingSession>>(
        listOf(
            AgilityTrainingSession(
                id = "sess_1",
                petId = 1L,
                timestamp = System.currentTimeMillis() - 86_400_000,
                obstaclesCleared = 3,
                repsCount = 12,
                xpEarned = 180,
                durationSeconds = 640,
                tricksPracticed = listOf("Sit & Focus Mark", "Anchor Stay")
            ),
            AgilityTrainingSession(
                id = "sess_2",
                petId = 1L,
                timestamp = System.currentTimeMillis() - 250_000_000,
                obstaclesCleared = 4,
                repsCount = 15,
                xpEarned = 240,
                durationSeconds = 890,
                tricksPracticed = listOf("High-Five / Paw", "360° Body Spin")
            )
        )
    )
    val recentSessions: StateFlow<List<AgilityTrainingSession>> = _recentSessions.asStateFlow()

    init {
        listenToFirestoreAgilitySessions()
    }

    private fun listenToFirestoreAgilitySessions() {
        try {
            firestore.collection("ar_agility_sessions")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(10)
                .addSnapshotListener { snapshot, err ->
                    if (err != null || snapshot == null) return@addSnapshotListener
                    val cloudSessions = snapshot.documents.mapNotNull { doc ->
                        val petId = doc.getLong("petId") ?: 1L
                        val timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis()
                        val obstacles = doc.getLong("obstaclesCleared")?.toInt() ?: 0
                        val reps = doc.getLong("repsCount")?.toInt() ?: 0
                        val xp = doc.getLong("xpEarned")?.toInt() ?: 0
                        val dur = doc.getLong("durationSeconds")?.toInt() ?: 300
                        @Suppress("UNCHECKED_CAST")
                        val tricks = doc.get("tricksPracticed") as? List<String> ?: emptyList()
                        AgilityTrainingSession(
                            id = doc.id,
                            petId = petId,
                            timestamp = timestamp,
                            obstaclesCleared = obstacles,
                            repsCount = reps,
                            xpEarned = xp,
                            durationSeconds = dur,
                            tricksPracticed = tricks
                        )
                    }
                    if (cloudSessions.isNotEmpty()) {
                        _recentSessions.value = cloudSessions
                    }
                }
        } catch (_: Exception) {}
    }

    fun setSelectedSubTab(tabIndex: Int) {
        _selectedSubTab.value = tabIndex
    }

    fun setSelectedObstacleType(type: AgilityObstacleType) {
        _selectedObstacleType.value = type
    }

    fun setActiveTrick(trickId: String) {
        _activeTrickId.value = trickId
    }

    // --- Clicker Engine ---
    fun playClickerSound(trickId: String? = null) {
        // 1. Crisp audible click tone
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 60)
        } catch (_: Exception) {}

        // 2. Haptic vibration feedback
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator?.vibrate(VibrationEffect.createOneShot(45, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                vibrator?.vibrate(VibrationEffect.createOneShot(45, VibrationEffect.DEFAULT_AMPLITUDE))
            }
        } catch (_: Exception) {}

        // 3. Award XP & update trick repetition
        val targetId = trickId ?: _activeTrickId.value
        val trick = _tricks.value.find { it.id == targetId }
        val xpGain = trick?.xpReward ?: 15

        _tricks.update { list ->
            list.map { t ->
                if (t.id == targetId) {
                    val nextReps = t.repsCompleted + 1
                    t.copy(repsCompleted = nextReps, isMastered = nextReps >= 10)
                } else t
            }
        }

        val updatedXp = _totalXp.value + xpGain
        _totalXp.value = updatedXp

        viewModelScope.launch {
            _eventFlow.emit("🔔 Click Marked! +$xpGain XP for ${trick?.name ?: "Agility"}")
            syncMasteryToFirestore(updatedXp)
        }
    }

    // --- Agility Arena Obstacle Placement ---
    fun placeObstacle(x: Float, y: Float) {
        val newObstacle = PlacedObstacle(
            id = "obs_${System.currentTimeMillis()}",
            type = _selectedObstacleType.value,
            xPos = x.coerceIn(0.1f, 0.9f),
            yPos = y.coerceIn(0.1f, 0.9f)
        )
        _placedObstacles.update { it + newObstacle }
        viewModelScope.launch {
            _eventFlow.emit("Placed ${_selectedObstacleType.value.displayName} in Arena!")
        }
    }

    fun toggleClearObstacle(obstacleId: String) {
        _placedObstacles.update { list ->
            list.map { obs ->
                if (obs.id == obstacleId) {
                    val nextCleared = !obs.isCleared
                    if (nextCleared) {
                        val xpBonus = obs.type.xpValue
                        _totalXp.update { it + xpBonus }
                        viewModelScope.launch {
                            _eventFlow.emit("🎉 Cleared ${obs.type.displayName}! +$xpBonus XP")
                            syncMasteryToFirestore(_totalXp.value)
                        }
                    }
                    obs.copy(isCleared = nextCleared, clearedAt = if (nextCleared) System.currentTimeMillis() else null)
                } else obs
            }
        }
    }

    fun clearAllObstacles() {
        _placedObstacles.value = emptyList()
        viewModelScope.launch {
            _eventFlow.emit("Agility Arena reset.")
        }
    }

    // --- Training Timer ---
    fun startTimer() {
        if (_isTimerRunning.value) return
        _isTimerRunning.value = true
        timerJob = viewModelScope.launch {
            while (_isTimerRunning.value) {
                delay(1000)
                _timerSeconds.update { it + 1 }
            }
        }
    }

    fun pauseTimer() {
        _isTimerRunning.value = false
        timerJob?.cancel()
    }

    fun resetTimer() {
        pauseTimer()
        _timerSeconds.value = 0
    }

    // --- Session Persistence ---
    fun finishSession(petId: Long = 1L) {
        val clearedCount = _placedObstacles.value.count { it.isCleared }
        val duration = _timerSeconds.value
        val reps = _tricks.value.sumOf { it.repsCompleted }
        val sessionXp = (clearedCount * 25) + (reps * 15)

        val session = AgilityTrainingSession(
            id = "sess_${System.currentTimeMillis()}",
            petId = petId,
            timestamp = System.currentTimeMillis(),
            obstaclesCleared = clearedCount,
            repsCount = reps,
            xpEarned = sessionXp,
            durationSeconds = duration,
            tricksPracticed = _tricks.value.filter { it.repsCompleted > 0 }.map { it.name }
        )

        _recentSessions.update { listOf(session) + it }
        resetTimer()

        viewModelScope.launch {
            try {
                firestore.collection("ar_agility_sessions").document(session.id)
                    .set(
                        mapOf(
                            "petId" to session.petId,
                            "timestamp" to session.timestamp,
                            "obstaclesCleared" to session.obstaclesCleared,
                            "repsCount" to session.repsCount,
                            "xpEarned" to session.xpEarned,
                            "durationSeconds" to session.durationSeconds,
                            "tricksPracticed" to session.tricksPracticed
                        )
                    )
                _eventFlow.emit("🏆 Training Session Saved! Earned +$sessionXp Agility XP")
            } catch (e: Exception) {
                _eventFlow.emit("Session recorded locally.")
            }
        }
    }

    private suspend fun syncMasteryToFirestore(xp: Int) {
        try {
            val levelObj = computeMastery(xp)
            firestore.collection("pet_trick_mastery").document("pet_1")
                .set(
                    mapOf(
                        "totalXp" to xp,
                        "level" to levelObj.level,
                        "title" to levelObj.title,
                        "badgeName" to levelObj.badgeName,
                        "updatedAt" to System.currentTimeMillis()
                    )
                )
        } catch (_: Exception) {}
    }

    private fun computeMastery(xp: Int): AgilityMasteryLevel {
        return when {
            xp < 200 -> AgilityMasteryLevel(1, "Curious Pup", xp, 200, "Bronze Paw", "🥉")
            xp < 500 -> AgilityMasteryLevel(2, "Novice Agility", xp, 500, "Silver Agility", "🥈")
            xp < 1000 -> AgilityMasteryLevel(3, "Park Prodigy", xp, 1000, "Gold Champion", "🥇")
            else -> AgilityMasteryLevel(4, "Grand Agility Champion", xp, 2000, "Diamond Trophy", "🏆")
        }
    }

    override fun onCleared() {
        super.onCleared()
        try {
            toneGenerator?.release()
        } catch (_: Exception) {}
        timerJob?.cancel()
    }
}
