package com.pawlife.app.feature.passport.presentation

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.pawlife.app.feature.passport.domain.VaccinationRecord
import com.pawlife.app.presentation.theme.Primary
import com.pawlife.app.presentation.theme.Secondary
import com.pawlife.app.presentation.theme.Warning

@Composable
fun PetPassportScreen(
    onNavigateBack: () -> Unit,
    viewModel: PassportViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0B0F19))
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {
            // Header Bar
            item {
                Spacer(modifier = Modifier.height(48.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier
                            .size(42.dp)
                            .background(Color(0xFF1E293B), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "OFFICIAL PET PASSPORT",
                            color = Color(0xFFF1F5F9),
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Global Immunity & Microchip Credential",
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp
                        )
                    }

                    IconButton(
                        onClick = { viewModel.toggleQrModal(true) },
                        modifier = Modifier
                            .size(42.dp)
                            .background(Color(0x2010B981), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.QrCode,
                            contentDescription = "Show QR",
                            tint = Primary
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Official Passport Gold-Trim Card
            item {
                PassportCard(
                    passport = uiState.passport,
                    onShowQr = { viewModel.toggleQrModal(true) }
                )
                Spacer(modifier = Modifier.height(20.dp))
            }

            // Quick Immunity Badge Metrics
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricChip(
                        title = "Rabies Booster",
                        value = "Valid '27",
                        subtitle = "TN Registry",
                        color = Primary,
                        modifier = Modifier.weight(1f)
                    )
                    MetricChip(
                        title = "Microchip ID",
                        value = "ISO 11784",
                        subtitle = "15-Digit FDX",
                        color = Secondary,
                        modifier = Modifier.weight(1f)
                    )
                    MetricChip(
                        title = "Status",
                        value = "Certified",
                        subtitle = "Fully Immune",
                        color = Color(0xFF10B981),
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(28.dp))
            }

            // Vaccination Ledger Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Immunization Ledger",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Verified Veterinary Booster Records",
                            color = Color(0xFF94A3B8),
                            fontSize = 12.sp
                        )
                    }

                    Button(
                        onClick = { viewModel.toggleAddDialog(true) },
                        colors = ButtonDefaults.buttonColors(containerColor = Primary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Log Booster",
                            color = Color.Black,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // List of Vaccine Records
            items(uiState.vaccinations) { record ->
                VaccineRecordCard(record = record)
                Spacer(modifier = Modifier.height(12.dp))
            }

            item {
                Spacer(modifier = Modifier.height(40.dp))
            }
        }

        // Dialog: High-Contrast QR Code Pass
        if (uiState.showQrModal) {
            QrCodePassDialog(
                passport = uiState.passport,
                onDismiss = { viewModel.toggleQrModal(false) },
                onShare = {
                    val sendIntent: Intent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(
                            Intent.EXTRA_TEXT,
                            "Official Pet Health Passport for ${uiState.passport.petName}:\n${uiState.passport.publicVerificationUrl}\nMicrochip: ${uiState.passport.microchipId}"
                        )
                        type = "text/plain"
                    }
                    val shareIntent = Intent.createChooser(sendIntent, "Share Pet Passport Credential")
                    context.startActivity(shareIntent)
                },
                onOpenPortal = {
                    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(uiState.passport.publicVerificationUrl))
                    context.startActivity(browserIntent)
                }
            )
        }

        // Dialog: Add Vaccine Record
        if (uiState.showAddDialog) {
            AddVaccineDialog(
                onDismiss = { viewModel.toggleAddDialog(false) },
                onSave = { name, cat, batch, clinic, doc, adminDate, expDate ->
                    viewModel.addVaccination(name, cat, batch, clinic, doc, adminDate, expDate)
                }
            )
        }
    }
}

@Composable
fun PassportCard(
    passport: com.pawlife.app.feature.passport.domain.PetPassportInfo,
    onShowQr: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF1E293B), Color(0xFF0F172A))
                ),
                RoundedCornerShape(20.dp)
            )
            .border(
                1.5.dp,
                Brush.linearGradient(
                    colors = listOf(Color(0xFFF59E0B), Color(0xFF10B981), Color(0xFF0EA5E9))
                ),
                RoundedCornerShape(20.dp)
            )
            .padding(20.dp)
    ) {
        Column {
            // Card Top Row: Emblems & Passport Number
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color(0xFFF59E0B).copy(alpha = 0.2f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "🐾", fontSize = 18.sp)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "REPUBLIC OF PETS",
                            color = Color(0xFFF59E0B),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = passport.officialRegistry,
                            color = Color(0xFF94A3B8),
                            fontSize = 9.sp
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .background(Color(0xFF10B981).copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "VET VERIFIED",
                        color = Color(0xFF10B981),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Pet Photo & Info Columns
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Photo Frame
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(Color(0xFF334155), CircleShape)
                        .border(2.dp, Color(0xFFF59E0B), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "🐕", fontSize = 42.sp)
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = passport.petName,
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${passport.breed} • ${passport.species}",
                        color = Color(0xFFCBD5E1),
                        fontSize = 13.sp
                    )
                    Text(
                        text = "Born: ${passport.birthDate} • ${passport.gender}",
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                    Text(
                        text = "Owner: ${passport.guardianName} (${passport.guardianCity})",
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Microchip Barcode Section
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0B0F19), RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "MICROCHIP TRANSPONDER ID",
                            color = Color(0xFF64748B),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = passport.microchipStandard,
                            color = Primary,
                            fontSize = 9.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Barcode Canvas
                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(30.dp)
                    ) {
                        val barWidth = size.width / 45f
                        val pattern = listOf(3, 1, 2, 1, 3, 2, 1, 1, 3, 2, 1, 3, 1, 2, 3, 1, 1, 2, 3, 1, 2, 3, 1)
                        var currentX = 10f
                        for (w in pattern) {
                            drawRect(
                                color = Color.White,
                                topLeft = Offset(currentX, 0f),
                                size = Size(barWidth * w, size.height)
                            )
                            currentX += barWidth * (w + 1)
                            if (currentX >= size.width - 20f) break
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = passport.microchipId,
                        color = Color(0xFFF1F5F9),
                        fontSize = 13.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Bottom Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onShowQr,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Primary),
                    border = ButtonDefaults.outlinedButtonBorder.copy(brush = Brush.horizontalGradient(listOf(Primary, Secondary))),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.QrCode,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Scan QR Pass", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Box(
                    modifier = Modifier
                        .background(Color(0xFF334155), RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                        .align(Alignment.CenterVertically)
                ) {
                    Text(
                        text = passport.passportId,
                        color = Color(0xFFCBD5E1),
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
fun MetricChip(
    title: String,
    value: String,
    subtitle: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(Color(0xFF1E293B), RoundedCornerShape(14.dp))
            .border(1.dp, color.copy(alpha = 0.25f), RoundedCornerShape(14.dp))
            .padding(12.dp)
    ) {
        Column {
            Text(text = title, color = Color(0xFF94A3B8), fontSize = 10.sp)
            Text(
                text = value,
                color = color,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 2.dp)
            )
            Text(text = subtitle, color = Color(0xFF64748B), fontSize = 9.sp)
        }
    }
}

@Composable
fun VaccineRecordCard(record: VaccinationRecord) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF1E293B), RoundedCornerShape(16.dp))
            .border(1.dp, Color(0xFF334155), RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .background(Color(0x2010B981), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Verified,
                            contentDescription = null,
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = record.vaccineName,
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${record.category} • Lot: ${record.batchLotNumber}",
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .background(Color(0x2010B981), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = record.status,
                        color = Color(0xFF10B981),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Timestamps and Clinic
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0B0F19), RoundedCornerShape(10.dp))
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = "ADMINISTERED", color = Color(0xFF64748B), fontSize = 9.sp)
                    Text(
                        text = record.administeredDate,
                        color = Color(0xFFE2E8F0),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "VALID UNTIL", color = Color(0xFF64748B), fontSize = 9.sp)
                    Text(
                        text = record.expiryDate,
                        color = Primary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "PROVIDER", color = Color(0xFF64748B), fontSize = 9.sp)
                    Text(
                        text = record.clinicName.split(" ").take(2).joinToString(" "),
                        color = Color(0xFFE2E8F0),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
fun QrCodePassDialog(
    passport: com.pawlife.app.feature.passport.domain.PetPassportInfo,
    onDismiss: () -> Unit,
    onShare: () -> Unit,
    onOpenPortal: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White, RoundedCornerShape(24.dp))
                .padding(24.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "PawLife Verifiable Pass",
                        color = Color(0xFF0F172A),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = null, tint = Color.Black)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // High-Contrast QR Code Visual (Zero-Dependency Canvas QR)
                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .background(Color.White)
                        .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val cellSize = size.width / 21f
                        
                        // Draw corner finder patterns
                        fun drawFinder(cx: Float, cy: Float) {
                            // Outer 7x7
                            drawRoundRect(
                                color = Color.Black,
                                topLeft = Offset(cx, cy),
                                size = Size(cellSize * 7, cellSize * 7),
                                cornerRadius = CornerRadius(4f, 4f)
                            )
                            // White 5x5
                            drawRect(
                                color = Color.White,
                                topLeft = Offset(cx + cellSize, cy + cellSize),
                                size = Size(cellSize * 5, cellSize * 5)
                            )
                            // Inner 3x3
                            drawRoundRect(
                                color = Color.Black,
                                topLeft = Offset(cx + cellSize * 2, cy + cellSize * 2),
                                size = Size(cellSize * 3, cellSize * 3),
                                cornerRadius = CornerRadius(2f, 2f)
                            )
                        }

                        // Top-Left Finder
                        drawFinder(0f, 0f)
                        // Top-Right Finder
                        drawFinder(cellSize * 14, 0f)
                        // Bottom-Left Finder
                        drawFinder(0f, cellSize * 14)

                        // Data grid dots simulation
                        val dotCoords = listOf(
                            Pair(8, 0), Pair(9, 1), Pair(11, 2), Pair(12, 0), Pair(8, 4), Pair(10, 5),
                            Pair(0, 8), Pair(2, 9), Pair(4, 10), Pair(6, 8), Pair(8, 8), Pair(10, 8), Pair(12, 8),
                            Pair(9, 10), Pair(11, 11), Pair(13, 10), Pair(10, 12), Pair(11, 13), Pair(8, 14),
                            Pair(14, 8), Pair(16, 9), Pair(18, 8), Pair(20, 10), Pair(15, 12), Pair(17, 13),
                            Pair(8, 16), Pair(10, 17), Pair(12, 16), Pair(14, 18), Pair(16, 17), Pair(18, 19),
                            Pair(9, 19), Pair(11, 20), Pair(13, 19), Pair(15, 20), Pair(17, 20), Pair(19, 20)
                        )

                        for (dot in dotCoords) {
                            drawRect(
                                color = Color.Black,
                                topLeft = Offset(dot.first * cellSize, dot.second * cellSize),
                                size = Size(cellSize, cellSize)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = passport.petName,
                    color = Color(0xFF0F172A),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Microchip: ${passport.microchipId}",
                    color = Color(0xFF64748B),
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "Scan with any smartphone camera to verify immunization records without app login.",
                    color = Color(0xFF64748B),
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                )

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onShare,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Share", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = onOpenPortal,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F172A)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(text = "Open Web View", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun AddVaccineDialog(
    onDismiss: () -> Unit,
    onSave: (String, String, String, String, String, String, String) -> Unit
) {
    var vaccineName by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Core Canine") }
    var batchLot by remember { mutableStateOf("LOT-2026-X8") }
    var clinic by remember { mutableStateOf("Anna Nagar Pet Hospital") }
    var doctor by remember { mutableStateOf("Dr. Priya Sharma, BVSc") }
    var adminDate by remember { mutableStateOf("Today") }
    var expiryDate by remember { mutableStateOf("1 Year Valid") }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1E293B), RoundedCornerShape(20.dp))
                .border(1.dp, Color(0xFF334155), RoundedCornerShape(20.dp))
                .padding(20.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Log Immunization Booster",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = null, tint = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = vaccineName,
                    onValueChange = { vaccineName = it },
                    label = { Text("Vaccine Name (e.g. Rabies Booster)", color = Color(0xFF94A3B8), fontSize = 12.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Primary,
                        unfocusedBorderColor = Color(0xFF475569)
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = batchLot,
                        onValueChange = { batchLot = it },
                        label = { Text("Batch / Lot #", color = Color(0xFF94A3B8), fontSize = 11.sp) },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Primary,
                            unfocusedBorderColor = Color(0xFF475569)
                        )
                    )
                    OutlinedTextField(
                        value = expiryDate,
                        onValueChange = { expiryDate = it },
                        label = { Text("Expiry / Valid Till", color = Color(0xFF94A3B8), fontSize = 11.sp) },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Primary,
                            unfocusedBorderColor = Color(0xFF475569)
                        )
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = clinic,
                    onValueChange = { clinic = it },
                    label = { Text("Veterinary Clinic", color = Color(0xFF94A3B8), fontSize = 12.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Primary,
                        unfocusedBorderColor = Color(0xFF475569)
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (vaccineName.isNotBlank()) {
                            onSave(vaccineName, category, batchLot, clinic, doctor, adminDate, expiryDate)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Save & Sync to Health Passport",
                        color = Color.Black,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
