package com.pawlife.app.feature.collar.presentation

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pawlife.app.presentation.theme.*
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollarScannerScreen(
    onNavigateBack: () -> Unit
) {
    var scanningState by remember { mutableStateOf("Scanning") } // Scanning, Found, Connected
    
    LaunchedEffect(Unit) {
        delay(3000)
        scanningState = "Found"
        delay(1500)
        scanningState = "Connected"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Smart Collar", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier.size(300.dp),
                contentAlignment = Alignment.Center
            ) {
                if (scanningState == "Scanning") {
                    RadarAnimation()
                }

                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .background(CardDark, CircleShape)
                        .border(2.dp, if (scanningState == "Connected") Success else Primary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (scanningState == "Connected") Icons.Default.CheckCircle else Icons.Default.Bluetooth,
                        contentDescription = "Bluetooth",
                        tint = if (scanningState == "Connected") Success else Primary,
                        modifier = Modifier.size(48.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(48.dp))
            
            Text(
                text = when(scanningState) {
                    "Scanning" -> "Looking for PawLife Collar..."
                    "Found" -> "Found PawLife Collar (BLE_89X)"
                    "Connected" -> "Connected Successfully!"
                    else -> ""
                },
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = SlateLight
            )
            
            if (scanningState == "Scanning") {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Make sure the collar is turned on and within range.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SlateSubtle
                )
            }
            
            if (scanningState == "Connected") {
                Spacer(modifier = Modifier.height(48.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = CardDark)
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Text("Live Stats", style = MaterialTheme.typography.titleMedium, color = SlateLight, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            StatItem("Heart Rate", "84 bpm", Primary)
                            StatItem("Activity", "Active", Secondary)
                            StatItem("Battery", "89%", Success)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatItem(label: String, value: String, color: Color) {
    Column {
        Text(label, style = MaterialTheme.typography.labelMedium, color = SlateSubtle)
        Text(value, style = MaterialTheme.typography.titleMedium, color = color, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun RadarAnimation() {
    val infiniteTransition = rememberInfiniteTransition(label = "radar")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = "radarScale"
    )
    val alpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = "radarAlpha"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .scale(scale)
            .background(Primary.copy(alpha = alpha * 0.5f), CircleShape)
            .border(1.dp, Primary.copy(alpha = alpha), CircleShape)
    )
}
