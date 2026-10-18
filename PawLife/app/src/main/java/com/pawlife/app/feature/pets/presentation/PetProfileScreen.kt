package com.pawlife.app.feature.pets.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import android.widget.Toast
import androidx.compose.ui.Alignment
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pawlife.app.feature.weight.presentation.WeightChartSection
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import com.pawlife.app.core.utils.ReminderManager
import android.os.Build
import coil.compose.AsyncImage
import com.pawlife.app.presentation.theme.Primary
import com.pawlife.app.presentation.theme.Secondary
import com.pawlife.app.presentation.theme.Success
import com.pawlife.app.presentation.theme.Warning
import com.pawlife.app.presentation.theme.CardDark
import com.pawlife.app.presentation.theme.BackgroundDark
import com.pawlife.app.presentation.theme.SlateLight
import com.pawlife.app.presentation.theme.SlateSubtle
import com.pawlife.app.presentation.theme.premiumShadow

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PetProfileScreen(
    viewModel: PetProfileViewModel = hiltViewModel(),
    onNavigateToActiveWalk: (Long) -> Unit = {},
    onNavigateToVault: (Long) -> Unit = {}
) {
    val pet by viewModel.pet.collectAsStateWithLifecycle()
    val weightRecords by viewModel.weightRecords.collectAsStateWithLifecycle()
    val walkRecords by viewModel.walkRecords.collectAsStateWithLifecycle()
    val context = LocalContext.current

    if (pet == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Primary)
        }
        return
    }

    val currentPet = pet!!
    var showWeightDialog by remember { mutableStateOf(false) }
    var inputWeight by remember { mutableStateOf("") }
    var inputNotes by remember { mutableStateOf("") }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null) {
                viewModel.updatePetPhoto(uri.toString())
            }
        }
    )

    Box(modifier = Modifier.fillMaxSize().background(BackgroundDark)) {
        Box(modifier = Modifier.fillMaxWidth().height(420.dp)) {
            AsyncImage(
                model = currentPet.photoUri ?: "https://images.unsplash.com/photo-1552053831-71594a27632d?auto=format&fit=crop&w=800&q=80",
                contentDescription = "Pet Image",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            
            Box(
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.TopEnd)
                    .padding(top = 32.dp) // Push down slightly for status bar
                    .size(48.dp)
                    .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                    .clickable {
                        photoPickerLauncher.launch(
                            androidx.activity.result.PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Edit, contentDescription = "Edit Photo", tint = Color.White)
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(420.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(BackgroundDark.copy(alpha = 0.4f), BackgroundDark),
                        startY = 50f
                    )
                )
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(280.dp))
                
                Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                    Text(currentPet.name, style = MaterialTheme.typography.displayMedium, color = SlateLight, fontWeight = FontWeight.Bold)
                    
                    // Simple age calculation for demo
                    val ageYears = ((System.currentTimeMillis() - currentPet.dateOfBirth) / 31556952000L).coerceAtLeast(1)
                    Text("${currentPet.breed} • $ageYears Years", style = MaterialTheme.typography.titleLarge, color = SlateSubtle)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .background(Success.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("😊", style = MaterialTheme.typography.labelLarge)
                        Text("Happy", style = MaterialTheme.typography.labelLarge, color = Success, fontWeight = FontWeight.Bold)
                    }
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(32.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        GlassStatCard("Weight", "${currentPet.weight} kg", Icons.Default.MonitorWeight, Primary)
                        GlassStatCard("Activity", "High", Icons.Default.DirectionsRun, Secondary)
                    }
                    
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(140.dp)
                            .background(CardDark.copy(alpha = 0.5f), CircleShape)
                            .premiumShadow(Success, 0.15f, 70.dp, 32.dp)
                    ) {
                        CircularProgressIndicator(
                            progress = { 0.92f },
                            modifier = Modifier.fillMaxSize().padding(8.dp),
                            color = Success,
                            trackColor = CardDark,
                            strokeWidth = 8.dp
                        )
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("92%", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold, color = Success)
                            Text("Healthy", style = MaterialTheme.typography.labelLarge, color = SlateSubtle)
                        }
                    }
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(32.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Weight History", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = SlateLight)
                    Button(
                        onClick = {
                            inputWeight = if (currentPet.weight > 0f) currentPet.weight.toString() else "10.0"
                            inputNotes = ""
                            showWeightDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Primary),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.Black)
                        Spacer(Modifier.width(4.dp))
                        Text("Log Weight", style = MaterialTheme.typography.labelMedium, color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .height(300.dp)
                        .background(CardDark, RoundedCornerShape(20.dp))
                        .padding(16.dp)
                ) {
                    if (weightRecords.isEmpty()) {
                        Text("No weight records yet. Tap 'Log Weight' to add.", color = SlateSubtle, modifier = Modifier.align(Alignment.Center))
                    } else {
                        WeightChartSection(records = weightRecords)
                    }
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(32.dp))
                Text("Activity History", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 24.dp))
                Spacer(modifier = Modifier.height(16.dp))
                
                if (walkRecords.isEmpty()) {
                    Text("No activities recorded.", color = SlateSubtle, modifier = Modifier.padding(horizontal = 24.dp))
                } else {
                    Column(
                        modifier = Modifier.padding(horizontal = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        walkRecords.take(3).forEach { record ->
                            val dateStr = java.text.SimpleDateFormat("MMM dd, yyyy HH:mm", java.util.Locale.getDefault()).format(java.util.Date(record.startTime))
                            val durMins = ((record.endTime - record.startTime) / 60000).coerceAtLeast(1)
                            Card(
                                modifier = Modifier.fillMaxWidth().premiumShadow(Primary, 0.05f, 20.dp, 10.dp).clip(RoundedCornerShape(16.dp)),
                                colors = CardDefaults.cardColors(containerColor = CardDark.copy(alpha = 0.8f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.DirectionsWalk, contentDescription = null, tint = Primary)
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Column {
                                        Text("$durMins min Walk", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
                                        Text(dateStr, style = MaterialTheme.typography.labelMedium, color = SlateSubtle)
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(32.dp))
                Text("Vaccination Timeline", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 24.dp))
                Spacer(modifier = Modifier.height(16.dp))
                TimelineItem("Rabies Booster", "Completed • 2 months ago", Success, true)
                TimelineItem("Parvovirus", "Due in 12 days", Warning, false)
            }
            
            item {
                Spacer(modifier = Modifier.height(32.dp))
                Text("Quick Actions", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 24.dp))
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    QuickActionButton("Vault", Secondary, Modifier.weight(1f)) {
                        onNavigateToVault(currentPet.id)
                    }
                    QuickActionButton("Start Walk", Success, Modifier.weight(1f)) {
                        onNavigateToActiveWalk(currentPet.id)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    val notificationPermissionState = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        rememberPermissionState(android.Manifest.permission.POST_NOTIFICATIONS)
                    } else null

                    QuickActionButton("Lost Pet Flyer", Warning, Modifier.weight(1f)) {
                        val pdfFile = FlyerGenerator.generateLostPetFlyer(context, currentPet)
                        com.pawlife.app.core.utils.ShareUtils.sharePdf(context, pdfFile)
                    }

                    QuickActionButton("Test Reminder", Primary, Modifier.weight(1f)) {
                        if (notificationPermissionState != null && !notificationPermissionState.status.isGranted) {
                            notificationPermissionState.launchPermissionRequest()
                        } else {
                            val reminderManager = ReminderManager(context)
                            reminderManager.scheduleMedicationReminder(currentPet.name, "Heartworm Pill", 5)
                        }
                    }
                }
            }
        }
    }

    if (showWeightDialog) {
        AlertDialog(
            onDismissRequest = { showWeightDialog = false },
            containerColor = CardDark,
            title = { Text("Log Weight for ${currentPet.name}", color = SlateLight, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    OutlinedTextField(
                        value = inputWeight,
                        onValueChange = { inputWeight = it },
                        label = { Text("Weight (kg)", color = SlateSubtle) },
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = SlateLight
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = inputNotes,
                        onValueChange = { inputNotes = it },
                        label = { Text("Notes (e.g. Monthly Weigh-In)", color = SlateSubtle) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = SlateLight
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val weightFloat = inputWeight.toFloatOrNull()
                    if (weightFloat != null && weightFloat > 0f) {
                        viewModel.addWeightRecord(weightFloat, inputNotes)
                        showWeightDialog = false
                        Toast.makeText(context, "Weight logged successfully!", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Please enter a valid weight in kg.", Toast.LENGTH_SHORT).show()
                    }
                }) {
                    Text("Save", color = Primary, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showWeightDialog = false }) {
                    Text("Cancel", color = SlateSubtle)
                }
            }
        )
    }
}

@Composable
fun GlassStatCard(label: String, value: String, icon: ImageVector, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.height(110.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CardDark.copy(alpha = 0.8f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp).fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.Start
        ) {
            Icon(icon, contentDescription = label, tint = color)
            Column {
                Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
                Text(label, style = MaterialTheme.typography.labelMedium, color = Color.Gray)
            }
        }
    }
}

@Composable
fun TimelineItem(title: String, subtitle: String, color: Color, isCompleted: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(16.dp)
                .background(if (isCompleted) color else CardDark, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (isCompleted) {
                Icon(Icons.Default.Check, contentDescription = "Done", tint = Color.White, modifier = Modifier.size(12.dp))
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = color)
        }
    }
}

@Composable
fun QuickActionButton(text: String, color: Color, modifier: Modifier = Modifier, onClick: () -> Unit = {}) {
    Button(
        onClick = onClick,
        modifier = modifier.height(56.dp),
        colors = ButtonDefaults.buttonColors(containerColor = color),
        shape = RoundedCornerShape(16.dp)
    ) {
        Text(text, fontWeight = FontWeight.Bold, color = Color.Black)
    }
}
