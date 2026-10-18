package com.pawlife.app.feature.vet.presentation

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Event
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.filled.VideoCall
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pawlife.app.core.data.db.entity.VetAppointmentEntity
import com.pawlife.app.core.domain.repository.VetAppointmentRepository
import com.pawlife.app.core.util.CalendarSyncHelper
import com.pawlife.app.presentation.theme.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class VetBookingViewModel @Inject constructor(
    private val repository: VetAppointmentRepository,
    private val calendarSyncHelper: CalendarSyncHelper
) : ViewModel() {

    val appointments: StateFlow<List<VetAppointmentEntity>> = repository.getAppointmentsForPet(1L)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun bookAppointment(clinic: String, purpose: String) {
        viewModelScope.launch {
            val appointmentTime = System.currentTimeMillis() + 86400000L // Tomorrow
            
            // Sync to native calendar
            val calendarId = calendarSyncHelper.insertEvent(
                title = "Vet Appointment: $purpose",
                description = "PawLife automatically synced appointment at $clinic",
                location = clinic,
                startTimeMs = appointmentTime,
                endTimeMs = appointmentTime + 3600000L // +1 hour duration
            )

            repository.insertAppointment(
                VetAppointmentEntity(
                    petId = 1L,
                    vetName = "PawLife Registered Vet",
                    clinicName = clinic,
                    clinicAddress = "123 Main St",
                    clinicPhone = "555-0199",
                    appointmentTime = appointmentTime,
                    purpose = purpose,
                    calendarEventId = calendarId?.toString(),
                    notes = ""
                )
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VetBookingScreen(
    viewModel: VetBookingViewModel = hiltViewModel()
) {
    val appointments by viewModel.appointments.collectAsState()
    val context = LocalContext.current
    var showBookingDialog by remember { mutableStateOf(false) }
    var showVideoCallDialog by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.WRITE_CALENDAR] == true &&
                      permissions[Manifest.permission.READ_CALENDAR] == true
        if (granted) {
            showBookingDialog = true
        } else {
            Toast.makeText(context, "Calendar permissions are required to sync appointments.", Toast.LENGTH_LONG).show()
        }
    }

    Scaffold(
        containerColor = BackgroundDark,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Vet Finder", style = MaterialTheme.typography.displaySmall, color = SlateLight, fontWeight = FontWeight.Bold)
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    val writeGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_CALENDAR) == PackageManager.PERMISSION_GRANTED
                    val readGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALENDAR) == PackageManager.PERMISSION_GRANTED
                    if (writeGranted && readGranted) {
                        showBookingDialog = true
                    } else {
                        permissionLauncher.launch(arrayOf(Manifest.permission.READ_CALENDAR, Manifest.permission.WRITE_CALENDAR))
                    }
                },
                containerColor = Primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Book", tint = Color.White)
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            // Tele-Vet Banner
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp)
                    .premiumShadow(Primary, 0.2f, 24.dp, 12.dp)
                    .bounceClick {
                        showVideoCallDialog = true
                    },
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Primary)
            ) {
                Row(
                    modifier = Modifier.padding(24.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Tele-Vet Live", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Connect with a certified vet via video call right now.", style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.8f))
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Box(modifier = Modifier.size(56.dp).background(Color.White.copy(alpha = 0.2f), CircleShape), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.VideoCall, contentDescription = "Video Call", tint = Color.White, modifier = Modifier.size(32.dp))
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text("Upcoming Visits", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = SlateLight, modifier = Modifier.padding(horizontal = 24.dp))
            Spacer(modifier = Modifier.height(16.dp))

            if (appointments.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Event, contentDescription = null, tint = SlateSubtle, modifier = Modifier.size(64.dp).padding(bottom = 16.dp))
                        Text("No upcoming appointments.", color = SlateLight, style = MaterialTheme.typography.titleLarge)
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(start = 24.dp, end = 24.dp, bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                items(appointments) { appt ->
                    Card(
                        modifier = Modifier.fillMaxWidth().premiumShadow(Primary, 0.05f, 20.dp, 10.dp).clip(RoundedCornerShape(20.dp)),
                        colors = CardDefaults.cardColors(containerColor = CardDark.copy(alpha = 0.8f)),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Column(Modifier.padding(20.dp)) {
                            Text(appt.clinicName, style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(4.dp))
                            Text("Purpose: ${appt.purpose}", style = MaterialTheme.typography.bodyMedium, color = SlateLight)
                            Spacer(Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val dateStr = SimpleDateFormat("MMM d, yyyy • h:mm a", Locale.getDefault()).format(Date(appt.appointmentTime))
                                Text(dateStr, style = MaterialTheme.typography.labelMedium, color = SlateSubtle)
                                
                                val syncText = if (appt.calendarEventId != null) "✓ Synced" else "No Sync"
                                val syncColor = if (appt.calendarEventId != null) Success else Warning
                                Text(syncText, style = MaterialTheme.typography.labelSmall, color = syncColor, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

    if (showBookingDialog) {
        var expanded by remember { mutableStateOf(false) }
        val clinics = listOf("Chennai Pet Clinic - Adyar", "Anna Nagar Vet Care", "Happy Paws - T Nagar", "Blue Cross - Guindy")
        var selectedClinic by remember { mutableStateOf(clinics[0]) }
        var purpose by remember { mutableStateOf("") }
        
        AlertDialog(
            onDismissRequest = { showBookingDialog = false },
            containerColor = CardDark,
            title = { Text("Book Vet Appointment (Chennai)", color = SlateLight) },
            text = {
                Column {
                    Box {
                        OutlinedTextField(
                            value = selectedClinic,
                            onValueChange = { },
                            readOnly = true,
                            label = { Text("Nearby Clinic", color = SlateSubtle) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = SlateLight
                            ),
                            trailingIcon = {
                                Icon(Icons.Default.ArrowDropDown, contentDescription = "Select")
                            }
                        )
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .background(Color.Transparent)
                                .clickable { expanded = true }
                        )
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                            modifier = Modifier.background(CardDark)
                        ) {
                            clinics.forEach { clinic ->
                                DropdownMenuItem(
                                    text = { Text(clinic, color = SlateLight) },
                                    onClick = {
                                        selectedClinic = clinic
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = purpose,
                        onValueChange = { purpose = it },
                        label = { Text("Purpose (e.g. Vaccination)", color = SlateSubtle) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = SlateLight
                        )
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.bookAppointment(selectedClinic, purpose)
                    showBookingDialog = false
                    Toast.makeText(context, "Booked and synced to calendar!", Toast.LENGTH_SHORT).show()
                }) {
                    Text("Book & Sync", color = Primary, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showBookingDialog = false }) { Text("Cancel", color = SlateSubtle) }
            }
        )
    }

    if (showVideoCallDialog) {
        TeleVetVideoCallDialog(
            petName = "Max",
            onDismiss = { showVideoCallDialog = false }
        )
    }
}

