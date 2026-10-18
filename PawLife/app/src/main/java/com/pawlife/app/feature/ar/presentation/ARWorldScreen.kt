package com.pawlife.app.feature.ar.presentation

import android.Manifest
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Hearing
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.ViewInAr
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.pawlife.app.feature.ar.domain.AgilityObstacleType
import com.pawlife.app.feature.ar.domain.CanineTrick
import com.pawlife.app.feature.ar.domain.PlacedObstacle
import com.pawlife.app.feature.ar.domain.TrickDifficulty
import com.pawlife.app.presentation.theme.BackgroundDark
import com.pawlife.app.presentation.theme.CardDark
import com.pawlife.app.presentation.theme.Primary
import com.pawlife.app.presentation.theme.Secondary
import com.pawlife.app.presentation.theme.SlateLight
import com.pawlife.app.presentation.theme.SlateSubtle
import com.pawlife.app.presentation.theme.Warning
import io.github.sceneview.ar.ARScene
import io.github.sceneview.ar.node.ArModelNode
import io.github.sceneview.ar.node.ArNode
import io.github.sceneview.ar.node.PlacementMode
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ARWorldScreen(
    viewModel: PawARViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val selectedSubTab by viewModel.selectedSubTab.collectAsStateWithLifecycle()
    val totalXp by viewModel.totalXp.collectAsStateWithLifecycle()
    val cameraPermissionState = rememberPermissionState(permission = Manifest.permission.CAMERA)

    LaunchedEffect(Unit) {
        viewModel.eventFlow.collectLatest { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        containerColor = BackgroundDark,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("PawAR Coach", fontWeight = FontWeight.Bold, color = SlateLight, fontSize = 20.sp)
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Primary.copy(alpha = 0.2f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Primary.copy(alpha = 0.4f))
                            ) {
                                Text(
                                    text = "$totalXp XP",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Primary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text("AR Agility Arena & Positive Clicker Trainer", fontSize = 11.sp, color = SlateSubtle)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundDark),
                actions = {
                    IconButton(onClick = { viewModel.finishSession() }) {
                        Icon(Icons.Default.CheckCircle, contentDescription = "Save Session", tint = Primary)
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.playClickerSound() },
                containerColor = Primary,
                contentColor = Color.Black,
                shape = CircleShape
            ) {
                Row(modifier = Modifier.padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Hearing, contentDescription = "Clicker")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("CLICK", fontWeight = FontWeight.Black, fontSize = 14.sp)
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Segmented Sub-Tab Switcher
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(CardDark)
                    .border(1.dp, Color(0xFF334155), RoundedCornerShape(16.dp))
                    .padding(4.dp)
            ) {
                listOf("Agility Arena", "Trick Coach", "Mastery & XP").forEachIndexed { index, label ->
                    val isSelected = selectedSubTab == index
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) Primary else Color.Transparent)
                            .clickable { viewModel.setSelectedSubTab(index) }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color.Black else SlateSubtle
                        )
                    }
                }
            }

            // Tab Content
            when (selectedSubTab) {
                0 -> AgilityArenaTab(
                    viewModel = viewModel,
                    hasCameraPermission = cameraPermissionState.status.isGranted,
                    onRequestPermission = { cameraPermissionState.launchPermissionRequest() }
                )
                1 -> TrickCoachTab(viewModel = viewModel)
                2 -> AgilityMasteryTab(viewModel = viewModel)
            }
        }
    }
}

// ── SUBTAB 1: AR AGILITY ARENA ────────────────────────────────────────────────
@Composable
fun AgilityArenaTab(
    viewModel: PawARViewModel,
    hasCameraPermission: Boolean,
    onRequestPermission: () -> Unit
) {
    val selectedObstacleType by viewModel.selectedObstacleType.collectAsStateWithLifecycle()
    val placedObstacles by viewModel.placedObstacles.collectAsStateWithLifecycle()
    var isLiveArCameraMode by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // Obstacle selector bar
        Text("Select Obstacle to Place:", fontSize = 12.sp, color = SlateSubtle, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(8.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(AgilityObstacleType.entries) { type ->
                val isSelected = selectedObstacleType == type
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) Secondary.copy(alpha = 0.2f) else CardDark)
                        .border(1.dp, if (isSelected) Secondary else Color(0xFF334155), RoundedCornerShape(12.dp))
                        .clickable { viewModel.setSelectedObstacleType(type) }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(type.iconEmoji, fontSize = 16.sp)
                        Text(
                            text = type.displayName,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) Secondary else SlateLight
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Arena Canvas / AR Viewport
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFF0F172A))
                .border(2.dp, Brush.verticalGradient(listOf(Primary.copy(alpha = 0.5f), Color(0xFF334155))), RoundedCornerShape(20.dp))
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        val normX = offset.x / size.width
                        val normY = offset.y / size.height
                        viewModel.placeObstacle(normX, normY)
                    }
                }
        ) {
            if (isLiveArCameraMode && hasCameraPermission) {
                // Live AR sceneview viewport
                ARSceneScreenInline()
            } else {
                // High-Fidelity 2D/3D Agility Simulation Grid
                Box(modifier = Modifier.fillMaxSize()) {
                    // Grid lines aesthetic
                    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceEvenly) {
                        repeat(5) {
                            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color.White.copy(alpha = 0.05f)))
                        }
                    }

                    // Virtual dog footprint indicator
                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(36.dp)
                            .background(Primary.copy(alpha = 0.15f), CircleShape)
                            .border(1.dp, Primary.copy(alpha = 0.3f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🐕", fontSize = 18.sp)
                    }

                    // Placed obstacles rendered on arena
                    placedObstacles.forEach { obs ->
                        val cleared = obs.isCleared
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(
                                    start = (obs.xPos * 300).dp,
                                    top = (obs.yPos * 360).dp
                                )
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (cleared) Primary.copy(alpha = 0.3f) else CardDark)
                                .border(1.5.dp, if (cleared) Primary else Warning, RoundedCornerShape(12.dp))
                                .clickable { viewModel.toggleClearObstacle(obs.id) }
                                .padding(8.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(obs.type.iconEmoji, fontSize = 20.sp)
                                Text(
                                    text = if (cleared) "CLEARED ✓" else "${obs.type.xpValue} XP",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (cleared) Primary else Warning
                                )
                            }
                        }
                    }

                    // Bottom instruction pill
                    Surface(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 12.dp),
                        shape = RoundedCornerShape(14.dp),
                        color = Color.Black.copy(alpha = 0.7f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
                    ) {
                        Text(
                            text = "Tap floor to place obstacle • Tap placed item to clear! (${placedObstacles.size} items)",
                            fontSize = 11.sp,
                            color = SlateLight,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            // Top right AR Camera mode toggle button
            Row(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isLiveArCameraMode) Primary else Color.Black.copy(alpha = 0.6f),
                    modifier = Modifier.clickable {
                        if (!hasCameraPermission) onRequestPermission()
                        isLiveArCameraMode = !isLiveArCameraMode
                    }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            Icons.Default.ViewInAr,
                            contentDescription = null,
                            tint = if (isLiveArCameraMode) Color.Black else Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = if (isLiveArCameraMode) "AR Live" else "AR Mode",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isLiveArCameraMode) Color.Black else Color.White
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color.Black.copy(alpha = 0.6f),
                    modifier = Modifier.clickable { viewModel.clearAllObstacles() }
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Clear All",
                        tint = SlateSubtle,
                        modifier = Modifier.padding(6.dp).size(18.dp)
                    )
                }
            }
        }
    }
}

// ── SUBTAB 2: TRICK COACH ─────────────────────────────────────────────────────
@Composable
fun TrickCoachTab(viewModel: PawARViewModel) {
    val tricks by viewModel.tricks.collectAsStateWithLifecycle()
    val activeTrickId by viewModel.activeTrickId.collectAsStateWithLifecycle()
    val timerSeconds by viewModel.timerSeconds.collectAsStateWithLifecycle()
    val isTimerRunning by viewModel.isTimerRunning.collectAsStateWithLifecycle()

    val activeTrick = tricks.find { it.id == activeTrickId } ?: tricks.first()

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Training Timer & Clicker Controls Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = CardDark)
            ) {
                Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.Timer, contentDescription = null, tint = Secondary, modifier = Modifier.size(20.dp))
                            val minutes = timerSeconds / 60
                            val seconds = timerSeconds % 60
                            Text(
                                text = String.format("%02d:%02d", minutes, seconds),
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = SlateLight
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            IconButton(onClick = { if (isTimerRunning) viewModel.pauseTimer() else viewModel.startTimer() }) {
                                Icon(
                                    if (isTimerRunning) Icons.Default.Stop else Icons.Default.PlayArrow,
                                    contentDescription = "Timer Toggle",
                                    tint = Primary
                                )
                            }
                            IconButton(onClick = { viewModel.resetTimer() }) {
                                Icon(Icons.Default.Refresh, contentDescription = "Reset Timer", tint = SlateSubtle)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Big Clicker Trigger Button
                    Button(
                        onClick = { viewModel.playClickerSound(activeTrick.id) },
                        colors = ButtonDefaults.buttonColors(containerColor = Primary, contentColor = Color.Black),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth().height(56.dp)
                    ) {
                        Icon(Icons.Default.Hearing, contentDescription = null, modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("MARK BEHAVIOR & CLICK (+${activeTrick.xpReward} XP)", fontWeight = FontWeight.Black, fontSize = 14.sp)
                    }
                }
            }
        }

        // Active Trick Guide Focus
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, Primary.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(activeTrick.iconEmoji, fontSize = 28.sp)
                            Column {
                                Text(activeTrick.name, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = SlateLight)
                                Text("Verbal Cue: \"${activeTrick.cuePhrase}\"", fontSize = 12.sp, color = Secondary, fontWeight = FontWeight.SemiBold)
                            }
                        }
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (activeTrick.isMastered) Primary.copy(alpha = 0.2f) else Warning.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = if (activeTrick.isMastered) "MASTERED ✓" else "${activeTrick.repsCompleted}/10 Reps",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (activeTrick.isMastered) Primary else Warning,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Text("Step-by-Step Training Guide:", fontSize = 12.sp, color = SlateSubtle, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(8.dp))

                    activeTrick.stepByStepInstructions.forEachIndexed { i, step ->
                        Row(modifier = Modifier.padding(vertical = 3.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("${i + 1}.", fontSize = 13.sp, color = Primary, fontWeight = FontWeight.Bold)
                            Text(step, fontSize = 13.sp, color = SlateLight, lineHeight = 18.sp)
                        }
                    }
                }
            }
        }

        // Other Tricks Curriculum List
        item {
            Text("Complete Trick Curriculum:", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = SlateLight)
        }

        items(tricks) { trick ->
            val isCurrent = trick.id == activeTrickId
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.setActiveTrick(trick.id) },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = if (isCurrent) Color(0xFF1E293B) else CardDark),
                border = if (isCurrent) androidx.compose.foundation.BorderStroke(1.dp, Primary) else null
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(trick.iconEmoji, fontSize = 24.sp)
                        Column {
                            Text(trick.name, fontWeight = FontWeight.Bold, color = SlateLight, fontSize = 15.sp)
                            Text("${trick.difficulty.label} • Cue: \"${trick.cuePhrase}\"", fontSize = 12.sp, color = SlateSubtle)
                        }
                    }
                    Text(
                        text = if (trick.isMastered) "Mastered ✓" else "${trick.repsCompleted}/10",
                        color = if (trick.isMastered) Primary else SlateSubtle,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

// ── SUBTAB 3: MASTERY & XP ────────────────────────────────────────────────────
@Composable
fun AgilityMasteryTab(viewModel: PawARViewModel) {
    val totalXp by viewModel.totalXp.collectAsStateWithLifecycle()
    val mastery by viewModel.masteryLevel.collectAsStateWithLifecycle()
    val sessions by viewModel.recentSessions.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Mastery Level Hero Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = CardDark),
                border = androidx.compose.foundation.BorderStroke(2.dp, Brush.horizontalGradient(listOf(Primary, Secondary)))
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("AGILITY RANK", fontSize = 11.sp, fontWeight = FontWeight.Black, color = Primary, letterSpacing = 1.sp)
                            Text(mastery.title, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = SlateLight)
                        }
                        Text(mastery.badgeIcon, fontSize = 42.sp)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Progress Bar
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("$totalXp XP", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Primary)
                        Text("${mastery.targetXp} XP to Level ${mastery.level + 1}", fontSize = 12.sp, color = SlateSubtle)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { mastery.progressPercent },
                        modifier = Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(5.dp)),
                        color = Primary,
                        trackColor = Color(0xFF334155)
                    )
                }
            }
        }

        // Trophies / Badges Shelf
        item {
            Text("Agility Badges & Trophies:", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = SlateLight)
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                listOf(
                    Triple("🥉", "Bronze Paw", totalXp >= 100),
                    Triple("🥈", "Silver Agility", totalXp >= 300),
                    Triple("🥇", "Gold Champion", totalXp >= 600),
                    Triple("🏆", "Diamond Cup", totalXp >= 1200)
                ).forEach { (icon, name, unlocked) ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (unlocked) CardDark else Color(0xFF1E293B).copy(alpha = 0.5f))
                            .border(1.dp, if (unlocked) Primary.copy(alpha = 0.5f) else Color.Transparent, RoundedCornerShape(12.dp))
                            .padding(10.dp)
                    ) {
                        Text(icon, fontSize = 26.sp, modifier = Modifier.scale(if (unlocked) 1f else 0.7f))
                        Text(name, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (unlocked) SlateLight else SlateSubtle)
                        Text(if (unlocked) "Unlocked" else "Locked", fontSize = 9.sp, color = if (unlocked) Primary else SlateSubtle)
                    }
                }
            }
        }

        // Recent Agility Sessions
        item {
            Text("Recent Training Sessions:", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = SlateLight)
        }

        items(sessions) { session ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardDark)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Agility & Trick Practice", fontWeight = FontWeight.Bold, color = SlateLight, fontSize = 14.sp)
                        Text(
                            text = "${session.obstaclesCleared} Obstacles • ${session.repsCount} Click Reps • ${session.durationSeconds / 60}m",
                            fontSize = 12.sp,
                            color = SlateSubtle
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Primary.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "+${session.xpEarned} XP",
                            fontWeight = FontWeight.Bold,
                            color = Primary,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

// ── INLINE AR SCENE ───────────────────────────────────────────────────────────
@Composable
fun ARSceneScreenInline() {
    val nodes = remember { mutableStateListOf<ArNode>() }
    val modelNode = remember {
        ArModelNode(placementMode = PlacementMode.BEST_AVAILABLE).apply {
            loadModelGlbAsync(
                glbFileLocation = "https://storage.googleapis.com/ar-answers-in-search-models/static/GoldenRetriever/model.glb",
                autoAnimate = true,
                scaleToUnits = 0.5f
            )
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        ARScene(
            modifier = Modifier.fillMaxSize(),
            nodes = nodes,
            planeRenderer = true,
            onTap = { hitResult ->
                if (!nodes.contains(modelNode)) {
                    modelNode.anchor = hitResult.createAnchor()
                    nodes.add(modelNode)
                }
            }
        )
    }
}
