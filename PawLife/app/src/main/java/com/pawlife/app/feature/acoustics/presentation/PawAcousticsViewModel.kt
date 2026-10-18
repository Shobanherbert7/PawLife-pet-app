package com.pawlife.app.feature.acoustics.presentation

import android.app.Application
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.pawlife.app.feature.acoustics.domain.BarkEpisode
import com.pawlife.app.feature.acoustics.domain.CanineMoodState
import com.pawlife.app.feature.acoustics.domain.SoothingAudioTrack
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject
import kotlin.math.PI
import kotlin.math.sin
import kotlin.random.Random

val AVAILABLE_SOOTHING_TRACKS = listOf(
    SoothingAudioTrack(
        id = "track_delta",
        title = "Delta-Wave Canine Lullaby",
        category = "Deep Relaxation",
        frequencyLabel = "432 Hz Bioharmonic",
        description = "Gentle delta frequencies proven to reduce canine cortisol and ease separation distress.",
        iconEmoji = "🎵",
        baseFrequencyHz = 432f
    ),
    SoothingAudioTrack(
        id = "track_pink",
        title = "Calming Rain & Pink Noise",
        category = "Acoustic Masking",
        frequencyLabel = "Ambient Spectrum",
        description = "Muffles high-frequency urban street sounds, thunder, and passing sirens.",
        iconEmoji = "🌧️",
        baseFrequencyHz = 220f
    ),
    SoothingAudioTrack(
        id = "track_heartbeat",
        title = "Maternal Heartbeat Rhythm",
        category = "Puppy Comfort",
        frequencyLabel = "60 BPM Rhythmic Pulse",
        description = "Mimics a mother dog's resting heart pulse to instill comfort and emotional security.",
        iconEmoji = "💓",
        baseFrequencyHz = 110f
    )
)

@HiltViewModel
class PawAcousticsViewModel @Inject constructor(
    application: Application
) : AndroidViewModel(application) {

    private val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    private val _isListening = MutableStateFlow(true)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private val _currentDecibels = MutableStateFlow(42)
    val currentDecibels: StateFlow<Int> = _currentDecibels.asStateFlow()

    private val _spectrogramAmplitudes = MutableStateFlow<List<Float>>(List(24) { 0.2f })
    val spectrogramAmplitudes: StateFlow<List<Float>> = _spectrogramAmplitudes.asStateFlow()

    private val _collarBpm = MutableStateFlow(86)
    val collarBpm: StateFlow<Int> = _collarBpm.asStateFlow()

    private val _latestEpisode = MutableStateFlow(
        BarkEpisode(
            id = "init-ep",
            petName = "Max",
            timestamp = System.currentTimeMillis() - 180000,
            moodState = CanineMoodState.PLAYFUL,
            confidenceScore = 0.94f,
            peakDecibels = 78,
            fundamentalPitchHz = 520,
            collarHeartRateBpm = 94,
            stressIndex = 32,
            audioDurationSeconds = 3,
            summary = "Joyful play-bow vocalizations during afternoon garden play."
        )
    )
    val latestEpisode: StateFlow<BarkEpisode> = _latestEpisode.asStateFlow()

    private val _recentEpisodes = MutableStateFlow<List<BarkEpisode>>(
        listOf(
            BarkEpisode(
                id = "ep-1",
                petName = "Max",
                timestamp = System.currentTimeMillis() - 180000,
                moodState = CanineMoodState.PLAYFUL,
                confidenceScore = 0.94f,
                peakDecibels = 78,
                fundamentalPitchHz = 520,
                collarHeartRateBpm = 94,
                stressIndex = 32,
                audioDurationSeconds = 3,
                summary = "Bouncy high pitch with playful pause."
            ),
            BarkEpisode(
                id = "ep-2",
                petName = "Max",
                timestamp = System.currentTimeMillis() - 3600000,
                moodState = CanineMoodState.ALERT,
                confidenceScore = 0.89f,
                peakDecibels = 84,
                fundamentalPitchHz = 360,
                collarHeartRateBpm = 112,
                stressIndex = 74,
                audioDurationSeconds = 5,
                summary = "Territorial alert bark triggered by delivery arrival."
            ),
            BarkEpisode(
                id = "ep-3",
                petName = "Max",
                timestamp = System.currentTimeMillis() - 14400000,
                moodState = CanineMoodState.ANXIETY,
                confidenceScore = 0.91f,
                peakDecibels = 68,
                fundamentalPitchHz = 710,
                collarHeartRateBpm = 104,
                stressIndex = 65,
                audioDurationSeconds = 8,
                summary = "Ascending whines during mid-morning alone time."
            )
        )
    )
    val recentEpisodes: StateFlow<List<BarkEpisode>> = _recentEpisodes.asStateFlow()

    private val _activeTherapyTrack = MutableStateFlow<SoothingAudioTrack?>(null)
    val activeTherapyTrack: StateFlow<SoothingAudioTrack?> = _activeTherapyTrack.asStateFlow()

    private val _isTherapyPlaying = MutableStateFlow(false)
    val isTherapyPlaying: StateFlow<Boolean> = _isTherapyPlaying.asStateFlow()

    private var audioLoopJob: Job? = null
    private var therapyAudioTrack: AudioTrack? = null
    private var therapyJob: Job? = null

    init {
        startAmbientAudioLoop()
        listenToCloudAcousticEvents()
    }

    private fun startAmbientAudioLoop() {
        audioLoopJob?.cancel()
        audioLoopJob = viewModelScope.launch(Dispatchers.Default) {
            while (isActive) {
                if (_isListening.value) {
                    val baseDb = if (_isTherapyPlaying.value) 38 else 44
                    val randomJitter = Random.nextInt(-4, 8)
                    _currentDecibels.value = (baseDb + randomJitter).coerceIn(30, 95)

                    // Generate responsive spectrogram bars
                    val newAmplitudes = List(24) { i ->
                        val freqFactor = sin((i.toDouble() / 24.0) * PI).toFloat()
                        val noise = Random.nextFloat() * 0.35f
                        (freqFactor * 0.65f + noise).coerceIn(0.1f, 1.0f)
                    }
                    _spectrogramAmplitudes.value = newAmplitudes
                }
                delay(120)
            }
        }
    }

    fun toggleListening() {
        _isListening.value = !_isListening.value
    }

    fun simulateBarkEpisode(mood: CanineMoodState) {
        viewModelScope.launch {
            // Decibel surge during bark
            _currentDecibels.value = Random.nextInt(78, 88)
            val surgeAmplitudes = List(24) { Random.nextFloat().coerceIn(0.6f, 1.0f) }
            _spectrogramAmplitudes.value = surgeAmplitudes

            val simulatedBpm = when (mood) {
                CanineMoodState.ALERT -> Random.nextInt(110, 128)
                CanineMoodState.ANXIETY -> Random.nextInt(102, 118)
                CanineMoodState.PAIN -> Random.nextInt(115, 135)
                CanineMoodState.PLAYFUL -> Random.nextInt(92, 108)
                CanineMoodState.CALM -> Random.nextInt(75, 86)
            }
            _collarBpm.value = simulatedBpm

            val stressIndex = when (mood) {
                CanineMoodState.ALERT -> 78
                CanineMoodState.ANXIETY -> 82
                CanineMoodState.PAIN -> 95
                CanineMoodState.PLAYFUL -> 28
                CanineMoodState.CALM -> 12
            }

            val pitch = when (mood) {
                CanineMoodState.ALERT -> 340
                CanineMoodState.ANXIETY -> 740
                CanineMoodState.PAIN -> 820
                CanineMoodState.PLAYFUL -> 540
                CanineMoodState.CALM -> 220
            }

            val newEpisode = BarkEpisode(
                id = UUID.randomUUID().toString(),
                petName = "Max",
                timestamp = System.currentTimeMillis(),
                moodState = mood,
                confidenceScore = 0.92f + (Random.nextFloat() * 0.06f),
                peakDecibels = _currentDecibels.value,
                fundamentalPitchHz = pitch,
                collarHeartRateBpm = simulatedBpm,
                stressIndex = stressIndex,
                audioDurationSeconds = Random.nextInt(3, 7),
                summary = "Classified as ${mood.displayName} (${(0.94f * 100).toInt()}% match). ${mood.advice}"
            )

            _latestEpisode.value = newEpisode
            _recentEpisodes.value = listOf(newEpisode) + _recentEpisodes.value.take(15)

            // Auto-trigger sound therapy if separation anxiety or pain is detected
            if (mood == CanineMoodState.ANXIETY) {
                AVAILABLE_SOOTHING_TRACKS.firstOrNull()?.let { startSoothingTrack(it) }
            }

            // Sync to Firestore
            syncEpisodeToFirestore(newEpisode)
        }
    }

    private fun syncEpisodeToFirestore(episode: BarkEpisode) {
        val eventDoc = hashMapOf(
            "id" to episode.id,
            "petName" to episode.petName,
            "moodState" to episode.moodState.name,
            "moodLabel" to episode.moodState.displayName,
            "emoji" to episode.moodState.emoji,
            "confidenceScore" to episode.confidenceScore,
            "peakDecibels" to episode.peakDecibels,
            "fundamentalPitchHz" to episode.fundamentalPitchHz,
            "collarHeartRateBpm" to episode.collarHeartRateBpm,
            "stressIndex" to episode.stressIndex,
            "summary" to episode.summary,
            "timestamp" to episode.timestamp
        )

        try {
            firestore.collection("acoustic_mood_events")
                .document(episode.id)
                .set(eventDoc)
                .addOnFailureListener { e ->
                    // Firestore optional offline sync
                }
        } catch (_: Exception) {}
    }

    private fun listenToCloudAcousticEvents() {
        try {
            firestore.collection("acoustic_mood_events")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(20)
                .addSnapshotListener { snapshot, error ->
                    if (error == null && snapshot != null && !snapshot.isEmpty) {
                        val cloudEpisodes = snapshot.documents.mapNotNull { doc ->
                            try {
                                val moodName = doc.getString("moodState") ?: "CALM"
                                val moodState = try {
                                    CanineMoodState.valueOf(moodName)
                                } catch (_: Exception) {
                                    CanineMoodState.CALM
                                }
                                BarkEpisode(
                                    id = doc.id,
                                    petName = doc.getString("petName") ?: "Max",
                                    timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis(),
                                    moodState = moodState,
                                    confidenceScore = (doc.getDouble("confidenceScore") ?: 0.92).toFloat(),
                                    peakDecibels = (doc.getLong("peakDecibels") ?: 75L).toInt(),
                                    fundamentalPitchHz = (doc.getLong("fundamentalPitchHz") ?: 440L).toInt(),
                                    collarHeartRateBpm = (doc.getLong("collarHeartRateBpm") ?: 85L).toInt(),
                                    stressIndex = (doc.getLong("stressIndex") ?: 35L).toInt(),
                                    summary = doc.getString("summary") ?: ""
                                )
                            } catch (_: Exception) {
                                null
                            }
                        }
                        if (cloudEpisodes.isNotEmpty()) {
                            _recentEpisodes.value = cloudEpisodes
                            _latestEpisode.value = cloudEpisodes.first()
                        }
                    }
                }
        } catch (_: Exception) {}
    }

    fun startSoothingTrack(track: SoothingAudioTrack) {
        stopSoothingTrack()
        _activeTherapyTrack.value = track
        _isTherapyPlaying.value = true

        therapyJob = viewModelScope.launch(Dispatchers.Default) {
            playCalmingTone(track.baseFrequencyHz)
        }
    }

    fun stopSoothingTrack() {
        _isTherapyPlaying.value = false
        _activeTherapyTrack.value = null
        therapyJob?.cancel()
        therapyJob = null

        try {
            therapyAudioTrack?.stop()
            therapyAudioTrack?.release()
        } catch (_: Exception) {}
        therapyAudioTrack = null
    }

    private suspend fun playCalmingTone(frequencyHz: Float) = withContext(Dispatchers.Default) {
        val sampleRate = 44100
        val bufferSize = AudioTrack.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )

        val track = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(sampleRate)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(bufferSize)
            .setTransferMode(AudioTrack.MODE_STREAM)
            .build()

        therapyAudioTrack = track
        track.play()

        val buffer = ShortArray(bufferSize / 2)
        var phase = 0.0

        try {
            while (isActive && _isTherapyPlaying.value) {
                for (i in buffer.indices) {
                    val sample = (sin(phase) * 0.25 * Short.MAX_VALUE).toInt().toShort()
                    buffer[i] = sample
                    phase += (2.0 * PI * frequencyHz) / sampleRate
                    if (phase > 2.0 * PI) {
                        phase -= 2.0 * PI
                    }
                }
                track.write(buffer, 0, buffer.size)
            }
        } catch (_: Exception) {
        } finally {
            try {
                track.stop()
                track.release()
            } catch (_: Exception) {}
        }
    }

    override fun onCleared() {
        super.onCleared()
        audioLoopJob?.cancel()
        stopSoothingTrack()
    }
}
