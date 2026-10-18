package com.pawlife.app.feature.ai.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.foundation.border
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.animation.core.*
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.pawlife.app.core.data.db.entity.PetEntity
import com.pawlife.app.presentation.theme.*
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.PickVisualMediaRequest
import android.graphics.ImageDecoder
import android.os.Build
import android.provider.MediaStore
import androidx.compose.ui.platform.LocalContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AIAnalyzerScreen(
    onNavigateToSimulator: () -> Unit = {},
    onNavigateToScanner: () -> Unit = {},
    viewModel: AIAnalyzerViewModel = hiltViewModel()
) {
    val chatHistory by viewModel.chatHistory.collectAsState()
    val isAnalyzing by viewModel.isAnalyzing.collectAsState()
    val selectedPet by viewModel.selectedPet.collectAsState()
    var inputText by remember { mutableStateOf("") }
    
    val context = LocalContext.current
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null) {
                val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, uri)).copy(android.graphics.Bitmap.Config.ARGB_8888, true)
                } else {
                    @Suppress("DEPRECATION")
                    MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                }
                viewModel.analyzeImage(bitmap)
            }
        }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier.size(32.dp).background(Primary, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            if (selectedPet?.photoUri != null) {
                                AsyncImage(
                                    model = selectedPet!!.photoUri,
                                    contentDescription = "Pet",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Icon(Icons.Default.AutoAwesome, contentDescription = "AI", tint = Color.White, modifier = Modifier.size(16.dp))
                            }                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("PawLife AI", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToScanner) {
                        Icon(Icons.Default.CameraAlt, contentDescription = "Scanner", tint = Secondary)
                    }
                    IconButton(onClick = onNavigateToSimulator) {
                        Icon(Icons.Default.Analytics, contentDescription = "Simulator")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundDark)
            )
        },
        containerColor = BackgroundDark
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).imePadding()) {
            if (chatHistory.isEmpty()) {
                Box(modifier = Modifier.weight(1f)) {
                    EmptyChatState(
                        pet = selectedPet,
                        onSuggestionClick = { 
                            viewModel.analyzeSymptoms(it) 
                        }
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f).padding(horizontal = 16.dp),
                    reverseLayout = true
                ) {
                    if (isAnalyzing) {
                        item {
                            TypingIndicator()
                        }
                    }
                    items(chatHistory.reversed()) { msg ->
                        ChatBubble(
                            text = msg.text, 
                            isUser = msg.isUser,
                            urgency = msg.urgency,
                            onExportPdf = if (!msg.isUser && msg.text.length > 40) {
                                {
                                    com.pawlife.app.core.util.VetReportGenerator.generateAndPrintReport(
                                        context = context,
                                        pet = selectedPet,
                                        diagnosisText = msg.text,
                                        urgency = msg.urgency ?: "MODERATE"
                                    )
                                }
                            } else null
                        )
                    }
                }
            }

            FloatingChatInputBar(
                inputText = inputText,
                onTextChange = { inputText = it },
                onSend = {
                    if (inputText.isNotBlank()) {
                        viewModel.analyzeSymptoms(inputText)
                        inputText = ""
                    }
                },
                onImageClick = {
                    photoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                }
            )
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun EmptyChatState(pet: PetEntity?, onSuggestionClick: (String) -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (pet?.photoUri != null) {
            AsyncImage(
                model = pet.photoUri,
                contentDescription = "Pet Image",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .border(2.dp, Primary, CircleShape)
            )
        } else {
            Icon(Icons.Default.Pets, contentDescription = "AI", tint = SlateSubtle, modifier = Modifier.size(64.dp))
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text("How can I help your pet today?", style = MaterialTheme.typography.headlineMedium, color = Color.White)
        Spacer(modifier = Modifier.height(32.dp))
        
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            SuggestionChip("My dog has been coughing...") { onSuggestionClick(it) }
            SuggestionChip("What should a Golden Retriever eat?") { onSuggestionClick(it) }
            SuggestionChip("Is chocolate dangerous for cats?") { onSuggestionClick(it) }
        }
    }
}

@Composable
fun SuggestionChip(text: String, onClick: (String) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().height(48.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardDark),
        onClick = { onClick(text) }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp).fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Primary, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text(text, style = MaterialTheme.typography.bodyMedium, color = SlateLight)
        }
    }
}

@Composable
fun ChatBubble(
    text: String, 
    isUser: Boolean,
    urgency: String? = null,
    onExportPdf: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom
    ) {
        if (!isUser) {
            Box(
                modifier = Modifier.size(28.dp).background(Primary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.AutoAwesome, contentDescription = "AI", tint = Color.White, modifier = Modifier.size(14.dp))
            }
            Spacer(modifier = Modifier.width(8.dp))
        }
        
        Column(
            modifier = Modifier.widthIn(max = 300.dp),
            horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
        ) {
            if (!isUser && urgency != null && text.length > 40) {
                val badgeColor = when (urgency.uppercase()) {
                    "EMERGENCY" -> Color(0xFFEF4444)
                    "MODERATE" -> Color(0xFFF59E0B)
                    else -> Color(0xFF10B981)
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = badgeColor.copy(alpha = 0.15f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, badgeColor.copy(alpha = 0.4f)),
                    modifier = Modifier.padding(bottom = 6.dp)
                ) {
                    Text(
                        text = "● TRIAGE: $urgency",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = badgeColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Box(
                modifier = Modifier
                    .background(
                        if (isUser) Primary else CardDark,
                        RoundedCornerShape(
                            topStart = 20.dp,
                            topEnd = 20.dp,
                            bottomStart = if (isUser) 20.dp else 4.dp,
                            bottomEnd = if (isUser) 4.dp else 20.dp
                        )
                    )
                    .premiumShadow(if (isUser) Primary else Color.Black, if (isUser) 0.3f else 0.1f, 20.dp, 8.dp)
                    .padding(16.dp)
            ) {
                Text(text, style = MaterialTheme.typography.bodyLarge, color = if (isUser) Color.White else SlateLight)
            }

            if (!isUser && text.length > 50 && onExportPdf != null) {
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = onExportPdf,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Primary),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Primary.copy(alpha = 0.5f)),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.PictureAsPdf, contentDescription = "PDF", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Export Clinical Vet PDF", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun TypingIndicator() {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(28.dp).background(Primary, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.AutoAwesome, contentDescription = "AI", tint = Color.White, modifier = Modifier.size(14.dp))
        }
        Spacer(modifier = Modifier.width(8.dp))
        Box(
            modifier = Modifier
                .background(CardDark.copy(alpha = alpha), RoundedCornerShape(20.dp))
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Text("...", style = MaterialTheme.typography.bodyLarge, color = SlateSubtle)
        }
    }
}

@Composable
fun FloatingChatInputBar(
    inputText: String, 
    onTextChange: (String) -> Unit, 
    onSend: () -> Unit,
    onImageClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .background(CardDark, RoundedCornerShape(32.dp))
            .premiumShadow(Color.Black, 0.2f, 32.dp, 16.dp, offsetY = 8.dp)
            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(32.dp))
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onImageClick) {
            Icon(Icons.Default.Image, contentDescription = "Upload", tint = SlateSubtle)
        }
        
        OutlinedTextField(
            value = inputText,
            onValueChange = onTextChange,
            placeholder = { Text("Ask anything...", color = SlateSubtle) },
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(24.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent,
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedTextColor = SlateLight,
                unfocusedTextColor = SlateLight
            )
        )
        
        Spacer(modifier = Modifier.width(4.dp))
        
        if (inputText.isNotBlank()) {
            IconButton(
                onClick = onSend,
                modifier = Modifier
                    .background(Primary, CircleShape)
                    .bounceClick { onSend() }
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = Color.White, modifier = Modifier.size(20.dp))
            }
        } else {
            IconButton(onClick = { /* TODO Mic */ }) {
                Icon(Icons.Default.Mic, contentDescription = "Microphone", tint = SlateSubtle)
            }
        }
    }
}
