package com.pawlife.app.feature.home.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pawlife.app.core.data.db.entity.PetEntity
import com.pawlife.app.presentation.components.EmptyState
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import android.widget.Toast
import com.pawlife.app.presentation.theme.*

@Composable
fun HomeScreen(
    onNavigateToAI: () -> Unit = {}, 
    onNavigateToPetProfile: (String) -> Unit = {}, 
    onNavigateToNotifications: () -> Unit = {}, 
    onNavigateToLostPet: () -> Unit = {}, 
    onNavigateToVetBooking: () -> Unit = {},
    onNavigateToAR: () -> Unit = {},
    onNavigateToScanner: (Long) -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onNavigateToMarketplace: () -> Unit = {},
    onNavigateToCollar: () -> Unit = {},
    onNavigateToActiveWalk: (Long) -> Unit = {},
    onNavigateToOnboarding: () -> Unit = {},
    onNavigateToFoodScanner: (Long) -> Unit = {},
    onNavigateToNearbyPlaces: () -> Unit = {},
    onNavigateToPassport: (Long) -> Unit = {},
    onNavigateToCommunity: () -> Unit = {},
    onNavigateToAcoustics: () -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel()
) {
    val pets by viewModel.pets.collectAsStateWithLifecycle()
    val pagerState = androidx.compose.foundation.pager.rememberPagerState(pageCount = { pets.size })
    val currentPetId = pets.getOrNull(pagerState.currentPage)?.id ?: -1L
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        contentPadding = PaddingValues(top = 48.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item {
            HeaderSection(onNavigateToSettings, onNavigateToNotifications)
        }
        
        item {
            if (pets.isEmpty()) {
                EmptyState(
                    title = "No Pets Yet",
                    subtitle = "Add your first pet to start tracking their health, activities, and memories.",
                    icon = Icons.Default.Pets,
                    buttonText = "Set Up Profile",
                    onButtonClick = { onNavigateToOnboarding() }
                )
            } else {
                androidx.compose.foundation.pager.HorizontalPager(
                    state = pagerState,
                    contentPadding = PaddingValues(horizontal = 0.dp),
                    pageSpacing = 16.dp,
                    modifier = Modifier.fillMaxWidth()
                ) { page ->
                    val pet = pets[page]
                    PetDashboardCard(pet = pet, onClick = { onNavigateToPetProfile(pet.id.toString()) })
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                DashboardMetricCard(
                    title = "Today's Walk", 
                    value = "2.4 km", 
                    icon = Icons.AutoMirrored.Filled.DirectionsWalk, 
                    color = Secondary, 
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigateToActiveWalk(if (currentPetId == -1L) 1L else currentPetId) }
                )
                DashboardMetricCard(
                    title = "Vaccine Pass", 
                    value = "Certified", 
                    icon = Icons.Default.MedicalServices, 
                    color = Warning, 
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigateToPassport(if (currentPetId == -1L) 1L else currentPetId) }
                )
            }
        }
        
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .bounceClick { onNavigateToMarketplace() }
                    .premiumShadow(color = Warning, alpha = 0.12f, shadowBlurRadius = 24.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = CardDark)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                        Box(
                            modifier = Modifier.size(44.dp).background(Warning.copy(alpha = 0.15f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.ShoppingBag, contentDescription = "Pantry", tint = Warning, modifier = Modifier.size(22.dp))
                        }
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("Smart Pantry Alert", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = SlateLight)
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Warning.copy(alpha = 0.2f)
                                ) {
                                    Text("18% Low", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Warning, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                }
                            }
                            Text("Royal Canin Kibble (~3 days left)", style = MaterialTheme.typography.labelMedium, color = SlateSubtle)
                        }
                    }

                    Button(
                        onClick = onNavigateToMarketplace,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Warning, contentColor = Color.Black),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text("Reorder", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        
        item {
            Text("Quick Actions", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = SlateLight)
            Spacer(modifier = Modifier.height(12.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                item { ActionPill("Passport", Icons.Default.MedicalServices, Warning) {
                    onNavigateToPassport(if (currentPetId == -1L) 1L else currentPetId)
                } }
                item { ActionPill("AI Food", Icons.Default.CameraAlt, Primary) {
                    onNavigateToFoodScanner(if (currentPetId == -1L) 1L else currentPetId)
                } }
                item { ActionPill("Parks & Vets", Icons.Default.Place, Secondary, onNavigateToNearbyPlaces) }
                item { ActionPill("Acoustics", Icons.Default.GraphicEq, Secondary, onNavigateToAcoustics) }
                item { ActionPill("BarkConnect", Icons.Default.Park, Primary, onNavigateToCommunity) }
                item { ActionPill("GPS Walk", Icons.AutoMirrored.Filled.DirectionsWalk, Secondary) {
                    onNavigateToActiveWalk(if (currentPetId == -1L) 1L else currentPetId)
                } }
                item { ActionPill("Collar", Icons.Default.Bluetooth, Primary, onNavigateToCollar) }
                item { ActionPill("Log Meal", Icons.Default.QrCodeScanner, Primary) { 
                    if (currentPetId == -1L) {
                        Toast.makeText(context, "Please add a pet first to log a meal.", Toast.LENGTH_SHORT).show()
                    } else {
                        onNavigateToScanner(currentPetId)
                    }
                } }
                item { ActionPill("AI Scan", Icons.Default.AutoAwesome, Primary, onNavigateToAI) }
                item { ActionPill("Market", Icons.Default.Storefront, Warning, onNavigateToMarketplace) }
                item { ActionPill("AR Agility", Icons.Default.ViewInAr, Warning, onNavigateToAR) }
                item { ActionPill("Vet", Icons.Default.LocalHospital, Success, onNavigateToVetBooking) }
            }
        }
        
        item {
            RecentActivitySection(currentPetId, viewModel)
        }
    }
}

@Composable
fun HeaderSection(onSettingsClick: () -> Unit, onNotificationsClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text("👋 Good Morning,", style = MaterialTheme.typography.bodyLarge, color = SlateSubtle)
            Text("Shoban", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold, color = SlateLight)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            IconButton(onClick = onNotificationsClick, modifier = Modifier.background(CardDark, CircleShape).bounceClick { onNotificationsClick() }) {
                Icon(Icons.Default.Notifications, contentDescription = "Notifications", tint = SlateLight)
            }
            IconButton(onClick = onSettingsClick, modifier = Modifier.background(CardDark, CircleShape).bounceClick { onSettingsClick() }) {
                Icon(Icons.Default.Settings, contentDescription = "Settings", tint = SlateLight)
            }
        }
    }
}

@Composable
fun PetDashboardCard(pet: PetEntity, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .bounceClick { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = CardDark)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Primary.copy(alpha = 0.5f), CardDark),
                            startY = 0f,
                            endY = 500f
                        )
                    )
            )
            
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("${pet.name} is Happy 😊", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = SlateLight)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("${pet.breed} • ${pet.weight} kg", style = MaterialTheme.typography.bodyMedium, color = SlateSubtle)
                    }
                    
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(80.dp)
                            .background(Success.copy(alpha = 0.2f), CircleShape)
                    ) {
                        CircularProgressIndicator(
                            progress = { 0.94f },
                            modifier = Modifier.fillMaxSize(),
                            color = Success,
                            trackColor = Color.Transparent,
                            strokeWidth = 6.dp
                        )
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("94%", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Success)
                            Text("Health", style = MaterialTheme.typography.labelSmall, color = Success)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DashboardMetricCard(
    title: String, 
    value: String, 
    icon: ImageVector, 
    color: Color, 
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = modifier
            .height(100.dp)
            .bounceClick { onClick() }
            .premiumShadow(color = color, alpha = 0.1f, shadowBlurRadius = 24.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CardDark)
    ) {
        Column(
            modifier = Modifier.padding(16.dp).fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Icon(icon, contentDescription = title, tint = color)
                Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = SlateLight)
            }
            Text(title, style = MaterialTheme.typography.labelMedium, color = SlateSubtle)
        }
    }
}

@Composable
fun ActionPill(title: String, icon: ImageVector, color: Color, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .bounceClick { onClick() }
            .background(CardDark, RoundedCornerShape(16.dp))
            .border(1.dp, color.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = title, tint = color, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(title, style = MaterialTheme.typography.labelLarge, color = SlateLight, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun RecentActivitySection(currentPetId: Long, viewModel: HomeViewModel) {
    val activities by viewModel.getRecentActivities(currentPetId).collectAsStateWithLifecycle(emptyList())

    Text("Recent Activity", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = SlateLight)
    Spacer(modifier = Modifier.height(12.dp))
    
    if (activities.isEmpty()) {
        Text("No recent activity yet.", color = SlateSubtle, style = MaterialTheme.typography.bodyMedium)
    } else {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            activities.take(5).forEach { activity ->
                ActivityRow(
                    title = activity.title,
                    subtitle = activity.subtitle,
                    time = formatTime(activity.timestampMs),
                    icon = activity.icon,
                    color = activity.color
                )
            }
        }
    }
}

private fun formatTime(timestampMs: Long): String {
    val format = java.text.SimpleDateFormat("MMM dd, h:mm a", java.util.Locale.getDefault())
    return format.format(java.util.Date(timestampMs))
}

@Composable
fun ActivityRow(title: String, subtitle: String, time: String, icon: ImageVector, color: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(CardDark, RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(color.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = title, tint = color, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = SlateLight)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = SlateSubtle)
            }
        }
        Text(time, style = MaterialTheme.typography.labelSmall, color = SlateSubtle)
    }
}
