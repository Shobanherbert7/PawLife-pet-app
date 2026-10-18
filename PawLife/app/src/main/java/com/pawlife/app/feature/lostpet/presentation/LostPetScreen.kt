package com.pawlife.app.feature.lostpet.presentation

import android.view.View
import android.widget.FrameLayout
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import com.pawlife.app.core.data.db.entity.PetEntity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pawlife.app.core.domain.repository.PetRepository
import com.pawlife.app.presentation.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LostPetScreen(
    petId: Long = 1L,
    viewModel: FlyerGeneratorViewModel = androidx.hilt.navigation.compose.hiltViewModel()
) {
    val context = LocalContext.current
    var contactPhone by remember { mutableStateOf("+91 98401 23456") }
    var reward by remember { mutableStateOf("₹5,000") }
    
    val pet by viewModel.getPet(petId).collectAsStateWithLifecycle(null)
    val allPets by viewModel.allPets.collectAsStateWithLifecycle(emptyList())

    val activePet = pet ?: allPets.firstOrNull() ?: PetEntity(
        id = 1L,
        name = "Max",
        species = "Dog",
        breed = "Golden Retriever",
        dateOfBirth = System.currentTimeMillis() - 94608000000L,
        gender = "Male",
        weight = 25f,
        photoUri = null,
        microchipId = "985141002348123",
        adoptionDate = null
    )
    
    // Create an invisible ComposeView that renders our 1080x1440 flyer exactly.
    val flyerComposeView = remember {
        ComposeView(context).apply {
            layoutParams = FrameLayout.LayoutParams(1080, 1440)
        }
    }

    LaunchedEffect(activePet, contactPhone, reward) {
        flyerComposeView.setContent {
            MaterialTheme {
                LostPetFlyerContent(
                    name = activePet.name,
                    breed = activePet.breed,
                    species = activePet.species,
                    contactInfo = contactPhone,
                    reward = reward,
                    photoUri = activePet.photoUri
                )
            }
        }
        flyerComposeView.measure(
            View.MeasureSpec.makeMeasureSpec(1080, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(1440, View.MeasureSpec.EXACTLY)
        )
        flyerComposeView.layout(0, 0, 1080, 1440)
    }

    Scaffold(
        containerColor = BackgroundDark,
        topBar = {
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 20.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("Emergency: Lost Pet", style = MaterialTheme.typography.headlineMedium, color = Warning, fontWeight = FontWeight.Bold)
            }
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    val bitmap = viewModel.generateFlyerBitmap(flyerComposeView)
                    viewModel.shareFlyerBitmap(context, bitmap)
                },
                containerColor = Warning,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Share, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Broadcast to Socials")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
                .verticalScroll(androidx.compose.foundation.rememberScrollState())
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Warning.copy(alpha = 0.15f)),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("⚠️ Emergency Protocol Activated", color = Warning, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "1. Generate & broadcast your flyer to WhatsApp/Instagram.\n2. Contact local veterinarians with Microchip #${activePet.microchipId ?: "985141002348123"}.\n3. Check nearby smart collars within BLE range.",
                        color = Color.White.copy(alpha = 0.9f),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Text("Flyer Customization for ${activePet.name}", color = SlateLight, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = contactPhone,
                onValueChange = { contactPhone = it },
                label = { Text("Emergency Contact Phone", color = SlateSubtle) },
                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = SlateLight),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = reward,
                onValueChange = { reward = it },
                label = { Text("Reward Amount", color = SlateSubtle) },
                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = SlateLight),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(24.dp))
            
            // Preview
            Text("Digital Flyer Preview:", color = SlateSubtle, style = MaterialTheme.typography.labelLarge)
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1080f / 1440f)
                    .clip(androidx.compose.foundation.shape.RoundedCornerShape(16.dp))
                    .padding(bottom = 100.dp)
            ) {
                LostPetFlyerContent(
                    name = activePet.name,
                    breed = activePet.breed,
                    species = activePet.species,
                    contactInfo = contactPhone,
                    reward = reward,
                    photoUri = activePet.photoUri
                )
            }
        }
    }
}

@Composable
fun LostPetFlyerContent(
    name: String,
    breed: String,
    species: String,
    contactInfo: String,
    reward: String?,
    photoUri: String? = null
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFF3E0))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("🚨 LOST PET 🚨",
            style = MaterialTheme.typography.displayMedium,
            color = Color(0xFFD32F2F),
            fontWeight = FontWeight.ExtraBold
        )
        Spacer(modifier = Modifier.height(32.dp))
        
        if (photoUri != null) {
            coil.compose.AsyncImage(
                model = photoUri,
                contentDescription = name,
                modifier = Modifier
                    .size(240.dp)
                    .clip(CircleShape)
                    .border(4.dp, Color(0xFFD32F2F), CircleShape),
                contentScale = androidx.compose.ui.layout.ContentScale.Crop
            )
        } else {
            Box(
                modifier = Modifier
                    .size(240.dp)
                    .background(Color.Gray, CircleShape)
                    .border(4.dp, Color(0xFFD32F2F), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(name.take(1).uppercase(), style = MaterialTheme.typography.displayLarge, color = Color.White)
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
        Text(name, style = MaterialTheme.typography.displayLarge, color = Color.Black, fontWeight = FontWeight.Bold)
        Text("$breed · $species", style = MaterialTheme.typography.headlineLarge, color = Color.DarkGray)
        Spacer(modifier = Modifier.height(24.dp))
        if (reward?.isNotEmpty() == true) {
            Text("REWARD: $reward", style = MaterialTheme.typography.displaySmall, color = Color(0xFFE65100), fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text("Please Contact:", style = MaterialTheme.typography.headlineMedium, color = Color.Black)
        Text("📞 $contactInfo", style = MaterialTheme.typography.displaySmall, color = Color.Black, fontWeight = FontWeight.Bold)
    }
}
