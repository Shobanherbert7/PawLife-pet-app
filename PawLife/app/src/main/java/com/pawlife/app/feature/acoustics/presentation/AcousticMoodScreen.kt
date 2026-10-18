package com.pawlife.app.feature.acoustics.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Hearing
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pawlife.app.feature.acoustics.domain.BarkEpisode
import com.pawlife.app.feature.acoustics.domain.CanineMoodState
import com.pawlife.app.feature.acoustics.domain.SoothingAudioTrack
import com.pawlife.app.presentation.theme.BackgroundDark
import com.pawlife.app.presentation.theme.CardDark
import com.pawlife.app.presentation.theme.Danger
import com.pawlife.app.presentation.theme.Primary
import com.pawlife.app.presentation.theme.Secondary
import com.pawlife.app.presentation.theme.SlateLight
import com.pawlife.app.presentation.theme.SlateSubtle
import com.pawlife.app.presentation.theme.Success
import com.pawlife.app.presentation.theme.Warning
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AcousticMoodScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: PawAcousticsViewModel = hiltViewModel()
) {
    val isListening by viewModel.isListening.collectAsStateWithLifecycle()
    val decibels by viewModel.currentDecibels.collectAsStateWithLifecycle()
    val spectrogramAmps by viewModel.spectrogramAmplitudes.collectAsStateWithLifecycle()
    val collarBpm by viewModel.collarBpm.collectAsStateWithLifecycle()
    val latestEpisode by viewModel.latestEpisode.collectAsStateWithLifecycle()
    val recentEpisodes by viewModel.recentEpisodes.collectAsStateWithLifecycle()
    val activeTrack by viewModel.activeTherapyTrack.collectAsStateWithLifecycle()
    val isTherapyPlaying by viewModel.isTherapyPlaying.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = BackgroundDark,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "PawAcoustics AI",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = SlateLight
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                color = if (isListening) Primary.copy(alpha = 0.2f) else SlateSubtle.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = if (isListening) "LIVE MIC" else "MUTED",
                                    color = if (isListening) Primary else SlateSubtle,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "Acoustic Bark & Stress Classifier",
                            style = MaterialTheme.typography.bodySmall.copy(color = SlateSubtle)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = SlateLight
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.toggleListening() }) {
                        Icon(
                            imageVector = if (isListening) Icons.Default.Mic else Icons.Default.MicOff,
                            contentDescription = "Toggle Mic",
                            tint = if (isListening) Primary else SlateSubtle
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundDark)
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            contentPadding = PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // ── Section 1: Real-time Audio Spectrogram & Decibel Visualizer ──
            item {
                SpectrogramCard(
                    isListening = isListening,
                    decibels = decibels,
                    amplitudes = spectrogramAmps
                )
            }

            // ── Section 2: Emotional Triage Hero Card ──
            item {
                EmotionalTriageHeroCard(
                    episode = latestEpisode,
                    collarBpm = collarBpm
                )
            }

            // ── Section 3: Canine Bark Simulator (Interactive testing) ──
            item {
                BarkSimulatorSection(
                    onSimulate = { mood -> viewModel.simulateBarkEpisode(mood) }
                )
            }

            // ── Section 4: Anti-Anxiety Acoustic Sound Therapy ──
            item {
                SoothingTherapySection(
                    isPlaying = isTherapyPlaying,
                    activeTrack = activeTrack,
                    onPlayTrack = { track -> viewModel.startSoothingTrack(track) },
                    onStopTrack = { viewModel.stopSoothingTrack() }
                )
            }

            // ── Section 5: Recent Acoustic Events History ──
            item {
                Text(
                    text = "Acoustic Mood History",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = SlateLight
                    )
                )
                Spacer(modifier = Modifier.height(10.dp))
            }

            items(recentEpisodes, key = { it.id }) { ep ->
                AcousticHistoryItem(episode = ep)
            }
        }
    }
}

// ── Components ─────────────────────────────────────────────────────────────

@Composable
fun SpectrogramCard(
    isListening: Boolean,
    decibels: Int,
    amplitudes: List<Float>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = CardDark),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.GraphicEq,
                        contentDescription = null,
                        tint = Secondary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Real-time Acoustic Spectrum",
                        style = MaterialTheme.typography.labelLarge.copy(
                            color = SlateLight,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                Surface(
                    color = if (decibels > 75) Danger.copy(alpha = 0.2f) else Secondary.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "$decibels dB SPL",
                        color = if (decibels > 75) Danger else Secondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Spectrogram Bar Waveform
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(BackgroundDark.copy(alpha = 0.7f))
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                val barCount = amplitudes.size.coerceAtLeast(1)
                val spacing = 8f
                val availableWidth = size.width - (spacing * (barCount - 1))
                val barWidth = (availableWidth / barCount).coerceAtLeast(4f)
                val maxHeight = size.height

                amplitudes.forEachIndexed { index, amp ->
                    val barHeight = (amp * maxHeight).coerceIn(10f, maxHeight)
                    val x = index * (barWidth + spacing)
                    val y = maxHeight - barHeight

                    val barColor = when {
                        amp > 0.75f -> Color(0xFFFB7185) // Danger
                        amp > 0.45f -> Color(0xFF0EA5E9) // Sky Blue
                        else -> Color(0xFF10B981) // Emerald
                    }

                    drawRoundRect(
                        color = barColor,
                        topLeft = Offset(x, y),
                        size = Size(barWidth, barHeight),
                        cornerRadius = CornerRadius(4f, 4f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (isListening) "Sampling 44.1kHz • FFT 24-band" else "Microphone Standby",
                    fontSize = 11.sp,
                    color = SlateSubtle
                )
                Text(
                    text = when {
                        decibels < 50 -> "Ambient Room"
                        decibels < 70 -> "Subtle Vocalization"
                        else -> "Vocal Bark Episode!"
                    },
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (decibels > 70) Danger else SlateSubtle
                )
            }
        }
    }
}

@Composable
fun EmotionalTriageHeroCard(
    episode: BarkEpisode,
    collarBpm: Int
) {
    val mood = episode.moodState
    val moodColor = Color(mood.colorHex)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = CardDark),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, moodColor.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = moodColor.copy(alpha = 0.2f),
                        shape = CircleShape,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(text = mood.emoji, fontSize = 24.sp)
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = mood.displayName,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = SlateLight
                            )
                        )
                        Text(
                            text = "AI Confidence ${(episode.confidenceScore * 100).toInt()}%",
                            fontSize = 12.sp,
                            color = moodColor,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Surface(
                    color = Color.White.copy(alpha = 0.06f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Sensors,
                            contentDescription = null,
                            tint = Warning,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "$collarBpm BPM",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = SlateLight
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Clinical description
            Text(
                text = mood.description,
                style = MaterialTheme.typography.bodyMedium.copy(color = SlateLight.copy(alpha = 0.9f))
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Behavioral Guidance Box
            Surface(
                color = BackgroundDark.copy(alpha = 0.6f),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, moodColor.copy(alpha = 0.25f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Psychology,
                            contentDescription = null,
                            tint = moodColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "AI Canine Behaviorist Advice",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = moodColor
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = mood.advice,
                        fontSize = 12.sp,
                        color = SlateLight
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Stress Level Bar & Pitch
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Physiological Stress Index: ${episode.stressIndex}/100",
                    fontSize = 11.sp,
                    color = SlateSubtle,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Pitch: ${episode.fundamentalPitchHz} Hz",
                    fontSize = 11.sp,
                    color = SlateSubtle
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            LinearProgressIndicator(
                progress = { episode.stressIndex / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = moodColor,
                trackColor = Color.White.copy(alpha = 0.08f)
            )
        }
    }
}

@Composable
fun BarkSimulatorSection(
    onSimulate: (CanineMoodState) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CardDark),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Hearing,
                    contentDescription = null,
                    tint = Warning,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Simulate Bark Episode",
                    style = MaterialTheme.typography.labelLarge.copy(
                        color = SlateLight,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
            Text(
                text = "Trigger audio burst to test AI mood triage & collar sync",
                fontSize = 11.sp,
                color = SlateSubtle
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SimulatorButton(
                    modifier = Modifier.weight(1f),
                    label = "Alert",
                    emoji = "🚨",
                    color = Warning,
                    onClick = { onSimulate(CanineMoodState.ALERT) }
                )
                SimulatorButton(
                    modifier = Modifier.weight(1f),
                    label = "Anxiety",
                    emoji = "🥺",
                    color = Danger,
                    onClick = { onSimulate(CanineMoodState.ANXIETY) }
                )
                SimulatorButton(
                    modifier = Modifier.weight(1f),
                    label = "Playful",
                    emoji = "🎾",
                    color = Secondary,
                    onClick = { onSimulate(CanineMoodState.PLAYFUL) }
                )
                SimulatorButton(
                    modifier = Modifier.weight(1f),
                    label = "Pain",
                    emoji = "🩹",
                    color = Color(0xFFEF4444),
                    onClick = { onSimulate(CanineMoodState.PAIN) }
                )
            }
        }
    }
}

@Composable
fun SimulatorButton(
    modifier: Modifier = Modifier,
    label: String,
    emoji: String,
    color: Color,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .clickable(onClick = onClick)
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, color.copy(alpha = 0.4f), RoundedCornerShape(12.dp)),
        color = color.copy(alpha = 0.12f)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = emoji, fontSize = 20.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@Composable
fun SoothingTherapySection(
    isPlaying: Boolean,
    activeTrack: SoothingAudioTrack?,
    onPlayTrack: (SoothingAudioTrack) -> Unit,
    onStopTrack: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CardDark),
        border = androidx.compose.foundation.BorderStroke(1.dp, Primary.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Spa,
                        contentDescription = null,
                        tint = Primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Anti-Anxiety Sound Therapy",
                        style = MaterialTheme.typography.labelLarge.copy(
                            color = SlateLight,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                if (isPlaying) {
                    Surface(
                        modifier = Modifier.clickable(onClick = onStopTrack),
                        color = Danger.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Stop,
                                contentDescription = "Stop",
                                tint = Danger,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "STOP",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Danger
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                AVAILABLE_SOOTHING_TRACKS.forEach { track ->
                    val isSelected = activeTrack?.id == track.id && isPlaying
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (isSelected) onStopTrack() else onPlayTrack(track)
                            }
                            .clip(RoundedCornerShape(14.dp))
                            .border(
                                1.dp,
                                if (isSelected) Primary else Color.White.copy(alpha = 0.08f),
                                RoundedCornerShape(14.dp)
                            ),
                        color = if (isSelected) Primary.copy(alpha = 0.15f) else BackgroundDark.copy(alpha = 0.5f)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = track.iconEmoji, fontSize = 24.sp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = track.title,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Primary else SlateLight
                                )
                                Text(
                                    text = "${track.frequencyLabel} • ${track.category}",
                                    fontSize = 11.sp,
                                    color = SlateSubtle
                                )
                            }
                            Icon(
                                imageVector = if (isSelected) Icons.Default.Stop else Icons.Default.PlayArrow,
                                contentDescription = if (isSelected) "Stop" else "Play",
                                tint = if (isSelected) Primary else SlateSubtle,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AcousticHistoryItem(episode: BarkEpisode) {
    val mood = episode.moodState
    val moodColor = Color(mood.colorHex)
    val timeStr = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(episode.timestamp))

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .border(1.dp, Color.White.copy(alpha = 0.06f), RoundedCornerShape(14.dp)),
        color = CardDark.copy(alpha = 0.7f)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = moodColor.copy(alpha = 0.18f),
                shape = CircleShape,
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(text = mood.emoji, fontSize = 18.sp)
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = mood.displayName,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = SlateLight
                    )
                    Text(
                        text = timeStr,
                        fontSize = 11.sp,
                        color = SlateSubtle
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${episode.peakDecibels} dB • Stress Index: ${episode.stressIndex}% • ${episode.collarHeartRateBpm} BPM",
                    fontSize = 11.sp,
                    color = SlateSubtle
                )
            }
        }
    }
}
